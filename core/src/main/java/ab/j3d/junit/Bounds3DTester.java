/*
 * (C) Copyright Numdata BV 2021-2021 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.junit;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Tool class to help with testing {@link Bounds3D} objects.
 *
 * @author Gerrit Meinders
 */
public class Bounds3DTester
{
	/**
	 * Asserts that one bounds is equal to another bounds.
	 *
	 * @param messagePrefix Prefix to failure messages.
	 * @param expected      Expected vector value.
	 * @param actual        Actual vector value.
	 * @param delta         Delta value to limit the acceptable value range.
	 *
	 * @throws AssertionError is the assertion fails.
	 */
	public static void assertEquals( final @Nullable String messagePrefix, final @NotNull Bounds3D expected, final @NotNull Bounds3D actual, final double delta )
	{
		final String actualPrefix = messagePrefix == null ? "" : messagePrefix + " - ";
		Vector3DTester.assertEquals( actualPrefix + "Incorrect 'v1'", expected.v1, actual.v1, delta );
		Vector3DTester.assertEquals( actualPrefix + "Incorrect 'v2'", expected.v2, actual.v2, delta );
	}

	/**
	 * Not used.
	 */
	private Bounds3DTester()
	{
	}
}
