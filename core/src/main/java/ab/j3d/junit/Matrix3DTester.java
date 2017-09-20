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
package ab.j3d.junit;

import ab.j3d.*;
import junit.framework.*;

/**
 * JUnit unit tool class to help with testing {@link Matrix3D} objects.
 *
 * @author  H.B.J. te Lintelo
 * @version $Revision$ $Date$
 */
public class Matrix3DTester
{
	/**
	 *
	 * Constant to specify a negative direction.
	 *
	 * @see     #assertRelativeDirectionX
	 * @see     #assertRelativeDirectionY
	 * @see     #assertRelativeDirectionZ
	 */
	public static final int LESS = -1;

	/**
	 * Test variant when testing two values with each other.
	 * (i.e. A equals B.)
	 */
	public static final int EQUAL = 0;

	/**
	 * Test variant when testing two values with each other.
	 * (i.e. A is greater than B.)
	 */
	public static final int GREATER = 1;

	/**
	 * Standard matrix usable for tests. This defines the identity matrix.
	 */
	public static final Matrix3D IDENTITY = Matrix3D.IDENTITY;

	/**
	 * Standard matrix usable for tests. This defines a rotation of 180 degrees
	 * over the Y-axis.
	 */
	public static final Matrix3D FLIPPED_OVER_X_AXIS = new Matrix3D(
		 1.0 ,  0.0 ,  0.0 , 0.0 ,
		 0.0 , -1.0 ,  0.0 , 0.0 ,
		 0.0 ,  0.0 , -1.0 , 0.0 );

	/**
	 * Standard matrix usable for tests. This defines a rotation of 180 degrees
	 * over the Y-axis.
	 */
	public static final Matrix3D FLIPPED_OVER_Y_AXIS = new Matrix3D(
		-1.0 ,  0.0 ,  0.0 , 0.0 ,
		 0.0 ,  1.0 ,  0.0 , 0.0 ,
		 0.0 ,  0.0 , -1.0 , 0.0 );

	/**
	 * Standard matrix usable for tests. This defines a rotation of 180 degrees
	 * over the Z-axis.
	 */
	public static final Matrix3D FLIPPED_OVER_Z_AXIS = new Matrix3D(
		-1.0 ,  0.0 ,  0.0 , 0.0 ,
		 0.0 , -1.0 ,  0.0 , 0.0 ,
		 0.0 ,  0.0 ,  1.0 , 0.0 );


	/**
	 * Array with possible base transforms for tests.
	 * <p />
	 * These transforms can be used to test different orientations. Each axis is
	 * set in steps of 45 degrees. Also, various translations are applied.
	 */
	public static final Matrix3D[] ROTATED_TEST_MATRICES;
	static
	{
		final Matrix3D[] matrices = new Matrix3D[ 8 * 8 * 8 ];

		int index = 0;
		for ( double rx = 0.0 ; rx < 360.0 ; rx += 45.0 )
		{
			for ( double ry = 0.0 ; ry < 360.0 ; ry += 45.0 )
			{
				for ( double rz = 0.0 ; rz < 360.0 ; rz += 45.0 )
				{
					matrices[ index++ ] = Matrix3D.getTransform( rx ,ry , rz , rx * ry , ry * rz , rz * rx );
				}
			}
		}

		Assert.assertEquals( "Initialization error." , matrices.length , index );
		ROTATED_TEST_MATRICES = matrices;
	}

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private Matrix3DTester()
	{
	}

	/**
	 * Asserts that the 'distance' between two matrices has a specific value.
	 * <p />
	 * The distance is defined as the distance between the two positional
	 * vectors defined by the translational parts of the 3D transformation
	 * matrices. These vectors are defined in the following 3x1 sub-matrix:
	 * <pre>
	 *   [   xx   xy   xz [ xo ] ]
	 *   [   yx   yy   yz [ yo ] ]
	 *   [   zx   zy   zz [ zo ] ]
	 *   [    0    0    0    1   ]
	 * <pre>
	 *
	 * @param   messagePrefix       Prefix to failure messages.
	 * @param   matrix1             First matrix determine distance between.
	 * @param   matrix2             Second matrix determine distance between.
	 * @param   expectedDistance    Expected distance between the matrices.
	 * @param   delta               Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 */
	public static void assertDistance( final String messagePrefix , final Matrix3D matrix1 , final Matrix3D matrix2 , final double expectedDistance , final double delta )
	{
		final double dx            = matrix2.xo - matrix1.xo;
		final double dy            = matrix2.yo - matrix1.yo;
		final double dz            = matrix2.zo - matrix1.zo;
		final double distance      = Math.sqrt( dx * dx + dy * dy + dz * dz );
		final String actualPrefix  = ( messagePrefix != null ) ? messagePrefix + " - " : "";
		final String messageSuffix = "\n\nmatrix1 =\n" + matrix1.toFriendlyString()
		                           + "\n\nmatrix2 =\n" + matrix2.toFriendlyString();

		Assert.assertEquals( actualPrefix + "Has incorrect distance." + messageSuffix , expectedDistance , distance , delta );
	}

