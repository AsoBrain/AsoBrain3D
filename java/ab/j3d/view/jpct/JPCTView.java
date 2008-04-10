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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.OverlayPainter;
import ab.j3d.view.Projector;
import ab.j3d.view.RenderQueue;
import ab.j3d.view.SwitchRenderingPolicyAction;
import ab.j3d.view.ViewControlInput;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.java2d.Painter;

/**
 * jPCT implementation of view model view.
 *
 * <p>The view can use hardware acceleration if the lwjgl library is available
 * and the available hardware supports it. Hardware acceleration is disabled by
 * default, but may be enabled on construction of the view.
 *
 * <p>
 * The view enables switching between hardware and software rendering. To do
 * this, a {@link ViewComponent} interface is defined with the functions needed
 * by the view. Two implementations of are provided. The first,
 * {@link ViewComponentImpl}, uses either software or hardware rendering
 * depending on the {@link ViewStrategy} provided at construction. The other,
 * {@link ViewComponentSwitcher}, implements switching between view components.
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
	private final ViewComponentSwitcher _viewComponent;

	/**
	 * Scene input translator for this View.
	 */
	private final ViewControlInput _controlInput;

	/**
	 * Model being viewed.
	 */
	private JPCTModel _model;

	/**
	 * Background color of the view.
	 */
	private Color _backgroundColor;

	/**
	 * Color for wireframes.
	 */
	private Color _wireframeColor;

	/**
	 * Whether the view needs to be updated to model changes.
	 */
	private boolean _updateNeeded;

	/**
	 * View component that uses the software renderer.
	 */
	private ViewComponentImpl _softwareView;

	/**
	 * View component using the software strategy.
	 */
	private ViewComponentImpl _hardwareView;

	/**
	 * Construct new jPCT-based view that uses no hardware acceleration.
	 *
	 * @param   model                   Model to create a view of.
	 * @param   id                      ID of the view.
	 * @param   backgroundColor         Background color of the view.
	 */
	public JPCTView( final JPCTModel model , final Object id , final Color backgroundColor )
	{
		this( model , id , backgroundColor , false );
	}

	/**
	 * Construct new jPCT-based view that uses no hardware acceleration.
	 *
	 * @param   model                   Model to create a view of.
	 * @param   id                      ID of the view.
	 * @param   backgroundColor         Background color of the view.
	 * @param   hardwareAccelerated     <code>true</code> to use hardware
	 *                                  acceleration (if available);
	 *                                  otherwise, no hardware acceleration is
	 *                                  used.
	 */
	public JPCTView( final JPCTModel model , final Object id , final Color backgroundColor , final boolean hardwareAccelerated )
	{
		super( model.getUnit() , id );
		_model               = model;
		_backgroundColor     = backgroundColor;
		_wireframeColor      = new Color( ~backgroundColor.getRGB() );

		Config.glColorDepth    = 24;    // @TODO set this on Linux only
		Config.useLocking      = true;
		Config.fadeoutLight    = true;

//		Config.specPow         = 60.0f;
//		Config.specTerm        = 30.0f;
//		Config.useFastSpecular = false;

		_viewComponent = createViewComponentSwitcher( hardwareAccelerated );
		_controlInput  = new ViewControlInput( model , this );

		_updateNeeded = true;
		startRenderer();
	}

	/**
	 * Adds an overlay painter to the view. Using overlay painters disables
	 * hardware acceleration.
	 *
	 * @param   painter     Overlay painter to be added.
	 */
	public void addOverlayPainter( final OverlayPainter painter )
	{
		super.addOverlayPainter( painter );
		setHardwareEnabled( false );
	}

	/**
	 * Removes an overlay painter from the view. Using overlay painters disables
	 * hardware acceleration.
	 *
	 * @param   painter     Overlay painter to be removed.
	 */
	public void removeOverlayPainter( final OverlayPainter painter )
	{
		super.removeOverlayPainter( painter );
		setHardwareEnabled( !hasOverlayPainters() );
	}

	/**
	 * Enables or disables hardware acceleration.
	 *
	 * @param   hardwareEnabled     <code>true</code> to enable hardware
	 *                              acceleration; <code>false</code> to disable
	 *                              hardware acceleration.
	 */
	private void setHardwareEnabled( final boolean hardwareEnabled )
	{
		final ViewComponentSwitcher switcher = _viewComponent;

		if ( hardwareEnabled && ( _hardwareView != null ) )
		{
			switcher.showViewComponent( _hardwareView );
		}
		else
		{
			switcher.showViewComponent( _softwareView );
		}
	}

	/**
	 * Returns an appropriate view component, based on whether hardware
	 * acceleration is available.
	 *
	 * @return  View component.
	 */
	private ViewComponentSwitcher createViewComponentSwitcher( final boolean hardwareAccelerated )
	{
		final ViewComponentImpl softwareView = new ViewComponentImpl( new SoftwareViewStrategy() );
		softwareView.setName( "software" );

		final ViewComponentSwitcher result = new ViewComponentSwitcher();
		result.addViewComponent( softwareView );

		final ViewComponentImpl hardwareView;
		if ( hardwareAccelerated )
		{
			boolean hardwareSupport = false;
			try
			{
				System.loadLibrary( "lwjgl" );
				hardwareSupport = true;
			}
			catch ( UnsatisfiedLinkError e )
			{
				/** Hardware OpenGL renderer is not available. */
			}
			catch ( SecurityException e )
			{
				/** Hardware OpenGL renderer is not available. */
			}

			if ( hardwareSupport )
			{
				hardwareView = new ViewComponentImpl( new HardwareViewStrategy() );
				hardwareView.setName( "hardware" );
				hardwareView.setBackground( _backgroundColor );
				result.addViewComponent( hardwareView );
				result.showViewComponent( hardwareView );
			}
			else
			{
				hardwareView = null;
			}
		}
		else
		{
			hardwareView = null;
		}

		_hardwareView = hardwareView;
		_softwareView = softwareView;

		return result;
	}

	/**
	 * Creates the render thread, running {@link Renderer}, and starts it.
	 */
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
	 * Updates the camera transformation in jPCT based on the current view
	 * transform.
	 *
	 * <p>NOTE: this method must only be invoked from render thread.
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

		final World skyBox = _model.getSkyBox();
		skyBox.buildAllObjects();

		final Matrix skyTransform = new Matrix();
		skyTransform.setDump( new float[]
			{
				(float)viewTransform.xx , (float)-viewTransform.yx , (float)-viewTransform.zx , 0.0f ,
				(float)viewTransform.xy , (float)-viewTransform.yy , (float)-viewTransform.zy , 0.0f ,
				(float)viewTransform.xz , (float)-viewTransform.yz , (float)-viewTransform.zz , 0.0f ,
				0.0f , 0.0f , 0.0f , 1.0f
			} );

		final Camera skyCamera = skyBox.getCamera();
		skyCamera.setFOV( skyCamera.convertRADAngleIntoFOV( (float)getAperture() ) );
		skyCamera.setBack( skyTransform );
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

				renderFrame();
				Thread.yield();
			}
		}
	}

	/**
	 * Renders the current frame.
	 *
	 * <p>NOTE: this method must only be invoked from render thread.
	 */
	private void renderFrame()
	{
		final ViewComponent viewComponent = _viewComponent;
		if ( viewComponent != null )
		{
			final FrameBuffer frameBuffer = viewComponent.getFrameBuffer();

			if ( frameBuffer != null )
			{
				final JPCTModel model = _model;

				final Color background = _backgroundColor;
				frameBuffer.clear( background );

				switch ( getRenderingPolicy() )
				{
					case WIREFRAME :
						{
							final Matrix3D  viewTransform = getViewTransform();
							final Projector projector     = getProjector();

							final RenderQueue renderQueue = new RenderQueue();

							final Graphics graphics = frameBuffer.getGraphics();

							final Node3DCollection<Node3D> scene = model.getScene();
							for ( int i = 0 ; i < scene.size() ; i++ )
							{
								final Node3D   node   = scene.getNode( i );
								final Matrix3D matrix = scene.getMatrix( i );
								final Matrix3D node2view = matrix.multiply( viewTransform );

								if ( node instanceof Object3D )
								{
									final Object3D object3d = (Object3D)node;
									object3d.outlinePaint = _wireframeColor;
									renderQueue.enqueueObject( projector , true , node2view , object3d, false );
								}
								/* @FIXME Handle Insert3D's here
								else
								{
								} */
							}

							Painter.paintQueue( (Graphics2D)graphics , renderQueue , true , false , false , false );
							graphics.dispose();

						}
						break;

					default:
						{
							final World world = getWorld();
							model.updateWorld();

							updateCamera();
							world.buildAllObjects();

							final World skyBox = model.getSkyBox();
							skyBox.renderScene( frameBuffer );
							skyBox.draw( frameBuffer );
							frameBuffer.clearZBufferOnly();

							world.renderScene( frameBuffer );
							world.draw( frameBuffer );

							frameBuffer.update();
						}
						break;
				}

				viewComponent.frameBufferUpdated();
			}
		}
	}

	public Action[] getActions( final Locale locale )
	{
		return new Action[] { new SwitchRenderingPolicyAction( locale , this , getRenderingPolicy() ) };
	}

	/**
	 * Provides a component to display the view on the screen using jPCT.
	 */
	private interface ViewComponent
	{
		/**
		 * Returns the view's projector.
		 *
		 * @return  Projector.
		 */
		Projector getProjector();

		/**
		 * Returns the view component.
		 *
		 * @return  View component.
		 */
		JComponent getComponent();

		/**
		 * Returns the frame buffer for this view component.
		 *
		 * @return  Frame buffer.
		 */
		FrameBuffer getFrameBuffer();

		/**
		 * Notifies the view component that the frame buffer was updated.
		 */
		void frameBufferUpdated();
	}

	/**
	 * View component that displays the view on the screen, using a method
	 * defined by a {@link ViewStrategy}.
	 */
	private class ViewComponentImpl
		extends EventForwardingComponent
		implements ViewComponent
	{
		/**
		 * Insets cache.
		 */
		private Insets _insets;

		/**
		 * Strategy used by the view component.
		 */
		private final ViewStrategy _strategy;

		/**
		 * Construct view component.
		 */
		private ViewComponentImpl( final ViewStrategy strategy )
		{
			_strategy    = strategy;
			_insets      = null;

			strategy.install( this );

			final Dimension size = new Dimension( MINIMUM_IMAGE_SIZE , MINIMUM_IMAGE_SIZE );
			setMinimumSize( size );
			setPreferredSize( size );
		}

		/**
		 * Returns the frame buffer displayed by the view component.
		 *
		 * @return  Frame buffer.
		 */
		public FrameBuffer getFrameBuffer()
		{
			FrameBuffer result = null;

			final ViewStrategy strategy = _strategy;
			if ( strategy != null )
			{
				final FrameBuffer frameBuffer = strategy.getFrameBuffer();

				/*
				 * Create a new frame buffer if needed, e.g. when resized.
				 */
				final Dimension size = getSize();
				if ( ( frameBuffer == null ) ||
				     ( frameBuffer.getOutputWidth()  != size.width  ) ||
				     ( frameBuffer.getOutputHeight() != size.height ) )
				{
					if ( frameBuffer != null )
					{
						frameBuffer.dispose();
					}

					if ( ( size.width > 0 ) && ( size.height > 0 ) )
					{
						result = new FrameBuffer( size.width , size.height , FrameBuffer.SAMPLINGMODE_NORMAL );
						strategy.frameBufferReplaced( this , result );
					}
					else
					{
						result = null;
					}
				}
				else
				{
					result = frameBuffer;
				}
			}

			return result;
		}

		/**
		 * Notifies the component that the contents of the frame buffer was
		 * modified.
		 */
		public void frameBufferUpdated()
		{
			_strategy.frameBufferUpdated( this );
		}

		public Projector getProjector()
		{
			final Insets      insets            = getInsets( _insets );
			final int         imageWidth        = getWidth() - insets.left - insets.right;
			final int         imageHeight       = getHeight() - insets.top - insets.bottom;
			final double      imageResolution   = getResolution();

			final double      viewUnit          = getUnit();

			final double      fieldOfView       = getAperture();
			final double      zoomFactor        = getZoomFactor();
			final double      frontClipDistance =  -0.1 / viewUnit;
			final double      backClipDistance  = -100.0 / viewUnit;

			return Projector.createInstance( getProjectionPolicy(), imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );
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

		public JComponent getComponent()
		{
			return this;
		}

		public String toString()
		{
			final Class<?> clazz         = getClass();
			final Class<?> strategyClass = _strategy.getClass();

			return clazz.getSimpleName() + "[strategy=" + strategyClass.getSimpleName() + "]";
		}
	}

	/**
	 * View component that provides the ability to switch between view
	 * components.
	 */
	private static class ViewComponentSwitcher
		extends EventForwardingComponent
		implements ViewComponent
	{
		/**
		 * Currently displayed view component.
		 */
		private ViewComponent _currentView;

		/**
		 * Constructs a new view component switcher.
		 */
		ViewComponentSwitcher()
		{
			_currentView = null;
			setLayout( new CardLayout() );
		}

		/**
		 * Adds the given view component to the view switcher.
		 *
		 * @param   viewComponent   View component to be added.
		 */
		public void addViewComponent( final ViewComponent viewComponent )
		{
			final JComponent component = viewComponent.getComponent();
			add( component , String.valueOf( System.identityHashCode( component ) ) );

			if ( _currentView == null )
			{
				_currentView = viewComponent;
			}
		}

		/**
		 * Shows the given view component and hides all others.
		 *
		 * @param   viewComponent   View component to be shown.
		 */
		public void showViewComponent( final ViewComponent viewComponent )
		{
			final ViewComponent oldViewComponent = _currentView;
			final JComponent    component        = viewComponent.getComponent();
			if ( !component.isVisible() )
			{
				final JComponent oldComponent = oldViewComponent.getComponent();

				_currentView = viewComponent;

				component.setVisible( true );
				oldComponent.setVisible( false );
			}
		}

		public FrameBuffer getFrameBuffer()
		{
			return _currentView.getFrameBuffer();
		}

		public void frameBufferUpdated()
		{
			_currentView.frameBufferUpdated();
		}

		public Projector getProjector()
		{
			return _currentView.getProjector();
		}

		public JComponent getComponent()
		{
			return _currentView.getComponent();
		}
	}

	/**
	 * Defines how frame buffer creation and updates are handled, including the
	 * actual displaying of the frame buffer on a view component.
	 */
	private abstract static class ViewStrategy
	{
		/**
		 * Frame buffer displayed by the component.
		 */
		private FrameBuffer _frameBuffer;

		/**
		 * Constructs a view strategy.
		 */
		protected ViewStrategy()
		{
			_frameBuffer = null;
		}

		/**
		 * Returns the frame buffer used by the strategy.
		 *
		 * @return  Frame buffer.
		 */
		public FrameBuffer getFrameBuffer()
		{
			return _frameBuffer;
		}

		/**
		 * Performs implementation-specific clean-up and initialization needed
		 * as a result fo replacing the frame buffer.
		 *
		 * @param   component   View component.
		 * @param   newBuffer   New frame buffer.
		 */
		public void frameBufferReplaced( final ViewComponent component , final FrameBuffer newBuffer )
		{
			_frameBuffer = newBuffer;
		}

		/**
		 * Notifies the strategy that the contents of the frame buffer was
		 * modified. Implementing classes should update the display when
		 * notified of a frame buffer update.
		 *
		 * @param   component   View component.
		 */
		public abstract void frameBufferUpdated( final ViewComponent component );

		/**
		 * Installs the strategy into the given view component. This may involve
		 * configuring the view component, e.g. adding child components, setting
		 * its layout manager, etc.
		 *
		 * @param   component   View component.
		 */
		public abstract void install( final ViewComponent component );
	}

	/**
	 * View component strategy for the (lightweight) software renderer.
	 * This strategy supports overlay painters.
	 */
	private class SoftwareViewStrategy
		extends ViewStrategy
	{
		public void frameBufferReplaced( final ViewComponent component , final FrameBuffer newBuffer )
		{
			super.frameBufferReplaced( component , newBuffer );
			newBuffer.enableRenderer( IRenderer.RENDERER_SOFTWARE , IRenderer.MODE_OPENGL );
		}

		public void frameBufferUpdated( final ViewComponent viewComponent )
		{
			final FrameBuffer frameBuffer = getFrameBuffer();

			final Graphics bufferGraphics = frameBuffer.getGraphics();
			if ( hasOverlayPainters() )
			{
				paintOverlay( (Graphics2D)bufferGraphics );
			}
			bufferGraphics.dispose();

			final JComponent component = viewComponent.getComponent();
			final Graphics graphics = component.getGraphics();
			frameBuffer.display( graphics );
			graphics.dispose();
		}

		public void install( final ViewComponent viewComponent )
		{
		}
	}

	/**
	 * View component strategy for the (heavyweight) hardware renderer.
	 * This strategy does not support overlay painters.
	 */
	private static class HardwareViewStrategy
		extends ViewStrategy
	{
		/**
		 * Canvas used for hardware-accelerated rendering.
		 */
		private Canvas _canvas = null;

		public void frameBufferReplaced( final ViewComponent component , final FrameBuffer newBuffer )
		{
			super.frameBufferReplaced( component , newBuffer );
			newBuffer.enableRenderer( IRenderer.RENDERER_OPENGL , IRenderer.MODE_OPENGL );
			setCanvas( component , newBuffer.enableGLCanvasRenderer( IRenderer.MODE_OPENGL ) );
			newBuffer.disableRenderer( IRenderer.RENDERER_SOFTWARE );
		}

		public void frameBufferUpdated( final ViewComponent component )
		{
			final FrameBuffer frameBuffer = getFrameBuffer();
			frameBuffer.displayGLOnly();

			final Graphics graphics = _canvas.getGraphics();
			_canvas.update( graphics );
			graphics.dispose();
		}

		/**
		 * Sets the component's canvas and configures it to fire mouse events
		 * to the component's listeners.
		 *
		 * @param   viewComponent   View component.
		 * @param   canvas          Canvas to be set.
		 */
		private void setCanvas( final ViewComponent viewComponent , final Canvas canvas )
		{
			final JComponent component = viewComponent.getComponent();

			component.removeAll();
			if ( canvas != null )
			{
				component.add( canvas );
				component.revalidate();

				final MouseListener[] mouseListeners = component.getMouseListeners();
				for ( final MouseListener listener : mouseListeners )
				{
					canvas.addMouseListener( listener );
				}

				final MouseMotionListener[] mouseMotionListeners = component.getMouseMotionListeners();
				for ( final MouseMotionListener listener : mouseMotionListeners )
				{
					canvas.addMouseMotionListener( listener );
				}
			}
			_canvas = canvas;
		}

		public void install( final ViewComponent viewComponent )
		{
			final JComponent component = viewComponent.getComponent();
			component.setLayout( new BorderLayout() );
		}
	}
}
