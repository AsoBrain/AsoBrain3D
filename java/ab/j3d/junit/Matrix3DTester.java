/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2004-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.junit;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import ab.j3d.Matrix3D;

/**
 * JUnit unit tool class to help with testing Matrix3D objects.
 *
 * @author  H.B.J. te Lintelo
 *
 * @version $Revision$ $Date$
 */
public final class Matrix3DTester
{
	/**
	 * Test variant when testing two values with each other.
	 * (i.e. A less than B.)
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
	 * Array with posssible (test)bases for the world.
	 * These bases can be used to set the world in different orientations
	 * There are steps from 45 degrees for each axis used to fill the array
	 * with orientation. Also a 'random' position is calculated.
	 */
	public static final Matrix3D[] ROTATED_TEST_MATRICES;

	static
	{
		final List matrices = new ArrayList();

		for ( int x = 0 ; x < 8 ; x++ )
		{
			final double rx = (double)x * 45.0;
			for ( int y = 0 ; y < 8 ; y++ )
			{
				final double ry = (double)y * 45.0;
				for ( int z = 0 ; z < 8 ; z++ )
				{
					final double rz = (double)z * 45.0;
					matrices.add( Matrix3D.getTransform( rx ,ry , rz , (double)(x * y) , (double)(y * z) , (double)(z * x) ) );
				}
			}
		}

		ROTATED_TEST_MATRICES = (Matrix3D[])matrices.toArray( new Matrix3D[ matrices.size() ] );
	}

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private Matrix3DTester()
	{
	}

	/**
	 * Asserts that the expected distances (x,y and z) between two matrices is
	 * true, <code>AssertionFailedError</code> is thrown otherwise.
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   matrix1     First matrix.
	 * @param   matrix2     Second matrix.
	 * @param   x           Expected x distance between the two matrices.
	 * @param   y           Expected y distance between the two matrices.
	 * @param   z           Expected z distance between the two matrices.
	 * @param   delta       Delta value checked distance.
	 */
	public static void assertDistance( final String messagePrefix , final Matrix3D matrix1 , final Matrix3D matrix2 , final double x , final double y , final double z , final double delta )
	{
		final Matrix3D m = matrix2.multiply( matrix1.inverse() );
		Assert.assertEquals( messagePrefix + " (x).", x , m.xo , delta );
		Assert.assertEquals( messagePrefix + " (y).", y , m.yo , delta );
		Assert.assertEquals( messagePrefix + " (z).", z , m.zo , delta );
	}

