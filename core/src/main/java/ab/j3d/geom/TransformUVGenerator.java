/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.geom;

import ab.j3d.*;

/**
 * This is the {@link UVGenerator} implementation uses a {@link Matrix3D} to
 * transform 3D to 2D coordinates.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TransformUVGenerator
	extends UVGenerator
{
	/**
	 * Transform from 3D model to 2D U/V-coordinates.
	 */
	private final Matrix3D _uvTransform;

	/**
	 * Construct generator.
	 *
	 * @param   uvTransform     Transform from 3D model to 2D U/V-coordinates.
	 */
	TransformUVGenerator( final Matrix3D uvTransform )
	{
		_uvTransform = uvTransform;
	}

	@Override
	public void generate( final double x, final double y, final double z )
	{
		final Matrix3D transform = _uvTransform;
		setU( (float)transform.transformX( x, y, z ) );
		setV( (float)transform.transformY( x, y, z ) );
	}
}
