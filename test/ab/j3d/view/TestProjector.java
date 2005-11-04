/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
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

		Projector projector = Projector.createInstance( Projector.PERSPECTIVE , 100, 100, 1.0, ViewModel.M, 0.1, 1000, Math.toRadians( 45), 1.0);

		Vector3D screen = Vector3D.INIT.set( 0, 100, 100);
		Vector3D world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		Vector3D expected = Vector3D.INIT.set( 41.42, 41.42, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 0, 0, 100);
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 41.42, -41.42, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 100, 100, 100);
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( -41.42, 42.42, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 100, 0, 100);
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( -41.42, -41.42, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 50, 50, 100);
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 0, 0, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.almostEquals( expected ));



		System.out.println( CLASS_NAME + ".ParallelProjector.testScreenToWorld()" );

		projector = Projector.createInstance( Projector.PARALLEL , 100, 100, 1.0, ViewModel.M, 0.1, 1000, Math.toRadians( 45), 1.0);

		screen = Vector3D.INIT.set( 0, 0, 100);
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( -1, 1, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 0, 100, 100);
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( -1, -1, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 100, 0, 100);
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 1, 1, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 100, 100, 100);
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 1, -1, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.distanceTo(  expected ) < 1.0);

		screen = Vector3D.INIT.set( 50, 50, 100);
		world = projector.screenToWorld( (int)screen.x , (int)screen.y, screen.z );
		expected = Vector3D.INIT.set( 0, 0, 100);
		System.out.println( "\nTesting " + screen.toString() );
		assertTrue( "The expected world coordinates are not correct. Expected: " + expected.toString() + "  result: " + world.toString() , world.almostEquals( expected ));

	}
}
