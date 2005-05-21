/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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
package ab.j3d.view.java2d;

import java.awt.Component;

import ab.j3d.Matrix3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewControl;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;

/**
 * Java 2D implementation of view model.
 *
 * @author G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java2dModel
	extends ViewModel
{
	/**
	 * Paint queue.
	 */
	private final Node3DCollection _paintQueue = new Node3DCollection();

	/**
	 * Construct new Java 2D view model.
	 */
	public Java2dModel()
	{
	}

	protected void initializeNode( final ViewModelNode node )
	{
	}

	protected void updateNodeTransform( final ViewModelNode node )
	{
		updateViews();
	}

	protected void updateNodeContent( final ViewModelNode node )
	{
		updateViews();
	}

	public Component createView( final Object id , final ViewControl viewControl )
	{
		final Java2dView view = new Java2dView( this , id , viewControl );
		addView( view );
		return view.getComponent();
	}

	public void updateViews()
	{
		final Node3DCollection queue = _paintQueue;
		queue.clear();

		final Object[] nodeIDs = getNodeIDs();
		for ( int i = 0 ; i < nodeIDs.length ; i++ )
		{
			final Object        id              = nodeIDs[ i ];
			final ViewModelNode node            = getNode( id );
			final Node3D        node3D          = node.getNode3D();
			final Matrix3D      transform       = node.getTransform();
//			final TextureSpec   textureOverride = node.getTextureOverride();
//			final float         opacity         = node.getOpacity();

			node3D.gatherLeafs( queue , Object3D.class , transform , false );
		}

		super.updateViews();
	}

	/**
	 * Get paint queue iterator.
	 *
	 * @return  Paint queue iterator.
	 */
	public Node3DCollection getPaintQueue()
	{
		return _paintQueue;
	}
}
