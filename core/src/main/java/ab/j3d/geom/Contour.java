/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import java.util.*;

import ab.j3d.*;

/**
 * This defines a contour. A contour is a single closed path whose segments
 * are defines by a set of points. The first point is the start point, the
 * second point defines the first segment, etc. The last point is
 * automatically connected to the start point.
 *
 * The main purpose of this class is providing intermediate analysis and storage
 * of 2D shape data.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Contour
{
	/**
	 * Shape classes.
	 */
	public enum ShapeClass
	{
		/**
		 * Nothingness.
		 */
		VOID,

		/**
		 * Line segment.
		 */
		LINE_SEGMENT,

		/**
		 * Open path.
		 */
		OPEN_PATH,

		/**
		 * Triangle with clockwise vertex order.
		 */
		CW_TRIANGLE,

		/**
		 * Triangle with counter-clockwise vertex order.
		 */
		CCW_TRIANGLE,

		/**
		 * Quad with clockwise vertex order.
		 */
		CW_QUAD,

		/**
		 * Quad with counter-clockwise vertex order.
		 */
		CCW_QUAD,

		/**
		 * Convex shape with clockwise vertex order.
		 */
		CW_CONVEX,

		/**
		 * Convex shape with counter-clockwise vertex order.
		 */
		CCW_CONVEX,

		/**
		 * Concave shape with clockwise vertex order.
		 */
		CW_CONCAVE,

		/**
		 * Concave shape with counter-clockwise vertex order.
		 */
		CCW_CONCAVE,

		/**
		 * Complex shape with possible self-intersection or multiple sub-paths.
		 */
		COMPLEX;

		/**
		 * Test if this shape is convex.
		 *
		 * @return  <code>true</code> if shape is convex.
		 */
		public boolean isConvex()
		{
			return ( ( this == CCW_TRIANGLE ) ||
			         ( this == CW_TRIANGLE ) ||
			         ( this == CCW_QUAD ) ||
			         ( this == CW_QUAD ) ||
			         ( this == CCW_CONVEX ) ||
			         ( this == CW_CONVEX ) );
		}

		/**
		 * Test if this shape has clockwise vertex order.
		 *
		 * @return  <code>true</code> if shape has clockwise vertex order;
		 *          <code>false</code> if counter-clockwise or undetermined.
		 */
		public boolean isClockwise()
		{
			return ( ( this == CW_TRIANGLE ) ||
			         ( this == CW_QUAD ) ||
			         ( this == CW_CONVEX ) ||
			         ( this == CW_CONCAVE ) );
		}

		/**
		 * Test if this shape has counter-clockwise vertex order.
		 *
		 * @return  <code>true</code> if shape has counter-clockwise vertex order;
		 *          <code>false</code> if clockwise or undetermined.
		 */
		public boolean isCounterClockwise()
		{
			return ( ( this == CCW_TRIANGLE ) ||
			         ( this == CCW_QUAD ) ||
			         ( this == CCW_CONVEX ) ||
			         ( this == CCW_CONCAVE ) );
		}
	}

	/**
	 * Shape class of contour.
	 */
	final ShapeClass _shapeClass;

	/**
	 * Points that define the contour.
	 */
	final List<Point> _points;

	/**
	 * Construct contour.
	 *
	 * @param   shapeClass  Shape class of contour.
	 * @param   points      Points that define the contour.
	 */
	public Contour( final ShapeClass shapeClass, final List<Point> points )
	{
		_shapeClass = shapeClass;
		_points = points;
	}

	/**
	 * Get {@link ShapeClass} of this contour.
	 *
	 * @return  {@link ShapeClass} of this contour.
	 */
	public ShapeClass getShapeClass()
	{
		return _shapeClass;
	}

	/**
	 * Get points that define the contour.
	 *
	 * @return  Points that define the contour.
	 */
	public List<Point> getPoints()
	{
		return Collections.unmodifiableList( _points );
	}

	/**
	 * Get angle between two subsequent segments of a path.
	 *
	 * @param   seg1x   First segment delta X.
	 * @param   seg1y   First segment delta Y.
	 * @param   seg2x   Second segment delta X.
	 * @param   seg2y   Second segment delta Y.
	 *
	 * @return  Angle between segments in radians (positive if angle is
	 *          counter-clockwise, negative if clockwise, <code>0</code> if
	 *          colinear).
	 */
	public static double getAngle( final double seg1x, final double seg1y, final double seg2x, final double seg2y )
	{
		final double result;

		final double normal = seg2y * seg1x - seg2x * seg1y;
		if ( normal < -0.001 )
		{
			final double dot     = seg1x * seg2x + seg1y * seg2y;
			final double prevLen = Math.sqrt( seg1x * seg1x + seg1y * seg1y );
			final double nextLen = Math.sqrt( seg2x * seg2x + seg2y * seg2y );

			result = -Math.acos( dot / ( prevLen * nextLen ) );
		}
		else if ( normal > 0.001 )
		{
			final double dot     = seg1x * seg2x + seg1y * seg2y;
			final double prevLen = Math.sqrt( seg1x * seg1x + seg1y * seg1y );
			final double nextLen = Math.sqrt( seg2x * seg2x + seg2y * seg2y );

			result = Math.acos( dot / ( prevLen * nextLen ) );
		}
		else
		{
			result = 0.0;
		}

		return result;
	}

	/**
	 * This defines a point on a contour.
	 *
	 * @author  Peter S. Heijnen
	 * @version $Revision$ $Date$
	 */
	public static class Point
		extends Vector2D
	{
		/**
		 * Serialized data version.
		 */
		private static final long serialVersionUID = -2830278300930211460L;

		/**
		 * Angle between the two segments connected by this point.
		 */
		double angle;

		/**
		 * Construct point of contour.
		 *
		 * @param   x   X coordinate of point.
		 * @param   y   Y coordinate of point.
		 */
		public Point( final double x, final double y )
		{
			super( x, y );
		}

		/**
		 * Get angle in radians between the two segments connected by this
		 * point.
		 *
		 * @return  Angle in radians between the two segments connected by this
		 *          point.
		 */
		public double getAngle()
		{
			return angle;
		}

		/**
		 * Set angle in radians between the two segments connected by this
		 * point.
		 *
		 * @return  Angle in radians between the two segments connected by this
		 *          point.
		 */
		public void setAngle( final double angle )
		{
			this.angle = angle;
		}
	}
}
