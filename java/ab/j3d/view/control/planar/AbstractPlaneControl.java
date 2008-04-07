/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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
package ab.j3d.view.control.planar;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import ab.j3d.control.ControlInputEvent;
import ab.j3d.view.ViewModelNode;

import com.numdata.oss.MathTools;

/**
 * This class provides some basic functionality of a {@link PlaneControl}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class AbstractPlaneControl
	implements PlaneControl
{
	/**
	 * Start X coordinate of drag operation.
	 */
	private double _startX;

	/**
	 * Start Y coordinate of drag operation.
	 */
	private double _startY;

	/**
	 * End X coordinate of drag operation.
	 */
	private double _endX;

	/**
	 * End Y coordinate of drag operation.
	 */
	private double _endY;

	/**
	 * Flag to indicate that this behavior is 'active'.
	 */
	private boolean _active;

	/**
	 * Construct new control.
	 */
	protected AbstractPlaneControl()
	{
		_startX = Double.NaN;
		_startY = Double.NaN;
		_endX   = Double.NaN;
		_endY   = Double.NaN;
		_active = false;
	}

	public boolean isEnabled()
	{
		return true;
	}

	public void mousePressed( final ControlInputEvent event , final ViewModelNode viewModelNode , final double x , final double y )
	{
		_startX = x;
		_startY = y;
		_endX   = x;
		_endY   = y;
		_active = true;
	}

	public void mouseDragged( final ControlInputEvent event , final ViewModelNode viewModelNode , final double x , final double y )
	{
		_endX = x;
		_endY = y;
	}

	public void mouseReleased( final ControlInputEvent event , final ViewModelNode viewModelNode , final double x , final double y )
	{
		_endX   = x;
		_endY   = y;
		_active = false;
	}

	/**
	 * Get start point of drag operation.
	 *
	 * @return  Start point of drag operation.
	 */
	public Point2D getStart()
	{
		return new Point2D.Double( _startX , _startY );
	}

	/**
	 * Get start X coordinate of drag operation.
	 *
	 * @return  Start X coordinate of drag operation.
	 */
	public double getStartX()
	{
		return _startX;
	}

	/**
	 * Get start Y coordinate of drag operation.
	 *
	 * @return  Start Y coordinate of drag operation.
	 */
	public double getStartY()
	{
		return _startY;
	}

	/**
	 * Get end point of drag operation.
	 *
	 * @return  End point of drag operation.
	 */
	public Point2D getEnd()
	{
		return new Point2D.Double( _endX , _endY );
	}

	/**
	 * Get end X coordinate of drag operation.
	 *
	 * @return  End X coordinate of drag operation.
	 */
	public double getEndX()
	{
		return _endX;
	}

	/**
	 * Get end Y coordinate of drag operation.
	 *
	 * @return  End Y coordinate of drag operation.
	 */
	public double getEndY()
	{
		return _endY;
	}

	/**
	 * Get movement of drag operation.
	 *
	 * @return  Movement of drag operation.
	 */
	public Point2D getDelta()
	{
		return new Point2D.Double( getDeltaX() , getDeltaY() );
	}

	/**
	 * Get movement along X axis of drag operation.
	 *
	 * @return  Movement along X axis of drag operation.
	 */
	public double getDeltaX()
	{
		return _endX - _startX;
	}

	/**
	 * Get movement along Y axis of drag operation.
	 *
	 * @return  Movement along Y axis of drag operation.
	 */
	public double getDeltaY()
	{
		return _endX - _startX;
	}

	/**
	 * Get line that represents the drag movement on the drag plane from the
	 * drag start point to the drag end point.
	 *
	 * @return  Line that represents the drag movement.
	 */
	public Line2D getLine2D()
	{
		return new Line2D.Double( _startX , _startY , _endX , _endY );
	}

	/**
	 * Get direction from start to end point of drag movement.
	 *
	 * @return  Direction from start to end point of drag movement.
	 */
	public Point2D getDirection()
	{
		final double distance = getDistance();
		return Double.isNaN( distance ) ? null : new Point2D.Double( getDeltaX() / distance , getDeltaY() / distance );
	}

	/**
	 * Get distance between the start and end point of drag movement.
	 *
	 * @return  Distance between the start and end point of drag movement.
	 */
	public double getDistance()
	{
		final double result;

		final double dx = getDeltaX();
		final double dy = getDeltaY();

		if ( !MathTools.almostEqual( dx , 0.0 ) && !MathTools.almostEqual( dx , 0.0 ) )
		{
			result = Math.sqrt( dx * dx + dy * dy );
		}
		else
		{
			result = Double.NaN;
		}

		return result;
	}

	/**
	 * Test if this behavior is active. Meaning that is currently being used.
	 *
	 * @return  <code>true</code> if this behavior is active;
	 *          <code>false</code> otherwise.
	 */
	public boolean isActive()
	{
		return _active;
	}
}