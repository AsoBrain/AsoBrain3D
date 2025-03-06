/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2025 Peter S. Heijnen
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

import Bounds3D from '../ab.j3d/Bounds3D.js';
import Matrix3D from '../ab.j3d/Matrix3D.js';
import Vector2D, { AnyVector2D } from '../ab.j3d/Vector2D.js';
import Vector3D, { AnyVector3D } from '../ab.j3d/Vector3D.js';

/**
 * Tolerance to use for floating-point comparisons.
 */
const EPSILON = 0.00001;
const TWO_PI = 2 * Math.PI;

/**
 * This class contains utility methods to solve common geometric problems.
 *
 * @author  Peter S. Heijnen
 */
export default class GeometryTools
{
	/**
	 * Get intersection of two lines.
	 *
	 * @param p1 First point on line 1.
	 * @param p2 Second point on line 1
	 * @param p3 First point on line 2.
	 * @param p4 Second point on line 2
	 *
	 * @return Point of intersection; {@code null} if no intersection exists
	 * (parallel lines).
	 */
	static getIntersectionBetweenLines( p1: AnyVector2D, p2: AnyVector2D, p3: AnyVector2D, p4: AnyVector2D ): Vector2D | null

	/**
	 * Get intersection of two lines.
	 *
	 * @param x1 First point on line 1.
	 * @param y1 First point on line 1
	 * @param x2 Second point on line 1
	 * @param y2 Second point on line 1
	 * @param x3 First point on line 2.
	 * @param y3 First point on line 2
	 * @param x4 Second point on line 2
	 * @param y4 Second point on line 2
	 *
	 * @return Point of intersection; {@code null} if no intersection exists
	 * (parallel lines).
	 */
	static getIntersectionBetweenLines( x1: number, y1: number, x2: number, y2: number, x3: number, y3: number, x4: number, y4: number ): Vector2D | null;

	static getIntersectionBetweenLines( x1: number | AnyVector2D, y1: number | AnyVector2D, x2: number | AnyVector2D, y2: number | AnyVector2D, x3?: number, y3?: number, x4?: number, y4?: number ): Vector2D | null
	{
		if ( arguments.length === 4 )
		{
			x3 = ( x2 as AnyVector2D ).x;
			y3 = ( x2 as AnyVector2D ).y;
			x4 = ( y2 as AnyVector2D ).x;
			y4 = ( y2 as AnyVector2D ).y;
			x2 = ( y1 as AnyVector2D ).x;
			y2 = ( y1 as AnyVector2D ).y;
			y1 = ( x1 as AnyVector2D ).y;
			x1 = ( x1 as AnyVector2D ).x;
		}

		let result: Vector2D = null;

		// @ts-ignore
		const d = ( y4 - y3 ) * ( x2 - x1 ) - ( x4 - x3 ) * ( y2 - y1 );
		if ( !this.almostEqual( d, 0.0 ) ) /* are not parallel, so they intersect at some point */
		{
			// @ts-ignore
			const n1 = ( x4 - x3 ) * ( y1 - y3 ) - ( y4 - y3 ) * ( x1 - x3 );
			const ua = n1 / d;

			// @ts-ignore
			const x = x1 + ua * ( x2 - x1 );
			// @ts-ignore
			const y = y1 + ua * ( y2 - y1 );

			result = new Vector2D( x, y );
		}

		return result;
	}

	/**
	 * Test if the specified point is 'inside' the specified 3D polygon. Points
	 * on the edges and vertices are also considered 'inside'. <dl>
	 *
	 * <dt>IMPORTANT:</dt><dd><strong>The polygon must be convex!</strong>
	 * And the point must be specified in the object's own coordinate system.</dd>
	 * </dl>
	 *
	 * For an explanation of the math used here, see this site under 'Solution 4 (3D)':
	 * <a href='http://paulbourke.net/geometry/polygonmesh/'>http://paulbourke.net/geometry/polygonmesh/</a>
	 *
	 * @param polygon Convex polygon to test point against.
	 * @param point   Point to test.
	 *
	 * @return {@code true} if the point is inside the polygon; {@code false}
	 * otherwise.
	 *
	 * @throws NullPointerException if {@code polygon} is {@code null}.
	 */
	static isPointInsidePolygon( polygon: AnyVector3D[], point: AnyVector3D ): boolean
	{
		let result: boolean;

		let vertexCount = polygon.length;
		if ( vertexCount >= 3 )
		{
			/*
			 * The following code is almost a carbon copy of the example code
			 * on the web page with only performance enhancements and data
			 * structure integration.
			 */
			let i;
			let d;
			let angleSum = 0.0;

			let p1x;
			let p1y;
			let p1z;
			let m1;

			i = vertexCount - 1;
			let p2x = polygon[ i ].x - point.x;
			let p2y = polygon[ i ].y - point.y;
			let p2z = polygon[ i ].z - point.z;
			let m2 = Math.sqrt( p2x * p2x + p2y * p2y + p2z * p2z );

			for ( i = 0; i < vertexCount; i++ )
			{
				p1x = p2x;
				p1y = p2y;
				p1z = p2z;
				m1 = m2;

				p2x = polygon[ i ].x - point.x;
				p2y = polygon[ i ].y - point.y;
				p2z = polygon[ i ].z - point.z;
				m2 = Math.sqrt( p2x * p2x + p2y * p2y + p2z * p2z );

				d = m1 * m2;
				if ( GeometryTools.almostEqual( d, 0.0 ) ) /* We are on a node, consider this inside */
				{
					break;
				}

				angleSum += Math.acos( ( p1x * p2x + p1y * p2y + p1z * p2z ) / d );
			}

			/*
			 * Inside if:
			 *  - Aborted prematurely; or
			 *  - Angle sum is 2PI
			 */
			result = ( i < vertexCount ) || GeometryTools.almostEqual( angleSum, TWO_PI );
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Test if the specified point is 'inside' the specified 3D polygon. Points
	 * on the edges and vertices are also considered 'inside'. <dl>
	 *
	 * For an explanation of the math used here, see this site under
	 * 'Solution 2 (2D)': <a href='http://paulbourke.net/geometry/polygonmesh/'>http://paulbourke.net/geometry/polygonmesh/</a>.
	 *
	 * @param polygon Polygon to test point against.
	 * @param point   Point to test.
	 *
	 * @return {@code true} if the point is inside the polygon; {@code false}
	 * otherwise.
	 *
	 * @throws NullPointerException if {@code polygon} is {@code null}.
	 */
	static isPointInsidePolygon2( polygon: AnyVector2D[], point: AnyVector2D ): boolean
	{
		let angle = 0;

		let p1x: number;
		let p1y: number;
		let p2x = polygon[ polygon.length - 1 ].x - point.x;
		let p2y = polygon[ polygon.length - 1 ].y - point.y;

		for ( let i = 0, n = polygon.length; i < n; i++ )
		{
			p1x = p2x;
			p1y = p2y;
			p2x = polygon[ i ].x - point.x;
			p2y = polygon[ i ].y - point.y;

			let dtheta = Math.atan2( p2y, p2x ) - Math.atan2( p1y, p1x );
			if ( dtheta > Math.PI )
			{
				dtheta -= TWO_PI;
			}
			else if ( dtheta < -Math.PI )
			{
				dtheta += TWO_PI;
			}

			angle += dtheta;
		}

		return Math.abs( angle ) >= Math.PI;
	}

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
