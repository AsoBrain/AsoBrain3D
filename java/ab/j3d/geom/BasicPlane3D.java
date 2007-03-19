/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
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
package ab.j3d.geom;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * Basic implementation of {@link Plane3D}. This implementation has several
 * constructors to simplify the creation of planes and setters, so that plane
 * properties can be modified.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class BasicPlane3D
	implements Plane3D
{
	/**
	 * Plane normal. This defines the <code>A</code>, <code>B</code>, and
	 * <code>C</code> variables  in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 */
	private Vector3D _normal;

	/**
	 * Distance component of plane relative to origin. This defines the
	 * <code>D</code> variable in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 */
	private double _distance;

	/**
	 * Flag to indicate that the plane is two-sided. This means, if set, that
	 * both sides of the plane are 'visible'; if not set, the plane is only
	 * visible from the side in which the plane normal points.
	 */
	private boolean _twoSided;

	/**
	 * Construct plane.
	 *
	 * @param   normalX     X component of plane normal in 3D space.
	 * @param   normalY     Y component of plane normal in 3D space.
	 * @param   normalZ     Z component of plane normal in 3D space.
	 * @param   distance    Distance to origin of plane in 3D space.
	 * @param   twoSided    Plane is two-sided.
	 *
	 * @throws  NullPointerException if <code>normal</code> is <code>null</code>.
	 */
	public BasicPlane3D( final double normalX , final double normalY , final double normalZ , final double distance , final boolean twoSided )
	{
		this( Vector3D.INIT.set( normalX , normalY , normalZ ) , distance , twoSided );
	}

	/**
	 * Construct transformed plane.
	 *
	 * @param   transform   Optional transformation to plane.
	 * @param   plane       Plane to transform.
	 *
	 * @throws  NullPointerException if <code>plane</code> is <code>null</code>.
	 */
	public BasicPlane3D( final Matrix3D transform , final Plane3D plane )
	{
		this( transform , plane.getNormal() , plane.getDistance() , plane.isTwoSided() );
	}

	/**
	 * Construct plane with the specified transformation applied to the specified
	 * plane normal and distance.
	 *
	 * @param   transform   Optional transformation to apply.
	 * @param   normal      Normal of plane in 3D space.
	 * @param   distance    Distance to origin of plane in 3D space.
	 * @param   twoSided    Plane is two-sided.
	 *
	 * @throws  NullPointerException if <code>normal</code> is <code>null</code>.
	 */
	public BasicPlane3D( final Matrix3D transform , final Vector3D normal , final double distance , final boolean twoSided )
	{
		if ( normal == null )
			throw new NullPointerException( "normal" );

		final Vector3D transformedNormal;
		final double   transformedDistance;

		if ( ( transform != null ) && ( transform != Matrix3D.INIT ) )
		{
			final double refX = normal.x * distance;
			final double refY = normal.y * distance;
			final double refZ = normal.z * distance;

			transformedNormal   = transform.rotate( normal );
			transformedDistance = transformedNormal.x * transform.transformX( refX, refY, refZ )
			                    + transformedNormal.y * transform.transformY( refX, refY, refZ )
			                    + transformedNormal.z * transform.transformZ( refX, refY, refZ );
		}
		else
		{
			transformedNormal   = normal;
			transformedDistance = distance;
		}

		_normal   = transformedNormal;
		_distance = transformedDistance;
		_twoSided = twoSided;
	}

	/**
	 * Construct plane through the specified point. This creates a copy of the
	 * specified plane with its distance adjusted so that the specified point is
	 * on the plane.
	 *
	 * @param   plane   Plane to translate.
	 * @param   point   Point the plane should pass through.
	 *
	 * @throws  NullPointerException if <code>plane</code> is <code>null</code>.
	 */
	public BasicPlane3D( final Plane3D plane , final Vector3D point )
	{
		final Vector3D normal = plane.getNormal();

		_normal   = normal;
		_distance = Vector3D.dot( normal , point );
		_twoSided = plane.isTwoSided();
	}

	/**
	 * Construct plane.
	 *
	 * @param   normal      Normal of plane in 3D space.
	 * @param   distance    Distance to origin of plane in 3D space.
	 * @param   twoSided    Plane is two-sided.
	 *
	 * @throws  NullPointerException if <code>normal</code> is <code>null</code>.
	 */
	public BasicPlane3D( final Vector3D normal , final double distance , final boolean twoSided )
	{
		if ( normal == null )
			throw new NullPointerException( "normal" );

		_normal   = normal;
		_distance = distance;
		_twoSided = twoSided;
	}

	public double getDistance()
	{
		return _distance;
	}

	public Vector3D getNormal()
	{
		return _normal;
	}

	public double getNormalX()
	{
		return _normal.x;
	}

	public double getNormalY()
	{
		return _normal.y;
	}

	public double getNormalZ()
	{
		return _normal.z;
	}

	public boolean isTwoSided()
	{
		return _twoSided;
	}

	/**
	 * Set distance component of plane relative to origin. This defines the
	 * <code>D</code> variable in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 *
	 * @param   distance    Distance to origin of plane in 3D space.
	 */
	public void setDistance( final double distance )
	{
		_distance = distance;
	}

	/**
	 * Set plane normal. This defines the <code>A</code>, <code>B</code>, and
	 * <code>C</code> variables  in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 *
	 * @param   normal      Normal of plane in 3D space.
	 *
	 * @throws  NullPointerException if <code>normal</code> is <code>null</code>.
	 */
	public void setNormal( final Vector3D normal )
	{
		_normal = normal;
	}

	/**
	 * Mark plane as one- or two-sided. If two-sided, both sides of the plane
	 * are 'visible'; if one-sided, the plane is only visible from the side in
	 * which the plane normal points.
	 *
	 * @param   twoSided    Plane is two-sided.
	 */
	public void setTwoSided( final boolean twoSided )
	{
		_twoSided = twoSided;
	}

	public String toString()
	{
		return BasicPlane3D.class.getName() + "[normal=" + _normal.toFriendlyString() + ",distance=" + _distance + ']';
	}
}
