/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.view.control.planar;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.text.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.view.*;

/**
 * This implementation of {@link Graphics2D} is used to draw on a plane in 3D
 * space.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class PlanarGraphics2D
	extends Graphics2D
{
	/**
	 * Encapsulated {@link Graphics2D} that renders to image.
	 */
	final Graphics2D _g2d;

	/**
	 * Transformation matrix that transforms planar coordinates to view coordinates.
	 */
	private final Matrix3D _plane2view;

	/**
	 * Projector that projects view coordinates to image coordinates.
	 */
	private final Projector _projector;

	/**
	 * Construct {@link PlanarGraphics2D}.
	 *
	 * @param   g2d         {@link Graphics2D} that renders to image.
	 * @param   plane2view  Transformation matrix that transforms planar coordinates to view coordinates.
	 * @param   projector   Projector that projects view coordinates to image coordinates.
	 */
	public PlanarGraphics2D( final Graphics2D g2d, final Matrix3D plane2view, final Projector projector )
	{
		_g2d        = g2d;
		_plane2view = plane2view;
		_projector  = projector;
	}

	/**
	 * This method is called when an unimplemented feature is used.
	 *
	 * @return  <code>null</code>.
	 */
	private static <T> T notImplemented()
	{
		throw new AssertionError();
	}

	/**
	 * Convert plane coordinates to image coordinates.
	 *
	 * @param   x   X coordinate on plane.
	 * @param   y   Y coordinate on plane.
	 *
	 * @return  Image coordinates.
	 */
	private double[] plane2image( final double x, final double y )
	{
		final double[] result = new double[ 2 ];
		final Matrix3D plane2view = _plane2view;
		_projector.project( result, 0, plane2view.transformX( x, y, 0.0 ), plane2view.transformY( x, y, 0.0 ), plane2view.transformZ( x, y, 0.0 ) );
		return result;
	}

	/**
	 * Convert a {@link Shape} on the plane to a {@link Shape} on the image.
	 *
	 * @param   shape   Shape on plane.
	 *
	 * @return  shape on image.
	 */
	private Shape planeShape2ImageShape( final Shape shape )
	{
		final Shape result;

		if ( shape instanceof Line2D )
		{
			final Line2D line = (Line2D)shape;

			final double[] p1 = plane2image( line.getX1(), line.getY1() );
			final double[] p2 = plane2image( line.getX2(), line.getY2() );

			result = new Line2D.Double( p1[ 0 ], p1[ 1 ], p2[ 0 ], p2[ 1 ] );
		}
		else
		{
			final PathIterator pathIterator = shape.getPathIterator( getTransform() );

			final GeneralPath imageShape = new GeneralPath( pathIterator.getWindingRule(), 10 );

			final double[] coordinates = new double[ 6 ];

			while ( !pathIterator.isDone() )
			{
				switch ( pathIterator.currentSegment( coordinates ) )
				{
					case PathIterator.SEG_MOVETO :
						{
							final double[] p = plane2image( coordinates[ 0 ], coordinates[ 1 ] );
							imageShape.moveTo( p[ 0 ], p[ 1 ] );
						}
						break;

					case PathIterator.SEG_LINETO :
						{
							final double[] p = plane2image( coordinates[ 0 ], coordinates[ 1 ] );
							imageShape.lineTo( p[ 0 ], p[ 1 ] );
						}
						break;

					case PathIterator.SEG_QUADTO :
						{
							final double[] p1 = plane2image( coordinates[ 0 ], coordinates[ 1 ] );
							final double[] p2 = plane2image( coordinates[ 2 ], coordinates[ 3 ] );
							imageShape.quadTo( p1[ 0 ], p1[ 1 ], p2[ 0 ], p2[ 1 ] );
						}
						break;

					case PathIterator.SEG_CUBICTO :
						{
							final double[] p1 = plane2image( coordinates[ 0 ], coordinates[ 1 ] );
							final double[] p2 = plane2image( coordinates[ 2 ], coordinates[ 3 ] );
							final double[] p3 = plane2image( coordinates[ 4 ], coordinates[ 5 ] );
							imageShape.curveTo( p1[ 0 ], p1[ 1 ], p2[ 0 ], p2[ 1 ], p3[ 0 ], p3[ 1 ] );
						}
						break;

					case PathIterator.SEG_CLOSE :
						{
							imageShape.closePath();
						}
						break;
				}

				pathIterator.next();
			}

			result = imageShape;
		}

		return result;
	}

	@Override
	public Graphics create()
	{
		return notImplemented();
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration()
	{
		return _g2d.getDeviceConfiguration();
	}

	@Override
	public Object getRenderingHint( final RenderingHints.Key hintKey )
	{
		return _g2d.getRenderingHint( hintKey );
	}

	@Override
	public RenderingHints getRenderingHints()
	{
		return _g2d.getRenderingHints();
	}

	@Override
	public void addRenderingHints( final Map<?, ?> hints )
	{
		_g2d.addRenderingHints( hints );
	}

	@Override
	public void setRenderingHint( final RenderingHints.Key hintKey, final Object hintValue )
	{
		_g2d.setRenderingHint( hintKey, hintValue );
	}

	@Override
	public void setRenderingHints( final Map<?,?> hints )
	{
		_g2d.setRenderingHints( hints );
	}

	@Override
	public Shape getClip()
	{
		return notImplemented();
	}

	@Override
	public void setClip( final Shape clip )
	{
		notImplemented();
	}

	@Override
	public void clip( final Shape s )
	{
		notImplemented();
	}

	@Override
	public Rectangle getClipBounds()
	{
		return notImplemented();
	}

	@Override
	public void setClip( final int x, final int y, final int width, final int height )
	{
		notImplemented();
	}

	@Override
	public void clipRect( final int x, final int y, final int width, final int height )
	{
		notImplemented();
	}

	@Override
	public boolean hit( final Rectangle rectangle, final Shape shape, final boolean onStroke )
	{
		notImplemented();
		return false;
	}

	@Override
	public void copyArea( final int x, final int y, final int width, final int height, final int dx, final int dy )
	{
		notImplemented();
	}

	@Override
	public Color getBackground()
	{
		return _g2d.getBackground();
	}

	@Override
	public void setBackground( final Color color )
	{
		_g2d.setBackground( color );
	}

	@Override
	public Color getColor()
	{
		return _g2d.getColor();
	}

	@Override
	public void setColor( final Color c )
	{
		_g2d.setColor( c );
	}

	@Override
	public Composite getComposite()
	{
		return _g2d.getComposite();
	}

	@Override
	public void setComposite( final Composite composite )
	{
		_g2d.setComposite( composite );
	}

	@Override
	public Font getFont()
	{
		return _g2d.getFont();
	}

	@Override
	public void setFont( final Font font )
	{
		_g2d.setFont( font );
	}

	@Override
	public FontMetrics getFontMetrics( final Font font )
	{
		return _g2d.getFontMetrics( font );
	}

	@Override
	public FontRenderContext getFontRenderContext()
	{
		return _g2d.getFontRenderContext();
	}

	@Override
	public Paint getPaint()
	{
		return _g2d.getPaint();
	}

	@Override
	public void setPaint( final Paint paint )
	{
		_g2d.setPaint( paint );
	}

	@Override
	public Stroke getStroke()
	{
		return _g2d.getStroke();
	}

	@Override
	public void setStroke( final Stroke stroke )
	{
		_g2d.setStroke( stroke );
	}

	@Override
	public void setPaintMode()
	{
		_g2d.setPaintMode();
	}

	@Override
	public void setXORMode( final Color color )
	{
		_g2d.setXORMode( color );
	}

	@Override
	public AffineTransform getTransform()
	{
		return null;
	}

	@Override
	public void setTransform( final AffineTransform tx )
	{
		notImplemented();
	}

	@Override
	public void rotate( final double theta )
	{
		notImplemented();
	}

	@Override
	public void rotate( final double theta, final double x, final double y )
	{
		notImplemented();
	}

	@Override
	public void scale( final double sx, final double sy )
	{
		notImplemented();
	}

	@Override
	public void shear( final double shx, final double shy )
	{
		notImplemented();
	}

	@Override
	public void transform( final AffineTransform tx )
	{
		notImplemented();
	}

	@Override
	public void translate( final double tx, final double ty )
	{
		notImplemented();
	}

	@Override
	public void translate( final int x, final int y )
	{
		notImplemented();
	}

	@Override
	public void draw( final Shape s )
	{
		_g2d.draw( planeShape2ImageShape( s ) );
	}

	@Override
	public void fill( final Shape s )
	{
		_g2d.fill( planeShape2ImageShape( s ) );
	}

	@Override
	public void drawArc( final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle )
	{
		draw( new Arc2D.Double( (double)x, (double)y, (double)width, (double)height, (double)startAngle, (double)arcAngle, Arc2D.OPEN ) );
	}

	@Override
	public void fillArc( final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle )
	{
		fill( new Arc2D.Double( (double)x, (double)y, (double)width, (double)height, (double)startAngle, (double)arcAngle, Arc2D.OPEN ) );
	}

	@Override
	public void drawGlyphVector( final GlyphVector g, final float x, final float y )
	{
		notImplemented();
	}

	@Override
	public void drawImage( final BufferedImage img, final BufferedImageOp op, final int x, final int y )
	{
		notImplemented();
	}

	@Override
	public boolean drawImage( final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1, final int sy1, final int sx2, final int sy2, final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	@Override
	public boolean drawImage( final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1, final int sy1, final int sx2, final int sy2, final Color bgcolor, final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	@Override
	public boolean drawImage( final Image img, final int x, final int y, final Color bgcolor, final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	@Override
	public boolean drawImage( final Image img, final int x, final int y, final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	@Override
	public boolean drawImage( final Image img, final int x, final int y, final int width, final int height, final Color bgcolor, final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	@Override
	public boolean drawImage( final Image img, final int x, final int y, final int width, final int height, final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	@Override
	public boolean drawImage( final Image img, final AffineTransform xform, final ImageObserver obs )
	{
		notImplemented();
		return false;
	}

	@Override
	public void drawLine( final int x1, final int y1, final int x2, final int y2 )
	{
		final double[] p1 = plane2image( (double)x1, (double)y1 );
		final double[] p2 = plane2image( (double)x2, (double)y2 );

		_g2d.drawLine( (int)p1[ 0 ], (int)p1[ 1 ], (int)p2[ 0 ], (int)p2[ 1 ] );
	}

	@Override
	public void drawOval( final int x, final int y, final int width, final int height )
	{
		draw( new Ellipse2D.Double( (double)x, (double)y, (double)width, (double)height ) );
	}

	@Override
	public void fillOval( final int x, final int y, final int width, final int height )
	{
		fill( new Ellipse2D.Double( (double)x, (double)y, (double)width, (double)height ) );
	}

	@Override
	public void drawPolygon( final int[] xPoints, final int[] yPoints, final int nPoints )
	{
		final int[] imageX = new int[ nPoints ];
		final int[] imageY = new int[ nPoints ];

		for ( int i = 0 ; i < nPoints ; i++ )
		{
			final double[] p = plane2image( (double)xPoints[ i ], (double)yPoints[ i ] );

			imageX[ i ] = (int)p[ 0 ];
			imageY[ i ] = (int)p[ 1 ];
		}

		_g2d.drawPolygon( imageX, imageY, nPoints );
	}

	@Override
	public void fillPolygon( final int[] xPoints, final int[] yPoints, final int nPoints )
	{
		final int[] imageX = new int[ nPoints ];
		final int[] imageY = new int[ nPoints ];

		for ( int i = 0 ; i < nPoints ; i++ )
		{
			final double[] p = plane2image( (double)xPoints[ i ], (double)yPoints[ i ] );

			imageX[ i ] = (int)p[ 0 ];
			imageY[ i ] = (int)p[ 1 ];
		}

		_g2d.fillPolygon( imageX, imageY, nPoints );
	}

	@Override
	public void drawPolyline( final int[] xPoints, final int[] yPoints, final int nPoints )
	{
		final int[] imageX = new int[ nPoints ];
		final int[] imageY = new int[ nPoints ];

		for ( int i = 0 ; i < nPoints ; i++ )
		{
			final double[] p = plane2image( (double)xPoints[ i ], (double)yPoints[ i ] );

			imageX[ i ] = (int)p[ 0 ];
			imageY[ i ] = (int)p[ 1 ];
		}

		_g2d.drawPolyline( imageX, imageY, nPoints );
	}

	@Override
	public void drawRect( final int x, final int y, final int width, final int height )
	{
		draw( new Rectangle2D.Double( (double)x, (double)y, (double)width, (double)height ) );
	}

	@Override
	public void fillRect( final int x, final int y, final int width, final int height )
	{
		fill( new Rectangle2D.Double( (double)x, (double)y, (double)width, (double)height ) );
	}

	@Override
	public void clearRect( final int x, final int y, final int width, final int height )
	{
		notImplemented();
	}

	@Override
	public void drawRenderableImage( final RenderableImage img, final AffineTransform xform )
	{
		notImplemented();
	}

	@Override
	public void drawRenderedImage( final RenderedImage img, final AffineTransform xform )
	{
		notImplemented();
	}

	@Override
	public void drawRoundRect( final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight )
	{
		draw( new RoundRectangle2D.Double( (double)x, (double)y, (double)width, (double)height, (double)arcWidth, (double)arcHeight ) );
	}

	@Override
	public void fillRoundRect( final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight )
	{
		fill( new RoundRectangle2D.Double( (double)x, (double)y, (double)width, (double)height, (double)arcWidth, (double)arcHeight ) );
	}

	@Override
	public void drawString( final AttributedCharacterIterator iterator, final float x, final float y )
	{
		notImplemented();
	}

	@Override
	public void drawString( final AttributedCharacterIterator iterator, final int x, final int y )
	{
		notImplemented();
	}

	@Override
	public void drawString( final String str, final float x, final float y )
	{
		final double[] point2d = plane2image( (double)x, (double)y );
		_g2d.drawString( str, (float)point2d[ 0 ], (float)point2d[ 1 ] );
	}

	@Override
	public void drawString( final String str, final int x, final int y )
	{
		drawString( str, (float)x, (float)y );
	}
}
