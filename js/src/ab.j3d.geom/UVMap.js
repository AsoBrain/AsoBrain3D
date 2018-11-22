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
import Vector3D from '../ab.j3d/Vector3D';

import UVGenerator from './UVGenerator';

/**
 * Defines a mapping from spatial coordinates to texture coordinates.
 *
 * @author Gerrit Meinders
 * @interface
 */
export default class UVMap
{
	/**
	 * Get generator for 2D points on texture for the given 3D plane.
	 *
	 * @param {?TextureMap} textureMap Specifies texture scale.
	 * @param {!Vector3D} normal Normal of face to map texture on.
	 * @param {boolean} flipTexture Flip texture direction.
	 *
	 * @return {UVGenerator} Generator for U/V-coordinates.
	 */
	getGenerator( textureMap, normal, flipTexture ) // eslint-disable-line no-unused-vars
	{
	}

	/**
	 * Applies to UV map to the given buffer geometry, overwriting existing
	 * texture coordinates for the color map.
	 *
	 * @param {THREE.BufferGeometry} geometry Geometry to update.
	 * @param {Appearance} appearance Provides color map information.
	 * @param {boolean} flipTexture Flip texture direction.
	 */
	applyToBufferGeometry( geometry, appearance, flipTexture = false )
	{
		const positions = geometry.attributes.position;
		const normals = geometry.attributes.normal;
		const uvs = geometry.attributes.uv;

		const vertexCount = positions.array.length / positions.itemSize;

		for ( let i = 0; i < vertexCount; i++ )
		{
			const normal = new Vector3D( normals.getX( i ), normals.getY( i ), normals.getZ( i ) );
			const generator = UVGenerator.getColorMapInstance( appearance, this, normal, flipTexture );
			generator.generate( positions.getX( i ), positions.getY( i ), positions.getZ( i ) );
			uvs.setXY( generator.u, generator.v );
		}
		uvs.needsUpdate = true;
	}
}
