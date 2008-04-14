/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2008
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

import com.numdata.oss.event.EventDispatcher;

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
	 * Number of event sequence being captured. Set to <code>-1</code> if no
	 * capture is active.
	 */
	private int _capturedSequenceNumber = -1;

	/**
	 * This method only filters {@link ControlInputEvent}s and redirects specific
	 * {@link MouseEvent}-related events to the various member methods.
	 *
	 * @param   event   Event to be filtered.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 */
	public EventObject filterEvent( final EventObject event )
	{
		EventObject result = event;

		if ( event instanceof ControlInputEvent )
		{
			final ControlInputEvent controlInputEvent = (ControlInputEvent)event;

			final boolean captured = updateCaptureState( controlInputEvent );

			result = captured ? null : event;

			switch ( controlInputEvent.getID() )
			{
				case MouseEvent.MOUSE_PRESSED :
					if ( !captured )
						result = mousePressed( controlInputEvent );
					break;

				case MouseEvent.MOUSE_DRAGGED :
					if ( captured )
						mouseDragged( controlInputEvent );
					break;

				case MouseEvent.MOUSE_RELEASED :
					if ( captured && !controlInputEvent.isMouseButtonDown() )
					{
						mouseReleased( controlInputEvent );
						stopCapture( controlInputEvent );
					}
					break;

				case MouseEvent.MOUSE_CLICKED :
					if ( !captured )
						result = mouseClicked( controlInputEvent );

					break;

				case KeyEvent.KEY_PRESSED :
					final int keyCode = controlInputEvent.getKeyCode();
					handleKey( keyCode );
					break;

				case KeyEvent.KEY_RELEASED :
			}
		}

		return result;
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
			eventDispatcher.requestFocus( this );

		_capturedSequenceNumber = event.getSequenceNumber();
	}

	/**
	 * Stop capture of an event sequence.
	 *
	 * @param   event   Control input event used to stop capture.
	 */
	protected void stopCapture( final ControlInputEvent event )
	{
		final EventDispatcher eventDispatcher = event.getEventDispatcher();
		if ( eventDispatcher.hasFocus( this ) )
			eventDispatcher.releaseFocus();

		_capturedSequenceNumber = -1;
	}

	/**
	 * This method is called by the {@link #filterEvent} method to test if the
	 * specified event is part of a captured sequence. It will also stop the
	 * capture automatically if a captured event sequence was terminated.
	 *
	 * @param   controlInputEvent     Control input event to test against capture.
	 *
	 * @return  <code>true</code> if capture is valid and active;
	 *          <code>false</code> if no capture is active.
	 */
	protected boolean updateCaptureState( final ControlInputEvent controlInputEvent )
	{
		final int capturedEvent = _capturedSequenceNumber;

		final boolean result = ( capturedEvent == controlInputEvent.getSequenceNumber() );

		if ( !result && ( capturedEvent >= 0 ) )
			stopCapture( controlInputEvent );

		return result;
	}

	/**
	 * This method is called when one of the mouse buttons is pressed and
	 * released.
	 * <p>
	 * This method is not called during captured event sequences. Starting
	 * captures within this method does not seem to be a good idea either.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 *
	 * @see     MouseEvent#MOUSE_CLICKED
	 */
	public EventObject mouseClicked( final ControlInputEvent event )
	{
		return event;
	}

	/**
	 * This method is called when one of the mouse buttons is pressed while no
	 * mouse event sequence capture has been started yet.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely (e.g. because
	 *          a capture was started).
	 *
	 * @see     MouseEvent#MOUSE_PRESSED
	 */
	public EventObject mousePressed( final ControlInputEvent event )
	{
		return event;
	}

	/**
	 * This method is called when the mouse is dragged during a captured mouse
	 * event sequence.
	 *
	 * @param   event   Control input event.
	 *
	 * @see     MouseEvent#MOUSE_DRAGGED
	 */
	public void mouseDragged( final ControlInputEvent event )
	{
	}

	/**
	 * This method is called when the mouse is released during a captured mouse
	 * event sequence. The event sequence will be terminated after this event.
	 *
	 * @param   event   Control input event.
	 *
	 * @see     MouseEvent#MOUSE_RELEASED
	 */
	public void mouseReleased( final ControlInputEvent event )
	{
	}

	/**
	 * This method is called when a key on the keyboard must be handled.
	 *
	 * @param   keyCode     Key code to handle.
	 *
	 * @see     KeyEvent#getKeyCode
	 */
	public void handleKey( final int keyCode )
	{
	}
}
