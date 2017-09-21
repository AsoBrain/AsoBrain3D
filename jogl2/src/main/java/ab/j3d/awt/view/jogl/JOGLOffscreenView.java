/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
 */
package ab.j3d.awt.view.jogl;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.fixedfunc.*;
import javax.media.opengl.glu.*;
import javax.swing.*;

import ab.j3d.awt.view.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import com.jogamp.opengl.util.awt.*;

/**
 * Off-screen JOGL view.
 *
 * @author Gerrit Meinders
 */
public class JOGLOffscreenView
extends OffscreenView3D
{
	/**
	 * Engine that created this view.
	 */
	private final JOGLEngine _joglEngine;

	/**
	 * Off-screen rendering target.
	 */
	private final GLOffscreenAutoDrawable _drawable;

	/**
	 * Specifies which OpenGL capabilities should be used, if available.
	 */
	private final JOGLConfiguration _configuration;

	/**
	 * Provides information about OpenGL capabilities.
	 */
	private JOGLCapabilities _capabilities = null;

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
	 * JOGL renderer.
	 */
	private JOGLRenderer _renderer;

	/**
	 * Graphics context for painting 2D graphics to the GL.
	 */
	private JOGLGraphics2D _graphics2D;

	/**
	 * Prevents the view from being disposed multiple times.
	 */
	private boolean _disposed = false;

	/**
	 * Copies the framebuffer to an AWT buffered image.
	 */
	private AWTGLReadBufferUtil _framebufferReader = null;

	/**
	 * Construct new view with an alpha channel.
	 *
	 * @param joglEngine Engine that created this view.
	 * @param scene      Scene to view.
	 */
	public JOGLOffscreenView( final JOGLEngine joglEngine, final Scene scene )
	{
		this( joglEngine, scene, true );
	}

	/**
	 * Construct new view.
	 *
	 * @param joglEngine Engine that created this view.
	 * @param scene      Scene to view.
	 * @param alpha      {@code true} to include an alpha channel.
	 */
	public JOGLOffscreenView( final JOGLEngine joglEngine, final Scene scene, final boolean alpha )
	{
		super( scene );

		final GLProfile profile = GLProfile.get( GLProfile.GL2 );
		if ( !Threading.isOpenGLThread() )
		{
			throw new IllegalStateException( "Must be called from OpenGL thread" );
		}

		_joglEngine = joglEngine;
		_frontClipDistance = 0.1 / scene.getUnit();
		_backClipDistance = 100.0 / scene.getUnit();
		_renderer = null;
		_graphics2D = null;

		/* Use heavyweight popups, since we use a heavyweight canvas */
		JPopupMenu.setDefaultLightWeightPopupEnabled( false );

		final GLCapabilities capabilities = new GLCapabilities( profile );
		capabilities.setRedBits( 8 );
		capabilities.setGreenBits( 8 );
		capabilities.setBlueBits( 8 );
		capabilities.setAlphaBits( alpha ? 8 : 0 );
		capabilities.setDepthBits( 24 );
		capabilities.setHardwareAccelerated( true );
		capabilities.setOnscreen( false );
		capabilities.setDoubleBuffered( false );
		capabilities.setBackgroundOpaque( !alpha );

		final GLDrawableFactory drawableFactory = GLDrawableFactory.getFactory( profile );
		final GLOffscreenAutoDrawable drawable = drawableFactory.createOffscreenAutoDrawable( null, capabilities, null, 640, 480 );
		drawable.setContext( drawable.createContext( null ), true );
		_drawable = drawable;

		final JOGLConfiguration configuration = joglEngine.getConfiguration();
		_configuration = configuration;
//		if ( configuration.isFSAAEnabled() )
//		{
//			capabilities.setSampleBuffers( true );
//			/* set multisampling to 4, most graphic cards support this, if they don't support multisampling it will silently fail */
//			capabilities.setNumSamples( 4 );
//		}

		final ViewControlInput controlInput = new ViewControlInput( this );
		_controlInput = controlInput;

		final DefaultViewControl defaultViewControl = new DefaultViewControl();
		controlInput.addControlInputListener( defaultViewControl );
		addOverlay( defaultViewControl );

		update();
	}

	@Override
	public BufferedImage renderImage( final int width, final int height )
	{
		final BufferedImage[] result = new BufferedImage[ 1 ];

		Threading.invokeOnOpenGLThread( true, new Runnable()
		{
			@Override
			public void run()
			{
				final GLOffscreenAutoDrawable drawable = _drawable;
				if ( ( drawable.getSurfaceWidth() != width ) || ( drawable.getSurfaceHeight() != height ) )
				{
					drawable.setSurfaceSize( width, height );
				}

				final GLContext context = drawable.getContext();
				if ( context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT )
				{
					throw new GLException( "Failed to make offscreen context current." );
				}

				if ( _capabilities == null ) // TODO: Find a better check to init only once.
				{
					init( drawable );

					// FIXME: Release context and make it current again. Don't know why, but image is empty otherwise.
					context.release();
					if ( context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT )
					{
						throw new GLException( "Failed to make offscreen context current." );
					}
				}

				try
				{
					final GL gl = context.getGL();
					gl.glViewport( 0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight() );
					display( drawable );

					final AWTGLReadBufferUtil readBufferUtil = getFramebufferReader();
					result[ 0 ] = readBufferUtil.readPixelsToBufferedImage( gl, true );
				}
				finally
				{
					context.release();
				}
			}
		} );

		return result[ 0 ];
	}

	/**
	 * Returns an instance of {@link AWTGLReadBufferUtil} to copy the contents
	 * of the framebuffer to a buffered image.
	 *
	 * @return Created or re-used instance.
	 */
	private AWTGLReadBufferUtil getFramebufferReader()
	{
		AWTGLReadBufferUtil result = _framebufferReader;
		if ( result == null )
		{
			final GLCapabilitiesImmutable chosenGLCapabilities = _drawable.getChosenGLCapabilities();
			result = new AWTGLReadBufferUtil( _drawable.getGLProfile(), !chosenGLCapabilities.isBackgroundOpaque() );
			_framebufferReader = result;
		}
		return result;
	}

	/**
	 * Returns the view's rendering configuration.
	 *
	 * @return Rendering configuration.
	 */
	public JOGLConfiguration getConfiguration()
	{
		return _configuration;
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

			try
			{
				Threading.invokeOnOpenGLThread(true, new Runnable()
				{
					@Override
					public void run()
					{
						disposeContext();
						_drawable.destroy();
					}
				} );

				super.dispose();
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}

			_framebufferReader = null;
		}
	}

	/**
	 * Disposes the GL context and associated resources. Either to dispose the view
	 * completely or to switch to another GL context (e.g. another screen).
	 */
	private void disposeContext()
	{
		final GLContext context = _drawable.getContext();
		if ( context != null )
		{
			try
			{
				context.release();
			}
			catch ( final GLException gle )
			{
				// expected
			}
			finally
			{
				context.destroy();
			}
		}

		_renderer = null;
		_capabilities = null;

		final JOGLGraphics2D graphics2D = _graphics2D;
		if ( graphics2D != null )
		{
			graphics2D.dispose();
			_graphics2D = null;
		}
	}

	@Override
	public Component getComponent()
	{
		return null;
	}

	@Override
	public void update()
	{
	}

	@Override
	public Projector getProjector()
	{
		final GLDrawable viewComponent = _drawable;
		final int imageWidth = viewComponent.getSurfaceWidth();
		final int imageHeight = viewComponent.getSurfaceHeight();
		final double imageResolution = getResolution();

		final Scene scene = getScene();
		final double viewUnit = scene.getUnit();

		final double fieldOfView = getFieldOfView();
		final double zoomFactor = getZoomFactor();
		final double frontClipDistance = _frontClipDistance;
		final double backClipDistance = _backClipDistance;

		return Projector.createInstance( getProjectionPolicy(), imageWidth, imageHeight, imageResolution, viewUnit, frontClipDistance, backClipDistance, fieldOfView, zoomFactor );
	}

	@Override
	public ViewControlInput getControlInput()
	{
		return _controlInput;
	}

	@Override
	public void setSize( final int width, final int height )
	{
		_drawable.setSurfaceSize( width, height );
	}

	@Override
	protected int getWidth()
	{
		return _drawable.getSurfaceWidth();
	}

	@Override
	protected int getHeight()
	{
		return _drawable.getSurfaceHeight();
	}

	/**
	 * Initialize GL context.
	 *
	 * @param glAutoDrawable Target for performing OpenGL rendering.
	 */
	private void init( final GLAutoDrawable glAutoDrawable )
	{
		_capabilities = new JOGLCapabilities( _drawable.getContext() );

		try
		{
			final GL gl = new DebugGL2( glAutoDrawable.getGL().getGL2() );
			glAutoDrawable.setGL( gl );

			getOrCreateRenderer( gl, true );
		}
		catch ( final Throwable t )
		{
			t.printStackTrace();
		}
	}

	/**
	 * Renders a frame.
	 *
	 * @param glAutoDrawable Target for performing OpenGL rendering.
	 */
	private void display( final GLAutoDrawable glAutoDrawable )
	{
		fireBeforeFrameEvent();

		try
		{
			displayImpl( glAutoDrawable );
		}
		catch ( final Throwable t )
		{
			t.printStackTrace();
		}

		fireAfterFrameEvent();
	}

	/**
	 * Renders a frame.
	 *
	 * @param glAutoDrawable Component to render to.
	 */
	protected void displayImpl( final GLAutoDrawable glAutoDrawable )
	{
		final GL gl = GLContext.getCurrentGL();
		final GL2 gl2 = gl.getGL2();

		final int width = glAutoDrawable.getSurfaceWidth();
		final int height = glAutoDrawable.getSurfaceHeight();

		if ( ( width > 0 ) && ( height > 0 ) )
		{
			final double aspect = (double)width / (double)height;

			final ProjectionPolicy projectionPolicy = getProjectionPolicy();
			if ( projectionPolicy == ProjectionPolicy.PARALLEL )
			{
				final Scene scene = getScene();
				final double left = -0.5 * (double)width;
				final double right = 0.5 * (double)width;
				final double bottom = -0.5 * (double)height;
				final double top = 0.5 * (double)height;
				final double scale = getZoomFactor() * scene.getUnit() / getResolution();
				final double near = _frontClipDistance * scale;
				final double far = _backClipDistance * scale;

				gl2.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
				gl2.glLoadIdentity();
				gl2.glOrtho( left, right, bottom, top, near, far );
				gl2.glScaled( scale, scale, scale );
			}
			else if ( projectionPolicy == ProjectionPolicy.PERSPECTIVE )
			{
				final double fov = Math.toDegrees( getFieldOfView() );
				final double near = _frontClipDistance;
				final double far = _backClipDistance;

				/* Setup the projection matrix. */
				gl2.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
				gl2.glLoadIdentity();

				final GLU glu = new GLU();
				glu.gluPerspective( fov, 1.0, near, far );
				gl.glHint( GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST );

				gl2.glScaled( 1.0, aspect, 1.0 );
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
		}
	}

	/**
	 * Renders the scene.
	 *
	 * @param gl OpenGL pipeline.
	 */
	private void renderScene( final GL gl )
	{
		final Scene scene = getScene();

		/* Setup initial style and apply style filters to this view. */
		final RenderStyle defaultStyle = new RenderStyle();
		final Collection<RenderStyleFilter> styleFilters = getRenderStyleFilters();
		final RenderStyle viewStyle = defaultStyle.applyFilters( styleFilters, this );

		/* Apply view transform. */
		final GL2 gl2 = gl.getGL2();
		gl2.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl2.glLoadIdentity();
		JOGLTools.glMultMatrixd( gl, getScene2View() );

		final JOGLRenderer renderer = getOrCreateRenderer( gl, false );
		renderer.setSceneToViewTransform( getScene2View() );
		renderer.renderScene( scene, styleFilters, viewStyle, getBackground(), getGrid() );
	}

	/**
	 * Creates a renderer for the given OpenGL pipeline or returns an existing
	 * one.
	 *
	 * @param gl         OpenGL pipeline.
	 * @param alwaysInit Always call {@link JOGLRenderer#init()} vs. only if a new
	 *                   renderer was created.
	 *
	 * @return New or existing renderer.
	 */
	private JOGLRenderer getOrCreateRenderer( final GL gl, final boolean alwaysInit )
	{
		JOGLRenderer renderer = _renderer;
		if ( renderer == null )
		{
			final TextureCache textureCache = _joglEngine.getTextureCache();
			textureCache.setAsynchronous( false );

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
}
