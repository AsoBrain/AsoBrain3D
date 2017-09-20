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
	private final XMLStreamReader _reader;

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
	StaxReader( final XMLStreamReader reader )
	{
		_reader = reader;
		_eventType = XMLEventType.START_DOCUMENT;
	}

	@NotNull
	public XMLEventType getEventType()
	{
		return _eventType;
	}

	@NotNull
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
		catch ( final XMLStreamException e )
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
			catch ( final XMLStreamException e )
			{
				throw new XMLException( e );
			}

			switch ( eventType )
			{
				case XMLStreamConstants.START_ELEMENT:
					result = XMLEventType.START_ELEMENT;
					break;

				case XMLStreamConstants.END_ELEMENT:
					result = XMLEventType.END_ELEMENT;
					break;

				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					result = XMLEventType.PROCESSING_INSTRUCTION;
					break;

				case XMLStreamConstants.CHARACTERS:
					result = XMLEventType.CHARACTERS;
					break;

				case XMLStreamConstants.COMMENT:
					result = null;
					break;

				case XMLStreamConstants.SPACE:
					result = XMLEventType.CHARACTERS;
					break;

				case XMLStreamConstants.START_DOCUMENT:
					result = XMLEventType.START_DOCUMENT;
					break;

				case XMLStreamConstants.END_DOCUMENT:
					result = XMLEventType.END_DOCUMENT;
					break;

				case XMLStreamConstants.ENTITY_REFERENCE:
					result = XMLEventType.CHARACTERS;
					break;

				case XMLStreamConstants.ATTRIBUTE:
					result = null;
					break;

				case XMLStreamConstants.DTD:
					result = XMLEventType.DTD;
					break;

				case XMLStreamConstants.CDATA:
					result = XMLEventType.CHARACTERS;
					break;

				case XMLStreamConstants.NAMESPACE:
					result = null;
					break;

				case XMLStreamConstants.NOTATION_DECLARATION:
					result = null;
					break;

				case XMLStreamConstants.ENTITY_DECLARATION:
					result = null;
					break;

				default:
					throw new XMLException( "Unknown event type: " + eventType );
			}
		}
		while ( result == null );

		_eventType = result;

		return result;
	}

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

	public int getAttributeCount()
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return _reader.getAttributeCount();
	}

	public String getAttributeNamespaceURI( final int index )
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( ( index < 0 ) || ( index >= getAttributeCount() ) )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ')' );
		}

		return _reader.getAttributeNamespace( index );
	}

	@NotNull
	public String getAttributeLocalName( final int index )
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( ( index < 0 ) || ( index >= getAttributeCount() ) )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ')' );
		}

		return _reader.getAttributeLocalName( index );
	}

	@NotNull
	public String getAttributeValue( final int index )
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( ( index < 0 ) || ( index >= getAttributeCount() ) )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ')' );
		}

		return _reader.getAttributeValue( index );
	}

	public String getAttributeValue( @NotNull final String localName )
	{
		final XMLEventType eventType = _eventType;
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		String result = null;

		final int attributeCount = _reader.getAttributeCount();
		for ( int i = 0; i < attributeCount; i++ )
		{
			if ( localName.equals( _reader.getAttributeLocalName( i ) ) )
			{
				result = _reader.getAttributeValue( i );
				break;
			}
		}

		return result;
	}

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
	public String getText()
	{
		if ( _eventType != XMLEventType.CHARACTERS )
		{
			throw new IllegalStateException( "Not allowed for " + _eventType );
		}

		return _reader.getText();
	}

	@NotNull
	public String getPITarget()
	{
		if ( _eventType != XMLEventType.PROCESSING_INSTRUCTION )
		{
			throw new IllegalStateException( "Not allowed for " + _eventType );
		}

		return _reader.getPITarget();
	}

	@NotNull
	public String getPIData()
	{
		if ( _eventType != XMLEventType.PROCESSING_INSTRUCTION )
		{
			throw new IllegalStateException( "Not allowed for " + _eventType );
		}

		return _reader.getPIData();
	}
}
