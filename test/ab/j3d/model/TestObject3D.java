/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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

/**
 * This class tests the <code>Object3D</code> class.
 *
 * @see     Object3D
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class TestObject3D
    extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestObject3D.class.getName();

	/**
	 * Test <code>Object3D.addFace()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Object3D#addFace
	 */
	public void testAddFace()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testAddFace" );

		final Vector3D[] points =
		{
			Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ),
			Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ),
			Vector3D.INIT.set( 1.0 , 0.0 , 1.0 ),
			Vector3D.INIT.set( 1.0 , 0.0 , 0.0 ),
		};

		final Object3D obj3d = new Object3D();
		assertEquals( "[pre] faceCount" , 0 , obj3d.getFaceCount() );
		assertEquals( "[pre] totalVertexCount" , 0 , obj3d.getPointCount() );

		obj3d.addFace( points , null , false );
		assertEquals( "[post] faceCount" , 1 , obj3d.getFaceCount() );
		assertEquals( "[post] totalVertexCount" , 4 , obj3d.getPointCount() );

		final Face3D face = obj3d.getFace( 0 );
		final int[] pointIndices = face.getPointIndices();
		assertEquals( "face.pointIndices.length" , 4 , pointIndices.length );
		assertEquals( "face.pointIndices[ 0 ]" , 0 , pointIndices[ 0 ] );
		assertEquals( "face.pointIndices[ 1 ]" , 1 , pointIndices[ 1 ] );
		assertEquals( "face.pointIndices[ 2 ]" , 2 , pointIndices[ 2 ] );
		assertEquals( "face.pointIndices[ 3 ]" , 3 , pointIndices[ 3 ] );
	}

	/**
	 * Test <code>Object3D.getFaceNormals()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Object3D#getFaceNormals
	 */
	public void testGetFaceNormals()
		throws Exception
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
		/* top    */ cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , null , false );
		/* bottom */ cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , null , false );
		/* front  */ cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , null , false );
		/* back   */ cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , null , false );
		/* left   */ cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , null , false );
		/* right  */ cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , null , false );

		final double[] faceNormals = cube.getFaceNormals();

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
	 * Test <code>Object3D.getOrAddPointIndex()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Object3D#getOrAddPointIndex
	 */
	public void testGetOrAddPointIndex()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetOrAddPointIndex" );

		final Object3D obj3d = new Object3D();
		assertEquals( "[pre] totalVertexCount" , 0 , obj3d.getPointCount() );

		assertEquals( "test1 - pointIndex"       , 0 , obj3d.getOrAddPointIndex( 0.0 , 0.0 , 0.0 ) );
		assertEquals( "test1 - totalVertexCount" , 1 , obj3d.getPointCount() );

		assertEquals( "test2 - pointIndex"       , 0 , obj3d.getOrAddPointIndex( 0.0 , 0.0 , 0.0 ) );
		assertEquals( "test2 - totalVertexCount" , 1 , obj3d.getPointCount() );

		assertEquals( "test3 - pointIndex"       , 1 , obj3d.getOrAddPointIndex( 1.0 , 0.0 , 0.0 ) );
		assertEquals( "test3 - totalVertexCount" , 2 , obj3d.getPointCount() );

		assertEquals( "test4 - pointIndex"       , 2 , obj3d.getOrAddPointIndex( 0.0 , 1.0 , 0.0 ) );
		assertEquals( "test4 - totalVertexCount" , 3 , obj3d.getPointCount() );

		assertEquals( "test5 - pointIndex"       , 3 , obj3d.getOrAddPointIndex( 0.0 , 0.0 , 1.0 ) );
		assertEquals( "test5 - totalVertexCount" , 4 , obj3d.getPointCount() );
	}

	/**
	 * Test <code>Object3D.getRangeAdjustment()</code> method.
	 *
	 * @see Object3D#getRangeAdjustment
	 */
	public static void testGetRangeAdjustment()
	{
		System.out.println( CLASS_NAME + ".testGetRangeAdjustment" );

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

			assertEquals( "getRangeAdjustment( " + value + " , " + range + " )" , result , Object3D.getRangeAdjustment( value , range ) );
		}
	}

	/**
	 * Test <code>Object3D.getVertexNormals()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Object3D#getVertexNormals
	 */
	public void testGetVertexNormals()
		throws Exception
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
		/* top    */ cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , null , false );
		/* bottom */ cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , null , false );
		/* front  */ cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , null , false );
		/* back   */ cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , null , false );
		/* left   */ cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , null , false );
		/* right  */ cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , null , false );

		final double e = Math.sqrt( 3.0 ) / 3.0;

		final double[] vertexNormals = cube.getVertexNormals();

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
