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
import java.util.*;
import javax.xml.*;

import ab.xml.*;

/**
 * Writes KML files. KML is an XML language focused on geographic visualization,
 * including annotation of maps and images. Geographic visualization includes
 * not only the presentation of graphical data on the globe, but also the
 * control of the user's navigation in the sense of where to go and where to
 * look.
 *
 * <p>
 * NOTE: This implementation writes only very minimalistic KML files, the bare minimum for SketchUp inter-operability.
 *
 * @see     <a href="http://www.opengeospatial.org/standards/kml/">Open Geospatial Consortium KML standard</a>
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class KmlWriter
{
	/**
	 * Namespace URI for KML version 2.2.
	 */
	public static final String NS_KML_2_2 = "http://www.opengis.net/kml/2.2";

	/**
	 * Internet Media Type for KML files.
	 */
	public static final String MEDIA_TYPE = "application/vnd.google-earth.kml+xml";

	/**
	 * URLs of models to be written.
	 */
	private final List<String> _modelHrefs;

	/**
	 * Constructs a new instance.
	 */
	public KmlWriter()
	{
		_modelHrefs = new ArrayList<String>();
	}

	/**
	 * Adds a model to the KML file by linking to the given URI.
	 *
	 * @param   modelHref   Model URI.
	 */
	public void addModelHref( final String modelHref )
	{
		_modelHrefs.add( modelHref );
	}

	/**
	 * Writes the KML file to the given output stream.
	 *
	 * @param   out     Stream to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	public void write( final OutputStream out )
		throws XMLException
	{
		final XMLWriterFactory factory = XMLWriterFactory.newInstance();
		factory.setIndenting( true );

		final XMLWriter writer = factory.createXMLWriter( out, "UTF-8" );
		write( writer );
		writer.flush();
	}

	/**
	 * Writes the KML file to the given writer.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	public void write( final XMLWriter writer )
		throws XMLException
	{
		writer.startDocument();
		writer.setPrefix( XMLConstants.DEFAULT_NS_PREFIX, NS_KML_2_2 );
		writer.startTag( NS_KML_2_2, "kml" );

		writer.startTag( NS_KML_2_2, "Placemark" );
		for ( final String modelHref : _modelHrefs )
		{
			writer.startTag( NS_KML_2_2, "Model" );

			// Location is required by Sketchup Pro 2015
			writer.startTag( NS_KML_2_2, "Location" );
			writer.startTag( NS_KML_2_2, "latitude" );
			writer.text( "0" );
			writer.endTag( NS_KML_2_2, "latitude" );
			writer.startTag( NS_KML_2_2, "longitude" );
			writer.text( "0" );
			writer.endTag( NS_KML_2_2, "longitude" );
			writer.startTag( NS_KML_2_2, "altitude" );
			writer.text( "0" );
			writer.endTag( NS_KML_2_2, "altitude" );
			writer.endTag( NS_KML_2_2, "Location" );

			writer.startTag( NS_KML_2_2, "Link" );
			writer.startTag( NS_KML_2_2, "href" );
			writer.text( modelHref );
			writer.endTag( NS_KML_2_2, "href" );
			writer.endTag( NS_KML_2_2, "Link" );

			writer.endTag( NS_KML_2_2, "Model" );
		}
		writer.endTag( NS_KML_2_2, "Placemark" );

		writer.endTag( NS_KML_2_2, "kml" );
		writer.endDocument();
	}
}
