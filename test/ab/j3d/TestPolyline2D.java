/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2001-2004 Numdata BV
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

		final Polyline2D pl = new Polyline2D( 123 , 456 );
		assertEquals( "rectangle should have 5 points!" , 5 , pl.getPointCount() );

		assertEquals( "rectangle[ 0 ] is off" , new PolyPoint2D(   0 ,   0 ) , pl.getPoint( 0 ) );
		assertEquals( "rectangle[ 1 ] is off" , new PolyPoint2D( 123 ,   0 ) , pl.getPoint( 1 ) );
		assertEquals( "rectangle[ 2 ] is off" , new PolyPoint2D( 123 , 456 ) , pl.getPoint( 2 ) );
		assertEquals( "rectangle[ 3 ] is off" , new PolyPoint2D(   0 , 456 ) , pl.getPoint( 3 ) );
		assertEquals( "rectangle[ 4 ] is off" , new PolyPoint2D(   0 ,   0 ) , pl.getPoint( 4 ) );
	}

	/**
	 * Test constructor for horizontal line.
	 */
	public static void testHorizontalLineConstructor()
	{
		System.out.println( CLASS_NAME + ".testHorizontalLineConstructor()" );

		final Polyline2D pl = new Polyline2D( 123 , 0 );
		assertEquals( "horizontal line should have 2 points" , 2 , pl.getPointCount() );

		assertEquals( "horizontal line[ 0 ] is off" , new PolyPoint2D(   0 , 0 ) , pl.getPoint( 0 ) );
		assertEquals( "horizontal line[ 1 ] is off" , new PolyPoint2D( 123 , 0 ) , pl.getPoint( 1 ) );
	}

	/**
	 * Test constructor for vertical line.
	 */
	public static void testVerticalLineConstructor()
	{
		System.out.println( CLASS_NAME + ".testVerticalLineConstructor()" );

		final Polyline2D pl = new Polyline2D( 0 , 456 );
		assertEquals( "vertical line should have 2 points" , 2 , pl.getPointCount() );

		assertEquals( "vertical line[ 0 ] is off" , new PolyPoint2D( 0 ,   0 ) , pl.getPoint( 0 ) );
		assertEquals( "vertical line[ 1 ] is off" , new PolyPoint2D( 0 , 456 ) , pl.getPoint( 1 ) );
	}

	/**
	 * Test getIntersection for two rectangular polylines.
	 * In this test, no intersection should be found.
	 */
	public static void testGetIntersection1()
	{
		System.out.println( CLASS_NAME + ".testGetIntersection1()" );

		final Polyline2D pl1 = new Polyline2D();
		pl1.append(     0 ,     0 );
		pl1.append(  1000 ,     0 );
		pl1.append(  1000 ,    18 );
		pl1.append(     0 ,    18 );
		pl1.append(     0 ,     0 );

		final Polyline2D pl2 = new Polyline2D();
		pl2.append(     0 ,    19 );
		pl2.append(  2000 ,    19 );
		pl2.append(  2000 ,    39 );
		pl2.append(     0 ,    39 );
		pl2.append(     0 ,    19 );

		final Polyline2D interSection = pl1.getIntersection(  pl2 );
		assertTrue( "Intersection found! (but no intersection is possible)" , interSection == null );
	}

	/**
	 * Test getIntersection for two rectangular polylines.
	 * In this test, an intersection should be found.
	 */
	public static void testGetIntersection2()
	{
		System.out.println( CLASS_NAME + ".testGetIntersection2()" );

		final Polyline2D pl1 = new Polyline2D();
		pl1.append(     0 ,     0 );
		pl1.append(  2047 ,     0 );
		pl1.append(  2047 ,    18 );
		pl1.append(     0 ,    18 );
		pl1.append(     0 ,     0 );

		final Polyline2D pl2 = new Polyline2D();
		pl2.append(     0 ,  -363 );
		pl2.append(  2047 ,  -363 );
		pl2.append(  2047 ,    35 );
		pl2.append(     0 ,    35 );
		pl2.append(     0 ,  -363 );

		final Polyline2D interSection = pl1.getIntersection(  pl2 );
		assertTrue( "No intersection found! (normal x, y values)" , interSection != null );
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
		pl1.append(     0 ,     0 );
		pl1.append(  4000 ,     0 );
		pl1.append(  4000 ,    18 );
		pl1.append(     0 ,    18 );
		pl1.append(     0 ,     0 );

		final Polyline2D pl2 = new Polyline2D();
		pl2.append(     0 ,  -363 );
		pl2.append(  4000 ,  -363 );
		pl2.append(  4000 ,    35 );
		pl2.append(     0 ,    35 );
		pl2.append(     0 ,  -363 );

		final Polyline2D interSection = pl1.getIntersection(  pl2 );
		assertTrue( "No intersection found! (large x value)" , interSection != null );
	}
}
