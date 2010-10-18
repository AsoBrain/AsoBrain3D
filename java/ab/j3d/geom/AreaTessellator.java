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

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Tessellator that uses Java2D's {@link Area} class in combination with a simple
 * and rather brute-force method. By subdividing the shapes to be tessellated
 * (using the Area class), reasonable performance can be achieved.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
class AreaTessellator
	implements Tessellator
{
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
	 * Construct new area tessellator.
	 */
	AreaTessellator()
	{
		_normalsFlipped        = false;
		_initialSubdivisionsX  = 1;
		_initialSubdivisionsY  = 1;
		_iteratedSubdivisionsX = 1;
		_iteratedSubdivisionsY = 1;
		_subdivisionIterations = 0;
		_flatness              = 1.0;
	}

	@Override
	public Vector3D getNormal()
	{
		return _normalsFlipped ? Vector3D.NEGATIVE_Z_AXIS : Vector3D.POSITIVE_Z_AXIS;
	}

	@Override
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

	static List<Area> subdivide( @NotNull final Area area, final int initialSubdivisionsX, final int initialSubdivisionsY, final int subdivisionsX, final int subdivisionsY, final int iterations )
	{
		if ( subdivisionsX <= 0 )
		{
			throw new IllegalArgumentException( "subdivisionsX: " + subdivisionsX );
		}

		if ( subdivisionsY <= 0 )
		{
			throw new IllegalArgumentException( "subdivisionsY: " + subdivisionsY );
		}

		final List<Area> result = new ArrayList<Area>( subdivisionsX * subdivisionsY );

		if ( area.isRectangular() )
		{
			result.add( area );
		}
		else
		{
			subdivideImpl( area, initialSubdivisionsX, initialSubdivisionsY, subdivisionsX, subdivisionsY, iterations, result );
		}

		return result;
	}

	private static void subdivideImpl( final Area area, final int subdivisionsX, final int subdivisionsY, final int nextSubdivisionsX, final int nextSubdivisionsY, final int remainingIterations, final List<Area> result )
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

				final Area subdivision = new Area( new Rectangle2D.Double( subdivisionX1, subdivisionY1, subdivisionWidth, subdivisionHeight ) );
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
							subdivideImpl( subdivision, nextSubdivisionsX, nextSubdivisionsY, nextSubdivisionsX, nextSubdivisionsY, remainingIterations - 1, result );
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
	 * Returns the tessellation of the given area as a list of shapes, one for
	 * each triangle.
	 *
	 * @param   area    Area to be tessellated.
	 *
	 * @return  Result of the tessellation as a list of triangular shapes.
	 */
	public List<Shape> tessellateToShapes( final Area area )
	{
		final BasicTessellationBuilder tessellationBuilder = new BasicTessellationBuilder();
		tessellationBuilder.getPrimitives();
		tessellate( tessellationBuilder, area );
		final List<Vector3D> vertices = tessellationBuilder.getVertices();

		final List<Shape> result = new ArrayList<Shape>();

		for ( final TessellationPrimitive primitive : tessellationBuilder.getPrimitives() )
		{
			if ( primitive instanceof TriangleFan )
			{
				final int[] primitiveVertices = primitive.getVertices();

				final Path2D polygon = new Path2D.Double( Path2D.WIND_NON_ZERO, primitiveVertices.length + 1 );

				Vector3D v = vertices.get( 0 );
				polygon.moveTo( v.getX(), v.getY() );

				for ( int i = 1; i < primitiveVertices.length; i++ )
				{
					v = vertices.get( i );
					polygon.lineTo( v.getX(), v.getY() );
				}

				polygon.closePath();
				result.add( polygon );
				break;
			}
			else
			{
				final int[] triangles = primitive.getTriangles();
				for (int i = 0; i < triangles.length; )
				{
					final Vector3D p1 = vertices.get( triangles[ i++ ] );
					final Vector3D p2 = vertices.get( triangles[ i++ ] );
					final Vector3D p3 = vertices.get( triangles[ i++ ] );

					final Path2D triangleShape = new Path2D.Double();
					triangleShape.moveTo( p1.getX(), p1.getY() );
					triangleShape.lineTo( p2.getX(), p2.getY() );
					triangleShape.lineTo( p3.getX(), p3.getY() );
					triangleShape.closePath();
					result.add( triangleShape );
				}
			}
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
	@Override
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
	@Override
	public void setFlatness( final double flatness )
	{
		_flatness = flatness;
	}

	@Override
	public void tessellate( @NotNull final TessellationBuilder builder, @NotNull final Shape positive, @NotNull final Collection<? extends Shape> negative )
	{
		final Area combinedArea = new Area( positive );
		for ( final Shape shape : negative )
		{
			combinedArea.subtract( ( shape instanceof Area ) ? (Area)shape : new Area( shape ) );
		}
		tessellate( builder, combinedArea );
	}

	@Override
	public void tessellate( @NotNull final TessellationBuilder builder, @NotNull final Shape shape )
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

		tessellate( builder, subdivide( area, _initialSubdivisionsX, _initialSubdivisionsY, _iteratedSubdivisionsX, _iteratedSubdivisionsY, _subdivisionIterations ) );
	}

	/**
	 * Tessellate areas.
	 *
	 * @param   builder     Builds the resulting tessellation.
	 * @param   areas       Areas to tessellate.
	 */
	private void tessellate( final TessellationBuilder builder, final Iterable<? extends Area> areas )
	{
		for ( final Area area : areas )
		{
			tessellateImpl( builder, area );
		}
	}

	/**
	 * Add tessellated area to tessellation result.
	 *
	 * @param   builder     Builds the resulting tessellation.
	 * @param   area        Area to tessellate.
	 */
	private void tessellateImpl( final TessellationBuilder builder, final Area area )
	{
		/*
		 * Find points and lines in the shape of the area.
		 */
		final Path2D flattenedShape;
		final Map<Integer,Point> pointSet = new HashMap<Integer,Point>();
		final List<Line> lines = new ArrayList<Line>();
		{
			flattenedShape = new Path2D.Double();
			flattenedShape.append( area.getPathIterator( null, _flatness ), false );
			final PathIterator pathIterator = flattenedShape.getPathIterator( null );

			final double[] coordinates = new double[ 6 ];
			Point movePoint = null;
			Point lastPoint = null;

			while ( !pathIterator.isDone() )
			{
				final int type = pathIterator.currentSegment( coordinates );
				if ( type == PathIterator.SEG_CLOSE )
				{
					if ( ( movePoint != null ) && ( lastPoint != null ) && ( lastPoint.vertex != movePoint.vertex ) )
					{
						lines.add( new Line( lastPoint, movePoint ) );
					}
				}
				else
				{
					final double x = coordinates[ 0 ];
					final double y = coordinates[ 1 ];
					final int vertex = builder.addVertex( x, y, 0.0 );
					Point point = new Point( vertex, x, y );

					final Point oldPoint = pointSet.put( Integer.valueOf( vertex ), point );
					if ( oldPoint != null )
					{
						point = oldPoint;
					}

					if ( type == PathIterator.SEG_MOVETO )
					{
						movePoint = point;
					}
					else if ( type == PathIterator.SEG_LINETO )
					{
						if ( ( lastPoint != null ) && ( lastPoint.vertex != vertex ) )
						{
							lines.add( new Line( lastPoint, point ) );
						}
					}

					lastPoint = point;
				}

				pathIterator.next();
			}
		}

		/*
		 * Find all other, non-intersecting, lines.
		 */
		final List<Point> points = new ArrayList<Point>( pointSet.values() );

		for ( int p1IndexIndex = 0; p1IndexIndex < points.size(); p1IndexIndex++ )
		{
			final Point p1 = points.get( p1IndexIndex );

			for ( int p2IndexIndex = p1IndexIndex + 1 ; p2IndexIndex < points.size(); p2IndexIndex++ )
			{
				final Point p2 = points.get( p2IndexIndex );

				final Line line = new Line( p1, p2 );
				if ( !lines.contains( line ) )
				{
					boolean addLine = true;

					for ( final Line existingLine : lines )
					{
						final double centerX = ( p1.x + p2.x ) / 2.0;
						final double centerY = ( p1.y + p2.y ) / 2.0;

						if ( !area.contains( centerX, centerY ) )
						{
							addLine = false;
							break;
						}

						if ( existingLine.intersects( line ) )
						{
							addLine = false;
							break;
						}
					}

					if ( addLine )
					{
						lines.add( line );
					}
				}
			}
		}

		/*
		 *  Map point index on point indexes of "linked" points.
		 */
		final List<Point>[] neighboursMap = new List[ points.size() ];

		for ( int pointIndex = 0 ; pointIndex < points.size(); pointIndex++ )
		{
			final List<Point> neightbours = new ArrayList<Point>();

			for ( final Line line : lines )
			{
				if ( line.p1.vertex == pointIndex )
				{
					neightbours.add( line.p2 );
				}
				else if ( line.p2.vertex == pointIndex )
				{
					neightbours.add( line.p1 );
				}
			}

			neighboursMap[ pointIndex ] = neightbours;
		}

		/*
		 * Find all triangles, ignore triangles that are not part of the panel area.
		 */
		final List<Triangle> triangles = new ArrayList<Triangle>();

		for ( int i = 0 ; i < points.size(); i++ )
		{
			final Point v1 = points.get( i );
			final List<Point> neighbours = neighboursMap[ i ];

			for ( final Point v2 : neighbours )
			{
				for ( final Point v3 : neighbours )
				{
					if ( v2 == v3 )
					{
						continue;
					}

					if ( neighboursMap[ v2.vertex ].contains( v3 ) )
					{
						final Triangle triangle = new Triangle( v1, v2, v3 );
						if ( !triangles.contains( triangle ) )
						{
							triangles.add( triangle );
						}
					}
				}
			}
		}

		for ( final Iterator<Triangle> iterator = triangles.iterator() ; iterator.hasNext() ; )
		{
			final Triangle triangle = iterator.next();

			final boolean partOfArea = !flattenedShape.contains( triangle.getPointWithinTriangle() );
			if ( partOfArea )
			{
				iterator.remove();
			}
		}

		if ( !triangles.isEmpty() )
		{
			// Make all triangles counter-clockwise.
			for ( final Triangle triangle : triangles )
			{
				final Point p1 = triangle.v1;
				final Point p2 = triangle.v2;
				final Point p3 = triangle.v3;

				final double cross = ( p2.x - p1.x ) * ( p3.y - p2.y ) - ( p2.y - p1.y ) * ( p3.x - p2.x );

				if ( _normalsFlipped ? ( cross > 0.0 ) : ( cross < 0.0 ) )
				{
					final Point temp = triangle.v2;
					triangle.v2 = triangle.v3;
					triangle.v3 = temp;
				}
			}

			// Add triangles to result
			final int[] vertices = new int[ triangles.size() * 3 ];
			int i = 0;
			for ( final Triangle triangle : triangles )
			{
				vertices[ i++ ] = triangle.v1.vertex;
				vertices[ i++ ] = triangle.v2.vertex;
				vertices[ i++ ] = triangle.v3.vertex;
			}


			builder.addPrimitive( new TriangleList( vertices ) );
		}
	}

	/**
	 * This class is used internally to temporarily store triangles.
	 */
	private static class Triangle
	{
		/**
		 * First corner of triangle.
		 */
		Point v1;

		/**
		 * Second corner of triangle.
		 */
		Point v2;

		/**
		 * Third corner of triangle.
		 */
		Point v3;

		/**
		 * Construct triangle.
		 *
		 * @param   v1  First corner of triangle.
		 * @param   v2  Second corner of triangle.
		 * @param   v3  Third corner of triangle.
		 */
		Triangle( final Point v1, final Point v2, final Point v3 )
		{
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
		}

		/**
		 * Get a point within this triangle.
		 *
		 * @return  Point in this triangle.
		 */
		private Point2D getPointWithinTriangle()
		{
			return new Point2D.Double( ( v1.x + v2.x ) / 4.0 + v3.x / 2.0, ( v1.y + v2.y ) / 4.0 + v3.y / 2.0 );
		}

		@Override
		public int hashCode()
		{
			return v1.hashCode() ^ v2.hashCode() ^ v3.hashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			return ( obj == this ) || ( ( obj instanceof Triangle ) && equals( ( Triangle ) obj ) );
		}

		/**
		 * Test wether two triangles are equal. Two triangles are equals when
		 * their vertices are equal independent of vertex order.
		 *
		 * @param   other   Line to compare with.
		 *
		 * @return  <code>true</code> if the lines are equal.
		 */
		public boolean equals( final Triangle other )
		{
			final Point thisV1 = v1;
			final Point thisV2 = v2;
			final Point thisV3 = v3;

			final Point otherV1 = other.v1;
			final Point otherV2 = other.v2;
			final Point otherV3 = other.v3;

			return ( thisV1.equals( otherV1 ) || thisV1.equals( otherV2 ) || thisV1.equals( otherV3 ) ) &&
			       ( thisV2.equals( otherV1 ) || thisV2.equals( otherV2 ) || thisV2.equals( otherV3 ) ) &&
			       ( thisV3.equals( otherV1 ) || thisV3.equals( otherV2 ) || thisV3.equals( otherV3 ) );
		}
	}

	/**
	 * This class is used internally to temporarily store lines.
	 */
	private static class Line
	{
		/**
		 * First endpoint of line.
		 */
		final Point p1;

		/**
		 * Second endpoint of line.
		 */
		final Point p2;

		/**
		 * Costruct line.
		 *
		 * @param   p1  First endpoint of line.
		 * @param   p2  Second endpoint of line.
		 */
		Line( final Point p1, final Point p2 )
		{
			this.p1 = p1;
			this.p2 = p2;
		}

		/**
		 * Test for intersection between this and another line.
		 *
		 * @param   other   Other line to test intersection with.
		 *
		 * @return  <code>true</code> if this and the other line intersect;
		 *          <code>false</code> otherwise.
		 */
		boolean intersects( final Line other )
		{
			return !equals( other ) && Line2D.linesIntersect( p1.x,  p1.y,  p2.x,  p2.y, other.p1.x,  other.p1.y,  other.p2.x,  other.p2.y );
		}


		@Override
		public int hashCode()
		{
			return p1.hashCode() ^ p2.hashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			return ( obj == this ) || ( ( obj instanceof Line ) && equals( ( Line ) obj ) );
		}


		/**
		 * Test wether two lines are equal. Two lines are equals when their
		 * end points are equal independent of end point order.
		 *
		 * @param   other   Line to compare with.
		 *
		 * @return  <code>true</code> if the lines are equal.
		 */
		public boolean equals( final Line other )
		{
			return ( this == other ) || ( ( p1.equals( other.p1 ) && p2.equals( other.p2 ) ) || ( p2.equals( other.p1 ) && p1.equals( other.p2 ) ) );
		}
	}

	/**
	 * This class is used internally to temporarily store lines.
	 */
	private static class Point
	{
		/**
		 * Vertex associated with point.
		 */
		final int vertex;

		/**
		 * X coordinate of point.
		 */
		final double x;

		/**
		 * Y coordinate of point.
		 */
		final double y;

		/**
		 * Create point.
		 *
		 * @param   vertex  Vertex associated with point.
		 * @param   x       X coordinate of point.
		 * @param   y       Y coordinate of point.
		 */
		Point( final int vertex, final double x, final double y )
		{
			this.vertex = vertex;
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode()
		{
			return vertex;
		}

		@Override
		public boolean equals( final Object obj )
		{
			return ( obj instanceof Point ) && ( ((Point)obj).vertex == vertex );
		}
	}
}
