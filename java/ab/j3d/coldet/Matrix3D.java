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
 * 4x4 matrix, used for transformations.
 *
 * @author  Amir Geva (original C++ version)
 * @author  Peter S. Heijnen (Java Port)
 * @version 1.1
 */
public class Matrix3D
{
	final float[][] m;

	public static final Matrix3D Identity = new Matrix3D();

	public Matrix3D()
	{
		this( 1.0f, 0.0f, 0.0f, 0.0f,
		      0.0f, 1.0f, 0.0f, 0.0f,
		      0.0f, 0.0f, 1.0f, 0.0f,
		      0.0f, 0.0f, 0.0f, 1.0f );
	}

	public Matrix3D( final float f11, final float f12, final float f13, final float f14,
	          final float f21, final float f22, final float f23, final float f24,
	          final float f31, final float f32, final float f33, final float f34,
	          final float f41, final float f42, final float f43, final float f44 )
	{
		m = new float[][] {
		{ f11 , f12 , f13 , f14 } , { f21 , f22 , f23 , f24 } ,
		{ f31 , f32 , f33 , f34 } , { f41 , f42 , f43 , f44 }
		};
	}

	Matrix3D multiply( final Matrix3D o )
	{
		return new Matrix3D(
		m[ 0 ][ 0 ] * o.m[ 0 ][ 0 ] + m[ 0 ][ 1 ] * o.m[ 1 ][ 0 ] + m[ 0 ][ 2 ] * o.m[ 2 ][ 0 ] + m[ 0 ][ 3 ] * o.m[ 3 ][ 0 ],
		m[ 0 ][ 0 ] * o.m[ 0 ][ 1 ] + m[ 0 ][ 1 ] * o.m[ 1 ][ 1 ] + m[ 0 ][ 2 ] * o.m[ 2 ][ 1 ] + m[ 0 ][ 3 ] * o.m[ 3 ][ 1 ],
		m[ 0 ][ 0 ] * o.m[ 0 ][ 2 ] + m[ 0 ][ 1 ] * o.m[ 1 ][ 2 ] + m[ 0 ][ 2 ] * o.m[ 2 ][ 2 ] + m[ 0 ][ 3 ] * o.m[ 3 ][ 2 ],
		m[ 0 ][ 0 ] * o.m[ 0 ][ 3 ] + m[ 0 ][ 1 ] * o.m[ 1 ][ 3 ] + m[ 0 ][ 2 ] * o.m[ 2 ][ 3 ] + m[ 0 ][ 3 ] * o.m[ 3 ][ 3 ],
		m[ 1 ][ 0 ] * o.m[ 0 ][ 0 ] + m[ 1 ][ 1 ] * o.m[ 1 ][ 0 ] + m[ 1 ][ 2 ] * o.m[ 2 ][ 0 ] + m[ 1 ][ 3 ] * o.m[ 3 ][ 0 ],
		m[ 1 ][ 0 ] * o.m[ 0 ][ 1 ] + m[ 1 ][ 1 ] * o.m[ 1 ][ 1 ] + m[ 1 ][ 2 ] * o.m[ 2 ][ 1 ] + m[ 1 ][ 3 ] * o.m[ 3 ][ 1 ],
		m[ 1 ][ 0 ] * o.m[ 0 ][ 2 ] + m[ 1 ][ 1 ] * o.m[ 1 ][ 2 ] + m[ 1 ][ 2 ] * o.m[ 2 ][ 2 ] + m[ 1 ][ 3 ] * o.m[ 3 ][ 2 ],
		m[ 1 ][ 0 ] * o.m[ 0 ][ 3 ] + m[ 1 ][ 1 ] * o.m[ 1 ][ 3 ] + m[ 1 ][ 2 ] * o.m[ 2 ][ 3 ] + m[ 1 ][ 3 ] * o.m[ 3 ][ 3 ],
		m[ 2 ][ 0 ] * o.m[ 0 ][ 0 ] + m[ 2 ][ 1 ] * o.m[ 1 ][ 0 ] + m[ 2 ][ 2 ] * o.m[ 2 ][ 0 ] + m[ 2 ][ 3 ] * o.m[ 3 ][ 0 ],
		m[ 2 ][ 0 ] * o.m[ 0 ][ 1 ] + m[ 2 ][ 1 ] * o.m[ 1 ][ 1 ] + m[ 2 ][ 2 ] * o.m[ 2 ][ 1 ] + m[ 2 ][ 3 ] * o.m[ 3 ][ 1 ],
		m[ 2 ][ 0 ] * o.m[ 0 ][ 2 ] + m[ 2 ][ 1 ] * o.m[ 1 ][ 2 ] + m[ 2 ][ 2 ] * o.m[ 2 ][ 2 ] + m[ 2 ][ 3 ] * o.m[ 3 ][ 2 ],
		m[ 2 ][ 0 ] * o.m[ 0 ][ 3 ] + m[ 2 ][ 1 ] * o.m[ 1 ][ 3 ] + m[ 2 ][ 2 ] * o.m[ 2 ][ 3 ] + m[ 2 ][ 3 ] * o.m[ 3 ][ 3 ],
		m[ 3 ][ 0 ] * o.m[ 0 ][ 0 ] + m[ 3 ][ 1 ] * o.m[ 1 ][ 0 ] + m[ 3 ][ 2 ] * o.m[ 2 ][ 0 ] + m[ 3 ][ 3 ] * o.m[ 3 ][ 0 ],
		m[ 3 ][ 0 ] * o.m[ 0 ][ 1 ] + m[ 3 ][ 1 ] * o.m[ 1 ][ 1 ] + m[ 3 ][ 2 ] * o.m[ 2 ][ 1 ] + m[ 3 ][ 3 ] * o.m[ 3 ][ 1 ],
		m[ 3 ][ 0 ] * o.m[ 0 ][ 2 ] + m[ 3 ][ 1 ] * o.m[ 1 ][ 2 ] + m[ 3 ][ 2 ] * o.m[ 2 ][ 2 ] + m[ 3 ][ 3 ] * o.m[ 3 ][ 2 ],
		m[ 3 ][ 0 ] * o.m[ 0 ][ 3 ] + m[ 3 ][ 1 ] * o.m[ 1 ][ 3 ] + m[ 3 ][ 2 ] * o.m[ 2 ][ 3 ] + m[ 3 ][ 3 ] * o.m[ 3 ][ 3 ] );
	}

