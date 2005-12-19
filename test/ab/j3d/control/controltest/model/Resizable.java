/*
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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

/**
 * {@link SceneElement}s can implement this interface to indicate they can be
 * resized. The setSize methods must be implemented to resize the element, the
 * getSize methods should return the current size.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public interface Resizable
{

	/**
	 * Returns the size of this element in the x dimension.
	 *
	 * @return  x dimension of this element.
	 */
	double getXSize();

	/**
	 * Returns the size of this element in the y dimension.
	 *
	 * @return  y dimension of this element.
	 */
	double getYSize();

	/**
	 * Returns the size of this element in the z dimension.
	 *
	 * @return  z dimension of this element.
	 */
	double getZSize();

	/**
	 * Sets the size of this element in the x dimension.
	 *
	 * @param   x   New x dimension of this element.
	 */
	void setXSize( final double x );

	/**
	 * Sets the size of this element in the y dimension.
	 *
	 * @param   y   New y dimension of this element.
	 */
	void setYSize( final double y );

	/**
	 * Sets the size of this element in the z dimension.
	 *
	 * @param   z   New z dimension of this element.
	 */
	void setZSize( final double z );

	/**
	 * Set the new size of this element.
	 *
	 * @param   x   New x dimension of this element.
	 * @param   y   New y dimension of this element.
	 * @param   z   New z dimension of this element.
	 */
	void setSize( final double x , final double y , final double z );

}
