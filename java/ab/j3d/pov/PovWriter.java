package ab.j3d.pov;

import java.io.*;

/**
 * This writer is used for POV-Ray files. It provides simple indentation and
 * line termination support.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovWriter
	extends FilterWriter
{
	/**
	 * Flag to indicate that output is a the beginning of a new line.
	 */
	private boolean _beginningOfLine = true;

	/**
	 * Current indent depth.
	 */
	private int _currentIndent = 0;

	/**
	 * Indenting string.
	 */
	private final String _indentString;

	/**
	 * Constructor Writer that is able to indent.
	 *
	 * @param   out     Writer object to provide the underlying stream.
	 */
	public PovWriter( final Writer out )
	{
		this( out, "    " );
	}

	/**
	 * Constructor Writer that is able to indent.
	 *
	 * @param   out     Writer object to provide the underlying stream.
	 */
	public PovWriter( final Writer out, final String indentString )
	{
		super( out );

		if ( indentString == null )
		{
			throw new NullPointerException( "indentString" );
		}

		_indentString = indentString;
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
}
