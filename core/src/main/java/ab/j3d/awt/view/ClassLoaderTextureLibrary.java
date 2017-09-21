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
import org.jetbrains.annotations.*;

/**
 * Texture library that loads textures using a {@link ClassLoader}.
 *
 * @author Gerrit Meinders
 */
public class ClassLoaderTextureLibrary
implements TextureLibrary
{
	/**
	 * Class loader used to resolve textures.
	 */
	private final ClassLoader _classLoader;

	/**
	 * Prefix added to texture names before resolving with the class loader.
	 */
	private final String _namePrefix;

	/**
	 * Suffix that is added to texture names if no texture is found.
	 */
	private String _optionalNameSuffix = ".jpg";

	/**
	 * Constructs a new instance using the system class loader.
	 */
	public ClassLoaderTextureLibrary()
	{
		this( ClassLoader.getSystemClassLoader() );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param classLoader Class loader used to resolve textures.
	 */
	public ClassLoaderTextureLibrary( @NotNull final ClassLoader classLoader )
	{
		_classLoader = classLoader;
		_namePrefix = null;
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param classLoader Class loader used to resolve textures.
	 * @param namePrefix  Prefix added to texture names before resolving with
	 *                    the class loader.
	 */
	public ClassLoaderTextureLibrary( @NotNull final ClassLoader classLoader, final String namePrefix )
	{
		_classLoader = classLoader;
		_namePrefix = namePrefix;
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
		final URL url = getUrl( textureMap );
		return url == null ? null : url.openStream();
	}

	@Nullable
	@Override
	public File getFile( @NotNull final TextureMap textureMap )
	{
		final URL url = getUrl( textureMap );
		try
		{
			return url == null || !"file".equals( url.getProtocol() ) ? null : new File( url.toURI() );
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
		String name = textureMap.getName();

		if ( _namePrefix != null )
		{
			name = _namePrefix + name;
		}

		URL result = _classLoader.getResource( name );

		if ( result == null )
		{
			if ( _optionalNameSuffix != null )
			{
				result = _classLoader.getResource( name + _optionalNameSuffix );
			}

			if ( result == null )
			{
				System.out.println( "Texture not found (using class loader): " + name );
			}
		}

		return result;
	}
}
