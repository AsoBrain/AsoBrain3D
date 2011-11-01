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
import java.util.*;

/**
 * This class is not an actual resource loader, it only forwards calls to the 
 * actual resource loader it was constructed with. Additionally, it remembers 
 * the files being asked for and stores them in a {@link HashSet}.
 *
 * <br /><br />
 *
 * @author  Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public class WrappedResourceLoader
	implements ResourceLoader
{
	/**
	 * Actual resourceloader
	 */
	private ResourceLoader _actualResourceLoader;

	/**
	 * Set of requested files.
	 */
	private Set<String> _requestedFiles = new HashSet<String>();

	/**
	 * Constructs a new {@link WrappedResourceLoader}.
	 *
	 * @param actualResourceLoader  The actual resourceloader to load resource from.
	 */
	public WrappedResourceLoader( final ResourceLoader actualResourceLoader )
	{
		_actualResourceLoader = actualResourceLoader;
	}

	public InputStream getResourceAsStream( final String path )
	{
		_requestedFiles.add( path );
		return _actualResourceLoader.getResourceAsStream( path );
	}

	public URL getResource( final String path )
	{
		_requestedFiles.add( path );
		return _actualResourceLoader.getResource( path );
	}

	/**
	 * Returns a collection of requested file names as strings.
	 *
	 * @return  a collection of requested file names a strings.
	 */
	public Collection<String> getRequestedFiles()
	{
		return Collections.unmodifiableSet( _requestedFiles );
	}

}
