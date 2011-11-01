/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.awt.view;

import javax.swing.*;

import ab.j3d.control.*;
import ab.j3d.model.*;

/**
 * This class implements a context menu for a {@link ContentNode}.
 *
 * @author  Jark Reijerink
 * @version $Revision$ $Date$
 */
public class ContentNodeContextMenu
	extends JPopupMenu
{
	/**
	 * {@link ControlInputEvent} that triggered this popup.
	 */
	private final ControlInputEvent _controlInputEvent;

	/**
	 * {@link ContentNode} for which this popup is created.
	 */
	private final ContentNode _contentNode;

	/**
	 * {@link Face3DIntersection} between node and pointer.
	 */
	private final Face3DIntersection _face3DIntersection;

	/**
	 * Create a {@link ContentNodeContextMenu} with the specified properties.
	 *
	 * @param   label               The string that a UI may use to display as a
	 *                              title for the popup menu.
	 * @param   controlInputEvent   {@link ControlInputEvent} that triggered this popup.
	 * @param   contentNode         {@link ContentNode} for which this popup is created.
	 * @param   face3DIntersection  {@link Face3DIntersection} between node and pointer.
	 */
	public ContentNodeContextMenu( final String label, final ControlInputEvent controlInputEvent, final ContentNode contentNode, final Face3DIntersection face3DIntersection )
	{
		super( label );
		_contentNode = contentNode;
		_controlInputEvent  = controlInputEvent;
		_face3DIntersection = face3DIntersection;
	}

	/**
	 * Get {@link ControlInputEvent} that triggered this popup.
	 *
	 * @return  {@link ControlInputEvent} that triggered this popup.
	 */
	public ControlInputEvent getControlInputEvent()
	{
		return _controlInputEvent;
	}

	/**
	 * Get {@link ContentNode} for which this popup is created.
	 *
	 * @return  {@link ContentNode} for which this popup is created.
	 */
	public ContentNode getContentNode()
	{
		return _contentNode;
	}

	/**
	 * Get {@link Face3DIntersection} between node and pointer.
	 *
	 * @return  {@link Face3DIntersection} between node and pointer.
	 */
	public Face3DIntersection getFace3DIntersection()
	{
		return _face3DIntersection;
	}
}
