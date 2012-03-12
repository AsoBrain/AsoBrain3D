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
 * This is the {@link UVGenerator} implementation for the {@link ManhattanUVMap}.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ManhattanUVGenerator
	extends UVGenerator
{
	/**
	 * 3D to U/V coordinate transformation.
	 */
	private final Matrix3D _uvTransform;

	/**
	 * Construct generator.
	 *
	 * @param   transform       Transforms model units to UV coordinates.
	 * @param   textureMap      Specifies texture scale.
	 * @param   flipTexture     Flip texture direction.
	 */
	ManhattanUVGenerator( final Matrix3D transform, final TextureMap textureMap, final boolean flipTexture )
	{
		final Matrix3D uvTransform;

		if ( ( textureMap != null ) && ( textureMap.getPhysicalWidth() > 0.0f ) && ( textureMap.getPhysicalHeight() > 0.0f ) )
		{
			final float tw = textureMap.getPhysicalWidth();
			final float th = textureMap.getPhysicalHeight();

			if ( flipTexture )
			{
				uvTransform = transform.set( transform.yx / tw, transform.yy / tw, transform.yz / tw, transform.yo / tw,
				                             transform.xx / th, transform.xy / th, transform.xz / th, transform.xo / th,
				                             transform.zx,      transform.zy,      transform.zz,      transform.zo );
			}
			else
			{
				uvTransform = transform.set( transform.xx / tw, transform.xy / tw, transform.xz / tw, transform.xo / tw,
				                              transform.yx / th, transform.yy / th, transform.yz / th, transform.yo / th,
				                              transform.zx,      transform.zy,      transform.zz,      transform.zo );
			}
		}
		else if ( flipTexture )
		{
			uvTransform = transform.set( transform.yx, transform.yy, transform.yz, transform.yo,
			                             transform.xx, transform.xy, transform.xz, transform.xo,
			                             transform.zx, transform.zy, transform.zz, transform.zo );
		}
		else
		{
			uvTransform = transform.set( transform.xx, transform.xy, transform.xz, transform.xo,
			                             transform.yx, transform.yy, transform.yz, transform.yo,
			                             transform.zx, transform.zy, transform.zz, transform.zo );
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
