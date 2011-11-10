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

/**
 * Writer for XML documents.
 *
 * <p>Differences with StAX, the Streaming API for XML:
 * <ul>
 *   <li>Empty tags must be closed with {@link #endTag}.</li>
 *   <li>Elements that are empty may automatically be written as empty tags,
 *       even when started with {@link #startTag}.</li>
 *   <li>Binding a namespace prefix using {@link #setPrefix} applies to the
 *       next element that is written, not the current element.</li>
 *   <li>Namespace declarations are automatically written after binding a
 *       namespace prefix using {@link #setPrefix}.</li>
 * </ul>
 *
 * <p>Differences with XML Pull:
 * <ul>
 *   <li>To ensure that an empty tag is written, use {@link #emptyTag} instead
 *       of {@link #startTag}. The latter may result in a separate end tag.</li>
 * </ul>
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface XMLWriter
{
	/**
	 * Binds a namespace to the given prefix. The namespace prefix is
	 * automatically declared as part of the next {@link #startTag}.
	 *
	 * @param   prefix          Prefix.
	 * @param   namespaceURI    Namespace URI to be bound.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	void setPrefix( @NotNull String prefix, @NotNull String namespaceURI )
		throws XMLException;

	/**
	 * Writes the XML declaration at the start of the document.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	void startDocument()
		throws XMLException;

	/**
	 * Writes a start tag. A start tag may automatically be changed into an
	 * empty tag if nothing is written inside the tag. To ensure an empty tag
	 * is written, use {@link #emptyTag}.
	 *
	 * @param   namespaceURI    Namespace URI.
	 * @param   localName       Local name of the element.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	void startTag( @Nullable String namespaceURI, @NotNull String localName )
		throws XMLException;

	/**
	 * Writes an empty tag. An empty tag must be closed using {@link #endTag},
	 * just like a regular {@link #startTag}.
	 *
	 * @param   namespaceURI    Namespace URI.
	 * @param   localName       Local name of the element.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	void emptyTag( @Nullable String namespaceURI, @NotNull String localName )
		throws XMLException;

	/**
	 * Writes an attribute.
	 *
	 * @param   namespaceURI    Namespace URI.
	 * @param   localName       Locale name of the attribute.
	 * @param   value           Value of the attribute.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	void attribute( @Nullable String namespaceURI, @NotNull String localName, @NotNull String value )
		throws XMLException;

	/**
	 * Writes character data.
	 *
	 * @param   characters  Character data to be written.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	void text( @NotNull String characters )
		throws XMLException;

	/**
	 * Writes an end tag.
	 *
	 * @param   namespaceURI    Namespace URI.
	 * @param   localName       Local name of the element.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	void endTag( @Nullable String namespaceURI, @NotNull String localName )
		throws XMLException;

	/**
	 * Writes the end of the document, closing any unclosed start tags.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	void endDocument()
		throws XMLException;

	/**
	 * Writes any cached data to the underlying output mechanism.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	void flush()
		throws XMLException;
}
