/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2009 Peter S. Heijnen
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

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.geom.BasicRay3D;

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

		final Material red   = new Material( Color.RED  .getRGB() );
		final Material green = new Material( Color.GREEN.getRGB() );

		final Object3DBuilder builder = new Object3DBuilder();
		builder.addFace( new Vector3D[] { lf , lb , rb , rf } , red   , false , false ); // Z =  size
		builder.addFace( new Vector3D[] { lb , lf , rf , rb } , green , false , false ); // Z = -size
		final Object3D twoSidedPlaneOnZ0 = builder.getObject3D();
		twoSidedPlaneOnZ0.setTag( "Plane" );

		final Matrix3D transform1 = Matrix3D.getTransform(  90.0 ,  0.0 , 0.0 ,    0.0 ,   0.0 , 0.0 );
		final Matrix3D transform2 = Matrix3D.getTransform(   0.0 , 90.0 , 0.0 ,  150.0 ,   0.0 , 0.0 );

		List<Face3DIntersection> selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , Matrix3D.INIT , new BasicRay3D( 0.0 , 0.0 , -500.0 , 0.0 , 0.0 , 1.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );

		Face3DIntersection intersection      = selection.get( 0 );
		Object             tag1              = intersection.getObjectID();
		Vector3D           intersectionPoint = intersection.getIntersectionPoint();
		Matrix3D           object2world      = intersection.getObject2world();
		Vector3D           local             = object2world.inverseTransform( intersectionPoint );

		assertEquals( "The wrong object was intersected" , "Plane" , tag1);
		assertTrue( "The object was not intersected at the right place" , local.almostEquals( 0.0 , 0.0 , 0.0 ) );


		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( -25.0 , -500.0 , -25.0 , 0.0 , 1.0 , 0.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );

		intersection      = selection.get( 0 );
		tag1              = intersection.getObjectID();
		intersectionPoint = intersection.getIntersectionPoint();
		object2world       = intersection.getObject2world();
		local             = object2world.inverseTransform( intersectionPoint );

		assertEquals( "The wrong object was intersected" , "Plane" , tag1);
		assertTrue( "The object was not intersected at the right place" , intersectionPoint.almostEquals( -25.0 , 0.0 , -25.0 ) );
		assertTrue( "The object was not intersected at the right place" , local.almostEquals( -25.0 , 25.0 , 0.0 ) );


		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( -25.0 , -50.0 , 25.0 , Math.sqrt( 0.5 ) , Math.sqrt( 0.5 ) , 0.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( 50.0 , -500.0 , 50.0 , 0.0 , 1.0 , 0.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( 50.1 , -500.0 , 0.0 , 0.0 , 1.0 , 0.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 0 , selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform1 , new BasicRay3D( 0.0 , -500.0 , 50.1 , 0.0 , 1.0 , 0.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 0 , selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( 150.0 , -50.0 , 0.0 , 1.0 , 0.0 , 0.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( 150.1 , 0.0 , -500.0 , 0.0 , 0.0 , 1.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 0 , selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( 150.0 , 50.1 , -500.0 , 0.0 , 0.0 , 1.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 0 , selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( 100.0 , 0.0 , 25.0 , 1.0 , 0.0 , 0.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null , false , "Plane" , transform2 , new BasicRay3D( -500.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , true ) );
		assertEquals( "Incorrect number of intersections;" , 1 , selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected" , "Plane" , tag1);
	}

	/**
	 * Test {@link Object3D#getVertexNormals()} method.
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

		final Object3DBuilder builder = new Object3DBuilder();
		/* top    */ builder.addFace( new Vector3D[] { lft , lbt , rbt , rft } , null , true , false );
		/* bottom */ builder.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , null , true , false );
		/* front  */ builder.addFace( new Vector3D[] { lfb , lft , rft , rfb } , null , true , false );
		/* back   */ builder.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , null , true , false );
		/* left   */ builder.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , null , true , false );
		/* right  */ builder.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , null , true , false );
		final Object3D cube = builder.getObject3D();

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
