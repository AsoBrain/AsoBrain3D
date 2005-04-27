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

import java.text.DecimalFormat;

/**
 * This class represents a 3D vector.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Vector3D
{
	/** X component of 3D vector. */ public final double x;
	/** Y component of 3D vector. */ public final double y;
	/** Z component of 3D vector. */ public final double z;

	/**
	 * Initial value of a vector (0-vector).
	 */
	public static final Vector3D INIT = new Vector3D( 0.0 , 0.0 , 0.0 );

	/**
	 * Construct new vector.
	 *
	 * @param   nx  X-coordinate of vector.
	 * @param   ny  Y-coordinate of vector.
	 * @param   nz  Z-coordinate of vector.
	 */
	private Vector3D( final double nx , final double ny , final double nz )
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
	public static double angle( final Vector3D v1 , final Vector3D v2 )
	{
		return Math.acos( cosAngle( v1 , v2 ) );
	}

	/**
	 * Test if two vectors are parallel to each other.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  <code>true</code> if the vectors are parallel;
	 *          <code>false</code> if not.
	 */
	public static boolean areParallel( final Vector3D v1 , final Vector3D v2 )
	{
		return Matrix3D.almostEqual( Math.abs( cosAngle( v1 , v2 ) ) , 1.0 );
	}

	/**
	 * Test if two vectors define the same direction.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  <code>true</code> if the vectors define the same direction;
	 *          <code>false</code> if not.
	 */
	public static boolean areSameDirection( final Vector3D v1 , final Vector3D v2 )
	{
		return Matrix3D.almostEqual( cosAngle( v1 , v2 ) , 1.0 );
	}

	/**
	 * Test if two vectors are perpendicular to each other.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  <code>true</code> if the vectors are perpendicular;
	 *          <code>false</code> if not.
	 */
	public static boolean arePerpendicular( final Vector3D v1 , final Vector3D v2 )
	{
		return Matrix3D.almostEqual( dot( v1 , v2 ) , 0.0 );
	}

	/**
	 * Get cos(angle) between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  cos(angle) between vectors.
	 */
	public static double cosAngle( final Vector3D v1 , final Vector3D v2 )
	{
		final double l = v1.length() * v2.length();
		return ( l == 0.0 ) ? 0.0 : ( dot( v1 , v2 ) / l );
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
	 * Calculate distance between two point vectors.
	 *
	 * @param   p1      First point vector to calculate the distance between.
	 * @param   p2      Second point vector to calculate the distance between.
	 *
	 * @return  Distance between this and the specified other vector.
	 */
	public static double distanceBetween( final Vector3D p1 , final Vector3D p2 )
	{
		return length( p1.x - p2.x , p1.y - p2.y , p1.z - p2.z );
	}

	/**
	 * Calculate distance between this point vector and another.
	 *
	 * @param   other   Point vector to calculate the distance to.
	 *
	 * @return  Distance between this and the other vector.
	 */
	public double distanceTo( final Vector3D other )
	{
		return distanceBetween( this , other );
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector and another
	 * one specified as argument.
	 *
	 * @param   x1      X-coordinate of first vector operand.
	 * @param   y1      Y-coordinate of first vector operand.
	 * @param   z1      Z-coordinate of first vector operand.
	 * @param   x2      X-coordinate of second vector operand.
	 * @param   y2      Y-coordinate of second vector operand.
	 * @param   z2      Z-coordinate of second vector operand.
	 *
	 * @return  Dot product.
	 */
	public static double dot( final double x1 , final double y1 , final double z1 , final double x2 , final double y2 , final double z2 )
	{
		return x1 * x2 + y1 * y2 + z1 * z2;
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector and another
	 * one specified as argument.
	 *
	 * @param   v1      First vector operand.
	 * @param   v2      Second vector operand.
	 *
	 * @return  Dot product.
	 */
	public static double dot( final Vector3D v1 , final Vector3D v2 )
	{
		return dot( v1.x , v1.y , v1.z , v2.x , v2.y , v2.z );
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
	public boolean equals( final double otherX , final double otherY , final double otherZ )
	{
		return ( Double.isNaN( otherX ) || ( otherX == x ) )
		    && ( Double.isNaN( otherY ) || ( otherY == y ) )
		    && ( Double.isNaN( otherZ ) || ( otherZ == z ) );
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
		long l;
		return (int)( ( l = Double.doubleToLongBits( x ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( y ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( z ) ) ^ ( l >>> 32 ) );
	}

	/**
	 * Convert string representation of vector back to <code>Vector3D</code>
	 * instance (see <code>toString()</code>).
	 *
	 * @param   value   String representation of object.
	 *
	 * @return  Object instance.
	 *
	 * @throws  NullPointerException if <code>value</code> is <code>null</code>.
	 * @throws  IllegalArgumentException if the string format is unrecognized.
	 * @throws  NumberFormatException if any of the numeric components are badly formatted.
	 *
	 * @see     #toString()
	 */
	public static Vector3D fromString( final String value )
	{
		if ( value == null )
			throw new NullPointerException( "value" );

		final int comma1 = value.indexOf( (int)',' );
		if ( comma1 < 1 )
			throw new IllegalArgumentException( "comma1" );

		final double x = Double.parseDouble( value.substring( 0 , comma1 ) );

		final int comma2 = value.indexOf( (int)',' , comma1 + 1 );
		if ( comma2 < 1 )
			throw new IllegalArgumentException( "comma2" );

		final double y = Double.parseDouble( value.substring( comma1 + 1 , comma2 ) );
		final double z = Double.parseDouble( value.substring( comma2 + 1 ) );

		return INIT.set( x , y , z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return  Length of vector.
	 */
	public double length()
	{
		final double x = this.x;
		final double y = this.y;
		final double z = this.z;
		return length( x , y , z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return  Length of vector.
	 */
	public static double length( final double x , final double y , final double z )
	{
		return Math.sqrt( x * x + y * y + z * z );
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
	public Vector3D minus( final double otherX , final double otherY , final double otherZ )
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
	public Vector3D multiply( final double factor )
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
		final double l = length();
		return ( ( l == 0.0 ) || ( l == 1.0 ) ) ? this : set( x / l , y / l , z / l );
	}

	/**
	 * Normalize the specified vector. If the vector has length 0 or 1, a
	 * 0-vector will be returned.
	 *
	 * @param   x   X-component of vector.
	 * @param   y   Y-component of vector.
	 * @param   z   Z-component of vector.
	 *
	 * @return  Normalized vector.
	 */
	public static Vector3D normalize( final double x , final double y , final double z )
	{
		final double l = length( x , y , z );
		return ( l == 0.0 ) ? INIT : new Vector3D( x / l , y / l , z / l );
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
	public Vector3D plus( final double otherX , final double otherY , final double otherZ )
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
	public Vector3D set( final double nx , final double ny , final double nz )
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
			result = new Vector3D( Double.isNaN( nx ) ? x : nx ,
			                       Double.isNaN( ny ) ? y : ny ,
			                       Double.isNaN( nz ) ? z : nz );
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

	/**
	 * Create human-readable representation of this <code>Vector3D</code> object.
	 * This is aspecially useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this <code>Vector3D</code> object.
	 */
	public String toFriendlyString()
	{
		return toFriendlyString( this );
	}

	/**
	 * Create human-readable representation of <code>Vector3D</code> object.
	 * This is aspecially useful for debugging purposes.
	 *
	 * @param   vector   Vector3D instance (<code>null</code> produces 'null').
	 *
	 * @return  Human-readable representation of <code>Vector3D</code> object.
	 */
	public static String toFriendlyString( final Vector3D vector )
	{
		final DecimalFormat df = new DecimalFormat( "0.0" );

		return ( vector == null ) ? "null" :
		       "[ " + df.format( vector.x ) + " , "
		            + df.format( vector.y ) + " , "
		            + df.format( vector.z ) + " ]";
	}
}
