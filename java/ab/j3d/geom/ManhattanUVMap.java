/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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

import java.awt.geom.Point2D;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

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
	private final Matrix3D _transform;

	/**
	 * Constructs a new UV-map based on the Manhattan-distance from each mapped
	 * point to the given origin.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   origin      Starting point for distance calculations.
	 */
	public ManhattanUVMap( final double modelUnits , final Vector3D origin )
	{
		_transform = Matrix3D.INIT.set(
			modelUnits , modelUnits , 0.0        , -modelUnits * ( origin.x + origin.y ) ,
			0.0        , 0.0        , modelUnits , -modelUnits * origin.y ,
			0.0        , 0.0        , 1.0        , 0.0 );
	}

	public Point2D.Float[] generate( final Material material , final double[] vertexCoordinates , final int[] vertexIndices , final boolean flipTexture )
	{
		final Point2D.Float[] result = new Point2D.Float[ vertexIndices.length ];

		final Matrix3D transform = _transform;

		final float scaleU;
		final float scaleV;

		if ( ( material != null ) && ( material.colorMapWidth > 0.0f ) && ( material.colorMapHeight > 0.0f ) )
		{
			scaleU = 1.0f / material.colorMapWidth;
			scaleV = 1.0f / material.colorMapHeight;
		}
		else
		{
			scaleU = 1.0f;
			scaleV = 1.0f;
		}

		for ( int i = 0 ; i < vertexIndices.length ; i++ )
		{
			final int base = vertexIndices[ i ] * 3;

			final double x = vertexCoordinates[ base ];
			final double y = vertexCoordinates[ base + 1 ];
			final double z = vertexCoordinates[ base + 2 ];

			final float tx = (float)transform.transformX( x , y , z );
			final float ty = (float)transform.transformY( x , y , z );

			result[ i ] = flipTexture ? new Point2D.Float( scaleU * ty , scaleV * tx ) : new Point2D.Float( scaleU * tx , scaleV * ty );
		}

		return result;
	}

	public Point2D.Float generate( final Material material , final Vector3D point , final Vector3D normal , final boolean flipTexture )
	{
		final Matrix3D transform = _transform;

		final float scaleU;
		final float scaleV;

		if ( ( material != null ) && ( material.colorMapWidth > 0.0f ) && ( material.colorMapHeight > 0.0f ) )
		{
			scaleU = 1.0f / material.colorMapWidth;
			scaleV = 1.0f / material.colorMapHeight;
		}
		else
		{
			scaleU = 1.0f;
			scaleV = 1.0f;
		}

		final float tx = (float)transform.transformX( point );
		final float ty = (float)transform.transformY( point );

		return flipTexture ? new Point2D.Float( scaleU * ty , scaleV * tx ) : new Point2D.Float( scaleU * tx , scaleV * ty );
	}
}
