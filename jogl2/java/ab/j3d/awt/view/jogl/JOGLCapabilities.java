/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2015 Peter S. Heijnen
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

import java.util.logging.*;
import javax.media.opengl.*;

import ab.j3d.view.*;

/**
 * Provides information about the capabilities and properties of an OpenGL
 * context.
 *
 * @author G. Meinders
 */
public class JOGLCapabilities
{
	/**
	 * OpenGL context to be used.
	 */
	private final GLContext _context;

	/**
	 * Flag to indicate that capabilities were determined by the {@link
	 * #determineCapabilities} method. This is done to prevent multiple (expensive)
	 * capability probes.
	 */
	private boolean _capabilitiesDetermined = false;

	/**
	 * OpenGL version 1.3 or above.
	 */
	private boolean _opengl13 = false;

	/**
	 * GLSL vertex/pixel shaders.
	 */
	private boolean _shaderObjects = false;

	/**
	 * GLSL vertex/pixel shaders (extension).
	 */
	private boolean _shaderObjectsARB = false;

	/**
	 * Off-screen rendering to framebuffer.
	 */
	private boolean _framebufferObject = false;

	/**
	 * Draw buffers.
	 */
	private boolean _drawBuffers = false;

	/**
	 * Draw buffers (extension).
	 */
	private boolean _drawBuffersARB = false;

	/**
	 * Occlusion query.
	 */
	private boolean _occlusionQuery = false;

	/**
	 * Occlusion query (extension).
	 */
	private boolean _occlusionQueryARB = false;

	/**
	 * Additional comparisons on shadow maps.
	 */
	private boolean _shadowFuncs = false;

	/**
	 * Depth texture support.
	 */
	private boolean _depthTexture = false;

	/**
	 * Depth buffer bits.
	 */
	private int _depthBits = -1;

	/**
	 * Depth/shadow texture support.
	 */
	private boolean _shadow = false;

	/**
	 * Rectangular textures.
	 */
	private boolean _textureRectangle = false;

	/**
	 * Non-power-of-two textures.
	 */
	private boolean _nonPowerOfTwo = false;

	/**
	 * Non-power-of-two textures (extension).
	 */
	private boolean _nonPowerOfTwoARB = false;

	/**
	 * Multi-texturing.
	 */
	private boolean _multitexture = false;

	/**
	 * Auto-generated mipmaps.
	 */
	private boolean _generateMipmap = false;

	/**
	 * Seperate blending for color and alpha.
	 */
	private boolean _blendFuncSeperate = false;

	/**
	 * Texture clamp to edges.
	 */
	private boolean _edgeClamp = false;

	/**
	 * Number of supported texture units.
	 */
	private int _maxTextureUnits = 1;

	/**
	 * OpenGL version.
	 */
	private String _version = null;

	/**
	 * OpenGL implementation vendor.
	 */
	private String _vendor = null;

	/**
	 * Supported extensions.
	 */
	private String _extensions = null;

	/**
	 * OpenGL implementation name/version.
	 */
	private String _renderer = null;

	/**
	 * Shading language version.
	 */
	private String _shadingLanguageVersion = null;

	/**
	 * Maximum number of varying floats.
	 */
	private int _maxVaryingFloats = -1;

	/**
	 * Maximum number of combined texture image units.
	 */
	private int _maxCombinedTextureImageUnits = -1;

	/**
	 * Maximum number of texture image units.
	 */
	private int _maxTextureImageUnits = -1;

	/**
	 * Maximum number of fragment shader uniform components.
	 */
	private int _maxFragmentUniformComponents = -1;

	/**
	 * Constructs a JOGL capabilities instance for the given OpenGL context.
	 *
	 * @param context OpenGL context to be used.
	 */
	public JOGLCapabilities( final GLContext context )
	{
		if ( context == null )
		{
			throw new NullPointerException( "context" );
		}

		_context = context;
	}

	/**
	 * Determines the capabilities and properties of the OpenGL context.
	 *
	 * @throws GLException if an OpenGL call fails.
	 */
	private void determineCapabilities()
	throws GLException
	{
		if ( !_capabilitiesDetermined )
		{
			_capabilitiesDetermined = true;

			final Probe probe = new CapabilitiesProbe();
			probe.invokeAndWait();
		}
	}

	/**
	 * Returns whether a rectangular texture target is supported.
	 *
	 * @return <code>true</code> if a rectangular texture target is supported.
	 */
	public boolean isTextureRectangleSupported()
	{
		determineCapabilities();
		return _textureRectangle;
	}

