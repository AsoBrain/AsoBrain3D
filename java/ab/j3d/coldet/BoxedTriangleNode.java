/*   ColDet - C++ 3D Collision Detection Library
 *   Copyright (C) 2000 Amir Geva
 *
 *   ColDet - 3D Collision Detection Library for Java
 *   Copyright (C) 2008 Numdata BV
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */
package ab.j3d.coldet;

/**
 *
 * @author  Amir Geva (original C++ version)
 * @author  Peter S. Heijnen (Java Port)
 * @version 1.1
 */
public class BoxedTriangleNode
	extends BoxTreeNode
{
	private Triangle _triangle;

	BoxedTriangleNode( final Vector3D v1 , final Vector3D v2 , final Vector3D v3 )
	{
		final Vector3D pos = new Vector3D( Math.min( Math.min( v1.x, v2.x ), v3.x ),
		                                   Math.min( Math.min( v1.y, v2.y ), v3.y ),
		                                   Math.min( Math.min( v1.z, v2.z ), v3.z ) );

		final Vector3D size = new Vector3D( Math.max( Math.max( v1.x, v2.x ), v3.x ) - pos.x,
		                                    Math.max( Math.max( v1.y, v2.y ), v3.y ) - pos.y,
		                                    Math.max( Math.max( v1.z, v2.z ), v3.z ) - pos.z );

		final Vector3D center = new Vector3D( pos.x + 0.5f * size.x, pos.y + 0.5f * size.y, pos.z + 0.5f * size.z );

		_pos = pos;
		_size = size;
		_center = center;
		_triangle = new Triangle( v1, v2, v3 );
	}

	public int getSonsNumber()
	{
		return 0;
	}

	public BoxTreeNode getSon( final int which )
	{
		return null;
	}

	public int getTrianglesNumber()
	{
		return 1;
	}

	public BoxedTriangleNode getTriangle( final int which )
	{
		return ( which == 0 ) ? this : null;
	}

	Triangle getTriangle()
	{
		return _triangle;
	}
}
