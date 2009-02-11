/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2009
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
import java.util.Collections;
import java.util.List;
import javax.swing.Action;

import ab.j3d.Bounds3D;
import ab.j3d.Bounds3DBuilder;
import ab.j3d.Matrix3D;
import ab.j3d.view.control.planar.PlaneControl;
import ab.j3d.view.control.planar.SubPlaneControl;

/**
 * Content node in {@link Scene}.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class ContentNode
{
	/**
	 * Application-assigned ID of this node.
	 */
	private final Object _id;

	/**
	 * Transform for this node.
	 */
	private Matrix3D _transform;

	/**
	 * Use alternate color.
	 */
	private boolean _alternate = false;

	/**
	 * Root in the 3D scene associated with this node.
	 */
	private Node3D _node3D;

	/**
	 * Context actions.
	 */
	private final List<Action> _contextActions = new ArrayList<Action>();

	/**
	 * Cached collection of nodes the content of this node.
	 */
	private Node3DCollection<Object3D> _cachedContent = null;

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
	public ContentNode( final Object id , final Matrix3D transform , final Node3D node3D )
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
	public final Object getID()
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
	public boolean collidesWith( final ContentNode thatNode )
	{
		boolean result = false;

		if ( ( thatNode != null ) && ( this != thatNode ) )
		{
			final Node3DCollection<Object3D> thisContent = getContent();
			final Node3DCollection<Object3D> thatContent = thatNode.getContent();

			if ( ( thisContent.size() > 0 ) && ( thatContent.size() > 0 ) )
			{
				final Matrix3D thisNode2World    = getTransform();
				final Matrix3D thatNode2World    = thatNode.getTransform();
				final Matrix3D thatNode2ThisNode = thatNode2World.multiply( thisNode2World.inverse() );

				for ( int i = 0 ; !result && ( i < thisContent.size() ) ; i++ )
				{
					final Object3D thisObject          = thisContent.getNode( i );
					final Matrix3D thisObject2ThisNode = thisContent.getMatrix( i );
					final Matrix3D thatNode2ThisObject = thatNode2ThisNode.multiply( thisObject2ThisNode.inverse() );

					for ( int j = 0 ; !result && ( j < thatContent.size() ) ; j++ )
					{
						final Object3D thatObject            = thatContent.getNode( j );
						final Matrix3D thatObject2ThatNode   = thatContent.getMatrix( j );
						final Matrix3D thatObject2ThisObject = thatObject2ThatNode.multiply( thatNode2ThisObject );

						result = thisObject.collidesWith( thatObject2ThisObject , thatObject );
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
	public final Bounds3D getBounds()
	{
		Bounds3D result = _cachedBounds3d;
		if ( result == null )
		{
			final Node3DCollection<Object3D> content = getContent();
			if ( content.size() > 0 )
			{
				final Bounds3DBuilder builder = new Bounds3DBuilder();

				for ( int i = 0 ; i < content.size() ; i++ )
				{
					final Object3D object3d    = content.getNode( i );
					final Matrix3D object2node = content.getMatrix( i );

					object3d.addBounds( builder , object2node );
				}

				result = builder.getBounds();
				_cachedBounds3d = result;
			}
		}
		return result;
	}

	/**
	 * Get collection of nodes with the content of this node. The returned
	 * collection is cached, so please make no alterations to it.
	 *
	 * @return  Content of this node (never <code>null</code>).
	 */
	public final Node3DCollection<Object3D> getContent()
	{
		Node3DCollection<Object3D> result = _cachedContent;

		if ( result == null )
		{
			result = new Node3DCollection<Object3D>();

			final Node3D root = getNode3D();
			if ( root != null )
			{
				root.collectNodes( result , Object3D.class , Matrix3D.INIT , false );
			}

			_cachedContent = result;
		}

		return result;
	}

	/**
	 * Get the transform for this node.
	 *
	 * @return  Transform for this node.
	 */
	public final Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set the transform for this node.
	 *
	 * @param   transform   Transform to set.
	 */
	public void setTransform( final Matrix3D transform )
	{
		if ( transform == null )
			throw new NullPointerException( "transform" );

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
	public final Node3D getNode3D()
	{
		return _node3D;
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
	public void addContextAction( final Action action )
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
	public void removeContextAction( final Action action )
	{
		_contextActions.remove( action );
	}

	/**
	 * Add a sub-plane control.
	 *
	 * @param   subPlaneControl     Sub-plane control to add.
	 */
	public void addSubPlaneControl( final SubPlaneControl subPlaneControl )
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
	public void removeSubPlaneControl( final SubPlaneControl subPlaneControl )
	{
		_subPlaneControls.remove( subPlaneControl );
	}

	/**
	 * Add listener for events about updated to this node.
	 *
	 * @param   listener    Listener to add.
	 */
	public void addContentNodeUpdateListener( final ContentNodeUpdateListener listener )
	{
		if ( listener == null )
			throw new NullPointerException();

		if ( _contentNodeUpdateListeners.contains( listener ) )
			throw new IllegalArgumentException( "already registered" );

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
			final ContentNodeUpdateEvent event = new ContentNodeUpdateEvent( this , ContentNodeUpdateEvent.RENDERING_PROPERTIES_UPDATED );

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
			final ContentNodeUpdateEvent event = new ContentNodeUpdateEvent( this , ContentNodeUpdateEvent.TRANSFORM_UPDATED );

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
			final ContentNodeUpdateEvent event = new ContentNodeUpdateEvent( this , ContentNodeUpdateEvent.CONTENT_UPDATED );

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
	public void setPlaneControl( final PlaneControl planeControl )
	{
		_planeControl = planeControl;
	}
}
