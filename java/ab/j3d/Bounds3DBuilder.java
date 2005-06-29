/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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
 * points. The initial bounds, which can only be expanded, can also be
 * specified.
 *
 * @see     Bounds3D
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Bounds3DBuilder
{
	/**
	 * Minimum X value sofar. If no initial bounds are specified, this is
	 * intialized to {@link Double.POSITIVE_INFINITY}.
	 */
	private double _minX;

	/**
	 * Minimum Y value sofar. If no initial bounds are specified, this is
	 * intialized to {@link Double.POSITIVE_INFINITY}.
	 */
	private double _minY;

	/**
	 * Minimum Z value sofar. If no initial bounds are specified, this is
	 * intialized to {@link Double.POSITIVE_INFINITY}.
	 */
	private double _minZ;

	/**
	 * Maximum X value sofar. If no initial bounds are specified, this is
	 * intialized to {@link Double.NEGATIVE_INFINITY}.
	 */
	private double _maxX;

	/**
	 * Maximum Y value sofar. If no initial bounds are specified, this is
	 * intialized to {@link Double.NEGATIVE_INFINITY}.
	 */
	private double _maxY;

	/**
	 * Maximum Z value sofar. If no initial bounds are specified, this is
	 * intialized to {@link Double.NEGATIVE_INFINITY}.
	 */
	private double _maxZ;

	/**
	 * Temporary result value. This is set to the initial bounds by the
	 * constructor if initial bounds were specified; otherwise, it will be
	 * set to {@link Bounds3D#INIT} initially. After that, it will be set to
	 * any non-<code>null</code> value returned by the {@link #getBounds()}
	 * method.
	 */
	private Bounds3D _result;

	/**
	 * Construct new {@link Bounds3D} builder without initial bounds.
	 */
	public Bounds3DBuilder()
	{
		this( null );
	}

	/**
	 * Construct new {@link Bounds3D} builder with the specified intial bounds.
	 *
	 * @param   initialBounds   Initial bounds to use (<code>null</code> => none).
	 */
	public Bounds3DBuilder( final Bounds3D initialBounds )
	{
		if ( initialBounds != null )
		{
			final Vector3D v1 = initialBounds.v1;
			final Vector3D v2 = initialBounds.v2;

			_minX   = Math.min( v1.x , v2.x );
			_minY   = Math.min( v1.y , v2.y );
			_minZ   = Math.min( v1.z , v2.z );
			_maxX   = Math.max( v1.x , v2.x );
			_maxY   = Math.max( v1.y , v2.y );
			_maxZ   = Math.max( v1.z , v2.z );
			_result = initialBounds;
		}
		else
		{
			final double min = Double.POSITIVE_INFINITY;
			final double max = Double.NEGATIVE_INFINITY;

			_minX   = min;
			_minY   = min;
			_minZ   = min;
			_maxX   = max;
			_maxY   = max;
			_maxZ   = max;
			_result = Bounds3D.INIT;
		}
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

		if ( ( _minX <= _maxX ) && ( _minY <= _maxY ) && ( _minZ <= _maxZ ) )
		{
			result = _result;
			result = result.set( result.v1.set( _minX , _minY , _minZ ) , result.v2.set( _maxX , _maxY , _maxZ ) );
			_result = result;
		}
		else
		{
			result = null;
		}

		return result;
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
		if ( x < _minX ) _minX = x;
		if ( y < _minY ) _minY = y;
		if ( z < _minZ ) _minZ = z;
		if ( x > _maxX ) _maxX = x;
		if ( y > _maxY ) _maxY = y;
		if ( z > _maxZ ) _maxZ = z;
	}
}
