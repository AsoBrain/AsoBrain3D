/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2004 Numdata BV
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
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Group;

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
	 * Java 3D utility toolbox.
	 */
	private final Java3dTools _j3dTools;

	/**
	 * BranchGroup that forms this node's root in the Java 3D scene graph.
	 * <pre>
	 *    (BG) - <code>j3dParent (contstructor argument)</code>
	 *     |
	 *    <b>(BG) <code>_rootBranchGroup</code></b>
	 *     |
	 *    (TG) <code>_transformGroup</code>
	 *     |
	 *    (BG) content branch group (re-created on updates)
	 * </pre>
	 */
	private final BranchGroup _rootBranchGroup;

	/**
	 * The j3d root node to convert the Ab root node to.
	 * <pre>
	 *    (BG) - <code>j3dParent (contstructor argument)</code>
	 *     |
	 *    (BG) <code>_rootBranchGroup</code>
	 *     |
	 *    <b>(TG) <code>_transformGroup</code></b>
	 *     |
	 *    (BG) content branch group (re-created on updates)
	 * </pre>
	 */
	private final TransformGroup _transformGroup;

	/**
	 * Construct new Java 3D view model node. This adds the following part to
	 * the Java 3D scene graph:
	 * <pre>
	 *    (BG) - <code>j3dParent (contstructor argument)</code>
	 *     |
	 *    (BG) <code>_rootBranchGroup</code>
	 *     |
	 *    (TG) <code>_transformGroup</code>
	 *     |
	 *    (BG) content branch group (re-created on updates)
	 * </pre>
	 *
	 * @param   j3dTools        Java 3D utility toolbox.
	 * @param   j3dParent       Parent node in Java 3D scene graph.
	 * @param   id              Application-assigned ID of this node.
	 * @param   node3D          Root in the 3D scene.
	 */
	Java3dNode( final Java3dTools j3dTools , final Group j3dParent , final Object id , final Node3D node3D )
	{
		super( id , node3D );

		_j3dTools = j3dTools;

		/*
		 * Create root branch group for this node.
		 */
		_rootBranchGroup = new BranchGroup();
		_rootBranchGroup.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		_rootBranchGroup.setCapability( BranchGroup.ALLOW_DETACH );

		/*
		 * Create transform group to place the object in the Java 3D universe.
		 */
		_transformGroup = new TransformGroup();
		_transformGroup.setCapability( TransformGroup.ALLOW_CHILDREN_READ   );
		_transformGroup.setCapability( TransformGroup.ALLOW_CHILDREN_WRITE );
		_transformGroup.setCapability( TransformGroup.ALLOW_CHILDREN_EXTEND );
		_transformGroup.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		_rootBranchGroup.addChild(_transformGroup );

		/*
		 * Create initial content.
		 */
		update();

		/*
		 * Add sub-graph  to Java 3D scene.
		 */
		j3dParent.addChild( _rootBranchGroup );
	}

	public void update()
	{
		final Node3DCollection nodes = new Node3DCollection();
		getNode3D().gatherLeafs( nodes , Object3D.class , Matrix3D.INIT , false );

		final BranchGroup bg = new BranchGroup();
		bg.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		bg.setCapability( BranchGroup.ALLOW_DETACH );

		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final Matrix3D xform = nodes.getMatrix( i );
			final Object3D obj3d = (Object3D)nodes.getNode( i );

			bg.addChild( _j3dTools.convertObject3DToNode( xform , obj3d , null , 1.0f ) );
		}

		/*
		 * Attach content to scene graph (replace existing branch group).
		 */
		if ( _transformGroup.numChildren() == 0 )
			_transformGroup.addChild( bg );
		else
			_transformGroup.setChild( bg , 0 );
	}

	public void setTransform( final Matrix3D transform )
	{
		super.setTransform( transform );
		_transformGroup.setTransform( Java3dTools.convertMatrix3DToTransform3D( transform ) );
	}

	/**
	 * Get Java3D scene graph object.
	 *
	 * @return  Java3D scene graph object.
	 */
	public BranchGroup getSceneGraphObject()
	{
		return _rootBranchGroup;
	}
}
