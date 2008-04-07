/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2008
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
 * Event fired by {@link ViewModelNode}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ViewModelNodeUpdateEvent
	extends EventObject
{
	/**
	 * ID for event to indicate an update to a node's rendering properties.
	 */
	public static final int RENDERING_PROPERTIES_UPDATED = 0;

	/**
	 * ID for event to indicate an update to a node's contents.
	 */
	public static final int TRANSFORM_UPDATED = 1;

	/**
	 * ID for event to indicate an update to a node's contents.
	 */
	public static final int CONTENT_UPDATED = 2;

	/**
	 * Event ID.
	 */
	private final int _id;

	private static final long serialVersionUID = -2324880479966704631L;

	/**
	 * Construct event.
	 *
	 * @param   viewModelNode   Origin of event.
	 * @param   id              Event ID.
	 */
	public ViewModelNodeUpdateEvent( final ViewModelNode viewModelNode , final int id )
	{
		super( viewModelNode );
		_id = id;
	}

	/**
	 * Get event ID.
	 *
	 * @return  Event ID.
	 */
	public int getId()
	{
		return _id;
	}
}
