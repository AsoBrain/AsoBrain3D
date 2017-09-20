/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2015 Peter S. Heijnen
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
 */

package ab.xml;

import org.jetbrains.annotations.*;

/**
 * Provides forward read-only access to XML documents.
 *
 * <p>Comments and declarations are ignored. All character data is treated
 * equally. Processing instructions are supported.
 *
 * @author G. Meinders
 */
public interface XMLReader
{
	/**
	 * Returns the event type for the current event. This is either the last
	 * value returned by {@link #next()}, or {@link XMLEventType#START_DOCUMENT}
	 * if {@code next()} has not been called yet.
	 *
	 * @return Current event type.
	 */
	@NotNull
	XMLEventType getEventType();

	/**
	 * Proceeds to the next parsing event and returns its event type.
	 *
	 * @return Event type.
	 *
	 * @throws XMLException if a parse error occurs.
	 * @throws IllegalStateException if called after a previous call returned
	 * {@link XMLEventType#END_DOCUMENT}.
	 */
	@NotNull
	XMLEventType next()
	throws XMLException;

	/**
	 * Returns the URI of the namespace of the current element. This is either
	 * the namespace bound to the element's prefix, the default namespace (if
	 * the element has no prefix) or {@code null} (if the element has no prefix
	 * and no default namespace is defined).
	 *
	 * @return Namespace URI, or {@code null} if not defined.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#START_ELEMENT} or {@link XMLEventType#END_ELEMENT}.
	 */
	@Nullable
	String getNamespaceURI();

	/**
	 * Returns the local name of the current element.
	 *
	 * @return Local name.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#START_ELEMENT} or {@link XMLEventType#END_ELEMENT}.
	 */
	@NotNull
	String getLocalName();

	/**
	 * Returns the number of attributes for the current element. This excludes
	 * any {@code xmlns} or {@code xmlns:prefix} attributes.
	 *
	 * @return Number of attributes.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#START_ELEMENT}.
	 */
	int getAttributeCount();

	/**
	 * Returns the namespace URI of the specified attribute. If the attribute
	 * has no prefix, this method will return {@code null}.
	 *
	 * @param index Attribute index.
	 *
	 * @return Namespace URI, or {@code null} if not specified.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#START_ELEMENT}.
	 * @throws IndexOutOfBoundsException if the current event is a {@link
	 * XMLEventType#START_ELEMENT} and {@code index < 0} or {@code index &gt;=
	 * getAttributeCount()}.
	 */
	@Nullable
	String getAttributeNamespaceURI( int index );

	/**
	 * Returns the local name of the specified attribute.
	 *
	 * @param index Attribute index.
	 *
	 * @return Local name.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#START_ELEMENT}.
	 * @throws IndexOutOfBoundsException if the current event is a {@link
	 * XMLEventType#START_ELEMENT} and {@code index < 0} or {@code index &gt;=
	 * getAttributeCount()}.
	 */
	@NotNull
	String getAttributeLocalName( int index );

	/**
	 * Returns the value of the specified attribute.
	 *
	 * @param index Attribute index.
	 *
	 * @return Attribute value.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#START_ELEMENT}.
	 * @throws IndexOutOfBoundsException if the current event is a {@link
	 * XMLEventType#START_ELEMENT} and {@code index < 0} or {@code index &gt;=
	 * getAttributeCount()}.
	 */
	@NotNull
	String getAttributeValue( int index );

	/**
	 * Returns the value of the specified attribute.
	 *
	 * @param localName Local name.
	 *
	 * @return Attribute value; {@code null} if not found.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#START_ELEMENT}.
	 */
	@Nullable
	String getAttributeValue( @NotNull String localName );

	/**
	 * Returns the value of the specified attribute.
	 *
	 * @param namespaceURI Namespace URI, or {@code null} for an attribute with
	 *                     no prefix.
	 * @param localName    Local name.
	 *
	 * @return Attribute value; {@code null} if not found.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#START_ELEMENT}.
	 */
	@Nullable
	String getAttributeValue( @Nullable String namespaceURI, @NotNull String localName );

	/**
	 * Returns the character data for the current event.
	 *
	 * @return Character data.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#CHARACTERS}.
	 */
	@NotNull
	String getText();

	/**
	 * Returns the processing instruction target for the current event.
	 *
	 * @return Processing instruction target.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#PROCESSING_INSTRUCTION}.
	 */
	@NotNull
	String getPITarget();

	/**
	 * Returns the processing instruction data for the current event.
	 *
	 * @return Processing instruction data.
	 *
	 * @throws IllegalStateException if the current event is not {@link
	 * XMLEventType#PROCESSING_INSTRUCTION}.
	 */
	@NotNull
	String getPIData();
}
