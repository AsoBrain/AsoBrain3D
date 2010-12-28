/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2010-2010
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

import com.numdata.oss.io.*;
import org.jetbrains.annotations.*;

/**
 * This class implements the {@link ResourceLoader} interface for resources
 * relative to a designated URI.
 *
 * @author  Peter S. Heijnen.
 * @version $Revision$ $Date$
 */
public class URIResourceLoader
	implements ResourceLoader
{
	/**
	 * Base URI to load resources from.
	 */
	@NotNull
	private final URI _baseURI;

	/**
	 * Construct resource loader.
	 *
	 * @param   baseURI     Base URI to load resources from.
	 */
	public URIResourceLoader( @NotNull final URI baseURI )
	{
		_baseURI = baseURI;
	}

	@Override
	public InputStream getResource( final String name )
		throws IOException
	{
		final URIConnection connection = URITools.openConnection( _baseURI.resolve( name ) );
		return connection.getInputStream();
	}
}