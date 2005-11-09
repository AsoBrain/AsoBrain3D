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
import java.util.Set;

import ab.j3d.model.Object3D;

/**
 * The <code>SelectionEvent</code> is thrown when an {@link Object3D} has been
 * selected.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class SelectionChangeEvent
extends EventObject
{
	/**
	 * A Set with the IDs of the selected objects.
	 */
	private Set _selection;

	/**
	 * Construct a new NodeSelectionEvent. <code>source</code> is the object
	 * which generated this event. <code>selection</code> is a set with the IDs
	 * of the selected objects. This should not be <code>null</code> when
	 * nothing is selected, but an empty set instead.
	 *
	 * @param source    The Object which generated this event
	 * @param selection A set with the IDs of the selected objects
	 */
	public SelectionChangeEvent( final Object source, final Set selection)
	{
		super( source );

		_selection = selection;
	}

	/**
	 * Returns a Set with the IDs of the selected objects.
	 * @return a Set with the IDs of the selected objects.
	 */
	public Set getSelection()
	{
		return _selection;
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
