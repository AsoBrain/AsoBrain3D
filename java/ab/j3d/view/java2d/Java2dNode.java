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

import java.util.LinkedList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.renderer.LeafCollection;
import ab.j3d.renderer.Object3D;
import ab.j3d.renderer.TreeNode;

/**
 * @author G.B.M. Rupert
 * @version $Revision$ $Date$
 * @FIXME Need comment
 */
public class ABtoJ2DConvertor
	extends ViewNode
{
	/**
	 * Paint queue.
	 */
	private final List _paintQueue;

	/**
	 * Construct new ABtoJ2DConvertor.
	 */
	public ABtoJ2DConvertor( final TreeNode abRootNode )
	{
		super( abRootNode );

		_paintQueue = new LinkedList();
	}

	/**
	 * Update view node.
	 */
	public void update()
	{
		final LeafCollection leafs = new LeafCollection();
		getAbRootNode().gatherLeafs( leafs , Object3D.class , Matrix3D.INIT , false );

		// update paint queue....
		// @FIXME How to determine 'smart' paint order....
		_paintQueue.clear();
		for ( int i = 0 ; i < leafs.size() ; i++ )
		{
			_paintQueue.add( leafs.getNode( i ) );
		}
	}

	/**
	 * Get the paint queue.
	 *
	 * @return  The paint queue.
	 */
	public List getPaintQueue()
	{
		return _paintQueue;
	}
}
