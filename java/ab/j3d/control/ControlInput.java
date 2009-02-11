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
package ab.j3d.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.geom.Ray3D;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.view.Projector;

import com.numdata.oss.event.EventDispatcher;

/**
 * The <code>ControlInput</code> receives input events, converts them to
 * {@link ControlInputEvent}s, and dispatches them to a local
 * {@link EventDispatcher}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public abstract class ControlInput
{
	/**
	 * X coordinate of pointer when drag operation was started. This is stored
	 * when a {@link MouseEvent#MOUSE_PRESSED} event is received.
	 */
	protected int _dragStartX;

	/**
	 * X coordinate of pointer when drag operation was started. This is stored
	 * when a {@link MouseEvent#MOUSE_PRESSED} event is received.
	 */
	protected int _dragStartY;

	/**
	 * The {@link EventDispatcher} that dispatches the
	 * {@link java.util.EventObject}s to registered {@link Control}s.
	 */
	private EventDispatcher _eventDispatcher;

	/**
	 * The number of the current series of events. This number is increased
	 * after a {@link MouseEvent#MOUSE_RELEASED} event with no mouse buttons
	 * down.
	 */
	private int _eventNumber;

	/**
	 * Wether or not the mouse has been dragged since the last mouse press
	 * event.
	 */
	private boolean _wasDragged;

	/**
	 * Construct new control input.
	 */
	protected ControlInput()
	{
		_dragStartX      = 0;
		_dragStartY      = 0;
		_eventDispatcher = new EventDispatcher();
		_eventNumber     = 0;
		_wasDragged      = false;
	}

	/**
	 * Creates a {@link ControlInputEvent} for a specific {@link InputEvent}.
	 *
	 * @param   inputEvent  {@link InputEvent} to create a
	 *                      {@link ControlInputEvent} for.
	 *
	 * @return  A {@link ControlInputEvent} for the specified {@link InputEvent}
	 *
	 * @see     #_eventNumber
	 * @see     #_wasDragged
	 */
	protected ControlInputEvent createControlnputEvent( final InputEvent inputEvent )
	{
		boolean wasDragged = _wasDragged;

		final int eventID = inputEvent.getID();
		if ( eventID == MouseEvent.MOUSE_PRESSED )
		{
			_dragStartX = ((MouseEvent)inputEvent).getX();
			_dragStartY = ((MouseEvent)inputEvent).getY();
		}
		else if ( eventID == MouseEvent.MOUSE_DRAGGED )
		{
			wasDragged = true;
		}

		final ControlInputEvent result = new ControlInputEvent( this , inputEvent , _eventNumber , wasDragged , _dragStartX , _dragStartY );

		if ( ( eventID == MouseEvent.MOUSE_RELEASED ) && !result.isMouseButtonDown() )
		{
			_eventNumber++;
			wasDragged = false;
		}

		_wasDragged = wasDragged;

		return result;
	}

	/**
	 * This method takes an {@link InputEvent}, converts it to a
	 * {@link ControlInputEvent}, and dispatches it on the local
	 * {@link EventDispatcher}.
	 *
	 * @param   inputEvent  Input event to dispatch.
	 */
	protected void dispatchControlInputEvent( final InputEvent inputEvent )
	{
		_eventDispatcher.dispatch( createControlnputEvent( inputEvent ) );
	}

	/**
	 * Returns the {@link EventDispatcher} that dispatches the
	 * {@link java.util.EventObject}s to registered {@link Control}s.
	 *
	 * @return  This translators {@link EventDispatcher}.
	 */
	public EventDispatcher getEventDispatcher()
	{
		return _eventDispatcher;
	}

	/**
	 * Returns a List of {@link Face3DIntersection}s, which hold information
	 * about the objects that are intersected by the specified ray.
	 *
	 * @param   ray     Ray to get intersections for.
	 *
	 * @return  A list of {@link Face3DIntersection}s, ordered from near to far.
	 *
	 * @throws  NullPointerException if <code>ray</code> is <code>null</code>.
	 */
	protected abstract List<Face3DIntersection> getIntersections( final Ray3D ray );

	/**
	 * Get {@link Projector} that was used to project the 3D scene onto the
	 * 2D image.
	 *
	 * @return  {@link Projector} used project the 3D scene onto the 2D image.
	 */
	protected abstract Projector getProjector();

	/**
	 * Get view transform.
	 *
	 * @return  Transform from scene to view coordinates.
	 */
	protected abstract Matrix3D getScene2View();

	/**
	 * Get view transform.
	 *
	 * @return  Transform from view to scene coordinates.
	 */
	protected abstract Matrix3D getView2Scene();
}
