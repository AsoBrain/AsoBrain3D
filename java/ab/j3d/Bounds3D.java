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
 * This class represents rectangular 3D bounds (specified by two vectors).
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Bounds3D
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
	 * Initial value of a box (0-box).
	 */
	public static final Bounds3D INIT = new Bounds3D( Vector3D.INIT , Vector3D.INIT );

	/**
	 * Create a new box.
	 *
	 * @param   v1  First vector of box.
	 * @param   v2  Second vector of box.
	 */
	private Bounds3D( final Vector3D v1 , final Vector3D v2 )
	{
		this.v1 = ( v1 == null ) ? Vector3D.INIT : v1;
		this.v2 = ( v2 == null ) ? Vector3D.INIT : v2;
	}

	/**
	 * Compare these bounds to the specified bounds.
	 *
	 * @param   v1  First vector of bounds to compare with.
	 * @param   v2  Second vector of bounds to compare with.
	 *
	 * @return  <code>true</code> if the bounds are equal,
	 *          <code>false</code> if not.
	 */
	public boolean equals( final Vector3D v1 , final Vector3D v2 )
	{
		return( ( ( v1 == null ) || this.v1.equals( v1 ) ) &&
				( ( v2 == null ) || this.v2.equals( v2 ) ) );
	}

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

	public int hashCode()
	{
		return v1.hashCode() ^ v2.hashCode();
	}

	/**
	 * Convert string representation of bounds back to <code>Bounds3D</code>
	 * instance (see <code>toString()</code>).
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
			throw new NullPointerException( "value" );

		final int semi = value.indexOf( (int)';' );
		if ( semi < 1 )
			throw new IllegalArgumentException( "semi" );

		final Vector3D v1 = Vector3D.fromString( value.substring( 0 , semi ) );
		final Vector3D v2 = Vector3D.fromString( value.substring( semi + 1 ) );

		return INIT.set( v1 , v2 );
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
	public static Bounds3D intersect( final Bounds3D bounds1 , final Bounds3D bounds2 )
	{
		return rebuild( bounds1 , bounds2 ,
			Math.max( Math.min( bounds1.v1.x , bounds1.v2.x ) , Math.min( bounds2.v1.x , bounds2.v2.x ) ) ,
			Math.max( Math.min( bounds1.v1.y , bounds1.v2.y ) , Math.min( bounds2.v1.y , bounds2.v2.y ) ) ,
			Math.max( Math.min( bounds1.v1.z , bounds1.v2.z ) , Math.min( bounds2.v1.z , bounds2.v2.z ) ) ,
			Math.min( Math.max( bounds1.v1.x , bounds1.v2.x ) , Math.max( bounds2.v1.x , bounds2.v2.x ) ) ,
			Math.min( Math.max( bounds1.v1.y , bounds1.v2.y ) , Math.max( bounds2.v1.y , bounds2.v2.y ) ) ,
			Math.min( Math.max( bounds1.v1.z , bounds1.v2.z ) , Math.max( bounds2.v1.z , bounds2.v2.z ) ) );
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
	public static boolean intersects( final Bounds3D bounds1 , final Bounds3D bounds2 )
	{
		return ( bounds1 != null )
		    && ( bounds2 != null )
		    && ( Math.min( bounds1.v1.x , bounds1.v2.x ) < Math.max( bounds2.v1.x , bounds2.v2.x ) )
		    && ( Math.min( bounds2.v1.x , bounds2.v2.x ) < Math.max( bounds1.v1.x , bounds1.v2.x ) )
		    && ( Math.min( bounds1.v1.y , bounds1.v2.y ) < Math.max( bounds2.v1.y , bounds2.v2.y ) )
		    && ( Math.min( bounds2.v1.y , bounds2.v2.y ) < Math.max( bounds1.v1.y , bounds1.v2.y ) )
		    && ( Math.min( bounds1.v1.z , bounds1.v2.z ) < Math.max( bounds2.v1.z , bounds2.v2.z ) )
		    && ( Math.min( bounds2.v1.z , bounds2.v2.z ) < Math.max( bounds1.v1.z , bounds1.v2.z ) );
	}

	/**
	 * Calculate joined bounds of the two specified bounding objects.
	 *
	 * @param   bounds1     First object for join.
	 * @param   bounds2     Seconds object for join.
	 *
	 * @return  Joined bounds.
	 */
	public static Bounds3D join( final Bounds3D bounds1 , final Bounds3D bounds2 )
	{
		return rebuild( bounds1 , bounds2 ,
			Math.min( Math.min( bounds1.v1.x , bounds1.v2.x ) , Math.min( bounds2.v1.x , bounds2.v2.x ) ) ,
			Math.min( Math.min( bounds1.v1.y , bounds1.v2.y ) , Math.min( bounds2.v1.y , bounds2.v2.y ) ) ,
			Math.min( Math.min( bounds1.v1.z , bounds1.v2.z ) , Math.min( bounds2.v1.z , bounds2.v2.z ) ) ,
			Math.max( Math.max( bounds1.v1.x , bounds1.v2.x ) , Math.max( bounds2.v1.x , bounds2.v2.x ) ) ,
			Math.max( Math.max( bounds1.v1.y , bounds1.v2.y ) , Math.max( bounds2.v1.y , bounds2.v2.y ) ) ,
			Math.max( Math.max( bounds1.v1.z , bounds1.v2.z ) , Math.max( bounds2.v1.z , bounds2.v2.z ) ) );
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

		final double x = Math.max( box.v1.x , box.v2.x );
		final double y = Math.max( box.v1.y , box.v2.y );
		final double z = Math.max( box.v1.z , box.v2.z );

			 if ( box.v1.equals( x , y , z ) ) result = box.v1;
		else if ( box.v2.equals( x , y , z ) ) result = box.v2;
		else result = Vector3D.INIT.set( x , y , z );

		return result;
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

		final double x = Math.min( bounds.v1.x , bounds.v2.x );
		final double y = Math.min( bounds.v1.y , bounds.v2.y );
		final double z = Math.min( bounds.v1.z , bounds.v2.z );

			 if ( bounds.v1.equals( x , y , z ) ) result = bounds.v1;
		else if ( bounds.v2.equals( x , y , z ) ) result = bounds.v2;
		else result = Vector3D.INIT.set( x , y , z );

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
		return minus( vector.x , vector.y , vector.z );
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
	public Bounds3D minus( final double x , final double y , final double z )
	{
		return ( ( x == 0.0 ) && ( y == 0.0 ) && ( z == 0.0 ) ) ? this
		     : set( v1.minus( x , y , z ) , v2.minus( x , y , z ) );
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
		return set( v1.multiply( factor ) , v2.multiply( factor ) );
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
		return plus( vector.x , vector.y , vector.z );
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
	public Bounds3D plus( final double x , final double y , final double z )
	{
		return ( ( x == 0.0 ) && ( y == 0.0 ) && ( z == 0.0 ) ) ? this
		     : set( v1.plus( x , y , z ) , v2.plus( x , y , z ) );
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
		final Bounds3D box1 , final Bounds3D box2 ,
		final double x1 , final double y1 , final double z1 ,
		final double x2 , final double y2 , final double z2 )
	{
		final Bounds3D result;

		/*
		 * Try to reuse the existing vectors. If not possible, create
		 * new ones.
		 */
		final Vector3D v1;

			 if ( box1.v1.equals( x1 , y1 , z1 ) ) v1 = box1.v1;
		else if ( box1.v2.equals( x1 , y1 , z1 ) ) v1 = box1.v2;
		else if ( box2.v1.equals( x1 , y1 , z1 ) ) v1 = box2.v1;
		else if ( box2.v2.equals( x1 , y1 , z1 ) ) v1 = box2.v2;
		else v1 = Vector3D.INIT.set( x1 , y1 , z1 );

		final Vector3D v2;

			 if ( box1.v1.equals( x2 , y2 , z2 ) ) v2 = box1.v1;
		else if ( box1.v2.equals( x2 , y2 , z2 ) ) v2 = box1.v2;
		else if ( box2.v1.equals( x2 , y2 , z2 ) ) v2 = box2.v1;
		else if ( box2.v2.equals( x2 , y2 , z2 ) ) v2 = box2.v2;
		else if (      v1.equals( x2 , y2 , z2 ) ) v2 =      v1;
		else v2 = Vector3D.INIT.set( x2 , y2 , z2 );

		/*
		 * Try to reuse the existing boxes. If not possible, create
		 * a new one.
		 */
			 if ( ( box1.v1 == v1 ) && ( box1.v2 == v2 ) ) result = box1;
		else if ( ( box2.v1 == v1 ) && ( box2.v2 == v2 ) ) result = box2;
		else result = Bounds3D.INIT.set( v1 , v2 );

		return result;
	}

	/**
	 * Set bounds to the specified vectors.
	 *
	 * @param   v1      First vector of bounds.
	 * @param   v2      Second vector of bounds.
	 *
	 * @return  Resulting bounds.
	 */
	public Bounds3D set( final Vector3D v1 , final Vector3D v2 )
	{
		return ( ( ( v1 == null ) || v1.equals( this.v1 ) )
		      && ( ( v2 == null ) || v2.equals( this.v2 ) ) ) ? this
		     : new Bounds3D( ( v1 == null ) ? this.v1 : v1 , ( v2 == null ) ? this.v2 : v2 );
	}

	/**
	 * Get size of these bounds.
	 *
	 * @return  Vector describing bound size (v2-v1).
	 */
	public Vector3D size()
	{
		return v2.set( Math.abs( v2.x - v1.x ) , Math.abs( v2.y - v1.y ) , Math.abs( v2.z - v1.z ) );
	}

	/**
	 * Determine sorted bounds. If bounds are sorted, than the x/y/z
	 * components of <code>v1</code> are always less or equal to the
	 * matching components of <code>v2</code>.
	 *
	 * @return  Resulting bounds.
	 */
	public Bounds3D sort()
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
		return bounds.set( min( bounds ) , max( bounds ) );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return  String representation of object.
	 */
	public String toString()
	{
		return v1 + ";" + v2;
	}

	/**
	 * Produce user-friendly string representation of the specified value.
	 *
	 * @param   bounds      Bounds3D value.
	 *
	 * @return  User-friendly string representation of the specified value.
	 */
	public static String toFriendlyString( final Bounds3D bounds )
	{
		return "( " + bounds.v1.x + " , " + bounds.v1.y + " , " + bounds.v1.z + " ) - ( " +
		              bounds.v2.x + " , " + bounds.v2.y + " , " + bounds.v2.z + " )";
	}
}
