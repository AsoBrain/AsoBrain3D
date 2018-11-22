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
import GeometryTools from '../ab.j3d.geom/GeometryTools';

/**
 * This class represents a 3D vector.
 *
 * @author Peter S. Heijnen
 */
export default class Vector3D
{
	/**
	 * Zero-vector.
	 */
	static ZERO = new Vector3D( 0, 0, 0 );

	/**
	 * Positive X-axis direction vector.
	 */
	static POSITIVE_X_AXIS = new Vector3D( 1, 0, 0 );

	/**
	 * Negative X-axis direction vector.
	 */
	static NEGATIVE_X_AXIS = new Vector3D( -1, 0, 0 );

	/**
	 * Positive Y-axis direction vector.
	 */
	static POSITIVE_Y_AXIS = new Vector3D( 0, 1, 0 );

	/**
	 * Negative Y-axis direction vector.
	 */
	static NEGATIVE_Y_AXIS = new Vector3D( 0, -1, 0 );

	/**
	 * Positive Z-axis direction vector.
	 */
	static POSITIVE_Z_AXIS = new Vector3D( 0, 0, 1 );

	/**
	 * Negative Z-axis direction vector.
	 */
	static NEGATIVE_Z_AXIS = new Vector3D( 0, 0, -1 );

	/**
	 * X component of 2D vector.
	 * @type number
	 */
	x;

	/**
	 * Y component of 2D vector.
	 * @type number
	 */
	y;

	/**
	 * Z component of 3D vector.
	 * @type number
	 */
	z;

