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
/**
 * This chunk simply holds a float value.
 * 
 * @author	Sjoerd Bouwman
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class FloatChunk extends DataChunk 
{
	/**
	 * Float value of chunk.
	 */
	public float f;
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public FloatChunk(int id) 
	{
		super(id);
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		return HEADER_SIZE + FLOAT_SIZE;
	}

	/**
	 * Reads the chunk from the input stream.
	 * 
	 * @param	is	the stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read(Ab3dsInputStream is) throws java.io.IOException
	{
		readHeader( is );

		f = is.readFloat();
	}

	/**
	 * Writes the chunk the output stream.
	 * 
	 * @param	os	the stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void write(Ab3dsOutputStream os) throws java.io.IOException 
	{
		writeHeader( os );

		os.writeFloat( f );
	}

}
