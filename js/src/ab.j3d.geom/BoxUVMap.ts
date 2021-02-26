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
import { ArrayTools } from '@numdata/oss';

import Matrix3D from '../ab.j3d/Matrix3D';
import Vector3D from '../ab.j3d/Vector3D';

import PlanarUVMap from './PlanarUVMap';
import UVMap from './UVMap';

/**
 * Defines a box UV-mapping.
 *
 * @author Gerrit Meinders
 */
export default class BoxUVMap
	extends UVMap
{
	/** Left map index.   */ static LEFT   = 0;
	/** Right map index.  */ static RIGHT  = 1;
	/** Front map index.  */ static FRONT  = 2;
	/** Back map index.   */ static BACK   = 3;
	/** Bottom map index. */ static BOTTOM = 4;
	/** Top map index.    */ static TOP    = 5;

	/**
	 * Transforms box to world coordinates.
	 */
	_box2wcs: Matrix3D;

	/**
	 * UV maps used to map the sides.
	 */
	_maps: PlanarUVMap[];

	/**
	 * Texture flipping for each map.
	 */
	_flips: boolean[];

	/**
	 * Construct new UV-map that applies a uniform box mapping with the
	 * specified box coordinate system.
	 *
	 * @param scale Size of a model unit in meters.
	 * @param [box2wcs] Transforms box to world coordinates.
	 * @param [flipLeft] Flip left texture direction.
	 * @param [flipRight] Flip right texture direction.
	 * @param [flipFront] Flip front texture direction.
	 * @param [flipBack] Flip right texture direction.
	 * @param [flipTop] Flip top texture direction.
	 * @param [flipBottom] Flip bottom texture direction.
	 */
	constructor( scale: number, box2wcs: Matrix3D, flipLeft = false, flipRight = false, flipFront = false, flipBack = false, flipTop = false, flipBottom = false )
	{
		super();

		if ( box2wcs === undefined )
		{
			box2wcs = Matrix3D.IDENTITY;
		}
		else if ( box2wcs instanceof Vector3D )
		{
			box2wcs = Matrix3D.getTranslation( box2wcs );
		}

		const maps = [];
		const flips = [];

		maps[ BoxUVMap.LEFT ] = new PlanarUVMap( scale, scale, new Matrix3D(
			-box2wcs.xy,  box2wcs.xz, -box2wcs.xx, box2wcs.xo,     // [  0,  0, -1 ]
			-box2wcs.yy,  box2wcs.yz, -box2wcs.yx, box2wcs.yo,     // [ -1,  0,  0 ] * box2wcs
			-box2wcs.zy,  box2wcs.zz, -box2wcs.zx, box2wcs.zo ) ); // [  0,  1,  0 ]

		flips[ BoxUVMap.LEFT ] = !!flipLeft;

		maps[ BoxUVMap.RIGHT ] = new PlanarUVMap( scale, scale, new Matrix3D(
			 box2wcs.xy,  box2wcs.xz,  box2wcs.xx, box2wcs.xo,     // [ 0,  0,  1 ]
			 box2wcs.yy,  box2wcs.yz,  box2wcs.yx, box2wcs.yo,     // [ 1,  0,  0 ] * box2wcs
			 box2wcs.zy,  box2wcs.zz,  box2wcs.zx, box2wcs.zo ) ); // [ 0,  1,  0 ]

		flips[ BoxUVMap.RIGHT ] = !!flipRight;

		maps[ BoxUVMap.FRONT ] = new PlanarUVMap( scale, scale, new Matrix3D(
			 box2wcs.xx,  box2wcs.xz, -box2wcs.xy, box2wcs.xo,    // [ 1,  0,  0 ]
			 box2wcs.yx,  box2wcs.yz, -box2wcs.yy, box2wcs.yo,    // [ 0,  0, -1 ] * box2wcs
			 box2wcs.zx,  box2wcs.zz, -box2wcs.zy, box2wcs.zo ) ); // [ 0,  1,  0 ]

		flips[ BoxUVMap.FRONT ] = !!flipFront;

		maps[ BoxUVMap.BACK ] = new PlanarUVMap( scale, scale, new Matrix3D(
			-box2wcs.xx,  box2wcs.xz,  box2wcs.xy, box2wcs.xo,     // [ -1,  0,  0 ]
			-box2wcs.yx,  box2wcs.yz,  box2wcs.yy, box2wcs.yo,     // [  0,  0,  1 ] * box2wcs
			-box2wcs.zx,  box2wcs.zz,  box2wcs.zy, box2wcs.zo ) ); // [  0,  1,  0 ]

		flips[ BoxUVMap.BACK ] = !!flipBack;

		maps[ BoxUVMap.BOTTOM ] = new PlanarUVMap( scale, scale, new Matrix3D(
			-box2wcs.xx,  box2wcs.xy, -box2wcs.xz, box2wcs.xo,     // [ -1,  0,  0 ]
			-box2wcs.yx,  box2wcs.yy, -box2wcs.yz, box2wcs.yo,     // [  0,  1,  0 ] * box2wcs
			-box2wcs.zx,  box2wcs.zy, -box2wcs.zz, box2wcs.zo ) ); // [  0,  0, -1 ]

		flips[ BoxUVMap.BOTTOM ] = !!flipBottom;

		maps[ BoxUVMap.TOP ] = new PlanarUVMap( scale, scale, box2wcs );

		flips[ BoxUVMap.TOP ] = !!flipTop;

		this._box2wcs = box2wcs;
		this._maps = maps;
		this._flips = flips;
	}

	/**
	 * Returns the planar UV-map for one of the sides of the box.
	 *
	 * @param {number} side Side to get the UV-map for. See constants.
	 *
	 * @return {PlanarUVMap} Planar UV-map for the specified side.
	 */
	getSide( side )
	{
		if ( ( side < 0 ) || ( side >= 6 ) )
		{
			throw new TypeError( "side: " + side );
		}
		return this._maps[ side ];
	}

	getGenerator( textureMap, normal: Vector3D, flipTexture: boolean )
	{
		const map = this.getTargetMap( normal );
		return this._maps[ map ].getGenerator( textureMap, normal, this._flips[ map ] !== flipTexture );
	}

	/**
	 * Get target map based on the specified normal vector. The normal vector is
	 * rotated using the
	 *
	 * @param {Vector3D} normal  Normal vector (does not need to be normalized).
	 *
	 * @return {number} Target map.
	 */
	getTargetMap( normal )
	{
		const boxNormal = this._box2wcs.inverseRotate( normal );

		const negX = ( boxNormal.x < 0 );
		const negY = ( boxNormal.y < 0 );
		const negZ = ( boxNormal.z < 0 );
		const absX = negX ? -boxNormal.x : boxNormal.x;
		const absY = negY ? -boxNormal.y : boxNormal.y;
		const absZ = negZ ? -boxNormal.z : boxNormal.z;

		return ( absZ >= absX ) ? ( absZ >= absY ) ? negZ ? BoxUVMap.BOTTOM : BoxUVMap.TOP
		                                           : negY ? BoxUVMap.FRONT : BoxUVMap.BACK
		                        : ( absX >= absY ) ? negX ? BoxUVMap.LEFT : BoxUVMap.RIGHT
		                                           : negY ? BoxUVMap.FRONT : BoxUVMap.BACK;
	}

	equals( other )
	{
		let result;

		if ( other === this )
		{
			result = true;
		}
		else if ( other instanceof BoxUVMap )
		{
			result = ArrayTools.equals( this._flips, other._flips ) &&
					 this._box2wcs.equals( other._box2wcs ) &&
					 ArrayTools.equals( this._maps, other._maps );
		}
		else
		{
			result = false;
		}

		return result;
	}
}
