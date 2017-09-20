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
package ab.j3d.model;

import java.util.*;

/**
 * Listener for {@link ContentNodeUpdateEvent}s from a {@link ContentNode}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface ContentNodeUpdateListener
	extends EventListener
{
	/**
	 * Called to notify the listener that a node's rendering properties were
	 * updated.
	 *
	 * @param   event   Event from {@link ContentNode}.
	 */
	void renderingPropertiesUpdated( ContentNodeUpdateEvent event );

	/**
	 * Called to notify the listener that a node's transform was updated.
	 *
	 * @param   event   Event from {@link ContentNode}.
	 */
	void transformUpdated( ContentNodeUpdateEvent event );

	/**
	 * Called to notify the listener that a node's contents were updated.
	 *
	 * @param   event   Event from {@link ContentNode}.
	 */
	void contentsUpdated( ContentNodeUpdateEvent event );
}
