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
 * This class represents a 3D vector. It is used by ROM, but can also
 * be used for other purposes (it does not depend on ROM classes).
 *
 * @author	Peter S. Heijnen
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class Vector3D
{
	public final float x;
	public final float y;
	public final float z;
	
	/**
	 * Initial value of a vector (0-vector).
	 */
	public final static Vector3D INIT = new Vector3D( 0f , 0f , 0f );
	/**
	 * Construct new vector.
	 *
	 * @param	x	X-coordinate of vector.
	 * @param	y	Y-coordinate of vector.
	 * @param	z	Z-coordinate of vector.
	 */
	private Vector3D( float nx , float ny , float nz )
	{
		x = nx;
		y = ny;
		z = nz;
	}

	/**
	 * Get angle between this vector and another one specified as argument.
	 *
	 * @param	v1		First vector.
	 * @param	v2		Second vector.
	 *
	 * @return	angle between vectors in radians.
	 */
	public static float angle( Vector3D v1 , Vector3D v2 )
	{
		return (float)Math.acos( cosAngle( v1 , v2 ) );
	}

	/**
	 * Get cos(angle) between this vector and another one specified as argument.
	 *
	 * @param	v1		First vector.
	 * @param	v2		Second vector.
	 *
	 * @return	cos(angle) between vectors.
	 */
	public static float cosAngle( Vector3D v1 , Vector3D v2 )
	{
		float l = length( v1 ) * length( v2 );
		if ( l == 0f ) return( 0f );
		return( dot( v1 , v2 ) / l );
	}

	/**
	 * Determine cross product of this vector with another vector.
	 *
	 * @param	v1		First vector.
	 * @param	v2		Second vector.
	 *
	 * @return	Resulting vector.
	 */
	public static Vector3D cross( Vector3D v1 , Vector3D v2 )
	{
		return v1.set( v1.y * v2.z - v1.z * v2.y ,
					   v1.z * v2.x - v1.x * v2.z ,
					   v1.x * v2.y - v1.y * v2.x );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return	Length of vector.
	 */
	public float distanceTo( Vector3D other )
	{
		final float dx = x - other.x;
		final float dy = y - other.y;
		final float dz = z - other.z;
		
		return (float)Math.sqrt( dx * dx + dy * dy + dz * dz );
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector
	 * and another one specified as argument.
	 *
	 * @param	v1		First vector.
	 * @param	v2		Second vector.
	 *
	 * @return	Dot product.
	 */
	public static float dot( Vector3D v1 , Vector3D v2 )
	{
		return( v1.x * v2.x + v1.y * v2.y + v1.z * v2.z );
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param	vector	Vector to compare with.
	 *
	 * @return	<code>true</code> if vectors are equal,
	 *		<code>false</code> if not.
	 */
	public boolean equals( Vector3D other )
	{
		if ( other == this ) return( true );
		if ( other == null ) return( false );
		return( x == other.x && y == other.y && z == other.z );
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param	x	X-coordinate of vector.
	 * @param	y	Y-coordinate of vector.
	 * @param	z	Z-coordinate of vector.
	 *
	 * @return	<code>true</code> if vectors are equal,
	 *		<code>false</code> if not.
	 */
	public boolean equals( float x , float y , float z )
	{
		return( ( x != x /* => NaN*/ || x == this.x ) &&
				( y != y /* => NaN*/ || y == this.y ) &&
				( z != z /* => NaN*/ || z == this.z ) );
	}

	/**
	 * Convert string representation of object (see toString()) back to
	 * object instance.
	 *
	 * @param	value	String representation of object.
	 *
	 * @return	Object instance.
	 */
	public static Vector3D fromString( String value )
	{
		StringTokenizer st = new StringTokenizer( value , "," );
		
		return Vector3D.INIT.set(
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return	Length of vector.
	 */
	public static float length( Vector3D vector )
	{
		return (float)Math.sqrt( vector.x * vector.x + vector.y * vector.y + vector.z * vector.z );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param	vector	Vector to subtract from this vector.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D minus( Vector3D other )
	{
		return minus( other.x , other.y , other.z );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param	x	X-coordinate of vector.
	 * @param	y	Y-coordinate of vector.
	 * @param	z	Z-coordinate of vector.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D minus( float x , float y , float z )
	{
		return set( this.x - x , this.y - y , this.z - z );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param	factor	Scale multiplication factor.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D multiply( float factor )
	{
		return set( x * factor , y * factor , z * factor );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param	vector	Vector to add to this vector.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D plus( Vector3D other )
	{
		return plus( other.x , other.y , other.z );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param	x	X-coordinate of vector.
	 * @param	y	Y-coordinate of vector.
	 * @param	z	Z-coordinate of vector.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D plus( float x , float y , float z )
	{
		return set( this.x + x , this.y + y , this.z + z );
	}

	/**
	 * Set vector to the specified coordinates.
	 *
	 * @param	x	X-coordinate of vector.
	 * @param	y	Y-coordinate of vector.
	 * @param	z	Z-coordinate of vector.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D set( float x , float y , float z )
	{
		if ( x != x /* => NaN*/ ) x = this.x;
		if ( y != y /* => NaN*/ ) y = this.y;
		if ( z != z /* => NaN*/ ) z = this.z;
		
		if ( x == 0f && y == 0f && z == 0f )
			return( INIT );
		
		if ( x == this.x && y == this.y && z == this.z )
			return( this );
		
		return new Vector3D( x , y , z );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return	String representation of object.
	 */
	public String toString()
	{
		return( x + "," + y + "," + z );
	}

}
