/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2018 Peter S. Heijnen
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

import Bounds3D from '../ab.j3d/Bounds3D';
import Matrix3D from '../ab.j3d/Matrix3D';
import Vector3D from '../ab.j3d/Vector3D';

/**
 * Tolerance to use for floating-point comparisons.
 */
const EPSILON = 0.00001;

/**
 * This class contains utility methods to solve common geometric problems.
 *
 * @author  Peter S. Heijnen
 */
export default class GeometryTools
{
	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param {number} value1 First value to compare.
	 * @param {number} value2 Second value to compare.
	 *
	 * @return {boolean} <code>true</code> is the values are within a tolerance of
	 *          {@link #EPSILON} of each other; <code>false</code> otherwise.
	 */
	static almostEqual( value1: number, value2: number ): boolean
	{
		let delta = value1 - value2;
		return ( delta <= EPSILON ) && ( delta >= -EPSILON );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param {number} value1 First value to compare.
	 * @param {number} value2 Second value to compare.
	 *
	 * @return {boolean} <code>true</code> is {@code value1} is greater than or within a
	 * tolerance of {@link #EPSILON} of {@code value2}; {@code false}
	 * otherwise.
	 */
	static greaterOrAlmostEqual( value1: number, value2: number ): boolean
	{
		return ( ( value2 - value1 ) <= EPSILON );
	}

	/**
	 * Test if the first operand is less than the second operand or almost equal
	 * (the difference between them approaches the value 0).
	 *
	 * @param {number} value1 First value to compare.
	 * @param {number} value2 Second value to compare.
	 *
	 * @return {boolean} <code>true</code> is {@code value1} is less than or within a
	 * tolerance of {@link #EPSILON} of {@code value2}; {@code false}
	 * otherwise.
	 */
	static lessOrAlmostEqual( value1: number, value2: number ): boolean
	{
		return ( ( value1 - value2 ) <= EPSILON );
	}

	/**
	 * Test if the first operand is significantly greater than the second
	 * operand (the difference between them exceeds a tolerance of {@link
	 * #EPSILON}).
	 *
	 * @param {number} value1 First value to compare.
	 * @param {number} value2 Second value to compare.
	 *
	 * @return {boolean} <code>true</code> is {@code value1} is at least {@link #EPSILON}
	 * greater than {@code value2}; {@code false} otherwise.
	 */
	static significantlyGreaterThan( value1: number, value2: number ): boolean
	{
		return ( ( value1 - value2 ) > EPSILON );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds a tolerance of {@link #EPSILON}).
	 *
	 * @param {number} value1 First value to compare.
	 * @param {number} value2 Second value to compare.
	 *
	 * @return {boolean} <code>true</code> is {@code value1} is at least {@link #EPSILON}
	 * less than {@code value2}; {@code false} otherwise.
	 */
	static significantlyLessThan( value1: number, value2: number )
	{
		return ( ( value2 - value1 ) > EPSILON );
	}

	/**
	 * Test oriented bounding box intersection.
	 *
	 * Borrowed code from <A href='http://channel9.msdn.com/ShowPost.aspx?PostID=276041'>XNA
	 * Oriented Bounding Box Intersection Test</A>, which was based on <A
	 * href='http://www.cs.unc.edu/~geom/theses/gottschalk/main.pdf'>Collision
	 * Queries using Oriented Boxes</A> by Stefan Gottschalk.
	 *
	 * @param {!Bounds3D} box1 Oriented bounding box #1.
	 * @param {!Matrix3D} from2to1 Transformation from box #2 to box #1.
	 * @param {!Bounds3D} box2 Oriented bounding box #2.
	 *
	 * @return {boolean} {@code true} if the bounding boxes intersect; {@code false}
	 * otherwise.
	 */
	static testOrientedBoundingBoxIntersection( box1: Bounds3D, from2to1: Matrix3D, box2: Bounds3D )
	{
		const ox1 = box1.v1.x;
		const oy1 = box1.v1.y;
		const oz1 = box1.v1.z;
		const dx1 = box1.v2.x - box1.v1.x;
		const dy1 = box1.v2.y - box1.v1.y;
		const dz1 = box1.v2.z - box1.v1.z;
		const ox2 = box2.v1.x;
		const oy2 = box2.v1.y;
		const oz2 = box2.v1.z;
		const dx2 = box2.v2.x - box2.v1.x;
		const dy2 = box2.v2.y - box2.v1.y;
		const dz2 = box2.v2.z - box2.v1.z;

		const extents1X = 0.5 * dx1;
		const extents1Y = 0.5 * dy1;
		const extents1Z = 0.5 * dz1;

		const extents2X = 0.5 * dx2;
		const extents2Y = 0.5 * dy2;
		const extents2Z = 0.5 * dz2;

		const centerOtherX = ox2 + 0.5 * dx2;
		const centerOtherY = oy2 + 0.5 * dy2;
		const centerOtherZ = oz2 + 0.5 * dz2;

		const separationX = from2to1.transformX( centerOtherX, centerOtherY, centerOtherZ ) - ( ox1 + 0.5 * dx1 );
		const separationY = from2to1.transformY( centerOtherX, centerOtherY, centerOtherZ ) - ( oy1 + 0.5 * dy1 );
		const separationZ = from2to1.transformZ( centerOtherX, centerOtherY, centerOtherZ ) - ( oz1 + 0.5 * dz1 );

		const absXX = Math.abs( from2to1.xx );
		const absXY = Math.abs( from2to1.xy );
		const absXZ = Math.abs( from2to1.xz );
		const absYX = Math.abs( from2to1.yx );
		const absYY = Math.abs( from2to1.yy );
		const absYZ = Math.abs( from2to1.yz );
		const absZX = Math.abs( from2to1.zx );
		const absZY = Math.abs( from2to1.zy );
		const absZZ = Math.abs( from2to1.zz );

		const result =
		/* Test 1 X axis */ GeometryTools.significantlyLessThan( Math.abs( separationX ), extents1X + Vector3D.dot6( extents2X, extents2Y, extents2Z, absXX, absXY, absXZ ) ) &&
		/* Test 1 Y axis */ GeometryTools.significantlyLessThan( Math.abs( separationY ), extents1Y + Vector3D.dot6( extents2X, extents2Y, extents2Z, absYX, absYY, absYZ ) ) &&
		/* Test 1 Z axis */ GeometryTools.significantlyLessThan( Math.abs( separationZ ), extents1Z + Vector3D.dot6( extents2X, extents2Y, extents2Z, absZX, absZY, absZZ ) ) &&
		/* Test 2 X axis */ GeometryTools.significantlyLessThan( Math.abs( Vector3D.dot6( from2to1.xx, from2to1.yx, from2to1.zx, separationX, separationY, separationZ ) ), Vector3D.dot6( extents1X, extents1Y, extents1Z, absXX, absYX, absZX ) + extents2X ) &&
		/* Test 2 Y axis */ GeometryTools.significantlyLessThan( Math.abs( Vector3D.dot6( from2to1.xy, from2to1.yy, from2to1.zy, separationX, separationY, separationZ ) ), Vector3D.dot6( extents1X, extents1Y, extents1Z, absXY, absYY, absZY ) + extents2Y ) &&
		/* Test 2 Z axis */ GeometryTools.significantlyLessThan( Math.abs( Vector3D.dot6( from2to1.xz, from2to1.yz, from2to1.zz, separationX, separationY, separationZ ) ), Vector3D.dot6( extents1X, extents1Y, extents1Z, absXZ, absYZ, absZZ ) + extents2Z ) &&
		/* Test 3 case 1 */ ( Math.abs( separationZ * from2to1.yx - separationY * from2to1.zx ) <= extents1Y * absZX + extents1Z * absYX + extents2Y * absXZ + extents2Z * absXY ) &&
		/* Test 3 case 2 */ ( Math.abs( separationZ * from2to1.yy - separationY * from2to1.zy ) <= extents1Y * absZY + extents1Z * absYY + extents2X * absXZ + extents2Z * absXX ) &&
		/* Test 3 case 3 */ ( Math.abs( separationZ * from2to1.yz - separationY * from2to1.zz ) <= extents1Y * absZZ + extents1Z * absYZ + extents2X * absXY + extents2Y * absXX ) &&
		/* Test 3 case 4 */ ( Math.abs( separationX * from2to1.zx - separationZ * from2to1.xx ) <= extents1X * absZX + extents1Z * absXX + extents2Y * absYZ + extents2Z * absYY ) &&
		/* Test 3 case 5 */ ( Math.abs( separationX * from2to1.zy - separationZ * from2to1.xy ) <= extents1X * absZY + extents1Z * absXY + extents2X * absYZ + extents2Z * absYX ) &&
		/* Test 3 case 6 */ ( Math.abs( separationX * from2to1.zz - separationZ * from2to1.xz ) <= extents1X * absZZ + extents1Z * absXZ + extents2X * absYY + extents2Y * absYX ) &&
		/* Test 3 case 7 */ ( Math.abs( separationY * from2to1.xx - separationX * from2to1.yx ) <= extents1X * absYX + extents1Y * absXX + extents2Y * absZZ + extents2Z * absZY ) &&
		/* Test 3 case 8 */ ( Math.abs( separationY * from2to1.xy - separationX * from2to1.yy ) <= extents1X * absYY + extents1Y * absXY + extents2X * absZZ + extents2Z * absZX ) &&
		/* Test 3 case 9 */ ( Math.abs( separationY * from2to1.xz - separationX * from2to1.yz ) <= extents1X * absYZ + extents1Y * absXZ + extents2X * absZY + extents2Y * absZX );
		/* No separating axes => we have intersection */
		return result;
	}
}
