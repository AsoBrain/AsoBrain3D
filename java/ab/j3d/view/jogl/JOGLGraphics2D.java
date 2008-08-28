/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2008
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
package ab.j3d.view.jogl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.nio.DoubleBuffer;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.j2d.TextRenderer;

/**
 * This class tries to imitate some functions of the java2d api in OpenGL using JOGL.
 *
 * @author  Jark Reijerink
 * @version $Revision$ $Date$
 */
public class JOGLGraphics2D
	extends Graphics2D
{
	/**
	 * Graphics2D object, needed for fonts
	 */
	private Graphics2D _g2d;

	/**
	 * Used for getting the size of the canvas.
	 */
	private GLAutoDrawable _gla;

	/**
	 * Used for displaying text using java2d and opengl, not accelerated.
	 */
	private TextRenderer _renderer = null;

	/**
	 * Construct new JOGL2dGraphics.
	 *
	 * @param   g2d     Used to get certain Graphics2D properties
	 * @param   gla     Used to get the size of the canvas
	 */
	public JOGLGraphics2D( final Graphics2D g2d , final GLAutoDrawable gla )
	{
		_g2d = g2d;
		_gla = gla;
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
	 * Start the ortographic projection conform the java2d grid (top left = 0,0)
	 */
	private void glBegin2D()
	{
		final GL gl = _gla.getGL();
		final DoubleBuffer vPort = DoubleBuffer.allocate( 4 );
		gl.glGetDoublev( GL.GL_VIEWPORT , vPort );
		gl.glMatrixMode( GL.GL_PROJECTION );

		// save projection settings
		gl.glPushMatrix();
		gl.glLoadIdentity();

		// Set orthographic projection: left, right, bottom, top, near, far
		gl.glOrtho( 0.0 , vPort.get( 2 ) , vPort.get( 3 ) , 0.0 , 0.0 , 1.0 );
		gl.glMatrixMode( GL.GL_MODELVIEW );

		// save model view settings
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glDisable( GL.GL_DEPTH_TEST );
		gl.glDisable( GL.GL_LIGHTING );
		gl.glDisable( GL.GL_CULL_FACE );

		final Color   color = getColor();
		final float[] argb  = color.getRGBComponents( null );
		gl.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );

		final Stroke stroke = getStroke();
		if( stroke instanceof BasicStroke )
		{
			gl.glLineWidth( ((BasicStroke)stroke).getLineWidth() );
		}
	}

	/**
	 * End the ortographic projection and restore previous settings.
	 */
	private void glEnd2D()
	{
		final GL gl = _gla.getGL();
		gl.glEnable( GL.GL_CULL_FACE );
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.glMatrixMode( GL.GL_PROJECTION );

		 // restore previous projection settings
		gl.glPopMatrix();
		gl.glMatrixMode( GL.GL_MODELVIEW );

		// restore previous model view settings
		gl.glPopMatrix();
		gl.glEnable( GL.GL_LIGHTING );
	}

	public Graphics create()
	{
		return notImplemented();
	}

	public void dispose()
	{
	}

	public GraphicsConfiguration getDeviceConfiguration()
	{
		return _g2d.getDeviceConfiguration();
	}

	public Object getRenderingHint( final RenderingHints.Key hintKey )
	{
		return _g2d.getRenderingHint( hintKey );
	}

	public RenderingHints getRenderingHints()
	{
		return _g2d.getRenderingHints();
	}

	public void addRenderingHints( final Map<?, ?> hints )
	{
		_g2d.addRenderingHints( hints );
	}

	public void setRenderingHint( final RenderingHints.Key hintKey , final Object hintValue )
	{
		_g2d.setRenderingHint( hintKey , hintValue );
	}

	public void setRenderingHints( final Map<?,?> hints )
	{
		_g2d.setRenderingHints( hints );
	}

	public Shape getClip()
	{
		return notImplemented();
	}

	public void setClip( final Shape clip )
	{
		notImplemented();
	}

	public void clip( final Shape s )
	{
		notImplemented();
	}

	public Rectangle getClipBounds()
	{
		return notImplemented();
	}

	public void setClip( final int x , final int y , final int width , final int height )
	{
		notImplemented();
	}

	public void clipRect( final int x , final int y , final int width , final int height )
	{
		notImplemented();
	}

	public boolean hit( final Rectangle rectangle , final Shape shape , final boolean onStroke )
	{
		notImplemented();
		return false;
	}

	public void copyArea( final int x , final int y , final int width , final int height , final int dx , final int dy )
	{
		notImplemented();
	}

	public Color getBackground()
	{
		return _g2d.getBackground();
	}

	public void setBackground( final Color color )
	{
		_g2d.setBackground( color );
	}

	public Color getColor()
	{
		return _g2d.getColor();
	}

	public void setColor( final Color c )
	{
		_g2d.setColor( c );
	}

	public Composite getComposite()
	{
		return _g2d.getComposite();
	}

	public void setComposite( final Composite composite )
	{
		_g2d.setComposite( composite );
	}

	public Font getFont()
	{
		return _g2d.getFont();
	}

	public void setFont( final Font font )
	{
		_g2d.setFont( font );
	}

	public FontMetrics getFontMetrics( final Font font )
	{
		return _g2d.getFontMetrics( font );
	}

	public FontRenderContext getFontRenderContext()
	{
		return _g2d.getFontRenderContext();
	}

	public Paint getPaint()
	{
		return _g2d.getPaint();
	}

	public void setPaint( final Paint paint )
	{
		_g2d.setPaint( paint );
	}

	public Stroke getStroke()
	{
		return _g2d.getStroke();
	}

	public void setStroke( final Stroke stroke )
	{
		_g2d.setStroke( stroke );
	}

	public void setPaintMode()
	{
		_g2d.setPaintMode();
	}

	public void setXORMode( final Color color )
	{
		_g2d.setXORMode( color );
	}

	public AffineTransform getTransform()
	{
		return null;
	}

	public void setTransform( final AffineTransform tx )
	{
		notImplemented();
	}

	public void rotate( final double theta )
	{
		notImplemented();
	}

	public void rotate( final double theta , final double x , final double y )
	{
		notImplemented();
	}

	public void scale( final double sx , final double sy )
	{
		notImplemented();
	}

	public void shear( final double shx , final double shy )
	{
		notImplemented();
	}

	public void transform( final AffineTransform tx )
	{
		notImplemented();
	}

	public void translate( final double tx , final double ty )
	{
		notImplemented();
	}

	public void translate( final int x, final int y )
	{
		notImplemented();
	}

	public void draw( final Shape shape )
	{
		final GL gl = _gla.getGL();

		glBegin2D();
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_LINE );
		drawFillImpl( gl , shape );
		glEnd2D();
	}

	public void fill( final Shape shape )
	{
		final GL gl = _gla.getGL();

		glBegin2D();
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_FILL);
		drawFillImpl( gl , shape );
		glEnd2D();
	}

	/**
	 * Implementation to draw/fill 2D shapes.
	 *
	 * @param   gl      GL context.
	 * @param   shape   Shape to render.
	 */
	private void drawFillImpl( final GL gl , final Shape shape )
	{
		if ( shape instanceof Line2D )
		{
			final Line2D line = (Line2D)shape;
			gl.glBegin( GL.GL_LINES );
			gl.glVertex2d( line.getX1(), line.getY1() );
			gl.glVertex2d( line.getX2(), line.getY2() );
			gl.glEnd();
		}
		else if ( shape instanceof Rectangle2D )
		{
			final Rectangle2D rectangle = (Rectangle2D)shape;
			glBegin2D();
			gl.glBegin( GL.GL_QUADS );
			gl.glVertex2d( rectangle.getMinX(), rectangle.getMinY() );
			gl.glVertex2d( rectangle.getMaxX(), rectangle.getMinY() );
			gl.glVertex2d( rectangle.getMaxX(), rectangle.getMaxY() );
			gl.glVertex2d( rectangle.getMinX(), rectangle.getMaxY() );
			gl.glEnd();
			glEnd2D();
		}
		else
		{
			boolean started = false;
			double  startX  = 0.0;
			double  startY  = 0.0;

			final double[] coordinates = new double[ 2 ];

			final PathIterator pathIterator = shape.getPathIterator( getTransform() , 1.0 );
			while ( !pathIterator.isDone() )
			{
				switch ( pathIterator.currentSegment( coordinates ) )
				{
					case PathIterator.SEG_MOVETO :
						if ( started )
							gl.glEnd();

						gl.glBegin( GL.GL_POLYGON );
						startX = coordinates[ 0 ];
						startY = coordinates[ 1 ];
						gl.glVertex2d( startX , startY );
						started = true;
						break;

					case PathIterator.SEG_CLOSE :
						if ( started )
						{
							gl.glEnd();
							gl.glVertex2d( startX , startY );
							started = false;
						}
						break;

					case PathIterator.SEG_LINETO :
						if ( started )
						{
							gl.glVertex2d( coordinates[ 0 ] , coordinates[ 1 ] );
						}
						break;
				}

				pathIterator.next();
			}

			if ( started )
			{
				gl.glEnd();
			}
		}
	}

	/**
	 * Draws a line from x1,y1 to x2,y2. Line width can be changed by using
	 * a BasicStroke in setStroke.
	 *
	 * @param x1    x coordinate of point 1
	 * @param y1    x coordinate of point 1
	 * @param x2    x coordinate of point 2
	 * @param y2    y coordinate of point 2
	 */
	public void drawLine( final int x1 , final int y1 , final int x2 , final int y2 )
	{
		final GL gl = _gla.getGL();
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_LINE );

		glBegin2D();
		gl.glBegin( GL.GL_LINES );
		gl.glVertex2i( x1 , y1 );
		gl.glVertex2i( x2 , y2 );
		gl.glEnd();
		glEnd2D();
	}

	public void drawPolyline( final int[] xPoints , final int[] yPoints , final int nPoints )
	{
		final GL gl = _gla.getGL();

		glBegin2D();
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_LINE );
		gl.glBegin( GL.GL_LINES );

		for( int i = 0 ; i < nPoints - 1 ; i++ )
		{
			gl.glVertex2i( xPoints[ i     ] , yPoints[ i     ] );
			gl.glVertex2i( xPoints[ i + 1 ] , yPoints[ i + 1 ] );
		}

		gl.glEnd();
		glEnd2D();
	}

	public void drawPolygon( final int[] xPoints , final int[] yPoints , final int nPoints )
	{
		final GL gl = _gla.getGL();

		glBegin2D();
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_LINE );
		gl.glBegin( GL.GL_POLYGON );

		for( int i = 0 ; i < nPoints ; i++ )
			gl.glVertex2i( xPoints[ i ] , yPoints[ i ] );

		gl.glEnd();
		glEnd2D();
	}

	public void fillPolygon( final int[] xPoints , final int[] yPoints , final int nPoints )
	{
		final GL gl = _gla.getGL();

		glBegin2D();
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_FILL);
		gl.glBegin( GL.GL_POLYGON );

		for ( int i = 0 ; i < nPoints ; i++ )
			gl.glVertex2i( xPoints[ i ] , yPoints[ i ] );

		gl.glEnd();
		glEnd2D();
	}

	public void drawArc( final int x , final int y , final int width , final int height , final int startAngle , final int arcAngle )
	{
		draw( new Arc2D.Double( (double)x , (double)y , (double)width , (double)height , (double)startAngle , (double)arcAngle , Arc2D.OPEN ) );
	}

	public void fillArc( final int x , final int y , final int width , final int height , final int startAngle , final int arcAngle )
	{
		fill( new Arc2D.Double( (double)x , (double)y , (double)width , (double)height , (double)startAngle , (double)arcAngle , Arc2D.OPEN ) );
	}

	public void drawGlyphVector( final GlyphVector g , final float x, final float y )
	{
		notImplemented();
	}

	public void drawImage( final BufferedImage img , final BufferedImageOp op , final int x , final int y )
	{
		notImplemented();
	}

	public boolean drawImage( final Image img , final int dx1 , final int dy1 , final int dx2 , final int dy2 , final int sx1 , final int sy1 , final int sx2 , final int sy2 , final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	public boolean drawImage( final Image img , final int dx1 , final int dy1 , final int dx2 , final int dy2 , final int sx1 , final int sy1 , final int sx2 , final int sy2 , final Color bgcolor , final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	public boolean drawImage( final Image img , final int x , final int y , final Color bgcolor , final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	public boolean drawImage( final Image img , final int x , final int y , final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	public boolean drawImage( final Image img , final int x , final int y , final int width , final int height , final Color bgcolor , final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	public boolean drawImage( final Image img , final int x , final int y , final int width , final int height , final ImageObserver observer )
	{
		notImplemented();
		return false;
	}

	public boolean drawImage( final Image img , final AffineTransform xform , final ImageObserver obs )
	{
		notImplemented();
		return false;
	}

	public void drawOval( final int x , final int y , final int width , final int height )
	{
		draw( new Ellipse2D.Double( (double)x , (double)y , (double)width , (double)height ) );
	}

	public void fillOval( final int x , final int y , final int width , final int height )
	{
		fill( new Ellipse2D.Double( (double)x , (double)y , (double)width , (double)height ) );
	}

	public void drawRect( final int x , final int y , final int width , final int height )
	{
		final GL gl = _gla.getGL();

		glBegin2D();
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_LINE );
		gl.glBegin( GL.GL_QUADS );
		gl.glVertex2i( x         , y          );
		gl.glVertex2i( x + width , y          );
		gl.glVertex2i( x + width , y + height );
		gl.glVertex2i( x         , y + height );
		gl.glEnd();
		glEnd2D();
	}

	public void fillRect( final int x , final int y , final int width , final int height )
	{
		final GL gl = _gla.getGL();

		glBegin2D();
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_FILL );
		gl.glBegin( GL.GL_QUADS );
		gl.glVertex2i( x         , y          );
		gl.glVertex2i( x + width , y          );
		gl.glVertex2i( x + width , y + height );
		gl.glVertex2i( x         , y + height );
		gl.glEnd();
		glEnd2D();
	}

	public void clearRect( final int x , final int y , final int width , final int height )
	{
		notImplemented();
	}

	public void drawRenderableImage( final RenderableImage img , final AffineTransform xform )
	{
		notImplemented();
	}

	public void drawRenderedImage( final RenderedImage img , final AffineTransform xform )
	{
		notImplemented();
	}

	public void drawRoundRect( final int x , final int y , final int width , final int height , final int arcWidth , final int arcHeight )
	{
		draw( new RoundRectangle2D.Double( (double)x , (double)y , (double)width , (double)height , (double)arcWidth , (double)arcHeight ) );
	}

	public void fillRoundRect( final int x , final int y , final int width , final int height , final int arcWidth , final int arcHeight )
	{
		fill( new RoundRectangle2D.Double( (double)x , (double)y , (double)width , (double)height , (double)arcWidth , (double)arcHeight ) );
	}

	public void drawString( final AttributedCharacterIterator iterator , final float x , final float y )
	{
		notImplemented();
	}

	public void drawString( final AttributedCharacterIterator iterator , final int x , final int y )
	{
		notImplemented();
	}

	public void drawString( final String str , final int x , final int y )
	{
		//create renderer if renderer has not been created already.
		if ( _renderer == null )
		    _renderer = new TextRenderer( getFont() );

		_renderer.setColor( getColor() );
		_renderer.beginRendering( _gla.getWidth() , _gla.getHeight() );
		// Draw text
		_renderer.draw( str , x , _gla.getHeight() - y );
		// Clean up rendering
		_renderer.endRendering();
	}

	public void drawString( final String str , final float x , final float y )
	{
		drawString( str , (int) x , (int) y );
	}
}
