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

/**
 * This class provides tools for various math problems.
 *
 * @author Gerrit Meinders
 * @author Peter S. Heijnen
 */
export default class MathTools
{
    /**
     * Test if the specified values are 'almost' equal (the difference between
     * them approaches the value 0).
     *
     * @param {number} value1 First value to compare.
     * @param {number} value2 Second value to compare.
     * @param {number} [epsilon] Tolerance (always a positive number).
     *
     * @return {boolean} {@code true} if the values are within a +/- 0.001 tolerance of each other; {@code false} otherwise.
     */
    static almostEqual( value1: number, value2: number, epsilon?: number ): boolean;

    /**
     * Test if the first operand is less than the second operand or almost
     * equal (the difference between them approaches the value 0).
     *
     * @param {number} value1 First value to compare.
     * @param {number} value2 Second value to compare.
     * @param {number} [epsilon] Tolerance (always a positive number).
     *
     * @return {boolean} <code>true</code> is {@code value1} is less than or within a +/- 0.001 tolerance of {@code value2}; {@code false} otherwise.
     */
    static lessOrAlmostEqual( value1: number, value2: number, epsilon?: number ): boolean;

    /**
     * Test if the first operand is greater than the second operand or almost
     * equal (the difference between them approaches the value 0).
     *
     * @param {number} value1 First value to compare.
     * @param {number} value2 Second value to compare.
     * @param {number} [epsilon] Tolerance (always a positive number).
     *
     * @return {boolean} <code>true</code> is {@code value1} is greater than or within the specified tolerance of {@code value2}; {@code false} otherwise.
     */
    static greaterOrAlmostEqual( value1: number, value2: number, epsilon?: number ): boolean;

    /**
     * Test if the first operand is significantly less than the second operand
     * (the difference between them exceeds the specified tolerance).
     *
     * @param {number} value1 First value to compare.
     * @param {number} value2 Second value to compare.
     * @param {number} [epsilon] Tolerance (always a positive number).
     *
     * @return {boolean} <code>true</code> is {@code value1} is at least the specified tolerance less than {@code value2}; {@code false} otherwise.
     */
    static significantlyLessThan( value1: number, value2: number, epsilon?: number ): boolean;

    /**
     * Test if the first operand is significantly greater than the second operand
     * (the difference between them exceeds the specified tolerance).
     *
     * @param {number} value1 First value to compare.
     * @param {number} value2 Second value to compare.
     * @param {number} [epsilon] Tolerance (always a positive number).
     *
     * @return {boolean} <code>true</code> is {@code value1} is at least the specified tolerance greater than {@code value2}; {@code false} otherwise.
     */
    static significantlyGreaterThan( value1: number, value2: number, epsilon?: number ): boolean;
}
