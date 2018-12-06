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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Defines a vertex of a 3D object.
 *
 * @see Object3D
 * @see Face3D
 *
 * @author Peter S. Heijnen
 */
public class Vertex3D
{
	/**
	 * Coordinates of vertex.
	 */
	public Vector3D point;

	/**
	 * Index of the vertex in {@link Object3D#getVertexCoordinates()}.
	 */
	public int vertexCoordinateIndex;

	/**
	 * Color map U coordinate.
	 */
	public float colorMapU;

	/**
	 * Color map V coordinate.
	 */
	public float colorMapV;

	/**
	 * Vertex normal ({@code null} if undetermined).
	 */
	Vector3D normal;

	/**
	 * Construct vertex.
	 *
	 * @param   point                   Coordinates of vertex.
	 * @param   vertexCoordinateIndex   Index of vertex coordinates.
	 * @param   colorMapU               Color map U coordinate.
	 * @param   colorMapV               Color map V coordinate.
	 */
	public Vertex3D( final Vector3D point, final int vertexCoordinateIndex, final float colorMapU, final float colorMapV )
	{
		this.point = point;
		this.vertexCoordinateIndex = vertexCoordinateIndex;
		this.colorMapU = colorMapU;
		this.colorMapV = colorMapV;
		normal = null;
	}

	/**
	 * Construct vertex.
	 *
	 * @param   point                   Coordinates of vertex.
	 * @param   normal                  Vertex normal.
	 * @param   vertexCoordinateIndex   Index of vertex coordinates.
	 * @param   colorMapU               Color map U coordinate.
	 * @param   colorMapV               Color map V coordinate.
	 */
	public Vertex3D( final Vector3D point, final Vector3D normal, final int vertexCoordinateIndex, final float colorMapU, final float colorMapV )
	{
		this.point = point;
		this.vertexCoordinateIndex = vertexCoordinateIndex;
		this.colorMapU = colorMapU;
		this.colorMapV = colorMapV;
		this.normal = normal;
	}

	/**
	 * Construct vertex.
	 *
	 * @param   point                   Coordinates of vertex.
	 * @param   vertexCoordinateIndex   Index of vertex coordinates.
	 */
	public Vertex3D( final Vector3D point, final int vertexCoordinateIndex )
	{
		this.point = point;
		this.vertexCoordinateIndex = vertexCoordinateIndex;
		normal = null;
		colorMapU = Float.NaN;
		colorMapV = Float.NaN;
	}

	/**
	 * Construct vertex.
	 *
	 * @param   vertexCoordinates       Vertex coordinates.
	 * @param   vertexCoordinateIndex   Index of vertex coordinates.
	 */
	public Vertex3D( final List<Vector3D> vertexCoordinates, final int vertexCoordinateIndex )
	{
		this( vertexCoordinates.get( vertexCoordinateIndex ), vertexCoordinateIndex );
	}

	/**
	 * Returns the vertex normal. Note that the vertex may not have an
	 * explicit normal, resulting in <code>null</code>.
	 *
	 * @return  Normal vector.
	 *
	 * @see     Face3D#getVertexNormal
	 */
	@Nullable
	public Vector3D getNormal()
	{
		return normal;
	}

	/**
	 * Set vertex normal.
	 *
	 * @param   normal  Vertex normal.
	*/
	public void setNormal( @Nullable final Vector3D normal )
	{
		this.normal = normal;
	}

	@Override
	public int hashCode()
	{
		return vertexCoordinateIndex;
	}

	@Override
	public boolean equals( final Object object )
	{
		final boolean result;

		if ( object == this )
		{
			result = true;
		}
		else if ( object instanceof Vertex3D )
		{
			final Vertex3D other = (Vertex3D)object;
			result = ( ( vertexCoordinateIndex == other.vertexCoordinateIndex ) &&
			           ( colorMapU == colorMapU ? ( colorMapU == other.colorMapU ) : other.colorMapU != other.colorMapU ) &&
			           ( colorMapV == colorMapV ? ( colorMapV == other.colorMapV ) : other.colorMapV != other.colorMapV ) &&
			           point.equals( other.point ) );
		}
		else
		{
			result = false;
		}

		return result;
	}
}
