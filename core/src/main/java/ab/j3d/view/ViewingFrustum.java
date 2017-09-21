/*
 * $Id$
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * ====================================================================
 */
package ab.j3d.view;

import ab.j3d.*;

/**
 * Provides information about the viewing frustum for a given projection.
 *
 * <p>
 * This implementation extracts the viewing frustum planes directly from the
 * projection matrix. (For details, read <a href="http://crazyjoke.free.fr/doc/3D/plane%20extraction.pdf">Fast
 * Extraction of Viewing Frustum Planes from the World-View-Projection Matrix</a>.)
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class ViewingFrustum
{
	/**
	 * Left side of the viewing frustum.
	 */
	private Vector4D _leftPlane;

	/**
	 * Right side of the viewing frustum.
	 */
	private Vector4D _rightPlane;

	/**
	 * Bottom side of the viewing frustum.
	 */
	private Vector4D _bottomPlane;

	/**
	 * Top side of the viewing frustum.
	 */
	private Vector4D _topPlane;

	/**
	 * Near side of the viewing frustum, i.e. the near clipping plane.
	 */
	private Vector4D _nearPlane;

	/**
	 * Far side of the viewing frustum, i.e. the far clipping plane.
	 */
	private Vector4D _farPlane;

	/**
	 * Constructs a new frustum for the given projection matrix.
	 *
	 * @param   projection  Projection matrix.
	 */
	public ViewingFrustum( final Matrix4D projection )
	{
		_leftPlane = new Vector4D(
			projection.wx + projection.xx,
			projection.wy + projection.xy,
			projection.wz + projection.xz,
			projection.ww + projection.xw
		);

		_rightPlane = new Vector4D(
			projection.wx - projection.xx,
			projection.wy - projection.xy,
			projection.wz - projection.xz,
			projection.ww - projection.xw
		);

		_bottomPlane = new Vector4D(
			projection.wx + projection.yx,
			projection.wy + projection.yy,
			projection.wz + projection.yz,
			projection.ww + projection.yw
		);

		_topPlane = new Vector4D(
			projection.wx - projection.yx,
			projection.wy - projection.yy,
			projection.wz - projection.yz,
			projection.ww - projection.yw
		);

		_nearPlane = new Vector4D(
			projection.wx + projection.zx,
			projection.wy + projection.zy,
			projection.wz + projection.zz,
			projection.ww + projection.zw
		);

		_farPlane = new Vector4D(
			projection.wx - projection.zx,
			projection.wy - projection.zy,
			projection.wz - projection.zz,
			projection.ww - projection.zw
		);
	}

	/**
	 * Returns whether the given bounds are at least partly contained in the
	 * viewing frustum.
	 *
	 * @param   transform   Transformation from local to world coordinate.
	 * @param   bounds      Bounds to be checked.
	 *
	 * @return  <code>true</code> if the frustum contains (part of) the bounds.
	 */
	public boolean contains( final Matrix3D transform, final Bounds3D bounds )
	{
		final Vector3D[] points = new Vector3D[]
		{
			transform.transform( bounds.v1.x, bounds.v1.y, bounds.v1.z ),
			transform.transform( bounds.v2.x, bounds.v1.y, bounds.v1.z ),
			transform.transform( bounds.v1.x, bounds.v2.y, bounds.v1.z ),
			transform.transform( bounds.v2.x, bounds.v2.y, bounds.v1.z ),
			transform.transform( bounds.v1.x, bounds.v1.y, bounds.v2.z ),
			transform.transform( bounds.v2.x, bounds.v1.y, bounds.v2.z ),
			transform.transform( bounds.v1.x, bounds.v2.y, bounds.v2.z ),
			transform.transform( bounds.v2.x, bounds.v2.y, bounds.v2.z )
		};

		return contains( _leftPlane, points ) &&
		       contains( _rightPlane, points ) &&
		       contains( _bottomPlane, points ) &&
		       contains( _topPlane, points ) &&
		       contains( _nearPlane, points ) &&
		       contains( _farPlane, points );
	}

	/**
	 * Returns whether the half-space in front of the given plane contains at
	 * least one of the given points. Points that are exactly on the plane are
	 * included in the half-space.
	 *
	 * @param   plane   Plane that defines the half-space.
	 * @param   points  Points to be checked
	 *
	 * @return  <code>true</code> if the half-space contains the point.
	 */
	private boolean contains( final Vector4D plane, final Vector3D[] points )
	{
		boolean result = false;
		for ( final Vector3D point : points )
		{
			result = contains( plane, point.x, point.y, point.z );
			if ( result )
			{
				break;
			}
		}
		return result;
	}

	/**
	 * Returns whether the half-space in front of the given plane contains the
	 * given point. Points that are exactly on the plane are included in the
	 * half-space.
	 *
	 * @param   plane   Plane that defines the half-space.
	 * @param   x       X-coordinate of the point.
	 * @param   y       Y-coordinate of the point.
	 * @param   z       Z-coordinate of the point.
	 *
	 * @return  <code>true</code> if the half-space contains the point.
	 */
	private boolean contains( final Vector4D plane, final double x, final double y, final double z )
	{
		return Vector4D.dot( plane.x, plane.y, plane.z, plane.w, x, y, z, 1.0 ) >= 0.0;
	}

	/**
	 * Returns the left side of the viewing frustum.
	 *
	 * @return  Left side of the viewing frustum.
	 */
	public Vector4D getLeftPlane()
	{
		return _leftPlane;
	}

	/**
	 * Returns the right side of the viewing frustum.
	 *
	 * @return  Right side of the viewing frustum.
	 */
	public Vector4D getRightPlane()
	{
		return _rightPlane;
	}

	/**
	 * Returns the bottom side of the viewing frustum.
	 *
	 * @return  Bottom side of the viewing frustum.
	 */
	public Vector4D getBottomPlane()
	{
		return _bottomPlane;
	}

	/**
	 * Returns the top side of the viewing frustum.
	 *
	 * @return  Top side of the viewing frustum.
	 */
	public Vector4D getTopPlane()
	{
		return _topPlane;
	}

	/**
	 * Returns the near side of the viewing frustum.
	 *
	 * @return  Near side of the viewing frustum.
	 */
	public Vector4D getNearPlane()
	{
		return _nearPlane;
	}

	/**
	 * Returns the far side of the viewing frustum.
	 *
	 * @return  Far side of the viewing frustum.
	 */
	public Vector4D getFarPlane()
	{
		return _farPlane;
	}
}
