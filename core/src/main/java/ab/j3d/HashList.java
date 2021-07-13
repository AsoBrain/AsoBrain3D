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
 * and {@link #indexOf} lookups. Modifications are rather costly, especially
 * adding/removing any element other than the element at the end of the list.
 *
 * @param <E> Element type.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "ClassExtendsConcreteCollection" )
public class HashList<E>
extends ArrayList<E>
{
	/**
	 * Serialize data version.
	 */
	private static final long serialVersionUID = -2142544575701848415L;

	/**
	 * Maps {@link Object#hashCode()} to {@link TreeSet}s with element indices.
	 */
	private final Map<Integer, TreeSet<Integer>> _indexHashmap = new HashMap<>();

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
	public int indexOfOrAdd( @NotNull final E element )
	{
		int result = -1;

		final Map<Integer, TreeSet<Integer>> indexHashmap = _indexHashmap;
		final Integer hashCode = element.hashCode();

		TreeSet<Integer> indices = indexHashmap.get( hashCode );
		if ( indices != null )
		{
			for ( final int index : indices )
			{
				if ( element.equals( get( index ) ) )
				{
					result = index;
					break;
				}
			}
		}

		if ( result < 0 )
		{
			/*
			 * Add index to map.
			 */
			if ( indices == null )
			{
				indices = new TreeSet<>();
				indexHashmap.put( hashCode, indices );
			}

			final int size = size();
			indices.add( size );
			super.add( size, element );
			result = size;
		}

		return result;
	}

	@Override
	public void add( final int index, final E element )
	{
		final Map<Integer, TreeSet<Integer>> indexHashmap = _indexHashmap;

		/*
		 * Increment index of all trailing elements.
		 */
		if ( index < size() )
		{
			for ( final Map.Entry<Integer, TreeSet<Integer>> entry : indexHashmap.entrySet() )
			{
				final TreeSet<Integer> newIndices = new TreeSet<>();

				for ( final Integer i : entry.getValue() )
				{
					if ( i >= index )
					{
						newIndices.add( i + 1 );
					}
					else
					{
						newIndices.add( i );
					}
				}

				entry.setValue( newIndices );
			}
		}

		/*
		 * Add index to map.
		 */
		final Integer hashCode = element.hashCode();

		final TreeSet<Integer> indices = indexHashmap.computeIfAbsent( hashCode, k -> new TreeSet<>() );

		indices.add( index );
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
		_indexHashmap.clear();
	}

	@Override
	public boolean contains( final Object object )
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
			final TreeSet<Integer> indices = _indexHashmap.get( object.hashCode() );
			if ( indices != null )
			{
				for ( final int index : indices )
				{
					if ( object.equals( get( index ) ) )
					{
						result = index;
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

		final TreeSet<Integer> indices = _indexHashmap.get( object.hashCode() );
		if ( indices != null )
		{
			for ( final int index : indices.descendingSet() )
			{
				if ( object.equals( get( index ) ) )
				{
					result = index;
					break;
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
		final E element = super.remove( index );

		final Map<Integer, TreeSet<Integer>> indexHashmap = _indexHashmap;

		/*
		 * Remove index from map.
		 */
		final Integer hashCode = element.hashCode();

		final TreeSet<Integer> indices = indexHashmap.get( hashCode );
		if ( indices.size() > 1 )
		{
			indices.remove( index );
		}
		else
		{
			indexHashmap.remove( hashCode );
		}

		/*
		 * Decrement index of all trailing elements.
		 */
		if ( index < size() )
		{
			for ( final Map.Entry<Integer, TreeSet<Integer>> entry : indexHashmap.entrySet() )
			{
				final TreeSet<Integer> newIndices = new TreeSet<>();

				for ( final Integer i : entry.getValue() )
				{
					if ( i > index )
					{
						newIndices.add( i - 1 );
					}
					else
					{
						newIndices.add( i );
					}
				}

				entry.setValue( newIndices );
			}
		}

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
		boolean result = false;

		int index = 0;
		while ( index < size() )
		{
			final E element = get( index );
			if ( collection.contains( element ) )
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
	public boolean retainAll( final Collection<?> collection )
	{
		boolean result = false;

		int index = 0;
		while ( index < size() )
		{
			final E element = get( index );
			if ( !collection.contains( element ) )
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

		if ( element != oldElement )
		{
			final Map<Integer, TreeSet<Integer>> indexHashmap = _indexHashmap;

			final Integer indexValue = index;

			Integer hashCode = oldElement.hashCode();

			TreeSet<Integer> indices = indexHashmap.get( hashCode );
			if ( indices.size() > 1 )
			{
				indices.remove( indexValue );
			}
			else
			{
				indexHashmap.remove( hashCode );
			}

			hashCode = element.hashCode();

			indices = indexHashmap.computeIfAbsent( hashCode, k -> new TreeSet<>() );

			indices.add( indexValue );
		}

		return super.set( index, element );
	}

	/**
	 * Iterator for {@link HashList}. This extends {@link ListIterator} to
	 * make sure {@link HashList#_indexHashmap} is updated when changes are
	 * made using the iterator's {@link #add}, {@link #set} or
	 * {@link #remove} methods.
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

		public void add( final E element )
		{
			HashList.this.add( _index++, element );
			_lastIndex = -1;
		}

		public boolean hasNext()
		{
			return _index < size();
		}

		public boolean hasPrevious()
		{
			return ( _index > 0 );
		}

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

		public int nextIndex()
		{
			return _index;
		}

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

		public int previousIndex()
		{
			return _index - 1;
		}

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

		public void set( final E value )
		{
			if ( _lastIndex == -1 )
			{
				throw new IllegalStateException( "call next or previous first" );
			}

			HashList.this.set( _lastIndex, value );
		}
	}
}
