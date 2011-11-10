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

import java.util.*;
import javax.xml.stream.*;

import org.jetbrains.annotations.*;

/**
 * XML writer implementation that uses StAX, the Streaming API for XML.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
class StaxWriter
	implements XMLWriter
{
	/**
	 * StAX writer to be used.
	 */
	private XMLStreamWriter _writer;

	/**
	 * Character encoding of the XML document.
	 */
	private String _encoding;

	/**
	 * <code>true</code> if the writer is currently writing an empty tag.
	 */
	private boolean _empty = false;

	/**
	 * Namespace declarations that need to be added to the next start element.
	 */
	private List<NamespaceDeclaration> _namespaces = new ArrayList<NamespaceDeclaration>();

	/**
	 * Constructs a new instance.
	 *
	 * @param   writer      StAX writer to be used.
	 * @param   encoding    Character encoding of the XML document.
	 */
	public StaxWriter( final XMLStreamWriter writer, final String encoding )
	{
		_writer = writer;
		_encoding = encoding;
	}

	@Override
	public void startDocument()
		throws XMLException
	{
		try
		{
			_writer.writeStartDocument( _encoding, "1.0" );
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void setPrefix( @NotNull final String prefix, @NotNull final String namespaceURI )
		throws XMLException
	{
		try
		{
			_writer.setPrefix( prefix, namespaceURI );
			_namespaces.add( new NamespaceDeclaration( prefix, namespaceURI ) );
		}
		catch ( XMLStreamException e )
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
			_writer.writeStartElement( namespaceURI, localName );
			for ( final NamespaceDeclaration namespace : _namespaces )
			{
				_writer.writeNamespace( namespace._prefix, namespace._namespaceURI );
			}
			_namespaces.clear();
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void emptyTag( final String namespaceURI, @NotNull final String localName )
		throws XMLException
	{
		try
		{
			_writer.writeEmptyElement( namespaceURI, localName );
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
		_empty = true;
	}

	@Override
	public void attribute( final String namespaceURI, @NotNull final String localName, @NotNull final String value )
		throws XMLException
	{
		try
		{
			if ( namespaceURI == null )
			{
				_writer.writeAttribute( localName, value );
			}
			else
			{
				_writer.writeAttribute( namespaceURI, localName, value );
			}
		}
		catch ( XMLStreamException e )
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
			_writer.writeCharacters( characters );
		}
		catch ( XMLStreamException e )
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
		else
		{
			try
			{
				_writer.writeEndElement();
			}
			catch ( XMLStreamException e )
			{
				throw new XMLException( e );
			}
		}
	}

	@Override
	public void endDocument()
		throws XMLException
	{
		try
		{
			_writer.writeEndDocument();
		}
		catch ( XMLStreamException e )
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
			_writer.flush();
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	/**
	 * Namespace declaration.
	 */
	private class NamespaceDeclaration
	{
		/**
		 * Prefix.
		 */
		private String _prefix;

		/**
		 * Namespace URI.
		 */
		private String _namespaceURI;

		/**
		 * Constructs a new instance.
		 *
		 * @param   prefix          Prefix.
		 * @param   namespaceURI    Namespace URI.
		 */
		public NamespaceDeclaration( final String prefix, final String namespaceURI )
		{
			_prefix = prefix;
			_namespaceURI = namespaceURI;
		}
	}
}
