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

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * This class implements a view control based on a 'from' and 'to' point.
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
	private final Vector3D _upPrimary;

	/**
	 * Secondary up vector. This up-vector is used in case the from-to vector is
	 * parallel to the primary up-vector (must be normalized).
	 */
	private final Vector3D _upSecondary;

	/**
	 * Transform that was derived from the from/to points.
	 */
	private Matrix3D _transform;

	/**
	 * Construct default from-to view. This creates a view from (0,0,0) along
	 * the positive Y-axis.
	 */
	public FromToViewControl()
	{
		this( Vector3D.INIT , Vector3D.INIT.set( 0 , 1 , 0 ) );
	}

	/**
	 * Construct from-to view from a point at a given distance towards the
	 * origin along the positive Y-axis.
	 */
	public FromToViewControl( final float distance )
	{
		this( Vector3D.INIT.set( 0 , -distance , 0 ) , Vector3D.INIT );
	}

	/**
	 * Construct new from-to view control.
	 *
	 * @param   from    Initial point to look from.
	 * @param   to      Initial point to look at.
	 */
	public FromToViewControl( final Vector3D from , final Vector3D to )
	{
		_from        = Vector3D.INIT;
		_to          = Vector3D.INIT;
		_transform   = Matrix3D.INIT;
		_upPrimary   = Vector3D.INIT.set( 0 , 0 , 1 );
		_upSecondary = Vector3D.INIT.set( 0 , 1 , 0 );

		look( from , to );
	}

	/**
	 * Set view to look 'from' one point 'to' another point.
	 *
	 * @param   from    Point to look from.
	 * @param   to      Point to look at.
	 */
	public void look( final Vector3D from , final Vector3D to )
	{
		if ( from == null )
			throw new NullPointerException( "from" );

		if ( to == null )
			throw new NullPointerException( "to" );

		if ( !from.equals( _from ) )
		{
			final Vector3D oldFrom = _from;
			_from = from;
			_pcs.firePropertyChange( "from" , oldFrom , from );
		}

		if ( !to.equals( _to ) )
		{
			final Vector3D oldTo = _to;
			_to = to;
			_pcs.firePropertyChange( "to" , oldTo , to );
		}

		final Matrix3D transform = getFromToTransform( _from , _to , _upPrimary , _upSecondary );
		if ( !transform.equals( _transform ) )
		{
			final Matrix3D oldTransform = _transform;
			_transform = transform;
			_pcs.firePropertyChange( "transform" , oldTransform , transform );
		}
	}

	/**
	 * Calculate transformation matrix based on the specified 'from' and 'to'
	 * points. An up-vector must also be specified to determine the correct view
	 * orientation. A primary and secondary up-vector is needed; the primary
	 * up-vector is used when possible, the secondary up-vector is used when the
	 * from-to vector is parallel to the primary up-vector.
	 *
	 * @param   from        Point to look from.
	 * @param   to          Point to look at.
	 * @param   upPrimary   Primary up-vector (must be normalized).
	 * @param   upSecondary Secondary up-vector (must be normalized).
	 *
	 * @return  Transformation matrix.
	 */
	public static Matrix3D getFromToTransform( final Vector3D from , final Vector3D to , final Vector3D upPrimary , final Vector3D upSecondary )
	{
		if ( from.almostEquals( to ) )
			throw new IllegalArgumentException( "'from' and 'to' can not be the same!" );

		/*
		 * Z-axis points out of the to-point (center) towards the from-point (eye).
		 */
		double zx = from.x - to.x;
		double zy = from.y - to.y;
		double zz = from.z - to.z;
		final double normalizeZ = 1.0 / Math.sqrt( zx * zx + zy * zy + zz * zz );
		zx *= normalizeZ;
		zy *= normalizeZ;
		zz *= normalizeZ;
//		System.out.println( "Math.sqrt( zx * zx + zy * zy + zz * zz ) = " + Math.sqrt( zx * zx + zy * zy + zz * zz ) );

		/*
		 * Select up-vector.
		 */
		Vector3D up = upPrimary;
		if ( Math.abs( up.x * zx + up.y * zy + up.z * zz ) > 0.999 )
			up = upSecondary;

		/*
		 * X-axis is perpendicular to the Z-axis and the up-vector.
		 */
		double xx = up.y * zz - up.z * zy;
		double xy = up.z * zx - up.x * zz;
		double xz = up.x * zy - up.y * zx;
		final double normalizeX = 1.0 / Math.sqrt( xx * xx + xy * xy + xz * xz );
		xx *= normalizeX;
		xy *= normalizeX;
		xz *= normalizeX;

		/*
		 * Y-axis is perpendicular to the Z- and X-axis.
		 */
		final double yx = zy * xz - zz * xy;
		final double yy = zz * xx - zx * xz;
		final double yz = zx * xy - zy * xx;
//		System.out.println( "Math.sqrt( yx * yx + yy * yy + yz * yz ) = " + Math.sqrt( yx * yx + yy * yy + yz * yz ) );

		/*
		 * Create matrix.
		 */
		return Matrix3D.INIT.set(
			xx , xy , xz , ( -from.x * xx -from.y * xy -from.z * xz ) ,
			yx , yy , yz , ( -from.x * yx -from.y * yy -from.z * yz ) ,
			zx , zy , zz , ( -from.x * zx -from.y * zy -from.z * zz ) );
	}

	/**
	 * Change te 'look-from' point.
	 *
	 * @param   from    Point to look from.
	 */
	public void lookFrom( final Vector3D from )
	{
		look( from , _to );
	}

	/**
	 * Change the 'look-at' point.
	 *
	 * @param   to  Point to look at.
	 */
	public void lookAt( final Vector3D to )
	{
		look( _from , to );
	}

	public Matrix3D getTransform()
	{
		return _transform;
	}

	//***************************************************
	// Some first test code for dragging support
	//***************************************************
	private Matrix3D oldTransform = _transform;
	private Vector3D oldFromPoint = _from;

	private double range;
	private double aboveAngle;
	private double aboutAngle;
	private Matrix3D rotBase;

	public void mouseViewChanged( final DragEvent event )
	{
	}

	public void dragStart()
	{
//		System.out.println( "DRAG START" );
		oldTransform = _transform;
		oldFromPoint = _from;

		range      = _from.distanceTo( _to );
		aboveAngle = 0;
		aboutAngle = 0;

		final double transX = _from.x - _to.x;
		final double transY = _from.y - _to.y;
		final double transZ = _from.z - _to.z;
		final Matrix3D trans = Matrix3D.INIT.plus( transX , transY , transZ );

		rotBase = getTransform().multiply( trans );
	}

	public void dragTo( final int buttonNr , final int deltaX , final int deltaY )
	{
//		System.out.println( "DRAG TO" );

		if ( buttonNr == 0 )
		{
			aboveAngle += deltaX * -0.01;
			aboutAngle += deltaY *  0.01;
		}
		else if ( buttonNr == 2 )
		{
			range += deltaY * 0.01;
		}

		final double rotX = Math.toRadians( aboutAngle );
		final double rotY = Math.toRadians( aboveAngle );

//		_transform = Matrix3D.INIT.rotateY( rotY ).rotateX( rotX ).multiply( rotBase ).setTranslation( 0 , 0 , -range );
		_transform = rotBase.rotateY( rotY ).rotateX( rotX ).setTranslation( 0 , 0 , -range );
	}

	public void dragStop()
	{
//		System.out.println( "DRAG STOP" );
		_from = _transform.inverse().multiply( _to );
	}
}