	/**
	 * Asserts that one matrix has the specified relative position to another
	 * matrix.
	 * <p />
	 * The position of a 3D transformation matrix is defined by its
	 * translational components as defined by the following 3x1 sub-matrix:
	 * <pre>
	 *   [   xx   xy   xz [ xo ] ]
	 *   [   yx   yy   yz [ yo ] ]
	 *   [   zx   zy   zz [ zo ] ]
	 *   [    0    0    0    1   ]
	 * <pre>
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   relativeTo      Matrix relative to which the test is performed.
	 * @param   matrix          Matrix to test the position of.
	 * @param   expectedX       Expected x distance between the two matrices.
	 * @param   expectedY       Expected y distance between the two matrices.
	 * @param   expectedZ       Expected z distance between the two matrices.
	 * @param   delta           Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 */
	public static void assertRelativePosition( final String messagePrefix , final Matrix3D relativeTo , final Matrix3D matrix , final double expectedX , final double expectedY , final double expectedZ , final double delta )
	{
		final Matrix3D relativeMatrix = matrix.multiply( relativeTo.inverse() );
		final String   actualPrefix   = ( messagePrefix != null ) ? messagePrefix + " - " : "";
		final String   messageSuffix  = "\n\nrelativeTo =\n" + relativeTo.toFriendlyString()
		                              + "\n\nmatrix =\n" + matrix.toFriendlyString()
		                              + "\n\nrelativeMatrix =\n" + relativeMatrix.toFriendlyString();

		Assert.assertEquals( actualPrefix + "Incorrect relative X-value." + messageSuffix , expectedX , relativeMatrix.xo , delta );
		Assert.assertEquals( actualPrefix + "Incorrect relative Y-value." + messageSuffix , expectedY , relativeMatrix.yo , delta );
		Assert.assertEquals( actualPrefix + "Incorrect relative Z-value." + messageSuffix , expectedZ , relativeMatrix.zo , delta );
	}

	/**
	 * Asserts that one matrix lies at the specified relative direction along
	 * the X-axis to another matrix.
	 * <p />
	 * The required direction is specified using an integer which is less than,
	 * equal to, or greater than <code>0</code>. You may use the constants
	 * <code>LESS</code>, <code>EQUAL</code>, or <code>GREATER</code> to specify
	 * this in a more friendly matter.
	 * <p />
	 * The position of a 3D transformation matrix is defined by its
	 * translational components as defined by the following 3x1 sub-matrix:
	 * <pre>
	 *   [   xx   xy   xz [ xo ] ]
	 *   [   yx   yy   yz [ yo ] ]
	 *   [   zx   zy   zz [ zo ] ]
	 *   [    0    0    0    1   ]
	 * <pre>
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   relativeTo      Matrix relative to which the test is performed.
	 * @param   matrix          Matrix to test the direction of.
	 * @param   direction       Relative direction to test (see method comment).
	 * @param   delta           Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 *
	 * @see     #LESS
	 * @see     #EQUAL
	 * @see     #GREATER
	 */
	public static void assertRelativeDirectionX( final String messagePrefix , final Matrix3D relativeTo , final Matrix3D matrix , final int direction , final double delta )
	{
		final Matrix3D relativeMatrix = matrix.multiply( relativeTo.inverse() );
		final String   actualPrefix   = ( messagePrefix != null ) ? messagePrefix + " - " : "";
		final String   messageSuffix  = "\n\nrelativeTo =\n" + relativeTo.toFriendlyString()
		                              + "\n\nmatrix =\n" + matrix.toFriendlyString()
		                              + "\n\nrelativeMatrix =\n" + relativeMatrix.toFriendlyString();

		if ( direction < 0 )
		{
			Assert.assertTrue( actualPrefix + "Should have 'xo < 0', but it isn't." + messageSuffix, relativeMatrix.xo < delta );
		}
		else if ( direction == 0 )
		{
			Assert.assertEquals( actualPrefix + "Should have 'xo = 0', but it isn't." + messageSuffix, 0.0, relativeMatrix.xo, delta );
		}
		else /* direction > 0 */
		{
			Assert.assertTrue( actualPrefix + "Should have 'xo > 0', but it isn't." + messageSuffix, relativeMatrix.xo > -delta );
		}
	}

