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
 * moved. The method setLocation must be implemented to move the element to a
 * new location and the {@link #getX}, {@link #getY} and {@link #getZ} methods
 * have to return the current location of the element.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public interface Movable
{

	/**
	 * Returns the x coordinate of the location of this element.
	 *
	 * @return  x coordinate of the location of this element.
	 */
	double getX();

	/**
	 * Returns the y coordinate of the location of this element.
	 *
	 * @return  y coordinate of the location of this element
	 */
	double getY();

	/**
	 * Returns the z coordinate of the location of this element.
	 *
	 * @return  z coordinate of the location of this element.
	 */
	double getZ();

	/**
	 * Set the new X coordinate of this element.
	 *
	 * @param   x   new x coordinate of this element
	 */
	void setX( final double x );

	/**
	 * Set the new Y coordinate of this element.
	 *
	 * @param   y   new y coordinate of this element
	 */
	void setY( final double y );

	/**
	 * Set the new Z coordinate of this element.
	 *
	 * @param   z   new z coordinate of this element
	 */
	void setZ( final double z );

	/**
	 * Sets the new location of this element.
	 *
	 * @param   x   new x coordinate.
	 * @param   y   new y coordinate.
	 * @param   z   new z coordinate.
	 */
	void setLocation( final double x , final double y , final double z );

}
