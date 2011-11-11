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

import javax.xml.stream.*;

import org.jetbrains.annotations.*;

/**
 * XML reader implementation that uses StAX, the Streaming API for XML.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
class StaxReader
	implements XMLReader
{
	/**
	 * StAX reader to be used.
	 */
	private XMLStreamReader _reader;

	/**
	 * Event type returned by the last call to {@link #next()}.
	 */
	@NotNull
	private XMLEventType _eventType;

	/**
	 * Constructs a new instance.
	 *
	 * @param   reader  StAX reader to be used.
	 */
	public StaxReader( final XMLStreamReader reader )
	{
		_reader = reader;
		_eventType = XMLEventType.START_DOCUMENT;
	}

	@NotNull
	@Override
	public XMLEventType getEventType()
	{
		return _eventType;
	}

	@NotNull
	@Override
	public XMLEventType next()
		throws XMLException
	{
		if ( _eventType == XMLEventType.END_DOCUMENT )
		{
			throw new IllegalStateException( "Not allowed after " + XMLEventType.END_DOCUMENT + " event." );
		}

		try
		{
			if ( !_reader.hasNext() )
			{
				throw new XMLException( "Unexpected end of document." );
			}
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}

		XMLEventType result;
		do
		{
			final int eventType;
			try
			{
				eventType = _reader.next();
			}
			catch ( XMLStreamException e )
			{
				throw new XMLException( e );
			}

			if ( eventType == XMLStreamConstants.START_ELEMENT )
			{
				result = XMLEventType.START_ELEMENT;
			}
			else if ( eventType == XMLStreamConstants.END_ELEMENT )
			{
				result = XMLEventType.END_ELEMENT;
			}
			else if ( eventType == XMLStreamConstants.PROCESSING_INSTRUCTION )
			{
				result = XMLEventType.PROCESSING_INSTRUCTION;
			}
			else if ( eventType == XMLStreamConstants.CHARACTERS )
			{
				result = XMLEventType.CHARACTERS;
			}
			else if ( eventType == XMLStreamConstants.COMMENT )
			{
				result = null;
			}
			else if ( eventType == XMLStreamConstants.SPACE )
			{
				result = XMLEventType.CHARACTERS;
			}
			else if ( eventType == XMLStreamConstants.START_DOCUMENT )
			{
				result = XMLEventType.START_DOCUMENT;
			}
			else if ( eventType == XMLStreamConstants.END_DOCUMENT )
			{
				result = XMLEventType.END_DOCUMENT;
			}
			else if ( eventType == XMLStreamConstants.ENTITY_REFERENCE )
			{
				result = XMLEventType.CHARACTERS;
			}
			else if ( eventType == XMLStreamConstants.ATTRIBUTE )
			{
				result = null;
			}
			else if ( eventType == XMLStreamConstants.DTD )
			{
				result = XMLEventType.DTD;
			}
			else if ( eventType == XMLStreamConstants.CDATA )
			{
				result = XMLEventType.CHARACTERS;
			}
			else if ( eventType == XMLStreamConstants.NAMESPACE )
			{
				result = null;
			}
			else if ( eventType == XMLStreamConstants.NOTATION_DECLARATION )
			{
				result = null;
			}
			else if ( eventType == XMLStreamConstants.ENTITY_DECLARATION )
			{
				result = null;
			}
			else
			{
				throw new XMLException( "Unknown event type: " + eventType );
			}
		}
		while ( result == null );

		_eventType = result;

		return result;
	}

	@Override
	public String getNamespaceURI()
	{
		final XMLEventType eventType = _eventType;
		if ( ( eventType != XMLEventType.START_ELEMENT ) &&
		     ( eventType != XMLEventType.END_ELEMENT ) )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return _reader.getNamespaceURI();
	}

	@NotNull
	@Override
	public String getLocalName()
	{
		final XMLEventType eventType = _eventType;
		if ( ( eventType != XMLEventType.START_ELEMENT ) &&
		     ( eventType != XMLEventType.END_ELEMENT ) )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return _reader.getLocalName();
	}

	@Override
	public int getAttributeCount()
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return _reader.getAttributeCount();
	}

	@Override
	public String getAttributeNamespaceURI( final int index )
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( ( index < 0 ) || ( index >= getAttributeCount() ) )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ")" );
		}

		return _reader.getAttributeNamespace( index );
	}

	@NotNull
	@Override
	public String getAttributeLocalName( final int index )
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( ( index < 0 ) || ( index >= getAttributeCount() ) )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ")" );
		}

		return _reader.getAttributeLocalName( index );
	}

	@NotNull
	@Override
	public String getAttributeValue( final int index )
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( ( index < 0 ) || ( index >= getAttributeCount() ) )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ")" );
		}

		return _reader.getAttributeValue( index );
	}

	@Override
	public String getAttributeValue( final String namespaceURI, @NotNull final String localName )
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return _reader.getAttributeValue( namespaceURI, localName );
	}

	@NotNull
	@Override
	public String getText()
	{
		if ( _eventType != XMLEventType.CHARACTERS )
		{
			throw new IllegalStateException( "Not allowed for " + _eventType );
		}

		return _reader.getText();
	}

	@NotNull
	@Override
	public String getPITarget()
	{
		if ( _eventType != XMLEventType.PROCESSING_INSTRUCTION )
		{
			throw new IllegalStateException( "Not allowed for " + _eventType );
		}

		return _reader.getPITarget();
	}

	@NotNull
	@Override
	public String getPIData()
	{
		if ( _eventType != XMLEventType.PROCESSING_INSTRUCTION )
		{
			throw new IllegalStateException( "Not allowed for " + _eventType );
		}

		return _reader.getPIData();
	}
}
