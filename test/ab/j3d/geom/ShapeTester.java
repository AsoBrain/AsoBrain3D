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

import com.numdata.oss.junit.*;
import junit.framework.*;

/**
 * Provides methods for testing classes that use {@link Shape}.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class ShapeTester
{
	/**
	 * Asserts that the path iterators for both shapes return the same values.
	 *
	 * @param   message     Failure message (prefix).
	 * @param   expected    Expected shape.
	 * @param   actual      Actual shape.
	 */
	public static void assertEquals( final String message, final Shape expected, final Shape actual )
	{
		final PathIterator expectedIterator = expected.getPathIterator( null );
		final PathIterator actualIterator = actual.getPathIterator( null );

		assertEquals( message, expectedIterator, actualIterator );
	}

	/**
	 * Asserts that the given path iterators return the same values.
	 *
	 * @param   message     Failure message (prefix).
	 * @param   expected    Expected path iterator.
	 * @param   actual      Actual path iterator.
	 */
	public static void assertEquals( final String message, final PathIterator expected, final PathIterator actual )
	{
		Assert.assertEquals( message + ": winding rule", expected.getWindingRule(), actual.getWindingRule() );

		int segmentIndex = 0;
		final double[] expectedCoords = new double[ 6 ];
		final double[] actualCoords = new double[ 6 ];

		while ( !expected.isDone() && !actual.isDone() )
		{
			final int expectedType = expected.currentSegment( expectedCoords );
			final int actualType = actual.currentSegment( actualCoords );

			Assert.assertEquals( message + ": segments[" + segmentIndex + "].type", expectedType, actualType );
			ArrayTester.assertEquals( message + ": segments[" + segmentIndex + "].coords", expectedCoords, actualCoords );

			segmentIndex++;
			expected.next();
			actual.next();
		}

		Assert.assertEquals( message + ": different length", expected.isDone(), actual.isDone() );
	}

	/**
	 * Utility class should not be instantiated.
	 */
	private ShapeTester()
	{
	}
}
