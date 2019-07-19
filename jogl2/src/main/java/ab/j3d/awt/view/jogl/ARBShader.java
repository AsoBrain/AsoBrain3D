/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2019 Peter S. Heijnen
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

import java.nio.charset.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;

/**
 * Represents an OpenGL Shading Language (GLSL) shader object. This
 * implementation uses the ARB extension for GLSL. As such, it may be supported
 * on OpenGL versions 1.4 and above.
 *
 * @author G. Meinders
 */
public class ARBShader
implements Shader
{
	/**
	 * Shader type.
	 */
	private final Type _shaderType;

	/**
	 * Shader source.
	 */
	private String[] _source = null;

	/**
	 * Shader object.
	 */
	private final long _shader;

	/**
	 * Constructs a new shader of the given type.
	 *
	 * @param shaderType Type of shader.
	 */
	public ARBShader( final Type shaderType )
	{
		if ( shaderType == null )
		{
			throw new NullPointerException( "shaderType" );
		}
		_shaderType = shaderType;

		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();

		final long shader;

		switch ( shaderType )
		{
			case VERTEX:
				shader = gl2.glCreateShaderObjectARB( GL2.GL_VERTEX_SHADER );
				break;

			case FRAGMENT:
				shader = gl2.glCreateShaderObjectARB( GL2.GL_FRAGMENT_SHADER );
				break;

			default:
				throw new IllegalArgumentException( "Unknown shader type: " + shaderType );
		}

		_shader = shader;
	}

	public long getShaderObject()
	{
		return _shader;
	}

	@Override
	public void setSource( final String... source )
	{
		final int[] length = new int[ source.length ];
		for ( int i = 0; i < source.length; i++ )
		{
			length[ i ] = source[ i ].length();
		}

		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();

		final long shader = _shader;
		gl2.glShaderSourceARB( shader, source.length, source, length, 0 );
		_source = source.clone();

		compile();
	}

	/**
	 * Compiles the shader.
	 *
	 * @throws GLException if compilation fails.
	 */
	private void compile()
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		final long shader = _shader;

		gl2.glCompileShaderARB( shader );

		final int[] compileStatus = new int[ 1 ];
		gl2.glGetObjectParameterivARB( shader, GL2.GL_OBJECT_COMPILE_STATUS_ARB, compileStatus, 0 );

		if ( compileStatus[ 0 ] == GL.GL_FALSE )
		{
			final int[] infoLogLength = new int[ 1 ];
			gl2.glGetObjectParameterivARB( shader, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, infoLogLength, 0 );

			final StringBuilder message = new StringBuilder();

			if ( infoLogLength[ 0 ] == 0 )
			{
				message.append( _shaderType );
				message.append( " shader failed to compile." );
			}
			else
			{
				final byte[] infoLog = new byte[ infoLogLength[ 0 ] ];
				gl2.glGetInfoLogARB( shader, infoLogLength[ 0 ], infoLogLength, 0, infoLog, 0 );
				message.append( new String( infoLog, 0, infoLogLength[ 0 ], Charset.forName( "iso-8859-1" ) ) );
			}

			final String[] source = _source;
			if ( ( source != null ) && ( source.length > 0 ) )
			{
				message.append( "\n---[ Shader program ]----------------------------\n" );
				for ( final String text : source )
				{
					message.append( text );
					message.append( '\n' );
				}
				message.append( "-------------------------------------------------" );
			}

			throw new GLException( message.toString() );
		}
	}

	@Override
	public void dispose()
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		gl2.glDeleteObjectARB( _shader );
	}
}
