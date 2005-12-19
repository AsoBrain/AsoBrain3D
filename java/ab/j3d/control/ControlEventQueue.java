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
package ab.j3d.control;

import java.util.LinkedList;
import java.util.List;

/**
 * The {@link ControlEventQueue} dispatches {@link ControlEvent}s to registered
 * {@link Control}s. An event is passed to a first {@link Control}, which can
 * either return the event or return null. If null is returned, nothing happens,
 * but if the event is returned, it is passed on to the next {@link Control} in
 * the list.<p>
 * {@link Control}s are allowed to modify an event.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public final class ControlEventQueue
{
	/**
	 * The list of registered {@link Control}s.
	 */
	private final List _controls;

	/**
	 * Construct new ControlEventQueue.
	 */
	public ControlEventQueue()
	{
		_controls = new LinkedList();
	}

	/**
	 * Adds a {@link Control} at the end of the list of controls.
	 *
	 * @param   control     The {@link Control} to add
	 */
	public void addControl( final Control control )
	{
		addControl( _controls.size() , control);
	}

	/**
	 * Adds a {@link Control} at the specified position in the list of controls.
	 *
	 * @param   index       The index at which to place the control
	 * @param   control     The Control to add
	 */
	public void addControl( final int index , final Control control )
	{
		_controls.add( index , control );
	}

	/**
	 * Removes a {@link Control} from the list of controls.
	 *
	 * @param   control     The {@link Control} to remove
	 */
	public void removeControl( final Control control )
	{
		_controls.remove( control );
	}

	/**
	 * Returns the number of {@link Control}s in this queue.
	 *
	 * @return  the number of {@link Control}s in the queue.
	 */
	public int size()
	{
		return _controls.size();
	}

	/**
	 * Dispatches a {@link ControlEvent} to the {@link Control}s in the list.
	 * The event is passed to a first control, which can either return the
	 * original event or return <code>null</code>. If <code>null</code> is
	 * returned, nothing happens, but if an event is returned, it is passed on
	 * to the next {@link Control} in the list.<p> {@link Control} are allowed
	 * modify an event.
	 *
	 * @param   e   The {@link ControlEvent} to dispatch.
	 */
	void dispatchEvent( final ControlEvent e )
	{
		ControlEvent event = e;
		for ( int i = 0 ; i < _controls.size() && event != null ; i++ )
		{
			final Control control = (Control)_controls.get( i );
			event = control.handleEvent( event );
		}
	}
}
