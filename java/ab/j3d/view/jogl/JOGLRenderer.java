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
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.model.Face3D.*;
import ab.j3d.view.*;
import com.numdata.oss.*;
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
	private static final int TEXTURE_UNIT_COLOR = GL.GL_TEXTURE0;

	/**
	 * Texture unit used for bump maps.
	 */
	private static final int TEXTURE_UNIT_BUMP = GL.GL_TEXTURE1;

	/**
	 * Texture unit used for environment maps (reflections).
	 */
	private static final int TEXTURE_UNIT_ENVIRONMENT = GL.GL_TEXTURE2;

	/**
	 * Texture unit used for depth peeling to store the 'near' depth.
	 */
	private static final int TEXTURE_UNIT_DEPTH_NEAR = GL.GL_TEXTURE3;

	/**
	 * Texture unit used for depth peeling to store the 'opaque' depth.
	 */
	private static final int TEXTURE_UNIT_DEPTH_OPAQUE = GL.GL_TEXTURE4;

	/**
	 * Texture unit used for depth peeling to blend layers together.
	 * May overlap with a texture unit used during the normal rendering process.
	 */
	private static final int TEXTURE_UNIT_BLEND_FRONT = GL.GL_TEXTURE0;

	/**
	 * Texture unit used for depth peeling to blend layers together.
	 * May overlap with a texture unit used during the normal rendering process.
	 */
	private static final int TEXTURE_UNIT_BLEND_BACK = GL.GL_TEXTURE1;

	/**
	 * Texture unit used for shadow mapping.
	 */
	private static final int TEXTURE_UNIT_SHADOW = GL.GL_TEXTURE7;

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
	 * Position of most dominant light in the scene.
	 */
	private Vector3D _dominantLightPosition;

	/**
	 * Intensity of most dominant light in the scene.
	 */
	private float _dominantLightIntensity;

	/**
	 * Set to the most dominant light source relative to an object, while that
	 * object is being rendered.
	 */
	private Vector3D _lightPositionRelativeToObject;

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
	 * GLSL shader implementation to be used, if any.
	 */
	private ShaderImplementation _shaderImplementation;

	/**
	 * Renders objects without color maps and without lighting.
	 */
	private ShaderProgram _unlit = null;

	/**
	 * Renders objects without color maps.
	 */
	private ShaderProgram _colored = null;

	/**
	 * Renders objects with color maps.
	 */
	private ShaderProgram _textured = null;

	/**
	 * Keeps track of loaded shader objects, so they can be deleted when the
	 * renderer is disposed of.
	 */
	private List<Shader> _shaders;

	/**
	 * Currently active shader program.
	 */
	private ShaderProgram _activeShader = null;

	/**
	 * Used to create a composite image from the layers resulting from rendering
	 * with depth peeling.
	 */
	private ShaderProgram _blend = null;

	/**
	 * Textures used as color buffers when using depth peeling.
	 */
	private Texture[] _colorBuffers;

	/**
	 * Textures used as depth buffers when using depth peeling.
	 */
	private Texture[] _depthBuffers;

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
	private boolean _firstPass = false;

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
	 * Construct new JOGL renderer.
	 *
	 * @param   gl                      GL pipeline.
	 * @param   configuration           Specifies which OpenGL capabilities
	 *                                  should be used, if available.
	 * @param   textureCache            Map containing {@link Texture}s used in the scene.
	 */
	public JOGLRenderer( final GL gl, final JOGLConfiguration configuration, final TextureCache textureCache )
	{
		_gl = gl;
		_state = null;

		_textureCache = textureCache;
		_configuration = configuration;

		_shaderImplementation = null;
		_shaders = null;

		_colorBuffers = null;
		_depthBuffers = null;

		_dominantLightIntensity = 0.0f;
		_dominantLightPosition = null;
		_lightPositionRelativeToObject = null;

		_sceneToView = Matrix3D.INIT;
		_viewToScene = Matrix3D.INIT;
		_sceneToViewRotation = Matrix3D.INIT;
		_viewToSceneRotation = Matrix3D.INIT;

		_shadowMap = null;
		_shadowPass = false;
		_accumulationTexture = -1;
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

		ShaderImplementation shaderImplementation = null;

		if ( configuration.isPerPixelLightingEnabled() ||
		     configuration.isDepthPeelingEnabled() ||
		     configuration.isShadowEnabled() )
		{
			if ( capabilities.isShaderSupported() )
			{
				System.out.println( "JOGLRenderer: Using core shaders." );
				shaderImplementation = new CoreShaderImplementation();
			}
			else if ( capabilities.isShaderSupportedARB() )
			{
				System.out.println( "JOGLRenderer: Using ARB shaders." );
				shaderImplementation = new ARBShaderImplementation();
			}
			else
			{
				System.out.println( "JOGLRenderer: No shader support is available." );
			}
		}
		else
		{
			System.out.println( "JOGLRenderer: No shader-dependent features are enabled." );
		}

		_shaderImplementation = shaderImplementation;

		Texture[] depthBuffers = null;
		Texture[] colorBuffers = null;

		if ( shaderImplementation == null )
		{
			_shaders = Collections.emptyList();
		}
		else
		{
			final boolean lightingEnabled     = configuration.isPerPixelLightingEnabled();
			final boolean depthPeelingEnabled = configuration.isDepthPeelingEnabled() &&
			                                    capabilities.isDepthPeelingSupported();
			final boolean shadowEnabled = configuration.isShadowEnabled();

			final List<Shader> shaders = new ArrayList<Shader>();
			try
			{
				/*
				 * Load vertex and fragment shaders.
				 */
				final Shader lightingVertex = loadShader( Shader.Type.VERTEX, "lighting-vertex.glsl" );
				shaders.add( lightingVertex );

				final Shader lightingFragment;
				if ( isMultiPassLightingEnabled() )
				{
					lightingFragment = loadShader( Shader.Type.FRAGMENT, "lighting-fragment.glsl", "#define MULTIPASS_LIGHTING" );
				}
				else
				{
					lightingFragment = loadShader( Shader.Type.FRAGMENT, "lighting-fragment.glsl" );
				}
				shaders.add( lightingFragment );

				final Shader materialVertex = loadShader( Shader.Type.VERTEX, "material-vertex.glsl" );
				shaders.add( materialVertex );

				final Shader materialFragment = loadShader( Shader.Type.FRAGMENT, "material-fragment.glsl" );
				shaders.add( materialFragment );

				final Shader depthPeelingFragment;
				if ( depthPeelingEnabled )
				{
					depthPeelingFragment = loadShader( Shader.Type.FRAGMENT, "depth-peeling-fragment.glsl" );
					shaders.add( depthPeelingFragment );
				}
				else
				{
					depthPeelingFragment = null;
				}

				Shader shadowVertex = null;
				Shader shadowFragment = null;

				if ( shadowEnabled )
				{
					shadowVertex = loadShader( Shader.Type.VERTEX, "shadow-vertex.glsl" );
					shaders.add( shadowVertex );

					final String shadowFragmentShader = configuration.isShadowMultisampleEnabled() ? "shadow-fragment-multisample.glsl" : "shadow-fragment.glsl";
					shadowFragment = loadShader( Shader.Type.FRAGMENT, shadowFragmentShader );
					shaders.add( shadowFragment );
				}

				/*
				 * Build shader programs for various rendering modes.
				 */
				final ShaderProgram unlit = shaderImplementation.createProgram( "unlit" );
				unlit.attach( createVertexShaderMain( "color", null, shadowEnabled ) );
				unlit.attach( createFragmentShaderMain( "color", null, shadowEnabled, depthPeelingEnabled ) );
				unlit.attach( materialVertex );
				unlit.attach( materialFragment );

				if ( shadowEnabled )
				{
					unlit.attach( shadowVertex );
					unlit.attach( shadowFragment );
				}

				if ( depthPeelingEnabled )
				{
					unlit.attach( depthPeelingFragment );
				}

				unlit.link();
				_unlit = unlit;

				final ShaderProgram colored  = shaderImplementation.createProgram( "colored"  );
				final String lightingFunction = lightingEnabled ? "lighting" : null;
				colored.attach( createVertexShaderMain( "color", lightingFunction, shadowEnabled ) );
				colored.attach( createFragmentShaderMain( "color", lightingFunction, shadowEnabled, depthPeelingEnabled ) );
				colored.attach( materialVertex );
				colored.attach( materialFragment );

				if ( lightingEnabled )
				{
					colored.attach( lightingVertex );
					colored.attach( lightingFragment );
				}

				if ( shadowEnabled )
				{
					colored.attach( shadowVertex );
					colored.attach( shadowFragment );
				}

				if ( depthPeelingEnabled )
				{
					colored.attach( depthPeelingFragment );
				}

				colored.link();
				_colored = colored;

				final ShaderProgram textured = shaderImplementation.createProgram( "textured" );
				textured.attach( createVertexShaderMain( "texture", lightingFunction, shadowEnabled ) );
				textured.attach( createFragmentShaderMain( "texture", lightingFunction, shadowEnabled, depthPeelingEnabled ) );
				textured.attach( materialVertex );
				textured.attach( materialFragment );

				if ( lightingEnabled )
				{
					textured.attach( lightingVertex );
					textured.attach( lightingFragment );
				}

				if ( shadowEnabled )
				{
					textured.attach( shadowVertex );
					textured.attach( shadowFragment );
				}

				if ( depthPeelingEnabled )
				{
					textured.attach( depthPeelingFragment );
				}

				textured.link();
				_textured = textured;

				if ( depthPeelingEnabled )
				{
					final Shader blendFragment = loadShader( Shader.Type.FRAGMENT, "blend-fragment.glsl" );
					shaders.add( blendFragment );

					final ShaderProgram blend = shaderImplementation.createProgram( "blend" );
					blend.attach( blendFragment );
					_blend = blend;

					colorBuffers = new Texture[ 3 ];
					depthBuffers = new Texture[ 3 ];
				}

				_shaders = shaders;
			}
			catch ( IOException e )
			{
				e.printStackTrace();
				disableShaders();
			}
			catch ( GLException e )
			{
				e.printStackTrace();
				disableShaders();
			}

			_depthBuffers = depthBuffers;
			_colorBuffers = colorBuffers;
		}

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
	 * Returns whether the renderer is using shaders.
	 *
	 * @return  <code>true</code> if shaders are enabled.
	 */
	public boolean isShadersEnabled()
	{
		return ( _shaderImplementation != null );
	}

	/**
	 * Permanently disables shaders, e.g. because of an error while compiling
	 * or linking a shader program. As a result, depth peeling is also disabled.
	 */
	private void disableShaders()
	{
		System.out.println( "JOGLRenderer: Disabling shaders. (" + _shaderImplementation + ')' );
		_shaderImplementation = null;
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
	 * Returns whether the renderer is using depth peeling.
	 *
	 * @return  <code>true</code> if depth peeling is enabled.
	 */
	private boolean isDepthPeelingEnabled()
	{
		return isShadersEnabled() && ( _depthBuffers != null );
	}

	/**
	 * Returns whether multi-pass lighting is supported and enabled.
	 *
	 * @return  <code>true</code> if multi-pass lighting is enabled.
	 */
	private boolean isMultiPassLightingEnabled()
	{
		return _configuration.isShadowEnabled() &&
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
	 * Creates a vertex shader providing the main method for rendering with the
	 * specified settings.
	 *
	 * @param   colorFunction       Name of the color function, defined in
	 *                              another vertex shader.
	 * @param   lightingFunction    Name of the lighting function, defined
	 *                              in another vertex shader;
	 *                              <code>null</code> to use no lighting.
	 * @param   shadowEnabled       <code>true</code> to use shadow mapping.
	 *
	 * @return  Created vertex shader.
	 */
	private Shader createVertexShaderMain( final String colorFunction, final String lightingFunction, final boolean shadowEnabled )
	{
		final StringBuilder source = new StringBuilder();

		source.append( "void " );
		source.append( colorFunction );
		source.append( "();" );

		if ( lightingFunction != null )
		{
			source.append( "void " );
			source.append( lightingFunction );
			source.append( "();" );
		}

		if ( shadowEnabled )
		{
			source.append( "void shadow();" );
		}

		source.append( "void main()" );
		source.append( '{' );

		source.append( "gl_Position = ftransform();" );
		source.append( colorFunction );
		source.append( "();" );

		if ( lightingFunction != null )
		{
			source.append( lightingFunction );
			source.append( "();" );
		}

		if ( shadowEnabled )
		{
			source.append( "shadow();" );
		}

		source.append( '}' );

		final Shader result = _shaderImplementation.createShader( Shader.Type.VERTEX );
		result.setSource( source.toString() );
		return result;
	}

	/**
	 * Creates a fragment shader providing the main method for rendering with
	 * the specified settings.
	 *
	 * @param   colorFunction           Name of the color function, defined in
	 *                                  another fragment shader.
	 * @param   lightingFunction        Name of the lighting function, defined
	 *                                  in another fragment shader;
	 *                                  <code>null</code> to use no lighting.
	 * @param   shadowEnabled           <code>true</code> to use shadow mapping.
	 * @param   depthPeelingEnabled     Whether depth peeling is enabled.
	 *
	 * @return  Created vertex shader.
	 */
	private Shader createFragmentShaderMain( final String colorFunction, final String lightingFunction, final boolean shadowEnabled, final boolean depthPeelingEnabled )
	{
		final StringBuilder source = new StringBuilder();

		if ( !shadowEnabled )
		{
			source.append( "float shadow() { return 1.0; }" );
		}

		if ( depthPeelingEnabled )
		{
			source.append( "void depthPeeling();" );
		}

		source.append( "vec4 " );
		source.append( colorFunction );
		source.append( "();" );

		if ( lightingFunction != null )
		{
			source.append( "vec4 " );
			source.append( lightingFunction );
			source.append( "( in vec4 color );" );
		}

		source.append( "void main()" );
		source.append( '{' );

		if ( depthPeelingEnabled )
		{
			source.append( "depthPeeling();" );
		}

		source.append( "gl_FragColor = " );
		if ( lightingFunction != null )
		{
			source.append( lightingFunction );
			source.append( "( " );
			source.append( colorFunction );
			source.append( "() );" );
		}
		else
		{
			source.append( colorFunction );
			source.append( "();" );
		}
		// TODO: Check if this works well with multi-pass lighting. Could end up too bright.
		source.append( "gl_FragColor.rgb += gl_FrontMaterial.emission.rgb * gl_FragColor.a;" );

		source.append( '}' );

		final Shader result = _shaderImplementation.createShader( Shader.Type.FRAGMENT );
		result.setSource( source.toString() );
		return result;
	}

	/**
	 * Loads a shader of the specified type.
	 *
	 * @param   shaderType      Type of shader.
	 * @param   name            Name of the resource to be loaded.
	 * @param   prefixLines     Shader program lines to be prepended, if any.
	 *
	 * @return  Loaded shader.
	 *
	 * @throws  IOException if an I/O error occurs.
	 * @throws  GLException if compilation of the shader fails.
	 */
	private Shader loadShader( final Shader.Type shaderType, final String name, final String... prefixLines )
		throws IOException
	{
		final Shader result = _shaderImplementation.createShader( shaderType );

		final Class<?>    clazz  = JOGLRenderer.class;
		final InputStream sourceIn = clazz.getResourceAsStream( name );
		if ( sourceIn == null )
		{
			throw new IOException( "Failed to load shader: " + name );
		}

		final String source = TextTools.loadText( sourceIn );

		if ( prefixLines.length == 0 )
		{
			result.setSource( source );
		}
		else
		{
			final String[] prefixedSource = new String[ prefixLines.length + 1 ];
			for ( int i = 0 ; i < prefixLines.length ; i++ )
			{
				final String prefixLine = prefixLines[ i ];
				prefixedSource[ i ] = prefixLine + '\n';
			}
			prefixedSource[ prefixLines.length ] = source;
			result.setSource( prefixedSource );
		}

		return result;
	}

	/**
	 * Releases any resources used by the renderer.
	 */
	public void dispose()
	{
		if ( _colored != null )
		{
			_colored.dispose();
		}

		if ( _textured != null )
		{
			_textured.dispose();
		}

		if ( _blend != null )
		{
			_blend.dispose();
		}

		if ( _shaders != null )
		{
			for ( final Shader shader : _shaders )
			{
				shader.dispose();
			}
		}
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
		if ( isShadersEnabled() || isReflectionsEnabled() )
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

		for ( int i = 0; i < lightPaths.size(); i++ )
		{
			_firstPass = ( i == 0 );
			initLights();

			final Node3DPath path = lightPaths.get( i );
			final Light3D light = (Light3D) path.getNode();
			final Matrix3D lightTransform = path.getTransform();
			final boolean castingShadows = isShadersEnabled() && _configuration.isShadowEnabled() && light.isCastingShadows();

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
			setShadowMapEnabled( _textured, castingShadows );
			setShadowMapEnabled( _colored, castingShadows );
			setShadowMapEnabled( _unlit, castingShadows );
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
			 * Render grid in all passes, such that it will properly occlude
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
	 * Enables or disables shadow mapping for the given shader.
	 *
	 * @param   shader              Shader to update.
	 * @param   shadowMapEnabled    <code>true</code> to enable shadow mapping.
	 */
	private static void setShadowMapEnabled( final ShaderProgram shader, final boolean shadowMapEnabled )
	{
		shader.enable();
		shader.setUniform( "shadowMapEnabled", shadowMapEnabled );
		shader.disable();
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
		// FIXME: Doesn't work with shadows enabled. Need to render shadow map for a single light.

		final GL gl = _gl;
		_firstPass = true;

		/*
		 * Set texture matrices to identity matrix (this should already be the
		 * case by default, but some OpenGL drivers seem to think otherwise).
		 */
		if ( isShadersEnabled() || isReflectionsEnabled() )
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
		initLights();

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
	 * Render objects in scene.
	 *
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	private void renderObjects( final List<ContentNode> nodes, final Collection<RenderStyleFilter> styleFilters, final RenderStyle sceneStyle )
	{
		final GL gl = _gl;

		for ( final ContentNode node : nodes )
		{
			final Matrix3D node2world = node.getTransform();
			final List<Node3DPath> content = node.getContent();

			final RenderStyle nodeStyle = sceneStyle.applyFilters( styleFilters, node );

			for ( final Node3DPath path : content )
			{
				final Matrix3D object2node = path.getTransform();
				final Matrix3D object2world = object2node.multiply( node2world );
				final Object3D object = (Object3D) path.getNode();
				final int faceCount = object.getFaceCount();

				final RenderStyle objectStyle = nodeStyle.applyFilters( styleFilters, object );

				if ( faceCount > 0 )
				{
					_lightPositionRelativeToObject = ( _dominantLightPosition != null ) ? object2world.inverseTransform( _dominantLightPosition ) : null;
					gl.glPushMatrix();
					JOGLTools.glMultMatrixd( gl, object2world );

					renderObject( object, objectStyle );

					gl.glPopMatrix();
				}
			}
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

		/* Set backface culling. */
		_state.setEnabled( GL.GL_CULL_FACE, true );
		gl.glCullFace( _shadowPass ? GL.GL_FRONT : GL.GL_BACK );

		setupShaders();

		if ( isShadersEnabled() && isDepthPeelingEnabled() )
		{
			/*
			 * Get viewport bounds.
			 */
			final int[] viewport = new int[ 4 ];
			gl.glGetIntegerv( GL.GL_VIEWPORT, viewport, 0 );
			final int width  = viewport[ 2 ];
			final int height = viewport[ 3 ];

			renderSceneWithDepthPeeling( width, height, nodes, styleFilters, sceneStyle );
		}
		else
		{
			renderSceneWithoutDepthPeeling( nodes, styleFilters, sceneStyle );
		}

		useShader( null );
	}

	/**
	 * Sets uniform variables of GLSL shaders, specifying e.g. the texture units
	 * to be used.
	 */
	private void setupShaders()
	{
		final GL gl = _gl;

		/*
		 * Get viewport bounds.
		 */
		final int[] viewport = new int[ 4 ];
		gl.glGetIntegerv( GL.GL_VIEWPORT, viewport, 0 );
		final int width  = viewport[ 2 ];
		final int height = viewport[ 3 ];

		/*
		 * Build shader programs and set uniform variables (i.e. parameters).
		 */
		final boolean depthPeelingEnabled = isDepthPeelingEnabled();
		final boolean shadowEnabled = _configuration.isShadowEnabled();

		if ( isShadersEnabled() )
		{
			try
			{
				// Renders objects with specified material color, without lighting.
				final ShaderProgram unlit = _unlit;
				if ( unlit != null )
				{
					unlit.enable();
					if ( depthPeelingEnabled )
					{
						unlit.setUniform( "depthNear"   , TEXTURE_UNIT_DEPTH_NEAR   - GL.GL_TEXTURE0 );
						unlit.setUniform( "depthOpaque" , TEXTURE_UNIT_DEPTH_OPAQUE - GL.GL_TEXTURE0 );
						unlit.setUniform( "width"       , (float)width );
						unlit.setUniform( "height"      , (float)height );
					}
					if ( shadowEnabled )
					{
						unlit.setUniform( "shadowMap", TEXTURE_UNIT_SHADOW - GL.GL_TEXTURE0 );
					}
					unlit.disable();
					unlit.validate();
				}

				// Renders objects with specified material color.
				final ShaderProgram colored = _colored;
				if ( colored != null )
				{
					colored.enable();
					if ( depthPeelingEnabled )
					{
						colored.setUniform( "depthNear"  , TEXTURE_UNIT_DEPTH_NEAR   - GL.GL_TEXTURE0 );
						colored.setUniform( "depthOpaque", TEXTURE_UNIT_DEPTH_OPAQUE - GL.GL_TEXTURE0 );
						colored.setUniform( "width"      , (float)width );
						colored.setUniform( "height"     , (float)height );
					}
					if ( shadowEnabled )
					{
						colored.setUniform( "shadowMap", TEXTURE_UNIT_SHADOW - GL.GL_TEXTURE0 );
					}
					colored.setUniform( "reflectionMap", TEXTURE_UNIT_ENVIRONMENT - GL.GL_TEXTURE0 );
					colored.disable();
					colored.validate();
				}

				// Renders objects with color map.
				final ShaderProgram textured = _textured;
				if ( textured != null )
				{
					textured.enable();
					textured.setUniform( "colorMap", TEXTURE_UNIT_COLOR - GL.GL_TEXTURE0 );
					textured.setUniform( "reflectionMap", TEXTURE_UNIT_ENVIRONMENT - GL.GL_TEXTURE0 );
					if ( depthPeelingEnabled )
					{
						textured.setUniform( "depthNear"  , TEXTURE_UNIT_DEPTH_NEAR   - GL.GL_TEXTURE0 );
						textured.setUniform( "depthOpaque", TEXTURE_UNIT_DEPTH_OPAQUE - GL.GL_TEXTURE0 );
						textured.setUniform( "width"      , (float)width );
						textured.setUniform( "height"     , (float)height );
					}
					if ( shadowEnabled )
					{
						textured.setUniform( "shadowMap", TEXTURE_UNIT_SHADOW - GL.GL_TEXTURE0 );
					}
					textured.disable();
					textured.validate();
				}
			}
			catch ( GLException e )
			{
				e.printStackTrace();
				disableShaders();
			}
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

		if ( false )
		{
			final Texture reflectionMap = _textureCache.getCubeMap( "reflection/metal" );
			if ( reflectionMap != null )
			{
				state.setEnabled( GL.GL_DEPTH_TEST, false );
				state.setEnabled( GL.GL_CULL_FACE, false );

				gl.glMatrixMode( GL.GL_MODELVIEW );
				gl.glPushMatrix();
				gl.glLoadIdentity(); // <-- FIXME Completely breaks everything. WTF?!
				JOGLTools.glMultMatrixd( gl, _sceneToViewRotation );

				reflectionMap.bind();
				reflectionMap.enable();

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
				glut.glutSolidCube( 10.0f );

				state.setEnabled( GL.GL_TEXTURE_GEN_S, false );
				state.setEnabled( GL.GL_TEXTURE_GEN_T, false );
				state.setEnabled( GL.GL_TEXTURE_GEN_R, false );

				reflectionMap.disable();

				gl.glPopMatrix();

				state.setEnabled( GL.GL_CULL_FACE, true );
				state.setEnabled( GL.GL_DEPTH_TEST, true );
			}
		}
	}

	/**
	 * Render the scene using depth peeling to render transparent faces.
	 *
	 * @param   width           Width of the framebuffer, in pixels.
	 * @param   height          Height of the framebuffer, in pixels.
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	private void renderSceneWithDepthPeeling( final int width, final int height, final List<ContentNode> nodes, final Collection<RenderStyleFilter> styleFilters, final RenderStyle sceneStyle )
	{
		final GL gl = _gl;
		final GLStateHelper state = _state;

		/*
		 * Configure light parameters.
		 */
		initLights();

		/*
		 * Create frame buffer object to render to textures.
		 */
		final int[] frameBuffer = new int[ 1 ];
		gl.glGenFramebuffersEXT( 1, frameBuffer, 0 );
		gl.glBindFramebufferEXT( GL.GL_FRAMEBUFFER_EXT, frameBuffer[ 0 ] );

		/*
		 * Create color and depth buffers, or re-use existing ones.
		 */
		final Texture[] colorBuffers = getColorBuffers( width, height );
		final Texture[] depthBuffers = getDepthBuffers( width, height );

		final Texture composite = colorBuffers[ 0 ];
		final Texture layer     = colorBuffers[ 1 ];
		final Texture opaque    = colorBuffers[ 2 ];

		/*
		 * Clear first color buffer, on which rendered layers are composited.
		 */
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, composite.getTarget(), composite.getTextureObject(), 0 );
		gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
		gl.glClear( GL.GL_COLOR_BUFFER_BIT );

		/*
		 * Initialize near and far depth buffers.
		 */
		// Near depth buffer starts at 0.0 (near clipping plane).
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, depthBuffers[ 1 ].getTarget(), depthBuffers[ 1 ].getTextureObject(), 0 );
		gl.glClearDepth( 0.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );

		// Far depth buffer starts at 1.0 (far clipping plane).
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, depthBuffers[ 0 ].getTarget(), depthBuffers[ 0 ].getTextureObject(), 0 );
		gl.glClearDepth( 1.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );

		/*
		 * Render opaque objects first, to a seperate depth and color buffer.
		 * The depth buffer is re-used while rendering transparent objects,
		 * skipping any objects that are fully occluded.
		 */
		state.setEnabled( GL.GL_DEPTH_TEST, true );
		gl.glDepthFunc( GL.GL_LEQUAL );
		state.setEnabled( GL.GL_BLEND, false );
		state.setEnabled( GL.GL_LIGHTING, true );

		final Texture depthOpaque = depthBuffers[ 2 ];
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, opaque.getTarget()     , opaque.getTextureObject()     , 0 );
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT , depthOpaque.getTarget(), depthOpaque.getTextureObject(), 0 );

		gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

		gl.glActiveTexture( TEXTURE_UNIT_DEPTH_NEAR );
		depthBuffers[ 1 ].enable();
		depthBuffers[ 1 ].bind();
		gl.glActiveTexture( TEXTURE_UNIT_DEPTH_OPAQUE );
		depthBuffers[ 0 ].enable();
		depthBuffers[ 0 ].bind();
		gl.glActiveTexture( TEXTURE_UNIT_COLOR );

		_renderMode = MultiPassRenderMode.OPAQUE_ONLY;
		renderObjects( nodes, styleFilters, sceneStyle );

		gl.glActiveTexture( TEXTURE_UNIT_DEPTH_OPAQUE );
		depthOpaque.enable();
		depthOpaque.bind();
		gl.glActiveTexture( TEXTURE_UNIT_COLOR );

		/*
		 * Render transparent objects in multiple passes using depth peeling.
		 */
		final int maximumPasses = 4;
		int pass;
		for ( pass = 0 ; pass < maximumPasses ; pass++ )
		{
			/*
			 * Read from and write to far ('normal') depth buffer.
			 * Read from near depth buffer, used by the depth peeling shader to
			 * skip previous layers.
			 */
			final Texture depthFar  = depthBuffers[   pass       % 2 ];
			final Texture depthNear = depthBuffers[ ( pass + 1 ) % 2 ];
			gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, layer.getTarget()   , layer.getTextureObject()   , 0 );
			gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT , depthFar.getTarget(), depthFar.getTextureObject(), 0 );
			gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
			gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

			gl.glActiveTexture( TEXTURE_UNIT_DEPTH_NEAR );
			depthNear.enable();
			depthNear.bind();
			gl.glActiveTexture( TEXTURE_UNIT_COLOR );

			/*
			 * Render scene, keeping track of the number of samples rendered.
			 * Depth peeling is finished when no more samples are rendered.
			 */
			final OcclusionQuery occlusionQuery = new OcclusionQuery();

			_renderMode = MultiPassRenderMode.TRANSPARENT_ONLY;
			renderObjects( nodes, styleFilters, sceneStyle );

			final int sampleCount = occlusionQuery.getSampleCount();
			if ( sampleCount == 0 )
			{
				break;
			}

			/*
			 * Blend this layer with the result.
			 */
			blend( composite, layer );
		}

		/*
		 * Clear depth buffers for second opaque rendering pass.
		 */
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, depthBuffers[ 0 ].getTarget(), depthBuffers[ 0 ].getTextureObject(), 0 );
		gl.glClearDepth( 0.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, depthBuffers[ 1 ].getTarget(), depthBuffers[ 1 ].getTextureObject(), 0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, depthOpaque.getTarget(), depthOpaque.getTextureObject(), 0 );
		gl.glClearDepth( 1.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );

		/*
		 * Delete the frame buffer object used to render to textures;
		 * from here on, we render to the default frame buffer.
		 */
		gl.glBindFramebufferEXT( GL.GL_FRAMEBUFFER_EXT, 0 );
		gl.glDeleteFramebuffersEXT( 1, frameBuffer, 0 );

		_renderMode = MultiPassRenderMode.OPAQUE_ONLY;
		renderObjects( nodes, styleFilters, sceneStyle );
		useShader( null );

		/*
		 * Render the depth-peeled composite image to the screen.
		 */
		state.setEnabled( GL.GL_DEPTH_TEST, false );
		state.setEnabled( GL.GL_LIGHTING, false );
		state.setEnabled( GL.GL_BLEND, true );
		state.setBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );

		gl.glActiveTexture( TEXTURE_UNIT_DEPTH_OPAQUE );
		depthOpaque.disable();
		gl.glActiveTexture( TEXTURE_UNIT_DEPTH_NEAR );
		depthBuffers[ 0 ].disable();
		gl.glActiveTexture( TEXTURE_UNIT_COLOR );

		renderToViewport( composite );

