/*
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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

import ab.j3d.Vector3D;

/**
 * This class tests the {@link Projector} class.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 * @see Projector
 */
public class TestProjector
extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestProjector.class.getName();

	/**
	 * Setup text fixture.
	 *
	 * @throws Exception if there was a problem setting up the fixture.
	 */
	public void setUp()
	throws Exception
	{
		super.setUp();
	}

	/**
	 * Tear down test fixture.
	 *
	 * @throws Exception if there was a problem tearing down the fixture.
	 */
	public void tearDown()
	throws Exception
	{
		super.tearDown();
	}

	/**
	 * Test the {@link Projector#screenToWorld} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testScreenToWorld()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".PerspectiveProjector.testScreenToWorld()" );

		Projector projector = Projector.createInstance( Projector.PERSPECTIVE , 100, 100, 1.0, ViewModel.M, 0.1, 1000.0, Math.toRadians( 45.0 ), 1.0);

		Vector3D screen = Vector3D.INIT.set( 0.0, 100.0, 100.0 );
		Vector3D world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		Vector3D expected = Vector3D.INIT.set( 41.42, 41.42, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 0.0, 0.0, 100.0 );
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 41.42, -41.42, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 100.0, 100.0, 100.0 );
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( -41.42, 42.42, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 100.0, 0.0, 100.0 );
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( -41.42, -41.42, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 50.0, 50.0, 100.0 );
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 0.0, 0.0, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);



		System.out.println( CLASS_NAME + ".ParallelProjector.testScreenToWorld()" );

		projector = Projector.createInstance( Projector.PARALLEL , 100, 100, 1.0, ViewModel.M, 0.1, 1000.0, Math.toRadians( 45.0 ), 1.0);

		screen = Vector3D.INIT.set( 0.0, 0.0, 100.0 );
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( -1.0, 1.0, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 0.0, 100.0, 100.0 );
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( -1.0, -1.0, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 100.0, 0.0, 100.0 );
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 1.0, 1.0, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 100.0, 100.0, 100.0 );
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 1.0, -1.0, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 50.0, 50.0, 100.0 );
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 0.0, 0.0, 100.0 );
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

	}
}
