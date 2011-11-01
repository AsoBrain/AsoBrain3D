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
package ab.j3d.view;

import java.util.*;

import ab.j3d.control.*;

/**
 * Listener for {@link ControlInputEvent control input events}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface ControlInputListener
	extends EventListener
{
	/**
	 * Called when input from a {@link ViewControlInput} is received. This input
	 * typically comes from the mouse, keyboard, touch surface, etc.
	 *
	 * @param   event   Control input event.
	 */
	void inputReceived( ControlInputEvent event );
}
