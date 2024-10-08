/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
package ab.j3d;

import org.jetbrains.annotations.*;

/**
 * This class provides tools for various math problems.
 *
 * @author G. Meinders
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "OverloadedMethodsWithSameNumberOfParameters" )
public final class MathTools
{
	/**
	 * Static instance useful for usage from scripting/expression libraries.
	 */
	@SuppressWarnings( "InstantiationOfUtilityClass" )
	public static final MathTools INSTANCE = new MathTools();

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private MathTools()
	{
	}

	/**
	 * Perform modulo operation whose result has the same sign as the divisor.
	 * This is different from the default Java implementation, which has the
	 * same sign as the dividend.
	 *
	 * @param dividend Dividend.
	 * @param divisor  Divisor.
	 *
	 * @return Result of modulo. Sign of result is same as the divisor.
	 */
	@Contract( pure = true )
	public static int mod( final int dividend, final int divisor )
	{
		final int tmp = dividend % divisor;
		return ( ( tmp != 0 ) && ( ( divisor < 0 ) != ( tmp < 0 ) ) ) ? tmp + divisor : tmp;
	}

	/**
	 * Perform modulo operation whose result has the same sign as the divisor.
	 * This is different from the default Java implementation, which has the
	 * same sign as the dividend.
	 *
	 * @param dividend Dividend.
	 * @param divisor  Divisor.
	 *
	 * @return Result of modulo. Sign of result is same as the divisor.
	 */
	@Contract( pure = true )
	public static float mod( final float dividend, final float divisor )
	{
		final float tmp = dividend % divisor;
		return ( ( tmp != 0.0f ) && ( ( divisor < 0.0f ) != ( tmp < 0.0f ) ) ) ? tmp + divisor : tmp;
	}

	/**
	 * Perform modulo operation whose result has the same sign as the divisor.
	 * This is different from the default Java implementation, which has the
	 * same sign as the dividend.
	 *
	 * @param dividend Dividend.
	 * @param divisor  Divisor.
	 *
	 * @return Result of modulo. Sign of result is same as the divisor.
	 */
	@Contract( pure = true )
	public static double mod( final double dividend, final double divisor )
	{
		final double tmp = dividend % divisor;
		return ( ( tmp != 0.0 ) && ( ( divisor < 0.0 ) != ( tmp < 0.0 ) ) ) ? tmp + divisor : tmp;
	}

	/**
	 * Test if the specified values are significantly different or 'almost'
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code 0} if the values are within a +/- 0.001 tolerance of each
	 * other; {@code -1} if {@code value1} is significantly less than {@code
	 * value2}; {@code 1} if {@code value1} is significantly greater than
	 * {@code value2};
	 */
	@Contract( pure = true )
	public static int significantCompare( final float value1, final float value2 )
	{
		final float delta = value1 - value2;
		return ( delta < -0.001f ) ? -1 : ( delta > 0.001f ) ? 1 : 0;
	}

	/**
	 * Test if the specified values are significantly different or 'almost'
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code 0} if the values are within the given tolerance of each
	 * other; {@code -1} if {@code value1} is significantly less than {@code
	 * value2}; {@code 1} if {@code value1} is significantly greater than
	 * {@code value2};
	 */
	@Contract( pure = true )
	public static int significantCompare( final float value1, final float value2, final float epsilon )
	{
		final float delta = value1 - value2;
		return ( delta < -epsilon ) ? -1 : ( delta > epsilon ) ? 1 : 0;
	}

	/**
	 * Test if the specified values are significantly different or 'almost'
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code 0} if the values are within a +/- 0.001 tolerance of each other;
	 * other; {@code -1} if {@code value1} is significantly less than {@code
	 * value2}; {@code 1} if {@code value1} is significantly greater than
	 * {@code value2};
	 */
	@Contract( pure = true )
	public static int significantCompare( final double value1, final double value2 )
	{
		final double delta = value1 - value2;
		return ( delta < -0.001 ) ? -1 : ( delta > 0.001 ) ? 1 : 0;
	}

	/**
	 * Test if the specified values are significantly different or 'almost'
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code 0} if the values are within the given tolerance of each
	 * other; {@code -1} if {@code value1} is significantly less than {@code
	 * value2}; {@code 1} if {@code value1} is significantly greater than
	 * {@code value2};
	 */
	@Contract( pure = true )
	public static int significantCompare( final double value1, final double value2, final double epsilon )
	{
		final double delta = value1 - value2;
		return ( delta < -epsilon ) ? -1 : ( delta > epsilon ) ? 1 : 0;
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} if the values are within a +/- 0.001 tolerance
	 * of each other; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean almostEqual( final float value1, final float value2 )
	{
		final float delta = value1 - value2;
		return ( delta <= 0.001f ) && ( delta >= -0.001f );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} if the values are within a +/- 0.001 tolerance
	 * of each other; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean almostEqual( final double value1, final double value2 )
	{
		final double delta = value1 - value2;
		return ( delta <= 0.001 ) && ( delta >= -0.001 );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} if the values are within the specified tolerance of
	 * each other; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean almostEqual( final float value1, final float value2, final float epsilon )
	{
		final float delta = value1 - value2;
		return ( delta <= epsilon ) && ( delta >= -epsilon );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} if the values are within the specified tolerance of
	 * each other; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean almostEqual( final double value1, final double value2, final double epsilon )
	{
		final double delta = value1 - value2;
		return ( delta <= epsilon ) && ( delta >= -epsilon );
	}

	/**
	 * Test if the first operand is less than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is less than or within a +/- 0.001
	 * tolerance of {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean lessOrAlmostEqual( final float value1, final float value2 )
	{
		return ( ( value1 - value2 ) <= 0.001f );
	}

	/**
	 * Test if the first operand is less than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is less than or within a +/- 0.001
	 * tolerance of {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean lessOrAlmostEqual( final double value1, final double value2 )
	{
		return ( ( value1 - value2 ) <= 0.001 );
	}

	/**
	 * Test if the first operand is less than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is less than or within the
	 * specified tolerance of {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean lessOrAlmostEqual( final float value1, final float value2, final float epsilon )
	{
		return ( ( value1 - value2 ) <= epsilon );
	}

	/**
	 * Test if the first operand is less than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is less than or within the
	 * specified tolerance of {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean lessOrAlmostEqual( final double value1, final double value2, final double epsilon )
	{
		return ( ( value1 - value2 ) <= epsilon );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is greater than or within a +/-
	 * 0.001 tolerance of {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean greaterOrAlmostEqual( final float value1, final float value2 )
	{
		return ( ( value2 - value1 ) <= 0.001f );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is greater than or within a +/-
	 * 0.001 tolerance of {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean greaterOrAlmostEqual( final double value1, final double value2 )
	{
		return ( ( value2 - value1 ) <= 0.001 );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is greater than or within the
	 * specified tolerance of {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean greaterOrAlmostEqual( final float value1, final float value2, final float epsilon )
	{
		return ( ( value2 - value1 ) <= epsilon );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is greater than or within the
	 * specified tolerance of {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean greaterOrAlmostEqual( final double value1, final double value2, final double epsilon )
	{
		return ( ( value2 - value1 ) <= epsilon );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is at least {@code 0.001} less
	 * than {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean significantlyLessThan( final float value1, final float value2 )
	{
		return ( ( value2 - value1 ) > 0.001f );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is at least {@code 0.001} less
	 * than {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean significantlyLessThan( final double value1, final double value2 )
	{
		return ( ( value2 - value1 ) > 0.001 );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is at least the
	 * specified tolerance less than {@code value2};
	 * {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean significantlyLessThan( final float value1, final float value2, final float epsilon )
	{
		return ( ( value2 - value1 ) > epsilon );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is at least the specified
	 * tolerance less than {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean significantlyLessThan( final double value1, final double value2, final double epsilon )
	{
		return ( ( value2 - value1 ) > epsilon );
	}

	/**
	 * Test if the first operand is significantly greater than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is at least {@code 0.001} greater
	 * than {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean significantlyGreaterThan( final float value1, final float value2 )
	{
		return ( ( value1 - value2 ) > 0.001f );
	}

	/**
	 * Test if the first operand is significantly greater than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is at least {@code 0.001} greater
	 * than {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean significantlyGreaterThan( final double value1, final double value2 )
	{
		return ( ( value1 - value2 ) > 0.001 );
	}

	/**
	 * Test if the first operand is significantly greater than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is at least the specified
	 * tolerance greater than {@code value2}; {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean significantlyGreaterThan( final float value1, final float value2, final float epsilon )
	{
		return ( ( value1 - value2 ) > epsilon );
	}

	/**
	 * Test if the first operand is significantly greater than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is at least the
	 * specified tolerance greater than {@code value2};
	 * {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean significantlyGreaterThan( final double value1, final double value2, final double epsilon )
	{
		return ( ( value1 - value2 ) > epsilon );
	}

	/**
	 * Limits the given value to the specified range. If the given value is
	 * {@code NaN}, the result will be {@code minimum}.
	 *
	 * @param value   Value.
	 * @param minimum Minimum value in the range.
	 * @param maximum Maximum value in the range.
	 *
	 * @return Value from the specified range.
	 */
	@Contract( pure = true )
	public static double clamp( final double value, final double minimum, final double maximum )
	{
		return ( value < minimum ) ? minimum : Math.min( value, maximum );
	}

	/**
	 * Returns the distance between the given ranges. The ranges, as
	 * well as their respective end-points may be specified in any order.
	 *
	 * <p>If the ranges are disjoint, the result is positive. For ranges that
	 * share a single value, the result is zero. If the ranges overlap, the
	 * result is negative.
	 *
	 * <p>If one range encloses the other or if both ranges are equal, the
	 * result is {@code NaN}. Also, if any parameter is {@code NaN},
	 * the result is undefined.
	 *
	 * @param x1 First end-point of the first range.
	 * @param x2 Second end-point of the first range.
	 * @param y1 First end-point of the second range.
	 * @param y2 Second end-point of the second range.
	 *
	 * @return Distance between the ranges, or {@code NaN}.
	 */
	@SuppressWarnings( "SuspiciousNameCombination" )
	@Contract( pure = true )
	public static float rangeDistance( final float x1, final float x2, final float y1, final float y2 )
	{
		/*
		 * Sort input values, such that:
		 *  - xMin <= xMax
		 *  - yMin <= yMax
		 *  - xMin <= yMin
		 */

		float xMin;
		float xMax;
		final float yMin;
		final float yMax;

		if ( x1 <= x2 )
		{
			xMin = x1;
			xMax = x2;
		}
		else
		{
			xMin = x2;
			xMax = x1;
		}

		if ( y1 <= y2 )
		{
			if ( xMin <= y1 )
			{
				yMin = y1;
				yMax = y2;
			}
			else
			{
				yMin = xMin;
				yMax = xMax;
				xMin = y1;
				xMax = y2;
			}
		}
		else
		{
			if ( xMin <= y2 )
			{
				yMin = y2;
				yMax = y1;
			}
			else
			{
				yMin = xMin;
				yMax = xMax;
				xMin = y2;
				xMax = y1;
			}
		}

		/*
		 * Determine the distance, if possible; otherwise NaN.
		 */

		final float result;
		if ( ( xMax < yMin ) || ( xMax < yMax ) && ( xMin < yMin ) )
		{
			result = yMin - xMax;
		}
		else
		{
			result = Float.NaN;
		}

		return result;
	}

	/**
	 * Returns the distance between the given ranges. The ranges, as
	 * well as their respective end-points may be specified in any order.
	 *
	 * <p>If the ranges are disjoint, the result is positive. For ranges that
	 * share a single value, the result is zero. If the ranges overlap, the
	 * result is negative.
	 *
	 * <p>If one range encloses the other or if both ranges are equal, the
	 * result is {@code NaN}. Also, if any parameter is {@code NaN},
	 * the result is undefined.
	 *
	 * @param x1 First end-point of the first range.
	 * @param x2 Second end-point of the first range.
	 * @param y1 First end-point of the second range.
	 * @param y2 Second end-point of the second range.
	 *
	 * @return Distance between the ranges, or {@code NaN}.
	 */
	@SuppressWarnings( "SuspiciousNameCombination" )
	@Contract( pure = true )
	public static double rangeDistance( final double x1, final double x2, final double y1, final double y2 )
	{
		/*
		 * Sort input values, such that:
		 *  - xMin <= xMax
		 *  - yMin <= yMax
		 *  - xMin <= yMin
		 */

		double xMin;
		double xMax;
		final double yMin;
		final double yMax;

		if ( x1 <= x2 )
		{
			xMin = x1;
			xMax = x2;
		}
		else
		{
			xMin = x2;
			xMax = x1;
		}

		if ( y1 <= y2 )
		{
			if ( xMin <= y1 )
			{
				yMin = y1;
				yMax = y2;
			}
			else
			{
				yMin = xMin;
				yMax = xMax;
				xMin = y1;
				xMax = y2;
			}
		}
		else
		{
			if ( xMin <= y2 )
			{
				yMin = y2;
				yMax = y1;
			}
			else
			{
				yMin = xMin;
				yMax = xMax;
				xMin = y2;
				xMax = y1;
			}
		}

		/*
		 * Determine the distance, if possible; otherwise NaN.
		 */

		final double result;
		if ( ( xMax < yMin ) || ( xMax < yMax ) && ( xMin < yMin ) )
		{
			result = yMin - xMax;
		}
		else
		{
			result = Double.NaN;
		}

		return result;
	}

	/**
	 * Returns the power of two nearest to {@code value}, which must be
	 * positive. If the value is equidistant to two powers of two, the higher
	 * power of two is returned.
	 *
	 * <p>For input values of 2<sup>30</sup>+2<sup>29</sup> or more, the
	 * correct result, 2<sup>31</sup>, can't be represented by a (signed)
	 * {@code int}. Therefore, these values are rounded downwards,
	 * resulting in 2<sup>30</sup>.
	 *
	 * @param value Value to find the nearest power of two for.
	 *
	 * @return Nearest power of two.
	 *
	 * @throws IllegalArgumentException if {@code value} isn't positive.
	 */
	@Contract( pure = true )
	public static int nearestPowerOfTwo( final int value )
	{
		if ( value <= 0 )
		{
			throw new IllegalArgumentException( "value: " + value );
		}

		final int highestOneBit = Integer.highestOneBit( value );
		final boolean roundUp = ( ( ( highestOneBit >> 1 ) & value ) != 0 );
		final int unsignedResult = roundUp ? ( highestOneBit << 1 ) : highestOneBit;

		return ( unsignedResult >= 0 ) ? unsignedResult : ( 1 << 30 );
	}

	/**
	 * Test if two objects are equals according to {@link Object#equals(Object)}
	 * in a null-safe manner.
	 *
	 * @param object1 First object to compare.
	 * @param object2 Second object to compare.
	 *
	 * @return {@code true} is the object are equal or both {@code null};
	 * {@code false} otherwise.
	 */
	@Contract( pure = true )
	public static boolean equals( @Nullable final Object object1, @Nullable final Object object2 )
	{
		return ( object1 != null ) ? object1.equals( object2 ) : ( object2 == null );
	}

	/**
	 * Calculate hash code for floating-point value. This is the equivalent of
	 * calling {@link Double#hashCode()}.
	 *
	 * @param value Value to calculate hash code for.
	 *
	 * @return Hash code; 0 if {@code value} is {@code null}.
	 */
	@Contract( pure = true )
	public static int hashCode( @Nullable final Double value )
	{
		final int result;

		if ( value != null )
		{
			final long bits = Double.doubleToLongBits( value );
			result = (int)( bits ^ ( bits >>> 32 ) );
		}
		else
		{
			result = 0;
		}

		return result;
	}

	/**
	 * Calculate hash code for floating-point value. This is the equivalent of
	 * calling {@link Double#valueOf(double)}.hashCode()}.
	 *
	 * @param value Value to calculate hash code for.
	 *
	 * @return Hash code.
	 */
	@Contract( pure = true )
	public static int hashCode( final double value )
	{
		final long bits = Double.doubleToLongBits( value );
		return (int)( bits ^ ( bits >>> 32 ) );
	}
}
