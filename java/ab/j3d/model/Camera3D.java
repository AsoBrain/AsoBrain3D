/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
package ab.j3d.model;

import ab.j3d.Matrix3D;

/**
 * This class is a camera node in the graphics tree and defines
 * a camera view of the world. Note that the camera model associated
 * with the camera node only specifies camera properties and not
 * how the camera is placed in the world (this must be done using
 * transformations).
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Camera3D
	extends Node3D
{
	/**
	 * Distance from view plane to eye. This defines the perspective effect
	 * of the view.
	 */
	public final double aperture;

	/**
	 * Linear zoom factor.
	 */
	public final double zoom;

	/**
	 * Constructor with specified scale.
	 *
	 * @param   zoom        Linear zoom factor.
	 * @param   aperture    Camera aperture in decimal degrees (0-180).
	 */
	public Camera3D( final double zoom , final double aperture )
	{
		this.zoom     = zoom;
		this.aperture = Math.tan( aperture * Matrix3D.DEG_TO_RAD / 2.0 );
	}

}
