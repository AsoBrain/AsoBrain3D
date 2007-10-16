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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
	 * The path to the file
	 */
	private String _path;

	/**
	 * Constructs a new FileResourceLoader based on the given path.
	 *
	 * @param path The path to the file.
	 */
	public FileResourceLoader( final String path ){
		_path = path;
	}


	public InputStream getResource( final String name )
	{
		FileInputStream result;
		try
		{
			result =  new FileInputStream( _path + name );
		}
		catch ( FileNotFoundException e )
		{/**
 * This class loads a specified resource from a zip-archive.
 * <br /><br />
 * Zip archive can be binary data or a ZipFile object.
 *
 * @author Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
			result =  null;
		}
		return result;
	}
}
