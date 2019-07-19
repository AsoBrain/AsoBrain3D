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

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.concurrent.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
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
	 * Creates texture data from the given buffered image, if not {@code null}. The
	 * image is automatically converted to a compatible size using {@link
	 * #createCompatibleTextureImage(BufferedImage)}.
	 *
	 * @param image Texture image.
	 *
	 * @return Compatible texture data.
	 */
	@Nullable
	protected TextureData createTextureData( @Nullable final BufferedImage image )
	{
		TextureData result = null;
		if ( image != null )
		{
			final BufferedImage compatibleImage = createCompatibleTextureImage( image );
			result = AWTTextureIO.newTextureData( GLProfile.get( GLProfile.GL2 ), compatibleImage, true );
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
	 * Sets common texture parameters for wrapping and mipmapping.
	 *
	 * @param   gl          OpenGL pipeline.
	 * @param   texture     Texture to set parameters for.
	 */
	private static void setTextureParameters( @NotNull final GL gl, @NotNull final Texture texture )
	{
		texture.setTexParameteri( gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
		texture.setTexParameteri( gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );

		if ( hasAutoMipMapGenerationSupport( gl ) )
		{
			try
			{
				/**
				 * Generate mip maps to avoid 'noise' on far-away textures.
				 */
				texture.setTexParameteri( gl, GL2.GL_GENERATE_MIPMAP, GL.GL_TRUE );

				/*
				 * Use linear texture filtering.
				 */
				texture.setTexParameteri( gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
				texture.setTexParameteri( gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_NEAREST );
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
