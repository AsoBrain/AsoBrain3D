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

import org.xmlpull.v1.*;

/**
 * XML writer factory for writers that use XML Pull. The main advantage of
 * XML Pull is its availability on Android (all versions).
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class XmlPullWriterFactory
	extends XMLWriterFactory
{
	/**
	 * Parser factory to create {@link XmlSerializer} instances with.
	 */
	private XmlPullParserFactory _parserFactory;

	/**
	 * Constructs a new instance.
	 */
	public XmlPullWriterFactory()
	{
		final XmlPullParserFactory parserFactory;
		try
		{
			parserFactory = XmlPullParserFactory.newInstance();
		}
		catch ( XmlPullParserException e )
		{
			throw new FactoryException( e );
		}
		_parserFactory = parserFactory;
	}

	@Override
	public XMLWriter createXMLWriter( final OutputStream out, final String encoding )
		throws XMLException
	{
		XmlSerializer serializer;
		try
		{
			serializer = _parserFactory.newSerializer();
		}
		catch ( XmlPullParserException e )
		{
			throw new XMLException( e );
		}

		try
		{
			serializer.setOutput( out, encoding );
		}
		catch ( IOException e )
		{
			throw new XMLException( e );
		}

		if ( isIndenting() )
		{
			final IndentingXmlSerializer indenting = new IndentingXmlSerializer( serializer );
			indenting.setNewline( getNewline() );
			indenting.setIndent( getIndent() );
			serializer = indenting;
		}

		return new XmlPullWriter( serializer, encoding );
	}

	@Override
	public XMLWriter createXMLWriter( final Writer writer, final String encoding )
		throws XMLException
	{
		XmlSerializer serializer;
		try
		{
			serializer = _parserFactory.newSerializer();
		}
		catch ( XmlPullParserException e )
		{
			throw new XMLException( e );
		}

		try
		{
			serializer.setOutput( writer );
		}
		catch ( IOException e )
		{
			throw new XMLException( e );
		}

		if ( isIndenting() )
		{
			final IndentingXmlSerializer indenting = new IndentingXmlSerializer( serializer );
			indenting.setNewline( getNewline() );
			indenting.setIndent( getIndent() );
			serializer = indenting;
		}

		return new XmlPullWriter( serializer, encoding );
	}
}
