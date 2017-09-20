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
import java.util.zip.*;

import org.jetbrains.annotations.*;

/**
 * This class loads resources from a ZIP-archive file.
 *
 * @author Peter S. Heijnen
 */
public class ZipFileResourceLoader
implements ResourceLoader
{
	/**
	 * ZIP file.
	 */
	private final ZipFile _zipFile;

	/**
	 * Constructs resource loader for ZIP file.
	 *
	 * @param zipFile ZIP file.
	 *
	 * @throws IOException if an error occurs while accessing the ZIP file.
	 */
	public ZipFileResourceLoader( @NotNull final File zipFile )
	throws IOException
	{
		this( new ZipFile( zipFile ) );
	}

	/**
	 * Constructs resource loader for ZIP file.
	 *
	 * @param zipFile ZIP file.
	 */
	public ZipFileResourceLoader( @NotNull final ZipFile zipFile )
	{
		_zipFile = zipFile;
	}

	@Nullable
	public URL getResource( final String path )
	{
		try
		{
			final String zipFileUrl = new File( _zipFile.getName() ).toURI().toURL().toExternalForm();
			return new URL( "jar:" + zipFileUrl + ( ( path.charAt( 0 ) == '/' ) ? "!" : "!/" ) + path );
		}
		catch ( final MalformedURLException e )
		{
			throw new IllegalArgumentException( path, e );
		}
	}

	@Nullable
	public InputStream getResourceAsStream( final String path )
	{
		InputStream result = null;

		final ZipEntry entry = _zipFile.getEntry( path );
		if ( entry != null )
		{
			try
			{
				result = _zipFile.getInputStream( entry );
			}
			catch ( final IOException ignored )
			{
			}
		}

		return result;
	}
}
