/* ====================================================================
 * $Id$
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

import java.nio.charset.Charset;
import javax.media.opengl.GL;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import ab.j3d.Vector3D;

/**
 * Represents an OpenGL Shading Language (GLSL) shader program.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class CoreShaderProgram
	implements ShaderProgram
{
	/**
	 * Name of the program, intended for traceability; not used by OpenGL.
	 */
	private final String _name;

	/**
	 * OpenGL pipeline.
	 */
	private final GL _gl;

	/**
	 * Shader program object.
	 */
	private final int _program;

	/**
	 * Whether the program is linked.
	 */
	private boolean _linked;

	/**
	 * Constructs a new shader program.
	 *
	 * @param   name    Name for the program, for traceability.
	 */
	public CoreShaderProgram( final String name )
	{
		_name    = name;
		_linked  = false;

		final GL gl = GLU.getCurrentGL();
		_gl = gl;

		_program = gl.glCreateProgram();
	}

	public int getProgramObject()
	{
		return _program;
	}

	public void attach( final Shader shader )
	{
		_gl.glAttachShader( _program , shader.getShaderObject() );
		_linked = false;
	}

	public void detach( final Shader shader )
	{
		_gl.glDetachShader( _program , shader.getShaderObject() );
		_linked = false;
	}

	public void link()
	{
		if ( !_linked )
		{
			final GL gl = _gl;
			final int program = _program;

			/*
			 * Link the program.
			 */
			gl.glLinkProgram( program );

			final int[] linkStatus = new int[ 1 ];
			gl.glGetProgramiv( program , GL.GL_LINK_STATUS , linkStatus , 0 );

			final String infoLog = getInfoLog();
			if ( linkStatus[ 0 ] == GL.GL_FALSE )
			{
				throw new GLException( _name + ": " + ( ( infoLog == null ) ? "Shader(s) failed to link." : infoLog ) );
			}

			_linked = true;
		}
	}

	public void validate()
	{
		final GL gl = _gl;
		final int program = _program;

		gl.glValidateProgram( program );

		final int[] validateStatus = new int[ 1 ];
		gl.glGetProgramiv( program , GL.GL_VALIDATE_STATUS , validateStatus , 0 );

		final String infoLog = getInfoLog();
		if ( validateStatus[ 0 ] == GL.GL_FALSE )
		{
			throw new GLException( _name + ": " + ( ( infoLog == null ) ? "Validation of shader(s) failed." : infoLog ) );
		}
	}

	public String getInfoLog()
	{
		final int[] infoLogLength = new int[ 1 ];
		_gl.glGetProgramiv( _program, GL.GL_INFO_LOG_LENGTH , infoLogLength , 0 );

		final String infoLog;
		if( infoLogLength[ 0 ] == 0 )
		{
			infoLog = null;
		}
		else
		{
			final byte[] infoLogBytes = new byte[ infoLogLength[ 0 ] ];
			_gl.glGetProgramInfoLog( _program, infoLogLength[ 0 ] , infoLogLength , 0 , infoLogBytes , 0 );
			infoLog = new String( infoLogBytes , 0 , infoLogLength[ 0 ] , Charset.forName( "iso-8859-1" ) );
		}
		return infoLog;
	}

	public void enable()
	{
		link();
		_gl.glEnable( GL.GL_VERTEX_PROGRAM_TWO_SIDE );
		_gl.glUseProgram( _program );
	}

	public void disable()
	{
		_gl.glUseProgram( 0 );
	}

	public void dispose()
	{
		_gl.glDeleteProgram( _program );
	}

	public void setUniform( final String identifier , final float value )
	{
		final int variable = _gl.glGetUniformLocation( _program , identifier );
		if ( variable == -1 )
		{
			throw new IllegalArgumentException( "Not a uniform variable: " + identifier );
		}
		_gl.glUniform1f( variable , value );
	}

	public void setUniform( final String identifier , final int value )
	{
		final int variable = _gl.glGetUniformLocation( _program , identifier );
		if ( variable == -1 )
		{
			throw new IllegalArgumentException( "Not a uniform variable: " + identifier );
		}
		_gl.glUniform1i( variable , value );
	}

	public void setUniform( final String identifier , final Vector3D value )
	{
		final int variable = _gl.glGetUniformLocation( _program , identifier );
		if ( variable == -1 )
		{
			throw new IllegalArgumentException( "Not a uniform variable: " + identifier );
		}
		_gl.glUniform3f( variable , (float)value.x , (float)value.y , (float)value.z );
	}
}
