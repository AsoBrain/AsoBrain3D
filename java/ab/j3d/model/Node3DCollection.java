/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
package ab.j3d.model;

import ab.j3d.Matrix3D;

/**
 * This collection class is used to store combinations of Matrix3D
 * and Node3D objects.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Node3DCollection
{
	/**
	 * Number of stored elements.
	 */
	private int _elementCount;

	/**
	 * Array with Matrix3D objects in collection. This size of this
	 * array may exceed the element count, but is never smaller.
	 */
	private Matrix3D[] _matrixData;

	/**
	 * Array with Node3D objects in collection. This size of this
	 * array may exceed the element count, but is never smaller.
	 */
	private Node3D[] _nodeData;

	/**
	 * Default constructor.
	 */
	public Node3DCollection()
	{
		_elementCount = 0;
		_matrixData = new Matrix3D[ 10 ];
		_nodeData = new Node3D[ 10 ];
	}

	/**
	 * Appends the specified node to the end of this collection.
	 *
	 * @param   matrix  Matrix3D associated with node.
	 * @param   node    Node to add.
	 */
	public synchronized void add( final Matrix3D matrix , final Node3D node )
	{
		ensureCapacity( _elementCount + 1 );

		_matrixData[ _elementCount   ] = matrix;
		_nodeData  [ _elementCount++ ] = node;
	}

	/**
	 * Removes all elements and sets the size to zero.
	 */
	public void clear()
	{
		removeAllElements();
	}

	/**
	 * Increases the capacity of this collection, if necessary, to ensure
	 * that it can hold at least the number of components specified by
	 * the minimum capacity argument.
	 *
	 * @param   minCapacity   The desired minimum capacity.
	 */
	public synchronized void ensureCapacity( final int minCapacity )
	{
		final int oldCapacity = _nodeData.length;

		if ( minCapacity > oldCapacity )
		{
			final Node3D[] oldNodes    = _nodeData;
			final Matrix3D[] oldMatrices = _matrixData;

			final int newCapacity = oldCapacity * 2;

		    _nodeData   = new Node3D[ newCapacity ];
		    _matrixData = new Matrix3D[ newCapacity ];

			System.arraycopy( oldNodes    , 0 , _nodeData   , 0 , _elementCount );
			System.arraycopy( oldMatrices , 0 , _matrixData , 0 , _elementCount );
		}
	}

	/**
	 * Get matrix at specified index.
	 *
	 * @param   index   Index of element.
	 *
	 * @return  Matrix3D object at specified index.
	 */
	public synchronized Matrix3D getMatrix( final int index )
	{
		if ( index >= _elementCount )
		    throw new ArrayIndexOutOfBoundsException( index );

		return( _matrixData[ index ] );
	}

	/**
	 * Get node at specified index.
	 *
	 * @param   index   Index of element.
	 *
	 * @return  Node3D object at specified index.
	 */
	public synchronized Node3D getNode( final int index )
	{
		if ( index >= _elementCount )
		    throw new ArrayIndexOutOfBoundsException( index );

		return( _nodeData[ index ] );
	}

	/**
	 * Removes all elements and sets the size to zero.
	 */
	public synchronized void removeAllElements()
	{
		for ( int i = 0 ; i < _elementCount ; i++ )
		{
			_nodeData  [ i ] = null;
			_matrixData[ i ] = null;
		}
		_elementCount = 0;
	}

	/**
	 * Returns the number of elements.
	 *
	 * @return  The number of elements.
	 */
	public synchronized int size()
	{
		return _elementCount;
	}
}
