/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
 */
package ab.j3d;

import java.text.*;
import java.util.*;
import java.util.regex.*;

import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * This class is used to represent a 3D transformation matrix (although it may
 * also be used for 2D transformations).
 *
 * <p>
 * The matrix is organized as follows:
 * <pre>
 * | xx xy xz xo |
 * | yx yy yz yo |
 * | zx zy zz zo |
 * | 0  0  0  1  |
 * </pre>
 *
 * <p>
 * <strong>CAUTION</strong>:
 * For historic reasons and compatibility with existing code, the methods
 * {@link #multiply} and {@link #multiplyInverse} premultiply the operands:
 * {@code multiply( A, B )} yields {@code B * A}. This is opposite to common
 * mathematical conventions, so beware!
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "StandardVariableNames", "FieldNamingConvention" } )
public class Matrix3D
{
	/** X quotient for X component. */
	public final double xx;

	/** Y quotient for X component. */
	public final double xy;

	/** Z quotient for X component. */
	public final double xz;

	/** Translation of X component. */
	public final double xo;

	/** X quotient for Y component. */
	public final double yx;

	/** Y quotient for Y component. */
	public final double yy;

	/** Z quotient for Y component. */
	public final double yz;

	/** Translation of Y component. */
	public final double yo;

	/** X quotient for Z component. */
	public final double zx;

	/** Y quotient for Z component. */
	public final double zy;

	/** Z quotient for Z component. */
	public final double zz;

	/** Translation of Z component. */
	public final double zo;

	/**
	 * Identity matrix.
	 */
	public static final Matrix3D IDENTITY = new Matrix3D(
	1.0, 0.0, 0.0, 0.0,
	0.0, 1.0, 0.0, 0.0,
	0.0, 0.0, 1.0, 0.0 );

	/**
	 * Regex pattern only consisting of comma character.
	 */
	private static final Pattern COMMA = Pattern.compile( "," );

	/**
	 * Construct a new matrix.
	 *
	 * @param nxx X quotient for X component.
	 * @param nxy Y quotient for X component.
	 * @param nxz Z quotient for X component.
	 * @param nxo Translation of X component.
	 * @param nyx X quotient for Y component.
	 * @param nyy Y quotient for Y component.
	 * @param nyz Z quotient for Y component.
	 * @param nyo Translation of Y component.
	 * @param nzx X quotient for Z component.
	 * @param nzy Y quotient for Z component.
	 * @param nzz Z quotient for Z component.
	 * @param nzo Translation of Z component.
	 */
	public Matrix3D( final double nxx, final double nxy, final double nxz, final double nxo,
	                 final double nyx, final double nyy, final double nyz, final double nyo,
	                 final double nzx, final double nzy, final double nzz, final double nzo )
	{
		xx = nxx;
		xy = nxy;
		xz = nxz;
		xo = nxo;

		yx = nyx;
		yy = nyy;
		yz = nyz;
		yo = nyo;

		zx = nzx;
		zy = nzy;
		zz = nzz;
		zo = nzo;
	}

	/**
	 * Get cosine of the given angle in decimal degrees.
	 *
	 * This method makes an effort to run optimized/rounded results for common
	 * angles in 45 degree intervals.
	 *
	 * @param deg Angle in decimal degrees.
	 *
	 * @return Cosine of angle.
	 */
	private static double degcos( final double deg )
	{
		final double result;

		final int i = (int)deg;
		//noinspection Duplicates
		switch ( ( i == deg )  ? ( i % 360 ) : -1 )
		{
			case 0:
				result = 1;
				break;

//			case 45:
//			case 315:
//			case -45:
//			case -315:
//				result = SQRT05;
//				break;

			case 90:
			case 270:
			case -90:
			case -270:
				result = 0;
				break;

//			case 135:
//			case 225:
//			case -135:
//			case -225:
//				result = -SQRT05;
//				break;

			case 180:
			case -180:
				result = -1;
				break;

			default:
				result = Math.cos( Math.toRadians( deg ) );
		}

		return result;
	}

	/**
	 * Get sine of the given angle in decimal degrees.
	 *
	 * This method makes an effort to run optimized/rounded results for common
	 * angles in 45 degree intervals.
	 *
	 * @param deg Angle in decimal degrees.
	 *
	 * @return Sine of angle.
	 */
	private static double degsin( final double deg )
	{
		final double result;

		final int i = (int)deg;
		//noinspection Duplicates
		switch ( ( i == deg ) ? ( i % 360 ) : -1 )
		{
			case 0:
			case 180:
			case -180:
				result = 0;
				break;

//			case 45:
//			case 135:
//			case -315:
//			case -225:
//				result = SQRT05;
//				break;

			case 90:
			case -270:
				result = 1;
				break;

//			case 225:
//			case 315:
//			case -45:
//			case -135:
//				result = -SQRT05;
//				break;

			case 270:
			case -90:
				result = -1;
				break;

			default:
				result = Math.sin( Math.toRadians( deg ) );
		}

		return result;
	}

	/**
	 * Get {@code Matrix3D} property with the specified name from a {@link
	 * Properties} object.
	 *
	 * @param properties Properties to get matrix from.
	 * @param name       Property name.
	 *
	 * @return {@code Matrix3D} object; {@code null} if property value is
	 * absent/invalid.
	 */
	@Contract( "null,_ -> null" )
	@Nullable
	public static Matrix3D getProperty( @Nullable final Properties properties, @NotNull final String name )
	{
		return getProperty( properties, name, null );
	}

	/**
	 * Get {@code Matrix3D} property with the specified name from a {@link
	 * Properties} object.
	 *
	 * @param properties   Properties to get matrix from.
	 * @param name         Property name.
	 * @param defaultValue Value to use if property value is absent/invalid.
	 *
	 * @return {@code Matrix3D} object; {@code defaultValue} if property value
	 * is absent/invalid.
	 */
	@Contract( "null,_,_ -> null; _,_,!null -> !null" )
	@Nullable
	public static Matrix3D getProperty( @Nullable final Properties properties, @NotNull final String name, @Nullable final Matrix3D defaultValue )
	{
		Matrix3D result = defaultValue;

		final String stringValue = ( properties != null ) ? properties.getProperty( name, null ) : null;
		if ( stringValue != null )
		{
			try
			{
				result = fromString( stringValue );
			}
			catch ( final Exception e )
			{
				/* ignore errors => return default */
			}
		}

		return result;
	}

	/**
	 * Compare this matrix to another matrix.
	 *
	 * @param other Matrix to compare with.
	 *
	 * @return {@code true} if the objects are almost equal; {@code false} if
	 * not.
	 *
	 * @see GeometryTools#almostEqual
	 */
	@Contract( "null -> false" )
	public boolean almostEquals( @Nullable final Matrix3D other )
	{
		//noinspection ObjectEquality
		return ( other != null )
		       && ( ( other == this )
		            || ( GeometryTools.almostEqual( xx, other.xx ) &&
		                 GeometryTools.almostEqual( xy, other.xy ) &&
		                 GeometryTools.almostEqual( xz, other.xz ) &&
		                 GeometryTools.almostEqual( xo, other.xo ) &&
		                 GeometryTools.almostEqual( yx, other.yx ) &&
		                 GeometryTools.almostEqual( yy, other.yy ) &&
		                 GeometryTools.almostEqual( yz, other.yz ) &&
		                 GeometryTools.almostEqual( yo, other.yo ) &&
		                 GeometryTools.almostEqual( zx, other.zx ) &&
		                 GeometryTools.almostEqual( zy, other.zy ) &&
		                 GeometryTools.almostEqual( zz, other.zz ) &&
		                 GeometryTools.almostEqual( zo, other.zo ) ) );
	}

