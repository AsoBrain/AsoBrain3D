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
 * Basic implementation of {@link ReflectionMap} interface.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class BasicReflectionMap
	extends BasicCubeMap
	implements ReflectionMap
{
	/**
	 * Reflectivity of the material when viewed parallel to its normal.
	 */
	private float _reflectionMin;

	/**
	 * Reflectivity of the material when viewed perpendicular to its normal.
	 */
	private float _reflectionMax;

	/**
	 * Intensity of the red-component of (specular) reflections.
	 */
	private float _reflectionRed;

	/**
	 * Intensity of the green-component of (specular) reflections.
	 */
	private float _reflectionGreen;

	/**
	 * Intensity of the blue-component of (specular) reflections.
	 */
	private float _reflectionBlue;

	@Override
	public float getReflectivityMin()
	{
		return _reflectionMin;
	}

	/**
	 * Get reflectivity of the material when viewed parallel to its normal.
	 *
	 * @param   reflectivity    Reflectivity if view is parallel to normal.
	 */
	public void setReflectionMin( final float reflectivity )
	{
		_reflectionMin = reflectivity;
	}

	@Override
	public float getReflectivityMax()
	{
		return _reflectionMax;
	}

	/**
	 * Get reflectivity of the material when viewed perpendicular to its normal.
	 *
	 * @param   reflectivity    Reflectivity if view is perpendicular to normal.
	 */
	public void setReflectionMax( final float reflectivity )
	{
		_reflectionMax = reflectivity;
	}

	@Override
	public float getIntensityRed()
	{
		return _reflectionRed;
	}

	/**
	 * Get intensity of the red-component of (specular) reflections.
	 *
	 * @param   intensity   Intensity of the red-component of reflections.
	 */
	public void setReflectionRed( final float intensity )
	{
		_reflectionRed = intensity;
	}

	@Override
	public float getIntensityGreen()
	{
		return _reflectionGreen;
	}

	/**
	 * Get intensity of the green-component of (specular) reflections.
	 *
	 * @param   intensity   Intensity of the green-component of reflections.
	 */
	public void setReflectionGreen( final float intensity )
	{
		_reflectionGreen = intensity;
	}

	@Override
	public float getIntensityBlue()
	{
		return _reflectionBlue;
	}

	/**
	 * Get intensity of the blue-component of (specular) reflections.
	 *
	 * @param   intensity   Intensity of the blue-component of reflections.
	 */
	public void setReflectionBlue( final float intensity )
	{
		_reflectionBlue = intensity;
	}

}
