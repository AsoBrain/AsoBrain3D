/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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
package ab.j3d.view.jogl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLPbuffer;
import javax.media.opengl.DebugGL;
import javax.media.opengl.glu.GLU;
import javax.swing.JPopupMenu;

import com.sun.opengl.util.j2d.Overlay;
import com.sun.opengl.util.texture.Texture;

import ab.j3d.model.Camera3D;
import ab.j3d.model.Scene;
import ab.j3d.view.control.DefaultViewControl;
import ab.j3d.view.ProjectionPolicy;
import ab.j3d.view.Projector;
import ab.j3d.view.RenderStyle;
import ab.j3d.view.RenderStyleFilter;
import ab.j3d.view.View3D;
import ab.j3d.view.ViewControlInput;

/**
 * JOGL view implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLView
	extends View3D
	implements GLEventListener
{
	/**
	 * Engine that created this view.
	 */
	private final JOGLEngine _joglEngine;

	/**
	 * Component through which a rendering of the view is shown.
	 */
	private final GLCanvas _glCanvas;

	/**
	 * Scene input translator for this View.
	 */
	private final ViewControlInput _controlInput;

	/**
	 * Front clipping plane distance in view units.
	 */
	private final double _frontClipDistance;

	/**
	 * Back clipping plane distance in view units.
	 */
	private final double _backClipDistance;

	/**
	 * Define JOGL2dGraphics here to allow reuse.
	 */
	private JOGLGraphics2D _j2d = null;

	/**
	 * Render thread.
	 */
	private RenderThread _renderThread;

	/**
	 * Whether {@link #init(GLAutoDrawable)} as been called at least once.
	 */
	private static boolean _firstInit = false;

	/**
	 * Construct new view.
	 *
	 * @param   joglEngine  Engine that created this view.
	 * @param   scene       Scene to view.
	 * @param   background  Background color to use for 3D views. May be
	 *                      <code>null</code>, in which case the default
	 *                      background color of the current look and feel is
	 */
	public JOGLView( final JOGLEngine joglEngine , final Scene scene , final Color background )
	{
		super( scene );

		_joglEngine = joglEngine;

		final double unit  = scene.getUnit();

		_frontClipDistance = 0.1 / unit;
		_backClipDistance  = 100.0 / unit;
		_renderThread      = null;

		/* Use heavyweight popups, since we use a heavyweight canvas */
		JPopupMenu.setDefaultLightWeightPopupEnabled( false );

		final GLCanvas glCanvas;

		final GLCapabilities capabilities   = new GLCapabilities();
		capabilities.setSampleBuffers( true );
		/* set multisampling to 4, most graphic cards support this, if they don't support multisampling it will silently fail */
		capabilities.setNumSamples( 4 );

		/* See if the model already contains a context. */
		glCanvas = new GLCanvas( capabilities , null , joglEngine.getContext() , null );
		joglEngine.setContext( glCanvas.getContext() );

		glCanvas.setMinimumSize( new Dimension( 0 , 0 ) ); //resize workaround
		glCanvas.addGLEventListener( this );
		_glCanvas = glCanvas;

		if ( background != null )
			_glCanvas.setBackground( background );

		_controlInput = new ViewControlInput( this );

		final DefaultViewControl defaultViewControl = new DefaultViewControl();
		appendControl( defaultViewControl );
		addOverlay( defaultViewControl );

		update();
	}

	/**
	 * Creates an offscreen buffer of the jogl context.
	 *
	 * @return  Offscreen {@link GLPbuffer} of the jogl context or
	 *          <code>NULL</code> if the graphic card doesnt have this ability.
	 */
	public GLPbuffer createOffscreenBuffer()
	{
		GLPbuffer buffer = null;
		final GLDrawableFactory factory = GLDrawableFactory.getFactory();
		if ( factory.canCreateGLPbuffer() )
		{
			buffer = factory.createGLPbuffer( _glCanvas.getChosenGLCapabilities() , null , _glCanvas.getWidth() , _glCanvas.getHeight() , _glCanvas.getContext() );
		}

		return buffer;
	}

	public Component getComponent()
	{
		return _glCanvas;
	}

	public void update()
	{
		startRenderer();
	}

	public Projector getProjector()
	{
		final GLCanvas  viewComponent     = _glCanvas;
		final int       imageWidth        = viewComponent.getWidth();
		final int       imageHeight       = viewComponent.getHeight();
		final double    imageResolution   = getResolution();

		final Scene     scene             = getScene();
		final double    viewUnit          = scene.getUnit();

		final double    fieldOfView       = getAperture();
		final double    zoomFactor        = getZoomFactor();
		final double    frontClipDistance = _frontClipDistance;
		final double    backClipDistance  = _backClipDistance;

		return Projector.createInstance( getProjectionPolicy() , imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );
	}

	protected ViewControlInput getControlInput()
	{
		return _controlInput;
	}

	/**
	 * Start render thread.
	 */
	private void startRenderer()
	{
		RenderThread renderThread = _renderThread;
		if ( renderThread == null || !renderThread.isAlive() )
		{
			if ( _glCanvas.isShowing() )
			{
				renderThread  = new RenderThread();
				_renderThread = renderThread;
				renderThread.start();
			}
		}
		else
		{
			renderThread.requestUpdate();
		}
	}

	/**
	 * Render loop for the view.
	 */
	private class RenderThread
		extends Thread
	{
		/**
		 * This thread control flag is set when <code>requestUpdate()</code> is
		 * called. It is used to trigger the thread loop to start rendering a new
		 * image. It is also tested at various loop points in the rendering code
		 * to abort rendering of a previous image, so the next rendering will be
		 * completed as soon as possible.
		 *
		 * @see     #requestUpdate()
		 */
		protected boolean _updateRequested;

		/**
		 * This thread control flag is set when <code>requestTermination()</code>
		 * is called. It is used as exit condition by the main thread loop.
		 *
		 * @see     #requestTermination()
		 * @see     #isAlive()
		 * @see     #join()
		 */
		private boolean _terminationRequested;

		/**
		 * Construct render thread.
		 */
		private RenderThread()
		{
			super( "JOGLView.renderThread:" );
			setDaemon( true );
			setPriority( NORM_PRIORITY );
			_updateRequested = true;
			_terminationRequested = false;
		}

		public void run()
		{
			System.out.println( "Render thread started: " + Thread.currentThread() );

			final GLCanvas viewComponent = _glCanvas;
			while ( !_terminationRequested && viewComponent.isShowing() )
			{
				try
				{
					if ( _updateRequested )
					{
						_updateRequested = false;

						if ( viewComponent.isShowing() )
						{
							viewComponent.display();
						}
					}
				}
				catch ( Throwable t )
				{
					System.err.println( "Render exception: " + t );
					t.printStackTrace( System.err );
				}

				/*
				 * No update needed or an exception occured.
				 *
				 * Wait 300ms or wait to be notified.
				 */
				try
				{
					synchronized ( this )
					{
						wait( 300L );
					}
				}
				catch ( InterruptedException e ) { /*ignored*/ }
			}

			_terminationRequested = false;
			_updateRequested      = false;

			System.out.println( "Renderer thread died: " + Thread.currentThread() );
		}

		/**
		 * Request update of rendered image.
		 */
		public void requestUpdate()
		{
			if ( !_terminationRequested )
			{
				_updateRequested = true;

				synchronized ( this )
				{
					notifyAll();
				}
			}
		}

		/**
		 * Request termination of the render thread.
		 */
		public void requestTermination()
		{
			_terminationRequested = true;

			synchronized ( this )
			{
				notifyAll();
			}
		}
	}

	/**
	 * Initialize GL context. Called once during initialization.
	 *
	 * @param   glAutoDrawable  Target for performing OpenGL rendering.
	 */
	public void init( final GLAutoDrawable glAutoDrawable )
	{
		final GL gl = new DebugGL( glAutoDrawable.getGL() );
		glAutoDrawable.setGL( gl );

		if ( !_firstInit )
		{
			_firstInit = true;

			System.out.println();
			System.out.println( " About OpenGL:" );
			System.out.println( "---------------" );
			System.out.println( "Version:    " + gl.glGetString( GL.GL_VERSION ) );
			System.out.println( "Vendor:     " + gl.glGetString( GL.GL_VENDOR ) );
			System.out.println( "Extensions: " + gl.glGetString( GL.GL_EXTENSIONS ) );
			System.out.println( "Renderer:   " + gl.glGetString( GL.GL_RENDERER ) );
			try
			{
				System.out.println( "Shaders:    " + gl.glGetString( GL.GL_SHADING_LANGUAGE_VERSION ) );
			}
			catch ( Exception e )
			{
				System.out.println( "Shaders:    n/a" );
			}
			System.out.println();
		}

		/* Enable depth buffering. */
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.glDepthMask( true );
		gl.glDepthFunc ( GL.GL_LEQUAL );

// @FIXME Disable explicit smoothing options for now. This causes extremely slow rendering on some machines. Should we set smoothing based on hardware capabilities?
//		/* Set smoothing. */
//		glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
//		glWrapper.setBlend( true );
//		gl.glEnable( GL.GL_LINE_SMOOTH ); //enable smooth lines
//		gl.glHint( GL.GL_LINE_SMOOTH_HINT , GL.GL_NICEST );
//		gl.glEnable( GL.GL_POLYGON_SMOOTH ); //enable smooth polygons
//		gl.glHint( GL.GL_POLYGON_SMOOTH_HINT , GL.GL_NICEST );
//		gl.glShadeModel( GL.GL_SMOOTH );

		/* Normalize lighting normals after scaling */
		gl.glEnable( GL.GL_NORMALIZE );
	}

	public void displayChanged( final GLAutoDrawable glAutoDrawable , final boolean b , final boolean b1 )
	{
	}

	/**
	 * Called whenever the GL canvas is resized.
	 *
	 * @param   glAutoDrawable  Target for performing OpenGL rendering.
	 * @param   x               X offset.
	 * @param   y               Y offset.
	 * @param   width           Width of canvas.
	 * @param   height          Height of canvas.
	 */
	public void reshape( final GLAutoDrawable glAutoDrawable , final int x , final int y , final int width , final int height )
	{
		final GL gl = glAutoDrawable.getGL();

		final Camera3D camera = getCamera();
		final double   fov    = Math.toDegrees( camera.getAperture() );
		final double   aspect = (double)width / (double)height;
		final double   near   = _frontClipDistance;
		final double   far    = _backClipDistance;

		/* Setup size of window to draw in. */
		gl.glViewport( x , y , width , height );

		if ( getProjectionPolicy() != ProjectionPolicy.PARALLEL )
		{
			//View is not parallel so let's use perspective view. No support for isometric view yet.

			/* Setup the projection matrix. */
			gl.glMatrixMode( GL.GL_PROJECTION );
			gl.glLoadIdentity();

			/* Set the perspective view */
			final GLU glu = new GLU();
			glu.gluPerspective( fov , 1.0 , near , far );
			gl.glHint( GL.GL_PERSPECTIVE_CORRECTION_HINT , GL.GL_NICEST ); // nice perspective calculations

			gl.glScaled( 1.0 , aspect , 1.0 );
		}
	}

	public void display( final GLAutoDrawable glAutoDrawable )
	{
		final GL gl = glAutoDrawable.getGL();

		final int width  = glAutoDrawable.getWidth();
		final int height = glAutoDrawable.getHeight();

		if ( ( width > 0 ) && ( height > 0 ) )
		{
			// apply parallel projection here, because the zoomfactor can be changed without resizing the window.
			if ( getProjectionPolicy() == ProjectionPolicy.PARALLEL )
			{
				final Scene    scene    = getScene();
				final Camera3D camera3D = getCamera();
				final double   left     = -0.5 * (double)width;
				final double   right    = +0.5 * (double)width;
				final double   bottom   = -0.5 * (double)height;
				final double   top      = +0.5 * (double)height;
				final double   near     = _frontClipDistance;
				final double   far      = _backClipDistance;
				final double   scale    = camera3D.getZoomFactor() * scene.getUnit() / getResolution();

				gl.glMatrixMode( GL.GL_PROJECTION );
				gl.glLoadIdentity();
				gl.glOrtho( left , right , bottom , top , near , far );
				gl.glScaled( scale , scale ,  scale );
			}

			renderScene( gl );

			if ( hasOverlay() )
			{
				JOGLGraphics2D j2d = _j2d;
				if ( j2d == null )
				{
					final Overlay overlay = new Overlay( glAutoDrawable );
					j2d = new JOGLGraphics2D( overlay.createGraphics() , glAutoDrawable );
					_j2d = j2d;
				}

				paintOverlay( j2d );
			}
		}
	}

	/**
	 * Renders the scene.
	 *
	 * @param   gl  OpenGL pipeline.
	 */
	private void renderScene( final GL gl )
	{
		final Scene scene = getScene();

		/* Setup initial style and apply style filters to this view. */
		final RenderStyle defaultStyle = new RenderStyle();
		final Collection<RenderStyleFilter> styleFilters = getRenderStyleFilters();
		final RenderStyle viewStyle = defaultStyle.applyFilters( styleFilters , this );

		/* Apply view transform. */
		gl.glMatrixMode( GL.GL_MODELVIEW );
		gl.glLoadIdentity();
		JOGLTools.glMultMatrixd( gl , getViewTransform() );

		/* Render scene. */
		final Map<String,Texture> textureCache = _joglEngine.getTextureCache();
		final JOGLRenderer renderer = new JOGLRenderer( gl , textureCache , _glCanvas.getBackground() , isGridEnabled() , getGrid2wcs() , getGridBounds() , getGridCellSize() , isGridHighlightAxes() , getGridHighlightInterval() );
		renderer.renderScene( scene.getContentNodes() , styleFilters , viewStyle );
	}
}
