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
 * This is an implementation of {@link Bounds2D} with variable properties.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class VarBounds2D
	extends Bounds2D
{
	/**
	 * Minimum X coordinate of these bounds.
	 */
	private double _minX;

	/**
	 * Minimum Y coordinate of these bounds.
	 */
	private double _minY;

	/**
	 * Maximum X coordinate of these bounds.
	 */
	private double _maxX;

	/**
	 * Maximum Y coordinate of these bounds.
	 */
	private double _maxY;

	/**
	 * Constructs bounds from other bounds.
	 *
	 * @param   bounds  Bounds whose properties to copy.
	 */
	public VarBounds2D( final Bounds2D bounds )
	{
		this( bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY() );
	}

	/**
	 * Construct bounds.
	 *
	 * @param   minX    Minimum X coordinate of bounds.
	 * @param   minY    Minimum Y coordinate of bounds.
	 * @param   maxX    Maximum X coordinate of bounds.
	 * @param   maxY    Maximum Y coordinate of bounds.
	 */
	public VarBounds2D( final double minX, final double minY, final double maxX, final double maxY )
	{
		_minX = minX;
		_minY = minY;
		_maxX = maxX;
		_maxY = maxY;
	}

	@Override
	public ConstBounds2D toConst()
	{
		return new ConstBounds2D( this );
	}

	@Override
	public VarBounds2D toVar()
	{
		return this;
	}

	/**
	 * Set bounds.
	 *
	 * @param   minX    Minimum X coordinate of bounds.
	 * @param   minY    Minimum Y coordinate of bounds.
	 * @param   maxX    Maximum X coordinate of bounds.
	 * @param   maxY    Maximum Y coordinate of bounds.
	 */
	public void setBounds( final double minX, final double minY, final double maxX, final double maxY )
	{
		_minX = minX;
		_minY = minY;
		_maxX = maxX;
		_maxY = maxY;
	}

	/**
	 * Get minimum X coordinate of bounds.
	 *
	 * @return  Minimum X coordinate of bounds.
	 */
	@Override
	public double getMinX()
	{
		return _minX;
	}

	/**
	 * Set minimum X coordinate of bounds.
	 *
	 * @return  Minimum X coordinate of bounds.
	 */
	public double setMinX()
	{
		return _minX;
	}

	/**
	 * Get minimum Y coordinate of bounds.
	 *
	 * @return  Minimum Y coordinate of bounds.
	 */
	@Override
	public double getMinY()
	{
		return _minY;
	}

	/**
	 * Set minimum Y coordinate of bounds.
	 *
	 * @return  Minimum Y coordinate of bounds.
	 */
	public double setMinY()
	{
		return _minY;
	}

	/**
	 * Get maximum X coordinate of bounds.
	 *
	 * @return  Maximum X coordinate of bounds.
	 */
	@Override
	public double getMaxX()
	{
		return _maxX;
	}

	/**
	 * Set maximum X coordinate of bounds.
	 *
	 * @return  Maximum X coordinate of bounds.
	 */
	public double setMaxX()
	{
		return _maxX;
	}

	/**
	 * Get maximum Y coordinate of bounds.
	 *
	 * @return  Maximum Y coordinate of bounds.
	 */
	@Override
	public double getMaxY()
	{
		return _maxY;
	}

	/**
	 * Set maximum Y coordinate of bounds.
	 *
	 * @return  Maximum Y coordinate of bounds.
	 */
	public double setMaxY()
	{
		return _maxY;
	}

	/**
	 * Add point to bounds. If needed, this extends the bounds so that the given
	 * point is contained in these bounds.
	 *
	 * @param   point   Point to add.
	 */
	public void add( final Vector2D point )
	{
		add( point.getX(), point.getY() );
	}

	/**
	 * Add point to bounds. If needed, this extends the bounds so that the given
	 * point is contained in these bounds.
	 *
	 * @param   x   X coordinate of point to add.
	 * @param   y   Y coordinate of point to add.
	 */
	public void add( final double x, final double y )
	{
		setBounds( Math.min( getMinX(), x ), Math.min( getMinY(), y ), Math.max( getMaxX(), x ), Math.max( getMaxY(), y ) );
	}

	/**
	 * Adds bounds to these bounds. This creates a union of the two bounds.
	 *
	 * @param   bounds  Bounds to add.
	 */
	public void add( final VarBounds2D bounds )
	{
		setBounds( Math.min( getMinX(), bounds.getMinX() ), Math.min( getMinY(), bounds.getMinY() ), Math.max( getMaxX(), bounds.getMaxX() ), Math.max( getMaxY(), bounds.getMaxY() ) );
	}
}
