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
			  1.0 ,  0.0 ,  0.0 ,  0.0 ,
			  0.0 ,  0.0 ,  1.0 ,  0.0 ,
			  0.0 , -1.0 ,  0.0 , -1.0 );

		final Matrix3D actual = fromToViewControl.getTransform();

		assertTrue( "Initial transform failed!\nExpected:" + expected.toFriendlyString() + "\nActual:" + actual.toFriendlyString()
		          , expected.almostEquals( actual ) );
	}
}
