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
package ab.j3d.appearance;

/**
 * Basic implementation of a texture map.
 *
 * @author Gerrit Meinders
 */
public class BasicTextureMap
extends AbstractTextureMap
{
	/**
	 * Constructs a new instance.
	 *
	 * @param name Name of the texture.
	 */
	public BasicTextureMap( final String name )
	{
		super( name, 0, 0 );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param name           Name of the texture.
	 * @param physicalWidth  Physical width in meters (<code>0.0</code> if
	 *                       unknown).
	 * @param physicalHeight Physical height in meters (<code>0.0</code> if
	 *                       unknown).
	 */
	public BasicTextureMap( final String name, final float physicalWidth, final float physicalHeight )
	{
		super( name, physicalWidth, physicalHeight );
	}
}
