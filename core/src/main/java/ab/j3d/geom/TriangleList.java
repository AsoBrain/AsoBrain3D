/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2018 Peter S. Heijnen
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
package ab.j3d.geom;

import java.util.*;

/**
 * List of independent triangles. Vertices 0, 1, 2 define the first triangle;
 * vertices 3, 4, 5 define the second triangle; then 6, 7, 8, and so on.
 *
 * @author Peter S. Heijnen
 */
public class TriangleList
	implements TessellationPrimitive
{
	/**
	 * Vertices that define the triangles.
	 */
	protected final int[] _vertices;

	/**
	 * Construct triangle list.
	 *
	 * @param   vertices    Vertices that define the triangles.
	 */
	public TriangleList( final int[] vertices )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_vertices = vertices;
	}

	public int[] getVertices()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return _vertices;
	}

	public int[] getTriangles()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return _vertices;
	}

	@Override
	public String toString()
	{
		return super.toString() + "{vertices=" + Arrays.toString( _vertices ) + '}';
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( _vertices );
	}

	@Override
	public boolean equals( final Object obj )
	{
		final boolean result;
		if ( obj == this )
		{
			result = true;
		}
		else if ( obj instanceof TriangleList )
		{
			final TriangleList other = (TriangleList)obj;
			result = Arrays.equals( _vertices, other._vertices );
		}
		else
		{
			result = false;
		}
		return result;
	}
}
