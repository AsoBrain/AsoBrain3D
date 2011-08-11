/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.view;

import java.awt.geom.*;
import java.util.*;

/**
 * A convex polygon in 2D. The polygon is constructed from an arbitrary set of
 * points, forming the convex hull of that set.
 *
 * <p>
 * This implementation uses the <a href="http://www.cse.unsw.edu.au/~lambert/java/3d/giftwrap.html">gift wrapping algorithm</a>
 * for computing the convex hull of a set of points. The signed area of
 * triangles is calculated using the cross product of two of its sides
 * (see <a href="http://en.wikipedia.org/wiki/Triangle#Using_vectors">Computing the area of a triangle using vectors</a>).
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class Convex2D
{
	/**
	 * List of points that defines the convex polygon. Only elements
	 * <code>0</code> through <code>_size - 1</code> are relevant. Any other
	 * elements are cached points that may be used by future calls to
	 * {@link #add()}.
	 *
	 * <p>If {@link #_convex} is set, the points describe the outline of the
	 * convex polygon. Otherwise, the list contains arbitrary points used
	 * as input. The {@link #makeConvex()} method performs the conversion
	 * between these two states.
	 */
	private final List<Point2D> _points;

	/**
	 * Number of relevant points in {@link #_points}.
	 */
	private int _size = 0;

	/**
	 * If set, {@link #_points} may contain an arbitrary set of points.
	 */
	private boolean _convex = true;

	/**
	 * Constructs a new instance.
	 *
	 * @param   initialCapacity     Initial capacity.
	 */
	public Convex2D( final int initialCapacity )
	{
		_points = new ArrayList<Point2D>( initialCapacity );
	}

	/**
	 * Removes all points, such that this instance can be reused.
	 */
	public void clear()
	{
		_size = 0;
		_convex = true;
	}

	/**
	 * Adds a point to the convex polygon. The returned object is used to set
	 * the position of the point. The initial position of the point is
	 * undefined.
	 *
	 * @return  Added point.
	 */
	public Point2D add()
	{
		final Point2D result;
		final List<Point2D> points = _points;
		final int index = _size++;
		if ( points.size() > index )
		{
			result = points.get( index );
		}
		else
		{
			result = new Point2D.Double();
			points.add( result );
		}
		_convex = false;
		return result;
	}

	/**
	 * Returns the number of points in the convex polygon.
	 *
	 * @return  Number of points.
	 */
	public int size()
	{
		makeConvexIfNeeded();
		return _size;
	}

	/**
	 * Calculates the area of the convex polygon.
	 *
	 * @return  Area of the convex polygon.
	 */
	public double area()
	{
		makeConvexIfNeeded();

		double result = 0.0;
		final List<Point2D> points = _points;

		final Point2D a = points.get( 0 );
		final double ax = a.getX();
		final double ay = a.getY();
		for ( int i = 2; i < _size; i++ )
		{
			final Point2D b = points.get( i - 1 );
			final Point2D c = points.get( i );

			// Always positive, because points are in counter-clockwise order.
			result += ( b.getX() - ax ) * ( c.getY() - ay ) - ( c.getX() - ax ) * ( b.getY() - ay );
		}

		return result / 2.0;
	}

	/**
	 * Ensures that the internal list points represents a convex polygon.
	 */
	private void makeConvexIfNeeded()
	{
		if ( !_convex )
		{
			makeConvex();
		}
	}

	/**
	 * Converts the list of arbitrary points into a convex polygon.
	 */
	private void makeConvex()
	{
		final List<Point2D> points = _points;
		final int size = _size;

		/*
		 * Find a starting point A with the minimum Y-coordinate.
		 */
		double minY = Double.POSITIVE_INFINITY;
		int indexMinY = 0;

		for ( int i = 0; i < size; i++ )
		{
			final Point2D point = points.get( i );
			final double y = point.getY();
			if ( y < minY )
			{
				minY = y;
				indexMinY = i;
			}
		}

		if ( indexMinY != 0 )
		{
			points.set( indexMinY, points.set( 0, points.get( indexMinY ) ) );
		}

		/*
		 * For the current starting point A, find a point B such that all
		 * other points lie to the left of AB.
		 */
		for ( int i = 1; i < size; i++ )
		{
			final int currentIndex = i - 1;
			final Point2D current = points.get( currentIndex );

			Point2D next = points.get( i );
			int nextIndex = i;

			for ( int j = i + 1; j <= size; j++ )
			{
				final int pointIndex = j % size;
				final Point2D point = points.get( pointIndex );

				final double signedArea = ( next.getX() - current.getX() ) * ( point.getY() - current.getY() ) - ( point.getX() - current.getX() ) * ( next.getY() - current.getY() );
				if ( signedArea < 0.0 )
				{
					/*
					 * Point is to the right of AB. Use it as new B.
					 */
					next = point;
					nextIndex = pointIndex;
				}
			}

			if ( nextIndex == 0 )
			{
				_size = i;
				break;
			}

			if ( i != nextIndex )
			{
				points.set( nextIndex, points.set( i, points.get( nextIndex ) ) );
			}
		}
	}
}
