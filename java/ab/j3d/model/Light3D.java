/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
package ab.j3d.model;

/**
 * This class is a light node in the graphics tree. It contains a
 * LightModel that defines the light associated with this node.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Light3D
	extends Node3D
{
	/**
	 * Intensity of light (0-255).
	 */
	private final int _intensity;

	/**
	 * Fall-off of light. This is factor f in the formula:
	 * <pre>
	 *               f
	 *    Il = ------------
	 *         f + distance
	 * </pre>
	 * This is used to calculate the light intensity at a specific distance.
	 * Obviously, this value must be greater than 0. Greater values result
	 * in less light intensity fall-off.
	 *
	 * Setting this to a negative value, will create an ambient light source.
	 */
	private final double _fallOff;

	/**
	 * Constructor.
	 *
	 * @param   intensity   Intensity of white light (0-255).
	 * @param   fallOff     Light fall-off characteristic (negavtive => ambient).
	 */
	public Light3D( final int intensity , final double fallOff )
	{
		_intensity = intensity;
		_fallOff   = fallOff;
	}

	/**
	 * Returns the intensity of the light. An intensity of zero indicates that
	 * no light is emitted (i.e. the light is off), while an intensity of 255
	 * indicates a 'typical' white light. Higher values may be specified for
	 * lights that are even brighter.
	 *
	 * @return  Intensity value.
	 */
	public int getIntensity()
	{
		return _intensity;
	}

	/**
	 * Returns the fall-off distance of the light, which is the distance at
	 * which the light reaches half its specified intensity.
	 *
	 * @return  Fall-off distance.
	 */
	public double getFallOff()
	{
		return _fallOff;
	}
}
