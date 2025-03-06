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
import TessellationPrimitive from './TessellationPrimitive.js';

/**
 * A quad list is a series of independent quads. Vertices 0, 1, 2, 3 define
 * the first quad; vertices 4, 5, 6, 7 define the second quad; then 8, 9,
 * 10, 11, and so on.
 *
 * @author  Peter S. Heijnen
 */
export default class QuadList implements TessellationPrimitive
{
	/**
	 * Empty triangle array.
	 */
	static NO_TRIANGLES: number[] = [];

	/**
	 * Vertices that define the quad strip.
	 */
	_vertices: number[];

	/**
	 * Cached triangles.
	 */
	_triangles: number[] | null;

	/**
	 * Construct quad strip.
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
			if ( vertexCount < 4 )
			{
				result = QuadList.NO_TRIANGLES;
			}
			else
			{
				/*
				 * result[ 0 ] = { vertices[0], vertices[1], vertices[2] }
				 * result[ 1 ] = { vertices[0], vertices[2], vertices[3] }
				 * result[ 2 ] = { vertices[4], vertices[5], vertices[6] }
				 * result[ 3 ] = { vertices[4], vertices[6], vertices[7] }
				 * result[ 4 ] = { vertices[8], vertices[9], vertices[10] }
				*/
				const resultLength = ( vertexCount / 4 ) * 6;
				result = new Array( resultLength );
				let resultIndex = 0;
				let vertexIndex = 0;
				while ( resultIndex < resultLength )
				{
					const v0 = vertices[ vertexIndex++ ];
					const v1 = vertices[ vertexIndex++ ];
					const v2 = vertices[ vertexIndex++ ];
					const v3 = vertices[ vertexIndex++ ];
					result[ resultIndex++ ] = v0;
					result[ resultIndex++ ] = v1;
					result[ resultIndex++ ] = v2;
					result[ resultIndex++ ] = v0;
					result[ resultIndex++ ] = v2;
					result[ resultIndex++ ] = v3;
				}
			}
			this._triangles = result;
		}
		return result;
	}
}
