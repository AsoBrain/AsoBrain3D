/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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
 * Basic implementation of {@link Triangle3D} interface.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class BasicTriangle3D
	extends AbstractTriangle3D
{
	/**
	 * First point of the triangle.
	 */
	private final Vector3D _p1;

	/**
	 * Second point of the triangle.
	 */
	private final Vector3D _p2;

	/**
	 * Third point of the triangle.
	 */
	private final Vector3D _p3;

	/**
	 * Plane is two-sided.
	 */
	private final boolean _twoSided;

	/**
	 * Construct new triangle 3D.
	 *
	 * @param   p1          First point of the triangle.
	 * @param   p2          Second point of the triangle.
	 * @param   p3          Third point of the triangle.
	 * @param   twoSided    Plane is two-sided.
	 */
	public BasicTriangle3D( final Vector3D p1 , final Vector3D p2 , final Vector3D p3 , final boolean twoSided )
	{
		_p1 = p1;
		_p2 = p2;
		_p3 = p3;
		_twoSided = twoSided;
	}

	public Vector3D getP1()
	{
		return _p1;
	}

	public Vector3D getP2()
	{
		return _p2;
	}

	public Vector3D getP3()
	{
		return _p3;
	}

	public boolean isTwoSided()
	{
		return _twoSided;
	}

}