	/**
	 * Returns whether non-power-of-two sized textures are supported.
	 *
	 * @return <code>true</code> if non-power-of-two textures are supported.
	 */
	public boolean isNonPowerOfTwoSupported()
	{
		determineCapabilities();
		return _nonPowerOfTwo;
	}

	/**
	 * Returns whether non-power-of-two sized textures are supported using the ARB
	 * extension.
	 *
	 * @return <code>true</code> if non-power-of-two textures are supported using
	 *         the ARB extension.
	 */
	public boolean isNonPowerOfTwoARBSupported()
	{
		determineCapabilities();
		return _nonPowerOfTwoARB;
	}

	/**
	 * Returns whether GLSL shader objects are supported using the OpenGL core API.
	 * Shaders were added to the core in OpenGL 2.0.
	 *
	 * @return <code>true</code> if support of GLSL shaders is available.
	 */
	public boolean isShaderSupported()
	{
		determineCapabilities();
		return _shaderObjects && ( _shadingLanguageVersion != null );
	}

	/**
	 * Returns whether GLSL shader objects are supported using the ARB extension,
	 * which was written for OpenGL version 1.4.
	 *
	 * @return <code>true</code> if support of GLSL shaders is available.
	 */
	public boolean isShaderSupportedARB()
	{
		determineCapabilities();
		return _shaderObjectsARB && ( _shadingLanguageVersion != null );
	}

	/**
	 * Returns whether cube maps are supported.
	 *
	 * @return <code>true</code> if cube maps are supported.
	 */
	public boolean isCubeMapSupported()
	{
		determineCapabilities();
		return _opengl13;
	}

	/**
	 * Returns whether {@link JOGLRenderer} can load, compile and link the shaders
	 * it requires. This may fail due to driver bugs or unknown programming
	 * errors.
	 *
	 * @return <code>true</code> if the shaders are working correctly.
	 */
	public boolean isJOGLRendererShadersSupported()
	{
		final JOGLRendererShadersProbe probe = new JOGLRendererShadersProbe();
		probe.invokeAndWait();
		return probe._result;
	}

	/**
	 * Returns the maximum number of regular texture units, as defined by
	 * ARB_multitexture and OpenGL 1.3.
	 *
	 * @return Maximum number of texture units; at least <code>2</code>.
	 */
	public int getMaxTextureUnits()
	{
		return _maxTextureUnits;
	}

	/**
	 * Logs a summary of OpenGL information and capabilities to the given
	 * logger.
	 *
	 * @param logger Logger.
	 * @param level  Log level.
	 */
	public void logSummary( final Logger logger, final Level level )
	{
		if ( logger.isLoggable( level ) )
		{
			determineCapabilities();

			logger.log( level, "OpenGL driver:       vendor={0}, renderer={1}, version={2}, shaders={3}",
			            new Object[] { _vendor, _renderer, _version, ( ( _shadingLanguageVersion != null ) ? _shadingLanguageVersion : "none" ) } );
			logger.log( level, "OpenGL extensions:   {0}", _extensions );
			logger.log( level, "OpenGL capabilities: shaderObjects={0}, framebufferObject={1}, drawBuffers={2}, occlusionQuery={3}, shadowFuncs={4}, depthTexture={5}, depthBufferBits={6}, shadow={7}, textureRectangle={8}, multitexture={9}, generateMipmap={10}, blendFuncSeperate={11}, edgeClamp={12}, nonPowerOfTwo={13}",
			            new Object[] {
			            ( _shaderObjects ? "yes (core)" : _shaderObjectsARB ? "yes (ARB)" : "no" ),
			            ( _framebufferObject ? "yes" : "no" ),
			            ( _drawBuffers ? "yes (core)" : _drawBuffersARB ? "yes (ARB)" : "no" ),
			            ( _occlusionQuery ? "yes (core)" : _occlusionQueryARB ? "yes (ARB)" : "no" ),
			            ( _shadowFuncs ? "yes" : "no" ),
			            ( _depthTexture ? "yes" : "no" ),
			            ( _depthBits < 0 ) ? "unknown" : String.valueOf( _depthBits ),
			            ( _shadow ? "yes" : "no" ),
			            ( _textureRectangle ? "yes" : "no" ),
			            ( _multitexture ? "yes" : "no" ),
			            ( _generateMipmap ? "yes" : "no" ),
			            ( _blendFuncSeperate ? "yes" : "no" ),
			            ( _edgeClamp ? "yes" : "no" ),
			            ( _nonPowerOfTwo ? _nonPowerOfTwoARB ? "yes (core,ARB)" : "yes (core)" : _nonPowerOfTwoARB ? "yes (ARB)" : "no" )
			            } );
			logger.log( level, "OpenGL limitations:  uniforms {0}, varyings {1}, combined tex units {2}, tex units {3}",
			            new Object[] { String.valueOf( _maxFragmentUniformComponents ), String.valueOf( _maxVaryingFloats ), String.valueOf( _maxCombinedTextureImageUnits ), String.valueOf( _maxTextureImageUnits ) } );
		}
	}

