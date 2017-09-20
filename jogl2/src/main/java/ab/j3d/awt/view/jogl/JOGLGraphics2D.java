/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
package ab.j3d.awt.view.jogl;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.nio.*;
import java.text.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.fixedfunc.*;

import com.jogamp.opengl.util.awt.*;

/**
 * This class tries to imitate some functions of the java2d api in OpenGL using JOGL.
 *
 * @author  Jark Reijerink
 */
public class JOGLGraphics2D
	extends Graphics2D
{
	/**
	 * Default font render context.
	 */
	private static final FontRenderContext DEFAULT_FONT_RENDER_CONTEXT = new FontRenderContext( null , false , false );

	/**
	 * Used for getting the size of the canvas.
	 */
	private GLAutoDrawable _gla;

	/**
	 * Used for displaying text using java2d and opengl, not accelerated.
	 */
	private TextRenderer _renderer = null;

	/**
	 * Current clip shape.
	 */
	private Shape _clip = null;

	/**
	 * Current transform.
	 */
	private AffineTransform _transform = new AffineTransform();

	/**
	 * Current background.
	 */
	private Color _background = null;

	/**
	 * Current paint.
	 */
	private Paint _paint = null;

	/**
	 * Current stroke.
	 */
	private Stroke _stroke = null;

	/**
	 * Current composite.
	 */
	private Composite _composite = null;

	/**
	 * Current font.
	 */
	private Font _font = null;

	/**
	 * Rendering hints.
	 */
	private final RenderingHints _renderingHints = new RenderingHints( Collections.<RenderingHints.Key,Object>emptyMap() );

	/**
	 * Construct new JOGL2dGraphics.
	 *
	 * @param   gla     Used to get the size of the canvas
	 */
	public JOGLGraphics2D( final GLAutoDrawable gla )
	{
		_gla  = gla;
		reset();
	}

	/**
	 * Resets the fonts and colors of the graphics context to their defaults.
	 */
	public void reset()
	{
		_font = new Font( Font.SANS_SERIF , Font.PLAIN , 12 );
		_paint = Color.BLACK;
		_background = Color.GRAY;
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
		final GL2 gl2 = gl.getGL2();

		final DoubleBuffer vPort = DoubleBuffer.allocate( 4 );
		gl2.glGetDoublev( GL.GL_VIEWPORT, vPort );
		gl2.glMatrixMode( GLMatrixFunc.GL_PROJECTION );

		// save projection settings
		gl2.glPushMatrix();
		gl2.glLoadIdentity();

		// Set orthographic projection: left, right, bottom, top, near, far
		gl2.glOrtho( 0.0 , vPort.get( 2 ) , vPort.get( 3 ) , 0.0 , 0.0 , 1.0 );
		gl2.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );

		// save model view settings
		gl2.glPushMatrix();
		gl2.glLoadIdentity();
		gl.glDisable( GL.GL_DEPTH_TEST );
		gl.glDisable( GLLightingFunc.GL_LIGHTING );
		gl.glDisable( GL.GL_CULL_FACE );

		/* Enable blending to support transparency. */
		gl.glEnable( GL.GL_BLEND );
		gl.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );

		final Color   color = getColor();
		final float[] argb  = color.getRGBComponents( null );
		gl2.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );

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
		final GL2 gl2 = gl.getGL2();

		gl.glEnable( GL.GL_CULL_FACE );
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl2.glMatrixMode( GLMatrixFunc.GL_PROJECTION );

		 // restore previous projection settings
		gl2.glPopMatrix();
		gl2.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );

		// restore previous model view settings
		gl2.glPopMatrix();
		gl.glEnable( GLLightingFunc.GL_LIGHTING );
	}

	/**
	 * Normalizes the given coordinate for the purpose of
	 * {@link RenderingHints#KEY_STROKE_CONTROL}.
	 *
	 * @param   coordinate  Coordinate to be normalized.
	 *
	 * @return  Normalized coordinate, no more than 0.5 units from the original.
	 */
	private float normalize( final float coordinate )
	{
		final float result;
		final Object strokeControl = getRenderingHint( RenderingHints.KEY_STROKE_CONTROL );
		if ( strokeControl == RenderingHints.VALUE_STROKE_PURE )
		{
			result = coordinate;
		}
		else
		{
			result = (float)(int)coordinate + 0.5f;
		}
		return result;
	}

	/**
	 * Normalizes the given coordinate for the purpose of
	 * {@link RenderingHints#KEY_STROKE_CONTROL}.
	 *
	 * @param   coordinate  Coordinate to be normalized.
	 *
	 * @return  Normalized coordinate, no more than 0.5 units from the original.
	 */
	private double normalize( final double coordinate )
	{
		final double result;
		final Object strokeControl = getRenderingHint( RenderingHints.KEY_STROKE_CONTROL );
		if ( strokeControl == RenderingHints.VALUE_STROKE_PURE )
		{
			result = coordinate;
		}
		else
		{
			result = Math.floor( coordinate ) + 0.5;
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
		_gla = null;

		final TextRenderer textRenderer = _renderer;
		if ( textRenderer != null )
		{
			if ( GLContext.getCurrent() != null )
			{
				textRenderer.dispose();
			}

			_renderer = null;
		}
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration()
	{
		return notImplemented();
	}

	@Override
	public Object getRenderingHint( final RenderingHints.Key hintKey )
	{
		return _renderingHints.get( hintKey );
	}

	@Override
	public RenderingHints getRenderingHints()
	{
		return _renderingHints;
	}

	@Override
	public void addRenderingHints( final Map<?,?> hints )
	{
		_renderingHints.putAll( hints );
	}

	@Override
	public void setRenderingHint( final RenderingHints.Key hintKey , final Object hintValue )
	{
		_renderingHints.put( hintKey , hintValue  );
	}

	@Override
	public void setRenderingHints( final Map<?,?> hints )
	{
		_renderingHints.clear();
		_renderingHints.putAll( hints );
	}

	@Override
	public void copyArea( final int x , final int y , final int width , final int height , final int dx , final int dy )
	{
		notImplemented();
	}

	@Override
	public Color getBackground()
	{
		return _background;
	}

	@Override
	public void setBackground( final Color color )
	{
		_background = color;
	}

	@Override
	public Color getColor()
	{
		final Paint p = getPaint();
		return ( p instanceof Color ) ? (Color)p : null;
	}

	@Override
	public void setColor( final Color c )
	{
		setPaint( c );
	}

	@Override
	public Paint getPaint()
	{
		return _paint;
	}

	@Override
	public void setPaint( final Paint paint )
	{
		_paint = paint;
	}

	@Override
	public Stroke getStroke()
	{
		return _stroke;
	}

	@Override
	public void setStroke( final Stroke stroke )
	{
		_stroke = stroke;
	}

	@Override
	public void setPaintMode()
	{
		notImplemented();
	}

	@Override
	public void setXORMode( final Color color )
	{
		notImplemented();
	}

	@Override
	public Shape getClip()
	{
		return _clip;
	}

	@Override
	public void setClip( final Shape clip )
	{
		_clip = clip;
	}

	@Override
	public void clip( final Shape clip )
	{
		if ( clip != null )
		{
			final Shape previousClip = _clip;
			if ( previousClip != null )
			{
				final Area area = new Area( previousClip );
				area.intersect( new Area( clip ) );
				setClip( area );
			}
			else
			{
				setClip( clip );
			}
		}
	}

	@Override
	public Rectangle getClipBounds()
	{
		final Shape clip = getClip();
		return ( clip == null ) ? null : clip.getBounds();
	}

	@Override
	public void clipRect( final int x , final int y , final int width , final int height )
	{
		clip( new Rectangle2D.Float( (float)x , (float)y , (float)width , (float)height ) );
	}

	@Override
	public void setClip( final int x , final int y , final int width , final int height )
	{
		setClip( new Rectangle2D.Float( (float)x , (float)y , (float)width , (float)height ) );
	}

	@Override
	public AffineTransform getTransform()
	{
		return _transform;
	}

	@Override
	public void setTransform( final AffineTransform transform )
	{
		_transform = transform;
	}

	@Override
	public void translate( final int x , final int y )
	{
		translate( (double)x , (double)y );
	}

	@Override
	public void translate( final double tx, final double ty )
	{
		transform( AffineTransform.getTranslateInstance( tx , ty ) );
	}

	@Override
	public void rotate( final double theta )
	{
		transform( AffineTransform.getRotateInstance( theta ) );
	}

	@Override
	public void rotate( final double theta, final double x, final double y )
	{
		transform( AffineTransform.getRotateInstance( theta , x , y ) );
	}

	@Override
	public void scale( final double sx, final double sy )
	{
		transform( AffineTransform.getScaleInstance( sx , sy ) );
	}

	@Override
	public void shear( final double shx , final double shy )
	{
		transform( AffineTransform.getShearInstance( shx , shy ) );
	}

	@Override
	public void transform( final AffineTransform tx )
	{
		final AffineTransform transform = new AffineTransform( getTransform() );
		transform.concatenate( tx );
		setTransform( transform );
	}

	@Override
	public void draw( final Shape shape )
	{
		final GL gl = _gla.getGL();
		final GL2 gl2 = gl.getGL2();

		glBegin2D();

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
					{
						gl2.glEnd();
					}

					gl2.glBegin( GL.GL_LINE_STRIP );
					startX = coordinates[ 0 ];
					startY = coordinates[ 1 ];
					gl2.glVertex2d( normalize( startX ), normalize( startY ) );
					started = true;
					break;

				case PathIterator.SEG_CLOSE :
					if ( started )
					{
						gl2.glVertex2d( normalize( startX ), normalize( startY ) );
						gl2.glEnd();
						started = false;
					}
					break;

				case PathIterator.SEG_LINETO :
					if ( started )
					{
						gl2.glVertex2d( normalize( coordinates[ 0 ] ), normalize( coordinates[ 1 ] ) );
					}
					break;
			}

			pathIterator.next();
		}

		if ( started )
		{
			gl2.glEnd();
		}

		glEnd2D();
	}

	@Override
	public void fill( final Shape shape )
	{
		final GL gl = _gla.getGL();
		final GL2 gl2 = gl.getGL2();

		glBegin2D();

		if ( shape instanceof Line2D )
		{
			final Line2D line = (Line2D)shape;
			gl2.glBegin( GL.GL_LINES );
			gl2.glVertex2d( line.getX1(), line.getY1() );
			gl2.glVertex2d( line.getX2(), line.getY2() );
			gl2.glEnd();
		}
		else if ( shape instanceof Rectangle2D )
		{
			final Rectangle2D rectangle = (Rectangle2D)shape;
			glBegin2D();
			gl2.glBegin( GL2.GL_QUADS );
			gl2.glVertex2d( rectangle.getMinX(), rectangle.getMinY() );
			gl2.glVertex2d( rectangle.getMaxX(), rectangle.getMinY() );
			gl2.glVertex2d( rectangle.getMaxX(), rectangle.getMaxY() );
			gl2.glVertex2d( rectangle.getMinX(), rectangle.getMaxY() );
			gl2.glEnd();
			glEnd2D();
		}
		else
		{
			// TODO: Support concave shapes, i.e. use tesselator.

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
						{
							gl2.glEnd();
						}

						gl2.glBegin( GL2.GL_POLYGON );
						startX = coordinates[ 0 ];
						startY = coordinates[ 1 ];
						gl2.glVertex2d( startX , startY );
						started = true;
						break;

					case PathIterator.SEG_CLOSE :
						if ( started )
						{
							gl2.glVertex2d( startX , startY );
							gl2.glEnd();
							started = false;
						}
						break;

					case PathIterator.SEG_LINETO :
						if ( started )
						{
							gl2.glVertex2d( coordinates[ 0 ] , coordinates[ 1 ] );
						}
						break;
				}

				pathIterator.next();
			}

			if ( started )
			{
				gl2.glEnd();
			}
		}

		glEnd2D();
	}

	@Override
	public Composite getComposite()
	{
		return _composite;
	}

	@Override
	public void setComposite( final Composite composite )
	{
		_composite = composite;
	}

	@Override
	public Font getFont()
	{
		return _font;
	}

	@Override
	public void setFont( final Font font )
	{
		_font = font;
	}

	@Override
	public FontMetrics getFontMetrics( final Font font )
	{
		final TextRenderer textRenderer = getTextRenderer();
		final LineMetrics lineMetrics = font.getLineMetrics( "", textRenderer.getFontRenderContext() );

		return new FontMetrics( font )
		{
			@Override
			public int getAscent()
			{
				return (int)lineMetrics.getAscent();
			}

			@Override
			public int getDescent()
			{
				return (int)lineMetrics.getDescent();
			}

			@Override
			public int getLeading()
			{
				return (int)lineMetrics.getLeading();
			}

			@Override
			public int getMaxAdvance()
			{
				return -1;
			}

			@Override
			public int charWidth( final char ch )
			{
				final Rectangle2D bounds = font.getStringBounds( new char[] { ch }, 0, 0, _renderer.getFontRenderContext() );
				return (int)bounds.getWidth();
			}

			@Override
			public int charsWidth( final char[] data, final int off, final int len )
			{
				final Rectangle2D bounds = font.getStringBounds( data, off, off + len, _renderer.getFontRenderContext() );
				return (int)bounds.getWidth();
			}
		};
	}

	@Override
	public FontRenderContext getFontRenderContext()
	{
		return DEFAULT_FONT_RENDER_CONTEXT;
	}

	@Override
	public void drawArc( final int x , final int y , final int width , final int height , final int startAngle , final int arcAngle )
	{
		draw( new Arc2D.Float( (float)x , (float)y , (float)width , (float)height , (float)startAngle , (float)arcAngle , Arc2D.OPEN ) );
	}

	@Override
	public void fillArc( final int x , final int y , final int width , final int height , final int startAngle , final int arcAngle )
	{
		fill( new Arc2D.Float( (float)x , (float)y , (float)width , (float)height , (float)startAngle , (float)arcAngle , Arc2D.OPEN ) );
	}

	@Override
	public void drawGlyphVector( final GlyphVector glyphVector , final float x , final float y )
	{
		fill( glyphVector.getOutline( x , y ) );
	}

	@Override
	public void drawImage( final BufferedImage image , final BufferedImageOp op , final int x , final int y )
	{
		BufferedImage result = op.createCompatibleDestImage( image , image.getColorModel() );
		result = op.filter( image , result );
		drawImage( result , x , y , null );
	}

	@Override
	public boolean drawImage( final Image image , final int x , final int y , final ImageObserver observer )
	{
		return drawImage( image , x , y , null , observer );
	}

	@Override
	public boolean drawImage( final Image image , final int x , final int y , final int width , final int height , final ImageObserver observer )
	{
		return drawImage( image , x , y , width , height , null , observer );
	}

	@Override
	public boolean drawImage( final Image image , final int x , final int y , final Color bgcolor , final ImageObserver observer )
	{
		return drawImage( image , x , y , image.getWidth( observer ) , image.getHeight( observer ) , bgcolor , observer );
	}

	@Override
	public boolean drawImage( final Image image , final int x , final int y , final int width , final int height , final Color bgcolor , final ImageObserver observer )
	{
		final boolean result;
		if ( ( width > 0 ) && ( height > 0 ) )
		{
			final Paint oldPaint = getPaint();
			setPaint( bgcolor );
			fillRect( x, y, x + width - 1 - x + 1, y + height - 1 - y + 1 );
			setPaint( oldPaint );

			final AffineTransform transform = AffineTransform.getTranslateInstance( (double)x , (double)y );
			transform.scale( (double)width / (double)image.getWidth( observer ) , (double)height / (double)image.getHeight( observer ) );
			result = drawImage( image, transform , observer );
		}
		else
		{
			result = true;
		}

		return result;
	}

	@Override
	public boolean drawImage( final Image image , final int dx1 , final int dy1 , final int dx2 , final int dy2 , final int sx1 , final int sy1 , final int sx2 , final int sy2 , final ImageObserver observer )
	{
		return drawImage( image , dx1 , dy1 , dx2 , dy2 , sx1 , sy1 , sx2 , sy2 , null , observer );
	}

	@Override
	public boolean drawImage( final Image image , final int dx1 , final int dy1 , final int dx2 , final int dy2 , final int sx1 , final int sy1 , final int sx2 , final int sy2 , final Color bgcolor , final ImageObserver observer )
	{
		final boolean result;

		if ( ( dx2 >= dx1 ) && ( dy2 >= dy1 ) && ( sx2 >= sx1 ) && ( sy2 >= sy1 ) )
		{
			final Paint oldPaint = getPaint();
			setPaint( bgcolor );
			fillRect( dx1, dy1, dx2 - dx1 + 1, dy2 - dy1 + 1 );
			setPaint( oldPaint );

			final double dwidth  = (double)( dx2 - dx1 + 1 );
			final double dheight = (double)( dy2 - dy1 + 1 );
			final double swidth  = (double)( sx2 - sx1 + 1 );
			final double sheight = (double)( sy2 - sy1 + 1 );

			final double scalex = dwidth / swidth;
			final double scaley = dheight / sheight;
			final double transx = (double)dx1 - (double)sx1 * scalex;
			final double transy = (double)dy1 - (double)sy1 * scaley;

			final AffineTransform transform = AffineTransform.getTranslateInstance( transx , transy );
			transform.scale( scalex , scaley );
			result = drawImage( image , transform , observer );
		}
		else
		{
			result = true;
		}

		return result;
	}

	@Override
	public boolean drawImage( final Image img , final AffineTransform xform , final ImageObserver obs )
	{
		notImplemented();
		return false;
	}

	@Override
	public void drawLine( final int x1 , final int y1 , final int x2 , final int y2 )
	{
		final GL gl = _gla.getGL();
		final GL2 gl2 = gl.getGL2();
		glBegin2D();
		gl2.glBegin( GL.GL_LINES );
		gl2.glVertex2f( normalize( (float)x1 ), normalize( (float)y1 ) );
		gl2.glVertex2f( normalize( (float)x2 ), normalize( (float)y2 ) );
		gl2.glEnd();
		glEnd2D();
	}

	@Override
	public void drawOval( final int x, final int y, final int width, final int height )
	{
		draw( new Ellipse2D.Float( (float)x , (float)y , (float)width , (float)height ) );
	}

	@Override
	public void fillOval( final int x , final int y , final int width , final int height )
	{
		fill( new Ellipse2D.Float( (float)x , (float)y , (float)width , (float)height ) );
	}

	@Override
	public void drawPolyline( final int[] xPoints , final int[] yPoints , final int nPoints )
	{
		final GL gl = _gla.getGL();
		final GL2 gl2 = gl.getGL2();

		glBegin2D();
		gl2.glBegin( GL.GL_LINE_STRIP );

		for ( int i = 0; i < nPoints; i++ )
		{
			gl2.glVertex2f( normalize( (float)xPoints[ i ] ), normalize( (float)yPoints[ i ] ) );
		}

		gl2.glEnd();
		glEnd2D();
	}

	@Override
	public void drawPolygon( final int[] xPoints , final int[] yPoints , final int nPoints )
	{
		final GL gl = _gla.getGL();
		final GL2 gl2 = gl.getGL2();

		glBegin2D();
		gl2.glBegin( GL.GL_LINE_LOOP );

		for ( int i = 0; i < nPoints; i++ )
		{
			gl2.glVertex2f( normalize( (float)xPoints[ i ] ), normalize( (float)yPoints[ i ] ) );
		}

		gl2.glEnd();
		glEnd2D();
	}

	@Override
	public void fillPolygon( final int[] xPoints , final int[] yPoints , final int nPoints )
	{
		final GL gl = _gla.getGL();
		final GL2 gl2 = gl.getGL2();

		glBegin2D();
		gl2.glBegin( GL2.GL_POLYGON );

		for ( int i = 0 ; i < nPoints ; i++ )
		{
			gl2.glVertex2i( xPoints[ i ], yPoints[ i ] );
		}

		gl2.glEnd();
		glEnd2D();
	}

	@Override
	public void drawRect( final int x , final int y , final int width , final int height )
	{
		final GL gl = _gla.getGL();
		final GL2 gl2 = gl.getGL2();

		glBegin2D();
		gl2.glBegin( GL.GL_LINE_LOOP );
		gl2.glVertex2f( normalize( (float)( x         ) ), normalize( (float)( y          ) ) );
		gl2.glVertex2f( normalize( (float)( x + width ) ), normalize( (float)( y          ) ) );
		gl2.glVertex2f( normalize( (float)( x + width ) ), normalize( (float)( y + height ) ) );
		gl2.glVertex2f( normalize( (float)( x         ) ), normalize( (float)( y + height ) ) );
		gl2.glEnd();
		glEnd2D();
	}

	@Override
	public void fillRect( final int x , final int y , final int width , final int height )
	{
		final GL gl = _gla.getGL();
		final GL2 gl2 = gl.getGL2();

		glBegin2D();
		gl2.glBegin( GL2.GL_QUADS );
		gl2.glVertex2i( x         , y          );
		gl2.glVertex2i( x + width , y          );
		gl2.glVertex2i( x + width , y + height );
		gl2.glVertex2i( x         , y + height );
		gl2.glEnd();
		glEnd2D();
	}

	@Override
	public void clearRect( final int x , final int y , final int width , final int height )
	{
		final Paint oldPaint = getPaint();
		setPaint( getBackground() );
		fillRect( x , y , width , height );
		setPaint( oldPaint );
	}

	@Override
	public void drawRenderableImage( final RenderableImage image , final AffineTransform transform )
	{
		drawRenderedImage( image.createDefaultRendering() , transform );
	}

	@Override
	public void drawRenderedImage( final RenderedImage img , final AffineTransform xform )
	{
		notImplemented();
	}

	@Override
	public void drawRoundRect( final int x , final int y , final int width , final int height , final int arcWidth , final int arcHeight )
	{
		draw( new RoundRectangle2D.Float( (float)x , (float)y , (float)width , (float)height , (float)arcWidth , (float)arcHeight ) );
	}

	@Override
	public void fillRoundRect( final int x , final int y , final int width , final int height , final int arcWidth , final int arcHeight )
	{
		fill( new RoundRectangle2D.Float( (float)x , (float)y , (float)width , (float)height , (float)arcWidth , (float)arcHeight ) );
	}

	@Override
	public void drawString( final String str , final int x , final int y )
	{
		final GLAutoDrawable gla = _gla;

		//create renderer if renderer has not been created already.
		final TextRenderer renderer = getTextRenderer();

		renderer.setColor( getColor() );
		renderer.beginRendering( gla.getSurfaceWidth() , gla.getSurfaceHeight() );
		// Draw text
		renderer.draw( str , x , gla.getSurfaceHeight() - y );
		// Clean up rendering
		renderer.endRendering();
	}

	/**
	 * Returns a text renderer for the current font.
	 *
	 * @return  Text renderer.
	 */
	private TextRenderer getTextRenderer()
	{
		TextRenderer renderer = _renderer;
		final Font font = getFont();
		if ( ( renderer == null ) || !font.equals( renderer.getFont() ) )
		{
			if ( renderer != null )
			{
				renderer.dispose();
			}
			renderer = new TextRenderer( getFont() );
			_renderer = renderer;
		}
		return renderer;
	}

	@Override
	public void drawString( final String str , final float x , final float y )
	{
		drawString( str , (int) x , (int) y );
	}

	@Override
	public void drawString( final AttributedCharacterIterator iterator , final int x , final int y )
	{
		drawString( iterator , (float)x , (float)y );
	}

	@Override
	public void drawString( final AttributedCharacterIterator iterator , final float x , final float y )
	{
		final StringBuilder sb = new StringBuilder();

		for ( char c = iterator.first() ; c != AttributedCharacterIterator.DONE ; c = iterator.next() )
		{
			sb.append( c );
		}

		drawString( sb.toString() , x , y );
	}

	@Override
	public boolean hit( final Rectangle rectangle , final Shape shape , final boolean onStroke )
	{
		Shape testedShape = shape;

		if ( onStroke )
		{
			final Stroke stroke = getStroke();
			testedShape = stroke.createStrokedShape( testedShape );
		}

		final AffineTransform transform = getTransform();
		testedShape = transform.createTransformedShape( testedShape );

		final Area area = new Area( testedShape );

		final Shape clip = getClip();
		if ( clip != null )
		{
			area.intersect( new Area( clip ) );
		}

		return area.intersects( (double)rectangle.x , (double)rectangle.y , (double)rectangle.width , (double)rectangle.height );
	}
}
