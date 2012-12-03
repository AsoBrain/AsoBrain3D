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
package ab.xml;

import java.io.*;
import javax.xml.stream.*;

import org.jetbrains.annotations.*;

/**
 * Factory for XML readers that use StAX, the Streaming API for XML.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
class StaxReaderFactory
	extends XMLReaderFactory
{
	/**
	 * Factory used to create StAX readers.
	 */
	private XMLInputFactory _factory;

	/**
	 * Constructs a new instance.
	 */
	public StaxReaderFactory()
	{
		try
		{
			XMLInputFactory factory;
			try
			{
				// NOTE: For OpenJDK, this requires a file 'META-INF/services/com.sun.xml.internal.stream.XMLInputFactoryImpl' containing the factory class name.
				factory = XMLInputFactory.newFactory( "com.sun.xml.internal.stream.XMLInputFactoryImpl", getClass().getClassLoader() );
			}
			catch ( FactoryConfigurationError e )
			{
				factory = XMLInputFactory.newFactory();
				System.err.println( getClass().getName() + ": Using StAX factory class " + factory.getClass().getName() + ", which is not the recommended implementation." );
			}

			factory.setProperty( XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE );
			factory.setProperty( XMLInputFactory.IS_COALESCING, Boolean.TRUE );
			factory.setProperty( XMLInputFactory.SUPPORT_DTD, Boolean.FALSE );

			_factory = factory;
		}
		catch ( FactoryConfigurationError e )
		{
			throw new FactoryException( e );
		}
	}

	@Override
	public XMLReader createXMLReader( @NotNull final InputStream in, final String encoding )
		throws XMLException
	{
		final XMLStreamReader reader;
		try
		{
			final XMLInputFactory factory = _factory;
			reader = factory.createXMLStreamReader( in, encoding );
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}

		return new StaxReader( reader );
	}
}
