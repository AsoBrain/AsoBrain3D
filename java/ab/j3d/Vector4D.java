/*
 * $Id$
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
package ab.j3d;

/**
 * A 4D vector.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class Vector4D
{
	/** X component of the vector. */ public final double x;
	/** Y component of the vector. */ public final double y;
	/** Z component of the vector. */ public final double z;
	/** W component of the vector. */ public final double w;

	/**
	 * Constructs a new vector.
	 *
	 * @param   x   X component of the vector.
	 * @param   y   Y component of the vector.
	 * @param   z   Z component of the vector.
	 * @param   w   W component of the vector.
	 */
	public Vector4D( final double x, final double y, final double z, final double w )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	/**
	 * Calculates the dot product of two 4-vectors.
	 *
	 * @param   x1  X component of the first vector.
	 * @param   y1  Y component of the first vector.
	 * @param   z1  Z component of the first vector.
	 * @param   w1  W component of the first vector.
	 * @param   x2  X component of the second vector.
	 * @param   y2  Y component of the second vector.
	 * @param   z2  Z component of the second vector.
	 * @param   w2  W component of the second vector.
	 *
	 * @return  Dot product of the vectors.
	 */
	public static double dot( final double x1, final double y1, final double z1, final double w1, final double x2, final double y2, final double z2, final double w2 )
	{
		return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2;
	}

	/**
	 * Returns the result of converting the vector from homogeneous coordinates
	 * to cartesian coordinates.
	 */
	public Vector3D homogeneousDivide()
	{
		return new Vector3D( x / w, y / w, z / w );
	}
}
