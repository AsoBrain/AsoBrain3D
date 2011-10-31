/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2011 Peter S. Heijnen
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

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

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
	@NotNull
	private Matrix3D _plane2wcs;

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane, with its UV
	 * origin at the spatial origin.
	 *
	 * @param   scale   Scale of the UV-map.
	 */
	public PlanarUVMap( final double scale )
	{
		this( scale, Matrix3D.IDENTITY );
	}

	/**
	 * Constructs a new planar UV-map perpendicular to the given normal, with
	 * its UV origin at the spatial origin.
	 *
	 * @param   scale   Scale of the UV-map.
	 * @param   normal  Normal vector indicating the orientation of the
	 *                  plane.
	 */
	public PlanarUVMap( final double scale, final Vector3D normal )
	{
		this( scale, Vector3D.ZERO, normal );
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
	public PlanarUVMap( final double scale, final Vector3D origin, final Vector3D normal )
	{
		this( scale, Matrix3D.getPlaneTransform( origin, normal, true ) );
	}

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane defined by the
	 * given transformation.
	 *
	 * @param   scale       Scale of the UV-map.
	 * @param   plane2wcs   Transform plane to world coordinates.
	 */
	public PlanarUVMap( final double scale, final Matrix3D plane2wcs )
	{
		this( (float)scale, (float)scale, plane2wcs );
	}

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane.
	 *
	 * @param   scaleU      Scale of the UV-map in the U-direction.
	 * @param   scaleV      Scale of the UV-map in the V-direction.
	 */
	public PlanarUVMap( final float scaleU, final float scaleV )
	{
		this( scaleU, scaleV, Matrix3D.IDENTITY );
	}

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane defined by the
	 * given transformation.
	 *
	 * @param   scaleU      Scale of the UV-map in the U-direction.
	 * @param   scaleV      Scale of the UV-map in the V-direction.
	 * @param   plane2wcs   Transform plane to world coordinates.
	 */
	public PlanarUVMap( final float scaleU, final float scaleV, @NotNull final Matrix3D plane2wcs )
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
	@NotNull
	public Matrix3D getPlane2wcs()
	{
		return _plane2wcs;
	}

	/**
	 * Sets the matrix that transforms plane coordinates to model coordinates.
	 * No scaling is performed by the matrix.
	 *
	 * @param   plane2wcs   Plane to world coordinate transformation matrix.
	 */
	public void setPlane2wcs( @NotNull final Matrix3D plane2wcs )
	{
		_plane2wcs = plane2wcs;
	}

	public float[] generate( @Nullable final TextureMap textureMap, @NotNull final List<? extends Vector3D> vertexCoordinates, @Nullable final int[] vertexIndices, final boolean flipTexture )
	{
		final int vertexCount = ( vertexIndices != null ) ? vertexIndices.length : vertexCoordinates.size();

		final float[] result = new float[ vertexCount * 2 ];

		final Matrix3D plane2wcs = _plane2wcs;

		final float scaleU;
		final float scaleV;

		if ( ( textureMap != null ) && ( textureMap.getPhysicalWidth() > 0.0f ) && ( textureMap.getPhysicalHeight() > 0.0f ) )
		{
			scaleU = _scaleU / textureMap.getPhysicalWidth();
			scaleV = _scaleV / textureMap.getPhysicalHeight();
		}
		else
		{
			scaleU = _scaleU;
			scaleV = _scaleV;
		}

		int j = 0;
		for ( int i = 0 ; i < vertexCount; i++ )
		{
			final Vector3D vertex = vertexCoordinates.get( ( vertexIndices != null ) ? vertexIndices[ i ] : i );

			final float tx = (float)plane2wcs.inverseTransformX( vertex );
			final float ty = (float)plane2wcs.inverseTransformY( vertex );

			result[ j++ ] = flipTexture ? ty * scaleU : tx * scaleU;
			result[ j++ ] = flipTexture ? tx * scaleV : ty * scaleV;
		}

		return result;
	}

	public void generate( @NotNull final Vector2f result, @Nullable final TextureMap textureMap, @NotNull final Vector3D point, @NotNull final Vector3D normal, final boolean flipTexture )
	{
		final Matrix3D plane2wcs = _plane2wcs;

		final float scaleU;
		final float scaleV;

		if ( ( textureMap != null ) && ( textureMap.getPhysicalWidth() > 0.0f ) && ( textureMap.getPhysicalHeight() > 0.0f ) )
		{
			scaleU = _scaleU / textureMap.getPhysicalWidth();
			scaleV = _scaleV / textureMap.getPhysicalHeight();
		}
		else
		{
			scaleU = _scaleU;
			scaleV = _scaleV;
		}

		final float tx = (float)plane2wcs.inverseTransformX( point );
		final float ty = (float)plane2wcs.inverseTransformY( point );

		result.set( ( flipTexture ? ty : tx ) * scaleU, ( flipTexture ? tx : ty ) * scaleV );
	}

	@Override
	public boolean equals( final Object obj )
	{
		final boolean result;

		if ( obj == this )
		{
			result = true;
		}
		else if ( obj instanceof PlanarUVMap )
		{
			final PlanarUVMap other = (PlanarUVMap)obj;
			result = ( _scaleU == other._scaleU ) &&
			         ( _scaleV == other._scaleV ) &&
			         _plane2wcs.equals( other._plane2wcs );
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
		return Float.floatToIntBits( _scaleU ) ^
		       Float.floatToIntBits( _scaleV ) ^
		       _plane2wcs.hashCode();
	}
}
