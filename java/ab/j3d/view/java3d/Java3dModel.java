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
import java.util.HashMap;
import java.util.Map;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Texture;

import ab.j3d.Vector3D;
import ab.j3d.renderer.TreeNode;

import com.numdata.soda.mountings.db.MountingDb;

/**
 * @author G.B.M. Rupert
 * @version $Revision$ $Date$
 * @FIXME Need comment
 */
public class J3dModel
	extends ViewModel
{
	/**
	 * Database access provider.
	 */
	private MountingDb _db;

	/**
	 * J3d universe.
	 */
	private J3dUniverse _universe;

	/**
	 * The scene graph. Contains a content graph and a view graph.
	 */
	private BranchGroup _sceneGraph;

	/**
	 * Content branch graph of the j3d tree.
	 */
	private BranchGroup _contentGraph;

	/**
	 *View branch graph of the j3d tree.
	 */
	private BranchGroup _viewGraph;

	/**
	 * Map used to cache textures. Maps texture code (<code>String</code>) to
	 * texture (<code>Texture</code>).
	 */
	private static final Map _textureCache = new HashMap();

	/**
	 * Construct new J3DModel.
	 *
	 * @param   db  Database access provider.
	 */
	public J3dModel( final MountingDb db )
	{
		_db                 = db;
		_universe           = new J3dUniverse();
		_sceneGraph         = new BranchGroup();
		_contentGraph = new BranchGroup();
		_viewGraph    = new BranchGroup();

		_contentGraph.setCapability( BranchGroup.ALLOW_CHILDREN_EXTEND );
		_viewGraph.setCapability   ( BranchGroup.ALLOW_CHILDREN_EXTEND );

		_sceneGraph.addChild( _contentGraph );
		_sceneGraph.addChild( _viewGraph    );
		_universe.addBranchGraph( _sceneGraph );
	}

	public void createNode( final Object ID , final TreeNode abNode )
	{
		final ABtoJ3DConvertor convertor = new ABtoJ3DConvertor( _db , abNode , this );
		addNode( ID , convertor );

		final BranchGroup node = new BranchGroup();
		node.addChild( convertor.getJ3dRootNode() );
		_contentGraph.addChild( node );
	}

	/**
	 * Get a part (sub tree) of the j3d tree.
	 *
	 * @param   ID  ID of the j3d sub tree.
	 *
	 * @return  A part (sub tree) of the j3d tree.
	 */
	public Group getJ3dRootNode( final Object ID )
	{
		Group result = null;

		final ABtoJ3DConvertor convertor = (ABtoJ3DConvertor)getNode( ID );
		if ( convertor != null )
		{
			result = convertor.getJ3dRootNode();
		}

		return result;
	}

	/**
	 * Create a view from a specified point to a specified point.
	 *
	 * @param   ID      ID of the view that is created.
	 * @param   from    Point to look from.
	 * @param   to      Point to look at.
	 */
	public Component createView( final Object ID , final Vector3D from , final Vector3D to )
	{
		final J3dView view = new J3dView( _universe , from , to );
		addView( ID , view );

		final BranchGroup node = new BranchGroup();
		node.addChild( view.getJ3dRootNode() );
		_viewGraph.addChild( node );

		return view.getCanvas();
	}

	/**
	 * Get texture from cache.
	 *
	 * @param   code    Code identifying the texture to get.
	 */
	Texture getTextureFromCache( final String code )
	{
		return (Texture)_textureCache.get( code );
	}

	/**
	 * Add texture to cache.
	 *
	 * @param   code    Code identifying the texture to get.
	 * @param   texture Texture to add to cache.
	 */
	void addTextureToCache( final String code , final Texture texture )
	{
		_textureCache.put( code , texture );
	}
}
