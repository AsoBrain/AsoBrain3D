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

import ab.j3d.Matrix3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

/**
 * The ViewInputTranslator subclasses {@link SceneInputTranslator} to provide
 * an InputTranslator for a {@link ViewModelView}
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class ViewInputTranslator
	extends SceneInputTranslator
{
	/**
	 * The ViewModel for the view
	 */
	private ViewModel _model;

	/**
	 * Reused collection of nodes in the model
	 */
	private Node3DCollection _tmpNodeCollection;

	/**
	 * The view to listen to for events
	 */
	private ViewModelView _view;

	/**
	 * Construct new ViewInputTranslator.
	 */
	public ViewInputTranslator( final ViewModelView view, final ViewModel model )
	{
		super(view.getComponent());

		_view = view;
		_model = model;
		_tmpNodeCollection = new Node3DCollection();
	}

	/**
	 * Returns a collections of {@link Object3D}s that are in the
	 * {@link ViewModel}.
	 *
	 * @return The collection of Object3Ds in the ViewModel
	 * @see SceneInputTranslator#getFacesAt
	 */
	protected Node3DCollection getScene()
	{
		final Node3DCollection nodeCollection = _tmpNodeCollection;
		nodeCollection.clear();

		final Object[] nodeIDs    = _model.getNodeIDs();
		final Matrix3D model2view = _view.getViewTransform();

		for ( int i = 0 ; i < nodeIDs.length ; i++ )
		{
			final Object        id         = nodeIDs[ i ];
			final ViewModelNode node       = _model.getNode( id );
			final Matrix3D      node2model = node.getTransform();
			final Node3D        node3D     = node.getNode3D();

			node3D.gatherLeafs( nodeCollection , Object3D.class , node2model.multiply( model2view ) , false );
		}

		return nodeCollection;
	}

	/**
	 * Returns the {@link Projector} for this view.
	 * @return The projector for this view
	 * @see SceneInputTranslator#getFacesAt
	 */
	protected Projector getProjector()
	{
		return _view.getProjector();
	}

}
