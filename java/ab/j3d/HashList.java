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
package ab.j3d;

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Adds index hashing to a {@link ArrayList} to provide fast {@link #contains}
 * and {@link #indexOf} lookups. Modifications are rather costly, especially
 * adding/removing any element other than the element at the end of the list.
 *
 * @param   <E>     Element type.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
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
	private final Map<Integer,TreeSet<Integer>> _indexHashmap = new HashMap<Integer,TreeSet<Integer>>();

	/**
	 * Construct empty list.
	 */
	public HashList()
	{
	}

	/**
	 * Construct list will all contents from the specified collection.
	 *
	 * @param   collection  Collection with initial contents.
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
	 * get index of <code>element</code> in list if it already exists or add it
	 * to the end of the list, and return the index of the <code>element</code>
	 * in the list.
	 *
	 * @param   element  Element to get index of or add to end of the list.
	 *
	 * @return  Index of object in list.
	 */
	public int indexOfOrAdd( @NotNull final E element )
	{
		int result = -1;

		final Map<Integer,TreeSet<Integer>> indexHashmap = _indexHashmap;
		final Integer hashCode = Integer.valueOf( element.hashCode() );

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
				indices = new TreeSet<Integer>();
				indexHashmap.put( hashCode, indices );
			}

			final int size = size();
			indices.add( Integer.valueOf( size ) );
			super.add( size, element );
			result = size;
		}

		return result;
	}

	@Override
	public void add( final int index, final E element )
	{
		final Map<Integer,TreeSet<Integer>> indexHashmap = _indexHashmap;

		/*
		 * Increment index of all trailing elements.
		 */
		if ( index < size() )
		{
			for ( final Map.Entry<Integer,TreeSet<Integer>> entry : indexHashmap.entrySet() )
			{
				final TreeSet<Integer> newIndices = new TreeSet<Integer>();

				for ( final Integer i : entry.getValue() )
				{
					if ( i >= index )
					{
						newIndices.add( Integer.valueOf( i + 1 ) );
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
		final Integer hashCode = Integer.valueOf( element.hashCode() );

		TreeSet<Integer> indices = indexHashmap.get( hashCode );
		if ( indices == null )
		{
			indices = new TreeSet<Integer>();
			indexHashmap.put( hashCode, indices );
		}

		indices.add( Integer.valueOf( index ) );
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
	public int indexOf( final Object object )
	{
		int result = -1;

		final TreeSet<Integer> indices = _indexHashmap.get( Integer.valueOf( object.hashCode() ) );
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

		return result;
	}

	@Override
	public int lastIndexOf( final Object object )
	{
		int result = -1;

		final TreeSet<Integer> indices = _indexHashmap.get( Integer.valueOf( object.hashCode() ) );
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
	public ListIterator<E> listIterator()
	{
		return new HashListIterator();
	}

	@Override
	public E remove( final int index )
	{
		final E element = super.remove( index );

		final Map<Integer,TreeSet<Integer>> indexHashmap = _indexHashmap;

		/*
		 * Remove index from map.
		 */
		final Integer hashCode = Integer.valueOf( element.hashCode() );

		final TreeSet<Integer> indices = indexHashmap.get( hashCode );
		if ( indices.size() > 1 )
		{
			indices.remove( Integer.valueOf( index ) );
		}
		else
		{
			indexHashmap.remove( hashCode );
		}

		/*
		 * Decrement index of all trailing elements.
		 */
		if ( index < size() - 1 )
		{
			for ( final Map.Entry<Integer,TreeSet<Integer>> entry : indexHashmap.entrySet() )
			{
				final TreeSet<Integer> newIndices = new TreeSet<Integer>();

				for ( final Integer i : entry.getValue() )
				{
					if ( i > index )
					{
						newIndices.add( Integer.valueOf( i - 1 ) );
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

		for ( final Object element : collection )
		{
			if ( remove( element ) )
			{
				result = true;
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
		for ( int toRemove = toIndex - fromIndex ; toRemove > 0 ; toRemove-- )
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
			final Map<Integer,TreeSet<Integer>> indexHashmap = _indexHashmap;

			final Integer indexValue = Integer.valueOf( index );

			Integer hashCode = Integer.valueOf( oldElement.hashCode() );

			TreeSet<Integer> indices = indexHashmap.get( hashCode );
			if ( indices.size() > 1 )
			{
				indices.remove( indexValue );
			}
			else
			{
				indexHashmap.remove( hashCode );
			}

			hashCode = Integer.valueOf( element.hashCode() );

			indices = indexHashmap.get( hashCode );
			if ( indices == null )
			{
				indices = new TreeSet<Integer>();
				indexHashmap.put( hashCode, indices );
			}

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
		int _index = -1;

		/**
		 * Flag to indicate that the current element can be removed.
		 */
		boolean _removable = false;

		/**
		 * Flag to indicate that the current element was removed.
		 */
		boolean _removed = false;

		public void add( final E element )
		{
			HashList.this.add( _index++, element );
			_removable = false;
		}

		public boolean hasNext()
		{
			return ( _index < size() - 1 );
		}

		public boolean hasPrevious()
		{
			return ( _index > 0 );
		}

		public E next()
		{
			if ( !hasNext() )
			{
				throw new NoSuchElementException();
			}

			int index = _index;
			if ( !_removed )
			{
				_index = ++index;
			}

			_removable = true;
			_removed = false;

			return get( index );
		}

		public int nextIndex()
		{
			return ( _removed ? _index : ( _index + 1 ) );
		}

		public E previous()
		{
			if ( !hasPrevious() )
			{
				throw new NoSuchElementException();
			}

			_removable = true;
			_removed = false;

			return get( --_index );
		}

		public int previousIndex()
		{
			return hasPrevious() ? ( _index - 1 ) : -1;
		}

		public void remove()
		{
			if ( !_removable )
			{
				throw new IllegalStateException( "can't remove twice" );
			}

			_removable = false;
			_removed = true;

			HashList.this.remove( _index );
		}

		public void set( final E value )
		{
			HashList.this.set( _index, value );
		}
	}
}
