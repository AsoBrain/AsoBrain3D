/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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

import java.io.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import ab.j3d.appearance.*;
import com.jogamp.opengl.util.texture.*;
import org.jetbrains.annotations.*;

/**
 * Provides a cube map texture, with support for loading texture data
 * asynchronously.
 *
 * @author G. Meinders
 */
public class CubeTextureProxy
	extends TextureProxy
{
	/**
	 * Cube map specification.
	 */
	private CubeMap _cubeMap;

	/** Texture data for the OpenGL -X side of the cube. */ private volatile TextureData _x1Data = null;
	/** Texture data for the OpenGL -Y side of the cube. */ private volatile TextureData _y1Data = null;
	/** Texture data for the OpenGL -Z side of the cube. */ private volatile TextureData _z1Data = null;
	/** Texture data for the OpenGL +X side of the cube. */ private volatile TextureData _x2Data = null;
	/** Texture data for the OpenGL +Y side of the cube. */ private volatile TextureData _y2Data = null;
	/** Texture data for the OpenGL +Z side of the cube. */ private volatile TextureData _z2Data = null;

	/**
	 * Constructs a new cube map texture from a single image.
	 *
	 * @param   cubeMap         Cube map.
	 * @param   textureCache    Texture cache.
	 */
	public CubeTextureProxy( @NotNull final CubeMap cubeMap, final TextureCache textureCache )
	{
		super( textureCache );
		_cubeMap = cubeMap;
	}

	@Override
	public TextureData call()
	throws IOException
	{
		final CubeMap cubeMap = _cubeMap;

		// NOTE: In OpenGL, the Y-axis is bottom/top and the Z-axis is front/rear.
		_x1Data = createTextureData( _textureCache.loadImage( cubeMap.getImageLeft() ) );
		_y1Data = createTextureData( _textureCache.loadImage( cubeMap.getImageBottom() ) );
		_z1Data = createTextureData( _textureCache.loadImage( cubeMap.getImageFront() ) );
		_x2Data = createTextureData( _textureCache.loadImage( cubeMap.getImageRight() ) );
		_y2Data = createTextureData( _textureCache.loadImage( cubeMap.getImageTop() ) );
		_z2Data = createTextureData( _textureCache.loadImage( cubeMap.getImageRear() ) );

		// Not applicable for a cube map.
		return null;
	}

	@Override
	public Texture getTexture()
	{
		Texture texture = _texture;

		if ( texture == null )
		{
			final TextureData x1Data = _x1Data;
			final TextureData y1Data = _y1Data;
			final TextureData z1Data = _z1Data;
			final TextureData x2Data = _x2Data;
			final TextureData y2Data = _y2Data;
			final TextureData z2Data = _z2Data;

			if ( ( x1Data != null ) && ( y1Data != null ) && ( z1Data != null ) &&
			     ( x2Data != null ) && ( y2Data != null ) && ( z2Data != null ) )
			{
				texture = TextureIO.newTexture( GL.GL_TEXTURE_CUBE_MAP );
				texture.bind( GLContext.getCurrentGL() );

				final GL gl = GLU.getCurrentGL();

				gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X , 0 , x1Data.getInternalFormat() , x1Data.getWidth() , x1Data.getHeight() , x1Data.getBorder() , x1Data.getPixelFormat() , x1Data.getPixelType() , x1Data.getBuffer() );
				gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y , 0 , y1Data.getInternalFormat() , y1Data.getWidth() , y1Data.getHeight() , y1Data.getBorder() , y1Data.getPixelFormat() , y1Data.getPixelType() , y1Data.getBuffer() );
				gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z , 0 , z1Data.getInternalFormat() , z1Data.getWidth() , z1Data.getHeight() , z1Data.getBorder() , z1Data.getPixelFormat() , z1Data.getPixelType() , z1Data.getBuffer() );
				gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X , 0 , x2Data.getInternalFormat() , x2Data.getWidth() , x2Data.getHeight() , x2Data.getBorder() , x2Data.getPixelFormat() , x2Data.getPixelType() , x2Data.getBuffer() );
				gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y , 0 , y2Data.getInternalFormat() , y2Data.getWidth() , y2Data.getHeight() , y2Data.getBorder() , y2Data.getPixelFormat() , y2Data.getPixelType() , y2Data.getBuffer() );
				gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z , 0 , z2Data.getInternalFormat() , z2Data.getWidth() , z2Data.getHeight() , z2Data.getBorder() , z2Data.getPixelFormat() , z2Data.getPixelType() , z2Data.getBuffer() );

				x1Data.flush();
				y1Data.flush();
				z1Data.flush();
				x2Data.flush();
				y2Data.flush();
				z2Data.flush();

				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL2ES1.GL_GENERATE_MIPMAP, GL.GL_FALSE );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_S     , GL.GL_CLAMP_TO_EDGE );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_T     , GL.GL_CLAMP_TO_EDGE );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL2ES2.GL_TEXTURE_WRAP_R , GL.GL_CLAMP_TO_EDGE );

				_texture = texture;
			}
		}

		return texture;
	}

	@Override
	public String toString()
	{
		return super.toString() + "[cubeMap=" + _cubeMap + "]";
	}
}