	/**
	 * Asserts that one matrix lies at the specified relative direction along
	 * the Y-axis to another matrix.
	 * <p />
	 * The required direction is specified using an integer which is less than,
	 * equal to, or greater than <code>0</code>. You may use the constants
	 * <code>LESS</code>, <code>EQUAL</code>, or <code>GREATER</code> to specify
	 * this in a more friendly matter.
	 * <p />
	 * The position of a 3D transformation matrix is defined by its
	 * translational components as defined by the following 3x1 sub-matrix:
	 * <pre>
	 *   [   xx   xy   xz [ xo ] ]
	 *   [   yx   yy   yz [ yo ] ]
	 *   [   zx   zy   zz [ zo ] ]
	 *   [    0    0    0    1   ]
	 * <pre>
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   relativeTo      Matrix relative to which the test is performed.
	 * @param   matrix          Matrix to test the direction of.
	 * @param   direction       Relative direction to test (see method comment).
	 * @param   delta           Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 *
	 * @see     #LESS
	 * @see     #EQUAL
	 * @see     #GREATER
	 */
	public static void assertRelativeDirectionY( final String messagePrefix , final Matrix3D relativeTo , final Matrix3D matrix , final int direction , final double delta )
	{
		final Matrix3D relativeMatrix = matrix.multiply( relativeTo.inverse() );
		final String   actualPrefix   = ( messagePrefix != null ) ? messagePrefix + " - " : "";
		final String   messageSuffix  = "\n\nrelativeTo =\n" + relativeTo.toFriendlyString()
		                              + "\n\nmatrix =\n" + matrix.toFriendlyString()
		                              + "\n\nrelativeMatrix =\n" + relativeMatrix.toFriendlyString();

		if ( direction < 0 )
		{
			Assert.assertTrue( actualPrefix + "Should have 'yo < 0', but it isn't." + messageSuffix, relativeMatrix.yo < delta );
		}
		else if ( direction == 0 )
		{
			Assert.assertEquals( actualPrefix + "Should have 'yo = 0', but it isn't." + messageSuffix, 0.0, relativeMatrix.yo, delta );
		}
		else /* direction > 0 */
		{
			Assert.assertTrue( actualPrefix + "Should have 'yo > 0', but it isn't." + messageSuffix, relativeMatrix.yo > -delta );
		}
	}

	/**
	 * Asserts that one matrix lies at the specified relative direction along
	 * the Z-axis to another matrix.
	 * <p />
	 * The required direction is specified using an integer which is less than,
	 * equal to, or greater than <code>0</code>. You may use the constants
	 * <code>LESS</code>, <code>EQUAL</code>, or <code>GREATER</code> to specify
	 * this in a more friendly matter.
	 * <p />
	 * The position of a 3D transformation matrix is defined by its
	 * translational components as defined by the following 3x1 sub-matrix:
	 * <pre>
	 *   [   xx   xy   xz [ xo ] ]
	 *   [   yx   yy   yz [ yo ] ]
	 *   [   zx   zy   zz [ zo ] ]
	 *   [    0    0    0    1   ]
	 * <pre>
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   relativeTo      Matrix relative to which the test is performed.
	 * @param   matrix          Matrix to test the direction of.
	 * @param   direction       Relative direction to test (see method comment).
	 * @param   delta           Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 *
	 * @see     #LESS
	 * @see     #EQUAL
	 * @see     #GREATER
	 */
	public static void assertRelativeDirectionZ( final String messagePrefix , final Matrix3D relativeTo , final Matrix3D matrix , final int direction , final double delta )
	{
		final Matrix3D relativeMatrix = matrix.multiply( relativeTo.inverse() );
		final String   actualPrefix   = ( messagePrefix != null ) ? messagePrefix + " - " : "";
		final String   messageSuffix  = "\n\nrelativeTo =\n" + relativeTo.toFriendlyString()
		                              + "\n\nmatrix =\n" + matrix.toFriendlyString()
		                              + "\n\nrelativeMatrix =\n" + relativeMatrix.toFriendlyString();

		if ( direction < 0 )
		{
			Assert.assertTrue( actualPrefix + "Should have 'zo < 0', but it isn't." + messageSuffix, relativeMatrix.zo < delta );
		}
		else if ( direction == 0 )
		{
			Assert.assertEquals( actualPrefix + "Should have 'zo = 0', but it isn't." + messageSuffix, 0.0, relativeMatrix.zo, delta );
		}
		else /*   direction > 0 */
		{
			Assert.assertTrue( actualPrefix + "Should have 'zo > 0', but it isn't." + messageSuffix, relativeMatrix.zo > -delta );
		}
	}

