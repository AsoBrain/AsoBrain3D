/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2004 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
package ab.light3d.renderer;

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

import ab.light3d.Matrix3D;

/**
 * This class forms a default implementation of a node of the
 * graphics tree. It can not be instantiated directly, but must
 * sub-classed for a specific purpose.
 * <p>
 * The implementation consists of the tree and event mechanism
 * of the graphics tree, so sub-classes should normally implement
 * only their specific properties.
 *
 * @author	Peter S. Heijnen
 * @author	Sjoerd Bouwman
 * @version	$Revision$ ($Date$, $Author$)
 */
public class TreeNode
{
	/**
	 * Tag of this node.
	 */
	private Object _tag;

	/**
	 * Collection of children of this node.
	 */
	private final Set _children = new HashSet();

	/**
	 * Parent of this node (<code>null</code> if this a root node).
	 */
	private TreeNode _parent;

	/**
	 * Construct empty tree node.
	 */
	public TreeNode()
	{
		_tag = null;
		_parent = null;
	}

	/**
	 * Add a child node to this node.
	 *
	 * @param	node	Node to add as a child.
	 *
	 * @return	Node that was added (same as node argument).
	 *
	 * @see	#removeChild
	 */
	public final TreeNode addChild( final TreeNode node )
	{
		if ( ( node != null ) && _children.add( node ) )
			node.setParent( this );

		return node;
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
	 * @param	leafs		Collection that contains all gathered leafs.
	 * @param	leafClass	Class of requested leafs.
	 * @param	xform		Transformation matrix upto this node.
	 * @param	upwards		Direction in which the tree is being traversed
	 *						(should be <code>true</code> for the first call).
	 *
	 */
	public void gatherLeafs( final LeafCollection leafs , final Class leafClass , final Matrix3D xform , final boolean upwards )
	{
		if ( upwards && !isRoot() )
		{
			getParent().gatherLeafs( leafs , leafClass , xform , true );
		}
		else
		{
			if ( isLeaf() )
			{
				/*
				 * If this is a leaf, add it to the collection.
				 */
				if ( leafClass.isInstance( this ) )
					leafs.add( xform , this );
			}
			else
			{
				/*
				 * If this is not a leaf, traverse the tree recursively to find them.
				 */
				final TreeNode[] children = getChildren();
				for ( int i = 0 ; i < children.length ; i++ )
				{
					final TreeNode node = children[ i ];
					node.gatherLeafs( leafs , leafClass , xform , false );
				}
			}
		}
	}

	/**
	 * Get array with all currently registered child nodes.
	 *
	 * @return	Array with child nodes.
	 *
	 * @see     #isLeaf()
	 */
	public final TreeNode[] getChildren()
	{
		return (TreeNode[])_children.toArray( new TreeNode[ _children.size() ] );
	}

	/**
	 * Get parent of this node.
	 *
	 * @return	Parent node, or <code>null</code> if none exists.
	 *
	 * @see #isRoot
	 */
	public final TreeNode getParent()
	{
		return( _parent );
	}

	/**
	 * Get root of the graphics tree.
	 *
	 * @return	Root node of graphics tree.
	 *
	 * @see #isRoot
	 */
	public final TreeNode getRoot()
	{
		TreeNode node = this;
		while( !node.isRoot() ) node = node.getParent();
		return( node );
	}

	/**
	 * Get tag that was set by setTag().
	 *
	 * @return	Tag that was set by setTag().
	 */
	public final Object getTag()
	{
		return _tag;
	}

	/**
	 * This method returns <code>true</code> if this node is a leaf node
	 * (it has no children).
	 *
	 * @return	<code>true</code> if this node is a leaf node,
	 *			<code>false</code> otherwise.
	 *
	 * @see	#getChildren
	 */
	public final boolean isLeaf()
	{
		return _children.isEmpty();
	}

	/**
	 * This method returns <code>true</code> if this node is a root node
	 * (it has no parents).
	 *
	 * @return	<code>true</code> if this node is a root node,
	 *			<code>false</code> otherwise.
	 *
	 * @see	#getParent
	 */
	public final boolean isRoot()
	{
		return( _parent == null );
	}

	/**
	 * Paint 2D representation of this node and all its leaf nodes.
	 *
	 * @param	g			Graphics context.
	 * @param	gXform		Transformation to pan/scale the graphics context.
	 * @param	objXform	Transformation from object's to view coordinate system.
	 */
	public void paint( final Graphics g , final Matrix3D gXform , final Matrix3D objXform )
	{
		final TreeNode[] children = getChildren();
		for ( int i = 0 ; i < children.length ; i++ )
		{
			final TreeNode node = children[ i ];
			node.paint( g , gXform , objXform );
		}
	}

	/**
	 * Remove all child nodes from this node.
	 *
	 * @see	#removeChild
	 */
	public final void removeAllChildren()
	{
		final TreeNode[] children = getChildren();

		_children.clear();
		for ( int i = 0 ; i < children.length ; i++ )
			children[ i ].setParent( null );
	}

	/**
	 * Remove a child node from this node.
	 *
	 * @param	node	Child node to remove.
	 *
	 * @see	#addChild
	 */
	public final void removeChild( final TreeNode node )
	{
		if ( ( node != null ) && _children.remove( node ) )
			node.setParent( null );
	}

	/**
	 * Set parent of this node.
	 *
	 * @param	parent	Parent node (existing parent is removed).
	 *
	 * @see #getRoot
	 * @see #isRoot
	 */
	public final void setParent( final TreeNode parent )
	{
		if ( parent != _parent )
		{
			if ( _parent != null )
				_parent.removeChild( this );

			_parent = parent;

			if ( _parent != null )
				_parent.addChild( this );
		}
	}

	/**
	 * Set tag of this node.
	 *
	 * @param	tag	Tag of node.
	 */
	public final void setTag( final Object tag )
	{
		_tag = tag;
	}

}
