package ab.light3d.renderer;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2002 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2002 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
import ab.light3d.Matrix3D;

/**
 * This class is a camera node in the graphics tree and defines
 * a camera view of the world. Note that the camera model associated
 * with the camera node only specifies camera properties and not
 * how the camera is placed in the world (this must be done using
 * transformations).
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class Camera
	extends TreeNode
{
	/**
	 * Distance from view plane to eye. This defines the perspective effect
	 * of the view.
	 */
	public final float aperture;

	/**
	 * Linear zoom factor.
	 */
	public final float zoom;

	/**
	 * Constructor with specified scale.
	 *
	 * @param	zoom		Linear zoom factor.
	 * @param	aperture	Camera aperture in decimal degrees (0-180).
	 */
	public Camera( final float zoom , final float aperture )
	{
		this.zoom		= zoom;
		this.aperture	= (float)Math.tan( aperture * Matrix3D.DEG_TO_RAD / 2f );
	}

}
