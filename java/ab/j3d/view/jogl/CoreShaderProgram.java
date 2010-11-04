/* ====================================================================
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2010
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

import java.nio.charset.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Represents an OpenGL Shading Language (GLSL) shader program. This
 * implementation uses the core API, available in OpenGL 2.0 and above.
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

		_program = gl.glCreateProgram();
	}

	@Override
	public int getProgramObject()
	{
		return _program;
	}

	@Override
	public void attach( final Shader shader )
	{
		final GL gl = GLU.getCurrentGL();
		gl.glAttachShader( _program , shader.getShaderObject() );
		_linked = false;
	}

	@Override
	public void detach( final Shader shader )
	{
		final GL gl = GLU.getCurrentGL();
		gl.glDetachShader( _program , shader.getShaderObject() );
		_linked = false;
	}

	@Override
	public void link()
	{
		if ( !_linked )
		{
			final GL gl = GLU.getCurrentGL();
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
				throw new GLException( ( infoLog == null ) ? ( "'" + _name + "' shader(s) failed to link (sorry, no details available)." ) : ( "Linking of '" + _name + "' shader(s) failed: " + infoLog ) );
			}

			_linked = true;
		}
	}

	@Override
	public void validate()
	{
		final GL gl = GLU.getCurrentGL();
		final int program = _program;

		gl.glValidateProgram( program );

		final int[] validateStatus = new int[ 1 ];
		gl.glGetProgramiv( program , GL.GL_VALIDATE_STATUS , validateStatus , 0 );

		final String infoLog = getInfoLog();
		if ( validateStatus[ 0 ] == GL.GL_FALSE )
		{
			throw new GLException( ( infoLog == null ) ? ( "Validation of '" + _name + "' shader(s) failed (sorry, no details available)." ) : ( "Validation of '" + _name + "' shader(s) failed: " + infoLog ) );
		}
	}

	@Nullable
	@Override
	public String getInfoLog()
	{
		final GL gl = GLU.getCurrentGL();
		final int[] infoLogLength = new int[ 1 ];
		gl.glGetProgramiv( _program, GL.GL_INFO_LOG_LENGTH , infoLogLength , 0 );

		final String infoLog;
		if( infoLogLength[ 0 ] == 0 )
		{
			infoLog = null;
		}
		else
		{
			final byte[] infoLogBytes = new byte[ infoLogLength[ 0 ] ];
			gl.glGetProgramInfoLog( _program, infoLogLength[ 0 ] , infoLogLength , 0 , infoLogBytes , 0 );
			infoLog = new String( infoLogBytes , 0 , infoLogLength[ 0 ] , Charset.forName( "iso-8859-1" ) );
		}
		return infoLog;
	}

	@Override
	public void enable()
	{
		final GL gl = GLU.getCurrentGL();
		link();
		gl.glEnable( GL.GL_VERTEX_PROGRAM_TWO_SIDE );
		gl.glUseProgram( _program );
	}

	@Override
	public void disable()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glUseProgram( 0 );
		gl.glDisable( GL.GL_VERTEX_PROGRAM_TWO_SIDE );
	}

	@Override
	public void dispose()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glDeleteProgram( _program );
	}

	@Override
	public void setUniform( final String identifier , final float value )
	{
		final GL gl = GLU.getCurrentGL();
		final int variable = gl.glGetUniformLocation( _program , identifier );
		if ( variable != -1 )
		{
			gl.glUniform1f( variable , value );
		}
	}

	@Override
	public void setUniform( final String identifier , final int value )
	{
		final GL gl = GLU.getCurrentGL();
		final int variable = gl.glGetUniformLocation( _program , identifier );
		if ( variable != -1 )
		{
			gl.glUniform1i( variable , value );
		}
	}

	@Override
	public void setUniform( final String identifier , final boolean value )
	{
		setUniform( identifier, value ? GL.GL_TRUE : GL.GL_FALSE );
	}

	@Override
	public void setUniform( final String identifier , final Vector3D value )
	{
		final GL gl = GLU.getCurrentGL();
		final int variable = gl.glGetUniformLocation( _program , identifier );
		if ( variable != -1 )
		{
			gl.glUniform3f( variable , (float)value.x , (float)value.y , (float)value.z );
		}
	}
}