	/**
	 * Asserts that the orientation from one matrix is equal to the orientation
	 * from another matrix.
	 * <p />
	 * The orientation of a 3D transformation matrix is defined by the following
	 * 3x3 sub-matrix:
	 * <pre>
	 *   [  [ xx   xy   xz ] xo  ]
	 *   [  [ yx   yy   yz ] yo  ]
	 *   [  [ zx   zy   zz ] zo  ]
	 *   [     0    0    0    1  ]
	 * <pre>
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   expected        Expected matrix value.
	 * @param   actual          Actual matrix value.
	 * @param   delta           Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 */
	public static void assertEqualOrientation( final String messagePrefix , final Matrix3D expected , final Matrix3D actual , final double delta )
	{
		final String actualPrefix = ( ( messagePrefix != null ) ? messagePrefix + "\n" : "" ) + "Expected:" + expected.toFriendlyString() + "\nActual: " + actual.toFriendlyString() + "\n";

		Assert.assertEquals( actualPrefix + "Incorrect 'xx' value." , expected.xx , actual.xx , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'xy' value." , expected.xy , actual.xy , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'xz' value." , expected.xz , actual.xz , delta );

		Assert.assertEquals( actualPrefix + "Incorrect 'yx' value." , expected.yx , actual.yx , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'yy' value." , expected.yy , actual.yy , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'yz' value." , expected.yz , actual.yz , delta );

		Assert.assertEquals( actualPrefix + "Incorrect 'zx' value." , expected.zx , actual.zx , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'zy' value." , expected.zy , actual.zy , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'zz' value." , expected.zz , actual.zz , delta );
	}

	/**
	 * Asserts that the TRANSLATION from one matrix is equal to the translation
	 * from another matrix.
	 * <p />
	 * The translational components of a 3D transformation matrix are defined by
	 * the following 3x1 sub-matrix:
	 * <pre>
	 *   [   xx   xy   xz [ xo ] ]
	 *   [   yx   yy   yz [ yo ] ]
	 *   [   zx   zy   zz [ zo ] ]
	 *   [    0    0    0    1   ]
	 * <pre>
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   expected        Expected matrix value.
	 * @param   actual          Actual matrix value.
	 * @param   delta           Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 */
	public static void assertEqualTranslation( final String messagePrefix , final Matrix3D expected , final Matrix3D actual , final double delta )
	{
		final String actualPrefix = ( ( messagePrefix != null ) ? messagePrefix + "\n" : "" ) + "Expected:" + expected.toFriendlyString() + "\nActual: " + actual.toFriendlyString() + "\n";

		Assert.assertEquals( actualPrefix + "Incorrect 'xo' value." , expected.xo , actual.xo , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'yo' value." , expected.yo , actual.yo , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'zo' value." , expected.zo , actual.zo , delta );
	}

	/**
	 * Asserts that one matrix is equal to another matrix.
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   expected        Expected matrix value.
	 * @param   actual          Actual matrix value.
	 * @param   delta           Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 */
	public static void assertEquals( final String messagePrefix , final Matrix3D expected , final Matrix3D actual , final double delta )
	{
		final String actualPrefix = ( ( messagePrefix != null ) ? messagePrefix + "\n" : "" ) + "Expected:" + expected.toFriendlyString() + "\nActual: " + actual.toFriendlyString() + "\n";

		Assert.assertEquals( actualPrefix + "Incorrect 'xx' value." , expected.xx , actual.xx , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'xy' value." , expected.xy , actual.xy , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'xz' value." , expected.xz , actual.xz , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'xo' value." , expected.xo , actual.xo , delta );

		Assert.assertEquals( actualPrefix + "Incorrect 'yx' value." , expected.yx , actual.yx , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'yy' value." , expected.yy , actual.yy , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'yz' value." , expected.yz , actual.yz , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'yo' value." , expected.yo , actual.yo , delta );

		Assert.assertEquals( actualPrefix + "Incorrect 'zx' value." , expected.zx , actual.zx , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'zy' value." , expected.zy , actual.zy , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'zz' value." , expected.zz , actual.zz , delta );
		Assert.assertEquals( actualPrefix + "Incorrect 'zo' value." , expected.zo , actual.zo , delta );
	}
}
