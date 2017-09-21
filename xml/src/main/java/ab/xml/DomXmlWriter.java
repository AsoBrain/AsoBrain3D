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

import java.util.*;
import javax.xml.*;
import javax.xml.parsers.*;

import org.jetbrains.annotations.*;
import org.w3c.dom.*;

/**
 * This {@link XMLWriter} builds a DOM model (fragment).
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class DomXmlWriter
	implements XMLWriter
{
	/**
	 * XML document.
	 */
	private Document _document;

	/**
	 * Current DOM node.
	 */
	private Node _node;

	/**
	 * <code>true</code> if the writer is currently writing an empty tag.
	 */
	private boolean _empty = false;

	/**
	 * Namespace declarations that need to be added to the next start element.
	 */
	private final LinkedList<Map<String,String>> _nsPrefixStack;

	/**
	 * Construct writer for a new document. Use {@link #startDocument()} to
	 * start the document.
	 */
	public DomXmlWriter()
	{
		this( null, null );
	}

	/**
	 * Construct writer for an existing document. The writer starts at the
	 * given node.
	 *
	 * @param   document    Document being written.
	 * @param   node        Node to start at.
	 */
	public DomXmlWriter( final Document document, final Node node )
	{
		_document = document;
		_node = node;

		final LinkedList<Map<String, String>> nsPrefixStack = new LinkedList<Map<String, String>>();
		nsPrefixStack.add( new LinkedHashMap<String, String>() );
		_nsPrefixStack = nsPrefixStack;
	}

	/**
	 * Get written document.
	 *
	 * @return  Document.
	 */
	public Document getDocument()
	{
		return _document;
	}

	public void startDocument()
		throws XMLException
	{
		if ( _document != null )
		{
			throw new XMLException( "Can't start document. We already have a document." );
		}

		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware( true );

		final DocumentBuilder db;
		try
		{
			db = dbf.newDocumentBuilder();
		}
		catch ( ParserConfigurationException e )
		{
			throw new XMLException( e );
		}

		final Document document = db.newDocument();
		_document = document;
		_node = document;
	}

	public void setPrefix( @NotNull final String prefix, @NotNull final String namespaceURI )
		throws XMLException
	{
		final Map<String, String> nsPrefixes = _nsPrefixStack.getLast();
		nsPrefixes.put( namespaceURI, prefix );
	}

	public void startTag( final String namespaceURI, @NotNull final String localName )
		throws XMLException
	{
		if ( _empty )
		{
			throw new XMLException( "Not allowed inside an empty tag. Use 'endTag' first." );
		}

		final Document document = _document;
		final Node node = _node;

		if ( ( document == null ) || ( node == null ) )
		{
			throw new XMLException( "Can't start tag before the document is started" );
		}

		final Element element;

		if ( namespaceURI != null )
		{
			element = document.createElementNS( namespaceURI, getQualifiedName( namespaceURI, localName ) );
		}
		else
		{
			element = document.createElementNS( XMLConstants.DEFAULT_NS_PREFIX, localName );
		}

		final Map<String, String> nsPrefixes = _nsPrefixStack.getLast();
		for ( final Map.Entry<String, String> entry : nsPrefixes.entrySet() )
		{
			final String namespaceUri = entry.getKey();
			final String prefix = entry.getValue();
			element.setAttributeNS( "http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, namespaceUri );
		}

		_nsPrefixStack.add( new LinkedHashMap<String, String>() );

		node.appendChild( element );
		_node = element;
	}

	public void emptyTag( final String namespaceURI, @NotNull final String localName )
		throws XMLException
	{
		startTag( namespaceURI, localName );
		_empty = true;
	}

	public void attribute( final String namespaceURI, @NotNull final String localName, @NotNull final String value )
		throws XMLException
	{
		final Node node = _node;

		if ( !( node instanceof Element ) )
		{
			throw new XMLException( "Must start tag before setting attributes" );
		}

		final Element element = (Element) node;

		if ( namespaceURI == null )
		{
			element.setAttributeNS( element.getNamespaceURI(), localName, value );
		}
		else
		{
			element.setAttributeNS( namespaceURI, getQualifiedName( namespaceURI, localName ), value );
		}
	}

	public void text( @NotNull final String characters )
		throws XMLException
	{
		if ( _empty )
		{
			throw new XMLException( "Not allowed inside an empty tag. Use 'endTag' first." );
		}

		final Document document = _document;
		final Node node = _node;

		if ( ( document == null ) || ( node == null ) )
		{
			throw new XMLException( "Can't start tag before the document is started" );
		}

		final Node lastChild = node.getLastChild();
		if ( lastChild instanceof Text )
		{
			final Text text = (Text) lastChild;
			text.appendData( characters );
		}
		else
		{
			node.appendChild( document.createTextNode( characters ) );
		}
	}

	public void endTag( final String namespaceURI, @NotNull final String localName )
		throws XMLException
	{
		_empty = false;

		final Node node = _node;

		final String nodeNamespaceURI = node.getNamespaceURI();
		final String nodeLocalName = node.getLocalName();

		if ( !localName.equals( nodeLocalName ) || ( ( namespaceURI == null ) ? ( nodeNamespaceURI != null ) : !namespaceURI.equals( nodeNamespaceURI ) ) )
		{
			throw new XMLException( "Unbalanced end tag (<" + getQualifiedName( nodeNamespaceURI, nodeLocalName ) + " xml:ns=\"" + nodeNamespaceURI + "\"> vs </" + getQualifiedName( namespaceURI, localName ) + " xml:ns=\"" + namespaceURI + "\">" );
		}

		_nsPrefixStack.removeLast();
		_node = node.getParentNode();
	}

	public void endDocument()
		throws XMLException
	{
		final Document document = _document;
		final Node node = _node;

		if ( ( document == null ) || ( node == null ) )
		{
			throw new XMLException( "Can't end document before the document is started" );
		}

		if ( _node != _document )
		{
			throw new XMLException( "Can't end document before the document is started" );
		}
	}

	public void flush()
		throws XMLException
	{
	}

	/**
	 * Get qualified name for a given name space and locale name.
	 *
	 * @param   namespaceURI    Namespace URI of node/attribute.
	 * @param   localName       Local name of the node/attribute.
	 *
	 * @return  Qualified name based on namespace URI and known prefixes;
	 *          <code>localName</code> if name could no be qualified.
	 */
	private String getQualifiedName( final String namespaceURI, final String localName )
	{
		String result = localName;

		for( final Iterator<Map<String, String>> it = _nsPrefixStack.descendingIterator(); it.hasNext(); )
		{
			final Map<String, String> nsPrefixes = it.next();
			final String prefix = nsPrefixes.get( namespaceURI );
			if ( prefix != null )
			{
				result = prefix + ':' + localName;
				break;
			}
		}

		return result;
	}
}