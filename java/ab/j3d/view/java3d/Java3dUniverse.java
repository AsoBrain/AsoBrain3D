/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2003-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.soda.Gerwin.AbtoJ3D;

import java.awt.event.MouseEvent;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

import ab.j3d.Vector3D;

/**
 * This class extends the <code>Simple Universe</code> and adds extra functionality to add multiple views.
 *
 * @author G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class J3dUniverse extends SimpleUniverse
{
	private BranchGroup _staticScene;

	public J3dUniverse()
	{
		//@FIXME The Viewer that is created standard by SimpleUniverse is shown,
		//@FIXME even though the viewer[] is emptied!?!?
		final Viewer notUsed = viewer[ 0 ];
		notUsed.setVisible( false );

		// Empty the viewer[], standard there are no viewers!
		viewer = new Viewer[ 0 ];

		createStaticScene();
	}

	public Viewer getViewer()
	{
		throw new RuntimeException( "this method is not multi-view capable" );
	}

	public Viewer getViewer( final int index )
	{
		return viewer[ index ];
	}

	public ViewingPlatform getViewingPlatform()
	{
		throw new RuntimeException( "this method is not multi-view capable" );
	}

	public ViewingPlatform getViewingPlatform( final int index )
	{
		return viewer[ index ].getViewingPlatform();
	}

    public Canvas3D getCanvas( final int canvasNum )
    {
		throw new RuntimeException( "this method is not multi-view capable" );
    }

    public Canvas3D getCanvas( final int viewerNum , final int canvasNum )
    {
	    return viewer[ viewerNum ].getCanvas3D( canvasNum );
    }

	public J3dPanel addView( final Vector3D from , final Vector3D to , final int numberOfTransforms , final boolean isPerspective )
	{
		if ( numberOfTransforms < 1 )
			throw new IllegalArgumentException( "numberOfTransforms < 1" );

		// Create transform for the view.
		final Transform3D viewTransform = getTransform( from , to );

		final ViewingPlatform viewingPlatform = new ViewingPlatform( numberOfTransforms );
        viewingPlatform.setUniverse( this );
		locale.addBranchGraph( viewingPlatform );

		if ( viewTransform != null )
			viewingPlatform.getMultiTransformGroup().getTransformGroup( 0 ).setTransform( viewTransform );

		final J3dPanel panel     = new J3dPanel( this );
		final Viewer   newViewer = new Viewer( panel );
		newViewer.setViewingPlatform( viewingPlatform );

		final View view = newViewer.getView();
		view.setProjectionPolicy( isPerspective ? View.PERSPECTIVE_PROJECTION : View.PARALLEL_PROJECTION );

		// set transparency stuff
		view.setDepthBufferFreezeTransparent( true );
		view.setTransparencySortingPolicy( View.TRANSPARENCY_SORT_GEOMETRY );
//		view.setBackClipDistance( 100 );

		final Viewer[] newViewers = new Viewer[ viewer.length + 1 ];
		System.arraycopy( viewer , 0 , newViewers , 0 , viewer.length );
		newViewers[ viewer.length ] = newViewer;
		viewer = newViewers;

		viewingPlatform.setViewPlatformBehavior( getOrbitBehavior( viewer.length -1) );

		return panel;
	}

	/**
	 * Get the transform to look from a specified point to a specified point.
	 *
	 * @param from  Point to look from.
	 * @param to    Point to look at.
	 *
	 * @return  The transform to look from 'from' to 'to'.
	 */
	public Transform3D getTransform( final Vector3D from , final Vector3D to )
	{
		if ( from.equals( to ) )
		{
			throw new RuntimeException( "getTransfrom( from , to ); 'from' and 'to' can not be the same!" );
		}

		final Point3d  point1   = new Point3d  ( from.x , from.y , from.z );
		final Point3d  point2   = new Point3d  ( to.x   , to.y   , to.z   );
		final Vector3d upVector = new Vector3d ( 0      , 1      , 0      );

		if ( from.x == 0 && from.y != 0 && from.z == 0 )
		{
			upVector.set( 0 , 0 , 1 );
		}

		final Transform3D transform = new Transform3D();
		transform.setTranslation( new Vector3f( from.x , from.y , from.z ) );
		transform.lookAt( point1 , point2 , upVector );
		transform.invert();

		return transform;
	}

	public OrbitBehavior getOrbitBehavior( final int viewNr )
	{
		final BoundingSphere bounds = new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 100.0 );

		final OrbitBehavior orbit = new OrbitBehavior( getCanvas( viewNr , 0 ) , OrbitBehavior.REVERSE_ALL | OrbitBehavior.STOP_ZOOM );
		orbit.setSchedulingBounds( bounds );

		return orbit;
	}

	public BranchGroup getStaticScene()
	{
		return _staticScene;
	}

	private void createStaticScene()
	{
		final BoundingSphere bounds = new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 100.0 );

		// create static scene
		final BranchGroup staticScene = new BranchGroup();

		// Set up the background
		final Background background = new Background( new Color3f( 0.8f , 0.8f , 0.8f ) );
