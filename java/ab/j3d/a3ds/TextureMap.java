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

/**
 * Material texture map chunk.
 * Also applies to material masks.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class TextureMap extends DataChunk
{
	private int _amount;
	private String _path;
	private int _options;
	private float _blur;
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public TextureMap( final int id )
	{
		super( id );
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return  the size of the chunk in bytes.
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
	 * @param   is	the stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read( final Ab3dsInputStream is )
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
	 * @param   amount	opacity percentage
	 * @param   path	location of map.
	 * @param   options	options (don't know).
	 * @param   blur	blur amount.
	 */
	public void set( final int amount , final String path , final int options , final float blur )
	{
		_amount = amount;
		_path = path;
		_options = options;
		_blur = blur;
	}

	/**
	 * Writes the chunk the output stream.
	 *
	 * @param   os	the stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void write( final Ab3dsOutputStream os )
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
