/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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
 */
package ab.j3d.awt.view.java2d;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.awt.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * This class can render a 3D scene directly to a {@link Graphics2D} context.
 *
 * @author Peter S. Heijnen
 */
public class Java2dRenderer
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private Java2dRenderer()
	{
	}

	/**
	 * Paint 2D representation of 3D objects at this node and its child nodes using
	 * the specified render style.
	 *
	 * @param g                 Graphics2D context.
	 * @param view2image        Projection transform for Graphics2D context
	 *                          (3D->2D, pan, sale).
	 * @param node2view         Transformation from node's to view coordinate
	 *                          system.
	 * @param node              Node to paint.
	 * @param renderStyle       Render style.
	 * @param renderStyleFilter Render style filter.
	 */
	public static void paintNode( @NotNull final Graphics2D g, @NotNull final Matrix3D view2image, @NotNull final Matrix3D node2view, @NotNull final Node3D node, @NotNull final RenderStyle renderStyle, @Nullable final RenderStyleFilter renderStyleFilter )
	{
		Node3D renderedNode = node;
		if ( ( node instanceof Object3D ) && ( (Object3D)renderedNode ).isLowDetailAvailable() )
		{
			final Object3D object = (Object3D)renderedNode;
			final Bounds3D boundingBox = object.getOrientedBoundingBox();
			if ( boundingBox != null )
			{
				final ConvexHull2D projectedBounds = new ConvexHull2D( 8 );

				final Matrix3D object2image = node2view.multiply( view2image );
				projectedBounds.add( object2image.transformX( boundingBox.v1.x, boundingBox.v1.y, boundingBox.v1.z ), object2image.transformY( boundingBox.v1.x, boundingBox.v1.y, boundingBox.v1.z ) );
				projectedBounds.add( object2image.transformX( boundingBox.v2.x, boundingBox.v1.y, boundingBox.v1.z ), object2image.transformY( boundingBox.v2.x, boundingBox.v1.y, boundingBox.v1.z ) );
				projectedBounds.add( object2image.transformX( boundingBox.v1.x, boundingBox.v2.y, boundingBox.v1.z ), object2image.transformY( boundingBox.v1.x, boundingBox.v2.y, boundingBox.v1.z ) );
				projectedBounds.add( object2image.transformX( boundingBox.v2.x, boundingBox.v2.y, boundingBox.v1.z ), object2image.transformY( boundingBox.v2.x, boundingBox.v2.y, boundingBox.v1.z ) );
				projectedBounds.add( object2image.transformX( boundingBox.v1.x, boundingBox.v1.y, boundingBox.v2.z ), object2image.transformY( boundingBox.v1.x, boundingBox.v1.y, boundingBox.v2.z ) );
				projectedBounds.add( object2image.transformX( boundingBox.v2.x, boundingBox.v1.y, boundingBox.v2.z ), object2image.transformY( boundingBox.v2.x, boundingBox.v1.y, boundingBox.v2.z ) );
				projectedBounds.add( object2image.transformX( boundingBox.v1.x, boundingBox.v2.y, boundingBox.v2.z ), object2image.transformY( boundingBox.v1.x, boundingBox.v2.y, boundingBox.v2.z ) );
				projectedBounds.add( object2image.transformX( boundingBox.v2.x, boundingBox.v2.y, boundingBox.v2.z ), object2image.transformY( boundingBox.v2.x, boundingBox.v2.y, boundingBox.v2.z ) );

				final double area = projectedBounds.area();
				renderedNode = object.getLevelOfDetail( area );
			}
		}

		if ( renderedNode != null )
		{
			final Matrix3D object2view;

			if ( renderedNode instanceof Transform3D )
			{
				final Matrix3D transformTransform = ( (Transform3D)renderedNode ).getTransform();
				object2view = transformTransform.multiply( node2view );
			}
			else
			{
				object2view = node2view;
			}

			RenderStyle nodeStyle = renderStyle;
			if ( renderStyleFilter != null )
			{
				nodeStyle = renderStyleFilter.applyFilter( nodeStyle, renderedNode );
			}

			if ( renderedNode instanceof Object3D )
			{
				final Object3D object = (Object3D)renderedNode;
				paintObject( g, view2image, object2view, object, nodeStyle );
			}

			final int childCount = renderedNode.getChildCount();

			for ( int i = 0; i < childCount; i++ )
			{
				paintNode( g, view2image, object2view, renderedNode.getChild( i ), nodeStyle, renderStyleFilter );
			}
		}
	}

	/**
	 * @noinspection JavaDoc
	 */
	private static void paintObject( final Graphics2D g, final Matrix3D view2image, final Matrix3D object2view, final Object3D object, final RenderStyle renderStyle )
	{
		final Color4 outlineColor = renderStyle.isStrokeEnabled() ? renderStyle.getStrokeColor() : null;
		final Color4 fillColor = renderStyle.isFillEnabled() ? renderStyle.getFillColor() : null;

		if ( ( outlineColor != null ) || ( fillColor != null ) )
		{
			if ( object instanceof Cone3D )
			{
				if ( !paintConeOrCylinder( g, view2image, object2view, ( (Cone3D)object ).height, ( (Cone3D)object ).radiusBottom, ( (Cone3D)object ).radiusTop, renderStyle ) )
				{
					paintMesh( g, view2image, object2view, object, renderStyle );
				}
			}
			else if ( object instanceof Cylinder3D )
			{
				if ( !paintConeOrCylinder( g, view2image, object2view, ( (Cylinder3D)object ).getHeight(), ( (Cylinder3D)object ).getRadius(), ( (Cylinder3D)object ).getRadius(), renderStyle ) )
				{
					paintMesh( g, view2image, object2view, object, renderStyle );
				}
			}
			else if ( object instanceof Sphere3D )
			{
				if ( !paintSphere( g, view2image, object2view, (Sphere3D)object, renderStyle ) )
				{
					paintMesh( g, view2image, object2view, object, renderStyle );
				}
			}
			else if ( object instanceof ExtrudedObject2D )
			{
				if ( !paintExtrudedShape( g, view2image, object2view, (ExtrudedObject2D)object, renderStyle ) )
				{
					paintMesh( g, view2image, object2view, object, renderStyle );
				}
			}
			else
			{
				paintMesh( g, view2image, object2view, object, renderStyle );
			}
		}
	}

	/**
	 * @noinspection JavaDoc
	 */
	private static void paintMesh( final Graphics2D g, final Matrix3D view2image, final Matrix3D object2view, final Object3D object, final RenderStyle renderStyle )
	{
		final Color4 outlineColor = renderStyle.isStrokeEnabled() ? renderStyle.getStrokeColor() : null;
		final Color4 fillColor = renderStyle.isFillEnabled() ? renderStyle.getFillColor() : null;
		final boolean applyLighting = renderStyle.isFillLightingEnabled();
		final Matrix3D object2image = object2view.multiply( view2image );

		final Path2D.Float path = new Path2D.Float();

//		final float[] rgb = ( applyLighting && ( fillColor != null ) ) ? fillColor.getRGBComponents( null ) : null;

		for ( final FaceGroup faceGroup : object.getFaceGroups() )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				final Tessellation tessellation = face.getTessellation();
				final List<Vertex3D> vertices = face.getVertices();
				final List<int[]> outlines = tessellation.getOutlines();

				if ( object2view.rotateZ( face.getNormal() ) >= 0.0 )
				{
					final Color faceFillColor;
					if ( fillColor == null )
					{
						faceFillColor = null;
					}
					else if ( applyLighting )
					{
						final Vector3D faceNormal = face.getNormal();
						final float transformedNormalZ = (float)object2view.rotateZ( faceNormal.x, faceNormal.y, faceNormal.z );
						final float factor = Math.min( 1.0f, 0.5f + 0.5f * Math.abs( transformedNormalZ ) );
						if ( factor < 1.0f )
						{
							faceFillColor = new Color( factor * fillColor.getRedFloat(), factor * fillColor.getGreenFloat(), factor * fillColor.getBlueFloat(), fillColor.getAlphaFloat() );
						}
						else
						{
							faceFillColor = new Color( fillColor.getRedFloat(), fillColor.getGreenFloat(), fillColor.getBlueFloat(), fillColor.getAlphaFloat() );
						}
					}
					else
					{
						faceFillColor = new Color( fillColor.getRedFloat(), fillColor.getGreenFloat(), fillColor.getBlueFloat(), fillColor.getAlphaFloat() );
					}

					final int fillAlpha = faceFillColor == null ? 0 : faceFillColor.getAlpha();

					/*
					 * Fill faces.
					 */
					if ( ( fillAlpha > 0 ) && ( faceFillColor != null ) )
					{
						final Color opaqueFaceFillColor = ( fillAlpha < 255 ) ? new Color( faceFillColor.getRGB() ) : faceFillColor;
						for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
						{
							final int[] triangles = primitive.getTriangles();
							if ( isRectangle( object2image, vertices, triangles ) )
							{
								double x1 = object2image.transformX( vertices.get( triangles[ 0 ] ).point );
								double y1 = object2image.transformY( vertices.get( triangles[ 0 ] ).point );
								double x2 = object2image.transformX( vertices.get( triangles[ 2 ] ).point );
								double y2 = object2image.transformY( vertices.get( triangles[ 2 ] ).point );

								if ( x1 > x2 )
								{
									final double temp = x1;
									x1 = x2;
									x2 = temp;
								}

								if ( y1 > y2 )
								{
									final double temp = y1;
									y1 = y2;
									y2 = temp;
								}

								g.setPaint( opaqueFaceFillColor );
								if ( fillAlpha < 255 )
								{
									fillEtched( g, x1, y1, x2, y2 );
								}
								else
								{
									g.fill( new Rectangle2D.Double( x1, y1, x2 - x1, y2 - y1 ) );
								}
							}
							else
							{
								path.reset();

								for ( int i = 0; i < triangles.length; i += 3 )
								{
									final Vector3D p1 = vertices.get( triangles[ i ] ).point;
									final Vector3D p2 = vertices.get( triangles[ i + 1 ] ).point;
									final Vector3D p3 = vertices.get( triangles[ i + 2 ] ).point;

									path.moveTo( object2image.transformX( p1 ), object2image.transformY( p1 ) );
									path.lineTo( object2image.transformX( p2 ), object2image.transformY( p2 ) );
									path.lineTo( object2image.transformX( p3 ), object2image.transformY( p3 ) );
									path.closePath();
								}

								if ( fillAlpha < 255 )
								{
									final Shape clip = g.getClip();
									g.setClip( path );
									g.setPaint( opaqueFaceFillColor );
									final Rectangle2D bounds = path.getBounds2D();
									fillEtched( g, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY() );
									g.setClip( clip );
								}
								else
								{
									g.setPaint( faceFillColor );
									g.fill( path );
								}
							}
						}
					}

					/*
					 * Draw outlines.
					 */
					for ( final int[] outline : outlines )
					{
						if ( outline.length > 1 )
						{
							path.reset();

							for ( int p = 0; p < outline.length; p++ )
							{
								final Vector3D p3d = vertices.get( outline[ p ] ).point;
								if ( p == 0 )
								{
									path.moveTo( object2image.transformX( p3d ), object2image.transformY( p3d ) );
								}
								else
								{
									path.lineTo( object2image.transformX( p3d ), object2image.transformY( p3d ) );
								}
							}

							if ( outlineColor != null )
							{
								g.setPaint( new Color( outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat() ) );
								g.draw( path );
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Fills the given rectangle using lines at a fixed distance from each other.
	 *
	 * @param g  Graphics context.
	 * @param x1 Minimum X-coordinate.
	 * @param y1 Minimum Y-coordinate.
	 * @param x2 Maximum X-coordinate.
	 * @param y2 Maximum Y-coordinate.
	 */
	private static void fillEtched( final Graphics2D g, final double x1, final double y1, final double x2, final double y2 )
	{
		final int step = 16;

		final int cMin = ( (int)( x1 + y1 ) / step + 1 ) * step;
		final int cMax = (int)( x2 + y2 ) / step * step;

		for ( int i = cMin; i <= cMax; i += step )
		{
			final int lineX1 = Math.min( (int)x2, i - (int)y1 );
			final int lineY1 = i - lineX1;
			final int lineY2 = Math.min( (int)y2, i - (int)x1 );
			final int lineX2 = i - lineY2;
			g.drawLine( lineX1, lineY1, lineX2, lineY2 );
		}
	}

	/**
	 * Returns whether the given triangles are a rectangle. This implementation
	 * performs a rough check to see if the triangles are a rectangle formed by a
	 * triangle fan. As such, this method may return false negatives.
	 *
	 * @param object2image Object to image transformation.
	 * @param vertices     Vertices.
	 * @param triangles    Vertex indices of each triangle.
	 *
	 * @return {@code true} if the triangles form a rectangle.
	 */
	private static boolean isRectangle( final Matrix3D object2image, final List<Vertex3D> vertices, final int[] triangles )
	{
		boolean result = false;

		if ( ( triangles.length == 6 ) && ( triangles[ 0 ] == triangles[ 3 ] ) && ( triangles[ 2 ] == triangles[ 4 ] ) )
		{
			final Vector3D p1 = vertices.get( triangles[ 0 ] ).point;
			final Vector3D p2 = vertices.get( triangles[ 1 ] ).point;
			final Vector3D p3 = vertices.get( triangles[ 2 ] ).point;
			final Vector3D p4 = vertices.get( triangles[ 5 ] ).point;

			result = ( ( object2image.transformX( p1 ) == object2image.transformX( p2 ) ) &&
			           ( object2image.transformY( p2 ) == object2image.transformY( p3 ) ) &&
			           ( object2image.transformX( p3 ) == object2image.transformX( p4 ) ) &&
			           ( object2image.transformY( p4 ) == object2image.transformY( p1 ) ) ) ||

			         ( ( object2image.transformY( p1 ) == object2image.transformY( p2 ) ) &&
			           ( object2image.transformX( p2 ) == object2image.transformX( p3 ) ) &&
			           ( object2image.transformY( p3 ) == object2image.transformY( p4 ) ) &&
			           ( object2image.transformX( p4 ) == object2image.transformX( p1 ) ) );
		}

		return result;
	}

	/**
	 * Paints the given extruded shape using the 2D shape it contains, if
	 * possible.
	 *
	 * @param g           Graphics context to paint to.
	 * @param view2image  Transformation from view coordinates to graphics context
	 *                    coordinates.
	 * @param object2view Transformation from object-local coordinates to view
	 *                    coordinates.
	 * @param object      Extruded shape to be painted.
	 * @param renderStyle Render style to use.
	 *
	 * @return {@code true} if the shape was painted; {@code false} if the shape
	 * should be painted by some other means.
	 */
	private static boolean paintExtrudedShape( final Graphics2D g, final Matrix3D view2image, final Matrix3D object2view, final ExtrudedObject2D object, final RenderStyle renderStyle )
	{
		final boolean result;

		final Color4 outlineColor = renderStyle.isStrokeEnabled() ? renderStyle.getStrokeColor() : null;
		final Color4 fillColor = renderStyle.isFillEnabled() ? renderStyle.getFillColor() : null;

		final Shape shape = object.getShape();
		final Vector3D extrusion = object.getExtrusion();

		g.translate( -0.5, -0.5 ); // Match rounding used in other paint methods.

		if ( ( extrusion.x != 0.0 ) || ( extrusion.y != 0.0 ) )
		{
			result = false;
		}
		else if ( MathTools.almostEqual( object2view.zz, 0.0 ) )
		{
			final Rectangle2D bounds = shape.getBounds2D();

			final Matrix3D object2graphics = object2view.multiply( view2image );

			final Vector3D v1 = object2graphics.transform( bounds.getMinX(), bounds.getMinY(), 0.0 );
			final Vector3D v2 = object2graphics.transform( bounds.getMaxX(), bounds.getMaxY(), extrusion.z );

			final double minX = Math.min( v1.x, v2.x );
			final double minY = Math.min( v1.y, v2.y );
			final double maxX = Math.max( v1.x, v2.x );
			final double maxY = Math.max( v1.y, v2.y );

			final Shape boundsShape = new Rectangle2D.Double( minX, minY, maxX - minX, maxY - minY );

			if ( fillColor != null )
			{
				g.setPaint( new Color( fillColor.getRedFloat(), fillColor.getGreenFloat(), fillColor.getBlueFloat(), fillColor.getAlphaFloat() ) );
				g.fill( boundsShape );
			}

			if ( outlineColor != null )
			{
				g.setPaint( new Color( outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat() ) );
				g.draw( boundsShape );
			}

			result = true;
		}
		else if ( MathTools.almostEqual( Math.abs( object2view.zz ), 1.0 ) )
		{
			final Matrix3D object2image = object2view.multiply( view2image );

			final AffineTransform object2graphics2D = new AffineTransform(
			object2image.xx, object2image.yx,
			object2image.xy, object2image.yy,
			object2image.xo, object2image.yo );

			final Path2D viewShape = new Path2D.Float();
			viewShape.append( shape.getPathIterator( object2graphics2D ), false );

			if ( fillColor != null )
			{
				g.setPaint( new Color( fillColor.getRedFloat(), fillColor.getGreenFloat(), fillColor.getBlueFloat(), fillColor.getAlphaFloat() ) );
				g.fill( viewShape );
			}

			if ( outlineColor != null )
			{
				g.setPaint( new Color( outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat() ) );
				g.draw( viewShape );
			}

			result = true;
		}
		else
		{
			result = false;
		}

		g.translate( 0.5, 0.5 ); // Undo translation applied above.

		return result;
	}

	/**
	 * @noinspection JavaDoc
	 */
	private static boolean paintConeOrCylinder( final Graphics2D g, final Matrix3D view2image, final Matrix3D object2view, final double height, final double radiusBottom, final double radiusTop, final RenderStyle renderStyle )
	{
		final boolean result;

		final Color4 outlineColor = renderStyle.isStrokeEnabled() ? renderStyle.getStrokeColor() : null;
		final Color4 fillColor = renderStyle.isFillEnabled() ? renderStyle.getFillColor() : null;
		final boolean applyLighting = renderStyle.isFillLightingEnabled();


		final double zz = object2view.zz;
		final double xz = object2view.xz;
		final double yz = object2view.yz;
		final double xo = object2view.xo;
		final double yo = object2view.yo;
		final double zo = object2view.zo;

//		final float goldenRatio = 0.6180339f;

		/*
		 * The cone's center axis (Z-axis) is is parallel on the view plane
		 * (on the XY / Z=0 plane).
		 *
		 * We can can only see the outline of the cone (trapezoid).
		 */
		if ( MathTools.almostEqual( zz, 0.0 ) )
		{
			// (xz,yz) = direction of cone Z-axis in XY plane
			// (xo,yo,zo) = view coordinate of cone bottom centeroid

			final double p1x = xo + yz * radiusBottom;            // p1 = bottom left,
			final double p1y = yo - xz * radiusBottom;
			final double p2x = xo + yz * radiusTop + xz * height; // p2 = top left
			final double p2y = yo - xz * radiusTop + yz * height;
			final double p3x = xo - yz * radiusTop + xz * height; // p3 = top right
			final double p3y = yo + xz * radiusTop + yz * height;
			final double p4x = xo - yz * radiusBottom;            // p4 = bottom right
			final double p4y = yo + xz * radiusBottom;

			/*
			 * Project and draw trapezoid.
			 */
			final float x1 = (float)view2image.transformX( p1x, p1y, zo );
			final float y1 = (float)view2image.transformY( p1x, p1y, zo );
			final float x2 = (float)view2image.transformX( p2x, p2y, zo );
			final float y2 = (float)view2image.transformY( p2x, p2y, zo );
			final float x3 = (float)view2image.transformX( p3x, p3y, zo );
			final float y3 = (float)view2image.transformY( p3x, p3y, zo );
			final float x4 = (float)view2image.transformX( p4x, p4y, zo );
			final float y4 = (float)view2image.transformY( p4x, p4y, zo );

			final Path2D.Float path = new Path2D.Float( Path2D.WIND_EVEN_ODD, 5 );
			path.moveTo( x1, y1 );
			path.lineTo( x2, y2 );
			path.lineTo( x3, y3 );
			path.lineTo( x4, y4 );
			path.closePath();

			if ( fillColor != null )
			{
				if ( applyLighting )
				{
					final float goldenRatio = 0.6180339f;
					final float highlightX = ( 1.0f - goldenRatio ) * x1 + goldenRatio * x4;
					final float highlightY = ( 1.0f - goldenRatio ) * y1 + goldenRatio * y4;
					g.setPaint( new GradientPaint( highlightX, highlightY, new Color( fillColor.getRedFloat(), fillColor.getGreenFloat(), fillColor.getBlueFloat(), fillColor.getAlphaFloat() ), x1, y1, new Color( outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat() ), true ) );
				}
				else
				{
					g.setPaint( new Color( fillColor.getRedFloat(), fillColor.getGreenFloat(), fillColor.getBlueFloat(), fillColor.getAlphaFloat() ) );
				}
				g.fill( path );
			}

			if ( outlineColor != null )
			{
				g.setPaint( new Color( outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat() ) );
				g.draw( path );
			}

			result = true;
		}
		/*
		 * Viewing along Z-axis. We can see, the bottom and/or top cap and the
		 * area between the two.
		 */
		else if ( MathTools.almostEqual( xz, 0.0 ) &&
		          MathTools.almostEqual( yz, 0.0 ) )
		{
			final Matrix3D combinedTransform = object2view.multiply( view2image );

			final float x = (float)combinedTransform.xo;
			final float y = (float)combinedTransform.yo;
			final float botZ = (float)combinedTransform.zo;
			final float botRadius;
			{
				final double dx = combinedTransform.xx * radiusBottom;
				final double dy = combinedTransform.yx * radiusBottom;
				botRadius = (float)Math.sqrt( dx * dx + dy * dy );
			}

			final Ellipse2D bot = MathTools.almostEqual( botRadius, 0.0 ) ? null : new Ellipse2D.Float( x - botRadius, y - botRadius, 2.0f * botRadius, 2.0f * botRadius );

			final float topZ = (float)combinedTransform.transformZ( 0.0, 0.0, height );
			final float topRadius;
			{
				final double dx = radiusTop * combinedTransform.xx + height * combinedTransform.xz;
				final double dy = radiusTop * combinedTransform.yx + height * combinedTransform.yz;
				topRadius = (float)Math.sqrt( dx * dx + dy * dy );
			}

			final Ellipse2D top = MathTools.almostEqual( topRadius, 0.0 ) ? null : new Ellipse2D.Float( x - topRadius, y - topRadius, 2.0f * topRadius, 2.0f * topRadius );

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

				final Color paint;
				if ( fillColor != null )
				{
//					if ( ( shadeFactor >= 0.1f ) && ( shadeFactor <= 1.0f ) && ( fillPaint instanceof Color ) && ( outlineColor instanceof Color ))
//					{
//						final float r = Math.max( topRadius, botRadius );
//						final float highlight = ( goldenRatio - 0.5f ) * r;
//						paint = new GradientPaint( x + highlight, y - highlight, (Color)fillPaint, x -r, y + r, (Color)outlineColor, true );
//					}
//					else
					{
						paint = new Color( fillColor.getRedFloat(), fillColor.getGreenFloat(), fillColor.getBlueFloat(), fillColor.getAlphaFloat() );
					}
					g.setPaint( paint );

					if ( !shape1.equals( shape2 ) )
					{
						g.fill( shape1 );
					}
				}
				else
				{
					paint = null;
				}

				if ( outlineColor != null )
				{
					g.setPaint( new Color( outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat() ) );
					if ( !shape1.equals( shape2 ) )
					{
						g.draw( shape1 );
					}
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
						g.setPaint( new Color( outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat() ) );
						g.draw( shape2 );
					}
				}
			}

//			rby = rby < 0 ? -rby : rby;
//
//			if ( rtx != 0 && rty != 0 )
//				g.drawOval( (x - (rtx / 2) ), (y - (rty / 2) ), rtx, rty );
//
//			if ( rbx != 0 && rby != 0 )
//				g.drawOval( (x - (rbx / 2) ), (y - (rby / 2) ), rbx, rby );

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

	/**
	 * @noinspection JavaDoc
	 */
	private static boolean paintSphere( final Graphics2D g, final Matrix3D view2image, final Matrix3D sphere2view, final Sphere3D sphere, final RenderStyle renderStyle )
	{
		final boolean result;

		final Color4 outlineColor = renderStyle.isStrokeEnabled() ? renderStyle.getStrokeColor() : null;
		final Color4 fillColor = renderStyle.isFillEnabled() ? renderStyle.getFillColor() : null;
		final boolean applyLighting = renderStyle.isFillLightingEnabled();

		final double radius = sphere.getRadius();

		final Matrix3D viewBase = sphere2view;
		final Matrix3D combinedTransform = viewBase.multiply( view2image );

		final float x = (float)combinedTransform.xo;
		final float y = (float)combinedTransform.yo;

		final float r;
		{
			final double xx = combinedTransform.xx;
			final double xy = combinedTransform.xy;
			final double xz = combinedTransform.xz;

			r = (float)( radius * Math.sqrt( xx * xx + xy * xy + xz * xz ) );
		}

		final Ellipse2D shape = MathTools.almostEqual( r, 0.0 ) ? null : new Ellipse2D.Float( x - r, y - r, r + r, r + r );

		if ( fillColor != null )
		{
			final Paint paint;
			if ( applyLighting && ( outlineColor != null ) )
			{
				final float goldenRatio = 0.6180339f;
				final float highlight = ( goldenRatio - 0.5f ) * r;

				paint = new GradientPaint( x + highlight, y - highlight, new Color( fillColor.getRedFloat(), fillColor.getGreenFloat(), fillColor.getBlueFloat(), fillColor.getAlphaFloat() ), x - r, y + r, new Color( outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat() ), true );
			}
			else
			{
				paint = new Color( fillColor.getRedFloat(), fillColor.getGreenFloat(), fillColor.getBlueFloat(), fillColor.getAlphaFloat() );
			}

			g.setPaint( paint );
			g.fill( shape );
		}

		if ( outlineColor != null )
		{
			g.setPaint( new Color( outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat() ) );
			g.draw( shape );
		}

		result = true;

		return result;
	}
}
