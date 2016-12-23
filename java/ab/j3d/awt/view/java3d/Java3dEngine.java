/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
 */
package ab.j3d.awt.view.java3d;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.media.j3d.*;
import javax.vecmath.*;

import ab.j3d.*;
import ab.j3d.awt.view.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * Java 3D render engine implementation.
 *
 * @author  G.B.M. Rupert
 */
public final class Java3dEngine
	implements RenderEngine , SceneUpdateListener
{
	/**
	 * 3D scene.
	 */
	private Scene _scene;

	/**
	 * Java 3D universe containing scene.
	 */
	private final Java3dUniverse _universe;

	/**
	 * Content branch graph of the Java 3D scene.
	 */
	private final BranchGroup _contentGraph;

	/**
	 * The ambient light source in the scene.
	 */
	private AmbientLight _ambientLight = null;

	/**
	 * Map node ID ({@link Object}) to Java 3D content graph object
	 * ({@link BranchGroup}).
	 */
	private final Map<Object,BranchGroup> _nodeContentMap = new HashMap<Object,BranchGroup>();

	/**
	 * Texture library to use.
	 */
	private TextureLibrary _textureLibrary;

	/**
	 * Construct new Java 3D model.
	 *
	 * @param scene          3D Scene to view.
	 * @param textureLibrary Texture library to use.
	 */
	public Java3dEngine( final Scene scene, final TextureLibrary textureLibrary )
	{
		this( scene, textureLibrary, new Color( 51 , 77 , 102 ) );
	}

	/**
	 * Construct new Java 3D model.
	 *
	 * @param scene          3D Scene to view.
	 * @param textureLibrary Texture library to use.
	 * @param background     Background color to use for 3D views.
	 */
	public Java3dEngine( @NotNull final Scene scene, final TextureLibrary textureLibrary, final Color background )
	{
		_scene = scene;
		scene.addSceneUpdateListener( this );

		_textureLibrary = textureLibrary;

		final Java3dUniverse universe = new Java3dUniverse( scene.getUnit(), background );
		_universe = universe;

		_contentGraph = Java3dTools.createDynamicScene( universe.getContent() );
	}

	/**
	 * Called to notify the listener that a content node was added to the scene.
	 * <p>
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
	 * @param   event   Event from {@link Scene}.
	 */
	public void contentNodeAdded( final SceneUpdateEvent event )
	{
		final ContentNode node = event.getNode();
		final Object id = node.getID();

		final BranchGroup nodeRoot = new BranchGroup();
		nodeRoot.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		nodeRoot.setCapability( BranchGroup.ALLOW_DETACH );
		_nodeContentMap.put( id , nodeRoot );

		final TransformGroup nodeTransform = new TransformGroup();
		nodeTransform.setTransform( Java3dTools.convertMatrix3DToTransform3D( node.getTransform() ) );
		nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_READ   );
		nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_WRITE );
		nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_EXTEND );
		nodeTransform.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		nodeRoot.addChild( nodeTransform );

		_contentGraph.addChild( nodeRoot );

		updateNodeContent( node );
	}

	public void contentNodeRemoved( final SceneUpdateEvent event )
	{
		final ContentNode node = event.getNode();

		final BranchGroup nodeRoot = getJava3dNode( node.getID() );
		if ( nodeRoot != null )
		{
			_contentGraph.removeChild( nodeRoot );
		}
	}

	public void contentNodeContentUpdated( final SceneUpdateEvent event )
	{
		updateNodeContent( event.getNode() );
	}

	public void contentNodePropertyChanged( final SceneUpdateEvent event )
	{
		final ContentNode node = event.getNode();
		final Object id = node.getID();
		final Matrix3D transform = node.getTransform();
		final TransformGroup nodeTransform = getJava3dTransform( id );

		nodeTransform.setTransform( Java3dTools.convertMatrix3DToTransform3D( transform ) );
	}

	public void ambientLightChanged( final SceneUpdateEvent event )
	{
		AmbientLight ambientLight = _ambientLight;
		if ( ambientLight == null )
		{
			ambientLight = new AmbientLight();
			ambientLight.setInfluencingBounds( getWorldBounds() );

			_ambientLight = ambientLight;
			_contentGraph.addChild( ambientLight );
		}

		final Scene scene = _scene;
		ambientLight.setColor( new Color3f( scene.getAmbientRed(), scene.getAmbientGreen(), scene.getAmbientBlue() ) );
	}

	/**
	 * Update content of node.
	 *
	 * @param   node    Node to update.
	 */
	private void updateNodeContent( final ContentNode node )
	{
		final Object         id            = node.getID();
		final TransformGroup nodeTransform = getJava3dTransform( id );
		final Matrix3D       transform     = node.getTransform();
		final Node3D         node3D        = node.getNode3D();

		final Node3DCollector objectCollector = new Node3DCollector( Object3D.class );
		Node3DTreeWalker.walk(  objectCollector, node3D );
		final List<Node3DPath> objects = objectCollector.getCollectedNodes();

		final BranchGroup bg = Shape3DBuilder.createBranchGroup( objects, _textureLibrary );

		final Node3DCollector lightCollector = new Node3DCollector( Light3D.class );
		Node3DTreeWalker.walk(  lightCollector, node3D );
		final List<Node3DPath> lights = lightCollector.getCollectedNodes();

		addLights( bg , lights , transform );

		/*
		 * Attach content to scene graph (replace existing branch group).
		 */
		nodeTransform.setTransform( Java3dTools.convertMatrix3DToTransform3D( transform ) );

		if ( nodeTransform.numChildren() == 0 )
		{
			nodeTransform.addChild( bg );
		}
		else
		{
			nodeTransform.setChild( bg , 0 );
		}
	}

	private static void addLights( final BranchGroup bg , final List<Node3DPath> lights , final Matrix3D transform )
	{
		if ( ( lights != null ) && !lights.isEmpty() )
		{
			final BoundingSphere worldBounds = getWorldBounds();

			for ( final Node3DPath path : lights )
			{
				final Light3D modelLight = (Light3D)path.getNode();

				final PointLight pointLight = new PointLight();
				pointLight.setPosition( (float)transform.xo, (float)transform.yo, (float)transform.zo );
				pointLight.setColor( new Color3f( modelLight.getDiffuseRed(), modelLight.getDiffuseGreen(), modelLight.getDiffuseBlue() ) );
				pointLight.setAttenuation( modelLight.getConstantAttenuation(), modelLight.getLinearAttenuation(), modelLight.getQuadraticAttenuation() );

				final float halfIntensityDistance = modelLight.getHalfIntensityDistance();
				if ( halfIntensityDistance > 0.0f )
				{
					pointLight.setInfluencingBounds( new BoundingSphere( new Point3d( 0.0, 0.0, 0.0 ), (double)halfIntensityDistance * 10.0 ) );
				}
				else if ( halfIntensityDistance == 0.0f )
				{
					pointLight.setInfluencingBounds( worldBounds );
				}

				bg.addChild( pointLight );
			}
		}
	}

	/**
	 * Returns bounds large enough to encompass the entire scene.
	 * @TODO How to determine the 'correct' world bounds.
	 *
	 * @return  World bounds.
	 */
	private static BoundingSphere getWorldBounds()
	{
		return new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 10000.0 );
	}

	public BranchGroup getJava3dNode( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		return _nodeContentMap.get( id );
	}

	public TransformGroup getJava3dTransform( final Object id )
	{
		final BranchGroup nodeRoot = getJava3dNode( id );
		return (TransformGroup)nodeRoot.getChild( 0 );
	}

	public View3D createView( final Scene scene )
	{
		if ( scene != _scene )
			throw new IllegalArgumentException( "Java 3D only supports one scene" );

		return new Java3dView( scene , _universe );
	}

	public OffscreenView3D createOffscreenView( final Scene scene )
	{
		throw new UnsupportedOperationException( "Offscreen view is not supported." );
	}

	public void dispose()
	{
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

	public void animationStarted( final SceneUpdateEvent event )
	{
	}

	public void animationStopped( final SceneUpdateEvent event )
	{
	}
}
