/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
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
 * This interface defines a plane in 3D space.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface Plane3D
{
	/**
	 * Distance component of plane relative to origin. This defines the
	 * <code>D</code> variable in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 *
	 * @return  Distance component of plane relative to origin.
	 *
	 * @see     #getNormal
	 */
	double getDistance();

	/**
	 * Get plane normal. This defines the <code>A</code>, <code>B</code>, and
	 * <code>C</code> variables  in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 * <dl>
	 *  <dt>NOTE:</dt>
	 *  <dd>Using the individual normal components (X,Y,Z) may be more efficient,
	 *   since this does not require a {@link Vector3D} instance.</dd>
	 * </dl>
	 *
	 * @see     #getDistance
	 */
	Vector3D getNormal();

	/**
	 * Get flag that indicates that the plane is two-sided. This means, if set,
	 * that both sides of the plane are 'visible'; if not set, the plane
	 * is only visible from the side in which the plane normal points.
	 *
	 * @return  <code>true</code> if the plane is two-sided;
	 *          <code>false</code> otherwise.
	 */
	boolean isTwoSided();
}
