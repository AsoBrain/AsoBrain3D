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
import java.awt.Color;

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

	/**
	 * Paint 2D representation of this 3D object. The object coordinates are
	 * transformed using the objXform argument. By default, the object is painted
	 * by drawing the outlines of its 'visible' faces. Derivatives of this class
	 * may implement are more realistic approach (sphere, cylinder).
	 * <p/>
	 * Objects are painted on the specified graphics context after being
	 * transformed again by gXform. This may be used to pan/scale the object on the
	 * graphics context (NOTE: IT MAY NOT ROTATE THE OBJECT!).
	 *
	 * @param g        Graphics context.
	 * @param gXform   Transformation to pan/scale the graphics context.
	 * @param objXform Transformation from object's to view coordinate system.
	 */
	public void paint( final Graphics g , final Matrix3D gXform , final Matrix3D objXform )
	{
		final Matrix3D mat  = xform.multiply( objXform );
		final Matrix3D mat2 = xform.multiply( objXform ).multiply( gXform );

		final boolean topViewDrawCircle   = Matrix3D.almostEqual( mat.xz , 0 ) && Matrix3D.almostEqual( mat.yz , 0 ) && ( Matrix3D.almostEqual(  mat.zz , 1 ) || Matrix3D.almostEqual( -mat.zz , 1 ) );
		final boolean frontViewDrawCircle = Matrix3D.almostEqual( mat.xz , 0 ) && Matrix3D.almostEqual( mat.zz , 0 ) &&   Matrix3D.almostEqual( -mat.yz , 1 );

		if ( topViewDrawCircle || frontViewDrawCircle )
		{
			final int x = (int)( mat2.xo );
			final int y = (int)( mat2.yo );

			int rtx = (int)( radiusTop    * 2 * gXform.xx );
			int rbx = (int)( radiusBottom * 2 * gXform.xx );
			int rty = (int)( radiusTop    * 2 * ( topViewDrawCircle ? gXform.yy : gXform.yz ) );
			int rby = (int)( radiusBottom * 2 * ( topViewDrawCircle ? gXform.yy : gXform.yz ) );

			rtx = rtx < 0 ? -rtx : rtx;
			rbx = rbx < 0 ? -rbx : rbx;
			rty = rty < 0 ? -rty : rty;
			rby = rby < 0 ? -rby : rby;

			if ( rtx != 0 && rty != 0 )
				g.drawOval( (x - (rtx / 2) ) , (y - (rty / 2) ) , rtx , rty );

			if ( rbx != 0 && rby != 0 )
				g.drawOval( (x - (rbx / 2) ) , (y - (rby / 2) ) , rbx , rby );
		}

		final float x = objXform.transformX( xform.xo , xform.yo , xform.zo );
		final float y = objXform.transformY( xform.xo , xform.yo , xform.zo );
		final float z = objXform.transformZ( xform.xo , xform.yo , xform.zo );

		/*
		 * Top views (look in direction of z-axis).
		 */
		float[] topViewPts = null;

		if ( mat2.xx < 0 && mat2.yz < 0 )
		{
			topViewPts = new float[] { -radiusBottom , 0      , radiusBottom , 0      ,
									   -radiusTop    , height , radiusTop    , height };
		}

		if ( mat2.xx > 0 && mat2.yz > 0 )
		{
			topViewPts = new float[] { -radiusBottom , 0       , radiusBottom , 0       ,
									   -radiusTop    , -height , radiusTop    , -height };
		}

		if ( mat2.xz > 0 && mat2.yx < 0 )
		{
			topViewPts = new float[] { 0      , -radiusBottom , 0      , radiusBottom ,
									   height , -radiusTop    , height , radiusTop    };
		}

		if ( mat2.xz < 0 && mat2.yx > 0 )
		{
			topViewPts = new float[] { 0       , -radiusBottom , 0       , radiusBottom ,
									   -height , -radiusTop    , -height , radiusTop    };
		}

		/*
		 * Front views.
		 */
		float[] frontViewPts = null;

		if ( mat2.xx > 0 && mat2.yz < 0 )
		{
			frontViewPts = new float[] { -radiusBottom , 0      , radiusBottom , 0      ,
										 -radiusTop    , height , radiusTop    , height };
		}

		if ( mat2.xx < 0 && mat2.yz > 0 )
		{
			frontViewPts = new float[] { -radiusBottom , 0       , radiusBottom , 0       ,
										 -radiusTop    , -height , radiusTop    , -height };
		}

		if ( mat2.xz > 0 && mat2.yy < 0 )
		{
			frontViewPts = new float[] { 0      , -radiusBottom , 0      , radiusBottom ,
										 height , -radiusTop    , height , radiusTop    };
		}

		if ( mat2.xz < 0 && mat2.yy < 0 )
		{
			frontViewPts = new float[] { 0       , -radiusBottom , 0       , radiusBottom ,
										 -height , -radiusTop    , -height , radiusTop    };
		}

		/*
		 * If the points are filled, draw them.
		 */
		if ( topViewPts != null && topViewPts.length == 8 )
		{
			drawLine( g , gXform , x + topViewPts[ 0 ] , y + topViewPts[ 1 ] , 0 , x + topViewPts[ 2 ] , y + topViewPts[ 3 ] , 0 );
			drawLine( g , gXform , x + topViewPts[ 4 ] , y + topViewPts[ 5 ] , 0 , x + topViewPts[ 6 ] , y + topViewPts[ 7 ] , 0 );
			drawLine( g , gXform , x + topViewPts[ 0 ] , y + topViewPts[ 1 ] , 0 , x + topViewPts[ 4 ] , y + topViewPts[ 5 ] , 0 );
			drawLine( g , gXform , x + topViewPts[ 2 ] , y + topViewPts[ 3 ] , 0 , x + topViewPts[ 6 ] , y + topViewPts[ 7 ] , 0 );
		}
		else if ( frontViewPts != null && frontViewPts.length == 8 )
		{
			drawLine( g , gXform , x + frontViewPts[ 0 ] , 0 , z + frontViewPts[ 1 ] , x + frontViewPts[ 2 ] , 0 , z + frontViewPts[ 3 ] );
			drawLine( g , gXform , x + frontViewPts[ 4 ] , 0 , z + frontViewPts[ 5 ] , x + frontViewPts[ 6 ] , 0 , z + frontViewPts[ 7 ] );
			drawLine( g , gXform , x + frontViewPts[ 0 ] , 0 , z + frontViewPts[ 1 ] , x + frontViewPts[ 4 ] , 0 , z + frontViewPts[ 5 ] );
			drawLine( g , gXform , x + frontViewPts[ 2 ] , 0 , z + frontViewPts[ 3 ] , x + frontViewPts[ 6 ] , 0 , z + frontViewPts[ 7 ] );
		}
		else
		{
			// Not painted, paint fully.
			super.paint( g , gXform , objXform );
		}
	}

	/**
	 * Draws a line between the specified coordinates. Coordinates are transformed
	 * using the specified matrix.
	 *
	 * @param	g		Graphics context.
	 * @param	gXform 	Transform for coordinates on graphics context.
	 * @param	x1		X coordinate of line's start point.
	 * @param	y1		Y coordinate of line's start point.
	 * @param	z1		Y coordinate of line's start point in front view (y and z are switched).
	 * @param	x2		X coordinate of line's end point.
	 * @param	y2		Y coordinate of line's end point.
	 * @param	z2		Y coordinate of line's end point in front view (y and z are switched).
	 */
	public static void drawLine( final Graphics g , final Matrix3D gXform , final float x1 , final float y1 , final float z1 , final float x2 , final float y2 , final float z2 )
	{
		g.drawLine( (int)gXform.transformX( x1 , y1 , z1 ) ,
		            (int)gXform.transformY( x1 , y1 , z1 ) ,
		            (int)gXform.transformX( x2 , y2 , z2 ) ,
		            (int)gXform.transformY( x2 , y2 , z2 ) );
	}
}