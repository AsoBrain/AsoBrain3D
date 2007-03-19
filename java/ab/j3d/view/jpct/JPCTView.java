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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.Matrix;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import ab.j3d.Matrix3D;
import ab.j3d.control.ControlInput;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.Projector;
import ab.j3d.view.ViewControlInput;
import ab.j3d.view.ViewModelView;

import com.numdata.oss.ArrayTools;

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
	 * The world that is viewed.
	 */
	private final World _world;

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

	private FrameBuffer _buffer;

	private JPCTModel _model;

	private boolean _idle = false;

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
		 * Construct view component.
		 */
		private ViewComponent()
		{
			_insets = null;
		}

		public Dimension getMinimumSize()
		{
			return new Dimension( MINIMUM_IMAGE_SIZE , MINIMUM_IMAGE_SIZE );
		}

		public Dimension getPreferredSize()
		{
			return new Dimension( MINIMUM_IMAGE_SIZE , MINIMUM_IMAGE_SIZE );
		}

		public Projector getProjector ()
		{
			final Insets      insets            = getInsets( _insets );
			final int         imageWidth        = getWidth() - insets.left - insets.right;
			final int         imageHeight       = getHeight() - insets.top - insets.bottom;
			final double      imageResolution   = getResolution();

			final double      viewUnit          = getUnit();

			final int         projectionPolicy  = _projectionPolicy;
			final double      fieldOfView       = getAperture();
			final double      zoomFactor        = getZoomFactor();
			final double      frontClipDistance =   -0.1 / viewUnit;
			final double      backClipDistance  = -100.0 / viewUnit;

			return Projector.createInstance( projectionPolicy , imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );
		}

		public void paintComponent( final Graphics g )
		{
			super.paintComponent( g );

			final int currentBufferWidth  = (int)_buffer.getMiddleX() * 2;
			final int currentBufferHeight = (int)_buffer.getMiddleY() * 2;

			final Insets insets      = getInsets( _insets );
			final int    imageWidth  = getWidth()  - insets.left - insets.right;
			final int    imageHeight = getHeight() - insets.top  - insets.bottom;

			if ( ( imageWidth != currentBufferWidth ) || ( imageHeight != currentBufferHeight ) )
			{
				_buffer = new FrameBuffer( imageWidth , imageHeight , FrameBuffer.SAMPLINGMODE_NORMAL );
				_buffer.enableRenderer( IRenderer.RENDERER_SOFTWARE, IRenderer.MODE_OPENGL );
			}

			_insets = insets;
		}
	}

	/**
	 * Construct new jPCTView.
	 */
	public JPCTView( final JPCTModel model , final Object id )
	{
		super( model.getUnit() , id );

		_model = model;

		_projectionPolicy = Projector.PERSPECTIVE;
		_renderingPolicy  = SOLID;

		_buffer = new FrameBuffer( 1024 , 768 , FrameBuffer.SAMPLINGMODE_NORMAL );
		_buffer.enableRenderer( IRenderer.RENDERER_SOFTWARE, IRenderer.MODE_OPENGL );

		_world = new World();
		_world.setAmbientLight( 200 , 200 , 200 );

		_viewComponent = new ViewComponent();
		_controlInput  = new ViewControlInput( model , this );

		update();
		mainLoop();
	}

	private void mainLoop()
	{
		final Runnable loop =new Runnable()
			{
				public void run()
				{
					final Thread currentThread = Thread.currentThread();
					currentThread.setPriority( Thread.NORM_PRIORITY );

					long nextCheck    = System.currentTimeMillis() + 1000L;
					int  frameCounter = 0;
					int  fps          = 0;
					while ( !_idle )
					{
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

						final Graphics graphics = _viewComponent.getGraphics();
						if ( graphics != null )
						{
							_buffer.clear();

							_world.renderScene( _buffer );
							_world.draw( _buffer );

							_buffer.update();
							_buffer.display( graphics );

							Thread.yield();
						}
					}
				}
			};

		final Thread viewThread = new Thread( loop , "viewThread:" + getID() );
		viewThread.start();
	}

	public Component getComponent()
	{
		return _viewComponent;
	}

	// For now, just clear the world and rebuild it.
	public void update()
	{
		// Stop render loop.
		_idle = true;

		// Clear the world.
		_world.removeAll();

		// Rebuild the world.
		final Node3DCollection nodes = _model.getScene();
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final Node3D   object       = nodes.getNode( i );
			final Matrix3D object2world = nodes.getMatrix( i );

			if ( object instanceof Object3D )
			{
				final com.threed.jpct.Object3D object3D = JPCTTools.convert2Object3D( (Object3D)object );
				final Matrix                   matrix   = JPCTTools.convert2Matrix  ( object2world     );

				object3D.setRotationMatrix( matrix );
				object3D.setTranslationMatrix( matrix );

				_world.addObject( object3D );
			}
			else if ( object instanceof Light3D )
			{
//				_world.addLight( ... );
			}
			else
			{
				/* Maybe there are some other types? */
			}
		}

		// Camera should also come from AB-tree.
		final Matrix3D ab2jpct = Matrix3D.INIT.set(
		1.0, 0.0, 0.0, 0.5 * _viewComponent.getWidth(),
		0.0, 0.0, -1.0, 0.5 * _viewComponent.getHeight(),
		0.0, -1.0, 0.0, 0.0 );

		final Matrix3D viewTransform = getViewTransform();
		final Matrix vt = JPCTTools.convert2Matrix( ab2jpct.multiply( viewTransform ) );

		final Camera camera = _world.getCamera();
		camera.setPosition( new SimpleVector( 0.0 , -1.0 / _model.getUnit() , 0.0  ) );
		camera.lookAt( new SimpleVector( 0.0 , 0.0 , 0.0 ) );

		System.out.println( ArrayTools.toString( camera.getBack().getDump() ) );

		final Matrix back = new Matrix();
		back.setDump( new float[] {
			 0.0f , -1.0f ,  0.0f ,  0.0f ,
			 0.0f ,  0.0f ,  1.0f ,  0.0f ,
			-1.0f ,  0.0f ,  0.0f ,  0.0f ,
			 0.0f ,  0.0f ,  0.0f ,  1.0f } );

		back.setDump( new float[] {
			 1.0f ,  0.0f ,  0.0f ,  0.0f ,
			 0.0f ,  0.0f ,  1.0f ,  0.0f ,
			 0.0f , -1.0f ,  0.0f ,  0.0f ,
			 0.0f ,  0.0f ,  0.0f ,  1.0f } );

		camera.setBack( back );

//		camera.setBack( vt );

		// Build all objects in the world.
		_world.buildAllObjects();

		// Start render loop.
		_idle = false;
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
}
