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
package ab.j3d;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * This class defines a 3D vector using single-precision floating-point values.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Vector3f
	implements Serializable
{
	/**
	 * Zero-vector.
	 */
	public static final Vector3f ZERO = new Vector3f( 0.0f, 0.0f, 0.0f );

	/**
	 * Positive X-axis direction vector.
	 */
	public static final Vector3f POSITIVE_X_AXIS = new Vector3f( 1.0f, 0.0f, 0.0f );

	/**
	 * Negative X-axis direction vector.
	 */
	public static final Vector3f NEGATIVE_X_AXIS = new Vector3f( -1.0f, 0.0f, 0.0f );

	/**
	 * Positive Y-axis direction vector.
	 */
	public static final Vector3f POSITIVE_Y_AXIS = new Vector3f( 0.0f, 1.0f, 0.0f );

	/**
	 * Negative Y-axis direction vector.
	 */
	public static final Vector3f NEGATIVE_Y_AXIS = new Vector3f( 0.0f, -1.0f, 0.0f );

	/**
	 * Positive Z-axis direction vector.
	 */
	public static final Vector3f POSITIVE_Z_AXIS = new Vector3f( 0.0f, 0.0f, 1.0f );

	/**
	 * Negative Z-axis direction vector.
	 */
	public static final Vector3f NEGATIVE_Z_AXIS = new Vector3f( 0.0f, 0.0f, -1.0f );

	/**
	 * X component of 3D vector.
	 */
	private float _x;

	/**
	 * Y component of 3D vector.
	 */
	private float _y;

	/**
	 * Z component of 3D vector.
	 */
	private float _z;

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 3198558829683527053L;

	/**
	 * Construct new vector.
	 *
	 * @param   x  X-coordinate of vector.
	 * @param   y  Y-coordinate of vector.
	 * @param   z  Z-coordinate of vector.
	 */
	public Vector3f( final float x, final float y, final float z )
	{
		_x = x;
		_y = y;
		_z = z;
	}

	/**
	 * Get X component of 3D vector.
	 *
	 * @return  X component of 3D vector.
	 */
	public float getX()
	{
		return _x;
	}

	/**
	 * Get Y component of 3D vector.
	 *
	 * @return  Y component of 3D vector.
	 */
	public float getY()
	{
		return _y;
	}

	/**
	 * Get Z component of 3D vector.
	 *
	 * @return  Z component of 3D vector.
	 */
	public float getZ()
	{
		return _z;
	}

	/**
	 * Set X component of 3D vector.
	 *
	 * @param   x   X component of 3D vector.
	 */
	public void setX( final float x)
	{
		_x = x;
	}

	/**
	 * Set Y component of 3D vector.
	 *
	 * @param   y   component of 3D vector.
	 */
	public void setY( final float y )
	{
		_y = y;
	}

	/**
	 * Set Z component of 3D vector.
	 *
	 * @param   z   Z component of 3D vector.
	 */
	public void setZ( final float z )
	{
		_z = z;
	}

	/**
	 * Set this vector to be identical to the source vector.
	 *
	 * @param   source  Source vector to copy.
	 */
	public void set( final Vector3f source )
	{
		set( source.getX(), source.getY(), source.getZ() );
	}

	/**
	 * Set this vector to be identical to the source vector.
	 *
	 * @param   x  X-coordinate of vector.
	 * @param   y  Y-coordinate of vector.
	 * @param   z  Z-coordinate of vector.
	 */
	public void set( final float x, final float y, final float z )
	{
		setX( x );
		setY( y );
		setZ( z );
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
	public static Vector3f getProperty( final Properties properties, final String name )
	{
		return getProperty( properties, name, null );
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
	public static Vector3f getProperty( final Properties properties, final String name, final Vector3f defaultValue )
	{
		Vector3f result = defaultValue;

		final String stringValue = ( properties != null ) ? properties.getProperty( name, null ) : null;
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
	public static float angle( final Vector3f v1, final Vector3f v2 )
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
	public static boolean areParallel( final Vector3f v1, final Vector3f v2 )
	{
		return MathTools.almostEqual( Math.abs( cosAngle( v1, v2 ) ), 1.0f );
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
	public static boolean areSameDirection( final Vector3f v1, final Vector3f v2 )
	{
		return MathTools.almostEqual( cosAngle( v1, v2 ), 1.0f );
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
	public static boolean arePerpendicular( final Vector3f v1, final Vector3f v2 )
	{
		return MathTools.almostEqual( dot( v1, v2 ), 0.0f );
	}

	/**
	 * Get cos(angle) between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  cos(angle) between vectors.
	 */
	public static float cosAngle( final Vector3f v1, final Vector3f v2 )
	{
		final float l = v1.length() * v2.length();
		return ( l == 0.0f ) ? 0.0f : ( dot( v1, v2 ) / l );
	}

	/**
	 * Determine cross product of this vector with another vector.
	 *
	 * @param   x1  X-coordinate of first vector operand.
	 * @param   y1  Y-coordinate of first vector operand.
	 * @param   z1  Z-coordinate of first vector operand.
	 * @param   x2  X-coordinate of second vector operand.
	 * @param   y2  Y-coordinate of second vector operand.
	 * @param   z2  Z-coordinate of second vector operand.
	 *
	 * @return  Resulting vector.
	 */
	public static Vector3f cross( final float x1, final float y1, final float z1, final float x2, final float y2, final float z2 )
	{
		return new Vector3f( y1 * z2 - z1 * y2,
		                     z1 * x2 - x1 * z2,
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
	public static Vector3f cross( final Vector3f v1, final Vector3f v2 )
	{
		return cross( v1.getX(), v1.getY(), v1.getZ(), v2.getX(), v2.getY(), v2.getZ() );
	}

	/**
	 * Determine cross product of this vector with another vector and store the
	 * result in the specified vector.
	 *
	 * @param   result  Result vector.
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 */
	public static void cross( final Vector3f result, final Vector3f v1, final Vector3f v2 )
	{
		cross( result, v1.getX(), v1.getY(), v1.getZ(), v2.getX(), v2.getY(), v2.getZ() );
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
	public static void cross( final Vector3f result, final float x1, final float y1, final float z1, final float x2, final float y2, final float z2 )
	{
		result.set( y1 * z2 - z1 * y2,
		            z1 * x2 - x1 * z2,
		            x1 * y2 - y1 * x2 );
	}

	/**
	 * Calculate distance between two point vectors.
	 *
	 * @param   p1      First point vector to calculate the distance between.
	 * @param   p2      Second point vector to calculate the distance between.
	 *
	 * @return  Distance between this and the specified other vector.
	 */
	public static float distanceBetween( final Vector3f p1, final Vector3f p2 )
	{
		return length( p1.getX() - p2.getX(), p1.getY() - p2.getY(), p1.getZ() - p2.getZ() );
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
		return distanceBetween( this, other );
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
	public static float dot( final float x1, final float y1, final float z1, final float x2, final float y2, final float z2 )
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
	public static float dot( final Vector3f v1, final Vector3f v2 )
	{
		return dot( v1.getX(), v1.getY(), v1.getZ(), v2.getX(), v2.getY(), v2.getZ() );
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
		            || ( MathTools.almostEqual( getX(), other.getX() ) &&
		                 MathTools.almostEqual( getY(), other.getY() ) &&
		                 MathTools.almostEqual( getZ(), other.getZ() ) ) );
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
	public boolean almostEquals( final float otherX, final float otherY, final float otherZ )
	{
		return MathTools.almostEqual( getX(), otherX ) &&
		       MathTools.almostEqual( getY(), otherY ) &&
		       MathTools.almostEqual( getZ(), otherZ );
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
	public boolean equals( final float otherX, final float otherY, final float otherZ )
	{
		return ( otherX == getX() ) && ( otherY == getY() ) && ( otherZ == getZ() );
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
			result = ( ( getX() == v.getX() ) && ( getY() == v.getY() ) && ( getZ() == v.getZ() ) );
		}
		else
		{
			result = false;
		}

		return result;
	}

	public int hashCode()
	{
		return Float.floatToIntBits( getX() ) ^ Float.floatToIntBits( getY() ) ^ Float.floatToIntBits( getZ() );
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
		{
			throw new NullPointerException( "value" );
		}

		final int comma1 = value.indexOf( (int)',' );
		if ( comma1 < 1 )
		{
			throw new IllegalArgumentException( "comma1" );
		}

		final float x = Float.parseFloat( value.substring( 0, comma1 ) );

		final int comma2 = value.indexOf( (int)',', comma1 + 1 );
		if ( comma2 < 1 )
		{
			throw new IllegalArgumentException( "comma2" );
		}

		final float y = Float.parseFloat( value.substring( comma1 + 1, comma2 ) );
		final float z = Float.parseFloat( value.substring( comma2 + 1 ) );

		return new Vector3f( x, y, z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return  Length of vector.
	 */
	public float length()
	{
		return length( getX(), getY(), getZ() );
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
	public static float length( final float x, final float y, final float z )
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
		return minus( other.getX(), other.getY(), other.getZ() );
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
	public Vector3f minus( final float otherX, final float otherY, final float otherZ )
	{
		return new Vector3f( getX() - otherX, getY() - otherY, getZ() - otherZ );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   other   Vector to subtract from this vector.
	 */
	public void minusLocal( final Vector3f other )
	{
		minusLocal( other.getX(), other.getY(), other.getZ() );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 * @param   otherZ  Z-coordinate of vector.
	 */
	public void minusLocal( final float otherX, final float otherY, final float otherZ )
	{
		set( getX() - otherX, getY() - otherY, getZ() - otherZ );
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
		return new Vector3f( getX() * scale, getY() * scale, getZ() * scale );
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
	public Vector3f scale( final float scaleX, final float scaleY, final float scaleZ )
	{
		return new Vector3f( getX() * scaleX, getY() * scaleY, getZ() * scaleZ );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scale   Scale multiplication factor.
	 */
	public void scaleLocal( final float scale )
	{
		scaleLocal( scale, scale, scale );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scaleX  X scale multiplication factor.
	 * @param   scaleY  Y scale multiplication factor.
	 * @param   scaleZ  Z scale multiplication factor.
	 */
	public void scaleLocal( final float scaleX, final float scaleY, final float scaleZ )
	{
		set( scaleX * getX(), scaleY * getY(), scaleZ * getZ() );
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
		return ( ( l == 0.0f ) || ( l == 1.0f ) ) ? this : new Vector3f( getX() / l, getY() / l, getZ() / l );
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
	public static Vector3f normalize( final float x, final float y, final float z )
	{
		final float l = length( x, y, z );
		return ( l == 0.0f ) ? ZERO : new Vector3f( x / l, y / l, z / l );
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
		return new Vector3f( getX() + other.getX(), getY() + other.getY(), getZ() + other.getZ() );
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
	public Vector3f plus( final float otherX, final float otherY, final float otherZ )
	{
		return new Vector3f( getX() + otherX, getY() + otherY, getZ() + otherZ );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   other   Vector to add to this vector.
	 */
	public void plusLocal( final Vector3f other )
	{
		plusLocal( other.getX(), other.getY(), other.getZ() );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 * @param   otherZ  Z-coordinate of vector.
	 */
	public void plusLocal( final float otherX, final float otherY, final float otherZ )
	{
		set( getX() + otherX, getY() + otherY, getZ() + otherZ );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return  String representation of object.
	 */
	public String toString()
	{
		return getX() + "," + getY() + ',' + getZ();
	}

	/**
	 * This function translates cartesian coordinates to polar/spherial
	 * coordinates.
	 * <p />
	 * The polar/spherial coordinates are defined as the triplet
	 * <code>( r, &theta;, &rho; )</code>, where r is radius, &theta; is the
	 * azimuth, and &rho; is the zenith.
	 *
	 * @return  Polar coordinates (radius,azimuth,zenith) based on cartesian
	 *          coordinates defined by this vector.
	 */
	public Vector3f cartesianToPolar()
	{
		return cartesianToPolar( getX(), getY(), getZ() );
	}

	/**
	 * This function translates cartesian coordinates to polar/spherial
	 * coordinates.
	 * <p />
	 * The polar/spherial coordinates are defined as the triplet
	 * <code>( r, &theta;, &rho; )</code>, where r is radius, &theta; is the
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
	public static Vector3f cartesianToPolar( final float x, final float y, final float z )
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
			final float azimuth = (float)Math.atan2( dy, dx );
			final float zenith  = (float)Math.atan2( Math.sqrt( xSquared + ySquared ), dz );

			result = new Vector3f( radius, azimuth, zenith );
		}

		return result;
	}

	/**
	 * This function translates polar/spherial coordinates to cartesian
	 * coordinates.
	 * <p />
	 * The polar/spherial coordinates are defined as the triplet
	 * <code>( r, &theta;, &rho; )</code>, where r is radius, &theta; is the
	 * azimuth, and &rho; is the zenith.
	 *
	 * @return  Cartesian coordinates based on polar coordinates
	 *          (radius,azimuth,zenith) defined by this vector.
	 */
	public Vector3f polarToCartesian()
	{
		return polarToCartesian( getX(), getY(), getZ() );
	}

	/**
	 * This function translates polar/spherial coordinates to cartesian
	 * coordinates.
	 * <p />
	 * The polar/spherial coordinates are defined as the triplet
	 * <code>( r, &theta;, &rho; )</code>, where r is radius, &theta; is the
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
	public static Vector3f polarToCartesian( final float radius, final float azimuth, final float zenith )
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

			result = new Vector3f( x, y, z );
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
			: "[ " + df.format( (double)vector.getX() ) +
			  ", " + df.format( (double)vector.getY() ) +
			  ", " + df.format( (double)vector.getZ() ) + " ]";
	}

	/**
	 * Make this vector the zero-vector.
	 */
	public void zero()
	{
		set( 0.0f, 0.0f, 0.0f );
	}
}