	@Override
	public boolean equals( final Object other )
	{
		final boolean result;

		if ( other == this )
		{
			result = true;
		}
		else if ( !( other instanceof Matrix3D ) )
		{
			result = false;
		}
		else
		{
			final Matrix3D m = (Matrix3D)other;

			result = ( xx == m.xx ) && ( xy == m.xy ) && ( xz == m.xz ) && ( xo == m.xo )
			         && ( yx == m.yx ) && ( yy == m.yy ) && ( yz == m.yz ) && ( yo == m.yo )
			         && ( zx == m.zx ) && ( zy == m.zy ) && ( zz == m.zz ) && ( zo == m.zo );
		}

		return result;
	}

	@Override
	public int hashCode()
	{
		long l;
		int result;
		l = Double.doubleToLongBits( xx );
		result = (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( xy );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( xz );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( xo );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( yx );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( yy );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( yz );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( yo );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( zx );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( zy );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( zz );
		result ^= (int)( l ^ l >>> 32 );
		l = Double.doubleToLongBits( zo );
		result ^= (int)( l ^ l >>> 32 );
		return result;
	}

	/**
	 * Convert string representation of matrix back to {@code Matrix3D} instance
	 * (see {@code toString()}).
	 *
	 * @param value String representation of object.
	 *
	 * @return Matrix3D instance.
	 *
	 * @throws NullPointerException if {@code value} is {@code null}.
	 * @throws IllegalArgumentException if the string format is unrecognized.
	 * @throws NumberFormatException if any of the numeric components are badly
	 * formatted.
	 * @see #toString()
	 */
	@NotNull
	public static Matrix3D fromString( @SuppressWarnings( "TypeMayBeWeakened" ) @NotNull final String value )
	{
		final String[] tokens = COMMA.split( value, 0 );
		if ( tokens.length != 12 )
		{
			throw new IllegalArgumentException( "tokens" );
		}

		final double xx = Double.parseDouble( tokens[ 0 ] );
		final double xy = Double.parseDouble( tokens[ 1 ] );
		final double xz = Double.parseDouble( tokens[ 2 ] );
		final double xo = Double.parseDouble( tokens[ 3 ] );

		final double yx = Double.parseDouble( tokens[ 4 ] );
		final double yy = Double.parseDouble( tokens[ 5 ] );
		final double yz = Double.parseDouble( tokens[ 6 ] );
		final double yo = Double.parseDouble( tokens[ 7 ] );

		final double zx = Double.parseDouble( tokens[ 8 ] );
		final double zy = Double.parseDouble( tokens[ 9 ] );
		final double zz = Double.parseDouble( tokens[ 10 ] );
		final double zo = Double.parseDouble( tokens[ 11 ] );

		return new Matrix3D( xx, xy, xz, xo,
		                     yx, yy, yz, yo,
		                     zx, zy, zz, zo );
	}

	/**
	 * Get transformation matrix based on 6 parameters specifying rotation
	 * angles and a translation vector. Starting with the identity matrix,
	 * rotation is performed (Z,X,Y order), than the translation is set.
	 *
	 * @param rx Rotation angle around X axis (degrees, <strong>clockwise</strong>).
	 * @param ry Rotation angle around Y axis (degrees, counter-clockwise).
	 * @param rz Rotation angle around Z axis (degrees, counter-clockwise).
	 * @param tx X component of translation vector.
	 * @param ty Y component of translation vector.
	 * @param tz Z component of translation vector.
	 *
	 * @return Transformation matrix.
	 */
	public static Matrix3D getTransform( final double rx, final double ry, final double rz, final double tx, final double ty, final double tz )
	{
		// FIXME: rx should be counter-clockwise!

		final double ctX = degcos( rx );
		final double stX = degsin( rx );
		final double ctY = degcos( ry );
		final double stY = degsin( ry );
		final double ctZ = degcos( rz );
		final double stZ = degsin( rz );

		return new Matrix3D(
		/* xx */  ctZ * ctY - stZ * stX * stY,
		/* xy */ -stZ * ctY - ctZ * stX * stY,
		/* xz */  ctX * stY,
		/* xo */  tx,

		/* yx */  stZ * ctX,
		/* yy */  ctZ * ctX,
		/* yz */        stX,
		/* y0 */  ty,

		/* zx */ -stZ * stX * ctY - ctZ * stY,
		/* zy */ -ctZ * stX * ctY + stZ * stY,
		/* zz */        ctX * ctY,
		/* zo */  tz );
	}

	/**
	 * Get transformation matrix based on the specified 'from' and 'to' points.
	 * The result can be used to convert world coordinates to view coordinates.
	 *
	 * <p>
	 * An up-vector must be specified to determine the correct view orientation.
	 * Note that both a primary and a secondary up-vector is needed; the primary
	 * up-vector is used when possible, the secondary up-vector is used when the
	 * from-to vector is parallel to the primary up-vector.
	 *
	 * @param from        Point to look from.
	 * @param to          Point to look at.
	 * @param upPrimary   Primary up-vector (must be normalized).
	 * @param upSecondary Secondary up-vector (must be normalized).
	 *
	 * @return Transformation matrix.
	 *
	 * @throws NullPointerException if any of the arguments is {@code null}.
	 * @throws IllegalArgumentException if the from and two points are too
	 * close.
	 */
	public static Matrix3D getFromToTransform( final Vector3D from, final Vector3D to, final Vector3D upPrimary, final Vector3D upSecondary )
	{
		if ( from.almostEquals( to ) )
		{
			throw new IllegalArgumentException( "from ~= to" );
		}

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
		 * Select up-vector. If the co-sinus of the angle between the Z-axis
		 * and up-vector approaches 1 or -1 (0 or 180 degrees), use the secondary
		 * up-vector; use the primary up-vector otherwise.
		 */
		Vector3D up = upPrimary;
		final double cos = up.x * zx + up.y * zy + up.z * zz;
		if ( ( cos < -0.999 ) || ( cos > 0.999 ) )
		{
			up = upSecondary;
		}

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

		/*
		 * Create matrix.
		 */
		return new Matrix3D( xx, xy, xz, ( -from.x * xx - from.y * xy - from.z * xz ),
		                     yx, yy, yz, ( -from.x * yx - from.y * yy - from.z * yz ),
		                     zx, zy, zz, ( -from.x * zx - from.y * zy - from.z * zz ) );
	}

	/**
	 * Calculate transform for a plane that passes through {@code origin} and
	 * has the specified {@code normal} vector.
	 *
	 * <p>
	 * The main purpose for this method is creating a suitable 3D transformation
	 * for a 2D plane whose Z-axis points 'out' of the plane; the orientation of
	 * the X/Y-axes on the plane is inherently indeterminate, but this function
	 * tries to find reasonable defaults.
	 *
	 * <p>
	 * A 0-vector multiplied with the resulting transform will match the {@code
	 * origin}.
	 *
	 * @param origin      Origin of plane.
	 * @param normal      Normal of plane.
	 * @param rightHanded 3D-space is right- vs. left-handed.
	 *
	 * @return Transformation matrix (translation set to 0-vector) to be used
	 * for extrusion of 2D shapes.
	 */
	public static Matrix3D getPlaneTransform( final Vector3D origin, final Vector3D normal, final boolean rightHanded )
	{
		return getPlaneTransform( origin.x, origin.y, origin.z, normal.x, normal.y, normal.z, rightHanded );
	}

	/**
	 * Calculate transform for a plane that passes through {@code origin} and
	 * has the specified {@code normal} vector.
	 *
	 * <p>
	 * The main purpose for this method is creating a suitable 3D transformation
	 * for a 2D plane whose Z-axis points 'out' of the plane; the orientation of
	 * the X/Y-axes on the plane is inherently indeterminate, but this function
	 * tries to find reasonable defaults.
	 *
	 * <p>
	 * A 0-vector multiplied with the resulting transform will match the {@code
	 * origin}.
	 *
	 * @param originX     Origin X-coordinate of plane.
	 * @param originY     Origin X-coordinate of plane.
	 * @param originZ     Origin X-coordinate of plane.
	 * @param normalX     X-component of normal vector of plane.
	 * @param normalY     X-component of normal vector of plane.
	 * @param normalZ     X-component of normal vector of plane.
	 * @param rightHanded 3D-space is right- vs. left-handed.
	 *
	 * @return Transformation matrix (translation set to 0-vector) to be used
	 * for extrusion of 2D shapes.
	 */
	public static Matrix3D getPlaneTransform( final double originX, final double originY, final double originZ, final double normalX, final double normalY, final double normalZ, final boolean rightHanded )
	{
		/*
		 * X-axis direction is perpendicular to the plane between the (local)
		 * Z-axis and the world's Z-axis. If the Z-axes are almost parallel,
		 * then the world Y-axis is used.
		 *
		 * This will keep the local X-axis on the X/Y-plane as much as possible.
		 */
		final double xAxisX;
		final double xAxisY;
		final double xAxisZ;

		if ( ( normalZ > -0.9 ) && ( normalZ < 0.9 ) )
		{
			final double hyp = Math.hypot( normalX, normalY );

			xAxisX = rightHanded ? -normalY / hyp : normalY / hyp;
			xAxisY = rightHanded ? normalX / hyp : -normalX / hyp;
			xAxisZ = 0.0;
		}
		else
		{
			final double hyp = Math.hypot( normalX, normalZ );

			xAxisX = rightHanded ? normalZ / hyp : -normalZ / hyp;
			xAxisY = 0.0;
			xAxisZ = rightHanded ? -normalX / hyp : normalX / hyp;
		}

		/*
		 * Y-axis can be derived simply from the calculated X- and Z-axis.
		 */
		final double yAxisX = normalY * xAxisZ - normalZ * xAxisY;
		final double yAxisY = normalZ * xAxisX - normalX * xAxisZ;
		final double yAxisZ = normalX * xAxisY - normalY * xAxisX;

		return new Matrix3D( xAxisX, yAxisX, normalX, originX,
		                     xAxisY, yAxisY, normalY, originY,
		                     xAxisZ, yAxisZ, normalZ, originZ );
	}

	/**
	 * Get rotation matrix. This eliminates scaling and translation properties
	 * from the current transformation matrix.
	 *
	 * @return Rotation material.
	 */
	public Matrix3D getRotation()
	{
		final double lx = Vector3D.length( xx, xy, xz );
		final double ly = Vector3D.length( yx, yy, yz );
		final double lz = Vector3D.length( zx, zy, zz );

		return new Matrix3D( xx / lx, xy / lx, xz / lx, 0.0,
		                     yx / ly, yy / ly, yz / ly, 0.0,
		                     zx / lz, zy / lz, zz / lz, 0.0 );
	}

	/**
	 * Get transformation matrix for a rotation about an arbitrary axis. The
	 * rotation is axis is specified by a pivot point and rotation axis
	 * direction. The rotation angle is specified in radians.
	 *
	 * <p>
	 * Also read <a href='http://www.cprogramming.com/tutorial/3d/rotation.html'>Rotation
	 * About an Arbitrary Axis</a> (written by: Confuted, with a cameo by
	 * Silvercord (Charles Thibualt)).
	 *
	 * @param pivot     Pivot point about which the rotation is performed.
	 * @param direction Rotation axis direction (must be a unit vector).
	 * @param thetaRad  Rotate theta radians.
	 *
	 * @return Transformation matrix with requested rotation.
	 */
	public static Matrix3D getRotationTransform( final Vector3D pivot, final Vector3D direction, final double thetaRad )
	{
		return getRotationTransform( pivot.x, pivot.y, pivot.z, direction.x, direction.y, direction.z, thetaRad );
	}

	/**
	 * Get transformation matrix for a rotation about an arbitrary axis. The
	 * rotation is axis is specified by a pivot point and rotation axis
	 * direction. The rotation angle is specified in radians.
	 *
	 * <p>
	 * Read <a href='http://www.mlahanas.de/Math/orientation.htm'>We consider
	 * the problem of the coordinate transformation about a rotation axis.</a>
	 * (written by Michael Lahanas) for a simple explanation of the problem and
	 * solution.
	 *
	 * @param pivotX     Pivot point about which the rotation is performed.
	 * @param pivotY     Pivot point about which the rotation is performed.
	 * @param pivotZ     Pivot point about which the rotation is performed.
	 * @param directionX Rotation axis direction (must be a unit vector).
	 * @param directionY Rotation axis direction (must be a unit vector).
	 * @param directionZ Rotation axis direction (must be a unit vector).
	 * @param thetaRad   Rotate theta radians.
	 *
	 * @return Transformation matrix with requested rotation.
	 */
	public static Matrix3D getRotationTransform( final double pivotX, final double pivotY, final double pivotZ, final double directionX, final double directionY, final double directionZ, final double thetaRad )
	{
		final double cos = Math.cos( thetaRad );
		final double sin = Math.sin( thetaRad );

		final double t = ( 1.0 - cos );
		final double tx = t * directionX;
		final double ty = t * directionY;
		final double tz = t * directionZ;

		final double xx = tx * directionX + cos;
		final double xy = tx * directionY - sin * directionZ;
		final double xz = tx * directionZ + sin * directionY;

		final double yx = ty * directionX + sin * directionZ;
		final double yy = ty * directionY + cos;
		final double yz = ty * directionZ - sin * directionX;

		final double zx = tz * directionX - sin * directionY;
		final double zy = tz * directionY + sin * directionX;
		final double zz = tz * directionZ + cos;

		return new Matrix3D( xx, xy, xz, pivotX - xx * pivotX - xy * pivotY - xz * pivotZ,
		                     yx, yy, yz, pivotY - yx * pivotX - yy * pivotY - yz * pivotZ,
		                     zx, zy, zz, pivotZ - zx * pivotX - zy * pivotY - zz * pivotZ );
	}

	/**
	 * Returns a transformation matrix that uniformly scales coordinates by the
	 * given factor.
	 *
	 * @param scale Scaling factor.
	 *
	 * @return Scaling matrix with the given factor.
	 */
	public static Matrix3D getScaleTransform( final double scale )
	{
		return getScaleTransform( scale, scale, scale );
	}

	/**
	 * Returns a transformation matrix that non-uniformly scales coordinates by
	 * the factor given for each axis.
	 *
	 * @param x Scaling factor for the x-axis.
	 * @param y Scaling factor for the y-axis.
	 * @param z Scaling factor for the z-axis.
	 *
	 * @return Scaling matrix with the given factor.
	 */
	public static Matrix3D getScaleTransform( final double x, final double y, final double z )
	{
		return new Matrix3D( x, 0.0, 0.0, 0.0,
		                     0.0, y, 0.0, 0.0,
		                     0.0, 0.0, z, 0.0 );
	}

	/**
	 * Construct inverse matrix.
	 * <pre>
	 *       | xx xy xz xo |
	 *       | yx yy yz yo |
	 *  T  = | zx zy zz zo |
	 *       | 0  0  0  1  |
	 *
	 *       | xx yz zx -xo*xx-yo*yx-zo*zx |
	 *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
	 *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
	 *       | 0  0  0  1                  |
	 * </pre>
	 *
	 * @return Inverse matrix.
	 */
	public Matrix3D inverse()
	{
		return new Matrix3D( xx, yx, zx, inverseXo(),
		                     xy, yy, zy, inverseYo(),
		                     xz, yz, zz, inverseZo() );
	}

	/**
	 * Get {@code xo} of inverse transform.
	 * <pre>
	 *       | xx xy xz xo |
	 *       | yx yy yz yo |
	 *  T  = | zx zy zz zo |
	 *       | 0  0  0  1  |
	 *
	 *       | xx yz zx <b>-xo*xx-yo*yx-zo*zx</b> |
	 *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
	 *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
	 *       | 0  0  0  1                  |
	 * </pre>
	 *
	 * @return Inverse matrix.
	 */
	public double inverseXo()
	{
		return -xo * xx - yo * yx - zo * zx;
	}

	/**
	 * Get {@code yo} of inverse transform.
	 * <pre>
	 *       | xx xy xz xo |
	 *       | yx yy yz yo |
	 *  T  = | zx zy zz zo |
	 *       | 0  0  0  1  |
	 *
	 *       | xx yz zx -xo*xx-yo*yx-zo*zx |
	 *   -1  | xy yy zy <b>-xo*xy-yo*yy-zo*zy</b> |
	 *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
	 *       | 0  0  0  1                  |
	 * </pre>
	 *
	 * @return Inverse matrix.
	 */
	public double inverseYo()
	{
		return -xo * xy - yo * yy - zo * zy;
	}

	/**
	 * Get {@code zo} of inverse transform.
	 * <pre>
	 *       | xx xy xz xo |
	 *       | yx yy yz yo |
	 *  T  = | zx zy zz zo |
	 *       | 0  0  0  1  |
	 *
	 *       | xx yz zx -xo*xx-yo*yx-zo*zx |
	 *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
	 *  T  = | xz yz zz <b>-xo*xz-yo*yz-zo*zz</b> |
	 *       | 0  0  0  1                  |
	 * </pre>
	 *
	 * @return Inverse matrix.
	 */
	public double inverseZo()
	{
		return -xo * xz - yo * yz - zo * zz;
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param vector Vector specifying the translation.
	 *
	 * @return new Matrix3D with translation
	 */
	public Matrix3D minus( final Vector3D vector )
	{
		return minus( vector.x, vector.y, vector.z );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param x X-coordinate of vector specifying the translation.
	 * @param y Y-coordinate of vector specifying the translation.
	 * @param z Z-coordinate of vector specifying the translation.
	 *
	 * @return new Matrix3D with translation
	 */
	public Matrix3D minus( final double x, final double y, final double z )
	{
		return ( ( x == 0.0 ) && ( y == 0.0 ) && ( z == 0.0 ) )
		       ? this
		       : setTranslation( xo - x, yo - y, zo - z );
	}

	/**
	 * Execute matrix multiplication between this and another matrix.
	 *
	 * @param other Matrix to multiply with.
	 *
	 * @return Resulting matrix (premultiplied: result = other * this).
	 */
	public Matrix3D multiply( final Matrix3D other )
	{
		return multiply( this, other );
	}

	/**
	 * Execute matrix multiplication between two matrices.
	 *
	 * @param m1 First matrix.
	 * @param m2 Second matrix.
	 *
	 * @return Resulting matrix (premultiplied: result = m2 * m1).
	 */
	public static Matrix3D multiply( final Matrix3D m1, final Matrix3D m2 )
	{
		return multiply( m1.xx, m1.xy, m1.xz, m1.xo,
		                 m1.yx, m1.yy, m1.yz, m1.yo,
		                 m1.zx, m1.zy, m1.zz, m1.zo,
		                 m2.xx, m2.xy, m2.xz, m2.xo,
		                 m2.yx, m2.yy, m2.yz, m2.yo,
		                 m2.zx, m2.zy, m2.zz, m2.zo );
	}

	/**
	 * Execute matrix multiplication between two matrices.
	 *
	 * @param xx1 X quotient for X component of first matrix.
	 * @param xy1 Y quotient for X component of first matrix.
	 * @param xz1 Z quotient for X component of first matrix.
	 * @param xo1 Translation of X component of first matrix.
	 * @param yx1 X quotient for Y component of first matrix.
	 * @param yy1 Y quotient for Y component of first matrix.
	 * @param yz1 Z quotient for Y component of first matrix.
	 * @param yo1 Translation of Y component of first matrix.
	 * @param zx1 X quotient for Z component of first matrix.
	 * @param zy1 Y quotient for Z component of first matrix.
	 * @param zz1 Z quotient for Z component of first matrix.
	 * @param zo1 Translation of Z component of first matrix.
	 * @param m2  Second matrix.
	 *
	 * @return Resulting matrix (premultiplied: result = m2 * m1).
	 */
	public static Matrix3D multiply(
	final double xx1, final double xy1, final double xz1, final double xo1,
	final double yx1, final double yy1, final double yz1, final double yo1,
	final double zx1, final double zy1, final double zz1, final double zo1,
	final Matrix3D m2 )
	{
		return multiply( xx1, xy1, xz1, xo1,
		                 yx1, yy1, yz1, yo1,
		                 zx1, zy1, zz1, zo1,
		                 m2.xx, m2.xy, m2.xz, m2.xo,
		                 m2.yx, m2.yy, m2.yz, m2.yo,
		                 m2.zx, m2.zy, m2.zz, m2.zo );
	}

	/**
	 * Execute matrix multiplication between two matrices.
	 *
	 * @param m1  First matrix.
	 * @param xx2 X quotient for X component of second matrix.
	 * @param xy2 Y quotient for X component of second matrix.
	 * @param xz2 Z quotient for X component of second matrix.
	 * @param xo2 Translation of X component of second matrix.
	 * @param yx2 X quotient for Y component of second matrix.
	 * @param yy2 Y quotient for Y component of second matrix.
	 * @param yz2 Z quotient for Y component of second matrix.
	 * @param yo2 Translation of Y component of second matrix.
	 * @param zx2 X quotient for Z component of second matrix.
	 * @param zy2 Y quotient for Z component of second matrix.
	 * @param zz2 Z quotient for Z component of second matrix.
	 * @param zo2 Translation of Z component of second matrix.
	 *
	 * @return Resulting matrix (premultiplied: result = m2 * m1).
	 */
	public static Matrix3D multiply(
	final Matrix3D m1,
	final double xx2, final double xy2, final double xz2, final double xo2,
	final double yx2, final double yy2, final double yz2, final double yo2,
	final double zx2, final double zy2, final double zz2, final double zo2 )
	{
		return multiply( m1.xx, m1.xy, m1.xz, m1.xo,
		                 m1.yx, m1.yy, m1.yz, m1.yo,
		                 m1.zx, m1.zy, m1.zz, m1.zo,
		                 xx2, xy2, xz2, xo2,
		                 yx2, yy2, yz2, yo2,
		                 zx2, zy2, zz2, zo2 );
	}

	/**
	 * Execute matrix multiplication between two matrices.
	 *
	 * @param xx1 X quotient for X component of first matrix.
	 * @param xy1 Y quotient for X component of first matrix.
	 * @param xz1 Z quotient for X component of first matrix.
	 * @param xo1 Translation of X component of first matrix.
	 * @param yx1 X quotient for Y component of first matrix.
	 * @param yy1 Y quotient for Y component of first matrix.
	 * @param yz1 Z quotient for Y component of first matrix.
	 * @param yo1 Translation of Y component of first matrix.
	 * @param zx1 X quotient for Z component of first matrix.
	 * @param zy1 Y quotient for Z component of first matrix.
	 * @param zz1 Z quotient for Z component of first matrix.
	 * @param zo1 Translation of Z component of first matrix.
	 * @param xx2 X quotient for X component of second matrix.
	 * @param xy2 Y quotient for X component of second matrix.
	 * @param xz2 Z quotient for X component of second matrix.
	 * @param xo2 Translation of X component of second matrix.
	 * @param yx2 X quotient for Y component of second matrix.
	 * @param yy2 Y quotient for Y component of second matrix.
	 * @param yz2 Z quotient for Y component of second matrix.
	 * @param yo2 Translation of Y component of second matrix.
	 * @param zx2 X quotient for Z component of second matrix.
	 * @param zy2 Y quotient for Z component of second matrix.
	 * @param zz2 Z quotient for Z component of second matrix.
	 * @param zo2 Translation of Z component of second matrix.
	 *
	 * @return Resulting matrix (premultiplied: result = m2 * m1).
	 */
	public static Matrix3D multiply(
	final double xx1, final double xy1, final double xz1, final double xo1,
	final double yx1, final double yy1, final double yz1, final double yo1,
	final double zx1, final double zy1, final double zz1, final double zo1,
	final double xx2, final double xy2, final double xz2, final double xo2,
	final double yx2, final double yy2, final double yz2, final double yo2,
	final double zx2, final double zy2, final double zz2, final double zo2 )
	{
		return new Matrix3D( xx1 * xx2 + yx1 * xy2 + zx1 * xz2,
		                     xy1 * xx2 + yy1 * xy2 + zy1 * xz2,
		                     xz1 * xx2 + yz1 * xy2 + zz1 * xz2,
		                     xo1 * xx2 + yo1 * xy2 + zo1 * xz2 + xo2,
		                     xx1 * yx2 + yx1 * yy2 + zx1 * yz2,
		                     xy1 * yx2 + yy1 * yy2 + zy1 * yz2,
		                     xz1 * yx2 + yz1 * yy2 + zz1 * yz2,
		                     xo1 * yx2 + yo1 * yy2 + zo1 * yz2 + yo2,
		                     xx1 * zx2 + yx1 * zy2 + zx1 * zz2,
		                     xy1 * zx2 + yy1 * zy2 + zy1 * zz2,
		                     xz1 * zx2 + yz1 * zy2 + zz1 * zz2,
		                     xo1 * zx2 + yo1 * zy2 + zo1 * zz2 + zo2 );
	}

	/**
	 * Multiply this matrix with another matrix and return the result of this
	 * multiplication.
	 *
	 * @param otherXX Matrix row 0, column 0 (x-factor for x).
	 * @param otherXY Matrix row 0, column 1 (y-factor for x).
	 * @param otherXZ Matrix row 0, column 2 (z-factor for x).
	 * @param otherXO Matrix row 0, column 3 (offset   for x).
	 * @param otherYX Matrix row 1, column 0 (x-factor for y).
	 * @param otherYY Matrix row 1, column 1 (y-factor for y).
	 * @param otherYZ Matrix row 1, column 2 (z-factor for y).
	 * @param otherYO Matrix row 1, column 3 (offset   for y).
	 * @param otherZX Matrix row 2, column 0 (x-factor for z).
	 * @param otherZY Matrix row 2, column 1 (y-factor for z).
	 * @param otherZZ Matrix row 2, column 2 (z-factor for z).
	 * @param otherZO Matrix row 2, column 3 (offset   for z).
	 *
	 * @return Resulting matrix (premultiplied: result = other * this).
	 */
	public Matrix3D multiply(
	final double otherXX, final double otherXY, final double otherXZ, final double otherXO,
	final double otherYX, final double otherYY, final double otherYZ, final double otherYO,
	final double otherZX, final double otherZY, final double otherZZ, final double otherZO )
	{
		return multiply( xx, xy, xz, xo,
		                 yx, yy, yz, yo,
		                 zx, zy, zz, zo,
		                 otherXX, otherXY, otherXZ, otherXO,
		                 otherYX, otherYY, otherYZ, otherYO,
		                 otherZX, otherZY, otherZZ, otherZO );
	}

	/**
	 * Execute matrix multiplication between this and the inverse of another
	 * matrix.
	 *
	 * @param other Matrix whose inverse to multiply with.
	 *
	 * @return Resulting matrix (premultiplied: result = other<sup>-1</sup> * this).
	 */
	public Matrix3D multiplyInverse( final Matrix3D other )
	{
		return multiply( xx, xy, xz, xo,
		                 yx, yy, yz, yo,
		                 zx, zy, zz, zo,
		                 other.xx, other.yx, other.zx, -other.xo * other.xx - other.yo * other.yx - other.zo * other.zx,
		                 other.xy, other.yy, other.zy, -other.xo * other.xy - other.yo * other.yy - other.zo * other.zy,
		                 other.xz, other.yz, other.zz, -other.xo * other.xz - other.yo * other.yz - other.zo * other.zz );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param vector Vector specifying the translation.
	 *
	 * @return new Matrix3D with translation
	 */
	public Matrix3D plus( final Vector3D vector )
	{
		return plus( vector.x, vector.y, vector.z );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param x X-coordinate of vector specifying the translation.
	 * @param y Y-coordinate of vector specifying the translation.
	 * @param z Z-coordinate of vector specifying the translation.
	 *
	 * @return new Matrix3D with translation
	 */
	public Matrix3D plus( final double x, final double y, final double z )
	{
		return ( x == 0.0 && y == 0.0 && z == 0.0 )
		       ? this
		       : setTranslation( xo + x, yo + y, zo + z );
	}

	/**
	 * Rotate about the X-axis in counter-clockwise direction.
	 *
	 * <p>
	 * The returned matrix is the current matrix multiplied by the following
	 * transformation matrix:
	 * <pre>
	 *    [   1     0     0   ]
	 *    [   0    cos  -sin  ]
	 *    [   0    sin   cos  ]
	 * </pre>
	 *
	 * @param thetaRad Rotate theta radians about the X-axis
	 *
	 * @return Rotated matrix.
	 */
	public Matrix3D rotateX( final double thetaRad )
	{
		final Matrix3D result;

		//noinspection Duplicates
		if ( GeometryTools.almostEqual( thetaRad, 0.0 ) )
		{
			result = this;
		}
		else
		{
			final double cos = Math.cos( thetaRad );
			final double sin = Math.sin( thetaRad );

			result = new Matrix3D( xx, xy, xz, xo,
			                       cos * yx - sin * zx, cos * yy - sin * zy, cos * yz - sin * zz, cos * yo - sin * zo,
			                       sin * yx + cos * zx, sin * yy + cos * zy, sin * yz + cos * zz, sin * yo + cos * zo );
		}

		return result;
	}

	/**
	 * Rotate about the Y-axis in counter-clockwise direction.
	 *
	 * <p>
	 * The returned matrix is the current matrix multiplied by the following
	 * transformation matrix:
	 * <pre>
	 *    [  cos    0    sin  ]
	 *    [   0     1     0   ]
	 *    [ -sin    0    cos  ]
	 * </pre>
	 *
	 * @param thetaRad Rotate theta radians about the Y-axis
	 *
	 * @return Rotated matrix.
	 */
	public Matrix3D rotateY( final double thetaRad )
	{
		final Matrix3D result;

		if ( GeometryTools.almostEqual( thetaRad, 0.0 ) )
		{
			result = this;
		}
		else
		{
			final double cos = Math.cos( thetaRad );
			final double sin = Math.sin( thetaRad );

			result = new Matrix3D( cos * xx + zx * sin, cos * xy + zy * sin, cos * xz + zz * sin, cos * xo + zo * sin,
			                       yx, yy, yz, yo,
			                       -sin * xx + zx * cos, -sin * xy + zy * cos, -sin * xz + zz * cos, -sin * xo + zo * cos );
		}

		return result;
	}

	/**
	 * Rotate about the Z-axis in counter-clockwise direction.
	 *
	 * <p>
	 * The returned matrix is the current matrix multiplied by the following
	 * transformation matrix:
	 * <pre>
	 *    [  cos  -sin    0   ]
	 *    [  sin   cos    0   ]
	 *    [   0     0     1   ]
	 * </pre>
	 *
	 * @param thetaRad Rotate theta radians about the Z-axis
	 *
	 * @return Rotated matrix.
	 */
	public Matrix3D rotateZ( final double thetaRad )
	{
		final Matrix3D result;

		//noinspection Duplicates
		if ( GeometryTools.almostEqual( thetaRad, 0.0 ) )
		{
			result = this;
		}
		else
		{
			final double cos = Math.cos( thetaRad );
			final double sin = Math.sin( thetaRad );

			result = new Matrix3D( cos * xx - sin * yx, cos * xy - sin * yy, cos * xz - sin * yz, cos * xo - sin * yo,
			                       sin * xx + cos * yx, sin * xy + cos * yy, sin * xz + cos * yz, sin * xo + cos * yo,
			                       zx, zy, zz, zo );
		}

		return result;
	}

	/**
	 * Set all values in the matrix and return the resulting matrix.
	 *
	 * @param nxx X quotient for X component.
	 * @param nxy Y quotient for X component.
	 * @param nxz Z quotient for X component.
	 * @param nxo Translation of X component.
	 * @param nyx X quotient for Y component.
	 * @param nyy Y quotient for Y component.
	 * @param nyz Z quotient for Y component.
	 * @param nyo Translation of Y component.
	 * @param nzx X quotient for Z component.
	 * @param nzy Y quotient for Z component.
	 * @param nzz Z quotient for Z component.
	 * @param nzo Translation of Z component.
	 *
	 * @return Resulting vector.
	 */
	public Matrix3D set( final double nxx, final double nxy, final double nxz, final double nxo,
	                     final double nyx, final double nyy, final double nyz, final double nyo,
	                     final double nzx, final double nzy, final double nzz, final double nzo )
	{
		final double lxx = Double.isNaN( nxx ) ? xx : nxx;
		final double lxy = Double.isNaN( nxy ) ? xy : nxy;
		final double lxz = Double.isNaN( nxz ) ? xz : nxz;
		final double lxo = Double.isNaN( nxo ) ? xo : nxo;
		final double lyx = Double.isNaN( nyx ) ? yx : nyx;
		final double lyy = Double.isNaN( nyy ) ? yy : nyy;
		final double lyz = Double.isNaN( nyz ) ? yz : nyz;
		final double lyo = Double.isNaN( nyo ) ? yo : nyo;
		final double lzx = Double.isNaN( nzx ) ? zx : nzx;
		final double lzy = Double.isNaN( nzy ) ? zy : nzy;
		final double lzz = Double.isNaN( nzz ) ? zz : nzz;
		final double lzo = Double.isNaN( nzo ) ? zo : nzo;

		return ( lxx == xx && lxy == xy && lxz == xz && lxo == xo &&
		         lyx == yx && lyy == yy && lyz == yz && lyo == yo &&
		         lzx == zx && lzy == zy && lzz == zz && lzo == zo )
		       ? this
		       : new Matrix3D( lxx, lxy, lxz, lxo,
		                       lyx, lyy, lyz, lyo,
		                       lzx, lzy, lzz, lzo );
	}

	/**
	 * Uniformly scale this matrix by the given factor.
	 *
	 * @param scale Scaling factor.
	 *
	 * @return This matrix scaled  with the given factor.
	 */
	public Matrix3D scale( final double scale )
	{
		return scale( scale, scale, scale );
	}

	/**
	 * Scale this matrix by the given factors.
	 *
	 * @param x Scaling factor for the x-axis.
	 * @param y Scaling factor for the y-axis.
	 * @param z Scaling factor for the z-axis.
	 *
	 * @return This matrix scaled  with the given factors.
	 */
	public Matrix3D scale( final double x, final double y, final double z )
	{
		return new Matrix3D( x * xx, x * xy, x * xz, x * xo, y * yx, y * yy, y * yz, y * yo, z * zx, z * zy, z * zz, z * zo );
	}

	/**
	 * Get translation vector from this transform.
	 *
	 * @return Translation vector.
	 */
	public Vector3D getTranslation()
	{
		return new Vector3D( xo, yo, zo );
	}

	/**
	 * Get translation matrix from the specified translation vector.
	 *
	 * @param x X-translation.
	 * @param y Y-translation.
	 * @param z Z-translation.
	 *
	 * @return Translation matrix.
	 */
	public static Matrix3D getTranslation( final double x, final double y, final double z )
	{
		return ( ( x == 0.0 ) && ( y == 0.0 ) && ( z == 0.0 ) ) ? IDENTITY : new Matrix3D( 1.0, 0.0, 0.0, x, 0.0, 1.0, 0.0, y, 0.0, 0.0, 1.0, z );
	}

	/**
	 * Get translation matrix from the specified translation vector.
	 *
	 * @param translation Translation vector.
	 *
	 * @return Translation matrix.
	 */
	public static Matrix3D getTranslation( final Vector3D translation )
	{
		return getTranslation( translation.getX(), translation.getY(), translation.getZ() );
	}

	/**
	 * Set translation of a transform to the specified vector.
	 *
	 * @param vector Vector to use.
	 *
	 * @return Resulting matrix.
	 */
	public Matrix3D setTranslation( final Vector3D vector )
	{
		return setTranslation( vector.x, vector.y, vector.z );
	}

	/**
	 * Set translation of a transform to the specified vector.
	 *
	 * @param x X-value of vector.
	 * @param y Y-value of vector.
	 * @param z Z-value of vector.
	 *
	 * @return Resulting matrix.
	 */
	public Matrix3D setTranslation( final double x, final double y, final double z )
	{
		return new Matrix3D( xx, xy, xz, x, yx, yy, yz, y, zx, zy, zz, z );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return String representation of object.
	 */
	@Override
	public String toString()
	{
		return xx + "," + xy + ',' + xz + ',' + xo + ',' +
		       yx + ',' + yy + ',' + yz + ',' + yo + ',' +
		       zx + ',' + zy + ',' + zz + ',' + zo;
	}

	/**
	 * Create human-readable representation of this {@code Matrix3D} object.
	 * This is aspecially useful for debugging purposes.
	 *
	 * @return Human-readable representation of this {@code Matrix3D} object.
	 */
	public String toFriendlyString()
	{
		return toFriendlyString( this );
	}

	/**
	 * Create human-readable representation of Matrix3D object. This is
	 * aspecially useful for debugging purposes.
	 *
	 * @param m Matrix3D instance.
	 *
	 * @return Human-readable representation of Matrix3D object.
	 */
	public static String toFriendlyString( final Matrix3D m )
	{
		return toFriendlyString( m, "\n\t\t\t", "\n\t\t\t" );
	}

	/**
	 * Create human-readable representation of Matrix3D object. This is
	 * aspecially useful for debugging purposes.
	 *
	 * @param m      Matrix3D instance.
	 * @param prefix Prefix of returned string.
	 * @param infix  Infix inserted between matrix rows in returned string.
	 *
	 * @return Human-readable representation of Matrix3D object.
	 */
	public static String toFriendlyString( final Matrix3D m, final String prefix, final String infix )
	{
		final StringBuilder sb = new StringBuilder();
		//noinspection IOResourceOpenedButNotSafelyClosed
		final Formatter formatter = new Formatter( sb, null );
		sb.append( prefix );
		formatter.format( "[ %5.2f, %5.2f, %5.2f, %7.1f ]", m.xx, m.xy, m.xz, m.xo );
		sb.append( infix );
		formatter.format( "[ %5.2f, %5.2f, %5.2f, %7.1f ]", m.yx, m.yy, m.yz, m.yo );
		sb.append( infix );
		formatter.format( "[ %5.2f, %5.2f, %5.2f, %7.1f ]", m.zx, m.zy, m.zz, m.zo );
		return sb.toString();
	}

	/**
	 * Create short human-readable representation of Matrix3D object.
	 *
	 * @return Human-readable representation of Matrix3D object.
	 */
	public String toShortFriendlyString()
	{
		final NumberFormat nf = NumberFormat.getNumberInstance( Locale.US );
		nf.setMinimumFractionDigits( 1 );
		nf.setMaximumFractionDigits( 1 );
		nf.setGroupingUsed( false );
		return "[[" + nf.format( xx ) + ',' + nf.format( xy ) + ',' + nf.format( xz ) + ',' + nf.format( xo ) +
		       "],[" + nf.format( yx ) + ',' + nf.format( yy ) + ',' + nf.format( yz ) + ',' + nf.format( yo ) +
		       "],[" + nf.format( zx ) + ',' + nf.format( zy ) + ',' + nf.format( zz ) + ',' + nf.format( zo ) + "]]";
	}

	/**
	 * Transform a vector using this transform.
	 *
	 * @param vector Vector to transform.
	 *
	 * @return Resulting vector.
	 */
	public Vector3D transform( final Vector3D vector )
	{
		return new Vector3D( vector.x * xx + vector.y * xy + vector.z * xz + xo,
		                     vector.x * yx + vector.y * yy + vector.z * yz + yo,
		                     vector.x * zx + vector.y * zy + vector.z * zz + zo );
	}

	/**
	 * Transform a vector using this transform.
	 *
	 * @param x X-value of vector.
	 * @param y Y-value of vector.
	 * @param z Z-value of vector.
	 *
	 * @return Resulting vector.
	 */
	public Vector3D transform( final double x, final double y, final double z )
	{
		return new Vector3D( x * xx + y * xy + z * xz + xo,
		                     x * yx + y * yy + z * yz + yo,
		                     x * zx + y * zy + z * zz + zo );
	}

	/**
	 * This function transforms a set of points. Point coordinates are supplied
	 * using double arrays containing a triplet for each point.
	 *
	 * @param source     Source array.
	 * @param dest       Destination array (may be {@code null} or too small to
	 *                   create new).
	 * @param pointCount Number of vertices.
	 *
	 * @return Array to which the transformed coordinates were written (may be
	 * different from the {@code dest} argument).
	 *
	 * @see #transform(double, double, double)
	 * @see #transform(Vector3D)
	 */
	public double[] transform( final double[] source, final double[] dest, final int pointCount )
	{
		double[] result = dest;

		//noinspection ArrayEquality,ObjectEquality
		if ( ( source != dest ) || ( this != IDENTITY ) )
		{
			final int resultLength = pointCount * 3;
			if ( ( result == null ) || ( resultLength > result.length ) )
			{
				result = new double[ resultLength ];
			}

			final double lxx = xx;
			final double lxy = xy;
			final double lxz = xz;
			final double lyx = yx;
			final double lyy = yy;
			final double lyz = yz;
			final double lzx = zx;
			final double lzy = zy;
			final double lzz = zz;
			final double lxo = xo;
			final double lyo = yo;
			final double lzo = zo;

			/*
			 * Perform rotate, translate, or copy only if possible.
			 */
			if ( ( lxx == 1 ) && ( lyy == 1 ) && ( lzz == 1 ) &&
			     ( lxy == 0 ) && ( lyx == 0 ) && ( lzx == 0 ) &&
			     ( lxz == 0 ) && ( lyz == 0 ) && ( lzy == 0 ) )
			{
				if ( ( lxo == 0 ) && ( lyo == 0 ) && ( lzo == 0 ) )
				{
					//noinspection ArrayEquality
					if ( source != result )
					{
						System.arraycopy( source, 0, result, 0, resultLength );
					}
				}
				else
				{
					for ( int i = 0; i < resultLength; i += 3 )
					{
						result[ i ] = source[ i ] + lxo;
						result[ i + 1 ] = source[ i + 1 ] + lyo;
						result[ i + 2 ] = source[ i + 2 ] + lzo;
					}
				}
			}
			else
			{
				//noinspection Duplicates
				for ( int i = 0; i < resultLength; i += 3 )
				{
					final double x = source[ i ];
					final double y = source[ i + 1 ];
					final double z = source[ i + 2 ];

					result[ i ] = x * lxx + y * lxy + z * lxz + lxo;
					result[ i + 1 ] = x * lyx + y * lyy + z * lyz + lyo;
					result[ i + 2 ] = x * lzx + y * lzy + z * lzz + lzo;
				}
			}
		}

		return result;
	}

	/**
	 * Transform a vector to X-coordinate using this transform.
	 *
	 * @param vector Vector to transform.
	 *
	 * @return Resulting X coordinate.
	 */
	public double transformX( final Vector3D vector )
	{
		return transformX( vector.x, vector.y, vector.z );
	}

	/**
	 * Transform a vector to X-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting X coordinate.
	 */
	public double transformX( final double x, final double y, final double z )
	{
		return x * xx + y * xy + z * xz + xo;
	}

	/**
	 * Transform a vector to Y-coordinate using this transform.
	 *
	 * @param vector Vector to transform.
	 *
	 * @return Resulting Y coordinate.
	 */
	public double transformY( final Vector3D vector )
	{
		return transformY( vector.x, vector.y, vector.z );
	}

	/**
	 * Transform a vector to Y-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Y coordinate.
	 */
	public double transformY( final double x, final double y, final double z )
	{
		return x * yx + y * yy + z * yz + yo;
	}

	/**
	 * Transform a vector to Z-coordinate using this transform.
	 *
	 * @param vector Vector to transform.
	 *
	 * @return Resulting Z coordinate.
	 */
	public double transformZ( final Vector3D vector )
	{
		return transformZ( vector.x, vector.y, vector.z );
	}

	/**
	 * Transform a vector to Z-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Z coordinate.
	 */
	public double transformZ( final double x, final double y, final double z )
	{
		return x * zx + y * zy + z * zz + zo;
	}

	/**
	 * Transform a box using this transform.
	 *
	 * @param box Box to transform.
	 *
	 * @return Resulting box.
	 */
	public Bounds3D transform( final Bounds3D box )
	{
		return new Bounds3D( transform( box.v1 ), transform( box.v2 ) );
	}

	/**
	 * Transform a vector using the inverse of this transform.
	 *
	 * @param vector Vector to transform.
	 *
	 * @return Resulting vector.
	 */
	public Vector3D inverseTransform( final Vector3D vector )
	{
		final double tx = vector.x - xo;
		final double ty = vector.y - yo;
		final double tz = vector.z - zo;

		return new Vector3D( tx * xx + ty * yx + tz * zx,
		                     tx * xy + ty * yy + tz * zy,
		                     tx * xz + ty * yz + tz * zz );
	}

	/**
	 * Transform a vector using the inverse of this transform.
	 *
	 * @param x X-value of vector.
	 * @param y Y-value of vector.
	 * @param z Z-value of vector.
	 *
	 * @return Resulting vector.
	 */
	public Vector3D inverseTransform( final double x, final double y, final double z )
	{
		final double tz = z - zo;
		final double ty = y - yo;
		final double tx = x - xo;

		return new Vector3D( tx * xx + ty * yx + tz * zx,
		                     tx * xy + ty * yy + tz * zy,
		                     tx * xz + ty * yz + tz * zz );
	}

	/**
	 * Inverse transform a vector to X-coordinate using this transform.
	 *
	 * @param vector Vector to transform.
	 *
	 * @return Resulting X coordinate.
	 */
	public double inverseTransformX( final Vector3D vector )
	{
		return inverseTransformX( vector.x, vector.y, vector.z );
	}

	/**
	 * Inverse transform a vector to X-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting X coordinate.
	 */
	public double inverseTransformX( final double x, final double y, final double z )
	{
		return ( x - xo ) * xx + ( y - yo ) * yx + ( z - zo ) * zx;
	}

	/**
	 * Inverse transform a vector to Y-coordinate using this transform.
	 *
	 * @param vector Vector to transform.
	 *
	 * @return Resulting Y coordinate.
	 */
	public double inverseTransformY( final Vector3D vector )
	{
		return inverseTransformY( vector.x, vector.y, vector.z );
	}

	/**
	 * Inverse transform a vector to Y-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Y coordinate.
	 */
	public double inverseTransformY( final double x, final double y, final double z )
	{
		return ( x - xo ) * xy + ( y - yo ) * yy + ( z - zo ) * zy;
	}

	/**
	 * Inverse transform a vector to Z-coordinate using this transform.
	 *
	 * @param vector Vector to transform.
	 *
	 * @return Resulting Z coordinate.
	 */
	public double inverseTransformZ( final Vector3D vector )
	{
		return inverseTransformZ( vector.x, vector.y, vector.z );
	}

	/**
	 * Inverse transform a vector to Z-coordinate using this transform.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Z coordinate.
	 */
	public double inverseTransformZ( final double x, final double y, final double z )
	{
		return ( x - xo ) * xz + ( y - yo ) * yz + ( z - zo ) * zz;
	}

	/**
	 * Rotate a (directional) vector using this transform. This multiplies the
	 * vector with this matrix, excluding the translational components.
	 *
	 * @param vector Directional vector to rotate.
	 *
	 * @return Rotated vector.
	 */
	public Vector3D rotate( final Vector3D vector )
	{
		return rotate( vector.x, vector.y, vector.z );
	}

	/**
	 * Rotate a vector using this transform. This multiplies the vector with
	 * this matrix, excluding the translational components.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return Rotated vector.
	 */
	public Vector3D rotate( final double x, final double y, final double z )
	{
		return new Vector3D( x * xx + y * xy + z * xz,
		                     x * yx + y * yy + z * yz,
		                     x * zx + y * zy + z * zz );
	}

	/**
	 * This function performs just the rotational part of of the transform on a
	 * set of vectors. Vectors are supplied using float arrays with a triplet
	 * for each vector.
	 *
	 * @param source      Source array.
	 * @param dest        Destination array (may be {@code null} or too small to
	 *                    create new).
	 * @param vectorCount Number of vertices.
	 *
	 * @return Array to which the transformed coordinates were written (may be
	 * different from the {@code dest} argument).
	 *
	 * @see #transform(double, double, double)
	 * @see #transform(Vector3D)
	 */
	public double[] rotate( final double[] source, final double[] dest, final int vectorCount )
	{
		double[] result = dest;

		//noinspection ArrayEquality,ObjectEquality
		if ( ( source != dest ) || ( this != IDENTITY ) )
		{
			final int resultLength = vectorCount * 3;
			if ( ( result == null ) || ( resultLength > result.length ) )
			{
				result = new double[ resultLength ];
			}

			final double lxx = xx;
			final double lxy = xy;
			final double lxz = xz;
			final double lyx = yx;
			final double lyy = yy;
			final double lyz = yz;
			final double lzx = zx;
			final double lzy = zy;
			final double lzz = zz;

			if ( ( lxx == 1 ) && ( lyy == 1 ) && ( lzz == 1 ) &&
			     ( lxy == 0 ) && ( lyx == 0 ) && ( lzx == 0 ) &&
			     ( lxz == 0 ) && ( lyz == 0 ) && ( lzy == 0 ) )
			{
				//noinspection ArrayEquality
				if ( source != result )
				{
					System.arraycopy( source, 0, result, 0, resultLength );
				}
			}
			else
			{
				//noinspection Duplicates
				for ( int resultIndex = 0; resultIndex < resultLength; resultIndex += 3 )
				{
					final double x = source[ resultIndex ];
					final double y = source[ resultIndex + 1 ];
					final double z = source[ resultIndex + 2 ];

					result[ resultIndex ] = x * lxx + y * lxy + z * lxz;
					result[ resultIndex + 1 ] = x * lyx + y * lyy + z * lyz;
					result[ resultIndex + 2 ] = x * lzx + y * lzy + z * lzz;
				}
			}
		}

		return result;
	}

	/**
	 * Rotate a vector to X-coordinate using this rotate.
	 *
	 * @param vector Directional vector to rotate.
	 *
	 * @return Resulting X coordinate.
	 */
	public double rotateX( final Vector3D vector )
	{
		return rotateX( vector.x, vector.y, vector.z );
	}

	/**
	 * Rotate a vector to X-coordinate using this rotate.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting X coordinate.
	 */
	public double rotateX( final double x, final double y, final double z )
	{
		return x * xx + y * xy + z * xz;
	}

	/**
	 * Rotate a vector to Y-coordinate using this rotate.
	 *
	 * @param vector Directional vector to rotate.
	 *
	 * @return Resulting Y coordinate.
	 */
	public double rotateY( final Vector3D vector )
	{
		return rotateY( vector.x, vector.y, vector.z );
	}

	/**
	 * Rotate a vector to Y-coordinate using this rotate.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Y coordinate.
	 */
	public double rotateY( final double x, final double y, final double z )
	{
		return x * yx + y * yy + z * yz;
	}

	/**
	 * Rotate a vector to Z-coordinate using this rotate.
	 *
	 * @param vector Directional vector to rotate.
	 *
	 * @return Resulting Z coordinate.
	 */
	public double rotateZ( final Vector3D vector )
	{
		return rotateZ( vector.x, vector.y, vector.z );
	}

	/**
	 * Rotate a vector to Z-coordinate using this rotate.
	 *
	 * @param x X-coordinate of vector.
	 * @param y Y-coordinate of vector.
	 * @param z Z-coordinate of vector.
	 *
	 * @return Resulting Z coordinate.
	 */
	public double rotateZ( final double x, final double y, final double z )
	{
		return x * zx + y * zy + z * zz;
	}

	/**
	 * Rotate a (directional) vector using the inverse of this transform. This
	 * multiplies the vector with the inverse of this matrix, excluding the
	 * translational components.
	 *
	 * @param vector Directional vector to rotate.
	 *
	 * @return Rotated vector.
	 */
	public Vector3D inverseRotate( final Vector3D vector )
	{
		final double x = vector.x;
		final double y = vector.y;
		final double z = vector.z;

		return new Vector3D( x * xx + y * yx + z * zx,
		                     x * xy + y * yy + z * zy,
		                     x * xz + y * yz + z * zz );
	}

	/**
	 * Rotate a vector using the inverse of this transform. This multiplies the
	 * vector with the inverse of this matrix, excluding the translational
	 * components.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return Rotated vector.
	 */
	public Vector3D inverseRotate( final double x, final double y, final double z )
	{
		return new Vector3D( x * xx + y * yx + z * zx,
		                     x * xy + y * yy + z * zy,
		                     x * xz + y * yz + z * zz );
	}

	/**
	 * Calculates the X-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param vector Directional vector to rotate.
	 *
	 * @return Resulting X coordinate.
	 */
	public double inverseRotateX( final Vector3D vector )
	{
		return vector.x * xx + vector.y * yx + vector.z * zx;
	}

	/**
	 * Calculates the X-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return Resulting X coordinate.
	 */
	public double inverseRotateX( final double x, final double y, final double z )
	{
		return x * xx + y * yx + z * zx;
	}

	/**
	 * Calculates the Y-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param vector Directional vector to rotate.
	 *
	 * @return Resulting Y-coordinate.
	 */
	public double inverseRotateY( final Vector3D vector )
	{
		return vector.x * xy + vector.y * yy + vector.z * zy;
	}

	/**
	 * Calculates the Y-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return Resulting Y-coordinate.
	 */
	public double inverseRotateY( final double x, final double y, final double z )
	{
		return x * xy + y * yy + z * zy;
	}

	/**
	 * Calculates the Z-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param vector Directional vector to rotate.
	 *
	 * @return Resulting Z-coordinate.
	 */
	public double inverseRotateZ( final Vector3D vector )
	{
		return vector.x * xz + vector.y * yz + vector.z * zz;
	}

	/**
	 * Calculates the Z-coordinate of the given vector when rotated with the
	 * inverse of this matrix.
	 *
	 * @param x X component of directional vector to rotate.
	 * @param y Y component of directional vector to rotate.
	 * @param z Z component of directional vector to rotate.
	 *
	 * @return Resulting Z-coordinate.
	 */
	public double inverseRotateZ( final double x, final double y, final double z )
	{
		return x * xz + y * yz + z * zz;
	}

	/**
	 * Returns whether the coordinate system represented by the matrix follows
	 * the right-hand rule.
	 *
	 * @return {@code true} if the coordinate system is righthanded.
	 */
	public boolean isRighthanded()
	{
		// cross product of x-axis and y-axis -> derived z-axis (righthanded)
		final double crossX = yx * zy - zx * yy;
		final double crossY = zx * xy - xx * zy;
		final double crossZ = xx * yy - yx * xy;

		// z-axis and derived (righthanded) z-axis should have same direction
		return ( Vector3D.dot( crossX, crossY, crossZ, xz, yz, zz ) > 0.0 );
	}

	/**
	 * Returns the determinant of the matrix.
	 *
	 * @return Determinant of the matrix.
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/Determinant">Determinant
	 * (Wikipedia)</a>
	 */
	public double determinant()
	{
		return xx * yy * zz -
		       xx * yz * zy -
		       xy * yx * zz +
		       xy * yz * zx +
		       xz * yx * zy -
		       xz * yy * zx;
	}
}
