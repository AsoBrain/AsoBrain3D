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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.media.opengl.GL;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ab.j3d.MapTools;

import com.numdata.oss.MathTools;
import com.numdata.oss.ui.ImageTools;

/**
 * Provides access to a {@link Texture} in such a way that it can be loaded
 * asynchronously. Typical usage involves submitting an object of this class to
 * an {@link ExecutorService} and passing the resulting future back using
 * {@link #setTextureData(Future)}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TextureProxy
	implements Callable<TextureData>
{
	/**
	 * Name of the texture image.
	 */
	protected final String _name;

	/**
	 * Texture cache.
	 */
	protected final TextureCache _textureCache;

	/**
	 * Future that will provide texture data, when it becomes available.
	 */
	private Future<TextureData> _textureData;

	/**
	 * Texture, when available.
	 */
	protected Texture _texture;

	/**
	 * Construct new texture proxy for a texture that is already loaded.
	 *
	 * @param   texture     Texture to be wrapped.
	 *
	 * @see     MapTools#loadImage
	 */
	public TextureProxy( @NotNull final Texture texture )
	{
		_name = null;
		_textureCache = null;
		_textureData = null;
		_texture = texture;
	}

	/**
	 * Construct new texture proxy.
	 *
	 * @param   name            Name of the texture image.
	 * @param   textureCache    Texture cache.
	 *
	 * @see     MapTools#loadImage
	 */
	public TextureProxy( @NotNull final String name , final TextureCache textureCache )
	{
		_name = name;
		_textureCache = textureCache;
		_textureData = null;
		_texture = null;
	}

	/**
	 * Returns whether the proxy has access to texture data to create a texture
	 * from when needed.
	 *
	 * @return  <code>true</code> if texture data is available/accessible.
	 */
	public boolean isTextureDataSet()
	{
		return ( _textureData != null );
	}

	/**
	 * Returns the texture, if available. Must be called on the OpenGL thread.
	 *
	 * @return  Texture.
	 *
	 * @throws  GLException if there is no current OpenGL context.
	 */
	@Nullable
	public Texture getTexture()
	{
		Texture texture = _texture;

		if ( texture == null )
		{
			final Future<TextureData> textureDataFuture = _textureData;
			if ( ( textureDataFuture != null ) && textureDataFuture.isDone() )
			{
				final TextureData textureData = getTextureData();
				if ( textureData != null )
				{
					texture = createTexture( textureData );
					textureData.flush();
					_texture = texture;
				}
			}
		}

		return texture;
	}

	/**
	 * Creates a texture from the given texture data.
	 *
	 * @param   textureData     Texture data.
	 *
	 * @return  Created texture.
	 */
	protected Texture createTexture( final TextureData textureData )
	{
		final Texture result = TextureIO.newTexture( textureData );

		final GL gl = GLU.getCurrentGL();
		setTextureParameters( gl , result );

		return result;
	}

	/**
	 * Returns the texture data, if available.
	 *
	 * @return  Texture data.
	 */
	public TextureData getTextureData()
	{
		TextureData result = null;

		final Future<TextureData> textureData = _textureData;
		if ( ( textureData != null ) && textureData.isDone() )
		{
			try
			{
				result = textureData.get();
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
			catch ( ExecutionException e )
			{
				e.printStackTrace();
				_textureData = null;
			}
		}

		return result;
	}

	/**
	 * Sets a future that will provide texture data when it becomes available.
	 *
	 * @param   textureData     Future providing texture data.
	 */
	public void setTextureData( @NotNull final Future<TextureData> textureData )
	{
		_textureData = textureData;
	}

	/**
	 * Loads and returns the texture data for the texture.
	 *
	 * @return  Texture data.
	 */
	@Override
	public TextureData call()
	{
		TextureData result = null;

		final BufferedImage bufferedImage = MapTools.loadImage( _name );
		if ( bufferedImage != null )
		{
			final BufferedImage compatibleImage = createCompatibleTextureImage( bufferedImage );
			final TextureData textureData = createTextureData( compatibleImage );
			result = textureData;
		}

		return result;
	}

	/**
	 * Scales the given image, if necessary according to the criteria specified
	 * by the texture cache.
	 *
	 * @param   image   Image to be scaled, if necessary.
	 *
	 * @return  Compatible texture image. If the given image already meets all
	 *          requirements, that same image is returned.
	 *
	 * @throws  IllegalStateException if the given OpenGL pipeline specifies a
	 *          non-positive maximum texture size.
	 */
	protected BufferedImage createCompatibleTextureImage( final BufferedImage image )
	{
		final TextureCache textureCache = _textureCache;

		int scaledWidth  = Math.min( textureCache.getMaximumTextureSize() , image.getWidth()  );
		int scaledHeight = Math.min( textureCache.getMaximumTextureSize() , image.getHeight() );

		/*
		 * Texture sizes may need to be powers of two.
		 */
		if ( !textureCache.isNonPowerOfTwo() )
		{
			scaledWidth  = MathTools.nearestPowerOfTwo( scaledWidth  );
			scaledHeight = MathTools.nearestPowerOfTwo( scaledHeight );
		}

		return ImageTools.createScaledInstance( image , scaledWidth , scaledHeight , false );
	}

	/**
	 * Copied from TextureData in JOGL 1.1.1, with some simplifications.
	 *
	 * @noinspection JavaDoc
	 */
	protected TextureData createTextureData( final BufferedImage image )
	{
		final int width = image.getWidth();
		final int height = image.getHeight();

		int internalFormat;
		int pixelType;
		int pixelFormat;

		boolean convertImage = false;
		boolean expectingGL12 = false;

		switch ( image.getType() )
		{
			case BufferedImage.TYPE_INT_RGB:
				internalFormat = GL.GL_RGB;
				pixelFormat = GL.GL_BGRA;
				pixelType = GL.GL_UNSIGNED_INT_8_8_8_8_REV;
				expectingGL12 = true;
				break;
			case BufferedImage.TYPE_INT_ARGB:
				internalFormat = GL.GL_RGBA;
				pixelFormat = GL.GL_RGBA;
				pixelType = GL.GL_UNSIGNED_BYTE;
				expectingGL12 = true;
				break;
			case BufferedImage.TYPE_INT_ARGB_PRE:
				internalFormat = GL.GL_RGBA;
				pixelFormat = GL.GL_BGRA;
				pixelType = GL.GL_UNSIGNED_INT_8_8_8_8_REV;
				expectingGL12 = true;
				break;
			case BufferedImage.TYPE_INT_BGR:
				internalFormat = GL.GL_RGB;
				pixelFormat = GL.GL_RGBA;
				pixelType = GL.GL_UNSIGNED_INT_8_8_8_8_REV;
				expectingGL12 = true;
				break;
			case BufferedImage.TYPE_3BYTE_BGR:
				internalFormat = GL.GL_RGB;
				pixelFormat = GL.GL_BGR;
				pixelType = GL.GL_UNSIGNED_BYTE;
				break;
			case BufferedImage.TYPE_BYTE_GRAY:
				internalFormat = GL.GL_RGB;
				pixelFormat = GL.GL_LUMINANCE;
				pixelType = GL.GL_UNSIGNED_BYTE;
				break;
			case BufferedImage.TYPE_USHORT_GRAY:
				internalFormat = GL.GL_RGB;
				pixelFormat = GL.GL_LUMINANCE;
				pixelType = GL.GL_UNSIGNED_SHORT;
				break;
			default:
				internalFormat = GL.GL_RGBA;
				pixelFormat = GL.GL_RGBA;
				pixelType = GL.GL_UNSIGNED_BYTE;
				convertImage = true;
				break;
		}

		final Buffer buffer;

		/*
		 * OpenGL below 1.2 (i.e. Windows default) doesn't support a lot of
		 * pixel formats. The image data has to be converted.
		 */
		convertImage |= ( expectingGL12 && !_textureCache.isOpenGL12() );

		if ( convertImage )
		{
			buffer = createBufferFromCustom( image );

			final ColorModel colorModel = image.getColorModel();
			internalFormat = colorModel.hasAlpha() ? GL.GL_RGBA : GL.GL_RGB;
			pixelFormat = colorModel.hasAlpha() ? GL.GL_RGBA : GL.GL_RGB;
			pixelType = GL.GL_UNSIGNED_BYTE;
		}
		else
		{
			buffer = wrapImageDataBuffer( image );
		}

		return new TextureData( internalFormat , width , height , 0 , pixelFormat , pixelType , true , false , true , buffer , null );
	}

	/**
	 * Copied from TextureData in JOGL 1.1.1.
	 *
	 * @noinspection MethodWithMultipleReturnPoints,JavaDoc
	 */
	protected static Buffer wrapImageDataBuffer( final BufferedImage image )
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
	 * Copied from TextureData in JOGL 1.1.1.
	 *
	 * @noinspection JavaDoc
	 */
	private static Buffer createBufferFromCustom( @NotNull final BufferedImage image )
	{
		final int width = image.getWidth();
		final int height = image.getHeight();

		// create a temporary image that is compatible with OpenGL
		final ColorModel imageColorModel = image.getColorModel();
		final boolean hasAlpha = imageColorModel.hasAlpha();

		final ColorModel colorModel;

		// Don't use integer components for packed int images
		final int dataBufferType;
		if ( isPackedInt( image ) )
		{
			dataBufferType = DataBuffer.TYPE_BYTE;
		}
		else
		{
			final WritableRaster imageRaster = image.getRaster();
			final DataBuffer imageBuffer = imageRaster.getDataBuffer();
			dataBufferType = imageBuffer.getDataType();
		}

		if ( dataBufferType == DataBuffer.TYPE_BYTE )
		{
			if ( hasAlpha )
			{
				colorModel = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ) , new int[] { 8 , 8 , 8 , 8 } , true , true , Transparency.TRANSLUCENT , DataBuffer.TYPE_BYTE );
			}
			else
			{
				colorModel = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ) , new int[] { 8 , 8 , 8 , 0 } , false , false , Transparency.OPAQUE , DataBuffer.TYPE_BYTE );
			}
		}
		else
		{
			if ( hasAlpha )
			{
				colorModel = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ) , null , true , true , Transparency.TRANSLUCENT , dataBufferType );
			}
			else
			{
				colorModel = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ) , null , false , false , Transparency.OPAQUE , dataBufferType );
			}
		}

		final boolean premultiplied = colorModel.isAlphaPremultiplied();
		final WritableRaster raster = colorModel.createCompatibleWritableRaster( width , height );
		final BufferedImage resultImage = new BufferedImage( colorModel , raster , premultiplied , null );

		// copy the source image into the temporary image
		final Graphics2D g = resultImage.createGraphics();
		g.setComposite( AlphaComposite.Src );
		g.drawImage( image , 0 , 0 , null );
		g.dispose();

		return wrapImageDataBuffer( resultImage );
	}

	/**
	 * Copied from TextureData in JOGL 1.1.1.
	 *
	 * @noinspection JavaDoc
	 */
	private static boolean isPackedInt( final BufferedImage image )
	{
		final int imageType = image.getType();
		return ( imageType == BufferedImage.TYPE_INT_RGB ||
		         imageType == BufferedImage.TYPE_INT_BGR ||
		         imageType == BufferedImage.TYPE_INT_ARGB ||
		         imageType == BufferedImage.TYPE_INT_ARGB_PRE );
	}

	/**
	 * Sets common texture parameters for wrapping and mipmapping.
	 *
	 * @param   gl          OpenGL pipeline.
	 * @param   texture     Texture to set parameters for.
	 */
	private static void setTextureParameters( @NotNull final GL gl , @NotNull final Texture texture )
	{
		texture.setTexParameteri( GL.GL_TEXTURE_WRAP_S , GL.GL_REPEAT );
		texture.setTexParameteri( GL.GL_TEXTURE_WRAP_T , GL.GL_REPEAT );

		if ( hasAutoMipMapGenerationSupport( gl ) )
		{
			try
			{
				/**
				 * Set generate mipmaps to true, this greatly increases performance and viewing pleasure in big scenes.
				 * @TODO need to find out if generated mipmaps are faster or if pregenerated mipmaps are faster
				 */
				texture.setTexParameteri( GL.GL_GENERATE_MIPMAP , GL.GL_TRUE );

				/** Set texture magnification to GL_LINEAR to support mipmaps. */
				texture.setTexParameteri( GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR );

				/** Set texture minification to GL_LINEAR_MIPMAP_NEAREST to support mipmaps. */
				texture.setTexParameteri( GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR_MIPMAP_NEAREST );
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

	@Override
	public String toString()
	{
		return super.toString() + "[" + _name + "]";
	}
}
