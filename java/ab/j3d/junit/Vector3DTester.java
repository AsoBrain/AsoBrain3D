/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import ab.j3d.Vector3D;

/**
 * JUnit unit tool class to help with testing <code>Vector3D</code> objects.
 *
 * @see     Vector3D
 *
 * @author  H.B.J. te Lintelo
 * @version $Revision$ $Date$
 */
public final class Vector3DTester
	extends Assert
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
			assertEquals( actualPrefix + "Incorrect 'x' value." , expected.x , actual.x , delta );
		}
		catch ( AssertionFailedError e )
		{
			errorX = e;
		}

		AssertionFailedError errorY = null;
		try
		{
			assertEquals( actualPrefix + "Incorrect 'y' value." , expected.y , actual.y , delta );
		}
		catch ( AssertionFailedError e )
		{
			errorY = e;
		}

		AssertionFailedError errorZ = null;
		try
		{
			assertEquals( actualPrefix + "Incorrect 'z' value." , expected.z , actual.z , delta );
		}
		catch ( AssertionFailedError e )
		{
			errorZ = e;
		}

		if ( ( ( errorX != null ) && ( ( errorY != null ) || ( errorZ != null ) ) )
		                          || ( ( errorY != null ) && ( errorZ != null ) ) )
		{
			assertEquals( messagePrefix , expected , actual );
		}
		else if ( errorX != null ) throw errorX;
		else if ( errorY != null ) throw errorY;
		else if ( errorZ != null ) throw errorZ;
	}
}
