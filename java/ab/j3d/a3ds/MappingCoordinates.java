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
 *
 * @author	Sjoerd Bouwman
 * @version	$Revision$ $Date$
 */
public class MappingCoordinates extends DataChunk 
{
	/**
	 * ???
	 */
	private float[] _mapX;
	private float[] _mapY;
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public MappingCoordinates( int id )
	{
		super( id );
		if ( Ab3dsFile.DEBUG ) 
			System.out.println( "Mapping coordinates" );
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		return HEADER_SIZE + INT_SIZE + 2 * _mapX.length * FLOAT_SIZE;
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

		int count = is.readInt();

		_mapX = new float[ count ];
		_mapY = new float[ count ];

		for ( int i = 0 ; i < count ; i++ )
		{
			_mapX[i] = is.readFloat();
			_mapY[i] = is.readFloat();
		}

		if ( Ab3dsFile.DEBUG ) 
		{
			System.out.println( "Mapping" );
			for ( int i = 0 ; i < count ; i++ )
			{
				System.out.println( "Vertex : " + i + " x:" + _mapX[i] + " y:" + _mapY[i] );
			}
		}
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

		os.writeInt( _mapX.length );

		for ( int i = 0 ; i < _mapX.length ; i++ )
		{
			os.writeFloat( _mapX[i] );
			os.writeFloat( _mapY[i] );
		}
	}

}
