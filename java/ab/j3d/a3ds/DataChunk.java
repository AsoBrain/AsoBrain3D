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
 * This chunk is the base class for any chunk that does not contain sub chunks.
 *
 * @author	Sjoerd Bouwman
 * @version	$Revision$ $Date$
 */
public abstract class DataChunk extends Chunk 
{
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public DataChunk( int id )
	{
		super( id );
	}

	/**
	 * Reads color from stream
	 *
	 * @param	is	stream to read from.
	 *
	 * @return	Ab3dsRGB chunk that was read.
	 *
	 * @throws IOException when reading failed.
	 */
	protected Ab3dsRGB readColor( Ab3dsInputStream is )
		throws IOException
	{
		Ab3dsRGB rgb = (Ab3dsRGB)createChunk( is.readInt() );
		rgb.read( is );
		
		return rgb;
	}

	/**
	 * Reads double byte from stream
	 *
	 * @param	is	stream to read from.
	 *
	 * @return	double byte that was read.
	 *
	 * @throws IOException when reading failed.
	 */
	protected int readDoubleByte( Ab3dsInputStream is )
		throws IOException
	{
		is.readInt();
		is.readLong();
		return is.readInt();
	}

	/**
	 * Writes color to stream
	 *
	 * @param	os		stream to write to.
	 * @param	id		the id of the color chunk.
	 * @param	chunk	the color chunk to write.
	 *
	 * @throws IOException when writing failed.
	 */
	protected void writeColor( Ab3dsOutputStream os , int id , Ab3dsRGB chunk )
		throws IOException
	{
		os.writeInt( id );
		os.writeLong( HEADER_SIZE + chunk.getSize() );
		chunk.write( os );
	}

	/**
	 * Writes a double byte chunk to stream
	 *
	 * @param	os		stream to write to.
	 * @param	id		the id of the color chunk.
	 * @param	value	double byte to write.
	 *
	 * @throws IOException when writing failed.
	 */
	protected void writeDoubleByte( Ab3dsOutputStream os , int id , int value )
		throws IOException
	{
		os.writeInt( id );
		os.writeLong( 2*HEADER_SIZE + INT_SIZE );
		os.writeInt( DOUBLE_BYTE );
		os.writeLong( HEADER_SIZE + INT_SIZE );
		os.writeInt( value );
	}

}
