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
 * Material texture map chunk.
 * Also applies to material masks.
 *
 * @author	Sjoerd Bouwman
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class TextureMap extends DataChunk 
{
	private int _amount;
	private String _path;
	private int _options;
	private float _blur;
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public TextureMap( int id ) 
	{
		super( id );
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		long size = HEADER_SIZE;	// the chunk itself

		size+= HEADER_SIZE + INT_SIZE;	// amount
		size+= HEADER_SIZE + STRING_SIZE( _path ); 	// path
		size+= HEADER_SIZE + INT_SIZE;	// map options
		size+= HEADER_SIZE + FLOAT_SIZE;	// map filtering blur
		
		return size;		
	}

	/**
	 * Reads the chunk from the input stream.
	 * 
	 * @param	is	the stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read( Ab3dsInputStream is ) 
		throws IOException 
	{
		readHeader( is );

		/*
		 * Read properties.
		 */
		int id = is.readInt();
		long size = is.readLong();
		
		while ( !is.isEOF() && is.getPointer() < _chunkEnd )
		{
			switch ( id )
			{
				case DOUBLE_BYTE		: _amount = is.readInt();		break;
				case MAP_PATH			: _path = is.readString();		break;
				case MAP_OPTIONS		: _options = is.readInt();		break;
				case MAP_BLUR			: _blur = is.readFloat();		break;
		
				default 				: is.skip( size - HEADER_SIZE );		// skip unknown chunks.
			}	
			
			if ( is.getPointer() < _chunkEnd )
			{
				id = is.readInt();
				size = is.readLong();
			}
		}

		if ( Ab3dsFile.DEBUG ) System.out.println( "  Map = " + _path );
	}

	/**
	 * Set parameters for TextureMap
	 *
	 * @param	amount	opacity percentage
	 * @param	path	location of map.
	 * @param	options	options (don't know).
	 * @param	blur	blur amount.
	 */
	public void set( int amount , String path , int options , float blur )
	{
		_amount = amount;
		_path = path;
		_options = options;
		_blur = blur;
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
		writeHeader( os );

		os.writeInt( DOUBLE_BYTE );
		os.writeLong( HEADER_SIZE + INT_SIZE );
		os.writeInt( _amount );

		os.writeInt( MAP_PATH );
		os.writeLong( HEADER_SIZE + STRING_SIZE( _path ) );
		os.writeString( _path );

		os.writeInt( MAP_OPTIONS );
		os.writeLong( HEADER_SIZE + INT_SIZE );
		os.writeInt( _options );

		os.writeInt( MAP_BLUR );
		os.writeLong( HEADER_SIZE + FLOAT_SIZE );
		os.writeFloat( _blur );
	}

}
