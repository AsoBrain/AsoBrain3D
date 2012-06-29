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
package ab.j3d.model;

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
	 * Coefficient a in f(x) = ax + b.
	 */
	private Vector3D _factor;

	/**
	 * Constant b in f(x) = ax + b.
	 */
	private Vector3D _constant;

	/**
	 * Finds a specific linear function f(x) = ax + b based on the given
	 * points (x, f(x)), by solving for a and b. If parameters or values
	 * are equal along a given axis, the vector will be constant along that
	 * axis and equal to the corresponding component of {@code valueA}.
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

		final double dx = parameterB.x - parameterA.x;
		final double factorX = MathTools.almostEqual( dx, 0.0, epsilon ) ? 0.0 : ( valueB.x - valueA.x ) / dx;
		final double constantX = valueA.x - factorX * parameterA.x;

		final double dy = parameterB.y - parameterA.y;
		final double factorY = MathTools.almostEqual( dy, 0.0, epsilon ) ? 0.0 : ( valueB.y - valueA.y ) / dy;
		final double constantY = valueA.y - factorY * parameterA.y;

		final double dz = parameterB.z - parameterA.z;
		final double factorZ = MathTools.almostEqual( dz, 0.0, epsilon ) ? 0.0 : ( valueB.z - valueA.z ) / dz;
		final double constantZ = valueA.z - factorZ * parameterA.z;

		return new LinearFunction3D( new Vector3D( factorX, factorY, factorZ ), new Vector3D( constantX, constantY, constantZ ) );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param   factor      Coefficient a in f(x) = ax + b.
	 * @param   constant    Constant b in f(x) = ax + b.
	 */
	public LinearFunction3D( final Vector3D factor, final Vector3D constant )
	{
		_constant = constant;
		_factor = factor;
	}

	/**
	 * Returns the factor, i.e. the coefficient a in f(x) = ax + b.
	 *
	 * @return  Factor.
	 */
	public Vector3D getFactor()
	{
		return _factor;
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
	 * Returns the value of the function for the given parameter.
	 *
	 * @param   parameter   Parameter x in f(x) = ax + b.
	 *
	 * @return  Value of the function.
	 */
	public Vector3D get( final Vector3D parameter )
	{
		return new Vector3D( _factor.x * parameter.x + _constant.x,
		                     _factor.y * parameter.y + _constant.y,
		                     _factor.z * parameter.z + _constant.z );
	}

	@Override
	public String toString()
	{
		return super.toString() + "[f(v) = " + _factor + " * v + " + _constant + "]";
	}

	/**
	 * Create human-readable representation of this object.
	 * This is especially useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this object.
	 */
	public String toFriendlyString()
	{
		return getClass().getSimpleName() + "[ f(v) = " + _factor.toFriendlyString() + " * v + " + _constant.toFriendlyString() + " ]";
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
		return new LinearFunction3D( getFactor().plus( other.getFactor() ), getConstant().plus( other.getConstant() ) );
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
		return new LinearFunction3D( getFactor().minus( other.getFactor() ), getConstant().minus( other.getConstant() ) );
	}
}
