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
import java.awt.Graphics;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;

/**
 * This class defines a 3D cylinder with optionally different radi for the caps. Using
 * these radi, this class may also be used to construct cones.
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class Cylinder3D
	extends Object3D
{
	/**
	 * Transformation applied to all vertices of the box.
	 */
	public final Matrix3D xform;

	/**
	 * Height of cylinder (z-axis).
	 */
	public final float   height;

	/**
	 * Radius at top (z=height).
	 */
	public final float   radiusTop;

	/**
	 * Radius at bottom (z=0).
	 */
	public final float   radiusBottom;

	/**
	 * Constructor for cylinder object. Radius of top or bottom may be set to 0 to create
	 * a cone.
	 *
	 * @param	xform				Transform to apply to the cylinder's vertices.
	 * @param	radiusBottom		Radius at bottom (z=0).
	 * @param	radiusTop			Radius at top (z=height).
	 * @param	height				Height of cylinder (z-axis).
	 * @param	numEdges			Number of edges to approximate circle (minimum: 3).
	 * @param	texture				Texture of cylinder.
	 * @param	smoothCircumference	Apply smoothing to circumference of cylinder.
	 * @param	smoothCaps			Apply smoothing to caps of cylinder.
	 */
	public Cylinder3D( final Matrix3D xform , final float radiusBottom , final float radiusTop , final float height , final int numEdges , final TextureSpec texture , final boolean smoothCircumference , final boolean smoothCaps )
	{
		if ( radiusBottom < 0 || radiusTop < 0 || height < 0 || numEdges < 3 )
			throw new IllegalArgumentException( "inacceptable arguments to Cylinder constructor (height=" + height + ", text=" + texture + ")" );

		if ( radiusBottom == 0 && radiusTop == 0 )
			throw new IllegalArgumentException( "radius of bottom or top of cylinder must be non-zero" );

		this.xform        = xform;
		this.radiusTop    = radiusTop;
		this.radiusBottom = radiusBottom;
		this.height       = height;

		generate( numEdges , texture , smoothCircumference , smoothCaps );
	}

	/**
	 * Generate Object3D properties for cylinder.
	 *
	 * @param	numEdges			Number of edges to approximate circle (minimum: 3).
	 * @param	texture				Texture of cylinder.
	 * @param	smoothCircumference	Apply smoothing to circumference of cylinder.
	 * @param	smoothCaps			Apply smoothing to caps of cylinder.
	 */
	public void generate( final int numEdges , TextureSpec texture , final boolean smoothCircumference , final boolean smoothCaps )
	{
		if ( texture == null )
			texture = new TextureSpec();

		/*
		 * Setup properties of cylinder.
		 */
		final boolean       hasBottom   = ( radiusBottom > 0 );
		final boolean       hasTop      = ( radiusTop > 0 );
		final int           vertexCount = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final int           faceCount   = ( hasBottom ? 1 : 0 ) + numEdges + ( hasTop ? 1 : 0 );
		final float[]       vertices    = new float[ vertexCount * 3 ];
		final int[][]       faceVert    = new int  [ faceCount ][];
		final TextureSpec[] faceMat     = new TextureSpec[ faceCount ];
		final boolean[]     faceSmooth  = new boolean[ faceCount ];

		/*
		 * Generate vertices.
		 */
		int v = 0;

		if ( hasBottom )
		{
			for ( int i = 0 ; i < numEdges ; i++ )
			{
				final float a = (float)( i * 2 * Math.PI / numEdges );

				vertices[ v++ ] =  (float)( Math.sin( a ) * radiusBottom );
				vertices[ v++ ] = -(float)( Math.cos( a ) * radiusBottom );
				vertices[ v++ ] = 0;
			}
		}
		else
		{
			vertices[ v++ ] = 0;
			vertices[ v++ ] = 0;
			vertices[ v++ ] = 0;
		}

		if ( hasTop )
		{
			for ( int i = 0 ; i < numEdges ; i++ )
			{
				final float a = (float)( i * 2 * Math.PI / numEdges );

				vertices[ v++ ] =  (float)( Math.sin( a ) * radiusTop );
				vertices[ v++ ] = -(float)( Math.cos( a ) * radiusTop );
				vertices[ v++ ] = height;
			}
		}
		else
		{
			vertices[ v/*++*/ ] = 0;
			vertices[ v/*++*/ ] = 0;
			vertices[ v/*++*/ ] = height;
		}

		xform.transform( vertices , vertices , vertices.length / 3 );

		/*
		 * Construct faces
		 */
		int f = 0;

		/*
		 * Bottom face (if it exists).
		 */
		if ( hasBottom )
		{
			final int[] fv = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
				fv[ i ] = i;

			faceVert  [ f   ] = fv;
			faceMat   [ f   ] = texture;
			faceSmooth[ f++ ] = smoothCaps;
		}

		/*
		 * Circumference.
		 */
		if ( hasBottom && hasTop )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int i2 = ( i1 + 1 ) % numEdges;
				faceVert  [ f   ] = new int[] { i2 , i1 , numEdges + i1 , numEdges + i2 };
				faceMat   [ f   ] = texture;
				faceSmooth[ f++ ] = smoothCircumference;
			}
		}
		else if ( hasBottom )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int i2 = ( i1 + 1 ) % numEdges;
				faceVert  [ f   ] = new int[] { i2 , i1 , numEdges };
				faceMat   [ f   ] = texture;
				faceSmooth[ f++ ] = smoothCircumference;
			}
		}
		else if ( hasTop )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int i2 = ( i1 + 1 ) % numEdges;

				faceVert  [ f   ] = new int[] { 0 , 1 + i1 , 1 + i2 };
				faceMat   [ f   ] = texture;
				faceSmooth[ f++ ] = smoothCircumference;
			}
		}

		/*
		 * Top face (if it exists).
		 */
		if ( hasTop )
		{
			final int[] fv = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
				fv[ i ] = ( hasBottom ? numEdges : 1 ) + ( numEdges - i - 1 );

			faceVert  [ f ] = fv;
			faceMat   [ f   ] = texture;
			faceSmooth[ f/*++*/ ] = smoothCaps;
		}

		/*
		 * Set Object3D properties.
		 */
		set( vertices ,  faceVert , faceMat , null , null , null , faceSmooth );
	}
}
