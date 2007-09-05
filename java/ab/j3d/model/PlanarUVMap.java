/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.model;

import ab.j3d.Material;
import ab.j3d.Vector3D;

/**
 * Defines a planar UV-mapping.
 *
 * @TODO Support mapping to an arbitrary plane.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class PlanarUVMap
	implements UVMap
{
	private final double _modelUnits;

	private final Vector3D _origin;

	/**
	 * Constructs a new UV-map based on the Manhattan-distance from each mapped
	 * point to the given origin.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   origin      Spatial coordinate of the UV-origin.
	 */
	public PlanarUVMap( final double modelUnits , final Vector3D origin )
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

			textureU[ i ] = (float)( scaleX * x );
			textureV[ i ] = (float)( scaleY * y );
		}
	}
}
