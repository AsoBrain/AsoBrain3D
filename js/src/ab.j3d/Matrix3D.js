/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
import toRadians from '@numdata/oss/lib/com.numdata.oss/toRadians';

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
	 * @type {number}
	 */
	xx;

	/**
	 * Y quotient for X component.
	 * @type {number}
	 */
	xy;

	/**
	 * Z quotient for X component.
	 * @type {number}
	 */
	xz;

	/**
	 * Translation of X component.
	 * @type {number}
	 */
	xo;

	/**
	 * X quotient for Y component.
	 * @type {number}
	 */
	yx;

	/**
	 * Y quotient for Y component.
	 * @type {number}
	 */
	yy;

	/**
	 * Z quotient for Y component.
	 * @type {number}
	 */
	yz;

	/**
	 * Translation of Y component.
	 * @type {number}
	 */
	yo;

	/**
	 * X quotient for Z component.
	 * @type {number}
	 */
	zx;

	/**
	 * Y quotient for Z component.
	 * @type {number}
	 */
	zy;

	/**
	 * Z quotient for Z component.
	 * @type {number}
	 */
	zz;

	/**
	 * Translation of Z component.
	 * @type {number}
	 */
	zo;

	/**
	 * Identity matrix.
	 * @type {Matrix3D}
	 */
	static IDENTITY = new Matrix3D(
		1.0, 0.0, 0.0, 0.0,
		0.0, 1.0, 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0 );

	/**
	 * Regex pattern only consisting of comma character.
	 * @private
	 */
	static COMMA = /,/;

	/**
	 * Construct a new matrix.
	 *
	 * @param {number} nxx X quotient for X component.
	 * @param {number} nxy Y quotient for X component.
	 * @param {number} nxz Z quotient for X component.
	 * @param {number} nxo Translation of X component.
	 * @param {number} nyx X quotient for Y component.
	 * @param {number} nyy Y quotient for Y component.
	 * @param {number} nyz Z quotient for Y component.
	 * @param {number} nyo Translation of Y component.
	 * @param {number} nzx X quotient for Z component.
	 * @param {number} nzy Y quotient for Z component.
	 * @param {number} nzz Z quotient for Z component.
	 * @param {number} nzo Translation of Z component.
	 */
	constructor( nxx, nxy, nxz, nxo,
	             nyx, nyy, nyz, nyo,
	             nzx, nzy, nzz, nzo )
	{
		if ( typeof nxx !== 'number' || typeof nxy !== 'number' || typeof nxz !== 'number' || typeof nxo !== 'number' ||
			 typeof nyx !== 'number' || typeof nyy !== 'number' || typeof nyz !== 'number' || typeof nyo !== 'number' ||
			 typeof nzx !== 'number' || typeof nzy !== 'number' || typeof nzz !== 'number' || typeof nzo !== 'number' )
		{
			throw new TypeError();
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
	 * @param {Matrix3D} other Matrix to compare with.
	 *
	 * @return {boolean} <code>true</code> if the objects are almost equal; <code>false</code> if not.
	 *
	 * @see GeometryTools#almostEqual
	 */
	almostEquals( other )
	{
		return ( other != null )
		    && ( ( other == this )
		      || ( GeometryTools.almostEqual( this.xx, other.xx ) &&
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
		           GeometryTools.almostEqual( this.zo, other.zo ) ) );
	}

	/**
	 * Returns whether the given object is a matrix equal to this.
	 *
	 * @param {*} other Object to compare with.
	 *
	 * @returns {boolean} true if equal.
	 */
	equals( other )
	{
		return ( other == this ) ||
			   ( other &&
				 ( this.xx == other.xx ) && ( this.xy == other.xy ) && ( this.xz == other.xz ) && ( this.xo == other.xo ) &&
				 ( this.yx == other.yx ) && ( this.yy == other.yy ) && ( this.yz == other.yz ) && ( this.yo == other.yo ) &&
				 ( this.zx == other.zx ) && ( this.zy == other.zy ) && ( this.zz == other.zz ) && ( this.zo == other.zo ) );
	}

	/**
	 * Get transformation matrix based on 6 parameters specifying rotation angles
	 * and a translation vector. Starting with the identity matrix, rotation is
	 * performed (Z,X,Y order), than the translation is set.
	 *
	 * @param {number} rx Rotation angle around X axis (degrees, <strong>clockwise</strong>).
	 * @param {number} ry Rotation angle around Y axis (degrees, counter-clockwise).
	 * @param {number} rz Rotation angle around Z axis (degrees, counter-clockwise).
	 * @param {number} tx X component of translation vector.
	 * @param {number} ty Y component of translation vector.
	 * @param {number} tz Z component of translation vector.
	 *
	 * @return {Matrix3D} Transformation matrix.
	 */
	static getTransform( rx, ry, rz, tx, ty, tz )
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
	 * @param {Vector3D} from        Point to look from.
	 * @param {Vector3D} to          Point to look at.
	 * @param {Vector3D} upPrimary   Primary up-vector (must be normalized).
	 * @param {Vector3D} upSecondary Secondary up-vector (must be normalized).
	 *
	 * @return {Matrix3D} Transformation matrix.
	 *
	 * @throws TypeError if any of the arguments is <code>null</code>.
	 * @throws Error if the from and two points are too close.
	 */
	static getFromToTransform( from, to, upPrimary, upSecondary )
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
	 * @param {Vector3D} origin          Origin of plane.
	 * @param {Vector3D} normal          Normal of plane.
	 * @param {boolean} rightHanded     3D-space is right- vs. left-handed.
	 *
	 * @return {Matrix3D} Transformation matrix (translation set to 0-vector) to be used for extrusion of 2D shapes.
	 */
	static getPlaneTransform( origin, normal, rightHanded )
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
	 * @param {number} originX Origin X-coordinate of plane.
	 * @param {number} originY Origin X-coordinate of plane.
	 * @param {number} originZ Origin X-coordinate of plane.
	 * @param {number} normalX X-component of normal vector of plane.
	 * @param {number} normalY X-component of normal vector of plane.
	 * @param {number} normalZ X-component of normal vector of plane.
	 * @param {boolean} rightHanded 3D-space is right- vs. left-handed.
	 *
	 * @return {Matrix3D} Transformation matrix (translation set to 0-vector) to be used for extrusion of 2D shapes.
	 */
	static _getPlaneTransform( originX, originY, originZ, normalX, normalY, normalZ, rightHanded )
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
	 * @return {Matrix3D} Rotation material.
	 */
	getRotation()
	{
		let lx = new Vector3D( this.xx, this.xy, this.xz ).length;
		let ly = new Vector3D( this.yx, this.yy, this.yz ).length;
		let lz = new Vector3D( this.zx, this.zy, this.zz ).length;

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
	 * @param {Vector3D} pivot       Pivot point about which the rotation is performed.
	 * @param {Vector3D} direction   Rotation axis direction (must be a unit vector).
	 * @param {number} thetaRad    Rotate theta radians.
	 *
	 * @return {Matrix3D} Transformation matrix with requested rotation.
	 */
	static getRotationTransform( pivot, direction, thetaRad )
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
	 * @param {number} pivotX      Pivot point about which the rotation is performed.
	 * @param {number} pivotY      Pivot point about which the rotation is performed.
	 * @param {number} pivotZ      Pivot point about which the rotation is performed.
	 * @param {number} directionX  Rotation axis direction (must be a unit vector).
	 * @param {number} directionY  Rotation axis direction (must be a unit vector).
	 * @param {number} directionZ  Rotation axis direction (must be a unit vector).
	 * @param {number} thetaRad    Rotate theta radians.
	 *
	 * @return {Matrix3D} Transformation matrix with requested rotation.
	 */
	static _getRotationTransform( pivotX, pivotY, pivotZ, directionX, directionY, directionZ, thetaRad )
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
	 * @param {number} x Scaling factor for the x-axis.
	 * @param {number} [y] Scaling factor for the y-axis.
	 * @param {number} [z] Scaling factor for the z-axis.
	 *
	 * @return {Matrix3D} Scaling matrix with the given factor.
	 */
	static getScaleTransform( x, y, z )
	{
		if ( arguments.length == 1 )
		{
			//noinspection JSSuspiciousNameCombination
			y = x;
			z = x;
		}

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
	 * @return {Matrix3D} Inverse matrix.
	 */
	inverse()
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
	 * @return {number} X-axis translation of inverse matrix.
	 */
	inverseXo()
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
	 * @return {number} Y-axis translation of inverse matrix.
	 */
	inverseYo()
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
	 * @return {number} Z-axis translation of inverse matrix.
	 */
	inverseZo()
	{
		return - this.xo * this.xz - this.yo * this.yz - this.zo * this.zz;
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param {number|Vector3D} x X-coordinate of vector specifying the translation.
	 * @param {number} [y] Y-coordinate of vector specifying the translation.
	 * @param {number} [z] Z-coordinate of vector specifying the translation.
	 *
	 * @return {Matrix3D} new Matrix3D with translation
	 */
	minus( x, y, z )
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return ( ( x == 0.0 ) && ( y == 0.0 ) && ( z == 0.0 ) ) ? this : this.setTranslation( new Vector3D( this.xo - x, this.yo - y, this.zo - z ) );
	}

	/**
	 * Execute matrix multiplication between this and another matrix.
	 *
	 * @param {Matrix3D} other Matrix to multiply with.
	 *
	 * @return {Matrix3D} Resulting matrix.
	 */
	multiply( other )
	{
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
	 * @param {number} xx1     X quotient for X component of first matrix.
	 * @param {number} xy1     Y quotient for X component of first matrix.
	 * @param {number} xz1     Z quotient for X component of first matrix.
	 * @param {number} xo1     Translation of X component of first matrix.
	 * @param {number} yx1     X quotient for Y component of first matrix.
	 * @param {number} yy1     Y quotient for Y component of first matrix.
	 * @param {number} yz1     Z quotient for Y component of first matrix.
	 * @param {number} yo1     Translation of Y component of first matrix.
	 * @param {number} zx1     X quotient for Z component of first matrix.
	 * @param {number} zy1     Y quotient for Z component of first matrix.
	 * @param {number} zz1     Z quotient for Z component of first matrix.
	 * @param {number} zo1     Translation of Z component of first matrix.
	 * @param {number} xx2     X quotient for X component of second matrix.
	 * @param {number} xy2     Y quotient for X component of second matrix.
	 * @param {number} xz2     Z quotient for X component of second matrix.
	 * @param {number} xo2     Translation of X component of second matrix.
	 * @param {number} yx2     X quotient for Y component of second matrix.
	 * @param {number} yy2     Y quotient for Y component of second matrix.
	 * @param {number} yz2     Z quotient for Y component of second matrix.
	 * @param {number} yo2     Translation of Y component of second matrix.
	 * @param {number} zx2     X quotient for Z component of second matrix.
	 * @param {number} zy2     Y quotient for Z component of second matrix.
	 * @param {number} zz2     Z quotient for Z component of second matrix.
	 * @param {number} zo2     Translation of Z component of second matrix.
	 *
	 * @return {Matrix3D} Resulting matrix.
	 */
	static _multiply(
		xx1, xy1, xz1, xo1,
		yx1, yy1, yz1, yo1,
		zx1, zy1, zz1, zo1,
		xx2, xy2, xz2, xo2,
		yx2, yy2, yz2, yo2,
		zx2, zy2, zz2, zo2 )
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
	 * @param {Matrix3D} other   Matrix whose inverse to multiply with.
	 *
	 * @return {Matrix3D} Resulting matrix.
	 */
	multiplyInverse( other )
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
	 * @param {number|Vector3D} x X-coordinate of vector specifying the translation.
	 * @param {number} [y] Y-coordinate of vector specifying the translation.
	 * @param {number} [z] Z-coordinate of vector specifying the translation.
	 *
	 * @return {Matrix3D} new Matrix3D with translation
	 */
	plus( x, y, z )
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		return ( x == 0.0 && y == 0.0 && z == 0.0 ) ? this : this.setTranslation( new Vector3D( this.xo + x, this.yo + y, this.zo + z ) );
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
	 * @param {number} thetaRad Rotate theta radians about the X-axis
	 *
	 * @return {Matrix3D} Rotated matrix.
	 */
	rotateX( thetaRad )
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
	 * @param {number} thetaRad Rotate theta radians about the Y-axis
	 *
	 * @return {Matrix3D} Rotated matrix.
	 */
	rotateY( thetaRad )
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
	 * @param {number} thetaRad Rotate theta radians about the Z-axis
	 *
	 * @return {Matrix3D} Rotated matrix.
	 */
	rotateZ( thetaRad )
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
	 * @param {number} nxx X quotient for X component.
	 * @param {number} nxy Y quotient for X component.
	 * @param {number} nxz Z quotient for X component.
	 * @param {number} nxo Translation of X component.
	 * @param {number} nyx X quotient for Y component.
	 * @param {number} nyy Y quotient for Y component.
	 * @param {number} nyz Z quotient for Y component.
	 * @param {number} nyo Translation of Y component.
	 * @param {number} nzx X quotient for Z component.
	 * @param {number} nzy Y quotient for Z component.
	 * @param {number} nzz Z quotient for Z component.
	 * @param {number} nzo Translation of Z component.
	 *
	 * @return {Matrix3D} Resulting vector.
	 */
	set( nxx, nxy, nxz, nxo,
		 nyx, nyy, nyz, nyo,
		 nzx, nzy, nzz, nzo )
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

		return ( lxx == this.xx && lxy == this.xy && lxz == this.xz && lxo == this.xo &&
		         lyx == this.yx && lyy == this.yy && lyz == this.yz && lyo == this.yo &&
		         lzx == this.zx && lzy == this.zy && lzz == this.zz && lzo == this.zo )
		       ? this
		       : new Matrix3D( lxx, lxy, lxz, lxo,
		                       lyx, lyy, lyz, lyo,
		                       lzx, lzy, lzz, lzo );
	}

	/**
	 * Scale this matrix by the given factors.
	 *
	 * @param {number} x Scaling factor for the x-axis.
	 * @param {number} [y] Scaling factor for the y-axis. (Default: x)
	 * @param {number} [z] Scaling factor for the z-axis. (Default: x)
	 *
	 * @return {Matrix3D} This matrix scaled  with the given factor.
	 */
	scale( x, y, z )
	{
		y = y === undefined ? x : y;
		z = z === undefined ? x : z;

		return new Matrix3D(
				x * this.xx, x * this.xy, x * this.xz, x * this.xo,
				y * this.yx, y * this.yy, y * this.yz, y * this.yo,
				z * this.zx, z * this.zy, z * this.zz, z * this.zo );
	}

	/**
	 * Get translation vector from this transform.
	 *
	 * @return {Vector3D} Translation vector.
	 */
	getTranslation()
	{
		return new Vector3D( this.xo, this.yo, this.zo );
	}

	/**
	 * Get translation matrix from the specified translation vector.
	 *
	 * @param {number|Vector3D} x X-translation.
	 * @param {number} [y] Y-translation.
	 * @param {number} [z] Z-translation.
	 *
	 * @return {Matrix3D} Translation matrix.
	 */
	static getTranslation( x, y, z )
	{
		if ( x instanceof Vector3D )
		{
			({x, y, z} = x);
		}

		if ( ( x == 0.0 ) && ( y == 0.0 ) && ( z == 0.0 ) )
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
	 * @param {number|Vector3D} x X-value of vector.
	 * @param {number} [y] Y-value of vector.
	 * @param {number} [z] Z-value of vector.
	 *
	 * @return {Matrix3D} Resulting matrix.
	 */
	setTranslation( x, y, z )
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
	 * @return {string} String representation of object.
	 */
	toString()
	{
		return this.xx + "," + this.xy + ',' + this.xz + ',' + this.xo + ',' +
		       this.yx + ',' + this.yy + ',' + this.yz + ',' + this.yo + ',' +
		       this.zx + ',' + this.zy + ',' + this.zz + ',' + this.zo;
	}

	/**
	 * Get string representation of object.
	 *
	 * @return {string} String representation of object.
	 */
	toFriendlyString()
	{
		return "\n\t\t\t[ " + this.xx + ", " + this.xy + ', ' + this.xz + ', ' + this.xo + ' ]' +
		       "\n\t\t\t[ " + this.yx + ', ' + this.yy + ', ' + this.yz + ', ' + this.yo + ' ]' +
		       "\n\t\t\t[ " + this.zx + ', ' + this.zy + ', ' + this.zz + ', ' + this.zo + ' ]';
	}

	/**
	 * Transform a vector using this transform.
	 *
	 * @param {number|Vector3D|Bounds3D} x X-value of vector.
	 * @param {number} [y] Y-value of vector.
	 * @param {number} [z] Z-value of vector.
	 *
	 * @return {Vector3D} Resulting vector.
	 */
	transform( x, y, z )
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
	 * @param {number[]} source      Source array.
	 * @param {number[]} dest        Destination array (may be <code>null</code> or too small to create new).
	 * @param {number} pointCount  Number of vertices.
	 *
	 * @return {number[]} Array to which the transformed coordinates were written (may be different from the <code>dest</code> argument).
	 *
	 * @see     #transform(double, double, double)
	 * @see     #transform(Vector3D)
	 */
	transformArray( source, dest, pointCount )
	{
		let result = dest;

		if ( ( source != dest ) || ( this != Matrix3D.IDENTITY ) )
		{
			let resultLength = pointCount * 3;
			if ( ( result == null ) || ( resultLength > result.length ) )
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
			if ( ( lxx == 1.0 ) && ( lxy == 0.0 ) && ( lxz == 0.0 ) &&
			     ( lyx == 0.0 ) && ( lyy == 1.0 ) && ( lyz == 0.0 ) &&
			     ( lzx == 0.0 ) && ( lzy == 0.0 ) && ( lzz == 1.0 ) )
			{
				if ( ( lxo == 0.0 ) && ( lyo == 0.0 ) && ( lzo == 0.0 ) )
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
			else if ( ( lxo == 0.0 ) && ( lyo == 0.0 ) && ( lzo == 0.0 ) )
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
	 * @param {number|Vector3D} x X-coordinate of vector.
	 * @param {number} y Y-coordinate of vector.
	 * @param {number} z Z-coordinate of vector.
	 *
	 * @return {number} Resulting X coordinate.
	 */
	transformX( x, y, z )
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
	 * @param {number|Vector3D} x X-coordinate of vector.
	 * @param {number} y Y-coordinate of vector.
	 * @param {number} z Z-coordinate of vector.
	 *
	 * @return {number} Resulting Y coordinate.
	 */
	transformY( x, y, z )
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
	 * @param {number|Vector3D} x X-coordinate of vector.
	 * @param {number} y Y-coordinate of vector.
	 * @param {number} z Z-coordinate of vector.
	 *
	 * @return {number} Resulting Z coordinate.
	 */
	transformZ( x, y, z )
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
	 * @param {number|Vector3D} x X-value of vector.
	 * @param {number} [y] Y-value of vector.
	 * @param {number} [z] Z-value of vector.
	 *
	 * @return {Vector3D} Resulting vector.
	 */
	inverseTransform( x, y, z )
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
	 * @param {number|Vector3D} x X-coordinate of vector.
	 * @param {number} [y] Y-coordinate of vector.
	 * @param {number} [z] Z-coordinate of vector.
	 *
	 * @return {number} Resulting X coordinate.
	 */
	inverseTransformX( x, y, z )
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
	 * @param {number|Vector3D} x X-coordinate of vector.
	 * @param {number} [y] Y-coordinate of vector.
	 * @param {number} [z] Z-coordinate of vector.
	 *
	 * @return {number} Resulting Y coordinate.
	 */
	inverseTransformY( x, y, z )
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
	 * @param {number|Vector3D} x X-coordinate of vector.
	 * @param {number} [y] Y-coordinate of vector.
	 * @param {number} [z] Z-coordinate of vector.
	 *
	 * @return {number} Resulting Z coordinate.
	 */
	inverseTransformZ( x, y, z )
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
	 * @param {number|Vector3D} x X component of directional vector to rotate.
	 * @param {number} [y] Y component of directional vector to rotate.
	 * @param {number} [z] Z component of directional vector to rotate.
	 *
	 * @return  Rotated vector.
	 */
	rotate( x, y, z )
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
	 * @param {number[]} source Source array.
	 * @param {number[]} dest Destination array (may be <code>null</code> or too small to create new).
	 * @param {number} vectorCount Number of vertices.
	 *
	 * @return {number[]} Array to which the transformed coordinates were written (may be different from the <code>dest</code> argument).
	 *
	 * @see     #transform(double, double, double)
	 * @see     #transform(Vector3D)
	 */
	rotateArray( source, dest, vectorCount )
	{
		let result = dest;

		if ( ( source != dest ) || ( this != Matrix3D.IDENTITY ) )
		{
			let resultLength = vectorCount * 3;
			if ( ( result == null ) || ( resultLength > result.length ) )
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

			if ( ( lxx == 1.0 ) && ( lxy == 0.0 ) && ( lxz == 0.0 )
			  && ( lyx == 0.0 ) && ( lyy == 1.0 ) && ( lyz == 0.0 )
			  && ( lzx == 0.0 ) && ( lzy == 0.0 ) && ( lzz == 1.0 ) )
			{
				if ( source != result )
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
	 * @param {number|Vector3D} x X-coordinate of vector.
	 * @param {number} y Y-coordinate of vector.
	 * @param {number} z Z-coordinate of vector.
	 *
	 * @return {number} Resulting X coordinate.
	 */
	rotateVectorX( x, y, z )
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
	 * @param {number|Vector3D} x X-coordinate of vector.
	 * @param {number} y Y-coordinate of vector.
	 * @param {number} z Z-coordinate of vector.
	 *
	 * @return {number} Resulting Y coordinate.
	 */
	rotateVectorY( x, y, z )
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
	 * @param {number|Vector3D} x X-coordinate of vector.
	 * @param {number} y Y-coordinate of vector.
	 * @param {number} z Z-coordinate of vector.
	 *
	 * @return {number} Resulting Z coordinate.
	 */
	rotateVectorZ( x, y, z )
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
	 * @param {number|Vector3D} x X component of directional vector to rotate.
	 * @param {number} [y] Y component of directional vector to rotate.
	 * @param {number} [z] Z component of directional vector to rotate.
	 *
	 * @return  Rotated vector.
	 */
	inverseRotate( x, y, z )
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
	 * @param {number|Vector3D} x X component of directional vector to rotate.
	 * @param {number} [y] Y component of directional vector to rotate.
	 * @param {number} [z] Z component of directional vector to rotate.
	 *
	 * @return {number} Resulting X coordinate.
	 */
	inverseRotateX( x, y, z )
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
	 * @param {number|Vector3D} x X component of directional vector to rotate.
	 * @param {number} [y] Y component of directional vector to rotate.
	 * @param {number} [z] Z component of directional vector to rotate.
	 *
	 * @return {number} Resulting Y-coordinate.
	 */
	inverseRotateY( x, y, z )
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
	 * @param {number|Vector3D} x X component of directional vector to rotate.
	 * @param {number} [y] Y component of directional vector to rotate.
	 * @param {number} [z] Z component of directional vector to rotate.
	 *
	 * @return {number} Resulting Z-coordinate.
	 */
	inverseRotateZ( x, y, z )
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
	 * @return {boolean} <code>true</code> if the coordinate system is right-handed.
	 */
	isRighthanded()
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
	 * @return {number} Determinant of the matrix.
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/Determinant">Determinant (Wikipedia)</a>
	 */
	determinant()
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
	 * @returns {number[]} Elements of the matrix (row-major order).
	 */
	toArray()
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
	 * @returns {number[]} Elements of the matrix (row-major order).
	 */
	toColumnMajorArray()
	{
		return [
			this.xx, this.yx, this.zx, 0,
			this.xy, this.yy, this.zy, 0,
			this.xz, this.yz, this.zz, 0,
			this.xo, this.yo, this.zo, 1
		];
	}
}
