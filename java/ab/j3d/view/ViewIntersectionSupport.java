/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import ab.j3d.control.IntersectionSupport;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.model.Node3D;
import ab.j3d.Matrix3D;

/**
 * Implements {@link IntersectionSupport} for a {@link ViewModel}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class ViewIntersectionSupport
extends IntersectionSupport
{
	/**
	 * The ViewModel used to get the objects for intersection.
	 */
	private final ViewModel _model;

	/**
	 * Construct new ViewIntersectionSupport.
	 *
	 * @param   model   The {@link ViewModel} from which the {@link Object3D}s
	 *                  in the scene can be retreived.
	 */
	public ViewIntersectionSupport( final ViewModel model )
	{
		_model = model;
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
}
