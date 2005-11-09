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

import java.util.List;

import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

/**
 * SelectionControl listens to events from a view (via the
 * {@link SceneInputTranslator}), and when the mouse is pressed while over an
 * object, this is passed on to the {@link SelectionModel}.
 * @author Mart Slot
 * @version $Revision$ $Date$
 */
public class SelectionControl
implements Control
{
	/**
	 * The event number of the last mouse release.
	 */
	private int _lastRelease = -1;

	/**
	 * A list with the last selection
	 */
	private List _lastSelection = null;

	/**
	 * The index of the object that was selected last.
	 */
	private int _lastSelectionIndex = -1;

	/**
	 * The SelectionModel that manages the selection.
	 */
	private SelectionModel _model;

	/**
	 * Construct new SelectionControl.
	 * @param model The SelectionModel that manages selection.
	 */
	public SelectionControl( SelectionModel model )
	{
		_model = model;
	}

	/**
	 * Deals with ControlEvent. If the event is a MouseControlEvent, and the
	 * mouse is released, the selection is changed. After that, null is
	 * returned. If this is not a MouseControlEvent, or not a mouse release
	 * event, the event is returned.
	 * @param e The ControlEvent to handle
	 * @return  <code>null</code> if the event was a mouse release event, or
	 *          the original event if it was not a mouse release event
	 */
	public ControlEvent handleEvent( ControlEvent e )
	{
		if ( e instanceof MouseControlEvent )
		{
			MouseControlEvent event = (MouseControlEvent)e;
			if ( event.getType() == MouseControlEvent.MOUSE_RELEASED )
			{
				Face3D selected = null;

				List selection = event.getFacesClicked();

				if ( !selection.isEmpty() )
				{
					int index = 0;
					if ( _lastRelease + 1 == event.getNumber() && selection == _lastSelection && index < selection
					.size() )
					{
						index = _lastSelectionIndex + 1;
					}

					selected = (Face3D)selection.get( index );

					_lastSelection = selection;
					_lastSelectionIndex = index;
				}

				if ( selected == null )
				{
					_model.setSelected( null );
				}
				else
				{
					Object3D object = selected.getObject();
					_model.setSelected( object );
				}

				_lastRelease = event.getNumber();
				return null;
			}
		}
		return e;
	}
}
