/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
 * This test verifies the <code>Matrix3D</code> class.
 *
 * @see     Matrix3D
 *
 * @author  Peter S. Heijnen
 * @version $Revision $ $Date$
 */
public class TestMatrix3D
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestMatrix3D.class.getName();

	/**
	 * Test <code>Matrix3D.equals()</code> method.
	 *
	 * RELATED BUGS (SOLVED):
	 * <ul>
	 *  <li>
	 *    <b>BUG:</b><br />
	 *    OBJ files contain bad geometric data.
	 *    <br />
	 *    <b>Symptom:</b><br />
	 *    The geometry for a 2nd scenario contains negative Y coordinates.
	 *    This seems to occur with almost every panel.
	 *    <br />
	 *    <b>Analysis:</b><br />
	 *    Matrix3D.equals() method did not compare the translation correctly
	 *    (comparing this.xo to other.xo/yo/zo instead of this.xo/yo/zo).
	 *    Incredible how this bug has never been spotted before.
	 *    <br />
	 *    <b>Fix:</b><br />
	 *    Fixed xo/yo/zo test in Matrix3D.equals() method.
	 *  </li>
	 * </ul>
	 */
	public static void testEquals()
	{
		System.out.println( CLASS_NAME + ".testEquals()" );
		final Matrix3D i = Matrix3D.INIT;
		Matrix3D m;

		/*
		 * INIT must match identity matrix
		 */
		assertEquals( "Matrix3D.equals() returned 'false' where it should have returned 'true'"
		            , i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) , i );

		/*
		 * Test if each component is correctly tested by equals()
		 */
		m = i.set( 9 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xx'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 9 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xy'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 9 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xz'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 0 , 9 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'xo'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 0 , 0 , 9 , 1 , 0 , 0 , 0 , 0 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yx'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 0 , 0 , 0 , 9 , 0 , 0 , 0 , 0 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yy'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 0 , 0 , 0 , 1 , 9 , 0 , 0 , 0 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yz'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 9 , 0 , 0 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'yo'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 9 , 0 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zx'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 9 , 1 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zy'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 9 , 0 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zz'" , !i.equals( m ) && !m.equals( i ) );

		m = i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 9 );
		assertTrue( "Matrix3D.equals() did not correctly test 'zo'" , !i.equals( m ) && !m.equals( i ) );
	}
}
