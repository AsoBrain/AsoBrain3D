/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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

import java.awt.geom.GeneralPath;
import java.util.Collection;

import junit.framework.Assert;

/**
 * Provides tools for testing a {@link Triangulator}.
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
			Assert.assertTrue( "Expected no triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath point = new GeneralPath();
			point.moveTo( 10.0 , 10.0 );

			final Triangulation     triangulation = triangulator.triangulate( point );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			Assert.assertTrue( "Expected no triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath point = new GeneralPath();
			point.moveTo( 10.0 , 10.0 );
			point.closePath();

			final Triangulation triangulation = triangulator.triangulate( point );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			Assert.assertTrue( "Expected no triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath line = new GeneralPath();
			line.moveTo( 10.0 , 10.0 );
			line.lineTo( 20.0 , 30.0 );

			final Triangulation     triangulation = triangulator.triangulate( line );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			Assert.assertTrue( "Expected no triangles." , triangles.isEmpty() );
		}

		{
			final GeneralPath clockwise = new GeneralPath();
			clockwise.moveTo( 10.0 , 10.0 );
			clockwise.lineTo( 20.0 , 30.0 );
			clockwise.lineTo( 15.0 , 5.0 );
			clockwise.closePath();

			final Triangulation     triangulation = triangulator.triangulate( clockwise );
			final Collection<int[]> triangles     = triangulation.getTriangles();
			Assert.assertFalse( "Expected triangles." , triangles.isEmpty() );
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
			Assert.assertFalse( "Expected triangles." , triangles.isEmpty() );
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
			Assert.assertFalse( "Expected triangles." , triangles.isEmpty() );
		}

		try
		{
			triangulator.triangulate( null );
			Assert.fail( "Expected 'NullPointerException'." );
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
