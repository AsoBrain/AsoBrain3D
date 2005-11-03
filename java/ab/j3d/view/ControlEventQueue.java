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

import java.util.List;
import java.util.LinkedList;

/**
 * The ControlEventQueue dispatches {@link ControlEvent}s to registered
 * {@link Control}s. An event is passed to a first control, which can either
 * return the event or return null. If null is returned, nothing happens, but
 * if the event is returned, it is passed on to the next control in the list.<p>
 * Controls are allowed modify an event.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class ControlEventQueue
{
	/**
	 * The list of registered controls.
	 */
	private List _controls;

	/**
	 * Construct new ControlEventQueue.
	 */
	public ControlEventQueue()
	{
		_controls = new LinkedList();
	}

	/**
	 * Adds a {@link Control} at the end of the list.
	 * @param control   The {@link Control} to add
	 */
	public void addControl( Control control )
	{
		addControl( _controls.size() , control);
	}

	/**
	 * Adds a {@link Control} at the specified position in the list.
	 * @param index     The index at which to place the control
	 * @param control   The Control to add
	 */
	public void addControl( int index, Control control )
	{
		_controls.add( index, control );
	}

	/**
	 * Removes a {@link Control } from the list
	 * @param control The Control to remove
	 */
	public void removeControl( Control control )
	{
		_controls.remove( control );
	}

	/**
	 * Dispatches an event to the controls in the list. An event is passed to a
	 * first control, which can either return the event or return null. If null
	 * is returned, nothing happens, but if the event is returned, it is passed
	 * on to the next control in the list.<p>
	 * Controls are allowed modify an event.
	 * @param event The event to dispatch
	 */
	public void dispatchEvent( ControlEvent event )
	{
		Control control = null;
		for ( int i = 0; i < _controls.size() && event != null; i++ )
		{
			control = (Control)_controls.get( i );
			event = control.handleEvent( event );
		}
	}
}
