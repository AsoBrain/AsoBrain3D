/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2004-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.soda.Gerwin.AbtoJ3D;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ab.j3d.Matrix3D;
import ab.j3d.renderer.TreeNode;

/**
 * @FIXME Need comment
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class ViewModel
{
	/**
	 * Map containg the nodes.
	 *
	 * key   : ID (<code>Object</code>).
	 * value : node (<code>ViewNode</code>).
	 */
	private Map _nodes;

	/**
	 * Construct new ViewModel.
	 */
	protected ViewModel()
	{
		_nodes = new HashMap();
	}

	/**
	 * Add view node. This method is only supposed to be used internally.
	 *
	 * @param   ID      Application-assigned ID for a view node.
	 * @param   node    Node to add.
	 *
	 * @throws  NullPointerException if ID or node is null.
	 */
	protected void addNode( final Object ID , final ViewNode node )
	{
		if ( ID == null )
			throw new NullPointerException( "ID" );

		if ( node == null )
			throw new NullPointerException( "node" );

		_nodes.put( ID , node );
	}

	/**
	 * Get view node by ID. This method is only supposed to be used internally.
	 *
	 * @param   ID      Application-assigned ID of view node to get.
	 *
	 * @throws  NullPointerException if ID is null.
	 */
	protected ViewNode getNode( final Object ID )
	{
		if ( ID == null )
			throw new NullPointerException( "ID" );

		return (ViewNode)_nodes.get( ID );
	}

	/**
	 * Get iterator for the IDs of all view sub trees that were added to the model.
	 *
	 * @return  Iterator for the IDs of all view sub trees that were added to the model.
	 */
	public Iterator getAllIDs()
	{
		return _nodes.keySet().iterator();
	}

	/**
	 * Create a new node (sub tree) in the view tree. The specified abNode is
	 * converted into view and then added to the view tree.
	 *
	 * @param   ID      ID of the view (sub)tree that is created. This ID can be
	 *                  used to update this specific part of the view tree.
	 * @param   abNode  AbNode to create a view node for.
	 */
	public abstract void createNode( Object ID , TreeNode abNode );

	/**
	 * Update a specified part (sub tree) of the view tree.
	 *
	 * @param   ID  ID of the (sub) tree to be updated.
	 */
	public void update( final Object ID )
	{
		final ViewNode node = getNode( ID );
		if ( node == null )
			throw new IllegalArgumentException( "ID '" + ID + "' does not refer to a known view node" );

		node.update();
	}

	/**
	 * Update a specified part (sub tree) of the view tree. If this sub tree
	 * is not created yet, it will be created (and then updated).
	 *
	 * @param   ID      ID of the view (sub)tree that is created. This ID can be
	 *                  used to update this specific part of the view tree.
	 * @param   abNode  AbNode to create a view node for.
	 */
	public void updateOrCreate( final Object ID , final TreeNode abNode )
	{
		final ViewNode node = getNode( ID );
		if ( node == null )
		{
			createNode( ID , abNode );
		}
		else
		{
			node.update();
		}
	}

	/**
	 * Set the transform for a specified part (sub tree) of the view tree.
	 *
	 * @param   transform   Transform to set.
	 */
	public void setTransform( final Object ID , final Matrix3D transform )
	{
		final ViewNode node = getNode( ID );
		if ( node == null )
			throw new IllegalArgumentException( "ID '" + ID + "' does not refer to a known view node" );

		node.setTransform( transform );
	}

	/**
	 * From a specified part (sub tree) of the view tree get the AbNode it was
	 * created/converted from.
	 *
	 * @param   ID  Id of the view sub tree.
	 *
	 * @return  AbNode that was used to create/convert the specified view
	 *          sub tree from;
	 *          <code>null</code> if the requested node was not found.
	 */
	public TreeNode getAbRootNode( final Object ID )
	{
		final ViewNode node = getNode( ID );
		return ( node != null ) ? node.getAbRootNode() : null;
	}
}
