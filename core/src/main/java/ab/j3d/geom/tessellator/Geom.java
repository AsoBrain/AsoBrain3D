/*
 * License Applicability. Except to the extent portions of this file are made
 * subject to an alternative license as permitted in the SGI Free Software
 * License B, Version 1.1 (the "License"), the contents of this file are subject
 * only to the provisions of the License. You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at Silicon
 * Graphics, Inc., attn: Legal Services, 1600 Amphitheatre Parkway, Mountain
 * View, CA 94043-1351, or at:
 *
 * http://oss.sgi.com/projects/FreeB
 *
 * Note that, as provided in the License, the Software is distributed on an
 * "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
 * DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
 * CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
 * PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
 *
 * NOTE: The Original Code (as defined below) has been licensed under the SGI
 * Free Software License B (Version 1.1), shown above ("SGI License"). Pursuant
 * to Section 3.2(3) of the SGI License, the Covered Code, is distributed as
 * in modified form as part of the AsoBrain 3D Toolkit, which is licensed under
 * an alternative license, the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. This alternative license applies to all code that
 * is not part of the "Original Code" (as defined below) and is
 * Copyright (C) 1999-2010 Peter S. Heijnen. You may obtain a copy of the
 * GNU Lesser General Public License from the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Original Code. The Original Code is: OpenGL Sample Implementation,
 * Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
 * Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
 * Copyright in any portions created by third parties is as indicated elsewhere
 * herein. All Rights Reserved.
 *
 * Author: Eric Veach, July 1994
 * Java Port: Pepijn Van Eeckhoudt, July 2003
 * Java Port: Nathan Parker Burg, August 2003
 * AsoBrain3D Port: Peter S. Heijnen, December 2010
 */
package ab.j3d.geom.tessellator;

import ab.j3d.*;

@SuppressWarnings( { "JavaDoc" } )
class Geom
{
	private Geom() {
	}

	/**
	 * Given three vertices u,v,w such that vertLeq(u,v) && vertLeq(v,w),
	 * evaluates the t-coord of the edge uw at the s-coord of the vertex v.
	 * Returns v->t - (uw)(v->s), ie. the signed distance from uw to v.
	 * If uw is vertical (and thus passes thru v), the result is zero.
	 *
	 * The calculation is extremely accurate and stable, even when v
	 * is very close to u or w.  In particular if we set v->t = 0 and
	 * let r be the negated result (this evaluates (uw)(v->s)), then
	 * r is guaranteed to satisfy MIN(u->t,w->t) <= r <= MAX(u->t,w->t).
	 */
	static double edgeEval( final Vertex u, final Vertex v, final Vertex w)
	{
		assert ( vertLeq(u, v) && vertLeq(v, w));

		final double ux = u.location.getX();
		final double vx = v.location.getX();
		final double wz = w.location.getX();

		final double gapL = vx - ux;
		final double gapR = wz - vx;

		if ( gapL + gapR > 0.0 )
		{
			final double uy = u.location.getY();
			final double vy = v.location.getY();
			final double wy = w.location.getY();

			if ( gapL < gapR )
			{
				return ( vy - uy ) + ( uy - wy ) * ( gapL / ( gapL + gapR ) );
			}
			else
			{
				return ( vy - wy ) + ( wy - uy ) * ( gapR / ( gapL + gapR ) );
			}
		}
		else
		{
			/* vertical line */
			return 0;
		}
	}

	static double edgeSign( final Vertex u, final Vertex v, final Vertex w )
	{
		assert ( vertLeq(u, v) && vertLeq(v, w));

		final double gapL = v.location.getX() - u.location.getX();
		final double gapR = w.location.getX() - v.location.getX();

		return ( ( gapL + gapR > 0.0 ) ) ? ( v.location.getY() - w.location.getY() ) * gapL + ( v.location.getY() - u.location.getY() ) * gapR : 0.0 /* vertical line */;
	}

