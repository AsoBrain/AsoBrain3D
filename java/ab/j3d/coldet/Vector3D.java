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
public class Vector3D
{
	final float x;
	final float y;
	final float z;

	public static final Vector3D Zero = new Vector3D();

	Vector3D()
	{
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
	}

	Vector3D( final float x, final float y, final float z )
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	Vector3D( final Vector3D v )
	{
		x = v.x;
		y = v.y;
		z = v.z;
	}

	float squareMagnitude()
	{
		return x * x + y * y + z * z;
	}

	float magnitude()
	{
		return (float)Math.sqrt( squareMagnitude() );
	}

	Vector3D normalized()
	{
		return multiply( ( 1.0f / magnitude() ) );
	}

	Vector3D multiply( final float scalar )
	{
		return new Vector3D( scalar * x, scalar * y, scalar * z );
	}

	float dotProduct( final Vector3D v )
	{
		return x * v.x + y * v.y + z * v.z;
	}

	Vector3D crossProduct( final Vector3D v )
	{
		return new Vector3D( y * v.z - v.y * z,
		                     z * v.x - v.z * x,
		                     x * v.y - v.x * y );
	}

	Vector3D plus( final Vector3D v )
	{
		return new Vector3D( x + v.x, y + v.y, z + v.z );
	}

	Vector3D minus( final Vector3D v )
	{
		return new Vector3D( x - v.x, y - v.y, z - v.z );
	}

	Vector3D transform( final Matrix3D m )
	{
		return new Vector3D( x * m.m[0][0] + y * m.m[1][0] + z * m.m[2][0] + m.m[3][0],
		                     x * m.m[0][1] + y * m.m[1][1] + z * m.m[2][1] + m.m[3][1],
		                     x * m.m[0][2] + y * m.m[1][2] + z * m.m[2][2] + m.m[3][2] );
	}

	Vector3D rotateVector( final Matrix3D m )
	{
		return new Vector3D( x * m.m[0][0] + y * m.m[1][0] + z * m.m[2][0],
		                     x * m.m[0][1] + y * m.m[1][1] + z * m.m[2][1],
		                     x * m.m[0][2] + y * m.m[1][2] + z * m.m[2][2] );
	}

	float get( final int i )
	{
		switch ( i )
		{
			case 0 : return x;
			case 1 : return y;
			case 2 : return z;
			default : throw new AssertionError();
		}
	}

}
