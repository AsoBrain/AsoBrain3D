package common.renderer;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000,2002 - All Rights Reserved
 *
 * This software may not be used, copyied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
import common.model.Matrix3D;

/**
 * This class is a camera node in the graphics tree and defines
 * a camera view of the world. Note that the camera model associated
 * with the camera node only specifies camera properties and not
 * how the camera is placed in the world (this must be done using
 * transformations).
 *
 * @version 1.0 (20011128, PSH) 
 * @author	Peter S. Heijnen
 */
public class Camera
	extends TreeNode
{
	/**
	 * Distance from view plane to eye. This defines the perspective effect
	 * of the view.
	 */
	public float aperture;

	/**
	 * Linear zoom factor.
	 */
	public float zoom;
	 
	/**
	 * Default constructor. Zoom is set to 10x, eye distance is set to 300mm.
	 */
	public Camera()
	{
		this( 10 , 300.0f );
	}

	/**
	 * Constructor with specified scale.
	 *
	 * @param	zoom		Linear zoom factor.
	 * @param	aperture	Camera aperture in decimal degrees (0-180).
	 */
	public Camera( float zoom , float aperture )
	{
		this.zoom		= zoom;
		this.aperture	= (float)Math.tan( aperture * Matrix3D.DEG_TO_RAD / 2f );
	}

}
