/*
 * (C) Copyright Numdata BV 2019-2019 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.awt.view.jogl;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;

/**
 * FIXME Need comment.
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
		System.out.println( "Renderbuffer.storage() bind " + _renderbuffer );
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
		System.out.println( "Renderbuffer.delete() " + _renderbuffer );
		gl.glDeleteRenderbuffers( 1, new int[] { _renderbuffer }, 0 );
	}
}
