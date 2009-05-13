/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2009
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

import java.awt.event.MouseWheelEvent;
import java.util.EventObject;
import java.util.NoSuchElementException;
import java.util.Properties;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Camera3D;
import ab.j3d.view.Projector;
import ab.j3d.view.View3D;

import com.numdata.oss.PropertyTools;

/**
 * This class implements a camera control that allows only camera panning and
 * zooming.
 * <p />
 * Panning is achieved by modifying the translational compnents of the view
 * matrix ({@link View3D#setScene2View}, while the zoom effect is
 * achieved by manipulating the camera ({@link Camera3D#setZoomFactor}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class PanAndZoomCameraControl
	extends CameraControl
{
	/**
	 * Saved view settings.
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private final Object[] _savedSettings;

	/**
	 * View transform at start of drag operation.
	 */
	private Matrix3D _dragStartScene2View = Matrix3D.INIT;

	/**
	 * Projector used when dragging started.
	 */
	private Projector _dragStartProjector = null;

	/**
	 * Create pan and zoom camera control.
	 *
	 * @param   view    View to be controlled.
	 */
	public PanAndZoomCameraControl( final View3D view )
	{
		super( view );

		_savedSettings = new Object[] { view.getScene2View() , new Double( view.getZoomFactor() ) };
	}

	/**
	 * Moves the camera such that it looks at the specified center point.
	 *
	 * @param   center  Point to center the view on.
	 */
	public void setCenter( final Vector3D center )
	{
		final Matrix3D transform = getScene2View();
		setScene2View( transform.minus( transform.xo + center.x , transform.yo + center.y , transform.zo + center.z ) );
	}

	public void save()
	{
		final View3D   view       = _view;
		final Matrix3D scene2view = view.getScene2View();
		final double   zoomFactor = view.getZoomFactor();

		final Object[] saved = _savedSettings;
		saved[ 0 ] = scene2view;
		saved[ 1 ] = new Double( zoomFactor );
	}

	public void restore()
	{
		final Camera3D camera = _view.getCamera();

		final Object[] saved = _savedSettings;
		camera.setZoomFactor( (Double)saved[ 1 ] );
		setScene2View( (Matrix3D)saved[ 0 ] );
	}

	public void saveSettings( final Properties settings )
	{
		if ( settings == null )
			throw new NullPointerException( "settings" );

		final View3D view = _view;
		final Matrix3D scene2view = view.getScene2View();
		final double zoomFactor = view.getZoomFactor();

		settings.setProperty( "scene2view" , scene2view.toString() );
		settings.setProperty( "zoomFactor"    , String.valueOf( zoomFactor ) );
	}

	public void loadSettings( final Properties settings )
	{
		try
		{
			final Matrix3D scene2view = Matrix3D.fromString( PropertyTools.getString( settings , "scene2view" ) );
			final double   zoomFactor    = PropertyTools.getDouble( settings , "zoomFactor" );

			final Object[] saved = _savedSettings;
			saved[ 0 ] = scene2view;
			saved[ 1 ] = new Double( zoomFactor );

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
		final View3D view = _view;

		_dragStartProjector = view.getProjector();
		_dragStartScene2View = view.getScene2View();

		return super.mousePressed( event );
	}

	public EventObject mouseDragged( final ControlInputEvent event )
	{
		if ( isCaptured() )
		{
			if ( event.isMouseButton2Down() )
			{
				zoomDrag( event );
			}
			else if ( event.isMouseButton3Down() )
			{
				pan( event );
			}
		}

		return super.mouseDragged( event );
	}

	public EventObject mouseWheelMoved( final ControlInputEvent event )
	{
		zoomWheel( event );
		return null;
	}

	/**
	 * Handle panning by dragging.
	 *
	 * @param   event   Mouse event.
	 */
	protected void pan( final ControlInputEvent event )
	{
		final View3D view = _view;
		final Matrix3D scene2view = _dragStartScene2View;

		final double toUnits = view.getPixelsToUnitsFactor();

		final double dx =  toUnits * (double)event.getDragDeltaX();
		final double dy = -toUnits * (double)event.getDragDeltaY();

		view.setScene2View( scene2view.plus( dx , dy , 0.0 ) );
	}

	/**
	 * Handle zoom by dragging.
	 *
	 * @param   event   Mouse event.
	 */
	protected void zoomDrag( final ControlInputEvent event )
	{
		final View3D view = _view;
		final Camera3D camera = view.getCamera();
		final double zoomToX = (double)event.getDragStartX();
		final double zoomToY = (double)event.getDragStartY();

		final int    deltaY        = event.getDragDeltaY();
		final double sensitivity   = 150.0; /* should this be configurable? */
		final double adjustment    = 1.0 + (double)Math.abs( deltaY ) / sensitivity;

		final Projector oldProjector    = _dragStartProjector;
		final Vector3D  oldViewPosition = oldProjector.imageToView( zoomToX , zoomToY , 0.0 );
		final Matrix3D  oldScene2View   = _dragStartScene2View;

		camera.setZoomFactor( ( deltaY > 0 ) ? oldProjector.getZoomFactor() / adjustment : oldProjector.getZoomFactor() * adjustment );

		final Projector newProjector    = view.getProjector();
		final Vector3D  newViewPosition = newProjector.imageToView( zoomToX , zoomToY , 0.0 );
		final Matrix3D  newScene2View   = oldScene2View.plus( newViewPosition.minus( oldViewPosition ) );

		view.setScene2View( newScene2View );
		view.update();
	}

	/**
	 * Handle zoom by turning wheel.
	 *
	 * @param   event   Mouse event.
	 */
	protected void zoomWheel( final ControlInputEvent event )
	{
		final View3D view = _view;
		final Camera3D camera = view.getCamera();
		final double zoomToX = (double)event.getX();
		final double zoomToY = (double)event.getY();

		final MouseWheelEvent mouseWheelEvent = (MouseWheelEvent)event.getMouseEvent();
		final int steps = mouseWheelEvent.getWheelRotation();

		final double sensitivity = 0.1;

		double factor = 0.0;
		for ( int i = 0 ; i < Math.abs( steps ) ; i++ )
		{
			factor = ( 1.0 - sensitivity ) * factor + sensitivity;
		}
		factor += 1.0;

		final Projector oldProjector    = view.getProjector();
		final Vector3D  oldViewPosition = oldProjector.imageToView( zoomToX , zoomToY , 0.0 );
		final Matrix3D  oldScene2View   = view.getScene2View();

		camera.setZoomFactor( ( steps > 0 ) ? oldProjector.getZoomFactor() / factor : oldProjector.getZoomFactor() * factor );

		final Projector newProjector    = view.getProjector();
		final Vector3D  newViewPosition = newProjector.imageToView( zoomToX , zoomToY , 0.0 );
		final Matrix3D  newScene2View   = oldScene2View.plus( newViewPosition.minus( oldViewPosition ) );

		view.setScene2View( newScene2View );
		view.update();
	}
}
