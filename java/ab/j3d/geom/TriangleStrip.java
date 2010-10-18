/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
package ab.j3d.geom;

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * A triangle strip is a series of connected triangles. Vertices 0, 1, 2 define
 * the first triangle; vertices 2, 1, 3 define the second triangle; then 2, 3,
 * 4, and so on. Notice the alternating orientation to correctly form part of a
 * surface.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TriangleStrip
	implements TessellationPrimitive
{
	/**
	 * Empty triangle array.
	 */
	private static final int[] NO_TRIANGLES = new int[ 0 ];

	/**
	 * Vertices that define the triangle strip.
	 */
	@NotNull
	protected final int[] _vertices;

	/**
	 * Cached triangles.
	 */
	private int[] _triangles;

	/**
	 * Construct triangle strip.
	 *
	 * @param   vertices    Vertices that define the strip.
	 */
	public TriangleStrip( @NotNull final int[] vertices )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_vertices = vertices;
		_triangles = null;
	}

	@Override
	@NotNull
	public int[] getVertices()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return _vertices;
	}

	@NotNull
	@Override
	public int[] getTriangles()
	{
		int[] result = _triangles;
		if ( result == null )
		{
			final int[] vertices = _vertices;
			final int vertexCount = vertices.length;
			if ( vertexCount < 3 )
			{
				result = NO_TRIANGLES;
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
				int v0 = vertices[ 0 ];
				int v1 = vertices[ 1 ];
				int v2;

				result = new int[ ( vertexCount - 2 ) * 3 ];
				int resultIndex = 0;
				boolean flip = false;

				for ( int vertexIndex = 2; vertexIndex < vertexCount; )
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

			_triangles = result;
		}

		return result;
	}

	@Override
	public String toString()
	{
		return super.toString() + "{vertices=" + Arrays.toString( _vertices ) + '}';
	}
}