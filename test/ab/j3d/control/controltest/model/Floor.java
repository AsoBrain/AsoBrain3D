/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.control.controltest.model;

import ab.j3d.Matrix3D;

/**
 * This class models a floor. A floor only has a length and a breadth, in this
 * case <code>_xSize</code> and <code>_ySize</code>.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class Floor
	extends SceneElement
{
	/**
	 * The floor size in the x dimension.
	 */
	private double _xSize;

	/**
	 * The floor size in the y dimension.
	 */
	private double _ySize;

	/**
	 * Construct new Floor.
	 *
	 * @param   sizeX   Floor size in the x dimension
	 * @param   sizeY   Floor size in the y dimension
	 */
	public Floor( final double sizeX , final double sizeY )
	{
		_xSize = sizeX;
		_ySize = sizeY;
	}

	/**
	 * Returns the size of this element in the x dimension.
	 *
	 * @return x dimension of this element.
	 */
	public double getXSize()
	{
		return _xSize;
	}

	/**
	 * Sets the size of this element in the x dimension.
	 *
	 * @param x The new x dimension of this element.
	 */
	public void setXSize( final double x )
	{
		_xSize = x;
		elementChanged();
	}

	/**
	 * Returns the size of this element in the y dimension.
	 *
	 * @return The y dimension of this element.
	 */
	public double getYSize()
	{
		return _ySize;
	}

	/**
	 * Sets the size of this element in the y dimension.
	 *
	 * @param y new y dimension of this element.
	 */
	public void setYSize( final double y )
	{
		_ySize = y;
		elementChanged();
	}

	/**
	 * Returns a {@link Matrix3D} with the transformation of this element. This
	 * should include translation, rotation and scale where needed.
	 *
	 * @return  Transformation matrix of this element.
	 */
	public Matrix3D getTransform()
	{
		return Matrix3D.INIT;
	}
}
