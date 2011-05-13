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

import java.awt.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.geom.*;
import junit.framework.*;

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

		final Vector3D lf = new Vector3D( -( 100.0 / 2.0 ), -( 100.0 / 2.0 ), 0.0 );
		final Vector3D rf = new Vector3D(    100.0 / 2.0  , -( 100.0 / 2.0 ), 0.0 );
		final Vector3D rb = new Vector3D(    100.0 / 2.0  ,    100.0 / 2.0  , 0.0 );
		final Vector3D lb = new Vector3D( -( 100.0 / 2.0 ),    100.0 / 2.0  , 0.0 );

		final Material red   = new Material( Color.RED  .getRGB() );
		final Material green = new Material( Color.GREEN.getRGB() );

		final Object3DBuilder builder = new Object3DBuilder();
		builder.addFace( new Vector3D[] { lf, lb, rb, rf }, red  , false, false ); // Z =  size
		builder.addFace( new Vector3D[] { lb, lf, rf, rb }, green, false, false ); // Z = -size
		final Object3D twoSidedPlaneOnZ0 = builder.getObject3D();
		twoSidedPlaneOnZ0.setTag( "Plane" );

		final Matrix3D transform1 = Matrix3D.getTransform(  90.0,  0.0, 0.0,    0.0,   0.0, 0.0 );
		final Matrix3D transform2 = Matrix3D.getTransform(   0.0, 90.0, 0.0,  150.0,   0.0, 0.0 );

		List<Face3DIntersection> selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, Matrix3D.IDENTITY, twoSidedPlaneOnZ0 ), Matrix3D.IDENTITY, new BasicRay3D( 0.0, 0.0, -500.0, 0.0, 0.0, 1.0, true ) );
		assertEquals( "Incorrect number of intersections;", 1, selection.size() );

		Face3DIntersection intersection      = selection.get( 0 );
		Object             tag1              = intersection.getObjectID();
		Vector3D           intersectionPoint = intersection.getIntersectionPoint();
		Matrix3D           object2world      = intersection.getObject2world();
		Vector3D           local             = object2world.inverseTransform( intersectionPoint );

		assertEquals( "The wrong object was intersected", "Plane", tag1);
		assertTrue( "The object was not intersected at the right place", local.almostEquals( 0.0, 0.0, 0.0 ) );


		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform1, twoSidedPlaneOnZ0 ), transform1, new BasicRay3D( -25.0, -500.0, -25.0, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersections;", 1, selection.size() );

		intersection      = selection.get( 0 );
		tag1              = intersection.getObjectID();
		intersectionPoint = intersection.getIntersectionPoint();
		object2world       = intersection.getObject2world();
		local             = object2world.inverseTransform( intersectionPoint );

		assertEquals( "The wrong object was intersected", "Plane", tag1);
		assertTrue( "The object was not intersected at the right place", intersectionPoint.almostEquals( -25.0, 0.0, -25.0 ) );
		assertTrue( "The object was not intersected at the right place", local.almostEquals( -25.0, 25.0, 0.0 ) );


		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform1, twoSidedPlaneOnZ0 ), transform1, new BasicRay3D( -25.0, -50.0, 25.0, Math.sqrt( 0.5 ), Math.sqrt( 0.5 ), 0.0, true ) );
		assertEquals( "Incorrect number of intersections;", 1, selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected", "Plane", tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform1, twoSidedPlaneOnZ0 ), transform1, new BasicRay3D( 50.0, -500.0, 50.0, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersections;", 1, selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected", "Plane", tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform1, twoSidedPlaneOnZ0 ), transform1, new BasicRay3D( 50.1, -500.0, 0.0, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersections;", 0, selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform1, twoSidedPlaneOnZ0 ), transform1, new BasicRay3D( 0.0, -500.0, 50.1, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersections;", 0, selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform1, twoSidedPlaneOnZ0 ), transform2, new BasicRay3D( 150.0, -50.0, 0.0, 1.0, 0.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersections;", 1, selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected", "Plane", tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform2, twoSidedPlaneOnZ0 ), transform2, new BasicRay3D( 150.1, 0.0, -500.0, 0.0, 0.0, 1.0, true ) );
		assertEquals( "Incorrect number of intersections;", 0, selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform2, twoSidedPlaneOnZ0 ), transform2, new BasicRay3D( 150.0, 50.1, -500.0, 0.0, 0.0, 1.0, true ) );
		assertEquals( "Incorrect number of intersections;", 0, selection.size() );

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform2, twoSidedPlaneOnZ0 ), transform2, new BasicRay3D( 100.0, 0.0, 25.0, 1.0, 0.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersections;", 1, selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected", "Plane", tag1);

		selection = twoSidedPlaneOnZ0.getIntersectionsWithRay( null, false, "Plane", new Node3DPath( null, transform2, twoSidedPlaneOnZ0 ), transform2, new BasicRay3D( -500.0, 0.0, 0.0, 1.0, 0.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersections;", 1, selection.size() );
		intersection = selection.get( 0 );
		tag1 = intersection.getObjectID();
		assertEquals( "The wrong object was intersected", "Plane", tag1);
	}
}
