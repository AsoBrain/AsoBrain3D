/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2009
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.jetbrains.annotations.NotNull;

/**
 * This class loads resources from the file system.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class FileResourceLoader
	implements ResourceLoader
{
	/**
	 * Base directory to read resources from.
	 */
	private final File _directory;

	/**
	 * Constructs resource loader.
	 *
	 * @param   path    Path to base directory to read resources from.
	 *
	 * @throws FileNotFoundException when path could not be found or is not a directory.
	 */
	public FileResourceLoader( @NotNull final String path )
		throws FileNotFoundException
	{
		this( new File( path ) );
	}

	/**
	 * Constructs resource loader.
	 *
	 * @param   directory   Base directory to read resources from.
	 *
	 * @throws  FileNotFoundException if the directory is not accessible.
	 */
	public FileResourceLoader( @NotNull final File directory )
		throws FileNotFoundException
	{
		if ( !directory.canRead() || !directory.isDirectory() )
		{
			throw new FileNotFoundException( "Could not open directory \"" + directory.getPath() + "\"" );
		}

		_directory = directory;
	}

	/**
	 * Get resource file for resource with given name.
	 *
	 * @param   name    Name of the resource to get.
	 *
	 * @return  Resource {@link File}.
	 */
	@NotNull
	protected File getFile( final String name )
	{
		return new File( _directory , name );
	}

	public InputStream getResource( final String name )
		throws FileNotFoundException
	{
		return new FileInputStream( getFile( name ) );
	}
}
