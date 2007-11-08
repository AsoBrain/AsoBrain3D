/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import com.numdata.oss.junit.ResourceBundleTester;

/**
 * This class tests the {@link ViewModelView} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestViewModelView
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestViewModelView.class.getName();

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

		final List<String> expectedKeys = new ArrayList();

		for ( final ViewModelView.RenderingPolicy policy : ViewModelView.RenderingPolicy.values() )
			expectedKeys.add( policy.name() );

		ResourceBundleTester.testBundles( ViewModelView.class , locales , false , expectedKeys , false , true , false );
	}
}
