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
	private Bounds3D( Vector3D nv1 , Vector3D nv2 )
	{
		if ( nv1 == null ) nv1 = Vector3D.INIT;
		if ( nv2 == null ) nv2 = Vector3D.INIT;
		v1 = nv1;
		v2 = nv2;
	}

	/**
	 * Compare this box to another box.
	 *
	 * @param	box	Box to compare with.
	 *
	 * @return	<code>true</code> if boxs are equal,
	 *		<code>false</code> if not.
	 */
	public final boolean equals( Bounds3D other )
	{
		if ( other == this ) return( true );
		if ( other == null ) return( false );
		return( v1.equals( other.v1 ) && v2.equals( other.v2 ) );
	}

	/**
	 * Compare this box to another box.
	 *
	 * @return	<code>true</code> if boxes are equal,
	 *			<code>false</code> if not.
	 */
	public final boolean equals( Vector3D v1 , Vector3D v2 )
	{
		return( ( ( v1 == null ) || this.v1.equals( v1 ) ) &&
				( ( v2 == null ) || this.v2.equals( v2 ) ) );
	}

	/**
	 * Convert string representation of object (see toString()) back to
	 * object instance.
	 *
	 * @param	value	String representation of object.
	 *
	 * @return	Object instance.
	 */
	public final static Bounds3D fromString( String value )
	{
		StringTokenizer st = new StringTokenizer( value , ";" );
		
		return Bounds3D.INIT.set(
			Vector3D.fromString( st.nextToken() ) ,
			Vector3D.fromString( st.nextToken() ) );
	}

	/**
	 * Calculate intersection between this box and another box. Note that
	 * if the boxes are disjunct, the result will have one or more negative
	 * factors for v2 - v1.
	 */
	public final static Bounds3D intersect( Bounds3D box1 , Bounds3D box2 )
	{
		return rebuild( box1 , box2 ,
			Math.max( Math.min( box1.v1.x , box1.v2.x ) , Math.min( box2.v1.x , box2.v2.x ) ) ,
			Math.max( Math.min( box1.v1.y , box1.v2.y ) , Math.min( box2.v1.y , box2.v2.y ) ) ,
			Math.max( Math.min( box1.v1.z , box1.v2.z ) , Math.min( box2.v1.z , box2.v2.z ) ) ,
			Math.min( Math.max( box1.v1.x , box1.v2.x ) , Math.max( box2.v1.x , box2.v2.x ) ) ,
			Math.min( Math.max( box1.v1.y , box1.v2.y ) , Math.max( box2.v1.y , box2.v2.y ) ) ,
			Math.min( Math.max( box1.v1.z , box1.v2.z ) , Math.max( box2.v1.z , box2.v2.z ) ) );
	}

	/**
	 * Determine whether the two specified boxes intersect.
	 */
	public final static boolean intersects( Bounds3D box1 , Bounds3D box2 )
	{
		if ( box1 == null || box2 == null )
			return false;
		return
		( Math.min( box1.v1.x , box1.v2.x ) < Math.max( box2.v1.x , box2.v2.x ) ) &&
		( Math.min( box2.v1.x , box2.v2.x ) < Math.max( box1.v1.x , box1.v2.x ) ) &&
		( Math.min( box1.v1.y , box1.v2.y ) < Math.max( box2.v1.y , box2.v2.y ) ) &&
		( Math.min( box2.v1.y , box2.v2.y ) < Math.max( box1.v1.y , box1.v2.y ) ) &&
		( Math.min( box1.v1.z , box1.v2.z ) < Math.max( box2.v1.z , box2.v2.z ) ) &&
		( Math.min( box2.v1.z , box2.v2.z ) < Math.max( box1.v1.z , box1.v2.z ) );
	}

	/**
	 * Calculate join between this box and another box.
	 */
	public final static Bounds3D join( Bounds3D box1 , Bounds3D box2 )
	{
		return rebuild( box1 , box2 ,
			Math.min( Math.min( box1.v1.x , box1.v2.x ) , Math.min( box2.v1.x , box2.v2.x ) ) ,
			Math.min( Math.min( box1.v1.y , box1.v2.y ) , Math.min( box2.v1.y , box2.v2.y ) ) ,
			Math.min( Math.min( box1.v1.z , box1.v2.z ) , Math.min( box2.v1.z , box2.v2.z ) ) ,
			Math.max( Math.max( box1.v1.x , box1.v2.x ) , Math.max( box2.v1.x , box2.v2.x ) ) ,
			Math.max( Math.max( box1.v1.y , box1.v2.y ) , Math.max( box2.v1.y , box2.v2.y ) ) ,
			Math.max( Math.max( box1.v1.z , box1.v2.z ) , Math.max( box2.v1.z , box2.v2.z ) ) );
	}

	/**
	 * Determine maximum vector of box.
	 *
	 * @return	Resulting vector.
	 */
	public final static Vector3D max( Bounds3D box )
	{
		float x = Math.max( box.v1.x , box.v2.x );
		float y = Math.max( box.v1.y , box.v2.y );
		float z = Math.max( box.v1.z , box.v2.z );
		
			 if ( box.v1.equals( x , y , z ) ) return( box.v1 );
		else if ( box.v2.equals( x , y , z ) ) return( box.v2 );
		else return Vector3D.INIT.set( x , y , z );
	}

	/**
	 * Determine minimum vector of box.
	 *
	 * @return	Resulting vector.
	 */
	public final static Vector3D min( Bounds3D box )
	{
		float x = Math.min( box.v1.x , box.v2.x );
		float y = Math.min( box.v1.y , box.v2.y );
		float z = Math.min( box.v1.z , box.v2.z );
		
			 if ( box.v1.equals( x , y , z ) ) return box.v1;
		else if ( box.v2.equals( x , y , z ) ) return box.v2;
		else return Vector3D.INIT.set( x , y , z );
	}

	/**
	 * Subtract a vector from this box.
	 *
	 * @param	vector	Vector to subtract from this box.
	 *
	 * @return	Resulting box.
	 */
	public final Bounds3D minus( Vector3D vector )
	{
		return minus( vector.x , vector.y , vector.z );
	}

	/**
	 * Subtract a vector from this box.
	 *
	 * @param	x	X-coordinate of box.
	 * @param	y	Y-coordinate of box.
	 * @param	z	Z-coordinate of box.
	 *
	 * @return	Resulting box.
	 */
	public final Bounds3D minus( float x , float y , float z )
	{
		if ( x == 0d && y == 0d && z == 0d ) return( this );
		return set( v1.minus( x , y , z ) , v2.minus( x , y , z ) );
	}

	/**
	 * Determine box after scalar multiplication.
	 *
	 * @param	factor	Scale multiplication factor.
	 *
	 * @return	Resulting box.
	 */
	public final Bounds3D multiply( float factor )
	{
		return set( v1.multiply( factor ) , v2.multiply( factor ) );
	}

	/**
	 * Add a vector to this box.
	 *
	 * @param	vector	Vector to add to this box.
	 *
	 * @return	Resulting box.
	 */
	public final Bounds3D plus( Vector3D vector )
	{
		return plus( vector.x , vector.y , vector.z );
	}

	/**
	 * Add a vector to this box.
	 *
	 * @param	x	X-coordinate of vector.
	 * @param	y	Y-coordinate of vector.
	 * @param	z	Z-coordinate of vector.
	 *
	 * @return	Resulting box.
	 */
	public final Bounds3D plus( float x , float y , float z )
	{
		if ( x == 0d && y == 0d && z == 0d ) return( this );
		return set( v1.plus( x , y , z ) , v2.plus( x , y , z ) );
	}

	/**
	 * Construct new box from the specified coordinates, and try to reuse
	 * existing boxes.
	 */
	private final static Bounds3D rebuild( Bounds3D box1 , Bounds3D box2 ,
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
	 * Stel box in op opgegeven coordinaten.
	 *
	 * @param	x	X-coordinaat of box.
	 * @param	y	Y-coordinaat of box.
	 * @param	z	Z-coordinaat of box.
	 *
	 * @return	Resulterende box.
	 */
	public final Bounds3D set( Vector3D v1 , Vector3D v2 )
	{
		if ( v1 == null ) v1 = this.v1;
		if ( v2 == null ) v2 = this.v2;
		
		if ( v1.equals( this.v1 ) &&  v2.equals( this.v2 ) )
		return( this );
		else
		return( new Bounds3D( v1 , v2 ) );
	}

	/**
	 * Get size of this box.
	 *
	 * @return	Vector describing box size (v2-v1).
	 */
	public final Vector3D size()
	{
		Bounds3D b = sort( this );
		return b.v2.minus( b.v1 );
	}

	/**
	 * Determine sorted box.
	 */
	public Bounds3D sort()
	{
		return sort( this );
	}

	/**
	 * Determine sorted box.
	 */
	public final static Bounds3D sort( Bounds3D box )
	{
		return( box.set( min( box ) , max( box ) ) );
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
