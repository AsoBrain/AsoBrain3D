/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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
 * ====================================================================
 */
package ab.j3d.model;

import ab.j3d.Material;

/**
 * Defines a sky box, specifying the materials to be used for creating an
 * infinitely distant background for a 3D scene.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class SkyBox3D
	extends Node3D
{
	/** Material for the north side of the sky box. */
	private final Material _north;

	/** Material for the east side of the sky box. */
	private final Material _east;

	/** Material for the south side of the sky box. */
	private final Material _south;

	/** Material for the west side of the sky box. */
	private final Material _west;

	/** Material for the ceiling of the sky box. */
	private final Material _ceiling;

	/** Material for the floor of the sky box. */
	private final Material _floor;

	/**
	 * Construct new sky box.
	 *
	 * @param   name    Name of the sky box. This name is suffixed with "-north",
	 *                  "-east", "-south", "-west", "-ceiling" and "-floor" to
	 *                  for the names of the color maps on each side of the sky
	 *                  box.
	 */
	public SkyBox3D( final String name )
	{
		this( new Material( name + "-north"   , 1.0f , 1.0f , 1.0f , 0.0f , 0.0f , 0.0f , 1.0f , 0.0f , 0.0f , 0.0f , 0 , 0.0f , 0.0f , 0.0f , name + "-north"   , 0.0f , 0.0f , false ) ,
		      new Material( name + "-east"    , 1.0f , 1.0f , 1.0f , 0.0f , 0.0f , 0.0f , 1.0f , 0.0f , 0.0f , 0.0f , 0 , 0.0f , 0.0f , 0.0f , name + "-east"    , 0.0f , 0.0f , false ) ,
		      new Material( name + "-south"   , 1.0f , 1.0f , 1.0f , 0.0f , 0.0f , 0.0f , 1.0f , 0.0f , 0.0f , 0.0f , 0 , 0.0f , 0.0f , 0.0f , name + "-south"   , 0.0f , 0.0f , false ) ,
		      new Material( name + "-west"    , 1.0f , 1.0f , 1.0f , 0.0f , 0.0f , 0.0f , 1.0f , 0.0f , 0.0f , 0.0f , 0 , 0.0f , 0.0f , 0.0f , name + "-west"    , 0.0f , 0.0f , false ) ,
		      new Material( name + "-ceiling" , 1.0f , 1.0f , 1.0f , 0.0f , 0.0f , 0.0f , 1.0f , 0.0f , 0.0f , 0.0f , 0 , 0.0f , 0.0f , 0.0f , name + "-ceiling" , 0.0f , 0.0f , false ) ,
		      new Material( name + "-floor"   , 1.0f , 1.0f , 1.0f , 0.0f , 0.0f , 0.0f , 1.0f , 0.0f , 0.0f , 0.0f , 0 , 0.0f , 0.0f , 0.0f , name + "-floor"   , 0.0f , 0.0f , false ) );
	}

	/**
	 * Construct new sky box.
	 *
	 * @param   north       Material to be used for the north side of the sky box.
	 * @param   east        Material to be used for the east side of the sky box.
	 * @param   south       Material to be used for the south side of the sky box.
	 * @param   west        Material to be used for the west side of the sky box.
	 * @param   ceiling     Material to be used for the ceiling of the sky box.
	 * @param   floor       Material to be used for the floor of the sky box.
	 */
	public SkyBox3D( final Material north , final Material east , final Material south , final Material west , final Material ceiling , final Material floor )
	{
		_north   = north;
		_east    = east;
		_south   = south;
		_west    = west;
		_ceiling = ceiling;
		_floor   = floor;
	}

	/**
	 * Returns the material of the north side of the sky box.
	 *
	 * @return  Material of the north side.
	 */
	public Material getNorth()
	{
		return _north;
	}

	/**
	 * Returns the material of the east side of the sky box.
	 *
	 * @return  Material of the east side.
	 */
	public Material getEast()
	{
		return _east;
	}

	/**
	 * Returns the material of the south side of the sky box.
	 *
	 * @return  Material of the south side.
	 */
	public Material getSouth()
	{
		return _south;
	}

	/**
	 * Returns the material of the west side of the sky box.
	 *
	 * @return  Material of the west side.
	 */
	public Material getWest()
	{
		return _west;
	}

	/**
	 * Returns the material of the ceiling of the sky box.
	 *
	 * @return  Material of the ceiling.
	 */
	public Material getCeiling()
	{
		return _ceiling;
	}

	/**
	 * Returns the material of the floor of the sky box.
	 *
	 * @return  Material of the floor.
	 */
	public Material getFloor()
	{
		return _floor;
	}
}
