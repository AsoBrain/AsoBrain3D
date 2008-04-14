/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-200
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

import java.awt.Component;
import java.util.EventObject;
import java.util.NoSuchElementException;
import java.util.Properties;

import ab.j3d.Matrix3D;
import ab.j3d.model.Camera3D;
import ab.j3d.view.ViewModelView;

import com.numdata.oss.PropertyTools;

/**
 * This class implements a camera control that allows only camera panning and
 * zooming.
 * <p />
 * Panning is achieved by modifying the translational compnents of the view
 * matrix ({@link ViewModelView#setViewTransform}, while the zoom effect is
 * achieved by manipulating the camera ({@link Camera3D#setZoomFactor}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class PanAndZoomCameraControl
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
	private Matrix3D _dragStartViewTransform = Matrix3D.INIT;

	/**
	 * Zoom factor at start of drag operation.
	 */
	private double _dragStartZoomFactor = 1.0;

	/**
	 * Create pan and zoom camera control.
	 *
	 * @param   view    View to be controlled.
	 */
	public PanAndZoomCameraControl( final ViewModelView view )
	{
		super( view );

		_savedSettings = new Object[] { view.getViewTransform() , new Double( view.getZoomFactor() ) };
	}

	public void save()
	{
		final ViewModelView view          = _view;
		final Matrix3D      viewTransform = view.getViewTransform();
		final double        zoomFactor    = view.getZoomFactor();

		final Object[] saved = _savedSettings;
		saved[ 0 ] = viewTransform;
		saved[ 1 ] = new Double( zoomFactor );
	}

	public void restore()
	{
		final Camera3D camera = _view.getCamera();

		final Object[] saved = _savedSettings;
		camera.setZoomFactor( (Double)saved[ 1 ] );
		setTransform( (Matrix3D)saved[ 0 ] );
	}

	public void saveSettings( final Properties settings )
	{
		if ( settings == null )
			throw new NullPointerException( "settings" );

		final ViewModelView view          = _view;
		final Matrix3D      viewTransform = view.getViewTransform();
		final double        zoomFactor    = view.getZoomFactor();

		settings.setProperty( "viewTransform" , viewTransform.toString() );
		settings.setProperty( "zoomFactor"    , String.valueOf( zoomFactor ) );
	}

	public void loadSettings( final Properties settings )
	{
		try
		{
			final Matrix3D viewTransform = Matrix3D.fromString( PropertyTools.getString( settings , "viewTransform" ) );
			final double   zoomFactor    = PropertyTools.getDouble( settings , "zoomFactor" );

			final Object[] saved = _savedSettings;
			saved[ 0 ] = viewTransform;
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
		final ViewModelView view          = _view;
		final Matrix3D      viewTransform = view.getViewTransform();
		final double        zoomFactor    = view.getZoomFactor();

		_dragStartViewTransform = viewTransform;
		_dragStartZoomFactor    = zoomFactor;

		return super.mousePressed( event );
	}

	protected void mouseDragButton1( final ControlInputEvent event )
	{
		final ViewModelView view          = _view;
		final Matrix3D      viewTransform = _dragStartViewTransform;

		final double toUnits = view.getPixelsToUnitsFactor();

		final double dx =  toUnits * (double)event.getDragDeltaX();
		final double dy = -toUnits * (double)event.getDragDeltaY();

		view.setViewTransform( viewTransform.plus( dx , dy , 0.0 ) );
	}

	protected void mouseDragButton2( final ControlInputEvent event )
	{
		mouseDragButton1( event );
	}

	protected void mouseDragButton3( final ControlInputEvent event )
	{
		final ViewModelView view   = _view;
		final Camera3D      camera = view.getCamera();

		final double oldZoomFactor = _dragStartZoomFactor;
		final int    deltaY        = event.getDragDeltaY();
		final double sensitivity   = 150.0; /* should this be configurable? */
		final double adjustment    = 1.0 + (double)Math.abs( deltaY ) / sensitivity;

		camera.setZoomFactor( ( deltaY < 0 ) ? oldZoomFactor / adjustment : oldZoomFactor * adjustment );

		final Component viewComponent = view.getComponent();
		viewComponent.repaint( 0 , 0 , 1 , 1 );
	}
}
