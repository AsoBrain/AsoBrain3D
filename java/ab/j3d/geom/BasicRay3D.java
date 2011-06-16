/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2009
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

import ab.j3d.*;

/**
 * Basic implementation of {@link Ray3D}. This implementation has several
 * constructors to simplify the creation of rays and setters, so that ray
 * properties can be modified.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class BasicRay3D
	implements Ray3D
{
	/**
	 * Direction of ray in 3D space.
	 */
	private Vector3D _direction;

	/**
	 * Origin of ray in 3D space.
	 */
	private Vector3D _origin;

	/**
	 * If true, this ray is a half-ray; otherwise, this is a complete ray.
	 */
	private boolean _halfRay;

	/**
	 * Construct ray with the specified ray origin and direction.
	 *
	 * @param   originX     X coordinate for origin of ray in 3D space.
	 * @param   originY     Y coordinate for origin of ray in 3D space.
	 * @param   originZ     Z coordinate for origin of ray in 3D space.
	 * @param   directionX  X component for direction of ray in 3D space.
	 * @param   directionY  Y component for direction of ray in 3D space.
	 * @param   directionZ  Z component for direction of ray in 3D space.
	 * @param   halfRay     If <code>true</code>, mark this as a half-ray;
	 *                      if <code>false</code>, mark this as a complete ray.
	 *
	 * @throws  NullPointerException if <code>origin</code> or <code>direction</code> is <code>null</code>.
	 */
	public BasicRay3D( final double originX , final double originY , final double originZ , final double directionX , final double directionY , final double directionZ , final boolean halfRay )
	{
		_origin    = Vector3D.INIT.set( originX , originY , originZ );
		_direction = Vector3D.INIT.set( directionX , directionY , directionZ );
		_halfRay   = halfRay;
	}

	/**
	 * Construct ray with the specified transformation applied to the specified
	 * ray origin and direction.
	 *
	 * @param   transform   Optional transformation to apply.
	 * @param   originX     X coordinate for origin of ray in 3D space.
	 * @param   originY     Y coordinate for origin of ray in 3D space.
	 * @param   originZ     Z coordinate for origin of ray in 3D space.
	 * @param   directionX  X component for direction of ray in 3D space.
	 * @param   directionY  Y component for direction of ray in 3D space.
	 * @param   directionZ  Z component for direction of ray in 3D space.
	 * @param   halfRay     If <code>true</code>, mark this as a half-ray;
	 *                      if <code>false</code>, mark this as a complete ray.
	 *
	 * @throws  NullPointerException if <code>origin</code> or <code>direction</code> is <code>null</code>.
	 */
	public BasicRay3D( final Matrix3D transform , final double originX , final double originY , final double originZ , final double directionX , final double directionY , final double directionZ , final boolean halfRay )
	{
		if ( ( transform != null ) && ( transform != Matrix3D.INIT ) )
		{
			_origin    = transform.transform( originX , originY , originZ );
			_direction = transform.rotate( directionX , directionY , directionZ );
		}
		else
		{
			_origin    = Vector3D.INIT.set( originX , originY , originZ );
			_direction = Vector3D.INIT.set( directionX , directionY , directionZ );
		}

		_halfRay = halfRay;
	}

	/**
	 * Construct transformed ray.
	 *
	 * @param   transform   Optional transformation to ray.
	 * @param   ray         Ray to transform.
	 *
	 * @throws  NullPointerException if <code>ray</code> is <code>null</code>.
	 */
	public BasicRay3D( final Matrix3D transform , final Ray3D ray )
	{
		this( transform , ray.getOrigin() , ray.getDirection() , ray.isHalfRay() );
	}

	/**
	 * Construct ray with the specified transformation applied to the specified
	 * ray origin and direction.
	 *
	 * @param   transform   Optional transformation to apply.
	 * @param   origin      Origin of ray in 3D space.
	 * @param   direction   Direction of ray in 3D space.
	 * @param   halfRay     If <code>true</code>, mark this as a half-ray;
	 *                      if <code>false</code>, mark this as a complete ray.
	 *
	 * @throws  NullPointerException if <code>origin</code> or <code>direction</code> is <code>null</code>.
	 */
	public BasicRay3D( final Matrix3D transform , final Vector3D origin , final Vector3D direction , final boolean halfRay )
	{
		if ( origin == null )
			throw new NullPointerException( "origin" );

		if ( direction == null )
			throw new NullPointerException( "direction" );

		if ( ( transform != null ) && ( transform != Matrix3D.INIT ) )
		{
			_origin    = transform.transform( origin );
			_direction = transform.rotate( direction );
		}
		else
		{
			_origin    = origin;
			_direction = direction;
		}

		_halfRay = halfRay;
	}

	/**
	 * Clone constructor.
	 *
	 * @param   original    Original ray to clone.
	 *
	 * @throws  NullPointerException if <code>original</code> is <code>null</code>.
	 */
	public BasicRay3D( final Ray3D original )
	{
		this( original.getOrigin() , original.getDirection() , original.isHalfRay() );
	}

	/**
	 * Construct ray.
	 *
	 * @param   origin      Origin of ray in 3D space.
	 * @param   direction   Direction of ray in 3D space.
	 * @param   halfRay     If <code>true</code>, mark this as a half-ray;
	 *                      if <code>false</code>, mark this as a complete ray.
	 *
	 * @throws  NullPointerException if an argument is <code>null</code>.
	 */
	public BasicRay3D( final Vector3D origin , final Vector3D direction , final boolean halfRay )
	{
		if ( origin == null )
			throw new NullPointerException( "origin" );

		if ( direction == null )
			throw new NullPointerException( "direction" );

		_origin    = origin;
		_direction = direction;
		_halfRay   = halfRay;
	}

	@Override
	public Vector3D getDirection()
	{
		return _direction;
	}

	@Override
	public Vector3D getOrigin()
	{
		return _origin;
	}

	@Override
	public boolean isHalfRay()
	{
		return _halfRay;
	}

	/**
	 * Set direction of ray in 3D space.
	 *
	 * @param   direction   Direction of ray in 3D space.
	 *
	 * @throws  NullPointerException if <code>direction</code> is <code>null</code>.
	 */
	public void setDirection( final Vector3D direction )
	{
		if ( direction == null )
			throw new NullPointerException( "direction" );

		_direction = direction;
	}

	/**
	 * Mark this ray as a half-ray or complete ray. A half-ray has a distinct
	 * point of origin and extends indefinitely into one direction; a complete
	 * ray has neither a distinct origin, nor a direction.
	 *
	 * @param   halfRay     If <code>true</code>, mark this as a half-ray;
	 *                      if <code>false</code>, mark this as a complete ray.
	 */
	public void setHalfRay( final boolean halfRay )
	{
		_halfRay = halfRay;
	}

	/**
	 * Set origin of ray in 3D space.
	 *
	 * @param   origin  Origin of ray in 3D space.
	 *
	 * @throws  NullPointerException if <code>origin</code> is <code>null</code>.
	 */
	public void setOrigin( final Vector3D origin )
	{
		if ( origin == null )
			throw new NullPointerException( "origin" );

		_origin = origin;
	}

	public String toString()
	{
		return BasicRay3D.class.getName() + "=[origin=" + _origin.toFriendlyString() + ",direction=" + _direction.toFriendlyString() + ']';
	}

	public boolean equals( final Object other )
	{
		final boolean result;
		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof Ray3D )
		{
			final Ray3D ray = (Ray3D)other;
			result = ( isHalfRay() == ray.isHalfRay() ) &&
		             _origin.equals( ray.getOrigin() ) &&
		             _direction.equals( ray.getDirection() );
		}
		else
		{
			result = false;
		}
		return result;
	}

	public int hashCode()
	{
		return _origin.hashCode() ^ _direction.hashCode();
	}
}
