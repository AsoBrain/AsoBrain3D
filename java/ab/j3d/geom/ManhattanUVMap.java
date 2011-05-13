/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2010 Peter S. Heijnen
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

import java.awt.geom.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * Defines a UV-mapping that derives the u-coordinate from the Manhattan
 * distance to a fixed point, while the v-coordinate is derived from the
 * z-coordinate.
 *
 * @see     <a href="http://mathworld.wolfram.com/TaxicabMetric.html">
 *          Taxicab Metric from Wolfram MathWorld</a>
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ManhattanUVMap
	implements UVMap
{
	/**
	 * Transforms model units to UV coordinates.
	 */
	@NotNull
	private final Matrix3D _transform;

	/**
	 * Constructs a new UV-map based on the Manhattan-distance from each mapped
	 * point to the given origin.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   origin      Starting point for distance calculations.
	 */
	public ManhattanUVMap( final double modelUnits, final Vector3D origin )
	{
		_transform = Matrix3D.INIT.set(
			modelUnits, modelUnits, 0.0       , -modelUnits * ( origin.x + origin.y ),
			0.0       , 0.0       , modelUnits, -modelUnits * origin.y,
			0.0       , 0.0       , 1.0       , 0.0 );
	}

	@Override
	public float[] generate( @Nullable final TextureMap textureMap, @NotNull final List<? extends Vector3D> vertexCoordinates, @Nullable final int[] vertexIndices, final boolean flipTexture )
	{
		final int vertexCount = ( vertexIndices != null ) ? vertexIndices.length : vertexCoordinates.size();

		final float[] result = new float[ vertexCount * 2 ];

		final Matrix3D transform = _transform;

		final float scaleU;
		final float scaleV;

		if ( ( textureMap != null ) && ( textureMap.getPhysicalWidth() > 0.0f ) && ( textureMap.getPhysicalHeight() > 0.0f ) )
		{
			scaleU = 1.0f / textureMap.getPhysicalWidth();
			scaleV = 1.0f / textureMap.getPhysicalHeight();
		}
		else
		{
			scaleU = 1.0f;
			scaleV = 1.0f;
		}

		int j = 0;
		for ( int i = 0 ; i < vertexIndices.length ; i++ )
		{
			final Vector3D vertex = vertexCoordinates.get( ( vertexIndices != null ) ? vertexIndices[ i ] : i );

			final float tx = (float)transform.transformX( vertex );
			final float ty = (float)transform.transformY( vertex );

			result[ j++ ] = flipTexture ? ty * scaleU : tx * scaleU;
			result[ j++ ] = flipTexture ? tx * scaleV : ty * scaleV;
		}

		return result;
	}

	@Override
	public void generate( @NotNull final Point2D result, @Nullable final TextureMap textureMap, @NotNull final Vector3D point, @NotNull final Vector3D normal, final boolean flipTexture )
	{
		final Matrix3D transform = _transform;

		final float scaleU;
		final float scaleV;

		if ( ( textureMap != null ) && ( textureMap.getPhysicalWidth() > 0.0f ) && ( textureMap.getPhysicalHeight() > 0.0f ) )
		{
			scaleU = 1.0f / textureMap.getPhysicalWidth();
			scaleV = 1.0f / textureMap.getPhysicalHeight();
		}
		else
		{
			scaleU = 1.0f;
			scaleV = 1.0f;
		}

		final float tx = (float)transform.transformX( point );
		final float ty = (float)transform.transformY( point );

		result.setLocation( scaleU * ( flipTexture ? ty: tx ) , scaleV * ( flipTexture ? tx : ty ) );
	}

	@Override
	public boolean equals( final Object obj )
	{
		final boolean result;

		if ( obj == this )
		{
			result = true;
		}
		else if ( obj instanceof ManhattanUVMap )
		{
			final ManhattanUVMap other = (ManhattanUVMap)obj;
			result = _transform.equals( other._transform );
		}
		else
		{
			result = false;
		}

		return result;
	}

	@Override
	public int hashCode()
	{
		return _transform.hashCode();
	}
}
