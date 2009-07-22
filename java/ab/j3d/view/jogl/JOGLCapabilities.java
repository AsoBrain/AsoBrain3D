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
import java.io.PrintStream;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;
import javax.media.opengl.Threading;

import com.sun.opengl.util.texture.Texture;

import ab.j3d.Matrix3D;

/**
 * Provides information about the capabilities and properties of an OpenGL
 * context.
 *
 * @author  G. Meinders
 * @version $Revision$
 */
public class JOGLCapabilities
{
	/**
	 * OpenGL context to be used.
	 */
	private final GLContext _context;

	/**
	 * Flag to indicate that capabilities were determined by the
	 * {@link #determineCapabilities} method. This is done to prevent
	 * multiple (expensive) capability probes.
	 */
	private boolean _capabilitiesDetermined = false;

	/** GLSL vertex/pixel shaders.             */ private boolean _shaderObjects     = false;
	/** GLSL vertex/pixel shaders (extension). */ private boolean _shaderObjectsARB  = false;

	/** Off-screen rendering to framebuffer.   */ private boolean _framebufferObject = false;
	/** Draw buffers.                          */ private boolean _drawBuffers       = false;
	/** Draw buffers (extension).              */ private boolean _drawBuffersARB    = false;

	/** Occlusion query.                       */ private boolean _occlusionQuery    = false;
	/** Occlusion query (extension).           */ private boolean _occlusionQueryARB = false;

	/** Additional comparisons on shadow maps. */ private boolean _shadowFuncs       = false;
	/** Depth texture support.                 */ private boolean _depthTexture      = false;
	/** Depth/shadow texture support.          */ private boolean _shadow            = false;

	/** Rectangular textures.                  */ private boolean _textureRectangle  = false;
	/** Multi-texturing.                       */ private boolean _multitexture      = false;
	/** Auto-generated mipmaps.                */ private boolean _generateMipmap    = false;
	/** Seperate blending for color and alpha. */ private boolean _blendFuncSeperate = false;
	/** Texture clamp to edges.                */ private boolean _edgeClamp         = false;

	/** OpenGL version.                        */ private String _version    = null;
	/** OpenGL implementation vendor.          */ private String _vendor     = null;
	/** Supported extensions.                  */ private String _extensions = null;
	/** OpenGL implementation name/version.    */ private String _renderer   = null;

	/** Shading language version.              */ private String _shadingLanguageVersion = null;

	/**
	 * Constructs a JOGL capabilities instance for the given OpenGL context.
	 *
	 * @param   context     OpenGL context to be used.
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
	 * @throws  GLException if an OpenGL call fails.
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
	 * Returns whether GLSL shader objects are supported using the OpenGL core
	 * API. Shaders were added to the core in OpenGL 2.0.
	 *
	 * @return  <code>true</code> if support of GLSL shaders is available.
	 */
	public boolean isShaderSupported()
	{
		determineCapabilities();
		return _shaderObjects;
	}

	/**
	 * Returns whether GLSL shader objects are supported using the ARB
	 * extension, which was written for OpenGL version 1.4.
	 *
	 * @return  <code>true</code> if support of GLSL shaders is available.
	 */
	public boolean isShaderSupportedARB()
	{
		determineCapabilities();
		return _shaderObjectsARB;
	}

	/**
	 * Returns whether depth peeling, as implemented by {@link JOGLRenderer}, is
	 * supported. In addition to OpenGL 2.0 functionality (some of which is also
	 * supported using extensions on earlier OpenGL versions), extensions are
	 * required to perform off-screen rendering.
	 *
	 * @return  <code>true</code> if the functionality required for
	 *          depth-peeling is available.
	 */
	public boolean isDepthPeelingSupported()
	{
		return isShaderSupported() &&
		       _depthTexture       &&
		       _shadow             &&
		       _shadowFuncs        &&
		       _occlusionQuery     &&
		       _multitexture       &&
		       _textureRectangle   &&
		       _framebufferObject;
	}

	/**
	 * Returns whether {@link JOGLRenderer} can load, compile and link the
	 * shaders it requires. This may fail due to driver bugs or unknown
	 * programming errors.
	 *
	 * @return  <code>true</code> if the shaders are working correctly.
	 */
	public boolean isJOGLRendererShadersSupported()
	{
		final JOGLRendererShadersProbe probe = new JOGLRendererShadersProbe();
		probe.invokeAndWait();
		return probe._result;
	}

