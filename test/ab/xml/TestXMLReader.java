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

import junit.framework.*;
import org.jetbrains.annotations.*;

/**
 * Provides common unit tests for {@link XMLReader} classes.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public abstract class TestXMLReader
	extends TestCase
{
	/**
	 * Factory that creates the <code>XMLReader</code>s to be tested.
	 */
	protected XMLReaderFactory _factory;

	/**
	 * Tests that the reader returns all the appropriate events for the
	 * input document, <code>TestXMLReader-1.xml</code>. The document for this
	 * test should be small, but should still require all interface methods to
	 * be read.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testDocument1()
		throws Exception
	{
		System.out.println( getClass().getName() + ".testDocument1()" );
		final XMLReader reader = _factory.createXMLReader( TestXMLReader.class.getResourceAsStream( "TestXMLReader-1.xml" ), null );

		assertEquals( "Unexpected event type.", XMLEventType.PROCESSING_INSTRUCTION, reader.next() );
		assertEquals( "Unexpected processing instruction target.", "magic", reader.getPITarget() );
		assertEquals( "Unexpected processing instruction data.", "processing instruction 1", reader.getPIData() );

		assertEquals( "Unexpected event type.", XMLEventType.PROCESSING_INSTRUCTION, reader.next() );
		assertEquals( "Unexpected processing instruction target.", "magic", reader.getPITarget() );
		assertEquals( "Unexpected processing instruction data.", "", reader.getPIData() );

		assertEquals( "Unexpected event type.", XMLEventType.DTD, reader.next() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", null, reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "example", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/ns1", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element1", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 2, reader.getAttributeCount() );
		assertEquals( "Unexpected namespace URI for attribute 0.", "http://www.example.com/ns1", reader.getAttributeNamespaceURI( 0 ) );
		assertEquals( "Unexpected local name for attribute 0.", "attribute1", reader.getAttributeLocalName( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "value1", reader.getAttributeValue( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "value1", reader.getAttributeValue( "http://www.example.com/ns1", "attribute1" ) );
		assertEquals( "Unexpected namespace URI for attribute 1.", null, reader.getAttributeNamespaceURI( 1 ) );
		assertEquals( "Unexpected local name for attribute 1.", "attribute2", reader.getAttributeLocalName( 1 ) );
		assertEquals( "Unexpected value for attribute 1.", "value2", reader.getAttributeValue( 1 ) );
		assertEquals( "Unexpected value for attribute 1.", "value2", reader.getAttributeValue( null, "attribute2" ) );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/default", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element2", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 1, reader.getAttributeCount() );
		assertEquals( "Unexpected namespace URI for attribute 0.", null, reader.getAttributeNamespaceURI( 0 ) );
		assertEquals( "Unexpected local name for attribute 0.", "attribute3", reader.getAttributeLocalName( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "value3", reader.getAttributeValue( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "value3", reader.getAttributeValue( null, "attribute3" ) );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/default", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element2", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/ns1", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element3", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 2, reader.getAttributeCount() );
		assertEquals( "Unexpected namespace URI for attribute 0.", "http://www.example.com/ns2", reader.getAttributeNamespaceURI( 0 ) );
		assertEquals( "Unexpected local name for attribute 0.", "attribute4", reader.getAttributeLocalName( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "value4", reader.getAttributeValue( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "value4", reader.getAttributeValue( "http://www.example.com/ns2", "attribute4" ) );
		assertEquals( "Unexpected namespace URI for attribute 1.", null, reader.getAttributeNamespaceURI( 1 ) );
		assertEquals( "Unexpected local name for attribute 1.", "attribute5", reader.getAttributeLocalName( 1 ) );
		assertEquals( "Unexpected value for attribute 1.", "ns2:value5", reader.getAttributeValue( 1 ) );
		assertEquals( "Unexpected value for attribute 1.", "ns2:value5", reader.getAttributeValue( null, "attribute5" ) );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "world", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.PROCESSING_INSTRUCTION, reader.next() );
		assertEquals( "Unexpected processing instruction target.", "magic", reader.getPITarget() );
		assertEquals( "Unexpected processing instruction data.", "processing instruction 2  ", reader.getPIData() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/ns1", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element3", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/ns2", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element4", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/ns2", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element4", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/ns1", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element1", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		final StringBuilder builder = new StringBuilder();
		final XMLEventType next = coalesceCharacters( reader, builder );
		assertEquals( "Unexpected character data.", "<entity references>", builder.toString().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, next );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/ns2", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element5", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 2, reader.getAttributeCount() );
		assertEquals( "Unexpected namespace URI for attribute 0.", null, reader.getAttributeNamespaceURI( 0 ) );
		assertEquals( "Unexpected local name for attribute 0.", "attribute6", reader.getAttributeLocalName( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "value6", reader.getAttributeValue( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "value6", reader.getAttributeValue( null, "attribute6" ) );
		assertEquals( "Unexpected namespace URI for attribute 1.", "http://www.example.com/ns2", reader.getAttributeNamespaceURI( 1 ) );
		assertEquals( "Unexpected local name for attribute 1.", "attribute7", reader.getAttributeLocalName( 1 ) );
		assertEquals( "Unexpected value for attribute 1.", "value7", reader.getAttributeValue( 1 ) );
		assertEquals( "Unexpected value for attribute 1.", "value7", reader.getAttributeValue( "http://www.example.com/ns2", "attribute7" ) );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.example.com/ns2", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "element5", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", null, reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "example", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.END_DOCUMENT, reader.next() );
	}

	/**
	 * Tests that the reader returns all the appropriate events for the
	 * input document, <code>TestXMLReader-2.xml</code>. This test focuses on
	 * coalescing of character events using a simple XHTML document with
	 * 5 paragraphs of "Lorem Ipsum".
	 *
	 * @throws  XMLException if the test fails.
	 */
	public void testDocument2()
		throws XMLException
	{
		System.out.println( getClass().getName() + ".testDocument2()" );
		final XMLReader reader = _factory.createXMLReader( TestStaxReader.class.getResourceAsStream( "TestXMLReader-2.xml" ), null );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "html", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 1, reader.getAttributeCount() );
		assertEquals( "Unexpected namespace URI for attribute 0.", "http://www.w3.org/XML/1998/namespace", reader.getAttributeNamespaceURI( 0 ) );
		assertEquals( "Unexpected local name for attribute 0.", "lang", reader.getAttributeLocalName( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "la", reader.getAttributeValue( 0 ) );
		assertEquals( "Unexpected value for attribute 0.", "la", reader.getAttributeValue( "http://www.w3.org/XML/1998/namespace", "lang" ) );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "head", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "title", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "Lorem Ipsum", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "title", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "head", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "body", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam\n" +
				"\t\t\tvehicula fermentum nibh, at placerat tellus hendrerit at. Quisque ac\n" +
				"\t\t\tarcu vel libero suscipit posuere. Sed mauris est, tristique egestas\n" +
				"\t\t\tmattis a, convallis et massa. Aliquam non sapien in augue dapibus\n" +
				"\t\t\tcursus. Cum sociis natoque penatibus et magnis dis parturient\n" +
				"\t\t\tmontes, nascetur ridiculus mus. Fusce in aliquet eros. Sed vitae\n" +
				"\t\t\ttortor eu neque varius accumsan vitae rhoncus est. Praesent in nisl\n" +
				"\t\t\turna. Curabitur rhoncus lacinia tellus, eu pretium nulla vulputate\n" +
				"\t\t\tsit amet. Maecenas laoreet, sem fringilla laoreet consequat, nibh\n" +
				"\t\t\tsapien fringilla odio, sed hendrerit magna massa vel urna. Cras\n" +
				"\t\t\ttempor, turpis in tempus ullamcorper, velit nisl eleifend lorem, sit\n" +
				"\t\t\tamet ultricies erat tellus nec purus. Mauris consectetur sodales\n" +
				"\t\t\tnunc, vitae varius augue blandit ut. Ut congue, sapien non\n" +
				"\t\t\tsollicitudin adipiscing, nisi nisl ultricies lectus, quis fermentum\n" +
				"\t\t\torci tellus vel tortor.", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "Pellentesque id metus lorem, vel suscipit ipsum. Curabitur\n" +
				"\t\t\tscelerisque consequat dictum. Pellentesque habitant morbi tristique\n" +
				"\t\t\tsenectus et netus et malesuada fames ac turpis egestas. Ut vulputate\n" +
				"\t\t\tneque et sapien auctor semper accumsan dui aliquet. Cum sociis\n" +
				"\t\t\tnatoque penatibus et magnis dis parturient montes, nascetur\n" +
				"\t\t\tridiculus mus. Vivamus aliquet odio sed sem rutrum posuere. Maecenas\n" +
				"\t\t\tsuscipit tortor lorem, eget porta erat. Donec ac nibh a massa\n" +
				"\t\t\tdapibus tempus sed eu lorem. Maecenas et est eros. Nunc feugiat dui\n" +
				"\t\t\tfringilla purus gravida egestas. Pellentesque habitant morbi\n" +
				"\t\t\ttristique senectus et netus et malesuada fames ac turpis egestas.", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "Pellentesque habitant morbi tristique senectus et netus et malesuada\n" +
				"\t\t\tfames ac turpis egestas. Donec fermentum justo ut dolor dictum\n" +
				"\t\t\ttincidunt aliquam mauris egestas. Donec eget eros augue, sed tempus\n" +
				"\t\t\tdolor. Integer elit est, porttitor id rutrum sed, adipiscing nec\n" +
				"\t\t\tnisi. Vivamus turpis lectus, egestas sed faucibus eu, ullamcorper\n" +
				"\t\t\teget dolor. Sed vitae sem ac sapien pulvinar dapibus. Vestibulum\n" +
				"\t\t\tlobortis, ligula vel condimentum rutrum, nunc nisl sollicitudin\n" +
				"\t\t\tmetus, a cursus turpis orci et mauris. Nunc quis mauris elit, quis\n" +
				"\t\t\tsodales justo.", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "Morbi eu velit eu sem viverra laoreet. Phasellus luctus lacinia\n" +
				"\t\t\tmauris in ultrices. Integer at eros ac arcu malesuada sodales ac nec\n" +
				"\t\t\tenim. Vivamus at arcu ut massa volutpat ultrices. Praesent rhoncus\n" +
				"\t\t\trutrum lorem ut placerat. Nam imperdiet sapien ac sem porta\n" +
				"\t\t\tvolutpat. Nam lobortis eleifend lectus vel lacinia. Ut massa sapien,\n" +
				"\t\t\tsagittis ac ornare non, lobortis at dui.", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "Integer ut elit a urna interdum facilisis. Quisque in odio at lacus\n" +
				"\t\t\tsodales porta. Mauris aliquam sodales tellus at scelerisque. Nulla\n" +
				"\t\t\tfacilisi. Nunc cursus feugiat turpis, vitae faucibus urna\n" +
				"\t\t\tpellentesque id. Suspendisse volutpat tortor sit amet dui adipiscing\n" +
				"\t\t\tnec hendrerit mi tempor. Maecenas a libero et orci varius pharetra\n" +
				"\t\t\ttincidunt at magna. Nullam a massa ipsum. Quisque placerat, sapien\n" +
				"\t\t\tvitae mattis ornare, neque mi luctus lacus, sed tincidunt leo erat\n" +
				"\t\t\teget dui. Sed gravida mi tellus, sed consequat mi. Curabitur\n" +
				"\t\t\tsollicitudin placerat arcu ultrices scelerisque. Maecenas non arcu\n" +
				"\t\t\tvitae lorem dictum aliquam. Donec vitae est et orci posuere\n" +
				"\t\t\tscelerisque.", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "p", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "body", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		assertEquals( "Unexpected character data.", "", reader.getText().trim() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", "http://www.w3.org/1999/xhtml", reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "html", reader.getLocalName() );

		assertEquals( "Unexpected event type.", XMLEventType.END_DOCUMENT, reader.next() );
	}

	/**
	 * Tests exception handling as specified by the {@link XMLReader} interface.
	 * This includes calling methods for event types that are not allowed and
	 * illegal arguments provided to methods. Exceptions relating to the
	 * underlying XML API and I/O (represented by {@link XMLException}) are not
	 * tested.
	 *
	 * @throws  XMLException if the test fails.
	 */
	public void testExceptionHandling()
		throws XMLException
	{
		System.out.println( getClass().getName() + ".testExceptionHandling()" );

		final XMLReader reader = createReaderForContent( "<?xml version='1.0'?><!DOCTYPE root><root>characters<?pi?></root>" );

		assertEquals( "Unexpected event type.", XMLEventType.START_DOCUMENT, reader.getEventType() );
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getNamespaceURI() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getLocalName() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeCount() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeNamespaceURI( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeLocalName( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( null, "attribute" ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getText() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPITarget() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPIData() ); } catch ( IllegalStateException e ) { /* Success! */ }

		assertEquals( "Unexpected event type.", XMLEventType.DTD, reader.next() );
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getNamespaceURI() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getLocalName() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeCount() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeNamespaceURI( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeLocalName( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( null, "attribute" ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getText() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPITarget() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPIData() ); } catch ( IllegalStateException e ) { /* Success! */ }

		assertEquals( "Unexpected event type.", XMLEventType.START_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", null, reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "root", reader.getLocalName() );
		assertEquals( "Unexpected attribute count.", 0, reader.getAttributeCount() );
		try { throw new AssertionError( "Expected IndexOutOfBoundsException, but return value was: " + reader.getAttributeNamespaceURI( 0 ) ); } catch ( IndexOutOfBoundsException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IndexOutOfBoundsException, but return value was: " + reader.getAttributeLocalName( 0 ) ); } catch ( IndexOutOfBoundsException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IndexOutOfBoundsException, but return value was: " + reader.getAttributeValue( 0 ) ); } catch ( IndexOutOfBoundsException e ) { /* Success! */ }
		assertNull( "Value of non-existent attribute should be null.", reader.getAttributeValue( null, "attribute" ) );
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getText() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPITarget() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPIData() ); } catch ( IllegalStateException e ) { /* Success! */ }

		assertEquals( "Unexpected event type.", XMLEventType.CHARACTERS, reader.next() );
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getNamespaceURI() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getLocalName() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeCount() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeNamespaceURI( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeLocalName( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( null, "attribute" ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		assertEquals( "Unexpected character data.", "characters", reader.getText() );
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPITarget() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPIData() ); } catch ( IllegalStateException e ) { /* Success! */ }

		assertEquals( "Unexpected event type.", XMLEventType.PROCESSING_INSTRUCTION, reader.next() );
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getNamespaceURI() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getLocalName() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeCount() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeNamespaceURI( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeLocalName( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( null, "attribute" ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getText() ); } catch ( IllegalStateException e ) { /* Success! */ }
		assertEquals( "Unexpected processing instruction target.", "pi", reader.getPITarget() );
		assertEquals( "Unexpected processing instruction data.", "", reader.getPIData() );

		assertEquals( "Unexpected event type.", XMLEventType.END_ELEMENT, reader.next() );
		assertEquals( "Unexpected namespace URI.", null, reader.getNamespaceURI() );
		assertEquals( "Unexpected local name.", "root", reader.getLocalName() );
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeCount() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeNamespaceURI( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeLocalName( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( null, "attribute" ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getText() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPITarget() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPIData() ); } catch ( IllegalStateException e ) { /* Success! */ }

		assertEquals( "Unexpected event type.", XMLEventType.END_DOCUMENT, reader.next() );
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.next() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getNamespaceURI() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getLocalName() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeCount() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeNamespaceURI( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeLocalName( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( 0 ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getAttributeValue( null, "attribute" ) ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getText() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPITarget() ); } catch ( IllegalStateException e ) { /* Success! */ }
		try { throw new AssertionError( "Expected IllegalStateException, but return value was: " + reader.getPIData() ); } catch ( IllegalStateException e ) { /* Success! */ }
	}

	/**
	 * Creates a reader to read the given XML document. This method always uses
	 * UTF-8 character encoding.
	 *
	 * @param   document   XML document.
	 *
	 * @return  XML reader.
	 *
	 * @throws  XMLException if an error occurs.
	 */
	protected XMLReader createReaderForContent( final String document )
		throws XMLException
	{
		return createReaderForContent( document, "UTF-8" );
	}

	/**
	 * Creates a reader to read the given XML document.
	 *
	 * @param   document   XML document.
	 * @param   encoding   Character encoding.
	 *
	 * @return  XML reader.
	 *
	 * @throws  XMLException if an error occurs.
	 */
	protected XMLReader createReaderForContent( final String document, final String encoding )
		throws XMLException
	{
		final ByteArrayInputStream in;
		try
		{
			in = new ByteArrayInputStream( document.getBytes( encoding ) );
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new AssertionError( e );
		}
		return _factory.createXMLReader( in, encoding );
	}

	/**
	 * Generates test code for a reference document read by the given reader.
	 *
	 * @param   reader  Reader to be used.
	 *
	 * @throws  XMLException if an XML-related error occurs.
	 */
	protected static void generateTestCode( final XMLReader reader )
		throws XMLException
	{
		XMLEventType eventType;
		do
		{
			eventType = reader.next();
			System.out.print( "assertEquals( \"Unexpected event type.\", XMLEventType." );
			System.out.print( eventType.name() );
			System.out.println( ", reader.next() );" );

			switch ( eventType )
			{
				case START_ELEMENT:
					System.out.print( "assertEquals( \"Unexpected namespace URI.\", " );
					printStringLiteral( reader.getNamespaceURI() );
					System.out.println( ", reader.getNamespaceURI() );" );

					System.out.print( "assertEquals( \"Unexpected local name.\", \"" );
					System.out.print( reader.getLocalName() );
					System.out.println( "\", reader.getLocalName() );" );

					System.out.print( "assertEquals( \"Unexpected attribute count.\", " );
					System.out.print( reader.getAttributeCount() );
					System.out.println( ", reader.getAttributeCount() );" );

					for ( int i = 0; i < reader.getAttributeCount(); i++ )
					{
						System.out.print( "assertEquals( \"Unexpected namespace URI for attribute " );
						System.out.print( i );
						System.out.print( ".\", " );
						printStringLiteral( reader.getAttributeNamespaceURI( i ) );
						System.out.print( ", reader.getAttributeNamespaceURI( " );
						System.out.print( i );
						System.out.println( " ) );" );

						System.out.print( "assertEquals( \"Unexpected local name for attribute " );
						System.out.print( i );
						System.out.print( ".\", \"" );
						System.out.print( reader.getAttributeLocalName( i ) );
						System.out.print( "\", reader.getAttributeLocalName( " );
						System.out.print( i );
						System.out.println( " ) );" );

						System.out.print( "assertEquals( \"Unexpected value for attribute " );
						System.out.print( i );
						System.out.print( ".\", \"" );
						System.out.print( reader.getAttributeValue( i ) );
						System.out.print( "\", reader.getAttributeValue( " );
						System.out.print( i );
						System.out.println( " ) );" );

						System.out.print( "assertEquals( \"Unexpected value for attribute " );
						System.out.print( i );
						System.out.print( ".\", \"" );
						System.out.print( reader.getAttributeValue( i ) );
						System.out.print( "\", reader.getAttributeValue( " );
						printStringLiteral( reader.getAttributeNamespaceURI( i ) );
						System.out.print( ", \"" );
						System.out.print( reader.getAttributeLocalName( i ) );
						System.out.println( "\" ) );" );
					}
					break;

				case END_ELEMENT:
					System.out.print( "assertEquals( \"Unexpected namespace URI.\", " );
					printStringLiteral( reader.getNamespaceURI() );
					System.out.println( ", reader.getNamespaceURI() );" );

					System.out.print( "assertEquals( \"Unexpected local name.\", \"" );
					System.out.print( reader.getLocalName() );
					System.out.println( "\", reader.getLocalName() );" );
					break;

				case CHARACTERS:
					System.out.print( "assertEquals( \"Unexpected character data.\", \"" );
					System.out.print( reader.getText().trim().replaceAll( "\t", "\\\\t" ).replaceAll( "\\n", "\\\\n\" + \n\t\t\"" ) );
					System.out.println( "\", reader.getText().trim() );" );
					break;

				case PROCESSING_INSTRUCTION:
					System.out.print( "assertEquals( \"Unexpected processing instruction target.\", \"" );
					System.out.print( reader.getPITarget() );
					System.out.println( "\", reader.getPITarget() );" );

					System.out.print( "assertEquals( \"Unexpected processing instruction data.\", \"" );
					System.out.print( reader.getPIData() );
					System.out.println( "\", reader.getPIData() );" );
					break;

				case DTD:
					break;
			}

			System.out.println();
		}
		while ( eventType != XMLEventType.END_DOCUMENT );
	}

	/**
	 * Prints the given string as a string literal. Does not escape characters
	 * that are not allowed in string literals.
	 *
	 * @param   s   String to be printed.
	 */
	private static void printStringLiteral( @Nullable final String s )
	{
		if ( s != null )
		{
			System.out.print( '"' );
		}
		System.out.print( s );
		if ( s != null )
		{
			System.out.print( '"' );
		}
	}

	/**
	 * Returns the next event from the given reader, optionally skipping one
	 * character event that consists only of whitespace.
	 *
	 * <p>Example:
	 * <pre>
	 * XMLReader reader;
	 * XMLEventType nonWhitespace = ignoreWhiteSpace( reader );
	 * </pre>
	 *
	 * @param   reader  Reader to be used.
	 *
	 * @return  Current event type.
	 *
	 * @throws  XMLException if the next event can't be read.
	 */
	protected XMLEventType ignoreWhiteSpace( final XMLReader reader )
		throws XMLException
	{
		XMLEventType result = reader.next();
		if ( ( result == XMLEventType.CHARACTERS ) && ( reader.getText().trim().isEmpty() ) )
		{
			result = reader.next();
		}
		return result;
	}

	/**
	 * Appends the character data for the current event and any character data
	 * events that follow it, then returns the event type of the event that
	 * follows the character data. This allows for characters events to be
	 * coalesced even if the reader output separate events (e.g. due to the
	 * presence of entity references).
	 *
	 * <p>Example:
	 * <pre>
	 * XMLReader reader;
	 * XMLEventType nonWhitespace = ignoreWhiteSpace( reader );
	 * </pre>
	 *
	 * @param   reader  Reader to be used.
	 * @param   result  Character sequence to append the characters to.
	 *
	 * @return  Current event type.
	 *
	 * @throws  XMLException if the next event can't be read.
	 * @throws  IOException if an I/O error occurs.
	 */
	protected XMLEventType coalesceCharacters( final XMLReader reader, final Appendable result )
		throws XMLException, IOException
	{
		XMLEventType eventType;
		do
		{
			result.append( reader.getText() );
			eventType = reader.next();
		}
		while ( eventType == XMLEventType.CHARACTERS );
		return eventType;
	}
}
