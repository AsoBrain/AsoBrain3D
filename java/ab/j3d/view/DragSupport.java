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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import ab.j3d.Matrix3D;

/**
 * This class implements functionality for controlling a view using the mouse.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class DragSupport
	implements MouseListener, MouseMotionListener
{
	/**
	 * Control mode: rotate.
	 */
	public static final int ROTATE = 0;

	/**
	 * Control mode: pan.
	 */
	public static final int PAN = 1;

	/**
	 * Control mode: zoom.
	 */
	public static final int ZOOM = 2;

	/**
	 * Drag event: drag start.
	 */
	public static final int DRAG_START = 0;

	/**
	 * Drag event: drag to.
	 */
	public static final int DRAG_TO = 1;

	/**
	 * Drag event: drag stop.
	 */
	public static final int DRAG_STOP = 2;

	/**
	 * Variable index constant for rotation around X axis.
	 */
	public static final int ROTATION_X = 0;

	/**
	 * Variable index constant for rotation around Y axis.
	 */
	public static final int ROTATION_Y = 1;

	/**
	 * Variable index constant for rotation around Z axis.
	 */
	public static final int ROTATION_Z = 2;

	/**
	 * Variable index constant for translation along X axis.
	 */
	public static final int TRANSLATION_X = 3;

	/**
	 * Variable index constant for translation along Y axis.
	 */
	public static final int TRANSLATION_Y = 4;

	/**
	 * Variable index constant for translation along Z axis.
	 */
	public static final int TRANSLATION_Z = 5;

	/**
	 * Dummy variable index for disabled variables.
	 */
	public static final int DISABLED = 6;

	/**
	 * Rotation and translation values.
	 */
	private final float[] _variables = new float[ 6 ];

	/**
	 * This is the current "control mode" of the view. This
	 * may be ZOOM, PAN, or ROTATE.
	 */
	private int _controlMode = ROTATE;

	/**
	 * Indices of variables controlled by dragging aking X for each control mode.
	 */
	public final int[] _controlX =  { ROTATION_Z , TRANSLATION_X , DISABLED };

	/**
	 * Indices of variables controlled by dragging aking Y for each control mode.
	 */
	public final int[] _controlY =  { ROTATION_X , TRANSLATION_Z , TRANSLATION_Y };

	/**
	 * Mouse sensivity for each control mode.
	 */
	final float[] _sensitivity = { 1.4f , 0.02f , 0.02f };

	/**
	 * Number of button that is currently down (-1 = none).
	 */
	private int _buttonDown;

	/**
	 * Drag start coordinate value for X-axis movement.
	 */
	private int _xStartCoordinate;

	private int _xLastKnownCoordinate;

	/**
	 * Drag start target value for X-axis movement.
	 */
	private float _xStartValue;

	/**
	 * Drag start coordinate value for Y-axis movement.
	 */
	private int _yStartCoordinate;

	private int _yLastKnownCoordinate;

	/**
	 * Drag start target value for Y-axis movement.
	 */
	private float _yStartValue;

	/**
	 * Registered event listeners.
	 */
	private final List _listeners = new ArrayList();

	/**
	 * Construct new MouseViewConrol.
	 *
	 * @param   target  Target component to attach mouse control to.
	 * @param   unit    Unit scale factor (e.g. <code>MM</code>).
	 */
	public DragSupport( final Component target , final float unit )
	{
		_buttonDown       = -1;
		_xStartCoordinate = 0;
		_yStartCoordinate = 0;
		_xStartValue      = 0;
		_yStartValue      = 0;

		target.addMouseListener( this );
		target.addMouseMotionListener( this );
//		target.addMouseWheelListener( this );

		if ( ( unit > 0 ) && ( unit != 1 ) )
		{
			_sensitivity[ PAN  ] /= unit;
			_sensitivity[ ZOOM ] /= unit;
		}
	}

	/**
	 * Get rotation around X axis (in degrees).
	 *
	 * @return  Rotation around X axis (in degrees).
	 */
	private float getRotationX()
	{
		return _variables[ ROTATION_X ];
	}

	/**
	 * Get rotation around Y axis (in degrees).
	 *
	 * @return  Rotation around Y axis (in degrees).
	 */
	private float getRotationY()
	{
		return _variables[ ROTATION_Y ];
	}

	/**
	 * Get rotation around Z axis (in degrees).
	 *
	 * @return  Rotation around Z axis (in degrees).
	 */
	private float getRotationZ()
	{
		return _variables[ ROTATION_Z ];
	}

	/**
	 * Get translation along X axis (in model units).
	 *
	 * @return  Translation along X axis (in model units).
	 */
	private float getTranslationX()
	{
		return _variables[ TRANSLATION_X ];
	}

	/**
	 * Get translation along Y axis (in model units).
	 *
	 * @return  Translation along Y axis (in model units).
	 */
	private float getTranslationY()
	{
		return _variables[ TRANSLATION_Y ];
	}

	/**
	 * Get translation along Z axis (in model units).
	 *
	 * @return  Translation along Z axis (in model units).
	 */
	private float getTranslationZ()
	{
		return _variables[ TRANSLATION_Z ];
	}

	/**
	 * Get transformation matrix based on the current variable values.
	 *
	 * @return  Transformation matrix.
	 */
	public Matrix3D getTransform()
	{
		return Matrix3D.getTransform(
			getRotationX() , getRotationY() , getRotationZ() ,
			getTranslationX() , getTranslationY() , getTranslationZ() );
	}

	/**
	 * Test if (any) mouse button is currently down.
	 *
	 * @return  <code>true</code> if a mouse button is down;
	 *          <code>false</code> otherwise.
	 */
	public boolean isButtonDown()
	{
		return ( _buttonDown >= 0 );
	}

	/**
	 * Set current control mode for panel.
	 *
	 * @param   mode    Control mode for panel (ZOOM,PAN,ROTATE).
	 */
	public final void setControlMode( final int mode )
	{
		_controlMode = mode;
	}

	public void addDragListener( final DragListener listener )
	{
		if ( !_listeners.contains( listener ) )
			_listeners.add( listener );
	}

	public void removeMouseViewListener( final DragListener listener )
	{
		_listeners.remove( listener );
	}

	protected void fireEvent( final int type )
	{
		if ( !_listeners.isEmpty() )
		{
			final DragEvent event = new DragEvent( this );
			for ( int i = 0 ; i < _listeners.size() ; i++ )
			{
				final DragListener listener = (DragListener)_listeners.get( i );

				switch( type )
				{
					case DRAG_START: listener.dragStart(); break;
					case DRAG_TO   : listener.dragTo( _buttonDown , _xStartCoordinate - _xLastKnownCoordinate , _yStartCoordinate - _yLastKnownCoordinate ); break;
					case DRAG_STOP : listener.dragStop(); break;
				}

				listener.mouseViewChanged( event ); // NOT NEEDED ANYMORE?
			}
		}
	}

	/**
	 * Get button number from the specified mouse event.
	 *
	 * @param   event   Mouse event.
	 *
	 * @return  Button number (0-2).
	 */
	private int getButtonNumber( final MouseEvent event )
	{
		final int modifiers = event.getModifiers();

		return ( ( modifiers & MouseEvent.BUTTON2_MASK ) != 0 ) ? 1 :
		       ( ( modifiers & MouseEvent.BUTTON3_MASK ) != 0 ) ? 2 : 0;
	}

	/**
	 * Get variable value.
	 *
	 * @param   varIndex    Variable index constant (negative for reverse value change).
	 *
	 * @return  Variable value.
	 */
	private float getValue( final int varIndex )
	{
		final int absIndex = ( varIndex < 0 ) ? -varIndex : varIndex;
		return ( absIndex == DISABLED ) ? 0 : _variables[ absIndex ];
	}

	/**
	 * Adjust variable value.
	 *
	 * @param   varIndex    Variable index constant (negative for reverse value change).
	 * @param   startValue  Value when dragging started.
	 * @param   deltaValue  Value change since dragging started.
	 */
	private void adjustValue( final int varIndex , final float startValue , final float deltaValue )
	{
		final int absIndex = ( varIndex < 0 ) ? -varIndex : varIndex;
		if ( absIndex != DISABLED )
			_variables[ absIndex ] = ( varIndex < 0 ) ? ( startValue - deltaValue ) : ( startValue + deltaValue );
	}

	public void mousePressed( final MouseEvent event )
	{
		final int buttonNr = getButtonNumber( event );
		final int mode     = ( buttonNr == 1 ) ? PAN : (buttonNr == 2) ? ZOOM : _controlMode;

		_xStartCoordinate = event.getX();
		_yStartCoordinate = event.getY();
		_xStartValue      = getValue( _controlX[ mode ] );
		_yStartValue      = getValue( _controlY[ mode ] );

		_xLastKnownCoordinate = event.getX(); // Temporary used, not sure on how the 'adjustValue()' method stores this info.
		_yLastKnownCoordinate = event.getY(); // Temporary used, not sure on how the 'adjustValue()' method stores this info.

		_buttonDown = buttonNr;
		fireEvent( DRAG_START );
	}

	public void mouseReleased( final MouseEvent event )
	{
		_buttonDown = -1;

		_xLastKnownCoordinate = event.getX(); // Temporary used, not sure on how the 'adjustValue()' method stores this info.
		_yLastKnownCoordinate = event.getY(); // Temporary used, not sure on how the 'adjustValue()' method stores this info.

		fireEvent( DRAG_STOP );
	}

	public void mouseDragged( final MouseEvent event )
	{
		final int   buttonNr    = getButtonNumber( event );
		final int   mode        = ( buttonNr == 1 ) ? PAN : (buttonNr == 2) ? ZOOM : _controlMode;
		final float sensitivity = _sensitivity[ mode ];

		adjustValue( _controlX[ mode ] , _xStartValue , sensitivity * ( event.getX() - _xStartCoordinate ) );
		adjustValue( _controlY[ mode ] , _yStartValue , sensitivity * ( _yStartCoordinate - event.getY() ) );

		_xLastKnownCoordinate = event.getX(); // Temporary used, not sure on how the 'adjustValue()' method stores this info.
		_yLastKnownCoordinate = event.getY(); // Temporary used, not sure on how the 'adjustValue()' method stores this info.

		fireEvent( DRAG_TO );
	}

	public void mouseMoved( final MouseEvent event )
	{
	}

	public void mouseClicked( final MouseEvent event )
	{
	}

	public void mouseEntered( final MouseEvent event )
	{
	}

	public void mouseExited( final MouseEvent event )
	{
	}
}
