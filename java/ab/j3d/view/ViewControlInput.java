/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2007
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

import java.util.LinkedList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.control.ComponentControlInput;
import ab.j3d.control.ControlInput;
import ab.j3d.geom.Ray3D;
import ab.j3d.model.Face3DIntersection;
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
	extends ComponentControlInput
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
	 * Returns the {@link Projector} for the {@link ViewModelView}.
	 *
	 * @return  The {@link Projector} for the {@link ViewModelView}.
	 */
	protected Projector getProjector()
	{
		return _view.getProjector();
	}

	public List<Face3DIntersection> getIntersections( final Ray3D ray )
	{
		final List<Face3DIntersection> result = new LinkedList();

		for ( final Object nodeID : _model.getNodeIDs() )
		{
			final ViewModelNode viewModelNode = _model.getNode( nodeID );
			final Node3D        node3D        = _model.getNode3D( nodeID );

			final Matrix3D node2model = viewModelNode.getTransform();

			final Node3DCollection<Object3D> subGraphNodes = new Node3DCollection<Object3D>();
			node3D.collectNodes( subGraphNodes , Object3D.class , node2model , false );

			for ( int i = 0 ; i < subGraphNodes.size() ; i++ )
			{
				final Object3D object       = subGraphNodes.getNode( i );
				final Matrix3D object2world = subGraphNodes.getMatrix( i );

				object.getIntersectionsWithRay( result , true , nodeID , object2world , ray );
			}
		}

		return result;
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
