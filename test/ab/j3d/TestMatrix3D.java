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
{
	/**
	 * Report error.
	 */
	protected static boolean error( String msg )
	{
		System.err.println( "ERROR: " + msg );
		return false;
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
		System.out.println( "-------------------------------------------------------------------------------" );
		System.out.println( TestMatrix3D.class.getName() + " - started." );

		if ( testEquals() )
		{
			System.out.println( "Test completed without errors." );
			return true;
		}
		else
		{
			System.out.println();
			System.err.println( "**************" );
			System.err.println( " Test Failed!" );
			System.err.println( "**************" );
			return false;
		}
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
	private static boolean testEquals()
	{
		System.out.println( "    - equals()" );

		boolean ok = true;
		final Matrix3D i = Matrix3D.INIT;

		/*
		 * INIT must match identify matrix
		 */
		if ( !i.equals( i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) ) )
			ok = error( "equals() returned 'false' where it should have returned 'true'" );

		/*
		 * test x?
		 */
		if ( i.equals( i.set( 9 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) )
		  ||           i.set( 9 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'xx'" );

		if ( i.equals( i.set( 1 , 9 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) )
		  ||           i.set( 1 , 9 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'xy'" );
			
		if ( i.equals( i.set( 1 , 0 , 9 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) )
		  ||           i.set( 1 , 0 , 9 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'xz'" );

		if ( i.equals( i.set( 1 , 0 , 0 , 9 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) )
		  ||           i.set( 1 , 0 , 0 , 9 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'xo'" );

		/*
		 * test y?
		 */
		if ( i.equals( i.set( 1 , 0 , 0 , 0 , 9 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ) )
		  ||           i.set( 1 , 0 , 0 , 0 , 9 , 1 , 0 , 0 , 0 , 0 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'yx'" );
			
		if ( i.equals( i.set( 1 , 0 , 0 , 0 , 0 , 9 , 0 , 0 , 0 , 0 , 1 , 0 ) )
		  ||           i.set( 1 , 0 , 0 , 0 , 0 , 9 , 0 , 0 , 0 , 0 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'yy'" );
			
		if ( i.equals( i.set( 1 , 0 , 0 , 0 , 0 , 1 , 9 , 0 , 0 , 0 , 1 , 0 ) )
		  ||           i.set( 1 , 0 , 0 , 0 , 0 , 1 , 9 , 0 , 0 , 0 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'yz'" );
			
		if ( i.equals( i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 9 , 0 , 0 , 1 , 0 ) )
		  ||           i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 9 , 0 , 0 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'yo'" );
			
		/*
		 * test z?
		 */
		if ( i.equals( i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 9 , 0 , 1 , 0 ) )
		  ||           i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 9 , 0 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'zx'" );
			
		if ( i.equals( i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 9 , 1 , 0 ) )
		  ||           i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 9 , 1 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'zy'" );
			
		if ( i.equals( i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 9 , 0 ) )
		  ||           i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 9 , 0 ).equals( i ) )
			ok = error( "equals() did not correctly test 'zz'" );
			
		if ( i.equals( i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 9 ) )
		  ||           i.set( 1 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 9 ).equals( i ) )
			ok = error( "equals() did not correctly test 'zo'" );
			
		return true;
	}

}
