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

import org.jetbrains.annotations.*;

/**
 * Texture map loaded from a local file.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class FileTextureMap
	extends AbstractTextureMap
{
	/**
	 * Image file URL.
	 */
	@NotNull
	private final URL _imageUrl;

	/**
	 * External form of image file URL. We need this, because we should not call
	 * {@link URL#equals(Object)} and {@link URL#hashCode()}, because they may
	 * perform DNS lookups and other heavyweight things.
	 */
	@NotNull
	private final String _imageUrlString;

	/**
	 * Construct texture map for image file.
	 *
	 * @param   imageFile   Image file.
	 */
	public FileTextureMap( @NotNull final File imageFile )
	{
		final URI uri = imageFile.toURI();
		try
		{
			final URL url = uri.toURL();
			_imageUrl = url;
			_imageUrlString = url.toExternalForm();
		}
		catch ( MalformedURLException e )
		{
			/* should never happen for file URI */
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Construct texture map for image file URL.
	 *
	 * @param   imageUrl    URL for image file.
	 */
	public FileTextureMap( @NotNull final URL imageUrl )
	{
		_imageUrl = imageUrl;
		_imageUrlString = imageUrl.toExternalForm();
	}

	/**
	 * Construct texture map for image file URL and given physical size.
	 *
	 * @param   imageUrl        URL for image file.
	 * @param   physicalWidth   Physical width in meters (<code>0.0</code> if
	 *                          indeterminate).
	 * @param   physicalHeight  Physical height in meters (<code>0.0</code> if
	 *                          indeterminate).
	 */
	public FileTextureMap( @NotNull final URL imageUrl, final float physicalWidth, final float physicalHeight )
	{
		super( physicalWidth, physicalHeight );
		_imageUrl = imageUrl;
		_imageUrlString = imageUrl.toExternalForm();
	}

	@NotNull
	public URL getImageUrl()
	{
		return _imageUrl;
	}

	public BufferedImage loadImage()
		throws IOException
	{
		try
		{
			return ImageIO.read( _imageUrl );
		}
		catch ( IOException e )
		{
			throw new IOException( "Failed to read texture image from url '" + _imageUrlString + '\'', e );
		}
	}

	@Override
	public boolean equals( final Object object )
	{
		final boolean result;
		if ( object == this )
		{
			result = true;
		}
		else if ( object instanceof FileTextureMap )
		{
			final FileTextureMap other = (FileTextureMap)object;
			result = super.equals( object ) && _imageUrlString.equals( other._imageUrlString );
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
		return super.hashCode() ^ _imageUrlString.hashCode();
	}
}
