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
	 * Parent of this node (<code>null</code> if this a root node).
	 */
	private Node3D _parent;

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
		_parent   = null;
		_children = new ArrayList();
		_tag      = null;
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
	public final Object getTag()
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
	public final void setTag( final Object tag )
	{
		_tag = tag;
	}

	/**
	 * Get parent of this node.
	 *
	 * @return  Parent node, or <code>null</code> if none exists.
	 *
	 * @see     #addChild
	 * @see     #removeChild
	 */
	public final Node3D getParent()
	{
		return _parent;
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
	public final Node3D getChild( final int index )
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
	public final int getChildCount()
	{
		return _children.size();
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
	public final boolean isLeaf()
	{
		return _children.isEmpty();
	}

	/**
	 * Add a child node to this node.
	 *
	 * @param   node    Node to add as a child.
	 *
	 * @return  Node that was added (same as node argument).
	 *
	 * @see     #removeChild
	 * @see     #getParent
	 */
	public final Node3D addChild( final Node3D node )
	{
		if ( node != null )
		{
			final List<Node3D> children = _children;
			if ( !children.contains( node ) )
			{
				children.add( node );
				setParentOfChild( node );
			}
		}

		return node;
	}

	/**
	 * This method is used internally by {@link #addChild} to update the
	 * {@link #_parent} field of a newly added child node. This takes care of
	 * removing the child node from its previous parent and setting it to this
	 * node.
	 *
	 * @param   node    Node whose {@link #_parent} field to update.
	 *
	 * @see     Insert3D#setParentOfChild
	 */
	void setParentOfChild( final Node3D node )
	{
		final Node3D oldParent = node._parent;
		if ( ( oldParent != null ) && ( oldParent != this ) )
			oldParent.removeChild( node );

		node._parent = this;
	}

	/**
	 * Remove a child node from this node.
	 *
	 * @param   node    Child node to remove.
	 *
	 * @see     #addChild
	 * @see     #getParent
	 */
	public final void removeChild( final Node3D node )
	{
		if ( ( node != null ) && _children.remove( node ) && ( node._parent == this ) )
			node._parent = null;
	}

	/**
	 * Remove all child nodes from this node.
	 *
	 * @see     #removeChild
	 */
	public final void removeAllChildren()
	{
		final List<Node3D> children = _children;
		while ( !children.isEmpty() )
			removeChild( children.get( children.size() - 1 ) );
	}

	/**
	 * This method creates a collection of leafs of the graphics tree of a
	 * specific class. This is typically used during the rendering process to
	 * collect objects that need to be rendered.
	 * <p>
	 * This proces consists of two phases: first going up the tree to find the
	 * root; then going down the tree to gather the leafs and storing them in
	 * a collection.
	 * <p>
	 * During both phases, a matrix is maintained that transforms coordinates
	 * from the current node to coordinates of the node where the proces
	 * started. If a node of the requested class is found, it will be stored
	 * in combination with its matrix in the returned collection.
	 *
	 * @param   leafs       Collection that contains all gathered leafs.
	 * @param   leafClass   Class of requested leafs (<code>null</code> => don't care).
	 * @param   transform   Transformation matrix upto this node.
	 * @param   upwards     Direction in which the tree is being traversed
	 *                      (should be <code>true</code> for the first call).
	 */
	public void gatherLeafs( final Node3DCollection leafs , final Class<? extends Node3D> leafClass , final Matrix3D transform , final boolean upwards )
	{
		final Node3D parent = getParent();
		if ( upwards && ( parent != null ) )
		{
			parent.gatherLeafs( leafs , leafClass , transform , true );
		}
		else
		{
			final List<Node3D> children = _children;

			if ( children.isEmpty() )
			{
				/*
				 * If this is a leaf, add it to the collection.
				 */
				if ( ( leafClass == null ) || leafClass.isInstance( this ) )
					leafs.add( transform , this );
			}
			else
			{
				/*
				 * If this is not a leaf, traverse the tree recursively to find them.
				 */
				for ( final Node3D node : children )
				{
					node.gatherLeafs( leafs, leafClass, transform, false );
				}
			}
		}
	}
}
