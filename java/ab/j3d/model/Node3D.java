/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2005 Peter S. Heijnen
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

import java.awt.Graphics2D;
import java.awt.Paint;
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
	private final List _children;

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
		return (Node3D)_children.get( index );
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
			final List children = _children;
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
		final List children = _children;
		while ( !children.isEmpty() )
			removeChild( (Node3D)children.get( children.size() - 1 ) );
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
	 * @param   xform       Transformation matrix upto this node.
	 * @param   upwards     Direction in which the tree is being traversed
	 *                      (should be <code>true</code> for the first call).
	 */
	public void gatherLeafs( final Node3DCollection leafs , final Class leafClass , final Matrix3D xform , final boolean upwards )
	{
		final Node3D parent = getParent();
		if ( upwards && ( parent != null ) )
		{
			parent.gatherLeafs( leafs , leafClass , xform , true );
		}
		else
		{
			final List children   = _children;
			final int  childCount = children.size();

			if ( childCount == 0 )
			{
				/*
				 * If this is a leaf, add it to the collection.
				 */
				if ( ( leafClass == null ) || leafClass.isInstance( this ) )
					leafs.add( xform , this );
			}
			else
			{
				/*
				 * If this is not a leaf, traverse the tree recursively to find them.
				 */
				for ( int i = 0 ; i < childCount ; i++ )
				{
					final Node3D node = (Node3D)children.get( i );
					node.gatherLeafs( leafs , leafClass , xform , false );
				}
			}
		}
	}

	/**
	 * Paint 2D representation of 3D objects at this node and its child nodes
	 * using rendering hints defined for this object. See the other
	 * {@link #paint(Graphics2D, Matrix3D, Matrix3D, Paint, Paint, float)}
	 * method in this class for a more elaborate
	 * description of this process.
	 * <p />
	 * If the <code>alternateAppearance</code> flag is set, objects should be
	 * use alternate rendering hints, if available. Alternate appearance can be
	 * used to visualize state information, like marking selected or active
	 * objects.
	 *
	 * @param   g                       Graphics2D context.
	 * @param   gTransform              Projection transform for Graphics2D context (3D->2D, pan, sale).
	 * @param   viewTransform           Transformation from object's to view coordinate system.
	 * @param   alternateAppearance     Use alternate appearance.
	 */
	public void paint( final Graphics2D g , final Matrix3D gTransform , final Matrix3D viewTransform , final boolean alternateAppearance )
	{
		final List children   = _children;
		final int  childCount = children.size();

		for ( int i = 0 ; i < childCount ; i++ )
		{
			final Node3D node = (Node3D)children.get( i );
			node.paint( g , gTransform , viewTransform , alternateAppearance );
		}
	}

	/**
	 * Paint 2D representation of this 3D object. The object coordinates are
	 * transformed using the <code>viewTransform</code> argument. By default, the object is painted
	 * by drawing the outlines of its 'visible' faces. Derivatives of this class
	 * may implement are more realistic approach (sphere, cylinder).
	 * <p />
	 * The rendering settings are determined by the <code>outlinePaint</code>,
	 * <code>fillPaint</code>, and <code>shadeFactor</code> arguments. The
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
	 * @param   outlinePaint    Paint to use for face outlines (<code>null</code> to disable drawing).
	 * @param   fillPaint       Paint to use for filling faces (<code>null</code> to disable drawing).
	 * @param   shadeFactor     Amount of shading that may be applied (0=none, 1=extreme).
	 */
	public void paint( final Graphics2D g , final Matrix3D gTransform , final Matrix3D viewTransform , final Paint outlinePaint , final Paint fillPaint , final float shadeFactor )
	{
		final List children   = _children;
		final int  childCount = children.size();

		for ( int i = 0 ; i < childCount ; i++ )
		{
			final Node3D node = (Node3D)children.get( i );
			node.paint( g , gTransform , viewTransform , outlinePaint , fillPaint , shadeFactor );
		}
	}
}
