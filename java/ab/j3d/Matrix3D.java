/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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

import java.text.DecimalFormat;

import com.numdata.oss.ArrayTools;
import com.numdata.oss.TextTools;

/**
 * This class is used to represent a 3D transformation matrix (although
 * it may also be used for 2D transformations).
 * <p />
 * The matrix is organized as follows:
 * <pre>
 * | xx xy xz xo |
 * | yx yy yz yo |
 * | zx zy zz zo |
 * | 0  0  0  1  |
 * </pre>
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Matrix3D
{
	/** X quotient for X component. */ public final double xx;
	/** Y quotient for X component. */ public final double xy;
	/** Z quotient for X component. */ public final double xz;
	/** Translation of X component. */ public final double xo;
	/** X quotient for Y component. */ public final double yx;
	/** Y quotient for Y component. */ public final double yy;
	/** Z quotient for Y component. */ public final double yz;
	/** Translation of Y component. */ public final double yo;
	/** X quotient for Z component. */ public final double zx;
	/** Y quotient for Z component. */ public final double zy;
	/** Z quotient for Z component. */ public final double zz;
	/** Translation of Z component. */ public final double zo;

	/**
	 * Multiplication factor to transform decimal degrees to radians.
	 */
	public static final double DEG_TO_RAD = Math.PI / 180.0;

	/**
	 * Initial value of a matrix (=identity matrix).
	 */
	public static final Matrix3D INIT = new Matrix3D(
		1.0 , 0.0 , 0.0 , 0.0 ,
		0.0 , 1.0 , 0.0 , 0.0 ,
		0.0 , 0.0 , 1.0 , 0.0 );

	/**
	 * Construct a new matrix.
	 *
	 * @param   nxx     X quotient for X component.
	 * @param   nxy     Y quotient for X component.
	 * @param   nxz     Z quotient for X component.
	 * @param   nxo     Translation of X component.
	 * @param   nyx     X quotient for Y component.
	 * @param   nyy     Y quotient for Y component.
	 * @param   nyz     Z quotient for Y component.
	 * @param   nyo     Translation of Y component.
	 * @param   nzx     X quotient for Z component.
	 * @param   nzy     Y quotient for Z component.
	 * @param   nzz     Z quotient for Z component.
	 * @param   nzo     Translation of Z component.
	 */
	private Matrix3D( final double nxx, final double nxy , final double nxz , final double nxo ,
	                  final double nyx, final double nyy , final double nyz , final double nyo ,
	                  final double nzx, final double nzy , final double nzz , final double nzo )
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
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is the values are within a +/- 0.001 tolerance
	 *          of eachother; <code>false</code> otherwise.
	 */
	public static boolean almostEqual( final double value1 , final double value2 )
	{
		final double delta = value1 - value2;
		return ( delta >= -0.001 ) && ( delta <= 0.001 );
	}

	/**
	 * Compare this matrix to another matrix.
	 *
	 * @param   other   Matrix to compare with.
	 *
	 * @return  <code>true</code> if the objects are almost equal;
	 *          <code>false</code> if not.
	 *
	 * @see     #almostEqual
	 */
	public boolean almostEquals( final Matrix3D other )
	{
		return ( other != null )
		    && ( ( other == this )
		      || ( almostEqual( xx , other.xx ) && almostEqual( xy , other.xy ) && almostEqual( xz , other.xz ) && almostEqual( xo , other.xo )
		        && almostEqual( yx , other.yx ) && almostEqual( yy , other.yy ) && almostEqual( yz , other.yz ) && almostEqual( yo , other.yo )
		        && almostEqual( zx , other.zx ) && almostEqual( zy , other.zy ) && almostEqual( zz , other.zz ) && almostEqual( zo , other.zo ) ) );
	}

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

	public int hashCode()
	{
		long l;
		return (int)( ( l = Double.doubleToLongBits( xx ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( xy ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( xz ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( xo ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( yx ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( yy ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( yz ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( yo ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( zx ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( zy ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( zz ) ) ^ ( l >>> 32 )
		            ^ ( l = Double.doubleToLongBits( zo ) ) ^ ( l >>> 32 ) );
	}

	/**
	 * Convert string representation of matrix back to <code>Matrix3D</code>
	 * instance (see <code>toString()</code>).
	 *
	 * @param   value   String representation of object.
	 *
	 * @return  Matrix3D instance.
	 *
	 * @throws  NullPointerException if <code>value</code> is <code>null</code>.
	 * @throws  IllegalArgumentException if the string format is unrecognized.
	 * @throws  NumberFormatException if any of the numeric components are badly formatted.
	 *
	 * @see     #toString()
	 */
	public static Matrix3D fromString( final String value )
	{
		if ( value == null )
			throw new NullPointerException( "value" );

		final String[] tokens = TextTools.tokenize( value , ',' );
		if ( tokens.length != 12 )
			throw new IllegalArgumentException( "tokens" );

		final double xx = Double.parseDouble( tokens[  0 ] );
		final double xy = Double.parseDouble( tokens[  1 ] );
		final double xz = Double.parseDouble( tokens[  2 ] );
		final double xo = Double.parseDouble( tokens[  3 ] );

		final double yx = Double.parseDouble( tokens[  4 ] );
		final double yy = Double.parseDouble( tokens[  5 ] );
		final double yz = Double.parseDouble( tokens[  6 ] );
		final double yo = Double.parseDouble( tokens[  7 ] );

		final double zx = Double.parseDouble( tokens[  8 ] );
		final double zy = Double.parseDouble( tokens[  9 ] );
		final double zz = Double.parseDouble( tokens[ 10 ] );
		final double zo = Double.parseDouble( tokens[ 11 ] );

		return INIT.set( xx , xy , xz , xo ,
		                 yx , yy , yz , yo ,
		                 zx , zy , zz , zo );
	}

	/**
	 * Get transformation matrix based on 6 parameters specifying rotation angles
	 * and a translation vector. Starting with the identity matrix, rotation is
	 * performed (Z,X,Y order), than the translation is set.
	 *
	 * @param   rx      Rotation angle around X axis (degrees).
	 * @param   ry      Rotation angle around Y axis (degrees).
	 * @param   rz      Rotation angle around Z axis (degrees).
	 * @param   tx      X component of translation vector.
	 * @param   ty      Y component of translation vector.
	 * @param   tz      Z component of translation vector.
	 *
	 * @return  Transformation matrix.
	 */
	public static Matrix3D getTransform( final double rx , final double ry , final double rz , final double tx , final double ty , final double tz )
	{
		final double radX = rx * DEG_TO_RAD;
		final double radY = ry * DEG_TO_RAD;
		final double radZ = rz * DEG_TO_RAD;

		final double ctX = Math.cos( radX );
		final double stX = Math.sin( radX );
		final double ctY = Math.cos( radY );
		final double stY = Math.sin( radY );
		final double ctZ = Math.cos( radZ );
		final double stZ = Math.sin( radZ );

		return new Matrix3D(
			/* xx */  ctZ * ctY - stZ * stX * stY ,
			/* xy */ -stZ * ctY - ctZ * stX * stY ,
			/* xz */  ctX * stY ,
			/* xo */  tx ,

			/* yx */  stZ * ctX ,
			/* yy */  ctZ * ctX ,
			/* yz */        stX ,
			/* y0 */  ty ,

			/* zx */ -stZ * stX * ctY - ctZ * stY ,
			/* zy */ -ctZ * stX * ctY + stZ * stY ,
			/* zz */        ctX * ctY ,
			/* zo */  tz );
	}

	/**
	 * Get transformation matrix based on the specified 'from' and 'to' points.
	 * The result can be used to convert world coordinates to view coordinates.
	 * <p />
	 * An up-vector must be specified to determine the correct view orientation.
	 * Note that both a primary and a secondary up-vector is needed; the primary
	 * up-vector is used when possible, the secondary up-vector is used when the
	 * from-to vector is parallel to the primary up-vector.
	 *
	 * @param   from        Point to look from.
	 * @param   to          Point to look at.
	 * @param   upPrimary   Primary up-vector (must be normalized).
	 * @param   upSecondary Secondary up-vector (must be normalized).
	 *
	 * @return  Transformation matrix.
	 *
	 * @throws  NullPointerException if any of the arguments is <code>null</code>.
	 * @throws  IllegalArgumentException if the from and two points are too close.
	 */
	public static Matrix3D getFromToTransform( final Vector3D from , final Vector3D to , final Vector3D upPrimary , final Vector3D upSecondary )
	{
		if ( from == null )
			throw new NullPointerException( "from" );

		if ( to == null )
			throw new NullPointerException( "to" );

		if ( upPrimary == null )
			throw new NullPointerException( "upPrimary" );

		if ( upSecondary == null )
			throw new NullPointerException( "upSecondary" );

		if ( from.almostEquals( to ) )
			throw new IllegalArgumentException( "from ~= to" );

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

		/*
		 * Create matrix.
		 */
		return INIT.set(
			xx , xy , xz , ( -from.x * xx -from.y * xy -from.z * xz ) ,
			yx , yy , yz , ( -from.x * yx -from.y * yy -from.z * yz ) ,
			zx , zy , zz , ( -from.x * zx -from.y * zy -from.z * zz ) );
	}

	/**
	 * Get transformation matrix for a rotation about an arbitrary axis. The
	 * rotation is axis is specified by a pivot point and rotation axis
	 * direction. The rotation angle is specified in radians.
	 * <p />
	 * Also read <a href='http://www.cprogramming.com/tutorial/3d/rotation.html'>Rotation About an Arbitrary Axis</a>
	 * (written by: Confuted, with a cameo by Silvercord (Charles Thibualt)).
	 *
	 * @param   pivot       Pivot point about which the rotation is performed.
	 * @param   direction   Rotation axis direction (must be a unit vector).
	 * @param   thetaRad    Rotate theta radians.
	 *
	 * @return  Transformation matrix with requested rotation.
	 */
	public static Matrix3D getRotationTransform( final Vector3D pivot , final Vector3D direction , final double thetaRad )
	{
		return getRotationTransform( pivot.x , pivot.y , pivot.z , direction.x , direction.y , direction.z , thetaRad );
	}

	/**
	 * Get transformation matrix for a rotation about an arbitrary axis. The
	 * rotation is axis is specified by a pivot point and rotation axis
	 * direction. The rotation angle is specified in radians.
	 * <p />
	 * Read <a href='http://www.mlahanas.de/Math/orientation.htm'>We consider the problem of the coordinate transformation about a rotation axis.</a>
	 * (written by Michael Lahanas) for a simple explanation of the problem and
	 * solution.
	 *
	 * @param   pivotX      Pivot point about which the rotation is performed.
	 * @param   pivotY      Pivot point about which the rotation is performed.
	 * @param   pivotZ      Pivot point about which the rotation is performed.
	 * @param   directionX  Rotation axis direction (must be a unit vector).
	 * @param   directionY  Rotation axis direction (must be a unit vector).
	 * @param   directionZ  Rotation axis direction (must be a unit vector).
	 * @param   thetaRad    Rotate theta radians.
	 *
	 * @return  Transformation matrix with requested rotation.
	 */
	public static Matrix3D getRotationTransform( final double pivotX , final double pivotY , final double pivotZ , final double directionX , final double directionY , final double directionZ , final double thetaRad )
	{
		final double cos = Math.cos( thetaRad );
		final double sin = Math.sin( thetaRad );

		final double t  = ( 1.0 - cos );
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

		return INIT.set( xx , xy , xz , pivotX - xx * pivotX - xy * pivotY - xz * pivotZ ,
		                 yx , yy , yz , pivotY - yx * pivotX - yy * pivotY - yz * pivotZ ,
		                 zx , zy , zz , pivotZ - zx * pivotX - zy * pivotY - zz * pivotZ );
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
	 * @return  Inverse matrix.
	 */
	public Matrix3D inverse()
	{
		return set( xx , yx , zx , - xo * xx - yo * yx - zo * zx ,
		            xy , yy , zy , - xo * xy - yo * yy - zo * zy ,
		            xz , yz , zz , - xo * xz - yo * yz - zo * zz );
	}

	/**
	 * Test is this matrix causes mirroring in the XY plane.
	 *
	 * @return  <code>true</code> if this matrix causes mirroring in the XY plane.
	 *          <code>false</code> if no mirroring occurs (normal transformation).
	 */
	public boolean isMirrorXY()
	{
		return ( xx * ( yy - yx ) + yx * ( xx - xy ) ) < 0;
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param   vector  Vector specifying the translation.
	 *
	 * @return  new Matrix3D with translation
	 */
	public Matrix3D minus( final Vector3D vector )
	{
		return minus( vector.x , vector.y , vector.z );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param   x       X-coordinate of vector specifying the translation.
	 * @param   y       Y-coordinate of vector specifying the translation.
	 * @param   z       Z-coordinate of vector specifying the translation.
	 *
	 * @return  new Matrix3D with translation
	 */
	public Matrix3D minus( final double x , final double y , final double z )
	{
		return ( ( x == 0.0 ) && ( y == 0.0 ) && ( z == 0.0 ) )
		     ? this
		     : setTranslation( xo - x , yo - y , zo - z );
	}

	/**
	 * Transform a box using this transform.
	 *
	 * @param   box     Box to transform.
	 *
	 * @return  Resulting box.
	 */
	public Bounds3D multiply( final Bounds3D box )
	{
		return box.set( multiply( box.v1 ) , multiply( box.v2 ) );
	}

	/**
	 * Execute matrix multiplication between two matrices and set the
	 * result in this transform.
	 *
	 * @param   other   Transform to multiply with.
	 *
	 * @return  Resulting matrix.
	 */
	public Matrix3D multiply( final Matrix3D other )
	{
		return set( /* xx */ xx * other.xx + yx * other.xy + zx * other.xz ,
		            /* xy */ xy * other.xx + yy * other.xy + zy * other.xz ,
		            /* xz */ xz * other.xx + yz * other.xy + zz * other.xz ,
		            /* xo */ xo * other.xx + yo * other.xy + zo * other.xz + other.xo ,
		            /* yx */ xx * other.yx + yx * other.yy + zx * other.yz ,
		            /* yy */ xy * other.yx + yy * other.yy + zy * other.yz ,
		            /* yz */ xz * other.yx + yz * other.yy + zz * other.yz ,
		            /* yo */ xo * other.yx + yo * other.yy + zo * other.yz + other.yo ,
		            /* zx */ xx * other.zx + yx * other.zy + zx * other.zz ,
		            /* zy */ xy * other.zx + yy * other.zy + zy * other.zz ,
		            /* zz */ xz * other.zx + yz * other.zy + zz * other.zz ,
		            /* zo */ xo * other.zx + yo * other.zy + zo * other.zz + other.zo );
	}

	/**
	 * Transform a vector using this transform.
	 *
	 * @param   vector  Vector to transform.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D multiply( final Vector3D vector )
	{
		return vector.set( vector.x * xx + vector.y * xy + vector.z * xz + xo ,
		                   vector.x * yx + vector.y * yy + vector.z * yz + yo ,
		                   vector.x * zx + vector.y * zy + vector.z * zz + zo );
	}

	/**
	 * Transform a vector using this transform.
	 *
	 * @param   x       X-value of vector.
	 * @param   y       Y-value of vector.
	 * @param   z       Z-value of vector.
	 *
	 * @return  Resulting vector.
	 */
	public Vector3D multiply( final double x , final double y , final double z )
	{
		return Vector3D.INIT.set( x * xx + y * xy + z * xz + xo ,
		                          x * yx + y * yy + z * yz + yo ,
		                          x * zx + y * zy + z * zz + zo );
	}

	/**
	 * Multiply this matrix with another matrix and return the result
	 * of this multiplication.
	 *
	 * @param   otherXX     Matrix row 0, column 0 (x-factor for x).
	 * @param   otherXY     Matrix row 0, column 1 (y-factor for x).
	 * @param   otherXZ     Matrix row 0, column 2 (z-factor for x).
	 * @param   otherXO     Matrix row 0, column 3 (offset   for x).
	 * @param   otherYX     Matrix row 1, column 0 (x-factor for y).
	 * @param   otherYY     Matrix row 1, column 1 (y-factor for y).
	 * @param   otherYZ     Matrix row 1, column 2 (z-factor for y).
	 * @param   otherYO     Matrix row 1, column 3 (offset   for y).
	 * @param   otherZX     Matrix row 2, column 0 (x-factor for z).
	 * @param   otherZY     Matrix row 2, column 1 (y-factor for z).
	 * @param   otherZZ     Matrix row 2, column 2 (z-factor for z).
	 * @param   otherZO     Matrix row 2, column 3 (offset   for z).
	 *
	 * @return  Resulting matrix.
	 */
	public Matrix3D multiply(
		final double otherXX , final double otherXY , final double otherXZ , final double otherXO ,
		final double otherYX , final double otherYY , final double otherYZ , final double otherYO ,
		final double otherZX , final double otherZY , final double otherZZ , final double otherZO )
	{
		return set(
		 /* xx */ xx * otherXX + yx * otherXY + zx * otherXZ ,
		 /* xy */ xy * otherXX + yy * otherXY + zy * otherXZ ,
		 /* xz */ xz * otherXX + yz * otherXY + zz * otherXZ ,
		 /* xo */ xo * otherXX + yo * otherXY + zo * otherXZ + otherXO ,

		 /* yx */ xx * otherYX + yx * otherYY + zx * otherYZ ,
		 /* yy */ xy * otherYX + yy * otherYY + zy * otherYZ ,
		 /* yz */ xz * otherYX + yz * otherYY + zz * otherYZ ,
		 /* yo */ xo * otherYX + yo * otherYY + zo * otherYZ + otherYO ,

		 /* zx */ xx * otherZX + yx * otherZY + zx * otherZZ ,
		 /* zy */ xy * otherZX + yy * otherZY + zy * otherZZ ,
		 /* zz */ xz * otherZX + yz * otherZY + zz * otherZZ ,
		 /* zo */ xo * otherZX + yo * otherZY + zo * otherZZ + otherZO );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param   vector  Vector specifying the translation.
	 *
	 * @return  new Matrix3D with translation
	 */
	public Matrix3D plus( final Vector3D vector )
	{
		return plus( vector.x , vector.y , vector.z );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param   x       X-coordintate of vector specifying the translation.
	 * @param   y       Y-coordintate of vector specifying the translation.
	 * @param   z       Z-coordintate of vector specifying the translation.
	 *
	 * @return  new Matrix3D with translation
	 */
	public Matrix3D plus( final double x , final double y , final double z )
	{
		return ( x == 0.0 && y == 0.0 && z == 0.0 )
		     ? this
		     : setTranslation( xo + x , yo + y , zo + z );
	}

	/**
	 * Rotate along the X-axis.
	 *
	 * @param   thetaRad    Rotate theta radians about the X-axis
	 *
	 * @return  Rotated matrix.
	 */
	public Matrix3D rotateX( final double thetaRad )
	{
		final Matrix3D result;

		if ( almostEqual( thetaRad , 0.0 ) )
		{
			result = this;
		}
		else
		{
			final double cos = Math.cos( thetaRad );
			final double sin = Math.sin( thetaRad );

			result = set(       xx            ,       xy            ,       xz            ,       xo            ,
			              cos * yx + sin * zx , cos * yy + sin * zy , cos * yz + sin * zz , cos * yo + sin * zo ,
			              cos * zx - sin * yx , cos * zy - sin * yy , cos * zz - sin * yz , cos * zo - sin * yo );
		}

		return result;
	}

	/**
	 * Rotate along the Y-axis.
	 *
	 * @param   thetaRad    Rotate theta radians about the Y-axis
	 *
	 * @return  Rotated matrix.
	 */
	public Matrix3D rotateY( final double thetaRad )
	{
		final Matrix3D result;

		if ( almostEqual( thetaRad , 0.0 ) )
		{
			result = this;
		}
		else
		{
			final double cos = Math.cos( thetaRad );
			final double sin = Math.sin( thetaRad );

			result = set( xx * cos + zx * sin , xy * cos + zy * sin , xz * cos + zz * sin , xo * cos + zo * sin ,
			              yx                  , yy                  , yz                  , yo                  ,
			              zx * cos - xx * sin , zy * cos - xy * sin , zz * cos - xz * sin , zo * cos - xo * sin );
		}

		return result;
	}

	/**
	 * Rotate along the Z-axis.
	 *
	 * @param   thetaRad    Rotate theta radians about the Z-axis
	 *
	 * @return  Rotated matrix.
	 */
	public Matrix3D rotateZ( final double thetaRad )
	{
		final Matrix3D result;

		if ( almostEqual( thetaRad , 0.0 ) )
		{
			result = this;
		}
		else
		{
			final double cos = Math.cos( thetaRad );
			final double sin = Math.sin( thetaRad );

			result = set( xx * cos - yx * sin , xy * cos - yy * sin , xz * cos - yz * sin , xo * cos - yo * sin ,
			              yx * cos + xx * sin , yy * cos + xy * sin , yz * cos + xz * sin , yo * cos + xo * sin ,
			              zx                  , zy                  , zz                  , zo );
		}

		return result;
	}

	/**
	 * Set all values in the matrix and return the resulting matrix.
	 *
	 * @param   nxx     X quotient for X component.
	 * @param   nxy     Y quotient for X component.
	 * @param   nxz     Z quotient for X component.
	 * @param   nxo     Translation of X component.
	 * @param   nyx     X quotient for Y component.
	 * @param   nyy     Y quotient for Y component.
	 * @param   nyz     Z quotient for Y component.
	 * @param   nyo     Translation of Y component.
	 * @param   nzx     X quotient for Z component.
	 * @param   nzy     Y quotient for Z component.
	 * @param   nzz     Z quotient for Z component.
	 * @param   nzo     Translation of Z component.
	 *
	 * @return  Resulting vector.
	 */
	public Matrix3D set( final double nxx , final double nxy , final double nxz , final double nxo ,
	                     final double nyx , final double nyy , final double nyz , final double nyo ,
	                     final double nzx , final double nzy , final double nzz , final double nzo )
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
		       : new Matrix3D( lxx , lxy , lxz , lxo ,
		                       lyx , lyy , lyz , lyo ,
		                       lzx , lzy , lzz , lzo );
	}

	/**
	 * Set translation of a transform to the specified vector.
	 *
	 * @param   vector  Vector to use.
	 *
	 * @return  Resulting matrix.
	 */
	public Matrix3D setTranslation( final Vector3D vector )
	{
		return setTranslation( vector.x , vector.y , vector.z );
	}

	/**
	 * Set translation of a transform to the specified vector.
	 *
	 * @param   x       X-value of vector.
	 * @param   y       Y-value of vector.
	 * @param   z       Z-value of vector.
	 *
	 * @return  Resulting matrix.
	 */
	public Matrix3D setTranslation( final double x , final double y , final double z )
	{
		final double lx = Double.isNaN( x ) ? xo : x;
		final double ly = Double.isNaN( y ) ? yo : y;
		final double lz = Double.isNaN( z ) ? zo : z;

		return ( ( lx == xo ) && ( ly == yo ) && ( lz == zo ) )
		     ? this
		     : set( xx , xy , xz , lx ,
		            yx , yy , yz , ly ,
		            zx , zy , zz , lz );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return  String representation of object.
	 */
	public String toString()
	{
		return xx + "," + xy + ',' + xz + ',' + xo + ',' +
		       yx + ',' + yy + ',' + yz + ',' + yo + ',' +
		       zx + ',' + zy + ',' + zz + ',' + zo;
	}

	/**
	 * Create human-readable representation of this <code>Matrix3D</code> object.
	 * This is aspecially useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this <code>Matrix3D</code> object.
	 */
	public String toFriendlyString()
	{
		return toFriendlyString( this );
	}

	/**
	 * Create human-readable representation of Matrix3D object. This is
	 * aspecially useful for debugging purposes.
	 *
	 * @param   m   Matrix3D instance.
	 *
	 * @return  Human-readable representation of Matrix3D object.
	 */
	public static String toFriendlyString( final Matrix3D m )
	{
		final StringBuffer sb = new StringBuffer();
		final DecimalFormat df = new DecimalFormat( "0.0" );

		sb.append( "\n\t\t\t[ " );
		TextTools.appendFixed( sb , df.format( m.xx ) , 4 , true , ' ' );
		sb.append( " , " );
		TextTools.appendFixed( sb , df.format( m.xy ) , 4 , true , ' ' );
		sb.append( " , " );
		TextTools.appendFixed( sb , df.format( m.xz ) , 4 , true , ' ' );
		sb.append( " , " );
		TextTools.appendFixed( sb , df.format( m.xo ) , 6 , true , ' ' );
		sb.append( " ]" );
		sb.append( "\n\t\t\t[ " );
		TextTools.appendFixed( sb , df.format( m.yx ) , 4 , true , ' ' );
		sb.append( " , " );
		TextTools.appendFixed( sb , df.format( m.yy ) , 4 , true , ' ' );
		sb.append( " , " );
		TextTools.appendFixed( sb , df.format( m.yz ) , 4 , true , ' ' );
		sb.append( " , " );
		TextTools.appendFixed( sb , df.format( m.yo ) , 6 , true , ' ' );
		sb.append( " ]" );
		sb.append( "\n\t\t\t[ " );
		TextTools.appendFixed( sb , df.format( m.zx ) , 4 , true , ' ' );
		sb.append( " , " );
		TextTools.appendFixed( sb , df.format( m.zy ) , 4 , true , ' ' );
		sb.append( " , " );
		TextTools.appendFixed( sb , df.format( m.zz ) , 4 , true , ' ' );
		sb.append( " , " );
		TextTools.appendFixed( sb , df.format( m.zo ) , 6 , true , ' ' );
		sb.append( " ]" );

		return sb.toString();
	}

	/**
	 * This function performs just the rotational part of of the transform on a
	 * set of vectors. Vectors are supplied using float arrays with a triplet for
	 * each vector.
	 *
	 * @param   source          Source array.
	 * @param   dest            Destination array (may be <code>null</code> or too small to create new).
	 * @param   vectorCount     Number of vertices.
	 *
	 * @return  Array to which the transformed coordinates were written
	 *          (may be different from the <code>dest</code> argument).
	 *
	 * @see     #multiply(double, double, double)
	 * @see     #multiply(Vector3D)
	 * @see     ArrayTools#ensureLength
	 */
	public double[] rotate( final double[] source , final double[] dest , final int vectorCount )
	{
		final int      resultLength = vectorCount * 3;
		final double[] result       = (double[])ArrayTools.ensureLength( dest , double.class , -1 , resultLength );

		final double lxx = xx;
		final double lxy = xy;
		final double lxz = xz;
		final double lyx = yx;
		final double lyy = yy;
		final double lyz = yz;
		final double lzx = zx;
		final double lzy = zy;
		final double lzz = zz;

		if ( ( lxx == 1.0 ) && ( lxy == 0.0 ) && ( lxz == 0.0 ) &&
		     ( lyx == 0.0 ) && ( lyy == 1.0 ) && ( lyz == 0.0 ) &&
		     ( lzx == 0.0 ) && ( lzy == 0.0 ) && ( lzz == 1.0 ) )
		{
			System.arraycopy( source , 0 , result , 0 , resultLength );
		}
		else
		{
			double x;
			double y;
			double z;

			for ( int i = 0 ; i < resultLength ; i += 3 )
			{
				x = source[ i     ];
				y = source[ i + 1 ];
				z = source[ i + 2 ];

				result[ i     ] = x * lxx + y * lxy + z * lxz;
				result[ i + 1 ] = x * lyx + y * lyy + z * lyz;
				result[ i + 2 ] = x * lzx + y * lzy + z * lzz;
			}
		}

		return result;
	}

	/**
	 * Rotate a vector to X-coordinate using this rotate.
	 *
	 * @param   x       X-coordinate of vector.
	 * @param   y       Y-coordinate of vector.
	 * @param   z       Z-coordinate of vector.
	 *
	 * @return  Resulting X coordinate.
	 */
	public double rotateX( final double x , final double y , final double z )
	{
		return x * xx + y * xy + z * xz;
	}

	/**
	 * Rotate a vector to Y-coordinate using this rotate.
	 *
	 * @param   x       X-coordinate of vector.
	 * @param   y       Y-coordinate of vector.
	 * @param   z       Z-coordinate of vector.
	 *
	 * @return  Resulting Y coordinate.
	 */
	public double rotateY( final double x , final double y , final double z )
	{
		return x * yx + y * yy + z * yz;
	}

	/**
	 * Rotate a vector to Z-coordinate using this rotate.
	 *
	 * @param   x       X-coordinate of vector.
	 * @param   y       Y-coordinate of vector.
	 * @param   z       Z-coordinate of vector.
	 *
	 * @return  Resulting Z coordinate.
	 */
	public double rotateZ( final double x , final double y , final double z )
	{
		return x * zx + y * zy + z * zz;
	}

	/**
	 * This function transforms a set of points. Point coordinates are supplied
	 * using double arrays containing a triplet for each point.
	 *
	 * @param   source      Source array.
	 * @param   dest        Destination array (may be <code>null</code> or too small to create new).
	 * @param   pointCount  Number of vertices.
	 *
	 * @return  Array to which the transformed coordinates were written
	 *          (may be different from the <code>dest</code> argument).
	 *
	 * @see     #multiply(double, double, double)
	 * @see     #multiply(Vector3D)
	 * @see     ArrayTools#ensureLength
	 */
	public double[] transform( final double[] source , final double[] dest , final int pointCount )
	{
		final int      resultLength = pointCount * 3;
		final double[] result       = (double[])ArrayTools.ensureLength( dest , double.class , -1 , resultLength );

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
		if ( ( lxx == 1.0 ) && ( lxy == 0.0 ) && ( lxz == 0.0 ) &&
		     ( lyx == 0.0 ) && ( lyy == 1.0 ) && ( lyz == 0.0 ) &&
		     ( lzx == 0.0 ) && ( lzy == 0.0 ) && ( lzz == 1.0 ) )
		{
			if ( ( lxo == 0.0 ) && ( lyo == 0.0 ) && ( lzo == 0.0 ) )
			{
				System.arraycopy( source , 0 , result , 0 , resultLength );
			}
			else
			{
				for ( int i = 0 ; i < resultLength ; i += 3 )
				{
					result[ i     ] = source[ i     ] + lxo;
					result[ i + 1 ] = source[ i + 1 ] + lyo;
					result[ i + 2 ] = source[ i + 2 ] + lzo;
				}
			}
		}
		else if ( ( lxo == 0.0 ) && ( lyo == 0.0 ) && ( lzo == 0.0 ) )
		{
			rotate( source , result , pointCount );
		}
		else
		{
			double x;
			double y;
			double z;

			for ( int i = 0 ; i < resultLength ; i += 3 )
			{
				x = source[ i     ];
				y = source[ i + 1 ];
				z = source[ i + 2 ];

				result[ i     ] = x * lxx + y * lxy + z * lxz + lxo;
				result[ i + 1 ] = x * lyx + y * lyy + z * lyz + lyo;
				result[ i + 2 ] = x * lzx + y * lzy + z * lzz + lzo;
			}
		}

		return result;
	}

	/**
	 * Transform a vector to X-coordinate using this transform.
	 *
	 * @param   x       X-coordinate of vector.
	 * @param   y       Y-coordinate of vector.
	 * @param   z       Z-coordinate of vector.
	 *
	 * @return  Resulting X coordinate.
	 */
	public double transformX( final double x , final double y , final double z )
	{
		return x * xx + y * xy + z * xz + xo;
	}

	/**
	 * Transform a vector to Y-coordinate using this transform.
	 *
	 * @param   x       X-coordinate of vector.
	 * @param   y       Y-coordinate of vector.
	 * @param   z       Z-coordinate of vector.
	 *
	 * @return  Resulting Y coordinate.
	 */
	public double transformY( final double x , final double y , final double z )
	{
		return x * yx + y * yy + z * yz + yo;
	}

	/**
	 * Transform a vector to Z-coordinate using this transform.
	 *
	 * @param   x       X-coordinate of vector.
	 * @param   y       Y-coordinate of vector.
	 * @param   z       Z-coordinate of vector.
	 *
	 * @return  Resulting Z coordinate.
	 */
	public double transformZ( final double x , final double y , final double z )
	{
		return x * zx + y * zy + z * zz + zo;
	}
}
