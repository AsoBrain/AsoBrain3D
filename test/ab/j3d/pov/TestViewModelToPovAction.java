/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.pov;

import java.util.Locale;

import junit.framework.TestCase;

import com.numdata.oss.junit.ResourceBundleTester;

/**
 * This class tests the {@link ViewModelToPovAction} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestViewModelToPovAction
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestViewModelToPovAction.class.getName();

	/**
	 * Test resource bundles for class.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testResources()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testResources()" );

		final Locale[] locales = { new Locale( "nl" , "NL" ) , Locale.US , Locale.GERMANY };

		ResourceBundleTester.testBundles( ViewModelToPovAction.class , locales , false , null , true , true , false );
	}
}
