/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2001-2005
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
 * This class describes a polyline control point in 2D.
 *
 * @author  Sjoerd Bouwman
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class PolyPoint2D
{
	/**
	 * Tolerance for almostEquals() method.
	 *
	 * @see     #almostEquals
	 */
	public static final double ALMOST = 0.0001;

	/**
	 * X coordinate of control point.
	 */
	public final double x;

	/**
	 * Y coordinate of control point.
	 */
	public final double y;

	/**
	 * Buldge factor of arc segment that ends at this point.
	 */
	public final double buldge;

	/**
	 * Construct new control point.
	 *
	 * @param   newX    X coordinate of control point.
	 * @param   newY    Y coordinate of control point.
	 */
	public PolyPoint2D( final double newX , final double newY , final double newBuldge )
	{
		x      = newX;
		y      = newY;
		buldge = newBuldge;
	}

	/**
	 * Clone constructor.
	 *
	 * @param   original    Original to clone.
	 */
	public PolyPoint2D( final PolyPoint2D original )
	{
		x      = original.x;
		y      = original.y;
		buldge = original.buldge;
	}

	/**
	 * Test if the specified point is 'almost equal to' this point. The
	 * other point must have the same coordinates with a extremely small
	 * tolerance.
	 *
	 * @param   other   Point to compare with.
	 *
	 * @return  <code>true</code> if the specified point is 'almost' equal;
	 *          <code>false</code> if the specified point is different.
	 */
	public boolean almostEquals( final PolyPoint2D other )
	{
		return almostEquals( other , ALMOST );
	}

	/**
	 * Test if the specified point is 'almost equal to' this point. The
	 * other point must have the same coordinates as this one with the
	 * specified tolerance.
	 *
	 * @param   other       Point to compare with.
	 * @param   tolerance   Acceptable tolerance.
	 *
	 * @return  <code>true</code> if the specified point is 'almost' equal;
	 *          <code>false</code> if the specified point is different.
	 */
	public boolean almostEquals( final PolyPoint2D other , final double tolerance )
	{
		final boolean result;

		if ( other == null )
		{
			result = false;
		}
		else if ( other == this )
		{
			result = true;
		}
		else
		{
			double d;
			result = ( ( d = x      - other.x      ) > -tolerance ) && ( d < tolerance )
		          && ( ( d = y      - other.y      ) > -tolerance ) && ( d < tolerance )
		          && ( ( d = buldge - other.buldge ) > -tolerance ) && ( d < tolerance );
		}

		return result;
	}

	/**
	 * Test for equality between this control point and the specified object.
	 * If the other object is a PolyPoint2D, this call is forwarded to
	 * #equals(PolyPoint2D).
	 *
	 * @param   obj   Object to compare with.
	 *
	 * @return  <code>true</code> if the specified object is equal to this;
	 *          <code>false</code> otherwise.
	 */
	public boolean equals( final Object obj )
	{
		final boolean result;

		if ( obj == null )
		{
			result = false;
		}
		else if ( obj == this )
		{
			result = true;
		}
		else if ( obj instanceof PolyPoint2D )
		{
			final PolyPoint2D other = (PolyPoint2D)obj;
			result = ( x == other.x ) && ( y == other.y ) && ( buldge == other.buldge );
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Returns a hash code for this object.
	 *
	 * @return  Hash code value for this object.
	 */
	public int hashCode()
	{
		long l;
		return (int)( ( l = Double.doubleToLongBits( x      ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( y      ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( buldge ) ) ^ ( l >>> 32 ) );
	}

	/**
	 * Get direction of segment with the given start and end points.
	 *
	 * @param   start   Start point.
	 * @param   end     End point.
	 *
	 * @return  Direction of segment;
	 *          <code>null</code> if start and end are too close.
	 */
	public static PolyPoint2D getDirection( final PolyPoint2D start , final PolyPoint2D end )
	{
		final PolyPoint2D result;

		if ( start.almostEquals( end ) )
		{
			result = null;
		}
		else
		{
			final double dx = end.x - start.x;
			final double dy = end.y - start.y;

			final double length = Math.sqrt( dx * dx + dy * dy );

			result = new PolyPoint2D( dx / length , dy / length , 0.0 );
		}

		return result;
	}

	/**
	 * Get length of segment with the given start and end points.
	 *
	 * @param   start   Start point.
	 * @param   end     End point.
	 *
	 * @return  Length of segment.
	 */
	public static double getLength( final PolyPoint2D start , final PolyPoint2D end )
	{
		final double result;

		if ( start.almostEquals( end ) )
		{
			result = 0.0;
		}
		else
		{
			final double dx = end.x - start.x;
			final double dy = end.y - start.y;

			result = Math.sqrt( dx * dx + dy * dy );
		}

		return result;
	}

	/**
	 * Construct point from string representation that was previously
	 * generated by the toString() method.
	 *
	 * @param   str     String representation of point.
	 *
	 * @return  Point that was created (may be <code>null</code>).
	 */
	public static PolyPoint2D createInstance( final String str )
	{
		final PolyPoint2D result;

		if ( str == null || str.length() == 0 )
			throw new IllegalArgumentException( "invalid point specification: " + str );

		final int firstComma = str.indexOf( (int)',' );
		if ( firstComma < 0 )
			throw new IllegalArgumentException( "insufficient tokens in specification: " + str );

		final String type = str.substring( 0 , firstComma );

		if ( "L".equals( type ) )
		{
			final int secondComma = str.indexOf( (int)',' , firstComma + 1 );
			if ( ( secondComma < 0 ) || ( str.indexOf( (int)',' , secondComma + 1 ) >= 0 ) )
				throw new IllegalArgumentException( "invalid token count in line specification: " + str );

			final double lineEndX = Double.parseDouble( str.substring( firstComma + 1 , secondComma ) );
			final double lineEndY = Double.parseDouble( str.substring( secondComma + 1 ) );

			result = new PolyPoint2D( lineEndX , lineEndY , 0.0 );
		}
		else if ( "A".equals( type ) )
		{
			final int secondComma = str.indexOf( (int)',' , firstComma + 1 );
			if ( secondComma < 0 )
				throw new IllegalArgumentException( "invalid token count in line specification: " + str );

			final int thirdComma = str.indexOf( (int)',' , secondComma + 1 );
			if ( ( thirdComma < 0 ) || ( str.indexOf( (int)',' , thirdComma + 1 ) >= 0 ) )
				throw new IllegalArgumentException( "invalid token count in line specification: " + str );

			final double arcEndX   = Double.parseDouble( str.substring( firstComma  + 1 , secondComma ) );
			final double arcEndY   = Double.parseDouble( str.substring( secondComma + 1 , thirdComma ) );
			final double arcBuldge = Double.parseDouble( str.substring( thirdComma  + 1 ) );

			result = new PolyPoint2D( arcEndX , arcEndY , arcBuldge );
		}
		else
		{
			throw new IllegalArgumentException( "unrecognized point type: " + type );
		}

		return result;
	}

	/**
	 * Get string representation of this object.
	 *
	 * @return  String representing this object.
	 */
	public String toString()
	{
		final double x      = this.x;
		final double y      = this.y;
		final double buldge = this.buldge;

		return ( buldge == 0.0 ) ? ( "L," + x + ',' + y ) : ( "A," + x + ',' + y + ',' + buldge );
	}
}
