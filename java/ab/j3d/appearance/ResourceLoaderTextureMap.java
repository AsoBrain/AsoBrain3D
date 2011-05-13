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
import javax.imageio.*;

import ab.j3d.loader.*;
import com.numdata.oss.*;
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
	private ResourceLoader _resourceLoader;

	/**
	 * Identifies the texture image.
	 */
	@NotNull
	private String _name;

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

	@Override
	public BufferedImage getImage( final boolean useCache )
	{
		BufferedImage result = null;

		final ResourceLoader resourceLoader = _resourceLoader;
		try
		{
			final InputStream inputStream = resourceLoader.getResource( _name );
			result = ImageIO.read( inputStream );
		}
		catch ( IOException e )
		{
			/* Silently ignored. */
		}

		return result;
	}

	@Override
	public boolean equals( final Object other )
	{
		final boolean result;
		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof ResourceLoaderTextureMap )
		{
			final ResourceLoaderTextureMap map = (ResourceLoaderTextureMap)other;
			result = super.equals( other ) &&
			         _resourceLoader.equals( map._resourceLoader ) &&
			         TextTools.equals( _name, map._name );
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
