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

import Matrix3D from './Matrix3D';
import Vector3D from "./Vector3D";
import Bounds3D from "./Bounds3D";

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
    _count: number;

    /**
     * Minimum X value so far.
     * @type {number}
     */
    _minX: number;

    /**
     * Minimum Y value so far.
     * @type {number}
     */
    _minY: number;

    /**
     * Minimum Z value so far.
     * @type {number}
     */
    _minZ: number;

    /**
     * Maximum X value so far.
     * @type {number}
     */
    _maxX: number;

    /**
     * Maximum Y value so far.
     * @type {number}
     */
    _maxY: number;

    /**
     * Maximum Z value so far.
     * @type {number}
     */
    _maxZ: number;

    /**
     * Summarized X values.
     * @type {number}
     */
    _sumX: number;

    /**
     * Summarized Y values.
     * @type {number}
     */
    _sumY: number;

    /**
     * Summarized Z values.
     * @type {number}
     */
    _sumZ: number;

    /**
     * Cached average point.
     * @type {Vector3D}
     */
    _average: Vector3D;

    /**
     * Cached bounds.
     * @type {Bounds3D}
     */
    _bounds: Bounds3D;

    _active: number;

    /**
     * Add 3D bounds to the bounding box.
     *
     * @param {Bounds3D} bounds Bounds to add.
     */
    addBounds( bounds: Bounds3D ): void;

    /**
     * Add 3D bounds to the bounding box.
     *
     * @param {number} x1 First X coordinate of bounds.
     * @param {number} y1 First Y coordinate of bounds.
     * @param {number} z1 First Z coordinate of bounds.
     * @param {number} x2 Second X coordinate of bounds.
     * @param {number} y2 Second Y coordinate of bounds.
     * @param {number} z2 Second Z coordinate of bounds.
     */
    addBounds( x1: number, y1: number, z1: number, x2: number, y2: number, z2: number ): void;

    /**
     * Add 3D bounds to the bounding box.
     *
     * @param {Matrix3D} [transform] Transformation to apply to bounds.
     * @param {Bounds3D} bounds Bounds to add.
     */
    addBounds( transform: Matrix3D, bounds: Bounds3D ): void;

    /**
     * Add 3D bounds to the bounding box.
     *
     * @param {Matrix3D} [transform] Transformation to apply to bounds.
     * @param {number} x1 First X coordinate of bounds.
     * @param {number} y1 First Y coordinate of bounds.
     * @param {number} z1 First Z coordinate of bounds.
     * @param {number} x2 Second X coordinate of bounds.
     * @param {number} y2 Second Y coordinate of bounds.
     * @param {number} z2 Second Z coordinate of bounds.
     */
    addBounds( transform: Matrix3D, x1: number, y1: number, z1: number, x2: number, y2: number, z2: number ): void;

    /**
     * Add point to the bounding box.
     *
     * @param {Vector3D} point Point to add.
     */
    addPoint( point: Vector3D ): void;

    /**
     * Add point to the bounding box.
     *
     * @param {number} x X coordinate of point.
     * @param {number} y Y coordinate of point.
     * @param {number} z Z coordinate of point.
     */
    addPoint( x: number, y: number, z: number ): void;

    /**
     * Add point to the bounding box.
     *
     * @param {Matrix3D} transform Transformation to apply to point.
     * @param {Vector3D} point Point to add.
     */
    addPoint( transform: Matrix3D, point: Vector3D ): void;

    /**
     * Add point to the bounding box.
     *
     * @param {Matrix3D} transform Transformation to apply to point.
     * @param {number} x X coordinate of point.
     * @param {number} y Y coordinate of point.
     * @param {number} z Z coordinate of point.
     */
    addPoint( transform: Matrix3D, x: number, y: number, z: number ): void;

    /**
     * Get average point from this builder. If no points were added, this method
     * returns {@link Vector3D#ZERO}.
     *
     * @return {Vector3D} Average point.
     */
    getAveragePoint(): Vector3D;

    /**
     * Get center point from this builder. If no points were added, this method
     * returns {@link Vector3D#ZERO}.
     *
     * @return {Vector3D} Center point.
     */
    getCenterPoint(): Vector3D;

    /**
     * Get resulting bounding box from this builder. These bounds are always
     * {@link Bounds3D#sorted() sorted}. If no initial bounds were specified and
     * no points were added to this builder, then this method will return
     * {@code null} to indicate that no bounding box could be calculated.
     *
     * @return {Bounds3D} Bounding box as {@link Bounds3D} instance; {@code null} if no bounding box could be determined.
     */
    getBounds(): Bounds3D;
}
