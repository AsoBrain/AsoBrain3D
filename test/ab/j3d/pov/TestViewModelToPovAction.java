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
