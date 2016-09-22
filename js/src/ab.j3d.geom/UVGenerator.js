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
	 * @param {?Appearance} appearance Appearance to get color map from.
	 * @param {?UVMap} uvMap UV-map to use.
	 * @param {!Vector3D} normal Normal of face to map texture on.
	 * @param {boolean} flipTexture Flip texture direction.
	 *
	 * @return {UVGenerator} Generator for U/V-coordinates.
	 */
	static getColorMapInstance( appearance, uvMap, normal, flipTexture )
	{
		let result = UVGenerator.ZERO_GENERATOR;

		if ( appearance && uvMap )
		{
			const colorMap = appearance.getColorMap();
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
	u;

	/**
	 * Last generated V-coordinate.
	 */
	v;

	constructor()
	{
		this.u = 0;
		this.v = 0;
	}

	/**
	 * Generate U/V coordinate for the given 3D point. The generated U/V
	 * coordinates can be retrieved using the getter methods of this class.
	 *
	 * @param {number} x X coordinate of 3D point to generate U/V coordinates for.
	 * @param {number} y Y coordinate of 3D point to generate U/V coordinates for.
	 * @param {number} z Z coordinate of 3D point to generate U/V coordinates for.
	 */
	generate( x, y, z )
	{
	}
}
