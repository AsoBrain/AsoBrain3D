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
public class ShaderProgram
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
	public ShaderProgram( final String name )
	{
		_name    = name;
		_linked  = false;

		final GL gl = GLU.getCurrentGL();
		_gl = gl;

		_program = gl.glCreateProgram();
	}

	/**
	 * Returns the underlying program object.
	 *
	 * @return  Program object.
	 */
	public int getProgramObject()
	{
		return _program;
	}

	/**
	 * Attaches the given shader to the program. The shader will be included
	 * when the program is next linked.
	 *
	 * @param   shader  Shader to be attached.
	 */
	public void attach( final Shader shader )
	{
		_gl.glAttachShader( _program , shader.getShaderObject() );
		_linked = false;
	}

	/**
	 * Detaches the given shader from the program. The shader will no longer be
	 * included when the program is next linked.
	 *
	 * @param   shader  Shader to be detached.
	 */
	public void detach( final Shader shader )
	{
		_gl.glAttachShader( _program , shader.getShaderObject() );
		_linked = false;
	}

	/**
	 * Links the currently attached shaders into an executable program.
	 *
	 * @throws  GLException if the shaders fail to link.
	 */
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

	/**
	 * Validates the program.
	 *
	 * @throws  GLException if the program fails to validate.
	 */
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

	/**
	 * Returns the program information log. The log may contain helpful
	 * information about problems encountered during compilation or validation.
	 *
	 * @return  Program information log.
	 */
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

	/**
	 * Activates the shader program, replacing OpenGL's fixed functionality.
	 * Note that at most one shader program can be active at any given time.
	 * If another shader program is currently active, it's implicitly
	 * deactivated.
	 *
	 * <p>
	 * If necessary, the program is automatically linked.
	 */
	public void enable()
	{
		link();
		_gl.glUseProgram( _program );
	}

	/**
	 * Deactivates the shader program, reverting back to OpenGL's fixed
	 * functionality.
	 */
	public void disable()
	{
		_gl.glUseProgram( 0 );
	}

	/**
	 * Deletes the resources used by the shader program object. The shader
	 * objects that the program consists of remain unaffected.
	 */
	public void dispose()
	{
		_gl.glDeleteProgram( _program );
	}

	/**
	 * Sets the uniform variable identified by the given name.
	 *
	 * @param   identifier  Name of the variable.
	 * @param   value       Value to be set.
	 *
	 * @throws  IllegalArgumentException if there is no such variable.
	 */
	public void setUniform( final String identifier , final float value )
	{
		final int variable = _gl.glGetUniformLocation( _program , identifier );
		if ( variable == -1 )
		{
			throw new IllegalArgumentException( "Not a uniform variable: " + identifier );
		}
		_gl.glUniform1f( variable , value );
	}

	/**
	 * Sets the uniform variable identified by the given name.
	 *
	 * @param   identifier  Name of the variable.
	 * @param   value       Value to be set.
	 *
	 * @throws  IllegalArgumentException if there is no such variable.
	 */
	public void setUniform( final String identifier , final int value )
	{
		final int variable = _gl.glGetUniformLocation( _program , identifier );
		if ( variable == -1 )
		{
			throw new IllegalArgumentException( "Not a uniform variable: " + identifier );
		}
		_gl.glUniform1i( variable , value );
	}

	/**
	 * Sets the uniform variable identified by the given name.
	 *
	 * @param   identifier  Name of the variable.
	 * @param   value       Value to be set.
	 *
	 * @throws  IllegalArgumentException if there is no such variable.
	 */
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
