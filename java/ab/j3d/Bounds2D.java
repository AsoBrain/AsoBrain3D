/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2003 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.soda.mountings;

/**
 * This class describes 2D boundaries.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Bounds2D
{
	/**
	 * Lower bound X coordinate.
	 */
	public float x1;

	/**
	 * Lower bound Y coordinate.
	 */
	public float y1;

	/**
	 * Upper bound X coordinate.
	 */
	public float x2;

	/**
	 * Upper bound Y coordinate.
	 */
	public float y2;

	/**
	 * Construct boundaries.
	 *
	 * @param   x1      Lower bound X coordinate.
	 * @param   y1      Lower bound Y coordinate.
	 * @param   x2      Upper bound X coordinate.
	 * @param   y2      Upper bound Y coordinate.
	 */
	public Bounds2D( final float x1 , final float y1 , final float x2 , final float y2 )
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
}
