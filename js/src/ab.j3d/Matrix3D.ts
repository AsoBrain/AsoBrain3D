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
import { toRadians } from '@numdata/oss';

import GeometryTools from '../ab.j3d.geom/GeometryTools';

import Vector3D from './Vector3D';
import Bounds3D from './Bounds3D';

/**
 * This class is used to represent a 3D transformation matrix (although
 * it may also be used for 2D transformations).
 *
 * <p>
 * The matrix is organized as follows:
 * <pre>
 * | xx xy xz xo |
 * | yx yy yz yo |
 * | zx zy zz zo |
 * | 0  0  0  1  |
 * </pre>
 *
 * @author  Peter S. Heijnen
 */
export default class Matrix3D
{
	/**
	 * X quotient for X component.
	 */
	readonly xx: number;

	/**
	 * Y quotient for X component.
	 */
	readonly xy: number;

	/**
	 * Z quotient for X component.
	 */
	readonly xz: number;

	/**
	 * Translation of X component.
	 */
	readonly xo: number;

	/**
	 * X quotient for Y component.
	 */
	readonly yx: number;

	/**
	 * Y quotient for Y component.
	 */
	readonly yy: number;

	/**
	 * Z quotient for Y component.
	 */
	readonly yz: number;

	/**
	 * Translation of Y component.
	 */
	readonly yo: number;

	/**
	 * X quotient for Z component.
	 */
	readonly zx: number;

	/**
	 * Y quotient for Z component.
	 */
	readonly zy: number;

	/**
	 * Z quotient for Z component.
	 */
	readonly zz: number;

	/**
	 * Translation of Z component.
	 */
	readonly zo: number;

	/**
	 * Identity matrix.
	 */
	static IDENTITY = new Matrix3D(
		1.0, 0.0, 0.0, 0.0,
		0.0, 1.0, 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0 );

	/**
	 * Construct a new matrix.
	 *
	 * @param nxx X quotient for X component.
	 * @param nxy Y quotient for X component.
	 * @param nxz Z quotient for X component.
	 * @param nxo Translation of X component.
	 * @param nyx X quotient for Y component.
	 * @param nyy Y quotient for Y component.
	 * @param nyz Z quotient for Y component.
	 * @param nyo Translation of Y component.
	 * @param nzx X quotient for Z component.
	 * @param nzy Y quotient for Z component.
	 * @param nzz Z quotient for Z component.
	 * @param nzo Translation of Z component.
	 */
	constructor( nxx: number, nxy: number, nxz: number, nxo: number,
	             nyx: number, nyy: number, nyz: number, nyo: number,
	             nzx: number, nzy: number, nzz: number, nzo: number )
	{
		if ( !Number.isFinite( nxx ) || !Number.isFinite( nxy ) || !Number.isFinite( nxz ) || !Number.isFinite( nxo ) ||
			 !Number.isFinite( nyx ) || !Number.isFinite( nyy ) || !Number.isFinite( nyz ) || !Number.isFinite( nyo ) ||
			 !Number.isFinite( nzx ) || !Number.isFinite( nzy ) || !Number.isFinite( nzz ) || !Number.isFinite( nzo ) )
		{
			throw new TypeError( String( [
				nxx, nxy, nxz, nxo,
				nyx, nyy, nyz, nyo,
				nzx, nzy, nzz, nzo
			] ) );
		}

		this.xx = nxx;
		this.xy = nxy;
		this.xz = nxz;
		this.xo = nxo;

		this.yx = nyx;
		this.yy = nyy;
		this.yz = nyz;
		this.yo = nyo;

		this.zx = nzx;
		this.zy = nzy;
		this.zz = nzz;
		this.zo = nzo;
	}

	/**
	 * Compare this matrix to another matrix.
	 *
	 * @param other Matrix to compare with.
	 *
	 * @return <code>true</code> if the objects are almost equal; <code>false</code> if not.
	 *
	 * @see GeometryTools#almostEqual
	 */
	almostEquals( other: Matrix3D ): boolean
	{
		return other && (
			( other === this ) ||
			( GeometryTools.almostEqual( this.xx, other.xx ) &&
			  GeometryTools.almostEqual( this.xy, other.xy ) &&
			  GeometryTools.almostEqual( this.xz, other.xz ) &&
			  GeometryTools.almostEqual( this.xo, other.xo ) &&
			  GeometryTools.almostEqual( this.yx, other.yx ) &&
			  GeometryTools.almostEqual( this.yy, other.yy ) &&
			  GeometryTools.almostEqual( this.yz, other.yz ) &&
			  GeometryTools.almostEqual( this.yo, other.yo ) &&
			  GeometryTools.almostEqual( this.zx, other.zx ) &&
			  GeometryTools.almostEqual( this.zy, other.zy ) &&
			  GeometryTools.almostEqual( this.zz, other.zz ) &&
			  GeometryTools.almostEqual( this.zo, other.zo ) )
		);
	}

	/**
	 * Returns whether the given object is a matrix equal to this.
	 *
	 * @param other Object to compare with.
	 *
	 * @returns true if equal.
	 */
	equals( other: any ): boolean
	{
		return ( other === this ) ||
			   ( other &&
				 ( this.xx === other.xx ) && ( this.xy === other.xy ) && ( this.xz === other.xz ) && ( this.xo === other.xo ) &&
				 ( this.yx === other.yx ) && ( this.yy === other.yy ) && ( this.yz === other.yz ) && ( this.yo === other.yo ) &&
				 ( this.zx === other.zx ) && ( this.zy === other.zy ) && ( this.zz === other.zz ) && ( this.zo === other.zo ) );
	}

	/**
	 * Get transformation matrix based on 6 parameters specifying rotation angles
	 * and a translation vector. Starting with the identity matrix, rotation is
	 * performed (Z,X,Y order), than the translation is set.
	 *
	 * @param rx Rotation angle around X axis (degrees, <strong>clockwise</strong>).
	 * @param ry Rotation angle around Y axis (degrees, counter-clockwise).
	 * @param rz Rotation angle around Z axis (degrees, counter-clockwise).
	 * @param tx X component of translation vector.
	 * @param ty Y component of translation vector.
	 * @param tz Z component of translation vector.
	 *
	 * @return Transformation matrix.
	 */
	static getTransform( rx: number, ry: number, rz: number, tx: number, ty: number, tz: number ): Matrix3D
	{
		// FIXME: rx should be counter-clockwise!
		let radX = toRadians( rx );
		let radY = toRadians( ry );
		let radZ = toRadians( rz );

		let ctX = Math.cos( radX );
		let stX = Math.sin( radX );
		let ctY = Math.cos( radY );
		let stY = Math.sin( radY );
		let ctZ = Math.cos( radZ );
		let stZ = Math.sin( radZ );

		return new Matrix3D(
			/* xx */  ctZ * ctY - stZ * stX * stY,
			/* xy */ -stZ * ctY - ctZ * stX * stY,
			/* xz */  ctX * stY,
			/* xo */  tx,

			/* yx */  stZ * ctX,
			/* yy */  ctZ * ctX,
			/* yz */        stX,
			/* y0 */  ty,

			/* zx */ -stZ * stX * ctY - ctZ * stY,
			/* zy */ -ctZ * stX * ctY + stZ * stY,
			/* zz */        ctX * ctY,
			/* zo */  tz );
	}

