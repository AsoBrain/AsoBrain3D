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
 * Describes a triangle.
 *
 * @author  Amir Geva (original C++ version)
 * @author  Peter S. Heijnen (Java Port)
 * @version 1.1
 */
public class Triangle
{
	Vector3D v1;
	Vector3D v2;
	Vector3D v3;
	Vector3D center;

	/**
	 * Default constructor.
	 */
	Triangle()
	{
		v1 = Vector3D.Zero;
		v2 = Vector3D.Zero;
		v3 = Vector3D.Zero;
		center = Vector3D.Zero;
	}

	/**
	 * Copy constructor.
	 *
	 * @param   original    Original to copy.
	 */
	Triangle( final Triangle original )
	{
		v1 = original.v1;
		v2 = original.v2;
		v3 = original.v3;
		center = original.center;
	}

	/**
	 * Constructor to build a triangle from 3 points.
	 *
	 * @param   v1  First point.
	 * @param   v2  Second point.
	 * @param   v3  Third point.
	 */
	Triangle( final Vector3D v1 , final Vector3D v2 , final Vector3D v3 )
	{
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		center = new Vector3D( v1.x + v2.x + v3.x / 3.0f, v1.y + v2.y + v3.y / 3.0f, v1.z + v2.z + v3.z / 3.0f );
	}

	/**
	 * Tests for intersection with another triangle.
	 *
	 * @param   t   Triangle to test against.
	 */
	boolean intersect( final Triangle t )
	{
		return testTriangleTriangle( v1, v2, v3, t.v1, t.v2, t.v3 );
	}

	/* Triangle/triangle intersection test routine,
	 * by Tomas Moller, 1997.
	 * See article "A Fast Triangle-Triangle Intersection Test",
	 * Journal of Graphics Tools, 2(2), 1997
	 *
	 * parameters: vertices of triangle 1: V0,V1,V2
	 *             vertices of triangle 2: U0,U1,U2
	 * result    : returns 1 if the triangles intersect, otherwise 0
	 *
	 */
	private static boolean testTriangleTriangle( final Vector3D v0, final Vector3D v1, final Vector3D v2, final Vector3D u0, final Vector3D u1, final Vector3D u2 )
	{
		/* compute plane equation of triangle(V0,V1,V2) */
		Vector3D e1 = v1.minus( v0 );
		Vector3D e2 = v2.minus( v0 );
		final Vector3D n1 = e1.crossProduct( e2 );
		final float d1 = -n1.dotProduct( v0 );
		/* plane equation 1: N1.X+d1=0 */

		/* put U0,U1,U2 into plane equation 1 to compute signed distances to the plane*/
		float du0 = n1.dotProduct( u0 ) + d1;
		float du1 = n1.dotProduct( u1 ) + d1;
		float du2 = n1.dotProduct( u2 ) + d1;

		/* coplanarity robustness check */
		if ( Math.abs( du0 ) < 0.000001f ) du0 = 0.0f;
		if ( Math.abs( du1 ) < 0.000001f ) du1 = 0.0f;
		if ( Math.abs( du2 ) < 0.000001f ) du2 = 0.0f;
		final float du0du1 = du0 * du1;
		final float du0du2 = du0 * du2;

		if ( du0du1 > 0.0f && du0du2 > 0.0f ) /* same sign on all of them + not equal 0 ? */
			return false;                    /* no intersection occurs */

		/* compute plane of triangle (U0,U1,U2) */
		e1 = u1.minus( u0 );
		e2 = u2.minus( u0 );
		final Vector3D n2 = e1.crossProduct( e2 );
		final float d2 = -n2.dotProduct( u0 );
		/* plane equation 2: N2.X+d2=0 */

		/* put V0,V1,V2 into plane equation 2 */
		float dv0 = n2.dotProduct( v0 ) + d2;
		float dv1 = n2.dotProduct( v1 ) + d2;
		float dv2 = n2.dotProduct( v2 ) + d2;

		if ( Math.abs( dv0 ) < 0.000001f ) dv0 = 0.0f;
		if ( Math.abs( dv1 ) < 0.000001f ) dv1 = 0.0f;
		if ( Math.abs( dv2 ) < 0.000001f ) dv2 = 0.0f;

		final float dv0dv1 = dv0 * dv1;
		final float dv0dv2 = dv0 * dv2;

		if ( dv0dv1 > 0.0f && dv0dv2 > 0.0f ) /* same sign on all of them + not equal 0 ? */
			return false;                    /* no intersection occurs */

		/* compute direction of intersection line */
		final Vector3D d = n1.crossProduct( n2 );

		/* compute and index to the largest component of D */
		int index = 0;
		float max = Math.abs( d.x );
		final float b = Math.abs( d.y );
		final float c = Math.abs( d.z );
		if ( b > max )
		{
			max = b;
			index = 1;
		}
		if ( c > max )
		{
			index = 2;
		}

		/* this is the simplified projection onto L*/
		final float vp0 = v0.get( index );
		final float vp1 = v1.get( index );
		final float vp2 = v2.get( index );

		final float up0 = u0.get( index );
		final float up1 = u1.get( index );
		final float up2 = u2.get( index );

		/* compute interval for triangle 1 */
		final float[] isect1 = computeIntervals( vp0, vp1, vp2, dv0, dv1, dv2, dv0dv1, dv0dv2 );
		if ( isect1 == null )
		{
			/* triangles are coplanar */
			return testCoplanarTriangleTriangle( n1, v0, v1, v2, u0, u1, u2 );
		}

		/* compute interval for triangle 2 */
		final float[] isect2 = computeIntervals( up0, up1, up2, du0, du1, du2, du0du1, du0du2 );
		if ( isect2 == null )
		{
			/* triangles are coplanar */
			return testCoplanarTriangleTriangle( n1, v0, v1, v2, u0, u1, u2 );
		}

		/* there is an intersection if the intervals overlap */
		return ( Math.max( isect1[ 0 ], isect1[ 1 ] ) >= Math.min( isect2[ 0 ], isect2[ 1 ] ) ) &&
		       ( Math.max( isect2[ 0 ], isect2[ 1 ] ) >= Math.min( isect1[ 0 ], isect1[ 1 ] ) );
	}

