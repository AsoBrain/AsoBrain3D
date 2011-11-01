/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.view.jogl2;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLException;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

import ab.j3d.MapTools;
import ab.j3d.Material;


/**
 * Provides loading and caching of textures for JOGL-based rendering.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TextureCache
{
	/**
	 * Texture cache key for the normalization cube map, used for DOT3 bump
	 * mapping.
	 */
	public static final String NORMALIZATION_CUBE_MAP = "__normalizationCubeMap";

	/**
	 * Cached textures, by name.
	 */
	private final Map<String,Texture> _textures;

	/**
	 * Set of textures, by name, with an alpha channel.
	 */
	private final Set<String> _alpha;

	/**
	 * Specifies the orientation/layout of a cube map represented as a single
	 * texture. Constants are named after the side that the center square faces.
	 */
	private enum CubeMapOrientation
	{
		/**
		 * Rotated 90 degrees around X-axis, compared to {@link #POSITIVE_Z}.
		 * <pre>
		 *     +---+
		 *     | Z+|
		 * +---+---+---+---+
		 * | X-| Y-| X+| Y+|
		 * +---+---+---+---+
		 *     | Z-|
		 *     +---+
		 * </pre>
		 */
		NEGATIVE_Y ,

		/**
		 * Default orientation for OpenGL, meaning that no rotation of
		 * sub-images is required using this layout.
		 * <pre>
		 *     +---+
		 *     | Y+|
		 * +---+---+---+---+
		 * | X-| Z+| X+| Z-|
		 * +---+---+---+---+
		 *     | Y-|
		 *     +---+
		 * </pre>
		 */
		POSITIVE_Z
	}

	/**
	 * Construct new texture cache.
	 */
	public TextureCache()
	{
		_textures = new HashMap<String,Texture>();
		_alpha = new HashSet<String>();
	}

	/**
	 * Removes all cached textures from the cache.
	 */
	public void clear()
	{
		_textures.clear();
		_alpha.clear();
	}

	/**
	 * Returns whether the specified texture has an alpha channel.
	 *
	 * @param   texture     Name of the texture.
	 *
	 * @return  <code>true</code> if the texture has an alpha channel.
	 */
	public boolean hasAlpha( final String texture )
	{
		return _alpha.contains( texture );
	}

	/**
	 * Get {@link Texture} for the specified map.
	 *
	 * @param   gl          OpenGL pipeline.
	 * @param   texture     Name of the texture map.
	 *
	 * @return  Texture for the specified name; <code>null</code> if the name was
	 *          empty or no map by the given name was found.
	 */
	public Texture getTexture( final GL gl , final String texture )
	{
		Texture result = null;

		if ( TextTools.isNonEmpty( texture ) )
		{
			final Map<String, Texture> textures = _textures;
			result = textures.get( texture );

			if ( result == null && !textures.containsKey( texture ) )
			{
				final BufferedImage bufferedImage = MapTools.loadImage( texture );
				if ( bufferedImage != null )
				{
					final boolean autoMipmapGeneration = hasAutoMipMapGenerationSupport( gl );

					final TextureData textureData = createTextureData( createCompatibleTextureImage( bufferedImage , gl ) );
					result = TextureIO.newTexture( textureData );

					result.setTexParameteri( GL.GL_TEXTURE_WRAP_S , GL.GL_REPEAT );
					result.setTexParameteri( GL.GL_TEXTURE_WRAP_T , GL.GL_REPEAT );

					if ( autoMipmapGeneration )
					{
						try
						{
							/**
							 * Set generate mipmaps to true, this greatly increases performance and viewing pleasure in big scenes.
							 * @TODO need to find out if generated mipmaps are faster or if pregenerated mipmaps are faster
							 */
							result.setTexParameteri( GL2.GL_GENERATE_MIPMAP , GL.GL_TRUE );

							/** Set texture magnification to GL_LINEAR to support mipmaps. */
							result.setTexParameteri( GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR );

							/** Set texture minification to GL_LINEAR_MIPMAP_NEAREST to support mipmaps. */
							result.setTexParameteri( GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR_MIPMAP_NEAREST );
						}
						catch ( GLException e )
						{
							/*
							 * If setting texture parameters fails, it's no
							 * severe problem. Catch any exception so the view
							 * doesn't crash.
							 */
							e.printStackTrace();
						}
					}

					final ColorModel colorModel = bufferedImage.getColorModel();
					if ( colorModel.hasAlpha() )
					{
						_alpha.add( texture );
					}
				}
			}

			_textures.put( texture , result );
		}

		return result;
	}

	/**
	 * Test if OpenGL supports auto-generated mipmaps.
	 *
	 * @param   gl  OpenGL pipeline.
	 *
	 * @return  <code>true</code> if OpenGL supports auto-generated mipmaps;
	 *          <code>false</code> otherwise.
	 */
	private static boolean hasAutoMipMapGenerationSupport( final GL gl )
	{
		return ( gl.isExtensionAvailable( "GL_VERSION_1_4" ) || gl.isExtensionAvailable( "GL_SGIS_generate_mipmap" ) );
	}

	/**
	 * Scales the given image, if necessary, such that it is compatible with the
	 * given OpenGL pipeline. The aspect ratio of the image may not be preserved.
	 *
	 * @param   image   Image to be scaled, if necessary.
	 * @param   gl      OpenGL pipeline.
	 *
	 * @return  Compatible texture image. If the given image already meets all
	 *          requirements, that same image is returned.
	 *
	 * @throws  IllegalStateException if the given OpenGL pipeline specifies a
	 *          non-positive maximum texture size.
	 */
	private static BufferedImage createCompatibleTextureImage( final BufferedImage image , final GL gl )
	{
		/*
		 * Textures must not exceed the maximum size. According to the OpenGL
		 * specification, this must be at least 64.
		 */
		final int[] maxTextureSizeBuffer = new int[ 1 ];
		gl.glGetIntegerv( GL.GL_MAX_TEXTURE_SIZE , maxTextureSizeBuffer , 0 );
		final int maximumTextureSize = Math.max( 64 , maxTextureSizeBuffer[ 0 ] );

		int scaledWidth  = Math.min( maximumTextureSize , image.getWidth()  );
		int scaledHeight = Math.min( maximumTextureSize , image.getHeight() );

		/*
		 * Texture sizes may need to be powers of two.
		 */
		if ( !gl.isExtensionAvailable( "GL_ARB_texture_non_power_of_two" ) )
		{
			scaledWidth  = MathTools.nearestPowerOfTwo( scaledWidth  );
			scaledHeight = MathTools.nearestPowerOfTwo( scaledHeight );
		}

		return ImageTools.createScaledInstance( image , scaledWidth , scaledHeight , false );
	}

	/**
	 * Get {@link Texture} for color map of {@link Material}.
	 *
	 * @param   gl              OpenGL pipeline.
	 * @param   material        Material to get color map texture from.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public Texture getColorMapTexture( final GL gl , final Material material )
	{
		final Texture result;

		if ( ( material != null ) && ( material.colorMap != null ) )
		{
			result = getTexture( gl , material.colorMap );
		}
		else
		{
			result = null;
		}
		return result;
	}

	/**
	 * Get {@link Texture} for bump map of {@link Material}.
	 *
	 * @param   gl          OpenGL pipeline.
	 * @param   material    MAterial to get bump map texture from.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public Texture getBumpMapTexture( final GL gl , final Material material )
	{
		Texture result = null;

		if ( ( material != null ) && TextTools.isNonEmpty( material.bumpMap ) )
		{
			final Map<String, Texture> textures = _textures;
			result = textures.get( material.bumpMap );

			if ( result == null )
			{
				BufferedImage bufferedImage = MapTools.loadImage( material.bumpMap );
				if ( bufferedImage != null )
				{
					bufferedImage = JOGLTools.createNormalMapFromBumpMap( bufferedImage );

					final boolean autoMipmapGeneration = hasAutoMipMapGenerationSupport( gl );

					result = TextureIO.newTexture( createTextureData( createCompatibleTextureImage( bufferedImage , gl ) ) );

					result.setTexParameteri( GL.GL_TEXTURE_WRAP_S , GL.GL_REPEAT );
					result.setTexParameteri( GL.GL_TEXTURE_WRAP_T , GL.GL_REPEAT );

					if ( autoMipmapGeneration )
					{
						try
						{
							/**
							 * Set generate mipmaps to true, this greatly increases performance and viewing pleasure in big scenes.
							 * @TODO need to find out if generated mipmaps are faster or if pregenerated mipmaps are faster
							 */
							result.setTexParameteri( GL2.GL_GENERATE_MIPMAP , GL.GL_TRUE );

							/** Set texture magnification to linear to support mipmaps. */
							result.setTexParameteri( GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR );

							/** Set texture minification to linear_mipmap)_nearest to support mipmaps */
							result.setTexParameteri( GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR_MIPMAP_NEAREST );
						}
						catch ( GLException e )
						{
							/*
							 * If setting texture parameters fails, it's no
							 * severe problem. Catch any exception so the view
							 * doesn't crash.
							 */
							e.printStackTrace();
						}
					}
				}

				textures.put( material.bumpMap , result );
			}
		}

		return result;
	}

	/**
	 * Get normalization cube map, used to perform DOT3 bump mapping. For each
	 * 3D texture coordinate, the value of the map represents the normalized
	 * vector from the origin in the direction of the coordinate.
	 *
	 * @param   gl  OpenGL pipeline.
	 *
	 * @return  Normalization cube map.
	 */
	public Texture getNormalizationCubeMap( final GL gl )
	{
		Texture result = _textures.get( NORMALIZATION_CUBE_MAP );
		if ( result == null )
		{
			result = JOGLTools.createNormalizationCubeMap( gl );
			_textures.put( NORMALIZATION_CUBE_MAP , result );
		}
		return result;
	}

	/**
	 * Returns a cube map based on the specified image. The image must have
	 * an aspect ratio of 4:3, consisting of 12 squares with the following
	 * layout:
	 * <pre>
	 *     +---+
	 *     | Y+|
	 * +---+---+---+---+
	 * | X-| Z+| X+| Z-|
	 * +---+---+---+---+
	 *     | Y-|
	 *     +---+
	 * </pre>
	 *
	 * @param   gl      OpenGL pipeline.
	 * @param   cube    Name of the cube map image.
	 *
	 * @return  Cube map texture.
	 */
	public Texture getCubeMap( final GL gl , final String cube )
	{
		final String key = "cube:" + cube;
		Texture result = _textures.get( key );

		if ( result == null )
		{
			final BufferedImage cubeImage = MapTools.getImage( cube );
			if ( cubeImage != null )
			{
				if ( cubeImage.getWidth() * 3 != cubeImage.getHeight() * 4 )
				{
					throw new IllegalArgumentException( "Cube map must have 4:3 aspect ratio" );
				}

				if ( cubeImage.getWidth() % 4 != 0 )
				{
					throw new IllegalArgumentException( "Cube map width must be a multiple of 4" );
				}

				if ( cubeImage.getHeight() % 3 != 0 )
				{
					throw new IllegalArgumentException( "Cube map height must be a multiple of 3" );
				}

				result = TextureIO.newTexture( GL.GL_TEXTURE_CUBE_MAP );
				result.bind();

				/*
				 * Create the six sides of the cube map.
				 */
				loadCubeMap( gl , cubeImage , CubeMapOrientation.NEGATIVE_Y );

				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL2.GL_GENERATE_MIPMAP   , GL.GL_TRUE );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR_MIPMAP_LINEAR );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_S     , GL.GL_CLAMP_TO_EDGE );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_T     , GL.GL_CLAMP_TO_EDGE );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL2.GL_TEXTURE_WRAP_R    , GL.GL_CLAMP_TO_EDGE );

				_textures.put( key, result );
			}
		}

		return result;
	}

	/**
	 * Returns a cube map based on the specified images.
	 *
	 * @param   gl  OpenGL pipeline.
	 * @param   x1  Image on the negative-X side of the cube.
	 * @param   y1  Image on the negative-Y side of the cube.
	 * @param   z1  Image on the negative-Z side of the cube.
	 * @param   x2  Image on the positive-X side of the cube.
	 * @param   y2  Image on the positive-Y side of the cube.
	 * @param   z2  Image on the positive-Z side of the cube.
	 *
	 * @return  Cube map texture.
	 */
	public Texture getCubeMap( final GL gl , final String x1 , final String y1 , final String z1 , final String x2 , final String y2 , final String z2 )
	{
		final String key = "cube6:" + x1 + ":" + y1 + ":" + z1 + ":" + x2 + ":" + y2 + ":" + z2;
		Texture result = _textures.get( key );

		if ( result == null )
		{
			result = TextureIO.newTexture( GL.GL_TEXTURE_CUBE_MAP );
			result.bind();

			/*
			 * Create the six sides of the cube map.
			 */
			loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X , x1 );
			loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y , y1 );
			loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z , z1 );
			loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X , x2 );
			loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y , y2 );
			loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z , z2 );

			gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR        );
			gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR        );
			gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_S     , GL.GL_CLAMP_TO_EDGE );
			gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_T     , GL.GL_CLAMP_TO_EDGE );
			gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL2.GL_TEXTURE_WRAP_R    , GL.GL_CLAMP_TO_EDGE );

			_textures.put( key, result );
		}

		return result;
	}

	/**
	 * Loads the given image as a cube map into the texture that is bound to the
	 * active texture unit.
	 *
	 * @param   gl              OpenGL pipeline.
	 * @param   image           Image to be loaded.
	 * @param   orientation     Orientation of the cube map representation.
	 */
	private static void loadCubeMap( final GL gl , final BufferedImage image , final CubeMapOrientation orientation )
	{
		final int size = image.getWidth() / 4;
		switch ( orientation )
		{
			case POSITIVE_Z:
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X , getSubImage( image , 0 , 0 , size , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y , getSubImage( image , 0 , size , 2 * size , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z , getSubImage( image , 0 , 3 * size , size , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X , getSubImage( image , 0 , 2 * size , size , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y , getSubImage( image , 0 , size , 0 , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z , getSubImage( image , 0 , size , size , size , size ) );
				break;

			case NEGATIVE_Y:
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X , getSubImage( image , -90 , 0 , size , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y , getSubImage( image , 0 , size , size , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z , getSubImage( image , 0 , size , 2 * size , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X , getSubImage( image , 90 , 2 * size , size , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y , getSubImage( image , 180 , 3 * size , size , size , size ) );
				loadCubeMap( gl , GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z , getSubImage( image , 0 , size , 0 , size , size ) );
				break;
		}
	}

	/**
	 * Loads the specified image as a cube map into the texture that is bound to
	 * the active texture unit.
	 *
	 * @param   gl      OpenGL pipeline.
	 * @param   target  Texture target.
	 * @param   image   Image to be loaded.
	 */
	private static void loadCubeMap( final GL gl , final int target , final String image )
	{
		final BufferedImage bufferedImage = MapTools.loadImage( image );
		if ( bufferedImage != null )
		{
			final BufferedImage compatibleImage = createCompatibleTextureImage( bufferedImage , gl );
			loadCubeMap( gl , target , compatibleImage );
		}
	}

	/**
	 * Loads the given image as a cube map into the texture that is bound to the
	 * active texture unit.
	 *
	 * @param   gl      OpenGL pipeline.
	 * @param   target  Texture target.
	 * @param   image   Image to be loaded.
	 */
	private static void loadCubeMap( final GL gl , final int target , final BufferedImage image )
	{
		final TextureData textureData = createTextureData( image );
		gl.glTexImage2D( target , 0 , textureData.getInternalFormat() , textureData.getWidth() , textureData.getHeight() , textureData.getBorder() , textureData.getPixelFormat() , textureData.getPixelType() , textureData.getBuffer() );
		textureData.flush();
	}

	/**
	 * Copied from TextureData in JOGL 1.1.1, with some simplifications.
	 *
	 * @noinspection JavaDoc
	 */
	private static TextureData createTextureData( final BufferedImage image )
	{
		final int width = image.getWidth();
		final int height = image.getHeight();

		final int pixelType;
		final int pixelFormat;
		switch ( image.getType() )
		{
			case BufferedImage.TYPE_INT_RGB:
				pixelFormat = GL2.GL_BGRA;
				pixelType = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
				break;
			case BufferedImage.TYPE_INT_ARGB:
				pixelFormat = GL.GL_RGBA;
				pixelType = GL.GL_UNSIGNED_BYTE;
				break;
			case BufferedImage.TYPE_INT_ARGB_PRE:
				pixelFormat = GL2.GL_BGRA;
				pixelType = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
				break;
			case BufferedImage.TYPE_INT_BGR:
				pixelFormat = GL.GL_RGBA;
				pixelType = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
				break;
			case BufferedImage.TYPE_3BYTE_BGR:
				pixelFormat = GL2.GL_BGR;
				pixelType = GL.GL_UNSIGNED_BYTE;
				break;
			case BufferedImage.TYPE_4BYTE_ABGR_PRE:
				pixelFormat = GL2.GL_ABGR_EXT;
				pixelType = GL.GL_UNSIGNED_BYTE;
				break;
			case BufferedImage.TYPE_USHORT_565_RGB:
				pixelFormat = GL.GL_RGB;
				pixelType = GL.GL_UNSIGNED_SHORT_5_6_5;
				break;
			case BufferedImage.TYPE_USHORT_555_RGB:
				pixelFormat = GL2.GL_BGRA;
				pixelType = GL2.GL_UNSIGNED_SHORT_1_5_5_5_REV;
				break;
			case BufferedImage.TYPE_BYTE_GRAY:
				pixelFormat = GL.GL_LUMINANCE;
				pixelType = GL.GL_UNSIGNED_BYTE;
				break;
			case BufferedImage.TYPE_USHORT_GRAY:
				pixelFormat = GL.GL_LUMINANCE;
				pixelType = GL.GL_UNSIGNED_SHORT;
				break;
			default:
				throw new IllegalArgumentException( "Unsupported image type: " + image.getType() );
		}

		final Buffer buffer = wrapImageDataBuffer( image );
		return new TextureData( GL.GL_RGBA , width , height , 0 , pixelFormat , pixelType , true , false , true , buffer , null );
	}

	/**
	 * Copied from TextureData in JOGL 1.1.1.
	 *
	 * @noinspection MethodWithMultipleReturnPoints,JavaDoc
	 */
	private static Buffer wrapImageDataBuffer( final BufferedImage image )
	{
		//
		// Note: Grabbing the DataBuffer will defeat Java2D's image
		// management mechanism (as of JDK 5/6, at least).  This shouldn't
		// be a problem for most JOGL apps, but those that try to upload
		// the image into an OpenGL texture and then use the same image in
		// Java2D rendering might find the 2D rendering is not as fast as
		// it could be.
		//

		final WritableRaster raster = image.getRaster();
		final DataBuffer data = raster.getDataBuffer();

		if ( data instanceof DataBufferByte )
		{
			return ByteBuffer.wrap( ( (DataBufferByte)data ).getData() );
		}
		else if ( data instanceof DataBufferDouble )
		{
			throw new RuntimeException( "DataBufferDouble rasters not supported by OpenGL" );
		}
		else if ( data instanceof DataBufferFloat )
		{
			return FloatBuffer.wrap( ( (DataBufferFloat)data ).getData() );
		}
		else if ( data instanceof DataBufferInt )
		{
			return IntBuffer.wrap( ( (DataBufferInt)data ).getData() );
		}
		else if ( data instanceof DataBufferShort )
		{
			return ShortBuffer.wrap( ( (DataBufferShort)data ).getData() );
		}
		else if ( data instanceof DataBufferUShort )
		{
			return ShortBuffer.wrap( ( (DataBufferUShort)data ).getData() );
		}
		else
		{
			throw new RuntimeException( "Unexpected DataBuffer type?" );
		}
	}

	/**
	 * Returns a sub-image from the given image.
	 *
	 * @param   image   Source image.
	 * @param   angle   Angle of rotation, in degrees.
	 * @param   x       Top-left corner x-coordinate of the source rectangle.
	 * @param   y       Top-left corner y-coordinate of the source rectangle.
	 * @param   w       Width of the source rectangle.
	 * @param   h       Height of the source rectangle.
	 *
	 * @return  Specified sub-image.
	 */
	private static BufferedImage getSubImage( final BufferedImage image , final int angle , final int x , final int y , final int w , final int h )
	{
		final BufferedImage result = new BufferedImage( w , h , image.getType() );
		final Graphics2D g = result.createGraphics();
		final AffineTransform transform = new AffineTransform();
		transform.translate( (double)( w / 2 ) , (double)( h / 2 ) );
		transform.rotate( Math.toRadians( (double)-angle ) );
		transform.translate( (double)( -x - w / 2 ) , (double)( -y - h / 2 ) );
		g.setTransform( transform );
		g.drawImage( image , 0 , 0 , null );
		g.dispose();
		return result;
	}
}
