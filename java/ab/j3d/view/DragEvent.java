/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2004 Numdata BV
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
package ab.j3d.view;

import java.util.EventObject;

/**
 * This type of event is fired by the <code>DragSupport</code> class.
 *
 * @see     DragSupport
 * @see     DragListener
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class DragEvent
	extends EventObject
{
	/**
	 * Event type: drag operation started.
	 */
	public static final int DRAG_START = 0;

	/**
	 * Event type: drag operation in progress (dragged to new position).
	 */
	public static final int DRAG_TO = 1;

	/**
	 * Event type: drag operation stopped.
	 */
	public static final int DRAG_STOP = 2;

	/**
	 * Event type.
	 */
	private final int _id;

	/**
	 * Button number on pointing device to select operation mode.
	 */
	private final int _buttonNumber;

	/**
	 * Number of times the button was pressed.
	 */
	private final int _clickCount;

	/**
	 * Start X coordinate of drag operation (pixels).
	 */
	private final int _startX;

	/**
	 * Start Y coordinate of drag operation (pixels).
	 */
	private final int _startY;

	/**
	 * Delta X relative to start of drag operation in pixels.
	 */
	private final int _deltaX;

	/**
	 * Delta Y relative to start of drag operation in pixels.
	 */
	private final int _deltaY;

	/**
	 * Delta X relative to start of drag operation in decimal degrees.
	 */
	private final double _deltaDegX;

	/**
	 * Delta Y relative to start of drag operation in decimal degrees.
	 */
	private final double _deltaDegY;

	/**
	 * Delta X relative to start of drag operation in radians.
	 */
	private final double _deltaRadX;

	/**
	 * Delta Y relative to start of drag operation in radians.
	 */
	private final double _deltaRadY;

	/**
	 * Delta X relative to start of drag operation in model units.
	 */
	private final double _deltaUnitX;

	/**
	 * Delta Y relative to start of drag operation in model units.
	 */
	private final double _deltaUnitY;

	/**
	 * Construct event.
	 *
	 * @param   source          Source from where the event originated.
	 * @param   id              Event type.
	 * @param   buttonNumber    Button number on pointing device to select operation mode.
	 * @param   clickCount      Number of times the button was pressed.
	 * @param   startX          X start coordinate of drag operation in pixels.
	 * @param   startY          Y start coordinate of drag operation in pixels.
	 * @param   toX             Current X end coordinate of drag operation in pixels.
	 * @param   toY             Current Y end coordinate of drag operation in pixels.
	 * @param   toDegrees       Multiplier to translate pixels to decimal degrees.
	 * @param   toRadians       Multiplier to translate pixels to radians.
	 * @param   toUnits         Multiplier to translate pixels to model units.
	 */
	public DragEvent( final DragSupport source , final int id , final int buttonNumber , final int clickCount , final int startX , final int startY , final int toX , final int toY , final double toDegrees , final double toRadians , final double toUnits )
	{
		super( source );

		final int deltaX = toX - startX;
		final int deltaY = toY - startY;

		final double dx = (double)deltaX;
		final double dy = (double)deltaY;

		_id           = id;
		_buttonNumber = buttonNumber;
		_clickCount   = clickCount;
		_startX       = startX;
		_startY       = startY;
		_deltaX       = deltaX;
		_deltaY       = deltaY;
		_deltaDegX    =  toDegrees * dx;
		_deltaDegY    = -toDegrees * dy;
		_deltaRadX    =  toRadians * dx;
		_deltaRadY    = -toRadians * dy;
		_deltaUnitX   =  toUnits   * dx;
		_deltaUnitY   = -toUnits   * dy;
	}

	/**
	 * Construct event.
	 *
	 * @param   source          Source from where the event originated.
	 * @param   id              Event type.
	 * @param   buttonNumber    Button number on pointing device to select operation mode.
	 * @param   clickCount      Number of times the button was pressed.
	 * @param   startX          Start X coordinate of drag operation (pixels).
	 * @param   startY          Start Y coordinate of drag operation (pixels).
	 * @param   deltaX          Delta X relative to start of drag operation in pixels.
	 * @param   deltaY          Delta Y relative to start of drag operation in pixels.
	 * @param   deltaDegX       Delta X relative to start of drag operation in decimal degrees.
	 * @param   deltaDegY       Delta Y relative to start of drag operation in decimal degrees.
	 * @param   deltaRadX       Delta X relative to start of drag operation in radians.
	 * @param   deltaRadY       Delta Y relative to start of drag operation in radians.
	 * @param   deltaUnitX      Delta X relative to start of drag operation in model units.
	 * @param   deltaUnitY      Delta Y relative to start of drag operation in model units.
	 */
	public DragEvent( final DragSupport source , final int id , final int buttonNumber , final int clickCount , final int startX , final int startY , final int deltaX , final int deltaY , final double deltaDegX , final double deltaDegY , final double deltaRadX , final double deltaRadY , final double deltaUnitX , final double deltaUnitY )
	{
		super( source );

		_id           = id;
		_buttonNumber = buttonNumber;
		_clickCount   = clickCount;
		_startX       = startX;
		_startY       = startY;
		_deltaX       = deltaX;
		_deltaY       = deltaY;
		_deltaDegX    = deltaDegX;
		_deltaDegY    = deltaDegY;
		_deltaRadX    = deltaRadX;
		_deltaRadY    = deltaRadY;
		_deltaUnitX   = deltaUnitX;
		_deltaUnitY   = deltaUnitY;
	}

	/**
	 * Get event type.
	 *
	 * @return  Event type (<code>DRAG_START , DRAG_TO, or DRAG_STOP</code>).
	 */
	public int getID()
	{
		return _id;
	}

	/**
	 * Get button number on pointing device to select operation mode.
	 *
	 * @return  Button number on pointing device to select operation mode.
	 */
	public int getButtonNumber()
	{
		return _buttonNumber;
	}

	/**
	 * Get number of times the button was pressed.
	 *
	 * @return  Number of times the button was pressed.
	 */
	public int getClickCount()
	{
		return _clickCount;
	}

	/**
	 * Get start X coordinate of drag operation (pixels).
	 *
	 * @return  Start X coordinate of drag operation (pixels).
	 */
	public int getStartX()
	{
		return _startX;
	}

	/**
	 * Get start Y coordinate of drag operation (pixels).
	 *
	 * @return  Start Y coordinate of drag operation (pixels).
	 */
	public int getStartY()
	{
		return _startY;
	}

	/**
	 * Get delta X relative to start of drag operation in pixels.
	 *
	 * @return  Delta X relative to start of drag operation in pixels.
	 */
	public int getDeltaX()
	{
		return _deltaX;
	}

	/**
	 * Get delta Y relative to start of drag operation in pixels.
	 *
	 * @return  Delta Y relative to start of drag operation in pixels.
	 */
	public int getDeltaY()
	{
		return _deltaY;
	}

	/**
	 * Get delta X relative to start of drag operation in decimal degrees.
	 *
	 * @return  Delta X relative to start of drag operation in decimal degrees.
	 */
	public double getDeltaDegX()
	{
		return _deltaDegX;
	}

	/**
	 * Get delta Y relative to start of drag operation in decimal degrees.
	 *
	 * @return  Delta Y relative to start of drag operation in decimal degrees.
	 */
	public double getDeltaDegY()
	{
		return _deltaDegY;
	}

	/**
	 * Get delta X relative to start of drag operation in radians.
	 *
	 * @return  Delta X relative to start of drag operation in radians.
	 */
	public final double getDeltaRadX()
	{
		return _deltaRadX;
	}

	/**
	 * Get delta Y relative to start of drag operation in radians.
	 *
	 * @return  Delta Y relative to start of drag operation in radians.
	 */
	public final double getDeltaRadY()
	{
		return _deltaRadY;
	}

	/**
	 * Get delta X relative to start of drag operation in model units.
	 *
	 * @return  Delta X relative to start of drag operation in model units.
	 */
	public final double getDeltaUnitX()
	{
		return _deltaUnitX;
	}

	/**
	 * Get delta Y relative to start of drag operation in model units.
	 *
	 * @return  Delta Y relative to start of drag operation in model units.
	 */
	public final double getDeltaUnitY()
	{
		return _deltaUnitY;
	}
}
