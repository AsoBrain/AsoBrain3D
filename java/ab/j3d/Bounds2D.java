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

/**
 * This class represents rectangular 2D bounds.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public abstract class Bounds2D
{
	/**
	 * Get constant version of these bounds. This will return this object when
	 * it is already constant.
	 *
	 * @return  {@link ConstBounds2D}.
	 */
	public abstract ConstBounds2D toConst();

	/**
	 * Get variable version of these bounds. This will return this object when
	 * it is already variable.
	 *
	 * @return  {@link VarBounds2D}.
	 */
	public abstract VarBounds2D toVar();

	/**
	 * Get width of bounds.
	 *
	 * @return  Width of bounds.
	 */
	public double getWidth()
	{
		return getMaxX() - getMinX();
	}

	/**
	 * Get height of bounds.
	 *
	 * @return  Height of bounds.
	 */
	public double getHeight()
	{
		return getMaxY() - getMinY();
	}

	/**
	 * Get minimum X coordinate of bounds.
	 *
	 * @return  Minimum X coordinate of bounds.
	 */
	public abstract double getMinX();

	/**
	 * Get minimum Y coordinate of bounds.
	 *
	 * @return  Minimum Y coordinate of bounds.
	 */
	public abstract double getMinY();

	/**
	 * Get maximum X coordinate of bounds.
	 *
	 * @return  Maximum X coordinate of bounds.
	 */
	public abstract double getMaxX();

	/**
	 * Get maximum Y coordinate of bounds.
	 *
	 * @return  Maximum Y coordinate of bounds.
	 */
	public abstract double getMaxY();

	/**
	 * Get X coordinate of bounds center point.
	 *
	 * @return  X coordinate of bounds center point.
	 */
	public double getCenterX()
	{
		return ( getMinX() + getMaxX() ) / 2.0;
	}

	/**
	 * Get Y coordinate of bounds center point.
	 *
	 * @return  Y coordinate of bounds center point.
	 */
	public double getCenterY()
	{
		return ( getMinY() + getMaxY() ) / 2.0;
	}

	/**
	 * Determines whether the <code>RectangularShape</code> is empty. When the
	 * <code>RectangularShape</code> is empty, it encloses no area.
	 *
	 * @return <code>true</code> if the <code>RectangularShape</code> is empty;
	 *         <code>false</code> otherwise.
	 */
	public boolean isEmpty()
	{
		return ( getWidth() <= 0.0 ) || ( getHeight() <= 0.0 );
	}

	/**
	 * Tests whether the given point is within these bounds.
	 *
	 * @param   point   Point to test.
	 *
	 * @return  <code>true</code> if the point is within these bounds;
	 *          <code>false</code> otherwise.
	 */
	public boolean contains( final Vector2D point )
	{
		return contains( point.getX(), point.getY() );
	}

	/**
	 * Tests whether the given point is within these bounds.
	 *
	 * @param   x   X coordinate of  point to test.
	 * @param   y   Y coordinate of  point to test.
	 *
	 * @return  <code>true</code> if the point is within these bounds;
	 *          <code>false</code> otherwise.
	 */
	public boolean contains( final double x, final double y )
	{
		return ( ( x >= getMinX() ) && ( y >= getMinY() ) && ( x <= getMaxX() ) && ( y <= getMaxY() ) );
	}

	/**
	 * Tests if the given bounds are entirely contained by these bounds.
	 *
	 * @param   other   Bounds to test.
	 *
	 * @return  <code>true</code> if the other bounds are contained wihin these
	 *          bounds; <code>false</code> otherwise.
	 */
	public boolean contains( final Bounds2D other )
	{
		return contains( other.getMinX(), other.getMinY(), other.getMaxX(), other.getMaxY() );
	}

	/**
	 * Tests if the given bounds are entirely contained by these bounds.
	 *
	 * @param   minX    Minimum X coordinate of bounds to test.
	 * @param   minY    Minimum Y coordinate of bounds to test.
	 * @param   maxX    Maximum X coordinate of bounds to test.
	 * @param   maxY    Maximum Y coordinate of bounds to test.
	 *
	 * @return  <code>true</code> if the other bounds are contained wihin these
	 *          bounds; <code>false</code> otherwise.
	 */
	public boolean contains( final double minX, final double minY, final double maxX, final double maxY )
	{
		return ( ( minX >= getMinX() ) && ( maxX <= getMaxX() ) && ( minY >= getMinY() ) && ( maxY <= getMaxY() ) );
	}

	/**
	 * Create intersection between this and the given bounds.
	 *
	 * @param   other   Bounds to intersect with.
	 *
	 * @return  Intersection of bounds;
	 *          <code>null</code> if the bounds are disjunct.
	 */
	public VarBounds2D intersect( final Bounds2D other )
	{
		return intersect( this, other );
	}

	/**
	 * Create intersection of two bounds.
	 *
	 * @param   bounds1     First bounds to intersect.
	 * @param   bounds2     Second bounds to intersect.
	 *
	 * @return  Intersection of bounds;
	 *          <code>null</code> if the bounds are disjunct.
	 */
	public static VarBounds2D intersect( final Bounds2D bounds1, final Bounds2D bounds2 )
	{
		final double x1 = Math.max( bounds1.getMinX(), bounds2.getMinX() );
		final double y1 = Math.max( bounds1.getMinY(), bounds2.getMinY() );
		final double x2 = Math.min( bounds1.getMaxX(), bounds2.getMaxX() );
		final double y2 = Math.min( bounds1.getMaxY(), bounds2.getMaxY() );

		return ( ( ( x2 >= x1 ) && ( y2 >= y1 ) ) ) ? new VarBounds2D( x1, y1, x2 - x1, y2 - y1 ) : null;
	}

	/**
	 * Test whether these bounds intersect with the given bounds.
	 *
	 * @param   other   Bounds to test for intersection.
	 *
	 * @return  <code>true</code> if the bounds intersect;
	 *          <code>false</code> otherwise.
	 */
	public boolean intersects( final Bounds2D other )
	{
		return intersects( other.getMinX(), other.getMinY(), other.getMaxX(), other.getMaxY() );
	}

	/**
	 * Test whether these bounds intersect with the given bounds.
	 *
	 * @param   minX    Minimum X coordinate of bounds to test.
	 * @param   minY    Minimum Y coordinate of bounds to test.
	 * @param   maxX    Maximum X coordinate of bounds to test.
	 * @param   maxY    Maximum Y coordinate of bounds to test.
	 *
	 * @return  <code>true</code> if the bounds intersect;
	 *          <code>false</code> otherwise.
	 */
	public boolean intersects( final double minX, final double minY, final double maxX, final double maxY )
	{
		return ( ( minX <= getMaxX() ) && ( maxX >= getMinX() ) && ( minY <= getMaxY() ) && ( maxY >= getMinY() ) );
	}

	/**
	 * Tests if the specified line segment intersects these bounds.
	 *
	 * @param   startX  X coordinate of line segment start point.
	 * @param   startY  Y coordinate of line segment start point.
	 * @param   endX    X coordinate of line segment end point.
	 * @param   endY    Y coordinate of line segment end point.
	 *
	 * @return  <code>true</code> if the line segment intersects these bounds;
	 *          <code>false</code> otherwise.
	 */
	public boolean intersectsLine( final double startX, final double startY, final double endX, final double endY )
	{
		final double minX = getMinX();
		final double minY = getMinY();
		final double maxX = getMaxX();
		final double maxY = getMaxY();

		boolean result = ( ( minX <= maxX ) && ( minY <= maxY ) );
		if ( result )
		{
			final boolean endOutMinX = ( endX < minX );
			final boolean endOutMinY = ( endY < minY );
			final boolean endOutMaxX = ( endX > maxX );
			final boolean endOutMaxY = ( endY > maxY );

			if ( endOutMinX || endOutMaxX || endOutMinY || endOutMaxY )
			{
				double curX = startX;
				double curY = startY;

				while ( true )
				{
					final boolean curOutMinX = ( curX < minX );
					final boolean curOutMinY = ( curY < minY );
					final boolean curOutMaxX = ( curX > maxX );
					final boolean curOutMaxY = ( curY > maxY );

					if ( ( curOutMinX && endOutMinX ) || ( curOutMinY && endOutMinY ) ||( curOutMaxX && endOutMaxX ) || ( curOutMaxY && endOutMaxY ) )
					{
						result = false;
						break;
					}

					if ( curOutMinX )
					{
						curY += ( minX - curX ) * ( endY - curY ) / ( endX - curX );
						curX = minX;
					}
					else if ( curOutMaxX )
					{
						curY += ( maxX - curX ) * ( endY - curY ) / ( endX - curX );
						curX = minX;
					}
					else if ( curOutMinY )
					{
						curX += ( minY - curY ) * ( endX - curX ) / ( endY - curY );
						curY = minY;
					}
					else if ( curOutMaxY )
					{
						curX += ( maxY - curY ) * ( endX - curX ) / ( endY - curY );
						curY = maxY;
					}
					else
					{
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Create union between this and the given bounds.
	 *
	 * @param   other   Bounds to union with.
	 *
	 * @return  Union of bounds.
	 */
	public VarBounds2D union( final Bounds2D other )
	{
		return union( this, other );
	}

	/**
	 * Create union of two bounds.
	 *
	 * @param   bounds1     First bounds to union.
	 * @param   bounds2     Second bounds to union.
	 *
	 * @return  Union of bounds.
	 */
	public static VarBounds2D union( final Bounds2D bounds1, final Bounds2D bounds2 )
	{
		final double x1 = Math.min( bounds1.getMinX(), bounds2.getMinX() );
		final double x2 = Math.max( bounds1.getMaxX(), bounds2.getMaxX() );
		final double y1 = Math.min( bounds1.getMinY(), bounds2.getMinY() );
		final double y2 = Math.max( bounds1.getMaxY(), bounds2.getMaxY() );

		return new VarBounds2D( x1, y1, x2 - x1, y2 - y1 );
	}

	@Override
	public int hashCode()
	{
		final long bits = Double.doubleToLongBits( getMinX() ) +
		                  Double.doubleToLongBits( getMinY() ) * 37L +
		                  Double.doubleToLongBits( getMaxX() ) * 43L +
		                  Double.doubleToLongBits( getMaxY() ) * 47L;

		return ( ( (int)bits ) ^ ( (int)( bits >> 32 ) ) );
	}

	@Override
	public boolean equals( final Object object )
	{
		final boolean result;

		if ( object == this )
		{
			result = true;
		}
		else if ( object instanceof Bounds2D )
		{
			final Bounds2D other = (Bounds2D)object;

			result =  ( ( getMinX() == other.getMinX() ) &&
			            ( getMinY() == other.getMinY() ) &&
			            ( getMaxX() == other.getMaxX() ) &&
			            ( getMaxY() == other.getMaxY() ) );
		}
		else
		{
			result = false;
		}

		return result;
	}
}
