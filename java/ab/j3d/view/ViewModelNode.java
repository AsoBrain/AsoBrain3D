/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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
package ab.j3d.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;

import ab.j3d.Bounds3D;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.control.planar.SubPlaneControl;

/**
 * Node in view model.
 *
 * @see     ViewModel
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class ViewModelNode
{
	/**
	 * List of listeners to notify about node events.
	 */
	private final ViewModel _listeners;

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
	 * Material to use instead of actual materials.
	 */
	private Material _materialOverride;

	/**
	 * Extra opacity (0.0=translucent, 1.0=opaque/unchanged).
	 */
	private float _opacity;

	/**
	 * Context actions.
	 */
	private final List<Action> _contextActions = new ArrayList<Action>();

	/**
	 * Cached {@link Bounds3D} containing the combined bounds of all the
	 * {@link Object3D}'s this {@link ViewModelNode} contains bounds3d.
	 * Will set to <code>null</code> if the content or the transform is changed.
	 */
	private Bounds3D _cachedBounds3d = null;

	/**
	 * List of {@link SubPlaneControl}'s attached to this Node.
	 */
	private final List<SubPlaneControl> _subPlaneControls = new ArrayList<SubPlaneControl>();

//	private final List<ViewModelNodeActionListener> _viewModelNodeActionListeners = new ArrayList();

	/**
	 * Construct new view model node. The <code>materialOverride</code> and
	 * <code>opacity</code> values can be used to provide extra hints for
	 * rendering objects.
	 *
	 * @param   listener            Listeners to notify about node events.
	 * @param   id                  Application-assigned ID of this node.
	 * @param   transform           Initial transform (<code>null</code> => identity).
	 * @param   node3D              Root in the 3D scene.
	 * @param   materialOverride    Material to use instead of actual materials.
	 * @param   opacity             Extra opacity (0.0=translucent, 1.0=opaque).
	 */
	public ViewModelNode( final ViewModel listener , final Object id , final Matrix3D transform , final Node3D node3D , final Material materialOverride , final float opacity )
	{
		_listeners        = listener;
		_id               = id;
		_node3D           = node3D;
		_materialOverride = materialOverride;
		_opacity          = opacity;
		_transform        = ( transform != null ) ? transform : Matrix3D.INIT;
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
	 * Returns the combined bounds of all the {@link ab.j3d.model.Object3D}'s this
	 * {@link ViewModelNode} contains.
	 * <br /><br />
	 * May return <code>null</code> if this {@link ViewModelNode} doesn't
	 * contains any {@link Object3D}'s.
	 *
	 * @return combined bounds of all the {@link ab.j3d.model.Object3D}'s this
	 * {@link ViewModelNode} contains.
	 */
	public final Bounds3D getBounds()
	{
		Bounds3D result = _cachedBounds3d;
		if( _cachedBounds3d == null)
		{
			final Node3DCollection<Object3D> object3DNode3DCollection;
			final Node3D node3D = getNode3D();

			object3DNode3DCollection = node3D.collectNodes( null , Object3D.class , Matrix3D.INIT , false );

			if ( object3DNode3DCollection != null )
			{
				for ( int i = 0 ; i < object3DNode3DCollection.size() ; i++ )
				{
					final Object3D object3d = object3DNode3DCollection.getNode( i );

					result = object3d.getBounds( object3DNode3DCollection.getMatrix( i ) , result );
				}
				object3DNode3DCollection.clear();
			}
			_cachedBounds3d = result;
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

		if( !transform.equals( _transform ) )
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
	 * Set root in the 3D scene associated with this node.
	 *
	 * @param   node3D  Root in the 3D scene associated with this node.
	 */
	public void setNode3D( final Node3D node3D )
	{
		_node3D = node3D;
	}

	/**
	 * Get material to override actual materials.
	 *
	 * @return  Material to use instead of actual materials;
	 *          <code>null</code> to use only actual materials.
	 */
	public Material getMaterialOverride()
	{
		return _materialOverride;
	}

	/**
	 * Set material to override actual materials.
	 *
	 * @param   material    Material to use instead of actual materials;
	 *                      <code>null</code> to use only actual materials.
	 */
	public void setMaterialOverride( final Material material )
	{
		if ( material != _materialOverride )
		{
			_materialOverride = material;
			fireRenderingPropertiesUpdated();
		}
	}

	/**
	 * Get extra opacity, multiplied with existing opacity values.
	 *
	 * @return  Extra opacity (0.0=translucent, 1.0=opaque/unchanged).
	 */
	public float getOpacity()
	{
		return _opacity;
	}

	/**
	 * Set extra opacity, multiplied with existing opacity values.
	 *
	 * @param   opacity     Extra opacity (0.0=translucent, 1.0=opaque/unchanged).
	 */
	public void setOpacity( final float opacity )
	{
		if ( opacity != _opacity )
		{
			_opacity = opacity;
			fireRenderingPropertiesUpdated();
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
		return hasContextActions() || hasPlanarControls() /*|| hasViewModelNodeActionListeners()*/;
	}

	/**
	 * Add a context action.
	 *
	 * @param   action  Context action to add.
	 */
	public void addContextAction( final Action action )
	{
		if( !_contextActions.contains( action ) )
			_contextActions.add( action );
	}

	/**
	 * Clear all context actions.
	 */
	public void clearContextActions()
	{
		_contextActions.clear();
	}

	/**
	 * Returns all the contextActions that apply to this viewModelNode as a {@link List }.
	 *
	 * @return all contextActions
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
	 * Add a planar control.
	 *
	 * @param   planarControl   Planar control to add.
	 */
	public void addPlanarControl( final SubPlaneControl planarControl )
	{
		_subPlaneControls.add( planarControl );
	}

	/**
	 * Clear all planar controls.
	 */
	public void clearPlanarControls()
	{
		_subPlaneControls.clear();
	}

	/**
	 * Returns all the planarControls that apply to this viewModelNode as a
	 * {@link List }.
	 *
	 * @return all planarControls
	 */
	public List<SubPlaneControl> getPlanarControls()
	{
		return Collections.unmodifiableList( _subPlaneControls );
	}

	/**
	 * Test if this node has any planar controls.
	 *
	 * @return  <code>true</code> if any planar controls were added;
	 *          <code>false</code> otherwise.
	 */
	private boolean hasPlanarControls()
	{
		return !_subPlaneControls.isEmpty();
	}

	/**
	 * Remove planar control.
	 *
	 * @param   control  Planar control to remove.
	 */
	public void removePlanarControl( final SubPlaneControl control )
	{
		_subPlaneControls.remove( control );
	}

//	private boolean hasViewModelNodeActionListeners()
//	{
//		return !_viewModelNodeActionListeners.isEmpty();
//	}
//
//	public List<ViewModelNodeActionListener> getViewModelNodeActionListeners()
//	{
//		return Collections.unmodifiableList( _viewModelNodeActionListeners );
//	}
//
//	public void addViewModelNodeActionListener( final ViewModelNodeActionListener viewModelNodeActionListener )
//	{
//		_viewModelNodeActionListeners.add( viewModelNodeActionListener );
//	}

	/**
	 * Send event about updated node rendering properties to all registered
	 * listeners.
	 */
	public void fireRenderingPropertiesUpdated()
	{
		_listeners.updateViews();
	}

	/**
	 * Send event about updated node transform to all registered listeners.
	 */
	public void fireTransformUpdated()
	{
		//reset bounds
		_cachedBounds3d = null;
		_listeners.updateNodeTransform( this );
	}

	/**
	 * Send event about updated node content to all registered listeners.
	 */
	public void fireContentUpdated()
	{
		//reset bounds
		_cachedBounds3d = null;
		_listeners.updateNodeContent( this );
	}
}
