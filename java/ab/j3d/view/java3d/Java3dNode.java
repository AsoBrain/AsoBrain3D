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
package ab.j3d.view.java3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;

import ab.j3d.Matrix3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewModelNode;

/**
 * View model node implementation for Java 3D.
 *
 * @see     Java3dModel
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java3dNode
	extends ViewModelNode
{
	/**
	 * Transform node to attach dynamic content to.
	 * <pre>
	 *    <b>(TG) <code>nodeTransform</code></b>
	 *     |
	 *    (BG) content branch group (re-created on updates)
	 * </pre>
	 */
	private final TransformGroup _nodeTransform;

	/**
	 * Construct new Java 3D view model node. This adds the following part to
	 * the Java 3D scene graph:
	 * <pre>
	 *    (TG) <code>nodeTransform</code>
	 *     |
	 *    (BG) content branch group (re-created on updates)
	 * </pre>
	 *
	 * @param   nodeTransform   Transform node in Java 3D scene graph.
	 * @param   id              Application-assigned ID of this node.
	 * @param   node3D          Root in the 3D scene.
	 */
	Java3dNode( final TransformGroup nodeTransform , final Object id , final Node3D node3D )
	{
		super( id , node3D );

		_nodeTransform = nodeTransform;

		/*
		 * Create initial content.
		 */
		update();
	}

	public void update()
	{
		final Node3D node3D = getNode3D();

		final Node3DCollection nodes = new Node3DCollection();
		node3D.gatherLeafs( nodes , Object3D.class , Matrix3D.INIT , false );

		final Java3dTools j3dTools = Java3dTools.getInstance();

		final BranchGroup bg = new BranchGroup();
		bg.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		bg.setCapability( BranchGroup.ALLOW_DETACH );

		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final Matrix3D xform = nodes.getMatrix( i );
			final Object3D obj3d = (Object3D)nodes.getNode( i );

			bg.addChild( j3dTools.convertObject3DToNode( xform , obj3d , null , 1.0f ) );
		}

		/*
		 * Attach content to scene graph (replace existing branch group).
		 */
		final TransformGroup nodeTransform = _nodeTransform;
		if ( nodeTransform.numChildren() == 0 )
			nodeTransform.addChild( bg );
		else
			nodeTransform.setChild( bg , 0 );
	}

	public void setTransform( final Matrix3D transform )
	{
		super.setTransform( transform );
		_nodeTransform.setTransform( Java3dTools.convertMatrix3DToTransform3D( transform ) );
	}
}
