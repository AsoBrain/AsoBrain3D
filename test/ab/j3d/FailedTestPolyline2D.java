/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2001-2005 Numdata BV
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

import junit.framework.*;

/**
 * Tests taken from {@link TestPolyline2D} that are known to fail.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class FailedTestPolyline2D
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = FailedTestPolyline2D.class.getName();

	/**
	 * Tests various intersections of a convex and a path, where the path
	 * overlaps one of the convex's edges.
	 */
	public static void testGetIntersection5()
	{
		System.out.println( CLASS_NAME + ".testGetIntersection5()" );

		System.out.println( " - Inside, line overlap, inside." );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(    0.0 , 299.5 );
			path.addLineSegment( 700.0 , 299.5 );
			path.addLineSegment( 700.0 , 300.5 );
			path.addLineSegment(   0.0 , 300.5 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , path , intersection );
		}

		System.out.println( " - Inside, line overlap, outside" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(    0.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 700.0 , 350.0 );
			path.addLineSegment( 800.0 , 350.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(    0.0 , 300.0 );
			expected.addLineSegment( 700.0 , 300.0 );
			expected.addLineSegment( 700.0 , 350.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Inside, line overlap, outside (through inside)" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(    0.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 700.0 , 500.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(    0.0 , 300.0 );
			expected.addLineSegment( 700.0 , 300.0 );
			expected.addLineSegment( 700.0 , 400.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Outside, line overlap, outside" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(  800.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 700.0 , 350.0 );
			path.addLineSegment( 800.0 , 350.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(  700.0 , 300.0 );
			expected.addLineSegment( 700.0 , 350.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Outside, line overlap, inside" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(  800.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 700.0 , 350.0 );
			path.addLineSegment( 600.0 , 350.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(  700.0 , 300.0 );
			expected.addLineSegment( 700.0 , 350.0 );
			expected.addLineSegment( 600.0 , 350.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Inside, point overlap, inside" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(    0.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 600.0 , 350.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(    0.0 , 300.0 );
			expected.addLineSegment( 700.0 , 300.0 );
			expected.addLineSegment( 600.0 , 350.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Inside, point overlap, inside, outside" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(    0.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 600.0 , 350.0 );
			path.addLineSegment( 800.0 , 350.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(    0.0 , 300.0 );
			expected.addLineSegment( 700.0 , 300.0 );
			expected.addLineSegment( 600.0 , 350.0 );
			expected.addLineSegment( 700.0 , 350.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Inside, point overlap, outside" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(    0.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 800.0 , 350.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(    0.0 , 300.0 );
			expected.addLineSegment( 700.0 , 300.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Inside, point overlap, outside (through inside)" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(    0.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 500.0 , 500.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(    0.0 , 300.0 );
			expected.addLineSegment( 700.0 , 300.0 );
			expected.addLineSegment( 600.0 , 400.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Outside, point overlap, outside" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(  800.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 800.0 , 350.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint( 700.0 , 300.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Outside, point overlap, inside" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint(  800.0 , 300.0 );
			path.addLineSegment( 700.0 , 300.0 );
			path.addLineSegment( 600.0 , 350.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(  700.0 , 300.0 );
			expected.addLineSegment( 600.0 , 350.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}

		System.out.println( " - Outside, through inside, outside" );
		{
			final Polyline2D convex = new Polyline2D();
			convex.addStartPoint(    0.0 ,   0.0 );
			convex.addLineSegment( 700.0 ,   0.0 );
			convex.addLineSegment( 700.0 , 400.0 );
			convex.addLineSegment(   0.0 , 400.0 );
			convex.close();

			final Polyline2D path = new Polyline2D();
			path.addStartPoint( -100.0 , 300.0 );
			path.addLineSegment( 800.0 , 300.0 );

			final Polyline2D expected = new Polyline2D();
			expected.addStartPoint(    0.0 , 300.0 );
			expected.addLineSegment( 700.0 , 300.0 );

			final Polyline2D intersection = convex.getIntersection( path );
			assertEquals( "Unexpected result." , expected , intersection );
		}
	}
}