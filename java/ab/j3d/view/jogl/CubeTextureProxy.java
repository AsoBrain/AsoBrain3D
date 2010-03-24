/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2010-2010 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view.jogl;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ab.j3d.MapTools;

/**
 * Provides a cube map texture, with support for loading texture data
 * asynchronously.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class CubeTextureProxy
	extends TextureProxy
{
	/** Name of the image for the negative-X side of the cube. */ private final String _x1;
	/** Name of the image for the negative-Y side of the cube. */ private final String _y1;
	/** Name of the image for the negative-Z side of the cube. */ private final String _z1;
	/** Name of the image for the positive-X side of the cube. */ private final String _x2;
	/** Name of the image for the positive-Y side of the cube. */ private final String _y2;
	/** Name of the image for the positive-Z side of the cube. */ private final String _z2;

	/** Texture data for the negative-X side of the cube. */ private volatile TextureData _x1Data = null;
	/** Texture data for the negative-Y side of the cube. */ private volatile TextureData _y1Data = null;
	/** Texture data for the negative-Z side of the cube. */ private volatile TextureData _z1Data = null;
	/** Texture data for the positive-X side of the cube. */ private volatile TextureData _x2Data = null;
	/** Texture data for the positive-Y side of the cube. */ private volatile TextureData _y2Data = null;
	/** Texture data for the positive-Z side of the cube. */ private volatile TextureData _z2Data = null;

	/**
	 * Specifies the orientation/layout of a cube map represented as a single
	 * texture. Constants are named after the side that the center square faces.
	 */
	public enum CubeMapOrientation
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
	 * Constructs a new cube map texture from a single image.
	 *
	 * @param   name            Name of the image.
	 * @param   textureCache    Texture cache.
	 */
	public CubeTextureProxy( @NotNull final String name , final TextureCache textureCache )
	{
		super( name , textureCache );
		_x1 = null;
		_y1 = null;
		_z1 = null;
		_x2 = null;
		_y2 = null;
		_z2 = null;
	}

	/**
	 * Constructs a new cube map texture from six seperate images.
	 *
	 * @param   x1              Image on the negative-X side of the cube.
	 * @param   y1              Image on the negative-Y side of the cube.
	 * @param   z1              Image on the negative-Z side of the cube.
	 * @param   x2              Image on the positive-X side of the cube.
	 * @param   y2              Image on the positive-Y side of the cube.
	 * @param   z2              Image on the positive-Z side of the cube.
	 * @param   textureCache    Texture cache.
	 */
	public CubeTextureProxy( @NotNull final String x1 , @NotNull final String y1 , @NotNull final String z1 , @NotNull final String x2 , @NotNull final String y2 , @NotNull final String z2 , final TextureCache textureCache )
	{
		super( "" , textureCache );
		_x1 = x1;
		_y1 = y1;
		_z1 = z1;
		_x2 = x2;
		_y2 = y2;
		_z2 = z2;
	}

	@Override
	public TextureData call()
	{
		if ( _x1 == null )
		{
			loadFromSingleImage();
		}
		else
		{
			loadFromMultipleImages();
		}

		// Not really applicable for a cube map.
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
				texture.bind();

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

				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_GENERATE_MIPMAP    , GL.GL_TRUE );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR_MIPMAP_LINEAR );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_S     , GL.GL_CLAMP_TO_EDGE );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_T     , GL.GL_CLAMP_TO_EDGE );
				gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_R     , GL.GL_CLAMP_TO_EDGE );

				_texture = texture;
			}
		}

		return texture;
	}

	/**
	 * Loads the texture data for the cube map from seperate images.
	 */
	private void loadFromMultipleImages()
	{
		_x1Data = loadTextureData( _x1 );
		_y1Data = loadTextureData( _y1 );
		_z1Data = loadTextureData( _z1 );
		_x2Data = loadTextureData( _x2 );
		_y2Data = loadTextureData( _y2 );
		_z2Data = loadTextureData( _z2 );
	}

	/**
	 * Creates texture data for the specified image.
	 *
	 * @param   name    Name of the image.
	 *
	 * @return  Texture data.
	 */
	@Nullable
	private TextureData loadTextureData( @NotNull final String name )
	{
		TextureData result = null;

		final BufferedImage bufferedImage = MapTools.loadImage( name );
		if ( bufferedImage != null )
		{
			final BufferedImage compatibleImage = createCompatibleTextureImage( bufferedImage );
			result = createTextureData( compatibleImage );
		}

		return result;
	}

	/**
	 * Loads the texture data for the cube map from a single image.
	 */
	private void loadFromSingleImage()
	{
		final BufferedImage cubeImage = MapTools.getImage( _name );
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

			final int size = cubeImage.getWidth() / 4;

			TextureData x1Data = null;
			TextureData y1Data = null;
			TextureData z1Data = null;
			TextureData x2Data = null;
			TextureData y2Data = null;
			TextureData z2Data = null;

			switch ( CubeMapOrientation.NEGATIVE_Y )
			{
				case POSITIVE_Z:
					x1Data = createTextureData( getSubImage( cubeImage, 0 , 0 , size , size , size ) );
					y1Data = createTextureData( getSubImage( cubeImage, 0 , size , 2 * size , size , size ) );
					z1Data = createTextureData( getSubImage( cubeImage, 0 , 3 * size , size , size , size ) );
					x2Data = createTextureData( getSubImage( cubeImage, 0 , 2 * size , size , size , size ) );
					y2Data = createTextureData( getSubImage( cubeImage, 0 , size , 0 , size , size ) );
					z2Data = createTextureData( getSubImage( cubeImage, 0 , size , size , size , size ) );
					break;

				case NEGATIVE_Y:
					x1Data = createTextureData( getSubImage( cubeImage, -90 , 0 , size , size , size ) );
					y1Data = createTextureData( getSubImage( cubeImage, 0 , size , size , size , size ) );
					z1Data = createTextureData( getSubImage( cubeImage, 0 , size , 2 * size , size , size ) );
					x2Data = createTextureData( getSubImage( cubeImage, 90 , 2 * size , size , size , size ) );
					y2Data = createTextureData( getSubImage( cubeImage, 180 , 3 * size , size , size , size ) );
					z2Data = createTextureData( getSubImage( cubeImage, 0 , size , 0 , size , size ) );
					break;
			}

			_x1Data = x1Data;
			_y1Data = y1Data;
			_z1Data = z1Data;
			_x2Data = x2Data;
			_y2Data = y2Data;
			_z2Data = z2Data;
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

	@Override
	public String toString()
	{
		return super.toString() + ( ( _name == "" ) ? "[" + _x1 + "," + _y1 + "," + _z1 + "," + _x2 + "," + _y2 + "," + _z2 + "]"
		                                            : "[" + _name + "]" );
	}
}
