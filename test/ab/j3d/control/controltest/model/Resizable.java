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
