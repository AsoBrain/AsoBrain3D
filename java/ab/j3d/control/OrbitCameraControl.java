/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2006
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

import java.util.EventObject;
import java.util.NoSuchElementException;
import java.util.Properties;

import com.numdata.oss.PropertyTools;

import ab.j3d.Matrix3D;
import ab.j3d.view.ViewModelView;

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
public final class OrbitCameraControl
	extends CameraControl
{
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
	public OrbitCameraControl( final ViewModelView view )
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
	public OrbitCameraControl( final ViewModelView view , final double distance )
	{
		this( view , 0.0 , 0.0 , 0.0 , 0.0 , -distance , 0.0 );
	}

	/**
	 * Create orbit camera control with the specified rotation and translation
	 * parameters.
	 *
	 * @param   view    View to be controlled.
	 * @param   rx      Initial rotation around X axis.
	 * @param   ry      Initial rotation around Y axis.
	 * @param   rz      Initial rotation around Z axis.
	 * @param   x       Initial translation along X axis.
	 * @param   y       Initial translation along Y axis.
	 * @param   z       Initial translation along Z axis.
	 */
	public OrbitCameraControl( final ViewModelView view , final double rx , final double ry , final double rz , final double x , final double y , final double z )
	{
		super( view );

		_dragStartRotationX    = 0.0;
		_dragStartRotationY    = 0.0;
//		_dragStartRotationZ    = 0.0;
		_dragStartTranslationX = 0.0;
		_dragStartTranslationY = 0.0;
		_dragStartTranslationZ = 0.0;

		_savedSettings = new double[] { _rotationX = rx , _rotationY = ry , _rotationZ = rz , _translationX = x , _translationY = y , _translationZ = z };
		updateTransform();
	}

	private void updateTransform()
	{
		setTransform( Matrix3D.getTransform( _rotationX , _rotationY , _rotationZ , _translationX , _translationY , _translationZ ) );
	}

	public void save()
	{
		final double[] saved = _savedSettings;
		saved[ 0 ] = _rotationX;
		saved[ 1 ] = _rotationY;
		saved[ 2 ] = _rotationZ;
		saved[ 3 ] = _translationX;
		saved[ 4 ] = _translationY;
		saved[ 5 ] = _translationZ;
//		System.out.println( "saved = " + ArrayTools.toString( saved ) );
	}

	public void restore()
	{
		final double[] saved = _savedSettings;
		setTransform( Matrix3D.getTransform( _rotationX = saved[ 0 ] , _rotationY = saved[ 1 ] , _rotationZ = saved[ 2 ] , _translationX = saved[ 3 ] , _translationY = saved[ 4 ] , _translationZ = saved[ 5 ] ) );
	}

	public void saveSettings( final Properties settings )
	{
		if ( settings == null )
			throw new NullPointerException( "settings" );

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
			final double rx = PropertyTools.getDouble( settings , "rx" );
			final double ry = PropertyTools.getDouble( settings , "ry" );
			final double rz = PropertyTools.getDouble( settings , "rz" );
			final double x  = PropertyTools.getDouble( settings , "x"  );
			final double y  = PropertyTools.getDouble( settings , "y"  );
			final double z  = PropertyTools.getDouble( settings , "z"  );

			final double[] saved = _savedSettings;
			saved[ 0 ] = rx;
			saved[ 1 ] = ry;
			saved[ 2 ] = rz;
			saved[ 3 ] = x;
			saved[ 4 ] = y;
			saved[ 5 ] = z;
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

	protected void mouseDragButton1( final ControlInputEvent event )
	{
		final double toDegrees = Math.toDegrees( _view.getPixelsToRadiansFactor() );

		_rotationY = _dragStartRotationY + toDegrees * (double)event.getDragDeltaX();
		_rotationX = _dragStartRotationX - toDegrees * (double)event.getDragDeltaY();

		updateTransform();
	}

	protected void mouseDragButton2( final ControlInputEvent event )
	{
		final double toUnits = _view.getPixelsToUnitsFactor();

		_translationX = _dragStartTranslationX + toUnits * (double)event.getDragDeltaX();
		_translationY = _dragStartTranslationY - toUnits * (double)event.getDragDeltaY();

		updateTransform();
	}

	protected void mouseDragButton3( final ControlInputEvent event )
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
