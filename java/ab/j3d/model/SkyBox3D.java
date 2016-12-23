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
package ab.j3d.model;

import ab.j3d.*;
import ab.j3d.appearance.*;

/**
 * Defines a sky box, specifying the appearances to be used for creating an
 * infinitely distant background for a 3D scene.
 *
 * @author G. Meinders
 */
public class SkyBox3D
extends Node3D
{
	/**
	 * Appearance for the north side of the sky box.
	 */
	private final Appearance _north;

	/**
	 * Appearance for the east side of the sky box.
	 */
	private final Appearance _east;

	/**
	 * Appearance for the south side of the sky box.
	 */
	private final Appearance _south;

	/**
	 * Appearance for the west side of the sky box.
	 */
	private final Appearance _west;

	/**
	 * Appearance for the ceiling of the sky box.
	 */
	private final Appearance _ceiling;

	/**
	 * Appearance for the floor of the sky box.
	 */
	private final Appearance _floor;

	/**
	 * Construct new sky box.
	 *
	 * @param name Name of the texture. The name is suffixed with "-north",
	 *             "-east", "-south", "-west", "-ceiling" and "-floor" to get
	 *             the names of the color maps on each side of the sky box.
	 */
	public SkyBox3D( final String name )
	{
		this( createAppearance( name + "-north" ),
		      createAppearance( name + "-east" ),
		      createAppearance( name + "-south" ),
		      createAppearance( name + "-west" ),
		      createAppearance( name + "-ceiling" ),
		      createAppearance( name + "-floor" ) );
	}

	/**
	 * Helper-method to create an {@link Appearance} based on an URL.
	 *
	 * @param name Name of the texture. The name is suffixed with "-north",
	 *             "-east", "-south", "-west", "-ceiling" and "-floor" to get
	 *             the names of the color maps on each side of the sky box.
	 *
	 * @return {@link Appearance}.
	 */
	private static Appearance createAppearance( final String name )
	{
		final BasicAppearance result = new BasicAppearance( name );
		result.setAmbientColor( Color4.WHITE );
		result.setColorMap( new BasicTextureMap( name + ".jpg" ) );
		return result;
	}

	/**
	 * Construct new sky box.
	 *
	 * @param north   Appearance to be used for the north side of the sky box.
	 * @param east    Appearance to be used for the east side of the sky box.
	 * @param south   Appearance to be used for the south side of the sky box.
	 * @param west    Appearance to be used for the west side of the sky box.
	 * @param ceiling Appearance to be used for the ceiling of the sky box.
	 * @param floor   Appearance to be used for the floor of the sky box.
	 */
	public SkyBox3D( final Appearance north, final Appearance east, final Appearance south, final Appearance west, final Appearance ceiling, final Appearance floor )
	{
		_north = north;
		_east = east;
		_south = south;
		_west = west;
		_ceiling = ceiling;
		_floor = floor;
	}

	/**
	 * Returns the appearance of the north side of the sky box.
	 *
	 * @return Appearance of the north side.
	 */
	public Appearance getNorth()
	{
		return _north;
	}

	/**
	 * Returns the appearance of the east side of the sky box.
	 *
	 * @return Appearance of the east side.
	 */
	public Appearance getEast()
	{
		return _east;
	}

	/**
	 * Returns the appearance of the south side of the sky box.
	 *
	 * @return Appearance of the south side.
	 */
	public Appearance getSouth()
	{
		return _south;
	}

	/**
	 * Returns the appearance of the west side of the sky box.
	 *
	 * @return Appearance of the west side.
	 */
	public Appearance getWest()
	{
		return _west;
	}

	/**
	 * Returns the appearance of the ceiling of the sky box.
	 *
	 * @return Appearance of the ceiling.
	 */
	public Appearance getCeiling()
	{
		return _ceiling;
	}

	/**
	 * Returns the appearance of the floor of the sky box.
	 *
	 * @return Appearance of the floor.
	 */
	public Appearance getFloor()
	{
		return _floor;
	}
}
