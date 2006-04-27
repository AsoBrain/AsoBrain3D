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
package ab.j3d.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import com.numdata.oss.event.EventDispatcher;

import ab.j3d.Matrix3D;
import ab.j3d.geom.Ray3D;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.Projector;

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
	 * Returns the ID for an {@link Object3D}.
	 *
	 * @param   object  The object for which to return the ID.
	 *
	 * @return  The ID for <code>object</code>.
	 */
	protected abstract Object getIDForObject( Object3D object );

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
	public List getIntersections( final Ray3D ray )
	{
		final List result = new LinkedList();

		final Node3DCollection scene = getScene();
		for ( int i = 0 ; i < scene.size() ; i++ )
		{
			final Object3D object       = (Object3D)scene.getNode( i );
			final Matrix3D object2world = scene.getMatrix( i );

			object.getIntersectionsWithRay( result , true , getIDForObject( object ) , object2world , ray );
		}

		return result;
	}

	/**
	 * Get {@link Projector} that was used to project the 3D scene onto the
	 * 2D image.
	 *
	 * @return  {@link Projector} used project the 3D scene onto the 2D image.
	 */
	protected abstract Projector getProjector();

	/**
	 * Returns a {@link Node3DCollection} with all {@link Object3D}s in the
	 * scene. Implementing classes should only put objects that need to be
	 * tested for intersection in this collection. The transform matrices in
	 * the {@link Node3DCollection} should hold the matrix for transforming the
	 * object to world coordinates. If the scene is empty, an empty
	 * {@link Node3DCollection} should be returned.
	 *
	 * @return  A {@link Node3DCollection} containing the objects in the scene.
	 */
	protected abstract Node3DCollection getScene();

	/**
	 * Returns the current view transform for this scene. The view transform
	 * transforms world to view coordinates.
	 *
	 * @return  View transform for this scene.
	 */
	protected abstract Matrix3D getViewTransform();
}
