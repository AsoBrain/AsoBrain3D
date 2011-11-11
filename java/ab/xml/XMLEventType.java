/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2010-2010 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.xml;

/**
 * Declares event type constants used in this API.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
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
	 * Indicates the end of an element. Occurs when an end tag or empty tag
	 * is parsed.
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
