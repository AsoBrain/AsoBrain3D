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
import java.text.*;
import java.util.*;

import com.numdata.oss.*;
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
	 * PI / 2.
	 */
	public static final double HALF_PI = Math.PI / 2.0;

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
	public static Shape flatten( @NotNull final Shape shape, final double flatness )
	{
		final Shape result;

		if ( containsCurves( shape ) )
		{
			final PathIterator it = shape.getPathIterator( null, flatness );
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
					moveX = coords[ 0 ];
					moveY = coords[ 1 ];
					previousX = moveX;
					previousY = moveY;
					currentX = moveX;
					currentY = moveY;
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
	 * This utility-method calculates an approximation of an arc segment or
	 * semicircle using quadratic B&eacute;zier curves and adds it to a
	 * {@link Path2D}.
	 *
	 * @param   path        Path to append arc to.
	 * @param   start       Start point of arc.
	 * @param   end         End point of arc.
	 * @param   angle       Included angle in radians.
	 */
	public static void appendArcCurves( final Path2D path, final Point2D start, final Point2D end, final double angle )
	{
		final double x1 = start.getX();
		final double y1 = start.getY();
		final double x2 = end.getX();
		final double y2 = end.getY();

		final double chordDeltaX = x2 - x1;
		final double chordDeltaY = y2 - y1;
		final double chordMidX = ( x1 + x2 ) / 2.0;
		final double chordMidY = ( y1 + y2 ) / 2.0;
		final double chordLength = Math.sqrt( chordDeltaX * chordDeltaX + chordDeltaY * chordDeltaY );

		final double halfChordLength = chordLength / 2.0;
		final double signedArcHeight = halfChordLength * Math.tan( angle / 4.0 );
		final double signedRadius = ( halfChordLength * halfChordLength + signedArcHeight * signedArcHeight ) / ( 2.0 * signedArcHeight );
		final double radius = Math.abs( signedRadius );
		final double tanApothem = ( signedRadius - signedArcHeight ) / chordLength;
		final Point2D center = new Point2D.Double( chordMidX - chordDeltaY * tanApothem, chordMidY + chordDeltaX * tanApothem );

		appendArcCurves( path, center, radius, end, angle );
	}

	/**
	 * This utility-method calculates an approximation of an arc segment or
	 * semicircle using quadratic B&eacute;zier curves and adds it to a
	 * {@link Path2D}.
	 * <p />
	 * The implementation is based on {@link ArcIterator} (most
	 * magic is in {@link ArcIterator#btan}), and information that
	 * was found at  <a href='http://www.afralisp.com/lisp/Bulges1.htm'>http://www.afralisp.com/lisp/Bulges1.htm</a>.
	 *
	 * @param   path        Path to append arc to.
	 * @param   center      Center of circle on which the arc is defined.
	 * @param   radius      Radius of circle on which the arc is defined.
	 * @param   end         End point of arc.
	 * @param   extend      Included angle of extend (in radians, may be negative).
	 */
	public static void appendArcCurves( final Path2D path, final Point2D center, final double radius, final Point2D end, final double extend  )
	{
		final int segmentCount = (int)Math.ceil( Math.abs( extend ) / HALF_PI );
		if ( segmentCount > 0 )
		{
			final double angleIncrement = extend / (double)segmentCount;

			final double bezierSegmentLength = 4.0 / 3.0 * Math.sin( angleIncrement / 2.0 ) / ( 1.0 + Math.cos( angleIncrement / 2.0  ) );
			if ( bezierSegmentLength != 0.0 )
			{
				double currentAngle = Math.atan2( end.getY() - center.getY(), end.getX() - center.getX() ) - extend;
				double cos1 = Math.cos( currentAngle ) * radius;
				double sin1 = Math.sin( currentAngle ) * radius;

				for ( int i = 0 ; i < segmentCount ; i++ )
				{
					currentAngle += angleIncrement;
					final double cos2 = Math.cos( currentAngle ) * radius;
					final double sin2 = Math.sin( currentAngle ) * radius;

					final double p1x = center.getX() + cos1 - bezierSegmentLength * sin1;
					final double p1y = center.getY() + sin1 + bezierSegmentLength * cos1;
					final double p2x = center.getX() + cos2 + bezierSegmentLength * sin2;
					final double p2y = center.getY() + sin2 - bezierSegmentLength * cos2;
					final double p3x = center.getX() + cos2;
					final double p3y = center.getY() + sin2;

					path.curveTo( p1x, p1y, p2x, p2y, p3x, p3y );

					cos1 = cos2;
					sin1 = sin2;
				}
			}
		}
	}

	/**
	 * Convert {@link Shape} to a friendly but short string format.
	 *
	 * @param   shape   {@link Shape} to convert.
	 *
	 * @return  Friendly but short string.
	 */
	public static String toShortFriendlyString( final Shape shape )
	{
		final String result;

		final PathIterator pathIterator = shape.getPathIterator( null );

		if ( pathIterator.isDone() )
		{
			result = "empty path";
		}
		else
		{
			final StringBuilder out = new StringBuilder();

			final NumberFormat df = TextTools.getNumberFormat( Locale.US, 0, 2, false );

			final double[] coords = new double[ 6 ];

			for ( ; !pathIterator.isDone() ; pathIterator.next() )
			{
				if ( out.length() == 0 )
				{
					out.append( "path { " );
				}
				else
				{
					out.append( ", " );
				}

				switch ( pathIterator.currentSegment( coords ) )
				{
					case PathIterator.SEG_LINETO:
						out.append( "LINE[" );
						out.append( df.format( coords[ 0 ] ) );
						out.append( ',' );
						out.append( df.format( coords[ 1 ] ) );
						out.append( ']' );
						break;

					case PathIterator.SEG_MOVETO:
						out.append( "START[" );
						out.append( df.format( coords[ 0 ] ) );
						out.append( ',' );
						out.append( df.format( coords[ 1 ] ) );
						out.append( ']' );
						break;

					case PathIterator.SEG_CLOSE:
						out.append( "CLOSE" );
						break;

					case PathIterator.SEG_QUADTO:
						out.append( "QUAD[" );
						out.append( df.format( coords[ 2 ] ) );
						out.append( ',' );
						out.append( df.format( coords[ 3 ] ) );
						out.append( "](p1=[" );
						out.append( df.format( coords[ 0 ] ) );
						out.append( ',' );
						out.append( df.format( coords[ 1 ] ) );
						out.append( "])" );
						break;

					case PathIterator.SEG_CUBICTO:
						out.append( "CUBIC[" );
						out.append( df.format( coords[ 4 ] ) );
						out.append( ',' );
						out.append( df.format( coords[ 5 ] ) );
						out.append( "](p1=[" );
						out.append( df.format( coords[ 0 ] ) );
						out.append( ',' );
						out.append( df.format( coords[ 1 ] ) );
						out.append( "],p2=[" );
						out.append( df.format( coords[ 2 ] ) );
						out.append( ',' );
						out.append( df.format( coords[ 3 ] ) );
						out.append( "])" );
						break;

					default :
						out.append( "unknown segment" );
				}
			}

			out.append( " }" );

			result = out.toString();
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
	static double getAngle( final double seg1x, final double seg1y, final double seg2x, final double seg2y )
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