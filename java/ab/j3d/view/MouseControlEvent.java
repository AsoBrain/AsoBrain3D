/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import java.awt.Event;
import java.util.List;

import ab.j3d.model.Face3D;

/**
 * @author Mart Slot
 * @version $Revision$ $Date$
 * This event indicates that a mouse action has occured on a view component.
 * The information of this event is available from this event. The x and y
 * component of the mouse location, the button that was pressed and the faces
 * that are beneath the mouse in the view component.
 */
public class MouseControlEvent
	extends ControlEvent
{
	/**
	 * The type of event. One of MOUSE_PRESSED, MOUSE_RELEASED, MOUSE_MOVED or
	 * MOUSE_DRAGGED
	 */
	private int _type;

	/**
	 * The event number. After each MOUSE_RELEASED event, this number is
	 * increased. This makes it easy to see if a mouse press and a mouse release
	 * event are from the same mouse click.
	 */
	private int _number;

	/**
	 * The modifier mask for this event. Modifiers represent the state of all
	 * modal keys, such as ALT, CTRL, META, and the mouse buttons just after the
	 * event occurred.
	 */
	private int _modifiers;

	/**
	 * The x location of the mouse
	 */
	private int _x;

	/**
	 * The y location of the mouse
	 */
	private int _y;

	/**
	 * The button that was clicked
	 */
	private int _button;

	/**
	 * Wether the mouse has been dragged since the button was pressed
	 */
	private boolean _mouseDragged;

	/**
	 * The faces that are beneath the mouse pointer on the view component.
	 */
	private List _nodesClicked;

	/**
	 * Creates a new MouseControlEvent
	 * @param number        The number of this event.
	 * @param type          The event type. One of MOUSE_PRESSED,
	 *                      MOUSE_RELEASED, MOUSE_MOVED or MOUSE_DRAGGED
	 * @param modifiers     The modifiers for this event
	 * @param x             The x location of the mouse
	 * @param y             The y location of the mouse
	 * @param button        The button that was pressed
	 * @param dragged       Wether or not the mouse has been dragged since it
	 *                      was pressed
	 * @param nodesClicked  The faces that are beneath the mouse pointer on the
	 *                      view component.
	 */
	public MouseControlEvent( final int number, final int type , final int modifiers , final int x , final int y , final int button, final boolean dragged , final List nodesClicked )
	{
		_number = number;
		_type = type;
		_modifiers = modifiers;
		_x = x;
		_y = y;
		_button = button;
		_mouseDragged = dragged;
		_nodesClicked = nodesClicked;
	}

	/**
	 * Returns the faces that are beneath the mouse in the view component.
	 * @return The faces beneath the mouse
	 */
	public List getNodesClicked()
	{
		return _nodesClicked;
	}

	/**
	 * Returns type of this event. One of MOUSE_PRESSED, MOUSE_RELEASED,
	 * MOUSE_MOVED or MOUSE_DRAGGED
	 * @return The event type.
	 */
	public int getType()
	{
		return _type;
	}

	/**
	 * The event number. After each MOUSE_RELEASED event, this number is
	 * increased. This makes it easy to see if a mouse press and a mouse release
	 * event are from the same mouse click.
	 * @return The event number
	 */
	public int getNumber()
	{
		return _number;
	}

	/**
	 * The button that was clicked
	 * @return The button that was clicked
	 */
	public int getButton()
	{
		return _button;
	}

	/**
	 * Wether the mouse has been dragged since the button was pressed
	 * @return True if the mouse was dragged
	 */
	public boolean isMouseDragged()
	{
		return _mouseDragged;
	}

	/**
	 * The modifier mask for this event. Modifiers represent the state of all
	 * modal keys, such as ALT, CTRL, META, and the mouse buttons just after the
	 * event occurred.
	 * These modifiers are the same as the extended modifiers in an AWT event.
	 * @return The modifier mask for this event.
	 */
	public int getModifiers()
	{
		return _modifiers;
	}

	/**
	 * Returns whether or not the Shift modifier is down on this event.
	 */
	public boolean isShiftDown()
	{
		return ( _modifiers & SHIFT_MASK ) != 0;
	}

	/**
	 * Returns whether or not the Control modifier is down on this event.
	 */
	public boolean isControlDown()
	{
		return ( _modifiers & CTRL_MASK ) != 0;
	}

	/**
	 * Returns whether or not the Meta modifier is down on this event.
	 */
	public boolean isMetaDown()
	{
		return ( _modifiers & META_MASK ) != 0;
	}

	/**
	 * Returns whether or not the Alt modifier is down on this event.
	 */
	public boolean isAltDown()
	{
		return ( _modifiers & ALT_MASK ) != 0;
	}

	/**
	 * Returns whether or not the AltGraph modifier is down on this event.
	 */
	public boolean isAltGraphDown()
	{
		return ( _modifiers & ALT_GRAPH_MASK ) != 0;
	}

	/**
	 * Create a human readable representation of this MouseControlEvent. This
	 * is especially useful for debugging.
	 * @return A human readable representation of this MouseControlEvent.
	 */
	public String toFriendlyString()
	{
		String string = "";

		string += "MouseControlEvent\n";

		final String[] types = new String[] { "", "Mouse pressed", "Mouse moved", "Mouse released", "Mouse dragged"};
		string += "Type: " + types[_type] +" \n";
		string += "Event number: " + _number +" \n";
		string += "Modifiers: " + _modifiers +" \n";
		string += "Clicked: button " + _button +" at (" + _x + " , " + _y + ")\n";
		string += "Mouse has " + (_mouseDragged ? "" : "not ") +" been dragged.\n";
		string += _nodesClicked.size() + " faces under the mouse: ";

		for ( int i = 0; i < _nodesClicked.size(); i++ )
		{
			final Face3D face = (Face3D)_nodesClicked.get( i );
			string += "  Face of " + face.getObject().getTag();
		}

		return string;
	}

	/**
	 * Indicates the mouse button has been pressed.
	 */
	public static final int MOUSE_PRESSED = 1;

	/**
	 * Indicates the mouse has been moved, without any buttons pressed.
	 */
	public static final int MOUSE_MOVED = 2;

	/**
	 * Indicates the mouse button has been released.
	 */
	public static final int MOUSE_RELEASED = 3;

	/**
	 * Indicates the mouse has been dragged.
	 */
	public static final int MOUSE_DRAGGED = 4;

    /**
     * Indicates no mouse buttons; used by {@link #getButton}.
     * @since 1.4
     */
    public static final int NOBUTTON = 0;

    /**
     * Indicates mouse button #1; used by {@link #getButton}.
     * @since 1.4
     */
    public static final int BUTTON1 = 1;

    /**
     * Indicates mouse button #2; used by {@link #getButton}.
     * @since 1.4
     */
    public static final int BUTTON2 = 2;

    /**
     * Indicates mouse button #3; used by {@link #getButton}.
     * @since 1.4
     */
    public static final int BUTTON3 = 3;

	/**
	 * The Shift key modifier constant. It is recommended that SHIFT_DOWN_MASK be
	 * used instead.
	 */
	public static final int SHIFT_MASK = Event.SHIFT_MASK;

	/**
	 * The Control key modifier constant. It is recommended that CTRL_DOWN_MASK be
	 * used instead.
	 */
	public static final int CTRL_MASK = Event.CTRL_MASK;

	/**
	 * The Meta key modifier constant. It is recommended that META_DOWN_MASK be
	 * used instead.
	 */
	public static final int META_MASK = Event.META_MASK;

	/**
	 * The Alt key modifier constant. It is recommended that ALT_DOWN_MASK be used
	 * instead.
	 */
	public static final int ALT_MASK = Event.ALT_MASK;

	/**
	 * The AltGraph key modifier constant.
	 */
	public static final int ALT_GRAPH_MASK = 1 << 5;

	/**
	 * The Mouse Button1 modifier constant. It is recommended that
	 * BUTTON1_DOWN_MASK be used instead.
	 */
	public static final int BUTTON1_MASK = 1 << 4;

	/**
	 * The Mouse Button2 modifier constant. It is recommended that
	 * BUTTON2_DOWN_MASK be used instead. Note that BUTTON2_MASK has the same value
	 * as ALT_MASK.
	 */
	public static final int BUTTON2_MASK = Event.ALT_MASK;

	/**
	 * The Mouse Button3 modifier constant. It is recommended that
	 * BUTTON3_DOWN_MASK be used instead. Note that BUTTON3_MASK has the same value
	 * as META_MASK.
	 */
	public static final int BUTTON3_MASK = Event.META_MASK;

	/**
	 * The Shift key extended modifier constant.
	 *
	 * @since 1.4
	 */
	public static final int SHIFT_DOWN_MASK = 1 << 6;

	/**
	 * The Control key extended modifier constant.
	 *
	 * @since 1.4
	 */
	public static final int CTRL_DOWN_MASK = 1 << 7;

	/**
	 * The Meta key extended modifier constant.
	 *
	 * @since 1.4
	 */
	public static final int META_DOWN_MASK = 1 << 8;

	/**
	 * The Alt key extended modifier constant.
	 *
	 * @since 1.4
	 */
	public static final int ALT_DOWN_MASK = 1 << 9;

	/**
	 * The Mouse Button1 extended modifier constant.
	 *
	 * @since 1.4
	 */
	public static final int BUTTON1_DOWN_MASK = 1 << 10;

	/**
	 * The Mouse Button2 extended modifier constant.
	 *
	 * @since 1.4
	 */
	public static final int BUTTON2_DOWN_MASK = 1 << 11;

	/**
	 * The Mouse Button3 extended modifier constant.
	 *
	 * @since 1.4
	 */
	public static final int BUTTON3_DOWN_MASK = 1 << 12;

	/**
	 * The AltGraph key extended modifier constant.
	 *
	 * @since 1.4
	 */
	public static final int ALT_GRAPH_DOWN_MASK = 1 << 13;
}
