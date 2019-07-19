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

import ab.j3d.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import org.jetbrains.annotations.*;

/**
 * Represents an OpenGL Shading Language (GLSL) shader program. This
 * implementation uses the ARB extension for GLSL. As such, it may be supported
 * on OpenGL versions 1.4 and above.
 *
 * @author  G. Meinders
 */
public class ARBShaderProgram
	implements ShaderProgram
{
	/**
	 * Name of the program, intended for traceability; not used by OpenGL.
	 */
	@Nullable
	private final String _name;

	/**
	 * Shader program object.
	 */
	private final long _program;

	/**
	 * Whether the program is linked.
	 */
	private boolean _linked;

	/**
	 * Constructs a new shader program.
	 *
	 * @param   name    Name for the program, for traceability.
	 */
	public ARBShaderProgram( @Nullable final String name )
	{
		_name = name;
		_linked = false;

		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();

		_program = gl2.glCreateProgramObjectARB();
	}

	/**
	 * Returns the underlying program object.
	 *
	 * @return  Program object.
	 */
	public long getProgramObject()
	{
		return _program;
	}

	@Override
	public void attach( final Shader shader )
	{
		if ( !( shader instanceof ARBShader ) )
		{
			throw new IllegalArgumentException( "Incompatible shader: " + shader );
		}
		final ARBShader arbShader = (ARBShader)shader;

		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		gl2.glAttachObjectARB( _program , arbShader.getShaderObject() );
		_linked = false;
	}

	@Override
	public void detach( final Shader shader )
	{
		if ( !( shader instanceof ARBShader ) )
		{
			throw new IllegalArgumentException( "Incompatible shader: " + shader );
		}
		final ARBShader arbShader = (ARBShader)shader;

		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		gl2.glDetachObjectARB( _program, arbShader.getShaderObject() );
		_linked = false;
	}

	@Override
	public void link()
	{
		if ( !_linked )
		{
			final GL gl = GLU.getCurrentGL();
			final GL2 gl2 = gl.getGL2();
			final long program = _program;

			/*
			 * Link the program.
			 */
			gl2.glLinkProgramARB( program );

			final int[] linkStatus = new int[ 1 ];
			gl2.glGetObjectParameterivARB( program, GL2.GL_OBJECT_LINK_STATUS_ARB, linkStatus, 0 );

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
		final GL2 gl2 = gl.getGL2();
		final long program = _program;

		gl2.glValidateProgramARB( program );

		final int[] validateStatus = new int[ 1 ];
		gl2.glGetObjectParameterivARB( program, GL2.GL_OBJECT_VALIDATE_STATUS_ARB, validateStatus, 0 );

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
		final GL2 gl2 = gl.getGL2();
		final int[] infoLogLength = new int[ 1 ];
		gl2.glGetObjectParameterivARB( _program, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, infoLogLength, 0 );

		final String infoLog;
		if( infoLogLength[ 0 ] == 0 )
		{
			infoLog = null;
		}
		else
		{
			final byte[] infoLogBytes = new byte[ infoLogLength[ 0 ] ];
			gl2.glGetInfoLogARB( _program, infoLogLength[ 0 ], infoLogLength, 0, infoLogBytes, 0 );
			infoLog = new String( infoLogBytes, 0, infoLogLength[ 0 ], Charset.forName( "iso-8859-1" ) );
		}
		return infoLog;
	}

	@Override
	public void enable()
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		link();
		gl2.glUseProgramObjectARB( _program );
	}

	@Override
	public void disable()
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		gl2.glUseProgramObjectARB( 0 );
	}

	@Override
	public void dispose()
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		gl2.glDeleteObjectARB( _program );
	}

	@Override
	public void setUniform( final String identifier, final float value )
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		final int variable = gl2.glGetUniformLocationARB( _program, identifier );
		if ( variable != -1 )
		{
			gl2.glUniform1fARB( variable, value );
		}
	}

	@Override
	public void setUniform( final String identifier, final int value )
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		final int variable = gl2.glGetUniformLocationARB( _program, identifier );
		if ( variable != -1 )
		{
			gl2.glUniform1iARB( variable, value );
		}
	}

	@Override
	public void setUniform( final String identifier, final boolean value )
	{
		setUniform( identifier, value ? GL.GL_TRUE : GL.GL_FALSE );
	}

	@Override
	public void setUniform( final String identifier, final Vector3D value )
	{
		setUniform( identifier, (float)value.x, (float)value.y, (float)value.z );
	}

	@Override
	public void setUniform( final String identifier, final float x, final float y, final float z )
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		final int variable = gl2.glGetUniformLocationARB( _program, identifier );
		if ( variable != -1 )
		{
			gl2.glUniform3fARB( variable, x, y, z );
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + "[" + _name + "]";
	}
}
