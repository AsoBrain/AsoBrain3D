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
import Vector3D from '../ab.j3d/Vector3D.js';
import MathTools from '../ab.j3d/MathTools.js';

/**
 * 3D linear function of the form f(x) = ax + b.
 *
 * @author Gerrit Meinders
 */
export default class LinearFunction3D
{
	/**
	 * Gradient a in f(x) = ax + b.
	 */
	_gradient: Vector3D;

	/**
	 * Constant b in f(x) = ax + b.
	 */
	_constant: Vector3D;

	/**
	 * Finds a specific linear function f(x) = ax + b based on the given points
	 * (x, f(x)), by solving for a and b. If the parameters are equal along a
	 * given axis, it follows that both values must also be equal (otherwise,
	 * the result is not a function); in this case the function will pass
	 * through the origin and the given point along that axis. If the parameters
	 * are not equal, but the values are equal along a given axis, the result
	 * vector will be constant along that axis and equal to the corresponding
	 * component of {@code valueA}.
	 *
	 * @param parameterA First value of x.
	 * @param valueA     First value of f(x).
	 * @param parameterB Second value of x.
	 * @param valueB     Second value of f(x).
	 *
	 * @return Specified linear function.
	 */
	static fromPoints( parameterA: Vector3D, valueA: Vector3D, parameterB: Vector3D, valueB: Vector3D ): LinearFunction3D
	{
		const epsilon = 1.0e-8;

		let gradientX;
		let constantX;

		if ( MathTools.almostEqual( valueA.x, valueB.x, epsilon ) )
		{
			gradientX = 0.0;
			constantX = valueA.x;
		}
		else
		{
			if ( MathTools.almostEqual( parameterA.x, parameterB.x, epsilon ) )
			{
				throw new TypeError( `Can't determine X-gradient, because X-parameters are the same, but X values are not (parameterA=${parameterA}, valueA=${valueA}, parameterB=${parameterB}, valueB=${valueB}` );
			}

			gradientX = ( valueB.x - valueA.x ) / ( parameterB.x - parameterA.x );
			constantX = valueA.x - gradientX * parameterA.x;
		}

		let gradientY;
		let constantY;

		if ( MathTools.almostEqual( valueA.y, valueB.y, epsilon ) )
		{
			gradientY = 0.0;
			constantY = valueA.y;
		}
		else
		{
			if ( MathTools.almostEqual( parameterA.y, parameterB.y, epsilon ) )
			{
				throw new TypeError( `Can't determine Y-gradient, because Y-parameters are the same, but Y values are not (parameterA=${parameterA}, valueA=${valueA}, parameterB=${parameterB}, valueB=${valueB}` );
			}

			gradientY = ( valueB.y - valueA.y ) / ( parameterB.y - parameterA.y );
			constantY = valueA.y - gradientY * parameterA.y;
		}

		let gradientZ;
		let constantZ;

		if ( MathTools.almostEqual( valueA.z, valueB.z, epsilon ) )
		{
			gradientZ = 0.0;
			constantZ = valueA.z;
		}
		else
		{
			if ( MathTools.almostEqual( parameterA.z, parameterB.z, epsilon ) )
			{
				throw new TypeError( `Can't determine Z-gradient, because Z-parameters are the same, but Z values are not (parameterA=${parameterA}, valueA=${valueA}, parameterB=${parameterB}, valueB=${valueB}` );
			}

			gradientZ = ( valueB.z - valueA.z ) / ( parameterB.z - parameterA.z );
			constantZ = valueA.z - gradientZ * parameterA.z;
		}

		return new LinearFunction3D( new Vector3D( gradientX, gradientY, gradientZ ), new Vector3D( constantX, constantY, constantZ ) );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param gradient Gradient a in f(x) = ax + b.
	 * @param constant Constant b in f(x) = ax + b.
	 */
	constructor( gradient: Vector3D, constant: Vector3D )
	{
		this._constant = constant;
		this._gradient = gradient;
	}

	/**
	 * Returns the value of the function for the given parameter.
	 *
	 * @param parameter Parameter x in f(x) = ax + b.
	 *
	 * @return Value of the function.
	 */
	get( parameter: Vector3D ): Vector3D
	{
		const gradient = this._gradient;
		const constant = this._constant;
		return new Vector3D( gradient.x * parameter.x + constant.x,
		                     gradient.y * parameter.y + constant.y,
		                     gradient.z * parameter.z + constant.z );
	}

	toString(): string
	{
		return `[f(v) = ${this._gradient} * v + ${this._constant}]`;
	}

	/**
	 * Create human-readable representation of this object. This is especially
	 * useful for debugging purposes.
	 *
	 * @return Human-readable representation of this object.
	 */
	toFriendlyString(): string
	{
		return `[ f(v) = ${this._gradient.toFriendlyString()} * v + ${this._constant.toFriendlyString()} ]`;
	}

	/**
	 * Returns the sum of this function and the given function.
	 *
	 * @param other Linear function.
	 *
	 * @return Sum of the two functions.
	 */
	plus( other: LinearFunction3D ): LinearFunction3D
	{
		return new LinearFunction3D( this._gradient.plus( other._gradient ), this._constant.plus( other._constant ) );
	}

	/**
	 * Returns the result of subtracting the given function from this function.
	 *
	 * @param other Linear function.
	 *
	 * @return (Signed) difference of the two functions.
	 */
	minus( other: LinearFunction3D ): LinearFunction3D
	{
		return new LinearFunction3D( this._gradient.minus( other._gradient ), this._constant.minus( other._constant ) );
	}
}
