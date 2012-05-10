/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import java.awt.event.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * This class implements a camera control that allows only camera panning and
 * zooming.
 * <p />
 * Panning is achieved by modifying the translational components of the view
 * matrix ({@link View3D#setScene2View}, while the zoom effect is
 * achieved by manipulating the view's zoom factor ({@link View3D#setZoomFactor}.
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
	protected final Map<String,Object> _savedSettings = new HashMap<String, Object>();

	/**
	 * View transform at start of drag operation.
	 */
	protected Matrix3D _dragStartScene2View = Matrix3D.IDENTITY;

	/**
	 * Projector used when dragging started.
	 */
	protected Projector _dragStartProjector = null;

	/**
	 * Create pan and zoom camera control.
	 *
	 * @param   view    View to be controlled.
	 */
	public PanAndZoomCameraControl( final View3D view )
	{
		super( view );
		save();
	}

	/**
	 * Moves the camera such that it looks at the specified center point, when
	 * projected on the current view plane.
	 *
	 * @param   center  Point to center the view on.
	 */
	public void setCenter( final Vector3D center )
	{
		final Matrix3D transform = getScene2View();
		final Vector3D centerOnPlane = transform.rotate( center );
		setScene2View( transform.setTranslation( -centerOnPlane.x , -centerOnPlane.y , transform.zo ) );
	}

	@Override
	public void save()
	{
		final View3D view = _view;
		final Map<String, Object> savedSettings = _savedSettings;
		savedSettings.put( "scene2view", view.getScene2View() );
		savedSettings.put( "zoomFactor", Double.valueOf( view.getZoomFactor() ) );
	}

	@Override
	public void restore()
	{
		final View3D view = _view;
		final Map<String, Object> savedSettings = _savedSettings;
		view.setZoomFactor( (Double)savedSettings.get( "zoomFactor" ) );
		view.setScene2View( (Matrix3D)savedSettings.get( "scene2view" ) );
	}

	@Override
	public void saveSettings( @NotNull final Properties settings )
	{
		final View3D view = _view;
		settings.setProperty( "scene2view", String.valueOf( view.getScene2View() ) );
		settings.setProperty( "zoomFactor", String.valueOf( view.getZoomFactor() ) );
	}

	@Override
	public void loadSettings( final Properties settings )
	{
		try
		{
			final View3D view = _view;

			final String scene2viewString = settings.getProperty( "scene2view" );
			if ( scene2viewString != null )
			{
				final Matrix3D scene2view = Matrix3D.fromString( scene2viewString );

				double zoomFactor = view.getZoomFactor();
				final String zoomFactorString = settings.getProperty( "zoomFactor" );
				if ( ( zoomFactorString != null ) && ( zoomFactorString.length() > 0 ) )
				{
					try
					{
						zoomFactor = Double.parseDouble( zoomFactorString );
					}
					catch ( NumberFormatException nfe )
					{
						/* ignored, will return default value */
					}
				}

				view.setZoomFactor( zoomFactor );
				view.setScene2View( scene2view );
				save();
			}
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

	@Override
	public void mousePressed( final ControlInputEvent event )
	{
		final View3D view = _view;
		_dragStartProjector = view.getProjector();
		_dragStartScene2View = view.getScene2View();

		super.mousePressed( event );
	}

	@Override
	protected boolean isDragStartEvent( final ControlInputEvent event )
	{
		return event.isMouseButton2Down() || event.isMouseButton3Down();
	}

	@Override
	public void mouseDragged( final ControlInputEvent event )
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
	}

	@Override
	public void mouseWheelMoved( final ControlInputEvent event )
	{
		zoomWheel( event );
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
		final double zoomToX = (double)event.getDragStartX();
		final double zoomToY = (double)event.getDragStartY();

		final int    deltaY        = event.getDragDeltaY();
		final double sensitivity   = 150.0; /* should this be configurable? */
		final double adjustment    = 1.0 + (double)Math.abs( deltaY ) / sensitivity;

		final Projector oldProjector    = _dragStartProjector;
		final Vector3D  oldViewPosition = oldProjector.imageToView( zoomToX , zoomToY , 0.0 );
		final Matrix3D  oldScene2View   = _dragStartScene2View;

		view.setZoomFactor( ( deltaY > 0 ) ? oldProjector.getZoomFactor() / adjustment : oldProjector.getZoomFactor() * adjustment );

		final Projector newProjector    = view.getProjector();
		final Vector3D  newViewPosition = newProjector.imageToView( zoomToX , zoomToY , 0.0 );
		final Matrix3D  newScene2View   = oldScene2View.plus( newViewPosition.minus( oldViewPosition ) );

		view.setScene2View( newScene2View );
	}

	/**
	 * Handle zoom by turning wheel.
	 *
	 * @param   event   Mouse event.
	 */
	protected void zoomWheel( final ControlInputEvent event )
	{
		final View3D view = _view;
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

		view.setZoomFactor( ( steps > 0 ) ? oldProjector.getZoomFactor() / factor : oldProjector.getZoomFactor() * factor );

		final Projector newProjector    = view.getProjector();
		final Vector3D  newViewPosition = newProjector.imageToView( zoomToX , zoomToY , 0.0 );
		final Matrix3D  newScene2View   = oldScene2View.plus( newViewPosition.minus( oldViewPosition ) );

		view.setScene2View( newScene2View );
	}
}