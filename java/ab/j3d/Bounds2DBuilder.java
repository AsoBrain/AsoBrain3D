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
 * @see     Bounds2D
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Bounds2DBuilder
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
	 * Temporary result value. This is set to the initial bounds by the
	 * constructor if initial bounds were specified; otherwise, it will be
	 * set to <code>null</code> initially. After that, it will be set to
	 * any non-<code>null</code> value returned by the {@link #getBounds()}
	 * method.
	 */
	private Bounds2D _result;

	/**
	 * Construct new {@link Bounds2D} builder without initial bounds.
	 */
	public Bounds2DBuilder()
	{
		this( null );
	}

	/**
	 * Construct new {@link Bounds2D} builder with the specified intial bounds.
	 *
	 * @param   initialBounds   Initial bounds to use (<code>null</code> => none).
	 */
	public Bounds2DBuilder( final Bounds2D initialBounds )
	{
		if ( initialBounds != null )
		{
			_minX   = initialBounds.minX;
			_minY   = initialBounds.minY;
			_maxX   = initialBounds.maxX;
			_maxY   = initialBounds.maxY;
			_result = initialBounds;
		}
		else
		{
			_minX   = Double.POSITIVE_INFINITY;
			_minY   = Double.POSITIVE_INFINITY;
			_maxX   = Double.NEGATIVE_INFINITY;
			_maxY   = Double.NEGATIVE_INFINITY;
			_result = null;
		}
	}

	/**
	 * Get resulting bounding box from this builder. If no initial bounds were
	 * specified and no points were added to this builder, then this method
	 * will return <code>null</code> to indicate that no bounding box could be
	 * calculated.
	 *
	 * @return  Bounding box as {@link Bounds2D} instance;
	 *          <code>null</code> if no bounding box could be determined.
	 */
	public Bounds2D getBounds()
	{
		Bounds2D result;

		final double minX = _minX;
		final double maxX = _maxX;
		final double minY = _minY;
		final double maxY = _maxY;

		if ( ( minX <= maxX ) && ( minY <= maxY ) )
		{
			result = _result;
			if ( ( result == null ) || ( result.minX != minX ) || ( result.minY != minY ) || ( result.maxX != maxX ) || ( result.maxY != maxY ) )
			{
				result = new Bounds2D( minX , minY , maxX , maxY );
				_result = result;
			}
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
	 * @param   x   X coordinate of point to add.
	 * @param   y   Y coordinate of point to add.
	 */
	public void addPoint( final double x , final double y )
	{
		if ( x < _minX ) _minX = x;
		if ( y < _minY ) _minY = y;
		if ( x > _maxX ) _maxX = x;
		if ( y > _maxY ) _maxY = y;
	}
}
