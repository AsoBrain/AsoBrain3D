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
import ab.j3d.appearance.*;

/**
 * This is the {@link UVGenerator} implementation for the {@link PlanarUVMap}.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class PlanarUVGenerator
	extends UVGenerator
{
	/**
	 * 3D to U/V coordinate transformation.
	 */
	private final Matrix3D _uvTransform;

	/**
	 * Construct generator.
	 *
	 * @param   plane2wcs       Transform plane to world coordinates.
	 * @param   textureMap      Specifies texture scale.
	 * @param   flipTexture     Flip texture direction.
	 */
	PlanarUVGenerator( final Matrix3D plane2wcs, final TextureMap textureMap, final boolean flipTexture )
	{
		final Matrix3D uvTransform;

		final double xx = plane2wcs.xx;
		final double yx = plane2wcs.xy;
		final double xy = plane2wcs.yx;
		final double xo = plane2wcs.inverseXo();
		final double yy = plane2wcs.yy;
		final double yz = plane2wcs.zy;
		final double xz = plane2wcs.zx;
		final double yo = plane2wcs.inverseYo();

		if ( ( textureMap != null ) && ( textureMap.getPhysicalWidth() > 0.0f ) && ( textureMap.getPhysicalHeight() > 0.0f ) )
		{
			final float w = textureMap.getPhysicalWidth();
			final float h = textureMap.getPhysicalHeight();

			uvTransform = flipTexture ? new Matrix3D( yx / w, yy / w, yz / w, yo / w, xx / h, xy / h, xz / h, xo / h, 0.0, 0.0, 0.0, 0.0 )
			                          : new Matrix3D( xx / w, xy / w, xz / w, xo / w, yx / h, yy / h, yz / h, yo / h, 0.0, 0.0, 0.0, 0.0 );
		}
		else
		{
			uvTransform = flipTexture ? new Matrix3D( yx, yy, yz, yo, xx, xy, xz, xo, 0.0, 0.0, 0.0, 0.0 )
			                          : new Matrix3D( xx, xy, xz, xo, yx, yy, yz, yo, 0.0, 0.0, 0.0, 0.0 );
		}

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
