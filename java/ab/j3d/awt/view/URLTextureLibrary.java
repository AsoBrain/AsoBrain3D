/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2017 Peter S. Heijnen
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
package ab.j3d.awt.view;

import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;

import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * Texture library that loads textures by resolving the texture name with a
 * fixed base URL, the 'imagesUrl'.
 *
 * @author Gerrit Meinders
 */
public class URLTextureLibrary
implements TextureLibrary
{
	/**
	 * Base URL for resolving texture names to images.
	 */
	@NotNull
	private URL _imagesUrl;

	/**
	 * Constructs a new instance.
	 *
	 * @param imagesUrl Base URL for resolving texture names to images.
	 */
	public URLTextureLibrary( @NotNull final URL imagesUrl )
	{
		_imagesUrl = imagesUrl;
	}

	@Override
	@Nullable
	public BufferedImage loadImage( @NotNull final TextureMap textureMap )
	throws IOException
	{
		final BufferedImage result;

		final URL imageUrl = getImageUrl( _imagesUrl, textureMap.getName() );
		if ( imageUrl == null )
		{
			result = null;
		}
		else
		{
			try
			{
				result = ImageIO.read( imageUrl );
			}
			catch ( IOException e )
			{
				throw new IOException( "Failed to read texture image from url '" + imageUrl + '\'', e );
			}
		}

		return result;
	}

	@Nullable
	@Override
	public InputStream openImageStream( @NotNull final TextureMap textureMap )
	throws IOException
	{
		final InputStream result;

		final URL imageUrl = getImageUrl( _imagesUrl, textureMap.getName() );
		if ( imageUrl == null )
		{
			result = null;
		}
		else
		{
			try
			{
				result = imageUrl.openStream();
			}
			catch ( IOException e )
			{
				throw new IOException( "Failed to read texture image from url '" + imageUrl + '\'', e );
			}
		}

		return result;
	}

	/**
	 * Get URL for image.
	 *
	 * @param   baseUrl     URL to resolve relative paths against.
	 * @param   path        Image path.
	 *
	 * @return  URL to image;
	 *          <code>null</code> if the path was <code>null</code> or empty.
	 */
	@Nullable
	private static URL getImageUrl( @NotNull final URL baseUrl, @Nullable final String path )
	{
		final URL result;

		if ( ( path == null ) || path.isEmpty() )
		{
			result = null;
		}
		else
		{
			try
			{
				if ( path.contains( ":/" ) )
				{
					result = new URL( path );
				}
				else
				{
					final String filename = path.substring( path.lastIndexOf( '/' ) + 1 );
					final boolean noExtension = ( filename.indexOf( '.' ) < 0 );
					final String pathWithExtension = noExtension ? path + ".jpg" : path;

					result = new URL( baseUrl, pathWithExtension );
				}
			}
			catch ( MalformedURLException e )
			{
				throw new IllegalArgumentException( "Bad URL spec: " + path, e );
			}
		}

		return result;
	}

	@Nullable
	@Override
	public File getFile( @NotNull final TextureMap textureMap )
	{
		final URL imageUrl = getImageUrl( _imagesUrl, textureMap.getName() );
		try
		{
			return ( imageUrl != null ) && "file".equals( imageUrl.getProtocol() ) ? new File( imageUrl.toURI() ) : null;
		}
		catch ( URISyntaxException e )
		{
			throw new IllegalArgumentException( "Failed to convert file URL to File object", e );
		}
	}

	@Override
	public URL getUrl( @NotNull final TextureMap textureMap )
	{
		return getImageUrl( _imagesUrl, textureMap.getName() );
	}

	@NotNull
	public URL getImagesUrl()
	{
		return _imagesUrl;
	}
}
