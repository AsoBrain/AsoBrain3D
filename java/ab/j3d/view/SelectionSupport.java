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

import java.util.HashSet;
import java.util.Iterator;

import ab.j3d.model.Node3D;

/**
 * SelectionSupport lets {@link NodeSelectionListener} register themselves with
 * it and notifies them when a {@link Node3D} has been selected.
 * The SelectionSupport class itself is abstract, so there should be
 * implementations for Java 3d and java 2d that handle the actual selection.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public abstract class SelectionSupport
{

	/**
	 * The {@link NodeSelectionListener}s that will be notified when a {@link
	 * Node3D} has been selected
	 */
	private HashSet _listeners = new HashSet();

	/**
	 * Construct new SelectionSupport.
	 */
	protected SelectionSupport()
	{
	}

	/**
	 * Add a {@link NodeSelectionListener}, which will be notified when a {@link
	 * Node3D} has been selected.
	 *
	 * @param   listener    The {@link NodeSelectionListener} to add
	 */
	public void addSelectionListener( final NodeSelectionListener listener )
	{
		_listeners.add( listener );
	}

	/**
	 * Remove a {@link NodeSelectionListener}.
	 *
	 * @param   listener    The {@link NodeSelectionListener} to remove
	 */
	public void removeSelectionListener( final NodeSelectionListener listener )
	{
		_listeners.remove( listener );
	}

	/**
	 * Fire a {@link NodeSelectionEvent} to all listening {@link
	 * NodeSelectionListener}s. <code>id</code> is the id of the selected {@link
	 * Node3D}.
	 *
	 * @param source The object that fired this event.
	 * @param id     The id of the selected Node3D
	 */
	protected void fireEvent( final Object source, final Object id )
	{
		final NodeSelectionEvent evt = new NodeSelectionEvent( source, id );
		for ( Iterator iter = _listeners.iterator(); iter.hasNext(); )
		{
			final NodeSelectionListener listener = (NodeSelectionListener)iter.next();
			listener.nodeSelected( evt );
		}
	}
}
