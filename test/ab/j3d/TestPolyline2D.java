package com.numdata.soda.backoffice;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2002-2003 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
import com.numdata.soda.common.SodaTestCase;

import com.numdata.soda.backoffice.PolyPoint2D;
import com.numdata.soda.backoffice.Polyline2D;

/**
 * Test functionality of Polyline2D class.
 *
 * @see	Polyline2D
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class TestPolyline2D
	extends SodaTestCase
{
	/**
	 * Test constructor for rectangular polyline.
	 */
	public static void testRectangleConstructor()
	{
		final Polyline2D pl = new Polyline2D( 123 , 456 );
		assertEquals( "rectangle should have 5 points!" , 5 , pl.getPointCount() );

		assertEquals( "rectangle[ 0 ] is off" , new PolyPoint2D(   0 ,   0 ) , pl.getPoint( 0 ) );
		assertEquals( "rectangle[ 1 ] is off" , new PolyPoint2D( 123 ,   0 ) , pl.getPoint( 1 ) );
		assertEquals( "rectangle[ 2 ] is off" , new PolyPoint2D( 123 , 456 ) , pl.getPoint( 2 ) );
		assertEquals( "rectangle[ 3 ] is off" , new PolyPoint2D(   0 , 456 ) , pl.getPoint( 3 ) );
		assertEquals( "rectangle[ 4 ] is off" , new PolyPoint2D(   0 ,   0 ) , pl.getPoint( 4 ) );
	}

	/**
	 * Test constructor for horizontal line.
	 */
	public static void testHorizontalLineConstructor()
	{
		final Polyline2D pl = new Polyline2D( 123 , 0 );
		assertEquals( "horizontal line should have 2 points" , 2 , pl.getPointCount() );

		assertEquals( "horizontal line[ 0 ] is off" , new PolyPoint2D(   0 , 0 ) , pl.getPoint( 0 ) );
		assertEquals( "horizontal line[ 1 ] is off" , new PolyPoint2D( 123 , 0 ) , pl.getPoint( 1 ) );
	}

	/**
	 * Test constructor for vertical line.
	 */
	public static void testVerticalLineConstructor()
	{
		final Polyline2D pl = new Polyline2D( 0 , 456 );
		assertEquals( "vertical line should have 2 points" , 2 , pl.getPointCount() );

		assertEquals( "vertical line[ 0 ] is off" , new PolyPoint2D( 0 ,   0 ) , pl.getPoint( 0 ) );
		assertEquals( "vertical line[ 1 ] is off" , new PolyPoint2D( 0 , 456 ) , pl.getPoint( 1 ) );
	}
}
