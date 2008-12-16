/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2008
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
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * Defines a planar UV-mapping.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class PlanarUVMap
	implements UVMap
{
	/**
	 * Size of a model unit in meters.
	 */
	private final double _modelUnits;

	/**
	 * Transformation from spatial to UV coordinates.
	 */
	private Matrix3D _transform;

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane, with its UV
	 * origin at the spatial origin.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 */
	public PlanarUVMap( final double modelUnits )
	{
		this( modelUnits , Matrix3D.INIT );
	}

	/**
	 * Constructs a new planar UV-map perpendicular to the given normal, with
	 * its UV origin at the spatial origin.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   normal      Normal vector indicating the orientation of the
	 *                      plane.
	 */
	public PlanarUVMap( final double modelUnits , final Vector3D normal )
	{
		this( modelUnits , Vector3D.INIT , normal );
	}

	/**
	 * Constructs a new planar UV-map perpendicular to the given normal, with
	 * its origin at the given position.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   origin      Spatial coordinate of the UV-origin.
	 * @param   normal      Normal vector indicating the orientation of the
	 *                      plane.
	 */
	public PlanarUVMap( final double modelUnits , final Vector3D origin , final Vector3D normal )
	{
		this( modelUnits , Matrix3D.getPlaneTransform( origin , normal , true ) );
	}

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane defined by the
	 * given transformation.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   transform   Transformation of the UV-plane relative to the
	 *                      XY-plane.
	 */
	public PlanarUVMap( final double modelUnits , final Matrix3D transform )
	{
		_modelUnits = modelUnits;
		_transform  = transform;
	}

	public void generate( final Material material , final double[] vertexCoordinates , final int[] vertexIndices , final boolean flipTexture , final float[] textureU , final float[] textureV )
	{
		final double scaleU = _modelUnits / material.colorMapWidth;
		final double scaleV = _modelUnits / material.colorMapHeight;

		for ( int i = 0 ; i < vertexIndices.length ; i++ )
		{
			final int base = vertexIndices[ i ] * 3;

			final double x = _transform.transformX( vertexCoordinates[ base ] , vertexCoordinates[ base + 1 ] , vertexCoordinates[ base + 2 ] );
			final double y = _transform.transformY( vertexCoordinates[ base ] , vertexCoordinates[ base + 1 ] , vertexCoordinates[ base + 2 ] );

			textureU[ i ] = (float)( scaleU * ( flipTexture ? y : x ) );
			textureV[ i ] = (float)( scaleV * ( flipTexture ? x : y ) );
		}
	}
}
