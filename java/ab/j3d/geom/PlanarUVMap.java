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
	 * Scale applied to model coordinates when calculating the texture
	 * U-coordinates.
	 */
	private float _scaleU;

	/**
	 * Scale applied to model coordinates when calculating the texture
	 * V-coordinates.
	 */
	private float _scaleV;

	/**
	 * Transforms plane coordinates to model coordinates in model units.
	 */
	private Matrix3D _plane2wcs;

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane, with its UV
	 * origin at the spatial origin.
	 *
	 * @param   scale   Scale of the UV-map.
	 */
	public PlanarUVMap( final double scale )
	{
		this( scale , Matrix3D.INIT );
	}

	/**
	 * Constructs a new planar UV-map perpendicular to the given normal, with
	 * its UV origin at the spatial origin.
	 *
	 * @param   scale   Scale of the UV-map.
	 * @param   normal  Normal vector indicating the orientation of the
	 *                  plane.
	 */
	public PlanarUVMap( final double scale , final Vector3D normal )
	{
		this( scale , Vector3D.INIT , normal );
	}

	/**
	 * Constructs a new planar UV-map perpendicular to the given normal, with
	 * its origin at the given position.
	 *
	 * @param   scale   Scale of the UV-map.
	 * @param   origin  Spatial coordinate of the UV-origin.
	 * @param   normal  Normal vector indicating the orientation of the
	 *                  plane.
	 */
	public PlanarUVMap( final double scale , final Vector3D origin , final Vector3D normal )
	{
		this( scale , Matrix3D.getPlaneTransform( origin , normal , true ) );
	}

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane defined by the
	 * given transformation.
	 *
	 * @param   scale       Scale of the UV-map.
	 * @param   plane2wcs   Transform plane to world coordinates.
	 */
	public PlanarUVMap( final double scale , final Matrix3D plane2wcs )
	{
		this( (float)scale , (float)scale , plane2wcs );
	}

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane.
	 *
	 * @param   scaleU      Scale of the UV-map in the U-direction.
	 * @param   scaleV      Scale of the UV-map in the V-direction.
	 */
	public PlanarUVMap( final float scaleU , final float scaleV )
	{
		this( scaleU , scaleV , Matrix3D.INIT );
	}

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane defined by the
	 * given transformation.
	 *
	 * @param   scaleU      Scale of the UV-map in the U-direction.
	 * @param   scaleV      Scale of the UV-map in the V-direction.
	 * @param   plane2wcs   Transform plane to world coordinates.
	 */
	public PlanarUVMap( final float scaleU , final float scaleV , final Matrix3D plane2wcs )
	{
		_scaleU = scaleU;
		_scaleV = scaleV;
		_plane2wcs = plane2wcs;
	}

	/**
	 * Returns the scale of the UV-map in the U-direction.
	 *
	 * @return  Scale of the UV-map in the U-direction.
	 */
	public float getScaleU()
	{
		return _scaleU;
	}

	/**
	 * Sets the scale of the UV-map in the U-direction.
	 *
	 * @param   scaleU  Scale of the UV-map in the U-direction.
	 */
	public void setScaleU( final float scaleU )
	{
		_scaleU = scaleU;
	}

	/**
	 * Returns the scale of the UV-map in the V-direction.
	 *
	 * @return  Scale of the UV-map in the V-direction.
	 */
	public float getScaleV()
	{
		return _scaleV;
	}

	/**
	 * Sets the scale of the UV-map in the V-direction.
	 *
	 * @param   scaleV  Scale of the UV-map in the V-direction.
	 */
	public void setScaleV( final float scaleV )
	{
		_scaleV = scaleV;
	}

	/**
	 * Returns the matrix that transforms plane coordinates to model
	 * coordinates. No scaling is performed by the matrix.
	 *
	 * @return  Plane to world coordinate transformation matrix.
	 */
	public Matrix3D getPlane2wcs()
	{
		return _plane2wcs;
	}

	/**
	 * Sets the matrix that transforms plane coordinates to model coordinates.
	 * No scaling is performed by the matrix.
	 *
	 * @param   plane2wcs   Plane to world coordinate transformation matrix.
	 *
	 * @throws  NullPointerException if any parameter is <code>null</code>.
	 */
	public void setPlane2wcs( final Matrix3D plane2wcs )
	{
		if ( plane2wcs == null )
		{
			throw new NullPointerException( "plane2wcs" );
		}

		_plane2wcs = plane2wcs;
	}

	public Point2D.Float[] generate( final Material material , final double[] vertexCoordinates , final int[] vertexIndices , final boolean flipTexture )
	{
		final Point2D.Float[] result = new Point2D.Float[ vertexIndices.length ];

		final Matrix3D plane2wcs = _plane2wcs;

		final float scaleU;
		final float scaleV;

		if ( ( material != null ) && ( material.colorMapWidth > 0.0f ) && ( material.colorMapHeight > 0.0f ) )
		{
			scaleU = _scaleU / material.colorMapWidth;
			scaleV = _scaleV / material.colorMapHeight;
		}
		else
		{
			scaleU = _scaleU;
			scaleV = _scaleV;
		}

		for ( int i = 0 ; i < vertexIndices.length ; i++ )
		{
			final int base = vertexIndices[ i ] * 3;

			final double wcsX = vertexCoordinates[ base ];
			final double wcsY = vertexCoordinates[ base + 1 ];
			final double wcsZ = vertexCoordinates[ base + 2 ];

			final float tx = (float)plane2wcs.inverseTransformX( wcsX , wcsY , wcsZ );
			final float ty = (float)plane2wcs.inverseTransformY( wcsX , wcsY , wcsZ );

			result[ i ] = flipTexture ? new Point2D.Float( ty * scaleU , tx * scaleV ) : new Point2D.Float( tx * scaleU , ty * scaleV );
		}

		return result;
	}

	public Point2D.Float generate( final Material material , final Vector3D wcsPoint , final Vector3D normal , final boolean flipTexture )
	{
		final Matrix3D plane2wcs = _plane2wcs;

		final float scaleU;
		final float scaleV;

		if ( ( material != null ) && ( material.colorMapWidth > 0.0f ) && ( material.colorMapHeight > 0.0f ) )
		{
			scaleU = _scaleU / material.colorMapWidth;
			scaleV = _scaleV / material.colorMapHeight;
		}
		else
		{
			scaleU = _scaleU;
			scaleV = _scaleV;
		}

		final float tx = (float)plane2wcs.inverseTransformX( wcsPoint );
		final float ty = (float)plane2wcs.inverseTransformY( wcsPoint );

		return flipTexture ? new Point2D.Float( ty * scaleU , tx * scaleV ) : new Point2D.Float( tx * scaleU , ty * scaleV );
	}
}
