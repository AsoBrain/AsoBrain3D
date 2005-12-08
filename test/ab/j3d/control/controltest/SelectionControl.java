/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.control.controltest;

import java.util.List;

import ab.j3d.control.Control;
import ab.j3d.control.ControlEvent;
import ab.j3d.control.MouseControlEvent;
import ab.j3d.control.Intersection;
import ab.j3d.control.controltest.model.Model;
import ab.j3d.control.controltest.model.SceneElement;
import ab.j3d.control.controltest.model.PaintableTriangle;
import ab.j3d.control.controltest.model.Floor;
import ab.j3d.control.controltest.model.TetraHedron;
import ab.j3d.model.Object3D;

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
	implements Control
{
	/**
	 * The {@link Model} that keeps track of selection.
	 */
	private final Model _model;

	/**
	 * Construct new SelectionControl.
	 * @param   main    The main application class.
	 */
	public SelectionControl( final ControlTest main )
	{
		_model = main.getModel();
	}

	/**
	 * Handles a {@link ControlEvent}. If the mouse was released on a
	 * {@link SceneElement} that is not a {@link Floor}, thath element is
	 * selected. If the mouse is released on one of the faces of a selected
	 * {@link TetraHedron}, that {@link PaintableTriangle} is selected.<p>
	 * If nothing is selected, the original event is returned. Otherwise, null
	 * is returned.
	 *
	 * @param   e   The event passed on.
	 * @return  The original event if nothing was selected, null if it was.
	 */
	public ControlEvent handleEvent( final ControlEvent e )
	{
		ControlEvent result = e;

		if ( e instanceof MouseControlEvent )
		{
			final MouseControlEvent event = (MouseControlEvent)e;

			if ( MouseControlEvent.MOUSE_RELEASED == event.getType()  && ! event.isMouseDragged() )
			{
				final List<Intersection> intersections = event.getIntersections();
				final Model model = _model;

				if ( ! intersections.isEmpty() )
				{
					final Intersection intersection = intersections.get( 0 );
					final Object id = intersection.getID();

					if ( id instanceof SceneElement && ! ( id instanceof Floor ) )
					{
						final SceneElement element = (SceneElement)id;
						if ( element == model.getSelection() )
						{
							if ( id instanceof TetraHedron )
							{
								final TetraHedron hedron = (TetraHedron)id;

								final Object3D object = intersection.getObject();
								final int faceIndex = object.getFaceIndex( intersection.getFace() );

								_model.setSelectedFace( hedron.getFace( faceIndex ) );
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
				}
				else
				{
					model.setSelection( null );
				}
				result = null;
			}
		}
		return result;
	}

	public int getDataRequiredMask()
	{
		return 0;
	}
}