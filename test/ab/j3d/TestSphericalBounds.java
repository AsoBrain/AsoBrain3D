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
package ab.j3d;

import junit.framework.TestCase;

/**
 * Unit test for the {@link SphericalBounds} class.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TestSphericalBounds
	extends TestCase
{
	/**
	 * Unit test for {@link SphericalBounds#contains(double, double)}.
	 */
	public void testContainsAngles()
	{
		{
			// Bounds containing only the upward direction. Note that the azimuth should be ignored.
			final SphericalBounds bounds = new SphericalBounds( 0.0 , 0.0 , 0.0 , 0.0 );
			assertTrue( "Bounds should contain direction." , bounds.contains( 0.0 , 0.0 ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( 0.0 , 2.0 * Math.PI ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( Math.PI , 0.0 ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( 2.0 * Math.PI , 2.0 * Math.PI ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( 0.0 , -2.0 * Math.PI ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( -Math.PI , 0.0 ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( -2.0 * Math.PI , -2.0 * Math.PI ) );
		}
		{
			// Bounds containing only the downward direction. Note that the azimuth should be ignored.
			final SphericalBounds bounds = new SphericalBounds( Math.PI , Math.PI , Math.PI , Math.PI );
			assertTrue( "Bounds should contain direction." , bounds.contains( Math.PI , Math.PI ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( Math.PI , 3.0 * Math.PI ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( 2.0 * Math.PI , Math.PI ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( 3.0 * Math.PI , 3.0 * Math.PI ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( 0.0 , -Math.PI ) );
			assertTrue( "Bounds should contain direction." , bounds.contains( -Math.PI ,  Math.PI ) );
			assertTrue( "Bounds should contain direction." , bounds.contains(  Math.PI , -Math.PI ) );
		}
		{
			// Bounds containing all angles in the XY-plane.
			final SphericalBounds bounds = new SphericalBounds( -Math.PI , Math.PI , 0.5 * Math.PI , 0.5 * Math.PI );
			for ( double azimuth = -360.0 ; azimuth <= 360.0 ; azimuth += 10.0 )
			{
				final double azimuthRad = Math.toRadians( azimuth );
				assertTrue( "Bounds should contain direction. (azimuth = " + azimuth + ")" , bounds.contains( azimuthRad ,  Math.PI * 0.5 ) );
				assertTrue( "Bounds should contain direction. (azimuth = " + azimuth + ")" , bounds.contains( azimuthRad , -Math.PI * 0.5 ) );
				assertTrue( "Bounds should contain direction. (azimuth = " + azimuth + ")" , bounds.contains( azimuthRad ,  Math.PI * 1.5 ) );
				assertTrue( "Bounds should contain direction. (azimuth = " + azimuth + ")" , bounds.contains( azimuthRad , -Math.PI * 1.5 ) );
			}
		}
		{
			// Bounds consisting of a half-circle on the XZ plane on the side of the positive X-axis.
			final SphericalBounds bounds = new SphericalBounds( 0.0 , 0.0 , 0.0 , Math.PI );
			for ( double zenith = 0.0 ; zenith <= 180.0 ; zenith += 10.0 )
			{
				final double zenithRad = Math.toRadians( zenith );

				assertTrue( "Bounds should contain direction. (zenith = " + zenith + ")" , bounds.contains( 0.0 , zenithRad ) );
				assertTrue( "Bounds should contain direction. (zenith = " + zenith + ")" , bounds.contains( 2.0 * Math.PI , zenithRad ) );
				assertTrue( "Bounds should contain direction. (zenith = " + zenith + ")" , bounds.contains(  Math.PI , -zenithRad ) );
				assertTrue( "Bounds should contain direction. (zenith = " + zenith + ")" , bounds.contains( -Math.PI , -zenithRad ) );

				if ( ( zenith != 0.0 ) && ( zenith != 180.0 ) )
				{
					assertFalse( "Bounds should not contain direction. (zenith = " + zenith + ")" , bounds.contains( 0.0 , -zenithRad ) );
					assertFalse( "Bounds should not contain direction. (zenith = " + zenith + ")" , bounds.contains( 2.0 * Math.PI , -zenithRad ) );
					assertFalse( "Bounds should not contain direction. (zenith = " + zenith + ")" , bounds.contains(  Math.PI , zenithRad ) );
					assertFalse( "Bounds should not contain direction. (zenith = " + zenith + ")" , bounds.contains( -Math.PI , zenithRad ) );
				}
			}
		}
	}

	/**
	 * Unit test for {@link SphericalBounds#contains(SphericalBounds, boolean)}.
	 */
	public void testContainsBounds()
	{
		final SphericalBounds bounds = new SphericalBounds( 1.5 , 2.5 , 1.0 , 2.0 );
		/* Exact match.              */ assertTrue ( "Should be inside bounds."     , bounds.contains( new SphericalBounds( 1.5 , 2.5 , 1.0 , 2.0 ) , false ) );
		/* Smaller azimuth range.    */ assertTrue ( "Should be inside bounds."     , bounds.contains( new SphericalBounds( 2.0 , 2.0 , 1.0 , 2.0 ) , false ) );
		/* Smaller zenith range.     */ assertTrue ( "Should be inside bounds."     , bounds.contains( new SphericalBounds( 1.5 , 2.5 , 1.5 , 1.5 ) , false ) );
		/* Point inside bounds.      */ assertTrue ( "Should be inside bounds."     , bounds.contains( new SphericalBounds( 2.0 , 2.0 , 1.5 , 1.5 ) , false ) );
		/* Azimuth before minimum.   */ assertFalse( "Should not be inside bounds." , bounds.contains( new SphericalBounds( 0.5 , 2.5 , 1.0 , 2.0 ) , false ) );
		/* Azimuth after maximum.    */ assertFalse( "Should not be inside bounds." , bounds.contains( new SphericalBounds( 1.5 , 3.0 , 1.0 , 2.0 ) , false ) );
		/* Zenith before minimum.    */ assertFalse( "Should not be inside bounds." , bounds.contains( new SphericalBounds( 1.5 , 2.5 , 0.0 , 2.0 ) , false ) );
		/* Zenith after maximum.     */ assertFalse( "Should not be inside bounds." , bounds.contains( new SphericalBounds( 1.5 , 2.5 , 1.0 , 3.0 ) , false ) );
		/* Inverted minimum/maximum. */ assertFalse( "Should not be inside bounds." , bounds.contains( new SphericalBounds( 2.5 , 1.5 , 1.0 , 2.0 ) , false ) );
	}

	/**
	 * Unit test for {@link SphericalBounds#fromRange(double, double, double, double, double)}.
	 */
	public void testFromRangeWithDelta()
	{
		final double delta   = Math.toRadians( 0.001 );
		final double epsilon = 0.001 * delta;

		{
			// range from 90 to 270 degrees on the XY plane.
			final SphericalBounds bounds = SphericalBounds.fromRange( 0.5 * Math.PI , Math.PI , 0.5 * Math.PI , 0.0 , delta );
			assertEquals( "Unexpected minimum azimuth." ,  0.5 * Math.PI - delta , bounds.getMinimumAzimuth() , epsilon );
			assertEquals( "Unexpected maximum azimuth." , -0.5 * Math.PI + delta , bounds.getMaximumAzimuth() , epsilon );
			assertEquals( "Unexpected minimum zenith."  ,  0.5 * Math.PI - delta , bounds.getMinimumZenith()  , epsilon );
			assertEquals( "Unexpected maximum zenith."  ,  0.5 * Math.PI + delta , bounds.getMaximumZenith()  , epsilon );
		}
	}

	/**
	 * Unit test for {@link SphericalBounds#add(double, double)}.
	 */
	public void testAdd()
	{
		SphericalBounds bounds = new SphericalBounds();

		bounds = bounds.add( 1.0 , 0.5 );
		assertEquals( "Unexpected bounds." , new SphericalBounds( 1.0 , 0.5 ) , bounds );
		bounds = bounds.add( 1.0 , 0.1 );
		assertEquals( "Unexpected bounds." , new SphericalBounds( 1.0 , 1.0 , 0.1 , 0.5 ) , bounds );
		bounds = bounds.add( 1.0 , 3.0 );
		assertEquals( "Unexpected bounds." , new SphericalBounds( 1.0 , 1.0 , 0.1 , 3.0 ) , bounds );

		bounds = bounds.add( 0.1 , 1.0 );
		assertEquals( "Unexpected bounds." , new SphericalBounds( 0.1 , 1.0 , 0.1 , 3.0 ) , bounds );
		bounds = bounds.add( 1.9 , 1.0 );
		assertEquals( "Unexpected bounds." , new SphericalBounds( 0.1 , 1.9 , 0.1 , 3.0 ) , bounds );
		bounds = bounds.add( -3.0 , 1.0 ); // Note that -3.0 is closer to 1.9 than to 0.1 (modulo 2pi).
		assertEquals( "Unexpected bounds." , new SphericalBounds( 0.1 , -3.0 , 0.1 , 3.0 ) , bounds );
	}
}
