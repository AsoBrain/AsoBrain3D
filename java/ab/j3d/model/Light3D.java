/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2007 Peter S. Heijnen
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
 * This class defines a light source in the scene graph.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Light3D
	extends Node3D
{
	/**
	 * Intensity of light (0-255). An intensity of zero indicates that
	 * no light is emitted (i.e. the light is off), while an intensity of 255
	 * indicates a 'typical' white light. Higher values may be specified for
	 * lights that are even brighter.
	 */
	private int _intensity;

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
	private double _fallOff;

	/**
	 * Default constructor.
	 */
	public Light3D()
	{
		this( 255 , 1000.0 );
	}

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
	 * Returns the intensity of the light. An intensity of zero indicates that
	 * no light is emitted (i.e. the light is off), while an intensity of 255
	 * indicates a 'typical' white light. Higher values may be specified for
	 * lights that are even brighter.
	 *
	 * @param   intensity   Light intensity (0=off, 255=white).
	 */
	public void setIntensity( final int intensity )
	{
		_intensity = intensity;
	}

	/**
	 * Returns the fall-off distance of the light. This is the distance at which
	 * the light reaches half its specified intensity. This is factor f in the
	 * formula:
	 * <pre>
	 *               f
	 *    Il = ------------
	 *         f + distance
	 * </pre>
	 * This is used to calculate the light intensity at a specific distance.
	 * Obviously, this value must be greater than 0. Greater values result
	 * in less light intensity fall-off.
	 *
	 * Setting this to <code>0</code> returns in a light source without
	 * fall-off, while a negative value, will create an ambient light source.
	 *
	 * @return  Fall-off distance.
	 */
	public double getFallOff()
	{
		return _fallOff;
	}

	/**
	 * Sets the fall-off distance of the light. This is the distance at which
	 * the light reaches half its specified intensity. This is factor f in the
	 * formula:
	 * <pre>
	 *               f
	 *    Il = ------------
	 *         f + distance
	 * </pre>
	 * This is used to calculate the light intensity at a specific distance.
	 * Obviously, this value must be greater than 0. Greater values result
	 * in less light intensity fall-off.
	 *
	 * Setting this to <code>0</code> returns in a light source without
	 * fall-off, while a negative value, will create an ambient light source.
	 *
	 * @param   fallOff     Fall-off distance.
	 */
	public void setFallOff( final double fallOff )
	{
		_fallOff = fallOff;
	}

	/**
	 * Test if this light is an ambient light source.
	 *
	 * @return  <code>true</code> if this is an ambient light source;
	 *          <code>false</code> otherwise.
	 */
	public boolean isAmbient()
	{
		return ( _fallOff < 0.0 );
	}
}