	/**
	 * Get transformation matrix based on the specified 'from' and 'to' points.
	 * The result can be used to convert world coordinates to view coordinates.
	 *
	 * <p>
	 * An up-vector must be specified to determine the correct view orientation.
	 * Note that both a primary and a secondary up-vector is needed; the primary
	 * up-vector is used when possible, the secondary up-vector is used when the
	 * from-to vector is parallel to the primary up-vector.
	 *
	 * @param from        Point to look from.
	 * @param to          Point to look at.
	 * @param upPrimary   Primary up-vector (must be normalized).
	 * @param upSecondary Secondary up-vector (must be normalized).
	 *
	 * @return Transformation matrix.
	 *
	 * @throws TypeError if any of the arguments is <code>null</code>.
	 * @throws Error if the from and two points are too close.
	 */
	static getFromToTransform( from: Vector3D, to: Vector3D, upPrimary: Vector3D, upSecondary: Vector3D ): Matrix3D
	{
		if ( from.almostEquals( to ) )
		{
			throw new Error( "from ~= to" );
		}

		/*
		 * Z-axis points out of the to-point (center) towards the from-point (eye).
		 */
		let zx = from.x - to.x;
		let zy = from.y - to.y;
		let zz = from.z - to.z;

		let normalizeZ = 1.0 / Math.sqrt( zx * zx + zy * zy + zz * zz );
		zx *= normalizeZ;
		zy *= normalizeZ;
		zz *= normalizeZ;

		/*
		 * Select up-vector. If the co-sinus of the angle between the Z-axis
		 * and up-vector approaches 1 or -1 (0 or 180 degrees), use the secondary
		 * up-vector; use the primary up-vector otherwise.
		 */
		let up = upPrimary;
		let cos = up.x * zx + up.y * zy + up.z * zz;
		if ( ( cos < -0.999 ) || ( cos > 0.999 ) )
		{
			up = upSecondary;
		}

		/*
		 * X-axis is perpendicular to the Z-axis and the up-vector.
		 */
		let xx = up.y * zz - up.z * zy;
		let xy = up.z * zx - up.x * zz;
		let xz = up.x * zy - up.y * zx;

		let normalizeX = 1.0 / Math.sqrt( xx * xx + xy * xy + xz * xz );
		xx *= normalizeX;
		xy *= normalizeX;
		xz *= normalizeX;

		/*
		 * Y-axis is perpendicular to the Z- and X-axis.
		 */
		let yx = zy * xz - zz * xy;
		let yy = zz * xx - zx * xz;
		let yz = zx * xy - zy * xx;

		/*
		 * Create matrix.
		 */
		return new Matrix3D( xx, xy, xz, ( -from.x * xx -from.y * xy -from.z * xz ),
		                     yx, yy, yz, ( -from.x * yx -from.y * yy -from.z * yz ),
		                     zx, zy, zz, ( -from.x * zx -from.y * zy -from.z * zz ) );
	}

	/**
	 * Calculate transform for a plane that passes through <code>origin</code>
	 * and has the specified <code>normal</code> vector.
	 *
	 * <p>
	 * The main purpose for this method is creating a suitable 3D transformation
	 * for a 2D plane whose Z-axis points 'out' of the plane; the orientation of
	 * the X/Y-axes on the plane is inherently indeterminate, but this function
	 * tries to find reasonable defaults.
	 *
	 * <p>
	 * A 0-vector multiplied with the resulting transform will match the
	 * <code>origin</code>.
	 *
	 * @param origin          Origin of plane.
	 * @param normal          Normal of plane.
	 * @param rightHanded     3D-space is right- vs. left-handed.
	 *
	 * @return Transformation matrix (translation set to 0-vector) to be used for extrusion of 2D shapes.
	 */
	static getPlaneTransform( origin: Vector3D, normal: Vector3D, rightHanded: boolean ): Matrix3D
	{
		return Matrix3D._getPlaneTransform( origin.x, origin.y, origin.z, normal.x, normal.y, normal.z, rightHanded );
	}

	/**
	 * Calculate transform for a plane that passes through <code>origin</code>
	 * and has the specified <code>normal</code> vector.
	 *
	 * <p>
	 * The main purpose for this method is creating a suitable 3D transformation
	 * for a 2D plane whose Z-axis points 'out' of the plane; the orientation of
	 * the X/Y-axes on the plane is inherently indeterminate, but this function
	 * tries to find reasonable defaults.
	 *
	 * <p>
	 * A 0-vector multiplied with the resulting transform will match the
	 * <code>origin</code>.
	 *
	 * @param originX Origin X-coordinate of plane.
	 * @param originY Origin X-coordinate of plane.
	 * @param originZ Origin X-coordinate of plane.
	 * @param normalX X-component of normal vector of plane.
	 * @param normalY X-component of normal vector of plane.
	 * @param normalZ X-component of normal vector of plane.
	 * @param rightHanded 3D-space is right- vs. left-handed.
	 *
	 * @return Transformation matrix (translation set to 0-vector) to be used for extrusion of 2D shapes.
	 */
	static _getPlaneTransform( originX: number, originY: number, originZ: number, normalX: number, normalY: number, normalZ: number, rightHanded: boolean ): Matrix3D
	{
		/*
		 * X-axis direction is perpendicular to the plane between the (local)
		 * Z-axis and the world's Z-axis. If the Z-axes are almost parallel,
		 * then the world Y-axis is used.
		 *
 		 * This will keep the local X-axis on the X/Y-plane as much as possible.
		 */
		let xAxisX;
		let xAxisY;
		let xAxisZ;

		if ( ( normalZ > -0.9 ) && ( normalZ < 0.9 ) )
		{
			let hyp = Math.hypot( normalX, normalY );

			xAxisX = rightHanded ? -normalY / hyp :  normalY / hyp;
			xAxisY = rightHanded ?  normalX / hyp : -normalX / hyp;
			xAxisZ = 0.0;
		}
		else
		{
			let hyp = Math.hypot( normalX, normalZ );

			xAxisX = rightHanded ?  normalZ / hyp : -normalZ / hyp;
			xAxisY = 0.0;
			xAxisZ = rightHanded ? -normalX / hyp :  normalX / hyp;
		}

		/*
		 * Y-axis can be derived simply from the calculated X- and Z-axis.
		 */
		let yAxisX = normalY * xAxisZ - normalZ * xAxisY;
		let yAxisY = normalZ * xAxisX - normalX * xAxisZ;
		let yAxisZ = normalX * xAxisY - normalY * xAxisX;

		return new Matrix3D( xAxisX, yAxisX, normalX, originX,
		                     xAxisY, yAxisY, normalY, originY,
		                     xAxisZ, yAxisZ, normalZ, originZ );
	}

