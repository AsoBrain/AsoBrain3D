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

import java.awt.*;
import java.awt.color.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.concurrent.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import com.sun.opengl.util.texture.*;
import org.jetbrains.annotations.*;

/**
 * Provides access to a {@link Texture} in such a way that it can be loaded
 * asynchronously. Typical usage involves submitting an object of this class to
 * an {@link ExecutorService} and passing the resulting future back using
 * {@link #setTextureData(Future)}.
 *
 * @author  G. Meinders
 */
public class TextureProxy
	implements Callable<TextureData>
{
	/**
	 * Name of the texture image.
	 */
	protected final TextureMap _textureMap;

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
	 */
	public TextureProxy( @NotNull final Texture texture )
	{
		_textureMap = null;
		_textureCache = null;
		_textureData = null;
		_texture = texture;
	}

	/**
	 * Construct new texture proxy.
	 *
	 * @param   textureMap      Texture map.
	 * @param   textureCache    Texture cache.
	 */
	public TextureProxy( @NotNull final TextureMap textureMap, final TextureCache textureCache )
	{
		_textureMap = textureMap;
		_textureCache = textureCache;
		_textureData = null;
		_texture = null;
	}

	/**
	 * Construct new texture proxy.
	 *
	 * @param   textureCache    Texture cache.
	 */
	protected TextureProxy( final TextureCache textureCache )
	{
		_textureMap = null;
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
					_textureData = null;
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
		setTextureParameters( gl, result );

		return result;
	}

	/**
	 * Returns the texture data, if available.
	 *
	 * @return  Texture data.
	 */
	@Nullable
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
				System.err.println( "getTextureData( " + _textureMap.getName() + " ) => " + e );
			}
			catch ( ExecutionException e )
			{
				System.err.println( "getTextureData( " + _textureMap.getName() + " ) => " + e.getCause() );
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
	@Nullable
	public TextureData call()
		throws IOException
	{
		return createTextureData( _textureCache.loadImage( _textureMap ) );
	}

	/**
	 * Creates texture data from the given buffered image, if not
	 * <code>null</code>. The image is automatically converted to a compatible
	 * format using {@link #createCompatibleTextureImage(BufferedImage)}.
	 *
	 * @param   image   Texture image.
	 *
	 * @return  Compatible texture data.
	 */
	@Nullable
	protected TextureData createTextureData( @Nullable final BufferedImage image )
	{
		TextureData result = null;
		if ( image != null )
		{
			final BufferedImage compatibleImage = createCompatibleTextureImage( image );
			result = createTextureDataFromCompatibleImage( compatibleImage );
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
		final BufferedImage result;

		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();

		final TextureCache textureCache = _textureCache;
		int scaledWidth  = Math.min( textureCache.getMaximumTextureSize(), imageWidth );
		int scaledHeight = Math.min( textureCache.getMaximumTextureSize(), imageHeight );

		/*
		 * Texture sizes may need to be powers of two.
		 */
		if ( !textureCache.isNonPowerOfTwo() )
		{
			scaledWidth  = MathTools.nearestPowerOfTwo( scaledWidth );
			scaledHeight = MathTools.nearestPowerOfTwo( scaledHeight );
		}

		if ( ( imageWidth == scaledWidth ) && ( imageHeight == scaledHeight ) )
		{
			result = image;
		}
		else
		{
			result = createScaledInstance( image, scaledWidth, scaledHeight );
		}

		return result;
	}

	/**
	 * Returns a scaled instance of the given image. When the image size is
	 * reduced, a multi-step bilinear scaling method is used. This method avoids
	 * glitches that appear when using traditional bilinear or bicubic when an
	 * image is scaled to less than 50% its original size. When the image size
	 * is increased, bicubic interpolation is used instead.
	 *
	 * <p>Based on code from an article published at 'java.net':
	 * <a href="http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html">
	 * The Perils of Image.getScaledInstance()</a>.
	 *
	 * @param   source          Image to be scaled.
	 * @param   targetWidth     Width of the result, in pixels.
	 * @param   targetHeight    Height of the result, in pixels.
	 *
	 * @return  Scaled version of the given image. If the source image already
	 *          had the specified target size, the source image is returned.
	 */
	public static BufferedImage createScaledInstance( final BufferedImage source, final int targetWidth, final int targetHeight )
	{
		/*
		 * Determine appropriate image type.
		 */
		final ColorModel sourceColorModel = source.getColorModel();
		final int scaledType = sourceColorModel.hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

		int currentWidth  = source.getWidth();
		int currentHeight = source.getHeight();

		/*
		 * Choose interpolation method.
		 */
		final Object interpolation;
		if ( ( targetWidth < currentWidth ) || ( targetHeight < currentHeight ) )
		{
			interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		}
		else
		{
			interpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		}

		/*
		 * Perform scaling, using multiple steps when reducing to less than 50%.
		 */
		BufferedImage result = source;

		while ( ( targetWidth != currentWidth ) || ( targetHeight != currentHeight ) )
		{
			currentWidth = Math.max( currentWidth / 2, targetWidth );
			currentHeight = Math.max( currentHeight / 2, targetHeight );

			/*
			 * Use specified target size instead of the aspect-correct size in
			 * the final scaling step.
			 */
			final boolean finalStep = ( currentWidth == targetWidth ) && ( currentHeight == targetHeight );
			final int scaledCanvasWidth = finalStep ? targetWidth : currentWidth;
			final int scaledCanvasHeight = finalStep ? targetHeight : currentHeight;

			/*
			 * Perform the scaling step.
			 */
			final BufferedImage scaledImage = new BufferedImage( scaledCanvasWidth, scaledCanvasHeight, scaledType );
			final Graphics2D g2 = scaledImage.createGraphics();
			g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, interpolation );
			g2.drawImage( result, 0, 0, scaledCanvasWidth, scaledCanvasHeight, null );
			g2.dispose();
			result = scaledImage;
		}

		return result;
	}

	/**
	 * Copied from TextureData in JOGL 1.1.1, with some simplifications.
	 *
	 * @noinspection JavaDoc
	 */
	protected TextureData createTextureDataFromCompatibleImage( final BufferedImage image )
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

		return new TextureData( internalFormat, width, height, 0, pixelFormat, pixelType, true, false, true, buffer, null );
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
				colorModel = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ), new int[] { 8, 8, 8, 8 }, true, true, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE );
			}
			else
			{
				colorModel = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ), new int[] { 8, 8, 8, 0 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE );
			}
		}
		else
		{
			if ( hasAlpha )
			{
				colorModel = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ), null, true, true, Transparency.TRANSLUCENT, dataBufferType );
			}
			else
			{
				colorModel = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ), null, false, false, Transparency.OPAQUE, dataBufferType );
			}
		}

		final boolean premultiplied = colorModel.isAlphaPremultiplied();
		final WritableRaster raster = colorModel.createCompatibleWritableRaster( width, height );
		final BufferedImage resultImage = new BufferedImage( colorModel, raster, premultiplied, null );

		// copy the source image into the temporary image
		final Graphics2D g = resultImage.createGraphics();
		g.setComposite( AlphaComposite.Src );
		g.drawImage( image, 0, 0, null );
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
	private static void setTextureParameters( @NotNull final GL gl, @NotNull final Texture texture )
	{
		texture.setTexParameteri( GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
		texture.setTexParameteri( GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );

		if ( hasAutoMipMapGenerationSupport( gl ) )
		{
			try
			{
				/**
				 * Generate mip maps to avoid 'noise' on far-away textures.
				 */
				texture.setTexParameteri( GL.GL_GENERATE_MIPMAP, GL.GL_TRUE );

				/*
				 * Use linear texture filtering.
				 */
				texture.setTexParameteri( GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
				texture.setTexParameteri( GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_NEAREST );
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
		return super.toString() + "[textureMap=" + _textureMap + "]";
	}
}
