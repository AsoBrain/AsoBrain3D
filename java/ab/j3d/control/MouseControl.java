/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import ab.j3d.view.*;
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
	private ViewControlInput _capturedSource = null;

	/**
	 * Number of event sequence being captured. Set to <code>-1</code> if no
	 * capture is active.
	 */
	private int _capturedSequence = -1;

	/**
	 * Event sequence to be captured on the next mouse dragged event.
	 */
	private ControlInputEvent _captureOnDrag = null;

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

	public void inputReceived( final ControlInputEvent event )
	{
		/*
		 * Stop capture if event is out of sequence.
		 */
		final ViewControlInput capturedSource = _capturedSource;
		final int capturedSequence = _capturedSequence;
		if ( ( capturedSource != event.getSource() ) || ( ( capturedSequence >= 0 ) && ( capturedSequence != event.getSequenceNumber() ) ) )
		{
			stopCapture();
		}

		/*
		 * Handle mouse events if the control is enabled.
		 */
		if ( isEnabled() && ( event.getMouseEvent() != null ) )
		{
			handleMouseEvent( event );
		}
	}

	/**
	 * Filter mouse event. This is only called if control is enabled.
	 *
	 * @param   event   Event from {@link ViewControlInput}.
	 */
	protected void handleMouseEvent( final ControlInputEvent event )
	{
		switch ( event.getID() )
		{
			case MouseEvent.MOUSE_MOVED :
				mouseMoved( event );
				break;

			case MouseEvent.MOUSE_PRESSED :
				mousePressed( event );
				break;

			case MouseEvent.MOUSE_DRAGGED :
				System.out.println( "MouseControl.MOUSE_DRAGGED" );
				final ControlInputEvent captureOnDrag = _captureOnDrag;
				if ( captureOnDrag != null )
				{
					startCapture( captureOnDrag );
					_captureOnDrag = null;
				}
				mouseDragged( event );
				break;

			case MouseEvent.MOUSE_RELEASED :
				_captureOnDrag = null;
				mouseReleased( event );
				break;

			case MouseEvent.MOUSE_CLICKED :
				mouseClicked( event );
				break;

			case MouseEvent.MOUSE_WHEEL :
				mouseWheelMoved( event );
				break;

			case MouseEvent.MOUSE_ENTERED :
				mouseEntered( event );
				break;

			case MouseEvent.MOUSE_EXITED :
				mouseExited( event );
				break;
		}
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
		final ViewControlInput source = event.getSource();
		if ( !source.hasFocus( this ) )
		{
			source.requestFocus( this );
		}

		_capturedSource = source;
		_capturedSequence = event.getSequenceNumber();
	}

	/**
	 * Stop capture of an event sequence.
	 */
	protected void stopCapture()
	{
		final ViewControlInput capturedSource = _capturedSource;
		if ( capturedSource != null )
		{
			capturedSource.releaseFocus();
		}
		_capturedSource = null;
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
		return ( ( _capturedSource != null ) && ( _capturedSequence >= 0 ) );
	}

	/**
	 * This method is called when one of the mouse buttons is pressed and
	 * released.
	 *
	 * @param   event   Control input event.
	 *
	 * @see     MouseEvent#MOUSE_CLICKED
	 */
	public void mouseClicked( final ControlInputEvent event )
	{
	}

	/**
	 * This method is called when the mouse wheel is moved.
	 *
	 * @param   event   Control input event.
	 */
	public void mouseWheelMoved( final ControlInputEvent event )
	{
	}

	/**
	 * This method is called when the mouse is moved.
	 *
	 * @param   event   Control input event.
	 */
	public void mouseMoved( final ControlInputEvent event )
	{
	}

	/**
	 * This method is called when one of the mouse buttons is pressed.
	 *
	 * @param   event   Control input event.
	 */
	public void mousePressed( final ControlInputEvent event )
	{
	}

	/**
	 * This method is called when the mouse is dragged.
	 *
	 * @param   event   Control input event.
	 */
	public void mouseDragged( final ControlInputEvent event )
	{
	}

	/**
	 * This method is called when the mouse is released. Any captured event
	 * sequence will be terminated after this event.
	 *
	 * @param   event   Control input event.
	 */
	public void mouseReleased( final ControlInputEvent event )
	{
		if ( isCaptured() && !event.isMouseButtonDown() )
		{
			stopCapture();
		}
	}

	/**
	 * This method is called when the mouse is enters a component.
	 *
	 * @param   event   Control input event.
	 */
	public void mouseEntered( final ControlInputEvent event )
	{
	}

	/**
	 * This method is called when the mouse is exits a component.
	 *
	 * @param   event   Control input event.
	 */
	public void mouseExited( final ControlInputEvent event )
	{
	}
}
