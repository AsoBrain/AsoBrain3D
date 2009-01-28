/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.view.ViewOverlay;
import ab.j3d.model.ContentNode;

/**
 * This interface defines control behavior for a {@link ContentNode} relative
 * to a plane.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface PlaneControl
	extends ViewOverlay
{
	/**
	 * Get transformation for the drag plane relative to the world coordinate
	 * system (WCS).
	 *
	 * @return  Transformation from drag plane to WCS.
	 */
	Matrix3D getPlane2Wcs();

	/**
	 * Test if plane is two-sided. A two-sided plane can be controlled from
	 * both sides.
	 *
	 * @return  <code>true</code> if the plane is two-sided;
	 *          <code>false</code> otherwise.
	 */
	boolean isPlaneTwoSided();

	/**
	 * Test if the control is enabled.
	 *
	 * @return  <code>true</code> if the control is enabled;
	 *          <code>false</code> otherwise.
	 */
	boolean isEnabled();

	/**
	 * Drag start event.
	 *
	 * @param   event           Event from control.
	 * @param   contentNode     Node whose plane is controlled.
	 * @param   wcsPoint        Drag point in WCS.
	 *
	 * @return  <code>true</code> if a drag operation is started;
	 *          <code>false</code> otherwise>
	 */
	boolean mousePressed( ControlInputEvent event , ContentNode contentNode , Vector3D wcsPoint );

	/**
	 * Drag event.
	 *
	 * @param   event           Event from control.
	 * @param   contentNode     Node whose plane is controlled.
	 * @param   wcsEnd          Drag point in WCS.
	 */
	void mouseDragged( ControlInputEvent event , ContentNode contentNode , Vector3D wcsEnd );

	/**
	 * Drag end event.
	 *
	 * @param   event           Event from control.
	 * @param   contentNode     Node whose plane is controlled.
	 * @param   wcsPoint        Drag point in WCS.
	 */
	void mouseReleased( ControlInputEvent event , ContentNode contentNode , Vector3D wcsPoint );
}
