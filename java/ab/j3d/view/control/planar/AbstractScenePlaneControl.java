/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2010-2010
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

import java.awt.geom.*;

import ab.j3d.control.*;
import ab.j3d.view.*;
import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Implements common functionality defined by {@link ScenePlaneControl}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public abstract class AbstractScenePlaneControl
	implements ScenePlaneControl
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
	protected AbstractScenePlaneControl()
	{
		_startX = Double.NaN;
		_startY = Double.NaN;
		_endX   = Double.NaN;
		_endY   = Double.NaN;
		_active = false;
	}

	@Override
	public boolean isVisible( final View3D view )
	{
		return isEnabled();
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public boolean mousePressed( final ControlInputEvent event , final double x , final double y )
	{
		_startX = x;
		_startY = y;
		_endX   = x;
		_endY   = y;
		_active = true;

		return isActive();
	}

	@Override
	public void mouseDragged( final ControlInputEvent event , final double x , final double y )
	{
		_endX = x;
		_endY = y;
	}

	@Override
	public void mouseReleased( final ControlInputEvent event , final double x , final double y )
	{
		_endX   = x;
		_endY   = y;
		_active = false;
	}

	@Override
	public void mouseMoved( final ControlInputEvent event, final double x, final double y )
	{
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
	@Nullable
	public Point2D getDirection()
	{
		final Point2D result;

		final double dx = _endX - _startX;
		final double dy = _endY - _startY;

		if ( !MathTools.almostEqual( dx , 0.0 ) && !MathTools.almostEqual( dx , 0.0 ) )
		{
			final double l = Math.sqrt( dx * dx + dy * dy );

			result = new Point2D.Double( dx / l, dy / l );
		}
		else
		{
			result = null;
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
