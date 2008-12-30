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

import ab.j3d.Vector3D;

/**
 * Triangle/triangle intersection test routine,
 * by Tomas Moller, 1997.
 * See article "A Fast Triangle-Triangle Intersection Test",
 * Journal of Graphics Tools, 2(2), 1997
 *
 * @author  Tomas Moller (original C version)
 * @author  Amir Geva (ColDet code)
 * @author  Peter S. Heijnen (Java Port)
 * @version 1.1
 */
public class TriTriMoeler
{
	/**
	 * Test intersection between to triangles in 3D.
	 *
	 * parameters: vertices of triangle 1: V0,V1,V2
	 *             vertices of triangle 2: U0,U1,U2
	 * result    : returns 1 if the triangles intersect, otherwise 0
	 *
	 */
	static boolean testTriangleTriangle( final Vector3D v0 , final Vector3D v1 , final Vector3D v2 , final Vector3D u0 , final Vector3D u1 , final Vector3D u2 )
	{
		/* compute plane equation of triangle(V0,V1,V2) */
		Vector3D e1 = v1.minus( v0 );
		Vector3D e2 = v2.minus( v0 );
		final Vector3D n1 = Vector3D.cross( e1 , e2 );
		final double d1 = -Vector3D.dot( n1 , v0 );
		/* plane equation 1: N1.X+d1=0 */

		/* put U0,U1,U2 into plane equation 1 to compute signed distances to the plane*/
		double du0 = Vector3D.dot( n1 , u0 ) + d1;
		double du1 = Vector3D.dot( n1 , u1 ) + d1;
		double du2 = Vector3D.dot( n1 , u2 ) + d1;

		/* coplanarity robustness check */
		if ( Math.abs( du0 ) < 0.000001 ) du0 = 0.0;
		if ( Math.abs( du1 ) < 0.000001 ) du1 = 0.0;
		if ( Math.abs( du2 ) < 0.000001 ) du2 = 0.0;
		final double du0du1 = du0 * du1;
		final double du0du2 = du0 * du2;

		if ( du0du1 > 0.0 && du0du2 > 0.0 ) /* same sign on all of them + not equal 0 ? */
			return false;                    /* no intersection occurs */

		/* compute plane of triangle (U0,U1,U2) */
		e1 = u1.minus( u0 );
		e2 = u2.minus( u0 );
		final Vector3D n2 = Vector3D.cross( e1, e2 );
		final double d2 = -Vector3D.dot( n2, u0 );
		/* plane equation 2: N2.X+d2=0 */

		/* put V0,V1,V2 into plane equation 2 */
		double dv0 = Vector3D.dot( n2 , v0 ) + d2;
		double dv1 = Vector3D.dot( n2 , v1 ) + d2;
		double dv2 = Vector3D.dot( n2 , v2 ) + d2;

		if ( Math.abs( dv0 ) < 0.000001 ) dv0 = 0.0;
		if ( Math.abs( dv1 ) < 0.000001 ) dv1 = 0.0;
		if ( Math.abs( dv2 ) < 0.000001 ) dv2 = 0.0;

		final double dv0dv1 = dv0 * dv1;
		final double dv0dv2 = dv0 * dv2;

		if ( dv0dv1 > 0.0 && dv0dv2 > 0.0 ) /* same sign on all of them + not equal 0 ? */
			return false;                    /* no intersection occurs */

		/* compute direction of intersection line */
		/* L = axis line with largest component of D */
		/* perform simplified projection onto L */
		final double absdx = Math.abs( n1.y * n2.z - n1.z * n2.y );
		final double absdy = Math.abs( n1.z * n2.x - n1.x * n2.z );
		final double absdz = Math.abs( n1.x * n2.y - n1.y * n2.x );

		final double vp0;
		final double vp1;
		final double vp2;

		final double up0;
		final double up1;
		final double up2;

		if ( ( absdx >= absdy ) && ( absdx >= absdz ) ) // L = X-axis
		{
			vp0 = v0.x;
			vp1 = v1.x;
			vp2 = v2.x;
			up0 = u0.x;
			up1 = u1.x;
			up2 = u2.x;
		}
		else if ( absdy >= absdz ) // L = Y-axis
		{
			vp0 = v0.y;
			vp1 = v1.y;
			vp2 = v2.y;
			up0 = u0.y;
			up1 = u1.y;
			up2 = u2.y;
		}
		else // L = Z-axis
		{
			vp0 = v0.z;
			vp1 = v1.z;
			vp2 = v2.z;
			up0 = u0.z;
			up1 = u1.z;
			up2 = u2.z;
		}

		/* compute interval for triangle 1 */
		final double[] isect1 = computeIntervals( vp0 , vp1 , vp2 , dv0 , dv1 , dv2 , dv0dv1 , dv0dv2 );
		if ( isect1 == null )
		{
			/* triangles are coplanar */
			return testCoplanarTriangleTriangle( n1, v0, v1, v2, u0, u1, u2 );
		}

		/* compute interval for triangle 2 */
		final double[] isect2 = computeIntervals( up0, up1, up2, du0, du1, du2, du0du1, du0du2 );
		if ( isect2 == null )
		{
			/* triangles are coplanar */
			return testCoplanarTriangleTriangle( n1, v0, v1, v2, u0, u1, u2 );
		}

		/* there is an intersection if the intervals overlap */
		return ( Math.max( isect1[ 0 ], isect1[ 1 ] ) >= Math.min( isect2[ 0 ], isect2[ 1 ] ) ) &&
		       ( Math.max( isect2[ 0 ], isect2[ 1 ] ) >= Math.min( isect1[ 0 ], isect1[ 1 ] ) );
	}

