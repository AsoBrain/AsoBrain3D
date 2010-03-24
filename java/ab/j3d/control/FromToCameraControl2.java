/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2009
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

import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.util.EventObject;
import java.util.Properties;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.view.View3D;

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
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class FromToCameraControl2
	extends CameraControl
{
	/**
	 * Point to look from.
	 */
	private Vector3D _from = null;

	/**
	 * Point to look at.
	 */
	private Vector3D _to = null;

	/**
	 * Up-vector (must be normalized).
	 */
	private Vector3D _up = null;

	/**
	 * Saved point to look from.
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private Vector3D _savedFrom = null;

	/**
	 * Saved point to look at.
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private Vector3D _savedTo = null;

	/**
	 * Saved point to look at.
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private Vector3D _savedUp = null;

	/**
	 * Point from where was being looked when dragging started.
	 * <p />
	 * This is used as temporary state variable for dragging operations.
	 */
	private Vector3D _dragStartFrom = Vector3D.INIT;

	/**
	 * Point to which was being looked when dragging started.
	 * <p />
	 * This is used as temporary state variable for dragging operations.
	 */
	private Vector3D _dragStartTo = Vector3D.INIT;

	/**
	 * Up-vector when dragging started.
	 * <p />
	 * This is used as temporary state variable for dragging operations.
	 */
	private Vector3D _dragStartUp = Vector3D.INIT;

	/**
	 * Construct default first person view. This creates a view from (0,-1,0) to
	 * the origin along the Y+ axis.
	 *
	 * @param   view    View to be controlled.
	 */
	public FromToCameraControl2( final View3D view )
	{
		this( view , 1.0 );
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
	public FromToCameraControl2( final View3D view , final double distance )
	{
		this( view , Vector3D.INIT.set( 0.0 , -distance , 0.0 ) , Vector3D.INIT );
	}

	/**
	 * Construct new first person camera control looking from the specified
	 * point to the other specified point. The up vector is derived from the
	 * from and to points, as specified by {@link #look(Vector3D,Vector3D)}.
	 *
	 * @param   view    View to be controlled.
	 * @param   from    Initial point to look from.
	 * @param   to      Initial point to look at.
	 *
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public FromToCameraControl2( final View3D view , final Vector3D from , final Vector3D to )
	{
		super( view );
		look( from , to );
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
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public FromToCameraControl2( final View3D view , final Vector3D from , final Vector3D to , final Vector3D up )
	{
		super( view );
		look( from , to , up );
		save();
	}

	/**
	 * Get point to look at.
	 *
	 * @return  Point to look at.
	 */
	public Vector3D getTo()
	{
		return _to;
	}

	/**
	 * Get point to look from.
	 *
	 * @return  Point to look from.
	 */
	public Vector3D getFrom()
	{
		return _from;
	}

	/**
	 * Get up-vector (must be normalized).
	 *
	 * @return  Up-vector (must be normalized).
	 */
	public Vector3D getUp()
	{
		return _up;
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
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public void look( final Vector3D from , final Vector3D to )
	{
		final Vector3D viewingDirection = Vector3D.normalize( to.x - from.x , to.y - from.y , to.z - from.z );
		final boolean isZAxis = viewingDirection.almostEquals( Vector3D.POSITIVE_Z_AXIS ) || viewingDirection.almostEquals( Vector3D.NEGATIVE_Z_AXIS );

		final Vector3D reference = !isZAxis ? Vector3D.POSITIVE_Z_AXIS : Vector3D.POSITIVE_Y_AXIS;
		final Vector3D normal    = Vector3D.cross( viewingDirection , reference );
		final Vector3D up        = Vector3D.cross( normal , viewingDirection );

		look( from , to , up.normalize() );
	}

	/**
	 * Set view to look 'from' one point 'to' another point. The up-vector
	 * should be orthogonal to the viewing direction.
	 *
	 * @param   from    Point to look from.
	 * @param   to      Point to look at.
	 * @param   up      Up-vector (must be normalized).
	 *
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public void look( final Vector3D from , final Vector3D to , final Vector3D up )
	{
		_from = from;
		_to   = to;
		_up   = up;
		setScene2View( Matrix3D.getFromToTransform( from , to , up , Vector3D.INIT ) );
	}

	public void save()
	{
		_savedFrom = _from;
		_savedTo   = _to;
		_savedUp   = _up;
	}

	public void restore()
	{
		look( _savedFrom , _savedTo , _savedUp );
	}

	public void saveSettings( final Properties settings )
	{
		if ( settings == null )
			throw new NullPointerException( "settings" );

		settings.setProperty( "from"      , _from     .toString() );
		settings.setProperty( "to"        , _to       .toString() );
		settings.setProperty( "up"        , _up       .toString() );
		settings.setProperty( "savedFrom" , _savedFrom.toString() );
		settings.setProperty( "savedTo"   , _savedTo  .toString() );
		settings.setProperty( "savedUp"   , _savedUp  .toString() );
	}

	public void loadSettings( final Properties settings )
	{
		try
		{
			final Vector3D from      = Vector3D.fromString( settings.getProperty( "from"      ) );
			final Vector3D to        = Vector3D.fromString( settings.getProperty( "to"        ) );
			final Vector3D up        = Vector3D.fromString( settings.getProperty( "up"        ) );
			final Vector3D savedFrom = Vector3D.fromString( settings.getProperty( "savedFrom" ) );
			final Vector3D savedTo   = Vector3D.fromString( settings.getProperty( "savedTo"   ) );
			final Vector3D savedUp   = Vector3D.fromString( settings.getProperty( "savedUp"   ) );

			look( from , to , up );

			_savedFrom = savedFrom;
			_savedTo   = savedTo;
			_savedUp   = savedUp;
		}
		catch ( NullPointerException e )
		{
			/* ignored, caused by missing properties */
		}
		catch ( IllegalArgumentException e )
		{
			/* ignored, caused by malformed properties or invalid control properties */
		}
	}

	public EventObject mousePressed( final ControlInputEvent event )
	{
		final EventObject result;

		if ( isSupportedEvent( event ) )
		{
			_dragStartFrom = _from;
			_dragStartTo   = _to;
			_dragStartUp   = _up;

			result = super.mousePressed( event );
		}
		else
		{
			result = event;
		}

		return result;
	}

	public EventObject mouseDragged( final ControlInputEvent event )
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

		return super.mouseDragged( event );
	}

	public boolean isSupportedEvent( final ControlInputEvent event )
	{
		return isDragFromAroundToEvent( event ) || isPanEvent( event ) || isZoomEvent( event );
	}

	protected boolean isDragFromAroundToEvent( final ControlInputEvent event )
	{
		return ( event.getSupportedModifiers() == ( InputEvent.BUTTON3_DOWN_MASK | InputEvent.CTRL_DOWN_MASK ) )
		    || ( event.getSupportedModifiers() == InputEvent.BUTTON2_DOWN_MASK );
	}

	protected boolean isPanEvent( final ControlInputEvent event )
	{
		return ( event.getSupportedModifiers() == InputEvent.BUTTON3_DOWN_MASK );
	}

	protected boolean isZoomEvent( final ControlInputEvent event )
	{
		return false;
	}

	public EventObject mouseWheelMoved( final ControlInputEvent event )
	{
		final MouseWheelEvent mouseWheelEvent = (MouseWheelEvent)event.getMouseEvent();
		zoom( -mouseWheelEvent.getWheelRotation() , event.isControlDown() );
		return null;
	}

	/**
	 * Moved the 'from' point around the 'to' point.
	 *
	 * @param   event   Drag event.
	 */
	protected void dragFromAroundTo( final ControlInputEvent event )
	{
		final Vector3D from     = _dragStartFrom;
		final Vector3D to       = _dragStartTo;
		final Vector3D up       = _dragStartUp;

		final double   toRadians = _view.getPixelsToRadiansFactor();
		final double   deltaX    = -toRadians * (double)event.getDragDeltaX();
		final double   deltaY    =  toRadians * (double)event.getDragDeltaY();

		final Vector3D orientationAxis = Vector3D.INIT.set( 0.0 , 0.0 , 1.0 );
		final Matrix3D orientation     = Matrix3D.getRotationTransform( Vector3D.INIT , orientationAxis , deltaX );
		final Vector3D orientedUp      = orientation.transform( up );

		Vector3D delta = from.minus( to );
		delta = orientation.transform( delta );

		Vector3D elevationAxis = Vector3D.cross( delta , orientedUp );
		elevationAxis = elevationAxis.normalize();

		final Matrix3D elevation = Matrix3D.getRotationTransform( Vector3D.INIT , elevationAxis , deltaY );
		final Vector3D newUp  = elevation.transform( orientedUp );

		Vector3D newFrom = elevation.transform( delta );
		newFrom = newFrom.plus( to );

		look( newFrom , to , newUp );
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
	public void zoom( final int amount , final boolean moveTarget )
	{
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

		final Vector3D from         = _from;
		final Vector3D to           = _to;
		final Vector3D delta        = to.minus( from );
		final Vector3D displacement = delta.multiply( factor );

		look( from.plus( displacement ) , moveTarget ? to.plus( displacement ) : to , _up );
	}

	/**
	 * Zoom by dragging.
	 *
	 * @param   event   Drag event.
	 */
	protected void zoom( final ControlInputEvent event )
	{
		final Vector3D from = _dragStartFrom;
		final Vector3D to   = _dragStartTo;

		final double deltaY = (double)event.getDragDeltaY();

		final double zoom = Math.max( 0.1 , 1.0 + deltaY / 100.0 );

		Vector3D newFrom = from;
		newFrom = newFrom.multiply( zoom );
		newFrom = newFrom.plus( to.multiply( 1.0 - zoom ) );
		look( newFrom , to , _up );
	}

	/**
	 * Pan by dragging.
	 *
	 * @param   event   Drag event.
	 */
	protected void pan( final ControlInputEvent event )
	{
		final Vector3D from  = _dragStartFrom;
		final Vector3D to    = _dragStartTo;
		final Vector3D up    = _dragStartUp;
		final Vector3D delta = from.minus( to );

		final Vector3D yAxis = up;

		Vector3D xAxis = Vector3D.cross( delta , yAxis );
		xAxis = xAxis.normalize();

		final double distance = delta.length();
		final Vector3D xMovement = xAxis.multiply( 0.01 * distance * (double)event.getDragDeltaX() );
		final Vector3D yMovement = yAxis.multiply( 0.01 * distance * (double)event.getDragDeltaY() );

		look( from.plus( xMovement.plus( yMovement ) ) , to.plus( xMovement.plus( yMovement ) ) , up );
	}
}
