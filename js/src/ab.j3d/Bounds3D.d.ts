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

/**
 * This class represents rectangular 3D bounds (specified by two vectors).
 *
 * @author Peter S. Heijnen
 */
export default class Bounds3D
{
    /**
     * Empty box defined by zero-vectors.
     * @type Bounds3D
     */
    static EMPTY: Bounds3D;

    /**
     * Calculate intersection between two bounding boxes. Note that the result
     * will have one or more negative factors for v2 - v1 when the bounding
     * boxes are disjunct.
     *
     * @param   {Bounds3D} bounds1     First object for intersection.
     * @param   {Bounds3D} bounds2     Seconds object for intersection.
     *
     * @return  {Bounds3D} Bounds of intersection.
     */
    static intersect( bounds1: Bounds3D, bounds2: Bounds3D ): Bounds3D;

    /**
     * Determine whether the two specified bounding boxes intersect. This method
     * does not return <code>true</code> if the intersection along any axis is
     * less than the specified <code>epsilon</code> value.
     *
     * @param {Bounds3D} bounds1 First object for intersection test.
     * @param {Bounds3D} bounds2 Seconds object for intersection test.
     * @param {number} [epsilon] Tolerance (always a positive number).
     *
     * @return {boolean} <code>true</code> if the bounds intersect;
     *          <code>false</code> if the bounds are disjunct.
     */
    static intersects( bounds1: Bounds3D, bounds2: Bounds3D, epsilon?: number ): boolean;

    /**
     * Construct new box from the specified coordinates, and try to reuse
     * existing boxes.
     *
     * @param {Bounds3D} box1 Reusable box object.
     * @param {Bounds3D} box2 Reusable box object.
     * @param {Vector3D} v1 First vector of bounds to set.
     * @param {Vector3D} v2 Second vector of bounds to set.
     *
     * @return {Bounds3D} Bounds3D object based on the desired coordinates.
     */
    static rebuild( box1: Bounds3D, box2: Bounds3D, v1: Vector3D, v2: Vector3D ): Bounds3D;

    /**
     * Create a new box.
     *
     * @param {Vector3D} [v1] First vector of box.
     * @param {Vector3D} [v2] Second vector of box.
     */
    constructor( v1?: Vector3D, v2?: Vector3D );

    /**
     * First vector component of box. Normally the minimum vector.
     * @type Vector3D
     */
    v1: Vector3D;
    /**
     * Second vector component of box. Normally the maximum vector.
     * @type Vector3D
     */
    v2: Vector3D;

    /**
     * Get center point of these bounds.
     *
     * @return {Vector3D} Vector describing boundary center (average of coordinates).
     */
    center(): Vector3D;

    /**
     * Get center X.
     *
     * @return {number} Center X (avaerage of X coordinate of vector 1 and vector 2).
     */
    centerX(): number;

    /**
     * Get center Y.
     *
     * @return {number} Center Y (avaerage of Y coordinate of vector 1 and vector 2).
     */
    centerY(): number;

    /**
     * Get center Z.
     *
     * @return {number} Center Z (avaerage of Z coordinate of vector 1 and vector 2).
     */
    centerZ(): number;

    /**
     * Get delta X.
     *
     * @return {number} Delta X (X coordinate of vector 1 substracted from vector 2).
     */
    deltaX(): number;

    /**
     * Get delta Y.
     *
     * @return {number} Delta Y (Y coordinate of vector 1 substracted from vector 2).
     */
    deltaY(): number;

    /**
     * Get delta Z.
     *
     * @return {number} Delta Z (Z coordinate of vector 1 substracted from vector 2).
     */
    deltaZ(): number;

    /**
     * Get volume contained within these bounds.
     *
     * @return {number} Volume contained within these bounds.
     */
    volume(): number;

    /**
     * Test whether these bounds are empty. This returns true if these bounds
     * describe a zero-volume, meaning that any of the coordinates is equal for
     * both vectors.
     *
     * @return {boolean} <code>true</code> if the bounds are empty (zero volume);
     *          <code>false</code> if the bounds are not empty (non-zero volume).
     */
    isEmpty(): boolean;

    /**
     * Test whether these bounds are sorted. This means that each coordinate of
     * the first vector is lesser or equal to the same coordinate of the second
     * vector.
     *
     * @return {boolean} <code>true</code> if the bounds are sorted;
     *          <code>false</code> if the bounds are not sorted.
     */
    isSorted(): boolean;

