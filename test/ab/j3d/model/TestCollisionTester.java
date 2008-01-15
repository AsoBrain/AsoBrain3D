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
package ab.j3d.model;

import junit.framework.TestCase;

import ab.j3d.Bounds3D;
import ab.j3d.Matrix3D;

/**
 * Unit test for the {@link CollisionTester} class.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class TestCollisionTester
	extends TestCase
{
	public void testOrientedBoundingBox()
	{
		{
			final Bounds3D first  = Bounds3D.fromString( "18.0,0.0,54.75;44.85,56.5,278.723625994732" );
			final Bounds3D second = Bounds3D.fromString( "0.0,0.0,0.0;500.0,764.0,18.0" );
			final Matrix3D secondToFirst = Matrix3D.INIT.set(
				0.0 , 0.0 , 1.0 ,  0.0 ,
				1.0 , 0.0 , 0.0 ,  0.0 ,
				0.0 , 1.0 , 0.0 , 18.0 );
			assertTrue( "Expected collision" , CollisionTester.testOrientedBoundingBox( first , secondToFirst , second ) );
		}

		{
			final Bounds3D first  = Bounds3D.fromString( "18.0,0.0,54.75;44.85,56.5,278.723625994732" );
			final Bounds3D second = Bounds3D.fromString( "0.0,0.0,0.0;578.0,779.0,18.0" );
			final Matrix3D secondToFirst = Matrix3D.INIT.set(
				1.0 ,  0.0 ,  0.0 ,   11.0 ,
				0.0 ,  0.0 , -1.0 ,    0.0 ,
				0.0 ,  1.0 ,  0.0 ,   10.5 );
			assertTrue( "Expected collision" , CollisionTester.testOrientedBoundingBox( first , secondToFirst , second ) );
		}

		{
			final Bounds3D first  = Bounds3D.fromString( "309.791845703125,0.0,54.75;336.641845703125,56.5,278.723625994732" );
			final Bounds3D second = Bounds3D.fromString( "0.0,0.0,0.0;151.85658264160156,480.0,18.0" );
			final Matrix3D secondToFirst = Matrix3D.INIT.set(
				1.0 ,  0.0 ,  0.0 ,  184.3 ,
				0.0 ,  1.0 ,  0.0 ,    0.0 ,
				0.0 ,  0.0 ,  1.0 ,  195.5 );
			assertTrue( "Expected collision" , CollisionTester.testOrientedBoundingBox( first , secondToFirst , second ) );
		}

		{
			final Bounds3D first  = Bounds3D.fromString( "309.791845703125,0.0,54.75;336.641845703125,56.5,278.723625994732" );
			final Bounds3D second = Bounds3D.fromString( "0.0,0.0,0.0;151.85658264160156,480.0,18.0" );
			final Matrix3D secondToFirst = Matrix3D.INIT.set(
				1.0 ,  0.0 ,  0.0 ,  184.3 ,
				0.0 ,  1.0 ,  0.0 ,    0.0 ,
				0.0 ,  0.0 ,  1.0 ,  195.5 );
			assertTrue( "Expected collision" , CollisionTester.testOrientedBoundingBox( first , secondToFirst , second ) );
		}

		{
			final Bounds3D first  = Bounds3D.fromString( "309.791845703125,0.0,54.75;336.641845703125,56.5,278.723625994732" );
			final Bounds3D second = Bounds3D.fromString( "0.0,0.0,0.0;480.0,373.0,18.0" );
			final Matrix3D secondToFirst = Matrix3D.INIT.set(
				0.0 ,  0.0 ,  1.0 ,  336.6 ,
				1.0 ,  0.0 ,  0.0 ,    0.0 ,
				0.0 ,  1.0 ,  0.0 ,   18.0 );
			assertTrue( "Expected collision" , CollisionTester.testOrientedBoundingBox( first , secondToFirst , second ) );
		}

		{
			final Bounds3D first  = Bounds3D.fromString( "309.791845703125,0.0,54.75;336.641845703125,56.5,278.723625994732" );
			final Bounds3D second = Bounds3D.fromString( "0.0,0.0,0.0;578.0,779.0,18.0" );
			final Matrix3D secondToFirst = Matrix3D.INIT.set(
				1.0 ,  0.0 ,  0.0 ,   11.0 ,
				0.0 ,  0.0 , -1.0 ,    0.0 ,
				0.0 ,  1.0 ,  0.0 ,   10.5 );
			assertTrue( "Expected collision" , CollisionTester.testOrientedBoundingBox( first , secondToFirst , second ) );
		}

		{
			final Bounds3D first  = Bounds3D.fromString( "555.15,0.0,54.75;582.0,56.5,278.723625994732" );
			final Bounds3D second = Bounds3D.fromString( "0.0,0.0,0.0;578.0,779.0,18.0" );
			final Matrix3D secondToFirst = Matrix3D.INIT.set(
				1.0 ,  0.0 ,  0.0 ,   11.0 ,
				0.0 ,  0.0 , -1.0 ,    0.0 ,
				0.0 ,  1.0 ,  0.0 ,   10.5 );
			assertTrue( "Expected collision" , CollisionTester.testOrientedBoundingBox( first , secondToFirst , second ) );
		}

		{
			final Bounds3D first  = Bounds3D.fromString( "555.15,0.0,54.75;582.0,56.5,278.723625994732" );
			final Bounds3D second = Bounds3D.fromString( "0.0,0.0,0.0;500.0,764.0,18.0" );
			final Matrix3D secondToFirst = Matrix3D.INIT.set(
				0.0 ,  0.0 ,  1.0 ,  582.0 ,
				1.0 ,  0.0 ,  0.0 ,    0.0 ,
				0.0 ,  1.0 ,  0.0 ,   18.0 );
			assertTrue( "Expected collision" , CollisionTester.testOrientedBoundingBox( first , secondToFirst , second ) );
		}
	}
}
