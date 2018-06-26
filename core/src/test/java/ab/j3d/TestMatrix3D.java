/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2018 Peter S. Heijnen
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

import ab.j3d.junit.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This tests the {@link Matrix3D} class.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "JavaDoc", "FieldNamingConvention" } )
public class TestMatrix3D
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestMatrix3D.class.getName();

	/**
	 * Test {@link Matrix3D#equals} method.
	 *
	 * RELATED BUGS (SOLVED): <ul> <li> <b>BUG:</b><br /> OBJ files contain bad
	 * geometric data. <br /> <b>Symptom:</b><br /> The geometry for a 2nd
	 * scenario contains negative Y coordinates. This seems to occur with almost
	 * every panel. <br /> <b>Analysis:</b><br /> Matrix3D.equals() method did
	 * not compare the translation correctly (comparing this.xo to
	 * other.xo/yo/zo instead of this.xo/yo/zo). Incredible how this bug has
	 * never been spotted before. <br /> <b>Fix:</b><br /> Fixed xo/yo/zo test
	 * in Matrix3D.equals() method. </li> </ul>
	 */
	@Test
	public void testEquals()
	{
		System.out.println( CLASS_NAME + ".testEquals()" );
		Matrix3D matrix;

		/*
		 * INIT must be identity matrix
		 */
		assertEquals( "Matrix3D.equals() identity failed", Matrix3D.IDENTITY, new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 ) );

		/*
		 * Test if each component is correctly tested by equals()
		 */
		matrix = new Matrix3D( 9.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xx'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 9.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xy'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 9.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xz'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 0.0, 9.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xo'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 9.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yx'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 9.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yy'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 9.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yz'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 9.0, 0.0, 0.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yo'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 9.0, 0.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zx'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 9.0, 1.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zy'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 9.0, 0.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zz'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );

		matrix = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 9.0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zo'", !Matrix3D.IDENTITY.equals( matrix ) && !matrix.equals( Matrix3D.IDENTITY ) );
	}

	/**
	 * Test the {@link Matrix3D#getFromToTransform} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGetFromToTransform()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetFromToTransform()" );

		@SuppressWarnings( { "JavaDoc", "InstanceVariableNamingConvention" } )
		class Test
		{
			final Vector3D from;

			final Vector3D to;

			final Object out;

			private Test( final Vector3D from, final Vector3D to, final Object out )
			{
				this.from = from;
				this.to = to;
				this.out = out;
			}
		}

		/*
		 * Define 'extreme' tests.
		 */

		final Test[] extremeTests =
		{
		/* Test #1  */ new Test( null, null, NullPointerException.class ),
		/* Test #2  */ new Test( null, Vector3D.ZERO, NullPointerException.class ),
		/* Test #3  */ new Test( Vector3D.ZERO, null, NullPointerException.class ),
		/* Test #4  */ new Test( Vector3D.ZERO, Vector3D.ZERO, IllegalArgumentException.class ),
		};

		/*
		 * Define tests for orthogonal views.
		 */
		final Matrix3D leftView = new Matrix3D(
		0.0, -1.0, 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0,
		-1.0, 0.0, 0.0, 0.0 );

		final Matrix3D rightView = new Matrix3D(
		0.0, 1.0, 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0,
		1.0, 0.0, 0.0, 0.0 );

		final Matrix3D frontView = new Matrix3D(
		1.0, 0.0, 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0,
		0.0, -1.0, 0.0, 0.0 );

		final Matrix3D rearView = new Matrix3D(
		-1.0, 0.0, 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0,
		0.0, 1.0, 0.0, 0.0 );

		final Matrix3D bottomView = new Matrix3D(
		-1.0, 0.0, 0.0, 0.0,
		0.0, 1.0, 0.0, 0.0,
		0.0, 0.0, -1.0, 0.0 );

		final Matrix3D topView = new Matrix3D(
		1.0, 0.0, 0.0, 0.0,
		0.0, 1.0, 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0 );

		final Test[] orthogonalTests =
		{
		/* Test #5  */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_X_AXIS, leftView ),
		/* Test #6  */ new Test( Vector3D.ZERO, Vector3D.NEGATIVE_X_AXIS, rightView ),
		/* Test #7  */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Y_AXIS, frontView ),
		/* Test #8  */ new Test( Vector3D.ZERO, Vector3D.NEGATIVE_Y_AXIS, rearView ),
		/* Test #9  */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Z_AXIS, bottomView ),
		/* Test #10 */ new Test( Vector3D.ZERO, Vector3D.NEGATIVE_Z_AXIS, topView ),

		/* Test #11 */ new Test( Vector3D.NEGATIVE_X_AXIS, Vector3D.ZERO, leftView.plus( 0.0, 0.0, -1.0 ) ),
		/* Test #12 */ new Test( Vector3D.POSITIVE_X_AXIS, Vector3D.ZERO, rightView.plus( 0.0, 0.0, -1.0 ) ),
		/* Test #13 */ new Test( Vector3D.NEGATIVE_Y_AXIS, Vector3D.ZERO, frontView.plus( 0.0, 0.0, -1.0 ) ),
		/* Test #14 */ new Test( Vector3D.POSITIVE_Y_AXIS, Vector3D.ZERO, rearView.plus( 0.0, 0.0, -1.0 ) ),
		/* Test #15 */ new Test( Vector3D.NEGATIVE_Z_AXIS, Vector3D.ZERO, bottomView.plus( 0.0, 0.0, -1.0 ) ),
		/* Test #16 */ new Test( Vector3D.POSITIVE_Z_AXIS, Vector3D.ZERO, topView.plus( 0.0, 0.0, -1.0 ) ),
		};

		/*
		 * Define tests for diagonal views.
		 */
		// final Vector3D X1Y0Z0 = new Vector3D(  1.0, -1.0, -1.0 );
		// final Vector3D X0Y1Z0 = new Vector3D( -1.0,  1.0, -1.0 );
		// final Vector3D X1Y1Z0 = new Vector3D(  1.0,  1.0, -1.0 );
		// final Vector3D X0Y0Z1 = new Vector3D( -1.0, -1.0,  1.0 );
		// final Vector3D X1Y0Z1 = new Vector3D(  1.0, -1.0,  1.0 );
		// final Vector3D X0Y1Z1 = new Vector3D( -1.0,  1.0,  1.0 );
		// final Vector3D X1Y1Z1 = new Vector3D(  1.0,  1.0,  1.0 );

		final double deg45 = Math.PI / 4.0;
		final double deg90 = Math.PI / 2.0;
		final double deg135 = deg90 + deg45;
		final double sqrt2 = Math.sqrt( 2.0 );
		final double sqrt3 = Math.sqrt( 3.0 );

		final double deg125_2 = deg90 + Math.atan( 1.0 / sqrt2 );

		final Test[] diagonalTests =
		{
		/* Test #17 */ new Test( new Vector3D( 0.0, -1.0, -1.0 ), Vector3D.ZERO, Matrix3D.IDENTITY.rotateX( -deg135 ).plus( 0.0, 0.0, -sqrt2 ) ),
		/* Test #18 */ new Test( new Vector3D( -1.0, 0.0, 1.0 ), Vector3D.ZERO, Matrix3D.IDENTITY.rotateY( deg45 ).rotateZ( deg90 ).plus( 0.0, 0.0, -sqrt2 ) ),
		/* Test #19 */ new Test( new Vector3D( 1.0, 0.0, 1.0 ), Vector3D.ZERO, Matrix3D.IDENTITY.rotateY( -deg45 ).rotateZ( -deg90 ).plus( 0.0, 0.0, -sqrt2 ) ),
		/* Test #20 */ new Test( Vector3D.POSITIVE_X_AXIS, Vector3D.ZERO, Matrix3D.IDENTITY.rotateZ( -deg90 ).rotateX( -deg90 ).plus( 0.0, 0.0, -1.0 ) ),
		/* Test #21 */ new Test( new Vector3D( -1.0, -1.0, -1.0 ), Vector3D.ZERO, Matrix3D.IDENTITY.rotateZ( deg45 ).rotateX( -deg125_2 ).plus( 0.0, 0.0, -sqrt3 ) ),
		};

		/*
		 * Execute tests.
		 */
		final Test[][] allTests = { extremeTests, orthogonalTests, diagonalTests };
		int testNr = 1;
		for ( final Test[] tests : allTests )
		{
			for ( final Test test : tests )
			{
				final String description = "Test #" + testNr++;

				final Class<?> expectedException = ( test.out instanceof Class ) ? (Class<?>)test.out : null;
				try
				{
					final Vector3D upPrimary = Vector3D.POSITIVE_Z_AXIS;
					final Vector3D upSecondary = Vector3D.POSITIVE_Y_AXIS;
					final Matrix3D actual = Matrix3D.getFromToTransform( test.from, test.to, upPrimary, upSecondary );

					if ( expectedException != null )
					{
						fail( description + " should have thrown exception" );
					}

					final Matrix3D expected = (Matrix3D)test.out;

					assertTrue( description + "\nExpected:" + expected/*.toFriendlyString()*/ + "\nActual:" + actual/*.toFriendlyString()*/, expected.almostEquals( actual ) );
				}
				catch ( final Exception e )
				{
					if ( expectedException == null )
					{
						System.err.println( description + " threw unexpected exception: " + e );
						throw e;
					}

					if ( !expectedException.equals( e.getClass() ) )
					{
						e.printStackTrace();
					}

					assertEquals( description + " threw wrong exception", expectedException, e.getClass() );
				}
			}
		}
	}

	/**
	 * Test the {@link Matrix3D#getRotationTransform} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGetRotationTransform()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetRotationTransform()" );

		class Test
		{
			final Vector3D pivot;

			final Vector3D direction;

			final double thetaRad;

			final Object result;

			private Test( final Vector3D pivot, final Vector3D direction, final double thetaRad, final Matrix3D result )
			{
				this.pivot = pivot;
				this.direction = direction;
				this.thetaRad = thetaRad;
				this.result = result;
			}
		}

		/*
		 * Define tests to execute.
		 */

		final Test[] tests =
		{
		/* Test #1 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_X_AXIS, 0.0, Matrix3D.IDENTITY ),
		/* Test #2 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_X_AXIS, 1.0, Matrix3D.IDENTITY.rotateX( 1.0 ) ),
		/* Test #3 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Y_AXIS, 0.0, Matrix3D.IDENTITY ),
		/* Test #4 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Y_AXIS, 1.0, Matrix3D.IDENTITY.rotateY( 1.0 ) ),
		/* Test #5 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Z_AXIS, 0.0, Matrix3D.IDENTITY ),
		/* Test #6 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Z_AXIS, 1.0, Matrix3D.IDENTITY.rotateZ( 1.0 ) ),
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final Class<?> expectedException = ( test.result instanceof Class ) ? (Class<?>)test.result : null;

			try
			{
				final Matrix3D actual = Matrix3D.getRotationTransform( test.pivot, test.direction, test.thetaRad );

				if ( expectedException != null )
				{
					fail( description + " should have thrown exception" );
				}

				final Matrix3D expected = (Matrix3D)test.result;

				assertEquals( description + "\nExpected:" + expected.toFriendlyString() + "\nActual:" + actual.toFriendlyString() + '\n', expected, actual );
			}
			catch ( final Exception e )
			{
				if ( expectedException == null )
				{
					System.err.println( description + " threw unexpected exception: " + e );
					throw e;
				}

				if ( !expectedException.equals( e.getClass() ) )
				{
					e.printStackTrace();
				}

				assertEquals( description + " threw wrong exception", expectedException, e.getClass() );
			}
		}
	}

	/**
	 * Test the {@link Matrix3D#rotateX} method.
	 */
	@Test
	public void testRotateX()
	{
		System.out.println( CLASS_NAME + ".testRotateX()" );

		/*
		 * Test matrix contents for simple rotations.
		 */
		for ( double angle = 0.0; angle < 360.0; angle += 15.0 )
		{
			final double rad = Math.toRadians( angle );
			final double cos = Math.cos( rad );
			final double sin = Math.sin( rad );

			final Matrix3D matrix = Matrix3D.IDENTITY.rotateX( rad );

			final String where = "Rotate X over " + angle + " degrees";

			assertEquals( where + " - xx", 1.0, matrix.xx, 0.0 );
			assertEquals( where + " - xy", 0.0, matrix.xy, 0.0 );
			assertEquals( where + " - xz", 0.0, matrix.xz, 0.0 );
			assertEquals( where + " - xo", 0.0, matrix.xo, 0.0 );
			assertEquals( where + " - yx", 0.0, matrix.yx, 0.0 );
			assertEquals( where + " - yy", cos, matrix.yy, 0.0 );
			assertEquals( where + " - yz", -sin, matrix.yz, 0.0 );
			assertEquals( where + " - yo", 0.0, matrix.yo, 0.0 );
			assertEquals( where + " - zx", 0.0, matrix.zx, 0.0 );
			assertEquals( where + " - zy", sin, matrix.zy, 0.0 );
			assertEquals( where + " - zz", cos, matrix.zz, 0.0 );
			assertEquals( where + " - zo", 0.0, matrix.zo, 0.0 );
		}

		/*
		 * Test vector rotation.
		 */
		final double[][][] vectorTests =
		{
		{ { 90.0 }, { 1.0, 0.0, 0.0 }, { 1.0, 0.0, 0.0 } },
		{ { 90.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 } },
		{ { 90.0 }, { 0.0, 0.0, 1.0 }, { 0.0, -1.0, 0.0 } },
		};

		for ( final double[][] test : vectorTests )
		{
			final double deg = test[ 0 ][ 0 ];
			final Vector3D vector = new Vector3D( test[ 1 ][ 0 ], test[ 1 ][ 1 ], test[ 1 ][ 2 ] );
			final Vector3D expected = new Vector3D( test[ 2 ][ 0 ], test[ 2 ][ 1 ], test[ 2 ][ 2 ] );

			final Matrix3D matrix = Matrix3D.IDENTITY.rotateX( Math.toRadians( deg ) );
			final Vector3D actual = matrix.transform( vector );

			if ( !expected.almostEquals( actual ) )
			{
				fail( "Rotate vector " + vector.toFriendlyString() + ' ' + deg + " degrees over X-axis failed -"
				      + " expected:" + expected.toFriendlyString()
				      + " but was:" + actual.toFriendlyString() );
			}
		}
	}

	/**
	 * Test the {@link Matrix3D#rotateY} method.
	 */
	@Test
	public void testRotateY()
	{
		System.out.println( CLASS_NAME + ".testRotateY()" );

		/*
		 * Test matrix contents for simple rotations.
		 */
		for ( double angle = 0.0; angle < 360.0; angle += 15.0 )
		{
			final double rad = Math.toRadians( angle );
			final double cos = Math.cos( rad );
			final double sin = Math.sin( rad );

			final Matrix3D matrix = Matrix3D.IDENTITY.rotateY( rad );

			final String where = "Rotate Y over " + angle + " degrees";

			assertEquals( where + " - xx", cos, matrix.xx, 0.0 );
			assertEquals( where + " - xy", 0.0, matrix.xy, 0.0 );
			assertEquals( where + " - xz", sin, matrix.xz, 0.0 );
			assertEquals( where + " - xo", 0.0, matrix.xo, 0.0 );
			assertEquals( where + " - yx", 0.0, matrix.yx, 0.0 );
			assertEquals( where + " - yy", 1.0, matrix.yy, 0.0 );
			assertEquals( where + " - yz", 0.0, matrix.yz, 0.0 );
			assertEquals( where + " - yo", 0.0, matrix.yo, 0.0 );
			assertEquals( where + " - zx", -sin, matrix.zx, 0.0 );
			assertEquals( where + " - zy", 0.0, matrix.zy, 0.0 );
			assertEquals( where + " - zz", cos, matrix.zz, 0.0 );
			assertEquals( where + " - zo", 0.0, matrix.zo, 0.0 );
		}

		/*
		 * Test vector rotation.
		 */
		final double[][][] vectorTests =
		{
		{ { 90.0 }, { 1.0, 0.0, 0.0 }, { 0.0, 0.0, -1.0 } },
		{ { 90.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 1.0, 0.0 } },
		{ { 90.0 }, { 0.0, 0.0, 1.0 }, { 1.0, 0.0, 0.0 } },
		};

		for ( final double[][] test : vectorTests )
		{
			final double deg = test[ 0 ][ 0 ];
			final Vector3D vector = new Vector3D( test[ 1 ][ 0 ], test[ 1 ][ 1 ], test[ 1 ][ 2 ] );
			final Vector3D expected = new Vector3D( test[ 2 ][ 0 ], test[ 2 ][ 1 ], test[ 2 ][ 2 ] );

			final Matrix3D matrix = Matrix3D.IDENTITY.rotateY( Math.toRadians( deg ) );
			final Vector3D actual = matrix.transform( vector );

			if ( !expected.almostEquals( actual ) )
			{
				fail( "Rotate vector " + vector.toFriendlyString() + ' ' + deg + " degrees over Y-axis failed -"
				      + " expected:" + expected.toFriendlyString()
				      + " but was:" + actual.toFriendlyString() );
			}
		}
	}

	/**
	 * Test the {@link Matrix3D#rotateZ} method.
	 */
	@Test
	public void testRotateZ()
	{
		System.out.println( CLASS_NAME + ".testRotateZ()" );

		/*
		 * Test matrix contents for simple rotations.
		 */
		for ( double angle = 0.0; angle < 360.0; angle += 15.0 )
		{
			final double rad = Math.toRadians( angle );
			final double cos = Math.cos( rad );
			final double sin = Math.sin( rad );

			final Matrix3D matrix = Matrix3D.IDENTITY.rotateZ( rad );

			final String where = "Rotate Z over " + angle + " degrees";

			assertEquals( where + " - xx", cos, matrix.xx, 0.0 );
			assertEquals( where + " - xy", -sin, matrix.xy, 0.0 );
			assertEquals( where + " - xz", 0.0, matrix.xz, 0.0 );
			assertEquals( where + " - xo", 0.0, matrix.xo, 0.0 );
			assertEquals( where + " - yx", sin, matrix.yx, 0.0 );
			assertEquals( where + " - yy", cos, matrix.yy, 0.0 );
			assertEquals( where + " - yz", 0.0, matrix.yz, 0.0 );
			assertEquals( where + " - yo", 0.0, matrix.yo, 0.0 );
			assertEquals( where + " - zx", 0.0, matrix.zx, 0.0 );
			assertEquals( where + " - zy", 0.0, matrix.zy, 0.0 );
			assertEquals( where + " - zz", 1.0, matrix.zz, 0.0 );
			assertEquals( where + " - zo", 0.0, matrix.zo, 0.0 );
		}

		/*
		 * Test vector rotation.
		 */
		final double[][][] vectorTests =
		{
		{ { 90.0 }, { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 } },
		{ { 90.0 }, { 0.0, 1.0, 0.0 }, { -1.0, 0.0, 0.0 } },
		{ { 90.0 }, { 0.0, 0.0, 1.0 }, { 0.0, 0.0, 1.0 } },
		};

		for ( final double[][] test : vectorTests )
		{
			final double deg = test[ 0 ][ 0 ];
			final Vector3D vector = new Vector3D( test[ 1 ][ 0 ], test[ 1 ][ 1 ], test[ 1 ][ 2 ] );
			final Vector3D expected = new Vector3D( test[ 2 ][ 0 ], test[ 2 ][ 1 ], test[ 2 ][ 2 ] );

			final Matrix3D matrix = Matrix3D.IDENTITY.rotateZ( Math.toRadians( deg ) );
			final Vector3D actual = matrix.transform( vector );

			if ( !expected.almostEquals( actual ) )
			{
				fail( "Rotate vector " + vector.toFriendlyString() + ' ' + deg + " degrees over Z-axis failed -"
				      + " expected:" + expected.toFriendlyString()
				      + " but was:" + actual.toFriendlyString() );
			}
		}
	}

	/**
	 * Test the {@link Matrix3D#getTransform} method.
	 */
	@Test
	public void testGetTransform()
	{
		System.out.println( CLASS_NAME + ".testGetTransform()" );

		/*
		 * Test matrix contents for simple rotations.
		 */
		System.out.println( " - Test individual rotation angles" );
		for ( int angle = -720; angle < 720.0; angle += 15 )
		{
			final double rad = Math.toRadians( angle );
			final double cos = Math.cos( rad );
			final double sin = Math.sin( rad );

			{
				final Matrix3D expected = new Matrix3D( 1, 0, 0, 0,
				                                        0, cos, sin, 0,
				                                        0, -sin, cos, 0 );
				final Matrix3D actual = Matrix3D.getTransform( angle, 0, 0, 0, 0, 0 );
				Matrix3DTester.assertEquals( "getTransform( " + angle + ", 0, 0, 0, 0, 0 )", expected, actual, 0.001 );
			}

			{
				final Matrix3D expected = new Matrix3D( cos, 0, sin, 0,
				                                        0, 1, 0, 0,
				                                        -sin, 0, cos, 0 );
				final Matrix3D actual = Matrix3D.getTransform( 0, angle, 0, 0, 0, 0 );
				Matrix3DTester.assertEquals( "getTransform( 0, " + angle + ", 0, 0, 0, 0 )", expected, actual, 0.001 );
			}

			{
				final Matrix3D expected = new Matrix3D( cos, -sin, 0, 0,
				                                        sin, cos, 0, 0,
				                                        0, 0, 1, 0 );
				final Matrix3D actual = Matrix3D.getTransform( 0, 0, angle, 0, 0, 0 );
				Matrix3DTester.assertEquals( "getTransform( 0, 0, " + angle + ", 0, 0, 0 )", expected, actual, 0.001 );
			}
		}

		System.out.println( " - Test (optimized) standard orientations" );
		Matrix3DTester.assertEquals( "getTransform( 0, 0, 0, 1, 2, 3 )", new Matrix3D( 1, 0, 0, 1, 0, 1, 0, 2, 0, 0, 1, 3 ), Matrix3D.getTransform( 0, 0, 0, 1, 2, 3 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 90, 0, 0, 4, 5, 6 )", new Matrix3D( 1, 0, 0, 4, 0, 0, 1, 5, 0, -1, 0, 6 ), Matrix3D.getTransform( 90, 0, 0, 4, 5, 6 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 180, 0, 0, 7, 8, 9 )", new Matrix3D( 1, 0, 0, 7, 0, -1, 0, 8, 0, 0, -1, 9 ), Matrix3D.getTransform( 180, 0, 0, 7, 8, 9 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 270, 0, 0, 10, 11, 12 )", new Matrix3D( 1, 0, 0, 10, 0, 0, -1, 11, 0, 1, 0, 12 ), Matrix3D.getTransform( 270, 0, 0, 10, 11, 12 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 0, 90, 0, 13, 14, 15 )", new Matrix3D( 0, 0, 1, 13, 0, 1, 0, 14, -1, 0, 0, 15 ), Matrix3D.getTransform( 0, 90, 0, 13, 14, 15 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 0, 180, 0, 16, 17, 18)", new Matrix3D( -1, 0, 0, 16, 0, 1, 0, 17, 0, 0, -1, 18 ), Matrix3D.getTransform( 0, 180, 0, 16, 17, 18 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 0, 270, 0, 19, 20, 21 )", new Matrix3D( 0, 0, -1, 19, 0, 1, 0, 20, 1, 0, 0, 21 ), Matrix3D.getTransform( 0, 270, 0, 19, 20, 21 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 0, 0, 90, 22, 23, 24 )", new Matrix3D( 0, -1, 0, 22, 1, 0, 0, 23, 0, 0, 1, 24 ), Matrix3D.getTransform( 0, 0, 90, 22, 23, 24 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 0, 0, 180, 25, 26, 27 )", new Matrix3D( -1, 0, 0, 25, 0, -1, 0, 26, 0, 0, 1, 27 ), Matrix3D.getTransform( 0, 0, 180, 25, 26, 27 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 0, 0, 270, 28, 29, 30 )", new Matrix3D( 0, 1, 0, 28, -1, 0, 0, 29, 0, 0, 1, 30 ), Matrix3D.getTransform( 0, 0, 270, 28, 29, 30 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 90, 90, 0, 31, 32, 33 )", new Matrix3D( 1, 0, 0, 31, 0, 0, 1, 32, 0, -1, 0, 33 ), Matrix3D.getTransform( 90, 0, 0, 31, 32, 33 ), 0.0 );
		Matrix3DTester.assertEquals( "getTransform( 0, 90, 270, 34, 35, 36 )", new Matrix3D( 1, 0, 0, 34, 0, 0, 1, 35, 0, -1, 0, 36 ), Matrix3D.getTransform( 90, 0, 0, 34, 35, 36 ), 0.0 );
	}
}
