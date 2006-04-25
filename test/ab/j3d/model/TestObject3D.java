/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2006 Peter S. Heijnen
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

import java.awt.Color;
import java.util.List;

import junit.framework.TestCase;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.geom.BasicRay3D;

import com.numdata.oss.junit.ArrayTester;

/**
 * This class tests the {@link Object3D} class.
 *
 * @see     Object3D
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class TestObject3D
    extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestObject3D.class.getName();

	/**
	 * Test {@link Object3D#addFace(Vector3D[], TextureSpec, boolean, boolean)}
	 * method.
	 */
	public static void testAddFace()
    {
		System.out.println( CLASS_NAME + ".testAddFace" );

		final Vector3D[] vertexCoordinates =
		{
			Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ),
			Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ),
			Vector3D.INIT.set( 1.0 , 0.0 , 1.0 ),
			Vector3D.INIT.set( 1.0 , 0.0 , 0.0 ),
		};

		final Object3D object = new Object3D();
		assertEquals( "[pre] faceCount" , 0 , object.getFaceCount() );
		assertEquals( "[pre] totalVertexCount" , 0 , object.getVertexCount() );

		object.addFace( vertexCoordinates , null , false , false );
		assertEquals( "[post] faceCount" , 1 , object.getFaceCount() );
		assertEquals( "[post] totalVertexCount" , 4 , object.getVertexCount() );

		final Face3D face = object.getFace( 0 );
	    ArrayTester.assertEquals( "face.vertexIndices" , "expected" , "vertexIndices" , new int[] { 0 , 1 , 2 , 3 } , face.getVertexIndices() );
	}

	/**
	 * Test {@link Object3D#getFaceNormals} method.
	 */
	public static void testGetFaceNormals()
	{
		System.out.println( CLASS_NAME + ".testGetFaceNormals" );

		final Vector3D lfb = Vector3D.INIT.set( -1.0 , -1.0 , -1.0 );
		final Vector3D rfb = Vector3D.INIT.set(  1.0 , -1.0 , -1.0 );
		final Vector3D rbb = Vector3D.INIT.set(  1.0 ,  1.0 , -1.0 );
		final Vector3D lbb = Vector3D.INIT.set( -1.0 ,  1.0 , -1.0 );
		final Vector3D lft = Vector3D.INIT.set( -1.0 , -1.0 ,  1.0 );
		final Vector3D rft = Vector3D.INIT.set(  1.0 , -1.0 ,  1.0 );
		final Vector3D rbt = Vector3D.INIT.set(  1.0 ,  1.0 ,  1.0 );
		final Vector3D lbt = Vector3D.INIT.set( -1.0 ,  1.0 ,  1.0 );

		final Object3D cube = new Object3D();
		/* top    */ cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , null , false , false );
		/* bottom */ cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , null , false , false );
		/* front  */ cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , null , false , false );
		/* back   */ cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , null , false , false );
		/* left   */ cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , null , false , false );
		/* right  */ cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , null , false , false );

		final double[] faceNormals = cube.getFaceNormals( null , null );

		assertEquals( "Normal(top).x"    ,  0.0 , faceNormals[  0 ] , 0.001 );
		assertEquals( "Normal(top).y"    ,  0.0 , faceNormals[  1 ] , 0.001 );
		assertEquals( "Normal(top).z"    ,  1.0 , faceNormals[  2 ] , 0.001 );

		assertEquals( "Normal(bottom).x" ,  0.0 , faceNormals[  3 ] , 0.001 );
		assertEquals( "Normal(bottom).y" ,  0.0 , faceNormals[  4 ] , 0.001 );
		assertEquals( "Normal(bottom).z" , -1.0 , faceNormals[  5 ] , 0.001 );

		assertEquals( "Normal(front).x"  ,  0.0 , faceNormals[  6 ] , 0.001 );
		assertEquals( "Normal(front).y"  , -1.0 , faceNormals[  7 ] , 0.001 );
		assertEquals( "Normal(front).z"  ,  0.0 , faceNormals[  8 ] , 0.001 );

		assertEquals( "Normal(back).x"   ,  0.0 , faceNormals[  9 ] , 0.001 );
		assertEquals( "Normal(back).y"   ,  1.0 , faceNormals[ 10 ] , 0.001 );
		assertEquals( "Normal(back).z"   ,  0.0 , faceNormals[ 11 ] , 0.001 );

		assertEquals( "Normal(left).x"   , -1.0 , faceNormals[ 12 ] , 0.001 );
		assertEquals( "Normal(left).y"   ,  0.0 , faceNormals[ 13 ] , 0.001 );
		assertEquals( "Normal(left).z"   ,  0.0 , faceNormals[ 14 ] , 0.001 );

		assertEquals( "Normal(right).x"  ,  1.0 , faceNormals[ 15 ] , 0.001 );
		assertEquals( "Normal(right).y"  ,  0.0 , faceNormals[ 16 ] , 0.001 );
		assertEquals( "Normal(right).z"  ,  0.0 , faceNormals[ 17 ] , 0.001 );
	}

	/**
	 * Test the {@link Object3D#getIntersectionsWithRay} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	public static void testGetIntersectionsWithRay()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetIntersectionsWithRay" );

		final Vector3D v0 = Vector3D.INIT;
		final Vector3D lf = v0.set( -( 100.0 / 2.0 ) , -( 100.0 / 2.0 ) , 0.0 );
		final Vector3D rf = v0.set(    100.0 / 2.0   , -( 100.0 / 2.0 ) , 0.0 );
		final Vector3D rb = v0.set(    100.0 / 2.0   ,    100.0 / 2.0   , 0.0 );
		final Vector3D lb = v0.set( -( 100.0 / 2.0 ) ,    100.0 / 2.0   , 0.0 );

		final TextureSpec red   = new TextureSpec( Color.red   );
		final TextureSpec green = new TextureSpec( Color.green );

		final Object3D twoSidedPlaneOnZ0 = new Object3D();
		twoSidedPlaneOnZ0.setTag( "Plane" );
		twoSidedPlaneOnZ0.addFace( new Vector3D[] { lf , lb , rb , rf } , red   , false , false ); // Z =  size
		twoSidedPlaneOnZ0.addFace( new Vector3D[] { lb , lf , rf , rb } , green , false , false ); // Z = -size

		final Matrix3D transform1 = Matrix3D.getTransform(  90.0 ,  0.0 , 0.0 ,    0.0 ,   0.0 , 0.0 );
		final Matrix3D transform2 = Matrix3D.getTransform(   0.0 , 90.0 , 0.0 ,  150.0 ,   0.0 , 0.0 );

		List selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( 0.0 , 0.0 , -500.0 , 0.0 , 0.0 , 1.0 ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );

		Face3DIntersection intersection      = (Face3DIntersection)selection.get( 0 );
		Object             tag1              = intersection.getObjectID();
		Vector3D           intersectionPoint = intersection.getIntersectionPoint();
		Matrix3D           object2world      = intersection.getObject2world();
		Vector3D           local             = object2world.inverseMultiply( intersectionPoint );

		assertEquals( "The wrong object was intersected" , "Plane" , tag1);
		assertTrue( "The object was not intersected at the right place" , local.almostEquals( 0.0 , 0.0 , 0.0 ) );


		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( -25.0 , -500.0 , -25.0 , 0.0 , 1.0 , 0.0 ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );

		intersection      = (Face3DIntersection)selection.get( 0 );
		tag1              = intersection.getObjectID();
		intersectionPoint = intersection.getIntersectionPoint();
		object2world       = intersection.getObject2world();
		local             = object2world.inverseMultiply( intersectionPoint );

		assertEquals( "The wrong object was intersected" , "Plane" , tag1);
		assertTrue( "The object was not intersected at the right place" , intersectionPoint.almostEquals( -25.0 , 0.0 , -25.0 ) );
		assertTrue( "The object was not intersected at the right place" , local.almostEquals( -25.0 , 25.0 , 0.0 ) );


		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( -25.0 , -50.0 , 25.0 , Math.sqrt( 0.5 ) , Math.sqrt( 0.5 ) , 0.0 ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		tag1 = ( (Face3DIntersection)selection.get( 0 ) ).getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( 50.0 , -500.0 , 50.0 , 0.0 , 1.0 , 0.0 ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		tag1 = ( (Face3DIntersection)selection.get( 0) ).getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( 50.1 , -500.0 , 0.0 , 0.0 , 1.0 , 0.0 ) );
		assertEquals( "Incorrect number of intersections;" , 0 , selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( 0.0 , -500.0 , 50.1 , 0.0 , 1.0 , 0.0 ) );
		assertEquals( "Incorrect number of intersections;" , 0 , selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( 150.0 , -50.0 , 0.0 , 1.0 , 0.0 , 0.0 ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		tag1 = ( (Face3DIntersection)selection.get( 0 ) ).getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( 150.1 , 0.0 , -500.0 , 0.0 , 0.0 , 1.0 ) );
		assertEquals( "Incorrect number of intersections;" , 0 , selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( 150.0 , 50.1 , -500.0 , 0.0 , 0.0 , 1.0 ) );
		assertEquals( "Incorrect number of intersections;" , 0 , selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( 100.0 , 0.0 , 25.0 , 1.0 , 0.0 , 0.0 ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		tag1 = ( (Face3DIntersection)selection.get( 0 ) ).getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( -500.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		tag1 = ( (Face3DIntersection)selection.get( 0 ) ).getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);
	}

	/**
	 * Test {@link Object3D#calculateBoundValue(int, int)} method.
	 */
	public static void testCalculateBoundValue()
	{
		System.out.println( CLASS_NAME + ".testCalculateBoundValue" );

		final int[][] tests =
		{
			{ -20 , 10 ,  20 } ,
			{ -19 , 10 ,  20 } ,
			{ -11 , 10 ,  20 } ,
			{ -10 , 10 ,  10 } ,
			{  -9 , 10 ,  10 } ,
			{  -1 , 10 ,  10 } ,
			{   0 , 10 ,   0 } ,
			{   1 , 10 ,   0 } ,
			{   9 , 10 ,   0 } ,
			{  10 , 10 , -10 } ,
			{  11 , 10 , -10 } ,
			{  19 , 10 , -10 } ,
			{  20 , 10 , -20 } ,
		};

		for ( int i = 0 ; i < tests.length ; i++ )
		{
			final int value  = tests[ i ][ 0 ];
			final int range  = tests[ i ][ 1 ];
			final int result = tests[ i ][ 2 ];

			assertEquals( "getRangeAdjustment( " + value + " , " + range + " )" , result , Object3D.calculateBoundValue( value , range ) );
		}
	}

	/**
	 * Test {@link Object3D#getVertexIndex(double, double, double)} method.
	 */
	public static void testGetVertexIndex()
	{
		System.out.println( CLASS_NAME + ".testGetVertexIndex" );

		final Object3D obj3d = new Object3D();
		assertEquals( "[pre] totalVertexCount" , 0 , obj3d.getVertexCount() );

		assertEquals( "test1 - vertexIndex"      , 0 , obj3d.getVertexIndex( 0.0 , 0.0 , 0.0 ) );
		assertEquals( "test1 - totalVertexCount" , 1 , obj3d.getVertexCount() );

		assertEquals( "test2 - vertexIndex"      , 0 , obj3d.getVertexIndex( 0.0 , 0.0 , 0.0 ) );
		assertEquals( "test2 - totalVertexCount" , 1 , obj3d.getVertexCount() );

		assertEquals( "test3 - vertexIndex"      , 1 , obj3d.getVertexIndex( 1.0 , 0.0 , 0.0 ) );
		assertEquals( "test3 - totalVertexCount" , 2 , obj3d.getVertexCount() );

		assertEquals( "test4 - vertexIndex"      , 2 , obj3d.getVertexIndex( 0.0 , 1.0 , 0.0 ) );
		assertEquals( "test4 - totalVertexCount" , 3 , obj3d.getVertexCount() );

		assertEquals( "test5 - vertexIndex"      , 3 , obj3d.getVertexIndex( 0.0 , 0.0 , 1.0 ) );
		assertEquals( "test5 - totalVertexCount" , 4 , obj3d.getVertexCount() );
	}

	/**
	 * Test {@link Object3D#getVertexNormals} method.
	 */
	public static void testGetVertexNormals()
	{
		System.out.println( CLASS_NAME + ".testGetVertexNormals" );

		final Vector3D lfb = Vector3D.INIT.set( -1.0 , -1.0 , -1.0 );
		final Vector3D rfb = Vector3D.INIT.set(  1.0 , -1.0 , -1.0 );
		final Vector3D rbb = Vector3D.INIT.set(  1.0 ,  1.0 , -1.0 );
		final Vector3D lbb = Vector3D.INIT.set( -1.0 ,  1.0 , -1.0 );
		final Vector3D lft = Vector3D.INIT.set( -1.0 , -1.0 ,  1.0 );
		final Vector3D rft = Vector3D.INIT.set(  1.0 , -1.0 ,  1.0 );
		final Vector3D rbt = Vector3D.INIT.set(  1.0 ,  1.0 ,  1.0 );
		final Vector3D lbt = Vector3D.INIT.set( -1.0 ,  1.0 ,  1.0 );

		final Object3D cube = new Object3D();
		/* top    */ cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , null , false , false );
		/* bottom */ cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , null , false , false );
		/* front  */ cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , null , false , false );
		/* back   */ cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , null , false , false );
		/* left   */ cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , null , false , false );
		/* right  */ cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , null , false , false );

		final double e = Math.sqrt( 3.0 ) / 3.0;

		final double[] vertexNormals = cube.getVertexNormals( null , null );

		assertEquals( "lft.x" , -e , vertexNormals[  0 ] , 0.001 );
		assertEquals( "lft.y" , -e , vertexNormals[  1 ] , 0.001 );
		assertEquals( "lft.z" ,  e , vertexNormals[  2 ] , 0.001 );

		assertEquals( "lbt.x" , -e , vertexNormals[  3 ] , 0.001 );
		assertEquals( "lbt.y" ,  e , vertexNormals[  4 ] , 0.001 );
		assertEquals( "lbt.z" ,  e , vertexNormals[  5 ] , 0.001 );

		assertEquals( "rbt.x" ,  e , vertexNormals[  6 ] , 0.001 );
		assertEquals( "rbt.y" ,  e , vertexNormals[  7 ] , 0.001 );
		assertEquals( "rbt.z" ,  e , vertexNormals[  8 ] , 0.001 );

		assertEquals( "rft.x" ,  e , vertexNormals[  9 ] , 0.001 );
		assertEquals( "rft.y" , -e , vertexNormals[ 10 ] , 0.001 );
		assertEquals( "rft.z" ,  e , vertexNormals[ 11 ] , 0.001 );

		assertEquals( "lbb.x" , -e , vertexNormals[ 12 ] , 0.001 );
		assertEquals( "lbb.y" ,  e , vertexNormals[ 13 ] , 0.001 );
		assertEquals( "lbb.z" , -e , vertexNormals[ 14 ] , 0.001 );

		assertEquals( "lfb.x" , -e , vertexNormals[ 15 ] , 0.001 );
		assertEquals( "lfb.y" , -e , vertexNormals[ 16 ] , 0.001 );
		assertEquals( "lfb.z" , -e , vertexNormals[ 17 ] , 0.001 );

		assertEquals( "rfb.x" ,  e , vertexNormals[ 18 ] , 0.001 );
		assertEquals( "rfb.y" , -e , vertexNormals[ 19 ] , 0.001 );
		assertEquals( "rfb.z" , -e , vertexNormals[ 20 ] , 0.001 );

		assertEquals( "rbb.x" ,  e , vertexNormals[ 21 ] , 0.001 );
		assertEquals( "rbb.y" ,  e , vertexNormals[ 22 ] , 0.001 );
		assertEquals( "rbb.z" , -e , vertexNormals[ 23 ] , 0.001 );
	}
}
