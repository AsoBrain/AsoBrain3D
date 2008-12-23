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
 * Base class for hierarchy tree nodes.
 *
 * @author  Amir Geva (original C++ version)
 * @author  Peter S. Heijnen (Java Port)
 * @version 1.1
 */
public abstract class BoxTreeNode
	extends Box
{
	/**
	 * Default constructor.
	 */
	protected BoxTreeNode()
	{
	}

	/**
	 * Constructor for a box from position and size.
	 */
	protected BoxTreeNode( final Vector3D pos , final Vector3D size )
	{
		super( pos, size );
	}

	/**
	 * Returns the number of sons this node has.
	 */
	public abstract int getSonsNumber();

	/**
	 * Returns a son node, by index.
	 */
	public abstract BoxTreeNode getSon( int which );

	/**
	 * Returns the number of triangles in this node. Only non-zero for leaf.
	 * nodes.
	 */
	public abstract int getTrianglesNumber();

	/**
	 * Returns the boxed triangle contained in this node by its index.
	 */
	public abstract BoxedTriangleNode getTriangle( int which );
}