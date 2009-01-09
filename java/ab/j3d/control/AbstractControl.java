/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import ab.j3d.view.ViewControlInput;
import ab.j3d.view.ViewModelView;

/**
 * This class implements some basic control functionality.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class AbstractControl
	implements Control
{
	/**
	 * Test if this control is currently enabled.
	 *
	 * @return  <code>true</code> if control is enabled;
	 *          <code>false</code> if control is disabled.
	 */
	public boolean isEnabled()
	{
		return true;
	}

	/**
	 * Filter mouse event. This is only called if contol is enabled.
	 *
	 * @param   event       Event from {@link ControlInput}.
	 * @param   view        View from which the event came.
	 * @param   mouseEvent  The actual mouse event.
	 *
	 * @return  Filtered event.
	 */
	protected EventObject filterMouseEvent( final ControlInputEvent event , final ViewModelView view , final MouseEvent mouseEvent )
	{
		return event;
	}

	/**
	 * Filter mouse event. This is only called if control is enabled.
	 *
	 * @param   event       Event from {@link ControlInput}.
	 * @param   view        View from which the event came.
	 * @param   keyEvent    The actual key event.
	 *
	 * @return  Filtered event.
	 */
	protected EventObject filterKeyEvent( final ControlInputEvent event , final ViewModelView view , final KeyEvent keyEvent )
	{
		return event;
	}

	public EventObject filterEvent( final EventObject event )
	{
		EventObject result = event;

		if ( isEnabled()  && ( event instanceof ControlInputEvent ) && ( event.getSource() instanceof ViewControlInput ) )
		{
			final ControlInputEvent controlInputEvent = (ControlInputEvent)event;
			final ViewControlInput  viewControlInput  = (ViewControlInput)event.getSource();
			final ViewModelView     view              = viewControlInput.getView();

			final MouseEvent mouseEvent = controlInputEvent.getMouseEvent();
			if ( mouseEvent != null )
			{
				result = filterMouseEvent( controlInputEvent, view, mouseEvent );
			}

			final KeyEvent keyEvent = controlInputEvent.getKeyEvent();
			if ( keyEvent != null )
			{
				result = filterKeyEvent( controlInputEvent , view , keyEvent );
			}
		}

		return result;
	}
}