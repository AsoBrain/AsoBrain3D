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

import java.util.EventObject;

/**
 * Event fired by {@link Scene}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class SceneUpdateEvent
	extends EventObject
{
	/**
	 * ID for event to indicate that a node was updated.
	 */
	public static final int CONTENT_NODE_ADDED = 0;

	/**
	 * ID for event to indicate that a node was updated.
	 */
	public static final int CONTENT_NODE_REMOVED = 1;

	/**
	 * ID for event to indicate that a node's content was updated.
	 */
	public static final int CONTENT_NODE_CONTENT_UPDATED = 2;

	/**
	 * ID for event to indicate that a node property has changed.
	 */
	public static final int CONTENT_NODE_PROPERTY_CHANGED = 3;

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -2324880479966704631L;

	/**
	 * Event ID.
	 */
	private final int _id;

	/**
	 * Related node.
	 */
	private ContentNode _node;

	/**
	 * Construct event.
	 *
	 * @param   scene   Origin of event.
	 * @param   id      Event ID.
	 * @param   node    Related node.
	 */
	public SceneUpdateEvent( final Scene scene , final int id , final ContentNode node )
	{
		super( scene );
		_id = id;
		_node = node;
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

	/**
	 * Get related node.
	 *
	 * @return  Related node.
	 */
	public ContentNode getNode()
	{
		return _node;
	}
}