	static double[] computeIntervals( final double vv0, final double vv1, final double vv2, final double d0, final double d1, final double d2, final double d0d1, final double d0d2 )
	{
		final double[] result;

		if ( d0d1 > 0.0 )
		{
			/* here we know that D0D2<=0.0 */
			/* that is D0, D1 are on the same side, D2 on the other or on the plane */
			result = new double[] { vv2 + ( vv0 - vv2 ) * d2 / ( d2 - d0 ) ,
			                        vv2 + ( vv1 - vv2 ) * d2 / ( d2 - d1 ) };
		}
		else if ( d0d2 > 0.0 )
		{
			/* here we know that d0d1<=0.0 */
			result = new double[] { vv1 + ( vv0 - vv1 ) * d1 / ( d1 - d0 ) ,
			                        vv1 + ( vv2 - vv1 ) * d1 / ( d1 - d2 ) };
		}
		else if ( d1 * d2 > 0.0 || d0 != 0.0 )
		{
			/* here we know that d0d1<=0.0 or that D0!=0.0 */
			result = new double[] { vv0 + ( vv1 - vv0 ) * d0 / ( d0 - d1 ) ,
			                        vv0 + ( vv2 - vv0 ) * d0 / ( d0 - d2 ) };
		}
		else if ( d1 != 0.0 )
		{
			result = new double[] { vv1 + ( vv0 - vv1 ) * d1 / ( d1 - d0 ) ,
			                        vv1 + ( vv2 - vv1 ) * d1 / ( d1 - d2 ) };
		}
		else if ( d2 != 0.0 )
		{
			result = new double[] { vv2 + ( vv0 - vv2 ) * d2 / ( d2 - d0 ) ,
			                        vv2 + ( vv1 - vv2 ) * d2 / ( d2 - d1 ) };
		}
		else
		{
			result = null;
		}

		return result;
	}

