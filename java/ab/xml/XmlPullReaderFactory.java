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

import org.jetbrains.annotations.*;
import org.xmlpull.v1.*;

/**
 * Factory for XML readers that use XML Pull.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
class XmlPullReaderFactory
	extends XMLReaderFactory
{
	/**
	 * Factory used to create XML Pull readers.
	 */
	private XmlPullParserFactory _factory;

	/**
	 * Constructs a new instance.
	 */
	public XmlPullReaderFactory()
	{
		try
		{
			final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware( true );
			_factory = factory;
		}
		catch ( XmlPullParserException e )
		{
			throw new FactoryException( e );
		}
	}

	@Override
	public XMLReader createXMLReader( @NotNull final InputStream in, final String encoding )
		throws XMLException
	{
		final XmlPullParser parser;
		try
		{
			parser = _factory.newPullParser();
			parser.setInput( in, encoding );
		}
		catch ( XmlPullParserException e )
		{
			throw new XMLException( e );
		}

		return new XmlPullReader( parser );
	}
}
