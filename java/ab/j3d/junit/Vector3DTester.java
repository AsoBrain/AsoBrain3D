/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.junit;

import ab.j3d.*;
import com.numdata.oss.junit.*;
import com.numdata.oss.junit.ArrayTester.*;
import junit.framework.*;

/**
 * JUnit unit tool class to help with testing {@link Vector3D} objects.
 *
 * @author  H.B.J. te Lintelo
 * @version $Revision$ $Date$
 */
public final class Vector3DTester
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private Vector3DTester()
	{
	}

	/**
	 * Asserts that one vector is equal to another vector.
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   expected        Expected vector value.
	 * @param   actual          Actual vector value.
	 * @param   delta           Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 */
	public static void assertEquals( final String messagePrefix , final Vector3D expected , final Vector3D actual , final double delta )
	{
		final String actualPrefix = ( messagePrefix != null ) ? messagePrefix + " - " : "";

		AssertionFailedError errorX = null;
		try
		{
			Assert.assertEquals( actualPrefix + "Incorrect 'x' value." , expected.x , actual.x , delta );
		}
		catch ( AssertionFailedError e )
		{
			errorX = e;
		}

		AssertionFailedError errorY = null;
		try
		{
			Assert.assertEquals( actualPrefix + "Incorrect 'y' value." , expected.y , actual.y , delta );
		}
		catch ( AssertionFailedError e )
		{
			errorY = e;
		}

		AssertionFailedError errorZ = null;
		try
		{
			Assert.assertEquals( actualPrefix + "Incorrect 'z' value." , expected.z , actual.z , delta );
		}
		catch ( AssertionFailedError e )
		{
			errorZ = e;
		}

		if ( ( ( errorX != null ) && ( ( errorY != null ) || ( errorZ != null ) ) )
		                          || ( ( errorY != null ) && ( errorZ != null ) ) )
		{
			Assert.assertEquals( messagePrefix , expected , actual );
		}
		else
		{
			if ( errorX != null )
			{
				throw errorX;
			}

			if ( errorY != null )
			{
				throw errorY;
			}

			if ( errorZ != null )
			{
				throw errorZ;
			}
		}
	}

	/**
	 * Asserts that one vector is equal to another vector.
	 *
	 * @param   messagePrefix   Prefix to failure messages.
	 * @param   expected        Expected vector value.
	 * @param   actual          Actual vector value.
	 * @param   delta           Delta value to limit the acceptable value range.
	 *
	 * @throws  AssertionFailedError is the assertion fails.
	 */
	public static void assertEquals( final String messagePrefix , final Vector3D[] expected , final Vector3D[] actual , final double delta )
	{
		ArrayTester.assertEquals( messagePrefix, expected, actual, new AssertEquals()
		{
			@Override
			public void assertEquals( final String message, final Object expectedValue, final Object actualValue )
			{
				Vector3DTester.assertEquals( message, (Vector3D)expectedValue, (Vector3D)actualValue, delta );
			}
		} );
	}
}
