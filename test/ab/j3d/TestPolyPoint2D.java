/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2002-2003 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.soda.mountings;

import junit.framework.TestCase;

/**
 * Test functionality of PolyPoint2D class.
 *
 * @see	PolyPoint2D
 *
 * @author	H.B.J. te Lintelo
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class TestPolyPoint2D
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestPolyPoint2D.class.getName();

	/**
	 * Test almostEqual( Polypoint2D ).
	 * In this test, two polypoints with the same small coordinates
	 * should 'almost be equal'.
	 */
	public static void testAlmostEqual1()
	{
		System.out.println( CLASS_NAME + ".testAlmostEqual1()" );

		final PolyPoint2D pp1 = new PolyPoint2D( 10f , 10f );
		final PolyPoint2D pp2 = new PolyPoint2D( 10f , 10f );
		assertTrue( "Points are equal, but almost equal test fails." , pp1.almostEquals( pp2 ) );
	}

	/**
	 * Test almostEqual( Polypoint2D ).
	 * In this test, two polypoints with different small coordinates
	 * should NOT be equal.
	 */
	public static void testAlmostEqual2()
	{
		System.out.println( CLASS_NAME + ".testAlmostEqual2()" );

		final PolyPoint2D pp1 = new PolyPoint2D( 10f , 10f );
		final PolyPoint2D pp2 = new PolyPoint2D( 11f , 11f );
		assertFalse( "Points are not equal, but almost equal test returns true." , pp1.almostEquals( pp2 ) );
	}

	/**
	 * Test almostEqual( Polypoint2D ).
	 * In this test, two polypoints with the same large coordinates
	 * should 'almost be equal'.
 	 * <P>
	 * RELATED BUGS:
	 * <UL>
	 *  <LI>
	 *    <B>BUG:</B><BR>
	 *    RuntimeException: Linesegment cannot have two intersections and end in convex shape.
	 *    <BR>
	 *    <B>Symptom:</B><BR>
 	 *    Large panels cannot have an connection to each other. (i.e. back and ceiling)
     *    <BR>
	 *    <B>Analysis:</B><BR>
	 *    The intersection between the connected surfaces is calculated using
	 *    PolyLine2D. In this class intersections are calculated per line
	 *    segment and checked if they are 'inside' or 'outside' of the
	 *    intersection. If two intersections are found, the start and end point
	 *    are compared to the last found point, to see if the line segment is
	 *    connected. This test is performed using the
	 *    <CODE>PolyPoint2D.almostEquals( PolyPoint2D , tolerance )</CODE>
	 *    method to cope with rounding errors. If the X or Y variable is larger
	 *    than 2047 mm, the check mysteriously? Further analysis showed that
	 *    the problem has to do with the maximum number of digits in a
	 *    <CODE>float</CODE> and the method used to handle the tolerance. To
	 *    compare some value (X) to another value (Y), the tolerance (0.0001)
	 *    was added to and subtracted from X before comparing it to Y.
	 *    <PRE>
	 *         0-------*-------0
	 *     X-0.0001f < X < X+0.0001f
	 *    </PRE>
	 *    This, however, causes trouble with large values of X due to the lack
	 *    of available digits (2048 +/- 0.0001 needs more digits than a float
	 *    can handle), and therefore effectively eliminating the tolerance value .
	 *    <BR>
	 *    <B>Solution:</B><BR>
	 *    Instead of adding/subtracting the tolerance to one of the value, the
	 *    text is performed against the difference between X and Y, which should
	 *    be between -tolerance and +tolerance.
	 *  </LI>
	 * </UL>
	 */
	public static void testAlmostEqual3()
	{
		System.out.println( CLASS_NAME + ".testAlmostEqual3()" );

		final PolyPoint2D pp1 = new PolyPoint2D( 4000f , 10f );
		final PolyPoint2D pp2 = new PolyPoint2D( 4000f , 10f );
		assertTrue( "Points are equal, but almost equal test fails." , pp1.almostEquals( pp2 ) );
	}
}
