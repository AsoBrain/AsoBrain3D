package common.renderer;

import java.util.Hashtable;
import common.db.TextureSpec;
import common.model.Matrix3D;

/**
 * This class defines a 3D box.
 *
 * @version 1.0 (20011128, PSH) 
 * @author	Peter S. Heijnen
 */
public class Box3D
	extends Object3D
{
	public final Matrix3D xform;
	public final float   dx;
	public final float   dy;
	public final float   dz;


	public final static int X_POS = 0;
	public final static int X_NEG = 1;
	public final static int Y_POS = 2;
	public final static int Y_NEG = 3;
	public final static int Z_POS = 4;
	public final static int Z_NEG = 5;

	/**
	 * Constructor for box object.
	 *
	 * @param	xform	Transform for vertices of object.
	 * @param	dx		Width of box (x-axis).
	 * @param	dy		Height of box (y-axis).
	 * @param	dz		Depth of box (z-axis).
	 */
	public Box3D( final Matrix3D xform , final float dx , final float dy , final float dz )
	{
		this( xform , dx , dy , dz , null , null );
	}

	/**
	 * Constructor for box object.
	 *
	 * @param	xform				Transformation to apply to all vertices of the box.
	 * @param	dx					Width of box (x-axis).
	 * @param	dy					Height of box (y-axis).
	 * @param	dz					Depth of box (z-axis).
	 * @param	frontalTexture		Texture to use for front and back (Y-axis).
	 * @param	flipFrontal			Rotate texture for front and back 90 degrees.
	 * @param	verticalTexture		Texture to use for left and right (X-axis).
	 * @param	flipVertical		Rotate texture for left and right 90 degrees.
	 * @param	horizontalTexture	Texture to use for top and bottom (Z-axis).
	 * @param	flipHorizontal		Rotate texture for top and bottom 90 degrees.
	 */
	public Box3D( final Matrix3D xform , final float dx , final float dy , final float dz , 
		TextureSpec frontalTexture    , boolean flipFrontal ,
		TextureSpec verticalTexture   , boolean flipVertical ,
		TextureSpec horizontalTexture , boolean flipHorizontal )
	{
		this.xform = xform;
		this.dx    = dx;
		this.dy    = dy;
		this.dz    = dz;

		
		generate( 
			frontalTexture , flipFrontal ,
			verticalTexture , flipVertical ,
			horizontalTexture , flipHorizontal );
	}

	/**
	 * Constructor for box object.
	 *
	 * @param	xform		Transformation to apply to all vertices of the box.
	 * @param	dx			Width of box (x-axis).
	 * @param	dy			Height of box (y-axis).
	 * @param	dz			Depth of box (z-axis).
	 * @param	mainTexture	Main texture of box.
	 * @param	sideTexture	Texture for sides of box.
	 */
	public Box3D( final Matrix3D xform , final float dx , final float dy , final float dz , final TextureSpec mainTexture , final TextureSpec sideTexture )
	{
		this.xform = xform;
		this.dx    = dx;
		this.dy    = dy;
		this.dz    = dz;

		float ax = Math.abs( dx );
		float ay = Math.abs( dy );
		float az = Math.abs( dz );
		
		/*
		 * Frontal (delta-Y is smallest)
		 */
		if ( ay < ax && ay < az )
			generate( mainTexture , false , sideTexture , false , sideTexture , true );
			
		/*
		 * Vertical (delta-X is smallest)
		 */
		else if ( ax < ay && ax < az )
			generate( sideTexture , false , mainTexture , false , sideTexture , false );
			
		/*
		 * Horizontal (delta-Z is smallest)
		 */
		else //if ( az < ax && az < ay )
			generate( sideTexture , true , sideTexture , true , mainTexture , true );
	}

	/**
	 * Constructor for box object.
	 *
	 * @param	x			Base position on X-axis.
	 * @param	y			Base position on Y-axis.
	 * @param	z			Base position on Z-axis.
	 * @param	dx			Width of box (x-axis).
	 * @param	dy			Height of box (y-axis).
	 * @param	dz			Depth of box (z-axis).
	 * @param	texture		Texture to use for all sides of the 3D box.
	 */
	public Box3D( final float x , final float y , final float z , final float dx , final float dy , final float dz , TextureSpec texture )
	{
		this( Matrix3D.INIT.plus( x , y , z ) , dx , dy , dz , texture , texture );
	}

	/**
	 * Generate Object3D properties.
	 *
	 * @param	frontalTexture		Texture to use for front and back (Y-axis).
	 * @param	flipFrontal			Rotate texture for front and back 90 degrees.
	 * @param	verticalTexture		Texture to use for left and right (X-axis).
	 * @param	flipVertical		Rotate texture for left and right 90 degrees.
	 * @param	horizontalTexture	Texture to use for top and bottom (Z-axis).
	 * @param	flipHorizontal		Rotate texture for top and bottom 90 degrees.
	 */
	public void generate( 
		TextureSpec frontalTexture    , boolean flipFrontal ,
		TextureSpec verticalTexture   , boolean flipVertical ,
		TextureSpec horizontalTexture , boolean flipHorizontal )
	{
		/*
		 * Check if any face has a texture image.
		 */
		final boolean hasTextureImage =
			( frontalTexture    != null && frontalTexture   .isTexture() ) ||
			( verticalTexture   != null && verticalTexture  .isTexture() ) ||
			( horizontalTexture != null && horizontalTexture.isTexture() );
			
		
		/*
		 * Create vertices;
		 */
		float[] vertices =
		{
			0  , 0  , 0  , // 0
			dx , 0  , 0  , // 1
			dx , dy , 0  , // 2
			0  , dy , 0  , // 3
			0  , 0  , dz , // 4
			dx , 0  , dz , // 5
			dx , dy , dz , // 6
			0  , dy , dz   // 7
		};
		
		xform.transform( vertices , vertices , 8 );

		/*
		 * Create faces
		 */
		int[][] faceVert =
		{
			{ 0 , 4 , 5 , 1 } , // 0 - front
			{ 2 , 6 , 7 , 3 } , // 1 - back
			{ 1 , 5 , 6 , 2 } , // 2 - right
			{ 3 , 7 , 4 , 0 } , // 3 - left
			{ 4 , 7 , 6 , 5 } , // 4 - top
			{ 1 , 2 , 3 , 0 }   // 5 - bottom
		};
		
		TextureSpec[] faceMat =
		{
			frontalTexture ,
			frontalTexture ,
			verticalTexture ,
			verticalTexture ,
			horizontalTexture ,
			horizontalTexture
		};

		int[][] faceTU = null;
		int[][] faceTV = null;
		if ( hasTextureImage )
		{
			faceTU = new int[ 6 ][];
			faceTV = new int[ 6 ][];
			
			generateTexture( frontalTexture , flipFrontal , 0 , 
				faceTU , xform.xo , xform.xo + dx ,
				faceTV , xform.zo , xform.zo + dz );

			generateTexture( verticalTexture , flipVertical , 2 , 
				faceTU , xform.yo , xform.yo + dy ,
				faceTV , xform.zo , xform.zo + dz );

			generateTexture( horizontalTexture , flipHorizontal , 4 , 
				faceTU , xform.xo , xform.xo + dx , 
				faceTV , xform.yo , xform.yo + dy );
		}

		/*
		 * Set Object3D properties.
		 */
		set( vertices , faceVert , faceMat , faceTU , faceTV , false );
	}

	/**
	 * Generate texture coordinates for the specified face.
	 *
	 * @param	texture		Texture to use.
	 * @param	flipUV		<code>true</code> te reverse meaning of u and v.
	 * @param	index		Index of face.
	 * @param	faceTU		Target array to store U texture coordinates.
	 * @param	tu1			Start U coordinate.
	 * @param	tu2			End U coordinate.
	 * @param	faceTV		Target array to store V texture coordinates.
	 * @param	tv1			Start V coordinate.
	 * @param	tv2			End V coordinate.
	 */
	public void generateTexture( 
		final TextureSpec texture , final boolean flipUV , final int index , 
		final int[][] faceTU , final float tu1 , final float tu2 , 
		final int[][] faceTV , final float tv1 , final float tv2 )
	{
		/*
		 * Return immediately if texture specification is invalid or does not
		 * specify a texture image.
		 */
		if ( texture == null || !texture.isTexture() )
			return;

		int i1,i2,t,m;
		int[][] a;
		float s = texture.textureScale;

		/*
		 * Translate the U1,U2 texture coordinates to the lowest possible (but
		 * positive) range. If either is negative, translation occurs in positive
		 * direction; if both are beyond the maximum value, translation occurs in
		 * negative direction.
		 */
		i1 = (int)( tu1 * s /*+ 0.5f*/ );
		i2 = (int)( tu2 * s /*+ 0.5f*/ );
		
		m = flipUV ? texture.getTextureHeight() : texture.getTextureWidth();
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

		m = flipUV ? texture.getTextureWidth() : texture.getTextureHeight();
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
