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
import java.util.zip.*;

import org.jetbrains.annotations.*;

/**
 * This class loads a specified resource from a ZIP-archive.
 *
 * @author  Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public class ZipResourceLoader
	implements ResourceLoader
{
	/**
	 * URL of ZIP file.
	 */
	private final URL _zipUrl;

	/**
	 *  The zip file as binary data.
	 */
	private byte[] _zipData;

	/**
	 * Constructs resource loader for ZIP file.
	 *
	 * @param   zipUrl  URL to ZIP archive.
	 */
	public ZipResourceLoader( @NotNull final URL zipUrl )
	{
		_zipUrl = zipUrl;
		_zipData = null;
	}

	/**
	 * Constructs resource loader for ZIP file.
	 *
	 * @param   zipData     Contents of ZIP file.
	 */
	public ZipResourceLoader( @NotNull final byte[] zipData )
	{
		_zipUrl = null;
		_zipData = zipData;
	}

	@Nullable
	public URL getResource( final String path )
	{
		URL result = null;

		final URL zipUrl = _zipUrl;
		if ( ( zipUrl != null ) && ( path != null ) && !path.isEmpty() )
		{
			try
			{
				final String separator = ( path.charAt( 0 ) == '/' ) ? "!" : "!/";
				result = new URL( "jar:" + zipUrl.toExternalForm() + separator + path );
			}
			catch ( MalformedURLException e )
			{
				throw new IllegalArgumentException( path, e );
			}
		}

		return result;
	}

	public InputStream getResourceAsStream( final String path )
	{
		final byte[] binaryData = _zipData;
		InputStream result = null;
		if ( ( binaryData != null ) && binaryData.length > 0 )
		{
			try
			{
				final ZipInputStream zis = getZipInputStream();
				try
				{
					while ( true )
					{
						final ZipEntry zipEntry = zis.getNextEntry();
						if ( path.equals( zipEntry.getName() ) )
						{
							result = zis;
							break;
						}
					}
				}
				finally
				{
					zis.close();
				}
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Get {@link ZipInputStream} for the ZIP file.
	 *
	 * @return  {@link ZipInputStream}.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	private ZipInputStream getZipInputStream()
		throws IOException
	{
		byte[] binaryData = _zipData;
		if ( binaryData == null )
		{
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();

			final InputStream in = _zipUrl.openStream();
			try
			{
				final byte [] buffer = new byte[ 10240 ];
				for ( int length = in.read( buffer ); length > 0; length = in.read( buffer ) )
				{
					bos.write( buffer, 0, length );
				}
			}
			finally
			{
				in.close();
			}

			binaryData = bos.toByteArray();
			_zipData = binaryData;
		}

		return new ZipInputStream( new ByteArrayInputStream( binaryData ) );
	}
}
