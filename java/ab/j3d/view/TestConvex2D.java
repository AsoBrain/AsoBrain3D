/*
 * $Id$
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
package ab.j3d.view;

import junit.framework.TestCase;

/**
 * Unit test for the {@link Convex2D} class.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TestConvex2D
	extends TestCase
{
	/**
	 * Tests {@link Convex2D#size()}.
	 *
	 * <p>
	 * Input points:
	 * <pre>
	 *        |
	 *  E     +  B
	 *        |
	 *        +
	 *        |
	 * -+--F--o--C--D-
	 *        |
	 *     A  +
	 *        |
	 * </pre>
	 * Note that points C and F are inside the convex hull.
	 */
	public void testSize()
	{
		final Convex2D convex = new Convex2D( 6 );
		convex.add().setLocation( -1.0, -1.0 ); // A
		convex.add().setLocation( 1.0, 2.0 );   // B
		convex.add().setLocation( 1.0, 0.0 );   // C
		convex.add().setLocation( 2.0, 0.0 );   // D
		convex.add().setLocation( -2.0, 2.0 );  // E
		convex.add().setLocation( -1.0, 0.0 );  // F
		assertEquals( "Unexpected value.", 4, convex.size() );
	}

	/**
	 * Tests {@link Convex2D#area()}.
	 *
	 * <p>
	 * Input points:
	 * <pre>
	 *        |
	 *  E     +  B
	 *        |
	 *        +
	 *        |
	 * -+--F--o--C--D-
	 *        |
	 *     A  +
	 *        |
	 * </pre>
	 * Note that points C and F are inside the convex hull.
	 */
	public void testArea()
	{
		final Convex2D convex = new Convex2D( 6 );
		convex.add().setLocation( -1.0, -1.0 ); // A
		convex.add().setLocation( 1.0, 2.0 );   // B
		convex.add().setLocation( 1.0, 0.0 );   // C
		convex.add().setLocation( 2.0, 0.0 );   // D
		convex.add().setLocation( -2.0, 2.0 );  // E
		convex.add().setLocation( -1.0, 0.0 );  // F
		assertEquals( "Unexpected value.", 8.0, convex.area(), 0.0 );
	}
}
