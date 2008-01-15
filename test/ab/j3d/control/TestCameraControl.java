/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2008 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.control;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import com.numdata.oss.junit.ResourceBundleTester;

/**
 * This class tests the {@link CameraControl} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestCameraControl
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestCameraControl.class.getName();

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

		final List<String> expectedKeys = new ArrayList<String>();

		for ( final Field field : CameraControl.class.getDeclaredFields() )
		{
			if ( Modifier.isPublic( field.getModifiers() ) )
			{
				final String value = (String)field.get( null );
				expectedKeys.add( value );
				expectedKeys.add( value + "Tip" );
				expectedKeys.add( value + "Icon" );
				expectedKeys.add( value + "Mnemonic" );
			}
		}

		ResourceBundleTester.testBundles( CameraControl.class , locales , false , expectedKeys , false , true , false );
	}
}
