/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2004-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import junit.framework.TestCase;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * This class tests the <code>FromToViewControl</code> class.
 *
 * @see     FromToViewControl
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class TestFromToViewControl
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestFromToViewControl.class.getName();


	/**
	 * Test the <code>FromToViewControl()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     FromToViewControl#FromToViewControl
	 */
	public void testFromToViewControl()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testFromToViewControl()" );

		final FromToViewControl fromToViewControl = new FromToViewControl();

		final Matrix3D expected = Matrix3D.INIT.set(
			 1 ,  0 ,  0 , 0 ,
			 0 ,  0 ,  1 , 0 ,
			 0 , -1 ,  0 , 0 );

		final Matrix3D actual = fromToViewControl.getTransform();

		assertTrue( "Initial transform failed!\nExpected:" + expected.toFriendlyString() + "\nActual:" + actual.toFriendlyString()
		          , expected.almostEquals( actual ) );
	}

	/**
	 * Test the <code>look()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     FromToViewControl#look
	 */
	public void testLook()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testLook()" );

		/*
		 * Define test properties.
		 */
		class Test
		{
			final Vector3D from;
			final Vector3D to;
			final Object   out;

			private Test( final Vector3D from , final Vector3D to , final Object out )
			{
				this.from = from;
				this.to   = to;
				this.out  = out;
			}
		}

		/*
		 * Define useful constants.
		 */
		final Vector3D v0     = Vector3D.INIT;
		final Matrix3D m0     = Matrix3D.INIT;
		final Vector3D origin = v0;


		/*
		 * Define 'extreme' tests.
		 */

		final Test[] extremeTests =
		{
			/* Test #1  */ new Test( null , null , NullPointerException.class ) ,
			/* Test #2  */ new Test( null , v0   , NullPointerException.class ) ,
			/* Test #3  */ new Test( v0   , null , NullPointerException.class ) ,
			/* Test #4  */ new Test( v0   , v0   , IllegalArgumentException.class ) ,
		};

		/*
		 * Define tests for orthogonal views.
		 */
		final Vector3D Xmin1  = v0.set( -1 ,  0 ,  0 );
		final Vector3D X1     = v0.set(  1 ,  0 ,  0 );
		final Vector3D Ymin1  = v0.set(  0 , -1 ,  0 );
		final Vector3D Y1     = v0.set(  0 ,  1 ,  0 );
		final Vector3D Zmin1  = v0.set(  0 ,  0 , -1 );
		final Vector3D Z1     = v0.set(  0 ,  0 ,  1 );

		final Matrix3D LEFT_VIEW = m0.set(
			 0 , -1 ,  0 , 0 ,
			 0 ,  0 ,  1 , 0 ,
			-1 ,  0 ,  0 , 0 );

		final Matrix3D RIGHT_VIEW = m0.set(
			 0 ,  1 ,  0 , 0 ,
			 0 ,  0 ,  1 , 0 ,
			 1 ,  0 ,  0 , 0 );

		final Matrix3D FRONT_VIEW = m0.set(
			 1 ,  0 ,  0 , 0 ,
			 0 ,  0 ,  1 , 0 ,
			 0 , -1 ,  0 , 0 );

		final Matrix3D BACK_VIEW = m0.set(
			-1 ,  0 ,  0 , 0 ,
			 0 ,  0 ,  1 , 0 ,
			 0 ,  1 ,  0 , 0 );

		final Matrix3D BOTTOM_VIEW = m0.set(
			-1 ,  0 ,  0 , 0 ,
			 0 ,  1 ,  0 , 0 ,
			 0 ,  0 , -1 , 0 );

		final Matrix3D TOP_VIEW = m0.set(
			 1 ,  0 ,  0 , 0 ,
			 0 ,  1 ,  0 , 0 ,
			 0 ,  0 ,  1 , 0 );

		final Test[] orthogonalTests =
		{
			/* Test #5  */ new Test( origin , X1    , LEFT_VIEW   ) ,
			/* Test #6  */ new Test( origin , Xmin1 , RIGHT_VIEW  ) ,
			/* Test #7  */ new Test( origin , Y1    , FRONT_VIEW  ) ,
			/* Test #8  */ new Test( origin , Ymin1 , BACK_VIEW   ) ,
			/* Test #9  */ new Test( origin , Z1    , BOTTOM_VIEW ) ,
			/* Test #10 */ new Test( origin , Zmin1 , TOP_VIEW    ) ,

			/* Test #11 */ new Test( Xmin1 , origin , LEFT_VIEW   .plus( 0 , 0 , -1 ) ) ,
			/* Test #12 */ new Test( X1    , origin , RIGHT_VIEW  .plus( 0 , 0 , -1 ) ) ,
			/* Test #13 */ new Test( Ymin1 , origin , FRONT_VIEW  .plus( 0 , 0 , -1 ) ) ,
			/* Test #14 */ new Test( Y1    , origin , BACK_VIEW   .plus( 0 , 0 , -1 ) ) ,
			/* Test #15 */ new Test( Zmin1 , origin , BOTTOM_VIEW .plus( 0 , 0 , -1 ) ) ,
			/* Test #16 */ new Test( Z1    , origin , TOP_VIEW    .plus( 0 , 0 , -1 ) ) ,
		};

		/*
		 * Define tests for diagonal views.
		 */
		final Vector3D X1Y0Z0 = v0.set(  1 , -1 , -1 );
		final Vector3D X0Y1Z0 = v0.set( -1 ,  1 , -1 );
		final Vector3D X1Y1Z0 = v0.set(  1 ,  1 , -1 );
		final Vector3D X0Y0Z1 = v0.set( -1 , -1 ,  1 );
		final Vector3D X1Y0Z1 = v0.set(  1 , -1 ,  1 );
		final Vector3D X0Y1Z1 = v0.set( -1 ,  1 ,  1 );
		final Vector3D X1Y1Z1 = v0.set(  1 ,  1 ,  1 );

		final double DEG45  = Math.PI / 4;
		final double DEG90  = Math.PI / 2;
		final double DEG135 = DEG90 + DEG45;
		final double SQRT2  = Math.sqrt( 2 );
		final double SQRT3  = Math.sqrt( 3 );

		final Test[] diagonalTests =
		{
			/* Test #17 */ new Test( v0.set(  0 , -1 , -1 ) , origin , m0.rotateX( DEG135 ).plus( 0 , 0 , -SQRT2 ) ) ,
			/* Test #18 */ new Test( v0.set( -1 ,  0 ,  1 ) , origin , m0.rotateY( DEG45 ).rotateZ( DEG90 ).plus( 0 , 0 , -SQRT2 ) ) ,
			/* Test #19 */ new Test( v0.set(  1 ,  0 ,  1 ) , origin , m0.rotateY( -DEG45 ).rotateZ( -DEG90 ).plus( 0 , 0 , -SQRT2 ) ) ,
			/* Test #20 */ new Test( v0.set(  1 ,  0 ,  0 ) , origin , m0.rotateZ( -DEG90 ).rotateX(  DEG90 ).plus( 0 , 0 , -1 ) ) ,
			/* Test #21 */ new Test( v0.set( -1 , -1 , -1 ) , origin , m0.rotateZ( Math.toRadians( 45 ) ).rotateX( Math.toRadians( 125.2 ) ).plus( 0 , 0 , -SQRT3 ) ) ,
		};

		/*
		 * Execute tests.
		 */
		final Test[][] allTests = { extremeTests , orthogonalTests , diagonalTests };
		int testNr = 1;
		for ( int i = 0 ; i < allTests.length ; i++ )
		{
			final Test[] tests = allTests[ i ];
			for ( int j = 0; j < tests.length; j++ )
			{
				final Test   test        = tests[ j ];
				final String description = "Test #" + testNr++;

				final Class expectedException = ( test.out instanceof Class ) ? (Class)test.out : null;
				try
				{
					final FromToViewControl fromToViewControl = new FromToViewControl( test.from , test.to );

					if ( expectedException != null )
						fail( description + " should have thrown exception" );

					final Matrix3D expected = (Matrix3D)test.out;
					final Matrix3D actual   = fromToViewControl.getTransform();

					assertTrue( description + "\nExpected:" + expected.toFriendlyString() + "\nActual:" + actual.toFriendlyString()
							  , expected.almostEquals( actual ) );
				}
				catch ( Exception e )
				{
					if ( expectedException == null )
					{
						System.err.println( description + " threw unexpected exception: " + e );
						throw e;
					}

					assertEquals( description + " threw wrong exception" , expectedException.getName() , e.getClass().getName() );
				}
			}
		}
	}
}
