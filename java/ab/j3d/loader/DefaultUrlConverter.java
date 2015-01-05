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
 * Converts URLs to strings with {@link URL#toExternalForm()}.
 *
 * @author Gerrit Meinders
 */
public class DefaultUrlConverter
implements UrlConverter
{
	@NotNull
	public String convertToString( @NotNull final URL url )
	{
		return url.toExternalForm();
	}
}
