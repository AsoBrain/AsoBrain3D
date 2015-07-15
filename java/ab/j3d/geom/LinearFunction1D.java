/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2015 Peter S. Heijnen
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
package ab.j3d.geom;

import ab.j3d.*;

/**
 * 3D linear function of the form f(x) = ax + b.
 *
 * @author G. Meinders
 */
public class LinearFunction1D
{
	/**
	 * Gradient a in f(x) = ax + b.
	 */
	private double _gradient;

	/**
	 * Constant b in f(x) = ax + b.
	 */
	private double _constant;

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
	public static LinearFunction1D fromPoints( final double parameterA, final double valueA, final double parameterB, final double valueB )
	{
		final double epsilon = 1.0e-8;

		final double gradient;
		final double constant;

		if ( MathTools.almostEqual( valueA, valueB, epsilon ) )
		{
			gradient = 0.0;
			constant = valueA;
		}
		else
		{
			if ( MathTools.almostEqual( parameterA, parameterB, epsilon ) )
			{
				throw new IllegalArgumentException( "Can't determine gradient, because parameters are the same, but values are not (parameterA=" + parameterA + ", valueA=" + valueA + ", parameterB=" + parameterB + ", valueB=" + valueB );
			}

			gradient = ( valueB - valueA ) / ( parameterB - parameterA );
			constant = valueA - gradient * parameterA;
		}

		return new LinearFunction1D( gradient, constant );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param gradient Gradient a in f(x) = ax + b.
	 * @param constant Constant b in f(x) = ax + b.
	 */
	public LinearFunction1D( final double gradient, final double constant )
	{
		_constant = constant;
		_gradient = gradient;
	}

	/**
	 * Returns the gradient, i.e. the gradient a in f(x) = ax + b.
	 *
	 * @return Gradient.
	 */
	public double getGradient()
	{
		return _gradient;
	}

	/**
	 * Sets the gradient, i.e. the gradient a in f(x) = ax + b.
	 *
	 * @param gradient Gradient.
	 */
	public void setGradient( final double gradient )
	{
		_gradient = gradient;
	}

	/**
	 * Returns the constant, i.e. the constant b in f(x) = ax + b.
	 *
	 * @return Constant.
	 */
	public double getConstant()
	{
		return _constant;
	}

	/**
	 * Sets the constant, i.e. the constant b in f(x) = ax + b.
	 *
	 * @param constant Constant.
	 */
	public void setConstant( final double constant )
	{
		_constant = constant;
	}

	/**
	 * Returns the value of the function for the given parameter.
	 *
	 * @param parameter Parameter x in f(x) = ax + b.
	 *
	 * @return Value of the function.
	 */
	public double get( final double parameter )
	{
		return _gradient * parameter + _constant;
	}

	@Override
	public String toString()
	{
		return super.toString() + "[f(v) = " + _gradient + " * v + " + _constant + ']';
	}

	/**
	 * Create human-readable representation of this object. This is especially
	 * useful for debugging purposes.
	 *
	 * @return Human-readable representation of this object.
	 */
	public String toFriendlyString()
	{
		final Class<?> clazz = getClass();
		return clazz.getSimpleName() + "[ f(v) = " + _gradient + " * v + " + _constant + " ]";
	}

	/**
	 * Returns the sum of this function and the given function.
	 *
	 * @param other Linear function.
	 *
	 * @return Sum of the two functions.
	 */
	public LinearFunction1D plus( final LinearFunction1D other )
	{
		return new LinearFunction1D( getGradient() + other.getGradient(), getConstant() + other.getConstant() );
	}

	/**
	 * Returns the result of subtracting the given function from this function.
	 *
	 * @param other Linear function.
	 *
	 * @return (Signed) difference of the two functions.
	 */
	public LinearFunction1D minus( final LinearFunction1D other )
	{
		return new LinearFunction1D( getGradient() - other.getGradient(), getConstant() - other.getConstant() );
	}
}
