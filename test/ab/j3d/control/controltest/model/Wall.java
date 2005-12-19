/*
 * $Id$
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
package ab.j3d.control.controltest.model;

import ab.j3d.Matrix3D;

/**
 * This class models a wall. A wall is always a box shape. The size of this box
 * may differ for each axis. The wall can be rotated around the z axis, and it
 * can be moved on the x,y plane.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class Wall
	extends SceneElement
	implements Resizable
{
	/**
	 * The x coordinate of the location of this wall.
	 */
	private double _x;

	/**
	 * The y coordinate of the location of this wall.
	 */
	private double _y;

	/**
	 * This wall's rotation around the z-axis.
	 */
	private double _rotation;

	/**
	 * The height of this wall.
	 */
	private double _height;

	/**
	 * The width of this wall.
	 */
	private double _width;

	/**
	 * The depth of this wall.
	 */
	private double _depth;

	/**
	 * Construct new {@link Wall}.
	 *
	 * @param   height  height of the wall.
	 * @param   width   width of the wall.
	 * @param   depth   depth of the wall.
	 */
	public Wall( final double height , final double width , final double depth )
	{
		this( 0.0 , 0.0 , 0.0 , height , width , depth );
	}

	/**
	 * Construct new {@link Wall}.
	 *
	 * @param   x           x coordinate of the location of the wall.
	 * @param   y           y coordinate of the location of the wall.
	 * @param   rotation    rotation of the wall in degrees.
	 * @param   height      height of the wall.
	 * @param   width       width of the wall.
	 * @param   depth       depth of the wall.
	 */
	public Wall( final double x , final double y , final double rotation , final double height , final double width , final double depth )
	{
		_x        = x;
		_y        = y;
		_rotation = rotation;
		_height   = height;
		_width    = width;
		_depth    = depth;
	}

	/**
	 * Returns a {@link Matrix3D} with the transformation of this element. This
	 * should include translation, rotation and scale where needed.
	 *
	 * @return  Transformation matrix of this element.
	 */
	public Matrix3D getTransform()
	{
		return Matrix3D.getTransform( 0.0 , 0.0 , _rotation , _x , _y , 0.0);
	}

	/**
	 * Returns the x coordinate of the location of this wall.
	 *
	 * @return  X coordinate of the location of this wall
	 */
	public double getX()
	{
		return _x;
	}

	/**
	 * Sets the x coordinate of the location of this wall.
	 *
	 * @param   x   new x coordinate.
	 */
	public void setX( final double x )
	{
		_x = x;
		elementChanged();
	}

	/**
	 * Returns the y coordinate of the location of this wall.
	 *
	 * @return  Y coordinate of the location of this wall
	 */
	public double getY()
	{
		return _y;
	}

	/**
	 * Sets the y coordinate of the location of this wall.
	 *
	 * @param   y   new y coordinate.
	 */
	public void setY( final double y )
	{
		_y = y;
		elementChanged();
	}

	/**
	 * Sets the new location of this wall.
	 *
	 * @param   x   x coordinate of the new location
	 * @param   y   y coordinate of the new location
	 */
	public void setLocation( final double x , final double y )
	{
		_x = x;
		_y = y;
		elementChanged();
	}

	/**
	 * Returns this wall's rotation. This is always a rotation around the
	 * z-axis, in degrees.
	 *
	 * @return  This wall's rotation in degrees.
	 */
	public double getRotation()
	{
		return _rotation;
	}

	/**
	 * Sets the new rotation of this wall. This is the rotation around
	 * the z axis, in degrees
	 *
	 * @param   r   new rotation of the wall, in degrees.
	 */
	public void setRotation( final double r )
	{
		_rotation = r;
		elementChanged();
	}

	/**
	 * Returns the size of this element in the x dimension.
	 *
	 * @return x dimension of this element.
	 */
	public double getXSize()
	{
		return _width;
	}

	/**
	 * Returns the size of this element in the y dimension.
	 *
	 * @return The y dimension of this element.
	 */
	public double getYSize()
	{
		return _depth;
	}

	/**
	 * Returns the size of this element in the z dimension.
	 *
	 * @return Z dimension of this element.
	 */
	public double getZSize()
	{
		return _height;
	}

	/**
	 * Sets the size of this element in the x dimension.
	 *
	 * @param x The new x dimension of this element.
	 */
	public void setXSize( final double x )
	{
		setSize( x , _depth , _height );
	}

	/**
	 * Sets the size of this element in the y dimension.
	 *
	 * @param y new y dimension of this element.
	 */
	public void setYSize( final double y )
	{
		setSize( _width , y , _height );
	}

	/**
	 * Sets the size of this element in the z dimension.
	 *
	 * @param z new z dimension of this element.
	 */
	public void setZSize( final double z )
	{
		setSize( _width , _depth , z );
	}

	/**
	 * Set the new size of this element.
	 *
	 * @param x new x dimension of this element.
	 * @param y new y dimension of this element.
	 * @param z new z dimension of this element.
	 */
	public void setSize( final double x , final double y , final double z )
	{
		boolean change = false;

		if ( x > 0.0 && _width != x )
		{
			_width = x;
			change = true;
		}
		if ( y > 0.0 && _depth != y )
		{
			_depth = y;
			change = true;
		}
		if ( z > 0.0 && _height != z )
		{
			_height = z;
			change = true;
		}

		if ( change )
		{
			elementChanged();
		}
	}
}
