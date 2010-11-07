/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Implementation of {@link Node3DVisitor} that builds a collection of visited
 * nodes of a specific type and the transformation matrix at the time of visit.
 *
 * @param   <T> Type of nodes to collect.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class CollectingNode3DVisitor<T extends Node3D>
	extends AbstractNode3DVisitor
{
	/**
	 * Type of objects to collect.
	 */
	private final Class<T> _type;

	/**
	 * Collection of nodes that were visited.
	 */
	final Node3DCollection<T> _collection;

	/**
	 * Create visitor. The initial transformation matrix is set to the
	 * identity matrix.
	 *
	 * @param   type    Type of objects to collect.
	 */
	public CollectingNode3DVisitor( final Class<T> type )
	{
		this( Matrix3D.IDENTITY, type );
	}

	/**
	 * Create visitor.
	 *
	 * @param   transform   Initial transformation matrix.
	 * @param   type        Type of objects to collect.
	 */
	public CollectingNode3DVisitor( final Matrix3D transform, final Class<T> type )
	{
		super( transform );
		_type = type;
		_collection = new Node3DCollection<T>();
	}

	/**
	 * Clears all collected information.
	 */
	public void clear()
	{
		_collection.clear();
	}

	/**
	 * Returns the number of collected nodes.
	 *
	 * @return  The number of collected nodes.
	 */
	public int size()
	{
		return _collection.size();
	}

	/**
	 * Get collected transformation matrix with specified index.
	 *
	 * @param   index   Index of collected node.
	 *
	 * @return  Transformation for the collected node with specified index.
	 *
	 * @throws  IndexOutOfBoundsException if an incorrect index is specified.
	 */
	public Matrix3D getTransform( final int index )
	{
		return _collection.getMatrix( index );
	}

	/**
	 * Get collected node with specified index.
	 *
	 * @param   index   Index of collected node.
	 *
	 * @return  T collected node with specified index.
	 *
	 * @throws  IndexOutOfBoundsException if an incorrect index is specified.
	 */
	public T getNode( final int index )
	{
		return _collection.getNode( index );
	}

	@Override
	public void visitNode( @NotNull final Node3D node )
	{
		if ( _type.isInstance( node ) )
		{
			_collection.add( getTransform(), (T)node );
		}
	}
}