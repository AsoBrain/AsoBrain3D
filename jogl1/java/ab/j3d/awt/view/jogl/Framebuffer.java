/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
 * @version $Revision$ $Date$
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
		gl.glGenFramebuffersEXT( framebuffers.length, framebuffers, 0 );
		_framebuffer = framebuffers[ 0 ];
	}

	/**
	 * Binds the framebuffer. The framebuffer is bound for both reading and
	 * drawing.
	 */
	public void bind()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindFramebufferEXT( GL.GL_FRAMEBUFFER_EXT, _framebuffer );
	}

	/**
	 * Binds the framebuffer for reading only. Another framebuffer may be
	 * bound for drawing at the same time.
	 */
	public void bindReadOnly()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindFramebufferEXT( GL.GL_READ_FRAMEBUFFER_EXT, _framebuffer );
	}

	/**
	 * Binds the framebuffer for drawing only. Another framebuffer may be
	 * bound for reading at the same time.
	 */
	public void bindDrawOnly()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindFramebufferEXT( GL.GL_DRAW_FRAMEBUFFER_EXT, _framebuffer );
	}

	/**
	 * Unbinds the framebuffer object. This effectively activates the system
	 * framebuffer.
	 */
	public static void unbind()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindFramebufferEXT( GL.GL_FRAMEBUFFER_EXT, 0 );
	}

	/**
	 * Disables color attachments for the framebuffer.
	 */
	public void disableColorAttachments()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glDrawBuffer( GL.GL_NONE );
		gl.glReadBuffer( GL.GL_NONE );
	}

	/**
	 * Deletes the framebuffer object.
	 */
	public void delete()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glDeleteFramebuffersEXT( 1, new int[] { _framebuffer }, 0 );
	}

	/**
	 * Checks the state of the framebuffer object.
	 *
	 * @throws  GLException if the framebuffer state indicates an error.
	 */
	public void check()
	{
		final GL gl = GLU.getCurrentGL();

		final int status = gl.glCheckFramebufferStatusEXT( GL.GL_FRAMEBUFFER_EXT );
		switch ( status )
		{
			case GL.GL_FRAMEBUFFER_COMPLETE_EXT:
				break;
			case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
				throw new GLException( "Framebuffer incomplete: attachment" );
			case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
				throw new GLException( "Framebuffer incomplete: missing attachment" );
			case GL.GL_FRAMEBUFFER_INCOMPLETE_DUPLICATE_ATTACHMENT_EXT:
				throw new GLException( "Framebuffer incomplete: duplicate attachment" );
			case GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
				throw new GLException( "Framebuffer incomplete: dimensions" );
			case GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
				throw new GLException( "Framebuffer incomplete: formats" );
			case GL.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
				throw new GLException( "Framebuffer incomplete: draw buffer" );
			case GL.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
				throw new GLException( "Framebuffer incomplete: read buffer" );
			case GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT:
				throw new GLException( "Framebuffer unsupported" );
			default:
				throw new GLException( "Unknown framebuffer status: " + status );
		}
	}
}
