/* $Id$
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
package ab.j3d;

/**
 * This class provides tools for various math problems.
 *
 * @author  G. Meinders
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class MathTools
{
	/**
	 * Perform modulo operation whose result has the same sign as the divisor.
	 * This is different from the default Java implementation, which has the
	 * same sign as the dividend.
	 *
	 * @param   dividend    Dividend.
	 * @param   divisor     Divisor.
	 *
	 * @return  Result of modulo. Sign of result is same as the divisor.
	 */
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
	 * @param   dividend    Dividend.
	 * @param   divisor     Divisor.
	 *
	 * @return  Result of modulo. Sign of result is same as the divisor.
	 */
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
	 * @param   dividend    Dividend.
	 * @param   divisor     Divisor.
	 *
	 * @return  Result of modulo. Sign of result is same as the divisor.
	 */
	public static double mod( final double dividend, final double divisor )
	{
		final double tmp = dividend % divisor;
		return ( ( tmp != 0.0 ) && ( ( divisor < 0.0 ) != ( tmp < 0.0 ) ) ) ? tmp + divisor : tmp;
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  {@code true} is the values are within a +/- 0.001 tolerance
	 *          of each other; {@code false} otherwise.
	 */
	public static boolean almostEqual( final float value1 , final float value2 )
	{
		final float delta = value1 - value2;
		return ( delta <= 0.001f ) && ( delta >= -0.001f );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  {@code true} is the values are within a +/- 0.001 tolerance
	 *          of each other; {@code false} otherwise.
	 */
	public static boolean almostEqual( final double value1 , final double value2 )
	{
		final double delta = value1 - value2;
		return ( delta <= 0.001 ) && ( delta >= -0.001 );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  {@code true} is the values are within the sepcified
	 *          tolerance of each other; {@code false} otherwise.
	 */
	public static boolean almostEqual( final float value1 , final float value2 , final float epsilon )
	{
		final float delta = value1 - value2;
		return ( delta <= epsilon ) && ( delta >= -epsilon );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  {@code true} is the values are within the sepcified
	 *          tolerance of each other; {@code false} otherwise.
	 */
	public static boolean almostEqual( final double value1 , final double value2 , final double epsilon )
	{
		final double delta = value1 - value2;
		return ( delta <= epsilon ) && ( delta >= -epsilon );
	}

	/**
	 * Test if the first operand is less than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is {@code value1} is less than or within
	 *          a +/- 0.001 tolerance of {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean lessOrAlmostEqual( final float value1 , final float value2 )
	{
		return ( ( value1 - value2 ) <= 0.001f );
	}

	/**
	 * Test if the first operand is less than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is {@code value1} is less than or within
	 *          a +/- 0.001 tolerance of {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean lessOrAlmostEqual( final double value1 , final double value2 )
	{
		return ( ( value1 - value2 ) <= 0.001 );
	}

	/**
	 * Test if the first operand is less than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  <code>true</code> is {@code value1} is less than or within
	 *          the specified tolerance of {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean lessOrAlmostEqual( final float value1 , final float value2 , final float epsilon )
	{
		return ( ( value1 - value2 ) <= epsilon );
	}

	/**
	 * Test if the first operand is less than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  <code>true</code> is {@code value1} is less than or within
	 *          the specified tolerance of {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean lessOrAlmostEqual( final double value1 , final double value2 , final double epsilon )
	{
		return ( ( value1 - value2 ) <= epsilon );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is {@code value1} is greater than or within
	 *          a +/- 0.001 tolerance of {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean greaterOrAlmostEqual( final float value1 , final float value2 )
	{
		return ( ( value2 - value1 ) <= 0.001f );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is {@code value1} is greater than or within
	 *          a +/- 0.001 tolerance of {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean greaterOrAlmostEqual( final double value1 , final double value2 )
	{
		return ( ( value2 - value1 ) <= 0.001 );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  <code>true</code> is {@code value1} is greater than or within
	 *          the specified tolerance of {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean greaterOrAlmostEqual( final float value1 , final float value2 , final float epsilon )
	{
		return ( ( value2 - value1 ) <= epsilon );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  <code>true</code> is {@code value1} is greater than or within
	 *          the specified tolerance of {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean greaterOrAlmostEqual( final double value1 , final double value2 , final double epsilon )
	{
		return ( ( value2 - value1 ) <= epsilon );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is {@code value1} is at least the
	 *          <code>0.001</code> less than {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean significantlyLessThan( final float value1 , final float value2 )
	{
		return ( ( value2 - value1 ) > 0.001f );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is {@code value1} is at least the
	 *          <code>0.001</code> less than {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean significantlyLessThan( final double value1 , final double value2 )
	{
		return ( ( value2 - value1 ) > 0.001 );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  <code>true</code> is {@code value1} is at least the
	 *          specified tolerance less than {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean significantlyLessThan( final float value1 , final float value2 , final float epsilon )
	{
		return ( ( value2 - value1 ) > epsilon );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  <code>true</code> is {@code value1} is at least the
	 *          specified tolerance less than {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean significantlyLessThan( final double value1 , final double value2 , final double epsilon )
	{
		return ( ( value2 - value1 ) > epsilon );
	}

	/**
	 * Test if the first operand is significantly greater than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is {@code value1} is at least the
	 *          <code>0.001</code> greater than {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean significantlyGreaterThan( final float value1 , final float value2 )
	{
		return ( ( value1 - value2 ) > 0.001f );
	}

	/**
	 * Test if the first operand is significantly greater than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is {@code value1} is at least the
	 *          <code>0.001</code> greater than {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean significantlyGreaterThan( final double value1 , final double value2 )
	{
		return ( ( value1 - value2 ) > 0.001 );
	}

	/**
	 * Test if the first operand is significantly greater than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  <code>true</code> is {@code value1} is at least the
	 *          specified tolerance greater than {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean significantlyGreaterThan( final float value1 , final float value2 , final float epsilon )
	{
		return ( ( value1 - value2 ) > epsilon );
	}

	/**
	 * Test if the first operand is significantly greater than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 * @param   epsilon     Tolerance (always a positive number).
	 *
	 * @return  <code>true</code> is {@code value1} is at least the
	 *          specified tolerance greater than {@code value2};
	 *          {@code false} otherwise.
	 */
	public static boolean significantlyGreaterThan( final double value1 , final double value2 , final double epsilon )
	{
		return ( ( value1 - value2 ) > epsilon );
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
	 * result is <code>NaN</code>. Also, if any parameter is {@code NaN},
	 * the result is undefined.
	 *
	 * @param   x1  First end-point of the first range.
	 * @param   x2  Second end-point of the first range.
	 * @param   y1  First end-point of the second range.
	 * @param   y2  Second end-point of the second range.
	 *
	 * @return Distance between the ranges, or {@code NaN}.
	 */
	public static float rangeDistance( final float x1 , final float x2 , final float y1 , final float y2 )
	{
		/*
		 * Sort input values, such that:
		 *  - xmin <= xmax
		 *  - ymin <= ymax
		 *  - xmin <= ymin
		 */

		      float xmin;
		      float xmax;
		final float ymin;
		final float ymax;

		if ( x1 <= x2 )
		{
			xmin = x1;
			xmax = x2;
		}
		else
		{
			xmin = x2;
			xmax = x1;
		}

		if ( y1 <= y2 )
		{
			if ( xmin <= y1 )
			{
				ymin = y1;
				ymax = y2;
			}
			else
			{
				ymin = xmin;
				ymax = xmax;
				xmin = y1;
				xmax = y2;
			}
		}
		else
		{
			if ( xmin <= y2 )
			{
				ymin = y2;
				ymax = y1;
			}
			else
			{
				ymin = xmin;
				ymax = xmax;
				xmin = y2;
				xmax = y1;
			}
		}

		/*
		 * Determine the distance, if possible; otherwise NaN.
		 */

		final float result;
		if ( ( xmax < ymin ) || ( xmax < ymax ) && ( xmin < ymin ) )
		{
			result = ymin - xmax;
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
	 * result is <code>NaN</code>. Also, if any parameter is {@code NaN},
	 * the result is undefined.
	 *
	 * @param   x1  First end-point of the first range.
	 * @param   x2  Second end-point of the first range.
	 * @param   y1  First end-point of the second range.
	 * @param   y2  Second end-point of the second range.
	 *
	 * @return Distance between the ranges, or {@code NaN}.
	 */
	public static double rangeDistance( final double x1 , final double x2 , final double y1 , final double y2 )
	{
		/*
		 * Sort input values, such that:
		 *  - xmin <= xmax
		 *  - ymin <= ymax
		 *  - xmin <= ymin
		 */

		      double xmin;
		      double xmax;
		final double ymin;
		final double ymax;

		if ( x1 <= x2 )
		{
			xmin = x1;
			xmax = x2;
		}
		else
		{
			xmin = x2;
			xmax = x1;
		}

		if ( y1 <= y2 )
		{
			if ( xmin <= y1 )
			{
				ymin = y1;
				ymax = y2;
			}
			else
			{
				ymin = xmin;
				ymax = xmax;
				xmin = y1;
				xmax = y2;
			}
		}
		else
		{
			if ( xmin <= y2 )
			{
				ymin = y2;
				ymax = y1;
			}
			else
			{
				ymin = xmin;
				ymax = xmax;
				xmin = y2;
				xmax = y1;
			}
		}

		/*
		 * Determine the distance, if possible; otherwise NaN.
		 */

		final double result;
		if ( ( xmax < ymin ) || ( xmax < ymax ) && ( xmin < ymin ) )
		{
			result = ymin - xmax;
		}
		else
		{
			result = Double.NaN;
		}

		return result;
	}

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private MathTools()
	{
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
	 * @param   value   Value to find the nearest power of two for.
	 *
	 * @return Nearest power of two.
	 *
	 * @throws IllegalArgumentException if {@code value} isn't positive.
	 */
	public static int nearestPowerOfTwo( final int value )
	{
		if ( value <= 0 )
		{
			throw new IllegalArgumentException( "value: " + value );
		}

		final int     highestOneBit  = Integer.highestOneBit( value );
		final boolean roundUp        = ( ( ( highestOneBit >> 1 ) & value ) != 0 );
		final int     unsignedResult = roundUp ? ( highestOneBit << 1 ) : highestOneBit;

		return ( unsignedResult >= 0 ) ? unsignedResult : ( 1 << 30 );
	}

	/**
	 * Test if two objects are equals according to {@link Object#equals(Object)}
	 * in a null-safe manner.
	 *
	 * @param   object1     First object to compare.
	 * @param   object2     Second object to compare.
	 *
	 * @return  {@code true} is the object are equal or both {@code null};
	 *          {@code false} otherwise.
	 */
	public static boolean equals( final Object object1 , final Object object2 )
	{
		return ( object1 != null ) ? object1.equals( object2 ) : ( object2 == null );
	}

	/**
	 * Calculate hash code for floating-point value. This is the equivalent of
	 * calling {@code Doouble.valueOf(value).hashCode()}.
	 *
	 * @param   value   Value to calculate hash code for.
	 *
	 * @return Hash code.
	 */
	public static int hashCode( final Double value )
	{
		final int result;

		if ( value != null )
		{
			final long bits = Double.doubleToLongBits( value );
			result = (int) ( bits ^ ( bits >>> 32 ) );
		}
		else
		{
			result = 0;
		}

		return result;
	}

	/**
	 * Calculate hash code for floating-point value. This is the equivalent of
	 * calling {@code Doouble.valueOf(value).hashCode()}.
	 *
	 * @param   value   Value to calculate hash code for.
	 *
	 * @return Hash code.
	 */
	public static int hashCode( final double value )
	{
		final long bits = Double.doubleToLongBits( value) ;
		return (int) ( bits ^ ( bits >>> 32 ) );
	}
}
