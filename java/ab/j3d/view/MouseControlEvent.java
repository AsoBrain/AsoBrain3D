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

/**
 * @author Mart Slot
 * @version $Revision$ $Date$
 * @FIXME Need comment
 */
public class MouseControlEvent
	extends ControlEvent
{
	private int _type;

	private int _number;

	private int _modifiers;

	private int _x;

	private int _y;

	private int _button;

	private boolean _mouseDragged;

	private List _nodesClicked;

	/**
	 * Construct new MouseControlEvent.
	 */
	public MouseControlEvent( int number, int type , int modifiers , int x , int y , int button, boolean dragged , List nodesClicked )
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

	public List getNodesClicked()
	{
		return _nodesClicked;
	}

	public int getType()
	{
		return _type;
	}

	public int getNumber()
	{
		return _number;
	}

	public int getButton()
	{
		return _button;
	}

	public boolean isMouseDragged()
	{
		return _mouseDragged;
	}

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


	public static final int MOUSE_PRESSED = 1;

	public static final int MOUSE_MOVED = 2;

	public static final int MOUSE_RELEASED = 3;

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
