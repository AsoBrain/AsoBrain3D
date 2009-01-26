/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2009 Peter S. Heijnen
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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Cylinder3D;
import ab.j3d.model.ExtrudedObject2D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Insert3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Sphere3D;
import ab.j3d.model.Transform3D;
import ab.j3d.view.RenderQueue;
import ab.j3d.view.RenderedPolygon;

import com.numdata.oss.MathTools;

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
	private static double[] paintVertexCoordinatesCache;

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
		final RenderedPolygon[] polygons = fill ? renderQueue.getQueuedPolygons() : renderQueue.getUnsortedQueue();
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
		for ( final RenderedPolygon polygon : polygons )
		{
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
				fillPaint = polygon._object.alternateFillColor;
			}
			else if ( useMaterialColor && ( material != null ) && ( material.colorMap == null ) )
			{
				fillPaint = new Color( material.getARGB() );
			}
			else
			{
				fillPaint = polygon._object.fillColor;
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
			final Color outlineColor;

			outlineColor = ( fillPaint != null )        ? Color.DARK_GRAY
			             : polygon._alternateAppearance ? polygon._object.alternateOutlineColor
			                                            : polygon._object.outlineColor;

			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING , RenderingHints.VALUE_ANTIALIAS_ON );
			g.setPaint( outlineColor );
			g.draw( polygon );
		}

		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING , antiAliasingValue );
	}

	/**
	 * Paint 2D representation of 3D objects at this node and its child nodes
	 * using rendering hints defined for this object. See the other
	 * {@link #paintNode(Graphics2D,Matrix3D,Matrix3D,Node3D,Color,Paint,float)}
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
		paintNode( g , gTransform , node2view , node , alternateAppearance , null );
	}

	/**
	 * Paint 2D representation of 3D objects at this node and its child nodes
	 * using rendering hints defined for this object. See the other
	 * {@link #paintNode(Graphics2D,Matrix3D,Matrix3D,Node3D,Color,Paint,float)}
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
	 * @param   fillPaintOverride       Override fill paint.
	 */
	public static void paintNode( final Graphics2D g , final Matrix3D gTransform , final Matrix3D node2view , final Node3D node , final boolean alternateAppearance , final Paint fillPaintOverride )
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
		{
			final Object3D object       = (Object3D)node;
			final Color    outlineColor =                                                     alternateAppearance ? object.alternateOutlineColor : object.outlineColor;
			final Paint    fillColor    = ( fillPaintOverride != null ) ? fillPaintOverride : alternateAppearance ? object.alternateFillColor    : object.fillColor;

			paintObject( g , gTransform , transform , object , outlineColor , fillColor , object.shadeFactor );
		}

		final int  childCount = node.getChildCount();

		for ( int i = 0 ; i < childCount ; i++ )
		{
			paintNode( g , gTransform , transform , node.getChild( i ) , alternateAppearance , fillPaintOverride );
		}
	}

	/**
	 * Paint 2D representation of this 3D object. The object coordinates are
	 * transformed using the <code>node2view</code> argument. By default, the
	 * object is painted by drawing the outlines of its 'visible' faces.
	 * <p />
	 * The rendering settings are determined by the <code>outlineColor</code>,
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
	 * @param   outlineColor    Paint to use for face outlines (<code>null</code> to disable drawing).
	 * @param   fillPaint       Paint to use for filling faces (<code>null</code> to disable drawing).
	 * @param   shadeFactor     Amount of shading that may be applied (0=none, 1=extreme).
	 */
	public static void paintNode( final Graphics2D g , final Matrix3D gTransform , final Matrix3D node2view , final Node3D node , final Color outlineColor , final Paint fillPaint , final float shadeFactor )
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
		{
			paintObject( g , gTransform , transform , (Object3D)node , outlineColor , fillPaint , shadeFactor );
		}

		final int  childCount = node.getChildCount();

		for ( int i = 0 ; i < childCount ; i++ )
			paintNode( g , gTransform , transform , node.getChild( i ) , outlineColor , fillPaint , shadeFactor );
	}

	private static void paintObject( final Graphics2D g , final Matrix3D gTransform , final Matrix3D object2view , final Object3D object , final Color outlineColor , final Paint fillPaint , final float shadeFactor )
	{
		final int faceCount = object.getFaceCount();

		if ( ( g != null )
		     && ( gTransform != null )
		     && ( object2view != null )
		     && ( ( outlineColor != null ) || ( fillPaint != null ) )
		     && ( faceCount > 0 )
		     && ( !( object instanceof Cylinder3D       ) || !paintCylinder     ( g , gTransform , object2view , (Cylinder3D      )object , outlineColor , fillPaint , shadeFactor ) )
		     && ( !( object instanceof Sphere3D         ) || !paintSphere       ( g , gTransform , object2view , (Sphere3D        )object , outlineColor , fillPaint , shadeFactor ) )
		     && ( !( object instanceof ExtrudedObject2D ) || !paintExtrudedShape( g , gTransform , object2view , (ExtrudedObject2D)object , outlineColor , fillPaint , shadeFactor ) ) )
		{
			/*
			 * If the array is to small, create a larger one.
			 */
			final double[] vertexCoordinates = object.getVertexCoordinates( object2view , paintVertexCoordinatesCache );
			paintVertexCoordinatesCache = vertexCoordinates;

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

				paintFace( g , gTransform , face , outlineColor , faceFillPaint , vertexCoordinates , xs , ys );
			}
		}
	}

	/**
	 * Paints the given extruded shape using the 2D shape it contains, if
	 * possible.
	 *
	 * @param   g               Graphics context to paint to.
	 * @param   gTransform      Transformation from view coordinates to graphics
	 *                          context coordinates.
	 * @param   object2view     Transformation from object-local coordinates to
	 *                          view coordinates.
	 * @param   object          Extruded shape to be painted.
	 * @param   outlineColor    Paint to use for the outline, or
	 *                          <code>null</code> to draw no outline.
	 * @param   fillPaint       Paint to fill the painted shape with, or
	 *                          <code>null</code> if the shape mustn't be filled.
	 * @param   shadeFactor     Shade factor to be applied; currently ignored.
	 *
	 * @return  <code>true</code> if the shape was painted; <code>false</code>
	 *          if the shape should be painted by some other means.
	 */
	private static boolean paintExtrudedShape( final Graphics2D g , final Matrix3D gTransform , final Matrix3D object2view , final ExtrudedObject2D object , final Color outlineColor , final Paint fillPaint , final float shadeFactor )
	{
		final boolean result;

		final Matrix3D viewBase = object.transform.multiply( object2view );
		final Shape    shape    = object.shape;

		g.translate( -0.5 , -0.5 ); // Match rounding used in other paint methods.

		if ( ( object.extrusion.x != 0.0 ) || ( object.extrusion.y != 0.0 ) )
		{
			result = false;
		}
		else if ( MathTools.almostEqual( viewBase.zz , 0.0 ) )
		{
			final Rectangle2D bounds = shape.getBounds2D();

			final Matrix3D object2graphics = viewBase.multiply( gTransform );

			final Vector3D v1 = object2graphics.multiply( bounds.getMinX() , bounds.getMinY() , 0.0 );
			final Vector3D v2 = object2graphics.multiply( bounds.getMaxX() , bounds.getMaxY() , object.extrusion.z  );

			final double minX = Math.min( v1.x , v2.x );
			final double minY = Math.min( v1.y , v2.y );
			final double maxX = Math.max( v1.x , v2.x );
			final double maxY = Math.max( v1.y , v2.y );

			final Rectangle2D.Double boundsShape = new Rectangle2D.Double( minX , minY , maxX - minX , maxY - minY );

			if ( fillPaint != null )
			{
				g.setPaint( fillPaint );
				g.fill( boundsShape );
			}

			if ( outlineColor != null )
			{
				g.setPaint( outlineColor );
				g.draw( boundsShape );
			}

			result = true;
		}
		else if ( MathTools.almostEqual( Math.abs( viewBase.zz ) , 1.0 ) )
		{
			final Matrix3D object2graphics = viewBase.multiply( gTransform );

			final AffineTransform viewTransform = new AffineTransform(
					object2graphics.xx , object2graphics.yx ,
					object2graphics.xy , object2graphics.yy ,
					object2graphics.xo , object2graphics.yo );

			final GeneralPath viewShape = new GeneralPath();
			viewShape.append( shape.getPathIterator( viewTransform ) , false );

			if ( fillPaint != null )
			{
				g.setPaint( fillPaint );
				g.fill( viewShape );
			}

			if ( outlineColor != null )
			{
				g.setPaint( outlineColor );
				g.draw( viewShape );
			}

			result = true;
		}
		else
		{
			result = false;
		}

		g.translate( 0.5 , 0.5 ); // Undo translation applied above.

		return result;
	}

	private static boolean paintCylinder( final Graphics2D g , final Matrix3D gTransform , final Matrix3D cylinder2view , final Cylinder3D cylinder , final Color outlineColor , final Paint fillPaint , final float shadeFactor )
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
		if ( MathTools.almostEqual( zz , 0.0 ) )
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
//				if ( ( shadeFactor >= 0.1f ) && ( shadeFactor <= 1.0f ) && ( fillPaint instanceof Color ) && ( outlineColor instanceof Color ) )
//				{
//					final float highlightX = ( 1.0f - goldenRatio ) * x1 + goldenRatio * x4;
//					final float highlightY = ( 1.0f - goldenRatio ) * y1 + goldenRatio * y4;
//					g.setPaint( new GradientPaint( highlightX , highlightY , (Color)fillPaint , x1 , y1 , (Color)outlineColor , true ) );
//				}
//				else
				{
					g.setPaint( fillPaint );
				}
				g.fill( path );
			}

			if ( outlineColor != null )
			{
				g.setPaint( outlineColor );
				g.draw( path );
			}

			result = true;
		}
		/*
		 * Viewing along Z-axis. We can see, the bottom and/or top cap and the
		 * area between the two.
		 */
		else if ( MathTools.almostEqual( xz , 0.0 ) &&
		          MathTools.almostEqual( yz , 0.0 ) )
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

			final Ellipse2D bot = MathTools.almostEqual( (double)botRadius , 0.0 ) ? null : new Ellipse2D.Float( x - botRadius , y - botRadius , 2.0f * botRadius , 2.0f * botRadius );

			final float topZ = (float)combinedTransform.transformZ( 0.0 , 0.0 , h );
			final float topRadius;
			{
				final double dx = rTop * combinedTransform.xx + h * combinedTransform.xz;
				final double dy = rTop * combinedTransform.yx + h * combinedTransform.yz;
				topRadius = (float)Math.sqrt( dx * dx + dy * dy );
			}

			final Ellipse2D top = MathTools.almostEqual( (double)topRadius , 0.0 ) ? null : new Ellipse2D.Float( x - topRadius , y - topRadius , 2.0f * topRadius , 2.0f * topRadius );

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
//					if ( ( shadeFactor >= 0.1f ) && ( shadeFactor <= 1.0f ) && ( fillPaint instanceof Color ) && ( outlineColor instanceof Color ))
//					{
//						final float r = Math.max( topRadius , botRadius );
//						final float highlight = ( goldenRatio - 0.5f ) * r;
//						paint = new GradientPaint( x + highlight , y - highlight , (Color)fillPaint , x -r , y + r , (Color)outlineColor , true );
//					}
//					else
					{
						paint = fillPaint;
					}
					g.setPaint( paint );

					if ( !shape1.equals( shape2 ) )
						g.fill( shape1 );
				}
				else
				{
					paint = null;
				}

				if ( outlineColor != null )
				{
					g.setPaint( outlineColor );
					if ( !shape1.equals( shape2 ) )
						g.draw( shape1 );
				}

				if ( shape2 != null )
				{
					if ( paint != null )
					{
						g.setPaint( paint );
						g.fill( shape2 );
					}

					if ( outlineColor != null )
					{
						g.setPaint( outlineColor );
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

	private static boolean paintSphere( final Graphics2D g , final Matrix3D gTransform , final Matrix3D sphere2view , final Sphere3D sphere , final Color outlineColor , final Paint fillPaint , final float shadeFactor )
	{
		final boolean result;

		final double dx = sphere.dx;
		if ( MathTools.almostEqual( dx , sphere.dy ) &&
		     MathTools.almostEqual( dx , sphere.dz ) )
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

			final Ellipse2D shape = MathTools.almostEqual( (double)r , 0.0 ) ? null : new Ellipse2D.Float( x - r , y - r , r + r , r + r );

			if ( fillPaint != null )
			{
				final Paint paint;
//				if ( ( shadeFactor >= 0.1f ) && ( shadeFactor <= 1.0f ) && ( fillPaint instanceof Color ) && ( outlineColor instanceof Color ))
//				{
//					final float goldenRatio = 0.6180339f;
//					final float highlight   = ( goldenRatio - 0.5f ) * r;
//
//					paint = new GradientPaint( x + highlight , y - highlight , (Color)fillPaint , x -r , y + r , (Color)outlineColor , true );
//				}
//				else
				{
					paint = fillPaint;
				}

				g.setPaint( paint );
				g.fill( shape );
			}

			if ( outlineColor != null )
			{
				g.setPaint( outlineColor );
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
	 * @param   outlineColor        Paint to use for face outlines (<code>null</code> to disable drawing).
	 * @param   fillPaint           Paint to use for filling faces (<code>null</code> to disable drawing).
	 * @param   vertexCoordinates   Coordinates of vertices (after view transform is applied).
	 * @param   xs                  Temporary storage for 2D coordinates.
	 * @param   ys                  Temporary storage for 2D coordinates.
	 *
	 * @see     #paintNode
	 */
	private static void paintFace( final Graphics2D g , final Matrix3D gTransform , final Face3D face , final Color outlineColor , final Paint fillPaint , final double[] vertexCoordinates , final int[] xs , final int[] ys )
	{
		final int   vertexCount   = face.getVertexCount();
		final int[] vertexIndices = face.getVertexIndices();

		if ( ( vertexCount > 0 ) && ( ( outlineColor != null ) || ( fillPaint != null ) ) )
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
						if ( outlineColor == null )
							g.drawLine( xs[ 0 ] , ys[ 0 ] , xs[ vertexCount - 1 ] , ys[ vertexCount - 1 ] );
					}
					else
					{
						g.fillPolygon( xs , ys , vertexCount );
					}
				}

				if ( outlineColor != null )
				{
					g.setPaint( outlineColor );
					if ( vertexCount < 3 ) /* point or line */
						g.drawLine( xs[ 0 ] , ys[ 0 ] , xs[ vertexCount - 1 ] , ys[ vertexCount - 1 ] );
					else
						g.drawPolygon( xs , ys , vertexCount );
				}
			}
		}
	}
}