	/**
	 * Prints a summary of OpenGL information and capabilities to the given
	 * output stream.
	 *
	 * @param   out     Stream to write to.
	 */
	public void printSummary( final PrintStream out )
	{
		determineCapabilities();

		out.println();
		out.println( " About OpenGL:" );
		out.println( "---------------" );
		out.println( "Version:    " + _version );
		out.println( "Vendor:     " + _vendor );
		out.println( "Extensions: " + _extensions );
		out.println( "Renderer:   " + _renderer );
		out.println( "Shaders:    " + ( ( _shadingLanguageVersion == null ) ? "none" : _shadingLanguageVersion ) );
		out.println();

		out.println( " OpenGL capabilities:" );
		out.println( "----------------------" );
		out.println( "shaderObjects     = " + ( _shaderObjects     ? "yes (core)" : _shaderObjectsARB  ? "yes (ARB)" : "no" ) );
		out.println( "framebufferObject = " + ( _framebufferObject ? "yes"        : "no" ) );
		out.println( "drawBuffers       = " + ( _drawBuffers       ? "yes (core)" : _drawBuffersARB    ? "yes (ARB)" : "no" ) );
		out.println( "occlusionQuery    = " + ( _occlusionQuery    ? "yes (core)" : _occlusionQueryARB ? "yes (ARB)" : "no" ) );
		out.println( "shadowFuncs       = " + ( _shadowFuncs       ? "yes"        : "no" ) );
		out.println( "depthTexture      = " + ( _depthTexture      ? "yes"        : "no" ) );
		out.println( "shadow            = " + ( _shadow            ? "yes"        : "no" ) );
		out.println( "textureRectangle  = " + ( _textureRectangle  ? "yes"        : "no" ) );
		out.println( "multitexture      = " + ( _multitexture      ? "yes"        : "no" ) );
		out.println( "generateMipmap    = " + ( _generateMipmap    ? "yes"        : "no" ) );
		out.println( "blendFuncSeperate = " + ( _blendFuncSeperate ? "yes"        : "no" ) );
		out.println( "edgeClamp         = " + ( _edgeClamp         ? "yes"        : "no" ) );
	}

	/**
	 * Check the OpenGL context for various capabilities.
	 */
	private class CapabilitiesProbe
		extends Probe
	{
		protected void run( final GL gl )
		{
			final boolean opengl12 = gl.isExtensionAvailable( "GL_VERSION_1_2" );
			final boolean opengl13 = gl.isExtensionAvailable( "GL_VERSION_1_3" );
			final boolean opengl14 = gl.isExtensionAvailable( "GL_VERSION_1_4" );
			final boolean opengl15 = gl.isExtensionAvailable( "GL_VERSION_1_5" );
			final boolean opengl20 = gl.isExtensionAvailable( "GL_VERSION_2_0" );

			_textureRectangle  = gl.isExtensionAvailable( "GL_ARB_texture_rectangle" );
			_framebufferObject = gl.isExtensionAvailable( "GL_EXT_framebuffer_object" );

			_shaderObjects    = opengl20;
			_shaderObjectsARB = gl.isExtensionAvailable( "GL_ARB_vertex_shader" ) &&
			                    gl.isExtensionAvailable( "GL_ARB_fragment_shader" ) &&
			                    gl.isExtensionAvailable( "GL_ARB_shader_objects" );

			_drawBuffers       = opengl20;
			_drawBuffersARB    = gl.isExtensionAvailable( "GL_ARB_draw_buffers" );

			// NOTE: For shaders written in low-level assembly langauge, the
			// following extensions would be needed: (or OpenGL 2+)
			//  - GL_ARB_fragment_program
			//  - GL_ARB_vertex_program

			_occlusionQuery    = opengl15;
			_occlusionQueryARB = gl.isExtensionAvailable( "GL_ARB_occlusion_query" );
			_shadowFuncs       = opengl15 || gl.isExtensionAvailable( "GL_EXT_shadow_funcs" );
			_depthTexture      = opengl14 || gl.isExtensionAvailable( "GL_ARB_depth_texture" );
			_shadow            = opengl14 || gl.isExtensionAvailable( "GL_ARB_shadow" );
			_blendFuncSeperate = opengl14 || gl.isExtensionAvailable( "GL_EXT_blend_func_separate" );
			_generateMipmap    = opengl14 || gl.isExtensionAvailable( "GL_SGIS_generate_mipmap" );

			_multitexture      = opengl13 || gl.isExtensionAvailable( "GL_ARB_multitexture" );

			/*
			 * NOTE: The extension is specified as 'GL_SGIS_texture_edge_clamp',
			 *       but Nvidia uses 'GL_EXT_texture_edge_clamp'.
			 */
			_edgeClamp         = opengl12 || gl.isExtensionAvailable( "GL_SGIS_texture_edge_clamp" )
			                              || gl.isExtensionAvailable( "GL_EXT_texture_edge_clamp" );

			final int[] colorBits = new int[ 4 ];
			gl.glGetIntegerv( GL.GL_RED_BITS   , colorBits , 0 );
			gl.glGetIntegerv( GL.GL_GREEN_BITS , colorBits , 1 );
			gl.glGetIntegerv( GL.GL_BLUE_BITS  , colorBits , 2 );
			gl.glGetIntegerv( GL.GL_ALPHA_BITS , colorBits , 3 );
			final int[] depthBits = new int[ 1 ];
			gl.glGetIntegerv( GL.GL_DEPTH_BITS , depthBits , 0 );
			final boolean atLeast8AlphaBits = ( colorBits[ 3 ] >= 8 );

			final boolean shaderObjects = _shaderObjects;

			// Limits the number of passes that could be combined using a
			// multi-layer depth-peeling algorithm.
			final int[] maxDrawBuffers = new int[ 1 ];
			if ( _drawBuffers )
			{
				gl.glGetIntegerv( GL.GL_MAX_DRAW_BUFFERS , maxDrawBuffers , 0 );
			}

			// Limits the complexity of shaders we can use.
			final int[] maxVaryingFloats = new int[ 1 ];
			if ( shaderObjects )
			{
				gl.glGetIntegerv( GL.GL_MAX_VARYING_FLOATS , maxVaryingFloats , 0 );
			}

			_version    = gl.glGetString( GL.GL_VERSION );
			_vendor     = gl.glGetString( GL.GL_VENDOR );
			_extensions = gl.glGetString( GL.GL_EXTENSIONS );
			_renderer   = gl.glGetString( GL.GL_RENDERER );
			try
			{
				_shadingLanguageVersion = gl.glGetString( GL.GL_SHADING_LANGUAGE_VERSION );
			}
			catch ( GLException e )
			{
				_shadingLanguageVersion = null;
			}
		}
	}

