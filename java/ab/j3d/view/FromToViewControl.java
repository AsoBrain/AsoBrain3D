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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * ====================================================================
 */
package ab.j3d.view;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * This class implements a view control based on a 'from' and 'to' point.
 *
 * @author  Peter S. Heijnen
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
	 * Construct new from-to view control.
	 *
	 * @param   from    Initial point to look from.
	 * @param   to      Initial point to look at.
	 */
	public FromToViewControl( final Vector3D from , final Vector3D to )
	{
		_from        = null;
		_to          = null;
		_transform   = null;
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
			throw new IllegalArgumentException( "getTransfrom( from , to ); 'from' and 'to' can not be the same!" );

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

		/*
		 * Select up-vector.
		 */
		Vector3D up = upPrimary;
		if ( Math.abs( up.x * zx + up.y * zy + up.z * zz ) > 0.999 )
			up = upSecondary;

		/*
		 * X-axis is perpendicular to the Z-axis and the up-vector.
		 */
		final double xx = up.y * zz - zy * up.z;
		final double xy = up.z * zx - up.x * zz;
		final double xz = up.x * zy - up.y * zx;

		/*
		 * Y-axis is perpendicular to the Z- and X-axis.
		 */
		final double yx = zy * xz - xy * zz;
		final double yy = zz * xx - zx * xz;
		final double yz = zx * xy - zy * xx;

		/*
		 * Create matrix.
		 */
		return Matrix3D.INIT.set(
			(float)xx , (float)xy , (float)xz , (float)( from.x * xx -from.y * xy -from.z * xz ) ,
			(float)yx , (float)yy , (float)yz , (float)( from.x * yx -from.y * yy -from.z * yz ) ,
			(float)zx , (float)zy , (float)zz , (float)( from.x * zx -from.y * zy -from.z * zz ) ).inverse();
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
}
