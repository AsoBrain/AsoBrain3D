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
 * Listener for events from the <code>MouseViewControl</code> class.
 *
 * @see     MouseViewControl
 * @see     MouseViewEvent
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface MouseViewListener
{
	/**
	 * This event is fired when the mouse view was changed.
	 *
	 * @param   event   Event from mouse view control.
	 */
	void mouseViewChanged( MouseViewEvent event );
}
