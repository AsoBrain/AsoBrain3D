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

import Bounds3D from '../ab.j3d/Bounds3D';
import Matrix3D from '../ab.j3d/Matrix3D';

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
    static almostEqual( value1: number, value2: number ): boolean;

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
    static greaterOrAlmostEqual( value1: number, value2: number ): boolean;

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
    static lessOrAlmostEqual( value1: number, value2: number ): boolean;

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
    static significantlyGreaterThan( value1: number, value2: number ): boolean;

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
    static significantlyLessThan( value1: number, value2: number ): boolean;

    /**
     * Test oriented bounding box intersection.
     *
     * Borrowed code from <A href='http://channel9.msdn.com/ShowPost.aspx?PostID=276041'>XNA
     * Oriented Bounding Box Intersection Test</A>, which was based on <A
     * href='http://www.cs.unc.edu/~geom/theses/gottschalk/main.pdf'>Collision
     * Queries using Oriented Boxes</A> by Stefan Gottschalk.
     *
     * @param {Bounds3D} box1 Oriented bounding box #1.
     * @param {Matrix3D} from2to1 Transformation from box #2 to box #1.
     * @param {Bounds3D} box2 Oriented bounding box #2.
     *
     * @return {boolean} {@code true} if the bounding boxes intersect; {@code false}
     * otherwise.
     */
    static testOrientedBoundingBoxIntersection( box1: Bounds3D, from2to1: Matrix3D, box2: Bounds3D ): boolean;
}
