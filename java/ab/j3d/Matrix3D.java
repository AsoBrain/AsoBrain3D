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

import java.util.StringTokenizer;

import com.numdata.oss.ArrayTools;

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
	/** X quotient for X component. */ public final float xx;
	/** Y quotient for X component. */ public final float xy;
	/** Z quotient for X component. */ public final float xz;
	/** Translation of X component. */ public final float xo;
	/** X quotient for Y component. */ public final float yx;
	/** Y quotient for Y component. */ public final float yy;
	/** Z quotient for Y component. */ public final float yz;
	/** Translation of Y component. */ public final float yo;
	/** X quotient for Z component. */ public final float zx;
	/** Y quotient for Z component. */ public final float zy;
	/** Z quotient for Z component. */ public final float zz;
	/** Translation of Z component. */ public final float zo;

	/**
	 * Multiplication factor to transform decimal degrees to radians.
	 */
	public static final float DEG_TO_RAD = (float)( Math.PI / 180.0 );

	/**
	 * Initial value of a matrix (=identity matrix).
	 */
	public static final Matrix3D INIT = new Matrix3D( 1,0,0,0, 0,1,0,0, 0,0,1,0 );

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
	private Matrix3D( final float nxx, final float nxy , final float nxz , final float nxo ,
	                  final float nyx, final float nyy , final float nyz , final float nyo ,
	                  final float nzx, final float nzy , final float nzz , final float nzo )
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
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is the values are within a +/- 0.001 tolerance
	 *          of eachother; <code>false</code> otherwise.
	 */
	public static boolean almostEqual( final float value1 , final float value2 )
	{
		final float delta = value1 - value2;
		return ( delta >= -0.001f ) && ( delta <= 0.001f );
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
		return Float.floatToIntBits( xx ) ^ Float.floatToIntBits( xy ) ^ Float.floatToIntBits( xz ) ^ Float.floatToIntBits( xo )
		     ^ Float.floatToIntBits( yx ) ^ Float.floatToIntBits( yy ) ^ Float.floatToIntBits( yz ) ^ Float.floatToIntBits( yo )
		     ^ Float.floatToIntBits( zx ) ^ Float.floatToIntBits( zy ) ^ Float.floatToIntBits( zz ) ^ Float.floatToIntBits( zo );
	}

	/**
	 * Convert string representation of object (see toString()) back to
	 * object instance.
	 *
	 * @param   value   String representation of object.
	 *
	 * @return  Object instance.
	 */
	public static Matrix3D fromString( final String value )
	{
		final StringTokenizer st = new StringTokenizer( value , "," );

		return Matrix3D.INIT.set( Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) ,
		                          Float.parseFloat( st.nextToken() ) );
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
	public static Matrix3D getTransform( final double rx , final double ry , final double rz , final float tx , final float ty , final float tz )
	{
		final double radX = rx * DEG_TO_RAD;
		final double radY = ry * DEG_TO_RAD;
		final double radZ = rz * DEG_TO_RAD;

		final float ctX = (float)Math.cos( radX );
		final float stX = (float)Math.sin( radX );
		final float ctY = (float)Math.cos( radY );
		final float stY = (float)Math.sin( radY );
		final float ctZ = (float)Math.cos( radZ );
		final float stZ = (float)Math.sin( radZ );

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
	public Matrix3D minus( final float x , final float y , final float z )
	{
		return ( ( x == 0 ) && ( y == 0 ) && ( z == 0 ) )
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
	public Vector3D multiply( final float x , final float y , final float z )
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
		final float otherXX , final float otherXY , final float otherXZ , final float otherXO ,
		final float otherYX , final float otherYY , final float otherYZ , final float otherYO ,
		final float otherZX , final float otherZY , final float otherZZ , final float otherZO )
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
	public Matrix3D plus( final float x , final float y , final float z )
	{
		return ( x == 0.0f && y == 0.0f && z == 0.0f )
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

		if ( almostEqual( thetaRad , 0 ) )
		{
			result = this;
		}
		else
		{
			final float cos = (float)Math.cos( thetaRad );
			final float sin = (float)Math.sin( thetaRad );

			result = set( xx                  , xy                  , xz                  , xo                  ,
			              yx * cos + zx * sin , yy * cos + zy * sin , yz * cos + zz * sin , yo * cos + zo * sin ,
			              zx * cos - yx * sin , zy * cos - yy * sin , zz * cos - yz * sin , zo * cos - yo * sin );
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

		if ( almostEqual( thetaRad , 0 ) )
		{
			result = this;
		}
		else
		{
			final float cos = (float)Math.cos( thetaRad );
			final float sin = (float)Math.sin( thetaRad );

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

		if ( almostEqual( thetaRad , 0 ) )
		{
			result = this;
		}
		else
		{
			final float cos = (float)Math.cos( thetaRad );
			final float sin = (float)Math.sin( thetaRad );

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
	public Matrix3D set( final float nxx , final float nxy , final float nxz , final float nxo ,
	                     final float nyx , final float nyy , final float nyz , final float nyo ,
	                     final float nzx , final float nzy , final float nzz , final float nzo )
	{
		final float lxx = Float.isNaN( nxx ) ? xx : nxx;
		final float lxy = Float.isNaN( nxy ) ? xy : nxy;
		final float lxz = Float.isNaN( nxz ) ? xz : nxz;
		final float lxo = Float.isNaN( nxo ) ? xo : nxo;
		final float lyx = Float.isNaN( nyx ) ? yx : nyx;
		final float lyy = Float.isNaN( nyy ) ? yy : nyy;
		final float lyz = Float.isNaN( nyz ) ? yz : nyz;
		final float lyo = Float.isNaN( nyo ) ? yo : nyo;
		final float lzx = Float.isNaN( nzx ) ? zx : nzx;
		final float lzy = Float.isNaN( nzy ) ? zy : nzy;
		final float lzz = Float.isNaN( nzz ) ? zz : nzz;
		final float lzo = Float.isNaN( nzo ) ? zo : nzo;

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
	public Matrix3D setTranslation( final float x , final float y , final float z )
	{
		final float lx = Float.isNaN( x ) ? xo : x;
		final float ly = Float.isNaN( y ) ? yo : y;
		final float lz = Float.isNaN( z ) ? zo : z;

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
	 * @see     #multiply(float, float, float)
	 * @see     #multiply(Vector3D)
	 * @see     ArrayTools#ensureLength
	 */
	public float[] rotate( final float[] source , final float[] dest , final int vectorCount )
	{
		final int     resultLength = vectorCount * 3;
		final float[] result       = (float[])ArrayTools.ensureLength( dest , float.class , -1 , resultLength );

		final float lxx = xx;
		final float lxy = xy;
		final float lxz = xz;
		final float lyx = yx;
		final float lyy = yy;
		final float lyz = yz;
		final float lzx = zx;
		final float lzy = zy;
		final float lzz = zz;

		if ( ( lxx == 1.0f ) && ( lxy == 0.0f ) && ( lxz == 0.0f ) &&
		     ( lyx == 0.0f ) && ( lyy == 1.0f ) && ( lyz == 0.0f ) &&
		     ( lzx == 0.0f ) && ( lzy == 0.0f ) && ( lzz == 1.0f ) )
		{
			System.arraycopy( source , 0 , result , 0 , resultLength );
		}
		else
		{
			float x;
			float y;
			float z;

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
	public float rotateX( final float x , final float y , final float z )
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
	public float rotateY( final float x , final float y , final float z )
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
	public float rotateZ( final float x , final float y , final float z )
	{
		return x * zx + y * zy + z * zz;
	}

	/**
	 * This function transforms a set of points. Point coordinates are supplied
	 * using float arrays containing a triplet for each point.
	 *
	 * @param   source      Source array.
	 * @param   dest        Destination array (may be <code>null</code> or too small to create new).
	 * @param   pointCount  Number of vertices.
	 *
	 * @return  Array to which the transformed coordinates were written
	 *          (may be different from the <code>dest</code> argument).
	 *
	 * @see     #multiply(float, float, float)
	 * @see     #multiply(Vector3D)
	 * @see     ArrayTools#ensureLength
	 */
	public float[] transform( final float[] source , final float[] dest , final int pointCount )
	{
		final int     resultLength = pointCount * 3;
		final float[] result       = (float[])ArrayTools.ensureLength( dest , float.class , -1 , resultLength );

		final float lxx = xx;
		final float lxy = xy;
		final float lxz = xz;
		final float lyx = yx;
		final float lyy = yy;
		final float lyz = yz;
		final float lzx = zx;
		final float lzy = zy;
		final float lzz = zz;
		final float lxo = xo;
		final float lyo = yo;
		final float lzo = zo;

		/*
		 * Perform rotate, translate, or copy only if possible.
		 */
		if ( ( lxx == 1.0f ) && ( lxy == 0.0f ) && ( lxz == 0.0f ) &&
		     ( lyx == 0.0f ) && ( lyy == 1.0f ) && ( lyz == 0.0f ) &&
		     ( lzx == 0.0f ) && ( lzy == 0.0f ) && ( lzz == 1.0f ) )
		{
			if ( ( lxo == 0.0f ) && ( lyo == 0.0f ) && ( lzo == 0.0f ) )
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
		else if ( ( lxo == 0.0f ) && ( lyo == 0.0f ) && ( lzo == 0.0f ) )
		{
			rotate( source , result , pointCount );
		}
		else
		{
			float x;
			float y;
			float z;

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
	public float transformX( final float x , final float y , final float z )
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
	public float transformY( final float x , final float y , final float z )
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
	public float transformZ( final float x , final float y , final float z )
	{
		return x * zx + y * zy + z * zz + zo;
	}
}
