/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
import java.util.*;

import ab.j3d.awt.*;
import junit.framework.*;

/**
 * Unit test for the {@link Tessellator}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TestTessellator
	extends TestCase
{
	/**
	 * Test some extra shapes to test basic tessellator functionality.
	 *
	 * @throws  Exception if the test fails.
	 */
	public static void testExtremeShapes()
		throws Exception
	{
		{
			final Shape emptyPath = new Path2D.Float();

			final Tessellator tessellator = ShapeTools.createTessellator( emptyPath, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			tessellator.getCounterClockwiseOutlines();

			Assert.assertTrue( "Expected no primitives." , primitives.isEmpty() );
		}

		{
			final Path2D point = new Path2D.Float();
			point.moveTo( 10.0 , 10.0 );

			final Tessellator tessellator = ShapeTools.createTessellator( point, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			tessellator.getCounterClockwiseOutlines();

			Assert.assertTrue( "Expected no primitives." , primitives.isEmpty() );
		}

		{
			final Path2D point = new Path2D.Float();
			point.moveTo( 10.0 , 10.0 );
			point.closePath();

			final Tessellator tessellator = ShapeTools.createTessellator( point, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			tessellator.getCounterClockwiseOutlines();

			Assert.assertTrue( "Expected no primitives." , primitives.isEmpty() );
		}

		{
			final Path2D line = new Path2D.Float();
			line.moveTo( 10.0 , 10.0 );
			line.lineTo( 20.0 , 30.0 );

			final Tessellator tessellator = ShapeTools.createTessellator( line, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			tessellator.getCounterClockwiseOutlines();

			Assert.assertTrue( "Expected no primitives." , primitives.isEmpty() );
		}

		{
			final Path2D clockwise = new Path2D.Float();
			clockwise.moveTo( 10.0 , 10.0 );
			clockwise.lineTo( 20.0 , 30.0 );
			clockwise.lineTo( 15.0 , 5.0 );
			clockwise.closePath();

			final Tessellator tessellator = ShapeTools.createTessellator( clockwise, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			tessellator.getCounterClockwiseOutlines();

			Assert.assertFalse( "Expected primitives." , primitives.isEmpty() );
		}

		{
			final Path2D clockwise = new Path2D.Float();
			clockwise.moveTo( 10.0 , 10.0 );
			clockwise.lineTo( 20.0 , 30.0 );
			clockwise.lineTo( 15.0 , 5.0 );
			clockwise.closePath();
			clockwise.moveTo( 110.0 , 10.0 );
			clockwise.lineTo( 120.0 , 30.0 );
			clockwise.lineTo( 115.0 , 5.0 );
			clockwise.closePath();

			final Tessellator tessellator = ShapeTools.createTessellator( clockwise, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			tessellator.getCounterClockwiseOutlines();

			Assert.assertFalse( "Expected primitives." , primitives.isEmpty() );
		}

		{
			final Path2D clockwise = new Path2D.Float();
			clockwise.moveTo( 10.0 , 10.0 );
			clockwise.lineTo( 20.0 , 30.0 );
			clockwise.lineTo( 15.0 , 5.0 );
			clockwise.closePath();
			clockwise.moveTo( 110.0 , 10.0 );
			clockwise.lineTo( 120.0 , 30.0 );
			clockwise.lineTo( 115.0 , 5.0 );

			final Tessellator tessellator = ShapeTools.createTessellator( clockwise, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			tessellator.getCounterClockwiseOutlines();

			Assert.assertFalse( "Expected primitives." , primitives.isEmpty() );
		}
	}
}