    /**
     * Test if this bounds contains the specified point.
     *
     * @param {Vector3D} point Point to test.
     *
     * @return {boolean} <code>true</code> if this bounds contains the specified point;
     *          <code>false</code> if the point is outside these bounds.
     */
    contains( point: Vector3D ): boolean;

    /**
     * Compare these bounds to the specified bounds.
     *
     * @param {Bounds3D} other Bounds to compare with.
     *
     * @return {boolean} <code>true</code> if the bounds are equal, <code>false</code> if not.
     */
    equals( other: Bounds3D ): boolean;

    /**
     * Calculate joined bounds of this and the given bounds or vector.
     *
     * @param  {Bounds3D|Vector3D} other Bounds or vector to join with.
     *
     * @return {Bounds3D} Joined bounds.
     */
    join( other: Bounds3D | Vector3D ): Bounds3D;

    /**
     * Determine maximum vector of box.
     *
     * @return {Vector3D} Resulting vector.
     */
    max(): Vector3D;

    /**
     * Determine maximum X coordinate of bounds.
     *
     * @return {number} Maximum X coordinate of bounds.
     */
    maxX(): number;

    /**
     * Determine maximum Y coordinate of bounds.
     *
     * @return {number} Maximum Y coordinate of bounds.
     */
    maxY(): number;

    /**
     * Determine maximum Z coordinate of bounds.
     *
     * @return {number} Maximum Z coordinate of bounds.
     */
    maxZ(): number;

    /**
     * Determine minimum vector of bounds.
     *
     * @return {Vector3D} Resulting vector.
     */
    min(): Vector3D;

    /**
     * Determine minimum X coordinate of bounds.
     *
     * @return {number} Minimum X coordinate of bounds.
     */
    minX(): number;

    /**
     * Determine minimum Y coordinate of bounds.
     *
     * @return {number} Minimum Y coordinate of bounds.
     */
    minY(): number;

    /**
     * Determine minimum Z coordinate of bounds.
     *
     * @return {number} Minimum Z coordinate of bounds.
     */
    minZ(): number;

    /**
     * Subtract vector from bounds.
     *
     * @param {Vector3D} vector  Vector to subtract from bounds.
     *
     * @return {Bounds3D} Resulting bounds.
     */
    minus( vector: Vector3D ): Bounds3D;

    /**
     * Determine box after scalar multiplication.
     *
     * @param {number} factor Scale multiplication factor.
     *
     * @return {Bounds3D} Resulting box.
     */
    multiply( factor: number ): Bounds3D;

    /**
     * Add a vector to bounds.
     *
     * @param {Vector3D} vector Vector to add to bounds.
     *
     * @return {Bounds3D} Resulting bounds.
     */
    plus( vector: Vector3D ): Bounds3D;

    /**
     * Set bounds to the specified vectors.
     *
     * @param {Vector3D} [newV1] First vector of bounds to set.
     * @param {Vector3D} [newV2] Second vector of bounds to set.
     *
     * @return {Bounds3D} Resulting bounds.
     */
    set( newV1?: Vector3D, newV2?: Vector3D ): Bounds3D;

    /**
     * Get size of these bounds.
     *
     * @return {Vector3D} Vector describing bound size (v2-v1).
     */
    size(): Vector3D;

    /**
     * Get size along X axis.
     *
     * @return {number} Size along X (distance between X coordinates of vector 1 and 2).
     */
    sizeX(): number;

    /**
     * Get size along Y axis.
     *
     * @return {number} Size along Y (distance between Y coordinates of vector 1 and 2).
     */
    sizeY(): number;

    /**
     * Get size along Z axis.
     *
     * @return {number} Size along Z (distance between Z coordinates of vector 1 and 2).
     */
    sizeZ(): number;

    /**
     * Determine sorted bounds. If bounds are sorted, than the x/y/z
     * components of {@link #v1} are always less or equal to the
     * matching components of {@link #v2}.
     *
     * @return {Bounds3D} Resulting bounds.
     */
    sorted(): Bounds3D;

    /**
     * Converts the bounds from an oriented bounding box (OBB) to an (world)
     * axis-aligned bounding box (AABB).
     *
     * @param {!Matrix3D} box2world Transforms box to world coordinates.
     *
     * @return {!Bounds3D} Axis-aligned bounding box.
     */
    convertObbToAabb( box2world: Matrix3D ): Bounds3D;
}
