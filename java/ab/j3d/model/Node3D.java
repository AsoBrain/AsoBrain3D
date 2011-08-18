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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a node in a 3D scene graph. It can be used directly as
 * a collection or merge point. Actual scene graph functionality is implemented
 * by sub-classes.
 * <p>
 * This class implements basic functionality for graph manipulation and
 * traversal.
 *
 * @author  Sjoerd Bouwman
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Node3D
{
	/**
	 * Collection of children of this node.
	 */
	private final List<Node3D> _children;

	/**
	 * Tag of this node.
	 * <p />
	 * A tag is an application-assigned object that is typically used to link
	 * application objects to scene graph objects. It is not used in any way
	 * by the AsoBrain 3D Toolkit classes.
	 *
	 * @see     #getTag
	 * @see     #setTag
	 */
	private Object _tag;

	/**
	 * Construct empty tree node.
	 */
	public Node3D()
	{
		_children = new ArrayList<Node3D>();
		_tag = null;
	}

	/**
	 * Invalidate any cached data in this node.
	 */
	protected void invalidateCache()
	{
	}

	/**
	 * Get tag that was set by {@link #setTag}.
	 * <p />
	 * A tag is an application-assigned object that is typically used to link
	 * application objects to scene graph objects. It is not used in any way
	 * by the AsoBrain 3D Toolkit classes.
	 *
	 * @return  Tag that was set by setTag().
	 *
	 * @see     #setTag
	 */
	public Object getTag()
	{
		return _tag;
	}

	/**
	 * Set tag of this node.
	 * <p />
	 * A tag is an application-assigned object that is typically used to link
	 * application objects to scene graph objects. It is not used in any way
	 * by the AsoBrain 3D Toolkit classes.
	 *
	 * @param   tag     Tag of node.
	 *
	 * @see     #getTag
	 */
	public void setTag( final Object tag )
	{
		_tag = tag;
	}

	/**
	 * Get child node with the specified index.
	 *
	 * @param   index   Index of child node.
	 *
	 * @return  Child node with specified index.
	 *
	 * @throws  IndexOutOfBoundsException if the index is out of range.
	 *
	 * @see     #getChildCount
	 */
	public Node3D getChild( final int index )
	{
		return _children.get( index );
	}

	/**
	 * Get number of child nodes of this node.
	 *
	 * @return  Number of child nodes (0 => none => leaf node).
	 *
	 * @see     #getChild
	 * @see     #isLeaf
	 */
	public int getChildCount()
	{
		return _children.size();
	}

	/**
	 * Returns the collection of children of this node.
	 *
	 * @return  Children of this node.
	 */
	public List<Node3D> getChildren()
	{
		return new ArrayList<Node3D>( _children );
	}

	/**
	 * This method returns <code>true</code> if this node is a leaf node
	 * (it has no children).
	 *
	 * @return  <code>true</code> if this node is a leaf node,
	 *          <code>false</code> otherwise.
	 *
	 * @see     #getChildCount
	 */
	public boolean isLeaf()
	{
		return _children.isEmpty();
	}

	/**
	 * Add a child node to this node.
	 *
	 * @param   node    Node to add as a child.
	 *
	 * @see     #removeChild
	 */
	public void addChild( @NotNull final Node3D node )
	{
		final List<Node3D> children = _children;
		if ( children.contains( node ) )
		{
			throw new IllegalArgumentException( node.toString() );
		}

		children.add( node );

		invalidateCache();
	}

	/**
	 * Add child nodes to this node.
	 *
	 * @param   nodes   Nodes to add as a child.
	 */
	public void addChildren( @NotNull final Node3D... nodes )
	{
		final List<Node3D> children = _children;
		boolean nodeAdded = false;
		for ( final Node3D node : nodes )
		{
			children.add( node );
			nodeAdded = true;
		}

		if ( nodeAdded )
		{
			invalidateCache();
		}
	}

	/**
	 * Add child nodes to this node.
	 *
	 * @param   nodes   Nodes to add as a child.
	 */
	public void addChildren( @NotNull final Collection<? extends Node3D> nodes )
	{
		if ( !nodes.isEmpty() )
		{
			final List<Node3D> children = _children;
			children.addAll( nodes );
			invalidateCache();
		}
	}

	/**
	 * Add child nodes to this node.
	 *
	 * @param   nodes   Nodes to add as a child.
	 */
	public void addChildren( @NotNull final Iterable<? extends Node3D> nodes )
	{
		final List<Node3D> children = _children;
		boolean nodeAdded = false;
		for ( final Node3D node : nodes )
		{
			children.add( node );
			nodeAdded = true;
		}

		if ( nodeAdded )
		{
			invalidateCache();
		}
	}

	/**
	 * Remove a child node from this node.
	 *
	 * @param   node    Child node to remove.
	 *
	 * @see     #addChild
	 */
	public void removeChild( @NotNull final Node3D node )
	{
		if ( !_children.remove( node ) )
		{
			throw new IllegalArgumentException( node.toString() );
		}

		invalidateCache();
	}

	/**
	 * Remove all child nodes from this node.
	 *
	 * @see     #removeChild
	 */
	public void removeAllChildren()
	{
		if ( !_children.isEmpty() )
		{
			_children.clear();
			invalidateCache();
		}
	}

	/**
	 * Calculate combined bounds all objects starting at this node.
	 *
	 * @param   transform   Transformation to apply to this node.
	 *
	 * @return  Calculated bounds;
	 *          <code>null</code> if bounds could not be determined.
	 */
	@Nullable
	public Bounds3D calculateBounds( final Matrix3D transform )
	{
		final Bounds3DBuilderVisitor worker = new Bounds3DBuilderVisitor();
		Node3DTreeWalker.walk( worker, transform, this );
		return worker.getBounds();
	}

	@Override
	public String toString()
	{
		final Class<?> clazz = getClass();
		return clazz.getSimpleName() + '@' + Integer.toHexString( hashCode() ) + "{tag=" + getTag() + '}';
	}

}
