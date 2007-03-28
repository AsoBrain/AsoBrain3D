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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.threed.jpct.Matrix;
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
	 * Maps node ID ({@link Object}) to jPCT nodes, such as
	 * {@link com.threed.jpct.Object3D} and {@link Light}.
	 */
	private final Map _nodeMap;

	/**
	 * There appears to be no getter for childs of an
	 * {@link com.threed.jpct.Object3D}, so they are stored here.
	 */
	private final Map _childNodes;

	/**
	 * Background color of the frame buffer.
	 */
	private Color _backgroundColor;

	/**
	 * Construct new jPCT model.
	 *
	 * @param   unit        Unit scale factor (e.g. {@link ViewModel.MM}).
	 * @param   background  Background color of the frame buffer.
	 */
	public JPCTModel( final double unit , final Color background )
	{
		super( unit );
		_backgroundColor = background;

		final World world = new World();
		_world = world;

		final int ambient = 75;// -100;
		world.setAmbientLight( ambient , ambient , ambient );

		_nodeMap    = new HashMap();
		_childNodes = new HashMap();
	}

	protected void initializeNode( final ViewModelNode node )
	{
		final Node3D node3D = node.getNode3D();

		final Map nodeMap = _nodeMap;
		final Object existing = nodeMap.get( node.getID() );
		if ( existing instanceof com.threed.jpct.Object3D )
		{
			removeObject( (com.threed.jpct.Object3D)existing );
		}

		final Matrix3D object2world = node.getTransform();

		final World world = _world;

		if ( node3D instanceof Object3D )
		{
			final com.threed.jpct.Object3D object = JPCTTools.convert2Object3D( (Object3D)node3D );
			setTransformation( object , object2world );

			world.addObject( object );
			nodeMap.put( node.getID() , object );
		}
		else if ( node3D instanceof Light3D )
		{
			/* Lights can't be removed, so they are re-used. */
			if ( existing == null )
			{
				final Light jpctLight = new Light( world );
				nodeMap.put( node.getID() , jpctLight );
			}
		}
		else
		{
			final com.threed.jpct.Object3D jpctObject = com.threed.jpct.Object3D.createDummyObj();
			jpctObject.setVisibility( false );

			world.addObject( jpctObject );

			final Node3DCollection leafs = new Node3DCollection();
			node3D.gatherLeafs( leafs , Object3D.class , object2world , false );

			final List childNodes = new ArrayList( leafs.size() );
			for ( int i = 0 ; i < leafs.size() ; i++ )
			{
				final Object3D leaf      = (Object3D)leafs.getNode( i );
				final Matrix3D transform = leafs.getMatrix( i );

				final com.threed.jpct.Object3D leaf3D = JPCTTools.convert2Object3D( leaf );
				setTransformation( leaf3D , transform );

				world.addObject( leaf3D );
				jpctObject.addChild( leaf3D );
				childNodes.add( leaf3D );
			}
			_childNodes.put( jpctObject , childNodes );

			nodeMap.put( node.getID() , jpctObject );
		}
	}

	private static void setTransformation( final com.threed.jpct.Object3D object3D , final Matrix3D object2world )
	{
		final Matrix matrix = JPCTTools.convert2Matrix( object2world );

		final SimpleVector translationVector = matrix.getTranslation();
		final Matrix translation = new Matrix();
		translation.translate( translationVector );

		final Matrix rotation = matrix.cloneMatrix();
		translationVector.scalarMul( -1.0f );
		rotation.translate( translationVector );

		object3D.setRotationMatrix( rotation );
		object3D.setTranslationMatrix( translation );
	}

	protected void updateNodeTransform( final ViewModelNode node )
	{
		final Matrix3D transform = node.getTransform();
		final Object   jpctNode  = _nodeMap.get( node.getID() );

		if ( jpctNode instanceof com.threed.jpct.Object3D )
		{
			setTransformation( (com.threed.jpct.Object3D)jpctNode , transform );
		}
		else if ( jpctNode instanceof Light )
		{
			final Light light = (Light)jpctNode;
			light.setPosition( new SimpleVector( (float)transform.xo , (float)transform.yo , (float)transform.zo ) );
		}
	}

	protected void updateNodeContent( final ViewModelNode node )
	{
		final Node3D node3D   = node.getNode3D();
		final Object jpctNode = _nodeMap.get( node.getID() );

		if ( jpctNode instanceof com.threed.jpct.Object3D )
		{
			initializeNode( node );
		}
		else if ( jpctNode instanceof Light )
		{
			final Light3D  light     = (Light3D)node3D;
			final Matrix3D transform = node.getTransform();

			final Light jpctLight = (Light)jpctNode;
			final float intensity = (float)light.getIntensity() * 10.0f / 255.0f;
			jpctLight.setPosition( new SimpleVector( transform.xo , transform.yo , transform.zo ) );
			jpctLight.setIntensity( intensity , intensity , intensity );
			jpctLight.setAttenuation( (float)light.getFallOff() );
		}
	}

	private void removeObject( final com.threed.jpct.Object3D object3D )
	{
		final List childNodes = (List)_childNodes.remove( object3D );
		if ( childNodes != null )
		{
			for ( Iterator i = childNodes.iterator() ; i.hasNext() ; )
			{
				final com.threed.jpct.Object3D childNode = (com.threed.jpct.Object3D)i.next();
				_world.removeObject( childNode );
			}
			childNodes.clear();
		}
		_world.removeObject( object3D );
	}

	public ViewModelView createView( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		final JPCTView view = new JPCTView( this , id , _backgroundColor );
		addView( view );
		return view;
	}

	public World getWorld()
	{
		return _world;
	}
}
