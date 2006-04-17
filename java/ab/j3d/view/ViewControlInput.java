/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2006
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

import ab.j3d.Matrix3D;
import ab.j3d.control.ControlInput;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

/**
 * The ViewInputTranslator subclasses {@link ControlInput} to provide
 * an InputTranslator for a {@link ViewModelView}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class ViewControlInput
	extends ControlInput
{
	/**
	 * {@link ViewModel} with the scene contents.
	 */
	private final ViewModel _model;

	/**
	 * {@link ViewModelView} whose input events to monitor.
	 */
	private final ViewModelView _view;

	/**
	 * Construct new ViewInputTranslator.
	 *
	 *
	 * @param   model   {@link ViewModel} with the scene contents.
	 * @param   view    {@link ViewModelView} whose input events to monitor.
	 */
	public ViewControlInput( final ViewModel model , final ViewModelView view )
	{
		super( view.getComponent() );

		_model = model;
		_view  = view;
	}

	/**
	 * Returns the ID for an {@link Object3D}, as stored in the
	 * {@link ViewModel}.
	 *
	 * @param   object  {@link Object3D} for which to return the ID.
	 *
	 * @return  ID of <code>object</code>
	 */
	protected Object getIDForObject( final Object3D object )
	{
		return _model.getID( object );
	}

	/**
	 * Returns the {@link Projector} for the {@link ViewModelView}.
	 *
	 * @return  The {@link Projector} for the {@link ViewModelView}.
	 */
	protected Projector getProjector()
	{
		return _view.getProjector();
	}

	/**
	 * Returns a {@link Node3DCollection} with all objects in the
	 * {@link ViewModel}.
	 *
	 * @return  a {@link Node3DCollection} with all objects in the
	 *          {@link ViewModel}.
	 */
	protected Node3DCollection getScene()
	{
		final Node3DCollection nodeCollection = new Node3DCollection();

		final Object[]    nodeIDs = _model.getNodeIDs();
		for ( int i = 0 ; i < nodeIDs.length ; i++ )
		{
			final Object id = nodeIDs[ i ];
			final ViewModelNode node = _model.getNode( id );
			final Matrix3D node2model = node.getTransform();
			final Node3D node3D = node.getNode3D();

			node3D.gatherLeafs( nodeCollection , Object3D.class , node2model , false );
		}

		return nodeCollection;
	}

	/**
	 * Returns the current view transform for the {@link ViewModelView}.
	 *
	 * @return  the view transform for the {@link ViewModelView}.
	 */
	protected Matrix3D getViewTransform()
	{
		return _view.getViewTransform();
	}
}
