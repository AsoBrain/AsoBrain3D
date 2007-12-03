/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2006
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

/**
 * @FIXME Need comment
 *
 * @author  Jark Reijerink
 * @version $Revision$ $Date$
 */
public class ViewModelNodePopupMenu
	extends JPopupMenu
{
	/**
	 * ControlInputEvent on which this {@link JPopupMenu} was triggered.
	 */
	private final ControlInputEvent _controlInputEvent;

	/**
	 * Constructs a {@link ViewModelNodePopupMenu} without an "invoker".
	 */
	public ViewModelNodePopupMenu()
	{
		this( null , null );
	}

	/**
	 * Constructs a {@link ViewModelNodePopupMenu} with the specified title.
	 *
	 * @param label the string that a UI may use to display as a title for the
	 *              popup menu.
	 */
	public ViewModelNodePopupMenu( final String label )
	{
		this( label , null );
	}

	/**
	 * Constructs a {@link ViewModelNodePopupMenu} with the specified title and
	 * {@link ControlInputEvent}.
	 *
	 * @param label                 the string that a UI may use to display as a
	 *                              title for the popup menu.
	 * @param controlInputEvent     the {@link ControlInputEvent} that was triggered
	 */
	public ViewModelNodePopupMenu( final String label , final ControlInputEvent controlInputEvent )
	{
		super( label );
		_controlInputEvent = controlInputEvent;
	}

	/**
	 * Returns the {@link ControlInputEvent} on which this {@link JPopupMenu}
	 * was triggered.
	 *
	 * @return {@link ControlInputEvent} on which this {@link JPopupMenu} was triggered.
	 */
	public ControlInputEvent getControlInputEvent()
	{
		return _controlInputEvent;
	}
}
