/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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

package ab.j3d;

import java.util.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import org.jetbrains.annotations.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test or {@link HashList}.
 *
 * @author Gerrit Meinders
 */
public class TestHashList
{
	private @NotNull HashList<String> _list;

	@Before
	public void setUp()
	{
		_list = new HashList<>();
	}

	@SuppressWarnings( "ConstantConditions" )
	@After
	public void tearDown()
	{
		_list = null;
	}

	@Test
	public void testAdd()
	{
		final List<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( 1, "c" );
		list.add( 2, "a" );
		assertEquals( "Unexpected elements.", asList( "a", "c", "a", "b" ), list );
		assertThrows( NullPointerException.class, () -> list.add( null ) );
	}

	@Test
	public void testAddAll()
	{
		final List<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.addAll( asList( "b", "c" ) );
		assertEquals( "Unexpected elements.", asList( "a", "b", "b", "c" ), list );
	}

	@Test
	public void testClear()
	{
		final List<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		list.clear();
		assertEquals( "Unexpected elements.", emptyList(), list );
	}

	@Test
	public void testContains()
	{
		final List<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		assertTrue( "Unexpected result.", list.contains( "a" ) );
		assertTrue( "Unexpected result.", list.contains( "b" ) );
		assertTrue( "Unexpected result.", list.contains( "c" ) );
		assertFalse( "Unexpected result.", list.contains( "d" ) );
		assertFalse( "Unexpected result.", list.contains( null ) );
	}

	@Test
	public void testContainsAll()
	{
		final List<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		assertTrue( "Unexpected result.", list.containsAll( asList( "c", "b", "a" ) ) );
		assertTrue( "Unexpected result.", list.containsAll( asList( "a", "c", "c" ) ) );
		assertFalse( "Unexpected result.", list.containsAll( asList( "a", "c", "d" ) ) );
	}

