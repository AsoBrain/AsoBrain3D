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
import java.util.Set;
import java.util.Iterator;

import ab.j3d.model.Object3D;

/**
 * The SelectionModel keeps a list of the ID's of the selected objects. {@link
 * SelectionListener}s can be registered to be notified when selection
 * changes.<p> Currently, only one object can be selected, but this will be
 * fixed in the future
 *
 * @author Mart Slot
 * @version $Revision$ $Date$
 */
public class SelectionModel
{
	/**
	 * The {@link ViewModel} for which this SelectionModel registers selection.
	 */
	private ViewModel _viewModel;

	/**
	 * The IDs of the currently selected objects.
	 */
	private Set _selection;

	/**
	 * The {@link SelectionListener}s to be notified when selection changes.
	 */
	private Set _listeners;

	/**
	 * Construct new SelectionModel.
	 *
	 * @param viewModel The {@link ViewModel} for which this SelectionModel
	 *                  registers selection.
	 */
	public SelectionModel( final ViewModel viewModel )
	{
		_viewModel = viewModel;

		_selection = new HashSet();
		_listeners = new HashSet();
	}

	/**
	 * Change the selection to <code>selected</code>
	 *
	 * @param selected The new object to be selected
	 */
	public void setSelected( final Object3D selected )
	{
		boolean changes = false;

		if ( selected != null )
		{
			final Object id = _viewModel.getID( selected );

			if ( _selection.size() != 1 || !_selection.contains( id ) )
			{
				_selection.clear();
				_selection.add( id );
				changes = true;
			}
		}
		else
		{
			if ( !_selection.isEmpty() )
			{
				_selection.clear();
				changes = true;
			}
		}

		if ( changes )
		{
			fireEvent();
		}

	}

	/**
	 * Returns the current selection. This is a list with the IDs of the selected
	 * objects.
	 *
	 * @return The current selection.
	 */
	public Set getSelection()
	{
		return _selection;
	}

	/**
	 * Fires a {@link SelectionChangeEvent} to all listening {@link
	 * SelectionListener}s
	 */
	private void fireEvent()
	{
		final SelectionChangeEvent evt = new SelectionChangeEvent( this, _selection );
		for ( Iterator iter = _listeners.iterator(); iter.hasNext(); )
		{
			final SelectionListener listener = (SelectionListener)iter.next();
			listener.selectionChanged( evt );
		}
	}

	/**
	 * Adds a {@link SelectionListener}. The listener will be notified when the
	 * selection changes.
	 *
	 * @param listener The <code>SelectionListener</code> to add
	 */
	public void addSelectionListener( final SelectionListener listener )
	{
		_listeners.add( listener );
	}

	/**
	 * Removes a {@link SelectionListener}.
	 *
	 * @param listener The <code>SelectionListener</code> to remove
	 */
	public void removeSelectionListener( final SelectionListener listener )
	{
		_listeners.remove( listener );
	}
}
