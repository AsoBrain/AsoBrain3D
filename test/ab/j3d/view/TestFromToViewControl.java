/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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
