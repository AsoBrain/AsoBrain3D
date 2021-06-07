/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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
 */
package ab.j3d.junit;

import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;
import org.junit.*;

/**
 * JUnit unit tool class to help with testing {@link Vector3D} objects.
 *
 * @author H.B.J. te Lintelo
 */
public class Vector3DTester
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
	 * @param messagePrefix Prefix to failure messages.
	 * @param expected      Expected vector value.
	 * @param actual        Actual vector value.
	 * @param delta         Delta value to limit the acceptable value range.
	 *
	 * @throws AssertionError is the assertion fails.
	 */
	public static void assertEquals( final @Nullable String messagePrefix, final @NotNull Vector3D expected, final @NotNull Vector3D actual, final double delta )
	{
		final String actualPrefix = ( messagePrefix != null ) ? messagePrefix + " - " : "";

		AssertionError errorX = null;
		try
		{
			Assert.assertEquals( actualPrefix + "Incorrect 'x' value.", expected.x, actual.x, delta );
		}
		catch ( AssertionError e )
		{
			errorX = e;
		}

		AssertionError errorY = null;
		try
		{
			Assert.assertEquals( actualPrefix + "Incorrect 'y' value.", expected.y, actual.y, delta );
		}
		catch ( AssertionError e )
		{
			errorY = e;
		}

		AssertionError errorZ = null;
		try
		{
			Assert.assertEquals( actualPrefix + "Incorrect 'z' value.", expected.z, actual.z, delta );
		}
		catch ( AssertionError e )
		{
			errorZ = e;
		}

		if ( ( ( errorX != null ) && ( ( errorY != null ) || ( errorZ != null ) ) )
		                          || ( ( errorY != null ) && ( errorZ != null ) ) )
		{
			Assert.assertEquals( messagePrefix, expected, actual );
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
	 * @param messagePrefix Prefix to failure messages.
	 * @param expected      Expected vector value.
	 * @param actual        Actual vector value.
	 * @param delta         Delta value to limit the acceptable value range.
	 *
	 * @throws AssertionError is the assertion fails.
	 */
	public static void assertEquals( final String messagePrefix, final Vector3D[] expected, final Vector3D[] actual, final double delta )
	{
		assertEquals( messagePrefix, Arrays.asList( expected ), Arrays.asList( actual ), delta );
	}

	/**
	 * Asserts that one vector is equal to another vector.
	 *
	 * @param messagePrefix Prefix to failure messages.
	 * @param expected      Expected vector value.
	 * @param actual        Actual vector value.
	 * @param delta         Delta value to limit the acceptable value range.
	 *
	 * @throws AssertionError is the assertion fails.
	 */
	public static void assertEquals( final String messagePrefix, final List<Vector3D> expected, final List<Vector3D> actual, final double delta )
	{
		final String actualPrefix = ( ( messagePrefix != null ) ? messagePrefix + " - " : "" );

		if ( expected == null )
		{
			Assert.assertNull( actualPrefix + "expected" + " is 'null', '" + "actual" + "' is not", actual );
		}
		else
		{
			Assert.assertNotNull( actualPrefix + "actual" + " is 'null', '" + "expected" + "' is not", actual );
			Assert.assertEquals( actualPrefix + '\'' + "expected" + "' should have same length as '" + "actual" + '\'', expected.size(), actual.size() );

			for ( int i = 0; i < expected.size(); i++ )
			{
				final String expectedValueName = "expected" + "[ " + i + " ]";
				final String actualValueName = "actual" + "[ " + i + " ]";
				final Vector3D expectedValue = expected.get( i );
				final Vector3D actualValue = actual.get( i );

				assertEquals( actualPrefix + "mismatch " + expectedValueName + " == " + actualValueName, expectedValue, actualValue, delta );
			}
		}
	}
}
