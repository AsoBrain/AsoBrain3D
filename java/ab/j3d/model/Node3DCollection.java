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

import com.numdata.oss.ArrayTools;

/**
 * This collection is used to store combinations of <code>Matrix3D</code> and
 * <code>Node3D</code> objects.
 *
 * @see     Node3D#gatherLeafs
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Node3DCollection
{
	/**
	 * Increment size for arrays.
	 *
	 * @see     ArrayTools#ensureLength
	 */
	public static final int INCREMENT_SIZE = 10;

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
		_matrixData   = null;
		_nodeData     = null;
	}

	/**
	 * Appends the specified node to the end of this collection.
	 *
	 * @param   matrix  Matrix3D associated with node.
	 * @param   node    Node to add.
	 */
	public synchronized void add( final Matrix3D matrix , final Node3D node )
	{
		ArrayTools.append( _matrixData , Matrix3D.class , _elementCount   , INCREMENT_SIZE , matrix );
		ArrayTools.append( _matrixData , Node3D  .class , _elementCount++ , INCREMENT_SIZE , node );
	}

	/**
	 * Removes all elements and sets the size to zero.
	 */
	public void clear()
	{
		final int elementCount = _elementCount;
		if ( elementCount > 0 )
		{
			ArrayTools.clear( _nodeData   , 0 , elementCount );
			ArrayTools.clear( _matrixData , 0 , elementCount );
			_elementCount = 0;
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
		return (Matrix3D)ArrayTools.get( _matrixData , _elementCount , index );
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
		return (Node3D)ArrayTools.get( _nodeData , _elementCount , index );
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