	/**
	 * Construct new vector.
	 *
	 * @param {number} x X-coordinate of vector.
	 * @param {number} y Y-coordinate of vector.
	 * @param {number} z Z-coordinate of vector.
	 */
	constructor( x, y, z )
	{
		if ( typeof x !== 'number' || typeof y !== 'number' || typeof z !== 'number'  )
		{
			throw new TypeError();
		}

		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Get angle between this vector and another one specified as argument.
	 *
	 * @param {Vector3D} v1 First vector.
	 * @param {Vector3D} v2 Second vector.
	 *
	 * @return {number} angle between vectors in radians.
	 */
	static angle( v1, v2 )
	{
		return Math.acos( Vector3D.cosAngle( v1, v2 ) );
	}

	/**
	 * Test if two vectors are parallel to each other.
	 *
	 * @param {Vector3D} v1 First vector.
	 * @param {Vector3D} v2 Second vector.
	 *
	 * @return {boolean} {@code true} if the vectors are parallel; {@code false} if not.
	 */
	static areParallel( v1, v2 )
	{
		return GeometryTools.almostEqual( Math.abs( Vector3D.cosAngle( v1, v2 ) ) - 1, 0 );
	}

	/**
	 * Test if two vectors define the same direction.
	 *
	 * @param {Vector3D} v1 First vector.
	 * @param {Vector3D} v2 Second vector.
	 *
	 * @return {boolean} {@code true} if the vectors define the same direction; {@code false} if not.
	 */
	static areSameDirection( v1, v2 )
	{
		return GeometryTools.almostEqual( Vector3D.cosAngle( v1, v2 ) - 1, 0 );
	}

	/**
	 * Test if two vectors are perpendicular to each other.
	 *
	 * @param {Vector3D} v1 First vector.
	 * @param {Vector3D} v2 Second vector.
	 *
	 * @return {boolean} {@code true} if the vectors are perpendicular; {@code false} if not.
	 */
	static arePerpendicular( v1, v2 )
	{
		return GeometryTools.almostEqual( Vector3D.dot( v1, v2 ), 0 );
	}

	/**
	 * Get cos(angle) between this vector and another one specified as argument.
	 *
	 * @param {Vector3D} v1 First vector.
	 * @param {Vector3D} v2 Second vector.
	 *
	 * @return {number} cos(angle) between vectors.
	 */
	static cosAngle( v1, v2 )
	{
		let l = v1.length() * v2.length();
		return ( l === 0 ) ? 0 : Vector3D.dot( v1, v2 ) / l;
	}

	/**
	 * Determine cross product between two vectors.
	 *
	 * <p>The cross product is related to the sine function by the equation
	 *
	 * <blockquote>|a &times; b| = |a| |b| sin &theta;</blockquote>
	 *
	 * where &theta; denotes the angle between the two vectors.
	 *
	 * @param {Vector3D} v1 First vector.
	 * @param {Vector3D} v2 Second vector.
	 *
	 * @return {Vector3D} Resulting vector.
	 */
	static cross( v1, v2 )
	{
		return Vector3D.ZERO.set(
				v1.y * v2.z - v1.z * v2.y,
				v1.z * v2.x - v1.x * v2.z,
				v1.x * v2.y - v1.y * v2.x
		);
	}

	/**
	 * Determine Z component of cross between two vectors.
	 *
	 * @param {Vector3D} v1 First vector.
	 * @param {Vector3D} v2 Second vector.
	 *
	 * @return {number} Resulting vector.
	 */
	static crossZ( v1, v2 )
	{
		return v1.x * v2.y - v1.y * v2.x;
	}

	/**
	 * Calculate distance between two point vectors.
	 *
	 * @param {Vector3D} p1 First point vector to calculate the distance between.
	 * @param {Vector3D} p2 Second point vector to calculate the distance between.
	 *
	 * @return {number} Distance between this and the specified other vector.
	 */
	static distanceBetween( p1, p2 )
	{
		return p1.distanceTo( p2 );
	}

	/**
	 * Calculate distance between this point vector and another.
	 *
	 * @param {Vector3D} other Point vector to calculate the distance to.
	 *
	 * @return {number} Distance between this and the other vector.
	 */
	distanceTo( other )
	{
		return other.minus( this ).length();
	}

	/**
	 * Get direction from one point to another point.
	 *
	 * @param {Vector3D} from Point vector for from-point.
	 * @param {Vector3D} to   Point vector for to-point.
	 *
	 * @return {Vector3D} Direction from from-point to to-point.
	 */
	static direction( from, to )
	{
		return to.minus( from ).normalize();
	}

	/**
	 * Get direction from this point vector to another.
	 *
	 * @param {Vector3D} other Point vector to calculate the direction to.
	 *
	 * @return {Vector3D} Direction from this to the other vector.
	 */
	directionTo( other )
	{
		return Vector3D.direction( this, other );
	}

	/**
	 * Calculate average of two vectors (i.e. center between two point vectors).
	 *
	 * @param {Vector3D} v1 First vector.
	 * @param {Vector3D} v2 Second vector.
	 *
	 * @return {Vector3D} Average vector (i.e. center point).
	 */
	static average( v1, v2 )
	{
		return v1.equals( v2 ) ? v1 : new Vector3D( 0.5 * ( v1.x + v2.x ), 0.5 * ( v1.y + v2.y ), 0.5 * ( v1.z + v2.z ) );
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector and another one
	 * specified as argument.
	 *
	 * <p The dot product is related to the cosine function by the equation
	 * *
	 * <blockquote>a &middot; b = |a| |b| cos &theta;</blockquote>
	 *
	 * where &theta; denotes the angle between the two vectors.
	 *
	 * @param {Vector3D} v1 First vector operand.
	 * @param {Vector3D} v2 Second vector operand.
	 *
	 * @return {number} Dot product.
	 */
	static dot( v1, v2 )
	{
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector and another one
	 * specified as argument.
	 *
	 * <p The dot product is related to the cosine function by the equation
	 * *
	 * <blockquote>a &middot; b = |a| |b| cos &theta;</blockquote>
	 *
	 * where &theta; denotes the angle between the two vectors.
	 *
	 * @param {number} x1 X-coordinate of first vector operand.
	 * @param {number} y1 Y-coordinate of first vector operand.
	 * @param {number} z1 Z-coordinate of first vector operand.
	 * @param {number} x2 X-coordinate of second vector operand.
	 * @param {number} y2 Y-coordinate of second vector operand.
	 * @param {number} z2 Z-coordinate of second vector operand.
	 *
	 * @return {number} Dot product.
	 */
	static dot6( x1, y1, z1, x2, y2, z2 )
	{
		return x1 * x2 + y1 * y2 + z1 * z2;
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param other Vector to compare with.
	 *
	 * @return {boolean} true if the objects are almost equal; {@code false} if not.
	 *
	 * @see GeometryTools#almostEqual
	 */
	almostEquals( other )
	{
		return other === this ||
			   GeometryTools.almostEqual( this.x, other.x ) &&
			   GeometryTools.almostEqual( this.y, other.y ) &&
			   GeometryTools.almostEqual( this.z, other.z );
	}

	/**
	 * Returns whether the given object is a vector equal to this.
	 *
	 * @param {*} other Object to compare with.
	 *
	 * @returns {boolean} true if equal.
	 */
	equals( other )
	{
		return ( this === other ) || ( other && ( this.x === other.x ) && ( this.y === other.y ) && ( this.z === other.z ) );
	}

	/**
	 * Get inverse vector.
	 *
	 * @return {Vector3D} Inverse vector.
	 */
	inverse()
	{
		return new Vector3D( -this.x, -this.y, -this.z );
	}

	/**
	 * Test whether this vector is a non-zero vector (its length is non-zero).
	 *
	 * This will return {@code false} when all components of this vector are
	 * zero or any component is NaN.
	 *
	 * @return {boolean} {@code true} if this is a non-zero vector.
	 */
	isNonZero()
	{
		return ( ( this.x !== 0 ) || ( this.y !== 0 ) || ( this.z !== 0 ) ) && ( this.x === this.x ) && ( this.y === this.y ) && ( this.z === this.z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return {number} Length of vector.
	 */
	length()
	{
		return Math.sqrt( this.x * this.x + this.y * this.y + this.z * this.z );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param {Vector3D} other Vector to subtract from this vector.
	 *
	 * @return {Vector3D} Resulting vector.
	 */
	minus( other )
	{
		return this.set( this.x - other.x, this.y - other.y, this.z - other.z );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param {number} factor Scale multiplication factor.
	 *
	 * @return {Vector3D} Resulting vector.
	 */
	multiply( factor )
	{
		return this.set( this.x * factor, this.y * factor, this.z * factor );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1, it
	 * will be returned as-is.
	 *
	 * @return {Vector3D} Normalized vector.
	 */
	normalize()
	{
		let l = this.length();
		return ( ( l === 0 ) || ( l === 1 ) ) ? this : this.set( this.x / l, this.y / l, this.z / l );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param {Vector3D} other Vector to add to this vector.
	 *
	 * @return {Vector3D} Resulting vector.
	 */
	plus( other )
	{
		return this.set( this.x + other.x, this.y + other.y, this.z + other.z );
	}

	/**
	 * Set vector to the specified coordinates.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return {Vector3D} Resulting vector.
	 */
	set( x, y, z )
	{
		return new Vector3D(
				x === undefined ? this.x : x,
				y === undefined ? this.y : y,
				z === undefined ? this.z : z
		);
	}

	/**
	 * Get string representation of object.
	 *
	 * @return {string} String representation of object.
	 */
	toString()
	{
		return this.x + "," + this.y + ',' + this.z;
	}

	/**
	 * Get string representation of object.
	 *
	 * @return {string} String representation of object.
	 */
	toFriendlyString()
	{
		return '[ ' + this.x + ', ' + this.y + ', ' + this.z + ' ]';
	}

	/**
	 * This function translates cartesian coordinates to polar/spherical
	 * coordinates.
	 *
	 * <p>The polar/spherical coordinates are defined as the triplet {@code (r,
		 * &theta;, &rho; )}, where r is radius, &theta; is the azimuth, and &rho; is
	 * the zenith.
	 *
	 * @return {Vector3D} Polar coordinates (radius,azimuth,zenith) based on cartesian
	 *         coordinates defined by this vector.
	 */
	cartesianToPolar()
	{
		return Vector3D.cartesianToPolar( this.x, this.y, this.z );
	}

	/**
	 * This function translates cartesian coordinates to polar/spherical
	 * coordinates.
	 *
	 * <p>The polar/spherical coordinates are defined as the triplet {@code (r,
		 * &theta;, &rho; )}, where r is radius, &theta; is the azimuth, and &rho; is
	 * the zenith.
	 *
	 * <p>See <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical
	 * Coordinates</a> at <a href="http://mathworld.wolfram.com/">MathWorld</a>.<br
	 * /> See <a href="http://astronomy.swin.edu.au/~pbourke/projection/coords/">Coordinate
	 * System Transformation</a> by <a href="http://astronomy.swin.edu.au/~pbourke/">Paul
	 * Bourke</a>.
	 *
	 * @param {number} x Cartesian X coordinate.
	 * @param {number} y Cartesian Y coordinate.
	 * @param {number} z Cartesian Z coordinate.
	 *
	 * @return {Vector3D} Polar coordinates (radius,azimuth,zenith) based on cartesian
	 *         coordinates defined by this vector.
	 */
	static cartesianToPolar( x, y, z )
	{
		let result;

		let xSquared = x * x;
		let ySquared = y * y;
		let zSquared = z * z;

		if ( ( xSquared === 0 ) && ( ySquared === 0 ) && ( zSquared === 0 ) )
		{
			result = Vector3D.ZERO;
		}
		else
		{
			let radius = Math.sqrt( xSquared + ySquared + zSquared );
			let azimuth = Math.atan2( y, x );
			let zenith = Math.atan2( Math.sqrt( xSquared + ySquared ), z );

			result = new Vector3D( radius, azimuth, zenith );
		}

		return result;
	}

	/**
	 * This function translates polar/spherical coordinates to cartesian
	 * coordinates.
	 *
	 * <p>The polar/spherical coordinates are defined as the triplet {@code (r,
		 * &theta;, &rho; )}, where r is radius, &theta; is the azimuth, and &rho; is
	 * the zenith.
	 *
	 * @return {Vector3D} Cartesian coordinates based on polar coordinates
	 *         (radius,azimuth,zenith) defined by this vector.
	 */
	polarToCartesian()
	{
		return Vector3D.polarToCartesian( this.x, this.y, this.z );
	}

	/**
	 * This function translates polar/spherical coordinates to cartesian
	 * coordinates.
	 *
	 * <p>The polar/spherical coordinates are defined as the triplet {@code (r,
		 * &theta;, &rho; )}, where r is radius, &theta; is the azimuth, and &rho; is
	 * the zenith.
	 *
	 * <p>See <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical
	 * Coordinates</a> at <a href="http://mathworld.wolfram.com/">MathWorld</a>.<br
	 * /> See <a href="http://astronomy.swin.edu.au/~pbourke/projection/coords/">Coordinate
	 * System Transformation</a> by <a href="http://astronomy.swin.edu.au/~pbourke/">Paul
	 * Bourke</a>.
	 *
	 * @param {number} radius  Radius of sphere.
	 * @param {number} azimuth Angle measured from the x-axis in the XY-plane (0 => point on
	 *                XZ-plane).
	 * @param {number} zenith  Angle measured from the z-axis toward the XY-plane (0 =>
	 *                point on Z-axis).
	 *
	 * @return {Vector3D} Cartesian coordinates based on polar coordinates
	 *         (radius,azimuth,zenith) defined by this vector.
	 */
	static polarToCartesian( radius, azimuth, zenith )
	{
		let result;

		if ( radius === 0 )
		{
			result = Vector3D.ZERO;
		}
		else
		{
			let radiusXY = radius * Math.sin( zenith );

			result = new Vector3D(
					radiusXY * Math.cos( azimuth ),
					radiusXY * Math.sin( azimuth ),
					radius * Math.cos( zenith )
			);
		}

		return result;
	}
}
