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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

import ab.j3d.Matrix3D;

/**
 * This class forms a default implementation of a node of the
 * graphics tree. It can not be instantiated directly, but must
 * sub-classed for a specific purpose.
 * <p>
 * The implementation consists of the tree and event mechanism
 * of the graphics tree, so sub-classes should normally implement
 * only their specific properties.
 *
 * @author  Sjoerd Bouwman
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Node3D
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
	private Node3D _parent;

	/**
	 * Construct empty tree node.
	 */
	public Node3D()
	{
		_tag = null;
		_parent = null;
	}

	/**
	 * Add a child node to this node.
	 *
	 * @param   node    Node to add as a child.
	 *
	 * @return  Node that was added (same as node argument).
	 *
	 * @see     #removeChild
	 */
	public final Node3D addChild( final Node3D node )
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
	 * @param   leafs       Collection that contains all gathered leafs.
	 * @param   leafClass   Class of requested leafs.
	 * @param   xform       Transformation matrix upto this node.
	 * @param   upwards     Direction in which the tree is being traversed
	 *                      (should be <code>true</code> for the first call).
	 */
	public void gatherLeafs( final Node3DCollection leafs , final Class leafClass , final Matrix3D xform , final boolean upwards )
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
				final Node3D[] children = getChildren();
				for ( int i = 0 ; i < children.length ; i++ )
				{
					final Node3D node = children[ i ];
					node.gatherLeafs( leafs , leafClass , xform , false );
				}
			}
		}
	}

	/**
	 * Get array with all currently registered child nodes.
	 *
	 * @return  Array with child nodes.
	 *
	 * @see     #isLeaf()
	 */
	public final Node3D[] getChildren()
	{
		return (Node3D[])_children.toArray( new Node3D[ _children.size() ] );
	}

	/**
	 * Get parent of this node.
	 *
	 * @return  Parent node, or <code>null</code> if none exists.
	 *
	 * @see #isRoot
	 */
	public final Node3D getParent()
	{
		return( _parent );
	}

	/**
	 * Get root of the graphics tree.
	 *
	 * @return  Root node of graphics tree.
	 *
	 * @see #isRoot
	 */
	public final Node3D getRoot()
	{
		Node3D node = this;
		while( !node.isRoot() ) node = node.getParent();
		return( node );
	}

	/**
	 * Get tag that was set by setTag().
	 *
	 * @return  Tag that was set by setTag().
	 */
	public final Object getTag()
	{
		return _tag;
	}

	/**
	 * This method returns <code>true</code> if this node is a leaf node
	 * (it has no children).
	 *
	 * @return  <code>true</code> if this node is a leaf node,
	 *          <code>false</code> otherwise.
	 *
	 * @see     #getChildren
	 */
	public final boolean isLeaf()
	{
		return _children.isEmpty();
	}

	/**
	 * This method returns <code>true</code> if this node is a root node
	 * (it has no parents).
	 *
	 * @return  <code>true</code> if this node is a root node,
	 *          <code>false</code> otherwise.
	 *
	 * @see     #getParent
	 */
	public final boolean isRoot()
	{
		return( _parent == null );
	}

	/**
	 * Paint 2D representation of this 3D object. The object coordinates are
	 * transformed using the objXform argument. By default, the object is painted
	 * by drawing the outlines of its 'visible' faces. Derivatives of this class
	 * may implement are more realistic approach (sphere, cylinder).
	 * <p />
	 * The rendering settings are determined by the <code>outlineColor</code>,
	 * <code>fillColor</code>, and <code>shadeFactor</code> arguments. The
	 * colors may be set to <code>null</code> to disable drawing of the
	 * outline or inside of faces respectively. The <code>shadeFactor</code> is
	 * used to modify the fill color based on the Z component of the face normal.
	 * A typical value of <code>0.5</code> would render faces pointing towards
	 * the Z-axis at 100%, and faces perpendicular to the Z-axis at 50%;
	 * specifying <code>0.0</code> completely disables the effect (always 100%);
	 * whilst <code>1.0</code> makes faces perpendicular to the Z-axis black
	 * (0%). The outline color is not influenced by the <code>shadeFactor</code>.
	 * <p />
	 * Objects are painted on the specified graphics context after being
	 * transformed again by gTransform. This may be used to pan/scale the object on the
	 * graphics context (NOTE: IT MAY NOT ROTATE THE OBJECT!).
	 *
	 * @param   g               Graphics2D context.
	 * @param   gTransform      Projection transform for Graphics2D context (3D->2D, pan, sale).
	 * @param   viewTransform   Transformation from object's to view coordinate system.
	 * @param   outlineColor    Color to use for face outlines (<code>null</code> to disable drawing).
	 * @param   fillColor       Color to use for filling faces (<code>null</code> to disable drawing).
	 * @param   shadeFactor     Amount of shading that may be applied (0=none, 1=extreme).
	 */
	public void paint( final Graphics2D g , final Matrix3D gTransform , final Matrix3D viewTransform , final Color outlineColor , final Color fillColor , final double shadeFactor )
	{
		final Node3D[] children = getChildren();
		for ( int i = 0 ; i < children.length ; i++ )
		{
			final Node3D node = children[ i ];
			node.paint( g , gTransform , viewTransform , outlineColor , fillColor , shadeFactor );
		}
	}

	/**
	 * Remove all child nodes from this node.
	 *
	 * @see     #removeChild
	 */
	public final void removeAllChildren()
	{
		final Node3D[] children = getChildren();

		_children.clear();
		for ( int i = 0 ; i < children.length ; i++ )
			children[ i ].setParent( null );
	}

	/**
	 * Remove a child node from this node.
	 *
	 * @param   node    Child node to remove.
	 *
	 * @see     #addChild
	 */
	public final void removeChild( final Node3D node )
	{
		if ( ( node != null ) && _children.remove( node ) )
			node.setParent( null );
	}

	/**
	 * Set parent of this node.
	 *
	 * @param   parent  Parent node (existing parent is removed).
	 *
	 * @see     #getRoot
	 * @see     #isRoot
	 */
	public final void setParent( final Node3D parent )
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
	 * @param   tag     Tag of node.
	 */
	public final void setTag( final Object tag )
	{
		_tag = tag;
	}
}
