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
	void setRotation( final double x, final double y, final double z );
}
