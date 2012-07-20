/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.model;

import java.util.*;
import java.util.concurrent.atomic.*;

import ab.j3d.*;
import ab.j3d.view.control.planar.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a 3D scene.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Scene
{
	/**
	 * Scale factor for meters (metric system).
	 */
	public static final double METER = 1.0;

	/**
	 * Scale factor for meters (metric system).
	 */
	public static final double M = 1.0;

	/**
	 * Scale factor for millimeters (metric system).
	 */
	public static final double MM = 0.001;

	/**
	 * Scale factor for centimeters (metric system).
	 */
	public static final double CM = 0.01;

	/**
	 * Scale factor for inches (imperial system).
	 */
	public static final double INCH = 0.0254;

	/**
	 * Scale factor for feet (imperial system).
	 */
	public static final double FOOT = 12.0 * INCH;

	/**
	 * Scale factor for yards (imperial system).
	 */
	public static final double YARD = 3.0 * FOOT;

	/**
	 * Scale factor for miles (imperial system).
	 */
	public static final double MILE = 1760.0 * YARD;

	/**
	 * Scale factor for miles (imperial system).
	 */
	public static final double NAUTIC_MILE = 2025.4 * YARD;

	/**
	 * Unit scale factor in this scene. This scale factor, when multiplied,
	 * converts design units to meters.
	 */
	protected final double _unit;

	/**
	 * Light intensity of the ambient light in the scene (red component).
	 */
	private float _ambientColorRed = 0.0f;

	/**
	 * Light intensity of the ambient light in the scene (green component).
	 */
	private float _ambientColorGreen = 0.0f;

	/**
	 * Light intensity of the ambient light in the scene (blue component).
	 */
	private float _ambientColorBlue = 0.0f;

	/**
	 * Content nodes.
	 */
	private final Map<Object,ContentNode> _contentNodes = new HashMap<Object,ContentNode>();

	/**
	 * List of listeners to notify about scene update events.
	 */
	private final List<SceneUpdateListener> _sceneUpdateListeners = new ArrayList<SceneUpdateListener>();

	/**
	 * Plane controls for the entire scene.
	 */
	private final List<ScenePlaneControl> _planeControls;

	/**
	 * Cached scene bounds.
	 */
	private Bounds3D _bounds;

	/**
	 * Flag to indicate that this scene is animated as opposed to static. An
	 * animated scene may be rendered continuously in order to see the
	 * animation; rendering a static scene continuously is a waste of resources.
	 */
	private boolean _animated = false;

	/**
	 * Keeps track of the sequence number of the latest {@link SceneUpdate}
	 * instance. This is used to determine whether updates are still current.
	 */
	private final AtomicInteger _updateSequenceNumber = new AtomicInteger();

	/**
	 * Performs tasks needed for scene updates asynchronously.
	 */
//	private final ExecutorService _executorService = Executors.newSingleThreadExecutor();

	/**
	 * Shared listener for content node updates.
	 */
	private final ContentNodeUpdateListener _contentNodeUpdateListener = new ContentNodeUpdateListener()
		{
			public void contentsUpdated( final ContentNodeUpdateEvent event )
			{
				invalidateCache();
				fireContentNodeContentUpdated( (ContentNode)event.getSource() );
			}

			public void renderingPropertiesUpdated( final ContentNodeUpdateEvent event )
			{
				fireContentNodePropertyChanged( (ContentNode)event.getSource() );
			}

			public void transformUpdated( final ContentNodeUpdateEvent event )
			{
				invalidateCache();
				fireContentNodePropertyChanged( (ContentNode)event.getSource() );
			}
		};

	/**
	 * Construct new scene.
	 *
	 * @param   unit    Unit scale factor (e.g. {@link Scene#MM}).
	 */
	public Scene( final double unit )
	{
		_unit = unit;
		_planeControls = new ArrayList<ScenePlaneControl>();
		_bounds = null;
	}

	/**
	 * Invalidate cached information about the scene. This should be called
	 * whenever the scene contents change.
	 */
	protected void invalidateCache()
	{
		_bounds = null;
	}

	/**
	 * Flag to indicate that this scene is animated as opposed to static. An
	 * animated scene may be rendered continuously in order to see the
	 * animation; rendering a static scene continuously is a waste of resources.
	 *
	 * @return  <code>true</code> if this scene is animated;
	 *          <code>false</code> if this scene is static.
	 */
	public boolean isAnimated()
	{
		return _animated;
	}

	/**
	 * Set flag to indicate that this scene is animated as opposed to static. An
	 * animated scene may be rendered continuously in order to see the
	 * animation; rendering a static scene continuously is a waste of resources.
	 *
	 * @param   animated    <code>true</code> if this scene is animated;
	 *                      <code>false</code> if this scene is static.
	 */
	public void setAnimated( final boolean animated )
	{
		final boolean oldValue = _animated;
		if ( animated != oldValue )
		{
			final List<SceneUpdateListener> listeners = _sceneUpdateListeners;
			if ( !listeners.isEmpty() )
			{
				if ( animated )
				{
					final SceneUpdateEvent event = new SceneUpdateEvent( this, SceneUpdateEvent.ANIMATION_STARTED, null );
					for ( final SceneUpdateListener listener : listeners )
					{
						listener.animationStarted( event );
					}
				}
				else
				{
					final SceneUpdateEvent event = new SceneUpdateEvent( this, SceneUpdateEvent.ANIMATION_STOPPED, null );
					for ( final SceneUpdateListener listener : listeners )
					{
						listener.animationStopped( event );
					}
				}
			}

			_animated = animated;
		}
	}

	/**
	 * Adds appropriate 'legacy lights' to the given scene. Lights will be
	 * created to mimic the behavior of the Java3D view before April 2007, when
	 * lights were still hard-coded into views. The added lights are given
	 * IDs starting with "legacy-light".
	 *
	 * @param   scene   Scene to add legacy lights to.
	 */
	public static void addLegacyLights( @NotNull final Scene scene )
	{
		scene.setAmbient( 0.4f, 0.4f, 0.4f );

		final Light3D directional1 = new DirectionalLight3D( Vector3D.normalize( -0.8,  1.0, -0.6 ) );
		final Light3D directional2 = new DirectionalLight3D( Vector3D.normalize(  1.0, -1.0,  0.4 ) );
		final Light3D directional3 = new DirectionalLight3D( Vector3D.normalize( -2.0,  0.0, -1.0 ) );

		directional1.setIntensity( 1.0f );
		directional1.setCastingShadows( true );
		directional2.setIntensity( 0.3f );
		directional3.setIntensity( 0.3f );

		scene.addContentNode( "legacy-light-1", Matrix3D.IDENTITY, directional1 );
		scene.addContentNode( "legacy-light-2", Matrix3D.IDENTITY, directional2 );
		scene.addContentNode( "legacy-light-3", Matrix3D.IDENTITY, directional3 );
	}

	/**
	 * Get ID's of all content nodes in the scene.
	 *
	 * @return  Set with ID's of all content nodes in the scene.
	 */
	public final Set<Object> getContentNodeIDs()
	{
		return new HashSet<Object>( _contentNodes.keySet() );
	}

	/**
	 * Get all content nodes in the scene.
	 *
	 * @return  All content nodes in the scene.
	 */
	public final List<ContentNode> getContentNodes()
	{
		return new ArrayList<ContentNode>( _contentNodes.values() );
	}

	/**
	 * Get content node by ID.
	 *
	 * @param   id  ID of content node.
	 *
	 * @return  Content node with the specified ID;
	 *          <code>null</code> if no node with the specified ID was found.
	 */
	public final ContentNode getContentNode( @NotNull final Object id )
	{
		return _contentNodes.get( id );
	}

	/**
	 * Add content node.
	 *
	 * @param   node    Content node to add.
	 *
	 * @return  Node that was added.
	 */
	public final ContentNode addContentNode( @NotNull final ContentNode node )
	{
		if ( !_contentNodes.containsValue( node ) )
		{
			removeContentNode( node.getID() );

			_contentNodes.put( node.getID(), node );
			invalidateCache();

			node.addContentNodeUpdateListener( _contentNodeUpdateListener );

			fireContentNodeAdded( node );
		}

		return node;
	}

	/**
	 * Add a new content node. If a content node with the same ID already
	 * exists, that node will be removed before adding the new content node.
	 *
	 * @param   id          ID of content node.
	 * @param   transform   Initial transform (<code>null</code> => identity).
	 * @param   node3D      Root in the 3D scene to create a content node for.
	 *
	 * @return  Content node that was added.
	 */
	public final ContentNode addContentNode( @NotNull final Object id, final Matrix3D transform, @NotNull final Node3D node3D )
	{
		return addContentNode( new ContentNode( id, transform, node3D ) );
	}

	/**
	 * Test if the scene contains a content node with the specified ID.
	 *
	 * @param   id  ID of content node.
	 *
	 * @return  <code>true</code> if a content node with the specified ID was found;
	 *          <code>false</code> if the scene contains no such content node.
	 */
	public final boolean hasContentNode( @NotNull final Object id )
	{
		return ( getContentNode( id ) != null );
	}

	/**
	 * Remove content node.
	 *
	 * @param   id  ID of content node.
	 */
	public void removeContentNode( @NotNull final Object id )
	{
		final ContentNode node = getContentNode( id );
		if ( node != null )
		{
			node.removeContentNodeUpdateListener( _contentNodeUpdateListener );
			_contentNodes.remove( id );
			invalidateCache();

			fireContentNodeRemoved( node );
		}
	}

	/**
	 * Remove all content nodes from this scene.
	 *
	 * @see     #removeContentNode
	 * @see     #getContentNodeIDs
	 */
	public final void removeAllContentNodes()
	{
		for ( final Object id : getContentNodeIDs() )
		{
			removeContentNode( id );
		}
	}

	/**
	 * Get boundsing box that contains all 3D objects in the scene.
	 *
	 * @return  Bounding box of scene;
	 *          <code>null</code> if scene is empty.
	 */
	@Nullable
	public Bounds3D getBounds()
	{
		Bounds3D result = _bounds;

		if ( result == null )
		{
			final Bounds3DBuilder bounds3DBuilder = new Bounds3DBuilder();

			for ( final ContentNode node : getContentNodes() )
			{
				final Bounds3D nodeBounds = node.getBounds();
				if ( nodeBounds != null )
				{
					bounds3DBuilder.addBounds( node.getTransform(), nodeBounds );
				}
			}

			result = bounds3DBuilder.getBounds();
			_bounds = result;
		}

		return result;
	}

	/**
	 * Perform tree-walk through entire scene with the given visitor.
	 *
	 * @param   visitor     Visitor that will be called for each visited node.
	 *
	 * @return  <code>true</code> if the tree walk was finished normally;
	 *          <code>false</code> if the tree walk was aborted.
	 */
	public boolean walk( @NotNull final Node3DVisitor visitor )
	{
		return walk( visitor, Matrix3D.IDENTITY );
	}

	/**
	 * Perform tree-walk through entire scene with the given visitor and initial
	 * transformation matrix.
	 *
	 * @param   visitor     Visitor that will be called for each visited node.
	 * @param   transform   Initial transformation matrix.
	 *
	 * @return  <code>true</code> if the tree walk was finished normally;
	 *          <code>false</code> if the tree walk was aborted.
	 */
	public boolean walk( @NotNull final Node3DVisitor visitor, @NotNull final Matrix3D transform )
	{
		boolean result = true;

		for ( final ContentNode contentNode : getContentNodes() )
		{
			final Matrix3D node2scene = contentNode.getTransform();
			result = Node3DTreeWalker.walk( visitor, node2scene.multiply( transform ), contentNode.getNode3D() );
			if ( !result )
			{
				break;
			}
		}

		return result;
	}

	/**
	 * Unit scale factor in this scene in meters per unit. This factor, when
	 * multiplied, converts units to meters.
	 *
	 * @return  Unit scale (meters per unit).
	 */
	public double getUnit()
	{
		return _unit;
	}

	/**
	 * Returns the light intensity of the ambient light in the scene for the
	 * red color component.
	 *
	 * <p>For a definition of light intensity, see {@link Light3D}.
	 *
	 * @return  Ambient light intensity. (red)
	 */
	public float getAmbientRed()
	{
		return _ambientColorRed;
	}

	/**
	 * Returns the light intensity of the ambient light in the scene for the
	 * red color component.
	 *
	 * @return  Ambient light intensity. (green)
	 */
	public float getAmbientGreen()
	{
		return _ambientColorGreen;
	}

	/**
	 * Returns the light intensity of the ambient light in the scene for the
	 * blue color component.
	 *
	 * @return  Ambient light intensity. (blue)
	 */
	public float getAmbientBlue()
	{
		return _ambientColorBlue;
	}

	/**
	 * Sets the light intensity of the ambient light in the scene.
	 *
	 * @param   intensity       Intensity of ambient light.
	 */
	public void setAmbient( final float intensity )
	{
		setAmbient( intensity, intensity, intensity );
	}

	/**
	 * Sets the light intensity of the ambient light in the scene.
	 *
	 * @param   redIntensity    Intensity of the red color component.
	 * @param   greenIntensity  Intensity of the green color component.
	 * @param   blueIntensity   Intensity of the blue color component.
	 */
	public void setAmbient( final float redIntensity, final float greenIntensity, final float blueIntensity )
	{
		//noinspection FloatingPointEquality
		if ( ( _ambientColorRed   != redIntensity   ) ||
		     ( _ambientColorGreen != greenIntensity ) ||
		     ( _ambientColorBlue  != blueIntensity  ) )
		{
			_ambientColorRed   = redIntensity;
			_ambientColorGreen = greenIntensity;
			_ambientColorBlue  = blueIntensity;

			fireAmbientLightChanged();
		}
	}

	/**
	 * Add listener for events about updated to this scene.
	 *
	 * @param   listener    Listener to add.
	 */
	public void addSceneUpdateListener( @NotNull final SceneUpdateListener listener )
	{
		if ( _sceneUpdateListeners.contains( listener ) )
		{
			throw new IllegalArgumentException( "already registered" );
		}

		_sceneUpdateListeners.add( listener );
	}

	/**
	 * Remove listener for events about updated to this scene.
	 *
	 * @param   listener    Listener to remove.
	 */
	public void removeSceneUpdateListener( final SceneUpdateListener listener )
	{
		_sceneUpdateListeners.remove( listener );
	}

	/**
	 * Send event about content node added to the scene to all registered
	 * listeners.
	 *
	 * @param   node    Content node that was added.
	 */
	public void fireContentNodeAdded( final ContentNode node )
	{
		final List<SceneUpdateListener> listeners = _sceneUpdateListeners;
		if ( !listeners.isEmpty() )
		{
			final SceneUpdateEvent event = new SceneUpdateEvent( this, SceneUpdateEvent.CONTENT_NODE_ADDED, node );

			for ( final SceneUpdateListener listener : listeners )
			{
				listener.contentNodeAdded( event );
			}
		}
	}

	/**
	 * Send event about content node that was removed from the scene to all
	 * registered listeners.
	 *
	 * @param   node    Content node that was removed.
	 */
	public void fireContentNodeRemoved( final ContentNode node )
	{
		final List<SceneUpdateListener> listeners = _sceneUpdateListeners;
		if ( !listeners.isEmpty() )
		{
			final SceneUpdateEvent event = new SceneUpdateEvent( this, SceneUpdateEvent.CONTENT_NODE_REMOVED, node );

			for ( final SceneUpdateListener listener : listeners )
			{
				listener.contentNodeRemoved( event );
			}
		}
	}

	/**
	 * Send event about updated content node content in the scene to all
	 * registered listeners.
	 *
	 * @param   node    Content node that was updated.
	 */
	public void fireContentNodeContentUpdated( final ContentNode node )
	{
		final List<SceneUpdateListener> listeners = _sceneUpdateListeners;
		if ( !listeners.isEmpty() )
		{
			final SceneUpdateEvent event = new SceneUpdateEvent( this, SceneUpdateEvent.CONTENT_NODE_CONTENT_UPDATED, node );

			for ( final SceneUpdateListener listener : listeners )
			{
				listener.contentNodeContentUpdated( event );
			}
		}
	}

	/**
	 * Send event about an updated content node property in the scene to all
	 * registered listeners.
	 *
	 * @param   node    Content node that was updated.
	 */
	public void fireContentNodePropertyChanged( final ContentNode node )
	{
		final List<SceneUpdateListener> listeners = _sceneUpdateListeners;
		if ( !listeners.isEmpty() )
		{
			final SceneUpdateEvent event = new SceneUpdateEvent( this, SceneUpdateEvent.CONTENT_NODE_PROPERTY_CHANGED, node );

			for ( final SceneUpdateListener listener : listeners )
			{
				listener.contentNodePropertyChanged( event );
			}
		}
	}

	/**
	 * Send event about a change in the scene's ambient light intensity to all
	 * registered listeners.
	 */
	public void fireAmbientLightChanged()
	{
		final List<SceneUpdateListener> listeners = _sceneUpdateListeners;
		if ( !listeners.isEmpty() )
		{
			final SceneUpdateEvent event = new SceneUpdateEvent( this, SceneUpdateEvent.AMBIENT_LIGHT_CHANGED, null );

			for ( final SceneUpdateListener listener : listeners )
			{
				listener.ambientLightChanged( event );
			}
		}
	}

	/**
	 * Returns plane controls that apply to the entire scene.
	 *
	 * @return  Plane controls.
	 */
	@NotNull
	public List<ScenePlaneControl> getPlaneControls()
	{
		return Collections.unmodifiableList( _planeControls );
	}

	/**
	 * Adds the given plane control to the scene.
	 *
	 * @param   planeControl    Plane control to be added.
	 */
	public void addPlaneControl( @NotNull final ScenePlaneControl planeControl )
	{
		_planeControls.add( planeControl );
	}

	/**
	 * Removes the given plane control to the scene.
	 *
	 * @param   planeControl    Plane control to be removed.
	 */
	public void removePlaneControl( @NotNull final ScenePlaneControl planeControl )
	{
		_planeControls.remove( planeControl );
	}

	/**
	 * Returns a new scene update instance.
	 *
	 * @return  Scene update.
	 */
	public SceneUpdate createUpdate()
	{
		return new SceneUpdate( this );
//		return new AsynchronousSceneUpdate( this, _executorService );
	}

	/**
	 * Returns a sequence number for a newly created {@link SceneUpdate}.
	 *
	 * @return  Scene update sequence number.
	 */
	int incrementAndGetUpdateSequenceNumber()
	{
		return _updateSequenceNumber.incrementAndGet();
	}

	/**
	 * Returns the sequence number of the newest {@link SceneUpdate}.
	 *
	 * @return  Scene update sequence number.
	 */
	int getUpdateSequenceNumber()
	{
		return _updateSequenceNumber.get();
	}
}
