/*   ColDet - C++ 3D Collision Detection Library
 *   Copyright (C) 2000 Amir Geva
 *
 *   ColDet - 3D Collision Detection Library for Java
 *   Copyright (C) 2008 Numdata BV
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */
package ab.j3d.coldet;

/**
 * AABB class, with support for testing against OBBs.
 *
 * @author Amir Geva (original C++ version)
 * @author Peter S. Heijnen (Java Port)
 * @version 1.1
 */
public class Box
{
	Box()
	{
	}

	/**
	 * Construct from scalar corner position and size
	 */
	Box( final float x, final float y, final float z, final float sx, final float sy, final float sz )
	{
		_pos = new Vector3D( x, y, z );
		_size = new Vector3D( sx, sy, sz );
		_center = new Vector3D( x + 0.5f * sx, y + 0.5f * sy, z + 0.5f * sz );
	}

	/**
	 * Construct from corner position and size
	 */
	Box( final Vector3D pos, final Vector3D size )
	{
		_pos = new Vector3D( pos );
		_size = new Vector3D( size );
		_center = new Vector3D( pos.plus( size.multiply( 0.5f ) ) );
	}

	/**
	 * Copy constructor
	 */
	Box( final Box b )
	{
		_pos = new Vector3D( b._pos );
		_size = new Vector3D( b._size );
		_center = new Vector3D( b._center );
	}

	/**
	 * Returns the box's position
	 */
	Vector3D getPosition()
	{
		return _pos;
	}

	/**
	 * Returns the sizes of the box's edges
	 */
	Vector3D getSize()
	{
		return _size;
	}

	/**
	 * Returns the center position of the box
	 */
	Vector3D getCenter()
	{
		return _center;
	}

	/**
	 * Returns the volume of the box
	 */
	float getVolume()
	{
		final Vector3D size = _size;
		return size.x * size.y * size.z;
	}


