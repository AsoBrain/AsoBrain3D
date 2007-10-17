/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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
package ab.j3d.model;

import java.awt.Shape;

import ab.j3d.Vector3D;

/**
 * Triangulates 2-dimensional shapes into triangles.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public interface Triangulator
{
	/**
	 * Returns the normal of the shapes being triangulated. The default value is
	 * [0, 0, 0].
	 *
	 * @return  Normal of triangulated shapes.
	 */
	Vector3D getNormal();

	/**
	 * Sets the normal used of the shapes being triangulated.
	 *
	 * @param   normal  Normal to be set.
	 */
	void setNormal( final Vector3D normal );

	/**
	 * Returns the flatness used when flattening input shapes.
	 *
	 * @return  Flatness used when flattening input shapes.
	 */
	double getFlatness();

	/**
	 * Sets the flatness used when flattening input shapes.
	 *
	 * @param   flatness    Flatness to be set.
	 */
	void setFlatness( final double flatness );

	/**
	 * Triangulates the given shape and returns the result.
	 *
	 * @param   shape   Shape to be triangulated.
	 *
	 * @return  Triangulation result.
	 */
	Triangulation triangulate( final Shape shape );
}
