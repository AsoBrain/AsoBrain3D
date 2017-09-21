/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

/**
 * Declares event type constants used in this API.
 *
 * @author G. Meinders
 */
public enum XMLEventType
{
	/**
	 * Indicates the start of the document. This is always the first event.
	 */
	START_DOCUMENT,

	/**
	 * Indicates the end of the document. This is always the last event.
	 */
	END_DOCUMENT,

	/**
	 * Indicates the start of an element. Occurs when a start tag or empty tag
	 * is parsed.
	 */
	START_ELEMENT,

	/**
	 * Indicates the end of an element. Occurs when an end tag or empty tag is
	 * parsed.
	 */
	END_ELEMENT,

	/**
	 * Indicates a character data event. This includes any whitespace, cdata
	 * sections and resolved entity references.
	 */
	CHARACTERS,

	/**
	 * Indicates a processing instruction. This does not include the XML
	 * declaration (which is not a processing instruction, though it does share
	 * similar syntax).
	 */
	PROCESSING_INSTRUCTION,

	/**
	 * Indicates a document type declaration.
	 */
	DTD,
}