//		final Background background = new Background( new Color3f( 0.2f , 0.3f , 0.4f ) );
//		Background background = new Background( new Color3f( 0.3f , 0.4f , 0.6f ) );
		background.setApplicationBounds( bounds );
		staticScene.addChild( background );

		// Set up the ambient light
		final Color3f ambientColor = new Color3f( 0.8f , 0.8f , 0.8f );
		final AmbientLight ambientLightNode = new AmbientLight( ambientColor );
		ambientLightNode.setInfluencingBounds( bounds );
		staticScene.addChild( ambientLightNode );

		// Set up the directional lights
		final Color3f  light1Color     = new Color3f (  1.0f ,  0.9f ,  0.8f );
		final Vector3f light1Direction = new Vector3f(  0.6f ,  1.0f ,  1.0f );
		final DirectionalLight light1 = new DirectionalLight( light1Color , light1Direction );
		light1.setInfluencingBounds( bounds );
//		scene.addChild( light1 );

		final Color3f  light2Color     = new Color3f (  1.0f ,  0.9f ,  0.8f );
		final Vector3f light2Direction = new Vector3f( -0.6f , -1.0f , -0.2f );
		final DirectionalLight light2 = new DirectionalLight( light2Color , light2Direction );
		light2.setInfluencingBounds( bounds );
		staticScene.addChild( light2 );

		final Color3f  light3Color     = new Color3f (  0.8f ,  0.7f , 0.6f );
		final Vector3f light3Direction = new Vector3f(  1.0f , -0.5f , -1.0f );
		final DirectionalLight light3 = new DirectionalLight( light3Color , light3Direction );
		light3.setInfluencingBounds( bounds );
		staticScene.addChild( light3 );

//		staticScene.addChild( J3dTools.createGrid( new Point3f( 0.0f , 2f , 0.0f ) , new Point3i( 30 , 4 , 30 ) , 0.5f , 5 , J3dTools.YELLOW ) );
//		staticScene.addChild( CabinetJava3D.createGrid( new Point3f( 0.0f , 1.5f , 0.0f ) , new Point3i( 20 , 6 , 20 ) , 0.25f , 4 , CabinetJava3D.BLACK ) );

		staticScene.setCapability( BranchGroup.ALLOW_CHILDREN_EXTEND );

		// add scene to universe
		staticScene.compile();
		addBranchGraph( staticScene );

		_staticScene = staticScene;
	}

	public class MyOrbitBehavior extends OrbitBehavior
	{
		private int startX = 0;
		private int startY = 0;
		private int diffX  = 0;
		private int diffY  = 0;

		public MyOrbitBehavior( final Canvas3D a , final int b )
		{
			super( a , b );
		}

		protected synchronized void integrateTransforms()
		{
			final TransformGroup target = targetTG;

			final Transform3D trans = new Transform3D();
			trans.rotX( diffX );
			trans.rotY( diffY );

			target.setTransform( trans );
		}

		protected void processMouseEvent( final MouseEvent event )
		{
			super.processMouseEvent( event );

			if ( event.getID() == MouseEvent.MOUSE_PRESSED)
			{
				startX = event.getX();
				startY = event.getY();
			}
			else if ( event.getID() == MouseEvent.MOUSE_DRAGGED )
			{
				diffX = event.getX() - startX;
				diffY = event.getY() - startY;

				integrateTransforms();
			}
			else if ( event.getID() == MouseEvent.MOUSE_RELEASED )
			{

			}
		}
	}
}
