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
import java.util.ArrayList;
import java.util.List;

/**
 * This chunk is the base class for any chunk that has subchunks.
 * The class itself can be used for chunks that only holds subchunks (no data).
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public class HierarchyChunk
	extends Chunk
{
	/**
	 * Collection of subchunks.
	 */
	private final List _chunks = new ArrayList();

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public HierarchyChunk( final int id )
	{
		super( id );
		//System.out.println( "New Hierarchy Chunk ID = " + getHex( id ) );
	}

	/**
	 * Adds a new chunk to the hierarchy.
	 *
	 * @param   chunk   The new chunk.
	 */
	public final void add( final Chunk chunk )
	{
		_chunks.add( chunk );
	}

	/**
	 * Get the chunk at the specified index.
	 *
	 * @param   index   Index of the chunk to get.
	 *
	 * @return  the chunk at specified index.
	 */
	public final Chunk getChunk( final int index )
	{
		return (Chunk)getChunks().get( index );
	}

	/**
	 * Gets the number of chunks in this hierarchy chunk.
	 *
	 * @return  the number of subchunks.
	 */
	public final int getChunkCount()
	{
		return getChunks().size();
	}

	/**
	 * Gets all subchunks.
	 *
	 * @return  List containing all subchunks of this chunk.
	 */
	public final List getChunks()
	{
		return _chunks;
	}

	/**
	 * Get all subchunks of this chunks with the specified ID.
	 *
	 * @param   id      ID of chunks to get.
	 *
	 * @return  List with all chunks matching the ID.
	 */
	public final List getChunksByID( final int id )
	{
		final List collect = new ArrayList();

		for ( int i = 0 ; i < getChunkCount() ; i++ )
		{
			final Chunk chunk = getChunk( i );
			if ( chunk.getID() == id )
				collect.add( chunk );
		}

		return collect;
	}

	/**
	 * Gets the first occurence of a chunk with specified ID.
	 *
	 * @param   id  ID of the Chunk to get.
	 *
	 * @return  Chunk with specified ID or null if not found.
	 */
	public final Chunk getFirstChunkByID( final int id )
	{
		Chunk result = null;

		for ( int i = 0 ; i < getChunkCount() ; i++ )
		{
			final Chunk chunk = getChunk( i );
			if ( chunk.getID() == id )
			{
				result = chunk;
				break;
			}
		}

		return result;
	}

	public long getSize()
	{
		final List sub = getChunks();

		int size = 0;
		for ( int i = 0 ; i < sub.size() ; i++ )
			size += ((Chunk)sub.get( i )).getSize();

		return HEADER_SIZE + size;
	}

	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );
		//System.out.println( "size = " + _chunkSize );
		readSubChunks( is );
	}

	/**
	 * Reads all subchunks of the hierarchchunk from inputstream.
	 *
	 * @param   is      Stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public final void readSubChunks( final Ab3dsInputStream is )
		throws IOException
	{
		int id = is.readInt();
		while ( !is.isEOF() && is.getPointer() < _chunkEnd )
		{
			System.out.println( this + " found id : " + getHex(id) + " at " + is.getPointer() );
			final Chunk sub = createChunk( id );
			sub.read( is );
			getChunks().add( sub );

			if ( is.getPointer() < _chunkEnd )
				id = is.readInt();
		}
	}

	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		writeHeader( os );
		writeSubChunks( os );
	}

	/**
	 * Writes all subchunks to the outputstream.
	 *
	 * @param   os      Stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public final void writeSubChunks( final Ab3dsOutputStream os )
		throws IOException
	{
		final List sub = getChunks();
		for ( int i = 0 ; i < sub.size() ; i++ )
		{
			((Chunk)sub.get( i )).write( os );
		}
	}

}
