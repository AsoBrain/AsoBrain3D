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

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;

import com.sun.j3d.utils.picking.PickTool;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewControl;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;

/**
 * View model implementation for Java 3D.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java3dModel
	extends ViewModel
{
	/**
	 * Java 3D universe containing scene.
	 */
	private final Java3dUniverse _universe;

	/**
	 * Content branch graph of the Java 3D scene.
	 */
	private final BranchGroup _contentGraph;

	/**
	 * Map node ID ({@link Object}) to Java 3D content graph object
	 * ({@link BranchGroup}).
	 */
	private final Map _nodeContentMap = new HashMap();

	/**
	 * Construct new Java 3D model.
	 */
	public Java3dModel()
	{
		this( MM , new Color( 51 , 77 , 102 ) );
	}

	/**
	 * Construct new Java 3D model.
	 *
	 * @param   unit            Unit scale factor (e.g. {@link ViewModel.MM}).
	 * @param   background      Background color to use for 3D views.
	 */
	public Java3dModel( final double unit , final Color background )
	{
		this( new Java3dUniverse( unit , background ) );
	}

	/**
	 * Construct new Java 3D model.
	 *
	 * @param   j3dUniverse Java 3D universe.
	 */
	public Java3dModel( final Java3dUniverse j3dUniverse )
	{
		_universe     = j3dUniverse;
		_contentGraph = Java3dTools.createDynamicScene( _universe.getContent() );
	}

	/**
	 * Setup the Java 3D side of a view node.
	 * <p />
	 * This adds the following part to the Java 3D content graph:
	 * <pre>
	 *    (G)  {@link Java3dUniverse#getContent()}
	 *     |
	 *    (BG) {@link #_contentGraph}
	 *     |
	 *    <b>(BG) <code>nodeRoot</code> (added to {@link #_nodeContentMap} with <code>id</code> as key)
	 *     |
	 *    (TG) <code>nodeTransform</code>
	 *     |
	 *    (BG) content branch group (re-created on updates)</b>
	 * </pre>
	 *
	 * @param   node    View node being set up.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	protected void initializeNode( final ViewModelNode node )
	{
		final Object id = node.getID();

		final BranchGroup nodeRoot = new BranchGroup();
		nodeRoot.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		nodeRoot.setCapability( BranchGroup.ALLOW_DETACH );
		nodeRoot.setUserData( id );
		_nodeContentMap.put( id , nodeRoot );

		final TransformGroup nodeTransform = new TransformGroup();
		nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_READ   );
		nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_WRITE );
		nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_EXTEND );
		nodeTransform.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		nodeTransform.setUserData( nodeRoot );
		nodeRoot.addChild( nodeTransform );

		_contentGraph.addChild( nodeRoot );
	}

	protected void updateNodeTransform( final ViewModelNode node )
	{
		final Object         id            = node.getID();
		final Matrix3D       transform     = node.getTransform();
		final TransformGroup nodeTransform = getJava3dTransform( id );

		nodeTransform.setTransform( Java3dTools.convertMatrix3DToTransform3D( transform ) );
	}

	protected void updateNodeContent( final ViewModelNode node )
	{
		final Object         id              = node.getID();
		final TransformGroup nodeTransform   = getJava3dTransform( id );
		final Matrix3D       transform       = node.getTransform();
		final Node3D         node3D          = node.getNode3D();
		final TextureSpec    textureOverride = node.getTextureOverride();
		final float          opacity         = node.getOpacity();

		final Node3DCollection nodes = new Node3DCollection();
		node3D.gatherLeafs( nodes , Object3D.class , Matrix3D.INIT , false );

		final BranchGroup bg = Shape3DBuilder.createBranchGroup( nodes , textureOverride , opacity );

		/* @FIXME: Because Java3D does not allow use of the getParent() method for a live scene, a node now has the parent in its user data. This will be fixed in j3d 1.4. */
		updateChildren( bg );
		bg.setUserData( nodeTransform );

		/*
		 * Attach content to scene graph (replace existing branch group).
		 */
		nodeTransform.setTransform( Java3dTools.convertMatrix3DToTransform3D( transform ) );

		if ( nodeTransform.numChildren() == 0 )
			nodeTransform.addChild( bg );
		else
			nodeTransform.setChild( bg , 0 );
	}

	/**
	 * Walks through the children of a group, and sets the parent as the user
	 * object. This is necessary because Java3D 1.3 does not allow the use of
	 * getParent() on a live scene.<br> If one of the children of
	 * <code>group</code> is a Group itself, this method is called on that group as
	 * well, so the tree is traversed recursively. If the child is a Shape3D, it it
	 * set to allow intersection. This is required for picking an object.
	 *
	 * @param group The {@link Group} object for which the children must be
	 *              updated.
	 *
	 * @see Java3dSelectionSupport
	 */
	private void updateChildren( final Group group )
	{
		for ( int i = 0; i < group.numChildren(); i++ )
		{
			final Node node = group.getChild( i );

			node.setUserData( group );
			if ( node instanceof Group )
			{
				updateChildren( (Group)node );
			}
			else if ( node instanceof Shape3D )
			{
				PickTool.setCapabilities( node, PickTool.INTERSECT_FULL );
			}
		}
	}

	public void removeNode( final Object id )
	{
		final BranchGroup nodeRoot = getJava3dNode( id );
		if ( nodeRoot != null )
			_contentGraph.removeChild( nodeRoot );

		super.removeNode( id );
	}

	public BranchGroup getJava3dNode( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		return (BranchGroup)_nodeContentMap.get( id );
	}

	public TransformGroup getJava3dTransform( final Object id )
	{
		final BranchGroup nodeRoot = getJava3dNode( id );
		return (TransformGroup)nodeRoot.getChild( 0 );
	}

	public Component createView( final Object id , final ViewControl viewControl )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		final Java3dView view = new Java3dView( _universe , id , viewControl );
		addView( view );
		return view.getComponent();
	}

	/**
	 * Get Java 3D universe containing scene.
	 *
	 * @return  Java 3D universe containing scene.
	 */
	public Java3dUniverse getUniverse()
	{
		return _universe;
	}
}
