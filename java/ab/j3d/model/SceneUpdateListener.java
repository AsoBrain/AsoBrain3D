/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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

import java.util.EventListener;

/**
 * Listener for {@link SceneUpdateEvent}s from a {@link Scene}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface SceneUpdateListener
	extends EventListener
{
	/**
	 * Called to notify the listener that a content node was added to the scene.
	 *
	 * @param   event   Event from {@link Scene}.
	 */
	void contentNodeAdded( SceneUpdateEvent event );

	/**
	 * Called to notify the listener that a content node was removed from the
	 * scene.
	 *
	 * @param   event   Event from {@link Scene}.
	 */
	void contentNodeRemoved( SceneUpdateEvent event );

	/**
	 * Called to notify the listener that a content node's content was updated.
	 *
	 * @param   event   Event from {@link Scene}.
	 */
	void contentNodeContentUpdated( SceneUpdateEvent event );

	/**
	 * Called to notify the listener that a content node's property was changed.
	 *
	 * @param   event   Event from {@link Scene}.
	 */
	void contentNodePropertyChanged( SceneUpdateEvent event );
}