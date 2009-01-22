/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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

import java.awt.event.KeyEvent;
import java.util.EventObject;
import java.util.Properties;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.view.ViewModelView;

/**
 * This class implements a camera control based on a first person view. The
 * control behavior of the {@link CameraControl} class is extended as
 * follows:
 * <dl>
 *  <dt>Dragging with the left mouse button</dt>
 *  <dd>Move 'to' point in plane perpendicular to the up vector.</dd>
 *
 *  <dt>Dragging with the middle mouse button</dt>
 *  <dd>Move 'from' point in plane perpendicular to the up vector.</dd>
 *
 *  <dt>Dragging with the right mouse button</dt>
 *  <dd>Move 'from' point closer or away from the 'to' point by moving the
 *      mouse up or down.</dd>
 *
 *  <dt>Cursor keys</dt>
 *  <dd>Move 'from' and 'to' point in plane  perpendicular to the up vector
 *      relative to the current view direction.</dd>
 * </dl>
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class FirstPersonCameraControl
	extends CameraControl
{
	/**
	 * Point to look from.
	 */
	private Vector3D _from;

	/**
	 * Point to look at.
	 */
	private Vector3D _to;

	/**
	 * Primary up-vector (must be normalized).
	 */
	private Vector3D _upPrimary;

	/**
	 * Secondary up vector. This up-vector is used in case the from-to vector is
	 * parallel to the primary up-vector (must be normalized).
	 */
	private Vector3D _upSecondary;

	/**
	 * Saved point to look from.
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private Vector3D _savedFrom;

	/**
	 * Saved point to look at.
	 *
	 * @see     #save()
	 * @see     #restore()
	 */
	private Vector3D _savedTo;

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
	 * Construct default first person view. This creates a view from (1,0,0) to
	 * the origin along the Y+ axis.
	 *
	 * @param   view    View to be controlled.
	 */
	public FirstPersonCameraControl( final ViewModelView view )
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
	public FirstPersonCameraControl( final ViewModelView view , final double distance )
	{
		this( view , Vector3D.INIT.set( 0.0 , -distance , 0.0 ) , Vector3D.INIT );
	}

	/**
	 * Construct new first person camera control looking from the specified point
	 * to the other specified point. The primary up vector is the Z+ axis, the
	 * secondary is the Y+ axis.
	 *
	 * @param   view    View to be controlled.
	 * @param   from    Initial point to look from.
	 * @param   to      Initial point to look at.
	 *
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public FirstPersonCameraControl( final ViewModelView view , final Vector3D from , final Vector3D to )
	{
		this( view , from , to , Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ) , Vector3D.INIT.set( 0.0 , 1.0 , 0.0 ) );
	}

	/**
	 * Construct new first person camera control looking from the specified point
	 * to the other specified point. The primary and secondary up vectors need to
	 * be specified to provide the proper view orientation.
	 *
	 * @param   view            View to be controlled.
	 * @param   from            Initial point to look from.
	 * @param   to              Initial point to look at.
	 * @param   upPrimary       Primary up-vector (must be normalized).
	 * @param   upSecondary     Secondary up vector. Used if from-to vector is
	 *                          parallel to the primary up-vector.
	 *
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public FirstPersonCameraControl( final ViewModelView view , final Vector3D from , final Vector3D to , final Vector3D upPrimary , final Vector3D upSecondary )
	{
		super( view );

		_from        = from;
		_to          = to;
		_upPrimary   = upPrimary;
		_upSecondary = upSecondary;

		_savedFrom   = from;
		_savedTo     = to;

		setTransform( Matrix3D.getFromToTransform( from , to , upPrimary , upSecondary ) );
	}

	/**
	 * Set view to look 'from' one point 'to' another point.
	 *
	 * @param   from    Point to look from.
	 * @param   to      Point to look at.
	 *
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public void look( final Vector3D from , final Vector3D to )
	{
		setFrom( from );
		setTo( to );
	}

	/**
	 * Set the point to look from.
	 *
	 * @param   from    New point to look from.
	 *
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public void setFrom( final Vector3D from )
	{
		if ( from == null )
			throw new NullPointerException( "from" );

		if ( !from.equals( _from ) )
		{
			_from = from;
			setTransform( Matrix3D.getFromToTransform( from , _to , _upPrimary , _upSecondary ) );
		}
	}

	/**
	 * Set the point to look at.
	 *
	 * @param   to      New point to look at.
	 */
	public void setTo( final Vector3D to )
	{
		if ( to == null )
			throw new NullPointerException( "to" );

		if ( !to.equals( _to ) )
		{
			_to = to;
			setTransform( Matrix3D.getFromToTransform( _from , to , _upPrimary , _upSecondary ) );
		}
	}

	/**
	 * Set primary up-vector.
	 *
	 * @param   upPrimary   Primary up-vector (must be normalized).
	 *
	 * @see     #setUpSecondary(Vector3D)
	 */
	public void setUpPrimary( final Vector3D upPrimary )
	{
		if ( upPrimary == null )
			throw new NullPointerException( "upPrimary" );

		_upPrimary = upPrimary;
	}

	/**
	 * Set secondary up vector. This up-vector is used in case the from-to
	 * vector is parallel to the primary up-vector.
	 *
	 * @param   upSecondary     Secondary up vector (must be normalized).
	 *
	 * @see     #setUpPrimary(Vector3D)
	 */
	public void setUpSecondary( final Vector3D upSecondary )
	{
		if ( upSecondary == null )
			throw new NullPointerException( "upSecondary" );

		_upSecondary = upSecondary;
	}

	public void save()
	{
		_savedFrom = _from;
		_savedTo   = _to;
	}

	public void restore()
	{
		look( _savedFrom , _savedTo );
	}

	public void saveSettings( final Properties settings )
	{
		if ( settings == null )
			throw new NullPointerException( "settings" );

		settings.setProperty( "from"        , _from       .toString() );
		settings.setProperty( "to"          , _to         .toString() );
		settings.setProperty( "upPrimary"   , _upPrimary  .toString() );
		settings.setProperty( "upSecondary" , _upSecondary.toString() );
		settings.setProperty( "savedFrom"   , _savedFrom  .toString() );
		settings.setProperty( "savedTo"     , _savedTo    .toString() );
	}

	public void loadSettings( final Properties settings )
	{
		try
		{
			final Vector3D from        = Vector3D.fromString( settings.getProperty( "from"        ) );
			final Vector3D to          = Vector3D.fromString( settings.getProperty( "to"          ) );
			final Vector3D upPrimary   = Vector3D.fromString( settings.getProperty( "upPrimary"   ) );
			final Vector3D upSecondary = Vector3D.fromString( settings.getProperty( "upSecondary" ) );
			final Vector3D savedFrom   = Vector3D.fromString( settings.getProperty( "savedFrom"   ) );
			final Vector3D savedTo     = Vector3D.fromString( settings.getProperty( "savedTo"     ) );

			/* verify settings */
			Matrix3D.getFromToTransform( from , to , upPrimary , upSecondary );

			/* activate settings */
			setUpPrimary( upPrimary );
			setUpSecondary( upSecondary );
			look( from , to );
			_savedFrom = savedFrom;
			_savedTo   = savedTo;
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

	/**
	 * Get size of steps.
	 *
	 * @return  Size of steps.
	 */
	private double getStepSize()
	{
		return _from.distanceTo( _to ) / 10.0;
	}

	public EventObject filterEvent( final EventObject event )
	{
		final EventObject result;

		if ( ( event instanceof ControlInputEvent ) && isEnabled() )
		{
			final ControlInputEvent controlInputEvent = (ControlInputEvent)event;

			final KeyEvent keyEvent = controlInputEvent.getKeyEvent();
			if ( ( keyEvent != null ) && ( keyEvent.getID() == KeyEvent.KEY_TYPED ) )
			{
				result = handleKeyTyped( controlInputEvent );
			}
			else
			{
				result = super.filterEvent( controlInputEvent );
			}
		}
		else
		{
			result = super.filterEvent( event );
		}

		return result;
	}

	/**
	 * Handle key.
	 *
	 * @param   event   Key event.
	 *
	 * @return  Filtered event (may be same or modified);
	 *          <code>null</code> to discard event completely.
	 */
	protected EventObject handleKeyTyped( final ControlInputEvent event )
	{
		EventObject result = event;

		if ( event.getID() == KeyEvent.KEY_PRESSED )
		{
			final int      keyCode = event.getKeyCode();
			final Vector3D from    = _from;
			final Vector3D to      = _to;

			switch ( keyCode )
			{
				case KeyEvent.VK_LEFT :
					moveSteps( from , to , getTransform() , -1.0 ,  0.0 ,  0.0 );
					result = null;
					break;

				case KeyEvent.VK_RIGHT :
					moveSteps( from , to , getTransform() ,  1.0 ,  0.0 ,  0.0 );
					result = null;
					break;

				case KeyEvent.VK_UP :
					moveSteps( from , to , getTransform() ,  0.0 ,  1.0 ,  0.0 );
					result = null;
					break;

				case KeyEvent.VK_DOWN :
					moveSteps( from , to , getTransform() ,  0.0 , -1.0 ,  0.0 );
					result = null;
					break;

				case KeyEvent.VK_PAGE_DOWN :
					moveSteps( from , to , getTransform() ,  0.0 ,  0.0 , -0.5 );
					result = null;
					break;

				case KeyEvent.VK_PAGE_UP :
					moveSteps( from , to , getTransform() ,  0.0 ,  0.0 ,  0.5 );
					result = null;
					break;
			}
		}

		return result;
	}

	public EventObject mousePressed( final ControlInputEvent event )
	{
		_dragStartFrom = _from;
		_dragStartTo   = _to;

		return super.mousePressed( event );
	}

	public EventObject mouseDragged( final ControlInputEvent event )
	{
		if ( isCaptured() )
		{
			switch ( event.getMouseButtonDown() )
			{
				case 1 :
					dragToAroundFrom( event );
					break;

				case 2 :
					dragFromAroundTo( event );
					break;

				case 3 :
					zoom( event );
					break;
			}
		}

		return super.mouseDragged( event );
	}

	/**
	 * Move the specified number of steps in any direction.
	 *
	 * @param   from        From point.
	 * @param   to          To point.
	 * @param   transform   Current view transform.
	 * @param   xSteps      Steps in X-direction.
	 * @param   ySteps      Steps in Y-direction.
	 * @param   zSteps      Steps in Z-direction.
	 */
	private void moveSteps( final Vector3D from , final Vector3D to , final Matrix3D transform , final double xSteps , final double ySteps , final double zSteps )
	{
		final Vector3D upPrimary = _upPrimary;
		final Vector3D zAxis     = Vector3D.INIT.set( transform.zx , transform.zy , transform.zz );
		final Vector3D xAxis     = Vector3D.cross( upPrimary , zAxis );
		final Vector3D yAxis     = Vector3D.cross( upPrimary , xAxis );

		final double stepSize = getStepSize();

		Vector3D movement = xAxis.multiply( xSteps * stepSize );
		movement = movement.plus( yAxis.multiply( ySteps * stepSize ) );
		movement = movement.plus( upPrimary.multiply( zSteps * stepSize ) );

		setFrom( from.plus( movement ) );
		setTo( to.plus( movement ) );
	}

	/**
	 * Drag the 'to' point around the 'from' point. This provides the ability
	 * to change the rotation or elevation.
	 *
	 * @param   event   Drag event.
	 */
	protected void dragToAroundFrom( final ControlInputEvent event )
	{
		final Vector3D upPrimary = _upPrimary;
		final Vector3D from      = _dragStartFrom;
		final Vector3D to        = _dragStartTo;
		final double   distance  = from.distanceTo( to );

		final double   toRadians = _view.getPixelsToRadiansFactor();
		final double   deltaX    = -toRadians * (double)event.getDragDeltaX();
		final double   deltaY    = -(double)event.getDragDeltaY();

		final Matrix3D rotation  = Matrix3D.getRotationTransform( from , upPrimary , deltaX );
		final Vector3D elevation = upPrimary.multiply( distance * deltaY / 100.0 );

		Vector3D newto = to;
		newto = rotation.multiply( newto );
		newto = newto.plus( elevation );
		setTo( newto );
	}

	/**
	 * Drag the 'from' point around the 'to' point. This provides the ability
	 * to change the rotation or elevation.
	 *
	 * @param   event   Drag event.
	 */
	protected void dragFromAroundTo( final ControlInputEvent event )
	{
		final Vector3D upPrimary = _upPrimary;
		final Vector3D from      = _dragStartFrom;
		final Vector3D to        = _dragStartTo;
		final double   distance  = to.distanceTo( from );

		final double   toRadians = _view.getPixelsToRadiansFactor();
		final double   deltaX    = -toRadians * (double)event.getDragDeltaX();
		final double   deltaY    = (double)event.getDragDeltaY();

		final Matrix3D rotation  = Matrix3D.getRotationTransform( to , upPrimary , deltaX );
		final Vector3D elevation = upPrimary.multiply( distance * deltaY / 100.0 );

		Vector3D newFrom = from;
		newFrom = rotation.multiply( newFrom );
		newFrom = newFrom.plus( elevation );
		setFrom( newFrom );
	}

	/**
	 * Zoom by moving the 'from' point away from or towards the 'to' point.
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
		setFrom( newFrom );
	}

	/**
	 * Move along the plane.
	 *
	 * @param   event   Drag event.
	 */
	protected void move( final ControlInputEvent event )
	{
		final Matrix3D transform = getTransform();
		final Vector3D upPrimary = _upPrimary;
		final Vector3D zAxis     = Vector3D.INIT.set( transform.zx , transform.zy , transform.zz );
		final Vector3D xAxis     = Vector3D.cross( upPrimary , zAxis );
		final Vector3D yAxis     = Vector3D.cross( upPrimary , xAxis );

		final Vector3D xMovement = Vector3D.INIT.plus( xAxis.multiply( (double)event.getDragDeltaX() * 50.0 ) );
		final Vector3D movement  = xMovement.plus( yAxis.multiply( (double)event.getDragDeltaY() * -100.0 ) );

		setFrom( _dragStartFrom.plus( movement ) );
		setTo( _dragStartTo.plus( movement ) );
	}

}
