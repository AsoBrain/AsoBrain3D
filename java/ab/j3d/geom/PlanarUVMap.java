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
 * Defines a planar UV-mapping.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class PlanarUVMap
	implements UVMap
{
	/**
	 * Transformation from model units to UV coordinates.
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
	 * @param   modelUnits      Size of a model unit in meters.
	 * @param   planeTransform  Transformation of the UV-plane relative to the XY-plane.
	 */
	public PlanarUVMap( final double modelUnits , final Matrix3D planeTransform )
	{
		_transform = planeTransform.scale( modelUnits );
	}

	public void generate( final Material material , final double[] vertexCoordinates , final int[] vertexIndices , final boolean flipTexture , final float[] textureU , final float[] textureV )
	{
		final Matrix3D transform = _transform;

		for ( int i = 0 ; i < vertexIndices.length ; i++ )
		{
			final int base = vertexIndices[ i ] * 3;

			final double x = vertexCoordinates[ base ];
			final double y = vertexCoordinates[ base + 1 ];
			final double z = vertexCoordinates[ base + 2 ];

			final float u = (float)transform.transformX( x , y , z );
			final float v = (float)transform.transformY( x , y , z );

			textureU[ i ] = flipTexture ? v : u;
			textureV[ i ] = flipTexture ? u : v;
		}
	}

	public Point2D.Float generate( final Material material , final Vector3D point , final Vector3D normal , final boolean flipTexture )
	{
		final Matrix3D transform = _transform;

		final float u = (float)transform.transformX( point );
		final float v = (float)transform.transformY( point );

		return flipTexture ? new Point2D.Float( v , u ) : new Point2D.Float( u , v );
	}
}
