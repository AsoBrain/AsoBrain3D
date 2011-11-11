/*
 * $Id$
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
import java.util.zip.*;

import ab.j3d.model.*;
import ab.xml.*;

/**
 * Writes KMZ files.
 *
 * @see     <a href="http://www.opengeospatial.org/standards/kml/">Open Geospatial Consortium KML standard</a>
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class KmzWriter
{
	/**
	 * Internet Media Type for KMZ files.
	 */
	public static final String MEDIA_TYPE = "application/vnd.google-earth.kmz";

	/**
	 * Scene to be written.
	 */
	private final Scene _scene;

	/**
	 * Constructs a new instance.
	 *
	 * @param   scene   Scene to be written.
	 */
	public KmzWriter( final Scene scene )
	{
		_scene = scene;
	}

	/**
	 * Writes the KMZ file to the given output stream.
	 *
	 * @param   out     Output stream to write to.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	public void write( final OutputStream out )
		throws IOException
	{
		final ZipOutputStream zipOut = new ZipOutputStream( out );
		zipOut.putNextEntry( new ZipEntry( "doc.kml" ) );

		final String colladaFileName = "model.dae";

		final KmlWriter kmlWriter = new KmlWriter();
		kmlWriter.addModelHref( colladaFileName );
		try
		{
			kmlWriter.write( zipOut );
		}
		catch ( XMLException e )
		{
			throw new IOException( e );
		}

		zipOut.putNextEntry( new ZipEntry( colladaFileName ) );

		final Map<String,URL> imageFiles = new HashMap<String, URL>();

		final ColladaWriter colladaWriter = new ColladaWriter( _scene )
		{
			@Override
			protected void writeImage( final XMLWriter writer, final String imageId, final String imageUri )
				throws XMLException
			{
				try
				{
					final URL imageUrl = new URL( imageUri );

					final String path = imageUrl.getPath();
					final String fileName = imageUri.substring( path.lastIndexOf( '/' ) + 1 );
					final int dot = fileName.lastIndexOf( '.' );
					final String extension = ( dot < 0 ) ? "" : fileName.substring( dot );
					final String localFileName = "image-" + imageId + extension;

					imageFiles.put( localFileName, imageUrl );

					super.writeImage( writer, imageId, localFileName );
				}
				catch ( MalformedURLException e )
				{
					super.writeImage( writer, imageId, imageUri );
				}
			}
		};

		try
		{
			colladaWriter.write( zipOut );
		}
		catch ( XMLException e )
		{
			throw new IOException( e );
		}

		zipOut.closeEntry();

		for ( final Map.Entry<String, URL> entry : imageFiles.entrySet() )
		{
			final String localFileName = entry.getKey();
			final URL imageURL = entry.getValue();

			zipOut.putNextEntry( new ZipEntry( localFileName ) );

			final byte[] buffer = new byte[ 65536 ];

			final InputStream imageIn = imageURL.openStream();
			try
			{
				int read;
				while ( ( read = imageIn.read( buffer ) ) >= 0 )
				{
					zipOut.write( buffer, 0, read );
				}
			}
			finally
			{
				imageIn.close();
			}

			zipOut.closeEntry();
		}

		zipOut.close();
	}
}
