/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.control;

/**
 * A Control listens to {@link ControlEvent}s, and manipulates the view or 3d
 * world with the information from the events it receives.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public interface Control
{
	/**
	 * This control is interested in mouse clicks.
	 */
	int MOUSE_CLICKS = 1;

	/**
	 * This control is interested in mouse moves.
	 */
	int MOUSE_MOVE = 1 << 1;

	/**
	 * This control is interested in mouse drags.
	 */
	int MOUSE_DRAG = 1 << 2;

	/**
	 * This control is interested in the objects under the mouse.
	 */
	int OBJECTS_UNDER_MOUSE = 1 << 3;

	/**
	 * Handle a {@link ControlEvent}. If the event should be passed on to the
	 * next {@link Control} in the {@link ControlEventQueue}, the original event
	 * should be returned. If not, <code>null</code> should be returned.
	 *
	 * @param   e   The {@link ControlEvent} that was fired.
	 *
	 * @return  The original {@link ControlEvent} if the next {@link Control}
	 *          should also get the event, or <code>null</code> if the next
	 *          {@link Control}s may not use this event.
	 */
	ControlEvent handleEvent( ControlEvent e );

	/**
	 * Returns a bitwise mask which tells what information this {@link Control}
	 * is interested in. This may help performance of the
	 * {@link SceneInputTranslator}, because it does not need to calculate
	 * information that is not used.
	 *
	 * @return  A bitwise mask which tells what information this {@link Control}
	 *          is interested in.
	 */
	int getDataRequiredMask();

}