	/**
	 * Get rotation matrix. This eliminates scaling and translation properties
	 * from the current transformation matrix.
	 *
	 * @return Rotation material.
	 */
	getRotation(): Matrix3D
	{
		let lx = new Vector3D( this.xx, this.xy, this.xz ).length();
		let ly = new Vector3D( this.yx, this.yy, this.yz ).length();
		let lz = new Vector3D( this.zx, this.zy, this.zz ).length();

		return new Matrix3D( this.xx / lx, this.xy / lx, this.xz / lx, 0.0,
		                     this.yx / ly, this.yy / ly, this.yz / ly, 0.0,
		                     this.zx / lz, this.zy / lz, this.zz / lz, 0.0 );
	}

	/**
	 * Get transformation matrix for a rotation about an arbitrary axis. The
	 * rotation is axis is specified by a pivot point and rotation axis
	 * direction. The rotation angle is specified in radians.
	 *
	 * <p>
	 * Also read <a href='http://www.cprogramming.com/tutorial/3d/rotation.html'>Rotation About an Arbitrary Axis</a>
	 * (written by: Confuted, with a cameo by Silvercord (Charles Thibualt)).
	 *
	 * @param pivot       Pivot point about which the rotation is performed.
	 * @param direction   Rotation axis direction (must be a unit vector).
	 * @param thetaRad    Rotate theta radians.
	 *
	 * @return Transformation matrix with requested rotation.
	 */
	static getRotationTransform( pivot: Vector3D, direction: Vector3D, thetaRad: number ): Matrix3D
	{
		return Matrix3D._getRotationTransform( pivot.x, pivot.y, pivot.z, direction.x, direction.y, direction.z, thetaRad );
	}

	/**
	 * Get transformation matrix for a rotation about an arbitrary axis. The
	 * rotation is axis is specified by a pivot point and rotation axis
	 * direction. The rotation angle is specified in radians.
	 *
	 * <p>
	 * Read <a href='http://www.mlahanas.de/Math/orientation.htm'>We consider the problem of the coordinate transformation about a rotation axis.</a>
	 * (written by Michael Lahanas) for a simple explanation of the problem and
	 * solution.
	 *
	 * @param pivotX      Pivot point about which the rotation is performed.
	 * @param pivotY      Pivot point about which the rotation is performed.
	 * @param pivotZ      Pivot point about which the rotation is performed.
	 * @param directionX  Rotation axis direction (must be a unit vector).
	 * @param directionY  Rotation axis direction (must be a unit vector).
	 * @param directionZ  Rotation axis direction (must be a unit vector).
	 * @param thetaRad    Rotate theta radians.
	 *
	 * @return Transformation matrix with requested rotation.
	 */
	static _getRotationTransform( pivotX: number, pivotY: number, pivotZ: number, directionX: number, directionY: number, directionZ: number, thetaRad: number ): Matrix3D
	{
		let cos = Math.cos( thetaRad );
		let sin = Math.sin( thetaRad );

		let t  = ( 1.0 - cos );
		let tx = t * directionX;
		let ty = t * directionY;
		let tz = t * directionZ;

		let xx = tx * directionX + cos;
		let xy = tx * directionY - sin * directionZ;
		let xz = tx * directionZ + sin * directionY;

		let yx = ty * directionX + sin * directionZ;
		let yy = ty * directionY + cos;
		let yz = ty * directionZ - sin * directionX;

		let zx = tz * directionX - sin * directionY;
		let zy = tz * directionY + sin * directionX;
		let zz = tz * directionZ + cos;

		return new Matrix3D( xx, xy, xz, pivotX - xx * pivotX - xy * pivotY - xz * pivotZ,
		                     yx, yy, yz, pivotY - yx * pivotX - yy * pivotY - yz * pivotZ,
		                     zx, zy, zz, pivotZ - zx * pivotX - zy * pivotY - zz * pivotZ );
	}

	/**
	 * Returns a transformation matrix that scales coordinates by the given
	 * factors. If only one argument is given, a uniform scaling matrix is
	 * created.
	 *
	 * @param x Scaling factor for the x-axis.
	 * @param [y] Scaling factor for the y-axis.
	 * @param [z] Scaling factor for the z-axis.
	 *
	 * @return Scaling matrix with the given factor.
	 */
	static getScaleTransform( x: number, y: number = x, z: number = x )
	{
		return new Matrix3D(   x, 0.0, 0.0, 0.0,
		                     0.0,   y, 0.0, 0.0,
		                     0.0, 0.0,   z, 0.0 );
	}

	/**
	 * Construct inverse matrix.
	 * <pre>
	 *       | xx xy xz xo |
	 *       | yx yy yz yo |
	 *  T  = | zx zy zz zo |
	 *       | 0  0  0  1  |
	 *
	 *       | xx yz zx -xo*xx-yo*yx-zo*zx |
	 *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
	 *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
	 *       | 0  0  0  1                  |
	 * </pre>
	 *
	 * @return Inverse matrix.
	 */
	inverse(): Matrix3D
	{
		return new Matrix3D( this.xx, this.yx, this.zx, this.inverseXo(),
		                     this.xy, this.yy, this.zy, this.inverseYo(),
		                     this.xz, this.yz, this.zz, this.inverseZo() );
	}

	/**
	 * Get <code>xo</code> of inverse transform.
	 * <pre>
	 *       | xx xy xz xo |
	 *       | yx yy yz yo |
	 *  T  = | zx zy zz zo |
	 *       | 0  0  0  1  |
	 *
	 *       | xx yz zx <b>-xo*xx-yo*yx-zo*zx</b> |
	 *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
	 *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
	 *       | 0  0  0  1                  |
	 * </pre>
	 *
	 * @return X-axis translation of inverse matrix.
	 */
	inverseXo(): number
	{
		return - this.xo * this.xx - this.yo * this.yx - this.zo * this.zx;
	}