	/**
	 * Check the OpenGL context for various capabilities.
	 */
	private class CapabilitiesProbe
	extends Probe
	{
		@Override
		protected void run( final GL gl )
		{
			final String version = gl.glGetString( GL.GL_VERSION );
			final boolean opengl12 = "1.2".compareTo( version ) <= 0;
			final boolean opengl13 = "1.3".compareTo( version ) <= 0;
			final boolean opengl14 = "1.4".compareTo( version ) <= 0;
			final boolean opengl15 = "1.5".compareTo( version ) <= 0;
			final boolean opengl20 = "2.0".compareTo( version ) <= 0;

			_opengl13 = opengl13;

			_textureRectangle = gl.isExtensionAvailable( "GL_ARB_texture_rectangle" );

			_nonPowerOfTwo = opengl20;
			_nonPowerOfTwoARB = gl.isExtensionAvailable( "GL_ARB_texture_non_power_of_two" );

			_framebufferObject = gl.isExtensionAvailable( "GL_EXT_framebuffer_object" );

			_shaderObjects = opengl20;
			_shaderObjectsARB = gl.isExtensionAvailable( "GL_ARB_vertex_shader" ) &&
			                    gl.isExtensionAvailable( "GL_ARB_fragment_shader" ) &&
			                    gl.isExtensionAvailable( "GL_ARB_shader_objects" );

			_drawBuffers = opengl20;
			_drawBuffersARB = gl.isExtensionAvailable( "GL_ARB_draw_buffers" );

			// NOTE: For shaders written in low-level assembly langauge, the
			// following extensions would be needed: (or OpenGL 2+)
			//  - GL_ARB_fragment_program
			//  - GL_ARB_vertex_program

			_occlusionQuery = opengl15;
			_occlusionQueryARB = gl.isExtensionAvailable( "GL_ARB_occlusion_query" );

			_shadowFuncs = opengl15 || gl.isExtensionAvailable( "GL_EXT_shadow_funcs" );
			_depthTexture = opengl14 || gl.isExtensionAvailable( "GL_ARB_depth_texture" );
			_shadow = opengl14 || gl.isExtensionAvailable( "GL_ARB_shadow" );
			_blendFuncSeperate = opengl14 || gl.isExtensionAvailable( "GL_EXT_blend_func_separate" );
			_generateMipmap = opengl14 || gl.isExtensionAvailable( "GL_SGIS_generate_mipmap" );

			if ( opengl13 || gl.isExtensionAvailable( "GL_ARB_multitexture" ) )
			{
				_multitexture = true;
				_maxTextureUnits = getInteger( gl, GL2ES1.GL_MAX_TEXTURE_UNITS );
			}

			/*
			 * NOTE: The extension is specified as 'GL_SGIS_texture_edge_clamp',
			 *       but Nvidia uses 'GL_EXT_texture_edge_clamp'.
			 */
			_edgeClamp = opengl12 || gl.isExtensionAvailable( "GL_SGIS_texture_edge_clamp" )
			             || gl.isExtensionAvailable( "GL_EXT_texture_edge_clamp" );

			final int[] colorBits = new int[ 4 ];
			gl.glGetIntegerv( GL.GL_RED_BITS, colorBits, 0 );
			gl.glGetIntegerv( GL.GL_GREEN_BITS, colorBits, 1 );
			gl.glGetIntegerv( GL.GL_BLUE_BITS, colorBits, 2 );
			gl.glGetIntegerv( GL.GL_ALPHA_BITS, colorBits, 3 );
			_depthBits = getInteger( gl, GL.GL_DEPTH_BITS );

			// Limits the number of passes that could be combined using a
			// multi-layer depth-peeling algorithm.
			if ( _drawBuffers )
			{
				getInteger( gl, GL2ES2.GL_MAX_DRAW_BUFFERS );
			}

			// Limits the complexity of shaders we can use.
			if ( _shaderObjects )
			{
				_maxVaryingFloats = getInteger( gl, GL2GL3.GL_MAX_VARYING_FLOATS );
				_maxCombinedTextureImageUnits = getInteger( gl, GL2ES2.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS );
				_maxTextureImageUnits = getInteger( gl, GL2ES2.GL_MAX_TEXTURE_IMAGE_UNITS );
				_maxFragmentUniformComponents = getInteger( gl, GL2ES3.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS );
				_shadingLanguageVersion = gl.glGetString( GL2ES2.GL_SHADING_LANGUAGE_VERSION );
			}
			else if ( _shaderObjectsARB )
			{
				_maxVaryingFloats = getInteger( gl, GL2GL3.GL_MAX_VARYING_FLOATS );
				_maxCombinedTextureImageUnits = getInteger( gl, GL2ES2.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS );
				_maxTextureImageUnits = getInteger( gl, GL2.GL_MAX_TEXTURE_IMAGE_UNITS_ARB );
				_maxFragmentUniformComponents = getInteger( gl, GL2ES3.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS );
				_shadingLanguageVersion = gl.glGetString( GL2.GL_SHADING_LANGUAGE_VERSION_ARB );
			}

			_version = gl.glGetString( GL.GL_VERSION );
			_vendor = gl.glGetString( GL.GL_VENDOR );
			_extensions = gl.glGetString( GL.GL_EXTENSIONS );
			_renderer = gl.glGetString( GL.GL_RENDERER );
		}
	}

