package common.model;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2002 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2002 - All Rights Reserved
 *
 * This software may not be used, copyied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
import java.util.StringTokenizer;

/**
 * This class is used to represent a 3D transformation matrix (although
 * it may also be used for 2D transformations).
 * <p>
 * The matrix is organized as follows:
 * <pre>
 * | xx xy xz xo |
 * | yx yy yz yo |
 * | zx zy zz zo |
 * | 0  0  0  1  |
 * </pre>
 *
 * @author	Peter S. Heijnen
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class Matrix3D
{
	public final float xx;
	public final float xy;
	public final float xz;
	public final float xo;
	public final float yx;
	public final float yy;
	public final float yz;
	public final float yo;
	public final float zx;
	public final float zy;
	public final float zz;
	public final float zo;
	
	public static final float DEG_TO_RAD = (float)( Math.PI / 180d );
	
	/**
	 * Initial value of a matrix (=identity matrix)
	 */
	public final static Matrix3D INIT = new Matrix3D(
		1f,0f,0f,0f, 0f,1f,0f,0f, 0f,0f,1f,0f );

	/**
	 * Construct a new transform
	 */
	private Matrix3D( float nxx, float nxy , float nxz , float nxo , float nyx, float nyy , float nyz , float nyo , float nzx, float nzy , float nzz , float nzo )
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
	public final static boolean almost0( double value )
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
	public final static boolean almost0( float value )
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
	public final static boolean almost1( double value )
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
	public final static boolean almost1( float value )
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
	public boolean equals( Object other )
	{
		if ( other == this ) return true;
		if ( other == null ) return false;
		if ( !( other instanceof Matrix3D ) ) return false;
		Matrix3D m = (Matrix3D)other;
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
	public final static Matrix3D fromString( String value )
	{
		StringTokenizer st = new StringTokenizer( value , "," );
		
		return Matrix3D.INIT.set(
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() ,
			Float.valueOf( st.nextToken() ).floatValue() );
	}

	/**
	 * Get transformation matrix based on 6 parameters specifying rotation angles
	 * and a translation vector. Starting with the identity matrix, rotation is 
	 * performed (Z,X,Y order), than the translation is set.
	 */
	public static final Matrix3D getTransform( float rx , float ry , float rz , float tx , float ty , float tz )
	{
		float ctX = (float)Math.cos( rx *= DEG_TO_RAD );
		float stX = (float)Math.sin( rx );
		float ctY = (float)Math.cos( ry *= DEG_TO_RAD );
		float stY = (float)Math.sin( ry );
		float ctZ = (float)Math.cos( rz *= DEG_TO_RAD );
		float stZ = (float)Math.sin( rz );

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
	 * <PRE>
	 *       | xx xy xz xo |
	 *       | yx yy yz yo |
	 *  T  = | zx zy zz zo |
	 *       | 0  0  0  1  |
	 *
	 *       | xx yz zx -xo*xx-yo*yx-zo*zx |
	 *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
	 *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
	 *       | 0  0  0  1                  |
	 * </PRE>
	 */
	public final Matrix3D inverse()
	{
		return set(
		xx , yx , zx , - xo * xx - yo * yx - zo * zx ,
		xy , yy , zy , - xo * xy - yo * yy - zo * zy ,
		xz , yz , zz , - xo * xz - yo * yz - zo * zz
		);
	}

	/**
	 * Return <code>true</code> if this matrxyd causes mirroring in the XY plane.
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
	public Matrix3D minus( Vector3D vector )
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
	public Matrix3D minus( float x , float y , float z )
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
	public Bounds3D multiply( Bounds3D box )
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
	public Matrix3D multiply( Matrix3D other )
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
	public Vector3D multiply( Vector3D vector )
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
	public Vector3D multiply( float x , float y , float z )
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
		float xx , float xy , float xz , float xo ,
		float yx , float yy , float yz , float yo ,
		float zx , float zy , float zz , float zo )
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
	public Matrix3D plus( Vector3D vector )
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
	public Matrix3D plus( float x , float y , float z )
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
		float lxx = xx , lxy = xy , lxz = xz;
		float lyx = yx , lyy = yy , lyz = yz;
		float lzx = zx , lzy = zy , lzz = zz;
		
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
	 */
	public Matrix3D rotateX( float theta )
	{
		if ( theta == 0d ) 
			return this;
		
		float ct = (float)Math.cos( theta );
		float st = (float)Math.sin( theta );
		
		return set(
		xx                , xy                , xz                , xo                ,
		yx * ct + zx * st , yy * ct + zy * st , yz * ct + zz * st , yo * ct + zo * st ,
		zx * ct - yx * st , zy * ct - yy * st , zz * ct - yz * st , zo * ct - yo * st );
	}

	/**
	 * Rotate along the Y-axis.
	 *
	 * @param	theta	Rotate theta radians about the Y-axis
	 */
	public Matrix3D rotateY( float theta )
	{
		if ( theta == 0d ) 
			return this;
		
		float ct = (float)Math.cos( theta );
		float st = (float)Math.sin( theta );
		
		return set(
		xx * ct + zx * st , xy * ct + zy * st , xz * ct + zz * st , xo * ct + zo * st ,
		yx                , yy                , yz                , yo                ,
		zx * ct - xx * st , zy * ct - xy * st , zz * ct - xz * st , zo * ct - xo * st );
	}

	/**
	 * Rotate along the Z-axis.
	 *
	 * @param	theta	Rotate theta radians about the Z-axis
	 */
	public Matrix3D rotateZ( float theta )
	{
		if ( theta == 0d ) 
			return this;
		
		float ct = (float)Math.cos( theta );
		float st = (float)Math.sin( theta );
		
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
	public Matrix3D setTranslation( Vector3D vector )
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
	public final void transform( float[] source , float[] dest , int vertexCount )
	{
		float lxx = xx , lxy = xy , lxz = xz;
		float lyx = yx , lyy = yy , lyz = yz;
		float lzx = zx , lzy = zy , lzz = zz;
		float lxo = xo , lyo = yo , lzo = zo;

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
	 */
	public float transformX( float x , float y , float z )
	{
		return x * xx + y * xy + z * xz + xo;
	}

	/**
	 * Transform a vector to Y-coordinate using this transform.
	 *
	 * @param	x		X-coordinate of vector.
	 * @param	y		Y-coordinate of vector.
	 * @param	z		Z-coordinate of vector.
	 */
	public float transformY( float x , float y , float z )
	{
		return x * yx + y * yy + z * yz + yo;
	}

	/**
	 * Transform a vector to Z-coordinate using this transform.
	 *
	 * @param	x		X-coordinate of vector.
	 * @param	y		Y-coordinate of vector.
	 * @param	z		Z-coordinate of vector.
	 */
	public float transformZ( float x , float y , float z )
	{
		return x * zx + y * zy + z * zz + zo;
	}

}
