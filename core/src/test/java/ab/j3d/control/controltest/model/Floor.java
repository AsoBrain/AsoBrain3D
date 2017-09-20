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
package ab.j3d.control.controltest.model;

import ab.j3d.*;

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
	public Floor( final double sizeX, final double sizeY )
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
	@Override
	public Matrix3D getTransform()
	{
		return Matrix3D.IDENTITY;
	}
}
