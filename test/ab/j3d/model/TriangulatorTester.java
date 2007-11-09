/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.model;

import java.awt.geom.GeneralPath;
import java.util.Collection;

import static junit.framework.Assert.*;

/**
 * Provides tools for testing a {@link Triangulator}
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TriangulatorTester
{
	public static void testExtremeShapes( final Triangulator triangulator )
	{
		{
			final GeneralPath emptyPath = new GeneralPath();

			final Triangulation     triangulation = triangulator.triangulate( emptyPath );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			assertTrue( "Expected no triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath point = new GeneralPath();
			point.moveTo( 10.0 , 10.0 );

			final Triangulation     triangulation = triangulator.triangulate( point );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			assertTrue( "Expected no triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath point = new GeneralPath();
			point.moveTo( 10.0 , 10.0 );
			point.closePath();

			final Triangulation     triangulation = triangulator.triangulate( point );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			assertTrue( "Expected no triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath line = new GeneralPath();
			line.moveTo( 10.0 , 10.0 );
			line.lineTo( 20.0 , 30.0 );

			final Triangulation     triangulation = triangulator.triangulate( line );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			assertTrue( "Expected no triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath clockwise = new GeneralPath();
			clockwise.moveTo( 10.0 , 10.0 );
			clockwise.lineTo( 20.0 , 30.0 );
			clockwise.lineTo( 15.0 , 5.0 );
			clockwise.closePath();

			final Triangulation     triangulation = triangulator.triangulate( clockwise );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			assertFalse( "Expected triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath clockwise = new GeneralPath();
			clockwise.moveTo( 10.0 , 10.0 );
			clockwise.lineTo( 20.0 , 30.0 );
			clockwise.lineTo( 15.0 , 5.0 );
			clockwise.closePath();
			clockwise.moveTo( 110.0 , 10.0 );
			clockwise.lineTo( 120.0 , 30.0 );
			clockwise.lineTo( 115.0 , 5.0 );
			clockwise.closePath();

			final Triangulation     triangulation = triangulator.triangulate( clockwise );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			assertFalse( "Expected triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath clockwise = new GeneralPath();
			clockwise.moveTo( 10.0 , 10.0 );
			clockwise.lineTo( 20.0 , 30.0 );
			clockwise.lineTo( 15.0 , 5.0 );
			clockwise.closePath();
			clockwise.moveTo( 110.0 , 10.0 );
			clockwise.lineTo( 120.0 , 30.0 );
			clockwise.lineTo( 115.0 , 5.0 );

			final Triangulation     triangulation = triangulator.triangulate( clockwise );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			assertFalse( "Expected triangles." , triangles.isEmpty() );
		}

		try
		{
			triangulator.triangulate( null );
			fail( "Expected 'NullPointerException'." );
		}
		catch ( NullPointerException e )
		{
			/* success */
		}
	}

	/**
	 * Utility class MUST NOT be instantiated.
	 */
	private TriangulatorTester()
	{
		throw new AssertionError();
	}
}
