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
package ab.j3d.loader;

import java.io.*;
import java.net.*;

import org.jetbrains.annotations.*;

/**
 * This class implements the {@link ResourceLoader} interface for resources
 * relative to a designated URL.
 *
 * @author  Peter S. Heijnen.
 * @version $Revision$ $Date$
 */
public class URLResourceLoader
	implements ResourceLoader
{
	/**
	 * Base URL to load resources from.
	 */
	@NotNull
	private final URL _baseUrl;

	/**
	 * Construct resource loader.
	 *
	 * @param   baseUrl     Base URL to load resources from.
	 */
	public URLResourceLoader( @NotNull final URL baseUrl )
	{
		_baseUrl = baseUrl;
	}

	/**
	 * Construct texture map for base file.
	 *
	 * @param   baseFile   Image file.
	 */
	public URLResourceLoader( @NotNull final File baseFile )
	{
		final URI uri = baseFile.toURI();
		try
		{
			final URL url = uri.toURL();
			_baseUrl = url;
		}
		catch ( MalformedURLException e )
		{
			/* should never happen for file URI */
			throw new IllegalArgumentException( e );
		}
	}

	public URL getResource( final String path )
	{
		try
		{
			return new URL( _baseUrl, path );
		}
		catch ( MalformedURLException e )
		{
			throw new IllegalArgumentException( path, e );
		}
	}

	public InputStream getResourceAsStream( final String path )
	{
		InputStream result = null;

		final URL url = getResource( path );
		if ( url != null )
		{
			try
			{
				result = url.openStream();
			}
			catch ( IOException e )
			{
				System.err.println( "error: getResource( '" + url + "' ) => " + e );
			}
		}

		return result;
	}
}
