/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
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
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.j2d.TextRenderer;

/**
 * This class tries to imitate some functions of the java2d api in OpenGL using JOGL.
 *
 * The following methods are currently implemented:
 * - drawString( final String str , final int x , final int y )
 * - drawLine( final int x1 , final int y1 , final int x2 , final int y2 )
 * - setStroke( final Stroke s )
 * - setColor( final Color color )
 * - getColor()
 * - setFont()
 * - getFont()
 * - getFontMetrics()
 *
 * @author  Jark Reijerink
 * @version $Revision$ $Date$
 */
public class JOGL2dGraphics extends Graphics2D
{
	/**
	 * Graphics2D object, needed for fonts
	 */
	private Graphics2D _g2d;

	/**
	 * Variable in which font is stored, will only be used if _renderer != null.
	 */
	private Font _font;

	/**
	 * Used for getting the size of the canvas.
	 */
	private GLAutoDrawable _gla;

	/**
	 * Used for drawing text aswell as shapes and lines.
	 */
	private Color _color;

	/**
	 * Used for displaying text using java2d and opengl, not accelerated.
	 */
	private TextRenderer _renderer = null;

	/**
	 * Construct new JOGL2dGraphics.
	 *
	 * @param g2d           Used to get certain Graphics2D properties
	 * @param gla           Used to get the size of the canvas
	 * @param fastFonts     Set to false if you want accurate fonts, set to true if you want fast fonts.
	 */
	public JOGL2dGraphics( final Graphics2D g2d , final GLAutoDrawable gla , final boolean fastFonts)
	{
		_g2d   = g2d;
		_font  = g2d.getFont();
		_gla   = gla;
		_color = Color.WHITE;
		if(!fastFonts)
			_renderer = new TextRenderer( getFont() );
	}

	/**
	 * Start the ortographic projection conform the java2d grid (top left = 0,0)
	 */
	private void glBegin2D()
	{
		final GL gl = GLU.getCurrentGL();
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
	}

	/**
	 * End the ortographic projection and restore previous settings.
	 */
	private void glEnd2D()
	{
		final GL gl = GLU.getCurrentGL();
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
		return this;
	}

	public void draw( final Shape s )
	{
	}

	public boolean drawImage( final Image img , final AffineTransform xform , final ImageObserver obs )
	{
		return false;
	}

	public void drawImage( final BufferedImage img , final BufferedImageOp op , final int x , final int y )
	{
	}

	public void drawRenderedImage( final RenderedImage img , final AffineTransform xform )
	{
	}

	public void drawRenderableImage( final RenderableImage img , final AffineTransform xform )
	{
	}

