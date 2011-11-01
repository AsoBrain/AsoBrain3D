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
 * This interface defines a half-ray in 3D space. A half-ray is a straight line
 * extending from a single point of origin indefinitely into one direction.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface Ray3D
{
	/**
	 * Get origin of ray in 3D space. For a complete ray, as apposed to a
	 * half-ray, this may be any point on the ray.
	 *
	 * @return  Origin of ray in 3D space.
	 */
	Vector3D getOrigin();

	/**
	 * Get direction of ray in 3D space.
	 *
	 * @return  Direction of ray in 3D space.
	 */
	Vector3D getDirection();

	/**
	 * Is this ray a half-ray, or a complete ray? A half-ray has a distinct
	 * point of origin and extends indefinitely into one direction; a complete
	 * ray has neither a distinct origin, nor a direction.
	 *
	 * @return  <code>true</code> if this is a half-ray;
	 *          <code>false</code> if this is a complete ray.
	 */
	boolean isHalfRay();
}
