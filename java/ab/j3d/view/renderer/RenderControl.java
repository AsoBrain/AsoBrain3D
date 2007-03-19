/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
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
package ab.j3d.view.renderer;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import ab.j3d.Matrix3D;
import ab.j3d.model.Transform3D;

/**
 * This class is used to control the camera transform used by the software
 * renderer.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class RenderControl
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
	 * Render panel being controlled.
	 */
	private final RenderPanel _renderPanel;

	/**
	 * Drag start coordinate value for X-axis movement.
	 */
	protected int _xStartCoordinate;

	/**
	 * Drag start coordinate value for Y-axis movement.
	 */
	protected int _yStartCoordinate;

	/**
	 * Drag start target value for X-axis movement.
	 */
	private double _xStartValue;

	/**
	 * Drag start target value for Y-axis movement.
	 */
	private double _yStartValue;

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
	final double _toRadians;

	/**
	 * Mouse sensivity to translate pixels to world units.
	 */
	final double _toUnits;

	/**
	 * Construct new MouseViewConrol.
	 *
	 * @param   renderPanel         Render panel to control.
	 * @param   pixelsToUnits       Pixels to units factor.
	 * @param   pixelsToDegrees     Pixels to decimal degrees factor.
	 */
	public RenderControl( final RenderPanel renderPanel , final double pixelsToUnits , final double pixelsToDegrees )
	{
		_renderPanel = renderPanel;

		_toUnits     = pixelsToUnits;
		_toRadians   = Math.toRadians( pixelsToDegrees );
		_sensitivity = new double[] { pixelsToDegrees , _toUnits , -_toUnits };

		_xStartCoordinate = 0;
		_yStartCoordinate = 0;
		_xStartValue      = 0.0;
		_yStartValue      = 0.0;

		renderPanel.addMouseListener( this );
		renderPanel.addMouseMotionListener( this );
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

	public void mousePressed( final MouseEvent event )
	{
		final int mode = getModeForButton( getButtonNumber( event ) );

		_xStartCoordinate = event.getX();
		_yStartCoordinate = event.getY();
		_xStartValue      = getValue( _controlX[ mode ] );
		_yStartValue      = getValue( _controlY[ mode ] );

		final RenderPanel renderPanel = _renderPanel;
		renderPanel.setRenderingMode( RenderPanel.QUICK );
		renderPanel.requestUpdate();
	}

	public void mouseDragged( final MouseEvent event )
	{
		final int mode = getModeForButton( getButtonNumber( event ) );
		adjustValue( _controlX[ mode ] , _xStartValue , _sensitivity[ mode ] * (double)( event.getX() - _xStartCoordinate ) );
		adjustValue( _controlY[ mode ] , _yStartValue , _sensitivity[ mode ] * (double)( _yStartCoordinate - event.getY() ) );

		final RenderPanel renderPanel = _renderPanel;
		final Transform3D modelTransform = renderPanel.getModelTransform();
		modelTransform.setTransform( getTransform() );
		_renderPanel.requestUpdate();
	}

	public void mouseReleased( final MouseEvent event )
	{
		final RenderPanel renderPanel = _renderPanel;
		renderPanel.setRenderingMode( RenderPanel.FULL );
		_renderPanel.requestUpdate();
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
	public double getRotationX()
	{
		return _variables[ ROTATION_X ];
	}

	/**
	 * Set rotation around X axis (in degrees).
	 *
	 * @param   value   Rotation around X axis (in degrees).
	 */
	public void setRotationX( final double value )
	{
		_variables[ ROTATION_X ] = value;
	}

	/**
	 * Get rotation around Y axis (in degrees).
	 *
	 * @return  Rotation around Y axis (in degrees).
	 */
	public double getRotationY()
	{
		return _variables[ ROTATION_Y ];
	}

	/**
	 * Set rotation around Y axis (in degrees).
	 *
	 * @param   value   Rotation around Y axis (in degrees).
	 */
	public void setRotationY( final double value )
	{
		_variables[ ROTATION_Y ] = value;
	}

	/**
	 * Get rotation around Z axis (in degrees).
	 *
	 * @return  Rotation around Z axis (in degrees).
	 */
	public double getRotationZ()
	{
		return _variables[ ROTATION_Z ];
	}

	/**
	 * Set rotation around Z axis (in degrees).
	 *
	 * @param   value   Rotation around Z axis (in degrees).
	 */
	public void setRotationZ( final double value )
	{
		_variables[ ROTATION_Z ] = value;
	}

	/**
	 * Get translation along X axis (in model units).
	 *
	 * @return  Translation along X axis (in model units).
	 */
	public double getTranslationX()
	{
		return _variables[ TRANSLATION_X ];
	}

	/**
	 * Set translation along X axis (in model units).
	 *
	 * @param   value   Translation along X axis (in model units).
	 */
	public void setTranslationX( final double value )
	{
		_variables[ TRANSLATION_X ] = value;
	}

	/**
	 * Get translation along Y axis (in model units).
	 *
	 * @return  Translation along Y axis (in model units).
	 */
	public double getTranslationY()
	{
		return _variables[ TRANSLATION_Y ];
	}

	/**
	 * Set translation along Y axis (in model units).
	 *
	 * @param   value   Translation along Y axis (in model units).
	 */
	public void setTranslationY( final double value )
	{
		_variables[ TRANSLATION_Y ] = value;
	}

	/**
	 * Get translation along Z axis (in model units).
	 *
	 * @return  Translation along Z axis (in model units).
	 */
	public double getTranslationZ()
	{
		return _variables[ TRANSLATION_Z ];
	}

	/**
	 * Set translation along Z axis (in model units).
	 *
	 * @param   value   Translation along Z axis (in model units).
	 */
	public void setTranslationZ( final double value )
	{
		_variables[ TRANSLATION_Z ] = value;
	}
}
