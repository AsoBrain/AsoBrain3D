/*
 * $Id$
 *
 * (C) Copyright 1999-2004 Sjoerd Bouwman (aso@asobrain.com)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it as you see fit.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ab.j3d.a3ds;

import java.io.*;

/**
 * Inputstream specially to read 3ds types.
 *
 * @author	Sjoerd Bouwman
 * @version	$Revision$ $Date$
 */
public class Ab3dsInputStream 
{
	/**
	 * Current file pointer.
	 */
	private long pointer = 0;

	/**
	 * Stream to read from.
	 */
	private InputStream is = null;

	/**
	 * If true, end of file is received.
	 */
	private boolean eof = false;
	/**
	 * Constructor.
	 *
	 * @param	is	Inputstream to get input from.
	 */
	public Ab3dsInputStream( InputStream is ) 
	{
		this.is = is;
	}

	/**
	 * Gets the current filepointer of the stream.
	 *
	 * @return	the current position in the file.
	 */
	public long getPointer()
	{
		return pointer;
	}

	/**
	 * Checks if the stream has ended.
	 *
	 * @return	true if the stream has ended.
	 */
	public boolean isEOF()
	{
		return eof;
	}

	/**
	 * Read boolean from stream.
	 *
	 * @return	 the read boolean.
	 *
	 * @throws IOException when reading failed.
	 */
	public boolean readBoolean() throws IOException
	{
		return readByte() == 0;
	}

	/**
	 * Read byte from stream.
	 *
	 * @return	 the read byte.
	 *
	 * @throws IOException when reading failed.
	 */
	public byte readByte() throws IOException
	{
		int r = is.read();
		if ( r < 0 )
		{
			eof = true;
			return 0;
		}
		pointer+=1;
		return (byte)r;
	}

	/**
	 * Read float from stream.
	 *
	 * @return	 the read float.
	 *
	 * @throws IOException when reading failed.
	 */
	public float readFloat() throws IOException
	{
		return Float.intBitsToFloat( (int)readLong() );
	}

	/**
	 * Read int from stream.
	 *
	 * @return	 the read int.
	 *
	 * @throws IOException when reading failed.
	 */
	public int readInt() throws IOException
	{
		int high = is.read();
		int low = is.read();
		if ( high < 0 || low < 0 )
		{
			eof = true;
			return 0;
		}
		pointer+=2;
	
		return high + (low<<8);
	}

	/**
	 * Read long from stream.
	 *
	 * @return	 the read long.
	 *
	 * @throws IOException when reading failed.
	 */
	public long readLong() throws IOException
	{
		long low = readInt();
		long high = readInt();
		return low + (high<<16);
	}

	/**
	 * Read string from stream.
	 *
	 * @return	 the read string.
	 *
	 * @throws IOException when reading failed.
	 */
	public String readString() throws IOException
	{
		String str = "";

		byte b = 0;
		while ( (b=readByte()) != 0 )
			str+=(char)b;
		
		return str;
	}

	/**
	 * Skip specified number of bytes in stream.
	 *
	 * @param	count	number of bytes to skip.
	 *
	 * @throws IOException when skipping failed.
	 */
	public void skip( long count ) throws IOException
	{
		is.skip( count );
		pointer+=count;
	}

}
