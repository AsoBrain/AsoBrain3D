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

import javax.xml.namespace.*;
import javax.xml.stream.*;

/**
 * Wraps an underlying {@link XMLStreamWriter} to automatically add
 * newlines and indenting for elements. Elements that do no contain nested
 * elements are themselves indented, but have no newlines and indenting added
 * around their content.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class IndentingXMLStreamWriter
	implements XMLStreamWriter
{
	/**
	 * Underlying writer.
	 */
	private final XMLStreamWriter _writer;

	/**
	 * Current nesting depth, used for indenting.
	 */
	private int _depth = 0;

	/**
	 * String inserted as a newline.
	 */
	private String _newline = "\n";

	/**
	 * String inserted for each level of indenting.
	 */
	private String _indent = "\t";

	/**
	 * Whether the current node contains no elements (like an 'xsd:simpleType').
	 */
	private boolean _simpleType = true;

	/**
	 * Constructs a new indenting writer.
	 *
	 * @param   writer  Underlying writer.
	 */
	public IndentingXMLStreamWriter( final XMLStreamWriter writer )
	{
		_writer = writer;
	}

	/**
	 * Sets the string to be used as a newline.
	 * The default is <code>"\n"</code>.
	 *
	 * @param   newline     String for newlines.
	 */
	public void setNewline( final String newline )
	{
		_newline = newline;
	}

	/**
	 * Sets the string to be used for indenting.
	 * The default is <code>"\t"</code>.
	 *
	 * @param   indent      String for indenting.
	 */
	public void setIndent( final String indent )
	{
		_indent = indent;
	}

	/**
	 * Starts a new line with the proper amount of indenting and increases the
	 * nesting depth.
	 *
	 * @throws  XMLStreamException if the whitespace can't be written to the
	 *          underlying stream.
	 */
	private void indentIn()
		throws XMLStreamException
	{
		_writer.writeCharacters( _newline );
		for (int i = 0; i < _depth; i++)
		{
			_writer.writeCharacters( _indent );
		}
		_depth++;
		_simpleType = true;
	}

	/**
	 * Starts a new line with the proper amount of indenting, but does not
	 * change the nesting depth. This is used for empty tags.
	 *
	 * @throws  XMLStreamException if the whitespace can't be written to the
	 *          underlying stream.
	 */
	private void indentSame()
		throws XMLStreamException
	{
		_writer.writeCharacters( _newline );
		for (int i = 0; i < _depth; i++)
		{
			_writer.writeCharacters( _indent );
		}
		_simpleType = false;
	}

	/**
	 * Decreases the nesting depth, and starts a new line with the proper amount
	 * of indenting (except for a {@link #_simpleType}).
	 *
	 * @throws  XMLStreamException if the whitespace can't be written to the
	 *          underlying stream.
	 */
	private void indentOut()
		throws XMLStreamException
	{
		_depth--;
		if ( !_simpleType )
		{
			_writer.writeCharacters( _newline );
			for (int i = 0; i < _depth; i++)
			{
				_writer.writeCharacters( _indent );
			}
		}
		_simpleType = false;
	}

	@Override
	public void writeStartElement( final String localName )
		throws XMLStreamException
	{
		indentIn();
		_writer.writeStartElement( localName );
	}

	@Override
	public void writeStartElement( final String namespaceURI, final String localName )
		throws XMLStreamException
	{
		indentIn();
		_writer.writeStartElement( namespaceURI, localName );
	}

	@Override
	public void writeStartElement( final String prefix, final String localName, final String namespaceURI )
		throws XMLStreamException
	{
		indentIn();
		_writer.writeStartElement( prefix, localName, namespaceURI );
	}

	@Override
	public void writeEmptyElement( final String namespaceURI, final String localName )
		throws XMLStreamException
	{
		indentSame();
		_writer.writeEmptyElement( namespaceURI, localName );
	}

	@Override
	public void writeEmptyElement( final String prefix, final String localName, final String namespaceURI )
		throws XMLStreamException
	{
		indentSame();
		_writer.writeEmptyElement( prefix, localName, namespaceURI );
	}

	@Override
	public void writeEmptyElement( final String localName )
		throws XMLStreamException
	{
		indentSame();
		_writer.writeEmptyElement( localName );
	}

	@Override
	public void writeEndElement()
		throws XMLStreamException
	{
		indentOut();
		_writer.writeEndElement();
	}

	@Override
	public void writeEndDocument()
		throws XMLStreamException
	{
		_writer.writeEndDocument();
	}

	@Override
	public void close()
		throws XMLStreamException
	{
		_writer.close();
	}

	@Override
	public void flush()
		throws XMLStreamException
	{
		_writer.flush();
	}

	@Override
	public void writeAttribute( final String localName, final String value )
		throws XMLStreamException
	{
		_writer.writeAttribute( localName, value );
	}

	@Override
	public void writeAttribute( final String prefix, final String namespaceURI, final String localName, final String value )
		throws XMLStreamException
	{
		_writer.writeAttribute( prefix, namespaceURI, localName, value );
	}

	@Override
	public void writeAttribute( final String namespaceURI, final String localName, final String value )
		throws XMLStreamException
	{
		_writer.writeAttribute( namespaceURI, localName, value );
	}

	@Override
	public void writeNamespace( final String prefix, final String namespaceURI )
		throws XMLStreamException
	{
		_writer.writeNamespace( prefix, namespaceURI );
	}

	@Override
	public void writeDefaultNamespace( final String namespaceURI )
		throws XMLStreamException
	{
		_writer.writeDefaultNamespace( namespaceURI );
	}

	@Override
	public void writeComment( final String data )
		throws XMLStreamException
	{
		_writer.writeComment( data );
	}

	@Override
	public void writeProcessingInstruction( final String target )
		throws XMLStreamException
	{
		_writer.writeProcessingInstruction( target );
	}

	@Override
	public void writeProcessingInstruction( final String target, final String data )
		throws XMLStreamException
	{
		_writer.writeProcessingInstruction( target, data );
	}

	@Override
	public void writeCData( final String data )
		throws XMLStreamException
	{
		_writer.writeCData( data );
	}

	@Override
	public void writeDTD( final String dtd )
		throws XMLStreamException
	{
		_writer.writeDTD( dtd );
	}

	@Override
	public void writeEntityRef( final String name )
		throws XMLStreamException
	{
		_writer.writeEntityRef( name );
	}

	@Override
	public void writeStartDocument()
		throws XMLStreamException
	{
		_writer.writeStartDocument();
	}

	@Override
	public void writeStartDocument( final String version )
		throws XMLStreamException
	{
		_writer.writeStartDocument( version );
	}

	@Override
	public void writeStartDocument( final String encoding, final String version )
		throws XMLStreamException
	{
		_writer.writeStartDocument( encoding, version );
	}

	@Override
	public void writeCharacters( final String text )
		throws XMLStreamException
	{
		_writer.writeCharacters( text );
	}

	@Override
	public void writeCharacters( final char[] text, final int start, final int len )
		throws XMLStreamException
	{
		_writer.writeCharacters( text, start, len );
	}

	@Override
	public String getPrefix( final String uri )
		throws XMLStreamException
	{
		return _writer.getPrefix( uri );
	}

	@Override
	public void setPrefix( final String prefix, final String uri )
		throws XMLStreamException
	{
		_writer.setPrefix( prefix, uri );
	}

	@Override
	public void setDefaultNamespace( final String uri )
		throws XMLStreamException
	{
		_writer.setDefaultNamespace( uri );
	}

	@Override
	public void setNamespaceContext( final NamespaceContext context )
		throws XMLStreamException
	{
		_writer.setNamespaceContext( context );
	}

	@Override
	public NamespaceContext getNamespaceContext()
	{
		return _writer.getNamespaceContext();
	}

	@Override
	public Object getProperty( final String name )
		throws IllegalArgumentException
	{
		return _writer.getProperty( name );
	}
}
