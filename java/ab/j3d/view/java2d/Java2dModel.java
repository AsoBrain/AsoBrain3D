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

import java.awt.Component;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ab.j3d.Vector3D;
import ab.j3d.renderer.TreeNode;

/**
 * @author G.B.M. Rupert
 * @version $Revision$ $Date$
 * @FIXME Need comment
 */
public class J2dModel
	extends ViewModel
{
	/**
	 * Paint queue.
	 */
	private final List _paintQueue;

	/**
	 * Construct new J2dModel.
	 */
	public J2dModel()
	{
		_paintQueue = new LinkedList();
	}

	/**
	 * Create a new node (sub tree) in the view tree. The specified abNode is
	 * converted into view and then added to the view tree.
	 *
	 * @param ID     ID of the view (sub)tree that is created. This ID can be used
	 *               to update this specific part of the view tree.
	 * @param abNode AbNode to create a view node for.
	 */
	public void createNode( final Object ID , final TreeNode abNode )
	{
		final ABtoJ2DConvertor convertor = new ABtoJ2DConvertor( abNode );
		addNode( ID , convertor );
		updateNode( ID );
	}

	/**
	 * Create a new view.
	 *
	 * @param ID   ID of the view that is created.
	 * @param from Point to look from.
	 * @param to   Point to look at.
	 */
	public Component createView( final Object ID , final Vector3D from , final Vector3D to )
	{
		final J2dView view = new J2dView( from , to );
		addView( ID , view );

		return view.getCanvas();
	}

	/**
	 * Update a specified part (sub tree) of the view tree.
	 *
	 * @param ID ID of the (sub) tree to be updated.
	 */
	public void updateNode( final Object ID )
	{
		super.updateNode( ID );

		updatePaintQueue();
		updateViews();
	}

	/**
	 * Update (repaint) all views.
	 */
	private void updateViews()
	{
		final Iterator iter = getAllViewIDs();
		while ( iter.hasNext() )
		{
			( (J2dView)iter.next() ).paint( _paintQueue );
		}
	}

	/**
	 * Update the paint queue.
	 */
	private void updatePaintQueue()
	{
		// In which order should the different nodes be painted....
		final Iterator iter = getAllNodeIDs();
		while( iter.hasNext() )
		{
			final ABtoJ2DConvertor convertor = (ABtoJ2DConvertor)iter.next();
			_paintQueue.addAll( convertor.getPaintQueue() );
		}
	}

	/**
	 * Get paint queue iterator.
	 *
	 * @return  Paint queue iterator.
	 */
	public Iterator getPaintQueue()
	{
		return _paintQueue.iterator();
	}
}
