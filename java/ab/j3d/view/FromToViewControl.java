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
 * ====================================================================
 */
package ab.j3d.view;

import java.util.Properties;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * This class implements a view control based on a 'from' and 'to' point. The
 * control behavior of the <code>ViewControl</code> class is extended as
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
public final class FromToViewControl
	extends ViewControl
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
	 * View transform when dragging started.
	 * <p />
	 * This is used as temporary state variable for dragging operations.
	 */
	private Matrix3D _dragStartTransform = Matrix3D.INIT;

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
	 * Construct default from-to view. This creates a view from (1,0,0) to the
	 * origin along the Y+ axis.
	 */
	public FromToViewControl()
	{
		this( 1.0 );
	}

	/**
	 * Construct from-to view from a point at a given distance towards the
	 * origin along the positive Y-axis.
	 *
	 * @throws  IllegalArgumentException if the distance is (almost) 0.
	 */
	public FromToViewControl( final double distance )
	{
		this( Vector3D.INIT.set( 0.0 , -distance , 0.0 ) , Vector3D.INIT );
	}

	/**
	 * Construct new from-to view control looking from the specified point to
	 * the other specified point. The primary up vector is the Z+ axis, the
	 * seconary is the Y+ axis.
	 *
	 * @param   from    Initial point to look from.
	 * @param   to      Initial point to look at.
	 *
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public FromToViewControl( final Vector3D from , final Vector3D to )
	{
		this( from , to , Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ) , Vector3D.INIT.set( 0.0 , 1.0 , 0.0 ) );
	}

	/**
	 * Construct new from-to view control looking from the specified point to
	 * the other specified point. The primary and secondary up vectors need to
	 * be specified to provide the proper view orientation.
	 *
	 * @param   from            Initial point to look from.
	 * @param   to              Initial point to look at.
	 * @param   upPrimary       Primary up-vector (must be normalized).
	 * @param   upSecondary     Secondary up vector. Used if from-to vector is
	 *                          parallel to the primary up-vector.
	 *
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public FromToViewControl( final Vector3D from , final Vector3D to , final Vector3D upPrimary , final Vector3D upSecondary )
	{
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

		final Vector3D oldFrom = _from;
		if ( !from.equals( oldFrom ) )
		{
			final Matrix3D transform = Matrix3D.getFromToTransform( from , _to , _upPrimary , _upSecondary );

			_from = from;
			_pcs.firePropertyChange( "from" , oldFrom , from );

			setTransform( transform );
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

		final Vector3D oldTo = _to;
		if ( !to.equals( oldTo ) )
		{
			final Matrix3D transform = Matrix3D.getFromToTransform( _from , to , _upPrimary , _upSecondary );

			_to = to;
			_pcs.firePropertyChange( "to" , oldTo , to );

			setTransform( transform );
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

		final Vector3D oldupPrimary = _upPrimary;
		_upPrimary = upPrimary;
		_pcs.firePropertyChange( "upPrimary" , oldupPrimary , upPrimary );
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

		final Vector3D oldUpSecondary = _upSecondary;
		_upSecondary = upSecondary;
		_pcs.firePropertyChange( "upSecondary" , oldUpSecondary , upSecondary );
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

	public void dragStart( final DragEvent event )
	{
		_dragStartTransform = getTransform();
		_dragStartFrom      = _from;
		_dragStartTo        = _to;

		super.dragStart( event );
	}

	protected void dragLeftButton( final DragEvent event )
	{
		final Vector3D upPrimary = _upPrimary;
		final Vector3D from      = _dragStartFrom;
		final Vector3D to        = _dragStartTo;

		final double   deltaX    = -event.getDeltaRadX();
		final double   deltaY    = -event.getDeltaUnitY();

		final Matrix3D rotation  = Matrix3D.getRotationTransform( to , upPrimary , deltaX );

		final Vector3D elevation = upPrimary.multiply( deltaY );

		Vector3D newFrom = from;
		newFrom = rotation.multiply( newFrom );
		newFrom = newFrom.plus( elevation );
		setFrom( newFrom );
	}

	protected void dragMiddleButton( final DragEvent event )
	{
		final Vector3D upPrimary = _upPrimary;
		final Vector3D from      = _dragStartFrom;
		final Matrix3D transform = _dragStartTransform;

		final double   deltaX    =  event.getDeltaUnitX();
		final double   deltaY    =  event.getDeltaUnitY();

		final Vector3D zAxis     = Vector3D.INIT.set( transform.zx , transform.zy , transform.zz );
		final Vector3D xAxis     = Vector3D.cross( zAxis , upPrimary );
		final Vector3D yAxis     = Vector3D.cross( upPrimary , xAxis );

		Vector3D newFrom = from;
		newFrom = newFrom.plus( xAxis.multiply( deltaX ) );
		newFrom = newFrom.plus( yAxis.multiply( deltaY ) );
		setFrom( newFrom );
	}

	protected void dragRightButton( final DragEvent event )
	{
		final Vector3D from = _dragStartFrom;
		final Vector3D to   = _dragStartTo;

		final double deltaY = (double)event.getDeltaY();

		final double zoom = Math.max( 0.1 , 1.0 + deltaY / 200.0 );

		Vector3D newFrom = from;
		newFrom = newFrom.multiply( zoom );
		newFrom = newFrom.plus( to.multiply( 1.0 - zoom ) );
		setFrom( newFrom );
	}
}