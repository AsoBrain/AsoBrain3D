package test.backoffice;

import backoffice.Polyline2D;
import backoffice.PolyPoint2D;
import ab.components.*;
import java.awt.*;


public class TestPolyline2D 
{
	public static Polyline2D getAdjusted( final Polyline2D pl , float[] segmentAdjustments )
	{
		boolean isClosed = pl.isClosed();
		
		PolyPoint2D[] pts = new PolyPoint2D[ pl.getPointCount() - ( isClosed ? 1 : 0 ) ];
		for ( int i = 0 ; i < pts.length ; i++ )
			pts[ i ] = pl.getPoint( i );
		
		for ( int i1 = 0 ; i1 < segmentAdjustments.length ; i1++ )
		{
			/*
			 * Get adjustment for segment. Ignore 0-adjustments.
			 */
			final float adjustment = segmentAdjustments[ i1 ];
			if ( adjustment < 0.001f && adjustment > -0.001f )
				continue;
				
			final int i2 = ( i1 + 1 ) % pts.length;
			final PolyPoint2D p1 = pts[ i1 ];
			final PolyPoint2D p2 = pts[ i2 ];

			/*
			 * Determine lenght and direction of segment (ignore 0-length segments).
			 */
			float sdx = p2.x - p1.x;
			float sdy = p2.y - p1.y;
			float l = (float)Math.sqrt( sdx * sdx + sdy * sdy );
			if ( l < 0.001f )
				continue;

			sdx = sdx / l;
			sdy = sdy / l;
			
			/*
			 * Adjust start point using the previous control point. Assume 90 degree angle if no such point exists.
			 */	
			if ( i1 == 0 && !isClosed )
			{
				pts[ i1 ] = new PolyPoint2D( p1.x - adjustment * sdy , p1.y + adjustment * sdx );
			}
			else
			{
				PolyPoint2D prev = pts[ ( ( i1 == 0 ) ? pts.length : i1 ) - 1 ];
				
				float tx = prev.x - p1.x;
				float ty = prev.y - p1.y;
				float tl = (float)Math.sqrt( tx * tx + ty * ty );
				if ( tl < 0.001f )
					continue;

				tx = tx / tl;
				ty = ty / tl;

				float cos = tx * sdx + ty * sdy;
				float sin = 1 - ( cos * cos );
				float hyp = (float)Math.sqrt( ( adjustment * adjustment ) / sin  );

				pts[ i1 ] = new PolyPoint2D( p1.x + hyp * tx , p1.y + hyp * ty );
			}

			/*
			 * Adjust end point using the next control point. Assume 90 degree angle if no such point exists.
			 */
			if ( !isClosed && ( i2 == pts.length - 1 ) )
			{
				pts[ i2 ] = new PolyPoint2D( p2.x - adjustment * sdy , p2.y + adjustment * sdx );
			}
			else
			{
				PolyPoint2D next = pts[ ( i2 + 1 ) % pts.length ];
				float tx = next.x - p2.x;
				float ty = next.y - p2.y;
				float tl = (float)Math.sqrt( tx * tx + ty * ty );
				if ( tl < 0.001f )
					continue;

				tx = tx / tl;
				ty = ty / tl;
				
				float cos = tx * sdx + ty * sdy;
				float sin = 1 - ( cos * cos );
				float hyp = (float)Math.sqrt( ( adjustment * adjustment ) / sin  );

				pts[ i2 ] = new PolyPoint2D( p2.x + hyp * tx , p2.y + hyp * ty );
			}
		}

		/*
		 * Build result.
		 */
		Polyline2D result = new Polyline2D();
		
		for ( int i = 0 ; i < pts.length ; i++ )
			result.append( pts[ i ] );
			
		if ( isClosed )
			result.close();
		
		return result;
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
	 * Run test application.
	 *
	 * @param	args	Command line arguments.
	 *
	 * @return	<code>true</code> if test was succesful;
	 *			<code>false</code> if one or more errors occured.
	 */
	public static boolean test( final String[] args )
	{
		final Polyline2D pl1 = new Polyline2D();
		pl1.append( 50 , 50 );
		pl1.append( 550 , 50 );
		pl1.append( 300 , 120 );
		pl1.append( 470 , 290 );
		pl1.append( 180 , 290 );
		pl1.append( 50 , 140 );
		pl1.close();

		final Polyline2D pl2 = getAdjusted( pl1 , new float[] { 10 , 10 , 10 , 10 , 10 , 10 } );
		
		AbDialog f = new AbDialog( TestPolyline2D.class.getName() , true );
		AbPanel p = new AbPanel()
		{
			private void paintPoly( Graphics g , Polyline2D pl )
			{
				for ( int i = 0 ; i < pl.getPointCount() ; i++ )
				{
					PolyPoint2D p1 = pl.getPoint( i );
					int x1 = Math.round( p1.x );
					int y1 = Math.round( p1.y );

					g.fillOval( x1 - 2 , y1 - 2 , 5 , 5 );

					if ( i < ( pl.getPointCount() - 1 ) )
					{
						PolyPoint2D p2 = pl.getPoint( i + 1 );
						int x2 = Math.round( p2.x );
						int y2 = Math.round( p2.y );
						
						g.drawLine( x1 , y1 , x2 , y2 );
					}
				}
			}
			
			protected void paintComponent( Graphics g )
			{
				super.paintComponent( g );
				
				g.setColor( Color.black );
				paintPoly( g , pl1 );
				
				g.setColor( Color.red );
				paintPoly( g , pl2 );
			}
		};
		p.setBackground( Color.white );
		f.getContent().add( p , BorderLayout.CENTER );
		f.setSize( 600 , 400 );
		f.setAlignment( 50 , 50 );
		f.show();
		return true;
	}

}
