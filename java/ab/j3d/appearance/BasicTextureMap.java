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
 * Basic implementation of the {@link TextureMap} interface.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class BasicTextureMap
	extends AbstractTextureMap
{
	/**
	 * Texture map image.
	 */
	private BufferedImage _image;

	/**
	 * Construct map without image. An image must be set for this texure map
	 * to be useful.
	 */
	public BasicTextureMap()
	{
		this( null );
	}

	/**
	 * Construct map for given image and undeterminate physical dimensions.
	 *
	 * @param   image   Texture map image.
	 */
	public BasicTextureMap( @Nullable final BufferedImage image )
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
	public BasicTextureMap( @Nullable final BufferedImage image, final float physicalWidth, final float physicalHeight )
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
	public void setImage( final BufferedImage image )
	{
		_image = image;
	}
}
