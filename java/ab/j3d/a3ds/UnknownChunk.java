package ab.a3ds;

/*
 * $Id$
 *
 * (C) Copyright 1999-2002 Sjoerd Bouwman (aso@asobrain.com)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it as you see fit.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import java.io.*;

/**
 * When the current implementation of this package does not know about
 * certain chunk type, this chunk class is used to skip the chunk.
 *
 * @author	Sjoerd Bouwman
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class UnknownChunk extends Chunk 
{
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public UnknownChunk( int id ) 
	{
		super(id);
		System.out.println( "New UNKNOWN Chunk ID = " + getHex( id ) );
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		return 0;
	}

	/**
	 * Reads the chunk from the input stream.
	 * 
	 * @param	is	the stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read( Ab3dsInputStream is ) 
		throws java.io.IOException 
	{
		readHeader( is );
		System.out.println( "size = " + _chunkSize );
		is.skip( _chunkEnd - is.getPointer() );
	}

	/**
	 * Writes the chunk the output stream.
	 * 
	 * @param	os	the stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void write( Ab3dsOutputStream os ) 
		throws IOException 
	{
		/*
		 * Don't write anything
		 */
		 //System.out.println( "Skipping unknown chunk : " + getHex( getID() ) );
	}

}
