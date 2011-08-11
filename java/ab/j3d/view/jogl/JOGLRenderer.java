/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.media.opengl.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import com.numdata.oss.ensemble.*;
import com.sun.opengl.util.*;
import com.sun.opengl.util.texture.*;
import org.jetbrains.annotations.*;

/**
 * Renderer implemented using JOGL.
 *
 * @author  Peter S. Heijnen
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class JOGLRenderer
{
	/**
	 * If enabled, objects are drawn with lines for face and vertex normals.
	 */
	private static final boolean DRAW_NORMALS = false;

	/**
	 * Texture unit used for color maps.
	 */
	static final int TEXTURE_UNIT_COLOR = GL.GL_TEXTURE0;

	/**
	 * Texture unit used for bump maps.
	 */
	static final int TEXTURE_UNIT_BUMP = GL.GL_TEXTURE1;

	/**
	 * Texture unit used for environment maps (reflections).
	 */
	static final int TEXTURE_UNIT_ENVIRONMENT = GL.GL_TEXTURE2;

	/**
	 * Texture unit used for shadow mapping.
	 */
	static final int TEXTURE_UNIT_SHADOW = GL.GL_TEXTURE7;

	/**
	 * OpenGL pipeline.
	 */
	private final GL _gl;

	/**
	 * Encapsulates complex and/or cacheable OpenGL state changes.
	 */
	private GLStateHelper _state;

	/**
	 * Texture cache.
	 */
	private final TextureCache _textureCache;

	/**
	 * Scene to view transformation.
	 */
	private Matrix3D _sceneToView;

	/**
	 * View to scene transformation.
	 */
	private Matrix3D _viewToScene;

	/**
	 * Scene to view transformation, excluding any translation components.
	 * This transformation is used for the sky box.
	 */
	private Matrix3D _sceneToViewRotation;

	/**
	 * View to scene transformation, excluding any translation components.
	 * This transformation is used for environment mapping.
	 */
	private Matrix3D _viewToSceneRotation;

	/**
	 * Manages shader programs.
	 */
	private ShaderManager _shaderManager;

	/**
	 * Specifies which objects should be rendered during the current rendering
	 * pass when performing multi-pass rendering.
	 */
	private MultiPassRenderMode _renderMode = MultiPassRenderMode.ALL;

	/**
	 * Rendering configuration to be used.
	 */
	private final JOGLConfiguration _configuration;

	/**
	 * Available rendering capabilities.
	 */
	private JOGLCapabilities _capabilities = null;

	/**
	 * Size of the shadow map texture (both width and height), in pixels.
	 */
	private final int _shadowSize = 512;

	/**
	 * Set to <code>true</code> while a shadow map is being rendered.
	 */
	private boolean _shadowPass;

	/**
	 * Serves as a fake accumulation buffer. Avoiding a real accumulation buffer
	 * (glAccum) allows for FSAA to work.
	 */
	private int _accumulationTexture;

	/**
	 * Whether color should be rendered for shadow maps, instead of only depth.
	 * (Useful for debugging only.)
	 */
	private static final boolean DEBUG_RENDER_SHADOW_MAP = false;

	/**
	 * Shadow map instance, reused to render all shadow maps.
	 */
	private ShadowMap _shadowMap;

	/**
	 * Indicates whether the first lighting pass is currently being rendered.
	 * Always <code>true</code> when using single-pass lighting.
	 */
	private boolean _renderUnlit = false;

	/**
	 * Manages geometry for objects in the scene.
	 */
	private GeometryObjectManager _geometryObjectManager = new GeometryObjectManager();

	/**
	 * Projector to be used for view frustum culling.
	 */
	private View3D _view;

	/**
	 * Specifies which objects should be rendered during the current rendering
	 * pass when performing multi-pass rendering.
	 */
	private enum MultiPassRenderMode
	{
		/** Render all faces.              */ ALL,
		/** Render only opaque faces.      */ OPAQUE_ONLY,
		/** Render only transparent faces. */ TRANSPARENT_ONLY
	}

	/**
	 * Keeps track of various statistics about the rendering process.
	 */
	@Nullable
	private RenderStatistics _statistics;

	/**
	 * Environment map for {@link #renderEnvironment()}.
	 */
	private SingleImageCubeMap _environmentMap = null;

	/**
	 * Construct new JOGL renderer.
	 *
	 * @param   gl              GL pipeline.
	 * @param   configuration   Specifies which OpenGL capabilities should be
	 *                          used, if available.
	 * @param   textureCache    Map containing {@link Texture}s used in the scene.
	 * @param   view            View to be rendered.
	 */
	public JOGLRenderer( final GL gl, final JOGLConfiguration configuration, final TextureCache textureCache, final View3D view )
	{
		_gl = gl;
		_state = null;

		_textureCache = textureCache;
		_configuration = configuration;
		_view = view;

		_shaderManager = null;

		_sceneToView = Matrix3D.IDENTITY;
		_viewToScene = Matrix3D.IDENTITY;
		_sceneToViewRotation = Matrix3D.IDENTITY;
		_viewToSceneRotation = Matrix3D.IDENTITY;

		_shadowMap = null;
		_shadowPass = false;
		_accumulationTexture = -1;

		_statistics = null;
	}

	/**
	 * Creates the OpenGL state helper for the renderer.
	 *
	 * @param   gl  OpenGL interface.
	 *
	 * @return  Create helper.
	 */
	private static GLStateHelper createGLStateHelper( final GL gl )
	{
		return new CachingGLStateHelper( gl );
	}

	/**
	 * Initialize GL context.
	 */
	public void init()
	{
		final GL gl = _gl;
		final GLStateHelper state = createGLStateHelper( gl );
		_state = state;

		/* Enable depth buffering. */
		state.setEnabled( GL.GL_DEPTH_TEST, true );
		gl.glDepthMask( true );
		gl.glDepthFunc ( GL.GL_LEQUAL );

		/* Enable polygon offsets. */
		state.setEnabled( GL.GL_POLYGON_OFFSET_FILL, true );
		state.setEnabled( GL.GL_POLYGON_OFFSET_LINE, true );
		state.setEnabled( GL.GL_POLYGON_OFFSET_POINT, true );

		/* Normalize lighting normals after scaling */
		state.setEnabled( GL.GL_NORMALIZE, true );

		_textureCache.init();

		final JOGLConfiguration configuration = _configuration;
		final JOGLCapabilities  capabilities  = new JOGLCapabilities( GLContext.getCurrent() );
		capabilities.printSummary( System.out );
		_capabilities = capabilities;

		final ShaderManager shaderManager;
		{
			ShaderImplementation shaderImplementation = null;

			if ( configuration.isPerPixelLightingEnabled() ||
			     configuration.isShadowEnabled() )
			{
				if ( capabilities.isShaderSupported() )
				{
					shaderImplementation = new CoreShaderImplementation();
				}
				else if ( capabilities.isShaderSupportedARB() )
				{
					shaderImplementation = new ARBShaderImplementation();
				}
			}

			shaderManager = new ShaderManager( shaderImplementation );
		}

		if ( shaderManager.isShaderSupportAvailable() )
		{
			try
			{
				shaderManager.init();
			}
			catch ( IOException e )
			{
				e.printStackTrace();
				shaderManager.disableShaders();
			}
		}

		_shaderManager = shaderManager;

		/* Set Light Model to two sided lighting. */
		gl.glLightModeli( GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE );

		/* Set local view point */
		gl.glLightModeli( GL.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE );

		/* Apply specular hightlight after texturing (otherwise, this would be done before texturing, so we won't see it). */
		if ( gl.isExtensionAvailable( "GL_VERSION_1_2" ) )
		{
			gl.glLightModeli( GL.GL_LIGHT_MODEL_COLOR_CONTROL, GL.GL_SEPARATE_SPECULAR_COLOR );
		}

		final ShadowMap shadowMap = _shadowMap;
		if ( shadowMap != null )
		{
			shadowMap.init( gl );
		}
	}

	/**
	 * Returns the shader manager.
	 *
	 * @return  Shader manager.
	 */
	public ShaderManager getShaderManager()
	{
		return _shaderManager;
	}

	/**
	 * Returns statistics about the rendering process. Statistics are only kept
	 * once this method has been called.
	 *
	 * @return  Rendering statistics.
	 */
	@Nullable
	public RenderStatistics getStatistics()
	{
		RenderStatistics result = _statistics;
		if ( result == null )
		{
			result = new RenderStatistics();
			_statistics = result;
		}

		return result;
	}

	/**
	 * Returns whether reflections are requested and the required OpenGL
	 * capabilities are supported.
	 *
	 * @return  <code>true</code> if reflections are enabled.
	 */
	private boolean isReflectionsEnabled()
	{
		return _configuration.isReflectionMapsEnabled() &&
		       _capabilities.isCubeMapSupported() &&
		       _capabilities.getMaxTextureUnits() >= 3;
	}

	/**
	 * Returns whether multi-pass lighting is supported and enabled.
	 *
	 * @return  <code>true</code> if multi-pass lighting is enabled.
	 */
	private boolean isMultiPassLightingEnabled()
	{
		return _shaderManager.isShaderSupportAvailable() &&
		       _configuration.isShadowEnabled() &&
		       _capabilities.isNonPowerOfTwoSupported() &&
		       _capabilities.isTextureRectangleSupported();
	}

	/**
	 * Returns whether any light in the given scene is casting shadows.
	 *
	 * @param   scene   Scene to be checked.
	 *
	 * @return  <code>true</code> if there is at least one shadow casting light.
	 */
	public static boolean isAnyLightCastingShadows( final Scene scene )
	{
		return !scene.walk( SHADOW_CASTING_LIGHT_VISITOR );
	}

	/**
	 * This visitor is used by {@link #isAnyLightCastingShadows} to detect
	 * lights that cast a shadow. It aborts if such a light is encountered.
	 */
	private static final Node3DVisitor SHADOW_CASTING_LIGHT_VISITOR = new Node3DVisitor()
	{
		@Override
		public boolean visitNode( @NotNull final Node3DPath path )
		{
			final Node3D node = path.getNode();
			return !( ( node instanceof Light3D ) && ( (Light3D) node ).isCastingShadows() );
		}
	};

	/**
	 * Sets the scene to view transformation. The inverse of this transformation
	 * is used for environment mapping, i.e. to make the environment stationary
	 * with respect the the world instead of the camera.
	 *
	 * @param   sceneToView     Scene to view transformation.
	 */
	public void setSceneToViewTransform( final Matrix3D sceneToView )
	{
		final Matrix3D viewToScene = sceneToView.inverse();

		_sceneToView = sceneToView;
		_viewToScene = viewToScene;

		_sceneToViewRotation = sceneToView.setTranslation( 0.0, 0.0, 0.0 );
		_viewToSceneRotation = viewToScene.setTranslation( 0.0, 0.0, 0.0 );
	}

	/**
	 * Releases any resources used by the renderer.
	 */
	public void dispose()
	{
		_shaderManager.dispose();
		_geometryObjectManager.dispose();
	}

	/**
	 * Render a scene.
	 *
	 * @param   scene           Scene to be rendered.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 * @param   background      Background to be rendered.
	 * @param   grid            Grid to be rendered (when enabled).
	 */
	public void renderScene( final Scene scene, final Collection<RenderStyleFilter> styleFilters, final RenderStyle sceneStyle, final Background background, final Grid grid )
	{
		final RenderStatistics statistics = _statistics;

		_state = createGLStateHelper( _gl );

		final boolean hasLights = !scene.walk( new Node3DVisitor()
		{
			@Override
			public boolean visitNode( @NotNull final Node3DPath path )
			{
				return !( path.getNode() instanceof Light3D );
			}
		} );

		if ( hasLights && isMultiPassLightingEnabled() )
		{
			renderSceneMultiPass( scene, styleFilters, sceneStyle, background, grid );
		}
		else
		{
			renderSceneSinglePass( scene, styleFilters, sceneStyle, background, grid );
		}

		if ( statistics != null )
		{
			System.out.println( "----------frameRendered" );
			statistics.frameRendered();
		}

		_geometryObjectManager.frameRendered();
	}

	/**
	 * Render a scene, with lights rendered in multiple passes.
	 *
	 * @param   scene           Scene to be rendered.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 * @param   background      Background to be rendered.
	 * @param   grid            Grid to be rendered (when enabled).
	 */
	public void renderSceneMultiPass( final Scene scene, final Collection<RenderStyleFilter> styleFilters, final RenderStyle sceneStyle, final Background background, final Grid grid )
	{
		final GL gl = _gl;
		final GLStateHelper state = _state;

		/*
		 * Set texture matrices to identity matrix (this should already be the
		 * case by default, but some OpenGL drivers seem to think otherwise).
		 */
		final ShaderManager shaderManager = _shaderManager;
		if ( shaderManager.isShaderSupportAvailable() || isReflectionsEnabled() )
		{
			gl.glMatrixMode( GL.GL_TEXTURE );
			for ( int i = 2 ; i >= 0 ; i-- )
			{
				gl.glActiveTexture( GL.GL_TEXTURE0 + i );
				gl.glLoadIdentity();
			}
			gl.glMatrixMode( GL.GL_MODELVIEW );
		}

		/*
		 * Create or update accumulation texture.
		 */
		final int[] viewport = new int[ 4 ];
		gl.glGetIntegerv( GL.GL_VIEWPORT, viewport, 0 );
		final int width  = viewport[ 2 ];
		final int height = viewport[ 3 ];

		int accumulationTexture = _accumulationTexture;
		boolean createAccumulationTexture = ( accumulationTexture == -1 );

		if ( !createAccumulationTexture )
		{
			final int[] textureWidthHeight = new int[2];
			gl.glGetTexLevelParameteriv( GL.GL_TEXTURE_2D, 0, GL.GL_TEXTURE_WIDTH, textureWidthHeight, 0 );
			gl.glGetTexLevelParameteriv( GL.GL_TEXTURE_2D, 0, GL.GL_TEXTURE_HEIGHT, textureWidthHeight, 1 );
			createAccumulationTexture = ( ( textureWidthHeight[ 0 ] != width ) || ( textureWidthHeight[ 1 ] != height ) );
		}

		if ( createAccumulationTexture )
		{
			final int[] textures = new int[ 1 ];

			if ( accumulationTexture != -1 )
			{
				textures[ 0 ] = accumulationTexture;
				gl.glDeleteTextures( 1, textures, 0 );
			}

			gl.glGenTextures( textures.length, textures, 0 );
			accumulationTexture = textures[ 0 ];

			gl.glBindTexture( GL.GL_TEXTURE_2D, accumulationTexture );
			gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_INT, null );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE );
			_accumulationTexture = accumulationTexture;
		}

		/*
		 * Render scene per light.
		 */
		final Node3DCollector lightCollector = new Node3DCollector( Light3D.class );
		scene.walk( lightCollector );
		final List<Node3DPath> lightPaths = lightCollector.getCollectedNodes();
		if ( lightPaths.isEmpty() )
		{
			throw new IllegalStateException( "Can't render multi-pass: there are no lights." );
		}

		shaderManager.setMultiPassLightingEnabled( true );

		for ( int i = 0; i < lightPaths.size(); i++ )
		{
			_renderUnlit = ( i == 0 );

			final Node3DPath path = lightPaths.get( i );
			final Light3D light = (Light3D) path.getNode();
			final Matrix3D lightTransform = path.getTransform();
			final boolean castingShadows = shaderManager.isShaderSupportAvailable() && _configuration.isShadowEnabled() && light.isCastingShadows();

			/*
			 * Shadow mapping pass (optional).
			 */
			ShadowMap shadowMap = null;
			if ( castingShadows )
			{
				shadowMap = _shadowMap;
				if ( shadowMap == null )
				{
					shadowMap = new ShadowMap( _shadowSize, DEBUG_RENDER_SHADOW_MAP );
					shadowMap.init( gl );
					_shadowMap = shadowMap;
				}

				shadowMap.setLight( light, lightTransform );
				shadowMap.begin( gl, scene );

				// Render to depth texture.
				_shadowPass = true;
				renderContentNodes( scene.getContentNodes(), styleFilters, sceneStyle );
				_shadowPass = false;

				shadowMap.end( gl );
			}

			/*
			 * Render from camera.
			 */

			if ( castingShadows )
			{
				// Bind shadow map.
				gl.glActiveTexture( TEXTURE_UNIT_SHADOW );
				gl.glBindTexture( GL.GL_TEXTURE_2D, shadowMap.getDepthTexture() );
				gl.glMatrixMode( GL.GL_TEXTURE );
				shadowMap.loadProjectionMatrix( gl );
				JOGLTools.glMultMatrixd( gl, _viewToScene );
				gl.glMatrixMode( GL.GL_MODELVIEW );
				gl.glActiveTexture( GL.GL_TEXTURE0 );
			}

			/*
			 * Render background during first pass. Use a black background for
			 * the remaining passes, needed for additive blending.
			 */
			if ( i == 0 )
			{
				state.setEnabled( GL.GL_LIGHTING, false );
				renderBackground( background );
				state.setEnabled( GL.GL_LIGHTING, true );
				gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT, new float[] { scene.getAmbientRed(), scene.getAmbientGreen(), scene.getAmbientBlue(), 1.0f }, 0 );
			}
			else
			{
				gl.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
				gl.glClearDepth( 1.0 );
				gl.glClear( GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT );
				gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT, new float[] { 0.0f, 0.0f, 0.0f, 1.0f }, 0 );
			}

			/*
			 * Render from the camera.
			 */
			gl.glMatrixMode( GL.GL_MODELVIEW );
			gl.glLoadIdentity();
			JOGLTools.glMultMatrixd( gl, _sceneToView );

			/*
			 * Render scene content with only the current light enabled.
			 */
			renderLight( GL.GL_LIGHT0, light, lightTransform );
			shaderManager.setShadowsEnabled( castingShadows );
			renderContentNodes( scene.getContentNodes(), styleFilters, sceneStyle );

			/*
			 * Add previous rendering passes.
			 */
			if ( i > 0 )
			{
				state.setEnabled( GL.GL_LIGHTING, false );
				gl.glBindTexture( GL.GL_TEXTURE_2D, accumulationTexture );
				state.setBlendFunc( GL.GL_ONE, GL.GL_ONE );
				state.setEnabled( GL.GL_BLEND, true );
				JOGLTools.renderToScreen( gl, accumulationTexture, -1.0f, -1.0f, 1.0f, 1.0f );
				state.setEnabled( GL.GL_BLEND, false );
				state.setBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
			}

			/*
			 * Render grid in final passes, such that it will properly occlude
			 * the objects behind it.
			 */
			if ( i == lightPaths.size() - 1 )
			{
				if ( grid.isEnabled() )
				{
					renderGrid( grid );
				}
			}

			/*
			 * DEBUG: Render shadow map color/depth texture to screen.
			 */
			if ( ( shadowMap != null ) && DEBUG_RENDER_SHADOW_MAP )
			{
				gl.glBindTexture( GL.GL_TEXTURE_2D, shadowMap.getDepthTexture() );
				gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_COMPARE_MODE, GL.GL_NONE );
				JOGLTools.renderToScreen( gl, shadowMap.getDepthTexture(), -1.0f, -1.0f, -0.5f, -0.5f );
				gl.glBindTexture( GL.GL_TEXTURE_2D, shadowMap.getDepthTexture() );
				gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_COMPARE_MODE, GL.GL_COMPARE_R_TO_TEXTURE );
				JOGLTools.renderToScreen( gl, shadowMap.getColorTexture(), 0.5f, -1.0f, 1.0f, -0.5f );
			}

			if ( i < lightPaths.size() - 1 )
			{
				/*
				 * Copy to texture.
				 */
				gl.glBindTexture( GL.GL_TEXTURE_2D, accumulationTexture );
				gl.glCopyTexSubImage2D( GL.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height );
			}
		}
	}

	/**
	 * Render a scene, with lights rendered in multiple passes.
	 *
	 * @param   scene           Scene to be rendered.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 * @param   background      Background to be rendered.
	 * @param   grid            Grid to be rendered (when enabled).
	 */
	public void renderSceneSinglePass( final Scene scene, final Collection<RenderStyleFilter> styleFilters, final RenderStyle sceneStyle, final Background background, final Grid grid )
	{
		// TODO: Support single-pass shadow mapping.

		final ShaderManager shaderManager = _shaderManager;
		shaderManager.setShadowsEnabled( false );
		shaderManager.setMultiPassLightingEnabled( false );

		final GL gl = _gl;
		_renderUnlit = true;

		/*
		 * Set texture matrices to identity matrix (this should already be the
		 * case by default, but some OpenGL drivers seem to think otherwise).
		 */
		if ( shaderManager.isShaderSupportAvailable() || isReflectionsEnabled() )
		{
			gl.glMatrixMode( GL.GL_TEXTURE );
			for ( int i = 2 ; i >= 0 ; i-- )
			{
				gl.glActiveTexture( GL.GL_TEXTURE0 + i );
				gl.glLoadIdentity();
			}
			gl.glMatrixMode( GL.GL_MODELVIEW );
		}

		/*
		 * Render background.
		 */
		final GLStateHelper state = _state;
		state.setEnabled( GL.GL_LIGHTING, false );
		renderBackground( background );
		gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT, new float[] { scene.getAmbientRed(), scene.getAmbientGreen(), scene.getAmbientBlue(), 1.0f }, 0 );
		state.setEnabled( GL.GL_LIGHTING, true );

		/*
		 * Enable lights.
		 */
		scene.walk( new Node3DVisitor()
		{
			/**
			 * Current light number(0=first).
			 */
			int _lightNumber = 0;

			/**
			 * Maximum supported number of lights (typically 8).
			 */
			final int _maxlights = getMaxLights();

			@Override
			public boolean visitNode( @NotNull final Node3DPath path )
			{
				final boolean result;

				final Node3D node = path.getNode();
				if ( node instanceof Light3D )
				{
					int lightNumber = _lightNumber;
					renderLight( GL.GL_LIGHT0 + lightNumber, (Light3D) node, path.getTransform() );
					_lightNumber = ++lightNumber;
					result = ( lightNumber < _maxlights );
				}
				else
				{
					result = true;
				}

				return result;
			}
		} );

		/*
		 * Render from the camera.
		 */
		gl.glMatrixMode( GL.GL_MODELVIEW );
		gl.glLoadIdentity();
		JOGLTools.glMultMatrixd( gl, _sceneToView );
		renderContentNodes( scene.getContentNodes(), styleFilters, sceneStyle );

		/*
		 * Render grid.
		 */
		if ( grid.isEnabled() )
		{
			gl.glMatrixMode( GL.GL_MODELVIEW );
			renderGrid( grid );
		}
	}

	/**
	 * Renders the given background.
	 *
	 * @param   background  Background to be rendered.
	 */
	private void renderBackground( final Background background )
	{
		final GL gl = _gl;
		final GLStateHelper state = _state;

		/* Clear depth and color buffer. */
		final Color backgroundColor = background.getColor();
		final float[] backgroundRGB = backgroundColor.getRGBColorComponents( null );
		gl.glClearColor( backgroundRGB[ 0 ], backgroundRGB[ 1 ], backgroundRGB[ 2 ], 1.0f );
		gl.glClearDepth( 1.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT );

		final List<Color> gradient = background.getGradient();
		if ( !gradient.isEmpty() )
		{
			state.setEnabled( GL.GL_CULL_FACE, false );
			state.setEnabled( GL.GL_DEPTH_TEST, false );

			gl.glMatrixMode( GL.GL_PROJECTION );
			gl.glPushMatrix();
			gl.glLoadIdentity();

			gl.glMatrixMode( GL.GL_MODELVIEW );
			gl.glPushMatrix();
			gl.glLoadIdentity();

			gl.glBegin( GL.GL_QUADS );
			state.setColor( gradient.get( 0 % gradient.size() ) );
			gl.glVertex2d( -1.0, -1.0 );
			state.setColor( gradient.get( 1 % gradient.size() ) );
			gl.glVertex2d( 1.0, -1.0 );
			state.setColor( gradient.get( 2 % gradient.size() ) );
			gl.glVertex2d( 1.0, 1.0 );
			state.setColor( gradient.get( 3 % gradient.size() ) );
			gl.glVertex2d( -1.0, 1.0 );
			gl.glEnd();

			gl.glPopMatrix();
			gl.glMatrixMode( GL.GL_PROJECTION );
			gl.glPopMatrix();
			gl.glMatrixMode( GL.GL_MODELVIEW );

			state.setEnabled( GL.GL_DEPTH_TEST, true );
			state.setEnabled( GL.GL_CULL_FACE, true );
		}

		final TextureMap image = background.getImage();
		if ( image != null )
		{
			final Texture texture = _textureCache.getTexture( image );
			if ( texture != null )
			{
				state.setEnabled( GL.GL_CULL_FACE, false );
				state.setEnabled( GL.GL_DEPTH_TEST, false );

				texture.enable();
				texture.bind();

				gl.glMatrixMode( GL.GL_PROJECTION );
				gl.glPushMatrix();
				gl.glLoadIdentity();

				gl.glMatrixMode( GL.GL_MODELVIEW );
				gl.glPushMatrix();
				gl.glLoadIdentity();

				final int[] viewport = new int[ 4 ];
				gl.glGetIntegerv( GL.GL_VIEWPORT, viewport, 0 );
				final int width  = viewport[ 2 ];
				final int height = viewport[ 3 ];

				final float viewAspect = (float)width / (float)height;
				final float textureAspect = texture.getAspectRatio();
				gl.glScalef( textureAspect, viewAspect, 1.0f );

				final double xo = background.getCenterX();
				final double yo = background.getCenterY();

				final double x1 = xo + -1.0;
				final double x2 = xo + 1.0;
				final double y1 = yo + -1.0;
				final double y2 = yo + 1.0;

				state.setColor( 1.0f, 1.0f, 1.0f, 1.0f );
				gl.glBegin( GL.GL_QUADS );
				gl.glTexCoord2d( 0.0, 1.0 );
				gl.glVertex2d( x1, y1 );
				gl.glTexCoord2d( 1.0, 1.0 );
				gl.glVertex2d( x2, y1 );
				gl.glTexCoord2d( 1.0, 0.0 );
				gl.glVertex2d( x2, y2 );
				gl.glTexCoord2d( 0.0, 0.0 );
				gl.glVertex2d( x1, y2 );
				gl.glEnd();

				gl.glPopMatrix();
				gl.glMatrixMode( GL.GL_PROJECTION );
				gl.glPopMatrix();
				gl.glMatrixMode( GL.GL_MODELVIEW );

				texture.disable();

				state.setEnabled( GL.GL_DEPTH_TEST, true );
				state.setEnabled( GL.GL_CULL_FACE, true );
			}
		}

//		renderEnvironment();
	}

	/**
	 * Renders an environment surrounding the entire scene using a cube map.
	 * This is currently not used, but provided for future reference.
	 */
	private void renderEnvironment()
	{
		SingleImageCubeMap environmentMap = _environmentMap;
		if ( environmentMap == null )
		{
			environmentMap = new SingleImageCubeMap( MapTools.loadImage( "ab3d/maps/reflect-sky-bw" ) );
			_environmentMap = environmentMap;
		}

		final Texture cubeMapTexture = _textureCache.getCubeMap( environmentMap );
		if ( cubeMapTexture != null )
		{
			final GL gl = _gl;
			final GLStateHelper state = _state;

			state.setEnabled( GL.GL_DEPTH_TEST, false );
			state.setEnabled( GL.GL_CULL_FACE, false );

			gl.glMatrixMode( GL.GL_MODELVIEW );
			gl.glPushMatrix();
			gl.glLoadIdentity();
			JOGLTools.glMultMatrixd( gl, _sceneToViewRotation );

			cubeMapTexture.bind();
			cubeMapTexture.enable();

			gl.glTexGeni( GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR );
			gl.glTexGeni( GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR );
			gl.glTexGeni( GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR );
			gl.glTexGenfv( GL.GL_S, GL.GL_OBJECT_PLANE, new float[] { 1.0f, 0.0f, 0.0f, 1.0f }, 0 );
			gl.glTexGenfv( GL.GL_T, GL.GL_OBJECT_PLANE, new float[] { 0.0f, 1.0f, 0.0f, 1.0f }, 0 );
			gl.glTexGenfv( GL.GL_R, GL.GL_OBJECT_PLANE, new float[] { 0.0f, 0.0f, 1.0f, 1.0f }, 0 );
			state.setEnabled( GL.GL_TEXTURE_GEN_S, true );
			state.setEnabled( GL.GL_TEXTURE_GEN_T, true );
			state.setEnabled( GL.GL_TEXTURE_GEN_R, true );

			final GLUT glut = new GLUT();
			state.setColor( 1.0f, 1.0f, 1.0f, 1.0f );
			glut.glutSolidCube( 1000.0f );

			state.setEnabled( GL.GL_TEXTURE_GEN_S, false );
			state.setEnabled( GL.GL_TEXTURE_GEN_T, false );
			state.setEnabled( GL.GL_TEXTURE_GEN_R, false );

			cubeMapTexture.disable();

			gl.glPopMatrix();

			state.setEnabled( GL.GL_CULL_FACE, true );
			state.setEnabled( GL.GL_DEPTH_TEST, true );
		}
	}

	/**
	 * Renders the given content nodes applying render styles as specified.
	 *
	 * @param   nodes           Nodes to be rendered.
	 * @param   styleFilters    Render style filters to be applied.
	 * @param   sceneStyle      Base render style for the entire scene.
	 */
	public void renderContentNodes( final List<ContentNode> nodes, final Collection<RenderStyleFilter> styleFilters, final RenderStyle sceneStyle )
	{
		final GL gl = _gl;

		_state.setEnabled( GL.GL_CULL_FACE, true );

		if ( _shadowPass )
		{
			gl.glCullFace( GL.GL_FRONT );

			_renderMode = MultiPassRenderMode.ALL;
			renderObjects( nodes, styleFilters, sceneStyle );
		}
		else
		{
			gl.glCullFace( GL.GL_BACK );

			_shaderManager.enable();

			_renderMode = MultiPassRenderMode.OPAQUE_ONLY;
			renderObjects( nodes, styleFilters, sceneStyle );

			_renderMode = MultiPassRenderMode.TRANSPARENT_ONLY;
			renderObjects( nodes, styleFilters, sceneStyle );

			_shaderManager.disable();
		}
	}

	/**
	 * Render objects in scene.
	 *
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	private void renderObjects( final List<ContentNode> nodes, final Collection<RenderStyleFilter> styleFilters, final RenderStyle sceneStyle )
	{
		final boolean shadowPass = _shadowPass;

		final Map<Duet<Object3D, RenderStyle>, List<Node3DPath>> objectGroups = new LinkedHashMap<Duet<Object3D, RenderStyle>, List<Node3DPath>>();

		for ( final ContentNode node : nodes )
		{
			if ( shadowPass && !node.isCastingShadows() )
			{
				continue;
			}

			final RenderStyle nodeStyle = sceneStyle.applyFilters( styleFilters, node );

			final Node3DTreeWalker treeWalker = new LevelOfDetailTreeWalker();
			treeWalker.walkNode( new Node3DVisitor()
			{
				@Override
				public boolean visitNode( @NotNull final Node3DPath path )
				{
					final Node3D node = path.getNode();
					if ( node instanceof Object3D )
					{
						final Object3D object = (Object3D)node;

						final View3D view = _view;
						final boolean visibleByCamera = view.isVisible( path.getTransform(), object );

						if ( shadowPass || visibleByCamera )
						{
							final RenderStyle objectStyle = nodeStyle.applyFilters( styleFilters, path );
							final BasicDuet<Object3D, RenderStyle> key = new BasicDuet<Object3D, RenderStyle>( object, objectStyle );

							List<Node3DPath> objectGroup = objectGroups.get( key );
							if ( objectGroup == null )
							{
								objectGroup = new ArrayList<Node3DPath>();
								objectGroups.put( key, objectGroup );
							}

							objectGroup.add( path );
						}
					}
					return true;
				}
			}, node.getTransform(), node.getNode3D() );
		}

		for ( final Map.Entry<Duet<Object3D, RenderStyle>, List<Node3DPath>> objectGroup : objectGroups.entrySet() )
		{
			final Duet<Object3D, RenderStyle> key = objectGroup.getKey();
			renderObject( key.getValue1(), objectGroup.getValue(), key.getValue2() );
		}
	}

	/**
	 * Returns the maximum number of lights supported by the OpenGL
	 * implementation. At least (and most commonly) 8 lights are supported.
	 *
	 * @return  Maximum number of lights.
	 */
	private int getMaxLights()
	{
		final int[] maxLights = new int[ 1 ];
		_gl.glGetIntegerv( GL.GL_MAX_LIGHTS, maxLights, 0 );
		return maxLights[ 0 ];
	}

	/**
	 * Renders the contents of the given texture to the viewport.
	 *
	 * @param   texture     Texture to be rendered.
	 */
	private void renderToViewport( final Texture texture )
	{
		final GL gl = _gl;

		texture.enable();
		texture.bind();
		toViewportSpace();

		final TextureCoords textureCoords = texture.getImageTexCoords();

		final float left   = textureCoords.left();
		final float bottom = textureCoords.bottom();
		final float right  = textureCoords.right();
		final float top    = textureCoords.top();

		_state.setColor( 1.0f, 1.0f, 1.0f, 1.0f );
		gl.glBegin( GL.GL_QUADS );
		gl.glMultiTexCoord2f( TEXTURE_UNIT_COLOR, left, bottom );
		gl.glVertex2d( -1.0, -1.0 );
		gl.glMultiTexCoord2f( TEXTURE_UNIT_COLOR, right, bottom );
		gl.glVertex2d( 1.0, -1.0 );
		gl.glMultiTexCoord2f( TEXTURE_UNIT_COLOR, right, top );
		gl.glVertex2d( 1.0, 1.0 );
		gl.glMultiTexCoord2f( TEXTURE_UNIT_COLOR, left, top );
		gl.glVertex2d( -1.0, 1.0 );
		gl.glEnd();

		fromViewportSpace();
		texture.disable();
	}

	/**
	 * Changes the projection and model-view transforms to viewport coordinates,
	 * ranging from -1 to 1. The current transforms are preserved and can be
	 * restored using {@link #fromViewportSpace()}.
	 */
	private void toViewportSpace()
	{
		final GL gl = _gl;
		gl.glMatrixMode( GL.GL_PROJECTION );
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode( GL.GL_MODELVIEW );
		gl.glPushMatrix();
		gl.glLoadIdentity();
	}

	/**
	 * Restores the projection and model-view transforms that were replaced by
	 * a previous call to {@link #toViewportSpace()}.
	 */
	private void fromViewportSpace()
	{
		final GL gl = _gl;
		gl.glMatrixMode( GL.GL_MODELVIEW );
		gl.glPopMatrix();
		gl.glMatrixMode( GL.GL_PROJECTION );
		gl.glPopMatrix();
		gl.glMatrixMode( GL.GL_MODELVIEW );
	}

	/**
	 * Renders small previews of the given textures on the screen, for debugging
	 * purposes.
	 *
	 * @param   textures    Texture to be rendered.
	 * @param   x           Horizontal position; <code>-1.0</code> for the left
	 *                      side of the screen, <code>1.0</code> for the right.
	 * @param   blend       Whether the texture should be alpha-blended.
	 */
	private void displayTextures( final Texture[] textures, final double x, final boolean blend )
	{
		final GL gl = _gl;

		/*
		 * Render one of the buffers to a small rectangle on screen.
		 */
		toViewportSpace();

		gl.glPushAttrib( GL.GL_ALL_ATTRIB_BITS ); // Don't use state helper until after 'glPopAttrib'!
		gl.glDisable( GL.GL_LIGHTING );

		if ( blend )
		{
			gl.glEnable( GL.GL_BLEND );
			gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
		}
		else
		{
			gl.glDisable( GL.GL_BLEND );
		}

		for ( int i = 0 ; i < textures.length ; i++ )
		{
			final Texture texture = textures[ i ];

			gl.glActiveTexture( TEXTURE_UNIT_COLOR );
			texture.enable();
			texture.bind();

			final double minX = x * 0.75 - 0.2;
			final double maxX = minX + 0.4;
			final double minY = -0.95 + 0.5 * (double)i;
			final double maxY = minY + 0.4;

			final TextureCoords textureCoords = texture.getImageTexCoords();

			final float left   = textureCoords.left();
			final float bottom = textureCoords.bottom();
			final float right  = textureCoords.right();
			final float top    = textureCoords.top();

			gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
			gl.glBegin( GL.GL_QUADS );
			gl.glMultiTexCoord2f( TEXTURE_UNIT_COLOR, left, bottom );
			gl.glVertex2d( minX, minY );
			gl.glMultiTexCoord2f( TEXTURE_UNIT_COLOR, right, bottom );
			gl.glVertex2d( maxX, minY );
			gl.glMultiTexCoord2f( TEXTURE_UNIT_COLOR, right, top );
			gl.glVertex2d( maxX, maxY );
			gl.glMultiTexCoord2f( TEXTURE_UNIT_COLOR, left, top );
			gl.glVertex2d( minX, maxY );
			gl.glEnd();
		}
		gl.glPopAttrib();

		fromViewportSpace();
	}

	/**
	 * Renders the given light.
	 *
	 * @param   lightNumber     OpenGL identifier for the light.
	 * @param   light           Light to be rendered.
	 * @param   light2world     Light to world transformation.
	 */
	private void renderLight( final int lightNumber, final Light3D light, final Matrix3D light2world )
	{
		final GL gl = _gl;

		gl.glLightfv( lightNumber, GL.GL_AMBIENT, new float[] {
		0.0f, 0.0f, 0.0f, 1.0f
		}, 0 );
		gl.glLightfv( lightNumber, GL.GL_DIFFUSE , new float[] { light.getDiffuseRed() , light.getDiffuseGreen() , light.getDiffuseBlue() , 1.0f }, 0 );
		gl.glLightfv( lightNumber, GL.GL_SPECULAR, new float[] { light.getSpecularRed(), light.getSpecularGreen(), light.getSpecularBlue(), 1.0f }, 0 );

		if ( light instanceof DirectionalLight3D )
		{
			final DirectionalLight3D directional = (DirectionalLight3D)light;
			final Vector3D direction = directional.getDirection();
			gl.glLightfv( lightNumber, GL.GL_POSITION, new float[] { -(float)direction.x, -(float)direction.y, -(float)direction.z, 0.0f }, 0 );
			gl.glLightf( lightNumber, GL.GL_CONSTANT_ATTENUATION , 1.0f );
			gl.glLightf( lightNumber, GL.GL_LINEAR_ATTENUATION   , 0.0f );
			gl.glLightf( lightNumber, GL.GL_QUADRATIC_ATTENUATION, 0.0f );
		}
		else
		{
			gl.glLightfv( lightNumber, GL.GL_POSITION, new float[]{ (float)light2world.xo, (float)light2world.yo, (float)light2world.zo, 1.0f }, 0 );
			gl.glLightf( lightNumber, GL.GL_CONSTANT_ATTENUATION , light.getConstantAttenuation()  );
			gl.glLightf( lightNumber, GL.GL_LINEAR_ATTENUATION   , light.getLinearAttenuation()    );
			gl.glLightf( lightNumber, GL.GL_QUADRATIC_ATTENUATION, light.getQuadraticAttenuation() );
		}

		if ( light instanceof SpotLight3D )
		{
			final SpotLight3D spot = (SpotLight3D)light;
			final Vector3D direction = light2world.rotate( spot.getDirection() );
			gl.glLightfv( lightNumber, GL.GL_SPOT_DIRECTION, new float[] { (float)direction.x   , (float)direction.y   , (float)direction.z           }, 0 );
			gl.glLightf ( lightNumber, GL.GL_SPOT_CUTOFF   , spot.getSpreadAngle() );
			gl.glLightf ( lightNumber, GL.GL_SPOT_EXPONENT , spot.getConcentration() );
		}
		else
		{
			gl.glLightf ( lightNumber, GL.GL_SPOT_CUTOFF   , 180.0f );
			gl.glLightf( lightNumber, GL.GL_SPOT_EXPONENT, 0.0f );
		}

		_state.setEnabled( lightNumber, true );
	}

	/**
	 * Renders the given object.
	 *
	 * @param   object          Object to be rendered.
	 * @param   paths           Node paths to the object.
	 * @param   objectStyle     Render style applied to the object.
	 */
	protected void renderObject( final Object3D object, final List<Node3DPath> paths, final RenderStyle objectStyle )
	{
		final boolean anyMaterialEnabled = objectStyle.isMaterialEnabled();
		final boolean anyFillEnabled     = objectStyle.isFillEnabled() && ( objectStyle.getFillColor() != null );
		final boolean anyStrokeEnabled   = objectStyle.isStrokeEnabled() && ( objectStyle.getStrokeColor() != null );
		final boolean anyVertexEnabled   = objectStyle.isVertexEnabled() && ( objectStyle.getVertexColor() != null );

		if ( anyMaterialEnabled || anyFillEnabled || anyStrokeEnabled || anyVertexEnabled )
		{
			final RenderStatistics statistics = _statistics;
			if ( statistics != null )
			{
				System.out.println( "objectRendered( " + object + " / " + object.getTag() + ", " + paths.size() + " )" );
				statistics.objectRendered( object, paths.size() );
			}

			if ( anyMaterialEnabled )
			{
				renderObjectMaterial( object, paths, objectStyle );
			}
			else if ( anyFillEnabled )
			{
				renderObjectFilled( object, paths, objectStyle );
			}

			if ( !_shadowPass )
			{
				if ( anyStrokeEnabled )
				{
					renderObjectStroked( object, paths, objectStyle );
				}

				if ( anyVertexEnabled )
				{
					renderObjectVertices( object, paths, objectStyle );
				}
			}
		}
	}

	/**
	 * Renders the given object with a material applied to it.
	 *
	 * @param   object          Object to be rendered.
	 * @param   paths           Node paths to the object.
	 * @param   objectStyle     Render style to be applied.
	 */
	private void renderObjectMaterial( @NotNull final Object3D object, @NotNull final List<Node3DPath> paths, @NotNull final RenderStyle objectStyle )
	{
		for ( final FaceGroup faceGroup : object.getFaceGroups() )
		{
			final Appearance appearance = faceGroup.getAppearance();

			if ( appearance != null )
			{
				final GL gl = _gl;
				final MultiPassRenderMode renderMode = _renderMode;

				/*
				 * Get textures.
				 */
				final TextureCache textureCache = _textureCache;

				final float extraAlpha = objectStyle.getMaterialAlpha();
				final float combinedAlpha = appearance.getDiffuseColorAlpha() * extraAlpha;
				final boolean isTransparent = ( combinedAlpha < 0.99f ) || textureCache.hasAlpha( appearance.getColorMap() );
				final boolean blend = ( renderMode != MultiPassRenderMode.OPAQUE_ONLY ) && isTransparent;

				if ( _shadowPass && ( appearance.getDiffuseColorAlpha() < 0.50f ) )
				{
					continue;
				}

				if ( ( ( renderMode != MultiPassRenderMode.OPAQUE_ONLY ) || !isTransparent ) &&
				     ( ( renderMode != MultiPassRenderMode.TRANSPARENT_ONLY ) || isTransparent ) )
				{
					final boolean hasLighting = objectStyle.isMaterialLightingEnabled();

					final ShaderManager shaderManager = _shaderManager;

					final Texture colorMap = textureCache.getColorMapTexture( appearance );
					final Texture bumpMap = shaderManager.isShaderSupportAvailable() && hasLighting ? textureCache.getBumpMapTexture( appearance ) : null;

					/*
					 * Set render/material properties.
					 */
					final GLStateHelper state = _state;
					if ( blend )
					{
						if ( combinedAlpha < 0.25f )
						{
							gl.glDepthMask( false );
						}
						state.setBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
					}

					state.setEnabled( GL.GL_BLEND, blend );
					state.setEnabled( GL.GL_LIGHTING, hasLighting );
					state.setAppearance( appearance, objectStyle, extraAlpha );

					/*
					 * Enable bump map.
					 */
					if ( bumpMap != null )
					{
						gl.glActiveTexture( TEXTURE_UNIT_BUMP );
						bumpMap.enable();
						bumpMap.bind();
						gl.glActiveTexture( TEXTURE_UNIT_COLOR );
					}

					final boolean reflectionsEnabled = isReflectionsEnabled();

					final ReflectionMap reflectionMap = appearance.getReflectionMap();
					final Texture reflectionTexture = reflectionsEnabled && ( reflectionMap != null ) ? textureCache.getCubeMap( reflectionMap ) : null;
					if ( reflectionTexture != null )
					{
						gl.glActiveTexture( TEXTURE_UNIT_ENVIRONMENT );
						reflectionTexture.enable();
						reflectionTexture.bind();

						if ( !shaderManager.isShaderSupportAvailable() )
						{
							/*
							 * Interpolate with previous texture stage.
							 */
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE );
							final float reflectivity = ( reflectionMap.getReflectivityMin() + reflectionMap.getReflectivityMax() ) / 2.0f;
							gl.glTexEnvfv( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, new float[] { 0.0f, 0.0f, 0.0f, reflectivity }, 0 );

							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_INTERPOLATE );
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE0_RGB, GL.GL_TEXTURE );
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE1_RGB, GL.GL_PREVIOUS );
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE2_RGB, GL.GL_CONSTANT );

							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_COMBINE_ALPHA, GL.GL_INTERPOLATE );
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE0_ALPHA, GL.GL_TEXTURE );
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE1_ALPHA, GL.GL_PREVIOUS );
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE2_ALPHA, GL.GL_CONSTANT );

							/*
							 * Generate reflection map UV coordinates.
							 */
							gl.glTexGeni( GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP );
							gl.glTexGeni( GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP );
							gl.glTexGeni( GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP );
							state.setEnabled( GL.GL_TEXTURE_GEN_S, true );
							state.setEnabled( GL.GL_TEXTURE_GEN_T, true );
							state.setEnabled( GL.GL_TEXTURE_GEN_R, true );
						}

						/*
						 * Inverse camera rotation.
						 */
						gl.glMatrixMode( GL.GL_TEXTURE );
						gl.glPushMatrix();
						JOGLTools.glMultMatrixd( gl, _viewToSceneRotation );
						gl.glMatrixMode( GL.GL_MODELVIEW );

						gl.glActiveTexture( TEXTURE_UNIT_COLOR );
					}

					/*
					 * Enable color map.
					 */
					if ( colorMap != null )
					{
						colorMap.enable();
						colorMap.bind();
					}

					if ( shaderManager != null )
					{
						shaderManager.setLightingEnabled( true );
						shaderManager.setTextureEnabled( colorMap != null );
						shaderManager.setReflectivity( reflectionMap );
					}

					/*
					 * Render faces.
					 */
					state.setEnabled( GL.GL_CULL_FACE, objectStyle.isBackfaceCullingEnabled() && !faceGroup.isTwoSided() );

					final GeometryObject geometryObject = _geometryObjectManager.getGeometryObject( faceGroup, GeometryType.FACES );
					for ( final Node3DPath path : paths )
					{
						final Matrix3D object2world = path.getTransform();
						gl.glPushMatrix();
						JOGLTools.glMultMatrixd( gl, object2world );
						geometryObject.draw();

						if ( DRAW_NORMALS )
						{
							renderFaceNormals( faceGroup.getFaces() );
						}

						gl.glPopMatrix();
					}

					/*
					 * Disable color map.
					 */
					if ( colorMap != null )
					{
						colorMap.disable();
					}

					/*
					 * Disable bump map.
					 */
					if ( bumpMap != null )
					{
						gl.glActiveTexture( TEXTURE_UNIT_BUMP );
						bumpMap.disable();
						gl.glActiveTexture( TEXTURE_UNIT_COLOR );
					}

					/*
					 * Disable reflection map.
					 */
					if ( reflectionTexture != null )
					{
						gl.glActiveTexture( TEXTURE_UNIT_ENVIRONMENT );

						gl.glMatrixMode( GL.GL_TEXTURE );
						gl.glPopMatrix();
						gl.glMatrixMode( GL.GL_MODELVIEW );

						if ( !shaderManager.isShaderSupportAvailable() )
						{
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE );

							state.setEnabled( GL.GL_TEXTURE_GEN_S, false );
							state.setEnabled( GL.GL_TEXTURE_GEN_T, false );
							state.setEnabled( GL.GL_TEXTURE_GEN_R, false );
						}

						reflectionTexture.disable();

						gl.glActiveTexture( TEXTURE_UNIT_COLOR );
					}

					if ( blend )
					{
						if ( combinedAlpha < 0.25f )
						{
							gl.glDepthMask( true );
						}
					}
				}
			}
		}
	}

	/**
	 * Renders the given object in a solid color.
	 *
	 * @param   object          Object to be rendered.
	 * @param   paths           Node paths to the object.
	 * @param   objectStyle     Render style to be applied.
	 */
	private void renderObjectFilled( @NotNull final Object3D object, @NotNull final List<Node3DPath> paths, @NotNull final RenderStyle objectStyle )
	{
		final MultiPassRenderMode renderMode = _renderMode;

		final GL gl = _gl;

		final Color   color           = objectStyle.getFillColor();
		final int     alpha           = color.getAlpha();
		final boolean blend           = ( renderMode != MultiPassRenderMode.OPAQUE_ONLY ) && ( alpha < 255 );
		final boolean hasLighting     = objectStyle.isFillLightingEnabled();

		if ( !hasLighting && !_renderUnlit )
		{
			gl.glColorMask( false, false, false, false );
		}

		if ( ( ( renderMode != MultiPassRenderMode.OPAQUE_ONLY      ) || ( alpha == 255 ) ) &&
		     ( ( renderMode != MultiPassRenderMode.TRANSPARENT_ONLY ) || ( alpha <  255 ) ) )
		{
			/*
			 * Set render/material properties.
			 */
			final GLStateHelper state = _state;
			if ( blend )
			{
				if ( alpha < 64 )
				{
					gl.glDepthMask( false );
				}
				state.setBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
			}

			state.setEnabled( GL.GL_BLEND, blend );
			state.setEnabled( GL.GL_LIGHTING, hasLighting );

			state.setColor( color );
			final ShaderManager shaderManager = _shaderManager;
			shaderManager.setLightingEnabled( hasLighting );
			shaderManager.setTextureEnabled( false );
			shaderManager.setReflectivity( null );

			/*
			 * Render faces.
			 */
			for ( final FaceGroup faceGroup : object.getFaceGroups() )
			{
				state.setEnabled( GL.GL_CULL_FACE, objectStyle.isBackfaceCullingEnabled() && !faceGroup.isTwoSided() );

				final GeometryObject geometryObject = _geometryObjectManager.getGeometryObject( faceGroup, GeometryType.FACES );
				for ( final Node3DPath path : paths )
				{
					final Matrix3D object2world = path.getTransform();
					gl.glPushMatrix();
					JOGLTools.glMultMatrixd( gl, object2world );
					geometryObject.draw();
					gl.glPopMatrix();
				}
			}

			if ( blend )
			{
				if ( alpha < 64 )
				{
					gl.glDepthMask( true );
				}
			}
		}

		if ( !hasLighting && !_renderUnlit )
		{
			gl.glColorMask( true, true, true, true );
		}
	}

	/**
	 * Renders the outlines of the given object.
	 *
	 * @param   object          Object to be rendered.
	 * @param   paths           Node paths to the object.
	 * @param   objectStyle     Render style to be applied.
	 */
	private void renderObjectStroked( final Object3D object, @NotNull final List<Node3DPath> paths, @NotNull final RenderStyle objectStyle )
	{
		final GL gl = _gl;

		final Color color = objectStyle.getStrokeColor();
		final float width = objectStyle.getStrokeWidth();
		final boolean hasLighting = objectStyle.isStrokeLightingEnabled();

		final boolean depthOnly = !hasLighting && !_renderUnlit;
		if ( depthOnly )
		{
			gl.glColorMask( false, false, false, false );
		}

		final GLStateHelper state = _state;
		state.setEnabled( GL.GL_BLEND, false );
		state.setEnabled( GL.GL_LIGHTING, hasLighting );
		state.setColor( color );

		gl.glLineWidth( width );

		final ShaderManager shaderManager = _shaderManager;
		shaderManager.setLightingEnabled( hasLighting );
		shaderManager.setTextureEnabled( false );
		shaderManager.setReflectivity( null );

		for ( final FaceGroup faceGroup : object.getFaceGroups() )
		{
			// FIXME: Backface culling doesn't work on vertices. Do it ourselves? (Shader?)
			final boolean backfaceCulling  = objectStyle.isBackfaceCullingEnabled() && !faceGroup.isTwoSided();

			final GeometryObject geometryObject = _geometryObjectManager.getGeometryObject( faceGroup, GeometryType.OUTLINES );
			for ( final Node3DPath path : paths )
			{
				final Matrix3D object2world = path.getTransform();
				gl.glPushMatrix();
				JOGLTools.glMultMatrixd( gl, object2world );
				geometryObject.draw();
				gl.glPopMatrix();
			}
		}

		if ( depthOnly )
		{
			gl.glColorMask( true, true, true, true );
		}
	}

	/**
	 * Renders the vertices of the given object.
	 *
	 * @param   object          Object to be rendered.
	 * @param   paths           Node paths to the object.
	 * @param   objectStyle     Render style to be applied.
	 */
	private void renderObjectVertices( @NotNull final Object3D object, @NotNull final List<Node3DPath> paths, @NotNull final RenderStyle objectStyle )
	{
		final GL gl = _gl;

		final boolean hasLighting = objectStyle.isVertexLightingEnabled();

		final boolean depthOnly = !hasLighting && !_renderUnlit;
		if ( depthOnly )
		{
			gl.glColorMask( false, false, false, false );
		}

		final GLStateHelper state = _state;
		state.setEnabled( GL.GL_BLEND, false );
		state.setEnabled( GL.GL_LIGHTING, hasLighting );
		state.setColor( objectStyle.getVertexColor() );

		final ShaderManager shaderManager = _shaderManager;
		shaderManager.setLightingEnabled( hasLighting );
		shaderManager.setTextureEnabled( false );
		shaderManager.setReflectivity( null );

		for ( final FaceGroup faceGroup : object.getFaceGroups() )
		{
			// FIXME: Backface culling doesn't work on vertices. Do it ourselves? (Shader?)
			final boolean backfaceCulling  = objectStyle.isBackfaceCullingEnabled() && !faceGroup.isTwoSided();

			final GeometryObject geometryObject = _geometryObjectManager.getGeometryObject( faceGroup, GeometryType.OUTLINES );
			for ( final Node3DPath path : paths )
			{
				final Matrix3D object2world = path.getTransform();
				gl.glPushMatrix();
				JOGLTools.glMultMatrixd( gl, object2world );
				geometryObject.draw();
				gl.glPopMatrix();
			}
		}

		if ( depthOnly )
		{
			gl.glColorMask( true, true, true, true );
		}
	}

	/**
	 * Renders the normals of the given face as lines.
	 *
	 * @param   faces   Faces to be rendered.
	 */
	private void renderFaceNormals( @NotNull final Collection<Face3D> faces )
	{
		final GL gl = _gl;
		gl.glBegin( GL.GL_LINES );

		for ( final Face3D face : faces )
		{
			final Vector3D normal = face.getNormal();

			double x = 0.0;
			double y = 0.0;
			double z = 0.0;

			final double scale = 10.0;
			for ( int i = 0 ; i < face.getVertexCount() ; i++ )
			{
				final Vector3D point = face.getVertex( i ).point;

				x += point.x;
				y += point.y;
				z += point.z;

				final Vector3D vertexNormal = face.getVertexNormal( i );
				gl.glVertex3d( point.x, point.y, point.z );
				gl.glVertex3d( point.x + scale * vertexNormal.x, point.y + scale * vertexNormal.y, point.z + scale * vertexNormal.z );
			}

			/*
			 * Render face normal (at average vertex coordinate).
			 */
			x /= (double)face.getVertexCount();
			y /= (double)face.getVertexCount();
			z /= (double)face.getVertexCount();

			gl.glVertex3d( x, y, z );
			gl.glVertex3d( x + scale * normal.x, y + scale * normal.y, z + scale * normal.z );
		}

		gl.glEnd();
	}

	/**
	 * Renders the given grid. This method is only called when the given grid is
	 * enabled.
	 *
	 * @param   grid    Grid to be rendered.
	 */
	private void renderGrid( @NotNull final Grid grid )
	{
		final GL gl = _gl;
		final GLStateHelper state = _state;

		gl.glPushMatrix();
		JOGLTools.glMultMatrixd( gl, grid.getGrid2wcs() );

		state.setEnabled( GL.GL_BLEND, true );
		state.setBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
		state.setEnabled( GL.GL_LINE_SMOOTH, true );
		state.setEnabled( GL.GL_LIGHTING, false );

		final Rectangle gridBounds = grid.getBounds();
		final int minCellX = gridBounds.x;
		final int maxCellX = minCellX + gridBounds.width;
		final int minCellY = gridBounds.y;
		final int maxCellY = minCellY + gridBounds.height;

		final int cellSize = grid.getCellSize();
		final int minX = minCellX * cellSize;
		final int maxX = maxCellX * cellSize;
		final int minY = minCellY * cellSize;
		final int maxY = maxCellY * cellSize;

		final boolean hightlightAxes = grid.isHighlightAxes();
		final int highlightInterval = grid.getHighlightInterval();

		gl.glLineWidth( 1.0f );
		state.setColor( 0.75f, 0.75f, 0.75f, 1.0f );
		gl.glBegin( GL.GL_LINES );

		for ( int x = minCellX ; x <= maxCellX ; x++ )
		{
			if ( ( !hightlightAxes || ( x != 0 ) ) && ( ( highlightInterval <= 1 ) || ( x % highlightInterval != 0 ) ) )
			{
				gl.glVertex3i( x * cellSize, minY, 0 );
				gl.glVertex3i( x * cellSize, maxY, 0 );
			}
		}

		for ( int y = minCellY ; y <= maxCellY ; y++ )
		{
			if ( ( !hightlightAxes || ( y != 0 ) ) && ( ( highlightInterval <= 1 ) || ( y % highlightInterval != 0 ) ) )
			{
				gl.glVertex3i( minX, y * cellSize, 0 );
				gl.glVertex3i( maxX, y * cellSize, 0 );
			}
		}

		gl.glEnd();

		if ( highlightInterval > 1 )
		{
			final int highlightMinX = minCellX - minCellX % highlightInterval;
			final int highLightMaxX = maxCellX - maxCellX % highlightInterval;
			final int highlightMinY = minCellX - minCellX % highlightInterval;
			final int highLightMaxY = maxCellX - maxCellX % highlightInterval;

			final boolean hasHighlightX = ( highLightMaxX >= highlightMinX ) && ( !hightlightAxes || ( highlightMinX < 0 ) || ( highLightMaxX > 0 ) );
			final boolean hasHighlightY = ( highLightMaxY >= highlightMinY ) && ( !hightlightAxes || ( highlightMinY < 0 ) || ( highLightMaxY > 0 ) );

			if ( hasHighlightX || hasHighlightY )
			{
				gl.glLineWidth( 1.5f );
				state.setColor( 0.5f, 0.5f, 0.5f, 1.0f );
				gl.glBegin( GL.GL_LINES );

				for ( int x = highlightMinX ; x <= highLightMaxX ; x += highlightInterval )
				{
					if ( !hightlightAxes || ( x != 0 ) )
					{
						gl.glVertex3i( x * cellSize, minY, 0 );
						gl.glVertex3i( x * cellSize, maxY, 0 );
					}
				}

				for ( int y = highlightMinY ; y <= highLightMaxY ; y += highlightInterval )
				{
					if ( !hightlightAxes || ( y != 0 ) )
					{
						gl.glVertex3i( minX, y * cellSize, 0 );
						gl.glVertex3i( maxX, y * cellSize, 0 );
					}
				}

				gl.glEnd();
			}
		}

		if ( hightlightAxes )
		{
			final boolean hasXaxis = ( minCellY <= 0 ) && ( maxCellY >= 0 );
			final boolean hasYaxis = ( minCellX <= 0 ) && ( maxCellX >= 0 );

			if ( ( hasXaxis || hasYaxis ) )
			{
				gl.glLineWidth( 2.0f );
				state.setColor( 0.1f, 0.1f, 0.1f, 1.0f );
				gl.glBegin( GL.GL_LINES );

				if ( hasXaxis )
				{
					gl.glVertex3i( minX, 0, 0 );
					gl.glVertex3i( maxX, 0, 0 );
				}

				if ( hasYaxis )
				{
					gl.glVertex3i( 0, minY, 0 );
					gl.glVertex3i( 0, maxY, 0 );
				}

				gl.glEnd();
			}
		}

		gl.glPopMatrix();
	}

	/**
	 * Provides information about the number of objects and primitives rendered
	 * during a single frame.
	 */
	public static class RenderStatistics
	{
		/**
		 * Number of primitives rendered so far during the current frame.
		 */
		private int _primitiveCounter;

		/**
		 * Number of primitives rendered during the last frame.
		 */
		private int _primitiveCount;

		/**
		 * Number of objects rendered so far during the current frame.
		 */
		private int _objectCounter;

		/**
		 * Number of objects rendered during the last frame.
		 */
		private int _objectCount;

		/**
		 * Set of unique objects rendered so far during the current frame.
		 */
		private final Set<Object> _uniqueObjects;

		/**
		 * Number of unique objects rendered during the last frame.
		 */
		private int _uniqueObjectCount;

		/**
		 * Determines the rendering framerate.
		 */
		private final FrameCounter _frameCounter;

		/**
		 * Constructs a new instance.
		 */
		private RenderStatistics()
		{
			_primitiveCounter = 0;
			_primitiveCount = 0;
			_objectCounter = 0;
			_objectCount = 0;
			_uniqueObjects = new HashSet<Object>();
			_uniqueObjectCount = 0;

			final FrameCounter frameCounter = new FrameCounter();
//			frameCounter.addChangeListener( new ChangeListener()
//			{
//				@Override
//				public void stateChanged( final ChangeEvent e )
//				{
//					System.out.println( frameCounter.get() + " fps" );
//				}
//			} );
			_frameCounter = frameCounter;
		}

		/**
		 * Returns the number of primitives rendered during the last frame.
		 *
		 * @return  Number of primitives.
		 */
		public int getPrimitiveCount()
		{
			return _primitiveCount;
		}

		/**
		 * Returns the number of unique objects rendered during the last frame.
		 * Unique is defined here as not equal to any of the other objects,
		 * based on {@link Object#equals(Object)}.
		 *
		 * @return  Number of unique objects.
		 */
		public int getUniqueObjectCount()
		{
			return _uniqueObjectCount;
		}

		/**
		 * Returns the number of objects rendered during the last frame. This
		 * includes objects that are equal and objects being rendered multiple
		 * times.
		 *
		 * @return  Number of objects.
		 */
		public int getObjectCount()
		{
			return _objectCount;
		}

		/**
		 * Called at the end of a frame, allowing the current value of each
		 * counter to be stored and to prepare for the next frame.
		 */
		private void frameRendered()
		{
			_primitiveCount = _primitiveCounter;
			_primitiveCounter = 0;

			_objectCount = _objectCounter;
			_objectCounter = 0;

			_uniqueObjectCount = _uniqueObjects.size();
			_uniqueObjects.clear();

			final FrameCounter frameCounter = _frameCounter;
			frameCounter.increment();
			frameCounter.get();
		}

		/**
		 * Called when an object is rendered, such that the statistics may be
		 * updated accordingly.
		 *
		 * @param   object  Object that was rendered.
		 * @param   count   Number of times the object was rendered.
		 */
		private void objectRendered( final Object object, final int count )
		{
			_uniqueObjects.add( object );
			_objectCounter += count;
		}

		/**
		 * Called when a primitive is rendered, such that the statistics may be
		 * updated accordingly.
		 */
		private void primitiveRendered()
		{
			_primitiveCounter++;
		}

		/**
		 * Returns the number of frames rendered during the previous second.
		 *
		 * @return  Number of frames rendered.
		 */
		public int getFPS()
		{
			return _frameCounter.get();
		}
	}

	/**
	 * Tree walker that takes level of detail of {@link Object3D}s into account.
	 */
	private class LevelOfDetailTreeWalker
		extends Node3DTreeWalker
	{
		/**
		 * Calculates projected object bounds.
		 */
		private final Convex2D _projectedBounds = new Convex2D( 8 );

		@Override
		public boolean walkNode( @NotNull final Node3DVisitor visitor, @NotNull final Node3DPath path )
		{
			boolean result = visitor.visitNode( path );
			if ( result )
			{
				final Node3D node = path.getNode();
				final Matrix3D transform = path.getTransform();

				for ( final Node3D child : node.getChildren() )
				{
					Node3D renderedChild = child;
					if ( _view.isLevelOfDetail() && ( renderedChild instanceof Object3D ) )
					{
						final Object3D object = (Object3D)renderedChild;
						if ( object.isLowDetailAvailable() )
						{
							final Bounds3D boundingBox = object.getOrientedBoundingBox();
							if ( boundingBox != null )
							{
								final Projector projector = _view.getProjector();
								final Matrix3D scene2View = _view.getScene2View();
								final Matrix3D object2View = path.getTransform().multiply( scene2View );

								_projectedBounds.clear();
								projector.project( _projectedBounds.add(), object2View.transform( boundingBox.v1.x, boundingBox.v1.y, boundingBox.v1.z ) );
								projector.project( _projectedBounds.add(), object2View.transform( boundingBox.v2.x, boundingBox.v1.y, boundingBox.v1.z ) );
								projector.project( _projectedBounds.add(), object2View.transform( boundingBox.v1.x, boundingBox.v2.y, boundingBox.v1.z ) );
								projector.project( _projectedBounds.add(), object2View.transform( boundingBox.v2.x, boundingBox.v2.y, boundingBox.v1.z ) );
								projector.project( _projectedBounds.add(), object2View.transform( boundingBox.v1.x, boundingBox.v1.y, boundingBox.v2.z ) );
								projector.project( _projectedBounds.add(), object2View.transform( boundingBox.v2.x, boundingBox.v1.y, boundingBox.v2.z ) );
								projector.project( _projectedBounds.add(), object2View.transform( boundingBox.v1.x, boundingBox.v2.y, boundingBox.v2.z ) );
								projector.project( _projectedBounds.add(), object2View.transform( boundingBox.v2.x, boundingBox.v2.y, boundingBox.v2.z ) );
								final double area = _projectedBounds.area();

								renderedChild = object.getLevelOfDetail( area );
							}
						}
					}

					if ( renderedChild != null )
					{
						if ( !walkNode( visitor, createPath( path, transform, renderedChild ) ) )
						{
							result = false;
							break;
						}
					}
				}
			}

			return result;
		}
	}
}
