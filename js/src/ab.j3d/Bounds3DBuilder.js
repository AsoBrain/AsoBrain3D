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
import Bounds3D from './Bounds3D';
import Vector3D from './Vector3D';

/**
 * This class can be used to calculate a bounding box around a collection of
 * points.
 *
 * @author  Peter S. Heijnen
 */
export default class Bounds3DBuilder
{
	/**
	 * Number of points that were added.
	 * @type {number}
	 */
	_count;

	/**
	 * Minimum X value so far.
	 * @type {number}
	 */
	_minX;

	/**
	 * Minimum Y value so far.
	 * @type {number}
	 */
	_minY;

	/**
	 * Minimum Z value so far.
	 * @type {number}
	 */
	_minZ;

	/**
	 * Maximum X value so far.
	 * @type {number}
	 */
	_maxX;

	/**
	 * Maximum Y value so far.
	 * @type {number}
	 */
	_maxY;

	/**
	 * Maximum Z value so far.
	 * @type {number}
	 */
	_maxZ;

	/**
	 * Summarized X values.
	 * @type {number}
	 */
	_sumX;

	/**
	 * Summarized Y values.
	 * @type {number}
	 */
	_sumY;

	/**
	 * Summarized Z values.
	 * @type {number}
	 */
	_sumZ;

	/**
	 * Cached average point.
	 * @type {Vector3D}
	 */
	_average;

	/**
	 * Cached bounds.
	 * @type {Bounds3D}
	 */
	_bounds;

	/**
	 * Construct new {@link Bounds3D} builder.
	 */
	constructor()
	{
		const min = Number.POSITIVE_INFINITY;
		const max = Number.NEGATIVE_INFINITY;

		this._active   = 0;
		this._minX   = min;
		this._minY   = min;
		this._minZ   = min;
		this._maxX   = max;
		this._maxY   = max;
		this._maxZ   = max;
		this._sumX    = 0.0;
		this._sumY    = 0.0;
		this._sumZ    = 0.0;
		this._average = Vector3D.ZERO;
		this._bounds  = Bounds3D.EMPTY;
	}

	/**
	 * Add 3D bounds to the bounding box.
	 *
	 * @param {Matrix3D} [transform] Transformation to apply to bounds.
	 * @param {number|Vector3D|Bounds3D} x1 First X coordinate of bounds, or the first coordinate as a vector, or the bounds.
	 * @param {number|Vector3D} [y1] First Y coordinate of bounds, or the second coordinate as a vector.
	 * @param {number} [z1] First Z coordinate of bounds.
	 * @param {number} [x2] Second X coordinate of bounds.
	 * @param {number} [y2] Second Y coordinate of bounds.
	 * @param {number} [z2] Second Z coordinate of bounds.
	 */
	addBounds( transform, x1, y1, z1, x2, y2, z2 )
	{
		if ( arguments.length == 1 )
		{
			const bounds = arguments[ 0 ];
			this.addPoint( bounds.v1 );
			this.addPoint( bounds.v2 );
		}
		else if ( arguments.length == 6 )
		{
			this.addPoint( arguments[ 0 ], arguments[ 1 ], arguments[ 2 ] );
			this.addPoint( arguments[ 3 ], arguments[ 4 ], arguments[ 5 ] );
		}
		else
		{
			if ( arguments.length == 2 )
			{
				const bounds = arguments[ 1 ];
				x1 = bounds.v1.x;
				y1 = bounds.v1.y;
				z1 = bounds.v1.z;
				x2 = bounds.v2.x;
				y2 = bounds.v2.y;
				z2 = bounds.v2.z;
			}
			else if ( arguments.length != 7 )
			{
				throw new TypeError( "invalid number of arguments: " + arguments );
			}

			this.addPoint( transform, x1, y1, z1 );
			this.addPoint( transform, x1, y1, z2 );
			this.addPoint( transform, x1, y2, z1 );
			this.addPoint( transform, x1, y2, z2 );
			this.addPoint( transform, x2, y1, z1 );
			this.addPoint( transform, x2, y1, z2 );
			this.addPoint( transform, x2, y2, z1 );
			this.addPoint( transform, x2, y2, z2 );
		}
	}

	/**
	 * Add transformed point to the bounding box.
	 *
	 * @param {Matrix3D} [transform] Transformation to apply to point.
	 * @param {number|Vector3D} x X coordinate of point, or the point as a vector.
	 * @param {number} [y] Y coordinate of point.
	 * @param {number} [z] Z coordinate of point.
	 */
	addPoint( transform, x, y, z )
	{
		if ( arguments.length == 1 )
		{
			const point = arguments[ 0 ];
			x = point.x;
			y = point.y;
			z = point.z;
		}
		else if ( arguments.length == 3 )
		{
			x = arguments[ 0 ];
			y = arguments[ 1 ];
			z = arguments[ 2 ];
		}
		else
		{
			if ( arguments.length == 2 )
			{
				const point = arguments[ 1 ];
				x = point.x;
				y = point.y;
				z = point.z;
			}
			else if ( arguments.length != 4 )
			{
				throw new TypeError( "invalid number of arguments: " + arguments );
			}

			const _x = transform.transformX( x, y, z );
			const _y = transform.transformY( x, y, z );
			const _z = transform.transformZ( x, y, z );

			x = _x;
			y = _y;
			z = _z;
		}

		this._active++;

		if ( x < this._minX )
		{
			this._minX = x;
		}

		if ( y < this._minY )
		{
			this._minY = y;
		}

		if ( z < this._minZ )
		{
			this._minZ = z;
		}

		if ( x > this._maxX )
		{
			this._maxX = x;
		}

		if ( y > this._maxY )
		{
			this._maxY = y;
		}

		if ( z > this._maxZ )
		{
			this._maxZ = z;
		}

		this._sumX += x;
		this._sumY += y;
		this._sumZ += z;
	}

	/**
	 * Get average point from this builder. If no points were added, this method
	 * returns {@link Vector3D#ZERO}.
	 *
	 * @return {Vector3D} Average point.
	 */
	getAveragePoint()
	{
		let result;

		const count = this._active;
		if ( count > 0 )
		{
			result = this._average.set( this._sumX / count , this._sumY / count, this._sumZ / count );
		}
		else
		{
			result = Vector3D.ZERO;
		}

		this._average = result;

		return result;
	}

	/**
	 * Get center point from this builder. If no points were added, this method
	 * returns {@link Vector3D#ZERO}.
	 *
	 * @return {Vector3D} Center point.
	 */
	getCenterPoint()
	{
		return ( this._active > 0 ) ? new Vector3D( 0.5 * ( this._minX + this._maxX ), 0.5 * ( this._minY + this._maxY ), 0.5 * ( this._minZ + this._maxZ ) ) : Vector3D.ZERO;
	}

	/**
	 * Get resulting bounding box from this builder. These bounds are always
	 * {@link Bounds3D#sorted() sorted}. If no initial bounds were specified and
	 * no points were added to this builder, then this method will return
	 * {@code null} to indicate that no bounding box could be calculated.
	 *
	 * @return {Bounds3D} Bounding box as {@link Bounds3D} instance; {@code null} if no bounding box could be determined.
	 */
	getBounds()
	{
		let result;

		if ( this._active > 0 )
		{
			result = this._bounds;
			result = result.set( result.v1.set( this._minX , this._minY , this._minZ ) , result.v2.set( this._maxX , this._maxY , this._maxZ ) );
			this._bounds = result;
		}
		else
		{
			result = null;
		}

		return result;
	}
}
