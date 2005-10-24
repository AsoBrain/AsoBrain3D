/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2005 Peter S. Heijnen
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
	 * Field of view (opening angle/aperture of camera) in radians. This only
	 * applies to perspective projections. Typically, this is set to 45 decimal
	 * degrees.
	 */
	private double _fieldOfView;

	/**
	 * Linear zoom factor.
	 */
	private double _zoomFactor;

	/**
	 * Constructor with scale 1:1 and a field of view of 45 decimal degrees.
	 */
	public Camera3D()
	{
		this( 1.0 , Math.toRadians( 45.0 ) );
	}

	/**
	 * Constructor with specified zoom factor and field of view.
	 *
	 * @param   zoomFactor      Linear zoom factor.
	 * @param   fieldOfView     Camera's field of view in radians.
	 */
	public Camera3D( final double zoomFactor , final double fieldOfView )
	{
		_zoomFactor  = zoomFactor;
		_fieldOfView = fieldOfView;
	}

	/**
	 * Get field of view (opening angle/aperture of camera) in radians. This
	 * only applies to perspective projections. Typically, this is set to 45
	 * decimal degrees.
	 *
	 * @return  Field of view in radians.
	 */
	public double getFieldOfView()
	{
		return _fieldOfView;
	}

	/**
	 * Set field of view (opening angle/aperture of camera) in radians. This
	 * only applies to perspective projections. Typically, this is set to 45
	 * decimal degrees.
	 *
	 * @param   fieldOfView     Field of view in radians.
	 */
	public void setFieldOfView( final double fieldOfView )
	{
		_fieldOfView = fieldOfView;
	}

	/**
	 * Get linear zoom factor.
	 *
	 * @return  Linear zoom factor.
	 */
	public double getZoomFactor()
	{
		return _zoomFactor;
	}

	/**
	 * Set linear zoom factor.
	 *
	 * @param   zoomFactor  Linear zoom factor.
	 */
	public void setZoomFactor( final double zoomFactor )
	{
		_zoomFactor = zoomFactor;
	}
}
