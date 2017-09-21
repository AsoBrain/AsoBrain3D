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

import org.xmlpull.v1.*;

/**
 * Wraps an underlying {@link XmlSerializer} to automatically add
 * newlines and indenting for elements. Elements that do no contain nested
 * elements are themselves indented, but have no newlines and indenting added
 * around their content.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class IndentingXmlSerializer
	implements XmlSerializer
{
	/**
	 * Underlying writer.
	 */
	private final XmlSerializer _writer;

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
	public IndentingXmlSerializer( final XmlSerializer writer )
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
	 * @throws  IOException if an I/O error occurs.
	 */
	private void indentIn()
		throws IOException
	{
		_writer.text( _newline );
		for (int i = 0; i < _depth; i++)
		{
			_writer.text( _indent );
		}
		_depth++;
		_simpleType = true;
	}

	/**
	 * Starts a new line with the proper amount of indenting, but does not
	 * change the nesting depth. This is used for empty tags.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	private void indentSame()
		throws IOException
	{
		_writer.text( _newline );
		for (int i = 0; i < _depth; i++)
		{
			_writer.text( _indent );
		}
		_simpleType = false;
	}

	/**
	 * Decreases the nesting depth, and starts a new line with the proper amount
	 * of indenting (except for a {@link #_simpleType}).
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	private void indentOut()
		throws IOException
	{
		_depth--;
		if ( !_simpleType )
		{
			_writer.text( _newline );
			for (int i = 0; i < _depth; i++)
			{
				_writer.text( _indent );
			}
		}
		_simpleType = false;
	}

	public XmlSerializer startTag( final String namespace, final String name )
		throws IOException
	{
		indentIn();
		_writer.startTag( namespace, name );
		return this;
	}

	public XmlSerializer endTag( final String namespace, final String name )
		throws IOException
	{
		indentOut();
		_writer.endTag( namespace, name );
		return this;
	}

	public void endDocument()
		throws IOException
	{
		_writer.endDocument();
	}

	public void flush()
		throws IOException
	{
		_writer.flush();
	}

	public XmlSerializer attribute( final String namespace, final String name, final String value )
		throws IOException
	{
		_writer.attribute( namespace, name, value );
		return this;
	}

	public void setFeature( final String name, final boolean state )
		throws IllegalArgumentException, IllegalStateException
	{
		_writer.setFeature( name, state );
	}

	public boolean getFeature( final String name )
	{
		return _writer.getFeature( name );
	}

	public void setProperty( final String name, final Object value )
		throws IllegalArgumentException, IllegalStateException
	{
		_writer.setProperty( name, value );
	}

	public Object getProperty( final String name )
	{
		return _writer.getProperty( name );
	}

	public void setOutput( final OutputStream os, final String encoding )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.setOutput( os, encoding );
	}

	public void setOutput( final Writer writer )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.setOutput( writer );
	}

	public void startDocument( final String encoding, final Boolean standalone )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.startDocument( encoding, standalone );
	}

	public void setPrefix( final String prefix, final String namespace )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.setPrefix( prefix, namespace );
	}

	public String getPrefix( final String namespace, final boolean generatePrefix )
		throws IllegalArgumentException
	{
		return _writer.getPrefix( namespace, generatePrefix );
	}

	public int getDepth()
	{
		return _writer.getDepth();
	}

	public String getNamespace()
	{
		return _writer.getNamespace();
	}

	public String getName()
	{
		return _writer.getName();
	}

	public XmlSerializer text( final String text )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		return _writer.text( text );
	}

	public XmlSerializer text( final char[] buf, final int start, final int len )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		return _writer.text( buf, start, len );
	}

	public void cdsect( final String text )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.cdsect( text );
	}

	public void entityRef( final String text )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.entityRef( text );
	}

	public void processingInstruction( final String text )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.processingInstruction( text );
	}

	public void comment( final String text )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.comment( text );
	}

	public void docdecl( final String text )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.docdecl( text );
	}

	public void ignorableWhitespace( final String text )
		throws IOException, IllegalArgumentException, IllegalStateException
	{
		_writer.ignorableWhitespace( text );
	}
}
