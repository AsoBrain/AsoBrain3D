/*
 * $Id$
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
package ab.j3d.control;

import java.awt.*;

import org.jetbrains.annotations.*;

/**
 * Represent an edge on one of the sides of a box control.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class BoxEdge
{
	/**
	 * Default color for edges.
	 */
	public static final Color DEFAULT_COLOR = new Color( 0.0f, 0.0f, 1.0f, 0.25f );

	/**
	 * Delegate that handles actions performed on the edge.
	 */
	private BoxControlDelegate _listener;

	/**
	 * Color of the edge.
	 */
	@Nullable
	private Color _color;

	/**
	 * Width of the edge, in scene units.
	 */
	private double _width;

	/**
	 * Constructs a new instance.
	 */
	public BoxEdge()
	{
		_listener = null;
		_color = DEFAULT_COLOR;
		_width = 0.1;
	}

	/**
	 * Returns the delegate that handles actions performed on the edge.
	 *
	 * @return  Delegate for this edge.
	 */
	public BoxControlDelegate getListener()
	{
		return _listener;
	}

	/**
	 * Sets the delegate that handles actions performed on the edge.
	 *
	 * @param   listener    Delegate to be set.
	 */
	public void setListener( final BoxControlDelegate listener )
	{
		_listener = listener;
	}

	/**
	 * Returns the color of the edge.
	 *
	 * @return  Edge color; <code>null</code> for invisible.
	 */
	@Nullable
	public Color getColor()
	{
		return _color;
	}

	/**
	 * Sets the color of the edge.
	 *
	 * @param   color   Color to be set.
	 */
	public void setColor( @Nullable final Color color )
	{
		_color = color;
	}

	/**
	 * Returns the width of the edge.
	 *
	 * @return  Edge width, in scene units.
	 */
	public double getWidth()
	{
		return _width;
	}

	/**
	 * Sets the width of the edge.
	 *
	 * @param   width   Width to be set.
	 */
	public void setWidth( final double width )
	{
		_width = width;
	}

	/**
	 * Returns whether the edge is enabled, i.e. capable of receiving events.
	 *
	 * @return  <code>true</code> if the edge is enabled.
	 */
	public boolean isEnabled()
	{
		return ( _listener != null ) && _listener.isEnabled();
	}

	/**
	 * Returns whether the edge is visible.
	 *
	 * @return  <code>true</code> if the edge is visible.
	 */
	public boolean isVisible()
	{
		return ( _listener != null ) && _listener.isVisible();
	}
}