	/**
	 * Get <code>yo</code> of inverse transform.
	 * <pre>
	 *       | xx xy xz xo |
	 *       | yx yy yz yo |
	 *  T  = | zx zy zz zo |
	 *       | 0  0  0  1  |
	 *
	 *       | xx yz zx -xo*xx-yo*yx-zo*zx |
	 *   -1  | xy yy zy <b>-xo*xy-yo*yy-zo*zy</b> |
	 *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
	 *       | 0  0  0  1                  |
	 * </pre>
	 *
	 * @return Y-axis translation of inverse matrix.
	 */
	inverseYo(): number
	{
		return - this.xo * this.xy - this.yo * this.yy - this.zo * this.zy;
	}

	/**
	 * Get <code>zo</code> of inverse transform.
	 * <pre>
	 *       | xx xy xz xo |
	 *       | yx yy yz yo |
	 *  T  = | zx zy zz zo |
	 *       | 0  0  0  1  |
	 *
	 *       | xx yz zx -xo*xx-yo*yx-zo*zx |
	 *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
	 *  T  = | xz yz zz <b>-xo*xz-yo*yz-zo*zz</b> |
	 *       | 0  0  0  1                  |
	 * </pre>
	 *
	 * @return Z-axis translation of inverse matrix.
	 */
	inverseZo(): number
	{
		return - this.xo * this.xz - this.yo * this.yz - this.zo * this.zz;
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param v Vector specifying the translation.
	 *
	 * @return new Matrix3D with translation
	 */
	minus( v: Vector3D ): Matrix3D;

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param x X-coordinate of vector specifying the translation.
	 * @param y Y-coordinate of vector specifying the translation.
	 * @param z Z-coordinate of vector specifying the translation.
	 *
	 * @return new Matrix3D with translation
	 */
	minus( x: number, y: number, z: number ): Matrix3D;

	minus( x: any, y?: number, z?: number ): Matrix3D
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return ( ( x === 0.0 ) && ( y === 0.0 ) && ( z === 0.0 ) ) ? this : this.setTranslation( new Vector3D( this.xo - x, this.yo - y, this.zo - z ) );
	}

	/**
	 * Execute matrix multiplication between this and another matrix.
	 *
	 * @param other Matrix to multiply with.
	 *
	 * @return Resulting matrix.
	 */
	multiply( other: Matrix3D ): Matrix3D
	{
		if ( !( other instanceof Matrix3D ) )
		{
			throw new TypeError( other );
		}
		return Matrix3D._multiply( this.xx, this.xy, this.xz, this.xo,
		                           this.yx, this.yy, this.yz, this.yo,
		                           this.zx, this.zy, this.zz, this.zo,
		                           other.xx, other.xy, other.xz, other.xo,
		                           other.yx, other.yy, other.yz, other.yo,
		                           other.zx, other.zy, other.zz, other.zo );
	}

	/**
	 * Execute matrix multiplication between two matrices.
	 *
	 * @param xx1     X quotient for X component of first matrix.
	 * @param xy1     Y quotient for X component of first matrix.
	 * @param xz1     Z quotient for X component of first matrix.
	 * @param xo1     Translation of X component of first matrix.
	 * @param yx1     X quotient for Y component of first matrix.
	 * @param yy1     Y quotient for Y component of first matrix.
	 * @param yz1     Z quotient for Y component of first matrix.
	 * @param yo1     Translation of Y component of first matrix.
	 * @param zx1     X quotient for Z component of first matrix.
	 * @param zy1     Y quotient for Z component of first matrix.
	 * @param zz1     Z quotient for Z component of first matrix.
	 * @param zo1     Translation of Z component of first matrix.
	 * @param xx2     X quotient for X component of second matrix.
	 * @param xy2     Y quotient for X component of second matrix.
	 * @param xz2     Z quotient for X component of second matrix.
	 * @param xo2     Translation of X component of second matrix.
	 * @param yx2     X quotient for Y component of second matrix.
	 * @param yy2     Y quotient for Y component of second matrix.
	 * @param yz2     Z quotient for Y component of second matrix.
	 * @param yo2     Translation of Y component of second matrix.
	 * @param zx2     X quotient for Z component of second matrix.
	 * @param zy2     Y quotient for Z component of second matrix.
	 * @param zz2     Z quotient for Z component of second matrix.
	 * @param zo2     Translation of Z component of second matrix.
	 *
	 * @return Resulting matrix.
	 */
	static _multiply(
		xx1: number, xy1: number, xz1: number, xo1: number,
		yx1: number, yy1: number, yz1: number, yo1: number,
		zx1: number, zy1: number, zz1: number, zo1: number,
		xx2: number, xy2: number, xz2: number, xo2: number,
		yx2: number, yy2: number, yz2: number, yo2: number,
		zx2: number, zy2: number, zz2: number, zo2: number ): Matrix3D
	{
		return new Matrix3D( xx1 * xx2 + yx1 * xy2 + zx1 * xz2,
		                     xy1 * xx2 + yy1 * xy2 + zy1 * xz2,
		                     xz1 * xx2 + yz1 * xy2 + zz1 * xz2,
		                     xo1 * xx2 + yo1 * xy2 + zo1 * xz2 + xo2,
		                     xx1 * yx2 + yx1 * yy2 + zx1 * yz2,
		                     xy1 * yx2 + yy1 * yy2 + zy1 * yz2,
		                     xz1 * yx2 + yz1 * yy2 + zz1 * yz2,
		                     xo1 * yx2 + yo1 * yy2 + zo1 * yz2 + yo2,
		                     xx1 * zx2 + yx1 * zy2 + zx1 * zz2,
		                     xy1 * zx2 + yy1 * zy2 + zy1 * zz2,
		                     xz1 * zx2 + yz1 * zy2 + zz1 * zz2,
		                     xo1 * zx2 + yo1 * zy2 + zo1 * zz2 + zo2 );
	}

	/**
	 * Execute matrix multiplication between this and the inverse of another
	 * matrix.
	 *
	 * @param other   Matrix whose inverse to multiply with.
	 *
	 * @return Resulting matrix.
	 */
	multiplyInverse( other: Matrix3D ): Matrix3D
	{
		return Matrix3D._multiply(
				this.xx, this.xy, this.xz, this.xo,
		        this.yx, this.yy, this.yz, this.yo,
		        this.zx, this.zy, this.zz, this.zo,
		        other.xx, other.yx, other.zx, -other.xo * other.xx - other.yo * other.yx - other.zo * other.zx,
		        other.xy, other.yy, other.zy, -other.xo * other.xy - other.yo * other.yy - other.zo * other.zy,
		        other.xz, other.yz, other.zz, -other.xo * other.xz - other.yo * other.yz - other.zo * other.zz );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param v Vector specifying the translation.
	 *
	 * @return new Matrix3D with translation
	 */
	plus( v: Vector3D ): Matrix3D;

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param x X-coordinate of vector specifying the translation.
	 * @param y Y-coordinate of vector specifying the translation.
	 * @param z Z-coordinate of vector specifying the translation.
	 *
	 * @return new Matrix3D with translation
	 */
	plus( x: number, y: number, z: number ): Matrix3D;

	plus( x: any, y?: number, z?: number ): Matrix3D
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return ( x === 0.0 && y === 0.0 && z === 0.0 ) ? this : this.setTranslation( new Vector3D( this.xo + x, this.yo + y, this.zo + z ) );
	}

