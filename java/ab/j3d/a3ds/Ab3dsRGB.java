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

/**
 * This chunk specifies a RGB color value.
 *
 * @author	Sjoerd Bouwman
 * @version	$Revision$ $Date$
 */
public class Ab3dsRGB extends DataChunk 
{
	/**
	 * If true, the color is specified by floats, otherwise by ints.
	 */
	private boolean _floats = false;

	/**
	 * Float value of color (0..1) for each value.
	 */
	private float _fr;
	private float _fg;
	private float _fb;

	/**
	 * Int value of color (0..255) for each value.
	 */
	private byte  _r;
	private byte  _g;
	private byte  _b;
	/**
	 * Constructs a rgb color with specified float values.
	 *
	 * @param	r	red segement (0..1).
	 * @param	g	green segement (0..1).
	 * @param	b	blue segement (0..1).
	 */
	public Ab3dsRGB( float r , float g , float b )
	{
		this( RGB_FLOAT , true );
		_fr = r;
		_fg = g;
		_fb = b;
	}

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id		the ID of the chunk.
	 * @param	floats	if true, the rgb is specified as floats, otherwise with bytes.
	 */
	public Ab3dsRGB( int id, boolean floats ) 
	{
		super( id );
		_floats = floats;
//		if ( Ab3dsFile.DEBUG ) System.out.println( "  - RGB " + (floats?"floats":"bytes") );
	}

	/**
	 * Constructs a rgb color with specified int values.
	 *
	 * @param	r	red segement (0..255).
	 * @param	g	green segement (0..255).
	 * @param	b	blue segement (0..255).
	 */
	public Ab3dsRGB( int r , int g , int b )
	{
		this( RGB_BYTE , false );
		_r = (byte)r;
		_g = (byte)g;
		_b = (byte)b;
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		return HEADER_SIZE + (_floats ? 3*FLOAT_SIZE : 3*BYTE_SIZE);
	}

	/**
	 * Reads the chunk from the input stream.
	 * 
	 * @param	is	the stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read( Ab3dsInputStream is) throws java.io.IOException 
	{
		readHeader( is );

		if ( _floats )
		{
			_fr = is.readFloat();
			_fg = is.readFloat();
			_fb = is.readFloat();
		}
		else
		{
			_r = is.readByte();
			_g = is.readByte();
			_b = is.readByte();
		}
	}

	/**
	 * Returns a String representation of this chunk.
	 *
	 * @return	this chunk as a string.
	 */
	public String toString()
	{
		if ( _floats )
			return "R:" + _fr + " G:" + _fg + " B:" + _fb;
		return "R:" + _r + " G:" + _g + " B:" + _b;
	}

	/**
	 * Writes the chunk the output stream.
	 * 
	 * @param	os	the stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void write( Ab3dsOutputStream os) 
		throws java.io.IOException 
	{
		writeHeader( os );

		if ( _floats )
		{
			os.writeFloat( _fr );
			os.writeFloat( _fg );
			os.writeFloat( _fb );
		}
		else
		{
			os.writeByte( _r );
			os.writeByte( _g );
			os.writeByte( _b );
		}
	}

}
