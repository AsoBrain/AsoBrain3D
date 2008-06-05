/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2008-2008 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.geom;

import junit.framework.TestCase;

import ab.j3d.Vector3D;

import com.numdata.oss.MathTools;

/**
 * Test {@link Triangle3D} class.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class TestTriangle3D
	extends TestCase
{
	/**
	 * Tests the {@link Triangle3D#calculatePerimeter} and {@link Triangle3D#calculateArea} methods.
	 */
	public void testTriangleMath()
	{
		/*
		 * Create test class.
		 */
		class Test
		{
			Vector3D _p1;
			Vector3D _p2;
			Vector3D _p3;

			double _expectedPerimeter;
			double _expectedArea;

			Test( final Vector3D p1 , final Vector3D p2 , final Vector3D p3 , final double expectedPerimeter , final double expectedArea )
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
		final Test[] tests = new Test[]
			{
				/* Test 1 */ new Test( Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ) , Vector3D.INIT.set( 10.0 , 0.0 ,  0.0 ) , Vector3D.INIT.set( 10.0 , 10.0 ,  0.0 ) , 34.142 , 50.000 ) ,
				/* Test 2 */ new Test( Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ) , Vector3D.INIT.set( 10.0 , 0.0 ,  0.0 ) , Vector3D.INIT.set( 20.0 , 10.0 ,  0.0 ) , 46.502 , 49.999 ) ,
				/* Test 3 */ new Test( Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ) , Vector3D.INIT.set( 10.0 , 0.0 ,  0.0 ) , Vector3D.INIT.set(  5.0 , 10.0 ,  0.0 ) , 32.360 , 50.000 ) ,
				/* Test 4 */ new Test( Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ) , Vector3D.INIT.set(  0.0 , 0.0 , 10.0 ) , Vector3D.INIT.set(  0.0 , 10.0 , 10.0 ) , 34.142 , 50.000 ) ,
			};

		/*
		 * Run tests.
		 */
		int testNr = 1;

		for ( final Test test : tests )
		{
			final String desc = "Test #" + ( testNr++ ) + ": ";

			final double actualPerimeter = Triangle3D.calculatePerimeter( test._p1 , test._p2 , test._p3 );
			assertTrue( desc + "Invalid perimeter calculated. Expected: " + test._expectedPerimeter + " Actual: " + actualPerimeter , MathTools.almostEqual( test._expectedPerimeter , actualPerimeter ) );

			final double actualArea = Triangle3D.calculateArea( test._p1 , test._p2 , test._p3 );
			assertTrue( desc + "Invalid area calculated. Expected: " + test._expectedArea + " Actual: " + actualArea , MathTools.almostEqual( test._expectedArea , actualArea ) );
		}
	}
}
