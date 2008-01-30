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
 * @version $Revision$ $Date$
 */
public class TestMatrix3D
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestMatrix3D.class.getName();

	public static final Matrix3D INIT = Matrix3D.INIT;

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
		Matrix3D m;

		/*
		 * INIT must be identity matrix
		 */
		assertEquals( "Matrix3D.equals() returned 'false' where it should have returned 'true'"
		            , INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 ) , INIT );

		/*
		 * Test if each component is correctly tested by equals()
		 */
		m = INIT.set( 9.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xx'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 9.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xy'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 9.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xz'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 0.0 , 9.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xo'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 9.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yx'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 9.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yy'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 9.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yz'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 9.0 , 0.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yo'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 9.0 , 0.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zx'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 9.0 , 1.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zy'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 9.0 , 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zz'" , !INIT.equals( m ) && !m.equals( INIT ) );

		m = INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 9.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zo'" , !INIT.equals( m ) && !m.equals( INIT ) );
	}

	/**
	 * Test the <code>getFromToTransform()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Matrix3D#getFromToTransform
	 */
	public void testGetFromToTransform()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetFromToTransform()" );

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

		final Matrix3D LEFT_VIEW = INIT.set(
			 0.0 , -1.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 ,  1.0 , 0.0 ,
			-1.0 ,  0.0 ,  0.0 , 0.0 );

		final Matrix3D RIGHT_VIEW = INIT.set(
			 0.0 ,  1.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 ,  1.0 , 0.0 ,
			 1.0 ,  0.0 ,  0.0 , 0.0 );

		final Matrix3D FRONT_VIEW = INIT.set(
			 1.0 ,  0.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 ,  1.0 , 0.0 ,
			 0.0 , -1.0 ,  0.0 , 0.0 );

		final Matrix3D BACK_VIEW = INIT.set(
			-1.0 ,  0.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 ,  1.0 , 0.0 ,
			 0.0 ,  1.0 ,  0.0 , 0.0 );

		final Matrix3D BOTTOM_VIEW = INIT.set(
			-1.0 ,  0.0 ,  0.0 , 0.0 ,
			 0.0 ,  1.0 ,  0.0 , 0.0 ,
			 0.0 ,  0.0 , -1.0 , 0.0 );

		final Matrix3D TOP_VIEW = INIT.set(
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
			/* Test #17 */ new Test( v0.set(  0.0 , -1.0 , -1.0 ) , origin , INIT.rotateX( -DEG135 )                      .plus( 0.0 , 0.0 , -SQRT2 ) ) ,
			/* Test #18 */ new Test( v0.set( -1.0 ,  0.0 ,  1.0 ) , origin , INIT.rotateY(   DEG45 ).rotateZ(      DEG90 ).plus( 0.0 , 0.0 , -SQRT2 ) ) ,
			/* Test #19 */ new Test( v0.set(  1.0 ,  0.0 ,  1.0 ) , origin , INIT.rotateY(  -DEG45 ).rotateZ(     -DEG90 ).plus( 0.0 , 0.0 , -SQRT2 ) ) ,
			/* Test #20 */ new Test( v0.set(  1.0 ,  0.0 ,  0.0 ) , origin , INIT.rotateZ(  -DEG90 ).rotateX(     -DEG90 ).plus( 0.0 , 0.0 , -1.0   ) ) ,
			/* Test #21 */ new Test( v0.set( -1.0 , -1.0 , -1.0 ) , origin , INIT.rotateZ(   DEG45 ).rotateX( -DEG_125_2 ).plus( 0.0 , 0.0 , -SQRT3 ) ) ,
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
		final Vector3D v0 = Vector3D.INIT;

		final Test[] tests =
		{
			/* Test #1 */ new Test( v0 , v0.set( 1.0 , 0.0 , 0.0 ) , 0.0 , INIT ),
			/* Test #2 */ new Test( v0 , v0.set( 1.0 , 0.0 , 0.0 ) , 1.0 , INIT.rotateX( 1.0 ) ),
			/* Test #3 */ new Test( v0 , v0.set( 0.0 , 1.0 , 0.0 ) , 0.0 , INIT ),
			/* Test #4 */ new Test( v0 , v0.set( 0.0 , 1.0 , 0.0 ) , 1.0 , INIT.rotateY( 1.0 ) ),
			/* Test #5 */ new Test( v0 , v0.set( 0.0 , 0.0 , 1.0 ) , 0.0 , INIT ),
			/* Test #6 */ new Test( v0 , v0.set( 0.0 , 0.0 , 1.0 ) , 1.0 , INIT.rotateZ( 1.0 ) ),
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

	/**
	 * Test the <code>rotateX()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Matrix3D#rotateX
	 */
	public void testRotateX()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testRotateX()" );

		/*
		 * Test matrix contents for simple rotations.
		 */
		for ( double angle = 0.0 ; angle < 360.0 ; angle += 15.0 )
		{
			final double rad = Math.toRadians( angle );
			final double cos = Math.cos( rad );
			final double sin = Math.sin( rad );

			final Matrix3D matrix = INIT.rotateX( rad );

			final String where = "Rotate X over " + angle + " degrees";

			assertEquals( where + " - xx" ,  1.0 , matrix.xx , 0.0 );
			assertEquals( where + " - xy" ,  0.0 , matrix.xy , 0.0 );
			assertEquals( where + " - xz" ,  0.0 , matrix.xz , 0.0 );
			assertEquals( where + " - xo" ,  0.0 , matrix.xo , 0.0 );
			assertEquals( where + " - yx" ,  0.0 , matrix.yx , 0.0 );
			assertEquals( where + " - yy" ,  cos , matrix.yy , 0.0 );
			assertEquals( where + " - yz" , -sin , matrix.yz , 0.0 );
			assertEquals( where + " - yo" ,  0.0 , matrix.yo , 0.0 );
			assertEquals( where + " - zx" ,  0.0 , matrix.zx , 0.0 );
			assertEquals( where + " - zy" ,  sin , matrix.zy , 0.0 );
			assertEquals( where + " - zz" ,  cos , matrix.zz , 0.0 );
			assertEquals( where + " - zo" ,  0.0 , matrix.zo , 0.0 );
		}

		/*
		 * Test vector rotation.
		 */
		final double[][][] vectorTests =
		{
			{ { 90 } , { 1.0 , 0.0 , 0.0 } , {  1.0 ,  0.0 ,  0.0 } } ,
			{ { 90 } , { 0.0 , 1.0 , 0.0 } , {  0.0 ,  0.0 ,  1.0 } } ,
			{ { 90 } , { 0.0 , 0.0 , 1.0 } , {  0.0 , -1.0 ,  0.0 } } ,
		};

		for ( int i = 0 ; i < vectorTests.length ; i++ )
		{
			final double[][] test     = vectorTests[ i ];
			final double     deg      = test[ 0 ][ 0 ];
			final Vector3D   vector   = Vector3D.INIT.set( test[ 1 ][ 0 ] , test[ 1 ][ 1 ] , test[ 1 ][ 2 ] );
			final Vector3D   expected = Vector3D.INIT.set( test[ 2 ][ 0 ] , test[ 2 ][ 1 ] , test[ 2 ][ 2 ] );

			final Matrix3D matrix = INIT.rotateX( Math.toRadians( deg ) );
			final Vector3D actual = matrix.multiply( vector );

			if ( !expected.almostEquals( actual ) )
			{
				fail( "Rotate vector " + vector.toFriendlyString() + ' ' + deg + " degrees over X-axis failed -"
				    + " expected:" + expected.toFriendlyString()
				    + " but was:" + actual.toFriendlyString() );
			}
		}
	}

	/**
	 * Test the <code>rotateY()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Matrix3D#rotateY
	 */
	public void testRotateY()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testRotateY()" );

		/*
		 * Test matrix contents for simple rotations.
		 */
		for ( double angle = 0.0 ; angle < 360.0 ; angle += 15.0 )
		{
			final double rad = Math.toRadians( angle );
			final double cos = Math.cos( rad );
			final double sin = Math.sin( rad );

			final Matrix3D matrix = INIT.rotateY( rad );

			final String where = "Rotate Y over " + angle + " degrees";

			assertEquals( where + " - xx" ,  cos , matrix.xx , 0.0 );
			assertEquals( where + " - xy" ,  0.0 , matrix.xy , 0.0 );
			assertEquals( where + " - xz" ,  sin , matrix.xz , 0.0 );
			assertEquals( where + " - xo" ,  0.0 , matrix.xo , 0.0 );
			assertEquals( where + " - yx" ,  0.0 , matrix.yx , 0.0 );
			assertEquals( where + " - yy" ,  1.0 , matrix.yy , 0.0 );
			assertEquals( where + " - yz" ,  0.0 , matrix.yz , 0.0 );
			assertEquals( where + " - yo" ,  0.0 , matrix.yo , 0.0 );
			assertEquals( where + " - zx" , -sin , matrix.zx , 0.0 );
			assertEquals( where + " - zy" ,  0.0 , matrix.zy , 0.0 );
			assertEquals( where + " - zz" ,  cos , matrix.zz , 0.0 );
			assertEquals( where + " - zo" ,  0.0 , matrix.zo , 0.0 );
		}

		/*
		 * Test vector rotation.
		 */
		final double[][][] vectorTests =
		{
			{ { 90 } , { 1.0 , 0.0 , 0.0 } , {  0.0 ,  0.0 , -1.0 } } ,
			{ { 90 } , { 0.0 , 1.0 , 0.0 } , {  0.0 ,  1.0 ,  0.0 } } ,
			{ { 90 } , { 0.0 , 0.0 , 1.0 } , {  1.0 ,  0.0 ,  0.0 } } ,
		};

		for ( int i = 0 ; i < vectorTests.length ; i++ )
		{
			final double[][] test     = vectorTests[ i ];
			final double     deg      = test[ 0 ][ 0 ];
			final Vector3D   vector   = Vector3D.INIT.set( test[ 1 ][ 0 ] , test[ 1 ][ 1 ] , test[ 1 ][ 2 ] );
			final Vector3D   expected = Vector3D.INIT.set( test[ 2 ][ 0 ] , test[ 2 ][ 1 ] , test[ 2 ][ 2 ] );

			final Matrix3D matrix = INIT.rotateY( Math.toRadians( deg ) );
			final Vector3D actual = matrix.multiply( vector );

			if ( !expected.almostEquals( actual ) )
			{
				fail( "Rotate vector " + vector.toFriendlyString() + ' ' + deg + " degrees over Y-axis failed -"
				    + " expected:" + expected.toFriendlyString()
				    + " but was:" + actual.toFriendlyString() );
			}
		}
	}

	/**
	 * Test the <code>rotateZ()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Matrix3D#rotateZ
	 */
	public void testRotateZ()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testRotateZ()" );

		/*
		 * Test matrix contents for simple rotations.
		 */
		for ( double angle = 0.0 ; angle < 360.0 ; angle += 15.0 )
		{
			final double rad = Math.toRadians( angle );
			final double cos = Math.cos( rad );
			final double sin = Math.sin( rad );

			final Matrix3D matrix = INIT.rotateZ( rad );

			final String where = "Rotate Z over " + angle + " degrees";

			assertEquals( where + " - xx" ,  cos , matrix.xx , 0.0 );
			assertEquals( where + " - xy" , -sin , matrix.xy , 0.0 );
			assertEquals( where + " - xz" ,  0.0 , matrix.xz , 0.0 );
			assertEquals( where + " - xo" ,  0.0 , matrix.xo , 0.0 );
			assertEquals( where + " - yx" ,  sin , matrix.yx , 0.0 );
			assertEquals( where + " - yy" ,  cos , matrix.yy , 0.0 );
			assertEquals( where + " - yz" ,  0.0 , matrix.yz , 0.0 );
			assertEquals( where + " - yo" ,  0.0 , matrix.yo , 0.0 );
			assertEquals( where + " - zx" ,  0.0 , matrix.zx , 0.0 );
			assertEquals( where + " - zy" ,  0.0 , matrix.zy , 0.0 );
			assertEquals( where + " - zz" ,  1.0 , matrix.zz , 0.0 );
			assertEquals( where + " - zo" ,  0.0 , matrix.zo , 0.0 );
		}

		/*
		 * Test vector rotation.
		 */
		final double[][][] vectorTests =
		{
			{ { 90 } , { 1.0 , 0.0 , 0.0 } , {  0.0 ,  1.0 ,  0.0 } } ,
			{ { 90 } , { 0.0 , 1.0 , 0.0 } , { -1.0 ,  0.0 ,  0.0 } } ,
			{ { 90 } , { 0.0 , 0.0 , 1.0 } , {  0.0 ,  0.0 ,  1.0 } } ,
		};

		for ( int i = 0 ; i < vectorTests.length ; i++ )
		{
			final double[][] test     = vectorTests[ i ];
			final double     deg      = test[ 0 ][ 0 ];
			final Vector3D   vector   = Vector3D.INIT.set( test[ 1 ][ 0 ] , test[ 1 ][ 1 ] , test[ 1 ][ 2 ] );
			final Vector3D   expected = Vector3D.INIT.set( test[ 2 ][ 0 ] , test[ 2 ][ 1 ] , test[ 2 ][ 2 ] );

			final Matrix3D matrix = INIT.rotateZ( Math.toRadians( deg ) );
			final Vector3D actual = matrix.multiply( vector );

			if ( !expected.almostEquals( actual ) )
			{
				fail( "Rotate vector " + vector.toFriendlyString() + ' ' + deg + " degrees over Z-axis failed -"
				    + " expected:" + expected.toFriendlyString()
				    + " but was:" + actual.toFriendlyString() );
			}
		}
	}
}
