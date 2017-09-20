/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d;

import java.util.*;

import junit.framework.*;

/**
 * Test for the {@link IntArray} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestIntArray
	extends TestCase
{
	private void assertData( final IntArray actual, final int... expectedData )
	{
		assertData( actual, expectedData, expectedData.length, -1 );
	}

	private void assertData( final IntArray actual, final int[] expectedData, final int expectedSize, final int expectedCapacity )
	{
		assertNotNull( "No 'actual' array!", actual );
		final int[] actualData = actual.getData();
		assertNotNull( "No 'actual' data!", actualData );
		assertEquals( "Size mistmatch", expectedSize, actual.getSize() );
		if ( expectedCapacity > 0 )
		{
			assertEquals( "Capacity mistmatch", expectedCapacity, actualData.length );
		}
		for ( int i = 0; i < expectedSize; i++ )
		{
			assertEquals( "Element[" + i + "] mismatch", expectedData[ i ], actualData[ i ] );
		}
	}

	/**
	 * Test {@link IntArray#add(Collection)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testAddCollection()
		throws Exception
	{
		final IntArray array = new IntArray();
		array.add( Arrays.asList( Integer.valueOf( 1 ), Integer.valueOf( 2 ), Integer.valueOf( 3 ) ) );
		assertData( array, 1, 2, 3 );
		array.add( new TreeSet<Integer>( Arrays.asList( Integer.valueOf( 6 ), Integer.valueOf( 5 ), Integer.valueOf( 4 ) ) ) );
		assertData( array, 1, 2, 3, 4, 5, 6 );
	}
}
