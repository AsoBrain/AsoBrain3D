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

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;

/**
 * Wrapper for an OpenGL renderbuffer object.
 *
 * @author Gerrit Meinders
 */
public class Renderbuffer
{
	private final int _renderbuffer;

	/**
	 * Constructs a new instance.
	 */
	public Renderbuffer()
	{
		final GL gl = GLU.getCurrentGL();
		final int[] renderbuffers = new int[ 1 ];
		gl.glGenRenderbuffers( 1, renderbuffers, 0 );
		_renderbuffer = renderbuffers[ 0 ];
	}

	public int getRenderbuffer()
	{
		return _renderbuffer;
	}

	public void bind()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindRenderbuffer( GL.GL_RENDERBUFFER, _renderbuffer );
	}

	public int getWidth()
	{
		final GL gl = GLU.getCurrentGL();
		final int[] size = new int[ 1 ];
		gl.glGetRenderbufferParameteriv( GL.GL_RENDERBUFFER, GL.GL_RENDERBUFFER_WIDTH, size, 0 );
		return size[ 0 ];
	}

	public int getHeight()
	{
		final GL gl = GLU.getCurrentGL();
		final int[] size = new int[ 1 ];
		gl.glGetRenderbufferParameteriv( GL.GL_RENDERBUFFER, GL.GL_RENDERBUFFER_HEIGHT, size, 0 );
		return size[ 0 ];
	}

	public void storage( final int width, final int height )
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindRenderbuffer( GL.GL_RENDERBUFFER, _renderbuffer );

		final int[] size = new int[ 2 ];
		gl.glGetRenderbufferParameteriv( GL.GL_RENDERBUFFER, GL.GL_RENDERBUFFER_WIDTH, size, 0 );
		gl.glGetRenderbufferParameteriv( GL.GL_RENDERBUFFER, GL.GL_RENDERBUFFER_HEIGHT, size, 1 );

		if ( ( size[ 0 ] != width ) || ( size[ 1 ] != height ) )
		{
			gl.glRenderbufferStorage( GL.GL_RENDERBUFFER, GL2ES2.GL_DEPTH_COMPONENT, width, height );
		}
	}

	public void delete()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glDeleteRenderbuffers( 1, new int[] { _renderbuffer }, 0 );
	}
}
