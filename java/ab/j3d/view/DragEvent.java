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

import java.util.EventObject;

/**
 * This type of event is fired by the <code>MouseViewControl</codE>
 *
 * @see     DragSupport
 * @see     DragListener
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class DragEvent
	extends EventObject
{
	/**
	 * Construct new mouse view event.
	 *
	 * @param   source      Source from where the event originated.
	 */
	public DragEvent( final Object source )
	{
		super( source );

		// Calculate delta X, Y, Z in pixels, degrees, radians, world units.
	}
}
