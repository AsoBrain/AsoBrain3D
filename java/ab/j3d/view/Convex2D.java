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
	 * Arrays with points that defines the convex polygon. Each points is stored
	 * as two elements in this array and only elements <code>0</code> through
	 * <code>_size * 2 - 1</code> are relevant. The rest of the array is
	 * pre-allocated and may be used by future calls to {@link #add}.
	 *
	 * <p>If {@link #_convex} is set, the points describe the outline of the
	 * convex polygon. Otherwise, the list contains arbitrary points used
	 * as input. The {@link #makeConvex()} method performs the conversion
	 * between these two states.
	 */
	private double[] _points;

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
		_points = new double[ initialCapacity * 2 ];
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
	 * Adds a point to the convex polygon.
	 *
	 * @param   x   X coordinate of point to add.
	 * @param   y   Y coordinate of point to add.
	 */
	public void add( final double x, final double y )
	{
		final int index = _size++ * 2;

		double[] points = _points;
		if ( index >= points.length )
		{
			// growth behavior is the same as 'java.util.ArrayList'
			final int newCapacity = Math.max( index + 2, ( ( points.length * 3 ) / 4 + 1 ) * 2 );
			points = Arrays.copyOf( points, newCapacity );
			_points = points;
		}

		points[ index ] = x;
		points[ index + 1 ] = y;
		_convex = false;
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

		final int size = _size;
		if ( size > 2 )
		{
			final double[] points = _points;
			final int length = size * 2;

			final double ax = points[ 0 ];
			final double ay = points[ 1 ];

			for ( int i = 4; i < length; i += 2 )
			{
				final double bx = points[ i - 2 ];
				final double by = points[ i - 1 ];
				final double cx = points[ i ];
				final double cy = points[ i + 1 ];

				// Always positive, because points are in counter-clockwise order.
				result += ( bx - ax ) * ( cy - ay ) - ( cx - ax ) * ( by - ay );
			}
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
		final double[] points = _points;
		final int length = _size * 2;

		/*
		 * Find a starting point A with the minimum Y-coordinate.
		 */
		double minY = Double.POSITIVE_INFINITY;
		int indexMinY = 0;

		for ( int i = 0; i < length; i += 2 )
		{
			final double y = points[ i + 1 ];
			if ( y < minY )
			{
				minY = y;
				indexMinY = i;
			}
		}

		if ( indexMinY != 0 )
		{
			final double x0 = points[ 0 ];
			final double y0 = points[ 1 ];
			points[ 0 ] = points[ indexMinY ];
			points[ 1 ] = points[ indexMinY + 1 ];
			points[ indexMinY ] = x0;
			points[ indexMinY + 1 ] = y0;
		}

		/*
		 * For the current starting point A, find a point B such that all
		 * other points lie to the left of AB.
		 */
		for ( int i = 2; i < length; i += 2 )
		{
			final double currentX = points[ i - 2 ];
			final double currentY = points[ i - 1 ];

			int nextIndex = i;
			double nextX = points[ nextIndex ];
			double nextY = points[ nextIndex + 1 ];

			for ( int j = i + 2; j <= length; j += 2 )
			{
				final int pointIndex = j % length;
				final double pointX = points[ pointIndex ];
				final double pointY = points[ pointIndex + 1 ];

				final double signedArea = ( nextX - currentX ) * ( pointY - currentY ) - ( pointX - currentX ) * ( nextY - currentY );
				if ( signedArea < 0.0 )
				{
					/*
					 * Point is to the right of AB. Use it as new B.
					 */
					nextX = pointX;
					nextY = pointY;
					nextIndex = pointIndex;
				}
			}

			if ( nextIndex == 0 )
			{
				_size = i / 2;
				break;
			}

			if ( i != nextIndex )
			{
				points[ nextIndex ] = points[ i ];
				points[ nextIndex + 1 ] = points[ i + 1 ];
				points[ i ] = nextX;
				points[ i + 1 ] = nextY;
			}
		}
	}
}