	/**
	 * Returns the value of the given OpenGL parameter. Retrieving an
	 * <code>int</code> value with <code>glGetInteger</code> requires an array.
	 * This method wraps the process of creating and reading from the array.
	 *
	 * @param gl        OpenGL pipeline.
	 * @param parameter Parameter to get. See {@link GL} constants.
	 *
	 * @return Value of the parameter.
	 *
	 * @throws GLException if an OpenGL-specific exception occurs.
	 */
	private static int getInteger( final GL gl, final int parameter )
	{
		final int[] result = new int[ 1 ];
		gl.glGetIntegerv( parameter, result, 0 );
		return result[ 0 ];
	}

	/**
	 * Check the OpenGL context for various capabilities.
	 */
	private class JOGLRendererShadersProbe
	extends Probe
	{
		private boolean _result = false;

		@Override
		protected void run( final GL gl )
		{
			if ( isShaderSupported() )
			{
				final JOGLConfiguration configuration = new JOGLConfiguration();
				final TextureCache textureCache = new TextureCache();
				final JOGLRenderer renderer = new JOGLRenderer( _context.getGL(), configuration, textureCache, null );
				renderer.init();
				final ShaderManager shaderManager = renderer.getShaderManager();
				_result = shaderManager.isShaderSupportAvailable();
				textureCache.dispose();
			}
		}
	}

	/**
	 * Base class for an object that performs OpenGL calls to determine the
	 * capabilities and properties of the OpenGL implementation.
	 */
	private abstract class Probe
	implements Runnable
	{
		/**
		 * This method is called on the OpenGL thread.
		 *
		 * @param gl OpenGL pipeline.
		 */
		protected abstract void run( GL gl );

		public final void run()
		{
			makeContextCurrentAndRun();
		}

		/**
		 * Makes the OpenGL context current on the calling thread, calls {@link
		 * #run(GL)} and then releases the OpenGL context.
		 */
		private void makeContextCurrentAndRun()
		{
			final GLContext context = _context;
			final GLContext current = GLContext.getCurrent();

			if ( context != current )
			{
				System.out.println( "Making context current..." );
				if ( context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT )
				{
					throw new GLException( "GLContext.makeCurrent failed" );
				}
			}

			try
			{
				run( context.getGL() );
			}
			finally
			{
				if ( context != current )
				{
					System.out.println( "Releasing context..." );
					context.release();

					if ( current != null )
					{
						System.out.println( "Making previous context current..." );
						if ( current.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT )
						{
							throw new GLException( "GLContext.makeCurrent failed (to restore old context)" );
						}
					}
				}
			}
		}

		/**
		 * Runs the probe on the OpenGL thread. This method blocks until the probe is
		 * completed.
		 */
		public final void invokeAndWait()
		{
			if ( Threading.isOpenGLThread() )
			{
				makeContextCurrentAndRun();
			}
			else
			{
				Threading.invokeOnOpenGLThread( true, this );
			}
		}
	}
}
