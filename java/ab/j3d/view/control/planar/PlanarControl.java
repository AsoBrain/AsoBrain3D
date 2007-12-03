/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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

import java.awt.Graphics2D;

import ab.j3d.Matrix3D;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

/**
 * This interface defines control behavior for a plane relative to a
 * {@link ViewModelNode}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface PlanarControl
{
	/**
	 * Get transformation that defines the coordinate system of the drag plane
	 * relative to a {@link ViewModelNode}.
	 *
	 * @return  Drag plan transformation.
	 */
	Matrix3D getPlane2Node();

	/**
	 * Get width of drag plane.
	 *
	 * @return  Width of drag plane.
	 */
	double getPlaneWidth();

	/**
	 * Get height of drag plane.
	 *
	 * @return  Width of drag plane.
	 */
	double getPlaneHeight();

	/**
	 * Test if plane is two-sided. A two-sided plane can be controlled from
	 * both sides.
	 *
	 * @return  <code>true</code> if the plane is two-sided;
	 *          <code>false</code> otherwise.
	 */
	boolean isPlaneTwoSided();

	/**
	 * Drag start event.
	 *
	 * @param   event           Event from control.
	 * @param   viewModelNode   Node whose plane is controlled.
	 * @param   x               X coordinate on plane.
	 * @param   y               Y coordinate on plane.
	 */
	void mousePressed( ControlInputEvent event , ViewModelNode viewModelNode , double x , double y );

	/**
	 * Drag event.
	 *
	 * @param   event           Event from control.
	 * @param   viewModelNode   Node whose plane is controlled.
	 * @param   x               X coordinate on plane.
	 * @param   y               Y coordinate on plane.
	 */
	void mouseDragged( ControlInputEvent event , ViewModelNode viewModelNode , double x , double y );

	/**
	 * Drag end event.
	 *
	 * @param   event           Event from control.
	 * @param   viewModelNode   Node whose plane is controlled.
	 * @param   x               X coordinate on plane.
	 * @param   y               Y coordinate on plane.
	 */
	void mouseReleased( ControlInputEvent event , ViewModelNode viewModelNode , double x , double y );

	/**
	 * Called after a view has been rendered, after which the painters can paint
	 * whatever they wish on the view component.
	 *
	 * @param   view    {@link ViewModelView} which has rendered the scene.
	 * @param   g2d     {@link Graphics2D} object which can do the painting.
	 */
	void paint( final ViewModelView view , final Graphics2D g2d );
}
