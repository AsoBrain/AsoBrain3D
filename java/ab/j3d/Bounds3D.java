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

import java.util.*;

/**
 * This class represents rectangular 3D bounds (specified by two vectors).
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Bounds3D
{
	/**
	 * First vector component of box. Normally the minimum vector.
	 *
	 * @see #sort
	 */
	public final Vector3D v1;

	/**
	 * Second vector component of box. Normally the maximum vector.
	 *
	 * @see #sort
	 */
	public final Vector3D v2;

	/**
	 * Empty box defined by zero-vectors.
	 */
	public static final Bounds3D EMPTY = new Bounds3D( Vector3D.ZERO, Vector3D.ZERO );

	/**
	 * Create a new box.
	 *
	 * @param   v1  First vector of box.
	 * @param   v2  Second vector of box.
	 */
	public Bounds3D( final Vector3D v1, final Vector3D v2 )
	{
		this.v1 = ( v1 == null ) ? Vector3D.ZERO : v1;
		this.v2 = ( v2 == null ) ? Vector3D.ZERO : v2;
	}

	/**
	 * Create a new box.
	 *
	 * @param   x1  X coordinate of first vector.
	 * @param   y1  Y coordinate of first vector.
	 * @param   z1  Z coordinate of first vector.
	 * @param   x2  X coordinate of second vector.
	 * @param   y2  Y coordinate of second vector.
	 * @param   z2  Z coordinate of second vector.
	 */
	public Bounds3D( final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		v1 = new Vector3D( x1, y1, z1 );
		v2 = new Vector3D( x2, y2, z2 );
	}

	/**
	 * Get <code>Bounds3D</code> property with the specified name from a
	 * {@link Properties} object.
	 *
	 * @param   properties  Properties to get bounds from.
	 * @param   name        Property name.
	 *
	 * @return  <code>Bounds3D</code> object;
	 *          <code>null</code> if property value is absent/invalid.
	 */
	public static Bounds3D getProperty( final Properties properties, final String name )
	{
		return getProperty( properties, name, null );
	}

	/**
	 * Get <code>Bounds3D</code> property with the specified name from a
	 * {@link Properties} object.
	 *
	 * @param   properties      Properties to get bounds from.
	 * @param   name            Property name.
	 * @param   defaultValue    Value to use if property value is absent/invalid.
	 *
	 * @return  <code>Bounds3D</code> object;
	 *          <code>defaultValue</code> if property value is absent/invalid.
	 */
	public static Bounds3D getProperty( final Properties properties, final String name, final Bounds3D defaultValue )
	{
		Bounds3D result = defaultValue;

		if ( properties != null )
		{
			final String stringValue = properties.getProperty( name, null );
			if ( stringValue != null )
			{
				try
				{
					result = fromString( stringValue );
				}
				catch ( Exception e )
				{
					/* ignore errors => return default */
				}
			}
		}

		return result;
	}

	/**
	 * Get center point of these bounds.
	 *
	 * @return  Vector describing boundary center (average of coordinates).
	 */
	public Vector3D center()
	{
		return v1.set( ( v1.getX() + v2.getX() ) / 2.0, ( v1.y + v2.y ) / 2.0, ( v1.getZ() + v2.getZ() ) / 2.0 );
	}

	/**
	 * Get center X.
	 *
	 * @return  Center X (avaerage of X coordinate of vector 1 and vector 2).
	 */
	public double centerX()
	{
		return 0.5 * ( v1.x + v2.x );
	}

	/**
	 * Get center Y.
	 *
	 * @return  Center Y (avaerage of Y coordinate of vector 1 and vector 2).
	 */
	public double centerY()
	{
		return 0.5 * ( v1.y + v2.y );
	}

	/**
	 * Get center Z.
	 *
	 * @return  Center Z (avaerage of Z coordinate of vector 1 and vector 2).
	 */
	public double centerZ()
	{
		return 0.5 * ( v1.z + v2.z );
	}

	/**
	 * Get delta X.
	 *
	 * @return  Delta X (X coordinate of vector 1 substracted from vector 2).
	 */
	public double deltaX()
	{
		return v2.x - v1.x;
	}

	/**
	 * Get delta Y.
	 *
	 * @return  Delta Y (Y coordinate of vector 1 substracted from vector 2).
	 */
	public double deltaY()
	{
		return v2.y - v1.y;
	}

	/**
	 * Get delta Z.
	 *
	 * @return  Delta Z (Z coordinate of vector 1 substracted from vector 2).
	 */
	public double deltaZ()
	{
		return v2.z - v1.z;
	}

	/**
	 * Get volume contained within these bounds.
	 *
	 * @return  Volume contained within these bounds.
	 */
	public double volume()
	{
		return Math.abs( v2.x - v1.x ) * Math.abs( v2.y - v1.y ) * Math.abs( v2.z - v1.z );
	}

	/**
	 * Test whether these bounds are empty. This returns true if these bounds
	 * describe a zero-volume, meaning that any of the coordinates is equal for
	 * both vectors.
	 *
	 * @return  <code>true</code> if the bounds are empty (zero volume);
	 *          <code>false</code> if the bounds are not empty (non-zero volume).
	 */
	public boolean isEmpty()
	{
		return ( ( v1.x == v2.x ) || ( v1.y == v2.y ) || ( v1.z == v2.z ) );
	}

	/**
	 * Test whether these bounds are sorted. This means that each coordinate of
	 * the first vector is lesser or equal to the same coordinate of the second
	 * vector.
	 *
	 * @return  <code>true</code> if the bounds are sorted;
	 *          <code>false</code> if the bounds are not sorted.
	 */
	public boolean isSorted()
	{
		return ( ( v1.x <= v2.x ) && ( v1.y <= v2.y ) && ( v1.z <= v2.z ) );
	}

	/**
	 * Test if this bounds contains the specified point.
	 *
	 * @param   point   Point to test.
	 *
	 * @return  <code>true</code> if this bounds contains the specified point;
	 *          <code>false</code> if the point is outside these bounds.
	 */
	public boolean contains( final Vector3D point )
	{
		return contains( point.x, point.y, point.z );
	}

	/**
	 * Test if this bounds contains the specified point.
	 *
	 * @param   x   X-coordinate of point.
	 * @param   y   Y-coordinate of point.
	 * @param   z   Z-coordinate of point.
	 *
	 * @return  <code>true</code> if this bounds contains the specified point;
	 *          <code>false</code> if the point is outside these bounds.
	 */
	public boolean contains( final double x, final double y, final double z )
	{
		return ( x >= Math.min( v1.x, v2.x ) ) && ( x <= Math.max( v1.x, v2.x ) ) &&
		       ( y >= Math.min( v1.y, v2.y ) ) && ( y <= Math.max( v1.y, v2.y ) ) &&
		       ( z >= Math.min( v1.z, v2.z ) ) && ( z <= Math.max( v1.z, v2.z ) );
	}

	/**
	 * Compare these bounds to the specified bounds.
	 *
	 * @param   otherV1     First vector of bounds to compare with.
	 * @param   otherV2     Second vector of bounds to compare with.
	 *
	 * @return  <code>true</code> if the bounds are equal,
	 *          <code>false</code> if not.
	 */
	public boolean equals( final Vector3D otherV1, final Vector3D otherV2 )
	{
		return( ( ( otherV1 == null ) || v1.equals( otherV1 ) ) &&
				( ( otherV2 == null ) || v2.equals( otherV2 ) ) );
	}

	@Override
	public boolean equals( final Object other )
	{
		final boolean result;

		if ( other == this )
		{
			result = true;
		}
		else if ( !( other instanceof Bounds3D ) )
		{
			result = false;
		}
		else
		{
			final Bounds3D b = (Bounds3D)other;
			result = ( v1.equals( b.v1 ) && v2.equals( b.v2 ) );
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return v1.hashCode() ^ v2.hashCode();
	}

	/**
	 * Convert string representation of bounds back to {@link Bounds3D}
	 * instance (see {@link #toString}).
	 *
	 * @param   value   String representation of object.
	 *
	 * @return  Bounds3D instance.
	 *
	 * @throws  NullPointerException if <code>value</code> is <code>null</code>.
	 * @throws  IllegalArgumentException if the string format is unrecognized.
	 * @throws  NumberFormatException if any of the numeric components are badly formatted.
	 *
	 * @see     #toString()
	 */
	public static Bounds3D fromString( final String value )
	{
		if ( value == null )
		{
			throw new NullPointerException( "value" );
		}

		final int semi = value.indexOf( (int)';' );
		if ( semi < 1 )
		{
			throw new IllegalArgumentException( "semi" );
		}

		final Vector3D v1 = Vector3D.fromString( value.substring( 0, semi ) );
		final Vector3D v2 = Vector3D.fromString( value.substring( semi + 1 ) );

		return EMPTY.set( v1, v2 );
	}

	/**
	 * Calculate intersection between two bounding boxes. Note that the result
	 * will have one or more negative factors for v2 - v1 when the bounding
	 * boxes are disjunct.
	 *
	 * @param   bounds1     First object for intersection.
	 * @param   bounds2     Seconds object for intersection.
	 *
	 * @return  Bounds of intersection.
	 */
	public static Bounds3D intersect( final Bounds3D bounds1, final Bounds3D bounds2 )
	{
		return rebuild( bounds1, bounds2,
			Math.max( Math.min( bounds1.v1.x, bounds1.v2.x ), Math.min( bounds2.v1.x, bounds2.v2.x ) ),
			Math.max( Math.min( bounds1.v1.y, bounds1.v2.y ), Math.min( bounds2.v1.y, bounds2.v2.y ) ),
			Math.max( Math.min( bounds1.v1.z, bounds1.v2.z ), Math.min( bounds2.v1.z, bounds2.v2.z ) ),
			Math.min( Math.max( bounds1.v1.x, bounds1.v2.x ), Math.max( bounds2.v1.x, bounds2.v2.x ) ),
			Math.min( Math.max( bounds1.v1.y, bounds1.v2.y ), Math.max( bounds2.v1.y, bounds2.v2.y ) ),
			Math.min( Math.max( bounds1.v1.z, bounds1.v2.z ), Math.max( bounds2.v1.z, bounds2.v2.z ) ) );
	}

	/**
	 * Determine whether the two specified bounding boxes intersect.
	 *
	 * @param   bounds1     First object for intersection test.
	 * @param   bounds2     Seconds object for intersection test.
	 *
	 * @return  <code>true</code> if the bounds intersect;
	 *          <code>false</code> if the bounds are disjunct.
	 */
	public static boolean intersects( final Bounds3D bounds1, final Bounds3D bounds2 )
	{
		return ( bounds1 != null )
		    && ( bounds2 != null )
		    && ( Math.min( bounds1.v1.x, bounds1.v2.x ) < Math.max( bounds2.v1.x, bounds2.v2.x ) )
		    && ( Math.min( bounds2.v1.x, bounds2.v2.x ) < Math.max( bounds1.v1.x, bounds1.v2.x ) )
		    && ( Math.min( bounds1.v1.y, bounds1.v2.y ) < Math.max( bounds2.v1.y, bounds2.v2.y ) )
		    && ( Math.min( bounds2.v1.y, bounds2.v2.y ) < Math.max( bounds1.v1.y, bounds1.v2.y ) )
		    && ( Math.min( bounds1.v1.z, bounds1.v2.z ) < Math.max( bounds2.v1.z, bounds2.v2.z ) )
		    && ( Math.min( bounds2.v1.z, bounds2.v2.z ) < Math.max( bounds1.v1.z, bounds1.v2.z ) );
	}

	/**
	 * Determine whether the two specified bounding boxes intersect. This method
	 * does not return <code>true</code> if the intersection along any axis is
	 * less than the specified <code>epsilon</code> value.
	 *
	 * @param   bounds1     First object for intersection test.
	 * @param   bounds2     Seconds object for intersection test.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  <code>true</code> if the bounds intersect;
	 *          <code>false</code> if the bounds are disjunct.
	 */
	public static boolean intersects( final Bounds3D bounds1, final Bounds3D bounds2, final double epsilon )
	{
		final boolean result;

		if ( ( bounds1 != null ) && ( bounds2 != null ) )
		{
			result = MathTools.significantlyLessThan( Math.min( bounds1.v1.x, bounds1.v2.x ), Math.max( bounds2.v1.x, bounds2.v2.x ), epsilon )
		          && MathTools.significantlyLessThan( Math.min( bounds2.v1.x, bounds2.v2.x ), Math.max( bounds1.v1.x, bounds1.v2.x ), epsilon )
		          && MathTools.significantlyLessThan( Math.min( bounds1.v1.y, bounds1.v2.y ), Math.max( bounds2.v1.y, bounds2.v2.y ), epsilon )
		          && MathTools.significantlyLessThan( Math.min( bounds2.v1.y, bounds2.v2.y ), Math.max( bounds1.v1.y, bounds1.v2.y ), epsilon )
		          && MathTools.significantlyLessThan( Math.min( bounds1.v1.z, bounds1.v2.z ), Math.max( bounds2.v1.z, bounds2.v2.z ), epsilon )
		          && MathTools.significantlyLessThan( Math.min( bounds2.v1.z, bounds2.v2.z ), Math.max( bounds1.v1.z, bounds1.v2.z ), epsilon );
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Calculate joined bounds of this bounding object and the specified point.
	 *
	 * @param   point   Point to join in bounds.
	 *
	 * @return  Joined bounds.
	 */
	public Bounds3D join( final Vector3D point )
	{
		return join( this, point.x, point.y, point.z );
	}

	/**
	 * Calculate joined bounds of this bounding object and the specified point.
	 *
	 * @param   x   X coordinate of point.
	 * @param   y   Y coordinate of point.
	 * @param   z   Z coordinate of point.
	 *
	 * @return  Joined bounds.
	 */
	public Bounds3D join( final double x, final double y, final double z )
	{
		return join( this, x, y, z );
	}

	/**
	 * Calculate joined bounds of a bounding object and a point.
	 *
	 * @param   bounds  Bounding object to join.
	 * @param   point   Point to join in bounds.
	 *
	 * @return  Joined bounds.
	 */
	public static Bounds3D join( final Bounds3D bounds, final Vector3D point )
	{
		return join( bounds, point.x, point.y, point.z );
	}

	/**
	 * Calculate joined bounds of a bounding object and a point.
	 *
	 * @param   bounds  Bounding object to join.
	 * @param   x       X coordinate of point.
	 * @param   y       Y coordinate of point.
	 * @param   z       Z coordinate of point.
	 *
	 * @return  Joined bounds.
	 */
	public static Bounds3D join( final Bounds3D bounds, final double x, final double y, final double z )
	{
		final Vector3D v1 = bounds.v1;
		final Vector3D v2 = bounds.v2;

		return bounds.set( Math.min( x, Math.min( v1.x, v2.x ) ),
		                   Math.min( y, Math.min( v1.y, v2.y ) ),
		                   Math.min( z, Math.min( v1.z, v2.z ) ),
		                   Math.max( x, Math.max( v1.x, v2.x ) ),
		                   Math.max( y, Math.max( v1.y, v2.y ) ),
		                   Math.max( z, Math.max( v1.z, v2.z ) ) );
	}

	/**
	 * Calculate joined bounds of this and other bounding object.
	 *
	 * @param   other   Bounds to join with.
	 *
	 * @return  Joined bounds.
	 */
	public Bounds3D join( final Bounds3D other )
	{
		return join ( this, other );
	}

	/**
	 * Calculate joined bounds of the two specified bounding objects.
	 *
	 * @param   bounds1     First object for join.
	 * @param   bounds2     Seconds object for join.
	 *
	 * @return  Joined bounds.
	 */
	public static Bounds3D join( final Bounds3D bounds1, final Bounds3D bounds2 )
	{
		return ( bounds1 == null ) ? bounds2 : ( bounds2 == null ) ? bounds1 : rebuild( bounds1, bounds2,
			Math.min( Math.min( bounds1.v1.x, bounds1.v2.x ), Math.min( bounds2.v1.x, bounds2.v2.x ) ),
			Math.min( Math.min( bounds1.v1.y, bounds1.v2.y ), Math.min( bounds2.v1.y, bounds2.v2.y ) ),
			Math.min( Math.min( bounds1.v1.z, bounds1.v2.z ), Math.min( bounds2.v1.z, bounds2.v2.z ) ),
			Math.max( Math.max( bounds1.v1.x, bounds1.v2.x ), Math.max( bounds2.v1.x, bounds2.v2.x ) ),
			Math.max( Math.max( bounds1.v1.y, bounds1.v2.y ), Math.max( bounds2.v1.y, bounds2.v2.y ) ),
			Math.max( Math.max( bounds1.v1.z, bounds1.v2.z ), Math.max( bounds2.v1.z, bounds2.v2.z ) ) );
	}

	/**
	 * Determine maximum vector of box.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D max()
	{
		return max( this );
	}

	/**
	 * Determine maximum vector of box.
	 *
	 * @param   box     Box to get the vector for.
	 *
	 * @return  Resulting vector.
	 */
	public static Vector3D max( final Bounds3D box )
	{
		final Vector3D result;

		final double x = Math.max( box.v1.x, box.v2.x );
		final double y = Math.max( box.v1.y, box.v2.y );
		final double z = Math.max( box.v1.z, box.v2.z );

		if ( box.v1.equals( x, y, z ) )
		{
			result = box.v1;
		}
		else if ( box.v2.equals( x, y, z ) )
		{
			result = box.v2;
		}
		else
		{
			result = new Vector3D( x, y, z );
		}

		return result;
	}

	/**
	 * Determine minimum vector of bounds.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D min()
	{
		return min( this );
	}

	/**
	 * Determine minimum vector of bounds.
	 *
	 * @param   bounds  Bounds to get the vector for.
	 *
	 * @return  Resulting vector.
	 */
	public static Vector3D min( final Bounds3D bounds )
	{
		final Vector3D result;

		final double x = Math.min( bounds.v1.x, bounds.v2.x );
		final double y = Math.min( bounds.v1.y, bounds.v2.y );
		final double z = Math.min( bounds.v1.z, bounds.v2.z );

		if ( bounds.v1.equals( x, y, z ) )
		{
			result = bounds.v1;
		}
		else if ( bounds.v2.equals( x, y, z ) )
		{
			result = bounds.v2;
		}
		else
		{
			result = new Vector3D( x, y, z );
		}

		return result;
	}

	/**
	 * Subtract vector from bounds.
	 *
	 * @param   vector  Vector to subtract from bounds.
	 *
	 * @return  Resulting bounds.
	 */
	public Bounds3D minus( final Vector3D vector )
	{
		return minus( vector.x, vector.y, vector.z );
	}

	/**
	 * Subtract vector from bounds.
	 *
	 * @param   x   X-coordinate of vector to subtract.
	 * @param   y   Y-coordinate of vector to subtract.
	 * @param   z   Z-coordinate of vector to subtract.
	 *
	 * @return  Resulting bounds.
	 */
	public Bounds3D minus( final double x, final double y, final double z )
	{
		return ( ( x == 0.0 ) && ( y == 0.0 ) && ( z == 0.0 ) ) ? this
		     : set( v1.minus( x, y, z ), v2.minus( x, y, z ) );
	}

	/**
	 * Determine box after scalar multiplication.
	 *
	 * @param   factor      Scale multiplication factor.
	 *
	 * @return  Resulting box.
	 */
	public Bounds3D multiply( final double factor )
	{
		return set( v1.multiply( factor ), v2.multiply( factor ) );
	}

	/**
	 * Add a vector to bounds.
	 *
	 * @param   vector      Vector to add to bounds.
	 *
	 * @return  Resulting bounds.
	 */
	public Bounds3D plus( final Vector3D vector )
	{
		return plus( vector.x, vector.y, vector.z );
	}

	/**
	 * Add a vector to bounds.
	 *
	 * @param   x   X-coordinate of vector to add.
	 * @param   y   Y-coordinate of vector to add.
	 * @param   z   Z-coordinate of vector to add.
	 *
	 * @return  Resulting bounds.
	 */
	public Bounds3D plus( final double x, final double y, final double z )
	{
		return ( ( x == 0.0 ) && ( y == 0.0 ) && ( z == 0.0 ) ) ? this
		     : set( v1.plus( x, y, z ), v2.plus( x, y, z ) );
	}

	/**
	 * Construct new box from the specified coordinates, and try to reuse
	 * existing boxes.
	 *
	 * @param   box1    Reusable box object.
	 * @param   box2    Reusable box object.
	 * @param   x1      X component of desired first vector component.
	 * @param   y1      Y component of desired first vector component.
	 * @param   z1      Z component of desired first vector component.
	 * @param   x2      X component of desired second vector component.
	 * @param   y2      Y component of desired second vector component.
	 * @param   z2      Z component of desired second vector component.
	 *
	 * @return  Bounds3D object based on the desired coordinates.
	 */
	private static Bounds3D rebuild(
		final Bounds3D box1, final Bounds3D box2,
		final double x1, final double y1, final double z1,
		final double x2, final double y2, final double z2 )
	{
		final Bounds3D result;

		/*
		 * Try to reuse the existing vectors. If not possible, create
		 * new ones.
		 */
		final Vector3D v1;

		if ( box1.v1.equals( x1, y1, z1 ) )
		{
			v1 = box1.v1;
		}
		else if ( box1.v2.equals( x1, y1, z1 ) )
		{
			v1 = box1.v2;
		}
		else if ( box2.v1.equals( x1, y1, z1 ) )
		{
			v1 = box2.v1;
		}
		else if ( box2.v2.equals( x1, y1, z1 ) )
		{
			v1 = box2.v2;
		}
		else
		{
			v1 = new Vector3D( x1, y1, z1 );
		}

		final Vector3D v2;

		if ( box1.v1.equals( x2, y2, z2 ) )
		{
			v2 = box1.v1;
		}
		else if ( box1.v2.equals( x2, y2, z2 ) )
		{
			v2 = box1.v2;
		}
		else if ( box2.v1.equals( x2, y2, z2 ) )
		{
			v2 = box2.v1;
		}
		else if ( box2.v2.equals( x2, y2, z2 ) )
		{
			v2 = box2.v2;
		}
		else if ( v1.equals( x2, y2, z2 ) )
		{
			v2 = v1;
		}
		else
		{
			v2 = new Vector3D( x2, y2, z2 );
		}

		/*
		 * Try to reuse the existing boxes. If not possible, create
		 * a new one.
		 */
		if ( ( box1.v1 == v1 ) && ( box1.v2 == v2 ) )
		{
			result = box1;
		}
		else if ( ( box2.v1 == v1 ) && ( box2.v2 == v2 ) )
		{
			result = box2;
		}
		else
		{
			result = new Bounds3D( v1, v2 );
		}

		return result;
	}

	/**
	 * Set bounds to the specified coordinates.
	 *
	 * @param   x1      First X coordinate of bounds.
	 * @param   y1      First Y coordinate of bounds.
	 * @param   z1      First Z coordinate of bounds.
	 * @param   x2      Second X coordinate of bounds.
	 * @param   y2      Second Y coordinate of bounds.
	 * @param   z2      Second Z coordinate of bounds.
	 *
	 * @return  Resulting bounds.
	 */
	public Bounds3D set( final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		final Vector3D v0    = Vector3D.ZERO;
		final Vector3D oldV1 = v1;
		final Vector3D oldV2 = v2;
		final Vector3D newV1 = oldV1.set( x1, y1, z1 );
		final Vector3D newV2 = oldV2.set( x2, y2, z2 );

		return ( ( newV1 == oldV1 ) && ( newV2 == oldV2 ) ) ? this :
		       ( ( newV1 == v0    ) && ( newV2 == v0    ) ) ? EMPTY :
		       new Bounds3D( newV1, newV2 );
	}

	/**
	 * Set bounds to the specified vectors.
	 *
	 * @param   newV1   First vector of bounds to set.
	 * @param   newV2   Second vector of bounds to set.
	 *
	 * @return  Resulting bounds.
	 */
	public Bounds3D set( final Vector3D newV1, final Vector3D newV2 )
	{
		return ( ( ( newV1 == null ) || newV1.equals( v1 ) )
		      && ( ( newV2 == null ) || newV2.equals( v2 ) ) ) ? this
		     : new Bounds3D( ( newV1 == null ) ? v1 : newV1, ( newV2 == null ) ? v2 : newV2 );
	}

	/**
	 * Get size of these bounds.
	 *
	 * @return  Vector describing bound size (v2-v1).
	 */
	public Vector3D size()
	{
		return v2.set( Math.abs( v2.x - v1.x ), Math.abs( v2.y - v1.y ), Math.abs( v2.z - v1.z ) );
	}

	/**
	 * Get sixe along X axis.
	 *
	 * @return  Sixe along X (distance between X coordinates of vector 1 and 2).
	 */
	public double sizeX()
	{
		return Math.abs( v2.x - v1.x );
	}

	/**
	 * Get siye along Y axis.
	 *
	 * @return  Siye along Y (distance between Y coordinates of vector 1 and 2).
	 */
	public double sizeY()
	{
		return Math.abs( v2.y - v1.y );
	}

	/**
	 * Get size along Z axis.
	 *
	 * @return  Size along Z (distance between Z coordinates of vector 1 and 2).
	 */
	public double sizeZ()
	{
		return Math.abs( v2.z - v1.z );
	}

	/**
	 * Determine sorted bounds. If bounds are sorted, than the x/y/z
	 * components of {@link #v1} are always less or equal to the
	 * matching components of {@link #v2}.
	 *
	 * @return  Resulting bounds.
	 */
	public Bounds3D sorted()
	{
		return sort( this );
	}

	/**
	 * Get sorted bounds.
	 *
	 * @param   bounds  Bounds to sort.
	 *
	 * @return  Sorted bounds.
	 */
	public static Bounds3D sort( final Bounds3D bounds )
	{
		return bounds.set( min( bounds ), max( bounds ) );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return  String representation of object.
	 */
	@Override
	public String toString()
	{
		return v1 + ";" + v2;
	}

	/**
	 * Create human-readable representation of this {@link Bounds3D} object.
	 * This is aspecially useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this {@link Bounds3D} object.
	 */
	public String toFriendlyString()
	{
		return toFriendlyString( this );
	}

	/**
	 * Create human-readable representation of {@link Bounds3D} object.
	 * This is aspecially useful for debugging purposes.
	 *
	 * @param   bounds      Bounds3D value (<code>null</code> produces 'null').
	 *
	 * @return  Human-readable representation of {@link Bounds3D} object.
	 */
	public static String toFriendlyString( final Bounds3D bounds )
	{
		return ( bounds == null ) ? "null" : "[ " + bounds.v1.toFriendlyString() + ", " + bounds.v2.toFriendlyString() + " ]";
	}
}
