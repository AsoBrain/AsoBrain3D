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
import java.util.*;
import java.util.regex.*;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

import static junit.framework.Assert.*;

/**
 * Tools for testing XML-related code.
 *
 * <h3>Processing instructions</h3>
 * <p>When comparing XML documents, it may not be possible (or practical) for
 * the two XML documents to be identical. For example if the documents include
 * a timestamp. To overcome this, reference documents may be annotated using
 * processing instructions.
 *
 * <p>The syntax for the processing instructions
 * supported by this class is:
 * <pre>&lt;?unit-test instruction [attribute="value" [...]] ?&gt;</pre>
 *
 * <p>For example:
 * <pre>&lt;?unit-test validate type="dateTime" ?&gt;</pre>
 *
 * <p>Available instructions and their attributes are listed in the table below.
 * The term 'content' refers to the text content at the current position in
 * the XML document. Attributes are required, unless specified otherwise.
 * <table>
 *   <tr><th>Instruction</th><th>Attribute</th><th>Description</th></tr>
 *   <tr><td>ignore</td><td></td><td>Ignores the content.</td></tr>
 *   <tr><td>validate</td><td></td><td>Ensures that the content is a valid lexical value for the specified XML Schema data type.</td></tr>
 *   <tr><td></td><td>type</td><td>XML Schema data type, e.g. <code>dateTime</code>.</td></tr>
 * </table>
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class XMLTestTools
{
	/**
	 * Asserts that the given stream contain an equivalent XML document.
	 *
	 * @param   expectedIn  Expected XML document.
	 * @param   actualIn    Actual XML document.
	 *
	 * @throws XMLStreamException if there is an error with the underlying XML.
	 */
	public static void assertXMLEquals( final InputStream expectedIn, final InputStream actualIn )
		throws XMLStreamException
	{
		final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
		xmlInputFactory.setProperty( XMLInputFactory.IS_COALESCING, Boolean.TRUE );

		final XMLEventReader actualReader = xmlInputFactory.createXMLEventReader( actualIn );
		final XMLEventReader expectedReader = xmlInputFactory.createXMLEventReader( expectedIn );

		final Pattern processingInstructionPattern = Pattern.compile( "([A-Za-z_][A-Z-a-z0-9_-]*)(?:\\s+([A-Za-z_][A-Z-a-z0-9_-]*)=\"([^\"]*)\")\\s*" );

		while ( expectedReader.hasNext() && actualReader.hasNext() )
		{
			final XMLEvent expectedEvent = expectedReader.nextEvent();
			final XMLEvent actualEvent = actualReader.nextEvent();

			if ( expectedEvent.getEventType() == XMLStreamConstants.PROCESSING_INSTRUCTION )
			{
				final ProcessingInstruction processingInstruction = (ProcessingInstruction)expectedEvent;
				if ( "unit-test".equals( processingInstruction.getTarget() ) )
				{
					final Matcher matcher = processingInstructionPattern.matcher( processingInstruction.getData() );
					if ( !matcher.matches() )
					{
						throw new AssertionError( "Invalid unit-test processing instruction: " + processingInstruction );
					}

					if ( "ignore".equals( matcher.group( 1 ) ) )
					{
						continue;
					}
					else if ( "validate".equals( matcher.group( 1 ) ) )
					{
						String type = null;

						for ( int i = 2; i < matcher.groupCount(); i += 2 )
						{
							final String attributeName = matcher.group( i );
							final String attributeValue = matcher.group( i + 1 );

							if ( "type".equals( attributeName ) )
							{
								type = attributeValue;
							}
						}

						if ( type == null )
						{
							throw new AssertionError( "Missing required attribute 'dataType' for unit-test processing instruction: " + processingInstruction );
						}

						assertEquals( "Unexpected event type.", XMLStreamConstants.CHARACTERS, actualEvent.getEventType() );
						try
						{
							if ( "dateTime".equals( type ) )
							{
								DatatypeConverter.parseDateTime( actualEvent.toString() );
							}
							else
							{
								throw new AssertionError( "Validation of data type '" + type + "' is not implemented." );
							}
						}
						catch ( Exception e )
						{
							throw (AssertionError)new AssertionError( "Invalid value for data type '" + type + "': " + actualEvent ).initCause( e );
						}

						continue;
					}
					else
					{
						throw new AssertionError( "Unsupported unit-test processing instruction: " + processingInstruction );
					}
				}
			}

			assertEquals( "Unexpected event type.", expectedEvent.getEventType(), actualEvent.getEventType() );
			assertEquals( "Unexpected event.", expectedEvent.toString(), actualEvent.toString() );
		}

		assertFalse( "Expected more events.", expectedReader.hasNext() );
		assertFalse( "Expected no more events.", actualReader.hasNext() );
	}

	/**
	 * Utility class should not be instantiated.
	 */
	private XMLTestTools()
	{
	}
}
