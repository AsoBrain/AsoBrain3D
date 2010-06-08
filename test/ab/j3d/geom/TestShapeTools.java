/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2009 Peter S. Heijnen
 * Copyright (C) 2009-2009 Numdata BV
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

import java.awt.*;
import java.awt.geom.*;

import ab.j3d.geom.ShapeTools.*;
import junit.framework.*;

/**
 * This class tests the {@link ShapeTools} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class TestShapeTools
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestShapeTools.class.getName();

	/**
	 * Test {@link ShapeTools#getShapeClass} method.
	 */
	public static void testGetShapeClass()
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
			final ShapeClass _expected;

			Test( final ShapeClass expected, final Shape shape )
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
			new Test( ShapeClass.OPEN_PATH   , new Arc2D.Double( 20.0, 10.0, 100.0, 80.0,  45.0, -90.0, Arc2D.OPEN ) ),
			new Test( ShapeClass.CCW_CONVEX  , new Arc2D.Double( 20.0, 10.0, 100.0, 80.0,  45.0, -90.0, Arc2D.PIE ) ),
			new Test( ShapeClass.CCW_CONVEX  , new Arc2D.Double( 20.0, 10.0, 100.0, 80.0,  45.0, -90.0, Arc2D.CHORD ) ),
			new Test( ShapeClass.OPEN_PATH   , new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, -45.0,  90.0, Arc2D.OPEN ) ),
			new Test( ShapeClass.CW_CONVEX   , new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, -45.0,  90.0, Arc2D.PIE ) ),
			new Test( ShapeClass.CW_CONVEX   , new Arc2D.Double( 20.0, 10.0, 100.0, 80.0, -45.0,  90.0, Arc2D.CHORD ) ),
			new Test( ShapeClass.VOID        , new Arc2D.Double( 20.0, 10.0,   0.0,  0.0,  45.0, -90.0, Arc2D.PIE ) ),
			new Test( ShapeClass.VOID        , new Arc2D.Double( 20.0, 10.0, -10.0, -1.0,  45.0, -90.0, Arc2D.PIE ) ),
			new Test( ShapeClass.CCW_CONVEX  , new Arc2D.Double( 20.0, 10.0, 100.0, 80.0,   0.0,   0.0, Arc2D.PIE ) ),
			new Test( ShapeClass.CCW_CONVEX  , new Ellipse2D.Double( 20.0, 10.0, 100.0, 80.0 ) ),
			new Test( ShapeClass.VOID        , new Ellipse2D.Double( 20.0, 10.0, 0.0, 0.0 ) ),
			new Test( ShapeClass.VOID        , new Ellipse2D.Double( 20.0, 10.0, -100.0, 0.0 ) ),
			new Test( ShapeClass.CCW_QUAD    , new Rectangle2D.Double( 20.0, 10.0, 100.0,  80.0 ) ),
			new Test( ShapeClass.VOID        , new Rectangle2D.Double( 20.0, 10.0, 0.0, 0.0 ) ),
			new Test( ShapeClass.VOID        , new Rectangle2D.Double( 20.0, 10.0, 0.0, -80.0 ) ),
			new Test( ShapeClass.LINE_SEGMENT, new Line2D.Double( 20.0, 10.0, 100.0, 80.0 ) ),
			new Test( ShapeClass.VOID        , new Line2D.Double( 20.0, 10.0, 20.0, 10.0 ) ),
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0 ; i < tests.length ; i++ )
		{
			System.out.println( " - Test #" + ( i + 1 ) );

			final Test test = tests[ i ];
			assertEquals( "Unexpected 'getShapeClass( shape.getPathIterator( null ) )' result", test._expected, ShapeTools.getShapeClass( test._shape.getPathIterator( null ) ) );
			assertEquals( "Unexpected 'getShapeClass( shape.getPathIterator( null, 0.5 ) )' result", test._expected,  ShapeTools.getShapeClass( test._shape.getPathIterator( null, 0.5 ) ) );
			assertEquals( "Unexpected 'getShapeClass( shape )' result", test._expected, ShapeTools.getShapeClass( test._shape ) );
		}
	}
}