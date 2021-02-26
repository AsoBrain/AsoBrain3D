/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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

import Matrix3D from '../ab.j3d/Matrix3D';

import TransformUVGenerator from './TransformUVGenerator';
import UVMap from './UVMap';
import Vector3D from '../ab.j3d/Vector3D';
import TextureMap from '../ab.j3d/TextureMap';

/**
 * Defines a planar UV-mapping.
 *
 * @author Gerrit Meinders
 */
export default class PlanarUVMap
	extends UVMap
{
	/**
	 * Scale applied to model coordinates when calculating the texture
	 * U-coordinates.
	 */
	_scaleU: number;

	/**
	 * Scale applied to model coordinates when calculating the texture
	 * V-coordinates.
	 */
	_scaleV: number;

	/**
	 * Transforms plane coordinates to model coordinates in model units.
	 */
	_plane2wcs: Matrix3D;

	/**
	 * Constructs a new planar UV-map parallel to the XY-plane defined by the
	 * given transformation.
	 *
	 * @param scaleU Scale of the UV-map in the U-direction.
	 * @param scaleV Scale of the UV-map in the V-direction.
	 * @param plane2wcs Transform plane to world coordinates.
	 */
	constructor( scaleU: number, scaleV: number, plane2wcs: Matrix3D )
	{
		super();
		this._scaleU = scaleU;
		this._scaleV = scaleV;
		this._plane2wcs = plane2wcs;
	}

	/**
	 * Returns the matrix that transforms plane coordinates to model
	 * coordinates. No scaling is performed by the matrix.
	 *
	 * @return Plane to world coordinate transformation matrix.
	 */
	getPlane2wcs(): Matrix3D
	{
		return this._plane2wcs;
	}

	/**
	 * Sets the matrix that transforms plane coordinates to model coordinates.
	 * No scaling is performed by the matrix.
	 *
	 * @param plane2wcs Plane to world coordinate transformation matrix.
	 */
	setPlane2wcs( plane2wcs: Matrix3D ): void
	{
		this._plane2wcs = plane2wcs;
	}

	getGenerator( textureMap: TextureMap, normal: Vector3D, flipTexture: boolean )
	{
		const plane2wcs = this._plane2wcs;
		let scaleU = this._scaleU;
		let scaleV = this._scaleV;

		if ( textureMap )
		{
			const physicalWidth = textureMap.physicalWidth;
			const physicalHeight = textureMap.physicalHeight;

			if ( ( physicalWidth > 0 ) && ( physicalHeight > 0 ) )
			{
				scaleU /= physicalWidth;
				scaleV /= physicalHeight;
			}
		}

		let uvTransform;

		if ( flipTexture )
		{
			uvTransform = new Matrix3D( plane2wcs.xy * scaleU, plane2wcs.yy * scaleU, plane2wcs.zy * scaleU, plane2wcs.inverseYo() * scaleU,
				plane2wcs.xx * scaleV, plane2wcs.yx * scaleV, plane2wcs.zx * scaleV, plane2wcs.inverseXo() * scaleV, 0.0, 0.0, 0.0, 0.0 );
		}
		else
		{
			uvTransform = new Matrix3D( plane2wcs.xx * scaleU, plane2wcs.yx * scaleU, plane2wcs.zx * scaleU, plane2wcs.inverseXo() * scaleU,
				plane2wcs.xy * scaleV, plane2wcs.yy * scaleV, plane2wcs.zy * scaleV, plane2wcs.inverseYo() * scaleV, 0.0, 0.0, 0.0, 0.0 );
		}

		return new TransformUVGenerator( uvTransform );
	}

	equals( other: any ): boolean
	{
		let result;

		if ( other === this )
		{
			result = true;
		}
		else if ( other instanceof PlanarUVMap )
		{
			result = ( this._scaleU === other._scaleU ) &&
			         ( this._scaleV === other._scaleV ) &&
			         this._plane2wcs.equals( other._plane2wcs );
		}
		else
		{
			result = false;
		}

		return result;
	}
}