	private static float[] computeIntervals( final float vv0, final float vv1, final float vv2, final float d0, final float d1, final float d2, final float d0d1, final float d0d2 )
	{
		final float[] result;

		if ( d0d1 > 0.0f )
		{
			/* here we know that D0D2<=0.0 */
			/* that is D0, D1 are on the same side, D2 on the other or on the plane */
			result = new float[] { vv2 + ( vv0 - vv2 ) * d2 / ( d2 - d0 ) ,
			                       vv2 + ( vv1 - vv2 ) * d2 / ( d2 - d1 ) };
		}
		else if ( d0d2 > 0.0f )
		{
			/* here we know that d0d1<=0.0 */
			result = new float[] { vv1 + ( vv0 - vv1 ) * d1 / ( d1 - d0 ) ,
			                       vv1 + ( vv2 - vv1 ) * d1 / ( d1 - d2 ) };
		}
		else if ( d1 * d2 > 0.0f || d0 != 0.0f )
		{
			/* here we know that d0d1<=0.0 or that D0!=0.0 */
			result = new float[] { vv0 + ( vv1 - vv0 ) * d0 / ( d0 - d1 ) ,
			                       vv0 + ( vv2 - vv0 ) * d0 / ( d0 - d2 ) };
		}
		else if ( d1 != 0.0f )
		{
			result = new float[] { vv1 + ( vv0 - vv1 ) * d1 / ( d1 - d0 ) ,
			                       vv1 + ( vv2 - vv1 ) * d1 / ( d1 - d2 ) };
		}
		else if ( d2 != 0.0f )
		{
			result = new float[] { vv2 + ( vv0 - vv2 ) * d2 / ( d2 - d0 ) ,
			                       vv2 + ( vv1 - vv2 ) * d2 / ( d2 - d1 ) };
		}
		else
		{
			result = null;
		}

		return result;
	}

