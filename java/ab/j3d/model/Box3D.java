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
package ab.j3d.model;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;

/**
 * This class defines a 3D box.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
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
	private final double _dx;

	/**
	 * Height of box (y-axis).
	 */
	private final double _dy;

	/**
	 * Depth of box (z-axis).
	 */
	private final double _dz;

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
	public Box3D( final Matrix3D xform , final double dx , final double dy , final double dz , final TextureSpec mainTexture , final TextureSpec sideTexture )
	{
		_xform = xform;
		_dx    = dx;
		_dy    = dy;
		_dz    = dz;

		/*
		 * Create point coordinates.
		 */
		final double[] pointCoords =
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

		setPointCoords( xform.transform( pointCoords , pointCoords , 8 ) );

		/*
		 * Set frontal, vertical, and horizontal face properties.
		 */
		final double ax = Math.abs( dx );
		final double ay = Math.abs( dy );
		final double az = Math.abs( dz );

		final boolean isFrontal    = ( ay < ax && ay < az );
		final boolean isVertical   = ( ax < ay && ax < az );
		final boolean isHorizontal = ( az < ax && az < ay );

		TextureSpec texture;
		final int[][] textureU = new int[ 2 ][];
		final int[][] textureV = new int[ 2 ][];

		/*
		 * Add front/back face
		 */
		texture = isFrontal ? mainTexture : sideTexture;

		if ( ( texture != null ) && texture.isTexture() )
			setTexture( texture , isHorizontal , 0 , textureU , xform.xo , xform.xo + _dx , textureV , xform.zo , xform.zo + _dz );

		addFace( new int[] { 0 , 4 , 5 , 1 } , texture , textureU[ 0 ] , textureV[ 0 ] , 1.0f , false );
		addFace( new int[] { 2 , 6 , 7 , 3 } , texture , textureU[ 1 ] , textureV[ 1 ] , 1.0f , false );

		/*
		 * Add right/left face
		 */
		texture = isVertical ? mainTexture : sideTexture;
		if ( ( texture != null ) && texture.isTexture() )
			setTexture( texture , isHorizontal , 0 , textureU , xform.yo , xform.yo + _dy , textureV , xform.zo , xform.zo + _dz );

		addFace( new int[] { 1 , 5 , 6 , 2 } , texture , textureU[ 0 ] , textureV[ 0 ] , 1.0f , false );
		addFace( new int[] { 3 , 7 , 4 , 0 } , texture , textureU[ 1 ] , textureV[ 1 ] , 1.0f , false );

		/*
		 * Add top/bottom face
		 */
		texture = isHorizontal ? mainTexture : sideTexture;
		if ( ( texture != null ) && texture.isTexture() )
			setTexture( texture , !isVertical , 0 , textureU , xform.xo , xform.xo + _dx , textureV , xform.yo , xform.yo + _dy );

		addFace( new int[] { 4 , 7 , 6 , 5 } , texture , textureU[ 0 ] , textureV[ 0 ] , 1.0f , false );
		addFace( new int[] { 1 , 2 , 3 , 0 } , texture , textureU[ 1 ] , textureV[ 1 ] , 1.0f , false );
	}

	/**
	 * Get width of box (x-axis).
	 *
	 * @return  Width of box (x-axis).
	 */
	public double getDX()
	{
		return _dx;
	}

	/**
	 * Get height of box (y-axis).
	 *
	 * @return  Height of box (y-axis).
	 */
	public double getDY()
	{
		return _dy;
	}

	/**
	 * Get depth of box (z-axis).
	 *
	 * @return  Depth of box (z-axis).
	 */
	public double getDZ()
	{
		return _dz;
	}

	/**
	 * Get transformation applied to all vertices of the box.
	 *
	 * @return  Transformation applied to all vertices of the box.
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
		final int[][] faceTU , final double tu1 , final double tu2 ,
		final int[][] faceTV , final double tv1 , final double tv2 )
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
		final double s = texture.textureScale;

		/*
		 * Translate the U1,U2 texture coordinates to the lowest possible (but
		 * positive) range. If either is negative, translation occurs in positive
		 * direction; if both are beyond the maximum value, translation occurs in
		 * negative direction.
		 */
		i1 = (int)( tu1 * s /*+ 0.5*/ );
		i2 = (int)( tu2 * s /*+ 0.5*/ );

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
		i1 = (int)( tv1 * s /*+ 0.5*/ );
		i2 = (int)( tv2 * s /*+ 0.5*/ );

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
