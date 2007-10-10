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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * This class loads a specified resource from a zip-archive.
 * <p />
 * Zip archive can be binary data or a ZipFile object.
 *
 * @author Wijnand Wieskamp
 */
public class ZipResourceLoader
implements ResourceLoader
{
	/**
	 * The ZipFile to read the specified resource from.
	 */
	private ZipFile _zipFile = null;

	/**
	 * The binary zip-archive.
	 */
	private byte[] _compressedData = null;

	/**
	 * Constructs a ZipResourceLoader based on given {@link ZipFile}
	 *
	 * @param zipFile The ZipFile object to load resources from.
	 */
	public ZipResourceLoader( final ZipFile zipFile )
	{
		_zipFile = zipFile;
	}

	/**
	 * Constructs a ZipResourceLoader based on given binary data.
	 *
	 * @param binaryData The zip archive as binary data
	 */
	public ZipResourceLoader( final byte[] binaryData )
	{
		_compressedData = binaryData.clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * In this case, a zip archive.
	 */
	public InputStream getResource( final String name )
	throws IOException
	{
		ZipEntry zipEntry;
		final ZipFile zipFile = _zipFile;
		InputStream result = null;
		if ( zipFile != null )
		{
			zipEntry = zipFile.getEntry( name );
			result = zipFile.getInputStream( zipEntry );
		}
		else if ( _compressedData != null )
		{
			final ZipInputStream zipInputStream = new ZipInputStream( new ByteArrayInputStream( _compressedData ) );
			while ( ( zipEntry = zipInputStream.getNextEntry() ) != null )
			{
				if ( name.equals( zipEntry.getName() ) )
				{
					result = zipInputStream;
				}
			}
		}
		if ( result == null )
			throw new FileNotFoundException( "File \"" + name + "\" not found in specified archive." );
		return result;
	}
}