	/**
	 * Oriented box intersection.
	 */
	boolean intersect( final Box other, final Vector3D[] rsN , final Matrix3D rsT )
	{
		final Vector3D thisCenter = getCenter();
		final Vector3D thisSize = getSize();
		final Vector3D otherCenter = other.getCenter();
		final Vector3D otherSize = other.getSize();

		final Vector3D bCenter = otherCenter.transform( rsT );
		final Vector3D ea = thisSize.multiply( 0.5f );
		final Vector3D eb = otherSize.multiply( 0.5f );

		final Vector3D distance = bCenter.minus( thisCenter );
		final Matrix3D c = new Matrix3D();
		final Matrix3D abs_C = new Matrix3D();
		float r0;
		float r1;
		float r;
		float r01;
		int i;

		for ( i = 0; i < 3; i++ )
		{
			c.set( i, 0, rsN[ 0 ].get( i ) );
			c.set( i, 1, rsN[ 1 ].get( i ) );
			c.set( i, 2, rsN[ 2 ].get( i ) );

			abs_C.set( i, 0, Math.abs( c.get( i, 0 ) ) );
			abs_C.set( i, 1, Math.abs( c.get( i, 1 ) ) );
			abs_C.set( i, 2, Math.abs( c.get( i, 2 ) ) );

			r = Math.abs( distance.get( i ) );
			r1 = eb.dotProduct( new Vector3D( abs_C.get( i , 0 ) , abs_C.get( i , 1 ) , abs_C.get( i , 2 ) ) );
			r01 = ea.get( i ) + r1;
			if ( r > r01 )
				return false;
		}

		for ( i = 0; i < 3; i++ )
		{
			r = Math.abs( rsN[ i ].dotProduct( distance ) );
			r0 = ea.x * abs_C.get( 0, i ) + ea.y * abs_C.get( 1, i ) + ea.z * abs_C
			.get( 2, i );
			r01 = r0 + eb.get( i );
			if ( r > r01 ) return false;
		}

		r = Math.abs( distance.z * c.get( 1, 0 ) - distance.y * c.get( 2, 0 ) );
		r0 = ea.y * abs_C.get( 2, 0 ) + ea.z * abs_C.get( 1, 0 );
		r1 = eb.y * abs_C.get( 0, 2 ) + eb.z * abs_C.get( 0, 1 );
		r01 = r0 + r1;
		if ( r > r01 ) return false;

		r = Math.abs( distance.z * c.get( 1, 1 ) - distance.y * c.get( 2, 1 ) );
		r0 = ea.y * abs_C.get( 2, 1 ) + ea.z * abs_C.get( 1, 1 );
		r1 = eb.x * abs_C.get( 0, 2 ) + eb.z * abs_C.get( 0, 0 );
		r01 = r0 + r1;
		if ( r > r01 ) return false;

		r = Math.abs( distance.z * c.get( 1, 2 ) - distance.y * c.get( 2, 2 ) );
		r0 = ea.y * abs_C.get( 2, 2 ) + ea.z * abs_C.get( 1, 2 );
		r1 = eb.x * abs_C.get( 0, 1 ) + eb.y * abs_C.get( 0, 0 );
		r01 = r0 + r1;
		if ( r > r01 ) return false;

		r = Math.abs( distance.x * c.get( 2, 0 ) - distance.z * c.get( 0, 0 ) );
		r0 = ea.x * abs_C.get( 2, 0 ) + ea.z * abs_C.get( 0, 0 );
		r1 = eb.y * abs_C.get( 1, 2 ) + eb.z * abs_C.get( 1, 1 );
		r01 = r0 + r1;
		if ( r > r01 ) return false;

		r = Math.abs( distance.x * c.get( 2, 1 ) - distance.z * c.get( 0, 1 ) );
		r0 = ea.x * abs_C.get( 2, 1 ) + ea.z * abs_C.get( 0, 1 );
		r1 = eb.x * abs_C.get( 1, 2 ) + eb.z * abs_C.get( 1, 0 );
		r01 = r0 + r1;
		if ( r > r01 ) return false;

		r = Math.abs( distance.x * c.get( 2, 2 ) - distance.z * c.get( 0, 2 ) );
		r0 = ea.x * abs_C.get( 2, 2 ) + ea.z * abs_C.get( 0, 2 );
		r1 = eb.x * abs_C.get( 1, 1 ) + eb.y * abs_C.get( 1, 0 );
		r01 = r0 + r1;
		if ( r > r01 ) return false;

		r = Math.abs( distance.y * c.get( 0, 0 ) - distance.x * c.get( 1, 0 ) );
		r0 = ea.x * abs_C.get( 1, 0 ) + ea.y * abs_C.get( 0, 0 );
		r1 = eb.y * abs_C.get( 2, 2 ) + eb.z * abs_C.get( 2, 1 );
		r01 = r0 + r1;
		if ( r > r01 ) return false;

		r = Math.abs( distance.y * c.get( 0, 1 ) - distance.x * c.get( 1, 1 ) );
		r0 = ea.x * abs_C.get( 1, 1 ) + ea.y * abs_C.get( 0, 1 );
		r1 = eb.x * abs_C.get( 2, 2 ) + eb.z * abs_C.get( 2, 0 );
		r01 = r0 + r1;
		if ( r > r01 ) return false;

		r = Math.abs( distance.y * c.get( 0, 2 ) - distance.x * c.get( 1, 2 ) );
		r0 = ea.x * abs_C.get( 1, 2 ) + ea.y * abs_C.get( 0, 2 );
		r1 = eb.x * abs_C.get( 2, 1 ) + eb.y * abs_C.get( 2, 0 );
		r01 = r0 + r1;

		return r <= r01;

	}

	/**
	 * Position of box corner.
	 */
	Vector3D _pos;

	/**
	 * Size of box box edges.
	 */
	Vector3D _size;

	/**
	 * Position of box center.  m_Pos+0.5f*m_Size;
	 */
	Vector3D _center;

}