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
package ab.j3d.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This event indicates that a mouse action has occured on a view component.
 * There are methods to return the x and y coordinates of the mouse location,
 * the button that was pressed and the faces that are beneath the mouse in the
 * view component.
 *
 * @author Mart Slot
 * @version $Revision$ $Date$
 */
public class MouseControlEvent
	extends ControlEvent
{
	/**
	 * Indicates the mouse button has been pressed.
	 */
	public static final int MOUSE_PRESSED = MouseEvent.MOUSE_PRESSED;

	/**
	 * Indicates the mouse has been moved, without any buttons pressed.
	 */
	public static final int MOUSE_MOVED = MouseEvent.MOUSE_MOVED;

	/**
	 * Indicates the mouse button has been released.
	 */
	public static final int MOUSE_RELEASED = MouseEvent.MOUSE_RELEASED;

	/**
	 * Indicates the mouse has been dragged.
	 */
	public static final int MOUSE_DRAGGED = MouseEvent.MOUSE_DRAGGED;

    /**
     * Indicates no mouse buttons; used by {@link #getButton}.
     */
    public static final int NOBUTTON = MouseEvent.NOBUTTON;

    /**
     * Indicates mouse button #1; used by {@link #getButton}.
     */
    public static final int BUTTON1 = MouseEvent.BUTTON1;

    /**
     * Indicates mouse button #2; used by {@link #getButton}.
     */
    public static final int BUTTON2 = MouseEvent.BUTTON2;

    /**
     * Indicates mouse button #3; used by {@link #getButton}.
     */
    public static final int BUTTON3 = MouseEvent.BUTTON3;

	/**
	 * The original mouse event.
	 */
	private MouseEvent _event;

	/**
	 * The type of event. One of {@link #MOUSE_PRESSED},
	 * {@link #MOUSE_RELEASED}, {@link #MOUSE_MOVED} or {@link #MOUSE_DRAGGED}.
	 */
	private int _type;

	/**
	 * The event number. After each {@link #MOUSE_RELEASED} event, this number
	 * is increased. This makes it easy to see if for example a mouse press and
	 * a mouse release event are from the same mouse click.
	 */
	private int _number;

	/**
	 * Wether the mouse has been dragged since the button was pressed.
	 */
	private boolean _mouseDragged;

	/**
	 * A list of {@link Intersection}s of all objects beneath the mouse pointer
	 * in the 3d world.
	 */
	private List _intersections;

	/**
	 * Creates a new MouseControlEvent.
	 *
	 * @param   event           The original mouse event
	 * @param   number          The number of this event.
	 * @param   dragged         Wether or not the mouse has been dragged since
	 *                          it was pressed.
	 * @param   intersections   The intersecting faces that are beneath the
	 *                          mouse pointer on the view component. Should be
	 *                          a list with {@link Intersection}s. If there are
	 *                          no intersections, this should be an empty list.
	 *
	 * @throws  NullPointerException        if <code>event</code> is null
	 * @throws  IllegalArgumentException    if the mouse event is not a mouse
	 *                                      press, release, move or drag event.
	 */
	public MouseControlEvent( final MouseEvent event , final int number , final boolean dragged , final List intersections )
	{
		final int id = event.getID();
		if ( MOUSE_PRESSED == id || MOUSE_RELEASED == id || MOUSE_MOVED == id || MOUSE_DRAGGED == id )
		{
			_type = id;
		}
		else
		{
			throw new IllegalArgumentException( "The mouse event should be a mouse press, release, move or drag event." );
		}

		_event = event;
		_number = number;
		_mouseDragged = dragged;
		_intersections = new ArrayList( intersections );
	}

	/**
	 * Returns a {@link List} of {@link Intersection}s of all objects beneath
	 * the mousepointer in the 3d world.
	 *
	 * @return  The faces beneath the mouse
	 */
	public List getIntersections()
	{
		return new ArrayList( _intersections );
	}

	/**
	 * Returns the type of this event. One of {@link #MOUSE_PRESSED},
	 * {@link #MOUSE_RELEASED}, {@link #MOUSE_MOVED} or {@link #MOUSE_DRAGGED}.
	 *
	 * @return  The event type.
	 */
	public int getType()
	{
		return _type;
	}

	/**
	 * The number of this event. After each MOUSE_RELEASED event, this number is
	 * increased. This makes it easy to see if for example a mouse press and a
	 * mouse release event are from the same mouse click.
	 *
	 * @return  The number of this event.
	 */
	public int getNumber()
	{
		return _number;
	}

	/**
	 * Wether the mouse has been dragged since the button was pressed.
	 *
	 * @return  <code>true</code> if the mouse was dragged, <code>false</code>
	 *          if not.
	 */
	public boolean isMouseDragged()
	{
		return _mouseDragged;
	}

	/**
	 * Returns the original {@link MouseEvent} for this
	 * {@link MouseControlEvent}.
	 *
	 * @return  the original {@link MouseEvent}
	 */
	public MouseEvent getMouseEvent()
	{
		return _event;
	}

	/**
	 * Returns the x coordinate of the mouse location for this event.
	 *
	 * @return  the x coordinate of the mouse location.
	 */
	public int getMouseX()
	{
		return _event.getX();
	}

	/**
	 * Returns the y coordinate of the mouse location for this event.
	 *
	 * @return  the y coordinate of the mouse location.
	 */
	public int getMouseY()
	{
		return _event.getY();
	}

	/**
	 * The mouse button that was clicked. Returns one of {@link #NOBUTTON},
	 * {@link #BUTTON1}, {@link #BUTTON2} or {@link #BUTTON3}.
	 *
	 * @return  The mouse button that was clicked
	 */
	public int getButton()
	{
		return _event.getButton();
	}

	/**
	 * The modifier mask for this event. Modifiers represent the state of all
	 * modal keys, such as ALT, CTRL, META, and the mouse buttons.<p>
	 * These modifiers are the same as the extended modifiers in the
	 * {@link InputEvent}.
	 *
	 * @return  The modifier mask for this event.
	 */
	public int getModifiers()
	{
		return _event.getModifiers();
	}

	/**
	 * Returns whether or not the Shift modifier is down on this event.
	 *
	 * @return  whether or not the Shift modifier is down on this event.
	 */
	public boolean isShiftDown()
	{
		return _event.isShiftDown();
	}

	/**
	 * Returns whether or not the Control modifier is down on this event.
	 *
	 * @return  whether or not the Control modifier is down on this event.
	 */
	public boolean isControlDown()
	{
		return _event.isControlDown();
	}

	/**
	 * Returns whether or not the Meta modifier is down on this event.
	 *
	 * @return  whether or not the Meta modifier is down on this event.
	 */
	public boolean isMetaDown()
	{
		return _event.isMetaDown();
	}

	/**
	 * Returns whether or not the Alt modifier is down on this event.
	 *
	 * @return  whether or not the Alt modifier is down on this event.
	 */
	public boolean isAltDown()
	{
		return _event.isAltDown();
	}

	/**
	 * Returns whether or not the AltGraph modifier is down on this event.
	 *
	 * @return  whether or not the AltGraph modifier is down on this event.
	 */
	public boolean isAltGraphDown()
	{
		return _event.isAltGraphDown();
	}

	/**
	 * Create a human readable representation of this MouseControlEvent. This
	 * is especially useful for debugging.
	 *
	 * @return  A human readable representation of this MouseControlEvent.
	 */
	public String toFriendlyString()
	{
		final StringBuffer sb = new StringBuffer();

		final List objectsUnderMouse = _intersections;

		sb.append( "MouseControlEvent\nType: " );
		switch ( _type )
		{
			case MOUSE_PRESSED :
				sb.append( "MOUSE_PRESSED" );
				break;

			case MOUSE_RELEASED :
				sb.append( "MOUSE_RELEASED" );
				break;

			case MOUSE_DRAGGED :
				sb.append( "MOUSE_DRAGGED" );
				break;

			case MOUSE_MOVED :
				sb.append( "MOUSE_MOVED" );
				break;


			default :
				sb.append( "???" );
		}

		sb.append( " \nEvent number: " );
		sb.append( _number );
		sb.append( " \nClicked: button " );
		sb.append( getButton() );
		sb.append( " at (" );
		sb.append( getMouseX() );
		sb.append( " , " );
		sb.append( getMouseY() );
		sb.append( ")\n" );
		sb.append( "Mouse has " );
		if ( !_mouseDragged )
			sb.append( "not " );
		sb.append( " been dragged.\n" );
		sb.append( objectsUnderMouse.size() );
		sb.append( " Objects under the mouse: " );

		for ( int i = 0; i < objectsUnderMouse.size(); i++ )
		{
			final Intersection intersection = (Intersection)objectsUnderMouse.get( i );

			sb.append( "  Object ID " );
			sb.append( intersection.getID() );
		}

		return sb.toString();
	}

}
