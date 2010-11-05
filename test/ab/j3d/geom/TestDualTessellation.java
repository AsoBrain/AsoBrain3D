/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import java.util.*;

import ab.j3d.*;
import junit.framework.*;
import org.jetbrains.annotations.*;

/**
 * Unit test for {@link DualTessellation}.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class TestDualTessellation
	extends TestCase
{
	/**
	 * Tests that {@link DualTessellation#addPrimitiveCopy} adds a correct copy
	 * of each kind of primitive in both normal and reverse order.
	 */
	public void testAddPrimitiveCopy()
	{
		final List<TessellationPrimitive> actualPrimitives = new ArrayList<TessellationPrimitive>();
		final TessellationBuilder builder = new TessellationBuilder()
		{
			@Override
			public void addOutline( @NotNull final int[] outline )
			{
			}

			@Override
			public void addPrimitive( @NotNull final TessellationPrimitive primitive )
			{
				actualPrimitives.add( primitive );
			}

			@Override
			public int addVertex( final double x, final double y, final double z )
			{
				return 0;
			}

			@NotNull
			@Override
			public Tessellation getTessellation()
			{
				return new BasicTessellation( Collections.<Vector3D>emptyList(), Collections.<int[]>emptyList(), Collections.<TessellationPrimitive>emptyList() );
			}
		};

		final List<TessellationPrimitive> tests = new ArrayList<TessellationPrimitive>();
		tests.add( new TriangleFan( new int[] { 0, 1, 2 } ) );
		tests.add( new TriangleFan( new int[] { 0, 1, 2, 3 } ) );
		tests.add( new TriangleFan( new int[] { 0, 1, 2, 3, 4 } ) );
		tests.add( new TriangleFan( new int[] { 0, 1, 2, 3, 4, 5 } ) );
		tests.add( new TriangleStrip( new int[] { 0, 1, 2 } ) );
		tests.add( new TriangleStrip( new int[] { 0, 1, 2, 3 } ) );
		tests.add( new TriangleStrip( new int[] { 0, 1, 2, 3, 4 } ) );
		tests.add( new TriangleStrip( new int[] { 0, 1, 2, 3, 4, 5 } ) );
		tests.add( new TriangleList( new int[] { 0, 1, 2 } ) );
		tests.add( new TriangleList( new int[] { 0, 1, 2, 3, 4, 5 } ) );

		final HashMap<Integer, Integer> vertexMap = new HashMap<Integer, Integer>();
		for ( int i = 0 ; i < 6 ; i++ )
		{
			vertexMap.put( Integer.valueOf( i ), Integer.valueOf( i ) );
		}

		for ( final TessellationPrimitive test : tests )
		{
			final List<int[]> expected = new ArrayList<int[]>();
			split( expected, test.getTriangles() );

			for ( int i = 0; i < 2; i++ )
			{
				final boolean reverse = ( i == 1 );

				actualPrimitives.clear();
				DualTessellation.addPrimitiveCopy( builder, test, vertexMap, reverse );

				final List<int[]> actual = new ArrayList<int[]>();
				for ( final TessellationPrimitive actualPrimitive : actualPrimitives )
				{
					split( actual, actualPrimitive.getTriangles() );
				}

				for ( final int[] expectedTriangle : expected )
				{
					assertContainsTriangle( expectedTriangle, actual, reverse );
				}
			}
		}
	}

	/**
	 * Splits the given array of triangle vertices into a list of arrays,
	 * one for each triangle.
	 *
	 * @param   result      List to add the triangles to.
	 * @param   vertices    Array to be split.
	 */
	private static void split( final List<int[]> result, final int[] vertices )
	{
		for ( int i = 0; i < vertices.length; i += 3 )
		{
			result.add( new int[] { vertices[ i ], vertices[ i + 1 ], vertices[ i + 2 ] } );
		}
	}

	/**
	 * Returns whether the given list contains the expected triangle.
	 *
	 * @param   expected    Expected triangle.
	 * @param   actual      Actual triangle.
	 * @param   reverse     <code>true</code> to test the reverse triangle;
	 *                      <code>false</code> to test triangles as-is.
	 */
	private static void assertContainsTriangle( final int[] expected, final List<int[]> actual, final boolean reverse )
	{
		boolean contains = false;
		for ( final int[] actualTriangle : actual )
		{
			if ( reverse ? reverseTriangle( expected, actualTriangle ) : sameTriangle( expected, actualTriangle ) )
			{
				contains = true;
				break;
			}
		}
		assertTrue( ( reverse ? "Reverse of triangle" : "Triangle" ) + " not found: " + Arrays.toString( expected ) + " in " + trianglesToString( actual ), contains );
	}

	/**
	 * Returns a string representation of the given list of triangles.
	 *
	 * @param   triangles   Triangles.
	 *
	 * @return  String representation.
	 */
	private static String trianglesToString( final List<int[]> triangles )
	{
		final StringBuilder result = new StringBuilder();
		result.append( '[' );
		for ( final Iterator<int[]> i = triangles.iterator(); i.hasNext(); )
		{
			result.append( Arrays.toString( i.next() ) );
			if ( i.hasNext() )
			{
				result.append( ',' );
			}
		}
		result.append( ']' );
		return result.toString();
	}

	/**
	 * Returns whether two triangles are the same, taking winding direction into
	 * account but not shift (offset in the array).
	 *
	 * @param   first   First triangle.
	 * @param   second  Second triangle.
	 *
	 * @return  <code>true</code> if the triangles are the same.
	 */
	private static boolean sameTriangle( final int[] first, final int[] second )
	{
		return first[ 0 ] == second[ 0 ] ?  first[ 1 ] == second[ 1 ] && first[ 2 ] == second[ 2 ] :
		       first[ 0 ] == second[ 1 ] ?  first[ 1 ] == second[ 2 ] && first[ 2 ] == second[ 0 ] :
		       first[ 0 ] == second[ 2 ] && first[ 1 ] == second[ 0 ] && first[ 2 ] == second[ 1 ];
	}

	/**
	 * Returns whether two triangles are each other's reverse, taking winding
	 * direction into account but not shift (offset in the array).
	 *
	 * @param   first   First triangle.
	 * @param   second  Second triangle.
	 *
	 * @return  <code>true</code> if the triangles are each other's reverse.
	 */
	private static boolean reverseTriangle( final int[] first, final int[] second )
	{
		return first[ 0 ] == second[ 0 ] ?  first[ 1 ] == second[ 2 ] && first[ 2 ] == second[ 1 ] :
		       first[ 0 ] == second[ 1 ] ?  first[ 1 ] == second[ 0 ] && first[ 2 ] == second[ 2 ] :
		       first[ 0 ] == second[ 2 ] && first[ 1 ] == second[ 1 ] && first[ 2 ] == second[ 0 ];
	}
}
