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
package ab.j3d.appearance;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import org.jetbrains.annotations.*;

/**
 * Implementation of {@link CubeMap} based on a single image.
 *
 * @author  G. Meinders
 * @version $Revision$ ($Date$, $Author$)
 */
public class SingleImageCubeMap
	implements CubeMap
{
	/**
	 * Specifies the orientation/layout of a cube map represented as a single
	 * texture. Constants are named after the side that the center square faces.
	 */
	public enum Orientation
	{
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
		POSITIVE_Z,

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
		NEGATIVE_Y,
	}

	/**
	 * Orientation/layout of single texture cube map.
	 */
	@NotNull
	private Orientation _orientation = Orientation.NEGATIVE_Y;

	/**
	 * Image for entire cube map.
	 */
	private BufferedImage _image = null;

	/**
	 * Image on the negative-X side of the cube.
	 */
	private BufferedImage _imageX1 = null;

	/**
	 * Image on the negative-Y side of the cube.
	 */
	private BufferedImage _imageY1 = null;

	/**
	 * Image on the negative-Z side of the cube.
	 */
	private BufferedImage _imageZ1 = null;

	/**
	 * Image on the positive-X side of the cube.
	 */
	private BufferedImage _imageX2 = null;

	/**
	 * Image on the positive-Y side of the cube.
	 */
	private BufferedImage _imageY2 = null;

	/**
	 * Image on the positive-Z side of the cube.
	 */
	private BufferedImage _imageZ2 = null;

	/**
	 * Construct cube map. Cube map properties must be set for this cube map to
	 * be functional.
	 */
	public SingleImageCubeMap()
	{
	}

	/**
	 * Create cube map from image with {@link Orientation#NEGATIVE_Y}
	 * orientation.
	 *
	 * @param   image   Image for entire cube map.
	 */
	public SingleImageCubeMap( @NotNull final BufferedImage image )
	{
		_image = image;
	}

	/**
	 * Create cube map from given image with the given orientation.
	 *
	 * @param   orientation     Orientation/layout of cube map.
	 * @param   image           Image for entire cube map.
	 *
	 * @throws  IllegalArgumentException if the image has invalid dimensions.
	 */
	public SingleImageCubeMap( @NotNull final Orientation orientation, @NotNull final BufferedImage image )
	{
		_orientation = orientation;
		_image = image;
	}

	/**
	 * Get orientation/layout of cube map represented as a single image.
	 *
	 * @return  Orientation/layout of cube map.
	 */
	@NotNull
	public Orientation getOrientation()
	{
		return _orientation;
	}

	/**
	 * Set orientation/layout of cube map represented as a single texture.
	 *
	 * @param   orientation     Orientation/layout of cube map.
	 */
	public void setOrientation( @NotNull final Orientation orientation )
	{
		_orientation = orientation;
	}

	/**
	 * Get image to use when the cube map is defined by a single image.
	 *
	 * @return  Image for entire cube map.
	 */
	@Nullable
	public BufferedImage getImage()
	{
		return _image;
	}

	/**
	 * Set single image to use for entire cube map.
	 *
	 * @param   image   Image for entire cube map.
	 *
	 * @throws  IllegalArgumentException if the image has invalid dimensions.
	 */
	public void setImage( final BufferedImage image )
	{
		if ( image != _image )
		{
			_imageX1 = null;
			_imageY1 = null;
			_imageZ1 = null;
			_imageX2 = null;
			_imageY2 = null;
			_imageZ2 = null;

			if ( image != null )
			{
				final int width = image.getWidth();
				final int height = image.getHeight();

				if ( width * 3 != height * 4 )
				{
					throw new IllegalArgumentException( "Cube map must have 4:3 aspect ratio, but is " + width + " x " + height );
				}

				if ( width % 4 != 0 )
				{
					throw new IllegalArgumentException( "Cube map width must be a multiple of 4, but is " + width + " x " + height );
				}

				if ( height % 3 != 0 )
				{
					throw new IllegalArgumentException( "Cube map height must be a multiple of 3, but is " + width + " x " + height );
				}
			}
		}
	}

	@Override
	public BufferedImage getImageX1()
	{
		BufferedImage result = _imageX1;
		if ( result == null )
		{
			final BufferedImage image = getImage();
			if ( image != null )
			{
				final int size = image.getWidth() / 4;

				switch ( getOrientation() )
				{
					case POSITIVE_Z:
						result = image.getSubimage( 0, size, size, size );
						break;

					case NEGATIVE_Y:
						result = getRotatedSubImage( image, 0, size, size, size, 270 );
//						result = getRotatedImage( image.getSubimage( 0, size, size, size ), 270 );
						break;

					default:
						throw new AssertionError( getOrientation() );
				}
			}
		}

		return result;
	}

	@Override
	public BufferedImage getImageY1()
	{
		BufferedImage result = _imageY1;
		if ( result == null )
		{
			final BufferedImage image = getImage();
			if ( image != null )
			{
				final int size = image.getWidth() / 4;

				switch ( getOrientation() )
					{
						case POSITIVE_Z:
							result = image.getSubimage( size, 2 * size, size, size );
							break;

						case NEGATIVE_Y:
							result = image.getSubimage( size, size, size, size );
							break;

						default:
							throw new AssertionError( getOrientation() );
					}
			}
		}

		return result;
	}

	@Override
	public BufferedImage getImageZ1()
	{
		BufferedImage result = _imageZ1;
		if ( result == null )
		{
			final BufferedImage image = getImage();
			if ( image != null )
			{
				final int size = image.getWidth() / 4;

				switch ( getOrientation() )
					{
						case POSITIVE_Z:
							result = image.getSubimage( size * 3, size, size, size );
							break;

						case NEGATIVE_Y:
							result = image.getSubimage( size, 2 * size, size, size );
							break;

						default:
							throw new AssertionError( getOrientation() );
					}
			}
		}

		return result;
	}

	@Override
	public BufferedImage getImageX2()
	{
		BufferedImage result = _imageX2;
		if ( result == null )
		{
			final BufferedImage image = getImage();
			if ( image != null )
			{
				final int size = image.getWidth() / 4;

				switch ( getOrientation() )
					{
						case POSITIVE_Z:
							result = image.getSubimage( 2 * size, size, size, size );
							break;

						case NEGATIVE_Y:
							result = getRotatedSubImage( image, 2 * size, size, size, size, 90 );
			//				result = getRotatedImage( image.getSubimage( 2 * size, size, size, size ), 90 );
							break;

						default:
							throw new AssertionError( getOrientation() );
					}
			}
		}

		return result;
	}

	@Override
	public BufferedImage getImageY2()
	{
		BufferedImage result = _imageY2;
		if ( result == null )
		{
			final BufferedImage image = getImage();
			if ( image != null )
			{
				final int size = image.getWidth() / 4;

				switch ( getOrientation() )
					{
						case POSITIVE_Z:
							result = image.getSubimage( size, 0, size, size );
							break;

						case NEGATIVE_Y:
							result = getRotatedSubImage( image, 3 * size, size, size, size, 180 );
			//				result = getRotatedImage( image.getSubimage( 3 * size, size, size, size ), 180 );
							break;

						default:
							throw new AssertionError( getOrientation() );
					}
			}
		}

		return result;
	}

	@Override
	public BufferedImage getImageZ2()
	{
		BufferedImage result = _imageZ2;
		if ( result == null )
		{
			final BufferedImage image = getImage();
			if ( image != null )
			{
				final int size = image.getWidth() / 4;

				switch ( getOrientation() )
					{
						case POSITIVE_Z:
							result = image.getSubimage( size, size, size, size );
							break;

						case NEGATIVE_Y:
							result = image.getSubimage( size, 0, size, size );
							break;

						default:
							throw new AssertionError( getOrientation() );
					}
			}
		}

		return result;
	}

	/**
	 * Returns a sub-image from the given image.
	 *
	 * @param   image   Source image.
	 * @param   x       Top-left corner x-coordinate of the source rectangle.
	 * @param   y       Top-left corner y-coordinate of the source rectangle.
	 * @param   w       Width of the source rectangle.
	 * @param   h       Height of the source rectangle.
	 * @param   angle   Angle of rotation, in degrees.
	 *
	 * @return  Specified sub-image.
	 */
	private static BufferedImage getRotatedSubImage( final BufferedImage image, final int x, final int y, final int w, final int h, final int angle )
	{
		final BufferedImage result = new BufferedImage( w, h, image.getType() );
		final Graphics2D g = result.createGraphics();
		final AffineTransform transform = new AffineTransform();
		transform.translate( (double)( w / 2 ), (double)( h / 2 ) );
		transform.rotate( Math.toRadians( (double)-angle ) );
		transform.translate( (double)( -x - w / 2 ), (double)( -y - h / 2 ) );
		g.setTransform( transform );
		g.drawImage( image, 0, 0, null );
		g.dispose();
		return result;
	}

	/**
	 * Returns rotated copy of the given image.
	 *
	 * @param   image   Source image.
	 * @param   angle   Angle of rotation (CCW), in degrees (multiple of 90).
	 *
	 * @return  Rotated image.
	 */
	private static BufferedImage getRotatedImage( final BufferedImage image, final int angle  )
	{
		final int size = image.getWidth();
		if ( image.getHeight() != size )
		{
			throw new IllegalArgumentException( "Image should be square, but is " + image.getWidth() + " x " + image.getHeight() );
		}

		final BufferedImage result = new BufferedImage( size, size, image.getType() );
		rotateSquareRaster( image.getRaster(), result.getRaster(), angle );
		return result;
	}

	/**
	 * Rotate contents from one raster and writes it to another.
	 *
	 * @param   src     Raster to get image from.
	 * @param   dst     Raster to write image to.
	 * @param   angle   Angle of rotation (CCW), in degrees (multiple of 90).
	 */
	private static void rotateSquareRaster( final Raster src, final WritableRaster dst, final int angle  )
	{
		final int size = src.getWidth();
		final int samplesPerPixel = src.getNumBands();
		final int[] samples = new int[ ( size + 1 ) * samplesPerPixel ];
		final int lastIndex = ( size - 1 ) * samplesPerPixel;
		final int tempIndex = lastIndex + samplesPerPixel;

		switch ( ( angle < 0 ) ? angle % 360 + 360 : angle % 360 )
		{
			case 90:
				/*    0 1 2    0 1 2
				 * 0  A B C    C H M
				 * 1  F G H -> B G L   90 CCW - 270 CW
				 * 2  K L M    A F K
				 */
				for ( int i = 0; i < size; i++ )
				{
					src.getPixels( i, 0, 1, size, samples );
					dst.setPixels( 0, size - 1 - i, size, 1, samples );
				}
				break;

			case 180:
				/*   0 1 2    0 1 2
				 * 0 A B C    M L K
				 * 1 F G H -> H G F
				 * 2 K L M    C B A
				 */
				for ( int i = 0; i < size; i++ )
				{
					src.getPixels( 0, i, size, 1, samples );

					for ( int head = 0, tail = lastIndex; head < tail; head += samplesPerPixel, tail -= samplesPerPixel )
					{
						System.arraycopy( samples, tail, samples, tempIndex, samplesPerPixel );
						System.arraycopy( samples, head, samples, tail, samplesPerPixel );
						System.arraycopy( samples, tempIndex, samples, head, samplesPerPixel );
					}

					dst.setPixels( size - 1 - i, 0, 1, size, samples );
				}
				break;

			case 270:
				/*   0 1 2    0 1 2
				 * 0 A B C    K F A
				 * 1 F G H -> L G B   270 CCW - 90 CW
				 * 2 K L M    M H C
				 */
				for ( int i = 0; i < size; i++ )
				{
					src.getPixels( 0, i, size, 1, samples );
					dst.setPixels( size - 1 - i, 0, 1, size, samples );
				}
				break;

			default:
				throw new IllegalArgumentException( "angle: " + angle );
		}
	}
}
