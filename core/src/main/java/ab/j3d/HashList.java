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

import org.jetbrains.annotations.*;

/**
 * Adds index hashing to a {@link ArrayList} to provide fast {@link #contains}
 * and {@link #indexOf} lookups. Modifications are rather costly, except for
 * adding an element to the end of the list.
 *
 * <p>This collection does not support {@code null} elements.
 *
 * @param <E> Element type.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders
 */
@SuppressWarnings( { "ClassExtendsConcreteCollection", "AccessingNonPublicFieldOfAnotherObject" } )
public class HashList<E>
extends ArrayList<E>
{
	/**
	 * Serialize data version.
	 */
	private static final long serialVersionUID = 8621248016512642421L;

	/**
	 * Maps {@link Object#hashCode()} to {@link HashSet}s with element indices.
	 */
	private final Map<Integer, List<Entry<E>>> _indexHashMap = new HashMap<>();

	/**
	 * Construct empty list.
	 */
	public HashList()
	{
	}

	/**
	 * Construct list will all contents from the specified collection.
	 *
	 * @param collection Collection with initial contents.
	 */
	public HashList( final Collection<? extends E> collection )
	{
		addAll( collection );
	}

	@Override
	public boolean add( final E element )
	{
		add( size(), element );
		return true;
	}

	/**
	 * Convenience method to implement common application of {@link HashList}:
	 * get index of {@code element} in list if it already exists or add it
	 * to the end of the list, and return the index of the {@code element}
	 * in the list.
	 *
	 * @param element Element to get index of or add to end of the list.
	 *
	 * @return Index of object in list.
	 */
	public int indexOfOrAdd( final @NotNull E element )
	{
		final Entry<E> entry = getOrAddEntry( element );
		int result = entry._index;

		if ( result < 0 )
		{
			result = size();
			entry._index = result;
			entry._lastIndex = result;
			super.add( result, element );
		}

		return result;
	}

	@Override
	public void add( final int index, final E element )
	{
		if ( element == null )
		{
			throw new NullPointerException( "null is not allowed" );
		}

		/*
		 * Increment index of all trailing elements.
		 */
		if ( index < size() )
		{
			incrementIndices( index );
		}

		/*
		 * Add index to map.
		 */
		final Entry<E> entry = getOrAddEntry( element );
		entry.add( index );

		super.add( index, element );
	}

	@Override
	public boolean addAll( final Collection<? extends E> collection )
	{
		for ( final E element : collection )
		{
			add( element );
		}

		return !collection.isEmpty();
	}

	@Override
	public boolean addAll( final int index, final Collection<? extends E> collection )
	{
		int i = index;

		for ( final E element : collection )
		{
			add( i++, element );
		}

		return !collection.isEmpty();
	}

	@Override
	public void clear()
	{
		super.clear();
		_indexHashMap.clear();
	}

	@Override
	public boolean contains( final @Nullable Object object )
	{
		return ( indexOf( object ) >= 0 );
	}

	@Override
	public boolean containsAll( final Collection<?> collection )
	{
		boolean result = true;

		for ( final Object element : collection )
		{
			if ( !contains( element ) )
			{
				result = false;
				break;
			}
		}

		return result;
	}

	@Override
	public int indexOf( final @Nullable Object object )
	{
		int result = -1;

		if ( object != null )
		{
			final List<Entry<E>> entries = _indexHashMap.get( object.hashCode() );
			if ( entries != null )
			{
				for ( final Entry<E> entry : entries )
				{
					if ( object.equals( entry._element ) )
					{
						result = entry._index;
						break;
					}
				}
			}
		}

		return result;
	}

	@Override
	public int lastIndexOf( final Object object )
	{
		int result = -1;

		if ( object != null )
		{
			final List<Entry<E>> entries = _indexHashMap.get( object.hashCode() );
			if ( entries != null )
			{
				for ( final Entry<E> entry : entries )
				{
					if ( object.equals( entry._element ) )
					{
						result = entry._lastIndex;
						break;
					}
				}
			}
		}

		return result;
	}

	@Override
	public @NotNull ListIterator<E> listIterator()
	{
		return new HashListIterator();
	}

	@Override
	public E remove( final int index )
	{
		final E element = get( index );

		removeFromEntry( index, element );

		/*
		 * Decrement index of all trailing elements.
		 */
		if ( index < size() - 1 )
		{
			decrementIndices( index );
		}

		super.remove( index );

		return element;
	}

	@Override
	public boolean remove( final Object object )
	{
		final boolean result;

		final int index = indexOf( object );
		if ( index >= 0 )
		{
			remove( index );
			result = true;
		}
		else
		{
			result = false;
		}

		return result;
	}

	@Override
	public boolean removeAll( final Collection<?> collection )
	{
		return removeOrRetainAll( collection, true );
	}

	@Override
	public boolean retainAll( final Collection<?> collection )
	{
		return removeOrRetainAll( collection, false );
	}

	/**
	 * Performs a 'remove all' or 'retain all' operation.
	 *
	 * @param collection Elements to remove/retain.
	 * @param remove     {@code true} to remove; {@code false} to retain.
	 *
	 * @return {@code true} if the list changed.
	 */
	private boolean removeOrRetainAll( final @Nullable Collection<?> collection, final boolean remove )
	{
		if ( collection == null )
		{
			throw new NullPointerException( "collection is null" );
		}

		boolean result = false;

		int index = 0;
		while ( index < size() )
		{
			final E element = get( index );
			if ( collection.contains( element ) == remove )
			{
				remove( index );
				result = true;
			}
			else
			{
				index++;
			}
		}

		return result;
	}

	@Override
	protected void removeRange( final int fromIndex, final int toIndex )
	{
		for ( int toRemove = toIndex - fromIndex; toRemove > 0; toRemove-- )
		{
			remove( fromIndex );
		}
	}

	@Override
	public E set( final int index, final E element )
	{
		final E oldElement = get( index );

		if ( !element.equals( oldElement ) )
		{
			removeFromEntry( index, oldElement );
			getOrAddEntry( element ).add( index );
		}

		return super.set( index, element );
	}

	/**
	 * Iterator for {@link HashList}. This extends {@link ListIterator} to
	 * make sure {@link HashList#_indexHashMap} is updated when changes are
	 * made using the {@link #add}, {@link #set} or {@link #remove} methods.
	 */
	private class HashListIterator
	implements ListIterator<E>
	{
		/**
		 * Index of current element.
		 */
		int _index = 0;

		/**
		 * Index that can be removed with {@link #remove()}.
		 */
		int _lastIndex = -1;

		@Override
		public void add( final E element )
		{
			HashList.this.add( _index++, element );
			_lastIndex = -1;
		}

		@Override
		public boolean hasNext()
		{
			return _index < size();
		}

		@Override
		public boolean hasPrevious()
		{
			return ( _index > 0 );
		}

		@Override
		public E next()
		{
			if ( !hasNext() )
			{
				//noinspection NewExceptionWithoutArguments
				throw new NoSuchElementException();
			}

			_lastIndex = _index;
			return get( _index++ );
		}

		@Override
		public int nextIndex()
		{
			return _index;
		}

		@Override
		public E previous()
		{
			if ( !hasPrevious() )
			{
				//noinspection NewExceptionWithoutArguments
				throw new NoSuchElementException();
			}

			final int index = --_index;
			_lastIndex = index;

			return get( index );
		}

		@Override
		public int previousIndex()
		{
			return _index - 1;
		}

		@Override
		public void remove()
		{
			if ( _lastIndex == -1 )
			{
				throw new IllegalStateException( "call next or previous first" );
			}

			HashList.this.remove( _lastIndex );
			_index = _lastIndex;
			_lastIndex = -1;
		}

		@Override
		public void set( final E value )
		{
			if ( _lastIndex == -1 )
			{
				throw new IllegalStateException( "call next or previous first" );
			}

			HashList.this.set( _lastIndex, value );
		}
	}

	/**
	 * Returns the entry for the given element or {@code null} if not found.
	 *
	 * @param element Element to find.
	 * @param entries Entries to search in.
	 *
	 * @return Entry for the element; {@code null} if not found.
	 */
	private @Nullable Entry<E> findEntry( final @NotNull E element, final @NotNull List<Entry<E>> entries )
	{
		Entry<E> result = null;
		for ( final Entry<E> index : entries )
		{
			if ( element.equals( index._element ) )
			{
				result = index;
				break;
			}
		}
		return result;
	}

	/**
	 * Returns the entry for the given element, creating a new entry if needed.
	 * If a new entry is created, its index is initially {@code -1}.
	 *
	 * @param element Element to get an entry for.
	 *
	 * @return Entry for the element.
	 */
	private @NotNull Entry<E> getOrAddEntry( final @NotNull E element )
	{
		final Integer hashCode = element.hashCode();
		final List<Entry<E>> entries = _indexHashMap.computeIfAbsent( hashCode, k -> new ArrayList<>() );
		Entry<E> entry = findEntry( element, entries );
		if ( entry == null )
		{
			entry = new Entry<>( element );
			entries.add( entry );
		}
		return entry;
	}

	/**
	 * Updates the entry for the given element, because the element was removed
	 * at the specified index.
	 *
	 * @param index   Index where the element was removed.
	 * @param element Removed element.
	 */
	private void removeFromEntry( final int index, final @NotNull E element )
	{
		/*
		 * Remove index from map.
		 */
		final Integer hashCode = element.hashCode();

		final List<Entry<E>> entries = _indexHashMap.get( hashCode );
		final Entry<E> entry = findEntry( element, entries );
		assert entry != null : "There must be an entry for every index.";

		if ( entry._index == index )
		{
			if ( entry._lastIndex == index )
			{
				// Only occurrence of this element. Remove the entry.
				entries.remove( entry );

				// Clean up the hash map if needed.
				if ( entries.isEmpty() )
				{
					_indexHashMap.remove( hashCode );
				}
			}
			else
			{
				// This is the first occurrence. Find the next index.
				int newIndex = -1;
				for ( int i = index + 1, n = entry._lastIndex; i <= n; i++ )
				{
					if ( element.equals( get( i ) ) )
					{
						newIndex = i;
						break;
					}
				}
				assert newIndex != -1 : "Element must at least be present at last index.";
				entry._index = newIndex;// + ( decrementFix ? 1 : 0 ); // In case the index will be decremented during the same operation.
			}
		}
		else if ( entry._lastIndex == index )
		{
			// This is the last occurrence. Find the previous index.
			int newLastIndex = -1;
			for ( int i = index - 1, n = entry._index; i >= n; i-- )
			{
				if ( element.equals( get( i ) ) )
				{
					newLastIndex = i;
					break;
				}
			}
			assert newLastIndex != -1 : "Element must at least be present at last index.";
			entry._lastIndex = newLastIndex;
		}
	}

	/**
	 * Increments element indices starting with the specified index.
	 *
	 * @param index Start index.
	 */
	private void incrementIndices( final int index )
	{
		for ( final Map.Entry<Integer, List<Entry<E>>> hashEntry : _indexHashMap.entrySet() )
		{
			for ( final Entry<E> entry : hashEntry.getValue() )
			{
				if ( entry._index >= index )
				{
					entry._index++;
					entry._lastIndex++;
				}
				else if ( entry._lastIndex >= index )
				{
					entry._lastIndex++;
				}
			}
		}
	}

	/**
	 * Decrements element indices starting with the specified index.
	 *
	 * @param index Start index.
	 */
	private void decrementIndices( final int index )
	{
		for ( final Map.Entry<Integer, List<Entry<E>>> hashEntry : _indexHashMap.entrySet() )
		{
			for ( final Entry<E> entry : hashEntry.getValue() )
			{
				if ( entry._index >= index )
				{
					entry._index--;
					entry._lastIndex--;
				}
				else if ( entry._lastIndex >= index )
				{
					entry._lastIndex--;
				}
			}
		}
	}

	/**
	 * Keeps track of the first and last index of one distinct element.
	 *
	 * @param <E> Element type.
	 */
	private static class Entry<E>
	{
		/**
		 * Element represented by this entry.
		 */
		private final @NotNull E _element;

		/**
		 * First index in the list.
		 */
		private int _index;

		/**
		 * Last index in the list.
		 */
		private int _lastIndex;

		/**
		 * Constructs a new instance.
		 *
		 * @param element Element to track first and last index for.
		 */
		private Entry( final @NotNull E element )
		{
			_element = element;
			_index = -1;
			_lastIndex = -1;
		}

		/**
		 * Updates the entry when the element is added at the specified index.
		 *
		 * @param index Index where the element is added.
		 */
		public void add( final int index )
		{
			assert index >= 0;
			if ( ( _index == -1 ) || ( index < _index ) )
			{
				_index = index;
			}
			if ( ( _lastIndex == -1 ) || ( index > _lastIndex ) )
			{
				_lastIndex = index;
			}
		}
	}
}