	/**
	 * Rotate about the X-axis in counter-clockwise direction.
	 *
	 * <p>
	 * The returned matrix is the current matrix multiplied by the following
	 * transformation matrix:
	 * <pre>
	 *    [   1     0     0   ]
	 *    [   0    cos  -sin  ]
	 *    [   0    sin   cos  ]
	 * </pre>
	 *
	 * @param thetaRad Rotate theta radians about the X-axis
	 *
	 * @return Rotated matrix.
	 */
	rotateX( thetaRad: number ): Matrix3D
	{
		let result;

		if ( GeometryTools.almostEqual( thetaRad, 0 ) )
		{
			result = this;
		}
		else
		{
			let cos = Math.cos( thetaRad );
			let sin = Math.sin( thetaRad );

			result = new Matrix3D( this.xx, this.xy, this.xz, this.xo,
			                       cos * this.yx - sin * this.zx, cos * this.yy - sin * this.zy, cos * this.yz - sin * this.zz, cos * this.yo - sin * this.zo,
			                       sin * this.yx + cos * this.zx, sin * this.yy + cos * this.zy, sin * this.yz + cos * this.zz, sin * this.yo + cos * this.zo );
		}

		return result;
	}

	/**
	 * Rotate about the Y-axis in counter-clockwise direction.
	 *
	 * <p>
	 * The returned matrix is the current matrix multiplied by the following
	 * transformation matrix:
	 * <pre>
	 *    [  cos    0    sin  ]
	 *    [   0     1     0   ]
	 *    [ -sin    0    cos  ]
	 * </pre>
	 *
	 * @param thetaRad Rotate theta radians about the Y-axis
	 *
	 * @return Rotated matrix.
	 */
	rotateY( thetaRad: number ): Matrix3D
	{
		let result;

		if ( GeometryTools.almostEqual( thetaRad, 0 ) )
		{
			result = this;
		}
		else
		{
			let cos = Math.cos( thetaRad );
			let sin = Math.sin( thetaRad );

			result = new Matrix3D(  cos * this.xx + this.zx * sin,  cos * this.xy + this.zy * sin,  cos * this.xz + this.zz * sin,  cos * this.xo + this.zo * sin,
			                       this.yx, this.yy, this.yz, this.yo,
			                       -sin * this.xx + this.zx * cos, -sin * this.xy + this.zy * cos, -sin * this.xz + this.zz * cos, -sin * this.xo + this.zo * cos );
		}

		return result;
	}

	/**
	 * Rotate about the Z-axis in counter-clockwise direction.
	 *
	 * <p>
	 * The returned matrix is the current matrix multiplied by the following
	 * transformation matrix:
	 * <pre>
	 *    [  cos  -sin    0   ]
	 *    [  sin   cos    0   ]
	 *    [   0     0     1   ]
	 * </pre>
	 *
	 * @param thetaRad Rotate theta radians about the Z-axis
	 *
	 * @return Rotated matrix.
	 */
	rotateZ( thetaRad: number ): Matrix3D
	{
		let result;

		if ( GeometryTools.almostEqual( thetaRad, 0 ) )
		{
			result = this;
		}
		else
		{
			let cos = Math.cos( thetaRad );
			let sin = Math.sin( thetaRad );

			result = new Matrix3D(
					cos * this.xx - sin * this.yx, cos * this.xy - sin * this.yy, cos * this.xz - sin * this.yz, cos * this.xo - sin * this.yo,
					sin * this.xx + cos * this.yx, sin * this.xy + cos * this.yy, sin * this.xz + cos * this.yz, sin * this.xo + cos * this.yo,
					this.zx, this.zy, this.zz, this.zo
			);
		}

		return result;
	}

	/**
	 * Set all values in the matrix and return the resulting matrix.
	 *
	 * @param nxx X quotient for X component.
	 * @param nxy Y quotient for X component.
	 * @param nxz Z quotient for X component.
	 * @param nxo Translation of X component.
	 * @param nyx X quotient for Y component.
	 * @param nyy Y quotient for Y component.
	 * @param nyz Z quotient for Y component.
	 * @param nyo Translation of Y component.
	 * @param nzx X quotient for Z component.
	 * @param nzy Y quotient for Z component.
	 * @param nzz Z quotient for Z component.
	 * @param nzo Translation of Z component.
	 *
	 * @return Resulting vector.
	 */
	set( nxx: number, nxy: number, nxz: number, nxo: number,
		 nyx: number, nyy: number, nyz: number, nyo: number,
		 nzx: number, nzy: number, nzz: number, nzo: number ): Matrix3D
	{
		let lxx = isNaN( nxx ) ? this.xx : nxx;
		let lxy = isNaN( nxy ) ? this.xy : nxy;
		let lxz = isNaN( nxz ) ? this.xz : nxz;
		let lxo = isNaN( nxo ) ? this.xo : nxo;
		let lyx = isNaN( nyx ) ? this.yx : nyx;
		let lyy = isNaN( nyy ) ? this.yy : nyy;
		let lyz = isNaN( nyz ) ? this.yz : nyz;
		let lyo = isNaN( nyo ) ? this.yo : nyo;
		let lzx = isNaN( nzx ) ? this.zx : nzx;
		let lzy = isNaN( nzy ) ? this.zy : nzy;
		let lzz = isNaN( nzz ) ? this.zz : nzz;
		let lzo = isNaN( nzo ) ? this.zo : nzo;

		return ( lxx === this.xx && lxy === this.xy && lxz === this.xz && lxo === this.xo &&
		         lyx === this.yx && lyy === this.yy && lyz === this.yz && lyo === this.yo &&
		         lzx === this.zx && lzy === this.zy && lzz === this.zz && lzo === this.zo )
		       ? this
		       : new Matrix3D( lxx, lxy, lxz, lxo,
		                       lyx, lyy, lyz, lyo,
		                       lzx, lzy, lzz, lzo );
	}

	/**
	 * Scale this matrix by the given factor.
	 *
	 * @param s Uniform scaling factor.
	 *
	 * @return This matrix scaled with the given factor.
	 */
	scale( s: number ): Matrix3D;

