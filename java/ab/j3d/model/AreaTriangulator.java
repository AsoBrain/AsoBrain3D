/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2008
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

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

import com.numdata.oss.MathTools;

/**
 * Triangular that uses Java2D's {@link Area} class in combination with a simple
 * and rather brute-force method. By subdividing the shapes to be triangulated
 * (using the Area class), reasonable performance can be achieved.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
class AreaTriangulator
	implements Triangulator
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = AreaTriangulator.class.getName();

	/**
	 * Indicates whether the normals of created triangles should be flipped.
	 */
	private boolean _normalsFlipped;

	/**
	 * Initial number of horizontal subdivisions.
	 */
	private int _initialSubdivisionsX;

	/**
	 * Initial number of vertical subdivisions.
	 */
	private int _initialSubdivisionsY;

	/**
	 * Number of additional horizontal subdivisions per iteration.
	 */
	private int _iteratedSubdivisionsX;

	/**
	 * Number of additional vertical subdivisions per iteration.
	 */
	private int _iteratedSubdivisionsY;

	/**
	 * Maximum number of subdivision iterations to be performed.
	 */
	private int _subdivisionIterations;

	/**
	 * Maximum allowable distance between the control points and the flattened
	 * curve, used when performing shape interpolation with a
	 * {@link FlatteningPathIterator}.
	 */
	private double _flatness;

	/**
	 * Construct new area triangulator.
	 */
	AreaTriangulator()
	{
		_normalsFlipped        = false;
		_initialSubdivisionsX  = 1;
		_initialSubdivisionsY  = 1;
		_iteratedSubdivisionsX = 1;
		_iteratedSubdivisionsY = 1;
		_subdivisionIterations = 0;
		_flatness              = 1.0;
	}

	public Vector3D getNormal()
	{
		return _normalsFlipped ? Vector3D.INIT.set( 0.0 , 0.0 , -1.0 ) : Vector3D.INIT.set( 0.0 , 0.0 , 1.0 );
	}

	public void setNormal( final Vector3D normal )
	{
		_normalsFlipped = ( normal.z < 0.0 );
	}

	/**
	 * Returns whether normals of created triangles should be flipped.
	 *
	 * @return  <code>true</code> if normals are flipped;
	 *          <code>false</code> otherwise.
	 */
	public boolean isNormalsFlipped()
	{
		return _normalsFlipped;
	}

	/**
	 * Sets whether normals of created triangles should be flipped.
	 *
	 * @param   normalsFlipped  <code>true</code> to flip normals;
	 *                          <code>false</code> otherwise.
	 */
	public void setNormalsFlipped( final boolean normalsFlipped )
	{
		_normalsFlipped = normalsFlipped;
	}

	static List<Area> subdivide( final Area area , final int initialSubdivisionsX , final int initialSubdivisionsY , final int subdivisionsX , final int subdivisionsY , final int iterations )
	{
		if ( area == null )
			throw new NullPointerException( "area" );
		if ( subdivisionsX <= 0 )
			throw new IllegalArgumentException( "subdivisionsX: " + subdivisionsX );
		if ( subdivisionsY <= 0 )
			throw new IllegalArgumentException( "subdivisionsY: " + subdivisionsY );

		final List<Area> result = new ArrayList<Area>( subdivisionsX * subdivisionsY );

		if ( area.isRectangular() )
		{
			result.add( area );
		}
		else
		{
			subdivideImpl( area , initialSubdivisionsX , initialSubdivisionsY , subdivisionsX , subdivisionsY , iterations , result );
		}

		return result;
	}

	private static void subdivideImpl( final Area area , final int subdivisionsX , final int subdivisionsY , final int nextSubdivisionsX , final int nextSubdivisionsY , final int remainingIterations , final List<Area> result )
	{
		final Rectangle2D bounds = area.getBounds2D();
		final double      minX   = bounds.getMinX();
		final double      minY   = bounds.getMinY();
		final double      width  = bounds.getWidth();
		final double      height = bounds.getHeight();

		Area rectangularAreas = null;

		for ( int x = 0 ; x < subdivisionsX ; x++ )
		{
			final double subdivisionX1 = minX + width * (double)  x       / (double)subdivisionsX;
			final double subdivisionX2 = minX + width * (double)( x + 1 ) / (double)subdivisionsX;
			final double subdivisionWidth = subdivisionX2 - subdivisionX1;

			for ( int y = 0 ; y < subdivisionsY ; y++ )
			{
				final double subdivisionY1 = minY + height * (double)  y       / (double)subdivisionsY;
				final double subdivisionY2 = minY + height * (double)( y + 1 ) / (double)subdivisionsY;
				final double subdivisionHeight = subdivisionY2 - subdivisionY1;

				final Area subdivision = new Area( new Rectangle2D.Double( subdivisionX1 , subdivisionY1 , subdivisionWidth , subdivisionHeight ) );
				subdivision.intersect( area );

				if ( !subdivision.isEmpty() )
				{
					if ( subdivision.isRectangular() )
					{
						if ( rectangularAreas == null )
						{
							rectangularAreas = subdivision;
						}
						else
						{
							rectangularAreas.add( subdivision );
						}
					}
					else
					{
						if ( remainingIterations > 0 )
						{
							subdivideImpl( subdivision , nextSubdivisionsX , nextSubdivisionsY , nextSubdivisionsX , nextSubdivisionsY , remainingIterations - 1 , result );
						}
						else
						{
							result.add( subdivision );
						}
					}
				}
			}
		}

		if ( rectangularAreas != null )
		{
			result.add( rectangularAreas );
		}
	}

	/**
	 * Returns the triangulation of the given area as a list of shapes, one for
	 * each triangle.
	 *
	 * @param   area    Area to be triangulated.
	 *
	 * @return  Result of the triangulation as a list of triangular shapes.
	 */
	public List<Shape> triangulateToShapes( final Area area )
	{
		final Triangulation triangulation = triangulate( area );

		final Collection<int[]> triangles = triangulation.getTriangles();
		final List<double[]>    points    = ( (TriangulationImpl)triangulation ).getPoints();
		final List<Shape>       result    = new ArrayList<Shape>( triangles.size() );

		for ( final int[] triangle : triangles )
		{
			final double[] p1 = points.get( triangle[ 0 ] );
			final double[] p2 = points.get( triangle[ 1 ] );
			final double[] p3 = points.get( triangle[ 2 ] );

			final GeneralPath triangleShape = new GeneralPath();
			triangleShape.moveTo( p1[ 0 ] , p1[ 1 ] );
			triangleShape.lineTo( p2[ 0 ] , p2[ 1 ] );
			triangleShape.lineTo( p3[ 0 ] , p3[ 1 ] );
			triangleShape.closePath();

			result.add( triangleShape );
		}

		return result;
	}

	/**
	 * Sets the initial number of horizontal subdivions.
	 *
	 * @param   initialSubdivisionsX    Number of horizontal subdivions
	 */
	public void setInitialSubdivisionsX( final int initialSubdivisionsX )
	{
		_initialSubdivisionsX = initialSubdivisionsX;
	}

	/**
	 * Sets the initial number of vertical subdivions.
	 *
	 * @param   initialSubdivisionsY    Number of vertical subdivions
	 */
	public void setInitialSubdivisionsY( final int initialSubdivisionsY )
	{
		_initialSubdivisionsY = initialSubdivisionsY;
	}

	/**
	 * Sets the number of additional horizontal subdivions made for each
	 * subdivision iteration.
	 *
	 * @param   iteratedSubdivisionsX   Number of horizontal subdivions for each
	 *                                  iteration.
	 */
	public void setIteratedSubdivisionsX( final int iteratedSubdivisionsX )
	{
		_iteratedSubdivisionsX = iteratedSubdivisionsX;
	}

	/**
	 * Sets the number of additional vertical subdivions made for each
	 * subdivision iteration.
	 *
	 * @param   iteratedSubdivisionsY   Number of vertical subdivions for each
	 *                                  iteration.
	 */
	public void setIteratedSubdivisionsY( final int iteratedSubdivisionsY )
	{
		_iteratedSubdivisionsY = iteratedSubdivisionsY;
	}

	/**
	 * Sets the maximum number of subdivision iterations to be performed.
	 *
	 * @param   subdivisionIterations   Maximum number of iterations.
	 */
	public void setSubdivisionIterations( final int subdivisionIterations )
	{
		_subdivisionIterations = subdivisionIterations;
	}

	/**
	 * Returns the maximum allowable distance between the control points and the
	 * flattened curve, used when performing shape interpolation with a
	 * {@link FlatteningPathIterator}.
	 *
	 * @return  Flatness of interpolated shapes.
	 */
	public double getFlatness()
	{
		return _flatness;
	}

	/**
	 * Sets the maximum allowable distance between the control points and the
	 * flattened curve, used when performing shape interpolation with a
	 * {@link FlatteningPathIterator}.
	 *
	 * @param   flatness    Flatness to be set.
	 */
	public void setFlatness( final double flatness )
	{
		_flatness = flatness;
	}

	private Triangulation triangulate( final List<Area> areas )
	{
		//System.out.println( CLASS_NAME + ".triangulate" );
		final long start = System.nanoTime();

		final List<Triangulation> triangulations = new ArrayList<Triangulation>( areas.size() );

		int pointCount    = 0;
		int triangleCount = 0;

		for ( final Area area : areas )
		{
			final Triangulation triangulation = triangulateImpl( area );
			triangulations.add( triangulation );

			final List<double[]>    points    = ( (TriangulationImpl)triangulation ).getPoints();
			final Collection<int[]> triangles = triangulation.getTriangles();

			pointCount    += points.size();
			triangleCount += triangles.size();
		}

		final List<double[]> points    = new ArrayList<double[]>( pointCount    );
		final List<int[]>    triangles = new ArrayList<int[]>   ( triangleCount );

		for ( final Triangulation triangulation : triangulations )
		{
			final int firstIndex = points.size();
			points.addAll( ( (TriangulationImpl)triangulation ).getPoints() );

			final Collection<int[]> areaTriangles = triangulation.getTriangles();

			if ( firstIndex == 0 )
			{
				triangles.addAll( areaTriangles );
			}
			else
			{
				for ( final int[] triangle : areaTriangles )
				{
					triangles.add( new int[]
						{
							triangle[ 0 ] + firstIndex ,
							triangle[ 1 ] + firstIndex ,
							triangle[ 2 ] + firstIndex
						} );
				}
			}
		}

		final TriangulationImpl result = new TriangulationImpl( points , triangles );

		final long end = System.nanoTime();
		//System.out.println( " - " + result._points.size() + " vertices, " + result._triangles.size() + " triangles in " + ( (double)( ( end - start ) / 100000L ) / 10.0 ) + " ms" );

		return result;
	}

	/**
	 * Triangulates the given shape and returns the result.
	 *
	 * @param   shape   Shape to be triangulated.
	 *
	 * @return  Triangulation result.
	 */
	public Triangulation triangulate( final Shape shape )
	{
		final Area area;

		if ( shape instanceof Area )
		{
			area = (Area)shape;
		}
		else
		{
			area = new Area( shape );
		}

		return triangulate( subdivide( area , _initialSubdivisionsX , _initialSubdivisionsY , _iteratedSubdivisionsX , _iteratedSubdivisionsY , _subdivisionIterations ) );
	}

	private Triangulation triangulateImpl( final Area area )
	{
		/*
		 * Find points and lines in the shape of the area.
		 */
		final GeneralPath flattenedShape = new GeneralPath();
		flattenedShape.append( area.getPathIterator( null , _flatness ) , false );
		final PathIterator pathIterator = flattenedShape.getPathIterator( null );

		final List<double[]> points = new ArrayList<double[]>();
		final List<int[]>    lines  = new ArrayList<int[]>();

		double movedToX     = 0.0;
		double movedToY     = 0.0;
		int    movedToIndex = 0;

		int lastPointIndex = -1;
		while ( !pathIterator.isDone() )
		{
			final double[] coordinates = new double[ 6 ];
			final int      type        = pathIterator.currentSegment( coordinates );

			if ( type == PathIterator.SEG_CLOSE )
			{
				final double[] lastPoint = points.get( points.size() - 1 );

				if ( !equalPoint( movedToX , movedToY , lastPoint[ 0 ] , lastPoint[ 1 ] ) )
				{
					lines.add( new int[] { lastPointIndex , movedToIndex } );
				}
			}
			else
			{
				int pointIndex = -1;
				for ( int j = 0; j < points.size(); j++ )
				{
					final double[] point = points.get( j );
					if ( MathTools.almostEqual( point[ 0 ] , coordinates[ 0 ] ) &&
				         MathTools.almostEqual( point[ 1 ] , coordinates[ 1 ] ) )
					{
						pointIndex = j;
						break;
					}
				}

				if ( pointIndex == -1 )
				{
					pointIndex = points.size();
					points.add( new double[] { coordinates[ 0 ] , coordinates[ 1 ] } );
				}

				if ( type == PathIterator.SEG_MOVETO )
				{
					movedToX = coordinates[ 0 ];
					movedToY = coordinates[ 1 ];
					movedToIndex = pointIndex;
				}
				else if ( type == PathIterator.SEG_LINETO )
				{
					lines.add( new int[] { lastPointIndex , pointIndex } );
				}

				lastPointIndex = pointIndex;
			}

			pathIterator.next();
		}

		/*
		 * Find all other, non-intersecting, lines.
		 */
		for ( int p1Index = 0 ; p1Index < points.size() ; p1Index++ )
		{
			for ( int p2Index = p1Index + 1 ; p2Index < points.size() ; p2Index++ )
			{
				boolean isNewLine = true;

				for ( int j = 0 ; isNewLine && j < lines.size() ; j++ )
				{
					final int[] line        = lines.get( j );
					final int   lineP1Index = line[ 0 ];
					final int   lineP2Index = line[ 1 ];

					if ( ( ( lineP1Index == p1Index ) && ( lineP2Index == p2Index ) )
					  || ( ( lineP1Index == p2Index ) && ( lineP2Index == p1Index ) ) )
					{
						isNewLine = false;
					}
				}

				if ( isNewLine )
				{
					boolean addLine = true;

					for ( int j = 0 ; addLine && j < lines.size() ; j++ )
					{
						final int[]    existingLine = lines.get( j );
						final double[] exLineP1     = points.get( existingLine[ 0 ] );
						final double[] exLineP2     = points.get( existingLine[ 1 ] );

						final double[] newLineP1 = points.get( p1Index );
						final double[] newLineP2 = points.get( p2Index );

						if ( !area.contains( ( newLineP1[ 0 ] + newLineP2[ 0 ] ) / 2.0 , ( newLineP1[ 1 ] + newLineP2[ 1 ] ) / 2.0 ) )
						{
							addLine = false;
						}
						else if ( intersectLines( exLineP1[ 0 ] , exLineP1[ 1 ] , exLineP2[ 0 ] , exLineP2[ 1 ] , newLineP1[ 0 ] , newLineP1[ 1 ] , newLineP2[ 0 ] , newLineP2[ 1 ] ) )
						{
							addLine = false;
						}
					}

					if ( addLine )
					{
						lines.add( new int[] { p1Index , p2Index } );
					}
				}
			}
		}

		/*
		 *  Map point index on point indexes of "linked" points.
		 */
		final List<Integer>[] pointMap = new ArrayList[ points.size() ];

		for ( int pIndex = 0 ; pIndex < points.size() ; pIndex++ )
		{
			final List<Integer> pointIndexes = new ArrayList<Integer>();

			for ( final int[] line : lines )
			{
				final int   lineP1Index = line[ 0 ];
				final int   lineP2Index = line[ 1 ];

				if ( lineP1Index == pIndex )
				{
					pointIndexes.add( new Integer( lineP2Index ) );
				}
				else if ( lineP2Index == pIndex )
				{
					pointIndexes.add( new Integer( lineP1Index ) );
				}
			}

			pointMap[ pIndex ] = pointIndexes;
		}

		/*
		 * Find all triangles, ignore triangles that are not part of the panel area.
		 */
		final List<int[]> triangles = new ArrayList<int[]>();

		for ( int p1Index = 0 ; p1Index < points.size() ; p1Index++ )
		{
			final List<Integer> neighbours = pointMap[ p1Index ];
			for ( final Integer p2Index : neighbours )
			{
				for ( final Integer p3Index : neighbours )
				{
					if ( p2Index.equals( p3Index ) )
						continue;

					if ( pointMap[ p2Index ].contains( p3Index ) )
					{
						boolean duplicate = false;
						for ( final int[] existing : triangles )
						{
							if ( ( existing[ 0 ] == p1Index || existing[ 1 ] == p1Index || existing[ 2 ] == p1Index ) &&
						         ( existing[ 0 ] == p2Index || existing[ 1 ] == p2Index || existing[ 2 ] == p2Index ) &&
						         ( existing[ 0 ] == p3Index || existing[ 1 ] == p3Index || existing[ 2 ] == p3Index ) )
							{
								duplicate = true;
								break;
							}
						}

						if ( !duplicate )
						{
							triangles.add( new int[] { p1Index , p2Index , p3Index } );
						}
					}
				}
			}
		}

		for ( final Iterator<int[]> iterator = triangles.iterator() ; iterator.hasNext() ; )
		{
			final int[] triangle = iterator.next();

			final double[] p1 = points.get( triangle[ 0 ] );
			final double[] p2 = points.get( triangle[ 1 ] );
			final double[] p3 = points.get( triangle[ 2 ] );

			final boolean partOfArea = !flattenedShape.contains( pointWithinTriangle( p1[ 0 ] , p1[ 1 ] , p2[ 0 ] , p2[ 1 ] , p3[ 0 ] , p3[ 1 ] ) );

			if ( partOfArea )
			{
				iterator.remove();
			}
		}

		// Make all triangles counter-clockwise.
		for ( final int[] triangle : triangles )
		{
			final double[] p1 = points.get( triangle[ 0 ] );
			final double[] p2 = points.get( triangle[ 1 ] );
			final double[] p3 = points.get( triangle[ 2 ] );

			final double dx1 = p2[ 0 ] - p1[ 0 ];
			final double dy1 = p2[ 1 ] - p1[ 1 ];
			final double dx2 = p3[ 0 ] - p2[ 0 ];
			final double dy2 = p3[ 1 ] - p2[ 1 ];

			final double cross = dx1 * dy2 - dy1 * dx2;

			if ( _normalsFlipped ? ( cross > 0.0 ) : ( cross < 0.0 ) )
			{
				final int temp = triangle[ 1 ];
				triangle[ 1 ] = triangle[ 2 ];
				triangle[ 2 ] = temp;
			}
		}

		return new TriangulationImpl( points , triangles );
	}

	private static Point2D pointWithinTriangle( final double p1x , final double p1y , final double p2x , final double p2y , final double p3x , final double p3y )
	{
		final Point2D p4 = new Point2D.Double( ( p1x + p2x ) / 2.0 , ( p1y + p2y ) / 2.0 );

		return new Point2D.Double( ( p3x + p4.getX() ) / 2.0 , ( p3y + p4.getY() ) / 2.0 );
	}

	private static boolean equalPoint( final double x1 , final double y1 , final double x2 , final double y2 )
	{
		return ( x1 == x2 ) && ( y1 == y2 );
	}

	private static boolean equalLine( final double line1Point1X , final double line1Point1Y , final double line1Point2X , final double line1Point2Y ,
	                                  final double line2Point1X , final double line2Point1Y , final double line2Point2X , final double line2Point2Y )
	{
		return ( equalPoint( line1Point1X , line1Point1Y , line2Point1X , line2Point1Y ) && equalPoint( line1Point2X , line1Point2Y , line2Point2X , line2Point2Y ) )
		    || ( equalPoint( line1Point1X , line1Point1Y , line2Point2X , line2Point2Y ) && equalPoint( line1Point2X , line1Point2Y , line2Point1X , line2Point1Y ) );
	}

	private static boolean intersectLines( final double line1Point1X , final double line1Point1Y , final double line1Point2X , final double line1Point2Y ,
	                                       final double line2Point1X , final double line2Point1Y , final double line2Point2X , final double line2Point2Y )
	{
		boolean result = false;

		if ( !equalLine(  line1Point1X ,  line1Point1Y ,  line1Point2X ,  line1Point2Y ,
	                      line2Point1X ,  line2Point1Y ,  line2Point2X ,  line2Point2Y ) )
		{
			if ( equalPoint( line1Point1X , line1Point1Y , line2Point1X , line2Point1Y ) || equalPoint( line1Point1X , line1Point1Y , line2Point2X , line2Point2Y )
			  || equalPoint( line1Point2X , line1Point2Y , line2Point1X , line2Point1Y ) || equalPoint( line1Point2X , line1Point2Y , line2Point2X , line2Point2Y ) )
			{
//				// lines coincide
//				if ( Line2D.ptLineDistSq( line1Point1X , line1Point1Y , line1Point2X , line1Point2Y , line2Point1X , line2Point1Y ) == 0.0 &&
//				     Line2D.ptLineDistSq( line1Point1X , line1Point1Y , line1Point2X , line1Point2Y , line2Point2X , line2Point2Y ) == 0.0 )
//				{
//					result = true;
//				}
			}
			else
			{
				result = Line2D.linesIntersect( line1Point1X ,  line1Point1Y ,  line1Point2X ,  line1Point2Y , line2Point1X ,  line2Point1Y ,  line2Point2X ,  line2Point2Y );
			}
		}

		return result;
	}

	/**
	 * Represents the result of a triangulation operation.
	 */
	private static class TriangulationImpl
		implements Triangulation
	{
		private List<double[]> _points;

		private Collection<int[]> _triangles;

		private TriangulationImpl( final List<double[]> points , final Collection<int[]> triangles )
		{
			_points    = points;
			_triangles = triangles;
		}

		/**
		 * Returns the vertices of the triangulation as a list of x/y coordinate
		 * pairs.
		 *
		 * @return  List of arrays, each containing an x and an y coordinate.
		 */
		public List<double[]> getPoints()
		{
			return Collections.unmodifiableList( _points );
		}

		public Collection<int[]> getTriangles()
		{
			return Collections.unmodifiableCollection( _triangles );
		}

		/**
		 * Returns the vertices of the triangulation as a list of 3D vectors.
		 * Since the triangulation consists only of 2D information, a
		 * transformation can be given to convert the coordinates to 3D. The
		 * z-coordinate of all vertices before transformation is <code>0.0</code>.
		 *
		 * @param   transform   Transformation to be applied to the vectors.
		 *
		 * @return  List of vectors indicating the position of each vertex.
		 */
		public List<Vector3D> getVertices( final Matrix3D transform )
		{
			final List<Vector3D> result = new ArrayList<Vector3D>( _points.size() );

			for ( final double[] point : _points )
			{
				result.add( transform.multiply( Vector3D.INIT.set( point[ 0 ] , point[ 1 ] , 0.0 ) ) );
			}

			return result;
		}
	}
}
