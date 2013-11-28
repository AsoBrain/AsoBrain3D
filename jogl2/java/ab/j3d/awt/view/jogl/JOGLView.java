/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.awt.view.jogl;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;

import ab.j3d.awt.view.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

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
	private final JOGLConfiguration _configuration;

	/**
	 * Provides information about OpenGL capabilities.
	 */
	private final JOGLCapabilities _capabilities;

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
	 * Texture cache listener.
	 */
	private final TextureCacheListener _textureCacheListener;

	/**
	 * Graphics context for painting 2D graphics to the GL.
	 */
	private JOGLGraphics2D _graphics2D;

	/**
	 * Prevents the view from being disposed multiple times.
	 */
	private boolean _disposed = false;

	/**
	 * Construct new view.
	 *
	 * @param   joglEngine  Engine that created this view.
	 * @param   scene       Scene to view.
	 */
	public JOGLView( final JOGLEngine joglEngine, final Scene scene )
	{
		super( scene );

		_joglEngine = joglEngine;
		_frontClipDistance = 0.1 / scene.getUnit();
		_backClipDistance = 100.0 / scene.getUnit();
		_renderThread = null;
		_renderer = null;
		_graphics2D = null;

		/* Use heavyweight popups, since we use a heavyweight canvas */
		JPopupMenu.setDefaultLightWeightPopupEnabled( false );

		final GLCanvas glCanvas;

		final GLCapabilities capabilities = new GLCapabilities();
		capabilities.setRedBits( 8 );
		capabilities.setGreenBits( 8 );
		capabilities.setBlueBits( 8 );

		final JOGLConfiguration configuration = joglEngine.getConfiguration();
		_configuration = configuration;
		if ( configuration.isFSAAEnabled() )
		{
			capabilities.setSampleBuffers( true );
			/* set multisampling to 4, most graphic cards support this, if they don't support multisampling it will silently fail */
			capabilities.setNumSamples( 4 );
		}

		/* See if the model already contains a context. */
		glCanvas = new SharedContextGLCanvas( capabilities );

		_capabilities = new JOGLCapabilities( glCanvas.getContext() );

		glCanvas.setMinimumSize( new Dimension( 0, 0 ) ); //resize workaround
		glCanvas.addGLEventListener( this );
		_glCanvas = glCanvas;

		final ViewControlInput controlInput = new ViewControlInput( this );
		_controlInput = controlInput;

		final DefaultViewControl defaultViewControl = new DefaultViewControl();
		controlInput.addControlInputListener( defaultViewControl );
		addOverlay( defaultViewControl );

		update();

		final TextureCacheListener textureCacheListener = new TextureCacheListener()
		{
			public void textureChanged( @NotNull final TextureCache textureCache, @NotNull final TextureProxy textureProxy )
			{
				startRenderer();
			}
		};
		final TextureCache textureCache = joglEngine.getTextureCache();
		textureCache.addListener( textureCacheListener );
		_textureCacheListener = textureCacheListener;
	}

	/**
	 * Add overlay that displays the render statistics.
	 */
	public void addStatisticsOverlay()
	{
		addOverlay( new RenderStatisticsOverlay() );
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
		return _capabilities;
	}

	@Override
	public double getFrontClipDistance()
	{
		return _frontClipDistance;
	}

	@Override
	public void setFrontClipDistance( final double frontClipDistance )
	{
		_frontClipDistance = frontClipDistance;
		update();
	}

	@Override
	public double getBackClipDistance()
	{
		return _backClipDistance;
	}

	@Override
	public void setBackClipDistance( final double backClipDistance )
	{
		_backClipDistance = backClipDistance;
		update();
	}

	@Override
	public void dispose()
	{
		if ( !_disposed )
		{
			_disposed = true;

			final JOGLEngine engine = _joglEngine;
			engine.unregisterView();

			try
			{
				final GLContext context = _glCanvas.getContext();

				if ( ( engine.getContext() != context ) ||
				     ( engine.getRegisteredViewCount() == 0 ) )
				{
					try
					{
						context.release();
					}
					catch ( GLException gle )
					{
						// expected
					}
					finally
					{
						context.destroy();
					}
				}

				_renderer = null;

				final JOGLGraphics2D graphics2D = _graphics2D;
				if ( graphics2D != null )
				{
					graphics2D.dispose();
					_graphics2D = null;
				}

				final TextureCache textureCache = engine.getTextureCache();
				textureCache.removeListener( _textureCacheListener );

				super.dispose();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates an offscreen buffer of the jogl context.
	 *
	 * @return  Offscreen {@link GLPbuffer} of the jogl context or
	 *          <code>NULL</code> if the graphic card doesnt have this ability.
	 */
	@Nullable
	public GLPbuffer createOffscreenBuffer()
	{
		GLPbuffer buffer = null;

		final GLDrawableFactory factory = GLDrawableFactory.getFactory();
		if ( factory.canCreateGLPbuffer() )
		{
			buffer = factory.createGLPbuffer( _glCanvas.getChosenGLCapabilities(), null, _glCanvas.getWidth(), _glCanvas.getHeight(), _glCanvas.getContext() );
		}

		return buffer;
	}

	@Override
	public Component getComponent()
	{
		return _glCanvas;
	}

	@Override
	public void update()
	{
		startRenderer();
	}

	@Override
	public Projector getProjector()
	{
		final GLCanvas  viewComponent     = _glCanvas;
		final int       imageWidth        = viewComponent.getWidth();
		final int       imageHeight       = viewComponent.getHeight();
		final double    imageResolution   = getResolution();

		final Scene     scene             = getScene();
		final double    viewUnit          = scene.getUnit();

		final double    fieldOfView       = getFieldOfView();
		final double    zoomFactor        = getZoomFactor();
		final double    frontClipDistance = _frontClipDistance;
		final double    backClipDistance  = _backClipDistance;

		return Projector.createInstance( getProjectionPolicy(), imageWidth, imageHeight, imageResolution, viewUnit, frontClipDistance, backClipDistance, fieldOfView, zoomFactor );
	}

	@Override
	public ViewControlInput getControlInput()
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

		@Override
		public void run()
		{
			final GLCanvas viewComponent = _glCanvas;
			while ( !_disposed && viewComponent.isShowing() )
			{
				boolean exceptionOccurred = false;

				try
				{
					if ( isAnimationRunning() || _updateRequested )
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

					exceptionOccurred = true;
				}

				if ( exceptionOccurred || !( isAnimationRunning() || _updateRequested ) )
				{
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
			}
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
			final GL gl = new DebugGL( glAutoDrawable.getGL() );
			glAutoDrawable.setGL( gl );

			getOrCreateRenderer( gl, true );
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}
	}

	public void displayChanged( final GLAutoDrawable glAutoDrawable, final boolean b, final boolean b1 )
	{
		/* Not implemented in reference implementation. */
	}

	public void reshape( final GLAutoDrawable glAutoDrawable, final int x, final int y, final int width, final int height )
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
		fireBeforeFrameEvent();

		try
		{
			displayImpl( glAutoDrawable );
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}

		fireAfterFrameEvent();
	}

	/**
	 * Renders a frame.
	 *
	 * @param   glAutoDrawable  Component to render to.
	 */
	protected void displayImpl( final GLAutoDrawable glAutoDrawable )
	{
		final GL gl = glAutoDrawable.getGL();

		final int width  = glAutoDrawable.getWidth();
		final int height = glAutoDrawable.getHeight();

		if ( ( width > 0 ) && ( height > 0 ) )
		{
			final double   aspect = (double)width / (double)height;

			final ProjectionPolicy projectionPolicy = getProjectionPolicy();
			if ( projectionPolicy == ProjectionPolicy.PARALLEL )
			{
				final Scene    scene    = getScene();
				final double   left     = -0.5 * (double)width;
				final double   right    =  0.5 * (double)width;
				final double   bottom   = -0.5 * (double)height;
				final double   top      =  0.5 * (double)height;
				final double   scale    = getZoomFactor() * scene.getUnit() / getResolution();
				final double   near     = _frontClipDistance * scale;
				final double   far      = _backClipDistance  * scale;

				gl.glMatrixMode( GL.GL_PROJECTION );
				gl.glLoadIdentity();
				gl.glOrtho( left, right, bottom, top, near, far );
				gl.glScaled( scale, scale, scale );
			}
			else if ( projectionPolicy == ProjectionPolicy.PERSPECTIVE )
			{
				final double fov  = Math.toDegrees( getFieldOfView() );
				final double near = _frontClipDistance;
				final double far  = _backClipDistance;

				/* Setup the projection matrix. */
				gl.glMatrixMode( GL.GL_PROJECTION );
				gl.glLoadIdentity();

				final GLU glu = new GLU();
				glu.gluPerspective( fov, 1.0, near, far );
				gl.glHint( GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST );

				gl.glScaled( 1.0, aspect, 1.0 );
			}
			else
			{
				throw new AssertionError( "Not implemented: " + projectionPolicy );
			}

			renderScene( gl );

			if ( hasOverlay() )
			{
				JOGLGraphics2D joglGraphics2D = _graphics2D;
				if ( joglGraphics2D == null )
				{
					joglGraphics2D = new JOGLGraphics2D( glAutoDrawable );
					_graphics2D = joglGraphics2D;
				}
				else
				{
					joglGraphics2D.reset();
				}

				paintOverlay( joglGraphics2D );
			}

			if ( isAnimationRunning() )
			{
				startRenderer();
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
		final RenderStyle viewStyle = defaultStyle.applyFilters( styleFilters, this );

		/* Apply view transform. */
		gl.glMatrixMode( GL.GL_MODELVIEW );
		gl.glLoadIdentity();
		JOGLTools.glMultMatrixd( gl, getScene2View() );

		final JOGLRenderer renderer = getOrCreateRenderer( gl, false );
		renderer.setSceneToViewTransform( getScene2View() );
		renderer.renderScene( scene, styleFilters, viewStyle, getBackground(), getGrid() );
	}

	/**
	 * Creates a renderer for the given OpenGL pipeline or returns an existing
	 * one.
	 *
	 * @param   gl          OpenGL pipeline.
	 * @param   alwaysInit  Always call {@link JOGLRenderer#init()} vs. only if
	 *                      a new renderer was created.
	 *
	 * @return  New or existing renderer.
	 */
	private JOGLRenderer getOrCreateRenderer( final GL gl, final boolean alwaysInit )
	{
		JOGLRenderer renderer = _renderer;
		if ( renderer == null )
		{
			final TextureCache textureCache = _joglEngine.getTextureCache();
			renderer = new JOGLRenderer( gl, _configuration, textureCache, this );
			renderer.init();
			_renderer = renderer;
		}
		else if ( alwaysInit )
		{
			renderer.init();
		}

		return renderer;
	}

	/**
	 * Canvas with a GL context that is shared with all other views created by
	 * the same {@link JOGLEngine} instance.
	 */
	private class SharedContextGLCanvas
		extends GLCanvas
	{
		/**
		 * Constructs a new canvas with the specified capabilities.
		 *
		 * @param   capabilities    Capabilities to be requested.
		 */
		SharedContextGLCanvas( final GLCapabilities capabilities )
		{
			super( capabilities, new CapabilitiesChooser(), _joglEngine.getContext(), null );

			final GLContext context = getContext();
			if ( ( context != null ) && ( _joglEngine.getContext() == null ) )
			{
				_joglEngine.setContext( context );
			}

			_joglEngine.registerView();

			addHierarchyListener( new DisposeListener() );
		}

		@Override
		public void addNotify()
		{
			super.addNotify();
			_joglEngine.setContext( getContext() );
		}
	}

	/**
	 * Diposes the view when its parent window is closed.
	 */
	private class DisposeListener
		extends WindowAdapter
		implements HierarchyListener
	{
		/**
		 * Root component.
		 */
		private Component _root = null;

		public void hierarchyChanged( final HierarchyEvent e )
		{
			if ( ( e.getChangeFlags() & (long)HierarchyEvent.PARENT_CHANGED ) == (long)HierarchyEvent.PARENT_CHANGED )
			{
				final Component root = SwingUtilities.getRoot( e.getChanged() );
				final Component oldRoot = _root;
				if ( oldRoot != root )
				{
					if ( oldRoot instanceof Window )
					{
						( (Window)oldRoot ).removeWindowListener( this );
					}

					if ( root instanceof Window )
					{
						( (Window)root ).addWindowListener( this );
					}

					_root = root;
				}
			}
		}

		@Override
		public void windowClosed( final WindowEvent e )
		{
			dispose();
		}
	}

	/**
	 * Chooses the optimal set of GL capabilities. Always chooses the
	 * recommended choice at the moment.
	 */
	private static class CapabilitiesChooser
		extends DefaultGLCapabilitiesChooser
	{
		@Override
		public int chooseCapabilities( final GLCapabilities desired, final GLCapabilities[] available, final int windowSystemRecommendedChoice )
		{
/*
			System.out.println( " - desired = " + desired );
			System.out.println( " - available" );
			for ( final GLCapabilities availableCapabilities : available )
			{
				System.out.println( "    - " + availableCapabilities );
			}
			System.out.println( " - windowSystemRecommendedChoice = " + windowSystemRecommendedChoice );
*/
			return super.chooseCapabilities( desired, available, windowSystemRecommendedChoice );
		}
	}

	/**
	 * Displays render statistics.
	 *
	 * @see     JOGLRenderer.RenderStatistics
	 */
	private class RenderStatisticsOverlay
		implements ViewOverlay
	{
		public void addView( final View3D view )
		{
		}

		public void removeView( final View3D view )
		{
		}

		public void paintOverlay( final View3D view, final Graphics2D g )
		{
			final JOGLRenderer renderer = _renderer;
			if ( renderer != null )
			{
				final JOGLRenderer.RenderStatistics statistics = renderer.getStatistics();
				if ( statistics != null )
				{
					final Font font = g.getFont();
					g.setFont( font.deriveFont( 10.0f ) );
					g.setColor( new Color( 0xa0ffffff, true ) );
					g.fillRect( 0, 0, 150, 50 );
					g.setColor( Color.BLACK );
					g.drawString( "FPS: " + statistics.getFPS(), 5, 15 );
					g.drawString( "Primitives: " + statistics.getPrimitiveCount(), 5, 30 );
					g.drawString( "Objects: " + statistics.getObjectCount() + " (" + statistics.getUniqueObjectCount() + " unique)", 5, 45 );
				}
			}
		}
	}
}
