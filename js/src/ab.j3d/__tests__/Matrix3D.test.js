/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2019 Peter S. Heijnen
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
import { assert, AssertionError } from 'chai';

import { toRadians } from '@numdata/oss';

import Matrix3D from '../Matrix3D';
import Vector3D from '../Vector3D';

/**
 * This tests the {@link Matrix3D} class.
 *
 * @author Peter S. Heijnen
 */
describe( 'Matrix3D', function()
{
	function createTestRunner( getActual )
	{
		return ( test, i ) =>
		{
			let description = "Test #" + ( i + 1 );

			if ( test.error )
			{
				assert.throws( () => getActual( test ), test.error );
			}
			else
			{
				var actual = getActual( test );
				let expected = test.result;
				var equal = test.almostEqual ? expected.almostEquals( actual ) : expected.equals( actual );
				assert.isTrue( equal, description + "\nExpected:" + expected.toFriendlyString() + "\nActual:" + actual.toFriendlyString() );
			}
		};
	}

	/**
	 * Test {@link Matrix3D#equals} method.
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
	it( 'equals', function()
	{
		let m;

		/*
		 * INIT must be identity matrix
		 */
		assert.isTrue( Matrix3D.IDENTITY.equals( new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 ) ), "Matrix3D.equals() identity failed" );

		/*
		 * Test if each component is correctly tested by equals()
		 */
		m = new Matrix3D( 9.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'xx'" );

		m = new Matrix3D( 1.0, 9.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'xy'" );

		m = new Matrix3D( 1.0, 0.0, 9.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'xz'" );

		m = new Matrix3D( 1.0, 0.0, 0.0, 9.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'xo'" );

		m = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 9.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'yx'" );

		m = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 9.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'yy'" );

		m = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 9.0, 0.0, 0.0, 0.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'yz'" );

		m = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 9.0, 0.0, 0.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'yo'" );

		m = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 9.0, 0.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'zx'" );

		m = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 9.0, 1.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'zy'" );

		m = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 9.0, 0.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'zz'" );

		m = new Matrix3D( 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 9.0 );
		assert.isTrue( !Matrix3D.IDENTITY.equals( m ) && !m.equals( Matrix3D.IDENTITY ), "Matrix3D.equals() did not correctly test 'zo'" );
	} );

	/**
	 * Test the {@link Matrix3D#getFromToTransform} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	it( 'getFromToTransform', function()
	{
		/**
		 * Define test properties.
		 *
		 * @noinspection JavaDoc
		 */
		function Test( from, to, result )
		{
			this.from = from;
			this.to   = to;
			if ( result === Error || result && result.prototype instanceof Error )
			{
				this.error = result;
			}
			else
			{
				this.result = result;
			}
		}
		Test.prototype = { almostEqual: true };

		/*
		 * Define 'extreme' tests.
		 */
		let extremeTests =
		[
			/* Test #1  */ new Test( null, null, TypeError ),
			/* Test #2  */ new Test( null, Vector3D.ZERO, TypeError ),
			/* Test #3  */ new Test( Vector3D.ZERO, null, TypeError ),
			/* Test #4  */ new Test( Vector3D.ZERO, Vector3D.ZERO, Error )
		];

		/*
		 * Define tests for orthogonal views.
		 */
		let leftView = new Matrix3D(
			 0.0, -1.0,  0.0, 0.0,
			 0.0,  0.0,  1.0, 0.0,
			-1.0,  0.0,  0.0, 0.0 );

		let rightView = new Matrix3D(
			 0.0,  1.0,  0.0, 0.0,
			 0.0,  0.0,  1.0, 0.0,
			 1.0,  0.0,  0.0, 0.0 );

		let frontView = new Matrix3D(
			 1.0,  0.0,  0.0, 0.0,
			 0.0,  0.0,  1.0, 0.0,
			 0.0, -1.0,  0.0, 0.0 );

		let rearView = new Matrix3D(
			-1.0,  0.0,  0.0, 0.0,
			 0.0,  0.0,  1.0, 0.0,
			 0.0,  1.0,  0.0, 0.0 );

		let bottomView = new Matrix3D(
			-1.0,  0.0,  0.0, 0.0,
			 0.0,  1.0,  0.0, 0.0,
			 0.0,  0.0, -1.0, 0.0 );

		let topView = new Matrix3D(
			 1.0,  0.0,  0.0, 0.0,
			 0.0,  1.0,  0.0, 0.0,
			 0.0,  0.0,  1.0, 0.0 );

		let orthogonalTests =
		[
			/* Test #5  */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_X_AXIS, leftView   ),
			/* Test #6  */ new Test( Vector3D.ZERO, Vector3D.NEGATIVE_X_AXIS, rightView  ),
			/* Test #7  */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Y_AXIS, frontView  ),
			/* Test #8  */ new Test( Vector3D.ZERO, Vector3D.NEGATIVE_Y_AXIS, rearView   ),
			/* Test #9  */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Z_AXIS, bottomView ),
			/* Test #10 */ new Test( Vector3D.ZERO, Vector3D.NEGATIVE_Z_AXIS, topView    ),

			/* Test #11 */ new Test( Vector3D.NEGATIVE_X_AXIS, Vector3D.ZERO, leftView   .plus( 0.0, 0.0, -1.0 ) ),
			/* Test #12 */ new Test( Vector3D.POSITIVE_X_AXIS, Vector3D.ZERO, rightView  .plus( 0.0, 0.0, -1.0 ) ),
			/* Test #13 */ new Test( Vector3D.NEGATIVE_Y_AXIS, Vector3D.ZERO, frontView  .plus( 0.0, 0.0, -1.0 ) ),
			/* Test #14 */ new Test( Vector3D.POSITIVE_Y_AXIS, Vector3D.ZERO, rearView   .plus( 0.0, 0.0, -1.0 ) ),
			/* Test #15 */ new Test( Vector3D.NEGATIVE_Z_AXIS, Vector3D.ZERO, bottomView .plus( 0.0, 0.0, -1.0 ) ),
			/* Test #16 */ new Test( Vector3D.POSITIVE_Z_AXIS, Vector3D.ZERO, topView    .plus( 0.0, 0.0, -1.0 ) )
		];

		/*
		 * Define tests for diagonal views.
		 */
		// let X1Y0Z0 = new Vector3D(  1.0, -1.0, -1.0 );
		// let X0Y1Z0 = new Vector3D( -1.0,  1.0, -1.0 );
		// let X1Y1Z0 = new Vector3D(  1.0,  1.0, -1.0 );
		// let X0Y0Z1 = new Vector3D( -1.0, -1.0,  1.0 );
		// let X1Y0Z1 = new Vector3D(  1.0, -1.0,  1.0 );
		// let X0Y1Z1 = new Vector3D( -1.0,  1.0,  1.0 );
		// let X1Y1Z1 = new Vector3D(  1.0,  1.0,  1.0 );

		let deg45  = Math.PI / 4.0;
		let deg90  = Math.PI / 2.0;
		let deg135 = deg90 + deg45;
		let sqrt2  = Math.sqrt( 2.0 );
		let sqrt3  = Math.sqrt( 3.0 );

		let deg125_2 = deg90 + Math.atan( 1.0 / sqrt2 );

		let diagonalTests =
		[
			/* Test #17 */ new Test( new Vector3D(  0.0, -1.0, -1.0 ), Vector3D.ZERO, Matrix3D.IDENTITY.rotateX( -deg135 )                      .plus( 0.0, 0.0, -sqrt2 ) ),
			/* Test #18 */ new Test( new Vector3D( -1.0,  0.0,  1.0 ), Vector3D.ZERO, Matrix3D.IDENTITY.rotateY(   deg45 ).rotateZ(      deg90 ).plus( 0.0, 0.0, -sqrt2 ) ),
			/* Test #19 */ new Test( new Vector3D(  1.0,  0.0,  1.0 ), Vector3D.ZERO, Matrix3D.IDENTITY.rotateY(  -deg45 ).rotateZ(     -deg90 ).plus( 0.0, 0.0, -sqrt2 ) ),
			/* Test #20 */ new Test( Vector3D.POSITIVE_X_AXIS, Vector3D.ZERO, Matrix3D.IDENTITY.rotateZ(  -deg90 ).rotateX(     -deg90 ).plus( 0.0, 0.0, -1.0   ) ),
			/* Test #21 */ new Test( new Vector3D( -1.0, -1.0, -1.0 ), Vector3D.ZERO, Matrix3D.IDENTITY.rotateZ(   deg45 ).rotateX(  -deg125_2 ).plus( 0.0, 0.0, -sqrt3 ) )
		];

		/*
		 * Execute tests.
		 */
		let allTests = extremeTests.concat( orthogonalTests, diagonalTests );
		allTests.forEach( createTestRunner( test =>
		{
			let upPrimary = Vector3D.POSITIVE_Z_AXIS;
			let upSecondary = Vector3D.POSITIVE_Y_AXIS;
			return Matrix3D.getFromToTransform( test.from, test.to, upPrimary, upSecondary );
		} ) );
	} );

	/**
	 * Test the {@link Matrix3D#getRotationTransform} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	it( 'getRotationTransform', function()
	{
		/**
		 * DefineS test properties.
		 *
		 * @noinspection JavaDoc
		 */
		function Test( pivot, direction, thetaRad, result )
		{
			this.pivot     = pivot;
			this.direction = direction;
			this.thetaRad  = thetaRad;
			if ( result && result.prototype instanceof Error )
			{
				this.error = result;
			}
			else
			{
				this.result = result;
			}
		}

		/*
		 * Define tests to execute.
		 */

		let tests =
		[
			/* Test #1 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_X_AXIS, 0.0, Matrix3D.IDENTITY ),
			/* Test #2 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_X_AXIS, 1.0, Matrix3D.IDENTITY.rotateX( 1.0 ) ),
			/* Test #3 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Y_AXIS, 0.0, Matrix3D.IDENTITY ),
			/* Test #4 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Y_AXIS, 1.0, Matrix3D.IDENTITY.rotateY( 1.0 ) ),
			/* Test #5 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Z_AXIS, 0.0, Matrix3D.IDENTITY ),
			/* Test #6 */ new Test( Vector3D.ZERO, Vector3D.POSITIVE_Z_AXIS, 1.0, Matrix3D.IDENTITY.rotateZ( 1.0 ) )
		];

		/*
		 * Execute tests.
		 */
		tests.forEach( createTestRunner( test => Matrix3D.getRotationTransform( test.pivot, test.direction, test.thetaRad ) ) );
	} );

	/**
	 * Test the {@link Matrix3D#rotateX} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	it( 'rotateX', function()
	{
		/*
		 * Test matrix contents for simple rotations.
		 */
		for ( let angle = 0 ; angle < 360 ; angle += 15 )
		{
			let rad = toRadians( angle );
			let cos = Math.cos( rad );
			let sin = Math.sin( rad );

			let matrix = Matrix3D.IDENTITY.rotateX( rad );

			let where = "Rotate X over " + angle + " degrees";

			assert.equal( matrix.xx, 1.0, where + " - xx" );
			assert.equal( matrix.xy, 0.0, where + " - xy" );
			assert.equal( matrix.xz, 0.0, where + " - xz" );
			assert.equal( matrix.xo, 0.0, where + " - xo" );
			assert.equal( matrix.yx, 0.0, where + " - yx" );
			assert.equal( matrix.yy, cos, where + " - yy" );
			assert.equal( matrix.yz, -sin, where + " - yz" );
			assert.equal( matrix.yo, 0.0, where + " - yo" );
			assert.equal( matrix.zx, 0.0, where + " - zx" );
			assert.equal( matrix.zy, sin, where + " - zy" );
			assert.equal( matrix.zz, cos, where + " - zz" );
			assert.equal( matrix.zo, 0.0, where + " - zo" );
		}

		/*
		 * Test vector rotation.
		 */
		let vectorTests = [
			{ deg: 90.0, vector: new Vector3D( 1.0, 0.0, 0.0 ), expected: new Vector3D( 1.0, 0.0, 0.0 ) },
			{ deg: 90.0, vector: new Vector3D( 0.0, 1.0, 0.0 ), expected: new Vector3D( 0.0, 0.0, 1.0 ) },
			{ deg: 90.0, vector: new Vector3D( 0.0, 0.0, 1.0 ), expected: new Vector3D( 0.0, -1.0, 0.0 ) }
		];

		vectorTests.forEach( test =>
		{
			let matrix = Matrix3D.IDENTITY.rotateX( toRadians( test.deg ) );
			let actual = matrix.transform( test.vector );

			if ( !test.expected.almostEquals( actual ) )
			{
				throw new AssertionError( "Rotate vector " + test.vector.toFriendlyString() + ' ' + test.deg + " degrees over X-axis failed -"
										  + " expected:" + test.expected.toFriendlyString()
										  + " but was:" + actual.toFriendlyString() );
			}
		} );
	} );

	/**
	 * Test the {@link Matrix3D#rotateY} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	it( 'rotateY', function()
	{
		/*
		 * Test matrix contents for simple rotations.
		 */
		for ( let angle = 0.0 ; angle < 360.0 ; angle += 15.0 )
		{
			let rad = toRadians( angle );
			let cos = Math.cos( rad );
			let sin = Math.sin( rad );

			let matrix = Matrix3D.IDENTITY.rotateY( rad );

			let where = "Rotate Y over " + angle + " degrees";

			assert.equal( matrix.xx, cos, where + " - xx" );
			assert.equal( matrix.xy, 0.0, where + " - xy" );
			assert.equal( matrix.xz, sin, where + " - xz" );
			assert.equal( matrix.xo, 0.0, where + " - xo" );
			assert.equal( matrix.yx, 0.0, where + " - yx" );
			assert.equal( matrix.yy, 1.0, where + " - yy" );
			assert.equal( matrix.yz, 0.0, where + " - yz" );
			assert.equal( matrix.yo, 0.0, where + " - yo" );
			assert.equal( matrix.zx, -sin, where + " - zx" );
			assert.equal( matrix.zy, 0.0, where + " - zy" );
			assert.equal( matrix.zz, cos, where + " - zz" );
			assert.equal( matrix.zo, 0.0, where + " - zo" );
		}

		/*
		 * Test vector rotation.
		 */
		let vectorTests =
		[
			{ deg: 90.0, vector: new Vector3D( 1.0, 0.0, 0.0 ), expected: new Vector3D( 0.0,  0.0, -1.0 ) },
			{ deg: 90.0, vector: new Vector3D( 0.0, 1.0, 0.0 ), expected: new Vector3D( 0.0,  1.0,  0.0 ) },
			{ deg: 90.0, vector: new Vector3D( 0.0, 0.0, 1.0 ), expected: new Vector3D( 1.0,  0.0,  0.0 ) }
		];

		vectorTests.forEach( test =>
		{
			let matrix = Matrix3D.IDENTITY.rotateY( toRadians( test.deg ) );
			let actual = matrix.transform( test.vector );

			if ( !test.expected.almostEquals( actual ) )
			{
				throw new AssertionError( "Rotate vector " + test.vector.toFriendlyString() + ' ' + test.deg + " degrees over Y-axis failed -"
										  + " expected:" + test.expected.toFriendlyString()
										  + " but was:" + actual.toFriendlyString() );
			}
		} );
	} );

	/**
	 * Test the {@link Matrix3D#rotateZ} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	it( 'rotateZ', function()
	{
		/*
		 * Test matrix contents for simple rotations.
		 */
		for ( let angle = 0.0 ; angle < 360.0 ; angle += 15.0 )
		{
			let rad = toRadians( angle );
			let cos = Math.cos( rad );
			let sin = Math.sin( rad );

			let matrix = Matrix3D.IDENTITY.rotateZ( rad );

			let where = "Rotate Z over " + angle + " degrees";

			assert.equal( matrix.xx, cos, where + " - xx" );
			assert.equal( matrix.xy, -sin, where + " - xy" );
			assert.equal( matrix.xz, 0.0, where + " - xz" );
			assert.equal( matrix.xo, 0.0, where + " - xo" );
			assert.equal( matrix.yx, sin, where + " - yx" );
			assert.equal( matrix.yy, cos, where + " - yy" );
			assert.equal( matrix.yz, 0.0, where + " - yz" );
			assert.equal( matrix.yo, 0.0, where + " - yo" );
			assert.equal( matrix.zx, 0.0, where + " - zx" );
			assert.equal( matrix.zy, 0.0, where + " - zy" );
			assert.equal( matrix.zz, 1.0, where + " - zz" );
			assert.equal( matrix.zo, 0.0, where + " - zo" );
		}

		/*
		 * Test vector rotation.
		 */
		let vectorTests =
		[
			{ deg: 90.0, vector: new Vector3D( 1.0, 0.0, 0.0 ), expected: new Vector3D(  0.0,  1.0,  0.0 ) },
			{ deg: 90.0, vector: new Vector3D( 0.0, 1.0, 0.0 ), expected: new Vector3D( -1.0,  0.0,  0.0 ) },
			{ deg: 90.0, vector: new Vector3D( 0.0, 0.0, 1.0 ), expected: new Vector3D(  0.0,  0.0,  1.0 ) }
		];

		vectorTests.forEach( test =>
		{
			let matrix = Matrix3D.IDENTITY.rotateZ( toRadians( test.deg ) );
			let actual = matrix.transform( test.vector );

			if ( !test.expected.almostEquals( actual ) )
			{
				throw new AssertionError( "Rotate vector " + test.vector.toFriendlyString() + ' ' + test.deg + " degrees over Z-axis failed -"
										  + " expected:" + test.expected.toFriendlyString()
										  + " but was:" + actual.toFriendlyString() );
			}
		} );
	} );
} );
