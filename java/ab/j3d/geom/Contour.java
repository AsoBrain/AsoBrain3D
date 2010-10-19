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

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import ab.j3d.geom.ShapeTools.*;
import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This defines a contour. A contour is a single closed path whose segments
 * are defines by a set of points. The first point is the start point, the
 * second point defines the first segment, etc. The last point is
 * automatically connected to the start point.
 *
 * The main purpose of this class is providing intermediate analysis and storage
 * of 2D shape data. The {@link #createContours} and {@link #addContours}
 * methods can beused to convienently construct {@link Contour}s from Java 2D
 * shapes.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Contour
{
	/**
	 * Shape class of contour.
	 */
	final ShapeClass _shapeClass;

	/**
	 * Points that define the contour.
	 */
	final List<Point> _points;

	/**
	 * Create contour(s) from a flattened {@link Shape}.
	 *
	 * @param   shape               Shape whose contour(s) to create.
	 * @param   flatness            Flatness used for contours.
	 * @param   counterClockwise    Make all contours counter-clockwise.
	 * @param   keepOpenPaths       Keep open paths as contours.
	 *
	 * @return  Contour(s) that were created.
	 */
	@NotNull
	public static List<Contour> createContours( @NotNull final Shape shape, final double flatness, final boolean counterClockwise, final boolean keepOpenPaths )
	{
		final List<Contour> result = new ArrayList<Contour>();
		addContours( result, shape.getPathIterator( null, flatness ), counterClockwise, keepOpenPaths );
		return result;
	}

	/**
	 * Add contour(s) from a {@link PathIterator}. If <code>keepOpenPaths</code>
	 * is set to <code>true</code>, then open paths are also added, otherwise
	 * only closed paths will be retained. The closing segment of closed shapes
	 * is not included.
	 *
	 * @param   contours                Collection to store contours in.
	 * @param   pathIterator            Path iterator to create contour from.
	 * @param   makeCounterClockwise    Make all contours counter-clockwise.
	 * @param   keepOpenPaths           Keep open paths as contours.
	 */
	public static void addContours( @NotNull final Collection<Contour> contours, @NotNull final PathIterator pathIterator, final boolean makeCounterClockwise, final boolean keepOpenPaths )
	{
		final List<Point> points = new ArrayList<Point>();

		boolean positiveAngles = false; /* encountered positive angles */
		boolean negativeAngles = false; /* encountered negative angles */
		double totalAngle = 0.0; /* should finish at 2PI (counter-clockwise) or -2PI (clockwise) for closed non-self-intersecting shapes */
		boolean reverseTested = false;
		boolean reverseContour;

		final double[] coords = new double[ 6 ];

		for ( ; !pathIterator.isDone() ; pathIterator.next() )
		{
			final int segmentType = pathIterator.currentSegment( coords );

			switch ( segmentType )
			{
				case PathIterator.SEG_MOVETO :
				{
					if ( keepOpenPaths )
					{
						final int pointCount = points.size();
						if ( pointCount >= 2 )
						{
							final List<Point> vertices = new ArrayList<Point>( points );
							final ShapeClass shapeClass = ( pointCount == 2 ) ? ShapeClass.LINE_SEGMENT : ShapeClass.OPEN_PATH;
							contours.add( new Contour( shapeClass, vertices ) );
							points.clear();
						}
					}

					final double moveX = coords[ 0 ];
					final double moveY = coords[ 1 ];
					positiveAngles = false;
					negativeAngles = false;
					totalAngle = 0.0;

					points.add( new Point( moveX, moveY ) );
					break;
				}

				case PathIterator.SEG_LINETO :
				{
					final int pointIndex = points.size();
					if ( pointIndex == 0 )
					{
						throw new IllegalStateException( "Line segment before move encountered" );
					}

					final double nextX = coords[ 0 ];
					final double nextY = coords[ 1 ];

					final Point current = points.get( pointIndex - 1 );

					if ( !MathTools.almostEqual( nextX, current.x ) || !MathTools.almostEqual( nextY, current.y ) )
					{
						final Point next = new Point( nextX, nextY );
						points.add( next );

						if ( pointIndex >= 2 )
						{
							final Point previous = points.get( pointIndex - 2 );

							final double angle = ShapeTools.getAngle( current.x - previous.x, current.y - previous.y, next.x - current.x, next.y - current.y );
							current.angle = angle;
							positiveAngles |= ( angle > 0.0 );
							negativeAngles |= ( angle < 0.0 );
							totalAngle += angle;
						}
					}
					break;
				}

				case PathIterator.SEG_CLOSE :
					int pointCount = points.size();
					if ( pointCount > 2 )
					{
						final Point start = points.get( 0 );

						Point last = points.get( pointCount - 1 );
						if ( MathTools.almostEqual( start.x, last.x ) && MathTools.almostEqual( start.y, last.y ) )
						{
							last = points.remove( --pointCount );
						}
						else
						{
							final Point previous = points.get( pointCount - 2 );

							final double angle = ShapeTools.getAngle( last.x - previous.x, last.y - previous.y, start.x - last.x, start.y - last.y );
							last.angle = angle;
							positiveAngles |= ( angle > 0.0 );
							negativeAngles |= ( angle < 0.0 );
							totalAngle += angle;
						}

						if ( pointCount > 2 )
						{
							final Point firstEdge = points.get( 1 );

							final double angle = ShapeTools.getAngle( start.x - last.x, start.y - last.y, firstEdge.x - start.x, firstEdge.y - start.y );
							start.angle = angle;
							positiveAngles |= ( angle > 0.0 );
							negativeAngles |= ( angle < 0.0 );
							totalAngle += angle;

							boolean counterClockwise = ( totalAngle >= 0.0 );
							reverseContour = ( !reverseTested && makeCounterClockwise && !counterClockwise );
							reverseTested = true;

							final List<Point> vertices;

							if ( reverseContour )
							{
								vertices = new ArrayList<Point>();
								for ( int i = pointCount; --i >= 0; )
								{
									vertices.add( points.get( i ) );
								}
								counterClockwise = !counterClockwise;
							}
							else
							{
								vertices = new ArrayList<Point>( points );
							}

							final ShapeClass shapeClass = ( positiveAngles && negativeAngles ) ? counterClockwise ? ShapeClass.CCW_CONCAVE : ShapeClass.CW_CONCAVE :
							                              ( pointCount == 3 ) ? counterClockwise ? ShapeClass.CCW_TRIANGLE : ShapeClass.CW_TRIANGLE :
							                              ( pointCount == 4 ) ? counterClockwise ? ShapeClass.CCW_QUAD : ShapeClass.CW_QUAD :
							                              counterClockwise ? ShapeClass.CCW_CONVEX : ShapeClass.CW_CONVEX;

							contours.add( new Contour( shapeClass, vertices ) );
						}

						points.clear();
					}
					break;

				default :
					throw new AssertionError( segmentType + "?" );
			}
		}

		if ( keepOpenPaths )
		{
			final int pointCount = points.size();
			if ( pointCount >= 2 )
			{
				final List<Point> vertices = new ArrayList<Point>( points );
				final ShapeClass shapeClass = ( pointCount == 2 ) ? ShapeClass.LINE_SEGMENT : ShapeClass.OPEN_PATH;
				contours.add( new Contour( shapeClass, vertices ) );
				points.clear();
			}
		}
	}

	/**
	 * Construct contour.
	 *
	 * @param   shapeClass  Shape class of contour.
	 * @param   points      Points that define the contour.
	 */
	Contour( final ShapeClass shapeClass, final List<Point> points )
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
	 * This defines a point on a contour.
	 *
	 * @author  Peter S. Heijnen
	 * @version $Revision$ $Date$
	 */
	public static class Point
		extends Point2D.Double
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
		Point( final double x, final double y )
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
	}
}