	/**
	 * Scale this matrix by the given factors.
	 *
	 * @param x Scaling factor for the x-axis.
	 * @param y Scaling factor for the y-axis.
	 * @param z Scaling factor for the z-axis.
	 *
	 * @return This matrix scaled with the given factors.
	 */
	scale( x: number, y: number, z: number ): Matrix3D;

	scale( x: any, y: number = x, z: number = x ): Matrix3D
	{
		return new Matrix3D(
				x * this.xx, x * this.xy, x * this.xz, x * this.xo,
				y * this.yx, y * this.yy, y * this.yz, y * this.yo,
				z * this.zx, z * this.zy, z * this.zz, z * this.zo );
	}

	/**
	 * Get translation vector from this transform.
	 *
	 * @return Translation vector.
	 */
	getTranslation(): Vector3D
	{
		return new Vector3D( this.xo, this.yo, this.zo );
	}

	/**
	 * Get translation matrix from the specified translation vector.
	 *
	 * @param translation Translation vector.
	 *
	 * @return Translation matrix.
	 */
	static getTranslation( translation: Vector3D ): Matrix3D;

	/**
	 * Get translation matrix from the specified translation vector.
	 *
	 * @param x X-translation.
	 * @param y Y-translation.
	 * @param z Z-translation.
	 *
	 * @return Translation matrix.
	 */
	static getTranslation( x: number, y: number, z: number ): Matrix3D;

