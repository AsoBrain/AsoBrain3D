/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2025 Peter S. Heijnen
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

import { BufferAttribute, BufferGeometry } from 'three';

import TextureMap from '../ab.j3d/TextureMap.js';
import Vector3D from '../ab.j3d/Vector3D.js';

import Appearance from './Appearance.js';
import UVGenerator from './UVGenerator.js';

/**
 * Defines a mapping from spatial coordinates to texture coordinates.
 *
 * @author Gerrit Meinders
 * @interface
 */
export default abstract class UVMap
{
	/**
	 * Get generator for 2D points on texture for the given 3D plane.
	 *
	 * @param textureMap Specifies texture scale.
	 * @param normal Normal of face to map texture on.
	 * @param flipTexture Flip texture direction.
	 *
	 * @return Generator for U/V-coordinates.
	 */
	abstract getGenerator( textureMap: TextureMap, normal: Vector3D, flipTexture: boolean ): UVGenerator;

	/**
	 * Applies to UV map to the given buffer geometry, overwriting existing
	 * texture coordinates for the color map.
	 *
	 * @param geometry Geometry to update.
	 * @param appearance Provides color map information.
	 * @param flipTexture Flip texture direction.
	 * @param start Start index.
	 * @param count Number of indices to process.
	 */
	applyToBufferGeometry( geometry: BufferGeometry, appearance: Appearance, flipTexture = false, start: number = 0, count: number = Infinity ): void
	{
		const positions = geometry.attributes.position;
		const normals = geometry.attributes.normal;
		let uvs = geometry.attributes.uv;
		if ( !uvs )
		{
			uvs = new BufferAttribute( new Float32Array( 2 * positions.count ), 2 );
			geometry.setAttribute( "uv", uvs );
		}

		const end = Math.min( positions.count, start + count );

		for ( let i = start; i < end; i++ )
		{
			const normal = new Vector3D( normals.getX( i ), normals.getY( i ), normals.getZ( i ) );
			const generator = UVGenerator.getColorMapInstance( appearance, this, normal, flipTexture );
			generator.generate( positions.getX( i ), positions.getY( i ), positions.getZ( i ) );
			uvs.setXY( i, generator.u, generator.v );
		}
		uvs.needsUpdate = true;
	}
}
