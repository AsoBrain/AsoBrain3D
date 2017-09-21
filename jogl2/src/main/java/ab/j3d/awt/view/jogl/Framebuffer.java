/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

/**
 * Provides a means for drawing to rendering destinations other than the buffers
 * provided to the GL by the window-system.
 *
 * @see     <a href="http://www.opengl.org/registry/specs/EXT/framebuffer_object.txt">EXT_framebuffer_object</a>
 * @see     <a href="http://www.opengl.org/registry/specs/EXT/framebuffer_blit.txt">EXT_framebuffer_blit</a>
 *
 * @author G. Meinders
 */
public class Framebuffer
{
	/**
	 * Identifies the framebuffer object in OpenGL.
	 */
	private final int _framebuffer;

	/**
	 * Construct new Framebuffer.
	 */
	public Framebuffer()
	{
		final GL gl = GLU.getCurrentGL();

		final int[] framebuffers = new int[ 1 ];
		gl.glGenFramebuffers( framebuffers.length, framebuffers, 0 );
		_framebuffer = framebuffers[ 0 ];
	}

	/**
	 * Binds the framebuffer. The framebuffer is bound for both reading and
	 * drawing.
	 */
	public void bind()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindFramebuffer( GL.GL_FRAMEBUFFER, _framebuffer );
	}

	/**
	 * Binds the framebuffer for reading only. Another framebuffer may be
	 * bound for drawing at the same time.
	 */
	public void bindReadOnly()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindFramebuffer( GL2GL3.GL_READ_FRAMEBUFFER, _framebuffer );
	}

	/**
	 * Binds the framebuffer for drawing only. Another framebuffer may be
	 * bound for reading at the same time.
	 */
	public void bindDrawOnly()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindFramebuffer( GL2GL3.GL_DRAW_FRAMEBUFFER, _framebuffer );
	}

	/**
	 * Unbinds the framebuffer object. This effectively activates the system
	 * framebuffer.
	 */
	public static void unbind()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindFramebuffer( GL.GL_FRAMEBUFFER, 0 );
	}

	/**
	 * Disables color attachments for the framebuffer.
	 */
	public void disableColorAttachments()
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();
		gl2.glDrawBuffer( GL.GL_NONE );
		gl2.glReadBuffer( GL.GL_NONE );
	}

	/**
	 * Deletes the framebuffer object.
	 */
	public void delete()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glDeleteFramebuffers( 1, new int[] { _framebuffer }, 0 );
	}

	/**
	 * Checks the state of the framebuffer object.
	 *
	 * @throws  GLException if the framebuffer state indicates an error.
	 */
	public void check()
	{
		final GL gl = GLU.getCurrentGL();

		final int status = gl.glCheckFramebufferStatus( GL.GL_FRAMEBUFFER );
		switch ( status )
		{
			case GL.GL_FRAMEBUFFER_COMPLETE:
				break;
			case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
				throw new GLException( "Framebuffer incomplete: attachment" );
			case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
				throw new GLException( "Framebuffer incomplete: missing attachment" );
			case GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
				throw new GLException( "Framebuffer incomplete: dimensions" );
			case GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
				throw new GLException( "Framebuffer incomplete: formats" );
			case GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
				throw new GLException( "Framebuffer incomplete: draw buffer" );
			case GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
				throw new GLException( "Framebuffer incomplete: read buffer" );
			case GL.GL_FRAMEBUFFER_UNSUPPORTED:
				throw new GLException( "Framebuffer unsupported" );
			default:
				throw new GLException( "Unknown framebuffer status: " + status );
		}
	}
}
