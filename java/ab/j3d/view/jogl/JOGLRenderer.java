/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.ContentNode;
import ab.j3d.model.DirectionalLight3D;
import ab.j3d.model.ExtrudedObject2D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Face3D.Vertex;
import ab.j3d.model.Light3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Scene;
import ab.j3d.model.SpotLight3D;
import ab.j3d.view.RenderStyle;
import ab.j3d.view.RenderStyleFilter;
import ab.j3d.view.Renderer;

import com.numdata.oss.TextTools;

/**
 * Implements {@link Renderer} for JOGL.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class JOGLRenderer
	extends Renderer
{
	/**
	 * If enabled, objects are drawn with lines for face and vertex normals.
	 */
	private static final boolean DRAW_NORMALS = false;

	/**
	 * OpenGL pipeline.
	 */
	private final GL _gl;

	/**
	 * Texture cache.
	 */
	private final Map<String,Texture> _textureCache;

	/**
	 * Wrapper for OpenGL pipeline.
	 */
	private GLWrapper _glWrapper;

	/**
	 * Counting variable with index of JOGL light.
	 *
	 * @see     #renderLight
	 */
	private int _lightIndex = 0;

	/**
	 * Maximum number of lights allowed in this scene.
	 */
	private int _maxLights = 0;

	/**
	 * Background color of image.
	 */
	private final Color _backgroundColor;

	/**
	 * Grid enabled/disabled flag.
	 */
	private boolean _gridEnabled;

	/**
	 * Transforms grid to world coordinates.
	 */
	private Matrix3D _grid2wcs;

	/**
	 * Bounds of grid in cell units.
	 */
	private Rectangle _gridBounds;

	/**
	 * Size of each grid cell in world units.
	 */
	private int _gridCellSize;

	/**
	 * If set, highlight X/Y grid axes.
	 */
	private boolean _gridHighlightAxes;

	/**
	 * Interval for highlighted grid lines. Less or equal to zero if
	 * highlighting is disabled.
	 */
	private int _gridHighlightInterval;

	/**
	 * Position of most dominant light in the scene.
	 */
	private Vector3D _dominantLightPosition;

	/**
	 * Intensity of most dominant light in the scene.
	 */
	private float _dominantLightIntensity;

	/**
	 * Temporary variable set by {@link #renderObjectBegin} to the most
	 * dominant light source relative to the object.
	 */
	private Vector3D _lightPositionRelativeToObject;

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
	private final List<Shader> _shaders;

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
	private final Texture[] _colorBuffers;

	/**
	 * Textures used as depth buffers when using depth peeling.
	 */
	private final Texture[] _depthBuffers;

	/**
	 * Specifies which objects should be rendered during the current rendering
	 * pass when performing multi-pass rendering.
	 */
	private MultiPassRenderMode _renderMode = MultiPassRenderMode.ALL;

	/**
	 * Specifies which objects should be rendered during the current rendering
	 * pass when performing multi-pass rendering.
	 */
	private enum MultiPassRenderMode
	{
		ALL ,
		OPAQUE_ONLY ,
		TRANSPARENT_ONLY
	}

	/**
	 * Construct new JOGL renderer.
	 *
	 * @param   gl                      GL pipeline.
	 * @param   configuration           Specifies which OpenGL capabilities
	 *                                  should be used, if available.
	 * @param   capabilities            Provides information about OpenGL
	 *                                  capabilities.
	 * @param   textureCache            Map containing {@link Texture}s used in the scene.
	 * @param   backgroundColor         Backgroundcolor to use.
	 * @param   gridIsEnabled           <code>true</code> if the grid must be rendered,
	 *                                  <code>false</code> otherwise.
	 * @param   grid2wcs                Transforms grid to world coordinates.
	 * @param   gridBounds              Bounds of grid.
	 * @param   gridCellSize            Size of each cell.
	 * @param   gridHighlightAxes       If set, hightlight X=0 and Y=0 axes.
	 * @param   gridHighlightInterval   Interval to use for highlighting grid lines.
	 */
	public JOGLRenderer( final GL gl , final JOGLConfiguration configuration , final JOGLCapabilities capabilities , final Map<String,Texture> textureCache , final Color backgroundColor , final boolean gridIsEnabled , final Matrix3D grid2wcs , final Rectangle gridBounds , final int gridCellSize , final boolean gridHighlightAxes , final int gridHighlightInterval )
	{
		_gl = gl;
		_textureCache = textureCache;

		_backgroundColor = backgroundColor;

		_gridEnabled = gridIsEnabled;
		_grid2wcs = grid2wcs;
		_gridBounds = gridBounds;
		_gridCellSize = gridCellSize;
		_gridHighlightAxes = gridHighlightAxes;
		_gridHighlightInterval = gridHighlightInterval;

		_glWrapper = null;

		_dominantLightIntensity = 0.0f;
		_dominantLightPosition = null;

		_lightPositionRelativeToObject = null;

		ShaderImplementation shaderImplementation = null;

		if ( configuration.isPerPixelLightingEnabled() ||
		     configuration.isDepthPeelingEnabled() )
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

		_shaderImplementation = shaderImplementation;

		Texture[] depthBuffers = null;
		Texture[] colorBuffers = null;

		if ( shaderImplementation != null )
		{
			final boolean lightingEnabled     = configuration.isPerPixelLightingEnabled();
			final boolean depthPeelingEnabled = configuration.isDepthPeelingEnabled() && capabilities.isDepthPeelingSupported();

			final List<Shader> shaders = new ArrayList<Shader>();
			try
			{
				/*
				 * Load vertex and fragment shaders.
				 */
				final Shader lightingVertex = loadShader( Shader.Type.VERTEX , "lighting-vertex.glsl" );
				shaders.add( lightingVertex );

				final Shader lightingFragment = loadShader( Shader.Type.FRAGMENT , "lighting-fragment.glsl" );
				shaders.add( lightingFragment );

				final Shader materialVertex = loadShader( Shader.Type.VERTEX , "material-vertex.glsl" );
				shaders.add( materialVertex );

				final Shader materialFragment = loadShader( Shader.Type.FRAGMENT , "material-fragment.glsl" );
				shaders.add( materialFragment );

				final Shader depthPeelingFragment;
				if ( depthPeelingEnabled )
				{
					depthPeelingFragment = loadShader( Shader.Type.FRAGMENT , "depth-peeling-fragment.glsl" );
					shaders.add( depthPeelingFragment );
				}
				else
				{
					depthPeelingFragment = null;
				}

				/*
				 * Build shader programs for various rendering modes.
				 */
				final ShaderProgram unlit = shaderImplementation.createProgram( "unlit" );
				unlit.attach( createVertexShaderMain  ( "color" , null ) );
				unlit.attach( createFragmentShaderMain( "color" , null , depthPeelingEnabled ) );
				unlit.attach( materialVertex );
				unlit.attach( materialFragment );

				if ( depthPeelingEnabled )
				{
					unlit.attach( depthPeelingFragment );
				}

				unlit.link();
				_unlit = unlit;

				final ShaderProgram colored  = shaderImplementation.createProgram( "colored"  );
				final String lightingFunction = lightingEnabled ? "lighting" : null;
				colored.attach( createVertexShaderMain  ( "color" , lightingFunction ) );
				colored.attach( createFragmentShaderMain( "color" , lightingFunction , depthPeelingEnabled ) );
				colored.attach( materialVertex );
				colored.attach( materialFragment );

				if ( lightingEnabled )
				{
					colored.attach( lightingVertex );
					colored.attach( lightingFragment );
				}

				if ( depthPeelingEnabled )
				{
					colored.attach( depthPeelingFragment );
				}

				colored.link();
				_colored = colored;

				final ShaderProgram textured = shaderImplementation.createProgram( "textured" );
				textured.attach( createVertexShaderMain  ( "texture" , lightingFunction ) );
				textured.attach( createFragmentShaderMain( "texture" , lightingFunction , depthPeelingEnabled ) );
				textured.attach( materialVertex );
				textured.attach( materialFragment );

				if ( lightingEnabled )
				{
					textured.attach( lightingVertex );
					textured.attach( lightingFragment );
				}

				if ( depthPeelingEnabled )
				{
					textured.attach( depthPeelingFragment );
				}

				textured.link();
				_textured = textured;

				if ( depthPeelingEnabled )
				{
					final Shader blendFragment = loadShader( Shader.Type.FRAGMENT , "blend-fragment.glsl" );
					shaders.add( blendFragment );

					final ShaderProgram blend = shaderImplementation.createProgram( "blend" );
					blend.attach( blendFragment );
					_blend = blend;

					colorBuffers = new Texture[ 3 ];
					depthBuffers = new Texture[ 3 ];
				}
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

			_shaders = shaders;
		}
		else
		{
			_shaders = Collections.emptyList();
		}

		_depthBuffers = depthBuffers;
		_colorBuffers = colorBuffers;
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
		_shaderImplementation = null;
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
	 * Creates a vertex shader providing the main method for rendering with the
	 * specified settings.
	 *
	 * @param   colorFunction       Name of the color function, defined in
	 *                              another vertex shader.
	 * @param   lightingFunction    Name of the lighting function, defined
	 *                              in another vertex shader;
	 *                              <code>null</code> to use no lighting.
	 *
	 * @return  Created vertex shader.
	 */
	private Shader createVertexShaderMain( final String colorFunction , final String lightingFunction )
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

		source.append( "void main()" );
		source.append( "{" );
		source.append( "gl_Position = ftransform();" );
		source.append( colorFunction );
		source.append( "();" );
		if ( lightingFunction != null )
		{
			source.append( lightingFunction );
			source.append( "();" );
		}
		source.append( "}" );

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
	 * @param   depthPeelingEnabled     Whether depth peeling is enabled.
	 *
	 * @return  Created vertex shader.
	 */
	private Shader createFragmentShaderMain( final String colorFunction , final String lightingFunction , final boolean depthPeelingEnabled )
	{
		final StringBuilder source = new StringBuilder();

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
		source.append( "{" );
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
		source.append( "}" );

		final Shader result = _shaderImplementation.createShader( Shader.Type.FRAGMENT );
		result.setSource( source.toString() );
		return result;
	}

	/**
	 * Sets whether the grid should be rendered.
	 *
	 * @param   gridEnabled     <code>true</code> to enable the grid;
	 *                          <code>false</code> otherwise.
	 */
	public void setGridEnabled( final boolean gridEnabled )
	{
		_gridEnabled = gridEnabled;
	}

	/**
	 * Loads a shader of the specified type.
	 *
	 * @param   shaderType  Type of shader.
	 * @param   name        Name of the resource to be loaded.
	 *
	 * @return  Loaded shader.
	 *
	 * @throws  IOException if an I/O error occurs.
	 * @throws  GLException if compilation of the shader fails.
	 */
	private Shader loadShader( final Shader.Type shaderType , final String name )
		throws IOException
	{
		final Shader result = _shaderImplementation.createShader( shaderType );

		final Class<?>    clazz  = JOGLRenderer.class;
		final InputStream source = clazz.getResourceAsStream( name );
		if ( source == null )
		{
			throw new IOException( "Failed to load shader: " + name );
		}

		result.setSource( TextTools.loadText( source ) );
		return result;
	}

	/**
	 * Releases any resources used by the renderer.
	 */
	public void dispose()
	{
		_colored.dispose();
		_textured.dispose();
		_blend.dispose();

		for ( final Shader shader : _shaders )
		{
			shader.dispose();
		}
	}

	public void renderScene( final Scene scene , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		_gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT , new float[] { scene.getAmbientRed() , scene.getAmbientGreen() , scene.getAmbientBlue() , 1.0f } , 0 );
		super.renderScene( scene , styleFilters , sceneStyle );
	}

	public void renderContentNodes( final List<ContentNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		final GL gl = _gl;
		final GLWrapper glWrapper = new GLWrapper( gl );
		_glWrapper = glWrapper;

		/* Clear depth and color buffer. */
		final float[] backgroundRGB = _backgroundColor.getRGBColorComponents( null );
		gl.glClearColor( backgroundRGB[ 0 ] , backgroundRGB[ 1 ] , backgroundRGB[ 2 ] , 1.0f );
		gl.glClearDepth( 1.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT );

		/* Set backface culling. */
		glWrapper.setCullFace( true );
		glWrapper.glCullFace( GL.GL_BACK );

		/*
		 * Get viewport bounds.
		 */
		final int[] viewport = new int[ 4 ];
		gl.glGetIntegerv( GL.GL_VIEWPORT , viewport , 0 );
		final int width  = viewport[ 2 ];
		final int height = viewport[ 3 ];

		/*
		 * Build shader programs and set uniform variables (i.e. parameters).
		 */
		final boolean depthPeelingEnabled = isDepthPeelingEnabled();

		// Renders objects with specified material color, without lighting.
		final ShaderProgram unlit = _unlit;
		if ( unlit != null )
		{
			unlit.enable();
			if ( depthPeelingEnabled )
			{
				// Texture unit 0 is reserved for color maps. (see below)
				// Texture unit 1 is reserved for future use. (bump mapping)
				unlit.setUniform( "depthNear"   , 2 );
				unlit.setUniform( "depthOpaque" , 3 );
				unlit.setUniform( "width"       , (float)width );
				unlit.setUniform( "height"      , (float)height );
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
				// Texture unit 0 is reserved for color maps. (see below)
				// Texture unit 1 is reserved for future use. (bump mapping)
				colored.setUniform( "depthNear"   , 2 );
				colored.setUniform( "depthOpaque" , 3 );
				colored.setUniform( "width"       , (float)width );
				colored.setUniform( "height"      , (float)height );
			}
			colored.disable();
			colored.validate();
		}

		// Renders objects with color map.
		final ShaderProgram textured = _textured;
		if ( textured != null )
		{
			textured.enable();
			textured.setUniform( "colorMap" , 0 );
			if ( depthPeelingEnabled )
			{
				// Texture unit 1 is reserved for future use. (bump mapping)
				textured.setUniform( "depthNear"   , 2 );
				textured.setUniform( "depthOpaque" , 3 );
				textured.setUniform( "width"       , (float)width );
				textured.setUniform( "height"      , (float)height );
			}
			textured.disable();
			textured.validate();
		}

		if ( depthPeelingEnabled )
		{
			renderSceneWithDepthPeeling( width , height , nodes , styleFilters , sceneStyle );
		}
		else
		{
			renderSceneWithoutDepthPeeling( nodes , styleFilters , sceneStyle );
		}

		useShader( null );
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
	private void renderSceneWithDepthPeeling( final int width , final int height , final List<ContentNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		final GL gl = _gl;
		final GLWrapper glWrapper = _glWrapper;

		/*
		 * Configure light parameters.
		 */
		_lightIndex = 0;
		_maxLights  = getMaxLights();
		renderLights( nodes );

		/*
		 * Create frame buffer object to render to textures.
		 */
		final int[] frameBuffer = new int[ 1 ];
		gl.glGenFramebuffersEXT( 1 , frameBuffer , 0 );
		gl.glBindFramebufferEXT( GL.GL_FRAMEBUFFER_EXT , frameBuffer[ 0 ] );

		/*
		 * Create color and depth buffers, or re-use existing ones.
		 */
		final Texture[] colorBuffers = getColorBuffers( width , height );
		final Texture[] depthBuffers = getDepthBuffers( width , height );

		final Texture composite = colorBuffers[ 0 ];
		final Texture layer     = colorBuffers[ 1 ];
		final Texture opaque    = colorBuffers[ 2 ];

		/*
		 * Clear first color buffer, on which rendered layers are composited.
		 */
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_COLOR_ATTACHMENT0_EXT , composite.getTarget() , composite.getTextureObject() , 0 );
		gl.glClearColor( 0.0f , 0.0f , 0.0f , 0.0f );
		gl.glClear( GL.GL_COLOR_BUFFER_BIT );

		/*
		 * Initialize near and far depth buffers.
		 */
		// Near depth buffer starts at 0.0 (near clipping plane).
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_DEPTH_ATTACHMENT_EXT , depthBuffers[ 1 ].getTarget() , depthBuffers[ 1 ].getTextureObject() , 0 );
		gl.glClearDepth( 0.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );

		// Far depth buffer starts at 1.0 (far clipping plane).
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_DEPTH_ATTACHMENT_EXT , depthBuffers[ 0 ].getTarget() , depthBuffers[ 0 ].getTextureObject() , 0 );
		gl.glClearDepth( 1.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );

		/*
		 * Render opaque objects first, to a seperate depth and color buffer.
		 * The depth buffer is re-used while rendering transparent objects,
		 * skipping any objects that are fully occluded.
		 */
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.glDepthFunc( GL.GL_LEQUAL );
		glWrapper.setBlend( false );
		glWrapper.setLighting( true );

		final Texture depthOpaque = depthBuffers[ 2 ];
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_COLOR_ATTACHMENT0_EXT , opaque.getTarget()      , opaque.getTextureObject()      , 0 );
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_DEPTH_ATTACHMENT_EXT  , depthOpaque.getTarget() , depthOpaque.getTextureObject() , 0 );

		gl.glClearColor( 0.0f , 0.0f , 0.0f , 0.0f );
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

		gl.glActiveTexture( GL.GL_TEXTURE2 );
		depthBuffers[ 1 ].enable();
		depthBuffers[ 1 ].bind();
		gl.glActiveTexture( GL.GL_TEXTURE3 );
		depthBuffers[ 0 ].enable();
		depthBuffers[ 0 ].bind();
		gl.glActiveTexture( GL.GL_TEXTURE0 );

		_renderMode = MultiPassRenderMode.OPAQUE_ONLY;
		renderObjects( nodes , styleFilters , sceneStyle );

		gl.glActiveTexture( GL.GL_TEXTURE3 );
		depthOpaque.enable();
		depthOpaque.bind();
		gl.glActiveTexture( GL.GL_TEXTURE0 );

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
			gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_COLOR_ATTACHMENT0_EXT , layer.getTarget()    , layer.getTextureObject()    , 0 );
			gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_DEPTH_ATTACHMENT_EXT  , depthFar.getTarget() , depthFar.getTextureObject() , 0 );
			gl.glClearColor( 0.0f , 0.0f , 0.0f , 0.0f );
			gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

			gl.glActiveTexture( GL.GL_TEXTURE2 );
			depthNear.enable();
			depthNear.bind();
			gl.glActiveTexture( GL.GL_TEXTURE0 );

			/*
			 * Render scene, keeping track of the number of samples rendered.
			 * Depth peeling is finished when no more samples are rendered.
			 */
			final OcclusionQuery occlusionQuery = new OcclusionQuery();

			_renderMode = MultiPassRenderMode.TRANSPARENT_ONLY;
			renderObjects( nodes , styleFilters , sceneStyle );

			final int sampleCount = occlusionQuery.getSampleCount();
			if ( sampleCount == 0 )
			{
				break;
			}

			/*
			 * Blend this layer with the result.
			 */
			blend( composite , layer );
		}

		/*
		 * Clear depth buffers for second opaque rendering pass.
		 */
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_DEPTH_ATTACHMENT_EXT , depthBuffers[ 0 ].getTarget() , depthBuffers[ 0 ].getTextureObject() , 0 );
		gl.glClearDepth( 0.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_DEPTH_ATTACHMENT_EXT , depthBuffers[ 1 ].getTarget() , depthBuffers[ 1 ].getTextureObject() , 0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_DEPTH_ATTACHMENT_EXT , depthOpaque.getTarget() , depthOpaque.getTextureObject() , 0 );
		gl.glClearDepth( 1.0 );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );

		/*
		 * Delete the frame buffer object used to render to textures;
		 * from here on, we render to the default frame buffer.
		 */
		gl.glBindFramebufferEXT( GL.GL_FRAMEBUFFER_EXT , 0 );
		gl.glDeleteFramebuffersEXT( 1 , frameBuffer , 0 );

		/*
		 * Render the grid and opaque objects.
		 */
		if ( _gridEnabled )
		{
			useShader( _unlit );
			drawGrid( _grid2wcs , _gridBounds , _gridCellSize , _gridHighlightAxes , _gridHighlightInterval );
		}

		_renderMode = MultiPassRenderMode.OPAQUE_ONLY;
		renderObjects( nodes , styleFilters , sceneStyle );
		useShader( null );

		/*
		 * Render the depth-peeled composite image to the screen.
		 */
		gl.glDisable( GL.GL_DEPTH_TEST );
		glWrapper.setLighting( false );
		glWrapper.setBlend( true );
		glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );

		gl.glActiveTexture( GL.GL_TEXTURE3 );
		depthOpaque.disable();
		gl.glActiveTexture( GL.GL_TEXTURE2 );
		depthBuffers[ 0 ].disable();
		gl.glActiveTexture( GL.GL_TEXTURE0 );

		renderToViewport( composite );

