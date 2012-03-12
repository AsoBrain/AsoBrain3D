/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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

import java.text.*;
import java.util.*;

import ab.j3d.geom.*;

/**
 * This class represents a 3D vector.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Vector3D
	extends Vector2D
{
	/**
	 * Zero-vector.
	 */
	public static final Vector3D ZERO = new Vector3D( 0.0, 0.0, 0.0 );

	/**
	 * Positive X-axis direction vector.
	 */
	public static final Vector3D POSITIVE_X_AXIS = new Vector3D( 1.0, 0.0, 0.0 );

	/**
	 * Negative X-axis direction vector.
	 */
	public static final Vector3D NEGATIVE_X_AXIS = new Vector3D( -1.0, 0.0, 0.0 );

	/**
	 * Positive Y-axis direction vector.
	 */
	public static final Vector3D POSITIVE_Y_AXIS = new Vector3D( 0.0, 1.0, 0.0 );

	/**
	 * Negative Y-axis direction vector.
	 */
	public static final Vector3D NEGATIVE_Y_AXIS = new Vector3D( 0.0, -1.0, 0.0 );

	/**
	 * Positive Z-axis direction vector.
	 */
	public static final Vector3D POSITIVE_Z_AXIS = new Vector3D( 0.0, 0.0, 1.0 );

	/**
	 * Negative Z-axis direction vector.
	 */
	public static final Vector3D NEGATIVE_Z_AXIS = new Vector3D( 0.0, 0.0, -1.0 );

	/**
	 * Z component of 3D vector.
	 */
	public final double z;

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 234972165412209583L;

	/**
	 * Construct new vector.
	 *
	 * @param   nx  X-coordinate of vector.
	 * @param   ny  Y-coordinate of vector.
	 * @param   nz  Z-coordinate of vector.
	 */
	public Vector3D( final double nx, final double ny, final double nz )
	{
		super( nx, ny );
		z = nz;
	}

	/**
	 * Get Z component of this vector.
	 *
	 * @return  Z component of this vector.
	*/
	public double getZ()
	{
		return z;
	}

	/**
	 * Get {@link Vector3D} property with the specified name from a
	 * {@link Properties} object.
	 *
	 * @param   properties  Properties to get vector from.
	 * @param   name        Property name.
	 *
	 * @return  {@link Vector3D} object;
	 *          <code>null</code> if property value is absent/invalid.
	 */
	public static Vector3D getProperty( final Properties properties, final String name )
	{
		return getProperty( properties, name, null );
	}

	/**
	 * Get {@link Vector3D} property with the specified name from a
	 * {@link Properties} object.
	 *
	 * @param   properties      Properties to get vector from.
	 * @param   name            Property name.
	 * @param   defaultValue    Value to use if property value is absent/invalid.
	 *
	 * @return  {@link Vector3D} object;
	 *          <code>defaultValue</code> if property value is absent/invalid.
	 */
	public static Vector3D getProperty( final Properties properties, final String name, final Vector3D defaultValue )
	{
		Vector3D result = defaultValue;

		if ( properties != null )
		{
			final String stringValue = properties.getProperty( name, null );
			if ( stringValue != null )
			{
				try
				{
					result = fromString( stringValue );
				}
				catch ( Exception e ) { /* ignore */ }
			}
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
	public static double angle( final Vector3D v1, final Vector3D v2 )
	{
		return Math.acos( cosAngle( v1, v2 ) );
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
	public static boolean areParallel( final Vector3D v1, final Vector3D v2 )
	{
		return GeometryTools.almostEqual( Math.abs( cosAngle( v1, v2 ) ) - 1.0, 0.0 );
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
	public static boolean areSameDirection( final Vector3D v1, final Vector3D v2 )
	{
		return GeometryTools.almostEqual( cosAngle( v1, v2 ) - 1.0, 0.0 );
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
	public static boolean arePerpendicular( final Vector3D v1, final Vector3D v2 )
	{
		return GeometryTools.almostEqual( dot( v1, v2 ), 0.0 );
	}

	/**
	 * Get cos(angle) between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  cos(angle) between vectors.
	 */
	public static double cosAngle( final Vector3D v1, final Vector3D v2 )
	{
		return cosAngle( v1.getX(), v1.getY(), v1.getZ(), v2.getX(), v2.getY(), v2.getZ() );
	}

	/**
	 * Get cos(angle) between two vectors.
	 *
	 * @param   x1      X-coordinate of first vector.
	 * @param   y1      Y-coordinate of first vector.
	 * @param   z1      Z-coordinate of first vector.
	 * @param   x2      X-coordinate of second vector.
	 * @param   y2      Y-coordinate of second vector.
	 * @param   z2      Z-coordinate of second vector.
	 *
	 * @return  cos(angle) between vectors.
	 */
	public static double cosAngle( final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		final double l = length( x1, y1, z1 ) * length( x2, y2, z2 );
		return ( l == 0.0 ) ? 0.0 : dot( x1, y1, z1, x2, y2, z2 ) / l;
	}

	/**
	 * Determine cross product between two vectors.
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
	public static Vector3D cross( final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		return ZERO.set( y1 * z2 - z1 * y2,
		                 z1 * x2 - x1 * z2,
		                 x1 * y2 - y1 * x2 );
	}

	/**
	 * Determine cross product between two vectors.
	 *
	 * <p>
	 * The cross product is related to the sine function by the equation
	 * <blockquote>|a &times; b| = |a| |b| sin &theta;</blockquote>
	 * where &theta; denotes the angle between the two vectors.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  Resulting vector.
	 */
	public static Vector3D cross( final Vector3D v1, final Vector3D v2 )
	{
		return cross( v1.getX(), v1.getY(), v1.getZ(), v2.getX(), v2.getY(), v2.getZ() );
	}

	/**
	 * Determine Z component of cross product between two vectors.
	 *
	 * @param   x1  X-coordinate of first vector operand.
	 * @param   y1  Y-coordinate of first vector operand.
	 * @param   x2  X-coordinate of second vector operand.
	 * @param   y2  Y-coordinate of second vector operand.
	 *
	 * @return  Resulting vector.
	 */
	public static double crossZ( final double x1, final double y1, final double x2, final double y2 )
	{
		return x1 * y2 - y1 * x2;
	}

	/**
	 * Determine Z component of cross between two vectors.
	 *
	 * @param   v1  First vector.
	 * @param   v2  Second vector.
	 *
	 * @return  Resulting vector.
	 */
	public static double crossZ( final Vector3D v1, final Vector3D v2 )
	{
		return crossZ( v1.getX(), v1.getY(), v2.getX(), v2.getY() );
	}

	/**
	 * Calculate distance between two point vectors.
	 *
	 * @param   p1  First point vector to calculate the distance between.
	 * @param   p2  Second point vector to calculate the distance between.
	 *
	 * @return  Distance between this and the specified other vector.
	 */
	public static double distanceBetween( final Vector3D p1, final Vector3D p2 )
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
	public double distanceTo( final Vector3D other )
	{
		return distanceBetween( this, other );
	}

	/**
	 * Get direction from one point to another point.
	 *
	 * @param   from    Point vector for from-point.
	 * @param   to      Point vector for to-point.
	 *
	 * @return  Direction from from-point to to-point.
	 */
	public static Vector3D direction( final Vector3D from, final Vector3D to )
	{
		return direction( from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ() );
	}

	/**
	 * Get direction from one point to another point.
	 *
	 * @param   x1  X coordinate of from-point.
	 * @param   y1  Y coordinate of from-point.
	 * @param   z1  Z coordinate of from-point.
	 * @param   x2  X coordinate of to-point.
	 * @param   y2  Y coordinate of to-point.
	 * @param   z2  Z coordinate of to-point.
	 *
	 * @return  Direction from from-point to to-point.
	 */
	public static Vector3D direction( final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		return normalize( x2 - x1, y2 - y1, z2 - z1 );
	}

	/**
	 * Get direction from this point vector to another.
	 *
	 * @param   other   Point vector to calculate the direction to.
	 *
	 * @return  Direction from this to the other vector.
	 */
	public Vector3D directionTo( final Vector3D other )
	{
		return direction( getX(), getY(), getZ(), other.getX(), other.getY(), other.getZ() );
	}

	/**
	 * Get direction from this point vector to another.
	 *
	 * @param   x   X coordinate of point to calculate the direction to.
	 * @param   y   Y coordinate of point to calculate the direction to.
	 * @param   z   Z coordinate of point to calculate the direction to.
	 *
	 * @return  Direction from this to the other vector.
	 */
	public Vector3D directionTo( final double x, final double y, final double z )
	{
		return direction( getX(), getY(), getZ(),  x, y, z );
	}

	/**
	 * Calculate average of two vectors (i.e. center between two point vectors).
	 *
	 * @param   v1  First vector.
	 * @param   v2  Second vector.
	 *
	 * @return  Average vector (i.e. center point).
	 */
	public static Vector3D average( final Vector3D v1, final Vector3D v2 )
	{
		return v1.equals( v2 ) ? v1 : new Vector3D( 0.5 * ( v1.getX() + v2.getX() ), 0.5 * ( v1.getY() + v2.getY() ), 0.5 * ( v1.getZ() + v2.getZ() ) );
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector and another
	 * one specified as argument.
	 *
	 * @param   x1  X-coordinate of first vector operand.
	 * @param   y1  Y-coordinate of first vector operand.
	 * @param   z1  Z-coordinate of first vector operand.
	 * @param   x2  X-coordinate of second vector operand.
	 * @param   y2  Y-coordinate of second vector operand.
	 * @param   z2  Z-coordinate of second vector operand.
	 *
	 * @return  Dot product.
	 */
	public static double dot( final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		return x1 * x2 + y1 * y2 + z1 * z2;
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector and another
	 * one specified as argument.
	 *
	 * <p>
	 * The dot product is related to the cosine function by the equation
	 * <blockquote>a &middot; b = |a| |b| cos &theta;</blockquote>
	 * where &theta; denotes the angle between the two vectors.
	 *
	 * @param   v1  First vector operand.
	 * @param   v2  Second vector operand.
	 *
	 * @return  Dot product.
	 */
	public static double dot( final Vector3D v1, final Vector3D v2 )
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
	 * @see     GeometryTools#almostEqual
	 */
	public boolean almostEquals( final Vector3D other )
	{
		return ( other == this ) || ( ( other != null ) && almostEquals( other.getX(), other.getY(), other.getZ() ) );
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
	 * @see     GeometryTools#almostEqual
	 */
	public boolean almostEquals( final double otherX, final double otherY, final double otherZ )
	{
		return GeometryTools.almostEqual( getX(), otherX ) &&
		       GeometryTools.almostEqual( getY(), otherY ) &&
		       GeometryTools.almostEqual( getZ(), otherZ );
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
	public boolean equals( final double otherX, final double otherY, final double otherZ )
	{
		return ( Double.isNaN( otherX ) || ( otherX == getX() ) ) &&
		       ( Double.isNaN( otherY ) || ( otherY == getY() ) ) &&
		       ( Double.isNaN( otherZ ) || ( otherZ == getZ() ) );
	}

	@Override
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
			result = ( ( getX() == v.getX() ) && ( getY() == v.getY() ) && ( getZ() == v.getZ() ) );
		}

		return result;
	}

	@Override
	public int hashCode()
	{
		long l;
		return (int)( ( l = Double.doubleToLongBits( getX() ) ) ^ ( l >>> 32 ) ^
		              ( l = Double.doubleToLongBits( getY() ) ) ^ ( l >>> 32 ) ^
		              ( l = Double.doubleToLongBits( getZ() ) ) ^ ( l >>> 32 ) );
	}

	/**
	 * Convert string representation of vector back to {@link Vector3D}
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
		{
			throw new NullPointerException( "value" );
		}

		final int comma1 = value.indexOf( (int)',' );
		if ( comma1 < 1 )
		{
			throw new IllegalArgumentException( value );
		}

		final double x = Double.parseDouble( value.substring( 0, comma1 ) );

		final int comma2 = value.indexOf( (int)',', comma1 + 1 );
		if ( comma2 < 1 )
		{
			throw new IllegalArgumentException( value );
		}

		final double y = Double.parseDouble( value.substring( comma1 + 1, comma2 ) );
		final double z = Double.parseDouble( value.substring( comma2 + 1 ) );

		return ZERO.set( x, y, z );
	}

	/**
	 * Get inverse vector.
	 *
	 * @return  Inverse vector.
	 */
	public Vector3D inverse()
	{
		return new Vector3D( -getX(), -getY(), -getZ() );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return  Length of vector.
	 */
	@Override
	public double length()
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
	public static double length( final double x, final double y, final double z )
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
	public Vector3D minus( final double otherX, final double otherY, final double otherZ )
	{
		return set( getX() - otherX, getY() - otherY, getZ() - otherZ );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   factor  Scale multiplication factor.
	 *
	 * @return  Resulting vector.
	 */
	@Override
	public Vector3D multiply( final double factor )
	{
		return set( getX() * factor, getY() * factor, getZ() * factor );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1,
	 * it will be returned as-is.
	 *
	 * @return  Normalized vector.
	 */
	@Override
	public Vector3D normalize()
	{
		final double l = length();
		return ( ( l == 0.0 ) || ( l == 1.0 ) ) ? this : set( getX() / l, getY() / l, getZ() / l );
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
	public static Vector3D normalize( final double x, final double y, final double z )
	{
		final double l = length( x, y, z );
		return ( l == 0.0 ) ? ZERO : new Vector3D( x / l, y / l, z / l );
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
		return plus( other.getX(), other.getY(), other.getZ() );
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
	public Vector3D plus( final double otherX, final double otherY, final double otherZ )
	{
		return set( getX() + otherX, getY() + otherY, getZ() + otherZ );
	}

	/**
	 * Set vector to the specified coordinates.
	 *
	 * @param   x   X-coordinate of vector.
	 * @param   y   Y-coordinate of vector.
	 * @param   z   Z-coordinate of vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D set( final double x, final double y, final double z )
	{
		final Vector3D result;

		if ( ZERO.equals( x, y, z ) )
		{
			result = ZERO;
		}
		else if ( ( this != ZERO ) && equals( x, y, z ) )
		{
			result = this;
		}
		else
		{
			result = new Vector3D( Double.isNaN( x ) ? getX() : x, Double.isNaN( y ) ? getY() : y, Double.isNaN( z ) ? getZ() : z );
		}

		return result;
	}

	/**
	 * Get string representation of object.
	 *
	 * @return  String representation of object.
	 */
	@Override
	public String toString()
	{
		return getX() + "," + getY() + ',' + getZ();
	}

	/**
	 * This function translates cartesian coordinates to polar/spherical
	 * coordinates.
	 * <p />
	 * The polar/spherical coordinates are defined as the triplet
	 * <code>( r, &theta;, &rho; )</code>, where r is radius, &theta; is the
	 * azimuth, and &rho; is the zenith.
	 *
	 * @return  Polar coordinates (radius,azimuth,zenith) based on cartesian
	 *          coordinates defined by this vector.
	 */
	public Vector3D cartesianToPolar()
	{
		return cartesianToPolar( getX(), getY(), getZ() );
	}

	/**
	 * This function translates cartesian coordinates to polar/spherical
	 * coordinates.
	 * <p />
	 * The polar/spherical coordinates are defined as the triplet
	 * <code>( r, &theta;, &rho; )</code>, where r is radius, &theta; is the
	 * azimuth, and &rho; is the zenith.
	 * <p />
	 * See <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical Coordinates</a>
	 * at <a href="http://mathworld.wolfram.com/">MathWorld</a>.<br />
	 * See <a href="http://astronomy.swin.edu.au/~pbourke/projection/coords/">Coordinate System Transformation</a>
	 * by <a href="http://astronomy.swin.edu.au/~pbourke/">Paul Bourke</a>.
	 *
	 * @param   x   Cartesian X coordinate.
	 * @param   y   Cartesian Y coordinate.
	 * @param   z   Cartesian Z coordinate.
	 *
	 * @return  Polar coordinates (radius,azimuth,zenith) based on cartesian
	 *          coordinates defined by this vector.
	 */
	public static Vector3D cartesianToPolar( final double x, final double y, final double z )
	{
		final Vector3D result;

		final double xSquared = x * x;
		final double ySquared = y * y;
		final double zSquared = z * z;

		if ( ( xSquared == 0.0 ) && ( ySquared == 0.0 ) && ( zSquared == 0.0 ) )
		{
			result = ZERO;
		}
		else
		{
			final double radius  = Math.sqrt( xSquared + ySquared + zSquared );
			final double azimuth = Math.atan2( y, x );
			final double zenith  = Math.atan2( Math.sqrt( xSquared + ySquared ), z );

			result = new Vector3D( radius, azimuth, zenith );
		}

		return result;
	}

	/**
	 * This function translates polar/spherical coordinates to cartesian
	 * coordinates.
	 * <p />
	 * The polar/spherical coordinates are defined as the triplet
	 * <code>( r, &theta;, &rho; )</code>, where r is radius, &theta; is the
	 * azimuth, and &rho; is the zenith.
	 *
	 * @return  Cartesian coordinates based on polar coordinates
	 *          (radius,azimuth,zenith) defined by this vector.
	 */
	public Vector3D polarToCartesian()
	{
		return polarToCartesian( getX(), getY(), getZ() );
	}

	/**
	 * This function translates polar/spherical coordinates to cartesian
	 * coordinates.
	 * <p />
	 * The polar/spherical coordinates are defined as the triplet
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
	public static Vector3D polarToCartesian( final double radius, final double azimuth, final double zenith )
	{
		final Vector3D result;

		if ( radius == 0.0 )
		{
			result = ZERO;
		}
		else
		{
			final double radiusXY = radius * Math.sin( zenith );

			result = new Vector3D( radiusXY * Math.cos( azimuth ),
			                       radiusXY * Math.sin( azimuth ),
			                       radius   * Math.cos( zenith  ) );
		}

		return result;
	}

	/**
	 * Create human-readable representation of this {@link Vector3D} object.
	 * This is especially useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this {@link Vector3D} object.
	 */
	@Override
	public String toFriendlyString()
	{
		return toFriendlyString( getX(), getY(), getZ() );
	}

	/**
	 * Create human-readable representation of {@link Vector3D} object.
	 * This is especially useful for debugging purposes.
	 *
	 * @param   vector   Vector3D instance (<code>null</code> produces 'null').
	 *
	 * @return  Human-readable representation of {@link Vector3D} object.
	 */
	public static String toFriendlyString( final Vector3D vector )
	{
		return ( vector == null ) ? "null" : toFriendlyString( vector.getX(), vector.getY(), vector.getZ() );
	}

	/**
	 * Create human-readable representation of a vector.
	 *
	 * @param   x   X component of vector.
	 * @param   y   Y component of vector.
	 * @param   z   Z component of vector.
	 *
	 * @return  Human-readable representation of vector.
	 */
	public static String toFriendlyString( final double x, final double y, final double z )
	{
		final NumberFormat df = TWO_DECIMAL_FORMAT;
		return "[ " + df.format( x ) + ", " + df.format( y ) + ", " + df.format( z ) + " ]";
	}

	/**
	 * Create short human-readable representation of this vector.
	 *
	 * @return  Human-readable representation of {@link Vector3D} object.
	 */
	@Override
	public String toShortFriendlyString()
	{
		return toShortFriendlyString( getX(), getY(), getZ() );
	}

	/**
	 * Create short human-readable representation of a vector.
	 *
	 * @param   vector   {@link Vector3D} (<code>null</code> produces 'null').
	 *
	 * @return  Human-readable representation of {@link Vector3D} object.
	 */
	public static String toShortFriendlyString( final Vector3D vector )
	{
		return ( vector == null ) ? "null" : toShortFriendlyString( vector.getX(), vector.getY(), vector.getZ() );
	}

	/**
	 * Create short human-readable representation of a vector.
	 *
	 * @param   x   X component of vector.
	 * @param   y   Y component of vector.
	 * @param   z   Z component of vector.
	 *
	 * @return  Human-readable representation of vector.
	 */
	public static String toShortFriendlyString( final double x, final double y, final double z )
	{
		final NumberFormat nf = ONE_DECIMAL_FORMAT;
		return '[' + nf.format( x ) + ',' + nf.format( y ) + ',' + nf.format( z ) + ']';
	}
}
