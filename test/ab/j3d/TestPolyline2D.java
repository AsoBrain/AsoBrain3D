package test.backoffice;

import backoffice.Polyline2D;
import backoffice.PolyPoint2D;
import ab.components.*;
import java.awt.*;
import java.awt.event.*;


public class TestPolyline2D
	extends AbPanel
	implements MouseListener , MouseMotionListener
{
	private Polyline2D _baseLine;
	private Polyline2D _adjusted;
	private int _dragPoint = -1;

	private TestPolyline2D( final Polyline2D base )
	{
		setBackground( Color.white );
		setOpaque( true );
		setDoubleBuffered( true );

		_baseLine = base;
		_adjusted = new Polyline2D( _baseLine );
		_adjusted.adjustAllSegments( 10 );
		
		addMouseListener( this );
		addMouseMotionListener( this );
	}

	/**
	 * Run test application.
	 *
	 * @param	args	Command line arguments.
	 */
	public static void main( final String args[] )
	{
		System.exit( test( args ) ? 0 : 1 );
	}

/**
 * Invoked when the mouse has been clicked on a component.
 */
public void mouseClicked(java.awt.event.MouseEvent e)
{
}

	/**
	 * Invoked when a mouse button is pressed on a component and then 
	 * dragged.  Mouse drag events will continue to be delivered to
	 * the component where the first originated until the mouse button is
	 * released (regardless of whether the mouse position is within the
	 * bounds of the component).
	 */
	public void mouseDragged( MouseEvent e )
	{
		if ( _dragPoint < 0 )
			return;
			
		
		Dimension size = getSize();
		int x = e.getX();
		int y = size.height - 1 - e.getY();
		
		if ( x < 0 || y < 0 || x >= size.width || y >= size.height )
			return;
		
		Polyline2D newPoly = new Polyline2D();
		for ( int i = 0 ; i < _baseLine.getPointCount() ; i++ )
		{
			if ( i == _dragPoint )
				newPoly.append( x , y );
			else
				newPoly.append( _baseLine.getPoint( i ) );
		}

		_baseLine = newPoly;
		
		_adjusted = new Polyline2D( _baseLine );
		_adjusted.adjustAllSegments( 10 );
		
		repaint();
	}

/**
 * Invoked when the mouse enters a component.
 */
public void mouseEntered(java.awt.event.MouseEvent e)
{
}

/**
 * Invoked when the mouse exits a component.
 */
public void mouseExited(java.awt.event.MouseEvent e)
{
}

/**
 * Invoked when the mouse button has been moved on a component
 * (with no buttons no down).
 */
public void mouseMoved(java.awt.event.MouseEvent e)
{
}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	public void mousePressed( final MouseEvent e )
	{
		Dimension size = getSize();
		int x = e.getX();
		int y = size.height - 1 - e.getY();
		
		if ( x < 0 || y < 0 || x >= size.width || y >= size.height )
			return;

		float distanceSquared = 3f * 3f;
		
		_dragPoint = -1;
		for ( int i = 0 ; i < _baseLine.getPointCount() ; i++ )
		{
			PolyPoint2D p = _baseLine.getPoint( i );
			float dx = p.x - x;
			float dy = p.y - y;

			if ( ( dx * dx + dy * dy ) < distanceSquared )
			{
				_dragPoint = i;
				break;
			}
		}

		repaint();
	}

	/**
	 * Invoked when a mouse button has been released on a component.
	 */
	public void mouseReleased( MouseEvent e )
	{
		_dragPoint = -1;
		repaint();
	}

	/**
	 * Paint component.
	 *
	 * @param	g	Graphics context.
	 */
	protected void paintComponent( Graphics g )
	{
		super.paintComponent( g );

		if ( _baseLine != null )
		{
			g.setColor( Color.black );
			paintPoly( g , _baseLine , _dragPoint , Color.black , Color.gray , Color.blue , 3 );
		}

		if ( _adjusted != null )
		{
			g.setColor( Color.red );
			paintPoly( g , _adjusted , -1 , Color.pink , Color.pink , null , 2 );
		}
	}

	/**
	 * Paint polyline using dots and lines.
	 *
	 * @param	g		Graphics context.
	 * @param	pl		Polyline to draw.
	 */
	private void paintPoly( Graphics g , Polyline2D pl , int selected , Color dotColor , Color lineColor , Color selectColor , int radius )
	{
		final int height = getHeight();
		final int maxIndex = pl.getPointCount() - 1;
		final int maxY = height - 1;
		
		g.setColor( lineColor );
		
		for ( int i = 0 ; i < maxIndex ; i++ )
		{
			PolyPoint2D p1 = pl.getPoint( i );
			PolyPoint2D p2 = pl.getPoint( i + 1 );
			
			g.drawLine( Math.round( p1.x ) , maxY - Math.round( p1.y ) ,
			            Math.round( p2.x ) , maxY - Math.round( p2.y ) );
		}

		final int ovalX    = -radius;
		final int ovalY    = maxY - radius;
		final int ovalSize = radius * 2 + 1;
		
		for ( int i = 0 ; i <= maxIndex ; i++ )
		{
			PolyPoint2D p = pl.getPoint( i );
			g.setColor( ( i == selected ) ? selectColor : dotColor );
			g.fillOval( ovalX + Math.round( p.x ) , ovalY - Math.round( p.y ) , ovalSize , ovalSize );
		}
		
	}

	/**
	 * Run test application.
	 *
	 * @param	args	Command line arguments.
	 *
	 * @return	<code>true</code> if test was succesful;
	 *			<code>false</code> if one or more errors occured.
	 */
	public static boolean test( final String[] args )
	{
		final Polyline2D base = new Polyline2D();
		base.append( 50 , 50 );
		base.append( 550 , 50 );
		base.append( 300 , 120 );
		base.append( 470 , 290 );
		base.append( 180 , 290 );
		base.append( 50 , 140 );
		base.close();
		
		AbDialog f = new AbDialog( TestPolyline2D.class.getName() , true );
		TestPolyline2D p = new TestPolyline2D( base );
		f.getContent().add( p , BorderLayout.CENTER );
		f.setSize( 600 , 400 );
		f.setAlignment( 50 , 50 );
		f.show();
		return true;
	}

}
