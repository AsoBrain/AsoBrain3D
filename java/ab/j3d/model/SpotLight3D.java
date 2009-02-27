/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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

import ab.j3d.Vector3D;

/**
 * A spot light emits light from a single point, but only in a cone pointing in
 * a certain direction. Spot lights are used to efficiently model a point light
 * source that is partly occluded by its container, without having to resort to
 * expensive techniques such as shadow mapping.
 *
 * <p>
 * The shape of the cone of light that is emitted is defined by the direction
 * of the spot and the spread angle, as shown below. In addition, the
 * concentration of the light can be specified, such that the sharpness of the
 * edge of the spot light can vary.
 * <pre>
 *    direction
 *        ^
 *        | spreadAngle
 * \      |<---->/
 *  \     |     /
 *   \    |    /
 *    \   |   /
 *     \  |  /
 *      \ | /
 *       \|/
 *        V
 *  light source
 * </pre>
 *
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class SpotLight3D
	extends Light3D
{
	/**
	 * Direction that the spot light points in.
	 */
	private Vector3D _direction = Vector3D.ZERO;

	/**
	 * The angle between the light direction and the edge of the light cone
	 * being emitted, in the range from <code>0.0</code> to <code>90.0</code>
	 * degrees.
	 */
	private float _spreadAngle = 0.0f;

	/**
	 * An exponent specifying the concentration of the emitted light, in the
	 * range from <code>0.0</code> (sharp) to <code>128.0</code> (smooth).
	 */
	private float _concentration = 0.0f;

	/**
	 * Constructs a new spot light with an intensity of <code>1.0</code>,
	 * pointing in the given direction, with the given spread angle. The light
	 * concentration is initially <code>0.0</code>.
	 *
	 * @param   direction       Direction that the spot light points in.
	 * @param   spreadAngle     Angle between the direction and outer edge of
	 *                          the light cone, in degrees. Must be between
	 *                          <code>0.0</code> and <code>90.0</code>.
	 */
	public SpotLight3D( final Vector3D direction , final float spreadAngle )
	{
		setDirection( direction );
		setSpreadAngle( spreadAngle );
	}

	/**
	 * Returns the direction that the spot light points in.
	 *
	 * @return  Direction of the spot light.
	 */
	public Vector3D getDirection()
	{
		return _direction;
	}

	/**
	 * Sets the direction that the spot light points in.
	 *
	 * @param   direction   Direction of the spot light.
	 */
	public void setDirection( final Vector3D direction )
	{
		_direction = direction;
	}

	/**
	 * Returns the angle between the direction of the spot light and the outer
	 * edge of the light cone.
	 *
	 * @return  Angle between the direction and outer edge of the light cone,
	 *          in degrees.
	 */
	public float getSpreadAngle()
	{
		return _spreadAngle;
	}

	/**
	 * Sets the angle between the direction of the spot light and the outer
	 * edge of the light cone.
	 *
	 * @param   spreadAngle     Angle between the direction and outer edge of
	 *                          the light cone, in degrees. Must be between
	 *                          <code>0.0</code> and <code>90.0</code>.
	 */
	public void setSpreadAngle( final float spreadAngle )
	{
		_spreadAngle = spreadAngle;
	}

	/**
	 * Returns the concentration of the emitted light. Light is attenuated
	 * towards the outer edge of the spot's cone. For lower values, the light
	 * has a sharper edge, such that only light closer to the edge is noticably
	 * attenuated. For higher values, the edge is smoother, with noticable
	 * attenuation starting closer to the center of the cone.
	 *
	 * @return  Concentration of the light cone, in the range from
	 *          <code>0.0</code> (sharp edge) to <code>128.0</code>
	 *          (smooth edge).
	 */
	public float getConcentration()
	{
		return _concentration;
	}

	/**
	 * Sets the concentration of the emitted light. Light is attenuated
	 * towards the outer edge of the spot's cone. For lower values, the light
	 * has a sharper edge, such that only light closer to the edge is noticably
	 * attenuated. For higher values, the edge is smoother, with noticable
	 * attenuation starting closer to the center of the cone.
	 *
	 * @param   concentration   Concentration of the light cone, in the range
	 *                          from <code>0.0</code> (sharp edge) to
	 *                          <code>128.0</code> (smooth edge).
	 */
	public void setConcentration( final float concentration )
	{
		_concentration = concentration;
	}
}