	/***********************************************************************
	 * Define versions of EdgeSign, EdgeEval with s and t transposed.
	 */

	/**
	 * Given three vertices u,v,w such that {@link #transLeq(Vertex, Vertex)
	 * transLeq(u,v)} && {@link #transLeq(Vertex, Vertex) transLeq(v,w)}
	 * evaluates the t-coord of the edge uw at the s-coord of the vertex v.
	 * Returns v->s - (uw)(v->t), ie. the signed distance from uw to v.
	 * If uw is vertical (and thus passes thru v), the result is zero.
	 *
	 * The calculation is extremely accurate and stable, even when v
	 * is very close to u or w.  In particular if we set v->s = 0 and
	 * let r be the negated result (this evaluates (uw)(v->t)), then
	 * r is guaranteed to satisfy MIN(u->s,w->s) <= r <= MAX(u->s,w->s).
	 */
	static double transEval( final Vertex u, final Vertex v, final Vertex w)
	{
		final double gapL;
		final double gapR;

		assert ( transLeq( u, v ) && transLeq( v, w ) );

		gapL = v.location.getY() - u.location.getY();
		gapR = w.location.getY() - v.location.getY();

		if ( gapL + gapR > 0.0 )
		{
			if ( gapL < gapR )
			{
				return ( v.location.getX() - u.location.getX() ) + ( u.location.getX() - w.location.getX() ) * ( gapL / ( gapL + gapR ) );
			}
			else
			{
				return ( v.location.getX() - w.location.getX() ) + ( w.location.getX() - u.location.getX() ) * ( gapR / ( gapL + gapR ) );
			}
		}

		/* vertical line */
		return 0.0;
	}

	/**
	 * Returns a number whose sign matches {@link #transEval(Vertex, Vertex, Vertex)} but which
	 * is cheaper to evaluate.  Returns > 0, == 0 , or < 0
	 * as v is above, on, or below the edge uw.
	 */
	static double transSign( final Vertex u, final Vertex v, final Vertex w)
	{
		final double gapL;
		final double gapR;

		assert ( transLeq(u, v) && transLeq(v, w));

		gapL = v.location.getY() - u.location.getY();
		gapR = w.location.getY() - v.location.getY();

		if (gapL + gapR > 0) {
			return (v.location.getX() - w.location.getX() ) * gapL + (v.location.getX() - u.location.getX() ) * gapR;
		}
		/* vertical line */
		return 0;
	}


	/**
	 * Given parameters a,x,b,y returns the value (b*x+a*y)/(a+b),
	 * or (x+y)/2 if a==b==0.  It requires that a,b >= 0, and enforces
	 * this in the rare case that one argument is slightly negative.
	 * The implementation is extremely stable numerically.
	 * In particular it guarantees that the result r satisfies
	 * MIN(x,y) <= r <= MAX(x,y), and the results are very accurate
	 * even when a and b differ greatly in magnitude.
	 */
	static double interpolate( double a, final double x, double b, final double y )
	{
		a = (a < 0) ? 0 : a;
		b = (b < 0) ? 0 : b;
		if (a <= b) {
			if (b == 0) {
				return (x + y) / 2.0;
			} else {
				return (x + (y - x) * (a / (a + b)));
			}
		} else {
			return (y + (x - y) * (b / (a + b)));
		}
	}