	/**
	 * Check the OpenGL context for various capabilities.
	 */
	private class JOGLRendererShadersProbe
		extends Probe
	{
		private boolean _result = false;

		protected void run( final GL gl )
		{
			if ( isShaderSupported() )
			{
				final JOGLRenderer renderer = new JOGLRenderer( JOGLCapabilities.this , _context.getGL() , new HashMap<String,Texture>() , Color.BLACK , false , Matrix3D.INIT , new Rectangle( 0 , 0 , 10 , 10 ) , 1 , false , 10 );
				_result = renderer.isShadersEnabled();
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
		 * Used to implement {@link #waitFor()}.
		 */
		private final Semaphore _semaphore = new Semaphore( 0 );

		/**
		 * Any uncaught throwable that occured during the {@link #run()} method.
		 * It's thrown
		 */
		private Throwable _throwable = null;

		/**
		 * This method is called on the OpenGL thread.
		 *
		 * @param   gl  OpenGL pipeline.
		 */
		protected abstract void run( final GL gl );

		public void run()
		{
			try
			{
				final GLContext context = _context;
				if ( context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT )
				{
					throw new GLException( "GLContext.makeCurrent failed" );
				}

				try
				{
					run( context.getGL() );
				}
				catch ( Throwable t )
				{
					_throwable = t;
				}
				finally
				{
					context.release();
				}
			}
			finally
			{
				_semaphore.release();
			}
		}

		/**
		 * Waits until the probe has completed determining which OpenGL
		 * capabilities are available.
		 *
		 * @throws  InterruptedException if the current thread is interrupted.
		 */
		public void waitFor()
			throws InterruptedException
		{
			_semaphore.acquire();

			final Throwable throwable = _throwable;
			if ( throwable != null )
			{
				if ( throwable instanceof RuntimeException )
				{
					throw (RuntimeException)throwable;
				}
				else if ( throwable instanceof Error )
				{
					throw (Error)throwable;
				}
				else
				{
					throw new RuntimeException( "Failed to determine JOGL capabilities." , throwable );
				}
			}
		}

		/**
		 * Runs the probe on the OpenGL thread. This method blocks until the
		 * probe is completed.
		 */
		public void invokeAndWait()
		{
			if ( Threading.isOpenGLThread() )
			{
				final GLContext context = _context;
				if ( context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT )
				{
					throw new GLException( "GLContext.makeCurrent failed" );
				}

				run( _context.getGL() );
			}
			else
			{
				Threading.invokeOnOpenGLThread( this );

				try
				{
					waitFor();
				}
				catch ( InterruptedException e )
				{
					/* Should not be interrupted. */
				}
			}
		}
	}
}
