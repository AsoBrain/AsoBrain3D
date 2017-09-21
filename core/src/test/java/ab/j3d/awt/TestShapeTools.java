/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
package ab.j3d.awt;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.geom.*;
import ab.j3d.geom.tessellator.*;
import junit.framework.*;

/**
 * This class tests the {@link ShapeTools} class.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class TestShapeTools
extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestShapeTools.class.getName();

	/**
	 * Test {@link ShapeTools#getShapeClass} method.
	 */
	public void testGetShapeClass()
	{
		System.out.println( CLASS_NAME + ".testGetShapeClass" );

		/**
		 * Defines a test.
		 *
		 * @noinspection JavaDoc
		 */
		class Test
		{
			final Shape _shape;

			final Contour.ShapeClass _expected;

			Test( final Contour.ShapeClass expected, final Shape shape )
			{
				_shape = shape;
				_expected = expected;
			}
		}

		/*
		 * Tests to execute
		 */
		final Test[] tests =
		{
		new Test( Contour.ShapeClass.OPEN_PATH, new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, 45.0, -90.0, Arc2D.OPEN ) ),
		new Test( Contour.ShapeClass.CCW_CONVEX, new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, 45.0, -90.0, Arc2D.PIE ) ),
		new Test( Contour.ShapeClass.CCW_CONVEX, new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, 45.0, -90.0, Arc2D.CHORD ) ),
		new Test( Contour.ShapeClass.OPEN_PATH, new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, -45.0, 90.0, Arc2D.OPEN ) ),
		new Test( Contour.ShapeClass.CW_CONVEX, new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, -45.0, 90.0, Arc2D.PIE ) ),
		new Test( Contour.ShapeClass.CW_CONVEX, new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, -45.0, 90.0, Arc2D.CHORD ) ),
		new Test( Contour.ShapeClass.VOID, new Arc2D.Double( 20.0, 10.0, 0.0, 0.0, 45.0, -90.0, Arc2D.PIE ) ),
		new Test( Contour.ShapeClass.VOID, new Arc2D.Double( 20.0, 10.0, -10.0, -1.0, 45.0, -90.0, Arc2D.PIE ) ),
		new Test( Contour.ShapeClass.CCW_CONVEX, new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, 0.0, 0.0, Arc2D.PIE ) ),
		new Test( Contour.ShapeClass.CCW_CONVEX, new Ellipse2D.Double( 20.0, 10.0, 100.0, 80.0 ) ),
		new Test( Contour.ShapeClass.VOID, new Ellipse2D.Double( 20.0, 10.0, 0.0, 0.0 ) ),
		new Test( Contour.ShapeClass.VOID, new Ellipse2D.Double( 20.0, 10.0, -100.0, 0.0 ) ),
		new Test( Contour.ShapeClass.CCW_QUAD, new Rectangle2D.Double( 20.0, 10.0, 100.0, 80.0 ) ),
		new Test( Contour.ShapeClass.VOID, new Rectangle2D.Double( 20.0, 10.0, 0.0, 0.0 ) ),
		new Test( Contour.ShapeClass.VOID, new Rectangle2D.Double( 20.0, 10.0, 0.0, -80.0 ) ),
		new Test( Contour.ShapeClass.LINE_SEGMENT, new Line2D.Double( 20.0, 10.0, 100.0, 80.0 ) ),
		new Test( Contour.ShapeClass.VOID, new Line2D.Double( 20.0, 10.0, 20.0, 10.0 ) ),
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			System.out.println( " - Test #" + ( i + 1 ) + ": shape=" + test._shape );

			assertEquals( "Unexpected 'getShapeClass( shape.getPathIterator( null ) )' result", test._expected, ShapeTools.getShapeClass( test._shape.getPathIterator( null ) ) );
			assertEquals( "Unexpected 'getShapeClass( shape.getPathIterator( null, 0.5 ) )' result", test._expected, ShapeTools.getShapeClass( test._shape.getPathIterator( null, 0.5 ) ) );
			assertEquals( "Unexpected 'getShapeClass( shape )' result", test._expected, ShapeTools.getShapeClass( test._shape ) );
		}
	}

	/**
	 * Test {@link ShapeTools#addPathToMesh(Mesh, PathIterator)} method.
	 */
	public void testAddPathToMesh()
	{
		System.out.println( CLASS_NAME + ".testAddPathToMesh" );

		System.out.println( " - Test #1 - Line" );
		{
			final Shape shape = new Line2D.Double( -10.0, -5.0, 10.0, 5.0 );

			final List<Vector2D> expected = Collections.emptyList();

			assertAddPathToMesh( shape, expected );
		}

		System.out.println( " - Test #2 - Rectangle" );
		{
			final Shape shape = new Rectangle2D.Double( -10.0, -5.0, 20.0, 10.0 );

			final List<Vector2D> expected = Arrays.asList(
			new Vector2D( -10.0, -5.0 ),
			new Vector2D( 10.0, -5.0 ),
			new Vector2D( 10.0, 5.0 ),
			new Vector2D( -10.0, 5.0 ) );

			assertAddPathToMesh( shape, expected );
		}

		System.out.println( " - Test #3 - Tail bites collinear head" );
		{
			/*
			 *  shape                 expected contour
			 *
			 *  L------L/C/M----L     3---------------0
			 *  |               |     |               |
			 *  L---------------L     2---------------1
			 */
			final Path2D.Double shape = new Path2D.Double();
			shape.moveTo( 5.0, 5.0 );
			shape.lineTo( 10.0, 5.0 );
			shape.lineTo( 10.0, -5.0 );
			shape.lineTo( -10.0, -5.0 );
			shape.lineTo( -10.0, 5.0 );
			shape.lineTo( 5.0, 5.0 );
			shape.closePath();

			final List<Vector2D> expected = Arrays.asList(
			new Vector2D( 10.0, 5.0 ),
			new Vector2D( 10.0, -5.0 ),
			new Vector2D( -10.0, -5.0 ),
			new Vector2D( -10.0, 5.0 ) );

			assertAddPathToMesh( shape, expected );
		}

		System.out.println( " - Test #4 - Triangle" );
		{
			/*
			 *  shape          expected contour
			 *
			 *  M              1
			 *  |              |\
			 *  |              | \
			 *  |  + Close     |  \
			 *  |              |   \
			 *  |              |    \
			 *  L-----L        2-----0
			 */
			final Path2D.Double shape = new Path2D.Double();
			shape.moveTo( -10.0, 20.0 );
			shape.lineTo( -10.0, -20.0 );
			shape.lineTo( 10.0, -20.0 );
			shape.closePath();

			final List<Vector2D> expected = Arrays.asList(
			new Vector2D( 10.0, -20.0 ),
			new Vector2D( -10.0, 20.0 ),
			new Vector2D( -10.0, -20.0 ) );

			assertAddPathToMesh( shape, expected );
		}

		System.out.println( " - Test #5 - Rectangle with closed gate" );
		{
			/*
			 *  shape                 expected contour
			 *
			 *  L----L  C  M----L     3---------------0
			 *  |               |     |               |
			 *  L---------------L     2---------------1
			 */
			final Path2D.Double shape = new Path2D.Double();
			shape.moveTo( 5.0, 5.0 );
			shape.lineTo( 10.0, 5.0 );
			shape.lineTo( 10.0, -5.0 );
			shape.lineTo( -10.0, -5.0 );
			shape.lineTo( -10.0, 5.0 );
			shape.lineTo( -5.0, 5.0 );
			shape.closePath();

			final List<Vector2D> expected = Arrays.asList(
			new Vector2D( 10.0, 5.0 ),
			new Vector2D( 10.0, -5.0 ),
			new Vector2D( -10.0, -5.0 ),
			new Vector2D( -10.0, 5.0 ) );

			assertAddPathToMesh( shape, expected );
		}

		System.out.println( " - Test #6 - Rectangle with common corner" );
		{
			/*
			 *  shape                 expected contour
			 *
			 *  L---------------L     2---------------1
			 *  |               |     |               |
			 *  L/C/M-----------L     3---------------0
			 */
			final Path2D.Double shape = new Path2D.Double();
			shape.moveTo( -10.0, -5.0 );
			shape.lineTo( 10.0, -5.0 );
			shape.lineTo( 10.0, 5.0 );
			shape.lineTo( -10.0, 5.0 );
			shape.lineTo( -10.0, -5.0 );
			shape.closePath();

			final List<Vector2D> expected = Arrays.asList(
			new Vector2D( -10.0, -5.0 ),
			new Vector2D( 10.0, -5.0 ),
			new Vector2D( 10.0, 5.0 ),
			new Vector2D( -10.0, 5.0 ) );

			assertAddPathToMesh( shape, expected );
		}

		System.out.println( " - Test #6 - Rectangle with common corner" );
		{
			/*
			 *  shape                 expected contour
			 *
			 *  L---------------L     2---------------1
			 *  |               |     |               |
			 *  L/C/M-----------L     3---------------0
			 */
			final Path2D.Double shape = new Path2D.Double();
			shape.moveTo( -10.0, -5.0 );
			shape.lineTo( 10.0, -5.0 );
			shape.lineTo( 10.0, 5.0 );
			shape.lineTo( -10.0, 5.0 );
			shape.lineTo( -10.0, -5.0 );
			shape.closePath();

			final List<Vector2D> expected = Arrays.asList(
			new Vector2D( -10.0, -5.0 ),
			new Vector2D( 10.0, -5.0 ),
			new Vector2D( 10.0, 5.0 ),
			new Vector2D( -10.0, 5.0 ) );

			assertAddPathToMesh( shape, expected );
		}

	}

	/**
	 * Helper-method for {@link #testAddPathToMesh()} to assert that the actual
	 * result is the expected result.
	 *
	 * @param shape    Shape to
	 * @param expected Expected contour vertices.
	 */
	private static void assertAddPathToMesh( final Shape shape, final List<Vector2D> expected )
	{
		final Collection<Vector2D> actual = new ArrayList<Vector2D>();

		final Mesh mesh = new Mesh( Mesh.WindingRule.ODD )
		{
			@Override
			public void addVertex( final Vector2D location )
			{
				actual.add( location );
				super.addVertex( location );
			}
		};

		ShapeTools.addPathToMesh( mesh, shape.getPathIterator( null, 0.1 ) );

		assertEquals( "Shape: " + shape + "\nExpected:" + expected + "\nActual: " + actual + "\n\n", expected, actual );
	}

}