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

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Node3D;

import com.numdata.oss.ui.ActionTools;

/**
 * The view model provides a high level interface between an application and a
 * 3D environment. The user able to interact with the view(s) of this model.
 * <p />
 * The following entities are defined by this model:
 * <dl>
 *   <dt>View model (<code>ViewModel</code>, this class)</dt>
 *   <dd>
 *     This is the main application interface. The main application functions
 *     are adding, updating, and nodes or views, and handling user interaction.
 *   </dd>
 *   <dt>Node (<code>ViewModelNode</code>)</dt>
 *   <dd>
 *     A node links an application-assigned object (<code>ID : Object</code>) to
 *     a part of the 3D scene (<code>abRootNode : Node3D</code>). The application
 *     defines how the specified object is represented in 3D, and allows it to be
 *     placed in the 3D environment. A transform can be applied to define the
 *     location, orientation, and scale of the object.
 *   </dd>
 *   <dt>View (<code>ViewModelView</code>)</dt>
 *   <dd>
 *     A view provides a graphical representation of the 3D scene
 *     (<code>component : Component</code>). Multiple views may be
 *     defined for a single view model; each identified by an
 *     application-assigned object (<code>ID : Object</code>).
 *   </dd>
 * </dl>
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class ViewModel
{
	/**
	 * List of nodes (element type: <code>ViewModelNode</code>).
	 */
	private final List _nodes = new ArrayList();

	/**
	 * List of views (element type: <code>ViewModelView</code>).
	 */
	private final List _views = new ArrayList();

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
	protected final ViewModelNode getNode( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		ViewModelNode result = null;

		for ( int i = 0 ; i < _nodes.size() ; i++ )
		{
			final ViewModelNode node = (ViewModelNode) _nodes.get( i );
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
	 * @return  List containing ID's of all nodes in the model.
	 */
	public final List getNodeIDs()
	{
		final List result = new ArrayList( _nodes.size() );
		for ( int i = 0 ; i < _nodes.size() ; i++ )
			result.add( ((ViewModelNode)_nodes.get( i ) ).getID() );

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
	protected void addNode( final ViewModelNode node )
	{
		if ( node == null )
			throw new NullPointerException( "node" );

		if ( !_nodes.contains( node ) )
			_nodes.add( node );
	}

	/**
	 * Create a new node (sub tree) in the view tree. The specified
	 * <code>node3D</code> is converted into view and then added to the view
	 * tree. If an existing node exists with the same ID, then the existing
	 * node will be removed before creating the new node.
	 *
	 * @param   id      Application-assigned ID of view model node.
	 * @param   node3D  Root in the 3D scene to create a view model node for.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public abstract void createNode( Object id , Node3D node3D );

	/**
	 * Remove view model node.
	 *
	 * @param   id      Application-assigned ID of view model node.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public void removeNode( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		_nodes.remove( id );
	}

	/**
	 * Set the transform for a specified part (sub tree) of the view tree.
	 *
	 * @param   id          Application-assigned ID of view model node.
	 * @param   transform   Transform to set.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final void setNodeTransform( final Object id , final Matrix3D transform )
	{
		final ViewModelNode node = getNode( id );
		if ( node == null )
			throw new IllegalArgumentException( "ID '" + id + "' does not refer to a known view model node" );

		node.setTransform( transform );
	}

	/**
	 * Update a specified part (sub tree) of the view tree.
	 *
	 * @param   id          Application-assigned ID of view model node.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public void updateNode( final Object id )
	{
		final ViewModelNode node = getNode( id );
		if ( node == null )
			throw new IllegalArgumentException( "ID '" + id + "' does not refer to a known view model node" );

		node.update();
	}

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
	 * @return  List containing ID's of all views for the model.
	 */
	public final List getViewIDs()
	{
		final List result = new ArrayList( _views.size() );
		for ( int i = 0 ; i < _views.size() ; i++ )
			result.add( ((ViewModelView)_views.get( i ) ).getID() );

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
	 * Create view panel with associated controls (if any).
	 *
	 * @param   id              Application-assigned ID for the new view.
	 * @param   viewControl     Control to use for this view.
	 *
	 * @return  Panel containing view.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final JPanel createViewPanel( final Locale locale , final Object id , final ViewControl viewControl )
	{
		final JPanel result = new JPanel( new BorderLayout() );

		result.add( createView( id , viewControl ) , BorderLayout.CENTER );

		final JToolBar toolbar = ActionTools.createToolbar( null , viewControl.getActions( locale ) );
		if ( toolbar != null )
			result.add( toolbar , BorderLayout.NORTH );

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
	 * Convenience method to create a new from-to view.
	 *
	 * @param   id      ID of the view that is created.
	 * @param   from    Point to look from.
	 * @param   to      Point to look at.
	 *
	 * @return  View component for create view.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	public final Component createView( final Object id , final Vector3D from , final Vector3D to )
	{
		return createView( id , new FromToViewControl( from , to ) );
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
		if ( id == null )
			throw new NullPointerException( "id" );

		_views.remove( id );
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
}
