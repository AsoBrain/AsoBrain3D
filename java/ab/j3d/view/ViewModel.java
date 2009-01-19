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
package ab.j3d.view;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.view.control.ViewModelNodeControl;

/**
 * The view model provides a high level interface between an application and a
 * 3D environment. The user able to interact with the view(s) of this model.
 * <p />
 * The following entities are defined by this model:
 * <dl>
 *   <dt>View model ({@link ViewModel}, this class)</dt>
 *   <dd>
 *     This is the main application interface. The main application functions
 *     are adding, updating, and nodes or views, and handling user interaction.
 *   </dd>
 *   <dt>Node ({@link ViewModelNode})</dt>
 *   <dd>
 *     A node links an application-assigned object (<code>ID</code> : {@link Object}) to
 *     a part of the 3D scene (<code>node</code> : {@link Node3D}). The application
 *     defines how the specified object is represented in 3D, and allows it to be
 *     placed in the 3D environment. A transform can be applied to define the
 *     location, orientation, and scale of the object.
 *   </dd>
 *   <dt>View ({@link ViewModelView})</dt>
 *   <dd>
 *     A view provides a graphical representation of the 3D scene
 *     (<code>component</code> : {@link Component}). Multiple views may be
 *     defined for a single view model; each identified by an
 *     application-assigned object (<code>ID</code> : {@link Object}).
 *   </dd>
 * </dl>
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class ViewModel
{
	/**
	 * List of nodes (element type: {@link ViewModelNode}).
	 */
	private final List<ViewModelNode> _nodes = new ArrayList<ViewModelNode>();

	/**
	 * List of views (element type: {@link ViewModelView}).
	 */
	private final List<ViewModelView> _views = new ArrayList<ViewModelView>();

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
	 * Unit scale factor in this model. This scale factor, when multiplied,
	 * converts design units to meters.
	 */
	protected final double _unit;

	/**
	 * Shared listener for node updates.
	 */
	private final ViewModelNodeUpdateListener _viewModelNodeUpdateListener = new ViewModelNodeUpdateListener()
		{
			public void contentsUpdated( final ViewModelNodeUpdateEvent event )
			{
				updateNodeContent( (ViewModelNode)event.getSource() );
			}

			public void renderingPropertiesUpdated( final ViewModelNodeUpdateEvent event )
			{
				updateViews();
			}

			public void transformUpdated( final ViewModelNodeUpdateEvent event )
			{
				updateNodeTransform( (ViewModelNode)event.getSource() );
			}
		};

	/**
	 * Shared control / overlay painter for view nodes.
	 */
	private final ViewModelNodeControl _viewModelNodeControl = new ViewModelNodeControl( this );

	/**
	 * Construct new view model.
	 *
	 * @param   unit    Unit scale factor (e.g. {@link ViewModel#MM}).
	 */
	protected ViewModel( final double unit )
	{
		_unit = unit;
	}

	/**
	 * Get node by ID. This method is only supposed to be used internally.
	 *
	 * @param   id      Application-assigned ID of view model node.
	 *
	 * @return  Node with the specified ID;
	 *          <code>null</code> if no node with the specified ID was found.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final ViewModelNode getNode( final Object id )
	{
		ViewModelNode result = null;

		if ( id == null )
			throw new NullPointerException( "id" );

		for ( final ViewModelNode node : _nodes )
		{
			if ( id.equals( node.getID() ) )
			{
				result = node;
				break;
			}
		}

		return result;
	}

	/**
	 * Get all nodes in the model.
	 *
	 * @return  Unmodifiable list of nodes in the model.
	 */
	public final List<ViewModelNode> getNodes()
	{
		return Collections.unmodifiableList( _nodes );
	}

	/**
	 * Get ID's of all nodes in the model.
	 *
	 * @return  Array containing ID's of all nodes in the model.
	 */
	public final Object[] getNodeIDs()
	{
		final List<ViewModelNode> nodes = _nodes;

		final Object[] result = new Object[ nodes.size() ];

		for ( int i = 0 ; i < result.length ; i++ )
		{
			final ViewModelNode node = nodes.get( i );
			result[ i ] = node.getID();
		}

		return result;
	}

	/**
	 * Get root 3D scene node for the specified node.
	 *
	 * @param   id      Application-assigned ID of node.
	 *
	 * @return  Root in the 3D scene associated with the specified node;
	 *          <code>null</code> if the requested node was not found.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final Node3D getNode3D( final Object id )
	{
		final ViewModelNode node = getNode( id );
		return ( node != null ) ? node.getNode3D() : null;
	}

	/**
	 * Add view model node. This method is only supposed to be used internally.
	 *
	 * @param   node    Node to add.
	 *
	 * @throws  NullPointerException if <code>node</code> is <code>null</code>.
	 */
	protected final void addNode( final ViewModelNode node )
	{
		if ( node == null )
			throw new NullPointerException( "node" );

		if ( !_nodes.contains( node ) )
		{
			_nodes.add( node );
			node.addViewModelNodeUpdateListener( _viewModelNodeUpdateListener );
			updateViews();
		}
	}

	/**
	 * Create a new node (sub tree) in the view tree. If a node with the same ID
	 * already exists, that node will be removed before creating the new node.
	 *
	 * @param   id                  Application-assigned ID of view model node.
	 * @param   node3D              Root in the 3D scene to create a view model node for.
	 * @param   materialOverride    Material to use instead of actual materials.
	 * @param   opacity             Extra opacity (0.0=translucent, 1.0=opaque).);
	 *
	 * @return  Node that was created.
	 *
	 * @throws  NullPointerException if <code>id</code> or <code>node3D<code> is <code>null</code>
	 */
	public final ViewModelNode createNode( final Object id , final Node3D node3D , final Material materialOverride , final float opacity )
	{
		return createNode( id , null , node3D , materialOverride , opacity );
	}

	/**
	 * Create a new node (sub tree) in the view tree with the specified initial
	 * transform. If a node with the same ID already exists, that node will be
	 * removed before creating the new node.
	 *
	 * @param   id                  Application-assigned ID of view model node.
	 * @param   transform           Initial transform (<code>null</code> => identity).
	 * @param   node3D              Root in the 3D scene to create a view model node for.
	 * @param   materialOverride    Material to use instead of actual materials.
	 * @param   opacity             Extra opacity (0.0=translucent, 1.0=opaque).);
	 *
	 * @return  Node that was created.
	 *
	 * @throws  NullPointerException if <code>id</code> or <code>node3D</code> is <code>null</code>.
	 */
	public final ViewModelNode createNode( final Object id , final Matrix3D transform , final Node3D node3D , final Material materialOverride , final float opacity )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		if ( node3D == null )
			throw new NullPointerException( "node3D" );

		removeNode( id );

		final ViewModelNode node = new ViewModelNode( id , transform , node3D , materialOverride , opacity );
		initializeNode( node );
		addNode( node );
		return node;
	}

	/**
	 * Test if view model contains a node with the specified ID.
	 *
	 * @param   id      Application-assigned ID of view model node.
	 *
	 * @return  <code>true</code> if a node with the specified ID was found;
	 *          <code>false</code> if the model contains no such node.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final boolean hasNode( final Object id )
	{
		return ( getNode( id ) != null );
	}

	/**
	 * Remove view model node.
	 *
	 * @param   id      Application-assigned ID of view model node.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public void removeNode( final Object id )
	{
		final ViewModelNode node = getNode( id );
		if ( node != null )
		{
			node.removeViewModelNodeUpdateListener( _viewModelNodeUpdateListener );
			_nodes.remove( node );
			updateViews();
		}
	}

	/**
	 * Remove all view model nodes from this model.
	 *
	 * @see     #removeNode
	 * @see     #getNodeIDs
	 */
	public final void removeAllNodes()
	{
		for ( final Object id : getNodeIDs() )
			removeNode( id );
	}

	/**
	 * This method initializes the contents in the view for the specified node.
	 * Its implemententation depends largely on the view model implementation,
	 * which typically sets up resources in the underlying render engine for the
	 * node.
	 *
	 * @param   node    Node being set up (never <code>null</code>).
	 */
	protected abstract void initializeNode( ViewModelNode node );

	/**
	 * This method updates the transform in the view for the specified node. Its
	 * implemententation depends largely on the view model implementation, which
	 * typically translates and updates the transform in the underlying render
	 * engine.
	 *
	 * @param   node    Node being updated (never <code>null</code>).
	 */
	protected abstract void updateNodeTransform( ViewModelNode node );

	/**
	 * This method updates the overlay. Its implemententation depends largely on
	 * the view model implementation, which typically re-renders the unaltered
	 * 3D background scene and calls all {@link OverlayPainter}s again.
	 */
	public abstract void updateOverlay();

	/**
	 * This method updates the contents in the view for the specified node. Its
	 * implemententation depends largely on the view model implementation, which
	 * typically translates the content of a node to a form suitable for the
	 * underlying render engine.
	 *
	 * @param   node    Node being updated (never <code>null</code>).
	 */
	protected abstract void updateNodeContent( ViewModelNode node );

	/**
	 * Add view model view. This method is only supposed to be used internally.
	 *
	 * @param   view    View to add.
	 *
	 * @throws  NullPointerException if view is null.
	 */
	protected final void addView( final ViewModelView view )
	{
		if ( view == null )
			throw new NullPointerException( "view" );

		if ( !_views.contains( view ) )
		{
			final ViewModelNodeControl viewModelNodeControl = _viewModelNodeControl;
			view.appendControl( viewModelNodeControl );
			view.addOverlayPainter( viewModelNodeControl );

			_views.add( view );
		}
	}

	/**
	 * Create a new view for this model.
	 *
	 * @return  View that was created.
	 */
	public abstract ViewModelView createView();

	/**
	 * Test if view model contains a view model view.
	 *
	 * @param   view    view model view to test for.
	 *
	 * @return  <code>true</code> if a view with the specified ID was found;
	 *          <code>false</code> if the model contains no such view.
	 *
	 * @throws  NullPointerException if <code>view</code> is <code>null</code>.
	 */
	public final boolean hasView( final ViewModelView view )
	{
		return ( _views.contains( view ) );
	}

	/**
	 * Remove view model view.
	 *
	 * @param   view   view model view to remove.
	 *
	 * @throws  NullPointerException if <code>view</code> is <code>null</code>.
	 */
	public final void removeView( final ViewModelView view )
	{
		if ( view != null )
		{
			final ViewModelNodeControl viewModelNodeControl = _viewModelNodeControl;
			view.removeControl( viewModelNodeControl );
			view.removeOverlayPainter( viewModelNodeControl );
			_views.remove( view );
		}
	}

	/**
	 * Remove all view model views from this model.
	 */
	public final void removeAllViews()
	{
		for ( final ViewModelView view : _views )
		{
			final ViewModelNodeControl viewModelNodeControl = _viewModelNodeControl;
			view.removeControl( viewModelNodeControl );
			view.removeOverlayPainter( viewModelNodeControl );
		}

		_views.clear();
	}

	/**
	 * Update all views.
	 */
	public void updateViews()
	{
		for ( final ViewModelView view : _views )
		{
			view.update();
		}
	}

	/**
	 * Get all nodes in the scene being viewed.
	 *
	 * @return  A {@link Node3DCollection} with all nodes in the scene
	 *          (may be empty, but never <code>null</code>).
	 */
	public Node3DCollection<Node3D> getScene()
	{
		final Node3DCollection<Node3D> result = new Node3DCollection<Node3D>();

		for ( final ViewModelNode node : _nodes )
		{
			final Node3D node3D = node.getNode3D();
			node3D.collectNodes( result , Node3D.class , node.getTransform() , false );
		}

		return result;
	}

	/**
	 * Unit scale factor in this model in meters per unit. This factor, when
	 * multiplied, converts units to meters.
	 *
	 * @return  Unit scale (meters per unit).
	 */
	public double getUnit()
	{
		return _unit;
	}
}
