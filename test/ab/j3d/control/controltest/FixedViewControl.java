/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2006-2006 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.control.controltest;

import java.util.Properties;

import ab.j3d.Vector3D;
import ab.j3d.Matrix3D;
import ab.j3d.view.DragEvent;
import ab.j3d.view.ViewControl;

/**
 * Temporary {@link ViewControl} that is nearly similar to
 * {@link ab.j3d.view.FromToViewControl} with the exception that the view does
 * not move when the mouse is dragged.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class FixedViewControl
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
	 * Construct default from-to view. This creates a view from (1,0,0) to the
	 * origin along the Y+ axis.
	 */
	public FixedViewControl()
	{
		this( 1.0 );
	}

	/**
	 * Construct from-to view from a point at a given distance towards the
	 * origin along the positive Y-axis.
	 *
	 * @param   distance    Distance from the origin.
	 *
	 * @throws  IllegalArgumentException if the distance is (almost) 0.
	 */
	public FixedViewControl( final double distance )
	{
		this( Vector3D.INIT.set( 0.0 , -distance , 0.0 ) , Vector3D.INIT );
	}

	/**
	 * Construct new FixedViewControl.
	 *
	 * @param   from            Initial point to look from.
	 * @param   to              Initial point to look at.
	 */
	public FixedViewControl( final Vector3D from , final Vector3D to )
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
	public FixedViewControl( final Vector3D from , final Vector3D to , final Vector3D upPrimary , final Vector3D upSecondary )
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
	}

	protected void dragLeftButton( final DragEvent event )
	{
	}

	protected void dragMiddleButton( final DragEvent event )
	{
	}

	protected void dragRightButton( final DragEvent event )
	{
	}
}
