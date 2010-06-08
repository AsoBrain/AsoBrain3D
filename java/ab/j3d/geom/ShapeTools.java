/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2010
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

import java.awt.*;
import java.awt.geom.*;

import org.jetbrains.annotations.*;

/**
 * Provides some utilities for working with 2D shapes.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ShapeTools
{
	/**
	 * Shape classes.
	 */
	public enum ShapeClass
	{
		/**
		 * Nothingness.
		 */
		VOID ,

		/**
		 * Line segment.
		 */
		LINE_SEGMENT ,

		/**
		 * Open path.
		 */
		OPEN_PATH ,

		/**
		 * Triangle with clockwise vertex order.
		 */
		CW_TRIANGLE ,

		/**
		 * Triangle with counter-clockwise vertex order.
		 */
		CCW_TRIANGLE ,

		/**
		 * Quad with clockwise vertex order.
		 */
		CW_QUAD ,

		/**
		 * Quad with counter-clockwise vertex order.
		 */
		CCW_QUAD ,

		/**
		 * Convex shape with clockwise vertex order.
		 */
		CW_CONVEX ,

		/**
		 * Convex shape with counter-clockwise vertex order.
		 */
		CCW_CONVEX ,

		/**
		 * Concave shape with clockwise vertex order.
		 */
		CW_CONCAVE ,

		/**
		 * Concave shape with counter-clockwise vertex order.
		 */
		CCW_CONCAVE ,

		/**
		 * Complex shape with possible self-intersection or multiple sub-paths.
		 */
		COMPLEX;

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
	 * Flatten shape.
	 *
	 * @param   shape       {@link Shape} to flatten.
	 * @param   flatness    Flatness to apply (<code>-1</code> for normal path).
	 *
	 * @return  <code>true</code> if the specified <code>shape</code> contains
	 *          at least one curve (potentially, that is).
	 *
	 * @throws  NullPointerException if a parameter is <code>null</code>.
	 */
	public static Shape flatten( @NotNull final Shape shape , final double flatness )
	{
		final Shape result;

		if ( containsCurves( shape ) )
		{
			final PathIterator it = shape.getPathIterator( null , flatness );
			final Path2D.Float path = new Path2D.Float( it.getWindingRule() );
			path.append( it, false );
			result = path;
		}
		else
		{
			result = shape;
		}

		return result;
	}

	/**
	 * Determine if {@link Shape} contains any curves.
	 *
	 * @param   shape   {@link Shape} to test.
	 *
	 * @return  <code>true</code> if the specified <code>shape</code> contains
	 *          at least one curve (potentially, that is).
	 *
	 * @throws  NullPointerException if a parameter is <code>null</code>.
	 */
	public static boolean containsCurves( @NotNull final Shape shape )
	{
		boolean result;

		if ( ( shape instanceof Line2D ) ||
		     ( shape instanceof Polygon ) ||
		     ( shape instanceof Rectangle2D ) )
		{
			result = false;
		}
		else if ( ( shape instanceof Arc2D ) ||
		          ( shape instanceof CubicCurve2D ) ||
		          ( shape instanceof Ellipse2D ) ||
		          ( shape instanceof QuadCurve2D ) ||
		          ( shape instanceof RoundRectangle2D ) )
		{
			result = true;
		}
		else
		{
			result = false;

			final float[] coords = new float[ 6 ];
			 for ( PathIterator i = shape.getPathIterator( null ) ; !result && !i.isDone() ; i.next() )
			{
				switch ( i.currentSegment( coords ) )
				{
					case PathIterator.SEG_CUBICTO :
					case PathIterator.SEG_QUADTO :
						result = true;
				}
			}
		}

		return result;
	}

	/**
	 * Get shape class.
	 *
	 * @param   shape   Shape to classify.
	 *
	 * @return  {@link ShapeClass}
	 *
	 * @throws  NullPointerException if a parameter is <code>null</code>.
	 */
	@NotNull
	public static ShapeClass getShapeClass( @NotNull final Shape shape )
	{
		final ShapeClass result;

		if ( shape instanceof Rectangle2D )
		{
			final Rectangle2D rectangle = (Rectangle2D)shape;

			if ( ( rectangle.getWidth() <= 0.0 ) || ( rectangle.getHeight() <= 0.0 ) )
			{
				result = ShapeClass.VOID;
			}
			else
			{
				result = ShapeClass.CCW_QUAD;
			}
		}
		else if ( shape instanceof Line2D )
		{
			final Line2D line = (Line2D)shape;

			result = ( ( line.getX1() == line.getX2() ) && ( line.getY1() == line.getY2() ) ) ? ShapeClass.VOID :  ShapeClass.LINE_SEGMENT;
		}
		else if ( shape instanceof Arc2D )
		{
			final Arc2D arc = (Arc2D)shape;

			if ( ( arc.getWidth() <= 0.0 ) || ( arc.getHeight() <= 0.0 ) )
			{
				result = ShapeClass.VOID;
			}
			else if ( arc.getArcType() == Arc2D.OPEN )
			{
				result = ShapeClass.OPEN_PATH;
			}
			else
			{
				result = ( arc.getAngleExtent() > 0.0 ) ? ShapeClass.CW_CONVEX : ShapeClass.CCW_CONVEX;
			}
		}
		else if ( ( shape instanceof Ellipse2D ) || ( shape instanceof RoundRectangle2D ) )
		{
			final RectangularShape rectangle = (RectangularShape)shape;

			if ( ( rectangle.getWidth() <= 0.0 ) || ( rectangle.getHeight() <= 0.0 ) )
			{
				result = ShapeClass.VOID;
			}
			else
			{
				result = ShapeClass.CCW_CONVEX;
			}
		}
		else
		{
			result = getShapeClass( shape.getPathIterator( null ) );
		}

		return result;
	}

	/**
	 * Get shape class from {@link PathIterator}. The iterator should be at
	 * the initial position in order to get a useful classification.
	 *
	 * @param   pathIterator    {@link PathIterator} to use.
	 *
	 * @return  {@link ShapeClass}.
	 *
	 * @throws  NullPointerException if any parameter is <code>null</code>.
	 */
	@NotNull
	public static ShapeClass getShapeClass( @NotNull final PathIterator pathIterator )
	{
		final ShapeClass result;

		int     numberOfSegments = 0;     /* number of non-void segments */
		boolean curved           = false;
		boolean positiveAngles   = false; /* encountered positive angles */
		boolean negativeAngles   = false; /* encountered negative angles */
		boolean multipleSubPaths = false; /* encountered multiple subpaths */

		final double[] coords = new double[ 6 ];

		double moveX = 0.0;
		double moveY = 0.0;
		double previousX = 0.0;
		double previousY = 0.0;
		double currentX = 0.0;
		double currentY = 0.0;
		boolean needFirst = true;
		double firstX = 0.0;
		double firstY = 0.0;
		double totalAngle = 0.0; /* should finish at 2PI (counter-clockwise) or -2PI (clockwise) for closed non-self-intersecting shapes */

		for ( ; !multipleSubPaths && !pathIterator.isDone() ; pathIterator.next() )
		{
			final int segmentType = pathIterator.currentSegment( coords );
			int numCoords = 2;

			switch ( segmentType )
			{
				case PathIterator.SEG_MOVETO :
					multipleSubPaths = ( numberOfSegments > 0 );
					moveX = previousX = currentX = coords[ 0 ];
					moveY = previousY = currentY = coords[ 1 ];
					needFirst = true;
					break;

				case PathIterator.SEG_CUBICTO :
					numCoords += 2;
					//noinspection fallthrough
				case PathIterator.SEG_QUADTO :
					numCoords += 2;
					curved = true;
					//noinspection fallthrough
				case PathIterator.SEG_LINETO :
					for ( int i = 0 ; i < numCoords ; i += 2 )
					{
						final double nextX = coords[ i ];
						final double nextY = coords[ i + 1 ];

						if ( ( nextX != currentX ) || ( nextY != currentY ) )
						{
							final double angle = getAngle( currentX - previousX, currentY - previousY, nextX - currentX, nextY - currentY );
							totalAngle += angle;
							positiveAngles |= ( angle > 0.0 );
							negativeAngles |= ( angle < 0.0 );

							if ( needFirst )
							{
								firstX = nextX;
								firstY = nextY;
								needFirst = false;
							}

							previousX = currentX;
							previousY = currentY;
							currentX = nextX;
							currentY = nextY;
							numberOfSegments++;
						}
					}
					break;

				case PathIterator.SEG_CLOSE :
					final double firstAngle = getAngle( moveX - currentX, moveY - currentY, firstX - moveX, firstY - moveY );
					totalAngle += firstAngle;
					positiveAngles |= ( firstAngle > 0.0 );
					negativeAngles |= ( firstAngle < 0.0 );

					if ( ( moveX != currentX ) || ( moveY != currentY ) )
					{
						final double lastAngle = getAngle( currentX - previousX, currentY - previousY, moveX - currentX, moveY - currentY );
						totalAngle += lastAngle;
						positiveAngles |= ( lastAngle > 0.0 );
						negativeAngles |= ( lastAngle < 0.0 );

						previousX = currentX;
						previousY = currentY;
						currentX = moveX;
						currentY = moveY;
						numberOfSegments++;
					}
					break;

				default :
					throw new AssertionError( segmentType + "?" );
			}
		}

		if ( numberOfSegments == 0 )
		{
			result = ShapeClass.VOID;
		}
		else if ( multipleSubPaths )
		{
			result = ShapeClass.COMPLEX;
		}
		else if ( numberOfSegments == 1 )
		{
			result = ShapeClass.LINE_SEGMENT;
		}
		else
		{
			final boolean closed = ( currentX == moveX ) && ( currentY == moveY );
			if ( !closed )
			{
				result = ShapeClass.OPEN_PATH;
			}
			else
			{
				if ( positiveAngles && negativeAngles )
				{
					result = ( totalAngle < 0.0 ) ? ShapeClass.CW_CONCAVE : ShapeClass.CCW_CONCAVE;
				}
				else if ( !curved && ( numberOfSegments == 3 ) )
				{
					result = negativeAngles ? ShapeClass.CW_TRIANGLE : ShapeClass.CCW_TRIANGLE;
				}
				else if ( !curved && ( numberOfSegments == 4 ) )
				{
					result = negativeAngles ? ShapeClass.CW_QUAD : ShapeClass.CCW_QUAD;
				}
				else
				{
					result = negativeAngles ? ShapeClass.CW_CONVEX : ShapeClass.CCW_CONVEX;
				}
			}
		}

		return result;
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
	private static double getAngle( final double seg1x, final double seg1y, final double seg2x, final double seg2y )
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
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private ShapeTools()
	{
	}
}