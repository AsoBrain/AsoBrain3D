/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2004 Numdata BV
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
package ab.j3d.view;

/**
 * Listener for events from the <code>DragSupport</code> class.
 *
 * @see     DragSupport
 * @see     DragEvent
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface DragListener
{
	/**
	 * This event is fired when a drag operation is started. When using a mouse,
	 * this happens when a mouse button is pressed.
	 *
	 * @param   event   Drag event.
	 *
	 * @see     DragEvent#DRAG_START
	 */
	void dragStart( DragEvent event );

	/**
	 * This event is fired when the drag operation is in progress and the
	 * drag point has been dragged to a new position. When using a mouse to
	 * drag, this happens when the mouse is moved while a mouse button is
	 * pressed.
	 *
	 * @param   event   Drag event.
	 *
	 * @see     DragEvent#DRAG_TO
	 */
	void dragTo( DragEvent event );

	/**
	 * This event is fired when the drag operation is stopped. When using a
	 * mouse to drag, this is when the mouse button is released.
	 *
	 * @param   event   Drag event.
	 *
	 * @see     DragEvent#DRAG_STOP
	 */
	void dragStop( DragEvent event );
}