//		displayTextures( colorBuffers, -1.0, true  );
//		displayTextures( depthBuffers,  1.0, false );

//		System.out.println( "Rendered " + width + " x " + height + " pixels in " + ( pass + 1 ) + " depth peeling pass(es) and 2 opaque passes" );
	}

	/**
	 * Render the scene using blending to render transparent faces.
	 *
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	private void renderSceneWithoutDepthPeeling( final List<ContentNode> nodes, final Collection<RenderStyleFilter> styleFilters, final RenderStyle sceneStyle )
	{
		// FIXME: Display lists are great for performance, but only work when no textures are loaded during the render pass!
//		final GL gl = _gl;
//		final int list = gl.glGenLists( 1 );
//		gl.glNewList( list, GL.GL_COMPILE_AND_EXECUTE );

		_renderMode = MultiPassRenderMode.OPAQUE_ONLY;
		renderObjects( nodes, styleFilters, sceneStyle );

		_renderMode = MultiPassRenderMode.TRANSPARENT_ONLY;
		renderObjects( nodes, styleFilters, sceneStyle );

//		gl.glEndList();
//		gl.glDeleteLists( list, 1 );
	}

	/**
	 * Returns textures of the given size to be used as color buffers. The
	 * number of color buffers is determined by the size of the
	 * {@link #_colorBuffers} array, which is used to cache color buffers.
	 *
	 * @param   width   Width of each color buffer, in pixels.
	 * @param   height  Height of each color buffer, in pixels.
	 *
	 * @return  Textures of the given size for use as color buffers.
	 */
	private Texture[] getColorBuffers( final int width, final int height )
	{
		final Texture[] result = _colorBuffers;
		for ( int i = 0 ; i < result.length ; i++ )
		{
			if ( ( result[ i ] == null ) ||
			     ( result[ i ].getWidth()  != width  ) ||
			     ( result[ i ].getHeight() != height ) )
			{
				final TextureData textureData = new TextureData( GL.GL_RGBA8, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, false, false, null, null );

				/*
				 * Force 'GL_TEXTURE_2D' target.
				 */
				result[ i ] = TextureIO.newTexture( GL.GL_TEXTURE_2D );
				result[ i ].updateImage( textureData, GL.GL_TEXTURE_2D );

				result[ i ].setTexParameteri( GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST       );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST       );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_WRAP_S    , GL.GL_CLAMP_TO_EDGE );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_WRAP_T    , GL.GL_CLAMP_TO_EDGE );
			}
		}
		return result;
	}

	/**
	 * Returns textures of the given size to be used as depth buffers. The
	 * number of depth buffers is determined by the size of the
	 * {@link #_depthBuffers} array, which is used to cache depth buffers.
	 *
	 * @param   width   Width of each depth buffer, in pixels.
	 * @param   height  Height of each depth buffer, in pixels.
	 *
	 * @return  Textures of the given size for use as depth buffers.
	 */
	private Texture[] getDepthBuffers( final int width, final int height )
	{
		final Texture[] result = _depthBuffers;
		for ( int i = 0 ; i < result.length ; i++ )
		{
			if ( ( result[ i ] == null ) ||
			     ( result[ i ].getWidth()  != width  ) ||
			     ( result[ i ].getHeight() != height ) )
			{
				final TextureData textureData = new TextureData( GL.GL_DEPTH_COMPONENT32, width, height, 0, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, false, false, false, null, null );
				result[ i ] = TextureIO.newTexture( GL.GL_TEXTURE_2D );
				result[ i ].updateImage( textureData, GL.GL_TEXTURE_2D );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_MIN_FILTER  , GL.GL_NEAREST       );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_MAG_FILTER  , GL.GL_NEAREST       );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_WRAP_S      , GL.GL_CLAMP_TO_EDGE );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_WRAP_T      , GL.GL_CLAMP_TO_EDGE );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_COMPARE_MODE, GL.GL_NONE );
			}
		}
		return result;
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
	 * Blend the given layer with the composite, such that the layer appears
	 * behind the current content of the composite.
	 *
	 * <p>
	 * <em>This operation has the side effect of replacing the active
	 * framebuffer's first color attachment ('GL_COLOR_ATTACHMENT0_EXT').</em>
	 *
	 * @param   composite   Composite to blend layer with.
	 * @param   layer       Layer to put behind the composite.
	 */
	private void blend( final Texture composite, final Texture layer )
	{
		final GL gl = _gl;
		final GLStateHelper state = _state;

		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, composite.getTarget(), composite.getTextureObject(), 0 );

		state.setEnabled( GL.GL_DEPTH_TEST, false );
		state.setEnabled( GL.GL_LIGHTING, false );

		gl.glActiveTexture( TEXTURE_UNIT_BLEND_BACK );
		layer.enable();
		layer.bind();
		gl.glActiveTexture( TEXTURE_UNIT_BLEND_FRONT );

		final ShaderProgram previousShader = _activeShader;
		final ShaderProgram blend = _blend;
		useShader( blend );
		blend.setUniform( "front", TEXTURE_UNIT_BLEND_FRONT - GL.GL_TEXTURE0 );
		blend.setUniform( "back" , TEXTURE_UNIT_BLEND_BACK  - GL.GL_TEXTURE0 );

		renderToViewport( composite );

		gl.glActiveTexture( TEXTURE_UNIT_BLEND_BACK );
		layer.disable();
		gl.glActiveTexture( TEXTURE_UNIT_BLEND_FRONT );

		state.setEnabled( GL.GL_DEPTH_TEST, true );
		state.setEnabled( GL.GL_LIGHTING, true );

		useShader( previousShader );
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
	 * Enables the given shader program, replacing the current one.
	 *
	 * @param   shader  Shader program to be used; <code>null</code> to enable
	 *                  OpenGL's fixed functionality.
	 */
	private void useShader( final ShaderProgram shader )
	{
		if ( isShadersEnabled() )
		{
			if ( _shadowPass )
			{
				if ( _activeShader != null )
				{
					_activeShader.disable();
					_activeShader = null;
				}
			}
			else
			{
				final ShaderProgram activeShader = _activeShader;
				if ( activeShader != shader )
				{
					_activeShader = shader;

					try
					{
						if ( activeShader != null )
						{
							activeShader.disable();
						}

						if ( shader != null )
						{
							shader.enable();
						}
					}
					catch ( GLException e )
					{
						e.printStackTrace();
						disableShaders();
					}
				}
			}
		}
	}

	/**
	 * Initializes lighting properties.
	 */
	private void initLights()
	{
		_dominantLightPosition = null;
		_dominantLightIntensity = 0.0f;
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

		gl.glLightfv( lightNumber, GL.GL_AMBIENT , new float[] { 0.0f, 0.0f, 0.0f, 1.0f }, 0 );
		gl.glLightfv( lightNumber, GL.GL_DIFFUSE , new float[] { light.getDiffuseRed() , light.getDiffuseGreen() , light.getDiffuseBlue() , 1.0f }, 0 );
		gl.glLightfv( lightNumber, GL.GL_SPECULAR, new float[] { light.getSpecularRed(), light.getSpecularGreen(), light.getSpecularBlue(), 1.0f }, 0 );

		if ( light instanceof DirectionalLight3D )
		{
			final DirectionalLight3D directional = (DirectionalLight3D)light;
			final Vector3D direction = directional.getDirection();
			gl.glLightfv( lightNumber, GL.GL_POSITION, new float[] { -(float)direction.x, -(float)direction.y, -(float)direction.z, 0.0f }, 0 );
		}
		else
		{
			if ( light instanceof SpotLight3D )
			{
				final SpotLight3D spot = (SpotLight3D)light;
				final Vector3D direction = light2world.rotate( spot.getDirection() );
				gl.glLightfv( lightNumber, GL.GL_POSITION      , new float[] { (float)light2world.xo, (float)light2world.yo, (float)light2world.zo, 1.0f }, 0 );
				gl.glLightfv( lightNumber, GL.GL_SPOT_DIRECTION, new float[] { (float)direction.x   , (float)direction.y   , (float)direction.z           }, 0 );
				gl.glLightf ( lightNumber, GL.GL_SPOT_CUTOFF   , spot.getSpreadAngle() );
				gl.glLightf ( lightNumber, GL.GL_SPOT_EXPONENT , spot.getConcentration() );
			}
			else
			{
				gl.glLightfv( lightNumber, GL.GL_POSITION, new float[] { (float)light2world.xo, (float)light2world.yo, (float)light2world.zo, 1.0f }, 0 );
			}

			gl.glLightf( lightNumber, GL.GL_CONSTANT_ATTENUATION , light.getConstantAttenuation()  );
			gl.glLightf( lightNumber, GL.GL_LINEAR_ATTENUATION   , light.getLinearAttenuation()    );
			gl.glLightf( lightNumber, GL.GL_QUADRATIC_ATTENUATION, light.getQuadraticAttenuation() );
		}

		_state.setEnabled( lightNumber, true );

		/**
		 * Determine dominant light position, used for bump mapping.
		 * This method can be rather inaccurate, especially if the most
		 * intense light is far away from a bump mapped object.
		 */
		final float lightIntensity = light.getIntensity();
		if ( ( _dominantLightPosition == null ) || ( _dominantLightIntensity < lightIntensity ) )
		{
			_dominantLightPosition = Vector3D.INIT.set( light2world.xo, light2world.yo, light2world.zo );
			_dominantLightIntensity = lightIntensity;
		}
	}

	/**
	 * Renders the given object.
	 *
	 * @param   object          Object to be rendered.
	 * @param   objectStyle     Render style applied to the object.
	 */
	protected void renderObject( final Object3D object, final RenderStyle objectStyle )
	{
		final boolean anyMaterialEnabled = objectStyle.isMaterialEnabled();
		final boolean anyFillEnabled     = objectStyle.isFillEnabled() && ( objectStyle.getFillColor() != null );
		final boolean anyStrokeEnabled   = objectStyle.isStrokeEnabled() && ( objectStyle.getStrokeColor() != null );
		final boolean anyVertexEnabled   = objectStyle.isVertexEnabled() && ( objectStyle.getVertexColor() != null );

		final int faceCount = object.getFaceCount();

		if ( anyMaterialEnabled || anyFillEnabled || anyStrokeEnabled || anyVertexEnabled )
		{
			if ( anyMaterialEnabled )
			{
				renderObjectMaterial( object, objectStyle );
			}
			else if ( anyFillEnabled )
			{
				renderObjectFilled( object, objectStyle );
			}

			if ( anyStrokeEnabled )
			{
				for ( int j = 0 ; j < faceCount; j++ )
				{
					renderStrokedFace( object.getFace( j ), objectStyle );
				}
			}

			if ( anyVertexEnabled )
			{
				for ( int j = 0 ; j < faceCount; j++ )
				{
					renderFaceVertices( objectStyle, object.getFace( j ) );
				}
			}
		}
	}

	/**
	 * Renders the given object with a material applied to it.
	 *
	 * @param   object          Object to be rendered.
	 * @param   objectStyle     Render style to be applied.
	 */
	protected void renderObjectMaterial( final Object3D object, final RenderStyle objectStyle )
	{
		final Map<Material, List<Face3D>> facesByMaterial;
		if ( objectStyle.getMaterialOverride() != null )
		{
			facesByMaterial = Collections.singletonMap( objectStyle.getMaterialOverride(), object.getFaces() );
		}
		else
		{
			facesByMaterial = CollectionTools.getGroupByField( object.getFaces(), "material", Material.class );
		}

		for ( final Map.Entry<Material, List<Face3D>> entry : facesByMaterial.entrySet() )
		{
			final Material material = entry.getKey();
			final List<Face3D> faces = entry.getValue();

			if ( material != null )
			{
				final GL gl = _gl;
				final MultiPassRenderMode renderMode = _renderMode;

				/*
				 * Get textures.
				 */
				final TextureCache textureCache = _textureCache;

				final float extraAlpha = objectStyle.getMaterialAlpha();
				final float combinedAlpha = material.diffuseColorAlpha * extraAlpha;
				final boolean isTransparent = ( combinedAlpha < 0.99f ) || textureCache.hasAlpha( material.colorMap );
				final boolean blend = !isDepthPeelingEnabled() && ( renderMode != MultiPassRenderMode.OPAQUE_ONLY ) && isTransparent;

				if ( _shadowPass && ( material.diffuseColorAlpha < 0.50f ) )
				{
					continue;
				}

				if ( ( ( renderMode != MultiPassRenderMode.OPAQUE_ONLY ) || !isTransparent ) &&
				     ( ( renderMode != MultiPassRenderMode.TRANSPARENT_ONLY ) || isTransparent ) )
				{
					final Vector3D lightPosition = _lightPositionRelativeToObject;
					final boolean hasLighting = objectStyle.isMaterialLightingEnabled() && ( lightPosition != null );

					final Texture colorMap = textureCache.getColorMapTexture( material );
					final Texture bumpMap = isShadersEnabled() && hasLighting ? textureCache.getBumpMapTexture( material ) : null;
					final Texture normalizationCubeMap = ( bumpMap != null ) ? textureCache.getNormalizationCubeMap() : null;

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
					state.setMaterial( material, objectStyle, extraAlpha );

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
					else if ( false ) // DOT3 bump mapping; disabled
					{
						/*
						 * Set The First Texture Unit To Normalize Our Vector From The
						 * Surface To The Light. Set The Texture Environment Of The First
						 * Texture Unit To Replace It With The Sampled Value Of The
						 * Normalization Cube Map.
						 */
						gl.glActiveTexture( GL.GL_TEXTURE0 );
						normalizationCubeMap.enable();
						normalizationCubeMap.bind();
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE );
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_REPLACE );
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE0_RGB, GL.GL_TEXTURE );

						/*
						 * Set The Second Unit To The Bump Map. Set The Texture Environment
						 * Of The Second Texture Unit To Perform A Dot3 Operation With The
						 * Value Of The Previous Texture Unit (The Normalized Vector Form
						 * The Surface To The Light) And The Sampled Texture Value (The
						 * Normalized Normal Vector Of Our Bump Map).
						 */
						gl.glActiveTexture( GL.GL_TEXTURE1 );
						bumpMap.enable();
						bumpMap.bind();
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE );
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_DOT3_RGB );
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE0_RGB, GL.GL_PREVIOUS );
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE1_RGB, GL.GL_TEXTURE );

						/*
						 * The third unit is used to apply the diffuse color of the
						 * material.
						 */
						gl.glActiveTexture( GL.GL_TEXTURE2 );
						bumpMap.enable();
						bumpMap.bind();
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE );
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_MODULATE );
						gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_SOURCE0_RGB, GL.GL_PRIMARY_COLOR );

						/*
						 * Set The Fourth Texture Unit To Our Texture. Set The Texture
						 * Environment Of The Third Texture Unit To Modulate (Multiply) The
						 * Result Of Our Dot3 Operation With The Texture Value.
						 */
						if ( colorMap != null )
						{
							gl.glActiveTexture( GL.GL_TEXTURE3 );
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE );
						}
					}

					final boolean reflectionsEnabled = isReflectionsEnabled();

					final Texture reflectionMap = reflectionsEnabled && ( material.reflectionMap != null ) ? textureCache.getCubeMap( material.reflectionMap ) : null;
					if ( reflectionMap != null )
					{
						gl.glActiveTexture( TEXTURE_UNIT_ENVIRONMENT );
						reflectionMap.enable();
						reflectionMap.bind();

						if ( !isShadersEnabled() )
						{
							/*
							 * Interpolate with previous texture stage.
							 */
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE );
							gl.glTexEnvfv( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, new float[] { 0.0f, 0.0f, 0.0f, material.reflectionMin }, 0 );

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
					final TextureCoords colorMapCoords;
					if ( colorMap != null )
					{
						useShader( _textured );
						colorMap.enable();
						colorMap.bind();
						colorMapCoords = colorMap.getImageTexCoords();
					}
					else
					{
						useShader( _colored );
						colorMapCoords = null;
					}

					final ShaderProgram activeShader = _activeShader;
					if ( activeShader != null )
					{
						final Vector3D reflectionColor = new Vector3D( (double)material.reflectionRed, (double)material.reflectionGreen, (double)material.reflectionBlue );
						activeShader.setUniform( "reflectionMin", material.reflectionMin );
						activeShader.setUniform( "reflectionMax", material.reflectionMax );
						activeShader.setUniform( "reflectionColor", reflectionColor );
					}

					/*
					 * Render faces.
					 */
					for ( final Face3D face : faces )
					{
						renderMaterialFace( face, objectStyle, bumpMap != null, colorMapCoords );
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
						gl.glActiveTexture( GL.GL_TEXTURE2 );
						bumpMap.disable();

						gl.glActiveTexture( GL.GL_TEXTURE1 );
						bumpMap.disable();

						gl.glActiveTexture( GL.GL_TEXTURE0 );
						normalizationCubeMap.disable();
					}

					/*
					 * Disable reflection map.
					 */
					if ( reflectionMap != null )
					{
						gl.glActiveTexture( TEXTURE_UNIT_ENVIRONMENT );

						gl.glMatrixMode( GL.GL_TEXTURE );
						gl.glPopMatrix();
						gl.glMatrixMode( GL.GL_MODELVIEW );

						if ( !isShadersEnabled() )
						{
							gl.glTexEnvi( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE );

							state.setEnabled( GL.GL_TEXTURE_GEN_S, false );
							state.setEnabled( GL.GL_TEXTURE_GEN_T, false );
							state.setEnabled( GL.GL_TEXTURE_GEN_R, false );
						}

						reflectionMap.disable();

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
	 * Renders a face with a material applied to it.
	 *
	 * @param   face            Face to be rendered.
	 * @param   style           Render style to be applied.
	 * @param   bumpMap         Whether bump mapping is enabled.
	 * @param   colorMapCoords  Texture coordinates for the color map.
	 */
	protected void renderMaterialFace( @NotNull final Face3D face, @NotNull final RenderStyle style, final boolean bumpMap, @Nullable final TextureCoords colorMapCoords )
	{
		final GL gl = _gl;
		final GLStateHelper state = _state;

		final List<Vertex> vertices = face.vertices;
		final Tessellation tessellation = face.getTessellation();
		final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();

		if ( !primitives.isEmpty() )
		{
			final Vector3D lightPosition = _lightPositionRelativeToObject;
			final boolean hasLighting = style.isMaterialLightingEnabled() && ( lightPosition != null );
			final boolean backfaceCulling = style.isBackfaceCullingEnabled() && !face.isTwoSided();
			final boolean setVertexNormals = hasLighting && face.smooth;

			/*
			 * Render face. Use multiple passes for two-sided lighting.
			 */
			final int passes = ( !backfaceCulling && hasLighting && isShadersEnabled() ) ? 2 : 1;
			final boolean multipass = ( passes > 1 );

			for ( int pass = 0; pass < passes; pass++ )
			{
				final boolean isBackFace = multipass && ( pass == 0 );
				state.setEnabled( GL.GL_CULL_FACE, multipass || backfaceCulling );

				if ( !setVertexNormals )
				{
					final Vector3D normal = face.getNormal();
					if ( isBackFace )
					{
						gl.glNormal3d( -normal.x, -normal.y, -normal.z );
					}
					else
					{
						gl.glNormal3d( normal.x, normal.y, normal.z );
					}
				}

				for ( final TessellationPrimitive primitive : primitives )
				{
					if ( primitive instanceof TriangleList )
					{
						gl.glBegin( GL.GL_TRIANGLES );
					}
					else if ( primitive instanceof TriangleFan )
					{
						gl.glBegin( GL.GL_TRIANGLE_FAN );
					}
					else if ( primitive instanceof TriangleStrip )
					{
						gl.glBegin( GL.GL_TRIANGLE_STRIP );
					}
					else
					{
						continue;
					}

					for ( final int vertexIndex : primitive.getVertices() )
					{
						final Vertex vertex = vertices.get( vertexIndex );
						final Vector3D point = vertex.point;

						if ( bumpMap )
						{
							// TODO: Doesn't really match with other uses of texture units, because normalization cube map comes before color map. (Without shaders, bump is ugly anyway, except for special circumstances.)
							gl.glMultiTexCoord3d( GL.GL_TEXTURE0, lightPosition.x + point.x, lightPosition.y + point.y, lightPosition.z + point.z );
							gl.glMultiTexCoord2f( GL.GL_TEXTURE1, vertex.colorMapU, vertex.colorMapV );

							if ( colorMapCoords != null )
							{
								final float u = colorMapCoords.left() + vertex.colorMapU * ( colorMapCoords.right() - colorMapCoords.left() );
								final float v = colorMapCoords.bottom() + vertex.colorMapV * ( colorMapCoords.top() - colorMapCoords.bottom() );
								gl.glMultiTexCoord2f( GL.GL_TEXTURE3, u, v );
							}
						}
						else if ( colorMapCoords != null )
						{
							final float u = colorMapCoords.left() + vertex.colorMapU * ( colorMapCoords.right() - colorMapCoords.left() );
							final float v = colorMapCoords.bottom() + vertex.colorMapV * ( colorMapCoords.top() - colorMapCoords.bottom() );
							gl.glTexCoord2f( u, v );
						}

						if ( setVertexNormals )
						{
							final Vector3D vertexNormal = face.getVertexNormal( vertexIndex );
							if ( isBackFace )
							{
								gl.glNormal3d( -vertexNormal.x, -vertexNormal.y, -vertexNormal.z );
							}
							else
							{
								gl.glNormal3d( vertexNormal.x, vertexNormal.y, vertexNormal.z );
							}
						}

						gl.glVertex3d( point.x, point.y, point.z );
					}

					gl.glEnd();
				}

				if ( DRAW_NORMALS && ( pass == passes - 1 ) )
				{
					renderFaceNormals( face );
				}
			}
		}
	}

	/**
	 * Renders the given object in a solid color.
	 *
	 * @param   object          Object to be rendered.
	 * @param   objectStyle     Render style to be applied.
	 */
	protected void renderObjectFilled( final Object3D object, final RenderStyle objectStyle )
	{
		final MultiPassRenderMode renderMode = _renderMode;

		final GL gl = _gl;

		final Color   color           = objectStyle.getFillColor();
		final int     alpha           = color.getAlpha();
		final boolean blend           = !isDepthPeelingEnabled() && ( renderMode != MultiPassRenderMode.OPAQUE_ONLY ) && ( alpha < 255 );
		final boolean hasLighting     = objectStyle.isFillLightingEnabled() && ( _lightPositionRelativeToObject != null );

		if ( !hasLighting && !_firstPass )
		{
			return;
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
			useShader( hasLighting ? _colored : _unlit );

			for ( final Face3D face : object.getFaces() )
			{
				renderFilledFace( face, objectStyle );
			}

			if ( blend )
			{
				if ( alpha < 64 )
				{
					gl.glDepthMask( true );
				}
			}
		}
	}

	/**
	 * Renders a face in a solid color.
	 *
	 * @param   face    Face to be rendered.
	 * @param   style   Render style to be applied.
	 */
	protected void renderFilledFace( final Face3D face, final RenderStyle style )
	{
		final GL gl = _gl;
		final GLStateHelper state = _state;

		final List<Vertex> vertices = face.vertices;
		final Tessellation tessellation = face.getTessellation();
		final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();

		final boolean backfaceCulling = style.isBackfaceCullingEnabled() && !face.isTwoSided();
		final boolean hasLighting     = style.isFillLightingEnabled() && ( _lightPositionRelativeToObject != null );
		final boolean setVertexNormals = hasLighting && face.smooth;

		if ( !primitives.isEmpty() )
		{
			/*
			 * Render face. Use multiple passes for two-sided lighting.
			 */
			final int passes = ( !backfaceCulling && hasLighting && isShadersEnabled() ) ? 2 : 1;
			final boolean multipass = ( passes > 1 );
			state.setEnabled( GL.GL_CULL_FACE, multipass || backfaceCulling );

			for ( int pass = 0 ; pass < passes ; pass++ )
			{
				final boolean isBackFace = multipass && ( pass == 0 );

				if ( !setVertexNormals )
				{
					final Vector3D normal = face.getNormal();
					if ( isBackFace )
					{
						gl.glNormal3d( -normal.x, -normal.y, -normal.z );
					}
					else
					{
						gl.glNormal3d( normal.x, normal.y, normal.z );
					}
				}

				for ( final TessellationPrimitive primitive : primitives )
				{
					if ( primitive instanceof TriangleList )
					{
						gl.glBegin( GL.GL_TRIANGLES );
					}
					else if ( primitive instanceof TriangleFan )
					{
						gl.glBegin( GL.GL_TRIANGLE_FAN );
					}
					else if ( primitive instanceof TriangleStrip )
					{
						gl.glBegin( GL.GL_TRIANGLE_STRIP );
					}
					else
					{
						continue;
					}

					for ( final int vertexIndex : primitive.getVertices() )
					{
						final Vertex vertex = vertices.get( vertexIndex );

						if ( setVertexNormals )
						{
							final Vector3D normal = face.getVertexNormal( vertexIndex );
							if ( isBackFace )
							{
								gl.glNormal3d( -normal.x, -normal.y, -normal.z );
							}
							else
							{
								gl.glNormal3d( normal.x, normal.y, normal.z );
							}
						}

						final Vector3D point = vertex.point;
						gl.glVertex3d( point.x, point.y, point.z );
					}

					gl.glEnd();
				}
			}
		}
	}

	/**
	 * Renders the outline of a face.
	 *
	 * @param   face    Face to be rendered.
	 * @param   style   Render style to be applied.
	 */
	protected void renderStrokedFace( final Face3D face, final RenderStyle style )
	{
		final GL gl = _gl;

		final List<Vertex> vertices = face.vertices;
		final int vertexCount = vertices.size();

		if ( vertexCount >= 2 )
		{
			final Color   color            = style.getStrokeColor();
			final float   width            = style.getStrokeWidth();
			final boolean backfaceCulling  = style.isBackfaceCullingEnabled() && !face.isTwoSided();
			final boolean hasLighting      = style.isStrokeLightingEnabled() && ( _lightPositionRelativeToObject != null );
			final boolean setVertexNormals = hasLighting && face.smooth;

			if ( !hasLighting && !_firstPass )
			{
				return;
			}

			/*
			 * Set render/material properties.
			 */
			final GLStateHelper state = _state;
			state.setEnabled( GL.GL_BLEND, false );
			gl.glLineWidth( width );
			// FIXME: Backface culling doesn't work on lines. Need to do it ourselves.

			state.setEnabled( GL.GL_LIGHTING, hasLighting );

			state.setColor( color );
			useShader( hasLighting ? _colored : _unlit );

			/*
			 * Render face.
			 */
			if ( !setVertexNormals )
			{
				final Vector3D normal = face.getNormal();
				gl.glNormal3d( normal.x, normal.y, normal.z );
			}

			final Tessellation tessellation = face.getTessellation();

			for ( final int[] outline : tessellation.getOutlines() )
			{
				gl.glBegin( GL.GL_LINE_LOOP );

				for ( final int vertexIndex : outline )
				{
					final Vertex vertex = vertices.get( vertexIndex );
					final Vector3D point = vertex.point;

					if ( setVertexNormals )
					{
						final Vector3D normal = face.getVertexNormal( vertexIndex );
						gl.glNormal3d( normal.x, normal.y, normal.z );
					}

					gl.glVertex3d( point.x, point.y, point.z );
				}

				gl.glEnd();
			}
		}
	}

	/**
	 * Renders the vertices of a face.
	 *
	 * @param   face    Face to be rendered.
	 * @param   style   Render style to be applied.
	 */
	protected void renderFaceVertices( final RenderStyle style, final Face3D face )
	{
		final List<Vertex> vertices = face.vertices;
		final int vertexCount = vertices.size();

		if ( vertexCount > 0 )
		{
			final GL gl = _gl;

			final Color   color            = style.getVertexColor();
			final boolean backfaceCulling  = style.isBackfaceCullingEnabled() && !face.isTwoSided();
			final boolean hasLighting      = style.isVertexLightingEnabled() && ( _lightPositionRelativeToObject != null );
			final boolean setVertexNormals = hasLighting && face.smooth;

			/*
			 * Set render/material properties.
			 */
			final GLStateHelper state = _state;
			state.setEnabled( GL.GL_BLEND, false );
			// TODO: implement backface culling, i.e. omit vertices for backfaces

			state.setEnabled( GL.GL_LIGHTING, hasLighting );
			state.setColor( color );

			/*
			 * Render vertices.
			 */
			gl.glBegin( GL.GL_POINTS );

			if ( !setVertexNormals )
			{
				final Vector3D normal = face.getNormal();
				gl.glNormal3d( normal.x, normal.y, normal.z );
			}

			for ( int vertexIndex = vertexCount ; --vertexIndex >= 0 ; )
			{
				final Vertex vertex = vertices.get( vertexIndex );
				final Vector3D point = vertex.point;

				if ( setVertexNormals )
				{
					final Vector3D normal = face.getVertexNormal( vertexIndex );
					gl.glNormal3d( normal.x, normal.y, normal.z );
				}

				gl.glVertex3d( point.x, point.y, point.z );
			}

			gl.glEnd();
		}
	}

	/**
	 * Renders the normals of the given face as lines.
	 *
	 * @param   face    Face to be rendered.
	 */
	private void renderFaceNormals( @NotNull final Face3D face )
	{
		final Vector3D normal = face.getNormal();

		double x = 0.0;
		double y = 0.0;
		double z = 0.0;

		final GL gl = _gl;
		final GLStateHelper state = _state;

		state.setColor( 0.0f, 1.0f, 1.0f, 1.0f );
		gl.glBegin( GL.GL_LINES );

		final double scale = 10.0;
		for ( int i = 0 ; i < face.getVertexCount() ; i++ )
		{
			final double faceX = face.getX( i );
			final double faceY = face.getY( i );
			final double faceZ = face.getZ( i );

			x += faceX;
			y += faceY;
			z += faceZ;

			final Vector3D vertexNormal = face.getVertexNormal( i );
			gl.glVertex3d( faceX, faceY, faceZ );
			gl.glVertex3d( faceX + scale * vertexNormal.x, faceY + scale * vertexNormal.y, faceZ + scale * vertexNormal.z );
		}

		state.setColor( 1.0f, 0.0f, 0.0f, 1.0f );
		x /= (double)face.getVertexCount();
		y /= (double)face.getVertexCount();
		z /= (double)face.getVertexCount();

		gl.glVertex3d( x, y, z );
		gl.glVertex3d( x + scale * normal.x, y + scale * normal.y, z + scale * normal.z );

		gl.glEnd();
	}

	/**
	 * Renders the given grid. This method is only called when the given grid is
	 * enabled.
	 *
	 * @param   grid    Grid to be rendered.
	 */
	protected void renderGrid( @NotNull final Grid grid )
	{
		useShader( null );

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
		if ( hightlightAxes )
		{
			final boolean hasXaxis = ( minCellY <= 0 ) && ( maxCellY >= 0 );
			final boolean hasYaxis = ( minCellX <= 0 ) && ( maxCellX >= 0 );

			if ( ( hasXaxis || hasYaxis ) )
			{
				gl.glLineWidth( 2.5f );
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

		final int highlightInterval = grid.getHighlightInterval();
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

		gl.glPopMatrix();
	}
}