	/**
	 * Given edges (o1,d1) and (o2,d2), compute their point of intersection.
	 * The computed point is guaranteed to lie in the intersection of the
	 * bounding rectangles defined by each edge.
	 */
	static Vector2D edgeIntersect( Vertex o1, Vertex d1, Vertex o2, Vertex d2 )
	{
		double z1;
		double z2;

		/* This is certainly not the most efficient way to find the intersection
		 * of two line segments, but it is very numerically stable.
		 *
		 * Strategy: find the two middle vertices in the {@link #vertLeq} ordering,
		 * and interpolate the intersection s-value from these.  Then repeat
		 * using the TransLeq ordering to find the intersection t-value.
		 */

		if ( !vertLeq( o1, d1 ) )
		{
			final Vertex temp = o1;
			o1 = d1;
			d1 = temp;
		}
		if ( !vertLeq( o2, d2 ) )
		{
			final Vertex temp = o2;
			o2 = d2;
			d2 = temp;
		}
		if ( !vertLeq( o1, o2 ) )
		{
			Vertex temp = o1;
			o1 = o2;
			o2 = temp;
			temp = d1;
			d1 = d2;
			d2 = temp;
		}

		final double x;

		if ( !vertLeq( o2, d1 ) )
		{
			/* Technically, no intersection -- do our best */
			x = ( o2.location.getX() + d1.location.getX() ) / 2.0;
		}
		else if ( vertLeq( d1, d2 ) )
		{
			/* Interpolate between o2 and d1 */
			z1 = edgeEval( o1, o2, d1 );
			z2 = edgeEval( o2, d1, d2 );
			if ( z1 + z2 < 0 )
			{
				z1 = -z1;
				z2 = -z2;
			}
			x = interpolate( z1, o2.location.getX(), z2, d1.location.getX() );
		}
		else
		{
			/* Interpolate between o2 and d2 */
			z1 = edgeSign( o1, o2, d1 );
			z2 = -edgeSign( o1, d2, d1 );
			if ( z1 + z2 < 0 )
			{
				z1 = -z1;
				z2 = -z2;
			}
			x = interpolate( z1, o2.location.getX(), z2, d2.location.getX() );
		}

		/* Now repeat the process for t */

		if ( !transLeq( o1, d1 ) )
		{
			final Vertex temp = o1;
			o1 = d1;
			d1 = temp;
		}
		if ( !transLeq( o2, d2 ) )
		{
			final Vertex temp = o2;
			o2 = d2;
			d2 = temp;
		}
		if ( !transLeq( o1, o2 ) )
		{
			Vertex temp = o2;
			o2 = o1;
			o1 = temp;
			temp = d2;
			d2 = d1;
			d1 = temp;
		}

		final double y;

		if ( !transLeq( o2, d1 ) )
		{
			/* Technically, no intersection -- do our best */
			y = ( o2.location.getY() + d1.location.getY() ) / 2.0;
		}
		else if ( transLeq( d1, d2 ) )
		{
			/* Interpolate between o2 and d1 */
			z1 = transEval( o1, o2, d1 );
			z2 = transEval( o2, d1, d2 );
			if ( z1 + z2 < 0 )
			{
				z1 = -z1;
				z2 = -z2;
			}
			y = interpolate( z1, o2.location.getY(), z2, d1.location.getY() );
		}
		else
		{
			/* Interpolate between o2 and d2 */
			z1 = transSign( o1, o2, d1 );
			z2 = -transSign( o1, d2, d1 );
			if ( z1 + z2 < 0 )
			{
				z1 = -z1;
				z2 = -z2;
			}
			y = interpolate( z1, o2.location.getY(), z2, d2.location.getY() );
		}

		return new Vector2D( x, y );
	}

	static boolean vertEq( final Vertex v1, final Vertex v2 )
	{
		return v1.location.getX() == v2.location.getX() && v1.location.getY() == v2.location.getY();
	}

	static boolean vertLeq( final Vertex v1, final Vertex v2 )
	{
		return v1.location.getX() < v2.location.getX() || ( v1.location.getX() == v2.location.getX() && v1.location.getY() <= v2.location.getY() );
	}

	/* Versions of {@link #vertLeq}, {@link #edgeSign}, {@link #edgeEval} with s and t transposed. */

	private static boolean transLeq( final Vertex u, final Vertex v )
	{
		return ( u.location.getY() < v.location.getY() ) || ( u.location.getY() == v.location.getY() && u.location.getX() <= v.location.getX() );
	}


	static final double EPSILON = 1.0e-5;

	static final double ONE_MINUS_EPSILON = 1.0 - EPSILON;
}
