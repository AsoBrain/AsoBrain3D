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

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

import ab.j3d.Vector3D;

/**
 * This class extends the <code>SimpleUniverse</code> class and adds extra
 * functionality to add multiple views.
 *
 * @see     SimpleUniverse
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java3dUniverse
	extends SimpleUniverse
{
	/**
	 * Scale factor for meters (metric system).
	 */
	public static final float METER = 1.0f;

	/**
	 * Scale factor for meters (metric system).
	 */
	public static final float M = 1.0f;

	/**
	 * Scale factor for millimeters (metric system).
	 */
	public static final float MM = 0.001f;

	/**
	 * Scale factor for centimeters (metric system).
	 */
	public static final float CM = 0.01f;

	/**
	 * Scale factor for inches (imperial system).
	 */
	public static final float INCH = 0.0254f;

	/**
	 * Scale factor for feet (imperial system).
	 */
	public static final float FOOT = 12 * INCH;

	/**
	 * Scale factor for yards (imperial system).
	 */
	public static final float YARD = 3 * FOOT;

	/**
	 * Scale factor for miles (imperial system).
	 */
	public static final float MILE = 1760 * YARD;

	/**
	 * Scale factor for miles (imperial system).
	 */
	public static final float NAUTIC_MILE = 2025.4f * YARD;

	/**
	 * Unit scale factor in this universe. This scale factor, when multiplied,
	 * converts design units to Java 3D units (meters).
	 */
	private final float _unit;

	/**
	 * The scene content is either a user-defined group node, or create using the
	 * internal <code>createStaticScene()</code> method.
	 */
	private final Group _content;

	/**
	 * Construct universe with the specified unit scale and no views.
	 * <p />
	 * A default scene graph is created with default lighting, background, etc.
	 * Applications can access (a part of) this scene graph using the
	 * <code>getContent()</code> method to add or remove content from the scene.
	 * <p />
	 * The unit scale factor, when multiplied, converts design units to Java 3D
	 * units (meters). A <code>TransformGroup</code> is created within the
	 * scene graph to perform the scaling.
	 *
	 * @param   unit    Unit scale factor (e.g. <code>MM</code>).
	 *
	 * @see     #addViewer
	 * @see     #getContent
	 */
	public Java3dUniverse( final float unit )
	{
		//@FIXME The Viewer that is created standard by SimpleUniverse is shown, even though the viewer[] is emptied!?!?
		final Viewer notUsed = viewer[ 0 ];
		notUsed.setVisible( false );
		// Empty the viewer[], standard there are no viewers!
		viewer = new Viewer[ 0 ];

		_unit = unit;
		_content = createStaticContent();
	}

	/**
	 * Construct universe with the specified canvas and unit scale.
	 * <p />
	 * The canvas is registered as canvas for the first (and only) initial view
	 * of the universe.
	 * <p />
	 * A default scene graph is created with default lighting, background, etc.
	 * Applications can access (a part of) this scene graph using the
	 * <code>getContent()</code> method to add or remove content from the scene.
	 * <p />
	 * The unit scale factor, when multiplied, converts design units to Java 3D
	 * units (meters). A <code>TransformGroup</code> is created within the
	 * scene graph to perform the scaling.
	 *
	 * @param   canvas3d    Canvas for initial view.
	 * @param   unit        Unit scale factor.
	 *
	 * @see     #getContent
	 */
	public Java3dUniverse( final Canvas3D canvas3d , final float unit )
	{
		super( canvas3d );

		_unit = unit;
		_content = createStaticContent();
	}

	/**
	 * Construct universe with the specified canvas, content, and unit scale.
	 * <p />
	 * The canvas is registered as canvas for the first (and only) initial view
	 * of the universe.
	 * <p />
	 * The <code>BranchGroup</code> in the content graph must be added to the
	 * universe using <code>addBranchGroup<()/code> by the caller, since it is
	 * not necessarily the same as the specifiec content graph (the latter may
	 * be a descendant node).
	 * <p />
	 * The unit scale factor is stored as-is and is not used in any way by this
	 * constructor.
	 *
	 * @param   canvas3d    Canvas for initial view.
	 * @param   content     Content graph.
	 * @param   unit        Unit scale factor.
	 *
	 * @see     #addBranchGraph
	 */
	public Java3dUniverse( final Canvas3D canvas3d , final Group content , final float unit )
	{
		super( canvas3d );

		_unit = unit;
		_content = content;
	}

	/**
	 * Create a default static content scene graph to which applications can add
	 * their own content.
	 *
	 * @return  Content scene graph object.
	 *
	 * @see     Java3dTools#createDynamicScene
	 * @see     #getContent
	 * @see     #_unit
	 */
	private Group createStaticContent()
	{
		final BoundingSphere bounds = new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 100.0 );

		// create static scene
		final BranchGroup scene = new BranchGroup();

		// Set up the background
//		final Background background = new Background( new Color3f( 0.8f , 0.8f , 0.8f ) );
		final Background background = new Background( new Color3f( 0.2f , 0.3f , 0.4f ) );
