/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2001-2004 Numdata BV
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
 * This class describes 2D boundaries.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Bounds2D
{
	/**
	 * Lower bound X coordinate.
	 */
	public final float minX;

	/**
	 * Lower bound Y coordinate.
	 */
	public final float minY;

	/**
	 * Upper bound X coordinate.
	 */
	public final float maxX;

	/**
	 * Upper bound Y coordinate.
	 */
	public final float maxY;

	/**
	 * Construct boundaries.
	 *
	 * @param   minX      Lower bound X coordinate.
	 * @param   minY      Lower bound Y coordinate.
	 * @param   maxX      Upper bound X coordinate.
	 * @param   maxY      Upper bound Y coordinate.
	 */
	public Bounds2D( final float minX , final float minY , final float maxX , final float maxY )
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
}
