/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2007 Peter S. Heijnen
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

import com.numdata.oss.MathTools;

/**
 * This class represents a 3D vector. This class is not synchornized and all
 * fields are fully accessible.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Vector
{
	/** X component of 3D vector. */ public double x;
	/** Y component of 3D vector. */ public double y;
	/** Z component of 3D vector. */ public double z;

	/**
	 * Construct new 0-vector.
	 */
	public Vector()
	{
		x = 0.0;
		y = 0.0;
		z = 0.0;
	}

	/**
	 * Construct new vector.
	 *
	 * @param   nx  X-coordinate of vector.
	 * @param   ny  Y-coordinate of vector.
	 * @param   nz  Z-coordinate of vector.
	 */
	public Vector( final double nx , final double ny , final double nz )
	{
		x = nx;
		y = ny;
		z = nz;
	}

	/**
	 * Helper method to determine the resulting <code>VarVector</code> instance
	 * for methods that take a user-specified destination object (<code>dest</code>).
	 *
	 * @param   nx  X-coordinate of vector.
	 * @param   ny  Y-coordinate of vector.
	 * @param   nz  Z-coordinate of vector.
	 */
	public void set( final double nx , final double ny , final double nz )
	{
		x = nx;
		y = ny;
		z = nz;
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param   other   Vector to compare with.
	 *
	 * @return  <code>true</code> if the objects are almost equal;
	 *          <code>false</code> if not.
	 *
	 * @see     MathTools#almostEqual
	 */
	public boolean almostEquals( final Vector other )
	{
		return ( other != null ) &&
		       ( ( other == this ) ||
		         ( MathTools.almostEqual( x, other.x ) &&
		           MathTools.almostEqual( y, other.y ) &&
		           MathTools.almostEqual( z, other.z ) ) );
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
		else if ( !( other instanceof Vector ) )
		{
			result = false;
		}
		else
		{
			final Vector v = (Vector)other;
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
	 * Get string representation of object.
	 *
	 * @return  String representation of object.
	 */
	public String toString()
	{
		return x + "," + y + ',' + z;
	}

	/**
	 * Get angle between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  angle between vectors in radians.
	 */
	public static double angle( final Vector v1 , final Vector v2 )
	{
		return Math.acos( cosAngle( v1 , v2 ) );
	}

	/**
	 * Get cos(angle) between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  cos(angle) between vectors.
	 */
	public static double cosAngle( final Vector v1 , final Vector v2 )
	{
		final double l = length( v1 ) * length( v2 );
		return ( l == 0.0 ) ? 0.0 : ( dot( v1 , v2 ) / l );
	}

	/**
	 * Determine cross product between two vectors.
	 *
	 * @param   dest    Result destination (<code>null</code> => create new).
	 * @param   v1      First vector operand for cross product.
	 * @param   v2      Second vector operand for cross product.
	 *
	 * @return  Resulting vector (<code>dest</code> or newly created object).
	 */
	public static Vector cross( final Vector dest , final Vector v1 , final Vector v2 )
	{
		return cross( dest , v1.x , v1.y , v1.z , v2.x , v2.y , v2.z );
	}

	/**
	 * Determine cross product between two vectors.
	 *
	 * @param   dest    Result destination (<code>null</code> => create new).
	 * @param   x1      X-coordinate of first vector operand for cross product.
	 * @param   y1      Y-coordinate of first vector operand for cross product.
	 * @param   z1      Z-coordinate of first vector operand for cross product.
	 * @param   x2      X-coordinate of second vector operand for cross product.
	 * @param   y2      Y-coordinate of second vector operand for cross product.
	 * @param   z2      Z-coordinate of second vector operand for cross product.
	 *
	 * @return  Resulting vector (<code>dest</code> or newly created object).
	 */
	public static Vector cross( final Vector dest , final double x1 , final double y1 , final double z1 , final double x2 , final double y2 , final double z2 )
	{
		return set( dest , y1 * z2 - z1 * y2 , z1 * x2 - x1 * z2 , x1 * y2 - y1 * x2 );
	}

	/**
	 * Calculate distance between two vectors (both should be absolute).
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  Distance between this and the specified other vector.
	 */
	public static double distanceBetween( final Vector v1 , final Vector v2 )
	{
		return length( v2.x - v1.x , v2.y - v1.y , v2.z - v1.z );
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
	public static double dot( final Vector v1 , final Vector v2 )
	{
		return dot( v1.x , v1.y , v1.z , v2.x , v2.y , v2.z );
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
	public static Vector fromString( final Vector dest , final String value )
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

		return set( dest , x , y , z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @param   v       Vector to determine length of.
	 *
	 * @return  Length of vector.
	 */
	public static double length( final Vector v )
	{
		return length( v.x , v.y , v.z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @param   x   X-coordinate of vector.
	 * @param   y   Y-coordinate of vector.
	 * @param   z   Z-coordinate of vector.
	 *
	 * @return  Length of vector.
	 */
	public static double length( final double x , final double y , final double z )
	{
		return Math.sqrt( x * x + y * y + z * z );
	}

	/**
	 * Subtract one vector from another.
	 *
	 * @param   dest    Result destination (<code>null</code> => create new).
	 * @param   v1      Vector to subtract from.
	 * @param   v2      Vector to subtract.
	 *
	 * @return  Resulting vector (<code>dest</code> or newly created object).
	 */
	public static Vector minus( final Vector dest , final Vector v1 , final Vector v2 )
	{
		return minus( dest , v1 , v2.x , v2.y , v2.z );
	}

	/**
	 * Subtract one vector from another.
	 *
	 * @param   dest    Result destination (<code>null</code> => create new).
	 * @param   v1      Vector to subtract from.
	 * @param   x2      X-coordinate of vector to subtract.
	 * @param   y2      Y-coordinate of vector to subtract.
	 * @param   z2      Z-coordinate of vector to subtract.
	 *
	 * @return  Resulting vector (<code>dest</code> or newly created object).
	 */
	public static Vector minus( final Vector dest , final Vector v1 , final double x2 , final double y2 , final double z2 )
	{
		return set( dest , v1.x - x2 , v1.y - y2 , v1.z - z2 );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   factor  Scale multiplication factor.
	 *
	 * @return  Resulting vector (<code>dest</code> or newly created object).
	 */
	public static Vector multiply( final Vector dest , final Vector v , final double factor )
	{
		return set( dest , v.x * factor , v.y * factor , v.z * factor );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1,
	 * it will be returned as-is.
	 *
	 * @return  Normalized vector.
	 *
	 * @return  Resulting vector (<code>dest</code> or newly created object).
	 */
	public static Vector normalize( final Vector dest , final Vector v )
	{
		double x = v.x;
		double y = v.y;
		double z = v.z;

		final double squared = x * x + y * y + z * z;
		if (   ( squared < -0.00001 )
		  || ( ( squared >  0.00001 )
		    && ( squared <  0.99999 ) )
		  ||   ( squared <  1.00001 ) )
		{
			final double l = Math.sqrt( squared );
			x /= l;
			y /= l;
			z /= l;
		}

		return set( dest , x , y , z );
	}

	/**
	 * Add one vector to another.
	 *
	 * @param   dest    Result destination (<code>null</code> => create new).
	 * @param   v1      Vector to add to.
	 * @param   v2      Vector to add.
	 *
	 * @return  Resulting vector (<code>dest</code> or newly created object).
	 */
	public static Vector plus( final Vector dest , final Vector v1 , final Vector v2 )
	{
		return plus( dest , v1 , v2.x , v2.y , v2.z );
	}

	/**
	 * Add one vector to another.
	 *
	 * @param   dest    Result destination (<code>null</code> => create new).
	 * @param   v1      Vector to add to.
	 * @param   x2      X-coordinate of vector to add.
	 * @param   y2      Y-coordinate of vector to add.
	 * @param   z2      Z-coordinate of vector to add.
	 *
	 * @return  Resulting vector (<code>dest</code> or newly created object).
	 */
	public static Vector plus( final Vector dest , final Vector v1 , final double x2 , final double y2 , final double z2 )
	{
		return set( dest , v1.x - x2 , v1.y - y2 , v1.z - z2 );
	}

	/**
	 * Helper method to determine the resulting <code>VarVector</code> instance
	 * for methods that take a user-specified destination object (<code>dest</code>).
	 *
	 * @param   dest    Result destination (<code>null</code> => create new).
	 * @param   x       X-coordinate of vector.
	 * @param   y       Y-coordinate of vector.
	 * @param   z       Z-coordinate of vector.
	 *
	 * @return  Resulting vector (<code>dest</code> or newly created object).
	 */
	public static Vector set( final Vector dest , final double x , final double y , final double z )
	{
		final Vector result;
		if ( dest == null )
		{
			result = new Vector( x , y , z );
		}
		else
		{
			result = dest;
			dest.x = x;
			dest.y = y;
			dest.z = z;
		}
		return result;
	}

	/**
	 * Create human-readable representation of Vector3D object. This is
	 * aspecially useful for debugging purposes.
	 *
	 * @param   vector   Vector3D instance.
	 *
	 * @return  Human-readable representation of Vector3D object.
	 */
	public static String toFriendlyString( final Vector vector )
	{
		final DecimalFormat df = new DecimalFormat( "0.0" );

		return "[ " + df.format( vector.x ) + " , "
		            + df.format( vector.y ) + " , "
		            + df.format( vector.z ) + " ]";
	}
}
