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

import org.jetbrains.annotations.*;
import org.xmlpull.v1.*;

/**
 * XML writer implementation that uses XML Pull.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
class XmlPullWriter
	implements XMLWriter
{
	/**
	 * XML Pull serializer to be used.
	 */
	private XmlSerializer _serializer;

	/**
	 * Character encoding of the XML document.
	 */
	private String _encoding;

	/**
	 * <code>true</code> if the writer is currently writing an empty tag.
	 */
	private boolean _empty;

	/**
	 * Constructs a new instance.
	 *
	 * @param   serializer  XML Pull serializer to be used.
	 * @param   encoding    Character encoding of the XML document.
	 */
	public XmlPullWriter( final XmlSerializer serializer, final String encoding )
	{
		_serializer = serializer;
		_encoding = encoding;
	}

	@Override
	public void setPrefix( @NotNull final String prefix, @NotNull final String namespaceURI )
		throws XMLException
	{
		try
		{
			_serializer.setPrefix( prefix, namespaceURI );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void startDocument()
		throws XMLException
	{
		try
		{
			_serializer.startDocument( _encoding, null );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void startTag( final String namespaceURI, @NotNull final String localName )
		throws XMLException
	{
		if ( _empty )
		{
			throw new XMLException( "Not allowed inside an empty tag. Use 'endTag' first." );
		}

		try
		{
			_serializer.startTag( namespaceURI, localName );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void emptyTag( @Nullable final String namespaceURI, @NotNull final String localName )
		throws XMLException
	{
		startTag( namespaceURI, localName );
		_empty = true;
	}

	@Override
	public void attribute( final String namespaceURI, @NotNull final String localName, @NotNull final String value )
		throws XMLException
	{
		try
		{
			_serializer.attribute( namespaceURI, localName, value );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void text( @NotNull final String characters )
		throws XMLException
	{
		if ( _empty )
		{
			throw new XMLException( "Not allowed inside an empty tag. Use 'endTag' first." );
		}

		try
		{
			_serializer.text( characters );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void endTag( final String namespaceURI, @NotNull final String localName )
		throws XMLException
	{
		if ( _empty )
		{
			_empty = false;
		}

		try
		{
			_serializer.endTag( namespaceURI, localName );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void endDocument()
		throws XMLException
	{
		try
		{
			_serializer.endDocument();
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void flush()
		throws XMLException
	{
		try
		{
			_serializer.flush();
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}
}
