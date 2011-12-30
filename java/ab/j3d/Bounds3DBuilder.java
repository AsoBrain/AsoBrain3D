/* $Id$
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
package ab.j3d;

import org.jetbrains.annotations.*;

/**
 * This class can be used to calculate a bounding box around a collection of
 * points.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Bounds3DBuilder
{
	/**
	 * Number of points that were added.
	 */
	private int _count;

	/**
	 * Minimum X value sofar.
	 */
	private double _minX;

	/**
	 * Minimum Y value sofar.
	 */
	private double _minY;

	/**
	 * Minimum Z value sofar.
	 */
	private double _minZ;

	/**
	 * Maximum X value sofar.
	 */
	private double _maxX;

	/**
	 * Maximum Y value sofar.
	 */
	private double _maxY;

	/**
	 * Maximum Z value sofar.
	 */
	private double _maxZ;

	/**
	 * Summarized X values.
	 */
	private double _sumX;

	/**
	 * Summarized Y values.
	 */
	private double _sumY;

	/**
	 * Summarized Z values.
	 */
	private double _sumZ;

	/**
	 * Cached average point.
	 */
	private Vector3D _average;

	/**
	 * Cached bounds.
	 */
	private Bounds3D _bounds;

	/**
	 * Construct new {@link Bounds3D} builder.
	 */
	public Bounds3DBuilder()
	{
		final double min = Double.POSITIVE_INFINITY;
		final double max = Double.NEGATIVE_INFINITY;

		_count   = 0;
		_minX   = min;
		_minY   = min;
		_minZ   = min;
		_maxX   = max;
		_maxY   = max;
		_maxZ   = max;
		_sumX    = 0.0;
		_sumY    = 0.0;
		_sumZ    = 0.0;
		_average = Vector3D.ZERO;
		_bounds  = Bounds3D.EMPTY;
	}

	/**
	 * Add {@link Bounds3D} to the bounding box.
	 *
	 * @param   bounds  {@link Bounds3D} to add.
	 */
	public void addBounds( final Bounds3D bounds )
	{
		addPoint( bounds.v1 );
		addPoint( bounds.v2 );
	}

	/**
	 * Add 3D bounds to the bounding box.
	 *
	 * @param   x1  First X coordinate of bounds.
	 * @param   y1  First Y coordinate of bounds.
	 * @param   z1  First Z coordinate of bounds.
	 * @param   x2  Second X coordinate of bounds.
	 * @param   y2  Second Y coordinate of bounds.
	 * @param   z2  Second Z coordinate of bounds.
	 */
	public void addBounds( final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		addPoint( x1, y1, z1 );
		addPoint( x2, y2, z2 );
	}

	/**
	 * Add transformed {@link Bounds3D} to the bounding box.
	 *
	 * @param   transform   Transformation to apply to {@link Bounds3D}.
	 * @param   bounds      {@link Bounds3D} to add.
	 */
	public void addBounds( final Matrix3D transform, final Bounds3D bounds )
	{
		addBounds( transform, bounds.v1.x, bounds.v1.y, bounds.v1.z, bounds.v2.x, bounds.v2.y, bounds.v2.z );
	}

	/**
	 * Add transformed 3D bounds to the bounding box.
	 *
	 * @param   transform   Transformation to apply to bounds.
	 * @param   x1          First X coordinate of bounds.
	 * @param   y1          First Y coordinate of bounds.
	 * @param   z1          First Z coordinate of bounds.
	 * @param   x2          Second X coordinate of bounds.
	 * @param   y2          Second Y coordinate of bounds.
	 * @param   z2          Second Z coordinate of bounds.
	 */
	public void addBounds( final Matrix3D transform, final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		addPoint( transform, x1, y1, z1 );
		addPoint( transform, x1, y1, z2 );
		addPoint( transform, x1, y2, z1 );
		addPoint( transform, x1, y2, z2 );
		addPoint( transform, x2, y1, z1 );
		addPoint( transform, x2, y1, z2 );
		addPoint( transform, x2, y2, z1 );
		addPoint( transform, x2, y2, z2 );
	}

	/**
	 * Add point to builder to include in the bounding box being built.
	 *
	 * @param   point   Point to add.
	 *
	 * @throws NullPointerException if {@code point} is {@code null}.
	 */
	public void addPoint( final Vector3D point )
	{
		addPoint( point.x , point.y , point.z );
	}

	/**
	 * Add point to builder to include in the bounding box being built.
	 *
	 * @param   x   X coordinate of point to add.
	 * @param   y   Y coordinate of point to add.
	 * @param   z   Z coordinate of point to add.
	 */
	public void addPoint( final double x , final double y , final double z )
	{
		_count++;

		if ( x < _minX )
		{
			_minX = x;
		}

		if ( y < _minY )
		{
			_minY = y;
		}

		if ( z < _minZ )
		{
			_minZ = z;
		}

		if ( x > _maxX )
		{
			_maxX = x;
		}

		if ( y > _maxY )
		{
			_maxY = y;
		}

		if ( z > _maxZ )
		{
			_maxZ = z;
		}

		_sumX += x;
		_sumY += y;
		_sumZ += z;
	}

	/**
	 * Add transformed point to the bounding box.
	 *
	 * @param   transform   Transformation to apply to point.
	 * @param   point       Point.
	 */
	public void addPoint( final Matrix3D transform, final Vector3D point )
	{
		addPoint( transform, point.x, point.y, point.z  );
	}

	/**
	 * Add transformed point to the bounding box.
	 *
	 * @param   transform   Transformation to apply to point.
	 * @param   x           X coordinate of point.
	 * @param   y           Y coordinate of point.
	 * @param   z           Z coordinate of point.
	 */
	public void addPoint( final Matrix3D transform, final double x, final double y, final double z )
	{
		addPoint( transform.transformX( x, y, z ), transform.transformY( x, y, z ), transform.transformZ( x, y, z ) );
	}

	/**
	 * Get average point from this builder. If no points were added, this method
	 * returns {@link Vector3D#ZERO}.
	 *
	 * @return  Average point.
	 */
	@NotNull
	public Vector3D getAveragePoint()
	{
		final Vector3D result;

		if ( _count > 0 )
		{
			final double count = (double)_count;
			result = _average.set( _sumX / count , _sumY / count, _sumZ / count );
		}
		else
		{
			result = Vector3D.ZERO;
		}

		_average = result;

		return result;
	}

	/**
	 * Get center point from this builder. If no points were added, this method
	 * returns {@link Vector3D#ZERO}.
	 *
	 * @return  Center point.
	 */
	@NotNull
	public Vector3D getCenterPoint()
	{
		return ( _count > 0 ) ? new Vector3D( 0.5 * ( _minX + _maxX ), 0.5 * ( _minY + _maxY ), 0.5 * ( _minZ + _maxZ ) ) : Vector3D.ZERO;
	}

	/**
	 * Get resulting bounding box from this builder. These bounds are always
	 * {@link Bounds3D#sorted() sorted}. If no initial bounds were specified and
	 * no points were added to this builder, then this method will return
	 * {@code null} to indicate that no bounding box could be calculated.
	 *
	 * @return Bounding box as {@link Bounds3D} instance;
	 *          {@code null} if no bounding box could be determined.
	 */
	@Nullable
	public Bounds3D getBounds()
	{
		Bounds3D result;

		if ( _count > 0 )
		{
			result = _bounds;
			result = result.set( result.v1.set( _minX , _minY , _minZ ) , result.v2.set( _maxX , _maxY , _maxZ ) );
			_bounds = result;
		}
		else
		{
			result = null;
		}

		return result;
	}
}
