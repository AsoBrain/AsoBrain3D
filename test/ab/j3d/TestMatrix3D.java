package test.common;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2001 - All Rights Reserved
 *
 * This software may not be used, copyied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
import common.model.Matrix3D;

/**
 * This test verifies the Matrix3D class.
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public class TestMatrix3D
	extends SodaTestCase
{
	/**
	 * Run test application.
	 *
	 * @param	args	Command line arguments.
	 */
	public static void main( final String args[] )
	{
		junit.textui.TestRunner.run( TestMatrix3D.class );
	}

	/**
	 * Test <code>Matrix3D.equals()</code> method.
	 *
	 * RELATED BUGS (SOLVED):
	 * <UL>
	 *  <LI>
	 *    <B>BUG:</B><BR>
	 *    OBJ files contain bad geometric data.
	 *    <BR>
	 *    <B>Symptom:</B><BR>
	 *    The geometry for a 2nd scenario contains negative Y coordinates.
	 *    This seems to occur with almost every panel.
	 *    <BR>
	 *    <B>Analysis:</B><BR>
	 *    Matrix3D.equals() method did not compare the translation correctly
	 *    (comparing this.xo to other.xo/yo/zo instead of this.xo/yo/zo).
	 *    Incredible how this bug has never been spotted before.
	 *    <BR>
	 *    <B>Fix:</B><BR>
	 *    Fixed xo/yo/zo test in Matrix3D.equals() method.
	 *  </LI>
	 * </UL>
	 */
	public static void testEquals()
	{
		//System.out.println( "    - equals()" );
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