	private static boolean testCoplanarTriangleTriangle( final Vector3D n, final Vector3D v0, final Vector3D v1, final Vector3D v2, final Vector3D u0, final Vector3D u1, final Vector3D u2 )
	{
		final int i0;
		final int i1;
		/* first project onto an axis-aligned plane, that maximizes the area */
		/* of the triangles, compute indices: i0,i1. */
		final Vector3D a = new Vector3D( Math.abs( n.x ), Math.abs( n.y ), Math.abs( n.z ) );

		if ( ( a.x >= a.y ) && ( a.x >= a.z ) ) /* A.x is greatest */
		{
			i0 = 1;
			i1 = 2;
		}
		else if ( ( a.y >= a.x ) && ( a.y >= a.z ) ) /* A.y is greatest */
		{
			i0 = 0;
			i1 = 2;
		}
		else /* A.z is greatest */
		{
			i0 = 0;
			i1 = 1;
		}

		/* test all edges of triangle 1 against the edges of triangle 2 */
		return testTriangleEdge( v0, v1, u0, u1, u2, i0, i1 ) ||
		       testTriangleEdge( v1, v2, u0, u1, u2, i0, i1 ) ||
		       testTriangleEdge( v2, v0, u0, u1, u2, i0, i1 ) ||

		       /* finally, test if tri1 is totally contained in tri2 or vice versa */
		       testTrianglePoint( v0, u0, u1, u2, i0, i1 ) ||
		       testTrianglePoint( u0, v0, v1, v2, i0, i1 );
	}


	private static boolean testTrianglePoint( final Vector3D v0, final Vector3D u0, final Vector3D u1, final Vector3D u2, final int i0, final int i1 )
	{
		/* is T1 completly inside T2? */
		/* check if V0 is inside tri(U0,U1,U2) */
		float a = u1.get( i1 ) - u0.get( i1 );
		float b = -( u1.get( i0 ) - u0.get( i0 ) );
		float c = -a * u0.get( i0 ) - b * u0.get( i1 );
		final float d0 = a * v0.get( i0 ) + b * v0.get( i1 ) + c;

		a = u2.get( i1 ) - u1.get( i1 );
		b = -( u2.get( i0 ) - u1.get( i0 ) );
		c = -a * u1.get( i0 ) - b * u1.get( i1 );
		final float d1 = a * v0.get( i0 ) + b * v0.get( i1 ) + c;

		a = u0.get( i1 ) - u2.get( i1 );
		b = -( u0.get( i0 ) - u2.get( i0 ) );
		c = -a * u2.get( i0 ) - b * u2.get( i1 );
		final float d2 = a * v0.get( i0 ) + b * v0.get( i1 ) + c;

		return ( ( d0 * d1 > 0.0f ) && ( d0 * d2 > 0.0f ) );
	}

	private static boolean testTriangleEdge( final Vector3D v0, final Vector3D v1, final Vector3D u0, final Vector3D u1, final Vector3D u2, final int i0, final int i1 )
	{
		final float ax = v1.get( i0 ) - v0.get( i0 );
		final float ay = v1.get( i1 ) - v0.get( i1 );

		return testEdgeEdge( v0, u0, u1, ax, ay, i0, i1 ) || /* test edge U0,U1 against V0,V1 */
		       testEdgeEdge( v0, u1, u2, ax, ay, i0, i1 ) || /* test edge U1,U2 against V0,V1 */
		       testEdgeEdge( v0, u2, u0, ax, ay, i0, i1 ); /* test edge U2,U1 against V0,V1 */
	}

	/* this edge to edge test is based on Franlin Antonio's gem:
	   "Faster Line Segment Intersection", in Graphics Gems III,
	   pp. 199-202 */
	private static boolean testEdgeEdge( final Vector3D v0, final Vector3D u0, final Vector3D u1, final float ax, final float ay, final int i0, final int i1 )
	{
		final float bx = u0.get( i0 ) - u1.get( i0 );
		final float by = u0.get( i1 ) - u1.get( i1 );
		final float cx = v0.get( i0 ) - u0.get( i0 );
		final float cy = v0.get( i1 ) - u0.get( i1 );
		final float f = ay * bx - ax * by;
		final float d = by * cx - bx * cy;

		final boolean result;

		if ( ( f > 0.0f ) && ( d >= 0.0f ) && ( d <= f ) )
		{
			final float e = ax * cy - ay * cx;
			result = ( ( e >= 0.0f ) && ( e <= f ) );
		}
		else if ( ( f < 0.0f ) && ( d <= 0.0f ) && ( d >= f ) )
		{
			final float e = ax * cy - ay * cx;
			result = ( ( e <= 0.0f ) && ( e >= f ) );
		}
		else
		{
			result = false;
		}

		return result;
	}
}


