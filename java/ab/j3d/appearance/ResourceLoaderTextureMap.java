/*
 * $Id$
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

import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;

import ab.j3d.loader.*;
import org.jetbrains.annotations.*;

/**
 * Texture map that uses the resource loader to retrieve its image.
 *
 * @author G. Meinders
 * @version $Revision$
 */
public class ResourceLoaderTextureMap
	extends AbstractTextureMap
{
	/**
	 * Resource loaded used to retrieve the texture image.
	 */
	@NotNull
	private final ResourceLoader _resourceLoader;

	/**
	 * Identifies the texture image.
	 */
	@NotNull
	private final String _name;

	/**
	 * Constructs a new texture map.
	 *
	 * @param   resourceLoader  Resource loader.
	 * @param   name            Name of the texture image resource.
	 */
	public ResourceLoaderTextureMap( @NotNull final ResourceLoader resourceLoader, @NotNull final String name )
	{
		_resourceLoader = resourceLoader;
		_name = name;
	}

	/**
	 * Constructs a new texture map.
	 *
	 * @param   resourceLoader  Resource loader.
	 * @param   name            Name of the texture image resource.
	 * @param   physicalWidth   Physical width of the texture, in meters.
	 * @param   physicalHeight  Physical height of the texture, in meters.
	 */
	public ResourceLoaderTextureMap( @NotNull final ResourceLoader resourceLoader, @NotNull final String name, final float physicalWidth, final float physicalHeight )
	{
		super( physicalWidth, physicalHeight );
		_resourceLoader = resourceLoader;
		_name = name;
	}

	public URL getImageUrl()
	{
		return _resourceLoader.getResource( _name );
	}

	public BufferedImage loadImage()
		throws IOException
	{
		BufferedImage result = null;

		final InputStream in = _resourceLoader.getResourceAsStream( _name );
		if ( in != null )
		{
			try
			{
				result = ImageIO.read( in );
				return result;
			}
			finally
			{
				in.close();
			}
		}

		return result;
	}

	@Override
	public boolean equals( final Object object )
	{
		final boolean result;
		if ( object == this )
		{
			result = true;
		}
		else if ( object instanceof ResourceLoaderTextureMap )
		{
			final ResourceLoaderTextureMap other = (ResourceLoaderTextureMap)object;
			result = super.equals( object ) &&
			         _resourceLoader.equals( other._resourceLoader ) &&
			         _name.equals( other._name );
		}
		else
		{
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return _name.hashCode() ^ super.hashCode();
	}
}
