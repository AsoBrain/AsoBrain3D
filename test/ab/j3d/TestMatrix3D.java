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
	 *
	 * @return	<code>true</code> if test was succesful;
	 *			<code>false</code> if one or more errors occured.
	 */
	public void testEquals()
	{
		//System.out.println( "    - equals()" );
		final Matrix3D i = Matrix3D.INIT;

		/*
		 * INIT must match identify matrix
		 */
		assertEquals( "Matrix3D.equals() returned 'false' where it should have returned 'true'"
		            , i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) , i );

		/*
		 * test x?
		 */
		assertNotEquals( "Matrix3D.equals() did not correctly test 'xx'"
		               , i , i.set( 9 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'xx'"
		               ,     i.set( 9 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) , i );

		assertNotEquals( "Matrix3D.equals() did not correctly test 'xy'"
		               , i , i.set( 1 , 9 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'xy'"
		               ,     i.set( 1 , 9 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) , i );
			
		assertNotEquals( "Matrix3D.equals() did not correctly test 'xz'"
		               , i , i.set( 1 , 0 , 9 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'xz'"
		               ,     i.set( 1 , 0 , 9 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) , i );

		assertNotEquals( "Matrix3D.equals() did not correctly test 'xo'"
		               , i , i.set( 1 , 0 , 0 , 9 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'xo'"
		               ,     i.set( 1 , 0 , 0 , 9 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) , i );

		/*
		 * test y?
		 */
		assertNotEquals( "Matrix3D.equals() did not correctly test 'yx'"
		               , i , i.set( 1 , 0 , 0 , 0 , 9 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'yx'"
		               ,     i.set( 1 , 0 , 0 , 0 , 9 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) , i );
			
		assertNotEquals( "Matrix3D.equals() did not correctly test 'yy'"
		               , i , i.set( 1 , 0 , 0 , 0 , 0 , 9 , 0 , 0 , 0 , 0 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'yy'"
		               ,     i.set( 1 , 0 , 0 , 0 , 0 , 9 , 0 , 0 , 0 , 0 , 1 , 0 ) , i );
			
		assertNotEquals( "Matrix3D.equals() did not correctly test 'yz'"
		               , i , i.set( 1 , 0 , 0 , 0 , 0 , 1 , 9 , 0 , 0 , 0 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'yz'"
		               ,     i.set( 1 , 0 , 0 , 0 , 0 , 1 , 9 , 0 , 0 , 0 , 1 , 0 ) , i );
			
		assertNotEquals( "Matrix3D.equals() did not correctly test 'yo'"
		               , i , i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 9 , 0 , 0 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'yo'"
		               ,     i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 9 , 0 , 0 , 1 , 0 ) , i );
			
		/*
		 * test z?
		 */
		assertNotEquals( "Matrix3D.equals() did not correctly test 'zx'"
		               , i , i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 9 , 0 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'zx'"
		               ,     i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 9 , 0 , 1 , 0 ) , i );
			
		assertNotEquals( "Matrix3D.equals() did not correctly test 'zy'"
		               , i , i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 9 , 1 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'zy'"
		               ,     i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 9 , 1 , 0 ) , i );
			
		assertNotEquals( "Matrix3D.equals() did not correctly test 'zz'"
		               , i , i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 9 , 0 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'zz'"
		               ,     i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 9 , 0 ) , i );
			
		assertNotEquals( "Matrix3D.equals() did not correctly test 'zo'"
		               , i , i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 9 ) );
		assertNotEquals( "Matrix3D.equals() did not correctly test 'zo'"
		               ,     i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 9 ) , i );
	}

}
