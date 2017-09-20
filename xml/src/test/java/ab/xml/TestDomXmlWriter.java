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
import javax.xml.parsers.*;

import junit.framework.*;
import org.w3c.dom.*;

/**
 * Test for the {@link DomXmlWriter} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestDomXmlWriter
	extends TestCase
{
	/**
	 * Tests that the writer can reproduce an XML document that is read using
	 * the standard DOM parser.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testDocument1()
		throws Exception
	{
		System.out.println( "========================================================" );
		final Document readDocument = readDocument( "TestDomXmlWriter.xml" );
		writeAsText( System.out, "read:", readDocument );

		System.out.println( "========================================================" );
		final DomXmlWriter writer = new DomXmlWriter();
		writeNode( writer, readDocument );
		final Document writtenDocument = writer.getDocument();
		writeAsText( System.out, "written:", writtenDocument );

		System.out.println( "========================================================" );
		final StringBuilder readText = new StringBuilder();
		writeAsText( readText, "", readDocument );
		final StringBuilder writtenText = new StringBuilder();
		writeAsText( writtenText, "", writtenDocument );
		assertEquals( "DOM model should unchanged", readText.toString(), writtenText.toString() );
	}

	/**
	 * Read XML file and return it as a DOM document.
	 *
	 * @param   path    XML file path.
	 *
	 * @return  DOM document.
	 *
	 * @throws  Exception if the XML document could not be read properly.
	 */
	private static Document readDocument( final String path )
		throws Exception
	{
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware( true );
		final DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse( TestDomXmlWriter.class.getResourceAsStream( path ) );
	}

	/**
	 * Write DOM node to the specified XML writer.
	 *
	 * @param   writer      Writes the XML document.
	 * @param   node        DOM node to write.
	 *
	 * @throws  XMLException if there was a problem writing the XML document.
	 */
	private void writeNode( final XMLWriter writer, final Node node )
		throws XMLException
	{
		switch ( node.getNodeType() )
		{
			case Node.DOCUMENT_NODE:
				writeDocument( writer, (Document) node );

				break;

			case Node.ELEMENT_NODE:
				writeElement( writer, (Element) node );
				break;

			case Node.TEXT_NODE:
				writer.text( node.getNodeValue() );
				break;

			default:
				throw new AssertionError( "Unsupported " + node.getNodeName() );
		}
	}

	/**
	 * Write DOM document node to the specified XML writer.
	 *
	 * @param   writer      Writes the XML document.
	 * @param   document    DOM document node to write.
	 *
	 * @throws  XMLException if there was a problem writing the XML document.
	 */
	private void writeDocument( final XMLWriter writer, final Document document )
		throws XMLException
	{
		writer.startDocument();
		writeChildNodes( writer, document );
		writer.endDocument();
	}

	/**
	 * Write DOM element node to the specified XML writer.
	 *
	 * @param   writer      Writes the XML document.
	 * @param   element     DOM element node to write.
	 *
	 * @throws  XMLException if there was a problem writing the XML document.
	 */
	private void writeElement( final XMLWriter writer, final Element element )
		throws XMLException
	{
		final NamedNodeMap attributes = element.getAttributes();
		if ( attributes != null )
		{
			for ( int i = 0 ; i < attributes.getLength(); i++ )
			{
				final Node attribute = attributes.item( i );
				if ( "xmlns".equals( attribute.getPrefix() ) )
				{
					writer.setPrefix( attribute.getLocalName(), attribute.getNodeValue() );
				}
			}
		}

		final NodeList childNodes = element.getChildNodes();
		if ( ( ( childNodes == null ) || ( childNodes.getLength() == 0 ) ) )
		{
			writer.emptyTag( element.getNamespaceURI(), element.getLocalName() );
		}
		else
		{
			writer.startTag( element.getNamespaceURI(), element.getLocalName() );
		}

		if ( attributes != null )
		{
			for ( int i = 0 ; i < attributes.getLength(); i++ )
			{
				final Node attribute = attributes.item( i );
				if ( !"xmlns".equals( attribute.getPrefix() ) )
				{
					writer.attribute( attribute.getNamespaceURI(), attribute.getLocalName(), attribute.getNodeValue() );
				}
			}
		}

		writeChildNodes( writer, element );

		writer.endTag( element.getNamespaceURI(), element.getLocalName() );
	}

	/**
	 * Write child nodes of a DOM node to the specified XML writer.
	 *
	 * @param   writer      Writes the XML document.
	 * @param   node        DOM node whose child nodes to write.
	 *
	 * @throws  XMLException if there was a problem writing the XML document.
	 */
	private void writeChildNodes( final XMLWriter writer, final Node node )
		throws XMLException
	{
		final NodeList childNodes = node.getChildNodes();
		if ( childNodes != null )
		{
			for ( int i = 0; i < childNodes.getLength(); i++ )
			{
				writeNode( writer, childNodes.item( i ) );
			}
		}
	}

	/**
	 * Write node as text.
	 *
	 * @param   out         Character stream to write output to.
	 * @param   indent      Indent to use for output.
	 * @param   node        Node to write.
	 *
	 * @throws  IOException if an error occurs while accessing resources.
	 */
	private void writeAsText( final Appendable out, final String indent, final Node node )
		throws IOException
	{
		final boolean isTag = ( node.getLocalName() != null );

		out.append( indent );
		out.append( isTag ? "<" : "" );
		out.append( node.getNodeName() );

		final NamedNodeMap attributes = node.getAttributes();
		if ( attributes != null )
		{
			for ( int i = 0 ; i < attributes.getLength(); i++ )
			{
				final Node attribute = attributes.item( i );
				out.append( ' ' );
				out.append( attribute.getNodeName() );
				out.append( "=\"" );
				out.append( attribute.getNodeValue() );
				out.append( '"' );
			}
		}

		final String nodeValue = node.getNodeValue();
		if ( nodeValue != null )
		{
			final String escaped = nodeValue.replace( "\n", "\\n" );
			out.append( " '" );
			out.append( escaped );
			out.append( '\'' );
		}

		out.append( isTag ? ">\n" : "\n" );

		final NodeList childNodes = node.getChildNodes();
		if ( childNodes != null )
		{
			final String childIndent = indent + "    ";

			for ( int i = 0; i < childNodes.getLength(); i++ )
			{
				writeAsText( out, childIndent, childNodes.item( i ) );
			}
		}

		if ( isTag )
		{
			out.append( indent );
			out.append( "</" );
			out.append( node.getNodeName() );
			out.append( ">\n" );
		}
	}
}