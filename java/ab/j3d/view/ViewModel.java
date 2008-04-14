/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2008
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import ab.j3d.Bounds3D;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.CameraControl;
import ab.j3d.control.FromToCameraControl;
import ab.j3d.control.OrbitCameraControl;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.Projector.ProjectionPolicy;
import ab.j3d.view.ViewModelView.RenderingPolicy;
import ab.j3d.view.control.ViewModelNodeControl;

import com.numdata.oss.ui.ActionTools;

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
	 * Returns the ID object for a {@link Node3D}. If the node is not part of
	 * this model, <code>null</code> is returned. <p>
	 * Note that the top most parent of the given node is the node for which the
	 * id is returned. This is because only these nodes have an id. This means
	 * two {@link Object3D}s can have the same id, because of the way the
	 * application has created the model.
	 *
	 * @param   node    The {@link Node3D} for which the id is required
	 *
	 * @return  The ID of the given node
	 */
	public final Object getID( final Node3D node )
	{
		Object result = null;

		Node3D topNode = node;
		while ( topNode.getParent() != null )
			topNode = topNode.getParent();

		for ( final ViewModelNode modelNode : _nodes )
		{
			if ( modelNode.getNode3D() == topNode )
			{
				result = modelNode.getID();
				if ( result != null )
					break;
			}
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
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final void createNode( final Object id , final Node3D node3D , final Material materialOverride , final float opacity )
	{
		createNode( id , null , node3D , materialOverride , opacity );
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
	 * Get view view by ID. This method is only supposed to be used internally.
	 *
	 * @param   id      Application-assigned ID of view model view.
	 *
	 * @return  View with the specified ID;
	 *          <code>null</code> if no view with the specified ID was found.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final ViewModelView getView( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		ViewModelView result = null;

		for ( final ViewModelView view : _views )
		{
			if ( id.equals( view.getID() ) )
			{
				result = view;
				break;
			}
		}

		return result;
	}

	/**
	 * Get view component for the specified view node.
	 *
	 * @param   id      Application-assigned ID of view.
	 *
	 * @return  Component with graphical representation of view;
	 *          <code>null</code> if the requested view was not found.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final Component getViewComponent( final Object id )
	{
		final ViewModelView view = getView( id );
		return ( view != null ) ? view.getComponent() : null;
	}

	/**
	 * Get ID's of all views for the model.
	 *
	 * @return  Array containing ID's of all views for the model.
	 */
	public final Object[] getViewIDs()
	{
		final Object[] result;

		final List<ViewModelView> views = _views;

		result = new Object[ views.size() ];
		for ( int i = 0 ; i < result.length ; i++ )
		{
			final ViewModelView view = views.get( i );
			result[ i ] = view.getID();
		}

		return result;
	}

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
	 * @param   id                      Application-assigned ID for the new view.
	 * @param   renderingPolicy         Desired rendering policy for view.
	 * @param   projectionPolicy        Desired projection policy for view.
	 * @param   estimatedSceneBounds    Estimated bounding box of scene.
	 * @param   viewDirection           Direction from which to view the scene.
	 *
	 * @return  View that was created.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final ViewModelView createView( final Object id , final RenderingPolicy renderingPolicy , final ProjectionPolicy projectionPolicy , final Bounds3D estimatedSceneBounds , final Vector3D viewDirection )
	{
		final ViewModelView view = createView( id );

		final double   sceneSize = Vector3D.distanceBetween( estimatedSceneBounds.v1 , estimatedSceneBounds.v2 );
		final Vector3D center    = estimatedSceneBounds.center();

		if ( ( renderingPolicy != RenderingPolicy.SOLID ) || ( projectionPolicy != ProjectionPolicy.PERSPECTIVE ) )
		{
			final double x = -center.x + viewDirection.x * sceneSize;
			final double y = -center.y + viewDirection.y * sceneSize;
			final double z = -center.z + viewDirection.z * sceneSize;

			final double rx = Math.toDegrees( Math.atan2( -viewDirection.y , -viewDirection.z ) );
			final double ry = Math.toDegrees( Math.atan2( -viewDirection.x , -viewDirection.z ) );

			view.setCameraControl( new OrbitCameraControl( view , rx , ry , 0.0 , x , y , z ) );
		}
		else
		{
			final Vector3D from = center.minus( viewDirection.multiply( sceneSize * 2.0 ) );
			view.setCameraControl( new FromToCameraControl( view , from , center ) );
		}

		view.setRenderingPolicy( renderingPolicy );
		view.setProjectionPolicy( projectionPolicy );

		return view;
	}

	/**
	 * Create a new view for this model.
	 *
	 * @param   id  Application-assigned ID for the new view.
	 *
	 * @return  View that was created.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public abstract ViewModelView createView( final Object id );

	/**
	 * Create view panel for the specified view.
	 *
	 * @param   locale      Locale for internationalized user interface.
	 * @param   id          Application-assigned ID of existing view.
	 *
	 * @return  Panel containing view.
	 *
	 * @throws  IllegalArgumentException if no view with the specified <code>id</code> was found.
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final JPanel createViewPanel( final Locale locale , final Object id )
	{
		final ViewModelView view = getView( id );
		if ( view == null )
			throw new IllegalArgumentException( String.valueOf( id ) );

		final String label = view.getLabel();

		final JToolBar toolbar = new JToolBar( label );
		if ( label != null )
		{
			toolbar.add( new JLabel( view.getLabel() + ": " ) );
		}

		final CameraControl cameraControl = view.getCameraControl();
		if ( cameraControl != null )
		{
			ActionTools.addToToolBar( toolbar , cameraControl.getActions( locale ) );
		}

		ActionTools.addToToolBar( toolbar , view.getActions( locale ) );

		final JPanel result = new JPanel( new BorderLayout() );
		result.add( view.getComponent() , BorderLayout.CENTER );
		result.add( toolbar , BorderLayout.NORTH );
		return result;
	}

	/**
	 * Test if view model contains a view with the specified ID.
	 *
	 * @param   id      Application-assigned ID of view model view.
	 *
	 * @return  <code>true</code> if a view with the specified ID was found;
	 *          <code>false</code> if the model contains no such view.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final boolean hasView( final Object id )
	{
		return ( getView( id ) != null );
	}

	/**
	 * Remove view model view.
	 *
	 * @param   id      Application-assigned ID for a view model view.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final void removeView( final Object id )
	{
		final ViewModelView view = getView( id );
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
	 *
	 * @see     #removeView
	 * @see     #getViewIDs
	 */
	public final void removeAllViews()
	{
		for ( final Object id : getViewIDs() )
			removeView( id );
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
