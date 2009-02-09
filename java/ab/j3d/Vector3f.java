/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2009 Numdata BV
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
import java.util.Properties;

import com.numdata.oss.MathTools;
import com.numdata.oss.PropertyTools;

/**
 * This class defines a 3D vector using single-precision floating-point values.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Vector3f
{
	/**
	 * Zero-vector.
	 */
	public static final Vector3f ZERO = new Vector3f( 0.0f , 0.0f , 0.0f );

	/**
	 * Positive X-axis direction vector.
	 */
	public static final Vector3f POSITIVE_X_AXIS = new Vector3f( 1.0f , 0.0f , 0.0f );

	/**
	 * Negative X-axis direction vector.
	 */
	public static final Vector3f NEGATIVE_X_AXIS = new Vector3f( -1.0f , 0.0f , 0.0f );

	/**
	 * Positive Y-axis direction vector.
	 */
	public static final Vector3f POSITIVE_Y_AXIS = new Vector3f( 0.0f , 1.0f , 0.0f );

	/**
	 * Negative Y-axis direction vector.
	 */
	public static final Vector3f NEGATIVE_Y_AXIS = new Vector3f( 0.0f , -1.0f , 0.0f );

	/**
	 * Positive Z-axis direction vector.
	 */
	public static final Vector3f POSITIVE_Z_AXIS = new Vector3f( 0.0f , 0.0f , 1.0f );

	/**
	 * Negative Z-axis direction vector.
	 */
	public static final Vector3f NEGATIVE_Z_AXIS = new Vector3f( 0.0f , 0.0f , -1.0f );

	/**
	 * X component of 3D vector.
	 */
	public float x;

	/**
	 * Y component of 3D vector.
	 */
	public float y;

	/**
	 * Z component of 3D vector.
	 */
	public float z;

	/**
	 * Construct new vector.
	 *
	 * @param   nx  X-coordinate of vector.
	 * @param   ny  Y-coordinate of vector.
	 * @param   nz  Z-coordinate of vector.
	 */
	public Vector3f( final float nx , final float ny , final float nz )
	{
		x = nx;
		y = ny;
		z = nz;
	}

	/**
	 * Set this vector to be identical to the source vector.
	 *
	 * @param   source  Source vector to copy.
	 */
	public void set( final Vector3f source )
	{
		x = source.x;
		y = source.y;
		z = source.z;
	}

	/**
	 * Set this vector to be identical to the source vector.
	 *
	 * @param   nx  X-coordinate of vector.
	 * @param   ny  Y-coordinate of vector.
	 * @param   nz  Z-coordinate of vector.
	 */
	public void set( final float nx , final float ny , final float nz )
	{
		x = nx;
		y = ny;
		z = nz;
	}

	/**
	 * Get <code>Vector3D</code> property with the specified name from a
	 * {@link Properties} object.
	 *
	 * @param   properties  Properties to get vector from.
	 * @param   name        Property name.
	 *
	 * @return  <code>Vector3D</code> object;
	 *          <code>null</code> if property value is absent/invalid.
	 */
	public static Vector3f getProperty( final Properties properties , final String name )
	{
		return getProperty( properties , name , null );
	}

	/**
	 * Get <code>Vector3D</code> property with the specified name from a
	 * {@link Properties} object.
	 *
	 * @param   properties      Properties to get vector from.
	 * @param   name            Property name.
	 * @param   defaultValue    Value to use if property value is absent/invalid.
	 *
	 * @return  <code>Vector3D</code> object;
	 *          <code>defaultValue</code> if property value is absent/invalid.
	 */
	public static Vector3f getProperty( final Properties properties , final String name , final Vector3f defaultValue )
	{
		Vector3f result = defaultValue;

		final String stringValue = PropertyTools.getString( properties , name , null );
		if ( stringValue != null )
		{
			try
			{
				result = fromString( stringValue );
			}
			catch ( Exception e ) { /* ignore */ }
		}

		return result;
	}

	/**
	 * Get angle between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  angle between vectors in radians.
	 */
	public static float angle( final Vector3f v1 , final Vector3f v2 )
	{
		return (float)Math.acos( (double)cosAngle( v1, v2 ) );
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
	public static boolean areParallel( final Vector3f v1 , final Vector3f v2 )
	{
		return MathTools.almostEqual( Math.abs( cosAngle( v1, v2 ) ) , 1.0f );
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
	public static boolean areSameDirection( final Vector3f v1 , final Vector3f v2 )
	{
		return MathTools.almostEqual( cosAngle( v1 , v2 ) , 1.0f );
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
	public static boolean arePerpendicular( final Vector3f v1 , final Vector3f v2 )
	{
		return MathTools.almostEqual( dot( v1 , v2 ) , 0.0f );
	}

	/**
	 * Get cos(angle) between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  cos(angle) between vectors.
	 */
	public static float cosAngle( final Vector3f v1 , final Vector3f v2 )
	{
		final float l = v1.length() * v2.length();
		return ( l == 0.0f ) ? 0.0f : ( dot( v1 , v2 ) / l );
	}

	/**
	 * Determine cross product of this vector with another vector.
	 *
	 * @param   x1      X-coordinate of first vector operand.
	 * @param   y1      Y-coordinate of first vector operand.
	 * @param   z1      Z-coordinate of first vector operand.
	 * @param   x2      X-coordinate of second vector operand.
	 * @param   y2      Y-coordinate of second vector operand.
	 * @param   z2      Z-coordinate of second vector operand.
	 *
	 * @return  Resulting vector.
	 */
	public static Vector3f cross( final float x1 , final float y1 , final float z1 , final float x2 , final float y2 , final float z2 )
	{
		return new Vector3f( y1 * z2 - z1 * y2 ,
		                     z1 * x2 - x1 * z2 ,
		                     x1 * y2 - y1 * x2 );
	}

	/**
	 * Determine cross product of this vector with another vector.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  Resulting vector.
	 */
	public static Vector3f cross( final Vector3f v1 , final Vector3f v2 )
	{
		return cross( v1.x , v1.y , v1.z , v2.x , v2.y , v2.z );
	}

	/**
	 * Determine cross product of this vector with another vector and store the
	 * result in the specified vector.
	 *
	 * @param   result  Result vector.
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 */
	public static void cross( final Vector3f result , final Vector3f v1 , final Vector3f v2 )
	{
		cross( result , v1.x , v1.y , v1.z , v2.x , v2.y , v2.z );
	}

	/**
	 * Determine cross product of this vector with another vector and store the
	 * result in the specified vector.
	 *
	 * @param   result  Result vector.
	 * @param   x1      X component of first vector.
	 * @param   y1      Y component of first vector.
	 * @param   z1      Z component of first vector.
	 * @param   x2      X component of second vector.
	 * @param   y2      Y component of second vector.
	 * @param   z2      Z component of second vector.
	 */
	public static void cross( final Vector3f result , final float x1 , final float y1 , final float z1 , final float x2 , final float y2 , final float z2 )
	{
		result.x = y1 * z2 - z1 * y2;
		result.y = z1 * x2 - x1 * z2;
		result.z = x1 * y2 - y1 * x2;
	}

	/**
	 * Calculate distance between two point vectors.
	 *
	 * @param   p1      First point vector to calculate the distance between.
	 * @param   p2      Second point vector to calculate the distance between.
	 *
	 * @return  Distance between this and the specified other vector.
	 */
	public static float distanceBetween( final Vector3f p1 , final Vector3f p2 )
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
	public float distanceTo( final Vector3f other )
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
	public static float dot( final float x1 , final float y1 , final float z1 , final float x2 , final float y2 , final float z2 )
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
	public static float dot( final Vector3f v1 , final Vector3f v2 )
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
	 * @see   MathTools#almostEqual
	 */
	public boolean almostEquals( final Vector3f other )
	{
		return ( other != null )
		       && ( ( other == this )
		            || ( MathTools.almostEqual( x , other.x ) &&
		                 MathTools.almostEqual( y , other.y ) &&
		                 MathTools.almostEqual( z , other.z ) ) );
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param   otherX  X-coordinate of vector to compare with.
	 * @param   otherY  Y-coordinate of vector to compare with.
	 * @param   otherZ  Z-coordinate of vector to compare with.
	 *
	 * @return  <code>true</code> if the objects are almost equal;
	 *          <code>false</code> if not.
	 *
	 * @see     MathTools#almostEqual
	 */
	public boolean almostEquals( final float otherX , final float otherY , final float otherZ )
	{
		return MathTools.almostEqual( x , otherX ) &&
		       MathTools.almostEqual( y , otherY ) &&
		       MathTools.almostEqual( z , otherZ );
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
		return ( otherX == x ) && ( otherY == y ) && ( otherZ == z );
	}

	public boolean equals( final Object other )
	{
		final boolean result;

		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof Vector3f )
		{
			final Vector3f v = (Vector3f)other;
			result = ( ( x == v.x ) && ( y == v.y ) && ( z == v.z ) );
		}
		else
		{
			result = false;
		}

		return result;
	}

	public int hashCode()
	{
		return Float.floatToIntBits( x ) ^ Float.floatToIntBits( y ) ^ Float.floatToIntBits( z );
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
	public static Vector3f fromString( final String value )
	{
		if ( value == null )
			throw new NullPointerException( "value" );

		final int comma1 = value.indexOf( (int)',' );
		if ( comma1 < 1 )
			throw new IllegalArgumentException( "comma1" );

		final float x = Float.parseFloat( value.substring( 0 , comma1 ) );

		final int comma2 = value.indexOf( (int)',' , comma1 + 1 );
		if ( comma2 < 1 )
			throw new IllegalArgumentException( "comma2" );

		final float y = Float.parseFloat( value.substring( comma1 + 1 , comma2 ) );
		final float z = Float.parseFloat( value.substring( comma2 + 1 ) );

		return new Vector3f( x , y , z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return  Length of vector.
	 */
	public float length()
	{
		return length( x , y , z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @param   x   X-component of vector.
	 * @param   y   Y-component of vector.
	 * @param   z   Z-component of vector.
	 *
	 * @return  Length of vector.
	 */
	public static float length( final float x , final float y , final float z )
	{
		final double dx = (double)x;
		final double dy = (double)y;
		final double dz = (double)z;
		return (float)Math.sqrt( dx * dx + dy * dy + dz * dz );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   other   Vector to subtract from this vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3f minus( final Vector3f other )
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
	public Vector3f minus( final float otherX , final float otherY , final float otherZ )
	{
		return new Vector3f( x - otherX , y - otherY , z - otherZ );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   other   Vector to subtract from this vector.
	 */
	public void minusLocal( final Vector3f other )
	{
		minusLocal( other.x , other.y , other.z );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 * @param   otherZ  Z-coordinate of vector.
	 */
	public void minusLocal( final float otherX , final float otherY , final float otherZ )
	{
		x -= otherX;
		y -= otherY;
		z -= otherZ;
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scale   Scale multiplication factor.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3f scale( final float scale )
	{
		return new Vector3f( x * scale , y * scale , z * scale );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scaleX  X scale multiplication factor.
	 * @param   scaleY  Y scale multiplication factor.
	 * @param   scaleZ  Z scale multiplication factor.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3f scale( final float scaleX , final float scaleY , final float scaleZ )
	{
		return new Vector3f( x * scaleX , y * scaleY , z * scaleZ );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scale   Scale multiplication factor.
	 */
	public void scaleLocal( final float scale )
	{
		x *= scale;
		y *= scale;
		z *= scale;
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scaleX  X scale multiplication factor.
	 * @param   scaleY  Y scale multiplication factor.
	 * @param   scaleZ  Z scale multiplication factor.
	 */
	public void scaleLocal( final float scaleX , final float scaleY , final float scaleZ )
	{
		x *= scaleX;
		y *= scaleY;
		z *= scaleZ;
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1,
	 * it will be returned as-is.
	 *
	 * @return  Normalized vector.
	 */
	public Vector3f normalize()
	{
		final float l = length();
		return ( ( l == 0.0f ) || ( l == 1.0f ) ) ? this : new Vector3f( x / l , y / l , z / l );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1,
	 * the vector is left unchanged.
	 */
	public void normalizeLocal()
	{
		final float l = length();
		if ( ( l != 0.0f ) && ( l != 1.0f ) )
		{
			x /= l;
			y /= l;
			z /= l;
		}
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
	public static Vector3f normalize( final float x , final float y , final float z )
	{
		final float l = length( x , y , z );
		return ( l == 0.0f ) ? ZERO : new Vector3f( x / l , y / l , z / l );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   other   Vector to add to this vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3f plus( final Vector3f other )
	{
		return new Vector3f( x + other.x , y + other.y , z + other.z );
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
	public Vector3f plus( final float otherX , final float otherY , final float otherZ )
	{
		return new Vector3f( x + otherX , y + otherY , z + otherZ );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   other   Vector to add to this vector.
	 */
	public void plusLocal( final Vector3f other )
	{
		x += other.x;
		y += other.y;
		z += other.z;
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 * @param   otherZ  Z-coordinate of vector.
	 */
	public void plusLocal( final float otherX , final float otherY , final float otherZ )
	{
		x += otherX;
		y += otherY;
		z += otherZ;
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
	 * This function translates cartesian coordinates to polar/spherial
	 * coordinates.
	 * <p />
	 * The polar/spherial coordinates are defined as the triplet
	 * <code>( r , &theta; , &rho; )</code>, where r is radius, &theta; is the
	 * azimuth, and &rho; is the zenith.
	 *
	 * @return  Polar coordinates (radius,azimuth,zenith) based on cartesian
	 *          coordinates defined by this vector.
	 */
	public Vector3f cartesianToPolar()
	{
		return cartesianToPolar( x , y , z );
	}

	/**
	 * This function translates cartesian coordinates to polar/spherial
	 * coordinates.
	 * <p />
	 * The polar/spherial coordinates are defined as the triplet
	 * <code>( r , &theta; , &rho; )</code>, where r is radius, &theta; is the
	 * azimuth, and &rho; is the zenith.
	 * <p />
	 * See <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical Coordinates</a>
	 * at <a href="http://mathworld.wolfram.com/">MathWorld</a>.<br />
	 * See <a href="http://astronomy.swin.edu.au/~pbourke/projection/coords/">Coordinate System Transformation</a>
	 * by <a href="http://astronomy.swin.edu.au/~pbourke/">Paul Bourke</a>.
	 *
	 * @param   x       Cartesian X coordinate.
	 * @param   y       Cartesian Y coordinate.
	 * @param   z       Cartesian Z coordinate.
	 *
	 * @return  Polar coordinates (radius,azimuth,zenith) based on cartesian
	 *          coordinates defined by this vector.
	 */
	public static Vector3f cartesianToPolar( final float x , final float y , final float z )
	{
		final Vector3f result;

		final double dx = (double)x;
		final double dy = (double)y;
		final double dz = (double)z;

		final double xSquared = dx * dx;
		final double ySquared = dy * dy;
		final double zSquared = dz * dz;

		if ( ( xSquared == 0.0 ) && ( ySquared == 0.0 ) && ( zSquared == 0.0 ) )
		{
			result = ZERO;
		}
		else
		{
			final float radius  = (float)Math.sqrt( xSquared + ySquared + zSquared );
			final float azimuth = (float)Math.atan2( dy , dx );
			final float zenith  = (float)Math.atan2( Math.sqrt( xSquared + ySquared ) , dz );

			result = new Vector3f( radius, azimuth, zenith );
		}

		return result;
	}

	/**
	 * This function translates polar/spherial coordinates to cartesian
	 * coordinates.
	 * <p />
	 * The polar/spherial coordinates are defined as the triplet
	 * <code>( r , &theta; , &rho; )</code>, where r is radius, &theta; is the
	 * azimuth, and &rho; is the zenith.
	 *
	 * @return  Cartesian coordinates based on polar coordinates
	 *          (radius,azimuth,zenith) defined by this vector.
	 */
	public Vector3f polarToCartesian()
	{
		return polarToCartesian( x , y , z );
	}

	/**
	 * This function translates polar/spherial coordinates to cartesian
	 * coordinates.
	 * <p />
	 * The polar/spherial coordinates are defined as the triplet
	 * <code>( r , &theta; , &rho; )</code>, where r is radius, &theta; is the
	 * azimuth, and &rho; is the zenith.
	 * <p />
	 * See <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical Coordinates</a>
	 * at <a href="http://mathworld.wolfram.com/">MathWorld</a>.<br />
	 * See <a href="http://astronomy.swin.edu.au/~pbourke/projection/coords/">Coordinate System Transformation</a>
	 * by <a href="http://astronomy.swin.edu.au/~pbourke/">Paul Bourke</a>.
	 *
	 * @param   radius      Radius of sphere.
	 * @param   azimuth     Angle measured from the x-axis in the XY-plane (0 => point on XZ-plane).
	 * @param   zenith      Angle measured from the z-axis toward the XY-plane (0 => point on Z-axis).
	 *
	 * @return  Cartesian coordinates based on polar coordinates
	 *          (radius,azimuth,zenith) defined by this vector.
	 */
	public static Vector3f polarToCartesian( final float radius , final float azimuth , final float zenith )
	{
		final Vector3f result;

		if ( radius == 0.0f )
		{
			result = ZERO;
		}
		else
		{
			final double dZenith  = (double)zenith;
			final double dAzimuth = (double)azimuth;

			final float radiusXY = radius * (float)Math.sin( dZenith );

			final float x = radiusXY * (float)Math.cos( dAzimuth );
			final float y = radiusXY * (float)Math.sin( dAzimuth );
			final float z = radius   * (float)Math.cos( dZenith );

			result = new Vector3f( x , y , z );
		}

		return result;
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
	public static String toFriendlyString( final Vector3f vector )
	{
		final DecimalFormat df = new DecimalFormat( "0.00" );
		return ( vector == null ) ? "null"
			: "[ " + df.format( (double)vector.x ) +
			  " , " + df.format( (double)vector.y ) +
			  " , " + df.format( (double)vector.z ) + " ]";
	}

	/**
	 * Make this vector the zero-vector.
	 */
	public void zero()
	{
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
	}
}