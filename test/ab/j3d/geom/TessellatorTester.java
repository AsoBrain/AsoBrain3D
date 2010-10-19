/* $Id$
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

import java.awt.geom.*;
import java.util.*;

import junit.framework.*;

/**
 * Provides tools for testing a {@link Tessellator}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TessellatorTester
{
	/**
	 * Test some extra shapes to test basic tessellator functionality.
	 *
	 * @param   tessellator  Tessellator to test.
	 */
	public static void testExtremeShapes( final Tessellator tessellator )
	{
		{
			final GeneralPath emptyPath = new GeneralPath();

			final TessellationBuilder tessellationBuilder = new BasicTessellationBuilder();
			tessellator.tessellate( tessellationBuilder, emptyPath );
			final Tessellation tessellation = tessellationBuilder.getTessellation();

			final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();
			Assert.assertTrue( "Expected no primitives." , primitives.isEmpty() );
		}

		{
			final GeneralPath point = new GeneralPath();
			point.moveTo( 10.0 , 10.0 );

			final TessellationBuilder tessellationBuilder = new BasicTessellationBuilder();
			tessellator.tessellate( tessellationBuilder, point );
			final Tessellation tessellation = tessellationBuilder.getTessellation();

			final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();
			Assert.assertTrue( "Expected no primitives." , primitives.isEmpty() );
		}

		{
			final GeneralPath point = new GeneralPath();
			point.moveTo( 10.0 , 10.0 );
			point.closePath();

			final TessellationBuilder tessellationBuilder = new BasicTessellationBuilder();
			tessellator.tessellate( tessellationBuilder, point );
			final Tessellation tessellation = tessellationBuilder.getTessellation();

			final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();
			Assert.assertTrue( "Expected no primitives." , primitives.isEmpty() );
		}

		{
			final GeneralPath line = new GeneralPath();
			line.moveTo( 10.0 , 10.0 );
			line.lineTo( 20.0 , 30.0 );

			final TessellationBuilder tessellationBuilder = new BasicTessellationBuilder();
			tessellator.tessellate( tessellationBuilder, line );
			final Tessellation tessellation = tessellationBuilder.getTessellation();

			final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();
			Assert.assertTrue( "Expected no primitives." , primitives.isEmpty() );
		}

		{
			final GeneralPath clockwise = new GeneralPath();
			clockwise.moveTo( 10.0 , 10.0 );
			clockwise.lineTo( 20.0 , 30.0 );
			clockwise.lineTo( 15.0 , 5.0 );
			clockwise.closePath();

			final TessellationBuilder tessellationBuilder = new BasicTessellationBuilder();
			tessellator.tessellate( tessellationBuilder, clockwise );
			final Tessellation tessellation = tessellationBuilder.getTessellation();

			final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();
			Assert.assertFalse( "Expected primitives." , primitives.isEmpty() );
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

			final TessellationBuilder tessellationBuilder = new BasicTessellationBuilder();
			tessellator.tessellate( tessellationBuilder, clockwise );
			final Tessellation tessellation = tessellationBuilder.getTessellation();

			final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();
			Assert.assertFalse( "Expected primitives." , primitives.isEmpty() );
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

			final TessellationBuilder tessellationBuilder = new BasicTessellationBuilder();
			tessellator.tessellate( tessellationBuilder, clockwise );
			final Tessellation tessellation = tessellationBuilder.getTessellation();

			final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();
			Assert.assertFalse( "Expected primitives." , primitives.isEmpty() );
		}
	}

	/**
	 * Utility class MUST NOT be instantiated.
	 */
	private TessellatorTester()
	{
		throw new AssertionError();
	}
}
