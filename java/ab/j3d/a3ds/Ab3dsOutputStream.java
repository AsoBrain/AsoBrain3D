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
import java.io.OutputStream;

/**
 * Outputstream specially to write 3ds types.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class Ab3dsOutputStream
{
	/**
	 * Stream to write to.
	 */
	private final OutputStream _os;

	/**
	 * Current file pointer.
	 */
	private long _pointer;

	/**
	 * Constructor.
	 *
	 * @param   os      Stream to write to.
	 */
	public Ab3dsOutputStream( final OutputStream os )
	{
		_os = os;
		_pointer = 0;
	}

	/**
	 * Gets the current filepointer of the stream.
	 *
	 * @return  Current position in the file.
	 */
	public long getPointer()
	{
		return _pointer;
	}

	/**
	 * Writes a boolean to stream.
	 *
	 * @param   b   Boolean to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public void writeBoolean( final boolean b )
		throws IOException
	{
		_os.write( b ? 1 : 0 );
		_pointer += 1;
	}

	/**
	 * Writes a byte to stream.
	 *
	 * @param   b   Byte to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public void writeByte( final byte b )
		throws IOException
	{
		_os.write( b );
		_pointer += 1;
	}

	/**
	 * Writes a float to stream.
	 *
	 * @param   f   Float to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public void writeFloat( final float f )
		throws IOException
	{
		writeLong( Float.floatToIntBits( f ) );
	}

	/**
	 * Writes an int to stream.
	 *
	 * @param   i   Int to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public void writeInt( final int i )
		throws IOException
	{
		final int high = i >> 8;
		final int low = i & 255;

		_os.write( low );
		_os.write( high );
		_pointer += 2;
	}

	/**
	 * Writes a long to stream.
	 *
	 * @param   l   Long to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public void writeLong( final long l )
		throws IOException
	{
		writeInt( (int)( l &  0xFFFF ) );
		writeInt( (int)( l >> 16 ) );
	}

	/**
	 * Writes a string to stream.
	 *
	 * @param   str     String to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public void writeString( final String str )
		throws IOException
	{
		for ( int i = 0 ; i < str.length() ; i++ )
			_os.write( (int)str.charAt( i ) );
		_os.write( 0 );

		_pointer += str.length() + 1;
	}
}
