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
import ab.j3d.control.SceneInputTranslator;
import ab.j3d.Matrix3D;

/**
 * The ViewInputTranslator subclasses {@link SceneInputTranslator} to provide
 * an InputTranslator for a {@link ViewModelView}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class ViewInputTranslator
	extends SceneInputTranslator
{
	/**
	 * The {@link ViewIntersectionSupport} for the view.
	 */
	private final ViewIntersectionSupport _support;

	/**
	 * The {@link ViewModelView} to listen to for events.
	 */
	private final ViewModelView _view;

	/**
	 * Construct new ViewInputTranslator.
	 *
	 * @param   view    The view to listen for events
	 * @param   model   The ViewModel for the view
	 */
	public ViewInputTranslator( final ViewModelView view, final ViewModel model )
	{
		super(view.getComponent());

		_view = view;
		_support = new ViewIntersectionSupport( model );
	}

	/**
	 * Returns the {@link IntersectionSupport} for the
	 * {@link ViewModelView}.
	 *
	 * @return  the {@link ViewIntersectionSupport} for the
	 *          {@link ViewModelView}.
	 */
	protected IntersectionSupport getIntersectionSupport()
	{
		return _support;
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
	 * Returns the current view transform for the {@link ViewModelView}.
	 *
	 * @return  the view transform for the {@link ViewModelView}.
	 */
	protected Matrix3D getViewTransform()
	{
		return _view.getViewTransform();
	}

}
