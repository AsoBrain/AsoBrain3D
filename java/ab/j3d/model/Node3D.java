package common.renderer;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000,2002 - All Rights Reserved
 *
 * This software may not be used, copyied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
import java.awt.Graphics;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import common.model.Matrix3D;

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
 *
 * #version 1.0 (19991224, PSH, initial implementation)
 * #version ?.? (20010328, PSH, translated to C++)
 * #version 1.180 (20010827, SB, name(String) -> tag(Object)
 * #Version 1.181 (20010919, PSH, minor stuff)
 *
 * @version 1.0 (20011128, PSH) 
 */
public class TreeNode
{
	/**
	 * Tag of this node.
	 */
	private Object _tag = null;
	
	/**
	 * Collection of children of this node.
	 */
	private Vector _children = new Vector();

	/**
	 * Parent of this node (<code>null</code> if this a root node).
	 */
	private TreeNode _parent = null;

	/**
	 * Add a child node to this node.
	 *
	 * @param	node	Node to add as a child.
	 *
	 * @return	Node that was added (same as node argument).
	 *
	 * @see	removeChild()
	 */
	public TreeNode addChild( TreeNode node )
	{
		if ( node == null || _children.contains( node ) )
			return( node );
			
		_children.addElement( node );
		node.setParent( this );
		
		return( node );
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
	public void gatherLeafs( LeafCollection leafs , Class leafClass ,
		Matrix3D xform , boolean upwards )
	{
		if ( upwards )
		{
			/*
			 * If this is the root, go downwards now.
			 */
			if ( isRoot() )
			{
				upwards = false;
			}

			/*
			 * If this is not the root, keep going upwards.
			 */
			else
			{
				getParent().gatherLeafs( leafs , leafClass , xform , upwards );
			}
			
		}

		if ( !upwards ) // downwards
		{
			/*
			 * If this is a leaf, add it to the collection.
			 */
			if ( isLeaf() )
			{
				if ( leafClass.isInstance( this ) )
					leafs.add( xform , this );
			}
			/*
			 * If this is not a leaf, traverse the tree recursively to find them.
			 */
			else
			{
				Enumeration e = _children.elements();
				while ( e.hasMoreElements() ) ((TreeNode)e.nextElement()).
						gatherLeafs( leafs , leafClass , xform , upwards );
			}
		}
	}

	/**
	 * Get child with the specified index.
	 *
	 * @param	index	Index of child to retrieve.
	 *
	 * @return	Child node, or <code>null</code> if the child does not exist.
	 *
	 * @see getChildCount()
	 */
	public TreeNode getChild( int index )
	{
		return( (TreeNode)_children.elementAt( index ) );
	}

	/**
	 * This function returns the number of child nodes (if any).
	 *
	 * @return	Number of child nodes.
	 *
	 * @see	getChild()
	 */ 
	public int getChildCount()
	{
		return( _children.size() );
	}

	/**
	 * Get parent of this node.
	 *
	 * @return	Parent node, or <code>null</code> if none exists.
	 *
	 * @see isRoot()
	 */
	public TreeNode getParent()
	{
		return( _parent );
	}

	/**
	 * Get root of the graphics tree.
	 *
	 * @return	Root node of graphics tree.
	 *
	 * @see isRoot()
	 */
	public TreeNode getRoot()
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
	public Object getTag()
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
	 * @see	getChildCount()
	 * @see	getChild()
	 */
	public boolean isLeaf()
	{
		return( _children.size() == 0 );
	}

	/**
	 * This method returns <code>true</code> if this node is a root node
	 * (it has no parents).
	 *
	 * @return	<code>true</code> if this node is a root node,
	 *			<code>false</code> otherwise.
	 *
	 * @see	getParentCount()
	 * @see	getParent()
	 */
	public boolean isRoot()
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
	public void paint( Graphics g , Matrix3D gXform , Matrix3D objXform )
	{
		Enumeration e = _children.elements();
		while ( e.hasMoreElements() ) ((TreeNode)e.nextElement()).
				paint( g , gXform , objXform );
	}

	/**
	 * Remove all child nodes from this node.
	 *
	 * @see	#removeChild()
	 */
	public void removeAllChildren()
	{
		while ( _children.size() > 0 )
		{
			TreeNode node = (TreeNode)_children.elementAt( 0 );
			_children.removeElement( node );
			node.setParent( null );
		}
	}

	/**
	 * Remove a child node from this node.
	 *
	 * @param	node	Child node to remove.
	 *
	 * @see	addChild()
	 */
	public void removeChild( TreeNode node )
	{
		if ( node == null || !_children.contains( node ) )
			return;

		_children.removeElement( node );
		node.setParent( null );
	}

	/**
	 * Set parent of this node.
	 *
	 * @param	parent	Parent node (existing parent is removed).
	 *
	 * @see getRoot()
	 * @see isRoot()
	 */
	public void setParent( TreeNode parent )
	{
		if ( parent == _parent )
			return;
	
		if ( _parent != null )
			_parent.removeChild( this );
			
		_parent = parent;

		if ( _parent != null )
			_parent.addChild( this );
	}

	/**
	 * Set tag of this node.
	 *
	 * @param	tag	Tag of node.
	 */
	public void setTag( Object tag )
	{
		_tag = tag;
	}

}