	/**
	 * This function draws a String on the OpenGL canvas.
	 *
	 * If there is a _renderer it draws a String using java2d underneath, which is slow if
	 * the java2d.opengl pipeline is not present.
	 *
	 * If there isn't a _renderer present it will use bitmap drawing to draw the font. This
	 * method only draws the font with the BITMAP_HELVETICA_12 font.
	 *
	 * @param str   String to draw on the canvas.
	 * @param x     Horizontal postion of the text.
	 * @param y     Vertical position of the text.
	 */
	public void drawString( final String str , final int x , final int y )
	{
		glBegin2D();
		if( _renderer !=null)
		{
			_renderer.beginRendering( _gla.getWidth() , _gla.getHeight() );
			// Draw text
			_renderer.draw( str , x , _gla.getHeight() - y );
			// Clean up rendering
			_renderer.endRendering();
		}
		else{
			final GL   gl   = GLU.getCurrentGL();
			final GLUT glut = new GLUT();
			gl.glRasterPos2i( x , y );
			glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12 , str );
		}
		glEnd2D();
	}

	public void drawString( final String str , final float x , final float y )
	{
		drawString( str , (int) x , (int) y );
	}

	public void drawString( final AttributedCharacterIterator iterator , final int x , final int y )
	{
	}

	public boolean drawImage( final Image img , final int x , final int y ,  final ImageObserver observer )
	{
		return false;
	}

	public boolean drawImage( final Image img , final int x , final int y ,  final int width ,  final int height ,  final ImageObserver observer )
	{
		return false;
	}

	public boolean drawImage( final Image img , final int x , final int y , final Color bgcolor ,  final ImageObserver observer )
	{
		return false;
	}

	public boolean drawImage( final Image img , final int x , final int y , final int width ,  final int height , final Color bgcolor , final ImageObserver observer )
	{
		return false;
	}

	public boolean drawImage( final Image img , final int dx1 , final int dy1 , final int dx2 , final int dy2 , final int sx1 , final int sy1 , final int sx2 , final int sy2 , final ImageObserver observer )
	{
		return false;
	}

	public boolean drawImage( final Image img , final int dx1 , final int dy1 , final int dx2 , final int dy2 , final int sx1 , final int sy1 , final int sx2 , final int sy2 , final Color bgcolor , final ImageObserver observer )
	{
		return false;
	}

	public void dispose()
	{
	}

	public void drawString( final AttributedCharacterIterator iterator , final float x , final float y )
	{
	}

	public void drawGlyphVector( final GlyphVector g , final float x , final float y )
	{
	}

	public void fill( final Shape s )
	{
	}

	public boolean hit( final Rectangle rect , final Shape s , final boolean onStroke )
	{
		return false;
	}

	public GraphicsConfiguration getDeviceConfiguration()
	{
		return null;
	}

	public void setComposite( final Composite comp )
	{
	}

	public void setPaint( final Paint paint )
	{
	}

	/**
	 * This method currently only supports the BasicStroke stroke.
	 * The gl.glLineWidth parameter only applies to GL.GL_LINES so it will only
	 * work if lines are drawn.
	 *
	 * @param s     Stroke that needs to be set
	 */
	public void setStroke( final Stroke s )
	{
		if( s instanceof BasicStroke )
		{
			final BasicStroke t = (BasicStroke) s;
			final GL gl = GLU.getCurrentGL();
			gl.glLineWidth( t.getLineWidth() );
		}
	}

	public void setRenderingHint( final RenderingHints.Key hintKey , final Object hintValue )
	{
	}

	public Object getRenderingHint( final RenderingHints.Key hintKey )
	{
		return null;
	}

	public void setRenderingHints( final  Map<?,?> hints )
	{
	}

	public void addRenderingHints( final Map<?,?> hints )
	{
	}

	public RenderingHints getRenderingHints()
	{
		return null;
	}

	public void translate( final int x , final int y )
	{
	}

	public Color getColor()
	{
		return _color;
	}

	/**
	 * Sets the color to be used by OpenGL. Alpha is supported.
	 *
	 * @param c     Color to be converted
	 */

	public void setColor( final Color c )
	{
		final GL gl = GLU.getCurrentGL();
		_color = c;
		final float[] argb = _color.getRGBComponents( null );
		gl.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );
	}

	public void setPaintMode()
	{
	}

	public void setXORMode( final Color c1 )
	{
	}

	public Font getFont()
	{
		return _font;
	}

	public void setFont( final Font font )
	{
		_font = font;
	}

	public FontMetrics getFontMetrics( final Font f )
	{
		return _g2d.getFontMetrics( f );
	}

	public Rectangle getClipBounds()
	{
		return null;
	}

	public void clipRect( final int x , final int y , final int width , final int height )
	{
	}

	public void setClip( final int x , final int y , final int width , final int height )
	{
	}

	public Shape getClip()
	{
		return null;
	}

	public void setClip( final Shape clip )
	{
	}

	public void copyArea( final int x , final int y , final int width , final int height , final int dx , final int dy )
	{
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
		final GL gl = GLU.getCurrentGL();
		glBegin2D();
		gl.glBegin( GL.GL_LINES );
		gl.glVertex2i( x1 , y1 );
		gl.glVertex2i( x2 , y2 );
		gl.glEnd();
		glEnd2D();
	}

	/**
	 * Draws a filled rectangle on the OpenGL Canvas.
	 *
	 * @param x         horizontal starting point
	 * @param y         vertical starting point
	 * @param width     width of the rectangle
	 * @param height    height of the rectangle
	 */
	public void fillRect( final int x , final int y , final int width , final int height )
	{
		final GL gl = GLU.getCurrentGL();
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
	}

	public void drawRoundRect( final int x , final int y , final int width , final int height , final int arcWidth , final int arcHeight )
	{
	}

	public void fillRoundRect( final int x , final int y , final int width , final int height , final int arcWidth , final int arcHeight )
	{
	}

	public void drawOval( final int x , final int y , final int width , final int height )
	{
	}

	public void fillOval( final int x , final int y , final int width , final int height )
	{
	}

	public void drawArc( final int x , final int y , final int width , final int height , final int startAngle , final int arcAngle )
	{
	}

	public void fillArc( final int x , final int y , final int width , final int height , final int startAngle , final int arcAngle )
	{
	}

	public void drawPolyline( final int[] xPoints , final int[] yPoints , final int nPoints )
	{
	}

	public void drawPolygon( final int[] xPoints , final int[] yPoints , final int nPoints )
	{
	}

	public void fillPolygon( final int[] xPoints , final int[] yPoints , final int nPoints )
	{
	}

	public void translate( final double tx , final double ty )
	{
	}

	public void rotate( final double theta )
	{
	}

	public void rotate( final double theta , final double x , final double y )
	{
	}

	public void scale( final double sx , final double sy )
	{
	}

	public void shear( final double shx , final double shy )
	{
	}

	public void transform( final AffineTransform affineTransform )
	{
	}

	public void setTransform( final AffineTransform affineTransform )
	{
	}

	public AffineTransform getTransform()
	{
		return null;
	}

	public Paint getPaint()
	{
		return null;
	}

	public Composite getComposite()
	{
		return null;
	}

	public void setBackground( final Color color )
	{
	}

	public Color getBackground()
	{
		return null;
	}

	public Stroke getStroke()
	{
		return null;
	}

	public void clip( final Shape s )
	{
	}

	public FontRenderContext getFontRenderContext()
	{
		return null;
	}
}