/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2025 Peter S. Heijnen
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
 */
package ab.j3d.view;

import ab.j3d.*;
import ab.j3d.model.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link Projector} class.
 *
 * @author Mart Slot
 * @author Peter S. Heijnen
 */
public class TestProjector
{
	@Test
	public void testProject_Perspective()
	{
		Projector projector = Projector.createInstance( ProjectionPolicy.PERSPECTIVE, 100, 100, 1.0, Scene.M, 0.1, 1000.0, Math.toRadians( 45.0 ), 1.0 );
		assertArrayEquals( new int[] { 0, 100 }, projector.project( new double[] { 41.42, 41.42, 100.0 }, new int[ 2 ], 1 ) );
		assertArrayEquals( new int[] { 0, 0 }, projector.project( new double[] { 41.42, -41.42, 100.0 }, new int[ 2 ], 1 ) );
		assertArrayEquals( new int[] { 100, 100 }, projector.project( new double[] { -41.42, 41.42, 100.0 }, new int[ 2 ], 1 ) );
		assertArrayEquals( new int[] { 100, 0 }, projector.project( new double[] { -41.42, -41.42, 100.0 }, new int[ 2 ], 1 ) );
		assertArrayEquals( new int[] { 50, 50 }, projector.project( new double[] { 0, 0, 100.0 }, new int[ 2 ], 1 ) );
	}

	@Test
	public void testProject_Parallel()
	{
		Projector projector = Projector.createInstance( ProjectionPolicy.PARALLEL, 100, 100, 1.0, Scene.M, 0.1, 1000.0, Math.toRadians( 45.0 ), 1.0 );
		assertArrayEquals( new int[] { 0, 0 }, projector.project( new double[] { -50, 50, 100.0 }, new int[ 2 ], 1 ) );
		assertArrayEquals( new int[] { 0, 100 }, projector.project( new double[] { -50, -50, 100.0 }, new int[ 2 ], 1 ) );
		assertArrayEquals( new int[] { 100, 0 }, projector.project( new double[] { 50, 50, 100.0 }, new int[ 2 ], 1 ) );
		assertArrayEquals( new int[] { 100, 100 }, projector.project( new double[] { 50, -50, 100.0 }, new int[ 2 ], 1 ) );
		assertArrayEquals( new int[] { 50, 50 }, projector.project( new double[] { 0, 0, 100.0 }, new int[ 2 ], 1 ) );
	}

	@Test
	public void testImageToView_Perspective()
	{
		Projector projector = Projector.createInstance( ProjectionPolicy.PERSPECTIVE, 100, 100, 1.0, Scene.M, 0.1, 1000.0, Math.toRadians( 45.0 ), 1.0 );

		Vector3D screen = new Vector3D( 0.0, 100.0, 100.0 );
		Vector3D world = projector.imageToView( screen.x, screen.y, screen.z );
		Vector3D expected = new Vector3D( 41.42, 41.42, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );

		screen = new Vector3D( 0.0, 0.0, 100.0 );
		world = projector.imageToView( screen.x, screen.y, screen.z );
		expected = new Vector3D( 41.42, -41.42, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );

		screen = new Vector3D( 100.0, 100.0, 100.0 );
		world = projector.imageToView( screen.x, screen.y, screen.z );
		expected = new Vector3D( -41.42, 42.42, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );

		screen = new Vector3D( 100.0, 0.0, 100.0 );
		world = projector.imageToView( screen.x, screen.y, screen.z );
		expected = new Vector3D( -41.42, -41.42, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );

		screen = new Vector3D( 50.0, 50.0, 100.0 );
		world = projector.imageToView( screen.x, screen.y, screen.z );
		expected = new Vector3D( 0.0, 0.0, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );
	}

	@Test
	public void testImageToView_Parallel()
	{
		Projector projector = Projector.createInstance( ProjectionPolicy.PARALLEL, 100, 100, 1.0, Scene.M, 0.1, 1000.0, Math.toRadians( 45.0 ), 1.0 );

		Vector3D screen = new Vector3D( 0.0, 0.0, 100.0 );
		Vector3D world = projector.imageToView( screen.x, screen.y, screen.z );
		Vector3D expected = new Vector3D( -50.0, 50.0, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );

		screen = new Vector3D( 0.0, 100.0, 100.0 );
		world = projector.imageToView( screen.x, screen.y, screen.z );
		expected = new Vector3D( -50.0, -50.0, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );

		screen = new Vector3D( 100.0, 0.0, 100.0 );
		world = projector.imageToView( screen.x, screen.y, screen.z );
		expected = new Vector3D( 50.0, 50.0, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );

		screen = new Vector3D( 100.0, 100.0, 100.0 );
		world = projector.imageToView( screen.x, screen.y, screen.z );
		expected = new Vector3D( 50.0, -50.0, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );

		screen = new Vector3D( 50.0, 50.0, 100.0 );
		world = projector.imageToView( screen.x, screen.y, screen.z );
		expected = new Vector3D( 0.0, 0.0, 100.0 );
		System.out.println( "    > testing " + screen );
		assertTrue( "The calculated world coordinates do not match the expected coordinates. Expected: " + expected + "  result: " + world.toString(), world.distanceTo( expected ) < 1.0 );
	}
}
