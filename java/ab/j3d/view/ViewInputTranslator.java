/*
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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
