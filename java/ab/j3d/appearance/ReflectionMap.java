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
package ab.j3d.appearance;

/**
 * A reflection map extends the {@link CubeMap} with reflection properties. The
 * reflection map may be used for real-time reflections. A renderer may use the
 * surrounding scene instead of this map.
 *
 * @author  G. Meinders
 * @version $Revision$ ($Date$, $Author$)
 */
public interface ReflectionMap
	extends CubeMap
{
	/**
	 * Get reflectivity of the material when viewed parallel to its normal.
	 *
	 * @return  Reflectivity when viewed parallel to its normal.
	 */
	float getReflectivityMin();

	/**
	 * Get reflectivity of the material when viewed perpendicular to its normal.
	 *
	 * @return  Reflectivity when viewed perpendicular to its normal.
	 */
	float getReflectivityMax();

	/**
	 * Get intensity of the red-component of (specular) reflections.
	 *
	 * @return  Intensity of the red-component of reflections.
	 */
	float getIntensityRed();

	/**
	 * Get intensity of the green-component of (specular) reflections.
	 *
	 * @return  Intensity of the green-component of reflections.
	 */
	float getIntensityGreen();

	/**
	 * Get intensity of the blue-component of (specular) reflections.
	 *
	 * @return  Intensity of the blue-component of reflections.
	 */
	float getIntensityBlue();
}
