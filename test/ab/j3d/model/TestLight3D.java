/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2009 Numdata BV
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
package ab.j3d.model;

import junit.framework.TestCase;

/**
 * Unit test for the {@link Light3D} class.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TestLight3D
	extends TestCase
{
	/**
	 * Test that the distance at which half attenuation is reached is calculated
	 * correctly.
	 */
	public void testHalfAttenuationDistance()
	{
		final Light3D light3D = new Light3D();

		light3D.setAttenuation( 0.0f , 0.0f , 1.0f );
		assertEquals( "For inverse square attenuation, distance should be sqrt(2)" , (float)Math.sqrt( 2.0 ) , light3D.getHalfIntensityDistance() , 0.0f );

		light3D.setAttenuation( 0.0f , 1.0f , 0.0f );
		assertEquals( "For inverse linear attenuation, distance should be 2" , 2.0f , light3D.getHalfIntensityDistance() , 0.0f );

		light3D.setAttenuation( 2.0f , 0.0f , 0.0f );
		assertEquals( "For constant attenuation, the return value must be 0.0" , 0.0f , light3D.getHalfIntensityDistance() , 0.0f );

		light3D.setAttenuation( 1.0f , 0.0f , 1.0f );
		assertEquals( "Attenuation should be 0.5 at distance 1." , 1.0f , light3D.getHalfIntensityDistance() , 0.0f );

		light3D.setAttenuation( 1.0f , 1.0f , 0.0f );
		assertEquals( "Attenuation should be 0.5 at distance 1." , 1.0f , light3D.getHalfIntensityDistance() , 0.0f );

		light3D.setAttenuation( 1.0f , 0.0f , 2.0f );
		assertEquals( "Attenuation should be 0.5 at distance sqrt(0.5)." , (float)Math.sqrt( 0.5 ) , light3D.getHalfIntensityDistance() , 0.0f );

		light3D.setAttenuation( 1.0f , 2.0f , 0.0f );
		assertEquals( "Attenuation should be 0.5 at distance 0.5." , 0.5f , light3D.getHalfIntensityDistance() , 0.0f );
	}

	/**
	 * Tests that the legacy fall-off distance is consistent with the new light
	 * attenuation factors.
	 */
	public void testFallOff()
	{
		final Light3D light3D = new Light3D();

		light3D.setFallOff( 200000.0 );
		assertEquals( "Half attenuation distance must match fall-off." , 200000.0f , light3D.getHalfIntensityDistance() , 1.0e-6f );

		light3D.setFallOff( 200.0 );
		assertEquals( "Half attenuation distance must match fall-off." , 200.0f , light3D.getHalfIntensityDistance() , 1.0e-6f );

		light3D.setFallOff( 0.0 );
		assertEquals( "Half attenuation distance must match fall-off." , 0.0f , light3D.getHalfIntensityDistance() , 1.0e-6f );
	}
}
