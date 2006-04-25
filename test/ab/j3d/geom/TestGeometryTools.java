/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2006
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
package ab.j3d.geom;

import junit.framework.TestCase;

import ab.j3d.Vector3D;
import ab.j3d.junit.Vector3DTester;

/**
 * This class tests the {@link GeometryTools} class.
 *
 * @see     GeometryTools
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class TestGeometryTools
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestGeometryTools.class.getName();

	/**
	 * Test the {@link GeometryTools#getIntersectionBetweenRayAndPolygon} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testGetIntersectionBetweenRayAndPolygon()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetIntersectionBetweenRayAndPolygon()" );

		/*
		 * Define test properties.
		 */
		class Test
		{
			final Polygon3D _polygon;
			final Ray3D    _ray;
			final Vector3D _expected;

			private Test( final Polygon3D polygon , final double rayOriginX , final double rayOriginY , final double rayOriginZ , final double rayDirectionX , final double rayDirectionY , final double rayDirectionZ , final Vector3D expected )
			{
				_polygon      = polygon;
				_ray          = new BasicRay3D( rayOriginX , rayOriginY , rayOriginZ , rayDirectionX , rayDirectionY , rayDirectionZ );
				_expected     = expected;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Vector3D v0 = Vector3D.INIT;

		final Vector3D leftFrontBottom  = v0.set( -1.0 , -1.0 , -1.0 );
		final Vector3D leftFrontTop     = v0.set( -1.0 , -1.0 ,  1.0 );
		final Vector3D leftRearBottom   = v0.set( -1.0 ,  1.0 , -1.0 );
		final Vector3D leftRearTop      = v0.set( -1.0 ,  1.0 ,  1.0 );
		final Vector3D rightFrontBottom = v0.set(  1.0 , -1.0 , -1.0 );
		final Vector3D rightFrontTop    = v0.set(  1.0 , -1.0 ,  1.0 );
		final Vector3D rightRearBottom  = v0.set(  1.0 ,  1.0 , -1.0 );
		final Vector3D rightRearTop     = v0.set(  1.0 ,  1.0 ,  1.0 );

		final Polygon3D leftSide   = new BasicPolygon3D( new Vector3D[] { leftRearBottom   , leftRearTop      , leftFrontTop     , leftFrontBottom  } , false );
		final Polygon3D rightSide  = new BasicPolygon3D( new Vector3D[] { rightFrontBottom , rightFrontTop    , rightRearTop     , rightRearBottom  } , false );
		final Polygon3D frontSide  = new BasicPolygon3D( new Vector3D[] { leftFrontBottom  , leftFrontTop     , rightFrontTop    , rightFrontBottom } , false );
		final Polygon3D rearSide   = new BasicPolygon3D( new Vector3D[] { rightRearBottom  , rightRearTop     , leftRearTop      , leftRearBottom   } , false );
		final Polygon3D bottomSide = new BasicPolygon3D( new Vector3D[] { leftRearBottom   , leftFrontBottom  , rightFrontBottom , rightRearBottom  } , false );
		final Polygon3D topSide    = new BasicPolygon3D( new Vector3D[] { leftFrontTop     , leftRearTop      , rightRearTop     , rightFrontTop    } , false );

		final Test[] tests =
		{
			/* Test #1  */ new Test( leftSide   , -2.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , v0.set( -1.0 ,  0.0 ,  0.0 ) ) ,
			/* Test #2  */ new Test( leftSide   , -1.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , v0.set( -1.0 ,  0.0 ,  0.0 ) ) ,
			/* Test #3  */ new Test( leftSide   ,  0.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , null                         ) ,
			/* Test #4  */ new Test( leftSide   ,  1.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , null                         ) ,
			/* Test #5  */ new Test( leftSide   ,  2.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , null                         ) ,

			/* Test #6  */ new Test( rightSide  , -2.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 , null                         ) ,
			/* Test #7  */ new Test( rightSide  , -1.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 , null                         ) ,
			/* Test #8  */ new Test( rightSide  ,  0.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 , null                         ) ,
			/* Test #9  */ new Test( rightSide  ,  1.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 , v0.set(  1.0 ,  0.0 ,  0.0 ) ) ,
			/* Test #10 */ new Test( rightSide  ,  2.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 , v0.set(  1.0 ,  0.0 ,  0.0 ) ) ,

			/* Test #11 */ new Test( frontSide  ,  0.0 , -2.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 , v0.set(  0.0 , -1.0 ,  0.0 ) ) ,
			/* Test #12 */ new Test( frontSide  ,  0.0 , -1.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 , v0.set(  0.0 , -1.0 ,  0.0 ) ) ,
			/* Test #13 */ new Test( frontSide  ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 , null                         ) ,
			/* Test #14 */ new Test( frontSide  ,  0.0 ,  1.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 , null                         ) ,
			/* Test #15 */ new Test( frontSide  ,  0.0 ,  2.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 , null                         ) ,

			/* Test #16 */ new Test( rearSide   ,  0.0 , -2.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 , null                         ) ,
			/* Test #17 */ new Test( rearSide   ,  0.0 , -1.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 , null                         ) ,
			/* Test #18 */ new Test( rearSide   ,  0.0 ,  0.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 , null                         ) ,
			/* Test #19 */ new Test( rearSide   ,  0.0 ,  1.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 , v0.set(  0.0 ,  1.0 ,  0.0 ) ) ,
			/* Test #20 */ new Test( rearSide   ,  0.0 ,  2.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 , v0.set(  0.0 ,  1.0 ,  0.0 ) ) ,

			/* Test #21 */ new Test( bottomSide ,  0.0 ,  0.0 , -2.0 ,  0.0 ,  0.0 ,  1.0 , v0.set(  0.0 ,  0.0 , -1.0 ) ) ,
			/* Test #22 */ new Test( bottomSide ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 ,  1.0 , v0.set(  0.0 ,  0.0 , -1.0 ) ) ,
			/* Test #23 */ new Test( bottomSide ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 , null                         ) ,
			/* Test #24 */ new Test( bottomSide ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 ,  1.0 , null                         ) ,
			/* Test #25 */ new Test( bottomSide ,  0.0 ,  0.0 ,  2.0 ,  0.0 ,  0.0 ,  1.0 , null                         ) ,

			/* Test #26 */ new Test( topSide    ,  0.0 ,  0.0 , -2.0 ,  0.0 ,  0.0 , -1.0 , null                         ) ,
			/* Test #27 */ new Test( topSide    ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 , -1.0 , null                         ) ,
			/* Test #28 */ new Test( topSide    ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 , -1.0 , null                         ) ,
			/* Test #29 */ new Test( topSide    ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , -1.0 , v0.set(  0.0 ,  0.0 ,  1.0 ) ) ,
			/* Test #30 */ new Test( topSide    ,  0.0 ,  0.0 ,  2.0 ,  0.0 ,  0.0 , -1.0 , v0.set(  0.0 ,  0.0 ,  1.0 ) ) ,
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final Vector3D result = GeometryTools.getIntersectionBetweenRayAndPolygon( test._polygon , test._ray );

			assertEquals( description, test._expected, result );
		}
	}

	/**
	 * Test the {@link GeometryTools#getIntersectionBetweenRayAndPlane} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	public static void testGetIntersectionBetweenRayAndPlane()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetIntersectionBetweenRayAndPlane()" );

		/*
		 * Define test properties.
		 */
		final class Test
		{
			final double   _planeNormalX;
			final double   _planeNormalY;
			final double   _planeNormalZ;
			final double   _planeDistance;
			final boolean  _planeTwoSided;
			final Vector3D _rayOrigin;
			final Vector3D _rayDirection;
			final Object   _expected;

			private Test( final double planeNormalX , final double planeNormalY , final double planeNormalZ , final double planeDistance , final boolean planeTwoSided , final Vector3D rayOrigin , final Vector3D rayDirection , final Vector3D expected )
			{
				_planeNormalX  = planeNormalX;
				_planeNormalY  = planeNormalY;
				_planeNormalZ  = planeNormalZ;
				_planeDistance = planeDistance;
				_planeTwoSided = planeTwoSided;
				_rayOrigin     = rayOrigin;
				_rayDirection  = rayDirection;
				_expected      = expected;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Vector3D v0 = Vector3D.INIT;

		final Test[] tests =
		{
			/*
			 * Tests with plane: Z = -5
			 */

			/* Test #1  */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #2  */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , v0.set( 0.0 , 0.0 , -5.0 ) ) ,
			/* Test #3  */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , v0.set( 0.0 , 0.0 , -5.0 ) ) ,
			/* Test #4  */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,
			/* Test #5  */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,
			/* Test #6  */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,

			/* Test #7  */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #8  */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , v0.set( 0.0 , 0.0 , -5.0 ) ) ,
			/* Test #9  */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , v0.set( 0.0 , 0.0 , -5.0 ) ) ,
			/* Test #10 */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , v0.set( 0.0 , 0.0 , -5.0 ) ) ,
			/* Test #11 */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,
			/* Test #12 */ new Test( 0.0 , 0.0 ,  1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,

			/*
			 * Tests with plane: Z = 0
			 */

			/* Test #13 */ new Test( 0.0 , 0.0 ,  1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #14 */ new Test( 0.0 , 0.0 ,  1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , v0.set( 0.0 , 0.0 ,  0.0 ) ) ,
			/* Test #15 */ new Test( 0.0 , 0.0 ,  1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , v0.set( 0.0 , 0.0 ,  0.0 ) ) ,
			/* Test #16 */ new Test( 0.0 , 0.0 ,  1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,
			/* Test #17 */ new Test( 0.0 , 0.0 ,  1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,
			/* Test #18 */ new Test( 0.0 , 0.0 ,  1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,

			/* Test #19 */ new Test( 0.0 , 0.0 , -1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #20 */ new Test( 0.0 , 0.0 , -1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #21 */ new Test( 0.0 , 0.0 , -1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #22 */ new Test( 0.0 , 0.0 , -1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , v0.set( 0.0 , 0.0 ,  0.0 ) ) ,
			/* Test #23 */ new Test( 0.0 , 0.0 , -1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , v0.set( 0.0 , 0.0 ,  0.0 ) ) ,
			/* Test #24 */ new Test( 0.0 , 0.0 , -1.0 ,  0.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,

			/*
			 * Tests with plane: Z = 5
			 */

			/* Test #25 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #26 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #27 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , v0.set( 0.0 , 0.0 ,  5.0 ) ) ,
			/* Test #28 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,
			/* Test #29 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,
			/* Test #30 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,

			/* Test #31 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , true  , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #32 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , true  , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #33 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , true  , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , v0.set( 0.0 , 0.0 ,  5.0 ) ) ,
			/* Test #34 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , true  , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , v0.set( 0.0 , 0.0 ,  5.0 ) ) ,
			/* Test #35 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , true  , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , v0.set( 0.0 , 0.0 ,  5.0 ) ) ,
			/* Test #36 */ new Test( 0.0 , 0.0 ,  1.0 ,  5.0 , true  , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,

			/* Test #37 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #38 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #39 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #40 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , false , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , v0.set( 0.0 , 0.0 ,  5.0 ) ) ,
			/* Test #41 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , false , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , v0.set( 0.0 , 0.0 ,  5.0 ) ) ,
			/* Test #42 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , false , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,

			/* Test #43 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #44 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
			/* Test #45 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , v0.set( 0.0 , 0.0 ,  5.0 ) ) ,
			/* Test #46 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 , -10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , v0.set( 0.0 , 0.0 ,  5.0 ) ) ,
			/* Test #47 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 ,   0.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , v0.set( 0.0 , 0.0 ,  5.0 ) ) ,
			/* Test #48 */ new Test( 0.0 , 0.0 , -1.0 , -5.0 , true  , v0.set( 0.0 , 0.0 ,  10.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,

			/*
			 * Tests with plane: X = 0
			 */

			/* Test #49 */ new Test( 1.0 , 0.0 ,  0.0 ,  0.0 , false , v0.set( 1.0 , 1.0 ,   1.0 ) , v0.set(  1.0 ,  0.0 ,  0.0 ) , null                       ) ,
			/* Test #50 */ new Test( 1.0 , 0.0 ,  0.0 ,  0.0 , false , v0.set( 1.0 , 1.0 ,   1.0 ) , v0.set(  0.0 ,  1.0 ,  0.0 ) , null                       ) ,
			/* Test #51 */ new Test( 1.0 , 0.0 ,  0.0 ,  0.0 , false , v0.set( 1.0 , 1.0 ,   1.0 ) , v0.set(  0.0 ,  0.0 ,  1.0 ) , null                       ) ,
			/* Test #52 */ new Test( 1.0 , 0.0 ,  0.0 ,  0.0 , false , v0.set( 1.0 , 1.0 ,   1.0 ) , v0.set( -1.0 ,  0.0 ,  0.0 ) , v0.set( 0.0 , 1.0 , 1.0 )  ) ,
			/* Test #53 */ new Test( 1.0 , 0.0 ,  0.0 ,  0.0 , false , v0.set( 1.0 , 1.0 ,   1.0 ) , v0.set(  0.0 , -1.0 ,  0.0 ) , null                       ) ,
			/* Test #54 */ new Test( 1.0 , 0.0 ,  0.0 ,  0.0 , false , v0.set( 1.0 , 1.0 ,   1.0 ) , v0.set(  0.0 ,  0.0 , -1.0 ) , null                       ) ,
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			Class expectedException = null;
			if ( ( test._expected instanceof Class ) && Exception.class.isAssignableFrom( (Class)test._expected ) )
				expectedException = (Class)test._expected;

			try
			{
				final Vector3D result = GeometryTools.getIntersectionBetweenRayAndPlane( test._planeNormalX , test._planeNormalY , test._planeNormalZ , test._planeDistance , test._planeTwoSided , test._rayOrigin , test._rayDirection );
				if ( expectedException != null )
					fail( description + " should have thrown exception" );

				assertEquals( description , test._expected , result );
			}
			catch ( Exception e )
			{
				if ( expectedException == null )
				{
					System.err.println( description + " threw unexpected exception: " + e );
					throw e;
				}

				assertEquals( description + " threw wrong exception", expectedException, e.getClass() );
			}
		}
	}

	/**
	 * Test the {@link GeometryTools#getPlaneNormal} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testGetPlaneNormal()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetPlaneNormal()" );

		/*
		 * Define test properties.
		 */
		class Test
		{
			final Vector3D _p1;
			final Vector3D _p2;
			final Vector3D _p3;
			final Vector3D _expected;

			private Test( final Vector3D p1 , final Vector3D p2 , final Vector3D p3 , final double normalX , final double normalY , final double normalZ )
			{
				_p1       = p1;
				_p2       = p2;
				_p3       = p3;
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
			/* Test #1  */ new Test( leftRearBottom   , leftRearTop      , leftFrontTop     , -1.0 ,  0.0 ,  0.0 ) ,
			/* Test #2  */ new Test( rightFrontBottom , rightFrontTop    , rightRearTop     ,  1.0 ,  0.0 ,  0.0 ) ,
			/* Test #3  */ new Test( leftFrontBottom  , leftFrontTop     , rightFrontTop    ,  0.0 , -1.0 ,  0.0 ) ,
			/* Test #4  */ new Test( rightRearBottom  , rightRearTop     , leftRearTop      ,  0.0 ,  1.0 ,  0.0 ) ,
			/* Test #5  */ new Test( leftRearBottom   , leftFrontBottom  , rightFrontBottom ,  0.0 ,  0.0 , -1.0 ) ,
			/* Test #6  */ new Test( leftFrontTop     , leftRearTop      , rightRearTop     ,  0.0 ,  0.0 ,  1.0 ) ,

			/* Test #7  */ new Test( leftRearBottom   , leftRearTop      , rightFrontTop    , -hr2 , -hr2 ,  0.0 ) ,
			/* Test #8  */ new Test( rightRearBottom  , rightRearTop     , leftFrontTop     , -hr2 ,  hr2 ,  0.0 ) ,
			/* Test #9  */ new Test( rightFrontBottom , rightFrontTop    , leftRearTop      ,  hr2 ,  hr2 ,  0.0 ) ,
			/* Test #10 */ new Test( leftFrontBottom  , leftFrontTop     , rightRearTop     ,  hr2 , -hr2 ,  0.0 ) ,

			/* Test #11 */ new Test( rightRearBottom  , leftRearTop      , leftFrontTop     , -hr2 ,  0.0 , -hr2 ) ,
			/* Test #12 */ new Test( leftRearBottom   , rightRearTop     , rightFrontTop    , -hr2 ,  0.0 ,  hr2 ) ,
			/* Test #13 */ new Test( rightFrontBottom , leftFrontTop     , leftRearTop      ,  hr2 ,  0.0 ,  hr2 ) ,
			/* Test #14 */ new Test( leftFrontBottom  , rightFrontTop    , rightRearTop     ,  hr2 ,  0.0 , -hr2 ) ,

			/* Test #15 */ new Test( leftRearBottom   , leftFrontTop     , rightFrontTop    ,  0.0 , -hr2 , -hr2 ) ,
			/* Test #16 */ new Test( leftFrontBottom  , leftRearTop      , rightRearTop     ,  0.0 , -hr2 ,  hr2 ) ,
			/* Test #17 */ new Test( leftFrontTop     , leftRearBottom   , rightRearBottom  ,  0.0 ,  hr2 ,  hr2 ) ,
			/* Test #18 */ new Test( leftRearTop      , leftFrontBottom  , rightFrontBottom ,  0.0 ,  hr2 , -hr2 ) ,
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test   test        = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final Vector3D expected = test._expected;
			final Vector3D actual   = GeometryTools.getPlaneNormal( test._p1 , test._p2 , test._p3 );

			Vector3DTester.assertEquals( description , expected , actual , 0.0001 );
		}
	}

	/**
	 * Test the {@link GeometryTools#isPointInsidePolygon} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testIsPointInsidePolygon()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testIsPointInsidePolygon()" );

		/*
		 * Define test properties.
		 */
		final class Test
		{
			final Polygon3D _polygon;
			final double    _x;
			final double    _y;
			final double    _z;
			final boolean   _expected;

			private Test( final Polygon3D polygon , final double x, final double y, final double z, final boolean expected )
			{
				_polygon  = polygon;
				_x        = x;
				_y        = y;
				_z        = z;
				_expected = expected;
			}
		}

		/*
		 * Define tests to execute.
		 *
		 *             Polygon 1              Polygon 2
		 *
		 * 30  --     1---------2       10   3---------2
		 *           /          |            |         |
		 * 20  --   0           3            |         |
		 *          |           |            |         |
		 * 10  --   7           4            |         |
		 *           \          |            |         |
		 *  0  --     6---------5      -10   0---------1
		 *  ^
		 *  Y       | |         |            |         |
		 *
		 *       X> 0 10       30          -10        10
		 *
		 *                 Z = 5                Z = 5
		 */
		final Polygon3D polygon1 = new Polygon3D()
			{
				private final Vector3D[] _vertices =
				{
					/* 0 */ Vector3D.INIT.set(  0.0 , 20.0 , 5.0 ) ,
					/* 1 */ Vector3D.INIT.set( 10.0 , 30.0 , 5.0 ) ,
					/* 2 */ Vector3D.INIT.set( 30.0 , 30.0 , 5.0 ) ,
					/* 3 */ Vector3D.INIT.set( 30.0 , 20.0 , 5.0 ) ,
					/* 4 */ Vector3D.INIT.set( 30.0 , 10.0 , 5.0 ) ,
					/* 5 */ Vector3D.INIT.set( 30.0 ,  0.0 , 5.0 ) ,
					/* 6 */ Vector3D.INIT.set( 10.0 ,  0.0 , 5.0 ) ,
					/* 7 */ Vector3D.INIT.set(  0.0 , 10.0 , 5.0 ) ,
				};

				private final Vector3D _normal;
				{
					final Vector3D[] vertices = _vertices;
					final Vector3D   y        = vertices[ 0 ].minus( vertices[ 1 ] );
					final Vector3D   x        = vertices[ 2 ].minus( vertices[ 1 ] );
					final Vector3D   cross    = Vector3D.cross( x , y );

					_normal = cross.normalize();
				}

				public int      getVertexCount()        { return _vertices.length; }
				public double   getX( final int index ) { return _vertices[ index ].x; }
				public double   getY( final int index ) { return _vertices[ index ].y; }
				public double   getZ( final int index ) { return _vertices[ index ].z; }
				public double   getDistance()           { return Vector3D.dot( _normal , _vertices[ 0 ] ); }
				public Vector3D getNormal()             { return _normal; }
				public double   getNormalX()            { return _normal.x; }
				public double   getNormalY()            { return _normal.y; }
				public double   getNormalZ()            { return _normal.z; }
				public boolean  isTwoSided()            { return false; }
			};

			final Polygon3D polygon2 = new Polygon3D()
			{
				private final Vector3D[] _vertices =
				{
					/* 0 */ Vector3D.INIT.set( -10.0 , -10.0 , 5.0 ) ,
					/* 1 */ Vector3D.INIT.set(  10.0 , -10.0 , 5.0 ) ,
					/* 2 */ Vector3D.INIT.set(  10.0 ,  10.0 , 5.0 ) ,
					/* 3 */ Vector3D.INIT.set( -10.0 ,  10.0 , 5.0 ) ,
				};

				private final Vector3D _normal = Vector3D.INIT.set( 0.0 , 0.0 , -1.0 );

				public int      getVertexCount()        { return _vertices.length; }
				public double   getX( final int index ) { return _vertices[ index ].x; }
				public double   getY( final int index ) { return _vertices[ index ].y; }
				public double   getZ( final int index ) { return _vertices[ index ].z; }
				public double   getDistance()           { return Vector3D.dot( _normal , _vertices[ 0 ] ); }
				public Vector3D getNormal()             { return _normal; }
				public double   getNormalX()            { return _normal.x; }
				public double   getNormalY()            { return _normal.y; }
				public double   getNormalZ()            { return _normal.z; }
				public boolean  isTwoSided()            { return false; }
			};

		final Test[] tests =
		{
			/* center */

			/* Test #1  */ new Test( polygon1 , 20.0 , 15.0 ,  0.0 , false ) ,
			/* Test #2  */ new Test( polygon1 , 20.0 , 15.0 ,  5.0 , true  ) ,
			/* Test #3  */ new Test( polygon1 , 20.0 , 15.0 , 10.0 , false ) ,

			/* Test #4  */ new Test( polygon2 ,  0.0 ,  0.0 ,  0.0 , false ) ,
			/* Test #5  */ new Test( polygon2 ,  0.0 ,  0.0 ,  5.0 , true  ) ,
			/* Test #6  */ new Test( polygon2 ,  0.0 ,  0.0 , 10.0 , false ) ,

			/* outside */

			/* Test #7  */ new Test( polygon1 ,   0.0 ,   0.0 ,  0.0 , false ) ,
			/* Test #8  */ new Test( polygon1 ,   0.0 ,   0.0 ,  5.0 , false ) ,
			/* Test #9  */ new Test( polygon1 ,   2.5 ,   2.5 ,  5.0 , false ) ,
			/* Test #10 */ new Test( polygon1 ,   0.0 ,  40.0 ,  5.0 , false ) ,
			/* Test #11 */ new Test( polygon1 ,  40.0 ,  20.0 ,  5.0 , false ) ,

			/* Test #12 */ new Test( polygon2 , -10.1 ,   0.0 ,  5.0 , false ) ,
			/* Test #13 */ new Test( polygon2 , -10.1 ,  10.0 ,  5.0 , false ) ,
			/* Test #14 */ new Test( polygon2 , -10.1 ,  10.1 ,  5.0 , false ) ,
			/* Test #15 */ new Test( polygon2 , -10.0 ,  10.1 ,  5.0 , false ) ,
			/* Test #16 */ new Test( polygon2 ,   0.0 ,  10.1 ,  5.0 , false ) ,
			/* Test #17 */ new Test( polygon2 ,  10.0 ,  10.1 ,  5.0 , false ) ,
			/* Test #18 */ new Test( polygon2 ,  10.1 ,  10.1 ,  5.0 , false ) ,
			/* Test #19 */ new Test( polygon2 ,  10.1 ,   0.0 ,  5.0 , false ) ,
			/* Test #20 */ new Test( polygon2 ,  10.1 , -10.0 ,  5.0 , false ) ,
			/* Test #21 */ new Test( polygon2 ,  10.1 , -10.1 ,  5.0 , false ) ,
			/* Test #22 */ new Test( polygon2 ,  10.0 , -10.1 ,  5.0 , false ) ,
			/* Test #23 */ new Test( polygon2 ,   0.0 , -10.1 ,  5.0 , false ) ,
			/* Test #24 */ new Test( polygon2 , -10.0 , -10.1 ,  5.0 , false ) ,
			/* Test #25 */ new Test( polygon2 , -10.1 , -10.1 ,  5.0 , false ) ,
			/* Test #26 */ new Test( polygon2 , -10.1 , -10.0 ,  5.0 , false ) ,

			/* on vertices */

			/* Test #27 */ new Test( polygon1 , polygon1.getX( 0 ) , polygon1.getY( 0 ) , polygon1.getZ( 0 ) , true ) ,
			/* Test #28 */ new Test( polygon1 , polygon1.getX( 1 ) , polygon1.getY( 1 ) , polygon1.getZ( 1 ) , true ) ,
			/* Test #29 */ new Test( polygon1 , polygon1.getX( 2 ) , polygon1.getY( 2 ) , polygon1.getZ( 2 ) , true ) ,
			/* Test #30 */ new Test( polygon1 , polygon1.getX( 3 ) , polygon1.getY( 3 ) , polygon1.getZ( 3 ) , true ) ,
			/* Test #31 */ new Test( polygon1 , polygon1.getX( 4 ) , polygon1.getY( 4 ) , polygon1.getZ( 4 ) , true ) ,
			/* Test #32 */ new Test( polygon1 , polygon1.getX( 5 ) , polygon1.getY( 5 ) , polygon1.getZ( 5 ) , true ) ,
			/* Test #33 */ new Test( polygon1 , polygon1.getX( 6 ) , polygon1.getY( 6 ) , polygon1.getZ( 6 ) , true ) ,
			/* Test #34 */ new Test( polygon1 , polygon1.getX( 7 ) , polygon1.getY( 7 ) , polygon1.getZ( 7 ) , true ) ,
			/* Test #35 */ new Test( polygon2 , polygon2.getX( 0 ) , polygon2.getY( 0 ) , polygon2.getZ( 0 ) , true ) ,
			/* Test #36 */ new Test( polygon2 , polygon2.getX( 1 ) , polygon2.getY( 1 ) , polygon2.getZ( 1 ) , true ) ,
			/* Test #37 */ new Test( polygon2 , polygon2.getX( 2 ) , polygon2.getY( 2 ) , polygon2.getZ( 2 ) , true ) ,
			/* Test #38 */ new Test( polygon2 , polygon2.getX( 3 ) , polygon2.getY( 3 ) , polygon2.getZ( 3 ) , true ) ,

			/* on edges */

			/* Test #39 */ new Test( polygon1 , 0.5 * ( polygon1.getX( 0 ) + polygon1.getX( 1 ) ) , 0.5 * ( polygon1.getY( 0 ) + polygon1.getY( 1 ) ) , 0.5 * ( polygon1.getZ( 0 ) + polygon1.getZ( 1 ) ) , true ) ,
			/* Test #40 */ new Test( polygon1 , 0.5 * ( polygon1.getX( 1 ) + polygon1.getX( 2 ) ) , 0.5 * ( polygon1.getY( 1 ) + polygon1.getY( 2 ) ) , 0.5 * ( polygon1.getZ( 1 ) + polygon1.getZ( 2 ) ) , true ) ,
			/* Test #41 */ new Test( polygon1 , 0.5 * ( polygon1.getX( 2 ) + polygon1.getX( 3 ) ) , 0.5 * ( polygon1.getY( 2 ) + polygon1.getY( 3 ) ) , 0.5 * ( polygon1.getZ( 2 ) + polygon1.getZ( 3 ) ) , true ) ,
			/* Test #42 */ new Test( polygon1 , 0.5 * ( polygon1.getX( 3 ) + polygon1.getX( 4 ) ) , 0.5 * ( polygon1.getY( 3 ) + polygon1.getY( 4 ) ) , 0.5 * ( polygon1.getZ( 3 ) + polygon1.getZ( 4 ) ) , true ) ,
			/* Test #43 */ new Test( polygon1 , 0.5 * ( polygon1.getX( 4 ) + polygon1.getX( 5 ) ) , 0.5 * ( polygon1.getY( 4 ) + polygon1.getY( 5 ) ) , 0.5 * ( polygon1.getZ( 4 ) + polygon1.getZ( 5 ) ) , true ) ,
			/* Test #44 */ new Test( polygon1 , 0.5 * ( polygon1.getX( 5 ) + polygon1.getX( 6 ) ) , 0.5 * ( polygon1.getY( 5 ) + polygon1.getY( 6 ) ) , 0.5 * ( polygon1.getZ( 5 ) + polygon1.getZ( 6 ) ) , true ) ,
			/* Test #45 */ new Test( polygon1 , 0.5 * ( polygon1.getX( 6 ) + polygon1.getX( 7 ) ) , 0.5 * ( polygon1.getY( 6 ) + polygon1.getY( 7 ) ) , 0.5 * ( polygon1.getZ( 6 ) + polygon1.getZ( 7 ) ) , true ) ,
			/* Test #46 */ new Test( polygon1 , 0.5 * ( polygon1.getX( 7 ) + polygon1.getX( 0 ) ) , 0.5 * ( polygon1.getY( 7 ) + polygon1.getY( 0 ) ) , 0.5 * ( polygon1.getZ( 7 ) + polygon1.getZ( 0 ) ) , true ) ,
			/* Test #47 */ new Test( polygon2 , 0.5 * ( polygon2.getX( 0 ) + polygon2.getX( 1 ) ) , 0.5 * ( polygon2.getY( 0 ) + polygon2.getY( 1 ) ) , 0.5 * ( polygon2.getZ( 0 ) + polygon2.getZ( 1 ) ) , true ) ,
			/* Test #48 */ new Test( polygon2 , 0.5 * ( polygon2.getX( 1 ) + polygon2.getX( 2 ) ) , 0.5 * ( polygon2.getY( 1 ) + polygon2.getY( 2 ) ) , 0.5 * ( polygon2.getZ( 1 ) + polygon2.getZ( 2 ) ) , true ) ,
			/* Test #49 */ new Test( polygon2 , 0.5 * ( polygon2.getX( 2 ) + polygon2.getX( 3 ) ) , 0.5 * ( polygon2.getY( 2 ) + polygon2.getY( 3 ) ) , 0.5 * ( polygon2.getZ( 2 ) + polygon2.getZ( 3 ) ) , true ) ,
			/* Test #50 */ new Test( polygon2 , 0.5 * ( polygon2.getX( 3 ) + polygon2.getX( 0 ) ) , 0.5 * ( polygon2.getY( 3 ) + polygon2.getY( 0 ) ) , 0.5 * ( polygon2.getZ( 3 ) + polygon2.getZ( 0 ) ) , true ) ,
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test   test        = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final boolean result = GeometryTools.isPointInsidePolygon( test._polygon , test._x , test._y , test._z );
			assertEquals( description, test._expected, result );
		}
	}
}
