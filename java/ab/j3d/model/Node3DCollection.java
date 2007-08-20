/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2007 Peter S. Heijnen
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

import java.util.ArrayList;
import java.util.List;

import ab.j3d.Matrix3D;

/**
 * This collection is used to store combinations of {@link Matrix3D} and
 * {@link Node3D} objects.
 *
 * @see     Node3D#collectNodes
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Node3DCollection<T extends Node3D>
{
	/**
	 * List of {@link Matrix3D} objects in collection.
	 */
	private final List<Matrix3D> _matrices = new ArrayList<Matrix3D>();

	/**
	 * List of nodes in collection.
	 */
	private final List<T> _nodes = new ArrayList<T>();

	/**
	 * Appends the specified node to the end of this collection.
	 *
	 * @param   matrix  Matrix3D associated with node.
	 * @param   node    Node to add.
	 */
	public void add( final Matrix3D matrix , final T node )
	{
		synchronized ( this )
		{
			_matrices.add( matrix );
			_nodes.add( node );
		}
	}

	/**
	 * Removes all elements and sets the size to zero.
	 */
	public void clear()
	{
		synchronized ( this )
		{
			_matrices.clear();
			_nodes.clear();
		}
	}

	/**
	 * Get matrix at specified index.
	 *
	 * @param   index   Index of element.
	 *
	 * @return  Matrix3D object at specified index.
	 */
	public Matrix3D getMatrix( final int index )
	{
		synchronized ( this )
		{
			return _matrices.get( index );
		}
	}

	/**
	 * Get node at specified index.
	 *
	 * @param   index   Index of element.
	 *
	 * @return  T object at specified index.
	 */
	public T getNode( final int index )
	{
		synchronized ( this )
		{
			return _nodes.get( index );
		}
	}

	/**
	 * Returns the number of elements.
	 *
	 * @return  The number of elements.
	 */
	public int size()
	{
		return _nodes.size();
	}
}
