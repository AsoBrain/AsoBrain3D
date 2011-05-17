/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import ab.j3d.*;

/**
 * Basic implementation of {@link Polygon3D}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class BasicPolygon3D
	implements Polygon3D
{
	/**
	 * Vertices that make up this polygon.
	 */
	private final Vector3D[] _vertices;

	/**
	 * Plane is two-sided.
	 */
	private final boolean _twoSided;

	/**
	 * Normal vector of polygon. Calculated on-demand by {@link #getNormal}.
	 */
	private Vector3D   _normal;

	/**
	 * Construct polygon.
	 *
	 * @param   vertices    Vertices that make up this polygon.
	 * @param   twoSided    Plane is two-sided.
	 *
	 * @throws  NullPointerException if <code>vertices</code> is <code>null</code>.
	 * @throws  IllegalArgumentException if <code>vertices</code> has less than 3 elements.
	 */
	public BasicPolygon3D( final Vector3D[] vertices, final boolean twoSided )
	{
		if ( vertices == null )
		{
			throw new NullPointerException( "vertices" );
		}

		if ( vertices.length < 3 )
		{
			throw new IllegalArgumentException( "polygon must have at least 3 vertices" );
		}

		_vertices = vertices.clone();
		_twoSided = twoSided;
		_normal   = null;
	}

	public int getVertexCount()
	{
		return _vertices.length;
	}

	public double getX( final int index )
	{
		return _vertices[ index ].x;
	}

	public double getY( final int index )
	{
		return _vertices[ index ].y;
	}

	public double getZ( final int index )
	{
		return _vertices[ index ].z;
	}

	public double getDistance()
	{
		return Vector3D.dot( getNormal(), _vertices[ 0 ] );
	}

	public Vector3D getNormal()
	{
		Vector3D result = _normal;
		if ( result == null )
		{
			final Vector3D[] vertices = _vertices;
			result = GeometryTools.getPlaneNormal( vertices[ 0 ], vertices[ 1 ], vertices[ 2 ] );
			_normal = result;
		}
		return result;
	}

	public boolean isTwoSided()
	{
		return _twoSided;
	}
}
