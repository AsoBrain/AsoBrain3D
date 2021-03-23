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
import TessellationPrimitive from './TessellationPrimitive';

/**
 * A triangle strip is a series of connected triangles. Vertices 0, 1, 2 define
 * the first triangle; vertices 2, 1, 3 define the second triangle; then 2, 3,
 * 4, and so on. Notice the alternating orientation to correctly form part of a
 * surface.
 *
 * @author  Peter S. Heijnen
 */
export default class TriangleStrip implements TessellationPrimitive
{
	/**
	 * Empty triangle array.
	 */
	static NO_TRIANGLES: number[] = [];

	/**
	 * Vertices that define the triangle strip.
	 */
	_vertices: number[];

	/**
	 * Cached triangles.
	 */
	_triangles: number[] | null;

	/**
	 * Construct triangle strip.
	 *
	 * @param vertices Vertices that define the strip.
	 */
	constructor( vertices: number[] )
	{
		this._vertices = vertices;
		this._triangles = null;
	}

	getVertices(): number[]
	{
		return this._vertices;
	}

	getTriangles(): number[]
	{
		let result = this._triangles;
		if ( !result )
		{
			const vertices = this._vertices;
			const vertexCount = vertices.length;
			if ( vertexCount < 3 )
			{
				result = TriangleStrip.NO_TRIANGLES;
			}
			else
			{
				/*
				 * result[ 0 ] = { vertices[0], vertices[1], vertices[2] }
				 * result[ 1 ] = { vertices[2], vertices[1], vertices[3] }
				 * result[ 2 ] = { vertices[2], vertices[3], vertices[4] }
				 * result[ 3 ] = { vertices[4], vertices[3], vertices[5] }
				 * result[ 4 ] = { vertices[4], vertices[5], vertices[6] }
				 */
				let v0 = vertices[ 0 ];
				let v1 = vertices[ 1 ];
				let v2;
				result = new Array( ( vertexCount - 2 ) * 3 );
				let resultIndex = 0;
				let flip = false;
				let vertexIndex = 2;
				while ( vertexIndex < vertexCount )
				{
					v2 = vertices[ vertexIndex++ ];
					result[ resultIndex++ ] = flip ? v1 : v0;
					result[ resultIndex++ ] = flip ? v0 : v1;
					result[ resultIndex++ ] = v2;
					v0 = v1;
					v1 = v2;
					flip = !flip;
				}
			}
			this._triangles = result;
		}
		return result;
	}
}
