/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
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
package ab.j3d.view.jpct;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.Light;

import ab.j3d.Matrix3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

/**
 * View model implementation for jPCT.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JPCTModel
	extends ViewModel
{
	/**
	 * World containing the objects represented by this model.
	 */
	private final World _world;

	/**
	 * Maps node IDs ({@link Object}) to jPCT objects.
	 */
	private final Map<Object,List<com.threed.jpct.Object3D>> _objects;

	/**
	 * Maps node IDs ({@link Object}) to jPCT lights.
	 */
	private final Map<Object,List<Light>> _lights;

	/**
	 * Combined ambient light level from all ambient lights in the model.
	 */
	private int _ambientLight;

	/**
	 * Ambient light intensity by node ID.
	 */
	private final Map<Object,Integer> _ambientLights;

	/**
	 * Set of available lights. These lights are still part of the world, but
	 * are no longer needed and therefore disabled. Before adding new lights
	 * to the world, these existing ones should be reused first.
	 */
	private final Set<Light> _availableLights;

	/**
	 * Background color of the frame buffer.
	 */
	private Color _backgroundColor;

	/**
	 * Modifications that have been made to the view model, but have not yet
	 * been propagated to the world ({@link #_world}).
	 */
	private final Queue<Modification> _modifications;

	/**
	 * Construct new jPCT model.
	 *
	 * @param   unit        Unit scale factor (e.g. {@link ViewModel#MM}).
	 * @param   background  Background color of the frame buffer.
	 */
	public JPCTModel( final double unit , final Color background )
	{
		super( unit );
		_backgroundColor = background;

		_world           = new World();

		_objects         = new HashMap<Object,List<com.threed.jpct.Object3D>>();
		_lights          = new HashMap<Object,List<Light>>();
		_ambientLights   = new HashMap<Object,Integer>();
		_availableLights = new HashSet<Light>();
		_modifications   = new ConcurrentLinkedQueue<Modification>();

		setAmbientLight( 0 );
	}

	protected void initializeNode( final ViewModelNode node )
	{
		_modifications.add( new Addition( node ) );
	}

	protected void updateNodeContent( final ViewModelNode node )
	{
		_modifications.add( new Update( node ) );
	}

	public void removeNode( final Object id )
	{
		final ViewModelNode node = getNode( id );
		if ( node != null )
		{
			super.removeNode( id );
			_modifications.add( new Removal( node ) );
		}
	}

	/**
	 * Creates and initializes the view-specific objects for the specified node.
	 *
	 * @param   node    Node to perform initialization for.
	 */
	private void initializeNodeImpl( final ViewModelNode node )
	{
		final Object   nodeID    = node.getID();
		final Node3D   node3D    = node.getNode3D();
		final Matrix3D transform = node.getTransform();

		final World world = _world;

		final Node3DCollection<Node3D> nodes = new Node3DCollection<Node3D>();

		node3D.collectNodes( nodes , Object3D.class , transform , false );

		if ( nodes.size() > 0 )
		{
			List<com.threed.jpct.Object3D> objects = _objects.get( nodeID );
			if ( objects == null )
			{
				objects = new ArrayList<com.threed.jpct.Object3D>();
				_objects.put( nodeID , objects );
			}

			for ( int i = 0 ; i < nodes.size() ; i++ )
			{
				final Object3D                 modelObject = (Object3D)nodes.getNode( i );
				final com.threed.jpct.Object3D viewObject  = JPCTTools.convert2Object3D( modelObject );

				JPCTTools.setTransformation( viewObject , nodes.getMatrix( i ) );
				viewObject.setSpecularLighting( true );

				objects.add( viewObject );
				world.addObject( viewObject );
			}
		}

		nodes.clear();
		node3D.collectNodes( nodes , Light3D.class , transform , false );

		if ( nodes.size() > 0 )
		{
			List<Light> lights = _lights.get( nodeID );
			if ( lights == null )
			{
				lights = new ArrayList<Light>();
				_lights.put( nodeID , lights );
			}

			int nodeAmbientLight = 0;

			for ( int i = 0 ; i < nodes.size() ; i++ )
			{
				final Light3D modelLight = (Light3D)node3D;

				final Matrix3D lightTransform = nodes.getMatrix( i );

				final int   modelIntensity = modelLight.getIntensity();
				final float viewIntensity  = (float)modelIntensity * 10.0f / 255.0f;
				final float fallOff        = (float)modelLight.getFallOff();

				if ( fallOff < 0.0f )
				{
					/* Note: jPCT's ambient lights use the same units as the model. */
					nodeAmbientLight += modelIntensity;
				}
				else
				{
					final Light   viewLight  = addLight();
					viewLight.setIntensity( viewIntensity , viewIntensity , viewIntensity );
					viewLight.setAttenuation( fallOff );
					viewLight.setPosition( new SimpleVector( lightTransform.xo , lightTransform.yo , lightTransform.zo ) );
					lights.add( viewLight );
				}
			}

			if ( nodeAmbientLight > 0 )
			{
				removeAmbientLight( nodeID );

				final int ambientLight = _ambientLight + nodeAmbientLight;
				_ambientLights.put( nodeID , Integer.valueOf( nodeAmbientLight ) );
				setAmbientLight( ambientLight );
			}
		}
	}

	private void setAmbientLight( final int ambientLight )
	{
		_ambientLight = ambientLight;
		final int worldAmbient = ambientLight / 2;
		_world.setAmbientLight( worldAmbient , worldAmbient , worldAmbient );
	}

	/**
	 * Adds a light to the world. If any lights that were previously removed
	 * using {@link #removeLight(Light)} are still available, one of those
	 * lights is re-enabled and returned.
	 *
	 * @return  Light instance.
	 */
	private Light addLight()
	{
		final Light result;
		if ( _availableLights.isEmpty() )
		{
			result = new Light( _world );
		}
		else
		{
			final Iterator<Light> iterator = _availableLights.iterator();
			result = iterator.next();
			result.enable();
			iterator.remove();
		}
		return result;
	}

	/**
	 * Removes a light from the world. Note that the light only appears to be
	 * removed, due to the lack of a <code>removeLight</code> method or
	 * something similar in the {@link World} class. In stead of actual removal,
	 * the light is disabled and internally kept for re-use.
	 *
	 * @param   light   Light to be removed.
	 */
	private void removeLight( final Light light )
	{
		light.disable();
		_availableLights.add( light );
	}

	protected void updateNodeTransform( final ViewModelNode node )
	{
		// @TODO implement a more efficient update method
		updateNodeContent( node );
	}

	private void updateNodeContentsImpl( final ViewModelNode node )
	{
		// @TODO implement a more efficient update method
		removeNodeImpl( node );
		initializeNodeImpl( node );
	}

	/**
	 * Removes the given nod efrom the model.
	 *
	 * @param   node    Node to be removed.
	 */
	private void removeNodeImpl( final ViewModelNode node )
	{
		final Object id = node.getID();

		final List<com.threed.jpct.Object3D> objects = _objects.get( id );
		if ( objects != null )
		{
			for ( final com.threed.jpct.Object3D object : objects )
			{
				_world.removeObject( object );
			}
			_objects.remove( id );
		}

		final List<Light> lights = _lights.get( id );
		if ( lights != null )
		{
			for ( final Light light : lights )
			{
				removeLight( light );
			}
			_lights.remove( id );
		}

		removeAmbientLight( id );
	}

	private void removeAmbientLight( final Object id )
	{
		final Integer nodeAmbientLight = _ambientLights.remove( id );
		if ( nodeAmbientLight != null )
		{
			final int ambientLight = _ambientLight - nodeAmbientLight;
			setAmbientLight( ambientLight );
		}
	}

	public ViewModelView createView( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		final JPCTView view = new JPCTView( this , id , _backgroundColor );
		addView( view );
		return view;
	}

	/**
	 * Returns the world containing the objects represented by this model.
	 *
	 * @return  World for this model.
	 */
	public World getWorld()
	{
		return _world;
	}

	/**
	 * Applies any updates made to the view model to the world. This method must
	 * only be called from the renderer thread (which is why this method exists
	 * in the first place). Modifications to the world cause problems when
	 * performed from outside the renderer thread.
	 *
	 * @return  <code>true</code> if the world was changed;
	 *          <code>false</code> otherwise, i.e. if no changes were needed.
	 */
	public boolean updateWorld()
	{
		final boolean result = !_modifications.isEmpty();

		if ( result )
		{
			for ( final Modification modification : _modifications )
			{
				modification.perform();
			}
		}

		return result;
	}

	/**
	 * Base class for classes that specify a modification to the model.
	 */
	private abstract static class Modification
	{
		protected final ViewModelNode _node;

		/**
		 * Constructs a modification for the given node.
		 *
		 * @param   node    Node that the modifications is related to.
		 */
		protected Modification( final ViewModelNode node )
		{
			_node = node;
		}

		/**
		 * Performs the modification in the current thread.
		 */
		protected abstract void perform();
	}

	/**
	 * Represents the addition of a node to the model.
	 */
	private class Addition
		extends Modification
	{
		/**
		 * Constructs an addition of the given node.
		 *
		 * @param   node    Added node.
		 */
		Addition( final ViewModelNode node )
		{
			super( node );
		}

		protected void perform()
		{
			initializeNodeImpl( _node );
		}
	}

	/**
	 * Represents an update of a node in the model.
	 */
	private class Update
		extends Modification
	{
		/**
		 * Constructs an update for the given node.
		 *
		 * @param   node    Updated node.
		 */
		Update( final ViewModelNode node )
		{
			super( node );
		}

		protected void perform()
		{
			updateNodeContentsImpl( _node );
		}
	}

	/**
	 * Represents the removal of a node from the model.
	 */
	private class Removal
		extends Modification
	{
		/**
		 * Constructs a removal of the given node.
		 *
		 * @param   node    Removed node.
		 */
		Removal( final ViewModelNode node )
		{
			super( node );
		}

		protected void perform()
		{
			removeNodeImpl( _node );
		}
	}
}
