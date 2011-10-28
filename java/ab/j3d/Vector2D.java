/* $Id$
 * ====================================================================
 * AsoBrain 2D Toolkit
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

import ab.j3d.geom.*;

/**
 * This class represents a 2D vector.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Vector2D
	implements Serializable
{
	/**
	 * Zero-vector.
	 */
	public static final Vector2D ZERO = new Vector2D( 0.0, 0.0 );

	/**
	 * Positive X-axis direction vector.
	 */
	public static final Vector2D POSITIVE_X_AXIS = new Vector2D( 1.0, 0.0 );

	/**
	 * Negative X-axis direction vector.
	 */
	public static final Vector2D NEGATIVE_X_AXIS = new Vector2D( -1.0, 0.0 );

	/**
	 * Positive Y-axis direction vector.
	 */
	public static final Vector2D POSITIVE_Y_AXIS = new Vector2D( 0.0, 1.0 );

	/**
	 * Negative Y-axis direction vector.
	 */
	public static final Vector2D NEGATIVE_Y_AXIS = new Vector2D( 0.0, -1.0 );

	/**
	 * X component of 2D vector.
	 */
	public final double x;

	/**
	 * Y component of 2D vector.
	 */
	public final double y;

	/**
	 * Number format with one fraction digit.
	 */
	protected static final NumberFormat ONE_DECIMAL_FORMAT;

	/**
	 * Number format with two fraction digits.
	 */
	protected static final NumberFormat TWO_DECIMAL_FORMAT;

	static
	{
		final NumberFormat oneDecimal = NumberFormat.getNumberInstance( Locale.US );
		oneDecimal.setMinimumFractionDigits( 1 );
		oneDecimal.setMaximumFractionDigits( 1 );
		oneDecimal.setGroupingUsed( false );
		ONE_DECIMAL_FORMAT = oneDecimal;

		final NumberFormat twoDecimals = NumberFormat.getNumberInstance( Locale.US );
		twoDecimals.setMinimumFractionDigits( 2 );
		twoDecimals.setMaximumFractionDigits( 2 );
		twoDecimals.setGroupingUsed( false );
		TWO_DECIMAL_FORMAT = twoDecimals;
	}

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 3298170395879862547L;

	/**
	 * Construct new vector.
	 *
	 * @param   x   X-coordinate of vector.
	 * @param   y   Y-coordinate of vector.
	 */
	public Vector2D( final double x, final double y )
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Get X-coordinate of vector.
	 *
	 * @return  X-coordinate of vector.
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * Get Y-coordinate of vector.
	 *
	 * @return  Y-coordinate of vector.
	 */
	public double getY()
	{
		return y;
	}

	/**
	 * Get angle between this vector and another one specified as argument.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  angle between vectors in radians.
	 */
	public static double angle( final Vector2D v1, final Vector2D v2 )
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
	public static boolean areParallel( final Vector2D v1, final Vector2D v2 )
	{
		return GeometryTools.almostEqual( Math.abs( cosAngle( v1, v2 ) ), 1.0 );
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
	public static boolean areSameDirection( final Vector2D v1, final Vector2D v2 )
	{
		return GeometryTools.almostEqual( cosAngle( v1, v2 ), 1.0 );
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
	public static boolean arePerpendicular( final Vector2D v1, final Vector2D v2 )
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
	public static double cosAngle( final Vector2D v1, final Vector2D v2 )
	{
		final double l = v1.length() * v2.length();
		return ( l == 0.0 ) ? 0.0 : ( dot( v1, v2 ) / l );
	}

	/**
	 * Determine Z component of cross product between two vectors.
	 *
	 * @param   x1      X-coordinate of first vector operand.
	 * @param   y1      Y-coordinate of first vector operand.
	 * @param   x2      X-coordinate of second vector operand.
	 * @param   y2      Y-coordinate of second vector operand.
	 *
	 * @return  Resulting vector.
	 */
	public static double crossZ( final double x1, final double y1, final double x2, final double y2 )
	{
		return x1 * y2 - y1 * x2;
	}

	/**
	 * Determine Z component of 3D cross vector between two 2D vectors.
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  Resulting vector.
	 */
	public static double crossZ( final Vector2D v1, final Vector2D v2 )
	{
		return crossZ( v1.getX(), v1.getY(), v2.getX(), v2.getY() );
	}

	/**
	 * Calculate distance between two point vectors.
	 *
	 * @param   p1      First point vector to calculate the distance between.
	 * @param   p2      Second point vector to calculate the distance between.
	 *
	 * @return  Distance between this and the specified other vector.
	 */
	public static double distanceBetween( final Vector2D p1, final Vector2D p2 )
	{
		return length( p1.getX() - p2.getX(), p1.getY() - p2.getY() );
	}

	/**
	 * Calculate distance between this point vector and another.
	 *
	 * @param   other   Point vector to calculate the distance to.
	 *
	 * @return  Distance between this and the other vector.
	 */
	public double distanceTo( final Vector2D other )
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
	public static Vector2D direction( final Vector2D from, final Vector2D to )
	{
		return direction( from.getX(), from.getY(), to.getX(), to.getY() );
	}

	/**
	 * Get direction from one point to another point.
	 *
	 * @param   x1      X coordinate of from-point.
	 * @param   y1      Y coordinate of from-point.
	 * @param   x2      X coordinate of to-point.
	 * @param   y2      Y coordinate of to-point.
	 *
	 * @return  Direction from from-point to to-point.
	 */
	public static Vector2D direction( final double x1, final double y1, final double x2, final double y2 )
	{
		return normalize( x2 - x1, y2 - y1 );
	}

	/**
	 * Get direction from this point vector to another.
	 *
	 * @param   other   Point vector to calculate the direction to.
	 *
	 * @return  Direction from this to the other vector.
	 */
	public Vector2D directionTo( final Vector2D other )
	{
		return direction( getX(), getY(), other.getX(), other.getY() );
	}

	/**
	 * Get direction from this point vector to another.
	 *
	 * @param   x       X coordinate of point to calculate the direction to.
	 * @param   y       Y coordinate of point to calculate the direction to.
	 *
	 * @return  Direction from this to the other vector.
	 */
	public Vector2D directionTo( final double x, final double y )
	{
		return direction( getX(), getY(), x, y );
	}

	/**
	 * Calculate average of two vectors (i.e. center between two point vectors).
	 *
	 * @param   v1  First vector.
	 * @param   v2  Second vector.
	 *
	 * @return  Average vector (i.e. center point).
	 */
	public static Vector2D average( final Vector2D v1, final Vector2D v2 )
	{
		return v1.equals( v2 ) ? v1 : new Vector2D( 0.5 * ( v1.getX() + v2.getX() ), 0.5 * ( v1.getY() + v2.getY() ) );
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector and another
	 * one specified as argument.
	 *
	 * @param   x1      X-coordinate of first vector operand.
	 * @param   y1      Y-coordinate of first vector operand.
	 * @param   x2      X-coordinate of second vector operand.
	 * @param   y2      Y-coordinate of second vector operand.
	 *
	 * @return  Dot product.
	 */
	public static double dot( final double x1, final double y1, final double x2, final double y2 )
	{
		return x1 * x2 + y1 * y2;
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
	 * @param   v1      First vector operand.
	 * @param   v2      Second vector operand.
	 *
	 * @return  Dot product.
	 */
	public static double dot( final Vector2D v1, final Vector2D v2 )
	{
		return dot( v1.getX(), v1.getY(), v2.getX(), v2.getY() );
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
	public boolean almostEquals( final Vector2D other )
	{
		return ( other == this ) || ( ( other != null ) && almostEquals( other.getX(), other.getY() ) );
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param   otherX  X-coordinate of vector to compare with.
	 * @param   otherY  Y-coordinate of vector to compare with.
	 *
	 * @return  <code>true</code> if the objects are almost equal;
	 *          <code>false</code> if not.
	 *
	 * @see     GeometryTools#almostEqual
	 */
	public boolean almostEquals( final double otherX, final double otherY )
	{
		return GeometryTools.almostEqual( getX(), otherX ) &&
		       GeometryTools.almostEqual( getY(), otherY );
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param   otherX  X-coordinate of vector to compare with.
	 * @param   otherY  Y-coordinate of vector to compare with.
	 *
	 * @return  <code>true</code> if vectors are equal;
	 *          <code>false</code> if not.
	 */
	public boolean equals( final double otherX, final double otherY )
	{
		return ( ( otherX == getX() ) && ( otherY == getY() ) );
	}

	@Override
	public boolean equals( final Object other )
	{
		final boolean result;

		if ( other == this )
		{
			result = true;
		}
		else if ( !( other instanceof Vector2D ) )
		{
			result = false;
		}
		else
		{
			final Vector2D v = (Vector2D)other;
			result = ( ( getX() == v.getX() ) && ( getY() == v.getY() ) );
		}

		return result;
	}

	@Override
	public int hashCode()
	{
		long l;
		return (int)( ( l = Double.doubleToLongBits( getX() ) ) ^ ( l >>> 32 ) ^
		              ( l = Double.doubleToLongBits( getY() ) ) ^ ( l >>> 32 ) );
	}

	/**
	 * Convert string representation of vector back to {@link Vector2D}
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
	public static Vector2D fromString( final String value )
	{
		if ( value == null )
		{
			throw new NullPointerException( "value" );
		}

		final int comma = value.indexOf( (int)',' );
		if ( comma < 1 )
		{
			throw new IllegalArgumentException( value );
		}

		final double x = Double.parseDouble( value.substring( 0, comma ) );
		final double y = Double.parseDouble( value.substring( comma + 1 ) );

		return ZERO.getInstance( x, y );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return  Length of vector.
	 */
	public double length()
	{
		return length( getX(), getY() );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @param   x   X-component of vector.
	 * @param   y   Y-component of vector.
	 *
	 * @return  Length of vector.
	 */
	public static double length( final double x, final double y )
	{
		return Math.sqrt( x * x + y * y );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   other   Vector to subtract from this vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2D minus( final Vector2D other )
	{
		return minus( other.getX(), other.getY() );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2D minus( final double otherX, final double otherY )
	{
		return getInstance( getX() - otherX, getY() - otherY );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   factor  Scale multiplication factor.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2D multiply( final double factor )
	{
		return getInstance( getX() * factor, getY() * factor );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1,
	 * it will be returned as-is.
	 *
	 * @return  Normalized vector.
	 */
	public Vector2D normalize()
	{
		final double l = length();
		return ( ( l == 0.0 ) || ( l == 1.0 ) ) ? this : getInstance( getX() / l, getY() / l );
	}

	/**
	 * Normalize the specified vector. If the vector has length 0 or 1, a
	 * 0-vector will be returned.
	 *
	 * @param   x   X-component of vector.
	 * @param   y   Y-component of vector.
	 *
	 * @return  Normalized vector.
	 */
	public static Vector2D normalize( final double x, final double y )
	{
		final double l = length( x, y );
		return ( l == 0.0 ) ? ZERO : new Vector2D( x / l, y / l );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   other   Vector to add to this vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2D plus( final Vector2D other )
	{
		return plus( other.getX(), other.getY() );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2D plus( final double otherX, final double otherY )
	{
		return getInstance( getX() + otherX, getY() + otherY );
	}

	/**
	 * Set vector to the specified coordinates.
	 *
	 * @param   x   X-coordinate of vector.
	 * @param   y   Y-coordinate of vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2D getInstance( final double x, final double y )
	{
		return ZERO.equals( x, y ) ? ZERO : equals( x, y ) ? this : new Vector2D( x, y );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return  String representation of object.
	 */
	@Override
	public String toString()
	{
		return getX() + "," + getY();
	}

	/**
	 * Create human-readable representation of this {@link Vector2D} object.
	 * This is especially useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this {@link Vector2D} object.
	 */
	public String toFriendlyString()
	{
		return toFriendlyString( getX(), getY() );
	}

	/**
	 * Create human-readable representation of {@link Vector2D} object.
	 * This is especially useful for debugging purposes.
	 *
	 * @param   vector   Vector2D instance (<code>null</code> produces 'null').
	 *
	 * @return  Human-readable representation of {@link Vector2D} object.
	 */
	public static String toFriendlyString( final Vector2D vector )
	{
		return ( vector == null ) ? "null" : toFriendlyString( vector.getX(), vector.getY() );
	}

	/**
	 * Create human-readable representation of a vector.
	 *
	 * @param   x   X component of vector.
	 * @param   y   Y component of vector.
	 *
	 * @return  Human-readable representation of vector.
	 */
	public static String toFriendlyString( final double x, final double y )
	{
		final NumberFormat df = TWO_DECIMAL_FORMAT;
		return "[ " + df.format( x ) + ", " + df.format( y ) + " ]";
	}

	/**
	 * Create short human-readable representation of this vector.
	 *
	 * @return  Human-readable representation of {@link Vector2D} object.
	 */
	public String toShortFriendlyString()
	{
		return toShortFriendlyString( getX(), getY() );
	}

	/**
	 * Create short human-readable representation of a vector.
	 *
	 * @param   vector   {@link Vector2D} (<code>null</code> produces 'null').
	 *
	 * @return  Human-readable representation of {@link Vector2D} object.
	 */
	public static String toShortFriendlyString( final Vector2D vector )
	{
		return ( vector == null ) ? "null" : toShortFriendlyString( vector.getX(), vector.getY() );
	}

	/**
	 * Create short human-readable representation of a vector.
	 *
	 * @param   x   X component of vector.
	 * @param   y   Y component of vector.
	 *
	 * @return  Human-readable representation of vector.
	 */
	public static String toShortFriendlyString( final double x, final double y )
	{
		final NumberFormat nf = ONE_DECIMAL_FORMAT;
		return '[' + nf.format( x ) + ',' + nf.format( y ) + ']';
	}
}
