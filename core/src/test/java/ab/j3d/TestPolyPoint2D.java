/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
 */
package ab.j3d;

import junit.framework.*;

/**
 * Test functionality of {@link PolyPoint2D} class.
 *
 * @author H.B.J. te Lintelo
 */
public class TestPolyPoint2D
extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestPolyPoint2D.class.getName();

	/**
	 * Test {@link PolyPoint2D#almostEquals(PolyPoint2D)}. In this test, two
	 * points with the same small coordinates should 'almost be equal'.
	 */
	public void testAlmostEqual1()
	{
		System.out.println( CLASS_NAME + ".testAlmostEqual1()" );

		final PolyPoint2D pp1 = new PolyPoint2D( 10.0, 10.0, 0.0 );
		final PolyPoint2D pp2 = new PolyPoint2D( 10.0, 10.0, 0.0 );
		assertTrue( "Points are equal, but almost equal test fails.", pp1.almostEquals( pp2 ) );
	}

	/**
	 * Test {@link PolyPoint2D#almostEquals(PolyPoint2D)}. In this test, two
	 * points with different small coordinates should NOT be equal.
	 */
	public void testAlmostEqual2()
	{
		System.out.println( CLASS_NAME + ".testAlmostEqual2()" );

		final PolyPoint2D pp1 = new PolyPoint2D( 10.0, 10.0, 0.0 );
		final PolyPoint2D pp2 = new PolyPoint2D( 11.0, 11.0, 0.0 );
		assertFalse( "Points are not equal, but almost equal test returns true.", pp1.almostEquals( pp2 ) );
	}

	/**
	 * Test {@link PolyPoint2D#almostEquals(PolyPoint2D)}. In this test, two
	 * points with the same large coordinates should 'almost be equal'.
	 *
	 * RELATED BUGS: <ul> <li> <b>BUG:</b><br /> RuntimeException: Line segment
	 * cannot have two intersections and end in convex shape. <br />
	 * <b>Symptom:</b><br /> Large panels cannot have an connection to each
	 * other. (i.e. back and ceiling) <br /> <b>Analysis:</b><br /> The
	 * intersection between the connected surfaces is calculated using
	 * PolyLine2D. In this class intersections are calculated per line segment
	 * and checked if they are 'inside' or 'outside' of the intersection. If two
	 * intersections are found, the start and end point are compared to the last
	 * found point, to see if the line segment is connected. This test is
	 * performed using the {@code PolyPoint2D.almostEquals( PolyPoint2D,
	 * tolerance )} method to cope with rounding errors. If the X or Y variable
	 * is larger than 2047 mm, the check mysteriously? Further analysis showed
	 * that the problem has to do with the maximum number of digits in a {@code
	 * float} and the method used to handle the tolerance. To compare some value
	 * (X) to another value (Y), the tolerance (0.0001) was added to and
	 * subtracted from X before comparing it to Y.
	 * <pre>
	 *         0-------*-------0
	 *     X-0.0001f < X < X+0.0001f
	 *    </pre>
	 * This, however, causes trouble with large values of X due to the lack of
	 * available digits (2048 +/- 0.0001 needs more digits than a float can
	 * handle), and therefore effectively eliminating the tolerance value . <br
	 * /> <b>Solution:</b><br /> Instead of adding/subtracting the tolerance to
	 * one of the value, the test is performed against the difference between X
	 * and Y, which should be between -tolerance and +tolerance. </li> </ul>
	 */
	public void testAlmostEqual3()
	{
		System.out.println( CLASS_NAME + ".testAlmostEqual3()" );

		final PolyPoint2D pp1 = new PolyPoint2D( 4000.0, 10.0, 0.0 );
		final PolyPoint2D pp2 = new PolyPoint2D( 4000.0, 10.0, 0.0 );
		assertTrue( "Points are equal, but almost equal test fails.", pp1.almostEquals( pp2 ) );
	}
}
