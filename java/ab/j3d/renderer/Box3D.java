package ab.j3d.renderer;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2002 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2002 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;

/**
 * This class defines a 3D box.
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class Box3D
	extends Object3D
{
	/**
	 * Transformation applied to all vertices of the box.
	 */
	private final Matrix3D _xform;

	/**
	 * Width of box (x-axis).
	 */
	private final float _dx;

	/**
	 * Height of box (y-axis).
	 */
	private final float _dy;

	/**
	 * Depth of box (z-axis).
	 */
	private final float _dz;

	/**
	 * Set box properties.
	 *
	 * @param   xform           Transformation to apply to all vertices of the box.
	 * @param   dx              Width of box (x-axis).
	 * @param   dy              Height of box (y-axis).
	 * @param   dz              Depth of box (z-axis).
	 * @param   mainTexture     Main texture of box.
	 * @param   sideTexture     Texture for sides of box.
	 */
	public Box3D( final Matrix3D xform , final float dx , final float dy , final float dz , final TextureSpec mainTexture , final TextureSpec sideTexture )
	{
		_xform = xform;
		_dx    = dx;
		_dy    = dy;
		_dz    = dz;

		/*
		 * Set frontal, vertical, and horizontal face properties.
		 */
		final float ax = Math.abs( dx );
		final float ay = Math.abs( dy );
		final float az = Math.abs( dz );
		final boolean isFrontal    = ( ay < ax && ay < az );
		final boolean isVertical   = ( ax < ay && ax < az );
		final boolean isHorizontal = ( az < ax && az < ay );

		/*
		 * Create vertices;
		 */
		final float[] vertices =
		{
			  0 ,   0 ,   0 , // 0
			_dx ,   0 ,   0 , // 1
			_dx , _dy ,   0 , // 2
			  0 , _dy ,   0 , // 3
			  0 ,   0 , _dz , // 4
			_dx ,   0 , _dz , // 5
			_dx , _dy , _dz , // 6
			  0 , _dy , _dz   // 7
		};

		xform.transform( vertices , vertices , 8 );

		/*
		 * Create faces
		 */
		final int[][] faceVert =
		{
			{ 0 , 4 , 5 , 1 } , // 0 - front
			{ 2 , 6 , 7 , 3 } , // 1 - back
			{ 1 , 5 , 6 , 2 } , // 2 - right
			{ 3 , 7 , 4 , 0 } , // 3 - left
			{ 4 , 7 , 6 , 5 } , // 4 - top
			{ 1 , 2 , 3 , 0 }   // 5 - bottom
		};

		/*
		 * Set texture properties.
		 */
		final TextureSpec frontalTexture    = isFrontal ? mainTexture : sideTexture;
		final boolean     flipFrontal       = isHorizontal;
		final TextureSpec verticalTexture   = isVertical ? mainTexture : sideTexture;
		final boolean     flipVertical      = isHorizontal;
		final TextureSpec horizontalTexture = isHorizontal ? mainTexture : sideTexture;
		final boolean     flipHorizontal    = !isVertical;

		final TextureSpec[] faceMat =
		{
			frontalTexture ,
			frontalTexture ,
			verticalTexture ,
			verticalTexture ,
			horizontalTexture ,
			horizontalTexture
		};

		final boolean[] faceSmooth =
		{
			false ,
			false ,
			false ,
			false ,
			false ,
			false
		};

		int[][] faceTU = null;
		int[][] faceTV = null;
		if ( ( mainTexture != null && mainTexture.isTexture() )
		  || ( sideTexture != null && sideTexture.isTexture() ) )
		{
			faceTU = new int[ 6 ][];
			faceTV = new int[ 6 ][];

			setTexture( frontalTexture , flipFrontal , 0 ,
				faceTU , xform.xo , xform.xo + _dx ,
				faceTV , xform.zo , xform.zo + _dz );

			setTexture( verticalTexture , flipVertical , 2 ,
				faceTU , xform.yo , xform.yo + _dy ,
				faceTV , xform.zo , xform.zo + _dz );

			setTexture( horizontalTexture , flipHorizontal , 4 ,
				faceTU , xform.xo , xform.xo + _dx ,
				faceTV , xform.yo , xform.yo + _dy );
		}

		/*
		 * Set Object3D properties.
		 */
		set( vertices , faceVert , faceMat , faceTU , faceTV , null , faceSmooth );
	}

	/**
	 * Get width of box (x-axis).
	 *
	 * @return  Width of box (x-axis).
	 */
	public float getDX()
	{
		return _dx;
	}

	/**
	 * Get height of box (y-axis).
	 *
	 * @return  Height of box (y-axis).
	 */
	public float getDY()
	{
		return _dy;
	}

	/**
	 * Get depth of box (z-axis).
	 *
	 * @return	Depth of box (z-axis).
	 */
	public float getDZ()
	{
		return _dz;
	}

	/**
	 * Get transformation applied to all vertices of the box.
	 *
	 * @return	Transformation applied to all vertices of the box.
	 */
	public Matrix3D getTransform()
	{
		return _xform;
	}

	/**
	 * Generate texture coordinates for the specified face.
	 *
	 * @param   texture     Texture to use.
	 * @param   flipUV      <code>true</code> te reverse meaning of u and v.
	 * @param   index       Index of face.
	 * @param   faceTU      Target array to store U texture coordinates.
	 * @param   tu1         Start U coordinate.
	 * @param   tu2         End U coordinate.
	 * @param   faceTV      Target array to store V texture coordinates.
	 * @param   tv1         Start V coordinate.
	 * @param   tv2         End V coordinate.
	 */
	private static void setTexture(
		final TextureSpec texture , final boolean flipUV , final int index ,
		final int[][] faceTU , final float tu1 , final float tu2 ,
		final int[][] faceTV , final float tv1 , final float tv2 )
	{
		/*
		 * Return immediately if texture specification is invalid or does not
		 * specify a texture image.
		 */
		if ( ( texture == null ) || !texture.isTexture() )
			return;

		int i1;
		int i2;
		int t;
		int m;
		int[][] a;
		final float s = texture.textureScale;

		/*
		 * Translate the U1,U2 texture coordinates to the lowest possible (but
		 * positive) range. If either is negative, translation occurs in positive
		 * direction; if both are beyond the maximum value, translation occurs in
		 * negative direction.
		 */
		i1 = (int)( tu1 * s /*+ 0.5f*/ );
		i2 = (int)( tu2 * s /*+ 0.5f*/ );

		m = flipUV ? texture.getTextureHeight( null ) : texture.getTextureWidth( null );
		if ( ( t = ( i1 < i2 ) ? i1 : i2 ) < 0 )
		{
			t -= m - 1;
			t -= t % m;
			i1 -= t;
			i2 -= t;
		}
		else if ( ( t = i1 + i2 - t ) >= m )
		{
			t -= t % m;
			i1 -= t;
			i2 -= t;
		}

		/*
		 * Set texture coordinates. Flip meaning of U and V if requested.
		 */
		a = ( flipUV ? faceTV : faceTU );
		a[ index     ] = new int[] { i1 , i1 , i2 , i2 };
		a[ index + 1 ] = new int[] { i2 , i2 , i1 , i1 };

		/*
		 * Translate the V1,V2 texture coordinates to the lowest possible (but
		 * positive) range. If either is negative, translation occurs in positive
		 * direction; if both are beyond the maximum value, translation occurs in
		 * negative direction.
		 */
		i1 = (int)( tv1 * s /*+ 0.5f*/ );
		i2 = (int)( tv2 * s /*+ 0.5f*/ );

		m = flipUV ? texture.getTextureWidth( null ) : texture.getTextureHeight( null );
		if ( ( t = ( i1 < i2 ) ? i1 : i2 ) < 0 )
		{
			t -= m - 1;
			t -= t % m;
			i1 -= t;
			i2 -= t;
		}
		else if ( ( t = i1 + i2 - t ) >= m )
		{
			t -= t % m;
			i1 -= t;
			i2 -= t;
		}

		/*
		 * Set texture coordinates. Flip meaning of U and V if requested.
		 */
		a = ( flipUV ? faceTU : faceTV);
		a[ index     ] =// new int[] { i1 , i2 , i2 , i1 };
		a[ index + 1 ] = new int[] { i1 , i2 , i2 , i1 };
	}

}
