/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2004 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
package ab.j3d;

import java.util.StringTokenizer;

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
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
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
	public static final float DEG_TO_RAD = (float)( Math.PI / 180d );

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
	 * Test if the specified value approaches the value 0.
	 *
	 * @param	value	Value to test.
	 *
	 * @return	<code>true</code> is the specified value is within a
	 *			+/- 0.01 tolerance of 0; <code>false</code> otherwise.
	 */
	public static final boolean almost0( final double value )
	{
		return value > -0.01d && value < 0.01d;
	}

	/**
	 * Test if the specified value approaches the value 0.
	 *
	 * @param	value	Value to test.
	 *
	 * @return	<code>true</code> is the specified value is within a
	 *			+/- 0.01 tolerance of 0; <code>false</code> otherwise.
	 */
	public static final boolean almost0( final float value )
	{
		return value > -0.01f && value < 0.01f;
	}

	/**
	 * Test if the specified value approaches the value 1.
	 *
	 * @param	value	Value to test.
	 *
	 * @return	<code>true</code> is the specified value is within a
	 *			+/- 0.01 tolerance of 1; <code>false</code> otherwise.
	 */
	public static final boolean almost1( final double value )
	{
		return value > 0.99d && value < 1.01d;
	}

	/**
	 * Test if the specified value approaches the value 1.
	 *
	 * @param	value	Value to test.
	 *
	 * @return	<code>true</code> is the specified value is within a
	 *			+/- 0.01 tolerance of 1; <code>false</code> otherwise.
	 */
	public static final boolean almost1( final float value )
	{
		return value > 0.99f && value < 1.01f;
	}

	/**
	 * Compare this object to another object.
	 *
	 * @param	other	Object to compare with.
	 *
	 * @return	<code>true</code> if the objects are equal;
	 *			<code>false</code> if not.
	 */
	public boolean equals( final Object other )
	{
		if ( other == this ) return true;
		if ( other == null ) return false;
		if ( !( other instanceof Matrix3D ) ) return false;

		final Matrix3D m = (Matrix3D)other;
		return ( xx == m.xx && xy == m.xy && xz == m.xz && xo == m.xo &&
		         yx == m.yx && yy == m.yy && yz == m.yz && yo == m.yo &&
		         zx == m.zx && zy == m.zy && zz == m.zz && zo == m.zo );
	}

	/**
	 * Convert string representation of object (see toString()) back to
	 * object instance.
	 *
	 * @param	value	String representation of object.
	 *
	 * @return	Object instance.
	 */
	public static final Matrix3D fromString( final String value )
	{
		final StringTokenizer st = new StringTokenizer( value , "," );

		return Matrix3D.INIT.set( new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() ,
		                          new Float( st.nextToken() ).floatValue() );
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
	public static final Matrix3D getTransform( float rx , float ry , float rz , final float tx , final float ty , final float tz )
	{
		final float ctX = (float)Math.cos( rx *= DEG_TO_RAD );
		final float stX = (float)Math.sin( rx );
		final float ctY = (float)Math.cos( ry *= DEG_TO_RAD );
		final float stY = (float)Math.sin( ry );
		final float ctZ = (float)Math.cos( rz *= DEG_TO_RAD );
		final float stZ = (float)Math.sin( rz );

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
	public final Matrix3D inverse()
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
		return ( xx * ( yy - yx ) + yx * ( xx - xy ) ) < 0d;
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param	vector	Vector specifying the translation.
	 *
	 * @return	new Matrix3D with translation
	 */
	public Matrix3D minus( final Vector3D vector )
	{
		return minus( vector.x , vector.y , vector.z );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param	x	x-coordinate of vector specifying the translation.
	 * @param	y	y-coordinate of vector specifying the translation.
	 * @param	z	z-coordinate of vector specifying the translation.
	 *
	 * @return	new Matrix3D with translation
	 */
	public Matrix3D minus( final float x , final float y , final float z )
	{
		if ( x == 0f && y == 0f && z == 0f ) return this;
		return setTranslation( xo - x , yo - y , zo - z );
	}

	/**
	 * Transform a box using this transform.
	 *
	 * @param	box		Box to transform.
	 *
	 * @return	Resulting box.
	 */
	public Bounds3D multiply( final Bounds3D box )
	{
		return box.set( multiply( box.v1 ) , multiply( box.v2 ) );
	}

	/**
	 * Execute matrix multiplication between two matrices and set the
	 * result in this transform.
	 *
	 * @param	other	Transform to multiply with.
	 *
	 * @return	Resulting matrix.
	 */
	public Matrix3D multiply( final Matrix3D other )
	{
		return set(
		 /* xx */ this.xx * other.xx + this.yx * other.xy + this.zx * other.xz ,
		 /* xy */ this.xy * other.xx + this.yy * other.xy + this.zy * other.xz ,
		 /* xz */ this.xz * other.xx + this.yz * other.xy + this.zz * other.xz ,
		 /* xo */ this.xo * other.xx + this.yo * other.xy + this.zo * other.xz + other.xo ,

		 /* yx */ this.xx * other.yx + this.yx * other.yy + this.zx * other.yz ,
		 /* yy */ this.xy * other.yx + this.yy * other.yy + this.zy * other.yz ,
		 /* yz */ this.xz * other.yx + this.yz * other.yy + this.zz * other.yz ,
		 /* yo */ this.xo * other.yx + this.yo * other.yy + this.zo * other.yz + other.yo ,

		 /* zx */ this.xx * other.zx + this.yx * other.zy + this.zx * other.zz ,
		 /* zy */ this.xy * other.zx + this.yy * other.zy + this.zy * other.zz ,
		 /* zz */ this.xz * other.zx + this.yz * other.zy + this.zz * other.zz ,
		 /* zo */ this.xo * other.zx + this.yo * other.zy + this.zo * other.zz + other.zo );
	}

	/**
	 * Transform a vector using this transform.
	 *
	 * @param	vector	Vector to transform.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D multiply( final Vector3D vector )
	{
		return vector.set(
			vector.x * xx + vector.y * xy + vector.z * xz + xo ,
			vector.x * yx + vector.y * yy + vector.z * yz + yo ,
			vector.x * zx + vector.y * zy + vector.z * zz + zo );
	}

	/**
	 * Transform a vector using this transform.
	 *
	 * @param	x	X-value of vector.
	 * @param	y	Y-value of vector.
	 * @param	z	Z-value of vector.
	 *
	 * @return	Resulting vector.
	 */
	public Vector3D multiply( final float x , final float y , final float z )
	{
		return Vector3D.INIT.set(
			x * xx + y * xy + z * xz + xo ,
			x * yx + y * yy + z * yz + yo ,
			x * zx + y * zy + z * zz + zo
		);
	}

	/**
	 * Multiply this matrix with another matrix and return the result
	 * of this multiplication.
	 *
	 * @param	xx	Matrix row 0, column 0 (x-factor for x).
	 * @param	xy	Matrix row 0, column 1 (y-factor for x).
	 * @param	xz	Matrix row 0, column 2 (z-factor for x).
	 * @param	xo	Matrix row 0, column 3 (offset   for x).
	 * @param	yx	Matrix row 1, column 0 (x-factor for y).
	 * @param	yy	Matrix row 1, column 1 (y-factor for y).
	 * @param	yz	Matrix row 1, column 2 (z-factor for y).
	 * @param	yo	Matrix row 1, column 3 (offset   for y).
	 * @param	zx	Matrix row 2, column 0 (x-factor for z).
	 * @param	zy	Matrix row 2, column 1 (y-factor for z).
	 * @param	zz	Matrix row 2, column 2 (z-factor for z).
	 * @param	zo	Matrix row 2, column 3 (offset   for z).
	 *
	 * @return	Resulting matrix.
	 */
	public final Matrix3D multiply(
		final float xx , final float xy , final float xz , final float xo ,
		final float yx , final float yy , final float yz , final float yo ,
		final float zx , final float zy , final float zz , final float zo )
	{
		return set(
		 /* xx */ this.xx * xx + this.yx * xy + this.zx * xz ,
		 /* xy */ this.xy * xx + this.yy * xy + this.zy * xz ,
		 /* xz */ this.xz * xx + this.yz * xy + this.zz * xz ,
		 /* xo */ this.xo * xx + this.yo * xy + this.zo * xz + xo ,

		 /* yx */ this.xx * yx + this.yx * yy + this.zx * yz ,
		 /* yy */ this.xy * yx + this.yy * yy + this.zy * yz ,
		 /* yz */ this.xz * yx + this.yz * yy + this.zz * yz ,
		 /* yo */ this.xo * yx + this.yo * yy + this.zo * yz + yo ,

		 /* zx */ this.xx * zx + this.yx * zy + this.zx * zz ,
		 /* zy */ this.xy * zx + this.yy * zy + this.zy * zz ,
		 /* zz */ this.xz * zx + this.yz * zy + this.zz * zz ,
		 /* zo */ this.xo * zx + this.yo * zy + this.zo * zz + zo );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param	vector	Vector specifying the translation.
	 *
	 * @return	new Matrix3D with translation
	 */
	public Matrix3D plus( final Vector3D vector )
	{
		return plus( vector.x , vector.y , vector.z );
	}

	/**
	 * Translate the transform by the specified vector.
	 *
	 * @param	x	x-coordintate of vector specifying the translation.
	 * @param	y	y-coordintate of vector specifying the translation.
	 * @param	z	z-coordintate of vector specifying the translation.
	 *
	 * @return	new Matrix3D with translation
	 */
	public Matrix3D plus( final float x , final float y , final float z )
	{
		if ( x == 0f && y == 0f && z == 0f ) return this;
		return setTranslation( xo + x , yo + y , zo + z );
	}

	/**
	 * This function performs just the rotational part of
	 * of the transform on a set of vertices. Vertices
	 * are supplied using float arrays with a sequence of
	 * 3 floats for each vertex.
	 *
	 * @param	source			Source array.
	 * @param	dest			Destination array.
	 * @param	vertexCount		Number of vertices.
	 */
	public final void rotate( final float[] source , final float[] dest , int vertexCount )
	{
		final float lxx = xx;
		final float lxy = xy;
		final float lxz = xz;
		final float lyx = yx;
		final float lyy = yy;
		final float lyz = yz;
		final float lzx = zx;
		final float lzy = zy;
		final float lzz = zz;

		if ( (lxx == 1f) && (lxy == 0f) && (lxz == 0f) &&
		     (lyx == 0f) && (lyy == 1f) && (lyz == 0f) &&
		     (lzx == 0f) && (lzy == 0f) && (lzz == 1f) )
		{
			System.arraycopy( source , 0 , dest , 0 , vertexCount * 3 );
		}
		else
		{
			float x,y,z;

			vertexCount *= 3;

			for ( int i = 0 ; i < vertexCount ; i += 3 )
			{
				x = source[ i     ];
				y = source[ i + 1 ];
				z = source[ i + 2 ];

				dest[ i     ] = x * lxx + y * lxy + z * lxz;
				dest[ i + 1 ] = x * lyx + y * lyy + z * lyz;
				dest[ i + 2 ] = x * lzx + y * lzy + z * lzz;
			}
		}
	}

	/**
	 * Rotate along the X-axis.
	 *
	 * @param	theta	Rotate theta radians about the X-axis
	 *
	 * @return  Rotated matrix.
	 */
	public Matrix3D rotateX( final float theta )
	{
		if ( theta == 0d )
			return this;

		final float ct = (float)Math.cos( theta );
		final float st = (float)Math.sin( theta );

		return set(
			xx                , xy                , xz                , xo                ,
			yx * ct + zx * st , yy * ct + zy * st , yz * ct + zz * st , yo * ct + zo * st ,
			zx * ct - yx * st , zy * ct - yy * st , zz * ct - yz * st , zo * ct - yo * st );
	}

	/**
	 * Rotate along the Y-axis.
	 *
	 * @param	theta	Rotate theta radians about the Y-axis
	 *
	 * @return  Rotated matrix.
	 */
	public Matrix3D rotateY( final float theta )
	{
		if ( theta == 0d )
			return this;

		final float ct = (float)Math.cos( theta );
		final float st = (float)Math.sin( theta );

		return set(
		xx * ct + zx * st , xy * ct + zy * st , xz * ct + zz * st , xo * ct + zo * st ,
		yx                , yy                , yz                , yo                ,
		zx * ct - xx * st , zy * ct - xy * st , zz * ct - xz * st , zo * ct - xo * st );
	}

	/**
	 * Rotate along the Z-axis.
	 *
	 * @param	theta	Rotate theta radians about the Z-axis
	 *
	 * @return  Rotated matrix.
	 */
	public Matrix3D rotateZ( final float theta )
	{
		if ( theta == 0d )
			return this;

		final float ct = (float)Math.cos( theta );
		final float st = (float)Math.sin( theta );

		return set(
			xx * ct - yx * st , xy * ct - yy * st , xz * ct - yz * st , xo * ct - yo * st ,
			yx * ct + xx * st , yy * ct + xy * st , yz * ct + xz * st , yo * ct + xo * st ,
			zx                , zy                , zz                , zo );
	}

	/**
	 * Set all values in the matrix and return the resulting matrix.
	 *
	 * @param	xx	Matrix row 0, column 0 (x-factor for x).
	 * @param	xy	Matrix row 0, column 1 (y-factor for x).
	 * @param	xz	Matrix row 0, column 2 (z-factor for x).
	 * @param	xo	Matrix row 0, column 3 (offset   for x).
	 * @param	yx	Matrix row 1, column 0 (x-factor for y).
	 * @param	yy	Matrix row 1, column 1 (y-factor for y).
	 * @param	yz	Matrix row 1, column 2 (z-factor for y).
	 * @param	yo	Matrix row 1, column 3 (offset   for y).
	 * @param	zx	Matrix row 2, column 0 (x-factor for z).
	 * @param	zy	Matrix row 2, column 1 (y-factor for z).
	 * @param	zz	Matrix row 2, column 2 (z-factor for z).
	 * @param	zo	Matrix row 2, column 3 (offset   for z).
	 *
	 * @return	Resulting vector.
	 */
	public final Matrix3D set(
		float xx , float xy , float xz , float xo ,
		float yx , float yy , float yz , float yo ,
		float zx , float zy , float zz , float zo )
	{
		if ( xx != xx /* => NaN*/ ) xx = this.xx;
		if ( xy != xy /* => NaN*/ ) xy = this.xy;
		if ( xz != xz /* => NaN*/ ) xz = this.xz;
		if ( xo != xo /* => NaN*/ ) xo = this.xo;

		if ( yx != yx /* => NaN*/ ) yx = this.yx;
		if ( yy != yy /* => NaN*/ ) yy = this.yy;
		if ( yz != yz /* => NaN*/ ) yz = this.yz;
		if ( yo != yo /* => NaN*/ ) yo = this.yo;

		if ( zx != zx /* => NaN*/ ) zx = this.zx;
		if ( zy != zy /* => NaN*/ ) zy = this.zy;
		if ( zz != zz /* => NaN*/ ) zz = this.zz;
		if ( zo != zo /* => NaN*/ ) zo = this.zo;

		if ( xx == this.xx && xy == this.xy && xz == this.xz && xo == this.xo &&
			 yx == this.yx && yy == this.yy && yz == this.yz && yo == this.yo &&
			 zx == this.zx && zy == this.zy && zz == this.zz && zo == this.zo )
		return( this );

		return new Matrix3D( xx , xy , xz , xo ,
							 yx , yy , yz , yo ,
							 zx , zy , zz , zo );
	}

	/**
	 * Set translation of a transform to the specified vector.
	 *
	 * @param	vector	Vector to use.
	 *
	 * @return	Resulting matrix.
	 */
	public Matrix3D setTranslation( final Vector3D vector )
	{
		return setTranslation( vector.x , vector.y , vector.z );
	}

	/**
	 * Set translation of a transform to the specified vector.
	 *
	 * @param	x	X-value of vector.
	 * @param	y	Y-value of vector.
	 * @param	z	Z-value of vector.
	 *
	 * @return	Resulting matrix.
	 */
	public Matrix3D setTranslation( float x , float y , float z )
	{
		if ( x != x /* => NaN*/ ) x = xo;
		if ( y != y /* => NaN*/ ) y = yo;
		if ( z != z /* => NaN*/ ) z = zo;

		if ( x == xo && y == yo && z == zo )
			return this;

		return set( xx , xy , xz , x ,
					yx , yy , yz , y ,
					zx , zy , zz , z );
	}

	/**
	 * Get string representation of object.
	 *
	 * @return	String representation of object.
	 */
	public String toString()
	{
		return( xx + "," + xy + "," + xz + "," + xo + "," +
				yx + "," + yy + "," + yz + "," + yo + "," +
				zx + "," + zy + "," + zz + "," + zo );
	}

	/**
	 * This function transforms a set of vertices. Vertices
	 * are supplied using float arrays with a sequence of
	 * 3 floats for each vertex.
	 *
	 * @param	source			Source array.
	 * @param	dest			Destination array.
	 * @param	vertexCount		Number of vertices.
	 */
	public final void transform( final float[] source , final float[] dest , int vertexCount )
	{
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
		if ( (lxx == 1f) && (lxy == 0f) && (lxz == 0f) &&
		     (lyx == 0f) && (lyy == 1f) && (lyz == 0f) &&
		     (lzx == 0f) && (lzy == 0f) && (lzz == 1f) )
		{
			if ( (lxo == 0f) && (lyo == 0f) && (lzo == 0f) )
			{
				System.arraycopy( source , 0 , dest , 0 , vertexCount * 3 );
			}
			else
			{
				vertexCount *= 3;
				for ( int i = 0 ; i < vertexCount ; i += 3 )
				{
					dest[ i     ] = source[ i     ] + lxo;
					dest[ i + 1 ] = source[ i + 1 ] + lyo;
					dest[ i + 2 ] = source[ i + 2 ] + lzo;
				}
			}
		}
		else if ( (lxo == 0f) && (lyo == 0f) && (lzo == 0f) )
		{
			rotate( source , dest , vertexCount );
		}
		else
		{
			float x,y,z;

			vertexCount *= 3;
			for ( int i = 0 ; i < vertexCount ; i += 3 )
			{
				x = source[ i     ];
				y = source[ i + 1 ];
				z = source[ i + 2 ];

				dest[ i     ] = x * lxx + y * lxy + z * lxz + lxo;
				dest[ i + 1 ] = x * lyx + y * lyy + z * lyz + lyo;
				dest[ i + 2 ] = x * lzx + y * lzy + z * lzz + lzo;
			}
		}
	}

	/**
	 * Transform a vector to X-coordinate using this transform.
	 *
	 * @param	x		X-coordinate of vector.
	 * @param	y		Y-coordinate of vector.
	 * @param	z		Z-coordinate of vector.
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
	 * @param	x		X-coordinate of vector.
	 * @param	y		Y-coordinate of vector.
	 * @param	z		Z-coordinate of vector.
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
	 * @param	x		X-coordinate of vector.
	 * @param	y		Y-coordinate of vector.
	 * @param	z		Z-coordinate of vector.
	 *
	 * @return  Resulting Z coordinate.
	 */
	public float transformZ( final float x , final float y , final float z )
	{
		return x * zx + y * zy + z * zz + zo;
	}

}
