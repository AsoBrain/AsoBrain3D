/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2009
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

import ab.j3d.Vector3D;

/**
 * Defines a triangle in 3D space.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public interface Triangle3D
	extends Polygon3D
{
	/**
	 * Get first point of triangle.
	 *
	 * @return  First point of triangle.
	 */
	Vector3D getP1();

	/**
	 * Get second point of triangle.
	 *
	 * @return  Second point of triangle.
	 */
	Vector3D getP2();

	/**
	 * Get third point of triangle.
	 *
	 * @return  Third point of triangle.
	 */
	Vector3D getP3();

	/**
	 * Get perimeter of this triangle.
	 *
	 * @return  Perimeter of this triangle.
	 */
	double getPerimeter();

	/**
	 * Get area of this triangle.
	 *
	 * @return  Area of this triangle.
	 */
	double getArea();
}