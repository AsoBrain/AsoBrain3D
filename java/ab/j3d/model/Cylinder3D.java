/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2005 Peter S. Heijnen
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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

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
	public final double height;

	/**
	 * Radius at top (z=height).
	 */
	public final double radiusTop;

	/**
	 * Radius at bottom (z=0).
	 */
	public final double radiusBottom;

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
	public Cylinder3D( final Matrix3D xform , final double radiusBottom , final double radiusTop , final double height , final int numEdges , final TextureSpec texture , final boolean smoothCircumference , final boolean smoothCaps )
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
	public void generate( final int numEdges , final TextureSpec texture , final boolean smoothCircumference , final boolean smoothCaps )
	{
		/*
		 * Setup properties of cylinder.
		 */
		final boolean  hasBottom   = ( radiusBottom > 0 );
		final boolean  hasTop      = ( radiusTop > 0 );
		final int      pointCount  = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final double[] pointCoords = new double[ pointCount * 3 ];

		/*
		 * Generate vertices.
		 */
		int v = 0;

		if ( hasBottom )
		{
			final double step = 2.0 * Math.PI / (double)numEdges;

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				final double a = (double)i * step;
				pointCoords[ v++ ] =  Math.sin( a ) * radiusBottom;
				pointCoords[ v++ ] = -Math.cos( a ) * radiusBottom;
				pointCoords[ v++ ] = 0.0;
			}
		}
		else
		{
			pointCoords[ v++ ] = 0.0;
			pointCoords[ v++ ] = 0.0;
			pointCoords[ v++ ] = 0.0;
		}

		if ( hasTop )
		{
			final double step = 2.0 * Math.PI / (double)numEdges;

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				final double a = (double)i * step;
				pointCoords[ v++ ] =  Math.sin( a ) * radiusTop;
				pointCoords[ v++ ] = -Math.cos( a ) * radiusTop;
				pointCoords[ v++ ] = height;
			}
		}
		else
		{
			pointCoords[ v++ ] = 0.0;
			pointCoords[ v++ ] = 0.0;
			pointCoords[ v/*++*/ ] = height;
		}

		clear();
		setPointCoords( xform.transform( pointCoords , pointCoords , pointCount ) );

		/*
		 * Bottom face (if it exists).
		 */
		if ( hasBottom )
		{
			final int[] fv = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
				fv[ i ] = i;

			addFace( fv , texture , smoothCaps );
		}

		/*
		 * Circumference.
		 */
		if ( hasBottom && hasTop )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int   i2 = ( i1 + 1 ) % numEdges;
				final int[] fv = { i2 , i1 , numEdges + i1 , numEdges + i2 };

				addFace( fv , texture , smoothCircumference );
			}
		}
		else if ( hasBottom )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int   i2 = ( i1 + 1 ) % numEdges;
				final int[] fv = { i2 , i1 , numEdges };

				addFace( fv , texture , smoothCircumference );
			}
		}
		else if ( hasTop )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int   i2 = ( i1 + 1 ) % numEdges;
				final int[] fv = { 0 , 1 + i1 , 1 + i2 };

				addFace( fv , texture , smoothCircumference );
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

			addFace( fv , texture , smoothCaps );
		}
	}

	public void paint( final Graphics2D g , final Matrix3D gTransform , final Matrix3D viewTransform , final Paint outlinePaint , final Paint fillPaint , final float shadeFactor )
	{
		final Matrix3D viewBase = xform.multiply( viewTransform );
		final double   h        = height;
		final double   rBottom  = radiusBottom;
		final double   rTop     = radiusTop;

		final double zz = viewBase.zz;
		final double xz = viewBase.xz;
		final double yz = viewBase.yz;
		final double xo = viewBase.xo;
		final double yo = viewBase.yo;
		final double zo = viewBase.zo;

		final float goldenRatio = 0.6180339f;

		/*
		 * The cylinder's center axis (Z-axis) is is parallel on the view plane
		 * (on the XY / Z=0 plane).
		 *
		 * We can can only see the outline of the cylinder (trapezoid).
		 */
		if ( Matrix3D.almostEqual( zz , 0.0 ) )
		{
			// (xz,yz) = direction of cylinder Z-axis in XY plane
			// (xo,yo,zo) = view coordinate of cylinder bottom centeroid

			final double p1x = xo  + yz * rBottom;       // p1 = bottom left,
			final double p1y = yo  - xz * rBottom;
			final double p2x = xo  + yz * rTop + xz * h; // p2 = top left
			final double p2y = yo  - xz * rTop + yz * h;
			final double p3x = xo  - yz * rTop + xz * h; // p3 = top right
			final double p3y = yo  + xz * rTop + yz * h;
			final double p4x = xo  - yz * rBottom;       // p4 = bottom right
			final double p4y = yo  + xz * rBottom;

			/*
			 * Project and draw trapezoid.
			 */
			final float x1 = (float)gTransform.transformX( p1x , p1y , zo );
			final float y1 = (float)gTransform.transformY( p1x , p1y , zo );
			final float x2 = (float)gTransform.transformX( p2x , p2y , zo );
			final float y2 = (float)gTransform.transformY( p2x , p2y , zo );
			final float x3 = (float)gTransform.transformX( p3x , p3y , zo );
			final float y3 = (float)gTransform.transformY( p3x , p3y , zo );
			final float x4 = (float)gTransform.transformX( p4x , p4y , zo );
			final float y4 = (float)gTransform.transformY( p4x , p4y , zo );

			final GeneralPath path = new GeneralPath( GeneralPath.WIND_EVEN_ODD , 5 );
			path.moveTo( x1 , y1 );
			path.lineTo( x2 , y2 );
			path.lineTo( x3 , y3 );
			path.lineTo( x4 , y4 );
			path.closePath();

			if ( fillPaint != null )
			{
				if ( ( shadeFactor >= 0.1 ) && ( shadeFactor <= 1.0 ) && ( fillPaint instanceof Color ) && ( outlinePaint instanceof Color ) )
				{
					final float highlightX = ( 1.0f - goldenRatio ) * x1 + goldenRatio * x4;
					final float highlightY = ( 1.0f - goldenRatio ) * y1 + goldenRatio * y4;
					g.setPaint( new GradientPaint( highlightX , highlightY , (Color)fillPaint , x1 , y1 , (Color)outlinePaint , true ) );
				}
				else
				{
					g.setPaint( fillPaint );
				}
				g.fill( path );
			}

			if ( outlinePaint != null )
			{
				g.setPaint( outlinePaint );
				g.draw( path );
			}
		}
		/*
		 * Viewing along Z-axis. We can see, the bottom and/or top cap and the
		 * area between the two.
		 */
		else if ( Matrix3D.almostEqual( xz , 0.0 )
		       && Matrix3D.almostEqual( yz , 0.0 ) )
		{
			final Matrix3D combinedTransform = viewBase.multiply( gTransform );

			final float x         = (float)combinedTransform.xo;
			final float y         = (float)combinedTransform.yo;
			final float botZ      = (float)combinedTransform.zo;
			final float botRadius;
			{
				final double dx = combinedTransform.xx * rBottom;
				final double dy = combinedTransform.yx * rBottom;
				botRadius = (float)Math.sqrt( dx * dx + dy * dy );
			}

			final Ellipse2D bot = Matrix3D.almostEqual( (double)botRadius , 0.0 ) ? null : new Ellipse2D.Float( x - botRadius , y - botRadius , 2.0f * botRadius , 2.0f * botRadius );

			final float topZ = (float)combinedTransform.transformZ( 0.0 , 0.0 , h );
			final float topRadius;
			{
				final double dx = rTop * combinedTransform.xx + h * combinedTransform.xz;
				final double dy = rTop * combinedTransform.yx + h * combinedTransform.yz;
				topRadius = (float)Math.sqrt( dx * dx + dy * dy );
			}

			final Ellipse2D top = Matrix3D.almostEqual( (double)topRadius , 0.0 ) ? null : new Ellipse2D.Float( x - topRadius , y - topRadius , 2.0f * topRadius , 2.0f * topRadius );

			if ( ( bot != null ) || ( top != null ) )
			{
				final Shape shape1;
				final Shape shape2;
				if ( top == null )
				{
					shape1 = bot;
					shape2 = null;
				}
				else if ( bot == null )
				{
					shape1 = top;
					shape2 = null;
				}
				else if ( topZ >= botZ )
				{
					shape1 = bot;
					shape2 = top;
				}
				else
				{
					shape1 = top;
					shape2 = bot;
				}

				final Paint paint;
				if ( fillPaint != null )
				{
					if ( ( shadeFactor >= 0.1 ) && ( shadeFactor <= 1.0 ) && ( fillPaint instanceof Color ) && ( outlinePaint instanceof Color ))
					{
						final float r = Math.max( topRadius , botRadius );
						final float highlight = ( goldenRatio - 0.5f ) * r;
						paint = new GradientPaint( x + highlight , y - highlight , (Color)fillPaint , x -r , y + r , (Color)outlinePaint , true );
					}
					else
					{
						paint = fillPaint;
					}
					g.setPaint( paint );
					g.fill( shape1 );
				}
				else
				{
					paint = null;
				}

				if ( outlinePaint != null )
				{
					g.setPaint( outlinePaint );
					g.draw( shape1 );
				}

				if ( shape2 != null )
				{
					if ( paint != null )
					{
						g.setPaint( paint );
						g.fill( shape2 );
					}

					if ( outlinePaint != null )
					{
						g.setPaint( outlinePaint );
						g.draw( shape2 );
					}
				}
			}

//			rby = rby < 0 ? -rby : rby;
//
//			if ( rtx != 0 && rty != 0 )
//				g.drawOval( (x - (rtx / 2) ) , (y - (rty / 2) ) , rtx , rty );
//
//			if ( rbx != 0 && rby != 0 )
//				g.drawOval( (x - (rbx / 2) ) , (y - (rby / 2) ) , rbx , rby );
		}
		/*
		 * @FIXME implement optimized painting with two lines and two ellipses.
		 *
		 * Overige situaties:
		 * 1) Z-as loop parallel aan XZ of YZ vlak (YZ resp. XZ is 0) => teken combinatie van ellips + 2 lijnen + halve ellips.
		 * 2) Overig => Zelfde als 1), alleen moet je wel een Java2D shape maken en deze transformeren om hem te kunnen tekenen...
		 * Strikt genomen is natuurlijk alles terug te voeren op 1)
		 */
		else
		{
			// Not painted, paint fully.
			super.paint( g , gTransform , viewTransform , outlinePaint , fillPaint , shadeFactor );
		}
	}
}
