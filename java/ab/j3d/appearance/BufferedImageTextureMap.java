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

import java.awt.image.*;

import org.jetbrains.annotations.*;

/**
 * Implementation of the {@link TextureMap} interface based on a buffered image.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class BufferedImageTextureMap
	extends AbstractTextureMap
{
	/**
	 * Texture map image.
	 */
	@Nullable
	private BufferedImage _image;

	/**
	 * Construct map without image. An image must be set for this texure map
	 * to be useful.
	 */
	public BufferedImageTextureMap()
	{
		this( null );
	}

	/**
	 * Construct map for given image and undeterminate physical dimensions.
	 *
	 * @param   image   Texture map image.
	 */
	public BufferedImageTextureMap( @Nullable final BufferedImage image )
	{
		this( image, 0.0f, 0.0f );
	}

	/**
	 * Construct map for given image and physical dimensions.
	 *
	 * @param   image           Texture map image.
	 * @param   physicalWidth   Physical width in meters (<code>0.0</code> if
	 *                          undeterminate).
	 * @param   physicalHeight  Physical height in meters (<code>0.0</code> if
	 *                          undeterminate).
	 */
	public BufferedImageTextureMap( @Nullable final BufferedImage image, final float physicalWidth, final float physicalHeight )
	{
		super( physicalWidth, physicalHeight );
		_image = image;
	}

	@Override
	public BufferedImage getImage( final boolean useCache )
	{
		return _image;
	}

	/**
	 * Set map image.
	 *
	 * @param   image   Texture map image.
	 */
	public void setImage( @Nullable final BufferedImage image )
	{
		_image = image;
	}

	@Override
	public boolean equals( final Object other )
	{
		final boolean result;
		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof BufferedImageTextureMap )
		{
			final BufferedImageTextureMap map = (BufferedImageTextureMap)other;
			final BufferedImage image = _image;
			result = super.equals( other ) &&
			         ( ( image == null ) ? ( map._image == null ) : image.equals( map._image ) );
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
		return super.hashCode() ^ ( ( _image == null ) ? 0 : _image.hashCode() );
	}
}
