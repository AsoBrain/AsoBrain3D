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

/**
 * Wrapper for an OpenGL texture object.
 *
 * @author Gerrit Meinders
 */
class TextureObject
{
	/**
	 * OpenGL texture object.
	 */
	private final int _texture;

	/**
	 * Constructs a new instance.
	 */
	public TextureObject()
	{
		final GL gl = GLContext.getCurrentGL();
		final int[] textures = new int[ 1 ];
		gl.glGenTextures( textures.length, textures, 0 );
		_texture = textures[ 0 ];
	}

	public int getTexture()
	{
		return _texture;
	}

	/**
	 * Binds the texture.
	 */
	public void bind()
	{
		final GL gl = GLContext.getCurrentGL();
		gl.glBindTexture( GL.GL_TEXTURE_2D, _texture );
	}

	/**
	 * Deletes the texture.
	 */
	public void delete()
	{
		final GL gl = GLContext.getCurrentGL();
		final int[] textures = { _texture };
		gl.glDeleteTextures( textures.length, textures, 0 );
	}
}
