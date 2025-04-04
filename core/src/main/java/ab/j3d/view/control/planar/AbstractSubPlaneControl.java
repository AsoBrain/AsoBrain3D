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
package ab.j3d.view.control.planar;

import java.awt.geom.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.model.*;


/**
 * This class provides some basic functionality of a {@link SubPlaneControl}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class AbstractSubPlaneControl
	implements SubPlaneControl
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
	protected AbstractSubPlaneControl()
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

	public boolean mousePressed( final ControlInputEvent event, final ContentNode contentNode, final double x, final double y )
	{
		_startX = x;
		_startY = y;
		_endX   = x;
		_endY   = y;
		_active = true;

		return isActive();
	}

	public void mouseDragged( final ControlInputEvent event, final ContentNode contentNode, final double x, final double y )
	{
		_endX = x;
		_endY = y;
	}

	public void mouseReleased( final ControlInputEvent event, final ContentNode contentNode, final double x, final double y )
	{
		_endX   = x;
		_endY   = y;
		_active = false;
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
	 * Get minimum X coordinate of dragged area on drag plane.
	 *
	 * @return  Minimum X coordinate of dragged area on drag plane.
	 */
	public double getMinX()
	{
		return Math.min( _startX, _endX );
	}

	/**
	 * Get minimum Y coordinate of dragged area on drag plane.
	 *
	 * @return  Minimum Y coordinate of dragged area on drag plane.
	 */
	public double getMinY()
	{
		return Math.min( _startY, _endY );
	}

	/**
	 * Get maximum Y coordinate of dragged area on drag plane.
	 *
	 * @return  Maximum Y coordinate of dragged area on drag plane.
	 */
	public double getMaxX()
	{
		return Math.max( _startX, _endX );
	}

	/**
	 * Get maximum Y coordinate of dragged area on drag plane.
	 *
	 * @return  Maximum Y coordinate of dragged area on drag plane.
	 */
	public double getMaxY()
	{
		return Math.max( _startY, _endY );
	}

	/**
	 * Get width of dragged area on drag plane.
	 *
	 * @return  Width of dragged area on drag plane.
	 */
	public double getWidth()
	{
		return Math.abs( _endX - _startX );
	}

	/**
	 * Get height of dragged area on drag plane.
	 *
	 * @return  Height of dragged area on drag plane.
	 */
	public double getHeight()
	{
		return Math.abs( _endX - _startX );
	}

	/**
	 * Get rectangle that represents the dragged area on the drag plane.
	 *
	 * @return  Rectangle that represents the dragged area on the drag plane.
	 */
	public Rectangle2D getRectangle2D()
	{
		return new Rectangle2D.Double( getMinX(), getMinY(), getWidth(), getHeight() );
	}

	/**
	 * Get line that represents the drag movement on the drag plane from the
	 * drag start point to the drag end point.
	 *
	 * @return  Line that represents the drag movement.
	 */
	public Line2D getLine2D()
	{
		return new Line2D.Double( _startX, _startY, _endX, _endY );
	}

	/**
	 * Get direction from start to end point of drag movement.
	 *
	 * @return  Direction from start to end point of drag movement.
	 */
	public Point2D getDirection()
	{
		final Point2D result;

		final double dx = _endX - _startX;
		final double dy = _endY - _startY;

		if ( !MathTools.almostEqual( dx, 0.0 ) && !MathTools.almostEqual( dx, 0.0 ) )
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
	 * Get end X coordinate of drag operation bound to limits of drag plane.
	 *
	 * @return  End X coordinate of drag operation bound to limits of drag plane..
	 */
	public double getBoundEndX()
	{
		return Math.min( Math.max( 0.0, _endX ), getPlaneWidth() );
	}

	/**
	 * Get end Y coordinate of drag operation bound to limits of drag plane.
	 *
	 * @return  End Y coordinate of drag operation bound to limits of drag plane..
	 */
	public double getBoundEndY()
	{
		return Math.min( Math.max( 0.0, _endY ), getPlaneHeight() );
	}

	/**
	 * Get minimum X coordinate of dragged area on drag plane. The dragged area
	 * is bound to the limits defined by this drag behavior.
	 *
	 * @return  Minimum X coordinate of bound dragged area on drag plane.
	 */
	public double getBoundMinX()
	{
		return Math.max( 0.0, getMinX() );
	}

	/**
	 * Get minimum Y coordinate of dragged area on drag plane. The dragged area
	 * is bound to the limits defined by this drag behavior.
	 *
	 * @return  Minimum Y coordinate of bound dragged area on drag plane.
	 */
	public double getBoundMinY()
	{
		return Math.max( 0.0, getMinY() );
	}

	/**
	 * Get maximum X coordinate of dragged area on drag plane. The dragged area
	 * is bound to the limits defined by this drag behavior.
	 *
	 * @return  Maximum X coordinate of bound dragged area on drag plane.
	 */
	public double getBoundMaxX()
	{
		return Math.min( getPlaneWidth(), getMaxX() );
	}

	/**
	 * Get maximum Y coordinate of dragged area on drag plane. The dragged area
	 * is bound to the limits defined by this drag behavior.
	 *
	 * @return  Maximum Y coordinate of bound dragged area on drag plane.
	 */
	public double getBoundMaxY()
	{
		return Math.min( getPlaneHeight(), getMaxY() );
	}

	/**
	 * Get width of dragged area on drag plane. The dragged area is bound to the
	 * limits defined by this drag behavior.
	 *
	 * @return  Width of dragged area on drag plane.
	 */
	public double getBoundWidth()
	{
		return getBoundMaxX() - getBoundMinX();
	}

	/**
	 * Get height of dragged area on drag plane. The dragged area is bound to
	 * the limits defined by this drag behavior.
	 *
	 * @return  Height of dragged area on drag plane.
	 */
	public double getBoundHeight()
	{
		return getBoundMaxY() - getBoundMinY();
	}

	/**
	 * Get rectangle that represents the dragged area on the drag plane. The
	 * dragged area is bound to the limits defined by this drag behavior.
	 *
	 * @return  Rectangle that represents the dragged area on the drag plane.
	 */
	public Rectangle2D getBoundRectangle2D()
	{
		return new Rectangle2D.Double( getBoundMinX(), getBoundMinY(), getBoundWidth(), getBoundHeight() );
	}

	/**
	 * Get line that represents the drag movement on the drag plane from the
	 * drag start point to the drag end point. The start and end point are bound
	 * to the limits defined by this drag behavior.
	 *
	 * @return  Line that represents the drag movement.
	 */
	public Line2D getBoundLine2D()
	{
		final double startX = _startX;
		final double startY = _startY;
		final double maxX = getPlaneWidth();
		final double maxY = getPlaneHeight();

		double endX = _endX;
		double endY = _endY;

		if ( endX < 0.0 )
		{
			endY = startY - startX * ( endY - startY ) / ( endX - startX );
			endX = 0.0;
		}

		if ( endX > maxX )
		{
			endY = startY + ( maxX - startX ) * ( endY - startY ) / ( endX - startX );
			endX = maxX;
		}

		if ( endY < 0.0 )
		{
			endX = startX - startY * ( endX - startX ) / ( endY - startY );
			endY = 0.0;
		}

		if ( endY > maxY )
		{
			endX = startX + ( maxY - startY ) * ( endX - startX ) / ( endY - startY );
			endY = maxY;
		}

		return new Line2D.Double( startX, startY, endX, endY );
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
