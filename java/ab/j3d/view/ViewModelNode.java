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

import ab.j3d.Matrix3D;
import ab.j3d.renderer.TreeNode;

/**
 * Node in view model.
 *
 * @see     ViewModel
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class ViewNode
{
	/**
	 * The Ab root node associated with this view node.
	 */
	private final TreeNode _abRootNode;

	/**
	 * Transform for this view node.
	 */
	private Matrix3D _transform;

	/**
	 * Construct new ViewNode.
	 *
	 * @param   abRootNode  Ab root node to associate with this view node.
	 */
	protected ViewNode( final TreeNode abRootNode )
	{
		_abRootNode = abRootNode;
		_transform = Matrix3D.INIT;
	}

	/**
	 * Update view node.
	 */
	public abstract void update();

	/**
	 * Get the transform for this view node.
	 *
	 * @return  Transform for this view node.
	 */
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set the transform for this view node.
	 *
	 * @param   transform   Transform to set.
	 */
	public void setTransform( final Matrix3D transform )
	{
		if ( transform == null )
			throw new NullPointerException( "transform" );

		_transform = transform;
	}

	/**
	 * Get the Ab root node associated with this view node.
	 *
	 * @return  Ab root node associated with this view node.
	 */
	public TreeNode getAbRootNode()
	{
		return _abRootNode;
	}
}
