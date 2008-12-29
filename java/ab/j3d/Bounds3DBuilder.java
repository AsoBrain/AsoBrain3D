/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2008
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

/**
 * This class can be used to calculate a bounding box around a collection of
 * points.
 *
 * @see     Bounds3D
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Bounds3DBuilder
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
		_average = Vector3D.INIT;
		_bounds  = Bounds3D.INIT;
	}

	/**
	 * Add point to builder to include in the bounding box being built.
	 *
	 * @param   point   Point to add.
	 *
	 * @throws  NullPointerException if <code>point</code> is <code>null</code>.
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
		if ( x < _minX ) _minX = x;
		if ( y < _minY ) _minY = y;
		if ( z < _minZ ) _minZ = z;
		if ( x > _maxX ) _maxX = x;
		if ( y > _maxY ) _maxY = y;
		if ( z > _maxZ ) _maxZ = z;
		_sumX += x;
		_sumY += y;
		_sumZ += z;
	}

	/**
	 * Get average point from this builder. If no points were added, this method
	 * returns {@link Vector3D#INIT}.
	 *
	 * @return  Average point.
	 */
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
			result = Vector3D.INIT;
		}

		_average = result;

		return result;
	}

	/**
	 * Get resulting bounding box from this builder. If no initial bounds were
	 * specified and no points were added to this builder, then this method
	 * will return <code>null</code> to indicate that no bounding box could be
	 * calculated.
	 *
	 * @return  Bounding box as {@link Bounds3D} instance;
	 *          <code>null</code> if no bounding box could be determined.
	 */
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
