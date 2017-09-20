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

/**
 * A dynamic integer array. Note that this class is meant for efficiency so it
 * actually exposes the internal data array.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class IntArray
{
	/**
	 * The array used for storage.
	 */
	private int[] _data;

	/**
	 * Number of elements in the array.
	 */
	private int _size;

	/**
	 * Constructs empty array with an initial capacity of ten.
	 */
	public IntArray()
	{
		this( 10 );
	}

	/**
	 * Constructs dynamic array based on an existing static array.
	 *
	 * @param   data    Static array to base this dynamic array on.
	 */
	public IntArray( final int[] data )
	{
		this( data, data.length );
	}

	/**
	 * Constructs dynamic array based on an existing static array. The given
	 * size may be less the array capacity (e.g. zero).
	 *
	 * @param   data    Array used for storage.
	 * @param   size    Number of elements in the array.
	 */
	public IntArray( final int[] data, final int size )
	{
		if ( data == null )
		{
			throw new IllegalArgumentException( "data = null" );
		}

		if ( ( size < 0 ) || ( size > data.length ) )
		{
			throw new IllegalArgumentException( "data.length=" + data.length + ", size=" + size );
		}

		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_data = data;
		_size = size;
	}

	/**
	 * Constructs empty array with the given initial capacity.
	 *
	 * @param   initialCapacity     Initial array capacity.
	 */
	public IntArray( final int initialCapacity )
	{
		this( new int[ initialCapacity ], 0 );
	}

	/**
	 * Add all elements from a collection to the end of this array.
	 *
	 * @param   collection  Collection to copy elements from.
	 */
	public void add( final Collection<Integer> collection )
	{
		ensureRemainingCapacity( collection.size() );

		final int[] data = _data;
		int size = _size;
		for ( final Integer element : collection )
		{
			data[ size++ ] = ( element != null ) ? element : 0;
		}
		_size = size;
	}

	/**
	 * Add element to this array.
	 *
	 * @param   element     Element to add.
	 */
	public void add( final int element )
	{
		ensureCapacity( _size + 1 );
		_data[ _size++ ] = element;
	}

	/**
	 * Add all elements from another array to the end of this array.
	 *
	 * @param   array   Array to copy elements from.
	 */
	public void add( final int... array )
	{
		insert( _size, array, 0, array.length );
	}

	/**
	 * Add all elements from another array to the end of this array.
	 *
	 * @param   array   Array to copy elements from.
	 */
	public void add( final IntArray array )
	{
		insert( _size, array.getData(), 0, array.size() );
	}

	/**
	 * Removes all of the elements from this array.
	 */
	public void clear()
	{
		_size = 0;
	}

	/**
	 * Test whether this array contains at least one element with the given
	 * value.
	 *
	 * @param   element     Element value to find.
	 *
	 * @return  {@code true} if this array contains the given value;
	 *          {@code false} if this array does not contain the given value.
	 */
	public boolean contains( final int element )
	{
		return ( indexOf( element ) >= 0 );
	}

	/**
	 * Ensure that this array can accommodate at least the given number of
	 * elements. If necessary, the array capacity is increased.
	 *
	 * @param minCapacity Desired minimum capacity.
	 */
	public void ensureCapacity( final int minCapacity )
	{
		final int[] data = _data;
		final int curCapacity = data.length;
		if ( minCapacity > curCapacity )
		{
			_data = Arrays.copyOf( data, Math.max( minCapacity, ( curCapacity * 3 ) / 2 + 1 ) );
		}
	}

	/**
	 * Make sure that this array can accommodate at least the given number of
	 * added elements.
	 *
	 * @param   minCapacity     Desired minimum remaining capacity.
	 */
	public void ensureRemainingCapacity( final int minCapacity )
	{
		ensureCapacity( _size + minCapacity );
	}

	/**
	 * Ensure that this array has at least the given size. If necessary, the
	 * size of the array is increased.
	 *
	 * @param minimumSize Minimum size of array.
	 * @param initValue Value to use for any added elements.
	 */
	public void ensureSize( final int minimumSize, final int initValue )
	{
		if ( minimumSize > size() )
		{
			setSize( minimumSize, initValue );
		}
	}

	/**
	 * Set the specified range of this array to a given value. The range extends
	 * from index {@code fromIndex}, inclusive, to index {@code toIndex},
	 * exclusive. If necessary, the array size is extended to accommodate the
	 * required number of elements.
	 *
	 * @param   fromIndex   Index of first element to fill (inclusive).
	 * @param   toIndex     Index of the last element to fill (exclusive).
	 * @param   value       Value to fill array with.
	 */
	public void fill( final int fromIndex, final int toIndex, final int value )
	{
		final int size = _size;
		if ( ( fromIndex < 0 ) || ( fromIndex > toIndex ) || ( toIndex > size ) )
		{
			throw new IndexOutOfBoundsException( "fromIndex: " + fromIndex + ", end: " + toIndex + ", size=" + size );
		}

		Arrays.fill( _data, fromIndex, toIndex, value );
	}

	/**
	 * Get element from this array.
	 *
	 * @param   index   Index of element.
	 *
	 * @return  Element.
	 */
	public int get( final int index )
	{
		final int size = _size;
		if ( index >= size )
		{
			throw new IndexOutOfBoundsException( "index: " + index + ", size=" + size );
		}

		return _data[ index ];
	}

	/**
	 * Get underlying array used for storage in this dynamic array. The capacity
	 * of this array is likely not the same as the number of elements that are
	 * stored and the array may be replaced with another array whenever needed.
	 *
	 * @return  Current array used as storage for this dynamic array.
	 */
	public int[] getData()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return _data;
	}

	/**
	 * Get last element from array.
	 *
	 * @return  Last element from array.
	 *
	 * @throws  NoSuchElementException if the array is empty.
	 */
	public int getLast()
	{
		final int size = _size;
		if ( size == 0 )
		{
			throw new NoSuchElementException( "size=0" );
		}

		return _data[ size -1 ];
	}

	/**
	 * Get number of elements in this array.
	 *
	 * @return number of elements in this array.
	 */
	public int getSize()
	{
		return _size;
	}

	/**
	 * Get index of first element with the given value in this array.
	 *
	 * @param   element     Value to find.
	 *
	 * @return  Index of first occurrence of the value in this array;
	 *          -1 if no element with the requested value was found.
	 */
	public int indexOf( final int element )
	{
		int result = -1;

		final int size = _size;
		final int[] data = _data;

		for ( int i = 0; i < size; i++ )
		{
			if ( data[ i ] == element )
			{
				result = i;
				break;
			}
		}

		return result;
	}

	/**
	 * Insert an element into the array.
	 *
	 * @param   index       Index to insert element at.
	 * @param   element     Element to insert.
	 */
	public void insert( final int index, final int element )
	{
		final int oldSize = _size;
		if ( index > oldSize )
		{
			throw new IndexOutOfBoundsException( "index: " + index + ", size=" + oldSize );
		}

		ensureRemainingCapacity( 1 );
		final int[] data = _data;
		if ( index < oldSize )
		{
			System.arraycopy( data, index, data, index + 1, oldSize - index );
		}
		data[ oldSize ] = element;
		_size = oldSize + 1;
	}

	/**
	 * Insert all elements from another array into this array.
	 *
	 * @param   index   Index in this array to insert elements at.
	 * @param   array   Array to copy elements from.
	 */
	public void insert( final int index, final int[] array )
	{
		insert( index, array, 0, array.length );
	}

	/**
	 * Insert elements from another array into this array.
	 *
	 * @param   index   Index in this array to insert elements at.
	 * @param   array   Array to copy data from.
	 * @param   offset  Index of first element in array to copy.
	 * @param   length  Number of elements to insert.
	 */
	public void insert( final int index, final int[] array, final int offset, final int length )
	{
		final int oldSize = _size;
		if ( index > oldSize )
		{
			throw new IndexOutOfBoundsException( "index: " + index + ", size=" + oldSize );
		}

		if ( length > 0 )
		{
			ensureRemainingCapacity( length );

			final int[] data = _data;
			if ( index < oldSize )
			{
				System.arraycopy( data, index, data, index + length, oldSize - index );
			}
			System.arraycopy( array, offset, data, index, length );
			_size = oldSize + length;
		}
	}

	/**
	 * Insert all elements from another array into this array.
	 *
	 * @param   index   Index in this array to insert elements at.
	 * @param   array   Array to copy elements from.
	 */
	public void insert( final int index, final IntArray array )
	{
		insert( index, array.getData(), 0, array.size() );
	}

	/**
	 * Insert elements from another array into this array.
	 *
	 * @param   index   Index in this array to insert elements at.
	 * @param   array   Array to copy data from.
	 * @param   offset  Index of first element in array to copy.
	 * @param   length  Number of elements to insert.
	 */
	public void insert( final int index, final IntArray array, final int offset, final int length )
	{
		insert( index, array.getData(), offset, length );
	}

	/**
	 * Returns {@code true} if this array contains no elements.
	 *
	 * @return {@code true} if this array contains no elements
	 */
	public boolean isEmpty()
	{
		return _size == 0;
	}

	/**
	 * Remove element from this array.
	 *
	 * @param   index   Index of element to remove.
	 *
	 * @return  Element that was removed.
	 */
	public int remove( final int index )
	{
		final int size = _size;
		if ( index >= size )
		{
			throw new IndexOutOfBoundsException( "index: " + index + ", size=" + size );
		}

		final int[] data = _data;
		final int oldValue = data[ index ];

		final int numMoved = _size - index - 1;
		if ( numMoved > 0 )
		{
			System.arraycopy( data, index + 1, data, index, numMoved );
		}

		return oldValue;
	}

	/**
	 * Remove the specified range from this array. The range extends from index
	 * {@code start}, inclusive, to index {@code end}, exclusive.
	 *
	 * @param   start   Index of first element to remove (inclusive).
	 * @param   end     Index of the last element to remove (exclusive).
	 */
	public void remove( final int start, final int end )
	{
		final int size = _size;
		if ( ( start < 0 ) || ( start > end ) || ( end > size ) )
		{
			throw new IndexOutOfBoundsException( "start: " + start + ", end: " + end + ", size=" + size );
		}

		final int numRemoved = end - start;
		if ( numRemoved > 0 )
		{
			if( end < size )
			{
				System.arraycopy( _data, end, _data, start, size - end );
			}
			_size = size - numRemoved;
		}
	}

	/**
	 * Remove last element from array.
	 *
	 * @return  Last element from array.
	 *
	 * @throws  NoSuchElementException if the array is empty.
	 */
	public int removeLast()
	{
		final int newSize = _size - 1;
		if ( newSize < 0 )
		{
			throw new NoSuchElementException( "size=0" );
		}

		_size = newSize;
		return _data[ newSize ];
	}

	/**
	 * Set element in this array.
	 *
	 * @param   index       Index
	 * @param   element     Element to set.
	 *
	 * @return  Previous element at the index.
	 */
	public int set( final int index, final int element )
	{
		final int size = _size;
		if ( index >= size )
		{
			throw new IndexOutOfBoundsException( "index: " + index + ", size=" + size );
		}

		final int oldValue = _data[ index ];
		_data[ index ] = element;
		return oldValue;
	}

	/**
	 * Set array to use for storage in this dynamic array. This also sets the
	 * size of array (number of elements that are stored) to the given array's
	 * length.
	 *
	 * @param   data    Array used for storage.
	 */
	public void setData( final int[] data )
	{
		setData( data, data.length );
	}

	/**
	 * Set array to use for storage in this dynamic array. This also sets the
	 * size of array (number of elements that are stored).
	 *
	 * @param   data    Array used for storage.
	 * @param   size    Number of elements in the array.
	 */
	public void setData( final int[] data, final int size )
	{
		if ( data == null )
		{
			throw new IllegalArgumentException( "data = null" );
		}

		if ( ( size < 0 ) || ( size > data.length ) )
		{
			throw new IllegalArgumentException( "data.length=" + data.length + ", size=" + size );
		}

		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_data = data;
		_size = size;
	}

	/**
	 * Set size of this array. If necessary, the array capacity is extended to
	 * accommodate the required number of elements. Newly added elements are
	 * initialized to given value.
	 *
	 * @param   size        New size of the array.
	 * @param   initValue   Value to use for any added elements.
	 */
	public void setSize( final int size, final int initValue )
	{
		final int oldSize = _size;
		if ( size > oldSize )
		{
			ensureCapacity( size );
			Arrays.fill( _data, oldSize, _data.length, initValue );
		}
		_size = size;
	}

	/**
	 * Get number of elements in this array.
	 *
	 * @return number of elements in this array.
	 */
	public int size()
	{
		return _size;
	}

	/**
	 * Create a copy of the data in this array.
	 *
	 * @return  Copy of data in this array.
	 */
	public int[] toArray()
	{
		return Arrays.copyOf( _data, _size );
	}

	/**
	 * Trims the capacity of this array to the current size. This can be used
	 * to minimize the array storage requirements.
	 */
	public void trimToSize()
	{
		final int[] data = _data;
		final int size = _size;
		if ( size < data.length )
		{
			_data = Arrays.copyOf( data, size );
		}
	}

	@Override
	public String toString()
	{
		final String result;

		final int size = _size;
		if ( size == 0 )
		{
			result = "[]";
		}
		else
		{
			final int[] data = _data;

			final StringBuilder sb = new StringBuilder();
			sb.append( '[' );

			for ( int i = 0; i < size; i++ )
			{
				if ( i > 0 )
				{
					sb.append( ", " );
				}

				sb.append( data[ i ] );
			}

			sb.append( ']' );
			result = sb.toString();
		}

		return result;
	}
}
