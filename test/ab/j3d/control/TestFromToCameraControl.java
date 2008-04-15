/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2008
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
package ab.j3d.control;

import java.awt.Component;

import junit.framework.TestCase;

import ab.j3d.Matrix3D;
import ab.j3d.view.Projector;
import ab.j3d.view.ViewModelView;

/**
 * This class tests the {@link FromToCameraControl} class.
 *
 * @see     FromToCameraControl
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class TestFromToCameraControl
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestFromToCameraControl.class.getName();

	/**
	 * Test the {@link FromToCameraControl} constructor.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testFromToCameraControl()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testConstructor()" );

		final FromToCameraControl fromToCameraControl = new FromToCameraControl( new ViewModelView( 1.0 , "id" )
			{
				public Component getComponent()
				{
					return null;
				}

				public void update()
				{
				}

				public Projector getProjector()
				{
					return null;
				}

				protected ControlInput getControlInput()
				{
					return null;
				}
		} );

		final Matrix3D expected = Matrix3D.INIT.set(
			  1.0 ,  0.0 ,  0.0 ,  0.0 ,
			  0.0 ,  0.0 ,  1.0 ,  0.0 ,
			  0.0 , -1.0 ,  0.0 , -1.0 );

		final Matrix3D actual = fromToCameraControl.getTransform();

		assertTrue( "Initial transform failed!\nExpected:" + expected.toFriendlyString() + "\nActual:" + actual.toFriendlyString()
		          , expected.almostEquals( actual ) );
	}
}
