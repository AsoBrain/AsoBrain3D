package common.model;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2001 - All Rights Reserved
 *
 * This software may not be used, copyied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
import java.util.StringTokenizer;

/**
 * This class represents rectangular 3D bounds (specified by two vectors).
 * It is used by ROM, but can also be used for other purposes (it does not
 * depend on ROM classes).
 *
 * @author	Peter S. Heijnen
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class Bounds3D
{
	public final Vector3D v1;
	public final Vector3D v2;
	
	/**
	 * Initial value of a box (0-box).
	 */
	public final static Bounds3D INIT = new Bounds3D( Vector3D.INIT , Vector3D.INIT );
	
	/**
	 * Create a new box.
	 *
	 * @param	v1	First vector of box.
	 * @param	v2	Second vector of box.
	 */
	private Bounds3D( final Vector3D v1 , final Vector3D v2 )
	{
		this.v1 = ( v1 == null ) ? Vector3D.INIT : v1;
		this.v2 = ( v2 == null ) ? Vector3D.INIT : v2;
	}

	/**
	 * Compare these bounds to the specified bounds.
	 *
	 * @param	v1		First vector of bounds to compare with.
	 * @param	v2		Second vector of bounds to compare with.
	 *
	 * @return	<code>true</code> if the bounds are equal,
	 *			<code>false</code> if not.
	 */
	public boolean equals( Vector3D v1 , Vector3D v2 )
	{
		return( ( ( v1 == null ) || this.v1.equals( v1 ) ) &&
				( ( v2 == null ) || this.v2.equals( v2 ) ) );
	}

	/**
	 * Compare this object to another object.
	 *
	 * @param	other	Object to compare with.
	 *
	 * @return	<code>true</code> if the objects are equal;
	 *			<code>false</code> if not.
	 */
	public boolean equals( Object other )
	{
		if ( other == this ) return true;
		if ( other == null ) return false;
		if ( !( other instanceof Bounds3D ) ) return false;
		Bounds3D b = (Bounds3D)other;

		return( v1.equals( b.v1 ) && v2.equals( b.v2 ) );
	}

	/**
	 * Convert string representation of object (see toString()) back to
	 * object instance.
	 *
	 * @param	value	String representation of object.
	 *
	 * @return	Object instance.
	 */
	public static Bounds3D fromString( final String value )
	{
		StringTokenizer st = new StringTokenizer( value , ";" );
		
		return Bounds3D.INIT.set(
			Vector3D.fromString( st.nextToken() ) ,
			Vector3D.fromString( st.nextToken() ) );
	}

	/**
	 * Calculate intersection between two bounding boxes. Note that the result
	 * will have one or more negative factors for v2 - v1 when the bounding
	 * boxes are disjunct.
	 *
	 * @param	bounds1		First object for intersection.
	 * @param	bounds2		Seconds object for intersection.
	 *
	 * @return	Bounds of intersection.
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
	 * @param	bounds1		First object for intersection test.
	 * @param	bounds2		Seconds object for intersection test.
	 *
	 * @return	<code>true</code> if the bounds intersect;
	 *			<code>false</code> if the bounds are disjunct.
	 */
	public static boolean intersects( final Bounds3D bounds1 , final Bounds3D bounds2 )
	{
		if ( bounds1 == null || bounds2 == null )
			return false;
		return
		( Math.min( bounds1.v1.x , bounds1.v2.x ) < Math.max( bounds2.v1.x , bounds2.v2.x ) ) &&
		( Math.min( bounds2.v1.x , bounds2.v2.x ) < Math.max( bounds1.v1.x , bounds1.v2.x ) ) &&
		( Math.min( bounds1.v1.y , bounds1.v2.y ) < Math.max( bounds2.v1.y , bounds2.v2.y ) ) &&
		( Math.min( bounds2.v1.y , bounds2.v2.y ) < Math.max( bounds1.v1.y , bounds1.v2.y ) ) &&
		( Math.min( bounds1.v1.z , bounds1.v2.z ) < Math.max( bounds2.v1.z , bounds2.v2.z ) ) &&
		( Math.min( bounds2.v1.z , bounds2.v2.z ) < Math.max( bounds1.v1.z , bounds1.v2.z ) );
	}

	/**
	 * Calculate joined bounds of the two specified bounding objects.
	 *
	 * @param	bounds1		First object for join.
	 * @param	bounds2		Seconds object for join.
	 *
	 * @return	Joined bounds.
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
	 * Determine maximum vector of bounds.
	 *
	 * @param	bounds	Bounds to get the vector for.
	 *
	 * @return	Resulting vector.
	 */
	public static Vector3D max( final Bounds3D box )
	{
		float x = Math.max( box.v1.x , box.v2.x );
		float y = Math.max( box.v1.y , box.v2.y );
		float z = Math.max( box.v1.z , box.v2.z );
		
			 if ( box.v1.equals( x , y , z ) ) return( box.v1 );
		else if ( box.v2.equals( x , y , z ) ) return( box.v2 );
		else return Vector3D.INIT.set( x , y , z );
	}

	/**
	 * Determine minimum vector of bounds.
	 *
	 * @param	bounds	Bounds to get the vector for.
	 *
	 * @return	Resulting vector.
	 */
	public static Vector3D min( final Bounds3D bounds )
	{
		float x = Math.min( bounds.v1.x , bounds.v2.x );
		float y = Math.min( bounds.v1.y , bounds.v2.y );
		float z = Math.min( bounds.v1.z , bounds.v2.z );
		
			 if ( bounds.v1.equals( x , y , z ) ) return bounds.v1;
		else if ( bounds.v2.equals( x , y , z ) ) return bounds.v2;
		else return Vector3D.INIT.set( x , y , z );
	}

	/**
	 * Subtract vector from bounds.
	 *
	 * @param	vector	Vector to subtract from bounds.
	 *
	 * @return	Resulting bounds.
	 */
	public Bounds3D minus( final Vector3D vector )
	{
		return minus( vector.x , vector.y , vector.z );
	}

	/**
	 * Subtract vector from bounds.
	 *
	 * @param	x	X-coordinate of vector to subtract.
	 * @param	y	Y-coordinate of vector to subtract.
	 * @param	z	Z-coordinate of vector to subtract.
	 *
	 * @return	Resulting bounds.
	 */
	public Bounds3D minus( final float x , final float y , final float z )
	{
		if ( x == 0f && y == 0f && z == 0f )
			return this;
		else
			return set( v1.minus( x , y , z ) , v2.minus( x , y , z ) );
	}

	/**
	 * Determine box after scalar multiplication.
	 *
	 * @param	factor	Scale multiplication factor.
	 *
	 * @return	Resulting box.
	 */
	public Bounds3D multiply( final float factor )
	{
		return set( v1.multiply( factor ) , v2.multiply( factor ) );
	}

	/**
	 * Add a vector to bounds.
	 *
	 * @param	vector	Vector to add to bounds.
	 *
	 * @return	Resulting bounds.
	 */
	public Bounds3D plus( final Vector3D vector )
	{
		return plus( vector.x , vector.y , vector.z );
	}

	/**
	 * Add a vector to bounds.
	 *
	 * @param	x	X-coordinate of vector to add.
	 * @param	y	Y-coordinate of vector to add.
	 * @param	z	Z-coordinate of vector to add.
	 *
	 * @return	Resulting bounds.
	 */
	public Bounds3D plus( final float x , final float y , final float z )
	{
		if ( x == 0f && y == 0f && z == 0f )
			return this;
		else
			return set( v1.plus( x , y , z ) , v2.plus( x , y , z ) );
	}

	/**
	 * Construct new box from the specified coordinates, and try to reuse
	 * existing boxes.
	 */
	private static Bounds3D rebuild( Bounds3D box1 , Bounds3D box2 ,
		float x1 , float y1 , float z1 ,
		float x2 , float y2 , float z2 )
	{
		/*
		 * Try to reuse the existing vectors. If not possible, create
		 * new ones.
		 */
		Vector3D v1;
		
			 if ( box1.v1.equals( x1 , y1 , z1 ) ) v1 = box1.v1;
		else if ( box1.v2.equals( x1 , y1 , z1 ) ) v1 = box1.v2;
		else if ( box2.v1.equals( x1 , y1 , z1 ) ) v1 = box2.v1;
		else if ( box2.v2.equals( x1 , y1 , z1 ) ) v1 = box2.v2;
		else v1 = Vector3D.INIT.set( x1 , y1 , z1 );
		
		Vector3D v2;
		
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
			 if ( box1.v1 == v1 && box1.v2 == v2 ) return( box1 );
		else if ( box2.v1 == v1 && box2.v2 == v2 ) return( box2 );
		else return Bounds3D.INIT.set( v1 , v2 );
	}

	/**
	 * Set bounds to the specified vectors.
	 *
	 * @param	v1	First vector of bounds.
	 * @param	v2	Second vector of bounds.
	 *
	 * @return	Resulting bounds.
	 */
	public Bounds3D set( final Vector3D v1 , final Vector3D v2 )
	{
		if ( ( v1 == null || v1.equals( this.v1 ) )
		  && ( v2 == null || v2.equals( this.v2 ) ) )
		{
			return this;
		}
		else
		{
			return new Bounds3D( v1 == null ? this.v1 : v1 , v2 == null ? this.v2 : v2 );
		}
	}

	/**
	 * Get size of these bounds.
	 *
	 * @return	Vector describing bound size (v2-v1).
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
	 * @return	Resulting bounds.
	 */
	public Bounds3D sort()
	{
		return sort( this );
	}

	/**
	 * Get sorted bounds.
	 *
	 * @param	bounds	Bounds to sort.
	 *
	 * @return	Sorted bounds.
	 */
	public static Bounds3D sort( final Bounds3D bounds )
	{
		return( bounds.set( min( bounds ) , max( bounds ) ) );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return	String representation of object.
	 */
	public String toString()
	{
		return( v1 + ";" + v2 );
	}

}
