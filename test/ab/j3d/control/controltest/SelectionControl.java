/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2009
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
package ab.j3d.control.controltest;

import java.util.*;

import ab.j3d.control.*;
import ab.j3d.control.controltest.model.*;
import ab.j3d.model.*;

/**
 * The SelectionControl handles events from the View, and selects
 * {@link SceneElement}s when the users clicks on one. If the user then clicks
 * on the face of a selected TetraHedron, the {@link PaintableTriangle} under
 * the mouse is then selected.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class SelectionControl
	extends MouseControl
{
	/**
	 * The {@link Model} that keeps track of selection.
	 */
	private final Model _model;

	/**
	 * Construct new SelectionControl.
	 *
	 * @param   model   {@link Model} that keeps track of selection.
	 */
	public SelectionControl( final Model model )
	{
		_model = model;
	}

	/**
	 * If the mouse was clicked on a {@link SceneElement} that is not a
	 * {@link Floor}, that element is selected. If the mouse is clicked on one
	 * of the faces of a selected {@link TetraHedron}, that
	 * {@link PaintableTriangle} is selected.
	 *
	 * @param   event   The event passed on.
	 *
	 * @return  The original event if nothing was selected, null if it was.
	 */
	@Override
	public EventObject mouseClicked( final ControlInputEvent event )
	{
		final EventObject result;

		final Model model = _model;

		final List<Face3DIntersection> intersections = event.getIntersections();
		if ( !intersections.isEmpty() )
		{
			final Face3DIntersection intersection = intersections.get( 0 );
			final Object id = intersection.getObjectID();

			if ( id instanceof SceneElement && ! ( id instanceof Floor ) )
			{
				final SceneElement element = (SceneElement)id;
				if ( element == model.getSelection() )
				{
					if ( id instanceof TetraHedron )
					{
						final TetraHedron hedron = (TetraHedron)id;

						// FIXME: broken
/*
						final Object3D object = intersection.getObject();
						for ( int faceIndex = 0 ; faceIndex < object.getFaceCount() ; faceIndex++ )
						{
							if ( intersection.getFace() == object.getFace( faceIndex ) )
							{
								_model.setSelectedFace( hedron.getFace( faceIndex ) );
								break;
							}
						}
*/
					}
				}
				else
				{
					model.setSelection( element );
				}
			}
			else
			{
				model.setSelection( null );
			}

			result = null;
		}
		else
		{
			model.setSelection( null );
			result = event;
		}

		return result;
	}
}
