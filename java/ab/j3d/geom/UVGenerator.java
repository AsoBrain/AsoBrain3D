/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
 */
package ab.j3d.geom;

import ab.j3d.*;
import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * This generator can be requested from {@link UVMap} to generate a series of
 * U/V coordinates.
 *
 * @author Peter S. Heijnen
 */
public abstract class UVGenerator
{
	/**
	 * A {@link UVGenerator} that always produces zeros.
	 */
	private static final UVGenerator ZERO_GENERATOR = new DummyUVGenerator();

	/**
	 * Get generator for 2D points on color map texture for the given appearance.
	 *
	 * @param appearance  Appearance to get color map from.
	 * @param uvMap       UV-map to use.
	 * @param normal      Normal of face to map texture on.
	 * @param flipTexture Flip texture direction.
	 *
	 * @return Generator for U/V-coordinates.
	 */
	public static UVGenerator getColorMapInstance( @Nullable final Appearance appearance, @Nullable final UVMap uvMap, @NotNull final Vector3D normal, final boolean flipTexture )
	{
		UVGenerator result = ZERO_GENERATOR;

		if ( ( appearance != null ) && ( uvMap != null ) )
		{
			final TextureMap colorMap = appearance.getColorMap();
			if ( colorMap != null )
			{
				result = uvMap.getGenerator( colorMap, normal, flipTexture );
			}
		}

		return result;
	}

	/**
	 * Last generated U-coordinate.
	 */
	private float _u = 0.0f;

	/**
	 * Last generated V-coordinate.
	 */
	private float _v = 0.0f;

	/**
	 * Generate U/V coordinate for the given 3D point. The generated U/V
	 * coordinates can be retrieved using the getter methods of this class.
	 *
	 * @param point 3D point to generate U/V coordinates for.
	 */
	public void generate( @NotNull final Vector3D point )
	{
		generate( point.x, point.y, point.z );
	}

	/**
	 * Generate U/V coordinate for the given 3D point. The generated U/V
	 * coordinates can be retrieved using the getter methods of this class.
	 *
	 * @param x X coordinate of 3D point to generate U/V coordinates for.
	 * @param y Y coordinate of 3D point to generate U/V coordinates for.
	 * @param z Z coordinate of 3D point to generate U/V coordinates for.
	 */
	public abstract void generate( final double x, final double y, final double z );

	/**
	 * Set generated U-coordinate. This should be called by the implementation.
	 *
	 * @param u U-coordinate to set.
	 */
	protected void setU( final float u )
	{
		_u = u;
	}

	/**
	 * Set generated V-coordinate. This should be called by the implementation.
	 *
	 * @param v V-coordinate to set.
	 */
	protected void setV( final float v )
	{
		_v = v;
	}

	/**
	 * Get generated U-coordinate for last point that was passed to the {@link
	 * #generate} method.
	 *
	 * @return Last generated U-coordinate.
	 */

	public float getU()
	{
		return _u;
	}

	/**
	 * Get generated V-coordinate for last point that was passed to the {@link
	 * #generate} method.
	 *
	 * @return Last generated V-coordinate.
	 */

	public float getV()
	{
		return _v;
	}
}