	static boolean testCoplanarTriangleTriangle( final Vector3D n, final Vector3D v0, final Vector3D v1, final Vector3D v2, final Vector3D u0, final Vector3D u1, final Vector3D u2 )
	{
		final int i0;
		final int i1;
		/* first project onto an axis-aligned plane, that maximizes the area */
		/* of the triangles, compute indices: i0,i1. */
		final Vector3D a = n.set( Math.abs( n.x ), Math.abs( n.y ), Math.abs( n.z ) );

		if ( ( a.x >= a.y ) && ( a.x >= a.z ) ) /* A.x is greatest */
		{
			i0 = 1;
			i1 = 2;
		}
		else if ( a.y >= a.z ) /* A.y is greatest */
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


	static boolean testTriangleEdge( final Vector3D v0, final Vector3D v1, final Vector3D u0, final Vector3D u1, final Vector3D u2, final int i0, final int i1 )
	{
		final double ax = get( v1, i0 ) - get( v0, i0 );
		final double ay = get( v1, i1 ) - get( v0, i1 );

		return testEdgeEdge( v0, u0, u1, ax, ay, i0, i1 ) || /* test edge U0,U1 against V0,V1 */
		       testEdgeEdge( v0, u1, u2, ax, ay, i0, i1 ) || /* test edge U1,U2 against V0,V1 */
		       testEdgeEdge( v0, u2, u0, ax, ay, i0, i1 ); /* test edge U2,U1 against V0,V1 */
	}

	/* this edge to edge test is based on Franlin Antonio's gem:
	   "Faster Line Segment Intersection", in Graphics Gems III,
	   pp. 199-202 */
	static boolean testEdgeEdge( final Vector3D v0, final Vector3D u0, final Vector3D u1, final double ax, final double ay, final int i0, final int i1 )
	{
		final double bx = get( u0, i0 ) - get( u1, i0 );
		final double by = get( u0, i1 ) - get( u1, i1 );
		final double cx = get( v0, i0 ) - get( u0, i0 );
		final double cy = get( v0, i1 ) - get( u0, i1 );
		final double f = ay * bx - ax * by;
		final double d = by * cx - bx * cy;

		final boolean result;

		if ( ( f > 0.0 ) && ( d >= 0.0 ) && ( d <= f ) )
		{
			final double e = ax * cy - ay * cx;
			result = ( ( e >= 0.0 ) && ( e <= f ) );
		}
		else if ( ( f < 0.0 ) && ( d <= 0.0 ) && ( d >= f ) )
		{
			final double e = ax * cy - ay * cx;
			result = ( ( e <= 0.0 ) && ( e >= f ) );
		}
		else
		{
			result = false;
		}

		return result;
	}

	static boolean testTrianglePoint( final Vector3D v0, final Vector3D u0, final Vector3D u1, final Vector3D u2, final int i0, final int i1 )
	{
		/* is T1 completly inside T2? */
		/* check if V0 is inside tri(U0,U1,U2) */
		double a = get( u1, i1 ) - get( u0, i1 );
		double b = -( get( u1, i0 ) - get( u0, i0 ) );
		double c = -a * get( u0, i0 ) - b * get( u0, i1 );
		final double d0 = a * get( v0, i0 ) + b * get( v0, i1 ) + c;

		a = get( u2, i1 ) - get( u1, i1 );
		b = -( get( u2, i0 ) - get( u1, i0 ) );
		c = -a * get( u1, i0 ) - b * get( u1, i1 );
		final double d1 = a * get( v0, i0 ) + b * get( v0, i1 ) + c;

		a = get( u0, i1 ) - get( u2, i1 );
		b = -( get( u0, i0 ) - get( u2, i0 ) );
		c = -a * get( u2, i0 ) - b * get( u2, i1 );
		final double d2 = a * get( v0, i0 ) + b * get( v0, i1 ) + c;

		return ( ( d0 * d1 > 0.0 ) && ( d0 * d2 > 0.0 ) );
	}

	static double get( final Vector3D vector , final int i )
	{
		switch ( i )
		{
			case 0 : return vector.x;
			case 1 : return vector.y;
			case 2 : return vector.z;
			default : throw new AssertionError();
		}
	}
}
