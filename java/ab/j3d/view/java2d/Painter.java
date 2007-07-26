/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2006 Peter S. Heijnen
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
package ab.j3d.view.java2d;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Cylinder3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Insert3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Sphere3D;
import ab.j3d.model.Transform3D;
import ab.j3d.view.RenderQueue;
import ab.j3d.view.RenderedPolygon;

/**
 * This class can paint a 3D scene directly to a {@link Graphics2D} context.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Painter
{
	/**
	 * This is used as cache storage for {@link #paintObject}.
	 */
	private static double[] _paintVertexCoordinatesCache;

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private Painter()
	{
	}

	/**
	 * Paint all polygons in the render queue.
	 *
	 * @param   g                   Graphics context to paint on.
	 * @param   renderQueue         Render queue whose contents to paint.
	 * @param   outline             Paint polygon outlines.
	 * @param   fill                Fill polygons (vs. outline only).
	 * @param   applyLighting       Apply lighting effect to filled polygons.
	 * @param   useMaterialColor    Try to apply material properties when filling polygons.
	 */
	public static void paintQueue( final Graphics2D g , final RenderQueue renderQueue , final boolean outline , final boolean fill , final boolean applyLighting , final boolean useMaterialColor )
	{
		final RenderedPolygon[] polygons = renderQueue.getQueuedPolygons();
		paintQueue( g , polygons , outline , fill , applyLighting , useMaterialColor );
	}

	/**
	 * Paint all specified polygons.
	 *
	 * @param   g                   Graphics context to paint on.
	 * @param   polygons            Polygons to paint.
	 * @param   outline             Paint polygon outlines.
	 * @param   fill                Fill polygons (vs. outline only).
	 * @param   applyLighting       Apply lighting effect to filled polygons.
	 * @param   useMaterialColor    Try to apply material properties when filling polygons.
	 */
	public static void paintQueue( final Graphics2D g , final RenderedPolygon[] polygons , final boolean outline , final boolean fill , final boolean applyLighting , final boolean useMaterialColor )
	{
		for ( int i = 0 ; i < polygons.length ; i++ )
		{
			final RenderedPolygon polygon = polygons[ i ];

			paintPolygon( g , polygon , outline , fill , applyLighting , useMaterialColor );
		}
	}

	/**
	 * Paint the specified polygon.
	 *
	 * @param   g                   Graphics context to paint on.
	 * @param   polygon             Polygon to paint.
	 * @param   outline             Paint polygon outlines.
	 * @param   fill                Fill polygons (vs. outline only).
	 * @param   applyLighting       Apply lighting effect to filled polygons.
	 * @param   useMaterialColor    Try to apply material properties when filling polygons.
	 */
	public static void paintPolygon( final Graphics2D g , final RenderedPolygon polygon , final boolean outline , final boolean fill , final boolean applyLighting , final boolean useMaterialColor )
	{
		final Object antiAliasingValue = g.getRenderingHint( RenderingHints.KEY_ANTIALIASING );

		Paint fillPaint = null;
		if ( fill || ( polygon._vertexCount < 3 ) )
		{
			final Material material = polygon._material;

			if ( polygon._alternateAppearance )
			{
				fillPaint = polygon._object.alternateFillPaint;
			}
			else if ( useMaterialColor && ( material != null ) && ( material.colorMap == null ) )
			{
				fillPaint = new Color( material.getARGB() );
			}
			else
			{
				fillPaint = polygon._object.fillPaint;
			}

			if ( fill && applyLighting && ( polygon._object.shadeFactor > 0.01f ) && ( fillPaint instanceof Color ) )
			{
				final float shadeFactor = polygon._object.shadeFactor;

				final float factor = Math.min( 1.0f , ( 1.0f - shadeFactor ) + shadeFactor * Math.abs( (float)polygon._planeNormalZ ) );
				if ( factor < 1.0f )
				{
					final Color color = (Color)fillPaint;
					final float[] rgb = color.getRGBComponents( null );

					fillPaint = new Color( factor * rgb[ 0 ] , factor * rgb[ 1 ] , factor * rgb[ 2 ] , rgb[ 3 ] );
				}
			}

			if ( fillPaint != null )
			{
				g.setRenderingHint( RenderingHints.KEY_ANTIALIASING , RenderingHints.VALUE_ANTIALIAS_OFF );
				g.setPaint( fillPaint );
				g.fill( polygon );
			}
		}

		if ( outline || ( fillPaint == null ) )
		{
			final Paint outlinePaint;

			outlinePaint = ( fillPaint != null )        ? Color.DARK_GRAY
						 : polygon._alternateAppearance ? polygon._object.alternateOutlinePaint
														: polygon._object.outlinePaint;

			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING , RenderingHints.VALUE_ANTIALIAS_ON );
			g.setPaint( outlinePaint );
			g.draw( polygon );
		}

		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING , antiAliasingValue );
	}

	/**
	 * Paint 2D representation of 3D objects at this node and its child nodes
	 * using rendering hints defined for this object. See the other
	 * {@link #paintNode(Graphics2D,Matrix3D,Matrix3D,Node3D,Paint,Paint,float)}
	 * method in this class for a more elaborate
	 * description of this process.
	 * <p />
	 * If the <code>alternateAppearance</code> flag is set, objects should be
	 * use alternate rendering hints, if available. Alternate appearance can be
	 * used to visualize state information, like marking selected or active
	 * objects.
	 *
	 * @param   g                       Graphics2D context.
	 * @param   gTransform              Projection transform for Graphics2D context (3D->2D, pan, sale).
	 * @param   node2view               Transformation from node's to view coordinate system.
	 * @param   node                    Node to paint.
	 * @param   alternateAppearance     Use alternate appearance.
	 */
	public static void paintNode( final Graphics2D g , final Matrix3D gTransform , final Matrix3D node2view , final Node3D node , final boolean alternateAppearance )
	{
		final Matrix3D transform;

		if ( node instanceof Insert3D )
		{
			final Matrix3D insertTransform = ((Insert3D)node).getTransform();
			transform = insertTransform.multiply( node2view );
		}
		else if ( node instanceof Transform3D )
		{
			final Matrix3D transformTransform = ((Transform3D)node).getTransform();
			transform = transformTransform.multiply( node2view );
		}
		else
		{
			transform = node2view;
		}

		if ( node instanceof Object3D )
			paintObject( g , gTransform , transform , (Object3D)node , alternateAppearance );

		final int  childCount = node.getChildCount();

		for ( int i = 0 ; i < childCount ; i++ )
			paintNode( g , gTransform , transform , node.getChild( i ) , alternateAppearance );
	}

	/**
	 * Paint 2D representation of this 3D object. The object coordinates are
	 * transformed using the <code>node2view</code> argument. By default, the
	 * object is painted by drawing the outlines of its 'visible' faces.
	 * <p />
	 * The rendering settings are determined by the <code>outlinePaint</code>,
	 * <code>fillPaint</code>, and <code>shadeFactor</code> arguments. The
	 * colors may be set to <code>null</code> to disable drawing of the
	 * outline or inside of faces respectively. The <code>shadeFactor</code> is
	 * used to modify the fill color based on the Z component of the face normal.
	 * A typical value of <code>0.5</code> would render faces pointing towards
	 * the Z-axis at 100%, and faces perpendicular to the Z-axis at 50%;
	 * specifying <code>0.0</code> completely disables the effect (always 100%);
	 * whilst <code>1.0</code> makes faces perpendicular to the Z-axis black
	 * (0%). The outline color is not influenced by the <code>shadeFactor</code>.
	 * <p />
	 * Objects are painted on the specified graphics context after being
	 * transformed again by gTransform. This may be used to pan/scale the object on the
	 * graphics context (NOTE: IT MAY NOT ROTATE THE OBJECT!).
	 *
	 * @param   g               Graphics2D context.
	 * @param   gTransform      Projection transform for Graphics2D context (3D->2D, pan, sale).
	 * @param   node2view       Transformation from node's to view coordinate system.
	 * @param   node            Node to paint.
	 * @param   outlinePaint    Paint to use for face outlines (<code>null</code> to disable drawing).
	 * @param   fillPaint       Paint to use for filling faces (<code>null</code> to disable drawing).
	 * @param   shadeFactor     Amount of shading that may be applied (0=none, 1=extreme).
	 */
	public static void paintNode( final Graphics2D g , final Matrix3D gTransform , final Matrix3D node2view , final Node3D node , final Paint outlinePaint , final Paint fillPaint , final float shadeFactor )
	{
		final Matrix3D transform;

		if ( node instanceof Insert3D )
		{
			final Matrix3D insertTransform = ((Insert3D)node).getTransform();
			transform = insertTransform.multiply( node2view );
		}
		else if ( node instanceof Transform3D )
		{
			final Matrix3D transformTransform = ((Transform3D)node).getTransform();
			transform = transformTransform.multiply( node2view );
		}
		else
		{
			transform = node2view;
		}

		if ( node instanceof Object3D )
			paintObject( g , gTransform , transform , (Object3D)node , outlinePaint , fillPaint , shadeFactor );

		final int  childCount = node.getChildCount();

		for ( int i = 0 ; i < childCount ; i++ )
			paintNode( g , gTransform , transform , node.getChild( i ) , outlinePaint , fillPaint , shadeFactor );
	}

	/**
	 * This implementation will use the <code>outlinePaint</code>,
	 * <code>fillPaint</code>, and <code>shadeFactor</code> to render the object,
	 * unless the <code>alternateAppearance</code> flag is set, in which case the
	 * <code>alternateOutlinePaint</code> and <code>alternateFillPaint</code>
	 * will be used.
	 *
	 * @param   g                       Graphics2D context.
	 * @param   gTransform              Projection transform for Graphics2D context (3D->2D, pan, sale).
	 * @param   node2view               Transformation from node's to view coordinate system.
	 * @param   node                    Node to paint.
	 * @param   alternateAppearance     Use alternate vs. regular appearance.
	 *
	 * @see     Object3D#outlinePaint
	 * @see     Object3D#fillPaint
	 * @see     Object3D#shadeFactor
	 * @see     Object3D#alternateOutlinePaint
	 * @see     Object3D#alternateFillPaint
	 */
	private static void paintObject( final Graphics2D g , final Matrix3D gTransform , final Matrix3D node2view , final Object3D node , final boolean alternateAppearance )
	{
		paintObject( g , gTransform , node2view , node , alternateAppearance ? node.alternateOutlinePaint : node.outlinePaint , alternateAppearance ? node.alternateFillPaint : node.fillPaint , node.shadeFactor );
	}

	private static void paintObject( final Graphics2D g , final Matrix3D gTransform , final Matrix3D object2view , final Object3D object , final Paint outlinePaint , final Paint fillPaint , final float shadeFactor )
	{
		final int faceCount = object.getFaceCount();

		if ( ( g != null )
		     && ( gTransform != null )
		     && ( object2view != null )
		     && ( ( outlinePaint != null ) || ( fillPaint != null ) )
		     && ( faceCount > 0 )
		     && ( !( object instanceof Cylinder3D ) || !paintCylinder( g , gTransform , object2view , (Cylinder3D)object , outlinePaint , fillPaint , shadeFactor ) )
		     && ( !( object instanceof Sphere3D   ) || !paintSphere  ( g , gTransform , object2view , (Sphere3D  )object , outlinePaint , fillPaint , shadeFactor ) ) )
		{
			/*
			 * If the array is to small, create a larger one.
			 */
			final double[] vertexCoordinates = object.getVertexCoordinates( object2view , _paintVertexCoordinatesCache );
			_paintVertexCoordinatesCache = vertexCoordinates;

			int maxVertexCount = 0;
			for ( int faceIndex = 0; faceIndex < faceCount; faceIndex++ )
			{
				final Face3D face = object.getFace( faceIndex );
				maxVertexCount = Math.max( maxVertexCount , face.getVertexCount() );
			}

			final int[] xs = new int[ maxVertexCount ];
			final int[] ys = new int[ maxVertexCount ];

			final float[]  rgb;

			if ( ( fillPaint instanceof Color ) && ( maxVertexCount > 2 ) && ( shadeFactor >= 0.1f ) && ( shadeFactor <= 1.0f ) )
				rgb = ((Color)fillPaint).getRGBComponents( null );
			else
				rgb = null;

			for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
			{
				final Face3D face = object.getFace( faceIndex );

				final Paint faceFillPaint;
				if ( ( rgb != null ) && ( face.getVertexCount() > 2 ) )
				{
					/*
					 * The <code>shadeFactor</code> is used to modify the fill color based on
					 * the Z component of a face's normal vector. A typical value of
					 * <code>0.5</code> would render faces pointing towards the Z-axis at 100%,
					 * and faces perpendicular to the Z-axis at 50%; specifying <code>0.0</code>
					 * completely disables the effect (always 100%); whilst <code>1.0</code>
					 * makes faces perpendicular to the Z-axis black (0%).
					 */
					final Vector3D faceNormal = face.getNormal();

					final float transformedNormalZ = (float)object2view.rotateZ( faceNormal.x , faceNormal.y , faceNormal.z );
					final float factor = Math.min( 1.0f , ( 1.0f - shadeFactor ) + shadeFactor * Math.abs( transformedNormalZ ) );

					faceFillPaint = ( factor < 1.0f ) ? new Color( factor * rgb[ 0 ] , factor * rgb[ 1 ] , factor * rgb[ 2 ] , rgb[ 3 ] ) : fillPaint;
				}
				else
				{
					faceFillPaint = fillPaint;
				}

				paintFace( g , gTransform , face , outlinePaint , faceFillPaint , vertexCoordinates , xs , ys );
			}
		}
	}

	private static boolean paintCylinder( final Graphics2D g , final Matrix3D gTransform , final Matrix3D cylinder2view , final Cylinder3D cylinder , final Paint outlinePaint , final Paint fillPaint , final float shadeFactor )
	{
		final boolean result;

		final Matrix3D viewBase = cylinder.xform.multiply( cylinder2view );
		final double   h        = cylinder.height;
		final double   rBottom  = cylinder.radiusBottom;
		final double   rTop     = cylinder.radiusTop;

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
				if ( ( shadeFactor >= 0.1f ) && ( shadeFactor <= 1.0f ) && ( fillPaint instanceof Color ) && ( outlinePaint instanceof Color ) )
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

			result = true;
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
					if ( ( shadeFactor >= 0.1f ) && ( shadeFactor <= 1.0f ) && ( fillPaint instanceof Color ) && ( outlinePaint instanceof Color ))
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

			result = true;
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
			result = false;
		}

		return result;
	}

	private static boolean paintSphere( final Graphics2D g , final Matrix3D gTransform , final Matrix3D sphere2view , final Sphere3D sphere , final Paint outlinePaint , final Paint fillPaint , final float shadeFactor )
	{
		final boolean result;

		final double dx = sphere.dx;
		if ( Matrix3D.almostEqual( dx , sphere.dy )
		     && Matrix3D.almostEqual( dx , sphere.dz ) )
		{
			final Matrix3D viewBase          = sphere.xform.multiply( sphere2view );
			final Matrix3D combinedTransform = viewBase.multiply( gTransform );

			final float x = (float)combinedTransform.xo;
			final float y = (float)combinedTransform.yo;

			final float r;
			{
				final double xx = combinedTransform.xx;
				final double xy = combinedTransform.xy;
				final double xz = combinedTransform.xz;

				r = (float)( 0.5 * dx * Math.sqrt( xx * xx + xy * xy + xz * xz ) );
			}

			final Ellipse2D shape = Matrix3D.almostEqual( (double)r , 0.0 ) ? null : new Ellipse2D.Float( x - r , y - r , r + r , r + r );

			if ( fillPaint != null )
			{
				final Paint paint;
				if ( ( shadeFactor >= 0.1f ) && ( shadeFactor <= 1.0f ) && ( fillPaint instanceof Color ) && ( outlinePaint instanceof Color ))
				{
					final float goldenRatio = 0.6180339f;
					final float highlight   = ( goldenRatio - 0.5f ) * r;

					paint = new GradientPaint( x + highlight , y - highlight , (Color)fillPaint , x -r , y + r , (Color)outlinePaint , true );
				}
				else
				{
					paint = fillPaint;
				}

				g.setPaint( paint );
				g.fill( shape );
			}

			if ( outlinePaint != null )
			{
				g.setPaint( outlinePaint );
				g.draw( shape );
			}

			result = true;
		}
		else
		{
			// Not painted, paint fully.
			result = false;
		}

		return result;
	}


	/**
	 * Paint 2D representation of this 3D face.
	 *
	 * @param   g                   Graphics2D context.
	 * @param   gTransform          Projection transform for Graphics2D context (3D->2D, pan, sale).
	 * @param   face                Face to paint.
	 * @param   outlinePaint        Paint to use for face outlines (<code>null</code> to disable drawing).
	 * @param   fillPaint           Paint to use for filling faces (<code>null</code> to disable drawing).
	 * @param   vertexCoordinates   Coordinates of vertices (after view transform is applied).
	 * @param   xs                  Temporary storage for 2D coordinates.
	 * @param   ys                  Temporary storage for 2D coordinates.
	 *
	 * @see     #paintNode
	 */
	private static void paintFace( final Graphics2D g , final Matrix3D gTransform , final Face3D face , final Paint outlinePaint , final Paint fillPaint , final double[] vertexCoordinates , final int[] xs , final int[] ys )
	{
		final int   vertexCount   = face.getVertexCount();
		final int[] vertexIndices = face.getVertexIndices();

		if ( ( vertexCount > 0 ) && ( ( outlinePaint != null ) || ( fillPaint != null ) ) )
		{
			boolean show = true;
			for ( int p = 0 ; p < vertexCount ; p++ )
			{
				final int vi = vertexIndices[ p ] * 3;

				final double x  = vertexCoordinates[ vi ];
				final double y  = vertexCoordinates[ vi + 1 ];
				final double z  = vertexCoordinates[ vi + 2 ];

				final int ix = (int)gTransform.transformX( x , y , z );
				final int iy = (int)gTransform.transformY( x , y , z );

				/*
				 * Perform backface removal if we have 3 points, so we can calculate the normal.
				 *
				 * c = (x1-x2)*(y3-y2)-(y1-y2)*(x3-x2)
				 */
				if ( ( p == 2 ) && !face.isTwoSided() )
				{
					show = ( ( ( xs[ 0 ] - xs[ 1 ] ) * ( iy - ys[ 1 ] ) )
					      <= ( ( ys[ 0 ] - ys[ 1 ] ) * ( ix - xs[ 1 ] ) ) );

					if ( !show )
						break;
				}

				xs[ p ] = ix;
				ys[ p ] = iy;
			}

			if ( show )
			{
				if ( fillPaint != null )
				{
					g.setPaint( fillPaint );

					if ( vertexCount < 3 ) /* point or line */
					{
						if ( outlinePaint == null )
							g.drawLine( xs[ 0 ] , ys[ 0 ] , xs[ vertexCount - 1 ] , ys[ vertexCount - 1 ] );
					}
					else
					{
						g.fillPolygon( xs , ys , vertexCount );
					}
				}

				if ( outlinePaint != null )
				{
					g.setPaint( outlinePaint );
					if ( vertexCount < 3 ) /* point or line */
						g.drawLine( xs[ 0 ] , ys[ 0 ] , xs[ vertexCount - 1 ] , ys[ vertexCount - 1 ] );
					else
						g.drawPolygon( xs , ys , vertexCount );
				}
			}
		}
	}
}
