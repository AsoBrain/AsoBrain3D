/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Sjoerd Bouwman
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
package ab.j3d.a3ds;

import java.io.IOException;
import java.io.InputStream;

/**
 * Inputstream specially to read 3ds types.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class Ab3dsInputStream
{
	/**
	 * Current file pointer.
	 */
	private long _pointer;

	/**
	 * Stream to read from.
	 */
	private final InputStream _is;

	/**
	 * If true, end of file is received.
	 */
	private boolean _eof;

	/**
	 * Constructor.
	 *
	 * @param   is      Inputstream to read from.
	 */
	public Ab3dsInputStream( final InputStream is )
	{
		_is      = is;
		_pointer = 0;
		_eof     = false;
	}

	/**
	 * Gets the current filepointer of the stream.
	 *
	 * @return the current position in the file.
	 */
	public long getPointer()
	{
		return _pointer;
	}

	/**
	 * Checks if the stream has ended.
	 *
	 * @return true if the stream has ended.
	 */
	public boolean isEOF()
	{
		return _eof;
	}

	/**
	 * Read boolean from stream.
	 *
	 * @return  Read boolean.
	 *
	 * @throws  IOException when reading failed.
	 */
	public boolean readBoolean()
		throws IOException
	{
		return readByte() == 0;
	}

	/**
	 * Read byte from stream.
	 *
	 * @return  Read byte.
	 *
	 * @throws  IOException when reading failed.
	 */
	public byte readByte()
		throws IOException
	{
		final byte result;

		final int r = _is.read();
		if ( r < 0 )
		{
			_eof = true;
			result = 0;
		}
		else
		{
			_pointer += 1;
			result = (byte)r;
		}

		return result;
	}

	/**
	 * Read float from stream.
	 *
	 * @return  Read float.
	 *
	 * @throws  IOException when reading failed.
	 */
	public float readFloat()
		throws IOException
	{
		return Float.intBitsToFloat( (int)readLong() );
	}

	/**
	 * Read int from stream.
	 *
	 * @return  Read int.
	 *
	 * @throws  IOException when reading failed.
	 */
	public int readInt()
		throws IOException
	{
		final int result;

		final int high = _is.read();
		final int low  = _is.read();

		if ( ( high < 0 ) || ( low < 0 ) )
		{
			_eof = true;
			result = 0;
		}
		else
		{
			_pointer += 2;
			result = high + ( low << 8 );
		}

		return result;
	}

	/**
	 * Read long from stream.
	 *
	 * @return the read long.
	 * @throws IOException when reading failed.
	 */
	public long readLong()
		throws IOException
	{
		final long low = readInt();
		final long high = readInt();
		return low + ( high << 16 );
	}

	/**
	 * Read string from stream.
	 *
	 * @return the read string.
	 * @throws IOException when reading failed.
	 */
	public String readString()
		throws IOException
	{
		String str = "";

		byte b;
		while ( ( b = readByte() ) != 0 )
			str += (char)b;

		return str;
	}

	/**
	 * Skip specified number of bytes in stream.
	 *
	 * @param count number of bytes to skip.
	 *
	 * @throws IOException when skipping failed.
	 */
	public void skip( final long count )
		throws IOException
	{
		_is.skip( count );
		_pointer += count;
	}
}
