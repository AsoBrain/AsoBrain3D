/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import ab.j3d.Bounds3D;
import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.control.Control;
import ab.j3d.model.Node3D;

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
	private final List _nodes = new ArrayList();

	/**
	 * List of views (element type: {@link ViewModelView}).
	 */
	private final List _views = new ArrayList();

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
	 * Construct new ViewModel.
	 */
	protected ViewModel()
	{
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

		final List nodes = _nodes;
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final ViewModelNode node = (ViewModelNode)nodes.get( i );
			if ( id == node.getID() )
			{
				result = node;
				break;
			}
		}

		return result;
	}

	/**
	 * Get ID's of all nodes in the model.
	 *
	 * @return  Array containing ID's of all nodes in the model.
	 */
	public final Object[] getNodeIDs()
	{
		final Object[] result;

		final List nodes = _nodes;

		result = new Object[ nodes.size() ];
		for ( int i = 0 ; i < result.length ; i++ )
			result[ i ] = ((ViewModelNode)nodes.get( i ) ).getID();

		return result;
	}

	/**
	 * Returns the ID object for a {@link Node3D}. If the node is not part of this
	 * model, <code>null</code> is returned.
	 *
	 * @param   node    The {@link Node3D} for which the id is required
	 *
	 * @return  The ID of the given node
	 */
	public final Object getID( final Node3D node )
	{
		Object result = null;

		final List nodes = _nodes;

		for ( int i = 0; i < nodes.size() && result == null; i++ )
		{
			final ViewModelNode modelNode = (ViewModelNode)nodes.get( i );
			if ( modelNode.getNode3D() == node )
			{
				result = modelNode.getID();
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
			_nodes.add( node );
	}

	/**
	 * Create a new node (sub tree) in the view tree. The specified
	 * {@link Node3D} is converted into view and then added to the view
	 * tree. If an existing node exists with the same ID, then the existing
	 * node will be removed before creating the new node.
	 *
	 * @param   id                  Application-assigned ID of view model node.
	 * @param   node3D              Root in the 3D scene to create a view model node for.
	 * @param   textureOverride     Texture to use instead of actual textures.
	 * @param   opacity             Extra opacity (0.0=translucent, 1.0=opaque).);
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final void createNode( final Object id , final Node3D node3D , final TextureSpec textureOverride , final float opacity )
	{
		createNode( id , null , node3D , textureOverride , opacity );
	}

	/**
	 * Create a new node (sub tree) in the view tree with the specified initial
	 * transform. The specified {@link Node3D} is converted into view and then
	 * added to the view tree. If an existing node exists with the same ID, then
	 * the existing node will be removed before creating the new node.
	 *
	 * @param   id                  Application-assigned ID of view model node.
	 * @param   transform           Initial transform (<code>null</code> => identity).
	 * @param   node3D              Root in the 3D scene to create a view model node for.
	 * @param   textureOverride     Texture to use instead of actual textures.
	 * @param   opacity             Extra opacity (0.0=translucent, 1.0=opaque).);
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final void createNode( final Object id , final Matrix3D transform , final Node3D node3D , final TextureSpec textureOverride , final float opacity )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		removeNode( id );

		if ( node3D != null )
		{
			final ViewModelNode node = new ViewModelNode( id , transform , node3D , textureOverride , opacity );
			initializeNode( node );
			addNode( node );
			updateNodeContent( node );
		}
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
			_nodes.remove( id );
	}

	/**
	 * Remove all view model nodes from this model.
	 *
	 * @see     #removeNode
	 * @see     #getNodeIDs
	 */
	public final void removeAllNodes()
	{
		final Object[] ids = getNodeIDs();
		for ( int i = 0 ; i < ids.length ; i++ )
			removeNode( ids[ i ] );
	}

	/**
	 * Set the transform for a specified part (sub tree) of the view tree.
	 *
	 * @param   id          Application-assigned ID of view model node.
	 * @param   transform   Transform to set.
	 *
	 * @throws  NullPointerException if an argument is <code>null</code>.
	 */
	public final void setNodeTransform( final Object id , final Matrix3D transform )
	{
		final ViewModelNode node = getNode( id );
		if ( node == null )
			throw new IllegalArgumentException( "ID '" + id + "' does not refer to a known view model node" );

		node.setTransform( transform );
		updateNodeTransform( node );
	}

	/**
	 * Update a specified part (sub tree) of the view tree.
	 *
	 * @param   id      Application-assigned ID of view model node.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final void updateNode( final Object id )
	{
		final ViewModelNode node = getNode( id );
		if ( node == null )
			throw new IllegalArgumentException( "ID '" + id + "' does not refer to a known view model node" );

		updateNodeContent( node );
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

		for ( int i = 0 ; i < _views.size() ; i++ )
		{
			final ViewModelView view = (ViewModelView) _views.get( i );
			if ( id == view.getID() )
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
	 * Get view control for the specified view node.
	 *
	 * @param   id      Application-assigned ID of view.
	 *
	 * @return  ViewControl that controls the specified view;
	 *          <code>null</code> if the requested view was not found.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final ViewControl getViewControl( final Object id )
	{
		final ViewModelView view = getView( id );
		return ( view != null ) ? view.getViewControl() : null;
	}

	/**
	 * Get ID's of all views for the model.
	 *
	 * @return  Array containing ID's of all views for the model.
	 */
	public final Object[] getViewIDs()
	{
		final Object[] result;

		final List views = _views;

		result = new Object[ views.size() ];
		for ( int i = 0 ; i < result.length ; i++ )
			result[ i ] = ((ViewModelView)views.get( i )).getID();

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
			_views.add( view );
	}

	/**
	 * Create a new view for this model.
	 *
	 * @param   id                      Application-assigned ID for the new view.
	 * @param   renderingPolicy         Desired rendering policy for view
	 *                                  ({@link ViewModelView#SOLID},
	 *                                  {@link ViewModelView#SCHEMATIC},
	 *                                  {@link ViewModelView#SKETCH}, or
	 *                                  {@link ViewModelView#WIREFRAME}).
	 * @param   projectionPolicy        Desired projection policy for view
	 *                                  ({@link Projector#PERSPECTIVE},
	 *                                  {@link Projector#PARALLEL}, or
	 *                                  {@link Projector#ISOMETRIC}).
	 * @param   estimatedSceneBounds    Estimated bounding box of scene.
	 * @param   viewDirection           Direction from which to view the scene.
	 *
	 * @return  View component for create view.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final Component createView( final Object id , final int renderingPolicy , final int projectionPolicy , final Bounds3D estimatedSceneBounds , final Vector3D viewDirection )
	{
		final ViewControl viewControl;

		final double   sceneSize = Vector3D.distanceBetween( estimatedSceneBounds.v1 , estimatedSceneBounds.v2 );
		final Vector3D center    = estimatedSceneBounds.center();

		if ( ( renderingPolicy != ViewModelView.SOLID ) || ( projectionPolicy != Projector.PERSPECTIVE ) )
		{
			final double x = -center.x + viewDirection.x * sceneSize;
			final double y = -center.y + viewDirection.y * sceneSize;
			final double z = -center.z + viewDirection.z * sceneSize;

			final double rx = Math.toDegrees( Math.atan2( -viewDirection.y , -viewDirection.z ) );
			final double ry = Math.toDegrees( Math.atan2( -viewDirection.x , -viewDirection.z ) );

			viewControl = new OrbitViewControl( rx , ry , 0.0 , x , y , z );
		}
		else
		{
			final Vector3D from = center.minus( viewDirection.multiply( sceneSize * 2.0 ) );
			viewControl = new FromToViewControl( from , center );
		}

		final Component result = createView( id , viewControl );

		final ViewModelView view = getView( id );
		view.setRenderingPolicy( renderingPolicy );
		view.setProjectionPolicy( projectionPolicy );

		return result;
	}

	/**
	 * Create a new view for this model.
	 *
	 * @param   id              Application-assigned ID for the new view.
	 * @param   viewControl     Control to use for this view.
	 *
	 * @return  View component for create view.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public abstract Component createView( final Object id , final ViewControl viewControl );

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

		final Component   viewComponent = view.getComponent();
		final ViewControl viewControl   = view.getViewControl();

		final JPanel result = new JPanel( new BorderLayout() );
		result.add( viewComponent , BorderLayout.CENTER );

		final JToolBar toolbar = ActionTools.createToolbar( null , viewControl.getActions( locale ) );
		if ( toolbar != null )
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
			_views.remove( view );
	}

	/**
	 * Remove all view model views from this model.
	 *
	 * @see     #removeView
	 * @see     #getViewIDs
	 */
	public final void removeAllViews()
	{
		final Object[] ids = getViewIDs();
		for ( int i = 0 ; i < ids.length ; i++ )
			removeView( ids[ i ] );
	}

	/**
	 * Update all views.
	 */
	public final void updateViews()
	{
		for ( int i = 0 ; i < _views.size() ; i++ )
		{
			final ViewModelView view = (ViewModelView) _views.get( i );
			view.update();
		}
	}

	/**
	 * Returns wether or not this view supports {@link Control}s. This should be
	 * checked before adding any {@link Control}s to a view.
	 *
	 * @return  <code>true</code> if this view supports {@link Control}s,
	 *          <code>false</code> if not.
	 */
	public abstract boolean supportsControls();

	/**
	 * Add a {@link Control} to all {@link ViewModelView}s currently added to
	 * the model. If a new view is added afterwards, the control is not added to
	 * that view.<p>
	 * Note that not all views support controls. This can be checked with the
	 * method {@link #supportsControls()}.
	 *
	 * @param   control     The {@link Control} to add
	 */
	public final void addControl( final Control control )
	{
		if ( supportsControls() )
		{
			for ( int i = 0 ; i < _views.size() ; i++ )
			{
				final ViewModelView view = (ViewModelView) _views.get( i );
				view.addControl( control );
			}
		}
	}

	/**
	 * Remove a {@link Control} from all {@link ViewModelView}s currently added
	 * to the model.
	 *
	 * @param   control     The {@link Control} to remove
	 */
	public final void removeControl( final Control control)
	{
		if ( !supportsControls() )
		{
			for ( int i = 0 ; i < _views.size() ; i++ )
			{
				final ViewModelView view = (ViewModelView) _views.get( i );
				view.removeControl( control );
			}
		}
	}
}
