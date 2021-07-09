/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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
package ab.j3d.pov;

import java.io.*;
import java.text.*;
import java.util.*;

import ab.j3d.awt.view.*;
import org.jetbrains.annotations.*;

/**
 * This writer is used for POV-Ray files. It provides simple indentation and
 * line termination support.
 *
 * @author Peter S. Heijnen
 */
public class PovWriter
	extends FilterWriter
{
	/**
	 * Number format to format numeric as floating-point values.
	 */
	private final NumberFormat _floatFormat;
	{
		final NumberFormat floatFormat = NumberFormat.getNumberInstance( Locale.US );
		floatFormat.setMinimumFractionDigits( 1 );
		floatFormat.setMaximumFractionDigits( 5 );
		floatFormat.setGroupingUsed( false );
		_floatFormat = floatFormat;
	}

	/**
	 * Number format to format numeric as floating-point values.
	 */
	private final NumberFormat _doubleFormat;
	{
		final NumberFormat doubleFormat = NumberFormat.getNumberInstance( Locale.US );
		doubleFormat.setMinimumFractionDigits( 1 );
		doubleFormat.setMaximumFractionDigits( 16 );
		doubleFormat.setGroupingUsed( false );
		_doubleFormat = doubleFormat;
	}

	/**
	 * Flag to indicate that output is a the beginning of a new line.
	 */
	private boolean _beginningOfLine = true;

	/**
	 * Current indent depth.
	 */
	private int _currentIndent = 0;

	/**
	 * Texture library used to resolve textures to files.
	 */
	@Nullable
	private final TextureLibrary _textureLibrary;

	/**
	 * Indenting string.
	 */
	private final String _indentString;

	/**
	 * Constructor Writer that is able to indent.
	 *
	 * @param out            Writer object to provide the underlying stream.
	 * @param textureLibrary Texture library used to resolve textures to files.
	 */
	public PovWriter( @NotNull final Writer out, @NotNull final TextureLibrary textureLibrary )
	{
		this( out, textureLibrary, "    " );
	}

	/**
	 * Constructor Writer that is able to indent.
	 *
	 * @param out            Writer object to provide the underlying stream.
	 * @param textureLibrary Texture library used to resolve textures to files;
	 *                       if {@code null} texture names are used.
	 * @param indentString   String used for indentation.
	 */
	public PovWriter( @NotNull final Writer out, @Nullable final TextureLibrary textureLibrary, @NotNull final String indentString )
	{
		super( out );
		_textureLibrary = textureLibrary;
		_indentString = indentString;
	}

	@Nullable
	public TextureLibrary getTextureLibrary()
	{
		return _textureLibrary;
	}

	/**
	 * Before every output is written, this method is called to check if
	 * indenting should be performed (_beginningOfLine). If so, sufficient
	 * spaces a prepended to match the indent size.
	 *
	 * @throws  IOException if output cannot be written.
	 */
	protected void checkWrite()
		throws IOException
	{
		if ( _beginningOfLine)
		{
			_beginningOfLine = false;

			final String indentString = _indentString;

			for ( int i = _currentIndent ; i > 0 ; i-- )
			{
				write( indentString );
			}
		}
	}

	/**
	 * Increase indenting by one step.
	 */
	public void indentIn()
	{
		_currentIndent++;
	}

	/**
	 * Decrease indenting by one step.
	 */
	public void indentOut()
	{
		if ( _currentIndent > 0 )
		{
			_currentIndent--;
		}
	}

	/**
	 * Generates a newline.
	 *
	 * @throws  IOException if output cannot be written.
	 */
	public void newLine()
		throws IOException
	{
		super.write( (int)'\n' );
		_beginningOfLine = true;
	}

	@Override
	public void write( final char[] cbuf, final int off, final int len )
		throws IOException
	{
		if ( len > 0 )
		{
			checkWrite();
		}

		super.write( cbuf, off, len );
	}

	@Override
	public void write( final int c )
		throws IOException
	{
		checkWrite();

		super.write( c );
	}

	@Override
	public void write(final String s, final int off, final int len)
		throws IOException
	{
		if ( len > 0 )
		{
			checkWrite();
		}

		super.write( s, off, len );
	}

	/**
	 * Writes a newline.
	 *
	 * @throws  IOException if output cannot be written.
	 */
	public void writeln()
		throws IOException
	{
		newLine();
	}

	/**
	 * Writes a String to the output followed by a newline.
	 *
	 * @param   s   String to write to the output.
	 *
	 * @throws  IOException if output cannot be written.
	 */
	public void writeln( final String s )
		throws IOException
	{
		write( s );
		newLine();
	}

	/**
	 * Format float-point value.
	 *
	 * @param value Floating-point value.
	 *
	 * @return Formatted string.
	 */
	protected String format( final double value )
	{
		return _floatFormat.format( value + 0.0 ); // Prevent negative zero.
	}

	/**
	 * Format float-point value.
	 *
	 * @param value Floating-point value.
	 *
	 * @return Formatted string.
	 */
	protected String formatPrecise( final double value )
	{
		return _doubleFormat.format( value + 0.0 ); // Prevent negative zero.
	}
}
