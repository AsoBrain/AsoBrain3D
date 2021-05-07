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
import GeometryTools from '../ab.j3d.geom/GeometryTools';

export interface AnyVector3D
{
	readonly x: number;
	readonly y: number;
	readonly z: number;
}

/**
 * This class represents a 3D vector.
 *
 * @author Peter S. Heijnen
 */
export default class Vector3D implements AnyVector3D
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
	 */
	readonly x: number;

	/**
	 * Y component of 2D vector.
	 */
	readonly y: number;

	/**
	 * Z component of 3D vector.
	 */
	readonly z: number;

	/**
	 * Construct new vector.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 */
	constructor( x: number, y: number, z: number )
	{
		if ( typeof x !== 'number' || typeof y !== 'number' || typeof z !== 'number' )
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
	 * @param v1 First vector.
	 * @param v2 Second vector.
	 *
	 * @return angle between vectors in radians.
	 */
	static angle( v1: AnyVector3D, v2: AnyVector3D ): number
	{
		return Math.acos( Vector3D.cosAngle( v1, v2 ) );
	}

	/**
	 * Test if two vectors are parallel to each other.
	 *
	 * @param v1 First vector.
	 * @param v2 Second vector.
	 *
	 * @return {@code true} if the vectors are parallel; {@code false} if not.
	 */
	static areParallel( v1: AnyVector3D, v2: AnyVector3D ): boolean
	{
		return GeometryTools.almostEqual( Math.abs( Vector3D.cosAngle( v1, v2 ) ) - 1, 0 );
	}

	/**
	 * Test if two vectors define the same direction.
	 *
	 * @param v1 First vector.
	 * @param v2 Second vector.
	 *
	 * @return {@code true} if the vectors define the same direction; {@code false} if not.
	 */
	static areSameDirection( v1: AnyVector3D, v2: AnyVector3D ): boolean
	{
		return GeometryTools.almostEqual( Vector3D.cosAngle( v1, v2 ) - 1, 0 );
	}

	/**
	 * Test if two vectors are perpendicular to each other.
	 *
	 * @param v1 First vector.
	 * @param v2 Second vector.
	 *
	 * @return {@code true} if the vectors are perpendicular; {@code false} if not.
	 */
	static arePerpendicular( v1: AnyVector3D, v2: AnyVector3D ): boolean
	{
		return GeometryTools.almostEqual( Vector3D.dot( v1, v2 ), 0 );
	}

	/**
	 * Get cos(angle) between this vector and another one specified as argument.
	 *
	 * @param v1 First vector.
	 * @param v2 Second vector.
	 *
	 * @return cos(angle) between vectors.
	 */
	static cosAngle( v1: AnyVector3D, v2: AnyVector3D ): number
	{
		let l = Vector3D._length( v1.x, v1.y, v1.z ) * Vector3D._length( v2.x, v2.y, v2.z );
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
	 * @param v1 First vector.
	 * @param v2 Second vector.
	 *
	 * @return Resulting vector.
	 */
	static cross( v1: AnyVector3D, v2: AnyVector3D ): Vector3D
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
	 * @param v1 First vector.
	 * @param v2 Second vector.
	 *
	 * @return Resulting vector.
	 */
	static crossZ( v1: AnyVector3D, v2: AnyVector3D ): number
	{
		return v1.x * v2.y - v1.y * v2.x;
	}

	/**
	 * Calculate distance between two point vectors.
	 *
	 * @param p1 First point vector to calculate the distance between.
	 * @param p2 Second point vector to calculate the distance between.
	 *
	 * @return Distance between this and the specified other vector.
	 */
	static distanceBetween( p1: Vector3D, p2: Vector3D ): number
	{
		return p1.distanceTo( p2 );
	}

	/**
	 * Calculate distance between this point vector and another.
	 *
	 * @param other Point vector to calculate the distance to.
	 *
	 * @return Distance between this and the other vector.
	 */
	distanceTo( other: Vector3D ): number
	{
		return other.minus( this ).length();
	}

	/**
	 * Get direction from one point to another point.
	 *
	 * @param from Point vector for from-point.
	 * @param to   Point vector for to-point.
	 *
	 * @return Direction from from-point to to-point.
	 */
	static direction( from: AnyVector3D, to: Vector3D ): Vector3D
	{
		return to.minus( from ).normalize();
	}

	/**
	 * Get direction from this point vector to another.
	 *
	 * @param other Point vector to calculate the direction to.
	 *
	 * @return Direction from this to the other vector.
	 */
	directionTo( other: Vector3D ): Vector3D
	{
		return Vector3D.direction( this, other );
	}

	/**
	 * Calculate average of two vectors (i.e. center between two point vectors).
	 *
	 * @param v1 First vector.
	 * @param v2 Second vector.
	 *
	 * @return Average vector (i.e. center point).
	 */
	static average( v1: Vector3D, v2: AnyVector3D ): Vector3D
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
	 * @param v1 First vector operand.
	 * @param v2 Second vector operand.
	 *
	 * @return Dot product.
	 */
	static dot( v1: AnyVector3D, v2: AnyVector3D ): number
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
	 * @param x1 X-coordinate of first vector operand.
	 * @param y1 Y-coordinate of first vector operand.
	 * @param z1 Z-coordinate of first vector operand.
	 * @param x2 X-coordinate of second vector operand.
	 * @param y2 Y-coordinate of second vector operand.
	 * @param z2 Z-coordinate of second vector operand.
	 *
	 * @return Dot product.
	 */
	static dot6( x1: number, y1: number, z1: number, x2: number, y2: number, z2: number ): number
	{
		return x1 * x2 + y1 * y2 + z1 * z2;
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param other Vector to compare with.
	 *
	 * @return true if the objects are almost equal; {@code false} if not.
	 *
	 * @see GeometryTools#almostEqual
	 */
	almostEquals( other: AnyVector3D ): boolean
	{
		return other === this ||
		       GeometryTools.almostEqual( this.x, other.x ) &&
		       GeometryTools.almostEqual( this.y, other.y ) &&
		       GeometryTools.almostEqual( this.z, other.z );
	}

	/**
	 * Returns whether the given object is a vector equal to this.
	 *
	 * @param other Object to compare with.
	 *
	 * @returns true if equal.
	 */
	equals( other: any ): boolean
	{
		return ( this === other ) || ( typeof other === 'object' ) && other && ( this.x === other.x ) && ( this.y === other.y ) && ( this.z === other.z );
	}

	/**
	 * Get inverse vector.
	 *
	 * @return Inverse vector.
	 */
	inverse(): Vector3D
	{
		return new Vector3D( -this.x, -this.y, -this.z );
	}

	/**
	 * Test whether this vector is a non-zero vector (its length is non-zero).
	 *
	 * This will return {@code false} when all components of this vector are
	 * zero or any component is NaN.
	 *
	 * @return {@code true} if this is a non-zero vector.
	 */
	isNonZero(): boolean
	{
		return ( ( this.x !== 0 ) || ( this.y !== 0 ) || ( this.z !== 0 ) ) && ( this.x === this.x ) && ( this.y === this.y ) && ( this.z === this.z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return Length of vector.
	 */
	length(): number
	{
		return Vector3D._length( this.x, this.y, this.z );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @param x X-component of vector.
	 * @param y Y-component of vector.
	 * @param z Z-component of vector.
	 *
	 * @return Length of vector.
	 */
	static _length( x: number, y: number, z: number ): number
	{
		return Math.sqrt( x * x + y * y + z * z );
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param other Vector to subtract from this vector.
	 *
	 * @return Resulting vector.
	 */
	minus( other: AnyVector3D ): Vector3D
	{
		return this.set( this.x - other.x, this.y - other.y, this.z - other.z );
	}

	/**
	 * Determine vector after scalar multiplication.
	 *
	 * @param factor Scale multiplication factor.
	 *
	 * @return Resulting vector.
	 */
	multiply( factor: number ): Vector3D
	{
		return this.set( this.x * factor, this.y * factor, this.z * factor );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1, it
	 * will be returned as-is.
	 *
	 * @return Normalized vector.
	 */
	normalize(): Vector3D
	{
		let l = this.length();
		return ( ( l === 0 ) || ( l === 1 ) ) ? this : this.set( this.x / l, this.y / l, this.z / l );
	}

	/**
	 * Add another vector to this vector.
	 *
	 * @param other Vector to add to this vector.
	 *
	 * @return Resulting vector.
	 */
	plus( other: AnyVector3D ): Vector3D;

	/**
	 * Add another vector to this vector.
	 *
	 * @param otherX X-coordinate of vector.
	 * @param otherY Y-coordinate of vector.
	 * @param otherZ Z-coordinate of vector.
	 *
	 * @return Resulting vector.
	 */
	plus( x: number, y: number, z: number ): Vector3D;

	/**
	 * Add another vector to this vector.
	 *
	 * @param other Vector to add to this vector.
	 *
	 * @return Resulting vector.
	 */
	plus( x: number | AnyVector3D, y?: number, z?: number ): Vector3D
	{
		return ( typeof x === 'object' ) ? this.set( this.x + x.x, this.y + x.y, this.z + x.z )
		                                 : this.set( this.x + x, this.y + y, this.z + z );
	}

	/**
	 * Set vector to the specified coordinates.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting vector.
	 */
	set( x: number, y: number, z: number ): Vector3D
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
	 * @return String representation of object.
	 */
	toString(): string
	{
		return this.x + "," + this.y + ',' + this.z;
	}

	/**
	 * Get string representation of object.
	 *
	 * @return String representation of object.
	 */
	toFriendlyString(): string
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
	 * @return Polar coordinates (radius,azimuth,zenith) based on cartesian
	 *         coordinates defined by this vector.
	 */
	cartesianToPolar(): Vector3D
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
	 * @param x Cartesian X coordinate.
	 * @param y Cartesian Y coordinate.
	 * @param z Cartesian Z coordinate.
	 *
	 * @return Polar coordinates (radius,azimuth,zenith) based on cartesian
	 *         coordinates defined by this vector.
	 */
	static cartesianToPolar( x: number, y: number, z: number ): Vector3D
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
	 * @return Cartesian coordinates based on polar coordinates
	 *         (radius,azimuth,zenith) defined by this vector.
	 */
	polarToCartesian(): Vector3D
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
	 * @param radius  Radius of sphere.
	 * @param azimuth Angle measured from the x-axis in the XY-plane (0 => point on XZ-plane).
	 * @param zenith  Angle measured from the z-axis toward the XY-plane (0 => point on Z-axis).
	 *
	 * @return Cartesian coordinates based on polar coordinates
	 *         (radius,azimuth,zenith) defined by this vector.
	 */
	static polarToCartesian( radius: number, azimuth: number, zenith: number ): Vector3D
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
