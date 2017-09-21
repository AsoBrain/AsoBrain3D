/*
 * $Id$
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

import ab.j3d.*;
import ab.j3d.model.*;

/**
 * Defines the behavior of part of a {@link BoxControl}.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public interface BoxControlDelegate
{
	/**
	 * Returns whether the parts of the box associated with this delegate is
	 * enabled.
	 *
	 * @return  <code>true</code> if associated box parts are enabled;
	 *          <code>false</code> otherwise.
	 */
	boolean isEnabled();

	/**
	 * Returns whether the parts of the box associated with this delegate is
	 * visible.
	 *
	 * @return  <code>true</code> if associated box parts are visible;
	 *          <code>false</code> otherwise.
	 */
	boolean isVisible();

	/**
	 * Notifies the delegate that a mouse button was pressed on a side or edge
	 * of the box.
	 *
	 * @param   event   Control input event.
	 * @param   node    Content node.
	 */
	void mousePressed( ControlInputEvent event, ContentNode node );

	/**
	 * Notifies the delegate that a side or edge of the box was dragged.
	 *
	 * @param   event   Control input event.
	 * @param   node    Content node.
	 * @param   offset  Amount dragged, in local (content node) coordinates.
	 */
	void mouseDragged( ControlInputEvent event, ContentNode node, Vector3D offset );

	/**
	 * Notifies the delegate that a mouse button was released.
	 *
	 * @param   event   Control input event.
	 * @param   node    Content node.
	 */
	void mouseReleased( ControlInputEvent event, ContentNode node );
}
