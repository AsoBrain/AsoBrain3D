/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
 * This test verifies the <code>Matrix3D</code> class.
 *
 * @see     Matrix3D
 *
 * @author  Peter S. Heijnen
 * @version $Revision $ $Date$
 */
public class TestMatrix3D
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestMatrix3D.class.getName();

	/**
	 * Test <code>Matrix3D.equals()</code> method.
	 *
	 * RELATED BUGS (SOLVED):
	 * <ul>
	 *  <li>
	 *    <b>BUG:</b><br />
	 *    OBJ files contain bad geometric data.
	 *    <br />
	 *    <b>Symptom:</b><br />
	 *    The geometry for a 2nd scenario contains negative Y coordinates.
	 *    This seems to occur with almost every panel.
	 *    <br />
	 *    <b>Analysis:</b><br />
	 *    Matrix3D.equals() method did not compare the translation correctly
	 *    (comparing this.xo to other.xo/yo/zo instead of this.xo/yo/zo).
	 *    Incredible how this bug has never been spotted before.
	 *    <br />
	 *    <b>Fix:</b><br />
	 *    Fixed xo/yo/zo test in Matrix3D.equals() method.
	 *  </li>
	 * </ul>
	 */
	public static void testEquals()
	{
		System.out.println( CLASS_NAME + ".testEquals()" );
		final Matrix3D i = Matrix3D.INIT;
		Matrix3D m;

		/*
		 * INIT must match identity matrix
		 */
		assertEquals( "Matrix3D.equals() returned 'false' where it should have returned 'true'"
		            , i.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 ) , i );

		/*
		 * Test if each component is correctly tested by equals()
		 */
		m = i.set( 9.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xx'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 9.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xy'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 9.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xz'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 0.0 , 9.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xo'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 0.0 , 0.0 , 9.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yx'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 9.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yy'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 9.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yz'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 9.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yo'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 9.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zx'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 9.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zy'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 9.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zz'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 9.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zo'" , !i.equals( m ) && !m.equals( i ) );
	}

	/**
	 * Test the <code>getFromToTransform()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 * @see     Matrix3D#getFromToTransform
	 */
	public void testGetFromToTransform()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetFromToTransform()" );

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
		final Vector3D Xmin1  = v0.set( -1.0 ,  0.0 ,  0.0 );
		final Vector3D X1     = v0.set(  1.0 ,  0.0 ,  0.0 );
		final Vector3D Ymin1  = v0.set(  0.0 , -1.0 ,  0.0 );
		final Vector3D Y1     = v0.set(  0.0 ,  1.0 ,  0.0 );
		final Vector3D Zmin1  = v0.set(  0.0 ,  0.0 , -1.0 );
		final Vector3D Z1     = v0.set(  0.0 ,  0.0 ,  1.0 );

		final Matrix3D LEFT_VIEW = m0.set(
			 0.0 , -1.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 ,  1.0 , 0.0 ,
			-1.0 ,  0.0 ,  0.0 , 0.0 );

		final Matrix3D RIGHT_VIEW = m0.set(
			 0.0 ,  1.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 ,  1.0 , 0.0 ,
			 1.0 ,  0.0 ,  0.0 , 0.0 );

		final Matrix3D FRONT_VIEW = m0.set(
			 1.0 ,  0.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 ,  1.0 , 0.0 ,
			 0.0 , -1.0 ,  0.0 , 0.0 );

		final Matrix3D BACK_VIEW = m0.set(
			-1.0 ,  0.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 ,  1.0 , 0.0 ,
			 0.0 ,  1.0 ,  0.0 , 0.0 );

		final Matrix3D BOTTOM_VIEW = m0.set(
			-1.0 ,  0.0 ,  0.0 , 0.0 ,
			 0.0 ,  1.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 , -1.0 , 0.0 );

		final Matrix3D TOP_VIEW = m0.set(
			 1.0 ,  0.0 ,  0.0 , 0.0 ,
			 0.0 ,  1.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 ,  1.0 , 0.0 );

		final Test[] orthogonalTests =
		{
			/* Test #5  */ new Test( origin , X1    , LEFT_VIEW   ) ,
			/* Test #6  */ new Test( origin , Xmin1 , RIGHT_VIEW  ) ,
			/* Test #7  */ new Test( origin , Y1    , FRONT_VIEW  ) ,
			/* Test #8  */ new Test( origin , Ymin1 , BACK_VIEW   ) ,
			/* Test #9  */ new Test( origin , Z1    , BOTTOM_VIEW ) ,
			/* Test #10 */ new Test( origin , Zmin1 , TOP_VIEW    ) ,

			/* Test #11 */ new Test( Xmin1 , origin , LEFT_VIEW   .plus( 0.0 , 0.0 , -1.0 ) ) ,
			/* Test #12 */ new Test( X1    , origin , RIGHT_VIEW  .plus( 0.0 , 0.0 , -1.0 ) ) ,
			/* Test #13 */ new Test( Ymin1 , origin , FRONT_VIEW  .plus( 0.0 , 0.0 , -1.0 ) ) ,
			/* Test #14 */ new Test( Y1    , origin , BACK_VIEW   .plus( 0.0 , 0.0 , -1.0 ) ) ,
			/* Test #15 */ new Test( Zmin1 , origin , BOTTOM_VIEW .plus( 0.0 , 0.0 , -1.0 ) ) ,
			/* Test #16 */ new Test( Z1    , origin , TOP_VIEW    .plus( 0.0 , 0.0 , -1.0 ) ) ,
		};

		/*
		 * Define tests for diagonal views.
		 */
		// final Vector3D X1Y0Z0 = v0.set(  1.0 , -1.0 , -1.0 );
		// final Vector3D X0Y1Z0 = v0.set( -1.0 ,  1.0 , -1.0 );
		// final Vector3D X1Y1Z0 = v0.set(  1.0 ,  1.0 , -1.0 );
		// final Vector3D X0Y0Z1 = v0.set( -1.0 , -1.0 ,  1.0 );
		// final Vector3D X1Y0Z1 = v0.set(  1.0 , -1.0 ,  1.0 );
		// final Vector3D X0Y1Z1 = v0.set( -1.0 ,  1.0 ,  1.0 );
		// final Vector3D X1Y1Z1 = v0.set(  1.0 ,  1.0 ,  1.0 );

		final double DEG45  = Math.PI / 4.0;
		final double DEG90  = Math.PI / 2.0;
		final double DEG135 = DEG90 + DEG45;
		final double SQRT2  = Math.sqrt( 2.0 );
		final double SQRT3  = Math.sqrt( 3.0 );

		final double DEG_125_2 = Math.toRadians( 125.2 );
		final Test[] diagonalTests =
		{
			/* Test #17 */ new Test( v0.set(  0.0 , -1.0 , -1.0 ) , origin , m0.rotateX( DEG135 )                     .plus( 0.0 , 0.0 , -SQRT2 ) ) ,
			/* Test #18 */ new Test( v0.set( -1.0 ,  0.0 ,  1.0 ) , origin , m0.rotateY(  DEG45 ).rotateZ(     DEG90 ).plus( 0.0 , 0.0 , -SQRT2 ) ) ,
			/* Test #19 */ new Test( v0.set(  1.0 ,  0.0 ,  1.0 ) , origin , m0.rotateY( -DEG45 ).rotateZ(    -DEG90 ).plus( 0.0 , 0.0 , -SQRT2 ) ) ,
			/* Test #20 */ new Test( v0.set(  1.0 ,  0.0 ,  0.0 ) , origin , m0.rotateZ( -DEG90 ).rotateX(     DEG90 ).plus( 0.0 , 0.0 , -1.0   ) ) ,
			/* Test #21 */ new Test( v0.set( -1.0 , -1.0 , -1.0 ) , origin , m0.rotateZ(  DEG45 ).rotateX( DEG_125_2 ).plus( 0.0 , 0.0 , -SQRT3 ) ) ,
		};

		/*
		 * Execute tests.
		 */
		final Test[][] allTests = { extremeTests , orthogonalTests , diagonalTests };
		int testNr = 1;
		for ( int i = 0 ; i < allTests.length ; i++ )
		{
			final Test[] tests = allTests[ i ];
			for ( int j = 0 ; j < tests.length ; j++ )
			{
				final Test   test        = tests[ j ];
				final String description = "Test #" + testNr++;

				final Class expectedException = ( test.out instanceof Class ) ? (Class)test.out : null;
				try
				{
					final Vector3D upPrimary   = Vector3D.INIT.set( 0.0 , 0.0 , 1.0 );
					final Vector3D upSecondary = Vector3D.INIT.set( 0.0 , 1.0 , 0.0 );
					final Matrix3D actual      = Matrix3D.getFromToTransform( test.from , test.to , upPrimary , upSecondary );

					if ( expectedException != null )
						fail( description + " should have thrown exception" );

					final Matrix3D expected = (Matrix3D)test.out;

					assertTrue( description + "\nExpected:" + expected.toFriendlyString() + "\nActual:" + actual.toFriendlyString() , expected.almostEquals( actual ) );
				}
				catch ( Exception e )
				{
					if ( expectedException == null )
					{
						System.err.println( description + " threw unexpected exception: " + e );
						throw e;
					}

					assertEquals( description + " threw wrong exception" , expectedException , e.getClass() );
				}
			}
		}
	}

	/**
	 * Test the <code>getRotationTransform()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Matrix3D#getRotationTransform
	 */
	public void testGetRotationTransform()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetRotationTransform()" );

		/*
		 * Define test properties.
		 */
		class Test
		{
			final Vector3D pivot;
			final Vector3D direction;
			final double   thetaRad;
			final Object   result;

			private Test( final Vector3D pivot , final Vector3D direction , final double thetaRad , final Matrix3D result )
			{
				this.pivot     = pivot;
				this.direction = direction;
				this.thetaRad  = thetaRad;
				this.result    = result;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Vector3D v0    = Vector3D.INIT;
		final Matrix3D ident = Matrix3D.INIT;

		final Test[] tests =
		{
			/* Test #1 */ new Test( v0 , v0.set( 1.0 , 0.0 , 0.0 ) , 0.0 , ident ),
			/* Test #2 */ new Test( v0 , v0.set( 1.0 , 0.0 , 0.0 ) , 1.0 , ident.rotateX( 1.0 ) ),
			/* Test #3 */ new Test( v0 , v0.set( 0.0 , 1.0 , 0.0 ) , 0.0 , ident ),
			/* Test #4 */ new Test( v0 , v0.set( 0.0 , 1.0 , 0.0 ) , 1.0 , ident.rotateY( 1.0 ) ),
			/* Test #5 */ new Test( v0 , v0.set( 0.0 , 0.0 , 1.0 ) , 0.0 , ident ),
			/* Test #6 */ new Test( v0 , v0.set( 0.0 , 0.0 , 1.0 ) , 1.0 , ident.rotateZ( 1.0 ) ),
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0 ; i < tests.length ; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final Class expectedException = ( test.result instanceof Class ) ? (Class)test.result : null;

			try
			{
				if ( expectedException != null )
					fail( description + " should have thrown exception" );

				final Matrix3D expected = (Matrix3D)test.result;
				final Matrix3D actual = Matrix3D.getRotationTransform( test.pivot , test.direction , test.thetaRad );

				assertEquals( description + "\nExpected:" + expected.toFriendlyString() + "\nActual:" + actual.toFriendlyString() + '\n' , expected , actual );
			}
			catch ( Exception e )
			{
				if ( expectedException == null )
				{
					System.err.println( description + " threw unexpected exception: " + e );
					throw e;
				}

				assertEquals( description + " threw wrong exception" , expectedException.getName() , e.getClass() );
			}
		}
	}
}
