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
 * A directional light emits parallel rays of light in a certain direction.
 * Directional lights are used to model light from very distant sources, such
 * as sunlight.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class DirectionalLight3D
	extends Light3D
{
	/**
	 * Direction in which light is emitted.
	 */
	private Vector3D _direction = Vector3D.ZERO;

	/**
	 * Constructs a new directional light with an intensity of <code>1.0</code>,
	 * pointing in the given direction.
	 */
	public DirectionalLight3D( final Vector3D direction )
	{
		setDirection( direction );
	}

	/**
	 * Returns the direction that the light shines towards.
	 *
	 * @return  Direction of the light.
	 */
	public Vector3D getDirection()
	{
		return _direction;
	}

	/**
	 * Sets the direction in which light is emitted.
	 *
	 * @param   direction   Light direction.
	 */
	public void setDirection( final Vector3D direction )
	{
		_direction = direction;
	}
}