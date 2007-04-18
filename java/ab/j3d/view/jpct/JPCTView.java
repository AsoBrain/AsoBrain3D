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
	 * Scene input translator for this View.
	 */
	private final ControlInput _controlInput;

	/**
	 * Model being viewed.
	 */
	private JPCTModel _model;

	/**
	 * Background color of the view.
	 */
	private Color _backgroundColor;

	/**
	 * Whether the view needs to be updated to model changes.
	 */
	private boolean _updateNeeded;

	/**
	 * UI component to present view to user.
	 */
	private class ViewComponent
		extends JComponent
	{
		/**
		 * Insets cache.
		 */
		private Insets _insets;

		/**
		 * Frame buffer displayed by the component.
		 */
		private FrameBuffer _frameBuffer;

		private ViewStrategy _strategy;

		/**
		 * Construct view component.
		 */
		private ViewComponent()
		{
			_strategy    = null;
			_insets      = null;
			_frameBuffer = null;

			final Dimension size = new Dimension( MINIMUM_IMAGE_SIZE, MINIMUM_IMAGE_SIZE );
			setMinimumSize( size );
			setPreferredSize( size );

//			addMouseListener( new MouseListener);
		}

		public void setStrategy( final ViewStrategy strategy )
		{
			final ViewStrategy oldStrategy = _strategy;
			if ( oldStrategy != strategy )
			{
				if ( oldStrategy != null )
				{
					oldStrategy.uninstall( this );
				}
				_strategy = strategy;
				strategy.install( this );
			}
		}

		/**
		 * Returns the frame buffer displayed by the view component.
		 *
		 * @return  Frame buffer.
		 */
		private FrameBuffer getFrameBuffer()
		{
			FrameBuffer result = null;

			if ( _strategy != null )
			{
				final Dimension size = getSize();

				result = _frameBuffer;

				if ( ( result == null ) ||
					 ( result.getOutputWidth()  != size.width  ) ||
					 ( result.getOutputHeight() != size.height ) )
				{
					if ( result != null )
					{
						result.dispose();
					}

					if ( ( size.width > 0 ) && ( size.height > 0 ) )
					{
						result = new FrameBuffer( size.width , size.height , FrameBuffer.SAMPLINGMODE_HARDWARE_ONLY );
						_frameBuffer = result;
						_strategy.frameBufferReplaced( this , result );
					}
				}
			}

			return result;
		}

		/**
		 * Notifies the component that the contents of the frame buffer was
		 * modified.
		 *
		 * @param   frameBuffer     Frame buffer.
		 */
		public void frameBufferUpdated( final FrameBuffer frameBuffer )
		{
			_strategy.frameBufferUpdated( this , frameBuffer );
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

		public void paint( final Graphics g )
		{
			JPCTView.this.update();
		}

		public void update( final Graphics g )
		{
			JPCTView.this.update();
		}
	}

	private interface ViewStrategy
	{
		/**
		 * Performs implementation-specific clean-up and initialization needed
		 * as a result fo replacing the frame buffer.
		 *
		 * @param   component   View component.
		 * @param   newBuffer   New frame buffer.
		 */
		void frameBufferReplaced( final ViewComponent component , final FrameBuffer newBuffer );

		/**
		 * Notifies the strategy that the contents of the frame buffer was
		 * modified.
		 *
		 * @param   component   View component.
		 * @param   frameBuffer     Frame buffer.
		 */
		void frameBufferUpdated( final ViewComponent component , final FrameBuffer frameBuffer );

		void install( final ViewComponent component );

		void uninstall( final ViewComponent component );
	}

	/**
	 * View component UI implementation for the (lightweight) software renderer.
	 */
	private class SoftwareViewStrategy
		implements ViewStrategy
	{
		public void frameBufferReplaced( final ViewComponent component , final FrameBuffer newBuffer )
		{
			newBuffer.enableRenderer( IRenderer.RENDERER_SOFTWARE , IRenderer.MODE_OPENGL );
		}

		public void frameBufferUpdated( final ViewComponent viewComponent , final FrameBuffer frameBuffer )
		{
			final Graphics graphics = viewComponent.getGraphics();
			frameBuffer.display( graphics );
			graphics.dispose();
		}

		public void install( final ViewComponent viewComponent )
		{
		}

		public void uninstall( final ViewComponent viewComponent )
		{
		}
	}

	/**
	 * View component implementation for the (heavyweight) hardware renderer.
	 */
	private class HardwareViewStrategy
		implements ViewStrategy
	{
		/**
		 * Canvas used for hardware-accelerated rendering.
		 */
		private Canvas _canvas = null;

		public void frameBufferReplaced( final ViewComponent component , final FrameBuffer newBuffer )
		{
			newBuffer.enableRenderer( IRenderer.RENDERER_OPENGL , IRenderer.MODE_OPENGL );
			setCanvas( component , newBuffer.enableGLCanvasRenderer( IRenderer.MODE_OPENGL ) );
			newBuffer.disableRenderer( IRenderer.RENDERER_SOFTWARE );
		}

		public void frameBufferUpdated( final ViewComponent component , final FrameBuffer frameBuffer )
		{
			frameBuffer.displayGLOnly();
			final Graphics graphics = _canvas.getGraphics();
			_canvas.update( graphics );
			graphics.dispose();
		}

		/**
		 * Sets the component's canvas and configures it to fire mouse events
		 * to the component's listeners.
		 *
		 * @param   component   View component.
		 * @param   canvas      Canvas to be set.
		 */
		private void setCanvas( final ViewComponent component , final Canvas canvas )
		{
			component.removeAll();
			if ( canvas != null )
			{
				component.add( canvas );
				component.revalidate();

				final MouseListener[] mouseListeners = component.getMouseListeners();
				for ( MouseListener listener : mouseListeners )
				{
					canvas.addMouseListener( listener );
				}

				final MouseMotionListener[] mouseMotionListeners = component.getMouseMotionListeners();
				for ( MouseMotionListener listener : mouseMotionListeners )
				{
					canvas.addMouseMotionListener( listener );
				}
			}
			_canvas = canvas;
		}

		public void install( final ViewComponent component )
		{
			component.setLayout( new BorderLayout() );
		}

		public void uninstall( final ViewComponent component )
		{
			component.remove( _canvas );
			component.revalidate();
		}
	}

	/**
	 * Construct new jPCT-based view.
	 */
	public JPCTView( final JPCTModel model , final Object id , final Color backgroundColor )
	{
		super( model.getUnit() , id );
		_backgroundColor = backgroundColor;

		_model = model;

		_projectionPolicy = Projector.PERSPECTIVE;
		_renderingPolicy  = SOLID;

		Config.glColorDepth    = 24;    // @FIXME: set this on Linux only
		Config.useLocking      = true;
		Config.fadeoutLight    = true;
		Config.specPow         = 60.0f;
		Config.specTerm        = 30.0f;
		Config.useFastSpecular = false;

		_viewComponent = createViewComponent();
		_controlInput  = new ViewControlInput( model , this );

		_updateNeeded = true;
		startRenderer();
	}

	/**
	 * Returns an appropriate view component, based on whether hardware
	 * acceleration is available.
	 *
	 * @return  View component.
	 */
	private ViewComponent createViewComponent()
	{
		boolean useHardware = false;
		try
		{
			System.loadLibrary( "lwjgl" );
			useHardware = true;
		}
		catch ( UnsatisfiedLinkError e )
		{
			/** Hardware OpenGL renderer is not available. */
		}
		catch ( SecurityException e )
		{
			/** Hardware OpenGL renderer is not available. */
		}

		final ViewComponent result = new ViewComponent();
		result.setStrategy( useHardware ? new HardwareViewStrategy() : new SoftwareViewStrategy() );
		return result;
	}

	private void startRenderer()
	{
		final Thread renderThread = new Thread( new Renderer() , "JPCTView.renderThread:" + getID() );
		renderThread.setPriority( Thread.NORM_PRIORITY );
		renderThread.start();
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
		synchronized ( this )
		{
			_updateNeeded = true;
			notifyAll();
		}
	}

	/**
	 * Updates the camera.
	 *
	 * NOTE: must only be invoked from renderer thread.
 	 */
	private void updateCamera()
	{
		final Matrix3D viewTransform = getViewTransform();

		final Matrix cameraTransform = new Matrix();
		cameraTransform.setDump( new float[]
			{
				(float)viewTransform.xx , (float)-viewTransform.yx , (float)-viewTransform.zx , 0.0f ,
				(float)viewTransform.xy , (float)-viewTransform.yy , (float)-viewTransform.zy , 0.0f ,
				(float)viewTransform.xz , (float)-viewTransform.yz , (float)-viewTransform.zz , 0.0f ,
				(float)viewTransform.xo , (float)-viewTransform.yo , (float)-viewTransform.zo , 1.0f
			} );

		final World world = getWorld();

		final Camera camera = world.getCamera();
		camera.setFOV( camera.convertRADAngleIntoFOV( (float)getAperture() ) );
		camera.setPosition( SimpleVector.ORIGIN );
		camera.setBack( cameraTransform );

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
		if ( _renderingPolicy != policy )
		{
			_renderingPolicy = policy;
		}
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

	/**
	 * Render loop for the view.
	 */
	private class Renderer
		implements Runnable
	{
		public void run()
		{
			long nextCheck    = System.currentTimeMillis() + 1000L;
			int  frameCounter = 0;
			int  fps;

			final ViewComponent viewComponent = _viewComponent;

			final World world = getWorld();

			updateCamera();
			while ( true )
			{
				/*
				 * Pause the renderer unless the are changes.
				 */
				try
				{
					synchronized ( JPCTView.this )
					{
						while ( !_updateNeeded )
						{
							JPCTView.this.wait();
						}
						_updateNeeded = false;
					}
				}
				catch ( InterruptedException e )
				{
					break;
				}

				final FrameBuffer frameBuffer = viewComponent.getFrameBuffer();

				if ( frameBuffer != null )
				{
					_model.updateWorld();

					updateCamera();
					world.buildAllObjects();

					frameBuffer.clear( _backgroundColor );

					world.renderScene( frameBuffer );
					if ( _renderingPolicy == WIREFRAME )
					{
						world.drawWireframe( frameBuffer , Color.BLACK );
					}
					else
					{
						world.draw( frameBuffer );
					}

					frameBuffer.update();
					viewComponent.frameBufferUpdated( frameBuffer );

					frameCounter ++;

					/*
					 * Calculate frames per second.
 					 */
					if ( System.currentTimeMillis() >= nextCheck )
					{
						nextCheck = System.currentTimeMillis() + 1000L;
						fps = frameCounter;
						frameCounter = 0;

						System.out.print( "fps = " );
						System.out.println( fps );
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
