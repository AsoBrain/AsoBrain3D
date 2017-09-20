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

/**
 * This interface defines a polygon in 3D space. All points of such a polygon
 * lie on the plane.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface Polygon3D
	extends Plane3D
{
	/**
	 * Get number of vertices that define this polygon.
	 *
	 * @return  Number of vertices.
	 */
	int getVertexCount();

	/**
	 * Get X coordinate of this polygon's vertex with the specified index.
	 *
	 * @param   index   Vertex index.
	 *
	 * @return  X coordinate for the specified vertex.
	 *
	 * @throws  IndexOutOfBoundsException if <code>index</code> is out of bounds.
	 */
	double getX( int index );

	/**
	 * Get Y coordinate of this polygon's vertex with the specified index.
	 *
	 * @param   index   Vertex index.
	 *
	 * @return  Y coordinate for the specified vertex.
	 *
	 * @throws  IndexOutOfBoundsException if <code>index</code> is out of bounds.
	 */
	double getY( int index );

	/**
	 * Get Z coordinate of this polygon's vertex with the specified index.
	 *
	 * @param   index   Vertex index.
	 *
	 * @return  Z coordinate for the specified vertex.
	 *
	 * @throws  IndexOutOfBoundsException if <code>index</code> is out of bounds.
	 */
	double getZ( int index );
}
