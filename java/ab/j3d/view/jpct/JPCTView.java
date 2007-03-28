/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * ====================================================================
 */
package ab.j3d.view.jpct;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.JComponent;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.Matrix;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import ab.j3d.Matrix3D;
import ab.j3d.control.ControlInput;
import ab.j3d.view.Projector;
import ab.j3d.view.ViewControlInput;
import ab.j3d.view.ViewModelView;

/**
 * jPCT implementation of view model view.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JPCTView
	extends ViewModelView
{
	/**
	 * Practical minimum size of images in dialog.
	 */
	private static final int MINIMUM_IMAGE_SIZE = 150;

	/**
	 * Component through which a rendering of the view is shown.
	 */
	private final ViewComponent _viewComponent;

	/**
	 * Projection policy of this view.
	 *
	 * @see     #setProjectionPolicy
	 */
	private int _projectionPolicy;

	/**
	 * Rendering policy of this view.
	 *
	 * @see     #setRenderingPolicy
	 */
	private int _renderingPolicy;

	/**
	 * The SceneInputTranslator for this View.
	 */
	private final ControlInput _controlInput;

	private JPCTModel _model;

	private boolean _stopRenderer = false;

	private Color _backgroundColor;

	private boolean _updateNeeded;

	/**
	 * UI component to present view to user.
	 */
	private final class ViewComponent
		extends JComponent
	{
		/**
		 * Insets cache.
		 */
		private Insets _insets;

		/**
		 * Canvas used for hardware-accelerated rendering.
		 */
		private Canvas _canvas;

		/**
		 * Construct view component.
		 */
		private ViewComponent()
		{
			_insets = null;
			_canvas = null;

			setLayout( new BorderLayout() );
		}

		public void setCanvas( final Canvas canvas )
		{
			removeAll();
			if ( canvas != null )
			{
				add( canvas );
				revalidate();

				final MouseListener[] mouseListeners = getMouseListeners();
				for ( int i = 0 ; i < mouseListeners.length ; i++ )
				{
					canvas.addMouseListener( mouseListeners[ i ] );
				}

				final MouseMotionListener[] mouseMotionListeners = getMouseMotionListeners();
				for ( int i = 0 ; i < mouseMotionListeners.length ; i++ )
				{
					canvas.addMouseMotionListener( mouseMotionListeners[ i ] );
				}
			}
			_canvas = canvas;
		}

		public void paint( final Graphics g )
		{
			super.paint( g );

			final Canvas canvas = _canvas;
			if ( canvas != null )
			{
				canvas.repaint();
			}
		}

		public Dimension getMinimumSize()
		{
			return new Dimension( MINIMUM_IMAGE_SIZE , MINIMUM_IMAGE_SIZE );
		}

		public Dimension getPreferredSize()
		{
			return new Dimension( MINIMUM_IMAGE_SIZE , MINIMUM_IMAGE_SIZE );
		}

		public Projector getProjector()
		{
			final Insets      insets            = getInsets( _insets );
			final int         imageWidth        = getWidth() - insets.left - insets.right;
			final int         imageHeight       = getHeight() - insets.top - insets.bottom;
			final double      imageResolution   = getResolution();

			final double      viewUnit          = getUnit();

			final int         projectionPolicy  = _projectionPolicy;
			final double      fieldOfView       = getAperture();
			final double      zoomFactor        = getZoomFactor();
			final double      frontClipDistance =  -0.1 / viewUnit;
			final double      backClipDistance  = -100.0 / viewUnit;

			return Projector.createInstance( projectionPolicy , imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );
		}

		public Dimension getFrameSize()
		{
			final Insets insets = getInsets( _insets );
			_insets = insets;

			final int imageWidth  = getWidth()  - insets.left - insets.right;
			final int imageHeight = getHeight() - insets.top  - insets.bottom;

			return new Dimension( imageWidth , imageHeight );
		}
	}

	/**
	 * Construct new jPCTView.
	 */
	public JPCTView( final JPCTModel model , final Object id , final Color backgroundColor )
	{
		super( model.getUnit() , id );
		_backgroundColor = backgroundColor;

		_model = model;

		_projectionPolicy = Projector.PERSPECTIVE;
		_renderingPolicy  = SOLID;

		Config.useLocking   = true;
		Config.fadeoutLight = true;

		_viewComponent = new ViewComponent();
		_controlInput  = new ViewControlInput( model , this );

		_updateNeeded = true;
		mainLoop();
	}

	private void mainLoop()
	{
		final Runnable renderer = new Renderer();
		final Thread viewThread = new Thread( renderer , "viewThread:" + getID() );
		viewThread.start();
	}

	private World getWorld()
	{
		return _model.getWorld();
	}

	public Component getComponent()
	{
		return _viewComponent;
	}

	public void update()
	{
		/*
		 * Renderer will perform an update before the next frame is rendered.
		 */
		_updateNeeded = true;
	}

	/**
	 * Updates the camera.
	 *
	 * NOTE: must only be invoked from renderer thread.
 	 */
	public void updateImpl()
	{
		final Matrix3D viewTransform = getViewTransform();

		final Matrix cameraRotation = new Matrix();
		cameraRotation.setDump( new float[]
			{
				(float)viewTransform.xx , (float)-viewTransform.yx , (float)-viewTransform.zx , 0.0f ,
				(float)viewTransform.xy , (float)-viewTransform.yy , (float)-viewTransform.zy , 0.0f ,
				(float)viewTransform.xz , (float)-viewTransform.yz , (float)-viewTransform.zz , 0.0f ,
				0.5f * (float)viewTransform.xo , 0.5f * (float)-viewTransform.yo , 0.5f * (float)-viewTransform.zo , 1.0f
			} ); // not sure why the "0.5f" works, but it does

		final World world = getWorld();

		final Camera camera = world.getCamera();
		camera.setFOV( camera.convertRADAngleIntoFOV( (float)getAperture() ) );

		final SimpleVector cameraPosition = new SimpleVector( 0.0 , 0.0 , 0.0 );
		cameraPosition.matMul( cameraRotation.invert() );

		camera.setPosition( cameraPosition );
		camera.setBack( cameraRotation );

		Config.farPlane = 10.0f / (float)_model.getUnit();

		// Build all objects in the world.
		world.buildAllObjects();
	}

	public void setProjectionPolicy( final int policy )
	{
		_projectionPolicy = policy;
	}

	public void setRenderingPolicy( final int policy )
	{
		_renderingPolicy = policy;
	}

	/**
	 * Returns the {@link Projector} for this view.
	 *
	 * @return  the {@link Projector} for this view
	 */
	public Projector getProjector()
	{
		return _viewComponent.getProjector();
	}

	protected ControlInput getControlInput()
	{
		return _controlInput;
	}

	private class Renderer
		implements Runnable
	{
		public void run()
		{
			final Thread currentThread = Thread.currentThread();
			currentThread.setPriority( Thread.NORM_PRIORITY );

			long nextCheck    = System.currentTimeMillis() + 1000L;
			int  frameCounter = 0;
			int  fps;

			final ViewComponent viewComponent = _viewComponent;

			FrameBuffer buffer = null;

			final Dimension   bufferSize = new Dimension();
			final Dimension   viewSize   = new Dimension();

			boolean renderOpenGL = false;
			try
			{
				System.loadLibrary( "lwjgl" );
				renderOpenGL = true;
			}
			catch ( UnsatisfiedLinkError e )
			{
				/** OpenGL renderer is not available. */
			}
			catch ( SecurityException e )
			{
				/** OpenGL renderer is not available. */
			}

			final World world = getWorld();

			while ( !_stopRenderer )
			{
				/*
				 * Create frame buffer (if needed) matching view size and with
				 * the appropriated renderer.
				 */
				viewComponent.getSize( viewSize );
				if ( viewSize.width * viewSize.height == 0 )
				{
					try
					{
						Thread.sleep( 10L );
					}
					catch ( InterruptedException e )
					{
					}
					continue;
				}

				if ( !viewSize.equals( bufferSize ) )
				{
					if ( buffer != null )
					{
						viewComponent.setCanvas( null );
						buffer.dispose();
					}

					//viewSize.width , viewSize.height
					Config.glColorDepth = 24;
					buffer = new FrameBuffer( viewSize.width , viewSize.height , FrameBuffer.SAMPLINGMODE_HARDWARE_ONLY );
					if ( renderOpenGL )
					{
						buffer.enableRenderer( IRenderer.RENDERER_OPENGL , IRenderer.MODE_OPENGL );
						viewComponent.setCanvas( buffer.enableGLCanvasRenderer( IRenderer.MODE_OPENGL ) );
						buffer.disableRenderer( IRenderer.RENDERER_SOFTWARE );
					}
					else
					{
						buffer.enableRenderer( IRenderer.RENDERER_SOFTWARE , IRenderer.MODE_OPENGL );
					}

					bufferSize.setSize( viewSize );
				}

				if ( _updateNeeded )
				{
					updateImpl();
					_updateNeeded = false;
				}

				// ---- Calculate fps ----
				if ( System.currentTimeMillis() >= nextCheck )
				{
					nextCheck = System.currentTimeMillis() + 1000L;
					fps = frameCounter;
					frameCounter = 0;

					System.out.println( "fps = " + fps );
				}
				frameCounter ++;
				// -----------------------

				final Graphics graphics = viewComponent.getGraphics();
				if ( graphics != null )
				{
					buffer.clear( _backgroundColor );

					/*
					 * @FIXME major problem: random exceptions in render loop
					 * These happen when the World is changed, so the problem
					 * probably lies in the implementation of the initialize/
					 * update methods of JPCTModel.
					 */
					world.renderScene( buffer );
					world.draw( buffer );

					buffer.update();
					buffer.display( graphics );
					if ( renderOpenGL )
					{
						viewComponent.repaint();
					}

					Thread.yield();
				}
			}
		}
	}

	public Action[] getActions( final Locale locale )
	{
		return new Action[ 0 ];
	}
}
