/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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

import { Appearance } from '@numdata/common';

import Vector3D from '../ab.j3d/Vector3D';

import UVMap from './UVMap';

/**
 * This generator can be requested from {@link UVMap} to generate a series of
 * U/V coordinates.
 *
 * @author Peter S. Heijnen
 */
export default class UVGenerator
{
	/**
	 * A generator that always produces zeros.
	 * @type UVGenerator
	 * @private
	 */
	static ZERO_GENERATOR = new UVGenerator();

	/**
	 * Get generator for 2D points on color map texture for the given appearance.
	 *
	 * @param appearance Appearance to get color map from.
	 * @param uvMap UV-map to use.
	 * @param normal Normal of face to map texture on.
	 * @param flipTexture Flip texture direction.
	 *
	 * @return Generator for U/V-coordinates.
	 */
	static getColorMapInstance( appearance: Appearance, uvMap: UVMap, normal: Vector3D, flipTexture: boolean ): UVGenerator
	{
		let result = UVGenerator.ZERO_GENERATOR;

		if ( appearance && uvMap )
		{
			const colorMap = appearance.colorMap;
			if ( colorMap )
			{
				result = uvMap.getGenerator( colorMap, normal, flipTexture );
			}
		}

		return result;
	}

	/**
	 * Last generated U-coordinate.
	 */
	u: number = 0;

	/**
	 * Last generated V-coordinate.
	 */
	v: number = 0;

	/**
	 * Generate U/V coordinate for the given 3D point. The generated U/V
	 * coordinates can be retrieved using the getter methods of this class.
	 *
	 * @param x X coordinate of 3D point to generate U/V coordinates for.
	 * @param y Y coordinate of 3D point to generate U/V coordinates for.
	 * @param z Z coordinate of 3D point to generate U/V coordinates for.
	 */
	generate( x: number, y: number, z: number ): void
	{
	}
}
