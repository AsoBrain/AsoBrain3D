/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2010
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
package ab.j3d.view.jogl2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLPbuffer;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JPopupMenu;

import ab.j3d.model.Camera3D;
import ab.j3d.model.Scene;
import ab.j3d.view.ProjectionPolicy;
import ab.j3d.view.Projector;
import ab.j3d.view.RenderStyle;
import ab.j3d.view.RenderStyleFilter;
import ab.j3d.view.View3D;
import ab.j3d.view.ViewControlInput;
import ab.j3d.view.control.DefaultViewControl;

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
	 * Specifies which OpenGL capabilities should be used, if available.
	 */
	private JOGLConfiguration _configuration;

	/**
	 * Provides information about OpenGL capabilities.
	 */
	private JOGLCapabilities _capabilities;

	/**
	 * Scene input translator for this View.
	 */
	private final ViewControlInput _controlInput;

	/**
	 * Front clipping plane distance in view units.
	 */
	private double _frontClipDistance;

	/**
	 * Back clipping plane distance in view units.
	 */
	private double _backClipDistance;

	/**
	 * Render thread.
	 */
	private RenderThread _renderThread;

	/**
	 * JOGL renderer.
	 */
	private JOGLRenderer _renderer;

	/**
	 * Construct new view.
	 *
	 * @param   joglEngine  Engine that created this view.
	 * @param   scene       Scene to view.
	 */
	public JOGLView( final JOGLEngine joglEngine , final Scene scene )
	{
		super( scene );

		_joglEngine        = joglEngine;
		_frontClipDistance =   0.1 / scene.getUnit();
		_backClipDistance  = 100.0 / scene.getUnit();
		_renderThread      = null;
		_renderer          = null;

		/* Use heavyweight popups, since we use a heavyweight canvas */
		JPopupMenu.setDefaultLightWeightPopupEnabled( false );

		final GLCanvas glCanvas;

		final GLProfile profile = GLProfile.getDefault();
		final GLCapabilities capabilities = new GLCapabilities( profile );
		capabilities.setSampleBuffers( true );
		/* set multisampling to 4, most graphic cards support this, if they don't support multisampling it will silently fail */
		capabilities.setNumSamples( 4 );

		/* See if the model already contains a context. */
		glCanvas = new GLCanvas( capabilities , null , joglEngine.getContext() , null );

		_configuration = joglEngine.getConfiguration();

		joglEngine.setContext( glCanvas.getContext() );

		glCanvas.setMinimumSize( new Dimension( 0 , 0 ) ); //resize workaround
		glCanvas.addGLEventListener( this );
		_glCanvas = glCanvas;

		_controlInput = new ViewControlInput( this );

		final DefaultViewControl defaultViewControl = new DefaultViewControl();
		appendControl( defaultViewControl );
		addOverlay( defaultViewControl );

		update();
	}

	/**
	 * Returns the view's rendering configuration.
	 *
	 * @return  Rendering configuration.
	 */
	public JOGLConfiguration getConfiguration()
	{
		return _configuration;
	}

	/**
	 * Returns the view's rendering capabilities.
	 *
	 * @return  Rendering capabilities.
	 */
	public JOGLCapabilities getCapabilities()
	{
		JOGLCapabilities capabilities = _capabilities;
		if ( capabilities == null )
		{
			final GLContext context = _glCanvas.getContext();
			if ( context != null )
			{
				capabilities = new JOGLCapabilities( context );
				_capabilities = capabilities;
			}
		}
		return capabilities;
	}

	public double getFrontClipDistance()
	{
		return _frontClipDistance;
	}

	public void setFrontClipDistance( final double frontClipDistance )
	{
		_frontClipDistance = frontClipDistance;
		update();
	}

	public double getBackClipDistance()
	{
		return _backClipDistance;
	}

	public void setBackClipDistance( final double backClipDistance )
	{
		_backClipDistance = backClipDistance;
		update();
	}

	public void setBackground( final Color color )
	{
		_glCanvas.setBackground( color );
	}

	public void dispose()
	{
		super.dispose();
		_renderer = null;
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
		final GLProfile profile = GLProfile.getDefault();
		final GLDrawableFactory factory = GLDrawableFactory.getFactory( profile );
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
	 * Disposes the current renderer. It will automatically be replaced with a
	 * new renderer when the next frame is rendered.
	 */
	public void disposeRenderer()
	{
		JOGLRenderer renderer = _renderer;

		if ( renderer != null )
		{
			final GLContext current = GLContext.getCurrent();
			final GLContext context = _glCanvas.getContext();

			if ( current == context )
			{
				renderer.dispose();
				renderer = null;
			}
			else
			{
				if ( context.makeCurrent() != GLContext.CONTEXT_NOT_CURRENT )
				{
					try
					{
						renderer.dispose();
						renderer = null;
					}
					finally
					{
						context.release();
						if ( current != null )
						{
							if ( current.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT )
							{
								System.out.println( "Failed to make original GL context current after 'disposeRenderer'." );
							}
						}
					}
				}
			}

			_renderer = renderer;
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
		 * Construct render thread.
		 */
		private RenderThread()
		{
			super( "JOGLView.renderThread:" );
			setDaemon( true );
			setPriority( NORM_PRIORITY );
			_updateRequested = true;
		}

		public void run()
		{
//			System.out.println( "Render thread started: " + Thread.currentThread() );

			final GLCanvas viewComponent = _glCanvas;
			while ( viewComponent.isShowing() )
			{
				try
				{
					if ( _updateRequested )
					{
						if ( viewComponent.isShowing() && ( viewComponent.getWidth() > 0 ) && ( viewComponent.getHeight() > 0 ) )
						{
							_updateRequested = false;
							viewComponent.display();
						}
					}
				}
				catch ( Throwable t )
				{
					System.err.println( "Render exception: " + t );
					t.printStackTrace( System.err );

					final GLContext context = GLContext.getCurrent();
					if ( context != null )
					{
						try
						{
							context.release();
						}
						catch ( GLException e )
						{
							e.printStackTrace();
						}
					}
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

//			System.out.println( "Renderer thread died: " + Thread.currentThread() );
		}

		/**
		 * Request update of rendered image.
		 */
		public void requestUpdate()
		{
			_updateRequested = true;

			synchronized ( this )
			{
				notifyAll();
			}
		}
	}

	/**
	 * Initialize GL context.
	 *
	 * @param   glAutoDrawable  Target for performing OpenGL rendering.
	 */
	public void init( final GLAutoDrawable glAutoDrawable )
	{
		try
		{
			final GL gl = glAutoDrawable.getGL();
			glAutoDrawable.setGL( gl );

			final JOGLRenderer renderer = getOrCreateRenderer( gl );
			renderer.init();
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}
	}

	@Override
	public void dispose( final GLAutoDrawable drawable )
	{
	}

	public void reshape( final GLAutoDrawable glAutoDrawable , final int x , final int y , final int width , final int height )
	{
		try
		{
			if ( _renderThread != null )
			{
				_renderThread.requestUpdate();
			}
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}
	}

	public final void display( final GLAutoDrawable glAutoDrawable )
	{
		try
		{
			displayImpl( glAutoDrawable );
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}
	}

	/**
	 * Renders a frame.
	 *
	 * @param   glAutoDrawable  Component to render to.
	 */
	protected void displayImpl( final GLAutoDrawable glAutoDrawable )
	{
		final GL gl1 = glAutoDrawable.getGL();
		final GL2 gl = gl1.getGL2();

		final int width  = glAutoDrawable.getWidth();
		final int height = glAutoDrawable.getHeight();

		if ( ( width > 0 ) && ( height > 0 ) )
		{
			final Camera3D camera = getCamera();
			final double   aspect = (double)width / (double)height;

			final ProjectionPolicy projectionPolicy = getProjectionPolicy();
			if ( projectionPolicy == ProjectionPolicy.PARALLEL )
			{
				final Scene    scene    = getScene();
				final Camera3D camera3D = getCamera();
				final double   left     = -0.5 * (double)width;
				final double   right    = +0.5 * (double)width;
				final double   bottom   = -0.5 * (double)height;
				final double   top      = +0.5 * (double)height;
				final double   scale    = camera3D.getZoomFactor() * scene.getUnit() / getResolution();
				final double   near     = _frontClipDistance * scale;
				final double   far      = _backClipDistance  * scale;

				gl.glMatrixMode( GL2.GL_PROJECTION );
				gl.glLoadIdentity();
				gl.glOrtho( left , right , bottom , top , near , far );
				gl.glScaled( scale , scale , scale );
			}
			else if ( projectionPolicy == ProjectionPolicy.PERSPECTIVE )
			{
				final double fov  = Math.toDegrees( camera.getAperture() );
				final double near = _frontClipDistance;
				final double far  = _backClipDistance;

				/* Setup the projection matrix. */
				gl.glMatrixMode( GL2.GL_PROJECTION );
				gl.glLoadIdentity();

				final GLU glu = new GLU();
				glu.gluPerspective( fov , 1.0 , near , far );
				gl.glHint( GL2.GL_PERSPECTIVE_CORRECTION_HINT , GL.GL_NICEST );

				gl.glScaled( 1.0 , aspect , 1.0 );
			}
			else
			{
				throw new AssertionError( "Not implemented: " + projectionPolicy );
			}

			renderScene( gl );

			if ( hasOverlay() )
			{
				final JOGLGraphics2D joglGraphics2D = new JOGLGraphics2D( glAutoDrawable );
				paintOverlay( joglGraphics2D );
				joglGraphics2D.dispose();
			}
		}
	}

	/**
	 * Renders the scene.
	 *
	 * @param   gl  OpenGL pipeline.
	 */
	private void renderScene( final GL2 gl )
	{
		final Scene scene = getScene();

		/* Setup initial style and apply style filters to this view. */
		final RenderStyle defaultStyle = new RenderStyle();
		final Collection<RenderStyleFilter> styleFilters = getRenderStyleFilters();
		final RenderStyle viewStyle = defaultStyle.applyFilters( styleFilters , this );

		/* Apply view transform. */
		gl.glMatrixMode( GL2.GL_MODELVIEW );
		gl.glLoadIdentity();
		JOGLTools.glMultMatrixd( gl , getScene2View() );

		final JOGLRenderer renderer = getOrCreateRenderer( gl );
		renderer.setGridEnabled( isGridEnabled() );
		renderer.setSceneToViewTransform( getScene2View() );
		renderer.renderScene( scene , styleFilters , viewStyle );
	}

	private JOGLRenderer getOrCreateRenderer( final GL gl )
	{
		JOGLRenderer renderer = _renderer;
		if ( renderer == null )
		{
			final TextureCache textureCache = _joglEngine.getTextureCache();
			renderer = new JOGLRenderer( gl , _configuration , textureCache , _glCanvas.getBackground() , isGridEnabled() , getGrid2wcs() , getGridBounds() , getGridCellSize() , isGridHighlightAxes() , getGridHighlightInterval() );
			renderer.init();
			_renderer = renderer;
		}
		return renderer;
	}
}
