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
package ab.j3d.awt.view;

import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;

import ab.j3d.appearance.*;
import ab.j3d.loader.*;
import org.jetbrains.annotations.*;

/**
 * Texture library based on a {@link ResourceLoader}.
 *
 * @author Gerrit Meinders
 */
public class ResourceLoaderTextureLibrary
implements TextureLibrary
{
	/**
	 * Resource loader.
	 */
	private final ResourceLoader _resourceLoader;

	/**
	 * Constructs a new instance.
	 *
	 * @param resourceLoader Resource loader.
	 */
	public ResourceLoaderTextureLibrary( final ResourceLoader resourceLoader )
	{
		_resourceLoader = resourceLoader;
	}

	@Nullable
	@Override
	public BufferedImage loadImage( @NotNull final TextureMap textureMap )
	throws IOException
	{
		final URL url = getUrl( textureMap );
		return url == null ? null : ImageIO.read( url );
	}

	@Nullable
	@Override
	public InputStream openImageStream( @NotNull final TextureMap textureMap )
	throws IOException
	{
		return _resourceLoader.getResourceAsStream( textureMap.getName() );
	}

	@Nullable
	@Override
	public File getFile( @NotNull final TextureMap textureMap )
	{
		final URL url = getUrl( textureMap );
		try
		{
			return ( url != null ) && "file".equals( url.getProtocol() ) ? new File( url.toURI() ) : null;
		}
		catch ( URISyntaxException e )
		{
			throw new IllegalArgumentException( "Failed to convert file URL to File object", e );
		}
	}

	@Nullable
	@Override
	public URL getUrl( @NotNull final TextureMap textureMap )
	{
		return _resourceLoader.getResource( textureMap.getName() );
	}
}