//		Background background = new Background( new Color3f( 0.3f , 0.4f , 0.6f ) );
		background.setApplicationBounds( bounds );
		scene.addChild( background );

		// Set up the ambient light
		final Color3f ambientColor = new Color3f( 1.0f , 1.0f , 1.0f );
		final AmbientLight ambientLightNode = new AmbientLight( ambientColor );
		ambientLightNode.setInfluencingBounds( bounds );
		scene.addChild( ambientLightNode );

		// Set up the directional lights
		final Color3f  light1Color     = new Color3f (  1.0f ,  1.0f ,  1.0f );
		final Vector3f light1Direction = new Vector3f(  0.6f ,  1.0f ,  1.0f );
		final DirectionalLight light1 = new DirectionalLight( light1Color , light1Direction );
		light1.setInfluencingBounds( bounds );
//		scene.addChild( light1 );

		final Color3f  light2Color     = new Color3f (  0.9f ,  0.9f ,  0.9f );
		final Vector3f light2Direction = new Vector3f( -0.6f , -1.0f , -0.2f );
		final DirectionalLight light2 = new DirectionalLight( light2Color , light2Direction );
		light2.setInfluencingBounds( bounds );
		scene.addChild( light2 );

		final Color3f  light3Color     = new Color3f (  0.8f ,  0.8f , 0.8f );
		final Vector3f light3Direction = new Vector3f(  1.0f , -0.5f , -1.0f );
		final DirectionalLight light3 = new DirectionalLight( light3Color , light3Direction );
		light3.setInfluencingBounds( bounds );
		scene.addChild( light3 );

//		scene.addChild( Java3dTools.createGrid( new Point3f( 0.0f , 0.0f , 0.0f ) , new Point3i( 30 , 4 , 30 ) , 0.5f , 5 , new Color3f( 1 , 1 , 0 ) ) );
		scene.addChild( Java3dTools.createGrid( new Point3f( 0.0f , 2.0f , 0.0f ) , new Point3i( 20 , 8 , 20 ) , 0.25f , 4 , new Color3f( 0 , 0 ,0 ) ) );

		// add scene to scene graph

		// determine result node (insert scale transform group if needed).
		final Group result;

		final float unit = getUnit();
		if ( ( unit > 0 ) && ( unit != 1 ) )
		{
			result = new TransformGroup( Java3dTools.createTransform3D( Vector3D.INIT , 0 , unit ) );
			scene.addChild( result );
		}
		else
		{
			result = scene;
		}

		result.setCapability( BranchGroup.ALLOW_CHILDREN_EXTEND );
		scene.compile();
		addBranchGraph( scene );

		return result;
	}

	public Viewer getViewer()
	{
		throw new RuntimeException( "this method is not multi-view capable" );
	}

	/**
	 * Get the specified viewer in this Java 3D Universe.
	 *
	 * @param   viewerNum   Index of the viewer in this universe.
	 *
	 * @return  The requested viewer.
	 */
	public Viewer getViewer( final int viewerNum )
	{
		return viewer[ viewerNum ];
	}

	public ViewingPlatform getViewingPlatform()
	{
		throw new RuntimeException( "this method is not multi-view capable" );
	}

	/**
	 * Returns the <code>ViewingPlatform</code> object in the scene graph
	 * associated with the specified viewer in this Java 3D Universe.
	 *
	 * @param   viewerNum   Index of the viewer in this universe.
	 *
	 * @return  ViewingPlatform object associated with the specified viewer.
	 */
	public ViewingPlatform getViewingPlatform( final int viewerNum )
	{
		return getViewer( viewerNum ).getViewingPlatform();
	}

	public Canvas3D getCanvas( final int canvasNum )
	{
		throw new RuntimeException( "this method is not multi-view capable" );
	}

	/**
	 * Returns the <code>Canvas3D</code> object at the specified index
	 * associated with the specified viewer in this Java 3D Universe.
	 *
	 * @param   viewerNum   Index of the viewer in this universe.
	 * @param   canvasNum   Index of the canvas in the viewer.
	 *
	 * @return  Canvas3D object associated with the specified viewer;
	 *          <code>null</code> if there is no canvas the given indices.
	 */
	public Canvas3D getCanvas( final int viewerNum , final int canvasNum )
	{
	    return getViewer( viewerNum ).getCanvas3D( canvasNum );
	}

	/**
	 * Add viewer to universe.
	 *
	 * @param   theViewer   Viewer to add.
	 */
	public void addViewer( final Viewer theViewer )
	{
		final Viewer[] newViewers = new Viewer[ viewer.length + 1 ];
		System.arraycopy( viewer , 0 , newViewers , 0 , viewer.length );
		newViewers[ viewer.length ] = theViewer;
		viewer = newViewers;
	}

	/**
	 * Get number of viewers in universe.
	 *
	 * @return  Number of viewers in universe.
	 */
	public int getViewerCount()
	{
		return viewer.length;
	}

	/**
	 * Content scene graph object. Applications, may use the returned node to
	 * add content to the universe.
	 *
	 * @return  Content scene graph object.
	 */
	public Group getContent()
	{
		return _content;
	}

	/**
	 * Unit scale factor in this universe. This scale factor, when multiplied,
	 * converts design units to Java 3D units (meters).
	 *
	 * @return  Unit scale factor in this universe.
	 */
	public float getUnit()
	{
		return _unit;
	}
}
