/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.view.BSPTree;

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
	 * Content nodes.
	 */
	private final Map<Object, ContentNode> _contentNodes = new HashMap<Object, ContentNode>();

	/**
	 * List of listeners to notify about scene update events.
	 */
	private final List<SceneUpdateListener> _sceneUpdateListeners = new ArrayList<SceneUpdateListener>();

	/**
	 * Binary Space Partitioning Tree ({@link BSPTree}) of this model.
	 * <p />
	 * The tree is only calculated when the scene changes (indicated by
	 * the {@link #_bspTreeDirty} field).
	 */
	private final BSPTree _bspTree;

	/**
	 * This internal flag is set to indicate that the scene is
	 * changed, so the {@link BSPTree} needs to be re-calculated.
	 */
	private boolean _bspTreeDirty;

	/**
	 * Shared listener for content node updates.
	 */
	private final ContentNodeUpdateListener _contentNodeUpdateListener = new ContentNodeUpdateListener()
		{
			public void contentsUpdated( final ContentNodeUpdateEvent event )
			{
				_bspTreeDirty = true;
				fireContentNodeContentUpdated( (ContentNode)event.getSource() );
			}

			public void renderingPropertiesUpdated( final ContentNodeUpdateEvent event )
			{
				fireContentNodePropertyChanged( (ContentNode)event.getSource() );
			}

			public void transformUpdated( final ContentNodeUpdateEvent event )
			{
				_bspTreeDirty = true;
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
		_bspTree = new BSPTree();
		_bspTreeDirty = true;
	}

	/**
	 * Adds appropriate 'legacy lights' to the given scene. Lights will be
	 * created to mimic the behavior of the Java3D view before April 2007, when
	 * lights were still hard-coded into views. The added lights are given
	 * IDs starting with "legacy-light".
	 *
	 * @param   scene   Scene to add legacy lights to.
	 *
	 * @throws  NullPointerException if <code>scene</code> is <code>null</code>.
	 */
	public static void addLegacyLights( final Scene scene )
	{
		if ( scene == null )
			throw new NullPointerException( "scene" );

		scene.addContentNode( "legacy-ambient-1" , Matrix3D.INIT , new Light3D( 10 , -1.0 ) , null , 1.0f );

		/* A distance of over 100000 units from the origin should be sufficient to mimic a directional light. */
		scene.addContentNode( "legacy-light-1" , Matrix3D.INIT.plus(  60000.0 ,  100000.0 ,  100000.0 ) , new Light3D( 255 , 300000.0 ) , null , 1.0f );
		scene.addContentNode( "legacy-light-2" , Matrix3D.INIT.plus( -60000.0 , -100000.0 , - 20000.0 ) , new Light3D( 230 , 300000.0 ) , null , 1.0f );
		scene.addContentNode( "legacy-light-3" , Matrix3D.INIT.plus( 100000.0 ,  -50000.0 , -100000.0 ) , new Light3D( 208 , 300000.0 ) , null , 1.0f );
	}

	/**
	 * Get binary Space Partitioning Tree ({@link BSPTree}) of this scene.
	 * <p />
	 * The tree is only calculated when the scene changes (indicated by
	 * the {@link #_bspTreeDirty} field).
	 *
	 * @return  The Binary Space Partitioning Tree of this scene.
	 */
	public BSPTree getBspTree()
	{
		final BSPTree result = _bspTree;

		if ( _bspTreeDirty )
		{
			result.reset();
			result.addScene( getContent() );
			result.build();

			_bspTreeDirty = false;
		}

		return result;
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
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final ContentNode getContentNode( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		return _contentNodes.get( id );
	}

	/**
	 * Add content node.
	 *
	 * @param   node    Content node to add.
	 *
	 * @return  Node that was added.
	 *
	 * @throws  NullPointerException if <code>node</code> is <code>null</code>.
	 */
	public final ContentNode addContentNode( final ContentNode node )
	{
		if ( node == null )
			throw new NullPointerException( "node" );

		if ( !_contentNodes.containsValue( node ) )
		{
			removeContentNode( node.getID() );

			_contentNodes.put( node.getID() , node );
			_bspTreeDirty = true;

			node.addContentNodeUpdateListener( _contentNodeUpdateListener );

			fireContentNodeAdded( node );
		}

		return node;
	}

	/**
	 * Add a new content node. If a content node with the same ID already
	 * exists, that node will be removed before adding the new content node.
	 *
	 * @param   id                  ID of content node.
	 * @param   node3D              Root in the 3D scene to create a content node for.
	 * @param   materialOverride    Material to use instead of actual materials.
	 * @param   opacity             Extra opacity (0.0=translucent, 1.0=opaque).);
	 *
	 * @return  Content node that was added.
	 *
	 * @throws  NullPointerException if <code>id</code> or <code>node3D<code> is <code>null</code>
	 */
	public final ContentNode addContentNode( final Object id , final Node3D node3D , final Material materialOverride , final float opacity )
	{
		return addContentNode( id , Matrix3D.INIT , node3D , materialOverride , opacity );
	}

	/**
	 * Add a new content node. If a content node with the same ID already
	 * exists, that node will be removed before adding the new content node.
	 *
	 * @param   id                  ID of content node.
	 * @param   transform           Initial transform (<code>null</code> => identity).
	 * @param   node3D              Root in the 3D scene to create a content node for.
	 * @param   materialOverride    Material to use instead of actual materials.
	 * @param   opacity             Extra opacity (0.0=translucent, 1.0=opaque).);
	 *
	 * @return  Content node that was added.
	 *
	 * @throws  NullPointerException if <code>id</code> or <code>node3D</code> is <code>null</code>.
	 */
	public final ContentNode addContentNode( final Object id , final Matrix3D transform , final Node3D node3D , final Material materialOverride , final float opacity )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		if ( node3D == null )
			throw new NullPointerException( "node3D" );

		final ContentNode result = new ContentNode( id , transform , node3D , materialOverride , opacity );
		addContentNode( result );
		return result;
	}

	/**
	 * Test if the scene contains a content node with the specified ID.
	 *
	 * @param   id  ID of content node.
	 *
	 * @return  <code>true</code> if a content node with the specified ID was found;
	 *          <code>false</code> if the scene contains no such content node.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final boolean hasContentNode( final Object id )
	{
		return ( getContentNode( id ) != null );
	}

	/**
	 * Remove content node.
	 *
	 * @param   id  ID of content node.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public void removeContentNode( final Object id )
	{
		final ContentNode node = getContentNode( id );
		if ( node != null )
		{
			node.removeContentNodeUpdateListener( _contentNodeUpdateListener );
			_contentNodes.remove( id );
			_bspTreeDirty = true;

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
	 * Get content of this scene. The content comprises all nodes in the scene.
	 *
	 * @return  A {@link Node3DCollection} with all nodes in the scene
	 *          (may be empty, but never <code>null</code>).
	 */
	public Node3DCollection<Node3D> getContent()
	{
		final Node3DCollection<Node3D> result = new Node3DCollection<Node3D>();

		for ( final ContentNode node : _contentNodes.values() )
		{
			final Node3D node3D = node.getNode3D();
			node3D.collectNodes( result , Node3D.class , node.getTransform() , false );
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
	 * Add listener for events about updated to this scene.
	 *
	 * @param   listener    Listener to add.
	 */
	public void addSceneUpdateListener( final SceneUpdateListener listener )
	{
		if ( listener == null )
			throw new NullPointerException();

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
			final SceneUpdateEvent event = new SceneUpdateEvent( this , SceneUpdateEvent.CONTENT_NODE_ADDED , node );

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
			final SceneUpdateEvent event = new SceneUpdateEvent( this , SceneUpdateEvent.CONTENT_NODE_REMOVED , node );

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
			final SceneUpdateEvent event = new SceneUpdateEvent( this , SceneUpdateEvent.CONTENT_NODE_CONTENT_UPDATED , node );

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
			final SceneUpdateEvent event = new SceneUpdateEvent( this , SceneUpdateEvent.CONTENT_NODE_PROPERTY_CHANGED , node );

			for ( final SceneUpdateListener listener : listeners )
			{
				listener.contentNodePropertyChanged( event );
			}
		}
	}
}
