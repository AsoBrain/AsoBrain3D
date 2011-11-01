/* $Id$
 * ====================================================================
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
 * ====================================================================
 */
package ab.j3d.geom;

import ab.j3d.*;
import junit.framework.*;

/**
 * Test {@link BasicTriangle3D} class.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class TestAbstractTriangle3D
	extends TestCase
{
	/**
	 * Tests the {@link AbstractTriangle3D#calculatePerimeter} and {@link AbstractTriangle3D#calculateArea} methods.
	 */
	public void testTriangleMath()
	{
		/**
		 * Test properties.
		 *
		 * @noinspection JavaDoc
		 */
		class Test
		{
			Vector3D _p1;
			Vector3D _p2;
			Vector3D _p3;

			double _expectedPerimeter;
			double _expectedArea;

			Test( final Vector3D p1, final Vector3D p2, final Vector3D p3, final double expectedPerimeter, final double expectedArea )
			{
				_p1 = p1;
				_p2 = p2;
				_p3 = p3;

				_expectedPerimeter = expectedPerimeter;
				_expectedArea      = expectedArea;
			}
		}

		/*
		 * Create tests.
		 */
		final Test[] tests =
			{
				/* Test 1 */ new Test( Vector3D.ZERO, new Vector3D( 10.0, 0.0,  0.0 ), new Vector3D( 10.0, 10.0,  0.0 ), 34.142, 50.000 ),
				/* Test 2 */ new Test( Vector3D.ZERO, new Vector3D( 10.0, 0.0,  0.0 ), new Vector3D( 20.0, 10.0,  0.0 ), 46.502, 49.999 ),
				/* Test 3 */ new Test( Vector3D.ZERO, new Vector3D( 10.0, 0.0,  0.0 ), new Vector3D(  5.0, 10.0,  0.0 ), 32.360, 50.000 ),
				/* Test 4 */ new Test( Vector3D.ZERO, new Vector3D(  0.0, 0.0, 10.0 ), new Vector3D(  0.0, 10.0, 10.0 ), 34.142, 50.000 ),
			};

		/*
		 * Run tests.
		 */
		int testNr = 1;

		for ( final Test test : tests )
		{
			final String desc = "Test #" + ( testNr++ ) + ": ";

			final double actualPerimeter = AbstractTriangle3D.calculatePerimeter( test._p1, test._p2, test._p3 );
			assertTrue( desc + "Invalid perimeter calculated. Expected: " + test._expectedPerimeter + " Actual: " + actualPerimeter, MathTools.almostEqual( test._expectedPerimeter, actualPerimeter ) );

			final double actualArea = AbstractTriangle3D.calculateArea( test._p1, test._p2, test._p3 );
			assertTrue( desc + "Invalid area calculated. Expected: " + test._expectedArea + " Actual: " + actualArea, MathTools.almostEqual( test._expectedArea, actualArea ) );
		}
	}
}