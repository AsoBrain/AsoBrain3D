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
 * Outputstream specially to write 3ds types.
 *
 * @author	Sjoerd Bouwman
 * @version	$Revision$ $Date$
 */
public class Ab3dsOutputStream 
{
	/**
	 * Current file pointer.
	 */
	private long pointer = 0;

	/**
	 * Stream to write to.
	 */
	private OutputStream os = null; 
	/**
	 * Constructor.
	 *
	 * @param	os	Outputstream to write to.
	 */
	public Ab3dsOutputStream( OutputStream os ) 
	{
		this.os = os;
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
	 * Writes a boolean to stream.
	 *
	 * @param	b	boolean to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public int writeBoolean( boolean b ) throws IOException
	{
		os.write( b ? 1 : 0 );
		pointer+=1;
		return 1;
	}

	/**
	 * Writes a byte to stream.
	 *
	 * @param	b	byte to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public int writeByte( byte b ) throws IOException
	{
		os.write( b );
		pointer+=1;
		return 1;
	}

	/**
	 * Writes a float to stream.
	 *
	 * @param	f	float to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public float writeFloat( float f ) throws IOException
	{
		return writeLong( Float.floatToIntBits( f ) );
	}

	/**
	 * Writes an int to stream.
	 *
	 * @param	i	int to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public int writeInt( int i ) throws IOException
	{
		int high = i >> 8;
		int low = i & 255;

		os.write( low );
		os.write( high );
		pointer+=2;
	
		return 2;
	}

	/**
	 * Writes a long to stream.
	 *
	 * @param	l	long to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public int writeLong( long l ) throws IOException
	{
		int low = (int)(l & 0xFFFF);
		int high = (int)(l >> 16);

		writeInt( low );
		writeInt( high );

		return 4;
	}

	/**
	 * Writes a string to stream.
	 *
	 * @param	s	string to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public int writeString( String str )
		throws IOException
	{
		for ( int i = 0 ; i < str.length() ; i++ )
		{
			os.write( (int)str.charAt( i ) );
		}
		os.write( 0 );
		pointer += str.length() + 1;
		return str.length() + 1;
	}

}
