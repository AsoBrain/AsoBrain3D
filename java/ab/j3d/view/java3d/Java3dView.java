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

import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.media.j3d.View;

import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 * @FIXME Need comment
 */
public class J3dView
	extends ViewView
{
	private static final int _PERSPECTIVE = 0;
	private static final int _PARALLEL    = 1;

	private J3dUniverse _universe;

	private ViewingPlatform _j3dRootNode;

	private J3dPanel _canvas;

	private Viewer _viewer;

	/**
	 * Construct new J3dView.
	 */
	public J3dView( final J3dUniverse universe , final Vector3D from , final Vector3D to )
	{
		_universe = universe;
		_canvas   = null;
		_viewer   = null;

		final Transform3D viewTransform = _universe.getTransform( from , to );

		final ViewingPlatform viewingPlatform = new ViewingPlatform( 1 );
        viewingPlatform.setUniverse( _universe );
		_j3dRootNode = viewingPlatform;

		if ( viewTransform != null )
		{
			_j3dRootNode.getMultiTransformGroup().getTransformGroup( 0 ).setTransform( viewTransform );
		}

		setProjectionPolicy( _PERSPECTIVE );
		createCanvas();

		// @FIXME Method??? How in abstract baseclass??
		_j3dRootNode.setViewPlatformBehavior( _universe.getOrbitBehavior( _universe.getViewerCount() -1) );
	}

	/**
	 * Get the j3d root node.
	 *
	 * @return  The j3d root node.
	 */
	public Group getJ3dRootNode()
	{
		return _j3dRootNode;
	}

	private void createCanvas()
	{
		_canvas = new J3dPanel( _universe );
		_viewer = new Viewer( _canvas );
		_viewer.setViewingPlatform( _j3dRootNode );

		// set transparency stuff
		final View view = _viewer.getView();
		view.setDepthBufferFreezeTransparent( true );
		view.setTransparencySortingPolicy( View.TRANSPARENCY_SORT_GEOMETRY );
		//view.setBackClipDistance( 100 );

		_universe.addViewer( _viewer );
	}

	public J3dPanel getCanvas()
	{
		return _canvas;
	}

	public void setTransform( final Matrix3D transform )
	{
		super.setTransform( transform );
		if ( _j3dRootNode != null )
			_j3dRootNode.getMultiTransformGroup().getTransformGroup( 0 ).setTransform( ABtoJ3DConvertor.convertMatrix3D( transform ) );
	}

	public void setProjectionPolicy( final int policy )
	{
		if ( !(policy == _PERSPECTIVE || policy == _PARALLEL) )
			throw new IllegalArgumentException( "Projection policy " + policy + " is not a valid policy." );

		final View view = _viewer.getView();
		view.setProjectionPolicy( policy == _PERSPECTIVE ? View.PERSPECTIVE_PROJECTION : View.PARALLEL_PROJECTION );
	}
}
