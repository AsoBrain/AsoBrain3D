/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2001-2005
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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.NoSuchElementException;

/**
 * This class describes 2D boundaries.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Bounds2D
	implements Shape
{
	/**
	 * Lower bound X coordinate.
	 */
	public final double minX;

	/**
	 * Lower bound Y coordinate.
	 */
	public final double minY;

	/**
	 * Upper bound X coordinate.
	 */
	public final double maxX;

	/**
	 * Upper bound Y coordinate.
	 */
	public final double maxY;

	/**
	 * Construct boundaries.
	 *
	 * @param   minX      Lower bound X coordinate.
	 * @param   minY      Lower bound Y coordinate.
	 * @param   maxX      Upper bound X coordinate.
	 * @param   maxY      Upper bound Y coordinate.
	 */
	public Bounds2D( final double minX , final double minY , final double maxX , final double maxY )
	{
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	/**
	 * Checks for intersection between this and other bounds.
	 *
	 * @param   other   Bounds to compare against.
	 *
	 * @return  <code>true</code> if bounds intersect;
	 *          <code>false</code> if bounds are disjunct.
	 */
	public boolean intersects( final Bounds2D other )
	{
		return ( minX <= other.maxX ) && ( maxX >= other.minX )
		    && ( minY <= other.maxY ) && ( maxY >= other.minY );
	}

	public double getX()
	{
		return minX;
	}

	public double getY()
	{
		return minY;
	}

	public double getWidth()
	{
		return maxX - minX;
	}

	public double getHeight()
	{
		return maxY - minY;
	}

	public boolean isEmpty()
	{
		return ( minX >= maxX ) || ( minY >= maxY );
	}

	public Rectangle getBounds()
	{
		final Rectangle result;

		if ( ( minX > maxY ) || ( minY > maxY ) )
		{
			result = new Rectangle();
		}
		else
		{
			final double x1 = Math.floor( minX );
			final double x2 = Math.ceil ( maxX );
			final double y1 = Math.floor( minY );
			final double y2 = Math.ceil ( maxY );

			result = new Rectangle( (int)x1 , (int)y1 , (int)( x2 - x1 ) , (int)( y2 - y1 ) );
		}

		return result;
	}

	public Rectangle2D getBounds2D()
	{
		return new Rectangle2D.Double( minX , minY , maxX - minX , maxY - minY );
	}

	public boolean contains( final double x , final double y )
	{
		return ( x >= minX ) && ( x <= maxX )
		    && ( y >= minY ) && ( y <= maxY );
	}

	public boolean intersects( final double x , final double y , final double w , final double h )
	{
		return ( maxX >= x     ) && ( maxY >= y     )
		    && ( minX <= x + w ) && ( minY <= y + h );
	}

	public boolean intersects( final Rectangle2D r )
	{
		return ( minX <= r.getMaxX() ) && ( maxX >= r.getMinX() )
		    && ( minY <= r.getMaxY() ) && ( maxY >= r.getMinY() );
	}

	public boolean contains( final double x , final double y , final double w , final double h )
	{
		return (   x       >= minX ) && (   y       >= minY )
		    && ( ( x + w ) <= maxX ) && ( ( y + h ) <= maxY );
	}

	public boolean contains( final Rectangle2D r )
	{
		return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public boolean contains( final Point2D p )
	{
		return contains( p.getX() , p.getY() );
	}

	public PathIterator getPathIterator( final AffineTransform at )
	{
		return new Iterator( at );
	}

	public PathIterator getPathIterator( final AffineTransform at , final double flatness )
	{
		return getPathIterator( at );
	}

	private class Iterator
		implements PathIterator
	{
		private final AffineTransform _at;

		private int _index;

		private Iterator( final AffineTransform at )
		{
			_at = at;
			_index = ( minX > maxY ) || ( minY > maxY ) ? 6 : 0;
		}

		public int getWindingRule()
		{
			return WIND_NON_ZERO;
		}

		public boolean isDone()
		{
			return ( _index > 5 );
		}

		public void next()
		{
			_index++;
		}

		public int currentSegment( final float[] coords )
		{
			final int result;

			final int index = _index;
			if ( index < 5 )
			{
				coords[ 0 ] = (float)( ( ( index == 1 ) || ( index == 2 ) ) ? maxX : minX );
				coords[ 1 ] = (float)( ( ( index == 2 ) || ( index == 3 ) ) ? maxY : minY );

				if ( _at != null )
					_at.transform( coords , 0 , coords , 0 , 1 );

				result = ( index == 0 ) ? SEG_MOVETO : SEG_LINETO;
			}
			else if ( index == 5 )
			{
				result = SEG_CLOSE;
			}
			else
			{
				throw new NoSuchElementException( "index=" + index );
			}

			return result;
		}

		public int currentSegment( final double[] coords )
		{
			final int result;

			final int index = _index;
			if ( index < 5 )
			{
				coords[ 0 ] = ( ( index == 1 ) || ( index == 2 ) ) ? maxX : minX;
				coords[ 1 ] = ( ( index == 2 ) || ( index == 3 ) ) ? maxY : minY;

				if ( _at != null )
					_at.transform( coords , 0 , coords , 0 , 1 );

				result = ( index == 0 ) ? SEG_MOVETO : SEG_LINETO;
			}
			else if ( index == 5 )
			{
				result = SEG_CLOSE;
			}
			else
			{
				throw new NoSuchElementException( "index=" + index );
			}

			return result;
		}
	}

	public String toString()
	{
		return "Bounds2D[minX=" + minX + ",minY" + minY + ",maxX=" + maxX + ",maxY=" + maxY + ']';
	}

	/**
	 * Create human-readable representation of this object.
	 * <p />
	 * This is specifically useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this object.
	 */
	public String toFriendlyString()
	{
		return "( " + minX + " , " + minY + " ) - ( " + maxX + " , " + maxY + " )";
	}
}