	@Test
	public void testIndexOf()
	{
		final List<String> list = _list;

		list.add( "a" );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "b" ) );
		list.add( "b" );
		assertEquals( "Unexpected index.", 1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "c" ) );
		list.add( "c" );
		assertEquals( "Unexpected index.", 2, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "d" ) );
		list.add( "b" );
		assertEquals( "Unexpected index.", 1, list.indexOf( "b" ) );
		list.add( "a" );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		list.add( "c" );
		assertEquals( "Unexpected elements.", asList( "a", "b", "c", "b", "a", "c" ), list );
		assertEquals( "Unexpected index.", 2, list.indexOf( "c" ) );

		list.add( 1, "z" );
		assertEquals( "Unexpected elements.", asList( "a", "z", "b", "c", "b", "a", "c" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", 2, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 3, list.indexOf( "c" ) );

		list.remove( 1 );
		assertEquals( "Unexpected elements.", asList( "a", "b", "c", "b", "a", "c" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 2, list.indexOf( "c" ) );

		list.remove( 0 );
		list.remove( 0 );
		assertEquals( "Unexpected elements.", asList( "c", "b", "a", "c" ), list );
		assertEquals( "Unexpected index.", 2, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 0, list.indexOf( "c" ) );
	}

	@Test
	public void testLastIndexOf()
	{
		final List<String> list = _list;

		list.add( "a" );
		assertEquals( "Unexpected index.", 0, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "b" ) );
		list.add( "b" );
		assertEquals( "Unexpected index.", 1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "c" ) );
		list.add( "c" );
		assertEquals( "Unexpected index.", 2, list.lastIndexOf( "c" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "d" ) );
		list.add( "b" );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "b" ) );
		list.add( "a" );
		assertEquals( "Unexpected index.", 4, list.lastIndexOf( "a" ) );
		list.add( "c" );
		assertEquals( "Unexpected elements.", asList( "a", "b", "c", "b", "a", "c" ), list );
		assertEquals( "Unexpected index.", 5, list.lastIndexOf( "c" ) );

		list.add( 4, "z" );
		assertEquals( "Unexpected elements.", asList( "a", "b", "c", "b", "z", "a", "c" ), list );
		assertEquals( "Unexpected index.", 5, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 6, list.lastIndexOf( "c" ) );

		list.remove( 4 );
		assertEquals( "Unexpected elements.", asList( "a", "b", "c", "b", "a", "c" ), list );
		assertEquals( "Unexpected index.", 4, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 5, list.lastIndexOf( "c" ) );

		list.remove( 5 );
		list.remove( 4 );
		assertEquals( "Unexpected elements.", asList( "a", "b", "c", "b" ), list );
		assertEquals( "Unexpected index.", 0, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 2, list.lastIndexOf( "c" ) );
	}

	@Test
	public void testListIterator()
	{

	}

	@Test
	public void testRemove()
	{
		final List<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );

		assertTrue( "Unexpected result.", list.remove( "b" ) );
		assertEquals( "Unexpected elements.", asList( "a", "c", "a", "b", "c" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", 3, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 2, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 4, list.lastIndexOf( "c" ) );

		assertTrue( "Unexpected result.", list.remove( "b" ) );
		assertEquals( "Unexpected elements.", asList( "a", "c", "a", "c" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 2, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "c" ) );

		assertFalse( "Unexpected result.", list.remove( "b" ) );
		assertEquals( "Unexpected elements.", asList( "a", "c", "a", "c" ), list );

		assertTrue( "Unexpected result.", list.remove( "a" ) );
		assertEquals( "Unexpected elements.", asList( "c", "a", "c" ), list );
		assertEquals( "Unexpected index.", 1, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 0, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 1, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 2, list.lastIndexOf( "c" ) );

		assertTrue( "Unexpected result.", list.remove( "c" ) );
		assertEquals( "Unexpected elements.", asList( "a", "c" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 0, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 1, list.lastIndexOf( "c" ) );

		assertTrue( "Unexpected result.", list.remove( "c" ) );
		assertEquals( "Unexpected elements.", singletonList( "a" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 0, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "c" ) );

		assertFalse( "Unexpected result.", list.remove( "c" ) );
		assertEquals( "Unexpected elements.", singletonList( "a" ), list );

		assertTrue( "Unexpected result.", list.remove( "a" ) );
		assertEquals( "Unexpected elements.", emptyList(), list );
		assertEquals( "Unexpected index.", -1, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "c" ) );

		assertFalse( "Unexpected result.", list.remove( "a" ) );
	}

	@Test
	public void testRemoveAll()
	{
		final List<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );

		assertFalse( "Unexpected result.", list.removeAll( singletonList( "d" ) ) );
		assertEquals( "Unexpected elements.", asList( "a", "b", "c", "a", "b", "c" ), list );

		assertTrue( "Unexpected result.", list.removeAll( asList( "a", "c" ) ) );
		assertEquals( "Unexpected elements.", asList( "b", "b" ), list );
		assertEquals( "Unexpected index.", -1, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", 0, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", 1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "c" ) );
	}

	@Test
	public void testRetainAll()
	{
		final List<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );

		assertTrue( "Unexpected result.", list.retainAll( asList( "a", "c" ) ) );
		assertEquals( "Unexpected elements.", asList( "a", "c", "a", "c" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 2, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "c" ) );

		assertFalse( "Unexpected result.", list.retainAll( asList( "a", "c" ) ) );
	}

	@Test
	public void testRemoveRange()
	{
		final HashList<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );

		list.removeRange( 2, 4 );
		assertEquals( "Unexpected elements.", asList( "a", "b", "b", "c" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 3, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 0, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", 2, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "c" ) );
	}

	@Test
	public void testSet()
	{
		final List<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );

		list.set( 2, "d" );
		assertEquals( "Unexpected elements.", asList( "a", "b", "d", "a", "b", "c" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 5, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 2, list.indexOf( "d" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", 4, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 5, list.lastIndexOf( "c" ) );
		assertEquals( "Unexpected index.", 2, list.lastIndexOf( "d" ) );

		list.set( 4, "d" );
		assertEquals( "Unexpected elements.", asList( "a", "b", "d", "a", "d", "c" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", 5, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 2, list.indexOf( "d" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", 1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", 5, list.lastIndexOf( "c" ) );
		assertEquals( "Unexpected index.", 4, list.lastIndexOf( "d" ) );

		list.set( 5, "d" );
		assertEquals( "Unexpected elements.", asList( "a", "b", "d", "a", "d", "d" ), list );
		assertEquals( "Unexpected index.", 0, list.indexOf( "a" ) );
		assertEquals( "Unexpected index.", 1, list.indexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.indexOf( "c" ) );
		assertEquals( "Unexpected index.", 2, list.indexOf( "d" ) );
		assertEquals( "Unexpected index.", 3, list.lastIndexOf( "a" ) );
		assertEquals( "Unexpected index.", 1, list.lastIndexOf( "b" ) );
		assertEquals( "Unexpected index.", -1, list.lastIndexOf( "c" ) );
		assertEquals( "Unexpected index.", 5, list.lastIndexOf( "d" ) );
	}

	@Test
	public void testIndexOfOrAdd()
	{
		final HashList<String> list = _list;
		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		list.add( "a" );
		list.add( "b" );
		assertEquals( "Unexpected index.", 0, list.indexOfOrAdd( "a" ) );
		assertEquals( "Unexpected index.", 1, list.indexOfOrAdd( "b" ) );
		assertEquals( "Unexpected index.", 2, list.indexOfOrAdd( "c" ) );
		assertEquals( "Unexpected index.", 5, list.indexOfOrAdd( "d" ) );
		assertEquals( "Unexpected elements.", asList( "a", "b", "c", "a", "b", "d" ), list );
	}
}