	static getTranslation( x: any, y?: number, z?: number ): Matrix3D
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		if ( ( x === 0.0 ) && ( y === 0.0 ) && ( z === 0.0 ) )
		{
			return Matrix3D.IDENTITY;
		}
		else
		{
			return new Matrix3D(
					1.0, 0.0, 0.0, x,
					0.0, 1.0, 0.0, y,
					0.0, 0.0, 1.0, z );
		}
	}

	/**
	 * Set translation of a transform to the specified vector.
	 *
	 * @param v Translation vector.
	 *
	 * @return Resulting matrix.
	 */
	setTranslation( v: Vector3D ): Matrix3D;

	/**
	 * Set translation of a transform to the specified vector.
	 *
	 * @param x X-value of vector.
	 * @param y Y-value of vector.
	 * @param z Z-value of vector.
	 *
	 * @return Resulting matrix.
	 */
	setTranslation( x: number, y: number, z: number ): Matrix3D;

	setTranslation( x: any, y?: number, z?: number ): Matrix3D
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return new Matrix3D(
				this.xx, this.xy, this.xz, x,
				this.yx, this.yy, this.yz, y,
				this.zx, this.zy, this.zz, z );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return String representation of object.
	 */
	toString(): string
	{
		return this.xx + "," + this.xy + ',' + this.xz + ',' + this.xo + ',' +
		       this.yx + ',' + this.yy + ',' + this.yz + ',' + this.yo + ',' +
		       this.zx + ',' + this.zy + ',' + this.zz + ',' + this.zo;
	}

	/**
	 * Get string representation of object.
	 *
	 * @return String representation of object.
	 */
	toFriendlyString(): string
	{
		return "\n\t\t\t[ " + this.xx + ", " + this.xy + ', ' + this.xz + ', ' + this.xo + ' ]' +
		       "\n\t\t\t[ " + this.yx + ', ' + this.yy + ', ' + this.yz + ', ' + this.yo + ' ]' +
		       "\n\t\t\t[ " + this.zx + ', ' + this.zy + ', ' + this.zz + ', ' + this.zo + ' ]';
	}

	/**
	 * Transform a bounds using this transform.
	 *
	 * @param b Bounds to transform.
	 *
	 * @return Resulting vector.
	 */
	transform( b: Bounds3D ): Vector3D;

	/**
	 * Transform a vector using this transform.
	 *
	 * @param v Vector to transform.
	 *
	 * @return Resulting vector.
	 */
	transform( v: Vector3D ): Vector3D;

	/**
	 * Transform a vector using this transform.
	 *
	 * @param x X-value of vector.
	 * @param y Y-value of vector.
	 * @param z Z-value of vector.
	 *
	 * @return Resulting vector.
	 */
	transform( x: number, y: number, z: number ): Vector3D;

	transform( x: any, y?: number, z?: number ): Vector3D
	{
		let result;

		if ( x instanceof Bounds3D )
		{
			result = new Bounds3D( this.transform( x.v1 ), this.transform( x.v2 ) );
		}
		else
		{
			if ( x instanceof Vector3D )
			{
				({x, y, z} = x);
			}

			result = new Vector3D(
					x * this.xx + y * this.xy + z * this.xz + this.xo,
					x * this.yx + y * this.yy + z * this.yz + this.yo,
					x * this.zx + y * this.zy + z * this.zz + this.zo );
		}

		return result;
	}

	/**
	 * This function transforms a set of points. Point coordinates are supplied
	 * using double arrays containing a triplet for each point.
	 *
	 * @param source      Source array.
	 * @param dest        Destination array (may be <code>null</code> or too small to create new).
	 * @param pointCount  Number of vertices.
	 *
	 * @return Array to which the transformed coordinates were written (may be different from the <code>dest</code> argument).
	 *
	 * @see     #transform(double, double, double)
	 * @see     #transform(Vector3D)
	 */
	transformArray( source: number[], dest: number[], pointCount: number ): number[]
	{
		let result = dest;

		if ( ( source !== dest ) || ( this !== Matrix3D.IDENTITY ) )
		{
			let resultLength = pointCount * 3;
			if ( !result || ( resultLength > result.length ) )
			{
				result = [];
				result.length = resultLength;
			}

			let lxx = this.xx;
			let lxy = this.xy;
			let lxz = this.xz;
			let lyx = this.yx;
			let lyy = this.yy;
			let lyz = this.yz;
			let lzx = this.zx;
			let lzy = this.zy;
			let lzz = this.zz;
			let lxo = this.xo;
			let lyo = this.yo;
			let lzo = this.zo;

			/*
			 * Perform rotate, translate, or copy only if possible.
			 */
			if ( ( lxx === 1.0 ) && ( lxy === 0.0 ) && ( lxz === 0.0 ) &&
			     ( lyx === 0.0 ) && ( lyy === 1.0 ) && ( lyz === 0.0 ) &&
			     ( lzx === 0.0 ) && ( lzy === 0.0 ) && ( lzz === 1.0 ) )
			{
				if ( ( lxo === 0.0 ) && ( lyo === 0.0 ) && ( lzo === 0.0 ) )
				{
					for ( let i = 0; i < resultLength; i++ )
					{
						result[ i ] = source[ i ];
					}
				}
				else
				{
					for ( let i = 0; i < resultLength; i += 3 )
					{
						result[ i ] = source[ i ] + lxo;
						result[ i + 1 ] = source[ i + 1 ] + lyo;
						result[ i + 2 ] = source[ i + 2 ] + lzo;
					}
				}
			}
			else if ( ( lxo === 0.0 ) && ( lyo === 0.0 ) && ( lzo === 0.0 ) )
			{
				this.rotateArray( source, result, pointCount );
			}
			else
			{
				for ( let i = 0; i < resultLength; i += 3 )
				{
					let x = source[ i ];
					let y = source[ i + 1 ];
					let z = source[ i + 2 ];

					result[ i ] = x * lxx + y * lxy + z * lxz + lxo;
					result[ i + 1 ] = x * lyx + y * lyy + z * lyz + lyo;
					result[ i + 2 ] = x * lzx + y * lzy + z * lzz + lzo;
				}
			}
		}

		return result;
	}

	/**
	 * Transform a vector to X-coordinate using this transform.
	 *
	 * @param v Vector to transform.
	 *
	 * @return Resulting X coordinate.
	 */
	transformX( v: Vector3D ): number;

	/**
	 * Transform a vector to X-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting X coordinate.
	 */
	transformX( x: number, y: number, z: number ): number;

	transformX( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return x * this.xx + y * this.xy + z * this.xz + this.xo;
	}

	/**
	 * Transform a vector to Y-coordinate using this transform.
	 *
	 * @param v Vector to transform.
	 *
	 * @return Resulting Y coordinate.
	 */
	transformY( v: Vector3D ): number;

	/**
	 * Transform a vector to Y-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Y coordinate.
	 */
	transformY( x: number, y: number, z: number ): number;

	transformY( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return x * this.yx + y * this.yy + z * this.yz + this.yo;
	}


	/**
	 * Transform a vector to Z-coordinate using this transform.
	 *
	 * @param v Vector to transform.
	 *
	 * @return Resulting Z coordinate.
	 */
	transformZ( v: Vector3D ): number;

	/**
	 * Transform a vector to Z-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Z coordinate.
	 */
	transformZ( x: number, y: number, z: number ): number;

	transformZ( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return x * this.zx + y * this.zy + z * this.zz + this.zo;
	}

	/**
	 * Transform a vector using the inverse of this transform.
	 *
	 * @param v Vector to transform.
	 *
	 * @return Resulting vector.
	 */
	inverseTransform( v: Vector3D ): Vector3D;

	/**
	 * Transform a vector using the inverse of this transform.
	 *
	 * @param x X-value of vector.
	 * @param y Y-value o ector.
	 * @param z Z-value o ector.
	 *
	 * @return Resulting vector.
	 */
	inverseTransform( x: number, y: number, z: number ): Vector3D;

	inverseTransform( x: any, y?: number, z?: number ): Vector3D
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		let tx = x - this.xo;
		let ty = y - this.yo;
		let tz = z - this.zo;

		return new Vector3D( tx * this.xx + ty * this.yx + tz * this.zx,
		                     tx * this.xy + ty * this.yy + tz * this.zy,
		                     tx * this.xz + ty * this.yz + tz * this.zz );
	}


	/**
	 * Inverse transform a vector to X-coordinate using this transform.
	 *
	 * @param v Vector to transform.
	 *
	 * @return Resulting X coordinate.
	 */
	inverseTransformX( v: Vector3D ): number;

	/**
	 * Inverse transform a vector to X-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting X coordinate.
	 */
	inverseTransformX( x: number, y: number, z: number ): number;

	inverseTransformX( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return ( x - this.xo ) * this.xx + ( y - this.yo ) * this.yx + ( z - this.zo ) * this.zx;
	}

	/**
	 * Inverse transform a vector to Y-coordinate using this transform.
	 *
	 * @param v Vector to transform.
	 *
	 * @return Resulting Y coordinate.
	 */
	inverseTransformY( v: Vector3D ): number;

	/**
	 * Inverse transform a vector to Y-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Y coordinate.
	 */
	inverseTransformY( x: number, y: number, z: number ): number;

	inverseTransformY( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return ( x - this.xo ) * this.xy + ( y - this.yo ) * this.yy + ( z - this.zo ) * this.zy;
	}

	/**
	 * Inverse transform a vector to Z-coordinate using this transform.
	 *
	 * @param v Vector to transform.
	 *
	 * @return Resulting Z coordinate.
	 */
	inverseTransformZ( v: Vector3D ): number;

	/**
	 * Inverse transform a vector to Z-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Z coordinate.
	 */
	inverseTransformZ( x: number, y: number, z: number ): number;

	inverseTransformZ( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return ( x - this.xo ) * this.xz + ( y - this.yo ) * this.yz + ( z - this.zo ) * this.zz;
	}


	/**
	 * Rotate a vector using this transform. This multiplies the vector with
	 * this matrix, excluding the translational components.
	 *
	 * @param v Vector to rotate.
	 *
	 * @return  Rotated vector.
	 */
	rotate( v: Vector3D ): Vector3D;

	/**
	 * Rotate a vector using this transform. This multiplies the vector with
	 * this matrix, excluding the translational components.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return  Rotated vector.
	 */
	rotate( x: number, y: number, z: number ): Vector3D;

	rotate( x: any, y?: number, z?: number ): Vector3D
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return new Vector3D( x * this.xx + y * this.xy + z * this.xz,
		                     x * this.yx + y * this.yy + z * this.yz,
		                     x * this.zx + y * this.zy + z * this.zz );
	}

	/**
	 * This function performs just the rotational part of of the transform on a
	 * set of vectors. Vectors are supplied using float arrays with a triplet for
	 * each vector.
	 *
	 * @param source Source array.
	 * @param dest Destination array (may be <code>null</code> or too small to create new).
	 * @param vectorCount Number of vertices.
	 *
	 * @return Array to which the transformed coordinates were written (may be different from the <code>dest</code> argument).
	 *
	 * @see     #transform(double, double, double)
	 * @see     #transform(Vector3D)
	 */
	rotateArray( source: number[], dest: number[], vectorCount: number ): number[]
	{
		let result = dest;

		if ( ( source !== dest ) || ( this !== Matrix3D.IDENTITY ) )
		{
			let resultLength = vectorCount * 3;
			if ( !result || ( resultLength > result.length ) )
			{
				result = [];
				result.length = resultLength;
			}

			let lxx = this.xx;
			let lxy = this.xy;
			let lxz = this.xz;
			let lyx = this.yx;
			let lyy = this.yy;
			let lyz = this.yz;
			let lzx = this.zx;
			let lzy = this.zy;
			let lzz = this.zz;

			if ( ( lxx === 1.0 ) && ( lxy === 0.0 ) && ( lxz === 0.0 )
			  && ( lyx === 0.0 ) && ( lyy === 1.0 ) && ( lyz === 0.0 )
			  && ( lzx === 0.0 ) && ( lzy === 0.0 ) && ( lzz === 1.0 ) )
			{
				if ( source !== result )
				{
					for ( let resultIndex = 0; resultIndex < resultLength; resultIndex++ )
					{
						result[ resultIndex ] = source[ resultIndex ];
					}
				}
			}
			else
			{
				for ( let resultIndex = 0; resultIndex < resultLength; resultIndex += 3 )
				{
					let x = source[ resultIndex ];
					let y = source[ resultIndex + 1 ];
					let z = source[ resultIndex + 2 ];

					result[ resultIndex ] = x * lxx + y * lxy + z * lxz;
					result[ resultIndex + 1 ] = x * lyx + y * lyy + z * lyz;
					result[ resultIndex + 2 ] = x * lzx + y * lzy + z * lzz;
				}
			}
		}

		return result;
	}

	/**
	 * Rotate a vector to X-coordinate using this rotate.
	 *
	 * @param v Vector.
	 *
	 * @return Resulting X coordinate.
	 */
	rotateVectorX( v: Vector3D ): number;

	/**
	 * Rotate a vector to X-coordinate using this rotate.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting X coordinate.
	 */
	rotateVectorX( x: number, y: number, z: number ): number;

	rotateVectorX( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return x * this.xx + y * this.xy + z * this.xz;
	}

	/**
	 * Rotate a vector to Y-coordinate using this rotate.
	 *
	 * @param v Vector.
	 *
	 * @return Resulting Y coordinate.
	 */
	rotateVectorY( v: Vector3D ): number;

	/**
	 * Rotate a vector to Y-coordinate using this rotate.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Y coordinate.
	 */
	rotateVectorY( x: number, y: number, z: number ): number;

	rotateVectorY( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return x * this.yx + y * this.yy + z * this.yz;
	}

	/**
	 * Rotate a vector to Z-coordinate using this rotate.
	 *
	 * @param v Vector.
	 *
	 * @return Resulting Z coordinate.
	 */
	rotateVectorZ( v: Vector3D ): number;

	/**
	 * Rotate a vector to Z-coordinate using this rotate.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Z coordinate.
	 */
	rotateVectorZ( x: number, y: number, z: number ): number;

	rotateVectorZ( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return x * this.zx + y * this.zy + z * this.zz;
	}

	/**
	 * Rotate a vector using the inverse of this transform. This multiplies the
	 * vector with the inverse of this matrix, excluding the translational
	 * components.
	 *
	 * @param v Vector to rotate.
	 *
	 * @return  Rotated vector.
	 */
	inverseRotate( v: Vector3D ): Vector3D;

	/**
	 * Rotate a vector using the inverse of this transform. This multiplies the
	 * vector with the inverse of this matrix, excluding the translational
	 * components.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return  Rotated vector.
	 */
	inverseRotate( x: number, y: number, z: number ): Vector3D;

	inverseRotate( x: any, y?: number, z?: number ): Vector3D
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return new Vector3D( x * this.xx + y * this.yx + z * this.zx,
		                     x * this.xy + y * this.yy + z * this.zy,
		                     x * this.xz + y * this.yz + z * this.zz );
	}

	/**
	 * Calculates the X-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param v Vector to rotate.
	 *
	 * @return Resulting X coordinate.
	 */
	inverseRotateX( v: Vector3D ): number;

	/**
	 * Calculates the X-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return Resulting X coordinate.
	 */
	inverseRotateX( x: number, y: number, z: number ): number;

	inverseRotateX( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return x * this.xx + y * this.yx + z * this.zx;
	}

	/**
	 * Calculates the Y-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param v Vector to rotate.
	 *
	 * @return Resulting Y-coordinate.
	 */
	inverseRotateY( v: Vector3D ): number;

	/**
	 * Calculates the Y-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return Resulting Y-coordinate.
	 */
	inverseRotateY( x: number, y: number, z: number ): number;

	inverseRotateY( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return x * this.xy + y * this.yy + z * this.zy;
	}

	/**
	 * Calculates the Z-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param v Vector to rotate.
	 *
	 * @return Resulting Z-coordinate.
	 */
	inverseRotateZ( v: Vector3D ): number;

	/**
	 * Calculates the Z-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return Resulting Z-coordinate.
	 */
	inverseRotateZ( x: number, y: number, z: number ): number;

	inverseRotateZ( x: any, y?: number, z?: number ): number
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return x * this.xz + y * this.yz + z * this.zz;
	}

	/**
	 * Returns whether the coordinate system represented by the matrix follows
	 * the right-hand rule.
	 *
	 * @return <code>true</code> if the coordinate system is right-handed.
	 */
	isRighthanded(): boolean
	{
		// cross product of x-axis and y-axis -> derived z-axis (right-handed)
		let crossX = this.yx * this.zy - this.zx * this.yy;
		let crossY = this.zx * this.xy - this.xx * this.zy;
		let crossZ = this.xx * this.yy - this.yx * this.xy;

		// z-axis and derived (right-handed) z-axis should have same direction
		return ( Vector3D.dot( new Vector3D( crossX, crossY, crossZ ), new Vector3D( this.xz, this.yz, this.zz ) ) > 0.0 );
	}

	/**
	 * Returns the determinant of the matrix.
	 *
	 * @return Determinant of the matrix.
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/Determinant">Determinant (Wikipedia)</a>
	 */
	determinant(): number
	{
		return this.xx * this.yy * this.zz -
		       this.xx * this.yz * this.zy -
		       this.xy * this.yx * this.zz +
		       this.xy * this.yz * this.zx +
		       this.xz * this.yx * this.zy -
		       this.xz * this.yy * this.zx;
	}

	/**
	 * Returns an array containing the elements of the matrix in row-major order.
	 *
	 * @returns Elements of the matrix (row-major order).
	 */
	toArray(): number[]
	{
		return [
			this.xx, this.xy, this.xz, this.xo,
			this.yx, this.yy, this.yz, this.yo,
			this.zx, this.zy, this.zz, this.zo,
			0, 0, 0, 1
		];
	}

	/**
	 * Returns an array containing the elements of the matrix in row-major order.
	 *
	 * @returns Elements of the matrix (row-major order).
	 */
	toColumnMajorArray(): number[]
	{
		return [
			this.xx, this.yx, this.zx, 0,
			this.xy, this.yy, this.zy, 0,
			this.xz, this.yz, this.zz, 0,
			this.xo, this.yo, this.zo, 1
		];
	}
}
