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

		final StAXColladaWriter colladaWriter = new StAXColladaWriter( _scene );
		colladaWriter.write( zipOut );

		for ( final URI imageURI : colladaWriter.getImages() )
		{
			if ( !imageURI.isAbsolute() )
			{
				final String path = imageURI.toString();
				zipOut.putNextEntry( new ZipEntry( path ) );

				// FIXME: Resolve image URIs the 'new way'. (Pending some ab3d architecture changes.)
				final URL imageURL = imageURI.toURL();
				final InputStream imageIn = new BufferedInputStream( imageURL.openStream() );
				try
				{
					int c;
					while ( ( c = imageIn.read() ) != -1 )
					{
						zipOut.write( c );
					}
				}
				finally
				{
					imageIn.close();
				}
			}
		}

		zipOut.close();
	}
}
