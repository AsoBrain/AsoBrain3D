package test.backoffice;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2002-2003 - All Rights Reserved
 *
 * This software may not be used, copyied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import ab.components.AbDialog;
import ab.components.AbPanel;

import backoffice.PolyPoint2D;
import backoffice.Polyline2D;

/**
 * This is a small test-app to manually test the Polyline2D class.
 *
 * @see	Polyline2D
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class TestPolyline2D
	extends AbPanel
	implements MouseListener , MouseMotionListener
{
	/**
	 * Base polyline to manipulate.
	 */
	private Polyline2D _baseLine;

	/**
	 * Extruded polyline.
	 */
	private Polyline2D _adjusted;

	/**
	 * Point being dragged.
	 */
	private int _dragPoint = -1;

	/**
	 * Construct test-app.
	 *
	 * @param	base	Base polyline to manipulate.
	 */
	private TestPolyline2D( final Polyline2D base )
	{
		setBackground( Color.white );
		setOpaque( true );
		setDoubleBuffered( true );

		_baseLine = base;

		_adjusted = new Polyline2D( _baseLine );
		final int maxSegment = _adjusted.getPointCount() - 1;
		for ( int i = 0 ; i < maxSegment ; i++ )
			_adjusted.adjustSegment( i , 10 );

		addMouseListener( this );
		addMouseMotionListener( this );
	}

	/**
	 * Run test application.
	 *
	 * @param	args	Command line arguments (ignored).
	 */
	public static void main( final String args[] )
	{
		test();
		System.exit( 0 );
	}

	/**
	 * Invoked when the mouse has been clicked on a component.
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseClicked( final MouseEvent e )
	{
	}

	/**
	 * Invoked when a mouse button is pressed on a component and then
	 * dragged.  Mouse drag events will continue to be delivered to
	 * the component where the first originated until the mouse button is
	 * released (regardless of whether the mouse position is within the
	 * bounds of the component).
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseDragged( final MouseEvent e )
	{
		if ( _dragPoint < 0 )
			return;


		final Dimension size = getSize();
		final int       x    = e.getX();
		final int       y    = size.height - 1 - e.getY();

		if ( x < 0 || y < 0 || x >= size.width || y >= size.height )
			return;

		final Polyline2D newPoly = new Polyline2D();
		for ( int i = 0 ; i < _baseLine.getPointCount() ; i++ )
		{
			if ( i == _dragPoint )
				newPoly.append( x , y );
			else
				newPoly.append( _baseLine.getPoint( i ) );
		}

		_baseLine = newPoly;

		_adjusted = new Polyline2D( _baseLine );
		final int maxSegment = _adjusted.getPointCount() - 1;
		for ( int i = 0 ; i < maxSegment ; i++ )
			_adjusted.adjustSegment( i , 10 );

		repaint();
	}

	/**
	 * Invoked when the mouse enters a component.
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseEntered( final MouseEvent e )
	{
	}

	/**
	 * Invoked when the mouse exits a component.
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseExited( final MouseEvent e )
	{
	}

	/**
	 * Invoked when the mouse button has been moved on a component
	 * (with no buttons no down).
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseMoved( final MouseEvent e )
	{
	}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 *
	 * @param	e	Mouse event.
	 */
	public void mousePressed( final MouseEvent e )
	{
		final Dimension size = getSize();
		final int       x    = e.getX();
		final int       y    = size.height - 1 - e.getY();

		if ( x < 0 || y < 0 || x >= size.width || y >= size.height )
			return;

		final float distanceSquared = 3f * 3f;

		_dragPoint = -1;
		for ( int i = 0 ; i < _baseLine.getPointCount() ; i++ )
		{
			final PolyPoint2D p  = _baseLine.getPoint( i );
			final float       dx = p.x - x;
			final float       dy = p.y - y;

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
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseReleased( final MouseEvent e )
	{
		_dragPoint = -1;
		repaint();
	}

	/**
	 * Paint component.
	 *
	 * @param	g	Graphics context.
	 */
	protected void paintComponent( final Graphics g )
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
	 * @param	g			Graphics context.
	 * @param	pl			Polyline to draw.
	 * @param	selected	Selected point index.
	 * @param	dotColor	Color for dots.
	 * @param	lineColor	Color for lines.
	 * @param	selectColor	Color for selected point.
	 * @param	radius		Size of points.
	 */
	private void paintPoly( final Graphics g , final Polyline2D pl , final int selected , final Color dotColor , final Color lineColor , final Color selectColor , final int radius )
	{
		final int height = getHeight();
		final int maxIndex = pl.getPointCount() - 1;
		final int maxY = height - 1;

		g.setColor( lineColor );

		for ( int i = 0 ; i < maxIndex ; i++ )
		{
			final PolyPoint2D p1 = pl.getPoint( i );
			final PolyPoint2D p2 = pl.getPoint( i + 1 );

			g.drawLine( Math.round( p1.x ) , maxY - Math.round( p1.y ) ,
			            Math.round( p2.x ) , maxY - Math.round( p2.y ) );
		}

		final int ovalX    = -radius;
		final int ovalY    = maxY - radius;
		final int ovalSize = radius * 2 + 1;

		for ( int i = 0 ; i <= maxIndex ; i++ )
		{
			final PolyPoint2D p = pl.getPoint( i );
			g.setColor( ( i == selected ) ? selectColor : dotColor );
			g.fillOval( ovalX + Math.round( p.x ) , ovalY - Math.round( p.y ) , ovalSize , ovalSize );
		}

	}

	/**
	 * Run test application.
	 */
	public static void test()
	{
		final Polyline2D base = new Polyline2D();
		base.append( 50 , 50 );
		base.append( 550 , 50 );
		base.append( 300 , 120 );
		base.append( 470 , 290 );
		base.append( 180 , 290 );
		base.append( 50 , 140 );
		base.close();

		final AbDialog f = new AbDialog( TestPolyline2D.class.getName() , true );
		final TestPolyline2D p = new TestPolyline2D( base );
		f.getContent().add( p , BorderLayout.CENTER );
		f.setSize( 600 , 400 );
		f.setAlignment( 50 , 50 );
		f.show();
	}

}