	Matrix3D multiply( final float scalar )
	{
		return new Matrix3D( scalar * get( 0, 0 ), scalar * get( 0, 1 ), scalar * get( 0, 2 ), scalar * get( 0, 3 ),
		                     scalar * get( 1, 0 ), scalar * get( 1, 1 ), scalar * get( 1, 2 ), scalar * get( 1, 3 ),
		                     scalar * get( 2, 0 ), scalar * get( 2, 1 ), scalar * get( 2, 2 ), scalar * get( 2, 3 ),
		                     scalar * get( 3, 0 ), scalar * get( 3, 1 ), scalar * get( 3, 2 ), scalar * get( 3, 3 ) );
	}


	float get( final int i, final int j )
	{
		return m[ i ][ j ];
	}

	void set( final int i, final int j, final float f )
	{
		m[ i ][ j ] = f;
	}

	Matrix3D adjoint()
	{
		return new Matrix3D(  minor( 1, 2, 3, 1, 2, 3 ), -minor( 0, 2, 3, 1, 2, 3 ), minor( 0, 1, 3, 1, 2, 3 ), -minor( 0, 1, 2, 1, 2, 3 ),
		                     -minor( 1, 2, 3, 0, 2, 3 ), minor( 0, 2, 3, 0, 2, 3 ), minor( 0, 1, 3, 0, 2, 3 ), minor( 0, 1, 2, 0, 2, 3 ),
		                      minor( 1, 2, 3, 0, 1, 3 ), -minor( 0, 2, 3, 0, 1, 3 ), minor( 0, 1, 3, 0, 1, 3 ), -minor( 0, 1, 2, 0, 1, 3 ),
		                     -minor( 1, 2, 3, 0, 1, 2 ), minor( 0, 2, 3, 0, 1, 2 ), -minor( 0, 1, 3, 0, 1, 2 ), minor( 0, 1, 2, 0, 1, 2 ) );
	}


	float determinant()
	{
		return m[ 0 ][ 0 ] * minor( 1, 2, 3, 1, 2, 3 ) -
		       m[ 0 ][ 1 ] * minor( 1, 2, 3, 0, 2, 3 ) +
		       m[ 0 ][ 2 ] * minor( 1, 2, 3, 0, 1, 3 ) -
		       m[ 0 ][ 3 ] * minor( 1, 2, 3, 0, 1, 2 );
	}

	Matrix3D inverse()
	{
		final Matrix3D adjoint = adjoint();
		return adjoint.multiply( 1.0f / determinant() );
	}

	float minor( final int r0, final int r1, final int r2, final int c0, final int c1, final int c2 )
	{
		return m[ r0 ][ c0 ] * ( m[ r1 ][ c1 ] * m[ r2 ][ c2 ] - m[ r2 ][ c1 ] * m[ r1 ][ c2 ] ) -
		       m[ r0 ][ c1 ] * ( m[ r1 ][ c0 ] * m[ r2 ][ c2 ] - m[ r2 ][ c0 ] * m[ r1 ][ c2 ] ) +
		                                                                                         m[ r0 ][ c2 ] * ( m[ r1 ][ c0 ] * m[ r2 ][ c1 ] - m[ r2 ][ c0 ] * m[ r1 ][ c1 ] );
	}
}