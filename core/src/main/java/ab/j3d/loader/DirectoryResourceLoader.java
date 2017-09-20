/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2015 Peter S. Heijnen
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
package ab.j3d.loader;

import java.io.*;
import java.net.*;

import org.jetbrains.annotations.*;

/**
 * This class loads resources from a directory.
 *
 * @author Peter S. Heijnen
 */
public class DirectoryResourceLoader
implements ResourceLoader
{
	/**
	 * ZIP file.
	 */
	private final File _directory;

	/**
	 * Constructs resource loader for ZIP file.
	 *
	 * @param directory ZIP file.
	 */
	public DirectoryResourceLoader( @NotNull final File directory )
	{
		_directory = directory;
		if ( !directory.isDirectory() )
		{
			throw new IllegalArgumentException( directory + " is not a directory" );
		}
	}

	@Nullable
	public URL getResource( final String path )
	{
		try
		{
			return new File( _directory, path ).toURI().toURL();
		}
		catch ( final MalformedURLException e )
		{
			throw new IllegalArgumentException( path, e );
		}
	}

	@SuppressWarnings ( "IOResourceOpenedButNotSafelyClosed" )
	@Nullable
	public InputStream getResourceAsStream( final String path )
	{

		InputStream result = null;

		try
		{
			result = new FileInputStream( new File( _directory, path ) );
		}
		catch ( final IOException ignored )
		{
		}

		return result;
	}
}
