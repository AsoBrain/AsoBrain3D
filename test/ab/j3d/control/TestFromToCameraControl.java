/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2010
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

import java.awt.*;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import junit.framework.*;
import org.jetbrains.annotations.*;

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

		final FromToCameraControl fromToCameraControl = new FromToCameraControl( new View3D( new Scene( Scene.MM ) )
			{
				@Nullable
				@Override
				public Component getComponent()
				{
					return null;
				}

				@Override
				public void update()
				{
				}

				@Nullable
				@Override
				public Projector getProjector()
				{
					return null;
				}

				@Nullable
				@Override
				protected ViewControlInput getControlInput()
				{
					return null;
				}

				@Override
				public double getFrontClipDistance()
				{
					return 0.0;
				}

				@Override
				public void setFrontClipDistance( final double front )
				{
				}

				@Override
				public double getBackClipDistance()
				{
					return 0.0;
				}

				@Override
				public void setBackClipDistance( final double back )
				{
				}
			} );

		final Matrix3D expected = new Matrix3D(
			  1.0 ,  0.0 ,  0.0 ,  0.0 ,
			  0.0 ,  0.0 ,  1.0 ,  0.0 ,
			  0.0 , -1.0 ,  0.0 , -1000.0 );

		final Matrix3D actual = fromToCameraControl.getScene2View();

		assertTrue( "Initial transform failed!\nExpected:" + expected.toFriendlyString() + "\nActual:" + actual.toFriendlyString()
		          , expected.almostEquals( actual ) );
	}
}
