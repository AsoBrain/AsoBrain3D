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

import java.io.*;

/**
 * When the current implementation of this package does not know about
 * certain chunk type, this chunk class is used to skip the chunk.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class UnknownChunk extends Chunk
{
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public UnknownChunk( final int id )
	{
		super(id);
		if ( Ab3dsFile.DEBUG )
			System.out.println( "New UNKNOWN Chunk ID = " + getHex( id ) );
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return  the size of the chunk in bytes.
	 */
	public long getSize()
	{
		return 0;
	}

	/**
	 * Reads the chunk from the input stream.
	 *
	 * @param   is	the stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read( final Ab3dsInputStream is )
		throws java.io.IOException
	{
		readHeader( is );
		is.skip( _chunkEnd - is.getPointer() );
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
		/*
		 * Don't write anything
		 */
		if ( Ab3dsFile.DEBUG )
			System.out.println( "Skipping unknown chunk : " + getHex( getID() ) );
	}
}
