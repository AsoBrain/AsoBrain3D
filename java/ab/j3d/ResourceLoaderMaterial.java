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
package ab.j3d;

import ab.j3d.appearance.*;
import ab.j3d.loader.*;

/**
 * Material which loads its texture from a {@link ResourceLoader}.
 *
 * @author  Wijnand Wieskamp
 * @version $Revision$
 */
public class ResourceLoaderMaterial
	extends Material
{
	/**
	 * {@link ResourceLoader} to use.
	 */
	private final ResourceLoader _resourceloader;

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -4730962090640159465L;

	/**
	 * Constructs a new {@link ResourceLoaderMaterial} with the given
	 * {@link ResourceLoader}.
	 *
	 * @param resourceLoader     {@link ResourceLoader} to use.
	 */
	public ResourceLoaderMaterial( final ResourceLoader resourceLoader )
	{
		_resourceloader = resourceLoader;
	}

	/**
	 * Construct {@link ResourceLoaderMaterial} for ARGB value.
	 *
	 * @param   argb    ARGB color specification.
	 *
	 * @see     java.awt.Color
	 */
	public ResourceLoaderMaterial( final int argb )
	{
		super( argb );
		_resourceloader = null;
	}

	@Override
	public TextureMap getColorMap()
	{
		final String colorMap = this.colorMap;
		return ( ( colorMap == null ) || colorMap.isEmpty() ) ? null : new ResourceLoaderTextureMap( _resourceloader, colorMap, colorMapWidth, colorMapHeight );
	}

	@Override
	public TextureMap getBumpMap()
	{
		final String bumpMap = this.bumpMap;
		return ( ( bumpMap == null ) || bumpMap.isEmpty() ) ? null : new ResourceLoaderTextureMap( _resourceloader, bumpMap, bumpMapWidth, bumpMapHeight );
	}
}
