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
package ab.j3d.view.control.planar;

import java.awt.*;
import java.awt.geom.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * A control behavior for a plane relative to the {@link Scene}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface ScenePlaneControl
{
	/**
	 * Get transformation for the drag plane relative to the world coordinate
	 * system (WCS).
	 *
	 * @return  Transformation from drag plane to WCS.
	 */
	Matrix3D getPlane2Wcs();

	/**
	 * Returns the bounds of the plane, in plane coordinates.
	 *
	 * @return  Bounds of the plane.
	 */
	Rectangle2D getPlaneBounds();

	/**
	 * Test if plane is two-sided. A two-sided plane can be controlled from
	 * both sides.
	 *
	 * @return  <code>true</code> if the plane is two-sided;
	 *          <code>false</code> otherwise.
	 */
	boolean isPlaneTwoSided();

	/**
	 * Returns whether the control is visible (in the given view) and should be
	 * painted.
	 *
	 * @param   view    View in which the control may be visible.
	 *
	 * @return  <code>true</code> if the control is visible;
	 *          <code>false</code> otherwise.
	 */
	boolean isVisible( View3D view );

	/**
	 * Returns whether the control is enabled and should receive events.
	 *
	 * @return  <code>true</code> if the control is enabled;
	 *          <code>false</code> otherwise.
	 */
	boolean isEnabled();

	/**
	 * Notifies the control that a mouse button was pressed. The control can
	 * capture the mouse at this point, such that it will receive subsequent
	 * mouse events.
	 *
	 * @param   event           Event from control.
	 * @param   x               X coordinate on plane.
	 * @param   y               Y coordinate on plane.
	 *
	 * @return  <code>true</code> if the event is captured;
	 *          <code>false</code> otherwise>
	 */
	boolean mousePressed( ControlInputEvent event, double x, double y );

	/**
	 * Notifies the control that the mouse was dragged. This event is only
	 * received if the mouse was previously captured by the control.
	 *
	 * @param   event           Event from control.
	 * @param   x               X coordinate on plane.
	 * @param   y               Y coordinate on plane.
	 */
	void mouseDragged( ControlInputEvent event, double x, double y );

	/**
	 * Notifies the control that the mouse button was released. This event is
	 * only received if the mouse was previously captured by the control.
	 *
	 * @param   event           Event from control.
	 * @param   x               X coordinate on plane.
	 * @param   y               Y coordinate on plane.
	 */
	void mouseReleased( ControlInputEvent event, double x, double y );

	/**
	 * Notifies the control that the mouse was moved.
	 *
	 * @param   event           Event from control.
	 * @param   x               X coordinate on plane.
	 * @param   y               Y coordinate on plane.
	 */
	void mouseMoved( ControlInputEvent event, double x, double y );

	/**
	 * This method can paint on the plane using 2D coordinates relative to the
	 * plane. It is called like a regular {@link ViewOverlay}
	 * whenever this control is active.
	 * <dl>
	 *  <dt>IMPORTANT:</dt>
	 *  <dd>This graphics context uses the plane as 2D context, not the rendered
	 *      image. If rendering in image coordinates is needed, a normal
	 *      {@link ViewOverlay} should be used.</dd>
	 * </dl>
	 *
	 * @param   view    {@link View3D} which has rendered the scene.
	 * @param   g2d     {@link Graphics2D} object which can do the 2D painting.
	 */
	void paint( final View3D view, final Graphics2D g2d );
}
