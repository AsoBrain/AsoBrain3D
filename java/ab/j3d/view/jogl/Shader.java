/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2009-2009 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view.jogl;

import java.nio.charset.Charset;
import javax.media.opengl.GL;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

/**
 * Represents an OpenGL Shading Language (GLSL) shader object.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class Shader
{
	/**
	 * OpenGL pipeline.
	 */
	protected final GL _gl;

	/**
	 * Shader object.
	 */
	protected final int _shader;

	/**
	 * Constructs a new shader of the given type.
	 *
	 * @param   shaderType  Type of shader; either {@link GL#GL_VERTEX_SHADER}
	 *                      or {@link GL#GL_FRAGMENT_SHADER}.
	 */
	protected Shader( final int shaderType )
	{
		final GL gl = GLU.getCurrentGL();
		_gl = gl;

		_shader = gl.glCreateShader( shaderType );
	}

	/**
	 * Returns the underlying shader object.
	 *
	 * @return  Shader object.
	 */
	public int getShaderObject()
	{
		return _shader;
	}

	/**
	 * Sets the source code of the shader, replacing any previously set code.
	 *
	 * @param   source  Source code of the shader.
	 */
	public void setSource( final String... source )
	{
		final int[] length = new int[ source.length ];
		for ( int i = 0 ; i < source.length ; i++ )
		{
			length[ i ] = source[ i ].length();
		}

		final GL gl = GLU.getCurrentGL();

		final int shader = _shader;
		gl.glShaderSource( shader, source.length , source , length , 0 );

		compile();
	}

	/**
	 * Compiles the shader.
	 */
	private void compile()
	{
		final GL gl = GLU.getCurrentGL();
		final int shader = _shader;

		gl.glCompileShader( shader );

		final int[] compileStatus = new int[ 1 ];
		gl.glGetShaderiv( shader , GL.GL_COMPILE_STATUS , compileStatus , 0 );

		if ( compileStatus[ 0 ] == GL.GL_FALSE )
		{
			final int[] infoLogLength = new int[ 1 ];
			gl.glGetShaderiv( shader , GL.GL_INFO_LOG_LENGTH , infoLogLength , 0 );

			final String message;
			if( infoLogLength[ 0 ] == 0 )
			{
				message = "Vertex shader failed to compile.";
			}
			else
			{
				final byte[] infoLog = new byte[ infoLogLength[ 0 ] ];
				gl.glGetShaderInfoLog( shader , infoLogLength[ 0 ] , infoLogLength , 0 , infoLog , 0 );
				message = new String( infoLog , 0 , infoLogLength[ 0 ] , Charset.forName( "iso-8859-1" ) );
			}

			throw new GLException( message );
		}
	}

	/**
	 * Deletes the resources used by the shader object.
	 */
	public void dispose()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glDeleteShader( _shader );
	}
}
