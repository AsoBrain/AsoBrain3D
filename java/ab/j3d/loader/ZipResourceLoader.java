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
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class loads a specified resource from a zip-archive.
 * <br /><br />
 * Zip archive can be binary data or a ZipFile object.
 *
 * @author Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public class ZipResourceLoader
	implements ResourceLoader
{

	/** The zip file as binary data constructed from ByteArrayInputStream @ constrcutor
	 */
	private byte[] _binaryData = null;

	/**
	 * Constructs a ZipResourceLoader based on given InputStream.
	 *
	 * @param inputStream The zip archive as InputStream.
	 *
	 * @throws IOException when ByteArrayInputStream could not be read.
	 */
	public ZipResourceLoader( final InputStream inputStream)
	throws IOException
	{
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int length;
		final byte [] buffer = new byte[10240]; //10K
        while ( ( length = inputStream.read( buffer ) ) > 0 )
        {
			bos.write(buffer, 0, length);
		}
		_binaryData = bos.toByteArray();
	}

	/**
	 * {@inheritDoc}
	 *
	 * In this case, a zip archive.
	 */
	public InputStream getResource( final String name )
		throws IOException
	{
		final byte[] binaryData = _binaryData;
		ZipEntry zipEntry;
		InputStream result = null;
		if ( binaryData != null && binaryData.length > 0)
		{
			final ZipInputStream zipInputStream = new ZipInputStream( new ByteArrayInputStream( binaryData ) );
			while ( result == null && ( zipEntry = zipInputStream.getNextEntry() ) != null)
			{
				System.out.println( "zipEntry.getName() = " + zipEntry.getName() );
				if ( name.equals( zipEntry.getName() ) )
				{
					result = zipInputStream;
				}
			}
		}
		if ( result == null )
			throw new FileNotFoundException( "File \"" + name + "\" not found in specified archive." );
		System.out.println( "Returning from zipresource : " + name );
		return result;
	}
}