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
	 * Construct new J2dModel.
	 */
	public J2dModel()
	{
	}

	/**
	 * Create a new node (sub tree) in the view tree. The specified abNode is
	 * converted into view and then added to the view tree.
	 *
	 * @param ID     ID of the view (sub)tree that is created. This ID can be used
	 *               to update this specific part of the view tree.
	 * @param abNode AbNode to create a view node for.
	 */
	public void createNode( Object ID , TreeNode abNode )
	{
	}

	/**
	 * Create a new view.
	 *
	 * @param ID   ID of the view that is created.
	 * @param from Point to look from.
	 * @param to   Point to look at.
	 */
	public Component createView( Object ID , Vector3D from , Vector3D to )
	{
		return null;
	}
}