	/**
	 * Asserts that the position from one matrix is on the correct side from
	 * another matrix. Check this for each axis (x,y and z). Throw
	 * <code>AssertionFailedError</code> if the assertion could not be granted.
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   matrix1         First matrix.
	 * @param   matrix2         Second matrix.
	 * @param   xPos            Expected side for x-axis, value can be:
	 *                          <ul>
	 *                          <li>-1 = matrix2 is less then matrix1</li>
	 *                          <li> 0 = matrix2 is equal with matrix1</li>
	 *                          <li> 1 = matrix2 is greater then matrix1</li>
	 *                          </ul>
	 * @param   yPos            Expected side for y-axis, value can be:
	 *                          <ul>
	 *                          <li>-1 = matrix2 is less then matrix1</li>
	 *                          <li> 0 = matrix2 is equal with matrix1</li>
	 *                          <li> 1 = matrix2 is greater then matrix1</li>
	 *                          </ul>
	 * @param   zPos            Expected side for z-axis, value can be:
	 *                          <ul>
	 *                          <li>-1 = matrix2 is less then matrix1</li>
	 *                          <li> 0 = matrix2 is equal with matrix1</li>
	 *                          <li> 1 = matrix2 is greater then matrix1</li>
	 *                          </ul>
	 * @param   testX           <code>True</code> to check the x side,
	 *                          <code>false</code> otherwise.
	 * @param   testY           <code>True</code> to check the y side,
	 *                          <code>false</code> otherwise.
	 * @param   testZ           <code>True</code> to check the z side,
	 *                          <code>false</code> otherwise.
	 */
	public static void assertPosition( final String messagePrefix , final Matrix3D matrix1 , final Matrix3D matrix2 , final int xPos , final int yPos , final int zPos , final boolean testX , final boolean testY , final boolean testZ )
	{
		final Matrix3D positionMatrix = matrix2.multiply( matrix1.inverse() );

		if ( testX )
		{
			switch( xPos )
			{
				case LESS    :{ Assert.assertTrue  ( messagePrefix + " (x-axis < 0),"  + positionMatrix.toFriendlyString() , positionMatrix.xo <  0        ); break; }
				case EQUAL   :{ Assert.assertEquals( messagePrefix + " (x-axis == 0)," + positionMatrix.toFriendlyString() , 0.0 , positionMatrix.xo , 0.001 ); break; }
				case GREATER :{ Assert.assertTrue  ( messagePrefix + " (x-axis > 0),"  + positionMatrix.toFriendlyString() , positionMatrix.xo >  0        ); break; }
				default      :{ Assert.fail( messagePrefix + ". Unknown choosen side value (x)." ); }
			}
		}

		if ( testY )
		{
			switch( yPos )
			{
				case LESS    :{ Assert.assertTrue  ( messagePrefix + " (y-axis < 0),"  + positionMatrix.toFriendlyString() , positionMatrix.yo <  0        ); break; }
				case EQUAL   :{ Assert.assertEquals( messagePrefix + " (y-axis == 0)," + positionMatrix.toFriendlyString() , 0.0 , positionMatrix.yo , 0.001 ); break; }
				case GREATER :{ Assert.assertTrue  ( messagePrefix + " (y-axis > 0),"  + positionMatrix.toFriendlyString() , positionMatrix.yo >  0        ); break; }
				default      :{ Assert.fail( messagePrefix + ". Unknown choosen side value (y)." ); }
			}
		}

		if ( testZ )
		{
			switch( zPos )
			{
				case LESS    :{ Assert.assertTrue  ( messagePrefix + " (z-axis < 0), " + positionMatrix.toFriendlyString() , positionMatrix.zo <  0         ); break; }
				case EQUAL   :{ Assert.assertEquals( messagePrefix + " (z-axis == 0)," + positionMatrix.toFriendlyString() , 0.0 ,  positionMatrix.zo , 0.001 ); break; }
				case GREATER :{ Assert.assertTrue  ( messagePrefix + " (z-axis > 0), " + positionMatrix.toFriendlyString() , positionMatrix.zo >  0         ); break; }
				default      :{ Assert.fail( messagePrefix + ". Unknown choosen side value (z)." ); }
			}
		}
	}

	/**
	 * Asserts that the orientation from one matrix is equal to the orientation
	 * from another matrix. Throw <code>AssertionFailedError</code> if the
	 * assertion could not be granted.
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   matrix1         First matrix.
	 * @param   matrix2         Second matrix.
	 */
	public static void assertOrientation( final String messagePrefix , final Matrix3D matrix1 , final Matrix3D matrix2 )
	{
		Assert.assertEquals( messagePrefix + " (xx)." , matrix1.xx , matrix2.xx );
		Assert.assertEquals( messagePrefix + " (xy)." , matrix1.xy , matrix2.xy );
		Assert.assertEquals( messagePrefix + " (xz)." , matrix1.xz , matrix2.xz );
		Assert.assertEquals( messagePrefix + " (yx)." , matrix1.yx , matrix2.yx );
		Assert.assertEquals( messagePrefix + " (yy)." , matrix1.yy , matrix2.yy );
		Assert.assertEquals( messagePrefix + " (yz)." , matrix1.yz , matrix2.yz );
		Assert.assertEquals( messagePrefix + " (zx)." , matrix1.zx , matrix2.zx );
		Assert.assertEquals( messagePrefix + " (zy)." , matrix1.zy , matrix2.zy );
		Assert.assertEquals( messagePrefix + " (zz)." , matrix1.zz , matrix2.zz );
	}
}