/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
 * ====================================================================
 */
package ab.j3d.geom;

import ab.j3d.*;

/**
 * 3D linear function of the form f(x) = ax + b.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class LinearFunction3D
{
	/**
	 * Gradient a in f(x) = ax + b.
	 */
	private Vector3D _gradient;

	/**
	 * Constant b in f(x) = ax + b.
	 */
	private Vector3D _constant;

	/**
	 * Finds a specific linear function f(x) = ax + b based on the given
	 * points (x, f(x)), by solving for a and b. If the parameters are equal
	 * along a given axis, it follows that both values must also be equal
	 * (otherwise, the result is not a function); in this case the function
	 * will pass through the origin and the given point along that axis.
	 * If the parameters are not equal, but the values are equal along a given
	 * axis, the result vector will be constant along that axis and equal to the
	 * corresponding component of {@code valueA}.
	 *
	 * @param   parameterA  First value of x.
	 * @param   valueA      First value of f(x).
	 * @param   parameterB  Second value of x.
	 * @param   valueB      Second value of f(x).
	 *
	 * @return  Specified linear function.
	 */
	public static LinearFunction3D fromPoints( final Vector3D parameterA, final Vector3D valueA, final Vector3D parameterB, final Vector3D valueB )
	{
		final double epsilon = 1.0e-8;

		final double gradientX;
		final double constantX;

		if ( MathTools.almostEqual( parameterA.x, parameterB.x, epsilon ) )
		{
			if ( !MathTools.almostEqual( valueA.x, valueB.x, epsilon ) )
			{
				throw new IllegalArgumentException( "Can't determine X-gradient, because X-parameters are the same, but X values are not (parameterA=" + parameterA + ", valueA=" + valueA + ", parameterB=" + parameterB + ", valueB=" + valueB );
			}

			gradientX = MathTools.almostEqual( parameterA.x, 0.0, epsilon ) ? 0.0 : valueA.x / parameterA.x;
			constantX = 0.0;
		}
		else if ( MathTools.almostEqual( valueA.x, valueB.x, epsilon ) )
		{
			gradientX = 0.0;
			constantX = valueA.x;
		}
		else
		{
			gradientX = ( valueB.x - valueA.x ) / ( parameterB.x - parameterA.x );
			constantX = valueA.x - gradientX * parameterA.x;
		}

		final double gradientY;
		final double constantY;

		if ( MathTools.almostEqual( parameterA.y, parameterB.y, epsilon ) )
		{
			if ( !MathTools.almostEqual( valueA.y, valueB.y, epsilon ) )
			{
				throw new IllegalArgumentException( "Can't determine Y-gradient, because Y-parameters are the same, but Y values are not (parameterA=" + parameterA + ", valueA=" + valueA + ", parameterB=" + parameterB + ", valueB=" + valueB );
			}

			gradientY = MathTools.almostEqual( parameterA.y, 0.0, epsilon ) ? 0.0 : valueA.y / parameterA.y;
			constantY = 0.0;
		}
		else if ( MathTools.almostEqual( valueA.y, valueB.y, epsilon ) )
		{
			gradientY = 0.0;
			constantY = valueA.y;
		}
		else
		{
			gradientY = ( valueB.y - valueA.y ) / ( parameterB.y - parameterA.y );
			constantY = valueA.y - gradientY * parameterA.y;
		}

		final double gradientZ;
		final double constantZ;

		if ( MathTools.almostEqual( parameterA.z, parameterB.z, epsilon ) )
		{
			if ( !MathTools.almostEqual( valueA.z, valueB.z, epsilon ) )
			{
				throw new IllegalArgumentException( "Can't determine Z-gradient, because Z-parameters are the same, but Z values are not (parameterA=" + parameterA + ", valueA=" + valueA + ", parameterB=" + parameterB + ", valueB=" + valueB );
			}

			gradientZ = MathTools.almostEqual( parameterA.z, 0.0, epsilon ) ? 0.0 : valueA.z / parameterA.z;
			constantZ = 0.0;
		}
		else if ( MathTools.almostEqual( valueA.z, valueB.z, epsilon ) )
		{
			gradientZ = 0.0;
			constantZ = valueA.z;
		}
		else
		{
			gradientZ = ( valueB.z - valueA.z ) / ( parameterB.z - parameterA.z );
			constantZ = valueA.z - gradientZ * parameterA.z;
		}

		return new LinearFunction3D( new Vector3D( gradientX, gradientY, gradientZ ), new Vector3D( constantX, constantY, constantZ ) );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param   gradient      Gradient a in f(x) = ax + b.
	 * @param   constant    Constant b in f(x) = ax + b.
	 */
	public LinearFunction3D( final Vector3D gradient, final Vector3D constant )
	{
		_constant = constant;
		_gradient = gradient;
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param   x   X function.
	 * @param   y   Y function.
	 * @param   z   Z function.
	 */
	public LinearFunction3D( final LinearFunction1D x, final LinearFunction1D y, final LinearFunction1D z )
	{
		this( new Vector3D( x.getGradient(), y.getGradient(), z.getGradient() ), new Vector3D( x.getConstant(), y.getConstant(), z.getConstant() ) );
	}

	/**
	 * Returns the gradient, i.e. the gradient a in f(x) = ax + b.
	 *
	 * @return  Gradient.
	 */
	public Vector3D getGradient()
	{
		return _gradient;
	}

	/**
	 * Sets the gradient, i.e. the gradient a in f(x) = ax + b.
	 *
	 * @param   gradient    Gradient.
	 */
	public void setGradient( final Vector3D gradient )
	{
		_gradient = gradient;
	}

	/**
	 * Returns the constant, i.e. the constant b in f(x) = ax + b.
	 *
	 * @return  Constant.
	 */
	public Vector3D getConstant()
	{
		return _constant;
	}

	/**
	 * Sets the constant, i.e. the constant b in f(x) = ax + b.
	 *
	 * @param   constant    Constant.
	 */
	public void setConstant( final Vector3D constant )
	{
		_constant = constant;
	}

	/**
	 * Get linear function for X-axis.
	 *
	 * @return  Linear function for X-axis.
	 */
	public LinearFunction1D getX()
	{
		final Vector3D gradient = getGradient();
		final Vector3D constant = getConstant();

		return new LinearFunction1D( gradient.x, constant.x );
	}

	/**
	 * Get linear function for Y-axis.
	 *
	 * @return  Linear function for Y-axis.
	 */
	public LinearFunction1D getY()
	{
		final Vector3D gradient = getGradient();
		final Vector3D constant = getConstant();

		return new LinearFunction1D( gradient.y, constant.y );
	}

	/**
	 * Get linear function for Z-axis.
	 *
	 * @return  Linear function for Z-axis.
	 */
	public LinearFunction1D getZ()
	{
		final Vector3D gradient = getGradient();
		final Vector3D constant = getConstant();

		return new LinearFunction1D( gradient.z, constant.z );
	}

	/**
	 * Returns the value of the function for the given parameter.
	 *
	 * @param   parameter   Parameter x in f(x) = ax + b.
	 *
	 * @return  Value of the function.
	 */
	public Vector3D get( final Vector3D parameter )
	{
		final Vector3D gradient = getGradient();
		final Vector3D constant = getConstant();
		return new Vector3D( gradient.x * parameter.x + constant.x,
		                     gradient.y * parameter.y + constant.y,
		                     gradient.z * parameter.z + constant.z );
	}

	@Override
	public String toString()
	{
		return super.toString() + "[f(v) = " + _gradient + " * v + " + _constant + ']';
	}

	/**
	 * Create human-readable representation of this object.
	 * This is especially useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this object.
	 */
	public String toFriendlyString()
	{
		final Class<?> clazz = getClass();
		return clazz.getSimpleName() + "[ f(v) = " + _gradient.toFriendlyString() + " * v + " + _constant.toFriendlyString() + " ]";
	}

	/**
	 * Returns the sum of this function and the given function.
	 *
	 * @param   other   Linear function.
	 *
	 * @return  Sum of the two functions.
	 */
	public LinearFunction3D plus( final LinearFunction3D other )
	{
		final Vector3D gradient = getGradient();
		final Vector3D constant = getConstant();
		return new LinearFunction3D( gradient.plus( other.getGradient() ), constant.plus( other.getConstant() ) );
	}

	/**
	 * Returns the result of subtracting the given function from this function.
	 *
	 * @param   other   Linear function.
	 *
	 * @return  (Signed) difference of the two functions.
	 */
	public LinearFunction3D minus( final LinearFunction3D other )
	{
		final Vector3D gradient = getGradient();
		final Vector3D constant = getConstant();
		return new LinearFunction3D( gradient.minus( other.getGradient() ), constant.minus( other.getConstant() ) );
	}
}
