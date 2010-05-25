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

import junit.framework.TestCase;

/**
 * Test functionality of <code>Polyline2D</code> class.
 *
 * @see     Polyline2D
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class TestPolyline2D
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestPolyline2D.class.getName();

	/**
	 * Test constructor for rectangular polyline.
	 */
	public static void testRectangleConstructor()
	{
		System.out.println( CLASS_NAME + ".testRectangleConstructor()" );

		final Polyline2D pl = new Polyline2D( 123.0 , 456.0 );
		assertEquals( "rectangle should have 5 points!" , 5 , pl.getPointCount() );

		assertEquals( "rectangle[ 0 ] is off" , new PolyPoint2D(   0.0 ,   0.0 , 0.0 ) , pl.getPoint( 0 ) );
		assertEquals( "rectangle[ 1 ] is off" , new PolyPoint2D( 123.0 ,   0.0 , 0.0 ) , pl.getPoint( 1 ) );
		assertEquals( "rectangle[ 2 ] is off" , new PolyPoint2D( 123.0 , 456.0 , 0.0 ) , pl.getPoint( 2 ) );
		assertEquals( "rectangle[ 3 ] is off" , new PolyPoint2D(   0.0 , 456.0 , 0.0 ) , pl.getPoint( 3 ) );
		assertEquals( "rectangle[ 4 ] is off" , new PolyPoint2D(   0.0 ,   0.0 , 0.0 ) , pl.getPoint( 4 ) );
	}

	/**
	 * Test constructor for horizontal line.
	 */
	public static void testHorizontalLineConstructor()
	{
		System.out.println( CLASS_NAME + ".testHorizontalLineConstructor()" );

		final Polyline2D pl = new Polyline2D( 123.0 , 0.0 );
		assertEquals( "horizontal line should have 2 points" , 2 , pl.getPointCount() );

		assertEquals( "horizontal line[ 0 ] is off" , new PolyPoint2D(   0.0 , 0.0 , 0.0 ) , pl.getPoint( 0 ) );
		assertEquals( "horizontal line[ 1 ] is off" , new PolyPoint2D( 123.0 , 0.0 , 0.0 ) , pl.getPoint( 1 ) );
	}

	/**
	 * Test constructor for vertical line.
	 */
	public static void testVerticalLineConstructor()
	{
		System.out.println( CLASS_NAME + ".testVerticalLineConstructor()" );

		final Polyline2D pl = new Polyline2D( 0.0 , 456.0 );
		assertEquals( "vertical line should have 2 points" , 2 , pl.getPointCount() );

		assertEquals( "vertical line[ 0 ] is off" , new PolyPoint2D( 0.0 ,   0.0 , 0.0 ) , pl.getPoint( 0 ) );
		assertEquals( "vertical line[ 1 ] is off" , new PolyPoint2D( 0.0 , 456.0 , 0.0 ) , pl.getPoint( 1 ) );
	}

	/**
	 * Test getIntersection for two rectangular polylines.
	 * In this test, no intersection should be found.
	 */
	public static void testGetIntersection1()
	{
		System.out.println( CLASS_NAME + ".testGetIntersection1()" );

		final Polyline2D pl1 = new Polyline2D();
		pl1.addStartPoint (     0.0 ,  0.0 );
		pl1.addLineSegment(  1000.0 ,  0.0 );
		pl1.addLineSegment(  1000.0 , 18.0 );
		pl1.addLineSegment(     0.0 , 18.0 );
		pl1.close();

		final Polyline2D pl2 = new Polyline2D();
		pl2.addStartPoint (     0.0 , 19.0 );
		pl2.addLineSegment(  2000.0 , 19.0 );
		pl2.addLineSegment(  2000.0 , 39.0 );
		pl2.addLineSegment(     0.0 , 39.0 );
		pl2.close();

		final Polyline2D interSection = pl1.getIntersection(  pl2 );
		assertNull( "Intersection found! (but no intersection is possible)" , interSection );
	}

	/**
	 * Test getIntersection for two rectangular polylines.
	 * In this test, an intersection should be found.
	 */
	public static void testGetIntersection2()
	{
		System.out.println( CLASS_NAME + ".testGetIntersection2()" );

		final Polyline2D pl1 = new Polyline2D();
		pl1.addStartPoint (     0.0 ,     0.0 );
		pl1.addLineSegment(  2047.0 ,     0.0 );
		pl1.addLineSegment(  2047.0 ,    18.0 );
		pl1.addLineSegment(     0.0 ,    18.0 );
		pl1.close();

		final Polyline2D pl2 = new Polyline2D();
		pl2.addStartPoint (     0.0 ,  -363.0 );
		pl2.addLineSegment(  2047.0 ,  -363.0 );
		pl2.addLineSegment(  2047.0 ,    35.0 );
		pl2.addLineSegment(     0.0 ,    35.0 );
		pl2.close();

		final Polyline2D interSection = pl1.getIntersection(  pl2 );
		assertNotNull( "No intersection found! (normal x, y values)" , interSection );
	}

	/**
	 * Test getIntersection for two rectangular polylines.
	 * In this test, two intersections should be found.
 	 * <p />
	 * RELATED BUGS:
	 * <ul>
	 *  <li>
	 *    <b>BUG:</b><br />
	 *    RuntimeException: Linesegment cannot have two intersections and end in convex shape.
	 *    <br />
	 *    <b>Symptom:</b><br />
 	 *    Large panels cannot have an connection to each other. (i.e. back and ceiling)
	 *    <br />
	 *    <b>Analysis:</b><br />
	 *    The intersection between the connected surfaces is calculated using
	 *    PolyLine2D. In this class intersections are calculated per line
	 *    segment and checked if they are 'inside' or 'outside' of the
	 *    intersection. If two intersections are found, the start and end point
	 *    are compared to the last found point, to see if the line segment is
	 *    connected. This test is performed using the
	 *    <code>PolyPoint2D.almostEquals( PolyPoint2D , tolerance )</code>
	 *    method to cope with rounding errors. If the X or Y variable is larger
	 *    than 2047 mm, the check mysteriously? Further analysis showed that
	 *    the problem has to do with the maximum number of digits in a
	 *    <code>float</code> and the method used to handle the tolerance. To
	 *    compare some value (X) to another value (Y), the tolerance (0.0001)
	 *    was added to and subtracted from X before comparing it to Y.
	 *    <pre>
	 *         0-------*-------0
	 *     X-0.0001f < X < X+0.0001f
	 *    </pre>
	 *    This, however, causes trouble with large values of X due to the lack
	 *    of available digits (2048 +/- 0.0001 needs more digits than a float
	 *    can handle), and therefore effectively eliminating the tolerance value .
	 *    <br />
	 *    <b>Solution:</b><br />
	 *    Instead of adding/subtracting the tolerance to one of the value, the
	 *    text is performed against the difference between X and Y, which should
	 *    be between -tolerance and +tolerance.
	 *  </li>
	 * </ul>
	 */
	public static void testGetIntersection3()
	{
		System.out.println( CLASS_NAME + ".testGetIntersection3()" );

		final Polyline2D pl1 = new Polyline2D();
		pl1.addStartPoint (     0.0 ,     0.0 );
		pl1.addLineSegment(  4000.0 ,     0.0 );
		pl1.addLineSegment(  4000.0 ,    18.0 );
		pl1.addLineSegment(     0.0 ,    18.0 );
		pl1.close();

		final Polyline2D pl2 = new Polyline2D();
		pl2.addStartPoint (     0.0 ,  -363.0 );
		pl2.addLineSegment(  4000.0 ,  -363.0 );
		pl2.addLineSegment(  4000.0 ,    35.0 );
		pl2.addLineSegment(     0.0 ,    35.0 );
		pl2.close();

		final Polyline2D interSection = pl1.getIntersection(  pl2 );
		assertNotNull( "No intersection found! (large x value)" , interSection );
	}

	/**
	 * Tests intersection of a rectangle with a path that intersects with the
	 * rectangle's corners.
	 */
	public void testGetIntersection4()
	{
		System.out.println( CLASS_NAME + ".testGetIntersection4()" );

		final Polyline2D convex = new Polyline2D();
		convex.addStartPoint(    0.0 ,   0.0 );
		convex.addLineSegment( 700.0 ,   0.0 );
		convex.addLineSegment( 700.0 , 400.0 );
		convex.addLineSegment(   0.0 , 400.0 );
		convex.close();

		final Polyline2D path = new Polyline2D();
		path.addStartPoint(  -400.0 , -300.0 );
		path.addLineSegment(  400.0 ,  300.0 );
		path.addLineSegment(  700.0 ,  400.0 );

		final Polyline2D expected = new Polyline2D();
		expected.addStartPoint(    0.0 ,   0.0 );
		expected.addLineSegment( 400.0 , 300.0 );
		expected.addLineSegment( 700.0 , 400.0 );

		final Polyline2D intersection = convex.getIntersection( expected );
		assertEquals( "Unexpected result." , expected , intersection );
	}

	/**
	 * Tests various intersections of a convex and a path, where the path
	 * overlaps one of the convex's edges.
	 */
	public void testGetIntersection5()
	{
		System.out.print( "Expect to fail: " );
		try
		{
			FailedTestPolyline2D.testGetIntersection5();
			fail( "Expected test to fail" );
		}
		catch ( Exception e )
		{
			/* we expect the test to fail, so this is good */
			e.printStackTrace();
		}
	}
}
