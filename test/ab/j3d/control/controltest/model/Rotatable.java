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
 * rotated. The setRotation methods must be implemented to rotate the element.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public interface Rotatable
{

	/**
	 * Returns this element's rotation around the X-axis.
	 *
	 * @return  This element's rotation around the X-axis.
	 */
	double getXRotation();

	/**
	 * Returns this element's rotation around the Y-axis.
	 *
	 * @return  This element's rotation around the Y-axis.
	 */
	double getYRotation();

	/**
	 * Returns this element's rotation around the Z-axis.
	 *
	 * @return  This element's rotation around the Z-axis.
	 */
	double getZRotation();

	/**
	 * Sets the new rotation around the X-axis.
	 *
	 * @param   x   new rotation around the X-axis.
	 */
	void setXRotation( final double x );

	/**
	 * Sets the new rotation around the Y-axis.
	 *
	 * @param   y   new rotation around the Y-axis.
	 */
	void setYRotation( final double y );

	/**
	 * Sets the new rotation around the Z-axis.
	 *
	 * @param   z   new rotation around the Z-axis.
	 */
	void setZRotation( final double z );

	/**
	 * Sets the new rotation.
	 *
	 * @param   x   new rotation around the X-axis.
	 * @param   y   new rotation around the Y-axis.
	 * @param   z   new rotation around the Z-axis.
	 */
	void setRotation( final double x , final double y , final double z );
}
