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
package ab.j3d.control.controltest;

import java.util.List;

import ab.j3d.Vector3D;
import ab.j3d.control.Control;
import ab.j3d.control.ControlEvent;
import ab.j3d.control.Intersection;
import ab.j3d.control.MouseControlEvent;
import ab.j3d.control.controltest.model.Model;
import ab.j3d.control.controltest.model.TetraHedron;
import ab.j3d.control.controltest.model.SceneElement;

/**
 * The SelectionControl handles events from the View, and moves
 * {@link TetraHedron}s when the user drags the mouse while one is selected.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class MoveControl
	implements Control
{
	/**
	 * The {@link Model} that keeps track of selection.
	 */
	private Model _model;

	/**
	 * Event number of the last control event.
	 *
	 * @see MouseControlEvent#getNumber()
	 */
	private int _lastPressNumber;

	/**
	 * The difference between the mouse click location and the selected tetra
	 * hedron's location. Used when dragging, so that the hedron stays in the
	 * same place in relation to the mouse.
	 */
	private Vector3D _dragDifference;

	/**
	 * Construct new MoveControl.
	 *
	 * @param   main    The main application class.
	 */
	public MoveControl( final ControlTest main )
	{
		_model = main.getModel();

		_lastPressNumber = -1;
		_dragDifference = Vector3D.INIT;
	}

	/**
	 * Handles a {@link ControlEvent}. If the user presses the mouse while over
	 * a selected {@link TetraHedron}, dragging is started. While the user drags
	 * the TetraHedron is moved along with the mouse over the ground plane.
	 * Dragging stops when the user releases the mouse.<p>
	 * This method returns <code>null</code> if an object was moved, and it
	 * returns the original event if nothing happened.
	 *
	 * @param   e   The event passed on.
	 *
	 * @return  Original event if no object is moved, <code>null</code> if an
	 *          object was moved.
	 */
	public ControlEvent handleEvent( final ControlEvent e )
	{
		ControlEvent result = e;

		if ( e instanceof MouseControlEvent )
		{
			final MouseControlEvent event = (MouseControlEvent)e;
			final SceneElement selection = _model.getSelection();
			final int type = event.getType();

			if ( type == MouseControlEvent.MOUSE_PRESSED )
			{
				if ( selection instanceof TetraHedron )
				{
					final TetraHedron  selectedHedron = (TetraHedron)selection;
					final List         intersections  = event.getIntersections();
					final Intersection intersection   = (Intersection)intersections.get( 0 );

					if ( selectedHedron == intersection.getID() )
					{
						final Vector3D dragStart = event.getIntersectionWithPlane( Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ) , Vector3D.INIT );

						if ( dragStart != null )
						{
							final Vector3D hedronLocation = Vector3D.INIT.set( selectedHedron.getX(), selectedHedron.getY(), 0.0 );
							_dragDifference = hedronLocation.minus( dragStart );
							_lastPressNumber = event.getNumber();
							result = null;
						}
					}
				}
			}
			else if ( type == MouseControlEvent.MOUSE_DRAGGED )
			{
				if ( event.getNumber() == _lastPressNumber )
				{
					if ( selection instanceof TetraHedron )
					{
						final TetraHedron selectedHedron = (TetraHedron)selection;

						final Vector3D dragPosition = event.getIntersectionWithPlane( Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ), Vector3D.INIT );

						if ( dragPosition != null )
						{
							final Vector3D location = dragPosition.plus( _dragDifference );

							selectedHedron.setX( location.x );
							selectedHedron.setY( location.y );

							result = null;
						}
					}
				}
			}
			else if ( type == MouseControlEvent.MOUSE_RELEASED )
			{
				if ( selection instanceof TetraHedron )
				{
					if ( event.getNumber() == _lastPressNumber )
					{
						result = null;
					}
				}
			}
		}

		return result;
	}

	public int getDataRequiredMask()
	{
		return 0;
	}
}
