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
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * This class implements a camera control based on a 'from' and 'to' point. The
 * control behavior of the {@link CameraControl} class is extended as
 * follows:
 * <dl>
 *  <dt>Dragging with the right mouse button.</dt>
 *  <dd>Pan.</dd>
 *
 *  <dt>Dragging with the right mouse button while pressing CTRL.</dt>
 *  <dd>Rotate 'from' point around 'to' point.</dd>
 *
 *  <dt>Dragging with the middle mouse button.</dt>
 *  <dd>Rotate 'from' point around 'to' point.</dd>
 *
 *  <dt>Mouse wheel.</dt>
 *  <dd>Moves the 'from' point away from or towards the 'to' point.
 *
 *  <dt>Mouse wheel while pressing CTRL.</dt>
 *  <dd>Moves both the 'to' and 'from' point in the current view direction.
 * </dl>
 *
 * @author  G.B.M. Rupert
 * @author  G. Meinders
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class FromToCameraControl
	extends CameraControl
{
	/**
	 * Distance of target point, relative to view position.
	 */
	private double _distance = 1000.0;

	/**
	 * Saved view transform.
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private Matrix3D _savedScene2View = Matrix3D.IDENTITY;

	/**
	 * Saved distance of target point, relative to view position..
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private double _savedDistance = 1000.0;

	/**
	 * Saved zoom factor.
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private double _savedZoomFactor = 1.0;

	/**
	 * View transform when dragging started.
	 * <p />
	 * This is used as temporary state variable for dragging operations.
	 */
	private Matrix3D _dragStartScene2View = Matrix3D.IDENTITY;

	/**
	 * Zoom factor when dragging started.
	 * <p />
	 * This is used as temporary state variable for dragging operations.
	 */
	private double _dragStartZoomFactor = 1.0;

	/**
	 * Distance of target point, relative to view position, when dragging started.
	 * <p />
	 * This is used as temporary state variable for dragging operations.
	 */
	private double _dragStartDistance = 1000.0;

	/**
	 * Event modifiers that allow rotating around the to-point. This valus is
	 * tested for equality against the event's
	 * {@link ControlInputEvent#getSupportedModifiers() supported modifiers}.
	 */
	private int _rotateAroundToEventModifiers = InputEvent.BUTTON1_DOWN_MASK;

	/**
	 * Event modifiers that allow panning. This valus is tested for equality
	 * against the event's {@link ControlInputEvent#getSupportedModifiers()
	 * supported modifiers}.
	 */
	private int _panEventModifiers = InputEvent.BUTTON3_DOWN_MASK;

	/**
	 * Construct default first person view. This creates a view from (0,-1,0) to
	 * the origin along the Y+ axis.
	 *
	 * @param   view    View to be controlled.
	 */
	public FromToCameraControl( final View3D view )
	{
		this( view, 1000.0 );
	}

	/**
	 * Construct first person view from a point at a given distance towards
	 * the origin along the positive Y-axis.
	 *
	 * @param   view        View to be controlled.
	 * @param   distance    Distance from the origin.
	 *
	 * @throws  IllegalArgumentException if the distance is (almost) 0.
	 */
	public FromToCameraControl( final View3D view, final double distance )
	{
		this( view, new Vector3D( 0.0, -distance, 0.0 ), Vector3D.ZERO );
	}

	/**
	 * Construct new first person camera control looking from the specified
	 * point to the other specified point. The up vector is derived from the
	 * from and to points, as specified by {@link #look(Vector3D, Vector3D)}.
	 *
	 * @param   view    View to be controlled.
	 * @param   from    Initial point to look from.
	 * @param   to      Initial point to look at.
	 *
	 * @throws NullPointerException if any of the arguments is {@code null}.
	 * @throws IllegalArgumentException if the from and two points are too close.
	 */
	public FromToCameraControl( final View3D view, final Vector3D from, final Vector3D to )
	{
		super( view );
		look( from, to );
		save();
	}

	/**
	 * Construct new first person camera control looking from the specified point
	 * to the other specified point. The up vectors needs to be specified to
	 * provide the proper view orientation.
	 *
	 * @param   view            View to be controlled.
	 * @param   from            Initial point to look from.
	 * @param   to              Initial point to look at.
	 * @param   up              Up-vector (must be normalized).
	 *
	 * @throws NullPointerException if any of the arguments is {@code null}.
	 * @throws IllegalArgumentException if the from and two points are too close.
	 */
	public FromToCameraControl( final View3D view, final Vector3D from, final Vector3D to, final Vector3D up )
	{
		super( view );
		look( from, to, up );
		save();
	}

	/**
	 * Get point being looked from.
	 *
	 * @return  Point being looked from.
	 */
	public Vector3D getFrom()
	{
		return getFrom( getScene2View() );
	}

	/**
	 * Get point being looked from.
	 *
	 * @param   scene2view  Transforms scene to view coordinates.
	 *
	 * @return  Point being looked from.
	 */
	private static Vector3D getFrom( final Matrix3D scene2view )
	{
		return scene2view.inverseTransform( 0.0, 0.0, 0.0 );
	}

	/**
	 * Get point being looked at.
	 *
	 * @return  Point being looked at.
	 */
	public Vector3D getTo()
	{
		return getTo( getScene2View(), _distance );
	}

	/**
	 * Get point being looked at.
	 *
	 * @param   scene2view  Transforms scene to view coordinates.
	 * @param   distance    Distance between from and to point.
	 *
	 * @return  Point being looked at.
	 */
	private static Vector3D getTo( final Matrix3D scene2view, final double distance )
	{
		return scene2view.inverseTransform( 0.0, 0.0, -distance );
	}

	/**
	 * Get view direction.
	 *
	 * @return  View direction.
	 */
	public Vector3D getDirection()
	{
		return getDirection( getScene2View() );
	}

	/**
	 * Get view direction.
	 *
	 * @param   scene2view  Transforms scene to view coordinates.
	 *
	 * @return  View direction.
	 */
	private static Vector3D getDirection( final Matrix3D scene2view )
	{
		/* view direction = negative direction of view's Z-axis in scene */
		return new Vector3D( -scene2view.zx, -scene2view.zy, -scene2view.zz );
	}

	/**
	 * Get distance of target point, relative to view position.
	 *
	 * @return  Distance of target point, relative to view position.
	 */
	public double getDistance()
	{
		return _distance;
	}

	/**
	 * Get up-vector.
	 *
	 * @return  Up-vector.
	 */
	public Vector3D getUp()
	{
		return getUp( getScene2View() );
	}

	/**
	 * Get up-vector.
	 *
	 * @param   scene2view  Transforms scene to view coordinates.
	 *
	 * @return  Up-vector.
	 */
	private static Vector3D getUp( final Matrix3D scene2view )
	{
		/* up = view's Y-axis direction in scene */
		return new Vector3D( scene2view.yx, scene2view.yy, scene2view.yz );
	}

	/**
	 * Get right-vector.
	 *
	 * @return  Right-vector.
	 */
	public Vector3D getRight()
	{
		return getRight( getScene2View() );
	}

	/**
	 * Get right-vector.
	 *
	 * @param   scene2view  Transforms scene to view coordinates.
	 *
	 * @return  Right-vector.
	 */
	private static Vector3D getRight( final Matrix3D scene2view )
	{
		/* right = view's X-axis direction in scene */
		return new Vector3D( scene2view.xx, scene2view.xy, scene2view.xz );
	}

	/**
	 * Set view to look 'from' one point 'to' another point. The up-vector is
	 * set to the normal vector orthogonal to the viewing direction in the plane
	 * defined by the viewing direction and either the positive Z-axis or the
	 * positive Y-axis (if the viewing direction and Z-axis coincide).
	 *
	 * @param   from    Point to look from.
	 * @param   to      Point to look at.
	 *
	 * @throws NullPointerException if any of the arguments is {@code null}.
	 * @throws IllegalArgumentException if the from and two points are too close.
	 */
	public void look( final Vector3D from, final Vector3D to )
	{
		final Vector3D viewingDirection = Vector3D.normalize( to.x - from.x, to.y - from.y, to.z - from.z );
		final boolean isZAxis = viewingDirection.almostEquals( Vector3D.POSITIVE_Z_AXIS ) || viewingDirection.almostEquals( Vector3D.NEGATIVE_Z_AXIS );

		final Vector3D reference = !isZAxis ? Vector3D.POSITIVE_Z_AXIS : Vector3D.POSITIVE_Y_AXIS;
		final Vector3D normal    = Vector3D.cross( viewingDirection, reference );
		final Vector3D up        = Vector3D.cross( normal, viewingDirection );

		look( from, to, up.normalize() );
	}

	/**
	 * Set view to look 'from' one point 'to' another point. The up-vector
	 * should be orthogonal to the viewing direction.
	 *
	 * @param   from    Point to look from.
	 * @param   to      Point to look at.
	 * @param   up      Up-vector (must be normalized).
	 *
	 * @throws NullPointerException if any of the arguments is {@code null}.
	 * @throws IllegalArgumentException if the from and two points are too close.
	 */
	public void look( final Vector3D from, final Vector3D to, final Vector3D up )
	{
		_distance = from.distanceTo( to );
		setScene2View( Matrix3D.getFromToTransform( from, to, up, Vector3D.ZERO ) );
	}

	@Override
	public void zoomToFit()
	{
		final View3D view = _view;
		final Scene scene = view.getScene();

		final Bounds3D sceneBounds = scene.getBounds();
		if ( sceneBounds != null )
		{
			view.zoomToFitSceneBounds( sceneBounds );
			_distance = Vector3D.distanceBetween( sceneBounds.center(), getFrom() );
		}
	}

	@Override
	public void save()
	{
		_savedScene2View = getScene2View();
		_savedZoomFactor = _view.getZoomFactor();
		_savedDistance = _distance;
	}

	@Override
	public void restore()
	{
		_distance = _savedDistance;
		_view.setZoomFactor( _savedZoomFactor );
		setScene2View( _savedScene2View );
	}

	@Override
	public void saveSettings( @NotNull final Properties settings )
	{
		settings.setProperty( "scene2view", String.valueOf( getScene2View() ) );
		settings.setProperty( "distance", String.valueOf( _distance ) );
		settings.setProperty( "zoomFactor", String.valueOf( _view.getZoomFactor() ) );
		settings.setProperty( "savedScene2View", String.valueOf( _savedScene2View ) );
		settings.setProperty( "savedDistance", String.valueOf( _savedDistance ) );
		settings.setProperty( "savedZoomFactor", String.valueOf( _savedZoomFactor ) );
	}

	@Override
	public void loadSettings( final Properties settings )
	{
		try
		{
			if ( settings.getProperty( "savedDistance" ) != null )
			{
				_savedDistance = Double.parseDouble( settings.getProperty( "savedDistance" ) );
			}


			if ( settings.getProperty( "savedZoomFactor" ) != null )
			{
				_savedZoomFactor = Double.parseDouble( settings.getProperty( "savedZoomFactor" ) );
			}

			if ( settings.getProperty( "savedScene2View" ) != null )
			{
				_savedScene2View = Matrix3D.fromString( settings.getProperty( "savedScene2View" ) );
			}

			if ( settings.getProperty( "distance" ) != null )
			{
				_distance = Double.parseDouble( settings.getProperty( "distance" ) );
			}

			if ( settings.getProperty( "zoomFactor" ) != null )
			{
				_view.setZoomFactor( Double.parseDouble( settings.getProperty( "zoomFactor" ) ) );
			}

			if ( settings.getProperty( "scene2view" ) != null )
			{
				setScene2View( Matrix3D.fromString( settings.getProperty( "scene2view" ) ) );
			}

			_view.update();

		}
		catch ( NullPointerException e )
		{
			e.printStackTrace();
			/* ignored, caused by missing properties */
		}
		catch ( IllegalArgumentException e )
		{
			e.printStackTrace();
			/* ignored, caused by malformed properties or invalid control properties */
		}
	}

	@Override
	public void mousePressed( final ControlInputEvent event )
	{
		_dragStartScene2View = getScene2View();
		_dragStartZoomFactor = _view.getZoomFactor();
		_dragStartDistance = _distance;

		super.mousePressed( event );
	}

	@Override
	public void mouseDragged( final ControlInputEvent event )
	{
		if ( isCaptured() )
		{
			if ( isDragFromAroundToEvent( event ) )
			{
				dragFromAroundTo( event );
			}
			else if ( isPanEvent( event ) )
			{
				pan( event );
			}
			else if ( isZoomEvent( event ) )
			{
				zoom( event );
			}
		}
	}

	@Override
	public boolean isDragStartEvent( final ControlInputEvent event )
	{
		return isDragFromAroundToEvent( event ) || isPanEvent( event ) || isZoomEvent( event );
	}

	/**
	 * Test whether the specified event is used for dragging the from-point
	 * around the to-point.
	 *
	 * @param   event   Event to test (always {@link MouseEvent#MOUSE_PRESSED}).
	 *
	 * @return  {@code true} if the event is a match;
	 *          {@code false} otherwise.
	 */
	protected boolean isDragFromAroundToEvent( final ControlInputEvent event )
	{
		return ( _rotateAroundToEventModifiers != 0 ) && ( event.getSupportedModifiers() == _rotateAroundToEventModifiers );
	}

	/**
	 * Test whether the specified event is used for panning.
	 *
	 * @param   event   Event to test (always {@link MouseEvent#MOUSE_PRESSED}).
	 *
	 * @return  {@code true} if the event is a match;
	 *          {@code false} otherwise.
	 */
	protected boolean isPanEvent( final ControlInputEvent event )
	{
		return ( ( _panEventModifiers != 0 ) && ( event.getSupportedModifiers() == _panEventModifiers ) );
	}

	/**
	 * Test whether the specified event is used for zooming.
	 *
	 * @param   event   Event to test (always {@link MouseEvent#MOUSE_PRESSED}).
	 *
	 * @return  {@code true} if the event is a match;
	 *          {@code false} otherwise.
	 */
	protected boolean isZoomEvent( final ControlInputEvent event )
	{
		return false;
	}

	@Override
	public void mouseWheelMoved( final ControlInputEvent event )
	{
		final MouseWheelEvent mouseWheelEvent = (MouseWheelEvent)event.getMouseEvent();
		zoom( -mouseWheelEvent.getWheelRotation(), event.isControlDown() );
	}

	/**
	 * Moved the 'from' point around the 'to' point.
	 *
	 * @param   event   Drag event.
	 */
	protected void dragFromAroundTo( final ControlInputEvent event )
	{
		final Matrix3D scene2view = _dragStartScene2View;
		final Vector3D from = getFrom( scene2view );
		final double distance = _dragStartDistance;
		final Vector3D to = getTo( scene2view, distance );
		final Vector3D up = getUp( scene2view );

		final double   toRadians    = _view.getPixelsToRadiansFactor();
		final double   deltaAzimuth = -toRadians * (double)event.getDragDeltaX();
		final double   deltaZenith  =  toRadians * (double)event.getDragDeltaY();

		final Matrix3D orientation = Matrix3D.getRotationTransform( Vector3D.ZERO, Vector3D.POSITIVE_Z_AXIS, deltaAzimuth );
		final Vector3D orientedUp  = orientation.transform( up );

		Vector3D delta = from.minus( to );
		delta = orientation.transform( delta );

		Vector3D elevationAxis = Vector3D.cross( delta, orientedUp );
		elevationAxis = elevationAxis.normalize();

		final Matrix3D elevation = Matrix3D.getRotationTransform( Vector3D.ZERO, elevationAxis, deltaZenith );
		final Vector3D newUp  = elevation.transform( orientedUp );

		Vector3D newFrom = elevation.transform( delta );
		newFrom = newFrom.plus( to );

		look( newFrom, to, newUp );
	}

	/**
	 * Moves the camera position towards the current position of the target
	 * point.
	 *
	 * @param   amount      Number of steps to zoom; positive to zoom in,
	 *                      negative to zoom out.
	 * @param   moveTarget  Whether to move the target point, such that the
	 *                      distance to the target remains constant.
	 */
	public void zoom( final int amount, final boolean moveTarget )
	{
		final Vector3D from = getFrom();
		final Vector3D direction = getDirection();
		final double distance = getDistance();
		final Vector3D to = getTo();
		final Vector3D up = getUp();

		final double sensitivity = 0.1;

		double factor = 0.0;
		for ( int i = 0 ; i < Math.abs( amount ) ; i++ )
		{
			factor = ( 1.0 - sensitivity ) * factor + sensitivity;
		}

		if ( amount < 0 )
		{
			factor = 1.0 - 1.0 / ( 1.0 - factor );
		}

		final Vector3D displacement = direction.multiply( distance * factor );

		look( from.plus( displacement ), moveTarget ? to.plus( displacement ) : to, up );
	}

	/**
	 * Zoom by dragging.
	 *
	 * @param   event   Drag event.
	 */
	protected void zoom( final ControlInputEvent event )
	{
		final Matrix3D scene2view = _dragStartScene2View;
		final Vector3D from = getFrom( scene2view );
		final double distance = _dragStartDistance;
		final Vector3D to = getTo( scene2view, distance );
		final Vector3D up = getUp( scene2view );

		final double deltaY = (double)event.getDragDeltaY();

		final double zoom = Math.max( 0.1, 1.0 + deltaY / 100.0 );

		Vector3D newFrom = from;
		newFrom = newFrom.multiply( zoom );
		newFrom = newFrom.plus( to.multiply( 1.0 - zoom ) );
		look( newFrom, to, up );
	}

	/**
	 * Pan by dragging.
	 *
	 * @param   event   Drag event.
	 */
	protected void pan( final ControlInputEvent event )
	{
		final Matrix3D scene2view = _dragStartScene2View;

//		final double toUnits = view.getPixelsToUnitsFactor();
		final double toUnits = 0.01 * _dragStartDistance;

		final double dx =  toUnits * (double)event.getDragDeltaX();
		final double dy = -toUnits * (double)event.getDragDeltaY();

		setScene2View( scene2view.plus( dx, dy, 0.0 ) );
	}

	/**
	 * Get event modifiers that allow panning. This valus is tested for equality
	 * against the event's {@link ControlInputEvent#getSupportedModifiers()
	 * supported modifiers}.
	 *
	 * @return  Event modifiers for panning.
	 */
	public int getPanEventModifiers()
	{
		return _panEventModifiers;
	}

	/**
	 * Get event modifiers that allow panning. This valus is tested for equality
	 * against the event's {@link ControlInputEvent#getSupportedModifiers()
	 * supported modifiers}.
	 *
	 * @param   modifiers   Event modifiers for panning.
	 */
	public void setPanEventModifiers( final int modifiers )
	{
		_panEventModifiers = modifiers;
	}

	/**
	 * Get event modifiers that allow rotating around the to-point. This valus
	 * is tested for equality against the event's
	 * {@link ControlInputEvent#getSupportedModifiers() supported modifiers}.
	 *
	 * @return  Event modifiers for dragging from-point around to-point.
	 */
	public int getRotateAroundToEventModifiers()
	{
		return _rotateAroundToEventModifiers;
	}

	/**
	 * Get event modifiers that allow rotating around the to-point. This valus
	 * is tested for equality against the event's
	 * {@link ControlInputEvent#getSupportedModifiers() supported modifiers}.
	 *
	 * @param   modifiers   Event modifiers for rotating around the to-point.
	 */
	public void setRotateAroundToEventModifiers( final int modifiers )
	{
		_rotateAroundToEventModifiers = modifiers;
	}
}