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

import java.awt.Color;
import java.awt.Graphics;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;

/**
 * This class defines a 3D cylinder with optionally different radi for the caps. Using
 * these radi, this class may also be used to construct cones.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
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
	 * @param   xform               Transform to apply to the cylinder's vertices.
	 * @param   radiusBottom        Radius at bottom (z=0).
	 * @param   radiusTop           Radius at top (z=height).
	 * @param   height              Height of cylinder (z-axis).
	 * @param   numEdges            Number of edges to approximate circle (minimum: 3).
	 * @param   texture             Texture of cylinder.
	 * @param   smoothCircumference Apply smoothing to circumference of cylinder.
	 * @param   smoothCaps          Apply smoothing to caps of cylinder.
	 */
	public Cylinder3D( final Matrix3D xform , final float radiusBottom , final float radiusTop , final float height , final int numEdges , final TextureSpec texture , final boolean smoothCircumference , final boolean smoothCaps )
	{
		if ( radiusBottom < 0 || radiusTop < 0 || height < 0 || numEdges < 3 )
			throw new IllegalArgumentException( "inacceptable arguments to Cylinder constructor (height=" + height + ", text=" + texture + ')' );

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
	 * @param   numEdges            Number of edges to approximate circle (minimum: 3).
	 * @param   texture             Texture of cylinder.
	 * @param   smoothCircumference Apply smoothing to circumference of cylinder.
	 * @param   smoothCaps          Apply smoothing to caps of cylinder.
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

	public void paint( final Graphics g , final Matrix3D gXform , final Matrix3D viewTransform , final Color outlineColor , final Color fillColor , final float shadeFactor )
	{
		final Matrix3D mat  = xform.multiply( viewTransform );
		final Matrix3D mat2 = xform.multiply( viewTransform ).multiply( gXform );

		final boolean topViewDrawCircle   = Matrix3D.almostEqual( mat.xz , 0 ) && Matrix3D.almostEqual( mat.yz , 0 ) && ( Matrix3D.almostEqual(  mat.zz , 1 ) || Matrix3D.almostEqual( -mat.zz , 1 ) );
		final boolean frontViewDrawCircle = Matrix3D.almostEqual( mat.xz , 0 ) && Matrix3D.almostEqual( mat.zz , 0 ) &&   Matrix3D.almostEqual( -mat.yz , 1 );

		/*
		 * In top- or in front-view a circle must be drawn.
		 */
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

		/*
		 * If no circle could be drawn, draw the outlines of the cylinder.
		 */
		float[] pts = null;

		// Top views (look in direction of z-axis).
		if ( mat2.xx < 0 && mat2.yz < 0 )
		{
			pts = new float[] { -radiusBottom , 0      , radiusBottom , 0      ,
			                    -radiusTop    , height , radiusTop    , height };
		}

		if ( mat2.xx > 0 && mat2.yz > 0 )
		{
			pts = new float[] { -radiusBottom , 0       , radiusBottom , 0       ,
			                    -radiusTop    , -height , radiusTop    , -height };
		}

		if ( mat2.xz > 0 && mat2.yx < 0 )
		{
			pts = new float[] { 0      , -radiusBottom , 0      , radiusBottom ,
			                    height , -radiusTop    , height , radiusTop    };
		}

		if ( mat2.xz < 0 && mat2.yx > 0 )
		{
			pts = new float[] { 0       , -radiusBottom , 0       , radiusBottom ,
			                    -height , -radiusTop    , -height , radiusTop    };
		}

		final boolean topView = ( pts != null && pts.length == 8 );

		// Front views.
		if ( mat2.xx > 0 && mat2.yz < 0 )
		{
			pts = new float[] { -radiusBottom , 0      , radiusBottom , 0      ,
			                    -radiusTop    , height , radiusTop    , height };
		}

		if ( mat2.xx < 0 && mat2.yz > 0 )
		{
			pts = new float[] { -radiusBottom , 0       , radiusBottom , 0       ,
			                    -radiusTop    , -height , radiusTop    , -height };
		}

		if ( mat2.xz > 0 && mat2.yy < 0 )
		{
			pts = new float[] { 0      , -radiusBottom , 0      , radiusBottom ,
			                    height , -radiusTop    , height , radiusTop    };
		}

		if ( mat2.xz < 0 && mat2.yy < 0 )
		{
			pts = new float[] { 0       , -radiusBottom , 0       , radiusBottom ,
			                    -height , -radiusTop    , -height , radiusTop    };
		}

		final boolean frontView = ( !topView && pts != null && pts.length == 8 );

		/*
		 * If the points are filled, draw them.
		 */
		if ( topView || frontView )
		{
			final float x = viewTransform.transformX( xform.xo , xform.yo , xform.zo );
			final float y = topView ? viewTransform.transformY( xform.xo , xform.yo , xform.zo ) :
			                          viewTransform.transformZ( xform.xo , xform.yo , xform.zo ) ;

			final float x1 = x + pts[ 0 ];
			final float x2 = x + pts[ 2 ];
			final float x3 = x + pts[ 4 ];
			final float x4 = x + pts[ 6 ];
			final float y1 = y + pts[ 1 ];
			final float y2 = y + pts[ 3 ];
			final float y3 = y + pts[ 5 ];
			final float y4 = y + pts[ 7 ];

			int p1x = 0;
			int p2x = 0;
			int p3x = 0;
			int p4x = 0;
			int p1y = 0;
			int p2y = 0;
			int p3y = 0;
			int p4y = 0;

			if ( topView )
			{
				p1x = (int)gXform.transformX( x1 , y1 , 0 );
				p2x = (int)gXform.transformX( x2 , y2 , 0 );
				p3x = (int)gXform.transformX( x3 , y3 , 0 );
				p4x = (int)gXform.transformX( x4 , y4 , 0 );
				p1y = (int)gXform.transformY( x1 , y1 , 0 );
				p2y = (int)gXform.transformY( x2 , y2 , 0 );
				p3y = (int)gXform.transformY( x3 , y3 , 0 );
				p4y = (int)gXform.transformY( x4 , y4 , 0 );
			}
			else if ( frontView )
			{
				p1x = (int)gXform.transformX( x1 , 0 ,y1 );
				p2x = (int)gXform.transformX( x2 , 0 ,y2 );
				p3x = (int)gXform.transformX( x3 , 0 ,y3 );
				p4x = (int)gXform.transformX( x4 , 0 ,y4 );
				p1y = (int)gXform.transformY( x1 , 0 ,y1 );
				p2y = (int)gXform.transformY( x2 , 0 ,y2 );
				p3y = (int)gXform.transformY( x3 , 0 ,y3 );
				p4y = (int)gXform.transformY( x4 , 0 ,y4 );
			}

			if ( fillColor != null )
			{
				g.setColor( fillColor );
				g.fillPolygon( new int[]{ p1x , p2x , p3x , p4x , p1x , p3x , p2x , p4x } ,
				               new int[]{ p1y , p2y , p3y , p4y , p1y , p3y , p2y , p4y } , 8 );
			}

			if ( outlineColor != null )
			{
				g.setColor( outlineColor );
				g.drawLine( p1x , p1y , p2x , p2y );
				g.drawLine( p3x , p3y , p4x , p4y );
				g.drawLine( p1x , p1y , p3x , p3y );
				g.drawLine( p2x , p2y , p4x , p4y );
			}
		}
		else
		{
			// Not painted, paint fully.
			super.paint( g , gXform , viewTransform , outlineColor , fillColor , shadeFactor );
		}
	}
}