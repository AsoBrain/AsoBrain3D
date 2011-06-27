/*
 * $Id$
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
package ab.j3d.view.control.planar;

import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.control.*;

/**
 * This interface defines control behavior for a {@link ContentNode}.
 *
 * <p>
 * A node control may also implement {@link ViewOverlay}, in which case it
 * should automatically be painted when bound to a content node. By default,
 * this behavior is provided by {@link DefaultViewControl}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface ContentNodeControl
{
	/**
	 * Returns the precedence of the control in comparison to other controls.
	 * Controls with a higher value come first. If two controls have the same
	 * precedence, another ordering may apply, e.g. depth.
	 *
	 * @return  Precedence value; controls with a higher precedence come first.
	 */
	int getPrecedence();

	/**
	 * Returns a reference depth for determining the order of controls, for
	 * an event with the given pointer ray.
	 *
	 * @param   pointerRay  Pointer ray.
	 *
	 * @return  Depth value for the given pointer ray;
	 *          <code>null</code> if the ray cannot activate the control.
	 */
	Double getDepth( Ray3D pointerRay );

	/**
	 * Notifies the control that mouse cursor was moved.
	 *
	 * @param   event           Event from control.
	 * @param   contentNode     Node whose plane is controlled.
	 */
	void mouseMoved( ControlInputEvent event, ContentNode contentNode );

	/**
	 * Notifies the control that a mouse button was pressed. The control can
	 * capture the mouse at this point, such that it will receive subsequent
	 * mouse events.
	 *
	 * @param   event           Event from control.
	 * @param   contentNode     Node whose plane is controlled.
	 *
	 * @return  <code>true</code> if the event is captured;
	 *          <code>false</code> otherwise.
	 */
	boolean mousePressed( ControlInputEvent event, ContentNode contentNode );

	/**
	 * Notifies the control that the mouse was dragged. This event is only
	 * received if the mouse was previously captured by the control.
	 *
	 * @param   event           Event from control.
	 * @param   contentNode     Node whose plane is controlled.
	 *
	 * @return  <code>true</code> to capture subsequent event;
	 *          <code>false</code> to stop capturing events.
	 */
	boolean mouseDragged( ControlInputEvent event, ContentNode contentNode );

	/**
	 * Drag end event.
	 *
	 * @param   event           Event from control.
	 * @param   contentNode     Node whose plane is controlled.
	 */
	void mouseReleased( ControlInputEvent event, ContentNode contentNode);
}
