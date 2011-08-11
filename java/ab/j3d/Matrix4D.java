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
package ab.j3d;

/**
 * A 4x4 matrix, suitable for 3D projections using homogeneous coordinates.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class Matrix4D
{
	/**
	 * Identity matrix.
	 */
	public static final Matrix4D IDENTITY = new Matrix4D(
		1.0, 0.0, 0.0, 0.0,
		0.0, 1.0, 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0,
		0.0, 0.0, 0.0, 1.0
	);

	/** X quotient for X component. */ public final double xx;
	/** Y quotient for X component. */ public final double xy;
	/** Z quotient for X component. */ public final double xz;
	/** W quotient for X component. */ public final double xw;
	/** X quotient for Y component. */ public final double yx;
	/** Y quotient for Y component. */ public final double yy;
	/** Z quotient for Y component. */ public final double yz;
	/** W quotient for Y component. */ public final double yw;
	/** X quotient for Z component. */ public final double zx;
	/** Y quotient for Z component. */ public final double zy;
	/** Z quotient for Z component. */ public final double zz;
	/** W quotient for Z component. */ public final double zw;
	/** X quotient for W component. */ public final double wx;
	/** Y quotient for W component. */ public final double wy;
	/** Z quotient for W component. */ public final double wz;
	/** W quotient for W component. */ public final double ww;

	/**
	 * Constructs a new matrix with the specified components.
	 *
	 * @param   xx  X quotient for X component.
	 * @param   xy  Y quotient for X component.
	 * @param   xz  Z quotient for X component.
	 * @param   xw  W quotient for X component.
	 * @param   yx  X quotient for Y component.
	 * @param   yy  Y quotient for Y component.
	 * @param   yz  Z quotient for Y component.
	 * @param   yw  W quotient for Y component.
	 * @param   zx  X quotient for Z component.
	 * @param   zy  Y quotient for Z component.
	 * @param   zz  Z quotient for Z component.
	 * @param   zw  W quotient for Z component.
	 * @param   wx  X quotient for W component.
	 * @param   wy  Y quotient for W component.
	 * @param   wz  Z quotient for W component.
	 * @param   ww  W quotient for W component.
	 */
	public Matrix4D( final double xx, final double xy, final double xz, final double xw, final double yx, final double yy, final double yz, final double yw, final double zx, final double zy, final double zz, final double zw, final double wx, final double wy, final double wz, final double ww )
	{
		this.xx = xx;
		this.xy = xy;
		this.xz = xz;
		this.xw = xw;
		this.yx = yx;
		this.yy = yy;
		this.yz = yz;
		this.yw = yw;
		this.zx = zx;
		this.zy = zy;
		this.zz = zz;
		this.zw = zw;
		this.wx = wx;
		this.wy = wy;
		this.wz = wz;
		this.ww = ww;
	}

	/**
	 * Constructs an instance from the given 3x4 matrix and the fourth row of
	 * the identity matrix.
	 *
	 * @param   matrix  Matrix to be copied.
	 */
	public Matrix4D( final Matrix3D matrix )
	{
		xx = matrix.xx;
		xy = matrix.xy;
		xz = matrix.xz;
		xw = matrix.xo;
		yx = matrix.yx;
		yy = matrix.yy;
		yz = matrix.yz;
		yw = matrix.yo;
		zx = matrix.zx;
		zy = matrix.zy;
		zz = matrix.zz;
		zw = matrix.zo;
		wx = 0.0;
		wy = 0.0;
		wz = 0.0;
		ww = 1.0;
	}

	/**
	 * Returns the transpose of the matrix.
	 *
	 * @return  Transpose of the matrix.
	 */
	public Matrix4D transpose()
	{
		return new Matrix4D(
			xx, yx, zx, wx,
			xy, yy, zy, wy,
			xz, yz, zz, wz,
			xw, yw, zw, ww
		);
	}

	/**
	 * Returns the result of multiplying the matrix with the given matrix.
	 *
	 * @param   matrix  Matrix to multiply with.
	 *
	 * @return  Result of the multiplication.
	 */
	public Matrix4D multiply( final Matrix4D matrix )
	{
		return new Matrix4D(
			xx * matrix.xx + xy * matrix.yx + xz * matrix.zx + xw * matrix.wx,
			xx * matrix.xy + xy * matrix.yy + xz * matrix.zy + xw * matrix.wy,
			xx * matrix.xz + xy * matrix.yz + xz * matrix.zz + xw * matrix.wz,
			xx * matrix.xw + xy * matrix.yw + xz * matrix.zw + xw * matrix.ww,

			yx * matrix.xx + yy * matrix.yx + yz * matrix.zx + yw * matrix.wx,
			yx * matrix.xy + yy * matrix.yy + yz * matrix.zy + yw * matrix.wy,
			yx * matrix.xz + yy * matrix.yz + yz * matrix.zz + yw * matrix.wz,
			yx * matrix.xw + yy * matrix.yw + yz * matrix.zw + yw * matrix.ww,

			zx * matrix.xx + zy * matrix.yx + zz * matrix.zx + zw * matrix.wx,
			zx * matrix.xy + zy * matrix.yy + zz * matrix.zy + zw * matrix.wy,
			zx * matrix.xz + zy * matrix.yz + zz * matrix.zz + zw * matrix.wz,
			zx * matrix.xw + zy * matrix.yw + zz * matrix.zw + zw * matrix.ww,

			wx * matrix.xx + wy * matrix.yx + wz * matrix.zx + ww * matrix.wx,
			wx * matrix.xy + wy * matrix.yy + wz * matrix.zy + ww * matrix.wy,
			wx * matrix.xz + wy * matrix.yz + wz * matrix.zz + ww * matrix.wz,
			wx * matrix.xw + wy * matrix.yw + wz * matrix.zw + ww * matrix.ww
		);
	}

	/**
	 * Returns the result of multiplying the matrix with a 4x4 matrix consisting
	 * of the given 3x4 matrix and the fourth row of the identity matrix.
	 *
	 * @param   matrix  First three rows of the matrix to multiply with.
	 *
	 * @return  Result of the multiplication.
	 */
	public Matrix4D multiply( final Matrix3D matrix )
	{
		return new Matrix4D(
			xx * matrix.xx + xy * matrix.yx + xz * matrix.zx,
			xx * matrix.xy + xy * matrix.yy + xz * matrix.zy,
			xx * matrix.xz + xy * matrix.yz + xz * matrix.zz,
			xx * matrix.xo + xy * matrix.yo + xz * matrix.zo + xw,

			yx * matrix.xx + yy * matrix.yx + yz * matrix.zx,
			yx * matrix.xy + yy * matrix.yy + yz * matrix.zy,
			yx * matrix.xz + yy * matrix.yz + yz * matrix.zz,
			yx * matrix.xo + yy * matrix.yo + yz * matrix.zo + yw,

			zx * matrix.xx + zy * matrix.yx + zz * matrix.zx,
			zx * matrix.xy + zy * matrix.yy + zz * matrix.zy,
			zx * matrix.xz + zy * matrix.yz + zz * matrix.zz,
			zx * matrix.xo + zy * matrix.yo + zz * matrix.zo + zw,

			wx * matrix.xx + wy * matrix.yx + wz * matrix.zx,
			wx * matrix.xy + wy * matrix.yy + wz * matrix.zy,
			wx * matrix.xz + wy * matrix.yz + wz * matrix.zz,
			wx * matrix.xo + wy * matrix.yo + wz * matrix.zo + ww
		);
	}

	/**
	 * Transforms the specified 3D vector with this matrix. This
	 * transformation uses homogeneous coordinates.
	 *
	 * @param   vector  Vector to be transformed.
	 *
	 * @return  Transformed vector.
	 */
	public Vector3D transform( final Vector3D vector )
	{
		return transform( vector.x, vector.y, vector.z );
	}

	/**
	 * Transforms the specified 3D vector with this matrix. This
	 * transformation uses homogeneous coordinates.
	 *
	 * @param   x   X-coordinate.
	 * @param   y   Y-coordinate.
	 * @param   z   Z-coordinate.
	 *
	 * @return  Transformed vector.
	 */
	public Vector3D transform( final double x, final double y, final double z )
	{
		final double rx = x * xx + y * xy + z * xz + xw;
		final double ry = x * yx + y * yy + z * yz + yw;
		final double rz = x * zx + y * zy + z * zz + zw;
		final double rw = x * wx + y * wy + z * wz + ww;
		return new Vector3D( rx / rw, ry / rw, rz / rw );
	}

	public String toString()
	{
		return xx + "," + xy + ',' + xz + ',' + xw + ';' +
		       yx + ',' + yy + ',' + yz + ',' + yw + ';' +
		       zx + ',' + zy + ',' + zz + ',' + zw + ';' +
		       wx + ',' + wy + ',' + wz + ',' + ww;
	}

	/**
	 * Returns a user-friendly string representation of the matrix.
	 *
	 * @return  User-friendly string representation.
	 */
	public String toFriendlyString()
	{
		return "Matrix4D[\n\t" + xx + ", " + xy + ", " + xz + ", " + xw +
		               ",\n\t" + yx + ", " + yy + ", " + yz + ", " + yw +
		               ",\n\t" + zx + ", " + zy + ", " + zz + ", " + zw +
		               ",\n\t" + wx + ", " + wy + ", " + wz + ", " + ww + "]";
	}
}
