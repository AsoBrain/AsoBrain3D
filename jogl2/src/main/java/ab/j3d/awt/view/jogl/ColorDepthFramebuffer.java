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
import org.jetbrains.annotations.*;

/**
 * Helper class for managing a framebuffer with color and depth attachments.
 * No OpenGL objects are created until the object is actually used.
 *
 * @author Gerrit Meinders
 */
public class ColorDepthFramebuffer
{
	/**
	 * Current width.
	 */
	private int _width = 0;

	/**
	 * Current height.
	 */
	private int _height = 0;

	/**
	 * Framebuffer.
	 */
	@Nullable
	private Framebuffer _framebuffer = null;

	/**
	 * Color attachment for the framebuffer.
	 */
	@Nullable
	private TextureObject _colorTexture = null;

	/**
	 * Depth attachment for the framebuffer.
	 */
	@Nullable
	private Renderbuffer _depthBuffer = null;

	/**
	 * Returns the framebuffer.
	 *
	 * @return Framebuffer.
	 */
	@NotNull
	private Framebuffer getFramebuffer()
	{
		Framebuffer result = _framebuffer;
		if ( result == null )
		{
			result = new Framebuffer();
			_framebuffer = result;
		}
		return result;
	}

	/**
	 * Returns the color texture attached to the framebuffer, if it exists.
	 *
	 * @return Color texture.
	 */
	@Nullable
	public TextureObject getColorTexture()
	{
		return _colorTexture;
	}

	/**
	 * Creates a new color texture for the framebuffer. The existing color
	 * texture is deleted.
	 *
	 * @return Created color texture.
	 */
	private TextureObject createColorTexture()
	{
		deleteColorTexture();

		final int width = _width;
		final int height = _height;

		if ( ( width == 0 ) && ( height == 0 ) )
		{
			throw new IllegalStateException( "Framebuffer size is not specified. Call update(..) first!" );
		}

		final TextureObject colorTexture = new TextureObject();
		_colorTexture = colorTexture;

		final GL gl = GLContext.getCurrentGL();
		colorTexture.bind();
		gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_INT, null );
		gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
		gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
		gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE );
		return colorTexture;
	}

	/**
	 * Returns the depth buffer attached to the framebuffer.
	 *
	 * @return Depth buffer.
	 */
	@NotNull
	private Renderbuffer getDepthBuffer()
	{
		Renderbuffer result = _depthBuffer;
		if ( result == null )
		{
			result = new Renderbuffer();
			_depthBuffer = result;
		}
		return result;
	}

	/**
	 * Initializes/updates the framebuffer and its attachments.
	 *
	 * @param width  Width of the framebuffer.
	 * @param height Height of the framebuffer.
	 */
	public void update( final int width, final int height )
	{
		if ( width <= 0 || height <= 0 )
		{
			throw new IllegalArgumentException( "width and height must be positive" );
		}

		if ( ( width != _width ) || ( height != _height ) )
		{
			_width = width;
			_height = height;

			final GL gl = GLContext.getCurrentGL();

			final TextureObject colorTexture = createColorTexture();

			final Framebuffer framebuffer = getFramebuffer();
			framebuffer.bind();
			gl.glFramebufferTexture2D( GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, colorTexture.getTexture(), 0 );

			final Renderbuffer depthBuffer = getDepthBuffer();
			depthBuffer.storage( width, height );
			gl.glFramebufferRenderbuffer( GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, depthBuffer.getRenderbuffer() );
			framebuffer.check();
		}
	}

	/**
	 * Binds the framebuffer.
	 */
	public void bind()
	{
		if ( _framebuffer == null )
		{
			throw new IllegalStateException( "Framebuffer is not initialized. Call update(..) first!" );
		}
		_framebuffer.bind();
	}

	/**
	 * Deletes the framebuffer and its attachments.
	 */
	public void delete()
	{
		deleteFramebuffer();
		deleteColorTexture();
		deleteDepthBuffer();
	}

	/**
	 * Deletes the framebuffer.
	 */
	private void deleteFramebuffer()
	{
		if ( _framebuffer != null )
		{
			_framebuffer.delete();
			_framebuffer = null;
		}
	}

	/**
	 * Deletes the color texture.
	 */
	private void deleteColorTexture()
	{
		if ( _colorTexture != null )
		{
			_colorTexture.delete();
			_colorTexture = null;
		}
	}

	/**
	 * Deletes the depth buffer.
	 */
	private void deleteDepthBuffer()
	{
		if ( _depthBuffer != null )
		{
			_depthBuffer.delete();
			_depthBuffer = null;
		}
	}
}
