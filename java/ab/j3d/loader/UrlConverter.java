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
 * Performs an implementation-specific conversion from a URL to a string. Such a
 * conversion is typically needed to refer to a resource in some export format
 * that don't support URLs. For example, most applications don't support URLs in
 * MTL files (Wavefront OBJ format; see {@link ObjWriter}).
 *
 * @author Gerrit Meinders
 */
public interface UrlConverter
{
	/**
	 * Converts the given URL to a string.
	 *
	 * @param url URL to convert.
	 *
	 * @return String representation of the URL.
	 */
	@NotNull
	String convertToString( @NotNull URL url );
}
