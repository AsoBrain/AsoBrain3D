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

/**
 * This class defines a 2D vector using single-precision floating-point values.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Vector2f
	implements Serializable
{
	/**
	 * Zero-vector.
	 */
	public static final Vector2f ZERO = new Vector2f( 0.0f, 0.0f );

	/**
	 * Positive X-axis direction vector.
	 */
	public static final Vector2f POSITIVE_X_AXIS = new Vector2f( 1.0f, 0.0f );

	/**
	 * Negative X-axis direction vector.
	 */
	public static final Vector2f NEGATIVE_X_AXIS = new Vector2f( -1.0f, 0.0f );

	/**
	 * Positive Y-axis direction vector.
	 */
	public static final Vector2f POSITIVE_Y_AXIS = new Vector2f( 0.0f, 1.0f );

	/**
	 * Negative Y-axis direction vector.
	 */
	public static final Vector2f NEGATIVE_Y_AXIS = new Vector2f( 0.0f, -1.0f );

	/**
	 * X component of 2D vector.
	 */
	private float _x;

	/**
	 * Y component of 2D vector.
	 */
	private float _y;

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 3198558829683527053L;

	/**
	 * Construct new vector.
	 *
	 * @param   x  X-coordinate of vector.
	 * @param   y  Y-coordinate of vector.
	 */
	public Vector2f( final float x, final float y )
	{
		_x = x;
		_y = y;
	}

	/**
	 * Get X component of 2D vector.
	 *
	 * @return  X component of 2D vector.
	 */
	public float getX()
	{
		return _x;
	}

	/**
	 * Get Y component of 2D vector.
	 *
	 * @return  Y component of 2D vector.
	 */
	public float getY()
	{
		return _y;
	}

	/**
	 * Set X component of 2D vector.
	 *
	 * @param   x   X component of 2D vector.
	 */
	public void setX( final float x )
	{
		_x = x;
	}

	/**
	 * Set Y component of 2D vector.
	 *
	 * @param   y   component of 2D vector.
	 */
	public void setY( final float y )
	{
		_y = y;
	}

	/**
	 * Set this vector to be identical to the source vector.
	 *
	 * @param   source  Source vector to copy.
	 */
	public void set( final Vector2f source )
	{
		set( source.getX(), source.getY() );
	}

	/**
	 * Set this vector to be identical to the source vector.
	 *
	 * @param   x  X-coordinate of vector.
	 * @param   y  Y-coordinate of vector.
	 */
	public void set( final float x, final float y )
	{
		setX( x );
		setY( y );
	}

	/**
	 * Get <code>Vector2D</code> property with the specified name from a
	 * {@link Properties} object.
	 *
	 * @param   properties  Properties to get vector from.
	 * @param   name        Property name.
	 *
	 * @return  <code>Vector2D</code> object;
	 *          <code>null</code> if property value is absent/invalid.
	 */
	public static Vector2f getProperty( final Properties properties, final String name )
	{
		return getProperty( properties, name, null );
	}

	/**
	 * Get <code>Vector2D</code> property with the specified name from a
	 * {@link Properties} object.
	 *
	 * @param   properties      Properties to get vector from.
	 * @param   name            Property name.
	 * @param   defaultValue    Value to use if property value is absent/invalid.
	 *
	 * @return  <code>Vector2D</code> object;
	 *          <code>defaultValue</code> if property value is absent/invalid.
	 */
	public static Vector2f getProperty( final Properties properties, final String name, final Vector2f defaultValue )
	{
		Vector2f result = defaultValue;

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
	public static float angle( final Vector2f v1, final Vector2f v2 )
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
	public static boolean areParallel( final Vector2f v1, final Vector2f v2 )
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
	public static boolean areSameDirection( final Vector2f v1, final Vector2f v2 )
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
	public static boolean arePerpendicular( final Vector2f v1, final Vector2f v2 )
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
	public static float cosAngle( final Vector2f v1, final Vector2f v2 )
	{
		final float l = v1.length() * v2.length();
		return ( l == 0.0f ) ? 0.0f : ( dot( v1, v2 ) / l );
	}

	/**
	 * Determine cross product of this vector with another vector.
	 *
	 * @param   x1  X-coordinate of first vector operand.
	 * @param   y1  Y-coordinate of first vector operand.
	 * @param   x2  X-coordinate of second vector operand.
	 * @param   y2  Y-coordinate of second vector operand.
	 *
	 * @return  Z component of 3D vector resulting from cross product between 2D vectors.
	 */
	public static float crossZ( final float x1, final float y1, final float x2, final float y2 )
	{
		return x1 * y2 - y1 * x2;
	}

	/**
	 * Determine 3D cross product between two 2D vectors and return the Z
	 * component of the resulting vector (the X/Y components are always zero).
	 *
	 * @param   v1      First vector.
	 * @param   v2      Second vector.
	 *
	 * @return  Z component of 3D vector resulting from cross product between 2D vectors.
	 */
	public static float crossZ( final Vector2f v1, final Vector2f v2 )
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
	public static float distanceBetween( final Vector2f p1, final Vector2f p2 )
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
	public float distanceTo( final Vector2f other )
	{
		return distanceBetween( this, other );
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
	public static float dot( final float x1, final float y1, final float x2, final float y2 )
	{
		return x1 * x2 + y1 * y2;
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
	public static float dot( final Vector2f v1, final Vector2f v2 )
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
	 * @see   MathTools#almostEqual
	 */
	public boolean almostEquals( final Vector2f other )
	{
		return ( other != null )
		       && ( ( other == this )
		            || ( MathTools.almostEqual( getX(), other.getX() ) &&
		                 MathTools.almostEqual( getY(), other.getY() ) ) );
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
	 * @see     MathTools#almostEqual
	 */
	public boolean almostEquals( final float otherX, final float otherY )
	{
		return MathTools.almostEqual( getX(), otherX ) &&
		       MathTools.almostEqual( getY(), otherY );
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
	public boolean equals( final float otherX, final float otherY )
	{
		return ( otherX == getX() ) && ( otherY == getY() );
	}

	public boolean equals( final Object other )
	{
		final boolean result;

		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof Vector2f )
		{
			final Vector2f v = (Vector2f)other;
			result = equals( v.getX(), v.getY() );
		}
		else
		{
			result = false;
		}

		return result;
	}

	public int hashCode()
	{
		return Float.floatToIntBits( getX() ) ^ Float.floatToIntBits( getY() );
	}

	/**
	 * Convert string representation of vector back to <code>Vector2D</code>
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
	public static Vector2f fromString( final String value )
	{
		if ( value == null )
		{
			throw new NullPointerException( "value" );
		}

		final int comma = value.indexOf( (int)',' );
		if ( comma < 1 )
		{
			throw new IllegalArgumentException( "comma" );
		}

		final float x = Float.parseFloat( value.substring( 0, comma ) );
		final float y = Float.parseFloat( value.substring( comma + 1 ) );

		return new Vector2f( x, y );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return  Length of vector.
	 */
	public float length()
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
	public static float length( final float x, final float y )
	{
		final double dx = (double)x;
		final double dy = (double)y;
		return (float)Math.sqrt( dx * dx + dy * dy );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   other   Vector to subtract from this vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2f minus( final Vector2f other )
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
	public Vector2f minus( final float otherX, final float otherY )
	{
		return new Vector2f( getX() - otherX, getY() - otherY );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   other   Vector to subtract from this vector.
	 */
	public void minusLocal( final Vector2f other )
	{
		minusLocal( other.getX(), other.getY() );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 */
	public void minusLocal( final float otherX, final float otherY )
	{
		set( getX() - otherX, getY() - otherY );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scale   Scale multiplication factor.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2f scale( final float scale )
	{
		return new Vector2f( getX() * scale, getY() * scale );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scaleX  X scale multiplication factor.
	 * @param   scaleY  Y scale multiplication factor.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2f scale( final float scaleX, final float scaleY )
	{
		return new Vector2f( getX() * scaleX, getY() * scaleY );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scale   Scale multiplication factor.
	 */
	public void scaleLocal( final float scale )
	{
		scaleLocal( scale, scale );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param   scaleX  X scale multiplication factor.
	 * @param   scaleY  Y scale multiplication factor.
	 */
	public void scaleLocal( final float scaleX, final float scaleY )
	{
		set( scaleX * getX(), scaleY * getY() );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1,
	 * it will be returned as-is.
	 *
	 * @return  Normalized vector.
	 */
	public Vector2f normalize()
	{
		final float l = length();
		return ( ( l == 0.0f ) || ( l == 1.0f ) ) ? this : new Vector2f( getX() / l, getY() / l );
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
	public static Vector2f normalize( final float x, final float y )
	{
		final float l = length( x, y );
		return ( l == 0.0f ) ? ZERO : new Vector2f( x / l, y / l );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   other   Vector to add to this vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2f plus( final Vector2f other )
	{
		return new Vector2f( getX() + other.getX(), getY() + other.getY() );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector2f plus( final float otherX, final float otherY )
	{
		return new Vector2f( getX() + otherX, getY() + otherY );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   other   Vector to add to this vector.
	 */
	public void plusLocal( final Vector2f other )
	{
		plusLocal( other.getX(), other.getY() );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param   otherX  X-coordinate of vector.
	 * @param   otherY  Y-coordinate of vector.
	 */
	public void plusLocal( final float otherX, final float otherY )
	{
		set( getX() + otherX, getY() + otherY );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return  String representation of object.
	 */
	public String toString()
	{
		return getX() + "," + getY();
	}

	/**
	 * Create human-readable representation of this <code>Vector2D</code> object.
	 * This is aspecially useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this <code>Vector2D</code> object.
	 */
	public String toFriendlyString()
	{
		return toFriendlyString( this );
	}

	/**
	 * Create human-readable representation of <code>Vector2D</code> object.
	 * This is aspecially useful for debugging purposes.
	 *
	 * @param   vector   Vector2D instance (<code>null</code> produces 'null').
	 *
	 * @return  Human-readable representation of <code>Vector2D</code> object.
	 */
	public static String toFriendlyString( final Vector2f vector )
	{
		final DecimalFormat df = new DecimalFormat( "0.00" );
		return ( vector == null ) ? "null"
			: "[ " + df.format( (double)vector.getX() ) +
			  ", " + df.format( (double)vector.getY() ) + " ]";
	}

	/**
	 * Make this vector the zero-vector.
	 */
	public void zero()
	{
		set( 0.0f, 0.0f );
	}
}
