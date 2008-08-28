/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2008
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
package ab.j3d.view.control;

import javax.swing.JPopupMenu;

import ab.j3d.control.ControlInputEvent;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.view.ViewModelNode;

/**
 * This class is used to create a {@link ViewModelNode} which may have a {@link ControlInputEvent}
 * and an {@link ViewModelNode} attached to it.
 *
 * @author  Jark Reijerink
 * @version $Revision$ $Date$
 */
public class ViewModelNodePopupMenu
	extends JPopupMenu
{
	/**
	 * ControlInputEvent on which this {@link ViewModelNodePopupMenu} was triggered.
	 */
	private final ControlInputEvent _controlInputEvent;

	/**
	 * ViewModelNode on which this {@link ViewModelNodePopupMenu} was triggered.
	 * If this variable is <code>null</code> it was not triggered on a {@link ViewModelNode}.
	 */
	private final ViewModelNode _viewModelNode;

	/**
	 * {@link Face3DIntersection} on which this {@link ViewModelNodePopupMenu} was triggered.
	 * If this variable is <code>null</code> it was not triggered on a {@link ViewModelNode}.
	 */
	private final Face3DIntersection _face3DIntersection;

	/**
	 * Constructs a {@link ViewModelNodePopupMenu} without an "invoker".
	 */
	public ViewModelNodePopupMenu()
	{
		this( null , null , null );
	}

	/**
	 * Create a {@link ViewModelNodePopupMenu} with the specified
	 * properties.
	 *
	 * @param controlInputEvent     {@link ControlInputEvent} that was triggered (can be <code>null</code>).
	 * @param viewModelNode         {@link ViewModelNode} on which was clicked (can be <code>null</code>).
	 * @param face3DIntersection    {@link Face3DIntersection} on which was clicked (can be <code>null</code>).
	 */
	public ViewModelNodePopupMenu( final ControlInputEvent controlInputEvent , final ViewModelNode viewModelNode , final Face3DIntersection face3DIntersection )
	{
		this( null , controlInputEvent , viewModelNode , face3DIntersection );
	}

	/**
	 * Create a {@link ViewModelNodePopupMenu} with the specified properties.
	 *
	 * @param label String that a UI may use to display as a title for the
	 *              popup menu.
	 */
	public ViewModelNodePopupMenu( final String label )
	{
		this( label , null , null , null );
	}

	/**
	 * Create a {@link ViewModelNodePopupMenu} with the specified properties.
	 *
	 * @param label                 The string that a UI may use to display as a
	 *                              title for the popup menu.
	 * @param controlInputEvent     {@link ControlInputEvent} that was triggered (can be <code>null</code>).
	 * @param viewModelNode         {@link ViewModelNode} on which was clicked (can be <code>null</code>).
	 * @param face3DIntersection    {@link Face3DIntersection} on which was clicked (can be <code>null</code>).
	 */
	public ViewModelNodePopupMenu( final String label , final ControlInputEvent controlInputEvent , final ViewModelNode viewModelNode , final Face3DIntersection face3DIntersection )
	{
		super( label );
		_viewModelNode      = viewModelNode;
		_controlInputEvent  = controlInputEvent;
		_face3DIntersection = face3DIntersection;
	}

	/**
	 * Returns the {@link ControlInputEvent} on which this {@link ViewModelNodePopupMenu}
	 * was triggered. May return <code>null</code>.
	 *
	 * @return {@link ControlInputEvent} on which this {@link ViewModelNodePopupMenu} was triggered.
	 */
	public ControlInputEvent getControlInputEvent()
	{
		return _controlInputEvent;
	}

	/**
	 * Returns the {@link ViewModelNode} on which this {@link ViewModelNodePopupMenu}
	 * was triggered. May return <code>null</code>.
	 *
	 * @return {@link ViewModelNode} on which this {@link ViewModelNodePopupMenu} was triggered.
	 */
	public ViewModelNode getViewModelNode()
	{
		return _viewModelNode;
	}

	/**
	 * Returns the {@link Face3DIntersection} on which this {@link ViewModelNodePopupMenu}
	 * was triggered. May return <code>null</code>.
	 *
	 * @return {@link ViewModelNode} on which this {@link ViewModelNodePopupMenu} was triggered.
	 */
	public Face3DIntersection getFace3DIntersection()
	{
		return _face3DIntersection;
	}
}
