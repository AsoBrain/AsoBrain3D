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
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
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
	 * Java 3D utility toolbox.
	 */
	private final Java3dTools _j3dTools;

	/**
	 * BranchGroup that forms this node's root in the Java 3D scene graph.
	 */
	private final BranchGroup _branchGroup;

	/**
	 * The j3d root node to convert the Ab root node to.
	 */
	private final TransformGroup _transformGroup;

	/**
	 * Construct new Java3dNode.
	 *
	 * @param   j3dTools    Java 3D utility toolbox.
	 * @param   id          Application-assigned ID of this node.
	 * @param   node3D      Root in the 3D scene.
	 */
	Java3dNode( final Java3dTools j3dTools , final Object id , final Node3D node3D )
	{
		super( id , node3D );

		_j3dTools = j3dTools;
		_branchGroup = new BranchGroup();

		/*
		 * Create the j3d-root node and set the scaling transform.
		 * The scaling transfrom is needed, because j3d uses meters and
		 * AB uses milimeters. A scalar of 0.001 transforms this correctly.
		 */
		_transformGroup = new TransformGroup();
		final Transform3D scaleTransform = new Transform3D();
		scaleTransform.rotX( Math.toRadians( -90 ) );
		scaleTransform.setScale( 0.001 );
		_transformGroup.setTransform( scaleTransform );
		_branchGroup.addChild( _transformGroup );

		/*
		 * Set capabilities.
		 */
		_transformGroup.setCapability( TransformGroup.ALLOW_TRANSFORM_READ );
		_transformGroup.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		_transformGroup.setCapability( TransformGroup.ALLOW_CHILDREN_WRITE  );
		_transformGroup.setCapability( TransformGroup.ALLOW_CHILDREN_READ   );

		/*
		 * Finally do an update to convert the ab root node to the j3d root node.
		 */
		update();
	}

	public void update()
	{
		final Node3DCollection leafs = new Node3DCollection();
		getNode3D().gatherLeafs( leafs , Object3D.class , Matrix3D.INIT , false );

		/*
		 * Nodes are not directly added to the j3d root node too avoid flickering.
		 */
		final BranchGroup group = new BranchGroup();
		group.setCapability( BranchGroup.ALLOW_DETACH );

		for ( int i = 0 ; i < leafs.size() ; i++ )
		{
			group.addChild( _j3dTools.convertObject3DToNode( leafs.getMatrix( i ) , (Object3D)leafs.getNode( i ) , null , 1.0f ) );
		}

		if ( _transformGroup.numChildren() == 0 )
		{
			_transformGroup.addChild( group );
		}
		else
		{
			_transformGroup.setChild( group , 0 );
		}
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
		return _branchGroup;
	}
}
