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
	 * Multiplication factor to transform decimal degrees to radians.
	 */
	private static final double DEG_TO_RAD = Math.PI / 180.0;

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
	private final double[] _variables = new double[ 6 ];

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
	final double[] _sensitivity;

	/**
	 * Mouse sensivity to translate pixels to decimal degrees.
	 */
	final double _toDegrees;

	/**
	 * Mouse sensivity to translate pixels to world units.
	 */
	final double _toUnits;

	/**
	 * Drag start coordinate value for X-axis movement.
	 */
	private int _xStartCoordinate;

	/**
	 * Drag start target value for X-axis movement.
	 */
	private double _xStartValue;

	/**
	 * Drag start coordinate value for Y-axis movement.
	 */
	private int _yStartCoordinate;

	/**
	 * Drag start target value for Y-axis movement.
	 */
	private double _yStartValue;

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
	public DragSupport( final Component target , final double unit )
	{
		_xStartCoordinate = 0;
		_yStartCoordinate = 0;
		_xStartValue      = 0.0;
		_yStartValue      = 0.0;

		target.addMouseListener( this );
		target.addMouseMotionListener( this );

		_toDegrees   = 1.4;
		_toUnits     = ( ( unit > 0 ) && ( unit != 1 ) ) ? ( 0.05 / unit ) : 0.05;
		_sensitivity = new double[] { _toDegrees , _toUnits , _toUnits };
	}

	public void addDragListener( final DragListener listener )
	{
		if ( !_listeners.contains( listener ) )
			_listeners.add( listener );
	}

	public void removeDragListener( final DragListener listener )
	{
		_listeners.remove( listener );
	}

	protected void fireEvent( final int id , final int buttonNumber , final int clickCount , final int x , final int y )
	{
		final List listeners = _listeners;
		if ( !listeners.isEmpty() )
		{
			final DragEvent event = new DragEvent( this , id , buttonNumber , clickCount ,  _xStartCoordinate , _yStartCoordinate , x , y , _toDegrees , _toDegrees * DEG_TO_RAD , _toUnits );
			for ( int i = 0 ; i < listeners.size() ; i++ )
			{
				final DragListener listener = (DragListener)listeners.get( i );

				switch ( id )
				{
					case DragEvent.DRAG_START : listener.dragStart( event ); break;
					case DragEvent.DRAG_TO    : listener.dragTo( event ); break;
					case DragEvent.DRAG_STOP  : listener.dragStop ( event ); break;
				}
			}
		}
	}

	/**
	 * Get suggested control mode for the specified button number.
	 *
	 * @param   buttonNumber    Button number on control device.
	 *
	 * @return  Control mode (<code>ROTATE</codE>, <code>PAN</code>, <code>ZOOM</code>).
	 */
	protected int getModeForButton( final int buttonNumber )
	{
		return ( buttonNumber == 1 ) ? PAN : ( buttonNumber == 2 ) ? ZOOM : _controlMode;
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
	 * Get variable value.
	 *
	 * @param   varIndex    Variable index constant (negative for reverse value change).
	 *
	 * @return  Variable value.
	 */
	private double getValue( final int varIndex )
	{
		final int absIndex = ( varIndex < 0 ) ? -varIndex : varIndex;
		return ( absIndex == DISABLED ) ? 0.0 : _variables[ absIndex ];
	}

	/**
	 * Adjust variable value.
	 *
	 * @param   varIndex    Variable index constant (negative for reverse value change).
	 * @param   startValue  Value when dragging started.
	 * @param   deltaValue  Value change since dragging started.
	 */
	private void adjustValue( final int varIndex , final double startValue , final double deltaValue )
	{
		final int absIndex = ( varIndex < 0 ) ? -varIndex : varIndex;
		if ( absIndex != DISABLED )
			_variables[ absIndex ] = ( varIndex < 0 ) ? ( startValue - deltaValue ) : ( startValue + deltaValue );
	}

	/**
	 * Get rotation around X axis (in degrees).
	 *
	 * @return  Rotation around X axis (in degrees).
	 */
	private double getRotationX()
	{
		return _variables[ ROTATION_X ];
	}

	/**
	 * Get rotation around Y axis (in degrees).
	 *
	 * @return  Rotation around Y axis (in degrees).
	 */
	private double getRotationY()
	{
		return _variables[ ROTATION_Y ];
	}

	/**
	 * Get rotation around Z axis (in degrees).
	 *
	 * @return  Rotation around Z axis (in degrees).
	 */
	private double getRotationZ()
	{
		return _variables[ ROTATION_Z ];
	}

	/**
	 * Get translation along X axis (in model units).
	 *
	 * @return  Translation along X axis (in model units).
	 */
	private double getTranslationX()
	{
		return _variables[ TRANSLATION_X ];
	}

	/**
	 * Get translation along Y axis (in model units).
	 *
	 * @return  Translation along Y axis (in model units).
	 */
	private double getTranslationY()
	{
		return _variables[ TRANSLATION_Y ];
	}

	/**
	 * Get translation along Z axis (in model units).
	 *
	 * @return  Translation along Z axis (in model units).
	 */
	private double getTranslationZ()
	{
		return _variables[ TRANSLATION_Z ];
	}

	public void mousePressed( final MouseEvent event )
	{
		final int buttonNumber = getButtonNumber( event );
		final int clickCount   = event.getClickCount();
		final int x            = event.getX();
		final int y            = event.getY();
		final int mode         = getModeForButton( buttonNumber );

		_xStartCoordinate = x;
		_yStartCoordinate = y;
		_xStartValue      = getValue( _controlX[ mode ] );
		_yStartValue      = getValue( _controlY[ mode ] );

		fireEvent( DragEvent.DRAG_START , buttonNumber , clickCount , x , y );
	}

	public void mouseDragged( final MouseEvent event )
	{
		final int buttonNumber = getButtonNumber( event );
		final int clickCount   = event.getClickCount();
		final int x            = event.getX();
		final int y            = event.getY();
		final int mode         = getModeForButton( buttonNumber );

		adjustValue( _controlX[ mode ] , _xStartValue , _sensitivity[ mode ] * (double)( x - _xStartCoordinate ) );
		adjustValue( _controlY[ mode ] , _yStartValue , _sensitivity[ mode ] * (double)( _yStartCoordinate - y ) );

		fireEvent( DragEvent.DRAG_TO , buttonNumber , clickCount , x , y );
	}

	public void mouseReleased( final MouseEvent event )
	{
		final int buttonNumber = getButtonNumber( event );
		final int clickCount   = event.getClickCount();
		final int x            = event.getX();
		final int y            = event.getY();

		fireEvent( DragEvent.DRAG_STOP , buttonNumber , clickCount , x , y );
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

	/**
	 * Get button number from mouse event.
	 *
	 * @param   mouseEvent  Mouse event to get button number from.
	 *
	 * @return  Button number (0-2).
	 */
	protected static int getButtonNumber( final MouseEvent mouseEvent )
	{
		final int modifiers = mouseEvent.getModifiers();

		return ( ( modifiers & MouseEvent.BUTTON2_MASK ) != 0 ) ? 1 : ( ( modifiers & MouseEvent.BUTTON3_MASK ) != 0 ) ? 2 : 0;
	}
}
