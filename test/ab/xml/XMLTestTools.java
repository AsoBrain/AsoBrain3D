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
import java.net.*;
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
		assertXMLEquals( null, expectedIn, actualIn );
	}

	/**
	 * Asserts that the given stream contain an equivalent XML document.
	 *
	 * @param   message     Assertion failure message.
	 * @param   expectedIn  Expected XML document.
	 * @param   actualIn    Actual XML document.
	 *
	 * @throws XMLStreamException if there is an error with the underlying XML.
	 */
	public static void assertXMLEquals( final String message, final InputStream expectedIn, final InputStream actualIn )
		throws XMLStreamException
	{
		final String messagePrefix = ( message != null ) && !message.isEmpty() ? message + " - " : message;

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

					final String instruction = matcher.group( 1 );

					final Map<String,String> instructionAttributes = new HashMap<String, String>();
					for ( int i = 2; i < matcher.groupCount(); i += 2 )
					{
						instructionAttributes.put( matcher.group( i ), matcher.group( i + 1 ) );
					}

					if ( "ignore".equals( instruction ) )
					{
						/* ignore :) */
					}
					else if ( "resource-url".equals( instruction ) )
					{
						processingResourceUrl( messagePrefix, actualEvent, instructionAttributes );
					}
					else if ( "validate".equals( instruction ) )
					{
						processValidate( messagePrefix, actualEvent, instructionAttributes );
					}
					else
					{
						throw new AssertionError( "Unsupported unit-test processing instruction: " + instruction );
					}

					continue;
				}
			}

			assertEquals( messagePrefix + "Unexpected event type.", expectedEvent.getEventType(), actualEvent.getEventType() );
			assertEquals( messagePrefix + "Unexpected event (type=" + expectedEvent.getEventType() + ").", expectedEvent.toString(), actualEvent.toString() );
		}

		assertFalse( messagePrefix + "Expected more events.", expectedReader.hasNext() );
		assertFalse( messagePrefix + "Expected no more events.", actualReader.hasNext() );
	}

	/**
	 * Asserts that the given stream contain an equivalent XML document.
	 *
	 * @param   message     Assertion failure message.
	 * @param   expected    Expected XML document.
	 * @param   actual      Actual XML document.
	 *
	 * @throws XMLStreamException if there is an error with the underlying XML.
	 */
	public static void assertXMLEquals( final String message, final String expected, final String actual )
		throws XMLStreamException
	{
		assertXMLEquals( message, new ByteArrayInputStream( expected.getBytes() ), new ByteArrayInputStream( actual.getBytes() ) );
	}

	/**
	 * Handle <code>&lt;?unit-test resource-url name="{name}"
	 * [className="{className}"] ?&gt;</code> processing instruction.
	 *
	 * This makes sure the XML element content is a reference to the specified
	 * resource. The class name is optional and may be used to specify which
	 * class (loader) is used to find the resource.
	 *
	 * @param   messagePrefix           Prefix for assertion failure messages.
	 * @param   event                   Current XML event being validated.
	 * @param   instructionAttributes   Processing instruction attributes.
	 */
	private static void processingResourceUrl( final String messagePrefix, final XMLEvent event, final Map<String, String> instructionAttributes )
	{
		String name = null;
		Class<?> clazz = XMLTestTools.class;

		for ( final Map.Entry<String, String> attribute : instructionAttributes.entrySet() )
		{
			final String attributeName = attribute.getKey();
			final String attributeValue = attribute.getValue();

			if ( "name".equals( attributeName ) )
			{
				name = attributeValue;
			}
			else if ( "className".equals( attributeName ) )
			{
				try
				{
					clazz = Class.forName( attributeValue );
				}
				catch ( ClassNotFoundException e )
				{
					throw new AssertionError( "Class '" + attributeValue + "' not found for '<?unit-test resource-url ?> processing instruction" );
				}
			}
			else
			{
				throw new AssertionError( "Unrecognized '" + attributeName + "' attribute for '<?unit-test resource-url ?> processing instruction" );
			}
		}

		if ( name == null )
		{
			throw new AssertionError( "Missing required 'name' attribute for '<?unit-test resource-url ?> processing instruction" );
		}

		final URL resourceUrl = clazz.getResource( name );
		assertNotNull( messagePrefix + "Can't find resource with name '" + name + "' for class '" + clazz.getName() + '\'', resourceUrl );

		assertEquals( messagePrefix + "Unexpected event type.", XMLStreamConstants.CHARACTERS, event.getEventType() );

		final Characters characters = event.asCharacters();
		final String actualUrl = characters.getData();

		assertEquals( messagePrefix + "Invalid resource URL for name '" + name + '\'', actualUrl, resourceUrl.toExternalForm() );
	}

	/**
	 * Handle <code>&lt;?unit-test validate type="{dataType}" ?&gt;</code>
	 * processing instruction.
	 *
	 * This ensures that the content is a valid lexical value for the specified
	 * XML Schema data type (e.g. <code>dateTime</code>).
	 *
	 * @param   messagePrefix           Prefix for assertion failure messages.
	 * @param   event                   Current XML event being validated.
	 * @param   instructionAttributes   Processing instruction attributes.
	 */
	private static void processValidate( final String messagePrefix, final XMLEvent event, final Map<String, String> instructionAttributes )
	{
		String type = null;

		for ( final Map.Entry<String, String> attribute : instructionAttributes.entrySet() )
		{
			final String attributeName = attribute.getKey();
			final String attributeValue = attribute.getValue();

			if ( "type".equals( attributeName ) )
			{
				type = attributeValue;
			}
			else
			{
				throw new AssertionError( "Unrecognized '" + attributeName + "' attribute for '<?unit-test validate ?> processing instruction" );
			}
		}

		if ( type == null )
		{
			throw new AssertionError( "Missing required attribute 'dataType' for '<?unit-test validate ?> processing instruction" );
		}

		assertEquals( messagePrefix + "Unexpected event type.", XMLStreamConstants.CHARACTERS, event.getEventType() );
		try
		{
			if ( "dateTime".equals( type ) )
			{
				final Characters characters = event.asCharacters();
				DatatypeConverter.parseDateTime( characters.getData() );
			}
			else
			{
				throw new AssertionError( "Validation of data type '" + type + "' is not implemented." );
			}
		}
		catch ( Exception e )
		{
			final AssertionError error = new AssertionError( "Invalid value for data type '" + type + "': " + event );
			error.initCause( e );
			throw error;
		}
	}

	/**
	 * Utility class should not be instantiated.
	 */
	private XMLTestTools()
	{
	}
}