/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
 * This class represents a 3D vector.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Vector3D
{
	/** X component of 3D vector. */ public final float x;
	/** Y component of 3D vector. */ public final float y;
	/** Z component of 3D vector. */ public final float z;

	/**
	 * Initial value of a vector (0-vector).
	 */
	public static final Vector3D INIT = new Vector3D( 0 , 0 , 0 );
	/**
	 * Construct new vector.
	 *
	 * @param   nx  X-coordinate of vector.
	 * @param   ny  Y-coordinate of vector.
	 * @param   nz  Z-coordinate of vector.
	 */
	private Vector3D( final float nx , final float ny , final float nz )
	{
		x = nx;
		y = ny;
		z = nz;
	}

	/**
	 * Get angle between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  angle between vectors in radians.
	 */
	public static float angle( final Vector3D v1 , final Vector3D v2 )
	{
		return (float)Math.acos( cosAngle( v1 , v2 ) );
	}

	/**
	 * Get cos(angle) between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  cos(angle) between vectors.
	 */
	public static float cosAngle( final Vector3D v1 , final Vector3D v2 )
	{
		final float l = v1.length() * v2.length();
		return ( l == 0.0f ) ? 0.0f : ( dot( v1 , v2 ) / l );
	}

	/**
	 * Determine cross product of this vector with another vector.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  Resulting vector.
	 */
	public static Vector3D cross( final Vector3D v1 , final Vector3D v2 )
	{
		return v1.set( v1.y * v2.z - v1.z * v2.y ,
		               v1.z * v2.x - v1.x * v2.z ,
		               v1.x * v2.y - v1.y * v2.x );
	}

	/**
	 * Calculate distance between this and another vector (both should be absolute).
	 *
	 * @param   other   Vector to calculate the distance to.
	 *
	 * @return  Distance between this and the specified other vector.
	 */
	public float distanceTo( final Vector3D other )
	{
		final float dx = x - other.x;
		final float dy = y - other.y;
		final float dz = z - other.z;

		return (float)Math.sqrt( dx * dx + dy * dy + dz * dz );
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector
	 * and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  Dot product.
	 */
	public static float dot( final Vector3D v1 , final Vector3D v2 )
	{
		return ( v1.x * v2.x + v1.y * v2.y + v1.z * v2.z );
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param   other   Vector to compare with.
	 *
	 * @return  <code>true</code> if the objects are almost equal;
	 *          <code>false</code> if not.
	 *
	 * @see     Matrix3D#almostEqual
	 */
	public boolean almostEquals( final Vector3D other )
	{
		return ( other != null )
		    && ( ( other == this )
		      || ( Matrix3D.almostEqual( x , other.x )
		        && Matrix3D.almostEqual( y , other.y )
		        && Matrix3D.almostEqual( z , other.z ) ) );
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param   otherX  X-coordinate of vector to compare with.
	 * @param   otherY  Y-coordinate of vector to compare with.
	 * @param   otherZ  Z-coordinate of vector to compare with.
	 *
	 * @return  <code>true</code> if vectors are equal;
	 *          <code>false</code> if not.
	 */
	public boolean equals( final float otherX , final float otherY , final float otherZ )
	{
		return ( Float.isNaN( otherX ) || ( otherX == x ) )
		    && ( Float.isNaN( otherY ) || ( otherY == y ) )
		    && ( Float.isNaN( otherZ ) || ( otherZ == z ) );
	}

	public boolean equals( final Object other )
	{
		final boolean result;

		if ( other == this )
		{
			result = true;
		}
		else if ( !( other instanceof Vector3D ) )
		{
			result = false;
		}
		else
		{
			final Vector3D v = (Vector3D)other;
			result = ( ( x == v.x ) && ( y == v.y ) && ( z == v.z ) );
		}

		return result;
	}

	public int hashCode()
	{
		return Float.floatToIntBits( x ) ^ Float.floatToIntBits( y ) ^ Float.floatToIntBits( z );
	}

	/**
	 * Convert string representation of object (see toString()) back to
	 * object instance.
	 *
	 * @param   value   String representation of object.
	 *
	 * @return  Object instance.
	 */
	public static Vector3D fromString( final String value )
	{
		final int comma1 = value.indexOf( ',' );
		final int comma2 = value.indexOf( ',' , comma1 + 1 );

		return Vector3D.INIT.set( Float.parseFloat( value.substring( 0 , comma1 ) ) ,
		                          Float.parseFloat( value.substring( comma1 + 1 , comma2 ) ) ,
		                          Float.parseFloat( value.substring( comma2 + 1 ) ) );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return  Length of vector.
	 */
	public float length()
	{
		return (float)Math.sqrt( x * x + y * y + z * z );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   other   Vector to subtract from this vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D minus( final Vector3D other )
	{
		return minus( other.x , other.y , other.z );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 * @param   otherZ  Z-coordinate of vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D minus( final float otherX , final float otherY , final float otherZ )
	{
		return set( x - otherX , y - otherY , z - otherZ );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   factor  Scale multiplication factor.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D multiply( final float factor )
	{
		return set( x * factor , y * factor , z * factor );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1,
	 * it will be returned as-is.
	 *
	 * @return  Normalized vector.
	 */
	public Vector3D normalize()
	{
		final float l = length();
		return ( l == 0 || l == 1 ) ? this : set( x / l , y / l , z / l );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   other   Vector to add to this vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D plus( final Vector3D other )
	{
		return plus( other.x , other.y , other.z );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 * @param   otherZ  Z-coordinate of vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D plus( final float otherX , final float otherY , final float otherZ )
	{
		return set( x + otherX , y + otherY , z + otherZ );
	}

	/**
	 * Set vector to the specified coordinates.
	 *
	 * @param   nx      X-coordinate of vector.
	 * @param   ny      Y-coordinate of vector.
	 * @param   nz      Z-coordinate of vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D set( final float nx , final float ny , final float nz )
	{
		final Vector3D result;

		if ( INIT.equals( nx , ny , nz ) )
		{
			result = INIT;
		}
		else if ( ( this != INIT ) && equals( nx , ny , nz ) )
		{
			result = this;
		}
		else
		{
			result = new Vector3D( Float.isNaN( nx ) ? x : nx ,
			                       Float.isNaN( ny ) ? y : ny ,
			                       Float.isNaN( nz ) ? z : nz );
		}

		return result;
	}

	/**
	 * Get string representation of object.
	 *
	 * @return  String representation of object.
	 */
	public String toString()
	{
		return x + "," + y + ',' + z;
	}
}
