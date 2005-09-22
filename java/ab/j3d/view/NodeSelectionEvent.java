/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import java.util.EventObject;

import ab.j3d.model.Node3D;

/**
 * The <code>NodeSelectionEvent</code> is thrown when a {@link Node3D} has been
 * selected.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class NodeSelectionEvent
extends EventObject
{
	/**
	 * The <code>id</code> of the selected {@link Node3D}
	 */
	private Object _id;

	/**
	 * Construct a new NodeSelectionEvent. <code>source</code> is the object which
	 * generated this event. <code>id</code> is the id of the selected {@link
	 * Node3D}
	 *
	 * @param source The Object which generated this event
	 * @param id     The id of the selected {@link Node3D}
	 */
	public NodeSelectionEvent( final Object source, final Object id )
	{
		super( source );

		_id = id;
	}

	/**
	 * The object on which the Event initially occurred.
	 *
	 * @return The object on which the Event initially occurred.
	 */
	public Object getSource()
	{
		return super.getSource();
	}

	/**
	 * The <code>id</code> of the selected {@link Node3D}
	 *
	 * @return The <code>id</code> of the selected {@link Node3D}
	 */
	public Object getNodeID()
	{
		return _id;
	}

	/**
	 * Returns a String representation of this EventObject.
	 *
	 * @return A String representation of this EventObject.
	 */
	public String toString()
	{
		return super.toString();
	}

}
