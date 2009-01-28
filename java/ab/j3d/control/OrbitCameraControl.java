/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2009
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
 * ====================================================================
 */
package ab.j3d.control;

import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.NoSuchElementException;
import java.util.Properties;

import ab.j3d.Matrix3D;
import ab.j3d.view.View3D;

import com.numdata.oss.PropertyTools;

/**
 * This class implements a camera control based on a 'from' and 'to' point. The
 * control behavior of the {@link CameraControl} class is extended as
 * follows:
 * <dl>
 *  <dt>Dragging with the left mouse button</dt>
 *  <dd>Move 'from' point in plane perpendicular to the up vector.</dd>
 *
 *  <dt>Dragging with the middle mouse button</dt>
 *  <dd>Rotate around 'to' point and change elevation.</dd>
 *
 *  <dt>Dragging with the right mouse button</dt>
 *  <dd>Move 'from' point closer or away from the 'to' point by moving the
 *      mouse up or down.</dd>
 * </dl>
 *
 * @author  Peter S. Heijnen
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class OrbitCameraControl
	extends CameraControl
{
	private double _originX;
	private double _originY;
	private double _originZ;
	private double _rotationX;
	private double _rotationY;
	private double _rotationZ;
	private double _translationX;
	private double _translationY;
	private double _translationZ;

	private double _dragStartRotationX;
	private double _dragStartRotationY;
//	private double _dragStartRotationZ;
	private double _dragStartTranslationX;
	private double _dragStartTranslationY;
	private double _dragStartTranslationZ;

	/**
	 * Saved view settings.
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private final double[] _savedSettings;

	/**
	 * Create orbit camera control with no rotations and a distance of one meter
	 * from the origin along the negative Y-axis.
	 *
	 * @param   view    View to be controlled.
	 */
	public OrbitCameraControl( final View3D view )
	{
		this( view , 1.0 );
	}

	/**
	 * Create orbit camera control with no rotations and the specified distance
	 * from the origin along the negative Y-axis.
	 *
	 * @param   view        View to be controlled.
	 * @param   distance    Distance from origin.
	 */
	public OrbitCameraControl( final View3D view , final double distance )
	{
		this( view , 0.0 , 0.0 , 0.0 , 0.0 , -distance , 0.0 );
	}

	/**
	 * Create orbit camera control with the specified rotation and translation parameters.
	 *
	 * @param   view        View to be controlled.
	 * @param   rx          Initial rotation around X axis.
	 * @param   ry          Initial rotation around Y axis.
	 * @param   rz          Initial rotation around Z axis.
	 * @param   x           Initial translation along X axis.
	 * @param   y           Initial translation along Y axis.
	 * @param   z           Initial translation along Z axis.
	 */
	public OrbitCameraControl( final View3D view , final double rx , final double ry , final double rz , final double x , final double y , final double z )
	{
		this( view , 0.0 , 0.0 , 0.0 , rx , ry , rz , x , y , z );
	}

	/**
	 * Create orbit camera control with the specified origin, rotation and
	 * translation parameters.
	 *
	 * @param   view    View to be controlled.
	 * @param   ox      Initial X-position of origin.
	 * @param   oy      Initial Y-position of origin.
	 * @param   oz      Initial Z-position of origin.
	 * @param   rx      Initial rotation around X axis.
	 * @param   ry      Initial rotation around Y axis.
	 * @param   rz      Initial rotation around Z axis.
	 * @param   x       Initial translation along X axis.
	 * @param   y       Initial translation along Y axis.
	 * @param   z       Initial translation along Z axis.
	 */
	public OrbitCameraControl( final View3D view , final double ox , final double oy , final double oz , final double rx , final double ry , final double rz , final double x , final double y , final double z )
	{
		super( view );

		_originX               = 0.0;
		_originY               = 0.0;
		_originZ               = 0.0;
		_dragStartRotationX    = 0.0;
		_dragStartRotationY    = 0.0;
//		_dragStartRotationZ    = 0.0;
		_dragStartTranslationX = 0.0;
		_dragStartTranslationY = 0.0;
		_dragStartTranslationZ = 0.0;

		_savedSettings = new double[] { _originX = ox , _originY = oy , _originZ = oz , _rotationX = rx , _rotationY = ry , _rotationZ = rz , _translationX = x , _translationY = y , _translationZ = z };
		updateTransform();
	}

	private void updateTransform()
	{
		final Matrix3D originTransform = Matrix3D.INIT.setTranslation( _originX , _originY , _originZ );

		setTransform( originTransform.multiply( Matrix3D.getTransform( _rotationX , _rotationY , _rotationZ , _translationX , _translationY , _translationZ ) ) );
	}

	public void save()
	{
		final double[] saved = _savedSettings;
		saved[ 0 ] = _originX;
		saved[ 1 ] = _originY;
		saved[ 2 ] = _originZ;
		saved[ 3 ] = _rotationX;
		saved[ 4 ] = _rotationY;
		saved[ 5 ] = _rotationZ;
		saved[ 6 ] = _translationX;
		saved[ 7 ] = _translationY;
		saved[ 8 ] = _translationZ;
	}

	public void restore()
	{
		final double[] saved = _savedSettings;
		_originX      = saved[ 0 ];
		_originY      = saved[ 1 ];
		_originZ      = saved[ 2 ];
		_rotationX    = saved[ 3 ];
		_rotationY    = saved[ 4 ];
		_rotationZ    = saved[ 5 ];
		_translationX = saved[ 6 ];
		_translationY = saved[ 7 ];
		_translationZ = saved[ 8 ];

		updateTransform();
	}

	public void saveSettings( final Properties settings )
	{
		if ( settings == null )
			throw new NullPointerException( "settings" );

		settings.setProperty( "ox" , String.valueOf( _originX      ) );
		settings.setProperty( "oy" , String.valueOf( _originY      ) );
		settings.setProperty( "oz" , String.valueOf( _originZ      ) );
		settings.setProperty( "rx" , String.valueOf( _rotationX    ) );
		settings.setProperty( "ry" , String.valueOf( _rotationY    ) );
		settings.setProperty( "rz" , String.valueOf( _rotationZ    ) );
		settings.setProperty( "x"  , String.valueOf( _translationX ) );
		settings.setProperty( "y"  , String.valueOf( _translationY ) );
		settings.setProperty( "z"  , String.valueOf( _translationZ ) );
	}

	public void loadSettings( final Properties settings )
	{
		try
		{
			final double ox = PropertyTools.getDouble( settings , "ox" );
			final double oy = PropertyTools.getDouble( settings , "oy" );
			final double oz = PropertyTools.getDouble( settings , "oz" );
			final double rx = PropertyTools.getDouble( settings , "rx" );
			final double ry = PropertyTools.getDouble( settings , "ry" );
			final double rz = PropertyTools.getDouble( settings , "rz" );
			final double x  = PropertyTools.getDouble( settings , "x"  );
			final double y  = PropertyTools.getDouble( settings , "y"  );
			final double z  = PropertyTools.getDouble( settings , "z"  );

			final double[] saved = _savedSettings;
			saved[ 0 ] = ox;
			saved[ 1 ] = oy;
			saved[ 2 ] = oz;
			saved[ 3 ] = rx;
			saved[ 4 ] = ry;
			saved[ 5 ] = rz;
			saved[ 6 ] = x;
			saved[ 7 ] = y;
			saved[ 8 ] = z;
			restore();
		}
		catch ( NoSuchElementException e )
		{
			/* ignored, caused by missing properties */
		}
		catch ( NumberFormatException e )
		{
			/* ignored, caused by malformed properties */
		}
	}

	public EventObject mousePressed( final ControlInputEvent event )
	{
		_dragStartRotationX    = _rotationX;
		_dragStartRotationY    = _rotationY;
//		_dragStartRotationZ    = _rotationZ;
		_dragStartTranslationX = _translationX;
		_dragStartTranslationY = _translationY;
		_dragStartTranslationZ = _translationZ;

		return super.mousePressed( event );
	}

	public EventObject mouseDragged( final ControlInputEvent event )
	{
		if ( isCaptured() )
		{
			switch ( event.getMouseButtonDown() )
			{
				case MouseEvent.BUTTON1 :
					rotate( event );
					break;

				case MouseEvent.BUTTON2 :
					pan( event );
					break;

				case MouseEvent.BUTTON3 :
					zoom( event );
					break;
			}
		}

		return super.mouseDragged( event );
	}

	protected void rotate( final ControlInputEvent event )
	{
		final double toDegrees = Math.toDegrees( _view.getPixelsToRadiansFactor() );

		_rotationY = _dragStartRotationY + toDegrees * (double)event.getDragDeltaX();
		_rotationX = _dragStartRotationX - toDegrees * (double)event.getDragDeltaY();

		updateTransform();
	}

	protected void pan( final ControlInputEvent event )
	{
		final double toUnits = _view.getPixelsToUnitsFactor();

		_translationX = _dragStartTranslationX + toUnits * (double)event.getDragDeltaX();
		_translationY = _dragStartTranslationY - toUnits * (double)event.getDragDeltaY();

		updateTransform();
	}

	protected void zoom( final ControlInputEvent event )
	{
		final double toUnits = _view.getPixelsToUnitsFactor();

		_translationZ = _dragStartTranslationZ - toUnits * (double)event.getDragDeltaY();
		updateTransform();
	}

	/**
	 * Get rotation around X axis (in degrees).
	 *
	 * @return  Rotation around X axis (in degrees).
	 */
	public double getRotationX()
	{
		return _rotationX;
	}

	/**
	 * Set rotation around X axis (in degrees).
	 *
	 * @param   value   Rotation around X axis (in degrees).
	 */
	public void setRotationX( final double value )
	{
		_rotationX = value;
	}

	/**
	 * Get rotation around Y axis (in degrees).
	 *
	 * @return  Rotation around Y axis (in degrees).
	 */
	public double getRotationY()
	{
		return _rotationY;
	}

	/**
	 * Set rotation around Y axis (in degrees).
	 *
	 * @param   value   Rotation around Y axis (in degrees).
	 */
	public void setRotationY( final double value )
	{
		_rotationY = value;
	}

	/**
	 * Get rotation around Z axis (in degrees).
	 *
	 * @return  Rotation around Z axis (in degrees).
	 */
	public double getRotationZ()
	{
		return _rotationZ;
	}

	/**
	 * Set rotation around Z axis (in degrees).
	 *
	 * @param   value   Rotation around Z axis (in degrees).
	 */
	public void setRotationZ( final double value )
	{
		_rotationZ = value;
	}

	/**
	 * Get translation along X axis (in model units).
	 *
	 * @return  Translation along X axis (in model units).
	 */
	public double getTranslationX()
	{
		return _translationX;
	}

	/**
	 * Set translation along X axis (in model units).
	 *
	 * @param   value   Translation along X axis (in model units).
	 */
	public void setTranslationX( final double value )
	{
		_translationX = value;
	}

	/**
	 * Get translation along Y axis (in model units).
	 *
	 * @return  Translation along Y axis (in model units).
	 */
	public double getTranslationY()
	{
		return _translationY;
	}

	/**
	 * Set translation along Y axis (in model units).
	 *
	 * @param   value   Translation along Y axis (in model units).
	 */
	public void setTranslationY( final double value )
	{
		_translationY = value;
	}

	/**
	 * Get translation along Z axis (in model units).
	 *
	 * @return  Translation along Z axis (in model units).
	 */
	public double getTranslationZ()
	{
		return _translationZ;
	}

	/**
	 * Set translation along Z axis (in model units).
	 *
	 * @param   value   Translation along Z axis (in model units).
	 */
	public void setTranslationZ( final double value )
	{
		_translationZ = value;
	}
}
