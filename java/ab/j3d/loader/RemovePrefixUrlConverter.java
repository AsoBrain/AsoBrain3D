/*
 * (C) Copyright Numdata BV 2015-2015 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.loader;

import java.net.*;

import org.jetbrains.annotations.*;

/**
 * Converts URLs to strings with {@link URL#toExternalForm()} and removes a
 * specific prefix (if found).
 */
public class RemovePrefixUrlConverter
implements UrlConverter
{
	/**
	 * Prefix to remove.
	 */
	private final String _removeUrlPrefix;

	/**
	 * Constructs a new instance.
	 *
	 * @param removeUrlPrefix Prefix to remove.
	 */
	public RemovePrefixUrlConverter( @NotNull final String removeUrlPrefix )
	{
		_removeUrlPrefix = removeUrlPrefix;
	}

	@NotNull
	public String convertToString( @NotNull final URL url )
	{
		String result = url.toExternalForm();
		final String removeUrlPrefix = _removeUrlPrefix;
		if ( result.startsWith( removeUrlPrefix ) )
		{
			result = result.substring( removeUrlPrefix.length() );
		}
		return result;
	}
}
