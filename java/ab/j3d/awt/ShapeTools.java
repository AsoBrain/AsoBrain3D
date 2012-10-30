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
package ab.j3d.awt;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.geom.tessellator.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * This class provides tools using the AsoBrain 3D toolkit with Java 2D shapes.
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
	 * Number format that has upto 2 decimals.
	 */
	public static final NumberFormat TOW_DECIMALS;

	static
	{
		final NumberFormat df = NumberFormat.getNumberInstance( Locale.US );
		df.setGroupingUsed( false );
		df.setMinimumFractionDigits( 0 );
		df.setMaximumFractionDigits( 2 );
		TOW_DECIMALS = df;
	}

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
		final List<Contour.Point> points = new ArrayList<Contour.Point>();

		boolean positiveAngles = false; /* encountered positive angles */
		boolean negativeAngles = false; /* encountered negative angles */
		double totalAngle = 0.0; /* should finish at 2PI (counter-clockwise) or -2PI (clockwise) for closed non-self-intersecting shapes */

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
							final List<Contour.Point> vertices = new ArrayList<Contour.Point>( points );
							final Contour.ShapeClass shapeClass = ( pointCount == 2 ) ? Contour.ShapeClass.LINE_SEGMENT : Contour.ShapeClass.OPEN_PATH;
							contours.add( new Contour( shapeClass, vertices ) );
						}
					}

					final double moveX = coords[ 0 ];
					final double moveY = coords[ 1 ];
					positiveAngles = false;
					negativeAngles = false;
					totalAngle = 0.0;

					points.clear();
					points.add( new Contour.Point( moveX, moveY ) );
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

					final Contour.Point current = points.get( pointIndex - 1 );

					if ( !MathTools.almostEqual( nextX, current.x ) || !MathTools.almostEqual( nextY, current.y ) )
					{
						final Contour.Point next = new Contour.Point( nextX, nextY );
						points.add( next );

						if ( pointIndex >= 2 )
						{
							final Contour.Point previous = points.get( pointIndex - 2 );

							final double angle = Contour.getAngle( current.x - previous.x, current.y - previous.y, next.x - current.x, next.y - current.y );
							current.setAngle( angle );
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
						final Contour.Point start = points.get( 0 );

						Contour.Point last = points.get( pointCount - 1 );
						if ( MathTools.almostEqual( start.x, last.x ) && MathTools.almostEqual( start.y, last.y ) )
						{
							last = points.remove( --pointCount );
						}
						else
						{
							final Contour.Point previous = points.get( pointCount - 2 );

							final double angle = Contour.getAngle( last.x - previous.x, last.y - previous.y, start.x - last.x, start.y - last.y );
							last.setAngle( angle );
							positiveAngles |= ( angle > 0.0 );
							negativeAngles |= ( angle < 0.0 );
							totalAngle += angle;
						}

						if ( pointCount > 2 )
						{
							final Contour.Point firstEdge = points.get( 1 );

							final double angle = Contour.getAngle( start.x - last.x, start.y - last.y, firstEdge.x - start.x, firstEdge.y - start.y );
							start.setAngle( angle );
							positiveAngles |= ( angle > 0.0 );
							negativeAngles |= ( angle < 0.0 );
							totalAngle += angle;

							boolean counterClockwise = ( totalAngle >= 0.0 );
							final boolean reverseContour = ( makeCounterClockwise && !counterClockwise );

							final List<Contour.Point> vertices;

							if ( reverseContour )
							{
								vertices = new ArrayList<Contour.Point>();
								for ( int i = pointCount; --i >= 0; )
								{
									vertices.add( points.get( i ) );
								}
								counterClockwise = !counterClockwise;
							}
							else
							{
								vertices = new ArrayList<Contour.Point>( points );
							}

							final Contour.ShapeClass shapeClass = ( positiveAngles && negativeAngles ) ? counterClockwise ? Contour.ShapeClass.CCW_CONCAVE : Contour.ShapeClass.CW_CONCAVE :
							                              ( pointCount == 3 ) ? counterClockwise ? Contour.ShapeClass.CCW_TRIANGLE : Contour.ShapeClass.CW_TRIANGLE :
							                              ( pointCount == 4 ) ? counterClockwise ? Contour.ShapeClass.CCW_QUAD : Contour.ShapeClass.CW_QUAD :
							                              counterClockwise ? Contour.ShapeClass.CCW_CONVEX : Contour.ShapeClass.CW_CONVEX;

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
				final List<Contour.Point> vertices = new ArrayList<Contour.Point>( points );
				final Contour.ShapeClass shapeClass = ( pointCount == 2 ) ? Contour.ShapeClass.LINE_SEGMENT : Contour.ShapeClass.OPEN_PATH;
				contours.add( new Contour( shapeClass, vertices ) );
				points.clear();
			}
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
	public static Shape flatten( final Shape shape, final double flatness )
	{
		if ( shape == null )
		{
			throw new IllegalArgumentException( "shape == null" );
		}

		final Shape result;

		if ( containsCurves( shape ) )
		{
			final PathIterator it = shape.getPathIterator( null, flatness );
			final Path2D.Double path = new Path2D.Double( it.getWindingRule() );
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
	public static boolean containsCurves( final Shape shape )
	{
		if ( shape == null )
		{
			throw new IllegalArgumentException( "shape == null" );
		}

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
	 * Create tessellator for a shape. Any curves in the shape are flattened
	 * using the given flatness.
	 *
	 * @param   shape       Shape to tessellate.
	 * @param   flatness    Maximum distance between line segments and the
	 *                      curves they approximate.
	 *
	 * @return  {@link Tessellator} for the shape.
	 */
	public static Tessellator createTessellator( final Shape shape, final double flatness )
	{
		return new Tessellator( createTessellatorMesh( shape, flatness ) );
	}

	/**
	 * Create a mesh to tessellate. Any curves in the shape are flattened using
	 * the given flatness.
	 *
	 * @param   shape       Shape to tessellate.
	 * @param   flatness    Maximum distance between line segments and the
	 *                      curves they approximate.
	 *
	 * @return  {@link Mesh} for the shape.
	 */
	private static Mesh createTessellatorMesh( @NotNull final Shape shape, final double flatness )
	{
		final PathIterator pathIterator = shape.getPathIterator( null, flatness );

		final Mesh.WindingRule windingRule;

		switch ( pathIterator.getWindingRule() )
		{
			case PathIterator.WIND_EVEN_ODD:
				windingRule = Mesh.WindingRule.ODD;
				break;

			case PathIterator.WIND_NON_ZERO:
				windingRule = Mesh.WindingRule.NONZERO;
				break;

			default:
				throw new AssertionError( "Illegal winding rule: " + pathIterator.getWindingRule() );
		}

		final Mesh result = new Mesh( windingRule );
		addPathToMesh( result, pathIterator );
		result.finish();

		return result;
	}

	/**
	 * Add contour(s) from a {@link PathIterator}.
	 *
	 * @param   mesh            Mesh to add contours to.
	 * @param   pathIterator    Path iterator to create contour from.
	 */
	private static void addPathToMesh( final Mesh mesh, @NotNull final PathIterator pathIterator )
	{
		final LinkedList<double[]> points = new LinkedList<double[]>();
		double[] cur = null;

		for ( ; !pathIterator.isDone() ; pathIterator.next() )
		{
			final double[] coords = new double[ 2 ];
			switch ( pathIterator.currentSegment( coords ) )
			{
				case PathIterator.SEG_MOVETO :
					points.clear();
					points.add( coords );
					cur = coords;
					break;

				case PathIterator.SEG_LINETO :
					if ( ( cur == null ) || !MathTools.almostEqual( coords[ 0 ], cur[ 0 ] ) || !MathTools.almostEqual( coords[ 1 ], cur[ 1 ] ) )
					{
						points.add( coords );
					}
					cur = coords;
					break;

				case PathIterator.SEG_CLOSE :
					if ( points.size() > 2 )
					{
						final double[] start = points.getFirst();

						final double[] last = points.getLast();
						if ( MathTools.almostEqual( start[ 0 ], last[ 0 ] ) && MathTools.almostEqual( start[ 1 ], last[ 1 ] ) )
						{
							points.removeLast();
						}

						if ( points.size() > 2 )
						{
							mesh.beginContour();

							for ( final double[] point : points )
							{
								mesh.addVertex( point[ 0 ], point[ 1 ] );
							}

							mesh.endContour();
						}

						points.clear();
						cur = start;
					}
					break;
			}
		}
	}

	/**
	 * Get shape class.
	 *
	 * @param   shape   Shape to classify.
	 *
	 * @return  {@link Contour.ShapeClass}
	 *
	 * @throws  IllegalArgumentException if parameter is <code>null</code>.
	 */
	public static Contour.ShapeClass getShapeClass( final Shape shape )
	{
		if ( shape == null )
		{
			throw new IllegalArgumentException( "shape == null" );
		}

		final Contour.ShapeClass result;

		if ( shape instanceof Rectangle2D )
		{
			final Rectangle2D rectangle = (Rectangle2D)shape;

			if ( ( rectangle.getWidth() <= 0.0 ) || ( rectangle.getHeight() <= 0.0 ) )
			{
				result = Contour.ShapeClass.VOID;
			}
			else
			{
				result = Contour.ShapeClass.CCW_QUAD;
			}
		}
		else if ( shape instanceof Line2D )
		{
			final Line2D line = (Line2D)shape;

			result = ( ( line.getX1() == line.getX2() ) && ( line.getY1() == line.getY2() ) ) ? Contour.ShapeClass.VOID :  Contour.ShapeClass.LINE_SEGMENT;
		}
		else if ( shape instanceof Arc2D )
		{
			final Arc2D arc = (Arc2D)shape;

			if ( ( arc.getWidth() <= 0.0 ) || ( arc.getHeight() <= 0.0 ) )
			{
				result = Contour.ShapeClass.VOID;
			}
			else if ( arc.getArcType() == Arc2D.OPEN )
			{
				result = Contour.ShapeClass.OPEN_PATH;
			}
			else
			{
				result = ( arc.getAngleExtent() > 0.0 ) ? Contour.ShapeClass.CW_CONVEX : Contour.ShapeClass.CCW_CONVEX;
			}
		}
		else if ( ( shape instanceof Ellipse2D ) || ( shape instanceof RoundRectangle2D ) )
		{
			final RectangularShape rectangle = (RectangularShape)shape;

			if ( ( rectangle.getWidth() <= 0.0 ) || ( rectangle.getHeight() <= 0.0 ) )
			{
				result = Contour.ShapeClass.VOID;
			}
			else
			{
				result = Contour.ShapeClass.CCW_CONVEX;
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
	 * @return  {@link Contour.ShapeClass}.
	 *
	 * @throws  IllegalArgumentException if parameter is <code>null</code>.
	 */
	public static Contour.ShapeClass getShapeClass( final PathIterator pathIterator )
	{
		final Contour.ShapeClass result;

		if ( pathIterator == null )
		{
			throw new IllegalArgumentException( "pathIterator == null" );
		}

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
							final double angle = Contour.getAngle( currentX - previousX, currentY - previousY, nextX - currentX, nextY - currentY );
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
					final double firstAngle = Contour.getAngle( moveX - currentX, moveY - currentY, firstX - moveX, firstY - moveY );
					totalAngle += firstAngle;
					positiveAngles |= ( firstAngle > 0.0 );
					negativeAngles |= ( firstAngle < 0.0 );

					if ( ( moveX != currentX ) || ( moveY != currentY ) )
					{
						final double lastAngle = Contour.getAngle( currentX - previousX, currentY - previousY, moveX - currentX, moveY - currentY );
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
			result = Contour.ShapeClass.VOID;
		}
		else if ( multipleSubPaths )
		{
			result = Contour.ShapeClass.COMPLEX;
		}
		else if ( numberOfSegments == 1 )
		{
			result = Contour.ShapeClass.LINE_SEGMENT;
		}
		else
		{
			final boolean closed = ( currentX == moveX ) && ( currentY == moveY );
			if ( !closed )
			{
				result = Contour.ShapeClass.OPEN_PATH;
			}
			else
			{
				if ( positiveAngles && negativeAngles )
				{
					result = ( totalAngle < 0.0 ) ? Contour.ShapeClass.CW_CONCAVE : Contour.ShapeClass.CCW_CONCAVE;
				}
				else if ( !curved && ( numberOfSegments == 3 ) )
				{
					result = negativeAngles ? Contour.ShapeClass.CW_TRIANGLE : Contour.ShapeClass.CCW_TRIANGLE;
				}
				else if ( !curved && ( numberOfSegments == 4 ) )
				{
					result = negativeAngles ? Contour.ShapeClass.CW_QUAD : Contour.ShapeClass.CCW_QUAD;
				}
				else
				{
					result = negativeAngles ? Contour.ShapeClass.CW_CONVEX : Contour.ShapeClass.CCW_CONVEX;
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
		appendArcCurves( path, start.getX(), start.getY(), end.getX(), end.getY(), angle );
	}

	/**
	 * This utility-method calculates an approximation of an arc segment or
	 * semicircle using quadratic B&eacute;zier curves and adds it to a
	 * {@link Path2D}.
	 *
	 * @param   path        Path to append arc to.
	 * @param   startX      Start X coordinate of arc.
	 * @param   startY      Start Y coordinate of arc.
	 * @param   endX        End X coordinate of arc.
	 * @param   endY        End Y coordinate of arc.
	 * @param   angle       Included angle in radians.
	 */
	public static void appendArcCurves( final Path2D path, final double startX, final double startY, final double endX, final double endY, final double angle )
	{
		final double chordDeltaX = endX - startX;
		final double chordDeltaY = endY - startY;
		final double chordMidX = ( startX + endX ) / 2.0;
		final double chordMidY = ( startY + endY ) / 2.0;
		final double chordLength = Math.sqrt( chordDeltaX * chordDeltaX + chordDeltaY * chordDeltaY );

		final double halfChordLength = chordLength / 2.0;
		final double signedArcHeight = halfChordLength * Math.tan( angle / 4.0 );
		final double signedRadius = ( halfChordLength * halfChordLength + signedArcHeight * signedArcHeight ) / ( 2.0 * signedArcHeight );
		final double radius = Math.abs( signedRadius );
		final double tanApothem = ( signedRadius - signedArcHeight ) / chordLength;
		final double centerX = chordMidX - chordDeltaY * tanApothem;
		final double centerY = chordMidY + chordDeltaX * tanApothem;

		appendArcCurves( path, centerX, centerY, radius, endX,  endY, angle );
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
		appendArcCurves( path, center.getX(), center.getY(), radius, end.getX(), end.getY(), extend );
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
	 * @param   centerX     Center of circle on which the arc is defined.
	 * @param   centerY     Center of circle on which the arc is defined.
	 * @param   radius      Radius of circle on which the arc is defined.
	 * @param   endX        End point of arc.
	 * @param   endY        End point of arc.
	 * @param   extend      Included angle of extend (in radians, may be negative).
	 */
	public static void appendArcCurves( final Path2D path, final double centerX, final double centerY, final double radius, final double endX, final double endY, final double extend  )
	{
		final int segmentCount = (int)Math.ceil( Math.abs( extend ) / HALF_PI );
		if ( segmentCount > 0 )
		{
			final double angleIncrement = extend / (double)segmentCount;

			final double bezierSegmentLength = 4.0 / 3.0 * Math.sin( angleIncrement / 2.0 ) / ( 1.0 + Math.cos( angleIncrement / 2.0  ) );
			if ( bezierSegmentLength != 0.0 )
			{
				double currentAngle = Math.atan2( endY - centerY, endX - centerX ) - extend;
				double cos1 = Math.cos( currentAngle ) * radius;
				double sin1 = Math.sin( currentAngle ) * radius;

				for ( int i = 0 ; i < segmentCount ; i++ )
				{
					currentAngle += angleIncrement;
					final double cos2 = Math.cos( currentAngle ) * radius;
					final double sin2 = Math.sin( currentAngle ) * radius;

					final double p1x = centerX + cos1 - bezierSegmentLength * sin1;
					final double p1y = centerY + sin1 + bezierSegmentLength * cos1;
					final double p2x = centerX + cos2 + bezierSegmentLength * sin2;
					final double p2y = centerY + sin2 - bezierSegmentLength * cos2;
					final double p3x = centerX + cos2;
					final double p3y = centerY + sin2;

					if ( i == segmentCount - 1 )
					{
						path.curveTo( p1x, p1y, p2x, p2y, endX, endY );
					}
					else
					{
						path.curveTo( p1x, p1y, p2x, p2y, p3x, p3y );
					}

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

		final NumberFormat df = TOW_DECIMALS;

		if ( shape instanceof Line2D )
		{
			final Line2D line = (Line2D)shape;
			result = "Line2D( p1:[" + df.format( line.getX1() ) +
			         ',' + df.format( line.getY1() ) +
			         "], p2:[" + df.format( line.getX2() ) +
			         ',' + df.format( line.getY2() ) + "] )";
		}
		else if ( shape instanceof Rectangle2D )
		{
			final Rectangle2D rectangle = (Rectangle2D)shape;
			result = "Rectangle2D( x:" + df.format( rectangle.getX() ) +
			         ", y:" + df.format( rectangle.getY() ) +
			         ", w:" + df.format( rectangle.getWidth() ) +
			         ", h:" + df.format( rectangle.getHeight() ) + " )";
		}
		else
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( shape );
			sb.append( " : " );

			final double[] coords = new double[ 6 ];
			boolean empty = true;

			for ( final PathIterator pathIterator = shape.getPathIterator( null ); !pathIterator.isDone() ; pathIterator.next() )
			{
				if ( empty )
				{
					sb.append( "path {" );
					empty = false;
				}
				else
				{
					sb.append( ", " );
				}

				switch ( pathIterator.currentSegment( coords ) )
				{
					case PathIterator.SEG_LINETO:
						sb.append( "LINE[" );
						sb.append( df.format( coords[ 0 ] ) );
						sb.append( ',' );
						sb.append( df.format( coords[ 1 ] ) );
						sb.append( ']' );
						break;

					case PathIterator.SEG_MOVETO:
						sb.append( "START[" );
						sb.append( df.format( coords[ 0 ] ) );
						sb.append( ',' );
						sb.append( df.format( coords[ 1 ] ) );
						sb.append( ']' );
						break;

					case PathIterator.SEG_CLOSE:
						sb.append( "CLOSE" );
						break;

					case PathIterator.SEG_QUADTO:
						sb.append( "QUAD[" );
						sb.append( df.format( coords[ 2 ] ) );
						sb.append( ',' );
						sb.append( df.format( coords[ 3 ] ) );
						sb.append( "](p1=[" );
						sb.append( df.format( coords[ 0 ] ) );
						sb.append( ',' );
						sb.append( df.format( coords[ 1 ] ) );
						sb.append( "])" );
						break;

					case PathIterator.SEG_CUBICTO:
						sb.append( "CUBIC[" );
						sb.append( df.format( coords[ 4 ] ) );
						sb.append( ',' );
						sb.append( df.format( coords[ 5 ] ) );
						sb.append( "](p1=[" );
						sb.append( df.format( coords[ 0 ] ) );
						sb.append( ',' );
						sb.append( df.format( coords[ 1 ] ) );
						sb.append( "],p2=[" );
						sb.append( df.format( coords[ 2 ] ) );
						sb.append( ',' );
						sb.append( df.format( coords[ 3 ] ) );
						sb.append( "])" );
						break;

					default :
						sb.append( "unknown segment" );
				}
			}

			if ( empty )
			{
				sb.append( "empty path" );
			}
			else
			{
				sb.append( " }" );
			}

			result = sb.toString();
		}

		return result;
	}

	/**
	 * Add extruded shape without caps.
	 *
	 * @param   objectBuilder   3D object builder.
	 * @param   shape           Shape to add.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   uvMap           Provides UV coordinates.
	 * @param   appearance      Appearance specification to use for shading.
	 * @param   flipTexture     Whether the side texture direction is flipped.
	 * @param   flatness        Flatness to use.
	 * @param   twoSided        Flag to indicate if extruded faces are two-sided.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   smooth          Shape is smooth.
	 *
	 * @throws  IllegalArgumentException if the shape is not extruded along
	 *          the z-axis.
	 */
	public static void addExtrudedShape( final Object3DBuilder objectBuilder, @NotNull final Shape shape, final double flatness, @NotNull final Vector3D extrusion, @NotNull final Matrix3D transform, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean twoSided, final boolean flipNormals, final boolean smooth )
	{
		final List<Contour> contours = createContours( shape, flatness, true, true );
		objectBuilder.addExtrudedShape( contours, extrusion, transform, appearance, uvMap, flipTexture, twoSided, flipNormals, smooth );
	}

	/**
	 * Add filled tessellated shape.
	 *
	 * @param   objectBuilder   3D object builder.
	 * @param   transform       Transforms 2D to 3D coordinates.
	 * @param   shape           Shape to add.
	 * @param   flatness        Flatness used to approximate curves.
	 * @param   appearance      Appearance specification to use for shading.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   flipTexture     Whether the bottom texture direction is flipped.
	 * @param   flipNormals     Flip normals using CW instead of CCW triangles.
	 * @param   twoSided        Resulting face will be two-sided (has backface).
	 */
	public static void addFilledShape2D( final Object3DBuilder objectBuilder, @NotNull final Matrix3D transform, @NotNull final Shape shape, final double flatness, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean flipNormals, final boolean twoSided )
	{
		objectBuilder.addFilledShape2D( transform, createTessellator( shape, flatness ), appearance, uvMap, flipTexture, flipNormals, twoSided );
	}

	/**
	 * Add text.
	 *
	 * @param   objectBuilder       3D object builder.
	 * @param   transform           Transforms text to the object being build.
	 * @param   text                Text to add.
	 * @param   font                Font to use.
	 * @param   scale               To apply to the font.
	 * @param   alignX              X alignment (0=left, 0.5=left, 1=right).
	 * @param   alignY              Y alignment (0=baseline, 0.5=center, 1=ascent).
	 * @param   alignZ              Z alignment (0=below, 0.5=center, 1=on top).
	 * @param   extrusion           Extrusion along Z-axis.
	 * @param   flatness            Flatness to use for curves.
	 * @param   topAppearance       Appearance for top surface.
	 * @param   topMap              UV map for top surface.
	 * @param   bottomAppearance    Appearance for bottom surface.
	 * @param   bottomMap           UV map for bottom surface.
	 * @param   sideAppearance      Appearance for side surfaces.
	 * @param   sideMap             UV map for side surfaces.
	 */
	public static void addText( final Object3DBuilder objectBuilder, final Matrix3D transform, final String text, final Font font, final double scale, final double alignX, final double alignY, final double alignZ, final double extrusion, final double flatness, final Appearance topAppearance, final BoxUVMap topMap, final Appearance bottomAppearance, final BoxUVMap bottomMap, final Appearance sideAppearance, final BoxUVMap sideMap )
	{
		final GlyphVector glyphVector = font.createGlyphVector( new FontRenderContext( null, false, true ), text );
		final Rectangle2D visualBounds = glyphVector.getVisualBounds();

		final double x = -visualBounds.getMinX() - alignX * visualBounds.getWidth();
		final double y = visualBounds.getMaxY() - alignY * visualBounds.getHeight();
		final double z = -alignZ * extrusion;

		final Matrix3D glyphTransform = Matrix3D.multiply( scale,  0.0,  0.0, scale * x, 0.0, -scale,  0.0, scale * y, 0.0,  0.0, -scale, scale * z, transform );
		final Vector3D extrusionVector = new Vector3D( 0.0, 0.0, -extrusion );
		final Tessellator outlineTessellator = createTessellator( glyphVector.getOutline(), flatness );
		objectBuilder.addExtrudedShape( outlineTessellator, extrusionVector, true, glyphTransform, true, topAppearance, topMap, false, true, bottomAppearance, bottomMap, false, true, sideAppearance, sideMap, false, false, false, true );
		}
}
