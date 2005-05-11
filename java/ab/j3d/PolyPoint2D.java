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
	 * Bulge factor of arc segment that ends at this point.
	 * <p />
	 * The bulge is the tangent of 1/4 of the included angle for the arc between
	 * the the previous point of the polyline and this point. A negative bulge
	 * value indicates that the arc goes clockwise from the first vertex to the
	 * second vertex. A bulge of 0 indicates a straight segment, and a bulge of
	 * 1 is a semicircle.
	 * <p />
	 * See: <a href='http://www.afralisp.com/lisp/Bulges1.htm'>http://www.afralisp.com/lisp/Bulges1.htm</a>
	 */
	public final double bulge;

	/**
	 * This utility-method calculates the enclosed angle of an arc segment for
	 * the specified bulge value.
	 *
	 * @param   bulge  Bulge factor for arc segment.
	 *
	 * @return  Angle for arc segment.
	 */
	public static double calculateAngleFromBulge( final double bulge )
	{
		return Math.atan( bulge ) * 4.0;
	}

	/**
	 * This utility-method calculates the bulge value for the specified enclosed
	 * angle of an arc segment.
	 *
	 * @param   angle   Angle for arc segment.
	 *
	 * @return  Bulge factor for arc segment.
	 */
	public static double calculateBulgeFromAngle( final double angle )
	{
		return Math.tan( angle / 4.0 );
	}

	/**
	 * This utility-method calculates the radius of the circle that an arc segment
	 * is part of. The sign of the returned value defines on what side of the
	 * chord the center point can be found. If positive, the center point is on
	 * the right side of the chord; if negative, the center point is on the left
	 * side.
	 *
	 * @param   p1X     Start X coordinate of arc segment.
	 * @param   p1Y     Start Y coordinate of arc segment.
	 * @param   p2X     End X coordinate of arc segment.
	 * @param   p2Y     End Y coordinate of arc segment.
	 * @param   bulge   Bulge factor.
	 *
	 * @return  Radius of circle containing arc (can be negative, see comment).
	 *
	 * @see     #bulge
	 */
	public static double calculateArcSegmentRadius( final double p1X , final double p1Y , final double p2X , final double p2Y , final double bulge )
	{
		final double deltaX          = p2X - p1X;
		final double deltaY          = p2Y - p1Y;

		final double chordLength     = Math.sqrt( deltaX * deltaX + deltaY * deltaY );
		final double halfChordLength = chordLength / 2.0;
		final double arcHeight       = halfChordLength * bulge;
		final double radius          = ( halfChordLength * halfChordLength + arcHeight * arcHeight ) / ( 2.0 * arcHeight );

		return radius;
	}

	/**
	 * This utility-method calculates the center point of the circle that an arc
	 * segment is part of.
	 *
	 * @param   p1X     Start X coordinate of arc segment.
	 * @param   p1Y     Start Y coordinate of arc segment.
	 * @param   p2X     End X coordinate of arc segment.
	 * @param   p2Y     End Y coordinate of arc segment.
	 * @param   bulge   Bulge factor.
	 *
	 * @return  Center-point of circle containing arc.
	 *
	 * @see     #bulge
	 */
	public static PolyPoint2D calculateArcSegmentCenter( final double p1X , final double p1Y , final double p2X , final double p2Y , final double bulge )
	{
		final double chordDeltaX = p2X - p1X;
		final double chordDeltaY = p2Y - p1Y;
		final double chordMidX   = ( p1X + p2X ) / 2.0;
		final double chordMidY   = ( p1Y + p2Y ) / 2.0;
		final double chordLength = Math.sqrt( chordDeltaX * chordDeltaX + chordDeltaY * chordDeltaY );

		final double halfChordLength = chordLength / 2.0;
		final double arcHeight       = halfChordLength * bulge;
		final double radius          = ( halfChordLength * halfChordLength + arcHeight * arcHeight ) / ( 2.0 * arcHeight );
		final double apothemLength   = ( radius - arcHeight );

		final double tanApothem      = apothemLength / chordLength;
		final double centerX         = chordMidX - tanApothem * chordDeltaY;
		final double centerY         = chordMidY + tanApothem * chordDeltaX;

		return new PolyPoint2D( centerX , centerY , 0.0 );
	}

	/**
	 * This utility-method calculates the start angle of an arc segment (relative
	 * to the X-axis, in counter-clockwise direction).
	 *
	 * @param   p1X     Start X coordinate of arc segment.
	 * @param   p1Y     Start Y coordinate of arc segment.
	 * @param   p2X     End X coordinate of arc segment.
	 * @param   p2Y     End Y coordinate of arc segment.
	 * @param   bulge   Bulge factor.
	 *
	 * @return  Start angle of arc (counter-clockwise, relative to X-axis).
	 *
	 * @see     #bulge
	 * @see     #getArcSegmentAngle
	 */
	public static double calculateArcSegmentStartAngle( final double p1X , final double p1Y , final double p2X , final double p2Y , final double bulge )
	{
		final PolyPoint2D center = calculateArcSegmentCenter( p1X , p1Y , p2X , p2Y , bulge );
		final double      angle  = Math.toDegrees( Math.atan2( p1Y - center.y , p1X - center.x ) );

		return ( angle < 0.0 ) ? angle + 360.0 : angle;
	}

	/**
	 * Construct new control point.
	 *
	 * @param   newX    X coordinate of control point.
	 * @param   newY    Y coordinate of control point.
	 */
	public PolyPoint2D( final double newX , final double newY , final double newBulge )
	{
		x     = newX;
		y     = newY;
		bulge = newBulge;
	}

	/**
	 * Clone constructor.
	 *
	 * @param   original    Original to clone.
	 */
	public PolyPoint2D( final PolyPoint2D original )
	{
		x     = original.x;
		y     = original.y;
		bulge = original.bulge;
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
			result = ( ( d = x     - other.x     ) > -tolerance ) && ( d < tolerance )
			      && ( ( d = y     - other.y     ) > -tolerance ) && ( d < tolerance )
			      && ( ( d = bulge - other.bulge ) > -tolerance ) && ( d < tolerance );
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
			result = ( x == other.x ) && ( y == other.y ) && ( bulge == other.bulge );
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
		return (int)( ( l = Double.doubleToLongBits( x     ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( y     ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( bulge ) ) ^ ( l >>> 32 ) );
	}

	/**
	 * Test if the segment ending at this point is an arc segment (line
	 * otherwise). This is derived from the {@link #bulge} value.
	 *
	 * @return  <code>true</code> if the segment ending at this point is an arc;
	 *          <code>false</code> otherwise (line / start point).
	 */
	public boolean isArcSegment()
	{
		return ( bulge != 0.0 );
	}

	/**
	 * Get angle for arc segment ending at this point. This angle is derived
	 * from the {@link #bulge} value.
	 *
	 * @return  Angle for arc segment ending at this point.
	 */
	public double getArcSegmentAngle()
	{
		return calculateAngleFromBulge( bulge );
	}

	/**
	 * Get center point of arc segment ending at this point. The start point of
	 * the arc must be specified.
	 *
	 * @param   start   Start point of arc.
	 *
	 * @return  Center point.
	 *
	 */
	public PolyPoint2D getArcSegmentCenter( final PolyPoint2D start )
	{
		return calculateArcSegmentCenter( start.x , start.y , x , y , bulge );
	}

	/**
	 * Get radius of arc segment ending at this point. The start point of the
	 * arc must be specified.
	 *
	 * @param   start   Start point of arc.
	 *
	 * @return  Radius of arc.
	 */
	public double getArcSegmentRadius( final PolyPoint2D start )
	{
		return calculateArcSegmentRadius( start.x , start.y , x , y , bulge );
	}

	/**
	 * Test if arc segment ending at this point is clockwise vs. counter-clockwise.
	 * This is derived from the {@link #bulge} value.
	 *
	 * @return  <code>true</code> if the arc segment is clockwise;
	 *          <code>false</code> if the arc segment is counter-clockwise or
	 *          not an arc at all.
	 */
	public boolean isArcSegmentClockwise()
	{
		return ( bulge < 0.0 );
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
	 * Extrude a control point from the segment with the given start and end
	 * points. This works as follows:
	 * <pre>
	 *
	 * <i>B</i> &lt; - - - <i>S</i> -------------> <i>E</i> - - - > <i>A</i>
	 *      <i>-e</i>                        <i>e</i>
	 *
	 * Where:
	 *   <i>S</i> = start point
	 *   <i>E</i> = end point
	 *   <i>e</i> = extruded segment length
	 *
	 *   <i>A</i> = resulting extruded point if <i>e</i> > 0.0
	 *   <i>B</i> = resulting extruded point if <i>e</i> < 0.0
	 * </pre>
	 *
	 * @param   start       Start point.
	 * @param   end         End point.
	 * @param   extrusion   Length of extruded segment (negative to extrude start point).
	 *
	 * @return  Extruded control-point;
	 *          <code>null</code> if no extruded point could be determined (the
	 *          <code>start</code>-<code>end</code> segment or extruded segment
	 *          has a zero length).
	 */
	public static PolyPoint2D getExtrudedPoint( final PolyPoint2D start , final PolyPoint2D end , final double extrusion )
	{
		final PolyPoint2D result;

		if ( extrusion != 0.0 )
		{
			final double length = getLength(  start , end );
			if ( length > 0.0 )
			{
				final double factor = ( ( ( extrusion > 0.0 ) ? length : 0.0 ) + extrusion ) / length;

				result = new PolyPoint2D( start.x + factor * ( end.x - start.x )
				                        , start.y + factor * ( end.y - start.y ) , 0.0 );
			}
			else
			{
				result = null;
			}
		}
		else
		{
			result = null;
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

			final double arcEndX  = Double.parseDouble( str.substring( firstComma  + 1 , secondComma ) );
			final double arcEndY  = Double.parseDouble( str.substring( secondComma + 1 , thirdComma ) );
			final double arcBulge = Double.parseDouble( str.substring( thirdComma  + 1 ) );

			result = new PolyPoint2D( arcEndX , arcEndY , arcBulge );
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
		final double x     = this.x;
		final double y     = this.y;
		final double bulge = this.bulge;

		return ( bulge == 0.0 ) ? ( "L," + x + ',' + y ) : ( "A," + x + ',' + y + ',' + bulge );
	}
}
