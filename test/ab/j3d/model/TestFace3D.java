/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2009
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
package ab.j3d.model;

import junit.framework.TestCase;

import ab.j3d.Vector3D;
import ab.j3d.junit.Vector3DTester;

/**
 * This class tests the {@link Face3D} class.
 *
 * @see     Face3D
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestFace3D
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestFace3D.class.getName();

	/**
	 * Test the {@link Face3D#getDistance} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testGetDistance()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetDistance()" );

		/*
		 * Define test properties.
		 */
		class Test
		{
			final Face3D _face;
			final double _expected;

			private Test( final Vector3D[] vertices , final double distance )
			{
				final Object3DBuilder builder = new Object3DBuilder();
				builder.addFace( vertices , null , false , false );
				final Object3D object = builder.getObject3D();

				_face     = object.getFace( 0 );
				_expected = distance;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Vector3D leftFrontBottom  = Vector3D.INIT.set( -1.0 , -1.0 , -1.0 );
		final Vector3D leftFrontTop     = Vector3D.INIT.set( -1.0 , -1.0 ,  1.0 );
		final Vector3D leftRearBottom   = Vector3D.INIT.set( -1.0 ,  1.0 , -1.0 );
		final Vector3D leftRearTop      = Vector3D.INIT.set( -1.0 ,  1.0 ,  1.0 );
		final Vector3D rightFrontBottom = Vector3D.INIT.set(  1.0 , -1.0 , -1.0 );
		final Vector3D rightFrontTop    = Vector3D.INIT.set(  1.0 , -1.0 ,  1.0 );
		final Vector3D rightRearBottom  = Vector3D.INIT.set(  1.0 ,  1.0 , -1.0 );
		final Vector3D rightRearTop     = Vector3D.INIT.set(  1.0 ,  1.0 ,  1.0 );

		final Test[] tests =
		{
			/* normal = on XY plane */

			/* Test #1  */ new Test( new Vector3D[] { leftRearBottom   , leftFrontBottom  , rightFrontBottom , rightRearBottom  } ,  1.0 ) ,
			/* Test #2  */ new Test( new Vector3D[] { leftFrontTop     , leftRearTop      , rightRearTop     , rightFrontTop    } ,  1.0 ) ,
			/* Test #3  */ new Test( new Vector3D[] { leftFrontBottom  , leftRearBottom   , rightRearBottom  , rightFrontBottom } , -1.0 ) ,
			/* Test #4  */ new Test( new Vector3D[] { leftRearTop      , leftFrontTop     , rightFrontTop    , rightRearTop     } , -1.0 ) ,
			/* Test #5  */ new Test( new Vector3D[] { leftRearBottom   , leftRearTop      , rightFrontTop    , rightFrontBottom } ,  0.0 ) ,
			/* Test #6  */ new Test( new Vector3D[] { rightRearBottom  , rightRearTop     , leftFrontTop     , leftFrontBottom  } ,  0.0 ) ,
			/* Test #7  */ new Test( new Vector3D[] { rightFrontBottom , rightFrontTop    , leftRearTop      , leftRearBottom   } ,  0.0 ) ,
			/* Test #8  */ new Test( new Vector3D[] { leftFrontBottom  , leftFrontTop     , rightRearTop     , rightRearBottom  } ,  0.0 ) ,

			/* normal = on XZ plane */

			/* Test #9  */ new Test( new Vector3D[] { leftFrontBottom  , leftFrontTop     , rightFrontTop    , rightFrontBottom } ,  1.0 ) ,
			/* Test #10 */ new Test( new Vector3D[] { rightRearBottom  , rightRearTop     , leftRearTop      , leftRearBottom   } ,  1.0 ) ,
			/* Test #11 */ new Test( new Vector3D[] { leftFrontTop     , leftFrontBottom  , rightFrontBottom , rightFrontTop    } , -1.0 ) ,
			/* Test #12 */ new Test( new Vector3D[] { rightRearTop     , rightRearBottom  , leftRearBottom   , leftRearTop      } , -1.0 ) ,
			/* Test #13 */ new Test( new Vector3D[] { rightRearBottom  , leftRearTop      , leftFrontTop     , rightFrontBottom } ,  0.0 ) ,
			/* Test #14 */ new Test( new Vector3D[] { leftRearBottom   , rightRearTop     , rightFrontTop    , leftFrontBottom  } ,  0.0 ) ,
			/* Test #15 */ new Test( new Vector3D[] { rightFrontBottom , leftFrontTop     , leftRearTop      , rightRearBottom  } ,  0.0 ) ,
			/* Test #16 */ new Test( new Vector3D[] { leftFrontBottom  , rightFrontTop    , rightRearTop     , leftRearBottom   } ,  0.0 ) ,

			/* normal = on YZ plane */

			/* Test #17 */ new Test( new Vector3D[] { leftRearBottom   , leftRearTop      , leftFrontTop     , leftFrontBottom  } ,  1.0 ) ,
			/* Test #18 */ new Test( new Vector3D[] { rightFrontBottom , rightFrontTop    , rightRearTop     , rightRearBottom  } ,  1.0 ) ,
			/* Test #19 */ new Test( new Vector3D[] { leftRearTop      , leftRearBottom   , leftFrontBottom  , leftFrontTop     } , -1.0 ) ,
			/* Test #20 */ new Test( new Vector3D[] { rightFrontTop    , rightFrontBottom , rightRearBottom  , rightRearTop     } , -1.0 ) ,
			/* Test #21 */ new Test( new Vector3D[] { leftRearBottom   , leftFrontTop     , rightFrontTop    , rightRearBottom  } ,  0.0 ) ,
			/* Test #22 */ new Test( new Vector3D[] { leftFrontBottom  , leftRearTop      , rightRearTop     , rightFrontBottom } ,  0.0 ) ,
			/* Test #23 */ new Test( new Vector3D[] { leftFrontTop     , leftRearBottom   , rightRearBottom  , rightFrontTop    } ,  0.0 ) ,
			/* Test #24 */ new Test( new Vector3D[] { leftRearTop      , leftFrontBottom  , rightFrontBottom , rightRearTop     } ,  0.0 ) ,
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test   test        = tests[ i ];
			final String description = "Test #" + ( i + 1 );
			final Face3D face        = test._face;

			final double expected = test._expected;
			final double actual   = face.getDistance();

			assertEquals( description , expected , actual , 0.0001 );
		}
	}

	/**
	 * Test the {@link Face3D#getNormal} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testGetNormal()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetNormal()" );

		/*
		 * Define test properties.
		 */
		class Test
		{
			final Face3D   _face;
			final Vector3D _expected;

			private Test( final Vector3D[] vertices , final double normalX , final double normalY , final double normalZ )
			{
				final Object3DBuilder builder = new Object3DBuilder();
				builder.addFace( vertices , null , false , false );
				final Object3D object = builder.getObject3D();

				_face     = object.getFace( 0 );
				_expected = Vector3D.INIT.set( normalX , normalY , normalZ );
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Vector3D leftFrontBottom  = Vector3D.INIT.set( -1.0 , -1.0 , -1.0 );
		final Vector3D leftFrontTop     = Vector3D.INIT.set( -1.0 , -1.0 ,  1.0 );
		final Vector3D leftRearBottom   = Vector3D.INIT.set( -1.0 ,  1.0 , -1.0 );
		final Vector3D leftRearTop      = Vector3D.INIT.set( -1.0 ,  1.0 ,  1.0 );
		final Vector3D rightFrontBottom = Vector3D.INIT.set(  1.0 , -1.0 , -1.0 );
		final Vector3D rightFrontTop    = Vector3D.INIT.set(  1.0 , -1.0 ,  1.0 );
		final Vector3D rightRearBottom  = Vector3D.INIT.set(  1.0 ,  1.0 , -1.0 );
		final Vector3D rightRearTop     = Vector3D.INIT.set(  1.0 ,  1.0 ,  1.0 );

		final double hr2 = Math.sqrt( 0.5 );

		final Test[] tests =
		{
			/* Test #1  */ new Test( new Vector3D[] { leftRearBottom   , leftRearTop      , leftFrontTop     , leftFrontBottom  } , -1.0 ,  0.0 ,  0.0 ) , /* left   */
			/* Test #2  */ new Test( new Vector3D[] { rightFrontBottom , rightFrontTop    , rightRearTop     , rightRearBottom  } ,  1.0 ,  0.0 ,  0.0 ) , /* right  */
			/* Test #3  */ new Test( new Vector3D[] { leftFrontBottom  , leftFrontTop     , rightFrontTop    , rightFrontBottom } ,  0.0 , -1.0 ,  0.0 ) , /* front  */
			/* Test #4  */ new Test( new Vector3D[] { rightRearBottom  , rightRearTop     , leftRearTop      , leftRearBottom   } ,  0.0 ,  1.0 ,  0.0 ) , /* rear   */
			/* Test #5  */ new Test( new Vector3D[] { leftRearBottom   , leftFrontBottom  , rightFrontBottom , rightRearBottom  } ,  0.0 ,  0.0 , -1.0 ) , /* bottom */
			/* Test #6  */ new Test( new Vector3D[] { leftFrontTop     , leftRearTop      , rightRearTop     , rightFrontTop    } ,  0.0 ,  0.0 ,  1.0 ) , /* top    */

			/* Test #7  */ new Test( new Vector3D[] { leftRearBottom   , leftRearTop      , rightFrontTop    , rightFrontBottom } , -hr2 , -hr2 ,  0.0 ) ,
			/* Test #8  */ new Test( new Vector3D[] { rightRearBottom  , rightRearTop     , leftFrontTop     , leftFrontBottom  } , -hr2 ,  hr2 ,  0.0 ) ,
			/* Test #9  */ new Test( new Vector3D[] { rightFrontBottom , rightFrontTop    , leftRearTop      , leftRearBottom   } ,  hr2 ,  hr2 ,  0.0 ) ,
			/* Test #10 */ new Test( new Vector3D[] { leftFrontBottom  , leftFrontTop     , rightRearTop     , rightRearBottom  } ,  hr2 , -hr2 ,  0.0 ) ,

			/* Test #11 */ new Test( new Vector3D[] { rightRearBottom  , leftRearTop      , leftFrontTop     , rightFrontBottom } , -hr2 ,  0.0 , -hr2 ) ,
			/* Test #12 */ new Test( new Vector3D[] { leftRearBottom   , rightRearTop     , rightFrontTop    , leftFrontBottom  } , -hr2 ,  0.0 ,  hr2 ) ,
			/* Test #13 */ new Test( new Vector3D[] { rightFrontBottom , leftFrontTop     , leftRearTop      , rightRearBottom  } ,  hr2 ,  0.0 ,  hr2 ) ,
			/* Test #14 */ new Test( new Vector3D[] { leftFrontBottom  , rightFrontTop    , rightRearTop     , leftRearBottom   } ,  hr2 ,  0.0 , -hr2 ) ,

			/* Test #15 */ new Test( new Vector3D[] { leftRearBottom   , leftFrontTop     , rightFrontTop    , rightRearBottom  } ,  0.0 , -hr2 , -hr2 ) ,
			/* Test #16 */ new Test( new Vector3D[] { leftFrontBottom  , leftRearTop      , rightRearTop     , rightFrontBottom } ,  0.0 , -hr2 ,  hr2 ) ,
			/* Test #17 */ new Test( new Vector3D[] { leftFrontTop     , leftRearBottom   , rightRearBottom  , rightFrontTop    } ,  0.0 ,  hr2 ,  hr2 ) ,
			/* Test #18 */ new Test( new Vector3D[] { leftRearTop      , leftFrontBottom  , rightFrontBottom , rightRearTop     } ,  0.0 ,  hr2 , -hr2 ) ,
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test   test        = tests[ i ];
			final String description = "Test #" + ( i + 1 );
			final Face3D face        = test._face;

			final Vector3D expected = test._expected;
			final Vector3D actual   = face.getNormal();

			Vector3DTester.assertEquals( description , expected , actual , 0.0001 );
		}
	}
}
