/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.view.control.planar.*;
import org.jetbrains.annotations.*;

/**
 * Content node in {@link Scene}.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class ContentNode
{
	/**
	 * Application-assigned ID of this node.
	 */
	@NotNull
	private final Object _id;

	/**
	 * Transform for this node.
	 */
	@NotNull
	private Matrix3D _transform;

	/**
	 * Use alternate color.
	 */
	private boolean _alternate = false;

	/**
	 * Root in the 3D scene associated with this node.
	 */
	@NotNull
	private Node3D _node3D;

	/**
	 * Context actions.
	 */
	private final List<Action> _contextActions = new ArrayList<Action>();

	/**
	 * Cached collection of nodes the content of this node.
	 */
	private List<Node3DPath> _cachedContent = null;

	/**
	 * Cached node oriented {@link Bounds3D} for all contents of this node.
	 */
	private Bounds3D _cachedBounds3d = null;

	/**
	 * List of {@link SubPlaneControl}'s attached to this Node.
	 */
	private final List<SubPlaneControl> _subPlaneControls = new ArrayList<SubPlaneControl>();

	/**
	 * Supported {@link PlaneControl} of this node.
	 */
	private PlaneControl _planeControl = null;

	/**
	 * List of listeners to notify about node events.
	 */
	private final List<ContentNodeUpdateListener> _contentNodeUpdateListeners = new ArrayList<ContentNodeUpdateListener>();

	/**
	 * Construct new content node. The <code>materialOverride</code> and
	 * <code>opacity</code> values can be used to provide extra hints for
	 * rendering objects.
	 *
	 * @param   id          Application-assigned ID of this node.
	 * @param   transform   Initial transform (<code>null</code> => identity).
	 * @param   node3D      Root in the 3D scene.
	 */
	public ContentNode( @NotNull final Object id, @Nullable final Matrix3D transform, @NotNull final Node3D node3D )
	{
		_id = id;
		_node3D = node3D;
		_transform = ( transform != null ) ? transform : Matrix3D.INIT;
	}

	/**
	 * Get application-assigned ID of this node.
	 *
	 * @return  Application-assigned ID of this node.
	 */
	@NotNull
	public Object getID()
	{
		return _id;
	}

	/**
	 * Test if this node collides with another.
	 *
	 * @param   thatNode    Node to test collision with.
	 *
	 * @return  <code>true</code> if the nodes collide;
	 *          <code>false</code> otherwise.
	 */
	public boolean collidesWith( @Nullable final ContentNode thatNode )
	{
		return collidesWith( getTransform(), thatNode );
	}

	/**
	 * Test if this node would collide with another with the specified
	 * transform.
	 *
	 * @param   thisNode2World  Transformation to apply to this node.
	 * @param   thatContentNode        Node to test collision with.
	 *
	 * @return  <code>true</code> if the nodes collide;
	 *          <code>false</code> otherwise.
	 */
	public boolean collidesWith( @NotNull final Matrix3D thisNode2World, @Nullable final ContentNode thatContentNode )
	{
		boolean result = false;

		if ( ( thatContentNode != null ) && ( this != thatContentNode ) )
		{
			final List<Node3DPath> thisContent = getContent();
			final List<Node3DPath> thatContent = thatContentNode.getContent();

			if ( !thisContent.isEmpty() && !thatContent.isEmpty() )
			{
				final Matrix3D thatNode2World = thatContentNode.getTransform();
				final Matrix3D thatNode2ThisNode = thatNode2World.multiplyInverse( thisNode2World );

				for ( final Node3DPath thisPath : thisContent )
				{
					final Node3D thisNode = thisPath.getNode();
					if ( thisNode instanceof Object3D )
					{
						final Object3D thisObject = (Object3D) thisNode;
						final Matrix3D thisObject2ThisNode = thisPath.getTransform();
						final Matrix3D thatNode2ThisObject = thatNode2ThisNode.multiplyInverse( thisObject2ThisNode );

						for ( final Node3DPath thatPath : thatContent )
						{
							final Node3D thatNode = thatPath.getNode();
							if ( thatNode instanceof Object3D )
							{
								final Object3D thatObject = (Object3D) thatNode;
								final Matrix3D thatObject2ThatNode = thatPath.getTransform();
								final Matrix3D thatObject2ThisObject = thatObject2ThatNode.multiply( thatNode2ThisObject );

								if ( thisObject.collidesWith( thatObject2ThisObject, thatObject ) )
								{
									result = true;
									break;
								}
							}
						}

						if ( result )
						{
							break;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Returns the combined bounds of all the {@link ab.j3d.model.Object3D}'s this
	 * {@link ContentNode} contains.
	 * <br /><br />
	 * May return <code>null</code> if this {@link ContentNode} doesn't
	 * contains any {@link Object3D}'s.
	 *
	 * @return combined bounds of all the {@link ab.j3d.model.Object3D}'s this
	 * {@link ContentNode} contains.
	 */
	@Nullable
	public Bounds3D getBounds()
	{
		Bounds3D result = _cachedBounds3d;
		if ( result == null )
		{
			final Node3D node3d = getNode3D();
			result = node3d.calculateBounds( Matrix3D.IDENTITY );
			_cachedBounds3d = result;
		}
		return result;
	}

	/**
	 * Get collection of nodes with the content of this node. The returned
	 * collection is cached, so please make no alterations to it.
	 *
	 * @return  Content of this node.
	 */
	@NotNull
	public List<Node3DPath> getContent()
	{
		List<Node3DPath> result = _cachedContent;
		if ( result == null )
		{
			final Node3DCollector collector = new Node3DCollector( Object3D.class );
			Node3DTreeWalker.walk( collector, getNode3D() );
			result = collector.getCollectedNodes();
			_cachedContent = result;
		}

		return result;
	}

	/**
	 * Get the transform for this node.
	 *
	 * @return  Transform for this node.
	 */
	@NotNull
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set the transform for this node.
	 *
	 * @param   transform   Transform to set.
	 */
	public void setTransform( @NotNull final Matrix3D transform )
	{
		if ( !transform.equals( _transform ) )
		{
			_transform = transform;
			fireTransformUpdated();
		}
	}

	/**
	 * Get root in the 3D scene associated with this node.
	 *
	 * @return  Root in the 3D scene associated with this node.
	 */
	@NotNull
	public Node3D getNode3D()
	{
		return _node3D;
	}

	/**
	 * Get root in the 3D scene associated with this node.
	 *
	 * @param   node3D  Root in the 3D scene associated with this node.
	 */
	public void setNode3D( @NotNull final Node3D node3D )
	{
		if ( _node3D != node3D )
		{
			_node3D = node3D;
			fireContentUpdated();
		}
	}

	/**
	 * Returns true if an alternate color is to be used.
	 *
	 * @return true if an alternate color is to be used.
	 */
	public boolean isAlternate()
	{
		return _alternate;
	}

	/**
	 * If set to true draws an alternate color.
	 *
	 * @param alternate whether to use an alternate color or not.
	 */
	public void setAlternate( final boolean alternate )
	{
		if ( alternate != _alternate )
		{
			_alternate = alternate;
			fireRenderingPropertiesUpdated();
		}
	}

	/**
	 * Returns true if this object is clickable.
	 * @return true if object is clickable.
	 */
	public boolean isClickable()
	{
		return hasContextActions() || hasSubPlaneControls() /*|| hasContentNodeUpdateListeners()*/;
	}

	/**
	 * Add a context action.
	 *
	 * @param   action  Context action to add.
	 */
	public void addContextAction( @NotNull final Action action )
	{
		if ( !_contextActions.contains( action ) )
		{
			_contextActions.add( action );
		}
	}

	/**
	 * Clear all context actions.
	 */
	public void clearContextActions()
	{
		_contextActions.clear();
	}

	/**
	 * Returns all the context actions that apply to this node.
	 *
	 * @return  Context actions.
	 */
	public List<Action> getContextActions()
	{
		return Collections.unmodifiableList( _contextActions );
	}

	/**
	 * Test if this node has any context actions.
	 *
	 * @return  <code>true</code> if any context actions were added;
	 *          <code>false</code> otherwise.
	 */
	private boolean hasContextActions()
	{
		return !_contextActions.isEmpty();
	}

	/**
	 * Remove context action.
	 *
	 * @param   action  Context action to remove.
	 */
	public void removeContextAction( @NotNull final Action action )
	{
		_contextActions.remove( action );
	}

	/**
	 * Add a sub-plane control.
	 *
	 * @param   subPlaneControl     Sub-plane control to add.
	 */
	public void addSubPlaneControl( @NotNull final SubPlaneControl subPlaneControl )
	{
		_subPlaneControls.add( subPlaneControl );
	}

	/**
	 * Clear all sub-plane controls.
	 */
	public void clearSubPlaneControls()
	{
		_subPlaneControls.clear();
	}

	/**
	 * Returns all the sub-plane controls.
	 *
	 * @return  Sub-plane controls.
	 */
	public List<SubPlaneControl> getSubPlaneControls()
	{
		return Collections.unmodifiableList( _subPlaneControls );
	}

	/**
	 * Test if this node has any sub-plane controls.
	 *
	 * @return  <code>true</code> if any sub-plane control is added;
	 *          <code>false</code> otherwise.
	 */
	private boolean hasSubPlaneControls()
	{
		return !_subPlaneControls.isEmpty();
	}

	/**
	 * Remove sub-plane control.
	 *
	 * @param   subPlaneControl     Sub-plane control to remove.
	 */
	public void removeSubPlaneControl( @NotNull final SubPlaneControl subPlaneControl )
	{
		_subPlaneControls.remove( subPlaneControl );
	}

	/**
	 * Add listener for events about updated to this node.
	 *
	 * @param   listener    Listener to add.
	 */
	public void addContentNodeUpdateListener( @NotNull final ContentNodeUpdateListener listener )
	{
		if ( _contentNodeUpdateListeners.contains( listener ) )
		{
			throw new IllegalArgumentException( "already registered" );
		}

		_contentNodeUpdateListeners.add( listener );
	}

	/**
	 * Remove listener for events about updated to this node.
	 *
	 * @param   listener    Listener to remove.
	 */
	public void removeContentNodeUpdateListener( final ContentNodeUpdateListener listener )
	{
		_contentNodeUpdateListeners.remove( listener );
	}

	/**
	 * Send event about updated node rendering properties to all registered
	 * listeners.
	 */
	public void fireRenderingPropertiesUpdated()
	{
		final List<ContentNodeUpdateListener> listeners = _contentNodeUpdateListeners;
		if ( !listeners.isEmpty() )
		{
			final ContentNodeUpdateEvent event = new ContentNodeUpdateEvent( this, ContentNodeUpdateEvent.RENDERING_PROPERTIES_UPDATED );

			for ( final ContentNodeUpdateListener listener : listeners )
			{
				listener.renderingPropertiesUpdated( event );
			}
		}
	}

	/**
	 * Send event about updated node transform to all registered listeners.
	 */
	public void fireTransformUpdated()
	{
		//reset bounds
		_cachedBounds3d = null;

		final List<ContentNodeUpdateListener> listeners = _contentNodeUpdateListeners;
		if ( !listeners.isEmpty() )
		{
			final ContentNodeUpdateEvent event = new ContentNodeUpdateEvent( this, ContentNodeUpdateEvent.TRANSFORM_UPDATED );

			for ( final ContentNodeUpdateListener listener : listeners )
			{
				listener.transformUpdated( event );
			}
		}
	}

	/**
	 * Send event about updated node content to all registered listeners.
	 */
	public void fireContentUpdated()
	{
		//reset bounds and content
		_cachedContent = null;
		_cachedBounds3d = null;

		final List<ContentNodeUpdateListener> listeners = _contentNodeUpdateListeners;
		if ( !listeners.isEmpty() )
		{
			final ContentNodeUpdateEvent event = new ContentNodeUpdateEvent( this, ContentNodeUpdateEvent.CONTENT_UPDATED );

			for ( final ContentNodeUpdateListener listener : listeners )
			{
				listener.contentsUpdated( event );
			}
		}
	}

	/**
	 * Get plane control.
	 *
	 * @return  Plane control;
	 *          <code>null</code> if no plane control is set.
	 */
	@Nullable
	public PlaneControl getPlaneControl()
	{
		return _planeControl;
	}

	/**
	 * Set plane control.
	 *
	 * @param   planeControl    Plane control to set or <code>null</code> to
	 *                          disable plane control.
	 */
	public void setPlaneControl( @Nullable final PlaneControl planeControl )
	{
		_planeControl = planeControl;
	}
}
