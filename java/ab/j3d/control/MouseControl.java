/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2010
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

import java.awt.event.*;
import java.util.*;

import ab.j3d.view.*;
import com.numdata.oss.event.*;
import org.jetbrains.annotations.*;

/**
 * This class provides low-level support for mouse-controlled operations.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class MouseControl
	implements Control
{
	/**
	 * Event dispatcher that is used for capture.
	 */
	@Nullable
	private EventDispatcher _capturedDispatcher = null;

	/**
	 * Number of event sequence being captured. Set to <code>-1</code> if no
	 * capture is active.
	 */
	private int _capturedSequence = -1;

	/**
	 * Event sequence to be captured on the next mouse dragged event.
	 */
	private ControlInputEvent _captureOnDrag;

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

	@Override
	@Nullable
	public EventObject filterEvent( final EventObject event )
	{
		EventObject result = event;

		if ( event instanceof ControlInputEvent )
		{
			final ControlInputEvent controlInputEvent = (ControlInputEvent)event;

			/*
			 * Stop capture if event is out of sequence.
			 */
			if ( ( _capturedDispatcher != controlInputEvent.getEventDispatcher() ) ||
			     ( ( _capturedSequence >= 0 ) && ( _capturedSequence != controlInputEvent.getSequenceNumber() ) ) )
			{
				stopCapture();
			}

			/*
			 * Filter mouse events if the control is enabled.
			 */
			if ( isEnabled() && ( controlInputEvent.getMouseEvent() != null ) )
			{
				result = filterMouseEvent( controlInputEvent );
			}
		}

		return result;
	}

	/**
	 * Filter mouse event. This is only called if contol is enabled.
	 *
	 * @param   event   Event from {@link ViewControlInput}.
	 *
	 * @return  Filtered event.
	 */
	@Nullable
	protected EventObject filterMouseEvent( final ControlInputEvent event )
	{
		final EventObject result;

		switch ( event.getID() )
		{
			case MouseEvent.MOUSE_MOVED :
				result = mouseMoved( event );
				break;

			case MouseEvent.MOUSE_PRESSED :
				result = mousePressed( event );
				break;

			case MouseEvent.MOUSE_DRAGGED :
				final ControlInputEvent captureOnDrag = _captureOnDrag;
				if ( captureOnDrag != null )
				{
					startCapture( captureOnDrag );
					_captureOnDrag = null;
				}
				result = mouseDragged( event );
				break;

			case MouseEvent.MOUSE_RELEASED :
				_captureOnDrag = null;
				result = mouseReleased( event );
				break;

			case MouseEvent.MOUSE_CLICKED :
				result = mouseClicked( event );
				break;

			case MouseEvent.MOUSE_WHEEL :
				result = mouseWheelMoved( event );
				break;

			case MouseEvent.MOUSE_ENTERED :
				result = mouseEntered( event );
				break;

			case MouseEvent.MOUSE_EXITED :
				result = mouseExited( event );
				break;

			default :
				result = event;
		}

		return result;
	}

	/**
	 * Capture all events starting from the next mouse dragged event.
	 *
	 * @param   event   Control input event whose sequence to capture.
	 *
	 * @see     #startCapture
	 */
	protected void startCaptureOnDrag( final ControlInputEvent event )
	{
		_captureOnDrag = event;
	}

	/**
	 * Capture all events in the event sequence related to the specified input
	 * event. This normally invoked from the {@link #mousePressed} method to
	 * initiate a mouse-controlled operation.
	 * <p>
	 * Releasing this capture is normally not needed, since it will
	 * automatically be stopped when the event sequence is completed.
	 *
	 * @param   event   Control input event whose sequence to capture.
	 *
	 * @see     #stopCapture
	 */
	protected void startCapture( final ControlInputEvent event )
	{
		final EventDispatcher eventDispatcher = event.getEventDispatcher();
		if ( !eventDispatcher.hasFocus( this ) )
		{
			eventDispatcher.requestFocus( this );
		}

		_capturedDispatcher = eventDispatcher;
		_capturedSequence = event.getSequenceNumber();
	}

	/**
	 * Stop capture of an event sequence.
	 */
	protected void stopCapture()
	{
		final EventDispatcher eventDispatcher = _capturedDispatcher;
		if ( ( eventDispatcher != null ) && eventDispatcher.hasFocus( this ) )
		{
			eventDispatcher.releaseFocus();
		}

		_capturedDispatcher = null;
		_capturedSequence = -1;
	}

	/**
	 * Test if a captured event sequence is currently active.
	 *
	 * @return  <code>true</code> if capture is active;
	 *          <code>false</code> if no capture is active.
	 */
	protected boolean isCaptured()
	{
		return ( ( _capturedDispatcher != null ) && ( _capturedSequence >= 0 ) );
	}

	/**
	 * This method is called when one of the mouse buttons is pressed and
	 * released.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 *
	 * @see     MouseEvent#MOUSE_CLICKED
	 */
	@Nullable
	public EventObject mouseClicked( final ControlInputEvent event )
	{
		return isCaptured() ? null : event;
	}

	/**
	 * This method is called when the mouse wheel is moved.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 *
	 * @see     MouseEvent#MOUSE_WHEEL
	 */
	@Nullable
	public EventObject mouseWheelMoved( final ControlInputEvent event )
	{
		return event;
	}

	/**
	 * This method is called when the mouse is moved.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 *
	 * @see     MouseEvent#MOUSE_MOVED
	 */
	@Nullable
	public EventObject mouseMoved( final ControlInputEvent event )
	{
		return event;
	}

	/**
	 * This method is called when one of the mouse buttons is pressed.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely (e.g. because
	 *          a capture was started).
	 *
	 * @see     MouseEvent#MOUSE_PRESSED
	 */
	@Nullable
	public EventObject mousePressed( final ControlInputEvent event )
	{
		return isCaptured() ? null : event;
	}

	/**
	 * This method is called when the mouse is dragged.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 *
	 * @see     MouseEvent#MOUSE_DRAGGED
	 */
	@Nullable
	public EventObject mouseDragged( final ControlInputEvent event )
	{
		return isCaptured() ? null : event;
	}

	/**
	 * This method is called when the mouse is released. Any captured event
	 * sequence will be terminated after this event.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 *
	 * @see     MouseEvent#MOUSE_RELEASED
	 */
	@Nullable
	public EventObject mouseReleased( final ControlInputEvent event )
	{
		final ControlInputEvent result;

		if ( isCaptured() )
		{
			if ( !event.isMouseButtonDown() )
			{
				stopCapture();
			}

			result = null;
		}
		else
		{
			result = event;
		}

		return result;
	}

	/**
	 * This method is called when the mouse is enters a component.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 *
	 * @see     MouseEvent#MOUSE_ENTERED
	 */
	@Nullable
	public EventObject mouseEntered( final ControlInputEvent event )
	{
		return event;
	}

	/**
	 * This method is called when the mouse is exits a component.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 *
	 * @see     MouseEvent#MOUSE_EXITED
	 */
	@Nullable
	public EventObject mouseExited( final ControlInputEvent event )
	{
		return event;
	}
}
