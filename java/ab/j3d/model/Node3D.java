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
		_parent = null;
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
	 * Returns the collection of children of this node.
	 *
	 * @return  Children of this node.
	 */
	public List<Node3D> getChildren()
	{
		return Collections.unmodifiableList( _children );
	}

	/**
	 * Test if the specified node is an ancestor of this node.
	 *
	 * @param   node    Node to test.
	 *
	 * @return  <code>true</code> if the specified node is an ancestor;
	 *          <code>false</code> otherwise.
	 */
	public final boolean isAncestor( final Node3D node )
	{
		boolean result = false;

		if ( node != null )
		{
			for ( Node3D ancestor = getParent() ; ancestor != null ; ancestor = ancestor.getParent() )
			{
				result = ( node == ancestor );
				if ( result )
				{
					break;
				}
			}
		}

		return result;
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
	 * @see     #removeChild
	 * @see     #getParent
	 */
	public final void addChild( @NotNull final Node3D node )
	{
		final List<Node3D> children = _children;
		if ( children.contains( node ) )
		{
			throw new IllegalArgumentException( node.toString() );
		}

		children.add( node );
		setParentOfChild( node );

		invalidateCache();
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
		{
			oldParent.removeChild( node );
		}

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
	public final void removeChild( @NotNull final Node3D node )
	{
		if ( !_children.remove( node ) )
		{
			throw new IllegalArgumentException( node.toString() );
		}

		if ( node._parent == this )
		{
			node._parent = null;
		}

		invalidateCache();
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
		{
			final Node3D node = children.remove( children.size() - 1 );
			if ( node._parent == this )
			{
				node._parent = null;
			}
		}

		invalidateCache();
	}

	/**
	 * Calculate combined of bounds all objects starting at this node.
	 *
	 * @param   xform   Transformation to apply to this node.
	 *
	 * @return  Calculated bounds;
	 *          <code>null</code> if bounds could not be determined.
	 */
	public final Bounds3D calculateBounds( final Matrix3D xform )
	{
		final Bounds3DBuilder builder = new Bounds3DBuilder();
		accept( new AbstractNode3DVisitor( xform )
		{
			@Override
			public void visitNode( @NotNull final Node3D node )
			{
				if( node instanceof Object3D )
				{
					final Object3D object3d = (Object3D) node;
					object3d.addBounds( builder, getTransform() );
				}
			}
		} );

		return builder.getBounds();
	}

	/**
	 * Iterates over the tree structure, and calls back to the given visitor
	 * while doing so.
	 *
	 * @param   visitor     Visitor to call back to.
	 */
	public void accept( final Node3DVisitor visitor )
	{
		visitor.visitNode( this );

		for ( final Node3D child : _children )
		{
			visitor.enterBranch( this, child);
			child.accept( visitor );
			visitor.exitBranch( this, child );
		}
	}

	/**
	 * This method collects nodes of a specific time from the scene graph.
	 * <p>
	 * The scene graph can be traversed in two directions. First, going up the
	 * tree to find the root, automatically followed by going down the tree to
	 * collect the nodes. Second, going down the tree immediately.
	 * <p>
	 * In both directions, a transformation is maintained that transforms
	 * coordinates from the current node to coordinates of the node where the
	 * process started. This transformation is collected in combination with
	 * the found node.
	 *
	 * @param   collection  Collection that contains all gathered nodes (may be
	 *                      <code>null</code> to create a collection on demand).
	 * @param   nodeClass   Class of requested nodes.
	 * @param   transform   Transformation matrix upto this node.
	 * @param   upwards     Direction in which the tree is being traversed.
	 *
	 * @return  Collection (same as <code>collection</code> or newly created if
	 *          it was <code>null</code> and at least one node was added).
	 */
	public <T extends Node3D> Node3DCollection<T> collectNodes( final Node3DCollection<T> collection, final Class<? extends T> nodeClass, final Matrix3D transform, final boolean upwards )
	{
		Node3DCollection<T> result = collection;

		final Node3D parent = getParent();
		if ( upwards && ( parent != null ) )
		{
			/*
			 * Traverse the tree upwards.
			 */
			result = parent.collectNodes( result, nodeClass, transform, true );
		}
		else
		{
			final List<Node3D> children = _children;

			/*
			 * If this is a matching node, add it to the collection.
			 */
			if ( nodeClass.isInstance( this ) )
			{
				if ( result == null )
				{
					result = new Node3DCollection<T>();
				}

				result.add( transform, (T)this );
			}

			/*
			 * Traverse the tree downwards.
			 */
			for ( final Node3D node : children )
			{
				result = node.collectNodes( result, nodeClass, transform, false );
			}
		}

		return result;
	}
}
