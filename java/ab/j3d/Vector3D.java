/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2004 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
package ab.light3d;

/**
 * This class represents a 3D vector. It is used by ROM, but can also
 * be used for other purposes (it does not depend on ROM classes).
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class Vector3D
{
	/** X component of 3D vector. */ public final float x;
	/** Y component of 3D vector. */ public final float y;
	/** Z component of 3D vector. */ public final float z;

	/**
	 * Initial value of a vector (0-vector).
	 */
	public static final Vector3D INIT = new Vector3D( 0f , 0f , 0f );
	/**
	 * Construct new vector.
	 *
	 * @param	nx	X-coordinate of vector.
	 * @param	ny	Y-coordinate of vector.
	 * @param	nz	Z-coordinate of vector.
	 */
	private Vector3D( final float nx , final float ny , final float nz )
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
	public static float angle( final Vector3D v1 , final Vector3D v2 )
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
	public static float cosAngle( final Vector3D v1 , final Vector3D v2 )
	{
		final float l = v1.length() * v2.length();
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
	public static Vector3D cross( final Vector3D v1 , final Vector3D v2 )
	{
		return v1.set( v1.y * v2.z - v1.z * v2.y ,
					   v1.z * v2.x - v1.x * v2.z ,
					   v1.x * v2.y - v1.y * v2.x );
	}

	/**
	 * Calculate distance between this and another vector (both should be absolute).
	 *
	 * @param   other   Vector to calculate the distance to.
	 *
	 * @return	Distance between this and the specified other vector.
	 */
	public float distanceTo( final Vector3D other )
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
	public static float dot( final Vector3D v1 , final Vector3D v2 )
	{
		return( v1.x * v2.x + v1.y * v2.y + v1.z * v2.z );
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param	x	X-coordinate of vector.
	 * @param	y	Y-coordinate of vector.
	 * @param	z	Z-coordinate of vector.
	 *
	 * @return	<code>true</code> if vectors are equal,
	 *	    	<code>false</code> if not.
	 */
	public boolean equals( final float x , final float y , final float z )
	{
		return( ( x != x /* => NaN*/ || x == this.x ) &&
				( y != y /* => NaN*/ || y == this.y ) &&
				( z != z /* => NaN*/ || z == this.z ) );
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param	other	Vector to compare with.
	 *
	 * @return	<code>true</code> if vectors are equal,
	 *  		<code>false</code> if not.
	 */
	public boolean equals( final Object other )
	{
		if ( other == this ) return true;
		if ( other == null ) return false;
		if ( !( other instanceof Vector3D ) ) return false;

		final Vector3D v = (Vector3D)other;
		return( x == v.x && y == v.y && z == v.z );
	}

	/**
	 * Convert string representation of object (see toString()) back to
	 * object instance.
	 *
	 * @param	value	String representation of object.
	 *
	 * @return	Object instance.
	 */
	public static Vector3D fromString( final String value )
	{
		final int comma1 = value.indexOf( ',' );
		final int comma2 = value.indexOf( ',' , comma1 + 1 );
		
		return Vector3D.INIT.set( new Float( value.substring( 0 , comma1 ) ).floatValue() ,
		                          new Float( value.substring( comma1 + 1 , comma2 ) ).floatValue() ,
		                          new Float( value.substring( comma2 + 1 ) ).floatValue() );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return	Length of vector.
	 */
	public float length()
	{
		return (float)Math.sqrt( x * x + y * y + z * z );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param	other	Vector to subtract from this vector.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D minus( final Vector3D other )
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
	public Vector3D minus( final float x , final float y , final float z )
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
	public Vector3D multiply( final float factor )
	{
		return set( x * factor , y * factor , z * factor );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1,
	 * it will be returned as-is.
	 *
	 * @return  Normalized vector.
	 */
	public Vector3D normalize()
	{
		float l = length();
		return ( l == 0 || l == 1 ) ? this : set( x / l , y / l , z / l );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param	other	Vector to add to this vector.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D plus( final Vector3D other )
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
	public Vector3D plus( final float x , final float y , final float z )
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
