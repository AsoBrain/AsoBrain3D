/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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

import ab.j3d.Material;
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
	private final double _modelUnits;

	private final Vector3D _origin;

	/**
	 * Constructs a new UV-map based on the Manhattan-distance from each mapped
	 * point to the given origin.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   origin      Starting point for distance calculations.
	 */
	public ManhattanUVMap( final double modelUnits , final Vector3D origin )
	{
		_modelUnits = modelUnits;
		_origin     = origin;
	}

	public void generate( final Material material , final double[] vertexCoordinates , final int[] vertexIndices , final float[] textureU , final float[] textureV )
	{
		final double scaleX = _modelUnits / material.colorMapWidth;
		final double scaleY = _modelUnits / material.colorMapHeight;

		final Vector3D origin = _origin;

		for ( int i = 0 ; i < vertexIndices.length ; i++ )
		{
			final int base = vertexIndices[ i ] * 3;

			final double x = vertexCoordinates[ base     ] - origin.x;
			final double y = vertexCoordinates[ base + 1 ] - origin.y;
			final double z = vertexCoordinates[ base + 2 ] - origin.z;

			textureU[ i ] = (float)( scaleX * ( x + y ) );
			textureV[ i ] = (float)( scaleY * z );
		}
	}
}
