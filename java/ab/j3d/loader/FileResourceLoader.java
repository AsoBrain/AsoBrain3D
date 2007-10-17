/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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

import com.numdata.oss.TextTools;

/**
 * This class loads a resource from a disk using a FileInputStream and makes it available as InputStream
 * <br /><br />
 *
 * @author  Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public class FileResourceLoader
	implements ResourceLoader
{
	/**
	 * Default directory to read from
	 */
	public static final String DEFAULT_DIRECTORY = "./";

	/**
	 * Directory to read from based on given path in constructor.
	 */
	private final File _directory;

	/**
	 * Constructs a new FileResourceLoader based on the given directory.
	 *
	 * @param path Path to the directory to search.
	 *
	 * @throws FileNotFoundException when path could not be found or is not a directory.
	 */
	public FileResourceLoader( final String path )
		throws FileNotFoundException
	{
		final File directory;
		if( TextTools.isNonEmpty( path ) )
		{
			directory = new File( path );
		}
		else
		{
			directory = new File( DEFAULT_DIRECTORY );
			System.out.println( "No folder specified, using default folder: \"" + DEFAULT_DIRECTORY + "\"" );
		}
		if( directory.canRead() && directory.isDirectory() )
		{
			_directory = directory;
		}
		else
		{
			throw new FileNotFoundException( "Could not open directory \"" + path + "\"" );
		}
	}


	public InputStream getResource( final String name )
		throws FileNotFoundException
	{
		InputStream result = null;
		if( _directory != null )
		{

			final File returnFile = new File( _directory.getAbsolutePath() + File.separator + name );
			if ( returnFile.isFile() && returnFile.canRead() )
			{
				result = new FileInputStream( returnFile );
			}
		}
		return result;
	}
}