//		displayTextures( colorBuffers , -1.0 , true  );
//		displayTextures( depthBuffers ,  1.0 , false );

//		System.out.println( "Rendered " + width + " x " + height + " pixels in " + ( pass + 1 ) + " depth peeling pass(es) and 2 opaque passes" );
	}

	/**
	 * Render the scene using blending to render transparent faces.
	 *
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	private void renderSceneWithoutDepthPeeling( final List<ContentNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		if ( _gridEnabled )
		{
			drawGrid( _grid2wcs , _gridBounds , _gridCellSize , _gridHighlightAxes , _gridHighlightInterval );
		}

		_renderMode = MultiPassRenderMode.OPAQUE_ONLY;
		super.renderContentNodes( nodes , styleFilters , sceneStyle );
		_renderMode = MultiPassRenderMode.TRANSPARENT_ONLY;
		super.renderContentNodes( nodes , styleFilters , sceneStyle );
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
	private Texture[] getColorBuffers( final int width , final int height )
	{
		final Texture[] result = _colorBuffers;
		for ( int i = 0 ; i < result.length ; i++ )
		{
			if ( ( result[ i ] == null ) ||
			     ( result[ i ].getWidth()  != width  ) ||
			     ( result[ i ].getHeight() != height ) )
			{
				final TextureData textureData = new TextureData( GL.GL_RGBA8 , width , height , 0 , GL.GL_RGBA , GL.GL_UNSIGNED_BYTE , false , false , false , null , null );

				/*
				 * Force 'GL_TEXTURE_2D' target.
				 */
				result[ i ] = TextureIO.newTexture( GL.GL_TEXTURE_2D );
				result[ i ].updateImage( textureData , GL.GL_TEXTURE_2D );

				result[ i ].setTexParameteri( GL.GL_TEXTURE_MIN_FILTER , GL.GL_NEAREST       );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_MAG_FILTER , GL.GL_NEAREST       );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_WRAP_S     , GL.GL_CLAMP_TO_EDGE );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_WRAP_T     , GL.GL_CLAMP_TO_EDGE );
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
	private Texture[] getDepthBuffers( final int width , final int height )
	{
		final Texture[] result = _depthBuffers;
		for ( int i = 0 ; i < result.length ; i++ )
		{
			if ( ( result[ i ] == null ) ||
			     ( result[ i ].getWidth()  != width  ) ||
			     ( result[ i ].getHeight() != height ) )
			{
				final TextureData textureData = new TextureData( GL.GL_DEPTH_COMPONENT32 , width , height , 0 , GL.GL_DEPTH_COMPONENT , GL.GL_FLOAT , false , false , false , null , null );
				result[ i ] = TextureIO.newTexture( GL.GL_TEXTURE_2D );
				result[ i ].updateImage( textureData , GL.GL_TEXTURE_2D );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_MIN_FILTER   , GL.GL_NEAREST       );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_MAG_FILTER   , GL.GL_NEAREST       );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_WRAP_S       , GL.GL_CLAMP_TO_EDGE );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_WRAP_T       , GL.GL_CLAMP_TO_EDGE );
				result[ i ].setTexParameteri( GL.GL_TEXTURE_COMPARE_MODE , GL.GL_NONE );
			}
		}
		return result;
	}

	/**
	 * Returns the maximum number of lights supported by the OpenGL
	 * implementation.
	 *
	 * @return  Maximum number of lights.
	 */
	private int getMaxLights()
	{
		final int[] maxLights = new int[ 1 ];
		_gl.glGetIntegerv( GL.GL_MAX_LIGHTS , maxLights, 0 );
		return maxLights[ 0 ];
	}

	/**
	 * Renders the contents of the given texture to the viewport.
	 *
	 * @param   texture     Texture to be rendered.
	 */
	private void renderToViewport( final Texture texture )
	{
		final GL        gl        = _gl;
		final GLWrapper glWrapper = _glWrapper;

		texture.enable();
		texture.bind();
		toViewportSpace();

		final TextureCoords textureCoords = texture.getImageTexCoords();

		final float left   = textureCoords.left();
		final float bottom = textureCoords.bottom();
		final float right  = textureCoords.right();
		final float top    = textureCoords.top();

		glWrapper.setColor( 1.0f , 1.0f , 1.0f , 1.0f );
		gl.glBegin( GL.GL_QUADS );
		gl.glMultiTexCoord2f( GL.GL_TEXTURE0 , left , bottom );
		gl.glVertex2d( -1.0 , -1.0 );
		gl.glMultiTexCoord2f( GL.GL_TEXTURE0 , right , bottom );
		gl.glVertex2d(  1.0 , -1.0 );
		gl.glMultiTexCoord2f( GL.GL_TEXTURE0 , right , top );
		gl.glVertex2d(  1.0 ,  1.0 );
		gl.glMultiTexCoord2f( GL.GL_TEXTURE0 , left , top );
		gl.glVertex2d( -1.0 ,  1.0 );
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
	private void blend( final Texture composite , final Texture layer )
	{
		final GL gl = _gl;
		final GLWrapper glWrapper = _glWrapper;

		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT , GL.GL_COLOR_ATTACHMENT0_EXT , composite.getTarget() , composite.getTextureObject() , 0 );

		gl.glDisable( GL.GL_DEPTH_TEST );
		glWrapper.setLighting( false );

		gl.glActiveTexture( GL.GL_TEXTURE1 );
		layer.enable();
		layer.bind();
		gl.glActiveTexture( GL.GL_TEXTURE0 );

		final ShaderProgram previousShader = _activeShader;
		final ShaderProgram blend = _blend;
		useShader( blend );
		blend.setUniform( "front" , 0 );
		blend.setUniform( "back"  , 1 );

		renderToViewport( composite );

		gl.glActiveTexture( GL.GL_TEXTURE1 );
		layer.disable();
		gl.glActiveTexture( GL.GL_TEXTURE0 );

		gl.glEnable( GL.GL_DEPTH_TEST );
		glWrapper.setLighting( true );

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
	private void displayTextures( final Texture[] textures , final double x , final boolean blend )
	{
		final GL        gl        = _gl;
		final GLWrapper glWrapper = _glWrapper;

		/*
		 * Render one of the buffers to a small rectangle on screen.
		 */
		toViewportSpace();

		gl.glPushAttrib( GL.GL_ALL_ATTRIB_BITS );
		glWrapper.setLighting( false );
		glWrapper.setBlend( blend );
		if ( blend )
		{
			glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
		}

		for ( int i = 0 ; i < textures.length ; i++ )
		{
			final Texture texture = textures[ i ];

			gl.glActiveTexture( GL.GL_TEXTURE0 );
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

			glWrapper.setColor( 1.0f , 1.0f , 1.0f , 1.0f );
			gl.glBegin( GL.GL_QUADS );
			gl.glMultiTexCoord2f( GL.GL_TEXTURE0 , left , bottom );
			gl.glVertex2d( minX , minY );
			gl.glMultiTexCoord2f( GL.GL_TEXTURE0 , right , bottom );
			gl.glVertex2d( maxX , minY );
			gl.glMultiTexCoord2f( GL.GL_TEXTURE0 , right , top );
			gl.glVertex2d( maxX , maxY );
			gl.glMultiTexCoord2f( GL.GL_TEXTURE0 , left , top );
			gl.glVertex2d( minX , maxY );
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
					disableShaders();
				}
			}
		}
	}

	protected void renderLights( final List<ContentNode> nodes )
	{
		final GL gl = _gl;

		_lightIndex = 0;
		_maxLights  = getMaxLights();
		_dominantLightPosition = null;
		_dominantLightIntensity = 0.0f;

		/* Set Light Model to two sided lighting. */
		gl.glLightModeli( GL.GL_LIGHT_MODEL_TWO_SIDE , GL.GL_TRUE );

		/* Set local view point */
		gl.glLightModeli( GL.GL_LIGHT_MODEL_LOCAL_VIEWER , GL.GL_TRUE );

		/* Apply specular hightlight after texturing (otherwise, this would be done before texturing, so we won't see it). */
		if ( gl.isExtensionAvailable( "GL_VERSION_1_2" ) )
		{
			gl.glLightModeli( GL.GL_LIGHT_MODEL_COLOR_CONTROL , GL.GL_SEPARATE_SPECULAR_COLOR );
		}

		/* Disable all lights */
		for( int i = 0 ; i < _maxLights ; i++ )
		{
			final int light = GL.GL_LIGHT0 + i;
			gl.glDisable( light );
			gl.glLightfv( light , GL.GL_POSITION , new float[] { 0.0f , 0.0f , 0.0f , 1.0f } , 0 );
			gl.glLightfv( light , GL.GL_DIFFUSE  , new float[] { 0.0f , 0.0f , 0.0f , 1.0f } , 0 );
			gl.glLightfv( light , GL.GL_SPECULAR , new float[] { 0.0f , 0.0f , 0.0f , 1.0f } , 0 );
			gl.glLightfv( light , GL.GL_AMBIENT  , new float[] { 0.0f , 0.0f , 0.0f , 1.0f } , 0 );
		}

		/* Let super render lights */
		super.renderLights( nodes );
	}

	protected void renderLight( final Matrix3D light2world , final Light3D light )
	{
		final GL gl = _gl;

		final int lightIndex = _lightIndex++;
		if ( lightIndex >= _maxLights )
		{
			throw new IllegalStateException( "No more than " + _maxLights + " lights supported." );
		}

		final int lightNumber = GL.GL_LIGHT0 + lightIndex;

		gl.glLightfv( lightNumber , GL.GL_AMBIENT  , new float[] { 0.0f , 0.0f , 0.0f , 1.0f } , 0 );
		gl.glLightfv( lightNumber , GL.GL_DIFFUSE  , new float[] { light.getDiffuseRed()  , light.getDiffuseGreen()  , light.getDiffuseBlue()  , 1.0f } , 0 );
		gl.glLightfv( lightNumber , GL.GL_SPECULAR , new float[] { light.getSpecularRed() , light.getSpecularGreen() , light.getSpecularBlue() , 1.0f } , 0 );

		if ( light instanceof DirectionalLight3D )
		{
			final DirectionalLight3D directional = (DirectionalLight3D)light;
			final Vector3D direction = directional.getDirection();
			gl.glLightfv( lightNumber , GL.GL_POSITION , new float[] { -(float)direction.x , -(float)direction.y , -(float)direction.z , 0.0f } , 0 );
		}
		else
		{
			if ( light instanceof SpotLight3D )
			{
				final SpotLight3D spot = (SpotLight3D)light;
				final Vector3D direction = light2world.rotate( spot.getDirection() );
				gl.glLightfv( lightNumber , GL.GL_POSITION       , new float[] { (float)light2world.xo , (float)light2world.yo , (float)light2world.zo , 1.0f } , 0 );
				gl.glLightfv( lightNumber , GL.GL_SPOT_DIRECTION , new float[] { (float)direction.x    , (float)direction.y    , (float)direction.z           } , 0 );
				gl.glLightf ( lightNumber , GL.GL_SPOT_CUTOFF    , spot.getSpreadAngle() );
				gl.glLightf ( lightNumber , GL.GL_SPOT_EXPONENT  , spot.getConcentration() );
			}
			else
			{
				gl.glLightfv( lightNumber , GL.GL_POSITION , new float[] { (float)light2world.xo , (float)light2world.yo , (float)light2world.zo , 1.0f } , 0 );
			}

			gl.glLightf( lightNumber , GL.GL_CONSTANT_ATTENUATION  , light.getConstantAttenuation()  );
			gl.glLightf( lightNumber , GL.GL_LINEAR_ATTENUATION    , light.getLinearAttenuation()    );
			gl.glLightf( lightNumber , GL.GL_QUADRATIC_ATTENUATION , light.getQuadraticAttenuation() );
		}

		gl.glEnable( lightNumber );

		/**
		 * Determine dominant light position, used for bump mapping.
		 * This method can be rather inaccurate, especially if the most
		 * intense light is far away from a bump mapped object.
		 */
		final float lightIntensity = light.getIntensity();
		if ( ( _dominantLightPosition == null ) || ( _dominantLightIntensity < lightIntensity ) )
		{
			_dominantLightPosition = Vector3D.INIT.set( light2world.xo , light2world.yo , light2world.zo );
			_dominantLightIntensity = lightIntensity;
		}
	}

	protected void renderObjectBegin( final Matrix3D object2world , final Object3D object , final RenderStyle objectStyle )
	{
		_lightPositionRelativeToObject = ( _dominantLightPosition != null ) ? object2world.inverseTransform( _dominantLightPosition ) : null;
		_gl.glPushMatrix();
		JOGLTools.glMultMatrixd( _gl , object2world );
	}

	protected void renderObject( final Object3D object , final RenderStyle objectStyle , final Collection<RenderStyleFilter> styleFilters , final Matrix3D object2world )
	{
		boolean anyMaterialEnabled = false;
		boolean anyFillEnabled     = false;
		boolean anyStrokeEnabled   = false;
		boolean anyVertexEnabled   = false;

		final int faceCount = object.getFaceCount();
		final RenderStyle[] faceStyles = new RenderStyle[ faceCount ];

		for ( int j = 0 ; j < faceCount; j++ )
		{
			final RenderStyle faceStyle = objectStyle.applyFilters( styleFilters , object.getFace( j ) );

			anyMaterialEnabled |= faceStyle.isMaterialEnabled();
			anyFillEnabled     |= faceStyle.isFillEnabled();
			anyStrokeEnabled   |= faceStyle.isStrokeEnabled();
			anyVertexEnabled   |= faceStyle.isVertexEnabled();

			faceStyles[ j ] = faceStyle;
		}

		if ( anyMaterialEnabled || anyFillEnabled || anyStrokeEnabled || anyVertexEnabled )
		{
			if ( anyMaterialEnabled )
			{
				for ( int j = 0 ; j < faceCount; j++ )
				{
					final RenderStyle faceStyle = faceStyles[ j ];
					if ( faceStyle.isMaterialEnabled() )
					{
						renderMaterialFace( object.getFace( j ), faceStyle );
					}
				}
			}
			else if ( anyFillEnabled )
			{
				for ( int j = 0 ; j < faceCount; j++ )
				{
					final RenderStyle faceStyle = faceStyles[ j ];
					if ( faceStyle.isFillEnabled() )
					{
						renderFilledFace( object.getFace( j ), faceStyle );
					}
				}
			}

			if ( anyStrokeEnabled )
			{
				for ( int j = 0 ; j < faceCount; j++ )
				{
					final RenderStyle faceStyle = faceStyles[ j ];
					if ( faceStyle.isStrokeEnabled() )
					{
						renderStrokedFace( object.getFace( j ), faceStyle );
					}
				}
			}

			if ( anyVertexEnabled )
			{
				for ( int j = 0 ; j < faceCount; j++ )
				{
					final RenderStyle faceStyle = faceStyles[ j ];
					if ( faceStyle.isVertexEnabled() )
					{
						renderFaceVertices( faceStyle , object2world , object.getFace( j ) );
					}
				}
			}
		}
	}

	protected void renderObjectEnd()
	{
		_gl.glPopMatrix();
	}

	protected void renderMaterialFace( final Face3D face , final RenderStyle style )
	{
		final int      vertexCount = face.getVertexCount();
		final Material material    = ( style.getMaterialOverride() != null ) ? style.getMaterialOverride() : face.material;

		if ( ( material != null ) && ( vertexCount >= 2 ) )
		{
			final GL        gl        = _gl;
			final GLWrapper glWrapper = _glWrapper;

			final MultiPassRenderMode renderMode   = _renderMode;

			final float   extraAlpha    = style.getMaterialAlpha();
			final float   combinedAlpha = material.diffuseColorAlpha * extraAlpha;
			final boolean blend         = !isDepthPeelingEnabled() &&
			                              ( renderMode != MultiPassRenderMode.OPAQUE_ONLY ) && ( combinedAlpha < 0.99f );

			if ( ( ( renderMode != MultiPassRenderMode.OPAQUE_ONLY      ) || ( combinedAlpha >= 1.0f ) ) &&
			     ( ( renderMode != MultiPassRenderMode.TRANSPARENT_ONLY ) || ( combinedAlpha <  1.0f ) ) )
			{
				final Map<String,Texture> textureCache = _textureCache;

				final Vector3D lightPosition    = _lightPositionRelativeToObject;
				final boolean  hasLighting      = style.isMaterialLightingEnabled() && ( lightPosition != null );
				final boolean  backfaceCulling  = style.isBackfaceCullingEnabled()  && !face.isTwoSided();
				final boolean  setVertexNormals = hasLighting                       && face.smooth;

				/*
				 * Get textures.
				 */
				final Texture colorMap    = JOGLTools.getColorMapTexture( gl , material , textureCache );
				final Texture bumpMap     = hasLighting ? JOGLTools.getBumpMapTexture( gl , material , textureCache ) : null;
				final boolean hasColorMap = ( colorMap != null );
				final boolean hasBumpMap  = ( bumpMap  != null );

				final Texture normalizationCubeMap = hasBumpMap ? JOGLTools.getNormalizationCubeMap( gl , textureCache ) : null;

				/*
				 * Set render/material properties.
				 */
				if ( blend )
				{
					if ( combinedAlpha < 0.25f )
					{
						gl.glDepthMask( false );
					}
					glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
				}
				glWrapper.setBlend( blend );
				glWrapper.setCullFace( backfaceCulling );
				glWrapper.setLighting( hasLighting );
				setMaterial( material , style , extraAlpha );

				/*
				 * Enable bump map.
				 */
				if ( hasBumpMap )
				{
					gl.glActiveTexture( GL.GL_TEXTURE1 );
					bumpMap.enable();
					bumpMap.bind();
					gl.glActiveTexture( GL.GL_TEXTURE0 );
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
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_COMBINE );
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_COMBINE_RGB      , GL.GL_REPLACE );
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE0_RGB      , GL.GL_TEXTURE );

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
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_COMBINE  );
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_COMBINE_RGB      , GL.GL_DOT3_RGB );
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE0_RGB      , GL.GL_PREVIOUS );
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE1_RGB      , GL.GL_TEXTURE  );

					/*
					 * The third unit is used to apply the diffuse color of the
					 * material.
					 */
					gl.glActiveTexture( GL.GL_TEXTURE2 );
					bumpMap.enable();
					bumpMap.bind();
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_COMBINE       );
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_COMBINE_RGB      , GL.GL_MODULATE      );
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE0_RGB      , GL.GL_PRIMARY_COLOR );

					/*
					 * Set The Fourth Texture Unit To Our Texture. Set The Texture
					 * Environment Of The Third Texture Unit To Modulate (Multiply) The
					 * Result Of Our Dot3 Operation With The Texture Value.
					 */
					if ( hasColorMap )
					{
						gl.glActiveTexture( GL.GL_TEXTURE3 );
						gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_MODULATE );
					}
				}

				/*
				 * Enable color map.
				 */
				final TextureCoords colorMapCoords;
				if ( hasColorMap )
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

				/*
				 * Render face.
				 */
				gl.glBegin( ( vertexCount == 1 ) ? GL.GL_POINTS :
				            ( vertexCount == 2 ) ? GL.GL_LINES :
				            ( vertexCount == 3 ) ? GL.GL_TRIANGLES :
				            ( vertexCount == 4 ) ? GL.GL_QUADS : GL.GL_POLYGON );

				if ( !setVertexNormals )
				{
					final Vector3D normal = face.getNormal();
					gl.glNormal3d( normal.x , normal.y , normal.z );
				}

				final List<Vertex> vertices = face.vertices;
				for ( int vertexIndex = vertexCount ; --vertexIndex >= 0 ; )
				{
					final Vertex vertex = vertices.get( vertexIndex );
					final Vector3D point = vertex.point;

					if ( hasBumpMap )
					{
						gl.glMultiTexCoord3d( GL.GL_TEXTURE0 , lightPosition.x + point.x , lightPosition.y + point.y , lightPosition.z + point.z );
						gl.glMultiTexCoord2f( GL.GL_TEXTURE1 , vertex.colorMapU , vertex.colorMapV );

						if ( hasColorMap )
						{
							gl.glMultiTexCoord2f( GL.GL_TEXTURE3 , vertex.colorMapU , vertex.colorMapV );
						}
					}
					else if ( hasColorMap )
					{
						final float u = colorMapCoords.left()   + vertex.colorMapU * ( colorMapCoords.right() - colorMapCoords.left() );
						final float v = colorMapCoords.bottom() + vertex.colorMapV * ( colorMapCoords.top() - colorMapCoords.bottom() );
						gl.glTexCoord2f( u , v );
					}

					if ( setVertexNormals )
					{
						final Vector3D vertexNormal = face.getVertexNormal( vertexIndex );
						gl.glNormal3d( vertexNormal.x , vertexNormal.y , vertexNormal.z );
					}

					gl.glVertex3d( point.x , point.y , point.z );
				}

				gl.glEnd();

				/*
				 * Disable color map.
				 */
				if ( hasColorMap )
				{
					colorMap.disable();
				}

				/*
				 * Disable bump map.
				 */
				if ( hasBumpMap )
				{
					gl.glActiveTexture( GL.GL_TEXTURE2 );
					bumpMap.disable();

					gl.glActiveTexture( GL.GL_TEXTURE1 );
					bumpMap.disable();

					gl.glActiveTexture( GL.GL_TEXTURE0 );
					normalizationCubeMap.disable();
				}

				if ( blend )
				{
					if ( combinedAlpha < 0.25f )
					{
						gl.glDepthMask( true );
					}
				}

				if ( DRAW_NORMALS )
				{
					for ( int vertexIndex = vertexCount ; --vertexIndex >= 0 ; )
					{
						final Vertex vertex = vertices.get( vertexIndex );
						final Vector3D point = vertex.point;

						Vector3D normal = face.smooth ? face.getVertexNormal( vertexIndex ) : face.getNormal();
						normal = point.plus( normal.multiply( 100.0 ) );

						gl.glBegin( GL.GL_LINES );
						gl.glVertex3d( point.x , point.y , point.z );
						gl.glVertex3d( normal.x , normal.y , normal.z );
						gl.glEnd();
					}
				}
			}
		}
	}

	protected void renderFilledFace( final Face3D face , final RenderStyle style )
	{
		final List<Vertex> vertices = face.vertices;
		final int vertexCount = vertices.size();

		if ( vertexCount >= 2 )
		{
			final MultiPassRenderMode renderMode = _renderMode;

			final GL gl = _gl;
			final GLWrapper glWrapper = _glWrapper;

			final Color   color           = style.getFillColor();
			final int     alpha           = color.getAlpha();
			final boolean blend           = !isDepthPeelingEnabled() &&
			                              ( renderMode != MultiPassRenderMode.OPAQUE_ONLY ) && ( alpha < 255 );
			final boolean backfaceCulling = style.isBackfaceCullingEnabled() && !face.isTwoSided();
			final boolean hasLighting     = style.isFillLightingEnabled() && ( _lightPositionRelativeToObject != null );
			final boolean setVertexNormals = hasLighting && face.smooth;

			if ( ( ( renderMode != MultiPassRenderMode.OPAQUE_ONLY      ) || ( alpha == 255 ) ) &&
			     ( ( renderMode != MultiPassRenderMode.TRANSPARENT_ONLY ) || ( alpha <  255 ) ) )
			{
				/*
				 * Set render/material properties.
				 */
				if ( blend )
				{
					if ( alpha < 64 )
					{
						gl.glDepthMask( false );
					}
					glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
				}
				glWrapper.setBlend( blend );
				glWrapper.setPolygonMode( GL.GL_FILL );
				glWrapper.setPolygonOffsetFill( true );
				glWrapper.glPolygonOffset( 0.0f , -1.0f );
				glWrapper.setCullFace( backfaceCulling );
				glWrapper.setLighting( hasLighting );
				setColor( color );
				useShader( hasLighting ? _colored : _unlit );

				/*
				 * Render face.
				 */
				gl.glBegin( ( vertexCount == 1 ) ? GL.GL_POINTS :
				            ( vertexCount == 2 ) ? GL.GL_LINES :
				            ( vertexCount == 3 ) ? GL.GL_TRIANGLES :
				            ( vertexCount == 4 ) ? GL.GL_QUADS : GL.GL_POLYGON );

				if ( !setVertexNormals )
				{
					final Vector3D normal = face.getNormal();
					gl.glNormal3d( normal.x , normal.y , normal.z );
				}

				for ( int vertexIndex = vertexCount ; --vertexIndex >= 0 ; )
				{
					final Vertex vertex = vertices.get( vertexIndex );

					if ( setVertexNormals )
					{
						final Vector3D normal = face.getVertexNormal( vertexIndex );
						gl.glNormal3d( normal.x , normal.y , normal.z );
					}

					gl.glVertex3d( vertex.point.x , vertex.point.y , vertex.point.z );
				}

				gl.glEnd();

				if ( blend )
				{
					if ( alpha < 64 )
					{
						gl.glDepthMask( true );
					}
				}
				glWrapper.setPolygonOffsetFill( false );
			}
		}
	}

	protected void renderStrokedFace( final Face3D face , final RenderStyle style )
	{
		final List<Vertex> vertices = face.vertices;
		final int vertexCount = vertices.size();

		if ( vertexCount >= 2 )
		{
			final Color   color            = style.getStrokeColor();
			final float   width            = style.getStrokeWidth();
			final boolean backfaceCulling  = style.isBackfaceCullingEnabled() && !face.isTwoSided();
			final boolean hasLighting      = style.isStrokeLightingEnabled() && ( _lightPositionRelativeToObject != null );
			final boolean setVertexNormals = hasLighting && face.smooth;

			/*
			 * Set render/material properties.
			 */
			final GLWrapper glWrapper = _glWrapper;
			glWrapper.setBlend( false );
			glWrapper.setPolygonMode( GL.GL_LINE );
			glWrapper.setPolygonOffsetFill( true );
			glWrapper.glPolygonOffset( 0.0f , -2.0f );
			glWrapper.glLineWidth( width );
			glWrapper.setCullFace( backfaceCulling );
			glWrapper.setLighting( hasLighting );
			setColor( color );
			useShader( hasLighting ? _colored : _unlit );

			final GL gl = _gl;

			/*
			 * Render face.
			 */
			// @FIXME Drawing outlines like this is only needed while Face3D doesn't support concave faces.
			// (A single Face3D could then be used for a concave shape, and the proper outline would be drawn automatically.)
			final Object3D object3D = face.getObject();
			if ( object3D instanceof ExtrudedObject2D )
			{
				if ( face == object3D.getFace( 0 ) )
				{
					final ExtrudedObject2D extruded  = (ExtrudedObject2D)object3D;
					final Shape            shape     = extruded.shape;
					final Vector3D         extrusion = extruded.extrusion;

					gl.glPushMatrix();
					JOGLTools.glMultMatrixd( gl , extruded.transform );

					/* Draw the bottom outline of the extruded shape. */
					drawShape( shape , extruded.flatness );

					/* Draw the sides of the extruded shape. */
					drawExtrusionLines( shape , extrusion );

					/* Draw the top outline of the extruded shape. */
					gl.glMultMatrixd( new double[] {
						1.0 , 0.0 , 0.0 , 0.0 ,
						0.0 , 1.0 , 0.0 , 0.0 ,
						0.0 , 0.0 , 1.0 , 0.0 ,
						extrusion.x , extrusion.y ,extrusion.z , 1.0 } , 0 );

					drawShape( shape , extruded.flatness );

					gl.glPopMatrix();
				}
			}
			else
			{
				gl.glBegin( ( vertexCount == 1 ) ? GL.GL_POINTS :
				            ( vertexCount == 2 ) ? GL.GL_LINES :
				            ( vertexCount == 3 ) ? GL.GL_TRIANGLES :
				            ( vertexCount == 4 ) ? GL.GL_QUADS : GL.GL_POLYGON );

				if ( !setVertexNormals )
				{
					final Vector3D normal = face.getNormal();
					gl.glNormal3d( normal.x , normal.y , normal.z );
				}

				for ( int vertexIndex = vertexCount ; --vertexIndex >= 0 ; )
				{
					final Vertex vertex = vertices.get( vertexIndex );
					final Vector3D point = vertex.point;

					if ( setVertexNormals )
					{
						final Vector3D normal = face.getVertexNormal( vertexIndex );
						gl.glNormal3d( normal.x , normal.y , normal.z );
					}

					gl.glVertex3d( point.x , point.y , point.z );
				}

				gl.glEnd();
			}

			glWrapper.setPolygonMode( GL.GL_FILL );
			glWrapper.setPolygonOffsetFill( false );
		}
	}

	protected void renderFaceVertices( final RenderStyle style , final Matrix3D object2world , final Face3D face )
	{
		final List<Vertex> vertices = face.vertices;
		final int vertexCount = vertices.size();

		if ( vertexCount > 0 )
		{
			final GL gl = _gl;
			final GLWrapper glWrapper = _glWrapper;

			final Color   color            = style.getVertexColor();
			final boolean backfaceCulling  = style.isBackfaceCullingEnabled() && !face.isTwoSided();
			final boolean hasLighting      = style.isVertexLightingEnabled() && ( _lightPositionRelativeToObject != null );
			final boolean setVertexNormals = hasLighting && face.smooth;

			/*
			 * Set render/material properties.
			 */
			glWrapper.setBlend( false  );
			glWrapper.setPolygonMode( GL.GL_POINT );
			glWrapper.setPolygonOffsetFill( true );
			glWrapper.glPolygonOffset( 0.0f , -2.0f );
			glWrapper.setCullFace( backfaceCulling );
			glWrapper.setLighting( hasLighting );
			setColor( color );

			/*
			 * Render face.
			 */
			gl.glBegin( ( vertexCount == 1 ) ? GL.GL_POINTS :
			            ( vertexCount == 2 ) ? GL.GL_LINES :
			            ( vertexCount == 3 ) ? GL.GL_TRIANGLES :
			            ( vertexCount == 4 ) ? GL.GL_QUADS : GL.GL_POLYGON );

			if ( !setVertexNormals )
			{
				final Vector3D normal = face.getNormal();
				gl.glNormal3d( normal.x , normal.y , normal.z );
			}

			for ( int vertexIndex = vertexCount ; --vertexIndex >= 0 ; )
			{
				final Vertex vertex = vertices.get( vertexIndex );
				final Vector3D point = vertex.point;

				if ( setVertexNormals )
				{
					final Vector3D normal = face.getVertexNormal( vertexIndex );
					gl.glNormal3d( normal.x , normal.y , normal.z );
				}

				gl.glVertex3d( point.x , point.y , point.z );
			}

			gl.glEnd();

			glWrapper.setPolygonMode( GL.GL_FILL );
			glWrapper.setPolygonOffsetFill( false );
		}
	}

	/**
	 * Set GL material properties.
	 *
	 * @param   color   Color to set.
	 */
	private void setColor( final Color color )
	{
		final float[] rgba  = color.getRGBComponents( null );
		final float   red   = rgba[ 0 ];
		final float   green = rgba[ 1 ];
		final float   blue  = rgba[ 2 ];
		final float   alpha = rgba[ 3 ];

		setColor( red , green , blue , alpha );
	}

	/**
	 * Sets the current color and material to the given color.
	 *
	 * @param   red     Red component.
	 * @param   green   Green component.
	 * @param   blue    Blue component.
	 * @param   alpha   Alpha component.
	 */
	private void setColor( final float red , final float green , final float blue , final float alpha )
	{
		final GLWrapper glWrapper = _glWrapper;
		glWrapper.setColor( red , green , blue , alpha );
		glWrapper.setMaterialAmbient( red , green , blue , alpha );
		glWrapper.setMaterialDiffuse( red , green , blue , alpha );
		glWrapper.setMaterialSpecular( 1.0f , 1.0f , 1.0f , alpha );
		glWrapper.setMaterialShininess( 16.0f );
		glWrapper.setMaterialEmission( 0.0f , 0.0f , 0.0f , alpha );
	}

	/**
	 * Set GL material properties.
	 *
	 * @param   material    {@link Material} properties.
	 * @param   style       Render style to be applied.
	 * @param   extraAlpha  Extra alpha multiplier.
	 */
	public void setMaterial( final Material material , final RenderStyle style , final float extraAlpha )
	{
		final float red;
		final float green;
		final float blue;
		final float alpha;

		if ( style.isFillEnabled() )
		{
			final Color materialDiffuse = new Color( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , extraAlpha * material.diffuseColorAlpha );
			final Color diffuse = RenderStyle.blendColors( style.getFillColor() , materialDiffuse );
			final float[] diffuseComponents = diffuse.getRGBComponents( null );

			red   = diffuseComponents[ 0 ];
			green = diffuseComponents[ 1 ];
			blue  = diffuseComponents[ 2 ];
			alpha = diffuseComponents[ 3 ];
		}
		else
		{
			red   = material.diffuseColorRed;
			green = material.diffuseColorGreen;
			blue  = material.diffuseColorBlue;
			alpha = material.diffuseColorAlpha * extraAlpha;
		}

		final GLWrapper glWrapper = _glWrapper;

		glWrapper.setColor( red , green , blue , alpha );
		glWrapper.setMaterialAmbient( material.ambientColorRed , material.ambientColorGreen , material.ambientColorBlue , alpha );
		glWrapper.setMaterialDiffuse( red , green , blue , alpha );
		glWrapper.setMaterialSpecular( material.specularColorRed , material.specularColorGreen , material.specularColorBlue , alpha );
		glWrapper.setMaterialEmission( material.emissiveColorRed , material.emissiveColorGreen , material.emissiveColorBlue , alpha );
		glWrapper.setMaterialShininess( (float)material.shininess );
	}

	/**
	 * Draws the outline of the given shape.
	 *
	 * @param   shape       Shape to be drawn.
	 * @param   flatness    Flatness used to interpolate the shape's curves.
	 *
	 * @see     Shape#getPathIterator(AffineTransform ,double)
	 */
	private void drawShape( final Shape shape , final double flatness )
	{
		final GL gl = _gl;
		gl.glBegin( GL.GL_LINE_STRIP );

		boolean isFirst = true;
		double  startX  = 0.0;
		double  startY  = 0.0;

		final double[] coords = new double[ 6 ];
		for ( final PathIterator pathIterator = shape.getPathIterator( null , flatness ) ; !pathIterator.isDone() ; pathIterator.next() )
		{
			final double x;
			final double y;

			final int type = pathIterator.currentSegment( coords );
			switch ( type )
			{
				case PathIterator.SEG_MOVETO:
					if ( !isFirst )
					{
						/* Start a new line strip. */
						gl.glEnd();
						gl.glBegin( GL.GL_LINE_STRIP );
					}
					else
					{
						isFirst = false;
					}

					x = coords[ 0 ];
					y = coords[ 1 ];
					startX = x;
					startY = y;
					break;

				case PathIterator.SEG_CLOSE:
					x = startX;
					y = startY;
					break;
				case PathIterator.SEG_LINETO:
					x = coords[ 0 ];
					y = coords[ 1 ];
					break;
				case PathIterator.SEG_QUADTO: // reduce to line
					x = coords[ 2 ];
					y = coords[ 3 ];
					break;
				case PathIterator.SEG_CUBICTO: // reduce to line
					x = coords[ 4 ];
					y = coords[ 5 ];
					break;

				default:
					throw new AssertionError( "unknown type: " + type );
			}

			gl.glVertex3d( x , y , 0.0 );
		}

		gl.glEnd();
	}

	/**
	 * Draw the sides of the extruded shape.
	 *
	 * @param   shape       Base shape.
	 * @param   extrusion   Extrusion vector (control-point displacement). This is a displacement relative to the shape being extruded.
	 */
	private void drawExtrusionLines( final Shape shape , final Vector3D extrusion )
	{
		final GL gl = _gl;

		gl.glBegin( GL.GL_LINES );

		final double[] coords = new double[ 6 ];
		for ( final PathIterator pathIterator = shape.getPathIterator( null ) ; !pathIterator.isDone() ; pathIterator.next() )
		{
			final double x;
			final double y;

			final int type = pathIterator.currentSegment( coords );
			switch ( type )
			{
				case PathIterator.SEG_MOVETO:
					x = coords[ 0 ];
					y = coords[ 1 ];
					break;
				case PathIterator.SEG_CLOSE:
					continue;
				case PathIterator.SEG_LINETO:
					x = coords[ 0 ];
					y = coords[ 1 ];
					break;
				case PathIterator.SEG_QUADTO: // reduce to line
					x = coords[ 2 ];
					y = coords[ 3 ];
					break;
				case PathIterator.SEG_CUBICTO: // reduce to line
					x = coords[ 4 ];
					y = coords[ 5 ];
					break;

				default:
					throw new AssertionError( "unknown type: " + type );
			}

			gl.glVertex3d( x , y , 0.0 );
			gl.glVertex3d( x + extrusion.x , y + extrusion.y , extrusion.z );
		}

		gl.glEnd();
	}

	/**
	 * Draw a 3D grid centered around point x,y,z with size dx,dy.
	 *
	 * @param   grid2world          Transforms grid to world coordinates.
	 * @param   gridBounds          Bounds of grid.
	 * @param   cellSize            Size of each cell.
	 * @param   hightlightAxes      If set, hightlight X=0 and Y=0 axes.
	 * @param   highlightInterval   Interval to use for highlighting grid lines.
	 */
	public void drawGrid( final Matrix3D grid2world , final Rectangle gridBounds , final int cellSize , final boolean hightlightAxes , final int highlightInterval )
	{
		if ( ( grid2world != null ) && ( gridBounds != null ) && ( gridBounds.width >= 0 ) && ( gridBounds.height >= 0 ) && ( cellSize >= 0 ) ) // argument sanity
		{
			final GL gl = _gl;
			final GLWrapper glWrapper = _glWrapper;

			gl.glPushMatrix();
			JOGLTools.glMultMatrixd( gl , grid2world );

			glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
			glWrapper.setBlend( true );
			glWrapper.setLineSmooth( true );
			glWrapper.setLighting( false );

			final int minCellX = gridBounds.x;
			final int maxCellX = minCellX + gridBounds.width;
			final int minCellY = gridBounds.y;
			final int maxCellY = minCellY + gridBounds.height;

			final int minX = minCellX * cellSize;
			final int maxX = maxCellX * cellSize;
			final int minY = minCellY * cellSize;
			final int maxY = maxCellY * cellSize;

			if ( hightlightAxes )
			{
				final boolean hasXaxis = ( minCellY <= 0 ) && ( maxCellY >= 0 );
				final boolean hasYaxis = ( minCellX <= 0 ) && ( maxCellX >= 0 );

				if ( ( hasXaxis || hasYaxis ) )
				{
					glWrapper.glLineWidth( 2.5f );
					setColor( 0.1f , 0.1f , 0.1f , 1.0f );
					gl.glBegin( GL.GL_LINES );

					if ( hasXaxis )
					{
						gl.glVertex3i( minX , 0 , 0 );
						gl.glVertex3i( maxX , 0 , 0 );
					}

					if ( hasYaxis )
					{
						gl.glVertex3i( 0 , minY , 0 );
						gl.glVertex3i( 0 , maxY , 0 );
					}

					gl.glEnd();
				}
			}

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
					glWrapper.glLineWidth( 1.5f );
					setColor( 0.5f , 0.5f , 0.5f , 1.0f );
					gl.glBegin( GL.GL_LINES );

					for ( int x = highlightMinX ; x <= highLightMaxX ; x += highlightInterval )
					{
						if ( !hightlightAxes || ( x != 0 ) )
						{
							gl.glVertex3i( x * cellSize , minY , 0 );
							gl.glVertex3i( x * cellSize , maxY , 0 );
						}
					}

					for ( int y = highlightMinY ; y <= highLightMaxY ; y += highlightInterval )
					{
						if ( !hightlightAxes || ( y != 0 ) )
						{
							gl.glVertex3i( minX , y * cellSize , 0 );
							gl.glVertex3i( maxX , y * cellSize , 0 );
						}
					}

					gl.glEnd();
				}
			}

			glWrapper.glLineWidth( 1.0f );
			setColor( 0.75f , 0.75f , 0.75f , 1.0f );
			gl.glBegin( GL.GL_LINES );

			for ( int x = minCellX ; x <= maxCellX ; x++ )
			{
				if ( ( !hightlightAxes || ( x != 0 ) ) && ( ( highlightInterval <= 1 ) || ( x % highlightInterval != 0 ) ) )
				{
					gl.glVertex3i( x * cellSize , minY , 0 );
					gl.glVertex3i( x * cellSize , maxY , 0 );
				}
			}

			for ( int y = minCellY ; y <= maxCellY ; y++ )
			{
				if ( ( !hightlightAxes || ( y != 0 ) ) && ( ( highlightInterval <= 1 ) || ( y % highlightInterval != 0 ) ) )
				{
					gl.glVertex3i( minX , y * cellSize , 0 );
					gl.glVertex3i( maxX , y * cellSize , 0 );
				}
			}

			gl.glEnd();

			gl.glPopMatrix();
		}
	}
}
