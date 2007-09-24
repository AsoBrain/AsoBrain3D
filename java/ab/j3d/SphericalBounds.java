/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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
package ab.j3d;

/**
 * Defines bounds in polar/spherical coordinates by specifying minimum and
 * maximum values for azimuth and zenith.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class SphericalBounds
{
	/** Minimum azimuth, in the range from -pi to pi. */
	private final double _minimumAzimuth;

	/** Maximum azimuth, in the range from -pi to pi. */
	private final double _maximumAzimuth;

	/** Minimum zenith, in the range from 0.0 to pi. */
	private final double _minimumZenith;

	/** Maximum zenith, in the range from 0.0 to pi. */
	private final double _maximumZenith;

	/**
	 * Constructs empty spherical bounds.
	 */
	public SphericalBounds()
	{
		_minimumAzimuth = Double.NaN;
		_maximumAzimuth = Double.NaN;
		_minimumZenith  = Double.NaN;
		_maximumZenith  = Double.NaN;
	}

	/**
	 * Constructs spherical bounds containing only the given direction.
	 *
	 * @param   azimuth     Azimuth of the direction, in radians.
	 * @param   zenith      Zenith of the direction, in radians.
	 */
	public SphericalBounds( final double azimuth , final double zenith )
	{
		this( azimuth , azimuth , zenith , zenith );
	}

	/**
	 * Constructs spherical bounds defined by the given minimum and maximum
	 * angles.
	 *
	 * @param   minimumAzimuth  Minimum azimuth, in the range from -pi to pi.
	 * @param   maximumAzimuth  Maximum azimuth, in the range from -pi to pi.
	 * @param   minimumZenith   Minimum zenith, in the range from 0.0 to pi.
	 * @param   maximumZenith   Maximum zenith, in the range from 0.0 to pi.
	 *
	 * @throws  IllegalArgumentException if any parameter is out of bounds or if
	 *          the minimum zenith is greater than the maximum zenith.
	 */
	public SphericalBounds( final double minimumAzimuth , final double maximumAzimuth , final double minimumZenith , final double maximumZenith )
	{
		if ( ( minimumAzimuth < -Math.PI ) || ( minimumAzimuth > Math.PI ) )
			throw new IllegalArgumentException( "minimumAzimuth" );
		if ( ( maximumAzimuth < -Math.PI ) || ( maximumAzimuth > Math.PI ) )
			throw new IllegalArgumentException( "maximumAzimuth" );

		if ( ( minimumZenith < 0.0 ) || ( minimumZenith > Math.PI ) )
			throw new IllegalArgumentException( "minimumZenith" );
		if ( ( maximumZenith < 0.0 ) || ( maximumZenith > Math.PI ) )
			throw new IllegalArgumentException( "maximumZenith" );
		if ( minimumZenith > maximumZenith )
			throw new IllegalArgumentException( "minimumZenith > maximumZenith" );

		_minimumAzimuth = minimumAzimuth;
		_maximumAzimuth = maximumAzimuth;
		_minimumZenith  = minimumZenith;
		_maximumZenith  = maximumZenith;
	}

	/**
	 * Returns spherical bounds for the specified angular ranges.
	 *
	 * @param   azimuthOffset   Azimuth offset, in radians, between -pi and pi.
	 * @param   azimuthRange    Azimuth range, in radians.
	 * @param   zenithOffset    Zenith offset, in radians, between 0.0 and pi.
	 * @param   zenithRange     Zenith range, in radians, between -pi and pi.
	 *
	 * @throws  IllegalArgumentException if zenithOffset + zenithRange is not
	 *          in the range from 0.0 to pi (both inclusive).
	 */
	public static SphericalBounds fromRange( final double azimuthOffset , final double azimuthRange , final double zenithOffset , final double zenithRange )
	{
		final double zenithEndOffset = zenithOffset + zenithRange;

		if ( zenithEndOffset < 0.0 )
			throw new IllegalArgumentException( "zenithOffset + zenithRange < 0.0" );

		if ( zenithEndOffset > Math.PI )
			throw new IllegalArgumentException( "zenithOffset + zenithRange > pi" );

		final double minimumAzimuth;
		final double maximumAzimuth;

		if ( Math.abs( azimuthRange ) >= 2.0 * Math.PI )
		{
			minimumAzimuth = -Math.PI;
			maximumAzimuth = Math.PI;
		}
		else
		{
			final double normalAzimuthOffset = normalize( azimuthOffset );

			minimumAzimuth = normalAzimuthOffset;
			maximumAzimuth = normalize( normalAzimuthOffset + azimuthRange );
		}

		final double minimumZenith = ( zenithRange >= 0.0 ) ? zenithOffset : zenithEndOffset;
		final double maximumZenith = ( zenithRange <  0.0 ) ? zenithOffset : zenithEndOffset;

		return new SphericalBounds( minimumAzimuth , maximumAzimuth , minimumZenith , maximumZenith );
	}

	/**
	 * Returns spherical bounds for the specified angular ranges.
	 *
	 * @param   azimuthOffset   Azimuth offset, in radians.
	 * @param   azimuthRange    Azimuth range, in radians.
	 * @param   zenithOffset    Zenith offset, in radians, between 0.0 and pi.
	 * @param   zenithRange     Zenith range, in radians, between -pi and pi.
	 * @param   delta           Amount to be added to the bounds on each side,
	 *                          in radians.
	 *
	 * @throws  IllegalArgumentException if zenithOffset + zenithRange is not
	 *          in the range from 0.0 to pi (both inclusive).
	 */
	public static SphericalBounds fromRange( final double azimuthOffset , final double azimuthRange , final double zenithOffset , final double zenithRange , final double delta )
	{
		/*
		 * Extend offset/range to include the given delta.
		 */
		final double extendedAzimuthRange  = ( azimuthRange >= 0.0 ) ? azimuthRange + 2.0 * delta : azimuthRange - 2.0 * delta;
		final double extendedAzimuthOffset = azimuthOffset - Math.signum( extendedAzimuthRange ) * delta;
		final double extendedZenithRange   = ( zenithRange >= 0.0 ) ? zenithRange + 2.0 * delta : zenithRange - 2.0 * delta;
		final double extendedZenithOffset  = zenithOffset - Math.signum( extendedZenithRange ) * delta;

		/*
		 * Prevent that zenith offset or range exceeds its valid range.
		 */
		final double normalZenithOffset = Math.min( Math.max( 0.0 , extendedZenithOffset ) , Math.PI );
		final double normalZenithRange  = Math.min( Math.max( -normalZenithOffset , extendedZenithRange ) , Math.PI - normalZenithOffset );

		return fromRange( extendedAzimuthOffset , extendedAzimuthRange , normalZenithOffset , normalZenithRange );
	}

	/**
	 * Returns spherical bounds containing only the direction from the origin
	 * to the given point.
	 *
	 * @param   point   Point, in cartesian coordinates.
	 *
	 * @return  Spherical bounds containing the direction to the given point.
	 */
	public static SphericalBounds fromDirectionTo( final Vector3D point )
	{
		final Vector3D polar = point.cartesianToPolar();
		return new SphericalBounds( polar.y , polar.y , polar.z , polar.z );
	}

	/**
	 * Returns the minimum azimuth included in the bounds.
	 *
	 * @return  Minimum azimuth, in the range from -pi to pi (both inclusive).
	 */
	public double getMinimumAzimuth()
	{
		return _minimumAzimuth;
	}

	/**
	 * Returns the azimuth inside the bounds equidistant to both the minimum and
	 * maximum azimuth.
	 *
	 * @return  Center azimuth.
	 */
	private double getCenterAzimuth()
	{
		double range = _maximumAzimuth - _minimumAzimuth;
		if ( range < 0.0 )
			range += 2.0 * Math.PI;

		final double centerAzimuth = _minimumAzimuth + range / 2.0;

		return ( centerAzimuth > Math.PI ) ? centerAzimuth - 2.0 * Math.PI
		                                   : centerAzimuth;
	}

	/**
	 * Returns the maximum azimuth included in the bounds.
	 *
	 * @return  Maximum azimuth, in the range from -pi to pi (both inclusive).
	 */
	public double getMaximumAzimuth()
	{
		return _maximumAzimuth;
	}

	/**
	 * Returns the minimum zenith included in the bounds.
	 *
	 * @return  Minimum zenith, in the range from -pi to pi (both inclusive).
	 */
	public double getMinimumZenith()
	{
		return _minimumZenith;
	}

	/**
	 * Returns the zenith inside the bounds equidistant to both the minimum and
	 * maximum zenith.
	 *
	 * @return  Center zenith.
	 */
	private double getCenterZenith()
	{
		return ( _minimumZenith + _maximumZenith ) / 2.0;
	}

	/**
	 * Returns the maximum zenith included in the bounds.
	 *
	 * @return  Maximum zenith, in the range from -pi to pi (both inclusive).
	 */
	public double getMaximumZenith()
	{
		return _maximumZenith;
	}

	/**
	 * Returns whether the spherical bounds are empty.
	 *
	 * @return  <code>true</code> if the bounds are empty;
	 *          <code>false</code> otherwise.
	 */
	public boolean isEmpty()
	{
		return Double.isNaN( _minimumAzimuth );
	}

	/**
	 * Returns <code>true</code> if the spherical bounds contain only a single
	 * direction.
	 *
	 * @return  <code>true</code> if the bounds contain a single angle;
	 *          <code>false</code> otherwise.
	 */
	public boolean isPoint()
	{
		return ( _minimumAzimuth == _maximumAzimuth ) &&
	           ( _minimumZenith  == _maximumZenith  );
	}

	/**
	 * Returns whether the given direction is contained in the spherical bounds.
	 *
	 * @param   azimuth     Azimuth of the direction, in radians.
	 * @param   zenith      Zenith of the direction, in radians.
	 *
	 * @return  <code>true</code> if the direction is contained in the bounds;
	 *          <code>false</code> otherwise.
	 */
	public boolean contains( final double azimuth , final double zenith )
	{
		final boolean result;

		if ( isEmpty() )
		{
			result = false;
		}
		else
		{
			final double signedNormalZenith = normalize( zenith );
			final double normalAzimuth      = normalize( ( signedNormalZenith >= 0.0 ) ? azimuth : azimuth + Math.PI );
			final double normalZenith       = Math.abs( signedNormalZenith );

			final double minimumAzimuth = _minimumAzimuth;
			final double maximumAzimuth = _maximumAzimuth;
			final double minimumZenith  = _minimumZenith;
			final double maximumZenith  = _maximumZenith;

			result = ( minimumZenith <= normalZenith ) && ( maximumZenith >= normalZenith ) &&
			         ( ( normalZenith ==     0.0 ) || // top    -> azimuth is irrelevant
			           ( normalZenith == Math.PI ) || // bottom -> azimuth is irrelevant
			           ( ( minimumAzimuth <= maximumAzimuth ) ? ( minimumAzimuth <= normalAzimuth ) && ( maximumAzimuth >= normalAzimuth )
			                                                  : ( minimumAzimuth <= normalAzimuth ) || ( maximumAzimuth >= normalAzimuth ) ) );
		}

		return result;
	}

	/**
	 * Returns whether the given direction is contained in the spherical bounds.
	 *
	 * @param   azimuth     Azimuth of the direction, in radians.
	 * @param   zenith      Zenith of the direction, in radians.
	 *
	 * @return  <code>true</code> if the direction is contained in the bounds;
	 *          <code>false</code> otherwise.
	 */
	public boolean contains( final double azimuth , final double zenith , final boolean includeOpposite )
	{
		return contains( azimuth , zenith ) ||
	           ( includeOpposite && contains( azimuth , zenith + Math.PI ) );
	}

	/**
	 * Returns whether the direction from the origin to the given cartesian
	 * coordinates is contained in the spherical bounds.
	 *
	 * @param   cartesian   Point in cartesian coordinates.
	 *
	 * @return  <code>true</code> if the direction is contained in the bounds;
	 *          <code>false</code> otherwise.
	 */
	public boolean containsDirectionTo( final Vector3D cartesian )
	{
		final Vector3D polar = cartesian.cartesianToPolar();
		return contains( polar.y , polar.z );
	}

	/**
	 * Returns whether the given direction is contained in the spherical bounds.
	 *
	 * @param   direction   Direction indicated by a vector in polar/spherical
	 *                      coordinates. See {@link Vector3D#cartesianToPolar()}.
	 *
	 * @return  <code>true</code> if the direction is contained in the bounds;
	 *          <code>false</code> otherwise.
	 */
	public boolean contains( final Vector3D direction )
	{
		return contains( direction.y , direction.z );
	}

	/**
	 * Returns whether the given direction or its opposite is contained in the
	 * spherical bounds.
	 *
	 * @param   direction           Direction indicated by a vector in spherical
	 *                              coordinates. See {@link Vector3D#cartesianToPolar()}.
	 * @param   includeOpposite     If <code>true</code>, the opposite direction
	 *                              is also checked.
	 *
	 * @return  <code>true</code> if the direction is contained in the bounds;
	 *          or if <code>includeOpposite</code> is true and the opposite
	 *          direction is contained in the bounds;
	 *          <code>false</code> otherwise.
	 */
	public boolean contains( final Vector3D direction , final boolean includeOpposite )
	{
		return contains( direction.y , direction.z , includeOpposite );
	}

	/**
	 * Returns whether the given spherical bounds are contained inside these
	 * spherical bounds.
	 *
	 * @param   bounds              Bounds to be checked.
	 * @param   includeOpposite     If <code>true</code>, the opposite direction
	 *                              is also checked.
	 *
	 * @return  <code>true</code> if the bounds contain each the extreme angles
	 *          of the given bounds; or if <code>includeOpposite</code> is true
	 *          and either both extreme angles or both their opposites are
	 *          contained in the bounds;
	 *          <code>false</code> otherwise.
	 */
	public boolean contains( final SphericalBounds bounds , final boolean includeOpposite )
	{
		final boolean containsMinimum = contains( bounds.getMinimumAzimuth() , bounds.getMinimumZenith() , false );
		final boolean containsCenter  = contains( bounds.getCenterAzimuth()  , bounds.getCenterZenith()  , false );
		final boolean containsMaximum = contains( bounds.getMaximumAzimuth() , bounds.getMaximumZenith() , false );

		final boolean result;

		if ( !includeOpposite || ( containsMinimum || containsCenter || containsMaximum ) )
		{
			result = ( containsMinimum && containsCenter && containsMaximum );
		}
		else
		{
			result = ( contains( bounds.getMinimumAzimuth() , bounds.getMinimumZenith() , true ) &&
			           contains( bounds.getCenterAzimuth()  , bounds.getCenterZenith()  , true ) &&
			           contains( bounds.getMaximumAzimuth() , bounds.getMaximumZenith() , true ) );
		}

		return result;
	}

	/**
	 * Returns the smallest spherical bounds containing both these bounds and
	 * the given direction. If the bounds already contain the given direction,
	 * <code>this</code> is returned.
	 *
	 * @param   azimuth     Azimuth of the direction, in radians.
	 * @param   zenith      Zenith of the direction, in radians.
	 *
	 * @return  Spherical bounds containing both these bounds and the given
	 *          direction.
	 */
	public SphericalBounds add( final double azimuth , final double zenith )
	{
		final SphericalBounds result;

		if ( isEmpty() )
		{
			result = new SphericalBounds( azimuth , zenith );
		}
		else if ( contains( azimuth , zenith ) )
		{
			result = this;
		}
		else
		{
			final double signedNormalZenith = normalize( zenith );
			final double normalAzimuth      = normalize( ( signedNormalZenith >= 0.0 ) ? azimuth : azimuth + Math.PI );
			final double normalZenith       = Math.abs( signedNormalZenith );

			final double azimuthFromMinimum = angleBetween( _minimumAzimuth , normalAzimuth );
			final double azimuthFromMaximum = angleBetween( _maximumAzimuth , normalAzimuth );

			if ( azimuthFromMinimum <= azimuthFromMaximum )
			{
				result = new SphericalBounds( normalAzimuth , _maximumAzimuth , Math.min( _minimumZenith  , normalZenith  ) , Math.max( _maximumZenith  , normalZenith  ) );
			}
			else
			{
				result = new SphericalBounds( _minimumAzimuth , normalAzimuth , Math.min( _minimumZenith  , normalZenith  ) , Math.max( _maximumZenith  , normalZenith  ) );
			}
		}

		return result;
	}

	/**
	 * Returns spherical bounds containing both these bounds and the direction
	 * from the origin to the given point. If the bounds already contain the
	 * given direction, <code>this</code> is returned.
	 *
	 * @param   point   Point in cartesian coordinates.
	 *
	 * @return  Spherical bounds containing both these bounds and the given
	 *          direction.
	 */
	public SphericalBounds addDirectionTo( final Vector3D point )
	{
		final Vector3D polar = point.cartesianToPolar();
		return add( polar.y , polar.z );
	}

	/**
	 * Normalizes the given angle to the range from -pi to pi (both inclusive).
	 *
	 * @param   angle   Angle to be normalized.
	 *
	 * @return  The equivalent angle in the range [-pi, pi].
	 */
	private static double normalize( final double angle )
	{
		final double twoPi = 2.0 * Math.PI;

		final double angleModTwoPi = angle % twoPi;

		final double result;
		if ( angleModTwoPi > Math.PI )
		{
			result = angleModTwoPi - twoPi;
		}
		else if ( angleModTwoPi < -Math.PI )
		{
			result = angleModTwoPi + twoPi;
		}
		else
		{
			result = angleModTwoPi;
		}

		return result;
	}

	/**
	 * Returns the smallest angle between the given angles.
	 *
	 * @param   first   First angle, in radians, between -pi and pi.
	 * @param   second  Second angle, in radians, between -pi and pi.
	 *
	 * @return
	 */
	private static double angleBetween( final double first , final double second )
	{
		final double difference = Math.abs( first - second );
		return ( difference > Math.PI ) ? Math.PI * 2.0 - difference
		                                : difference;
	}

	public int hashCode()
	{
		final long bigHash = ( Double.doubleToLongBits( _minimumAzimuth ) ^
		                       Double.doubleToLongBits( _maximumAzimuth ) ^
		                       Double.doubleToLongBits( _minimumZenith  ) ^
		                       Double.doubleToLongBits( _maximumZenith  ) );
		return (int)( bigHash >>> 32 ) ^ (int)bigHash;
	}

	public boolean equals( final Object other )
	{
		final boolean result;

		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof SphericalBounds )
		{
			final SphericalBounds bounds = (SphericalBounds)other;
			result = ( ( _minimumAzimuth == bounds._minimumAzimuth ) &&
			           ( _maximumAzimuth == bounds._maximumAzimuth ) &&
			           ( _minimumZenith  == bounds._minimumZenith  ) &&
			           ( _maximumZenith  == bounds._maximumZenith  ) );
		}
		else
		{
			result = false;
		}

		return result;
	}

	public String toString()
	{
		final Class<?> clazz = getClass();
		return clazz.getName() + "[ azimuth = [ " + _minimumAzimuth + " ; " + _maximumAzimuth + " ], zenith = [ " + _minimumZenith + " ; " + _maximumZenith + " ] ]";
	}
}
