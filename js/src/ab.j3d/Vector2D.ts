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

export interface AnyVector2D
{
	readonly x: number;
	readonly y: number;
}

/**
 * This class represents a 2D vector.
 *
 * @author  Peter S. Heijnen
 */
export default class Vector2D implements AnyVector2D
{
	/**
	 * Zero-vector.
	 */
	static readonly ZERO = new Vector2D( 0.0, 0.0 );

	/**
	 * Positive X-axis direction vector.
	 */
	static readonly POSITIVE_X_AXIS = new Vector2D( 1.0, 0.0 );

	/**
	 * Negative X-axis direction vector.
	 */
	static readonly NEGATIVE_X_AXIS = new Vector2D( -1.0, 0.0 );

	/**
	 * Positive Y-axis direction vector.
	 */
	static readonly POSITIVE_Y_AXIS = new Vector2D( 0.0, 1.0 );

	/**
	 * Negative Y-axis direction vector.
	 */
	static readonly NEGATIVE_Y_AXIS = new Vector2D( 0.0, -1.0 );

	/**
	 * X component of 2D vector.
	 */
	readonly x: number;

	/**
	 * Y component of 2D vector.
	 */
	readonly y: number;

	/**
	 * Construct new vector.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 */
	constructor( x: number, y: number )
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Determine Z component of 3D cross vector between two 2D vectors.
	 *
	 * @param v1 First vector.
	 * @param v2 Second vector.
	 *
	 * @return Resulting vector.
	 */
	static crossZ( v1: AnyVector2D, v2: AnyVector2D ): number;

	/**
	 * Determine Z component of 3D cross vector between two 2D vectors.
	 *
	 * @param x1 X-coordinate of first vector operand.
	 * @param y1 Y-coordinate of first vector operand.
	 * @param x2 X-coordinate of second vector operand.
	 * @param y2 Y-coordinate of second vector operand.
	 *
	 * @return Resulting vector.
	 */
	static crossZ( x1: number, y1: number, x2: number, y2: number ): number;

	static crossZ( x1: any, y1: any, x2?: number, y2?: number ): number
	{
		if ( arguments.length === 2 )
		{
			y2 = y1.y;
			x2 = y1.x;
			y1 = x1.y;
			x1 = x1.x;
		}

		return x1 * y2 - y1 * x2;
	}

	/**
	 * Get direction from one point to another point.
	 *
	 * @param from Point vector for from-point.
	 * @param to   Point vector for to-point.
	 *
	 * @return Direction from from-point to to-point.
	 */
	static direction( from: AnyVector2D, to: AnyVector2D ): Vector2D;

	/**
	 * Get direction from one point to another point.
	 *
	 * @param x1 X coordinate of from-point.
	 * @param y1 Y coordinate of from-point.
	 * @param x2 X coordinate of to-point.
	 * @param y2 Y coordinate of to-point.
	 *
	 * @return Direction from from-point to to-point.
	 */
	static direction( x1: number, y1: number, x2: number, y2: number ): Vector2D;

	static direction( x1: any, y1: any, x2?: number, y2?: number ): Vector2D
	{
		if ( arguments.length === 2 )
		{
			y2 = y1.y;
			x2 = y1.x;
			y1 = x1.y;
			x1 = x1.x;
		}

		return Vector2D.normalize( x2 - x1, y2 - y1 );
	}

	/**
	 * Calculate dot product (a.k.a. inner product) of this vector and another
	 * one specified as argument.
	 *
	 * <p>
	 * The dot product is related to the cosine function by the equation
	 * <blockquote>a &middot; b = |a| |b| cos &theta;</blockquote>
	 * where &theta; denotes the angle between the two vectors.
	 *
	 * @param v1 First vector operand.
	 * @param v2 Second vector operand.
	 *
	 * @return Dot product.
	 */
	static dot( v1: AnyVector2D, v2: AnyVector2D ): number
	{
		return v1.x * v2.x + v1.y * v2.y;
	}

	/**
	 * Compare this vector to another vector.
	 *
	 * @param otherX X-coordinate of vector to compare with.
	 * @param otherY Y-coordinate of vector to compare with.
	 *
	 * @return {@code true} if vectors are equal;
	 * {@code false} if not.
	 */
	equals( otherX: number, otherY: number ): boolean;

	equals( other: any ): boolean;

	equals( otherX: any, otherY?: any ): boolean
	{
		if ( otherY !== undefined )
		{
			return ( otherX === this.x ) && ( otherY === this.y );
		}
		else
		{
			return ( otherX == this ) || ( ( otherX instanceof Vector2D ) && ( this.x === otherX.x ) && ( this.y === otherX.y ) );
		}
	}

	/**
	 * Calculate length of vector.
	 *
	 * @return Length of vector.
	 */
	length(): number
	{
		return Vector2D._length( this.x, this.y );
	}

	/**
	 * Calculate length of vector.
	 *
	 * @param x X-component of vector.
	 * @param y Y-component of vector.
	 *
	 * @return Length of vector.
	 */
	static _length( x: number, y: number ): number
	{
		return Math.sqrt( x * x + y * y );
	}

	/**
	 * Normalize this vector (make length 1). If the vector has length 0 or 1,
	 * it will be returned as-is.
	 *
	 * @return Normalized vector.
	 */
	normalize(): Vector2D
	{
		const l = this.length();
		return ( ( l == 0.0 ) || ( l == 1.0 ) ) ? this : new Vector2D( this.x / l, this.y / l );
	}

	/**
	 * Normalize the specified vector. If the vector has length 0 or 1, a
	 * 0-vector will be returned.
	 *
	 * @param x X-component of vector.
	 * @param y Y-component of vector.
	 *
	 * @return Normalized vector.
	 */
	static normalize( x: number, y: number ): Vector2D
	{
		const l = Vector2D._length( x, y );
		return ( l == 0.0 ) ? Vector2D.ZERO : new Vector2D( x / l, y / l );
	}
}
