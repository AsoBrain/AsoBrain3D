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
import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This chunk is the base class for any chunk that has subchunks.
 * The class itself can be used for chunks that only holds subchunks (no data).
 * 
 * @author	Sjoerd Bouwman
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class HierarchyChunk extends Chunk 
{
	/**
	 * Collection of subchunks.
	 */
	private Vector _chunks = new Vector();

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public HierarchyChunk( int id )
	{
		super( id );
		//System.out.println( "New Hierarchy Chunk ID = " + getHex( id ) );
	}

	/**
	 * Adds a new chunk to the hierarchy.
	 *
	 * @param	chunk	the new chunk.
	 */
	public void add( Chunk chunk )
	{
		_chunks.addElement( chunk );
	}

	/**
	 * Get the chunk at the specified index.
	 *
	 * @param	index	the index of the chunk to get.
	 *
	 * @return	the chunk at specified index.
	 */
	public Chunk getChunk( int index )
	{
		return (Chunk)getChunks().elementAt( index );
	}

	/**
	 * Gets the number of chunks in this hierarchy chunk.
	 *
	 * @return	the number of subchunks.
	 */
	public int getChunkCount()
	{
		return getChunks().size();
	}

	/**
	 * Gets all subchunks.
	 *
	 * @return	Vector containing all subchunks of this chunk.
	 */
	public Vector getChunks()
	{
		if ( _chunks == null )
			_chunks = new Vector();
			
		return _chunks;
	}

	/**
	 * Get all subchunks of this chunks with the specified ID.
	 *
	 * @param	id	the id of chunks to get.
	 *
	 * @return	Vector with all chunks matching the ID.
	 */
	public Vector getChunksByID( int id )
	{
		Vector collect = new Vector();

		for ( int i = 0 ; i < getChunkCount() ; i++ )
		{
			if ( getChunk( i ).getID() == id )
				collect.addElement( getChunk( i ) );
		}	
			
		return collect;
	}

	/**
	 * Gets the first occurence of a chunk with specified ID.
	 *
	 * @param	id	ID of the Chunk to get.
	 *
	 * @return	Chunk with specified ID or null if not found.
	 */
	public Chunk getFirstChunkByID( int id )
	{
		for ( int i = 0 ; i < getChunkCount() ; i++ )
		{
			if ( getChunk( i ).getID() == id )
				return getChunk( i );
		}	
			
		return null;
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		Vector sub = getChunks();

		int size = 0;
		for ( int i = 0 ; i < sub.size() ; i++ )
		{
			size += ((Chunk)sub.elementAt( i )).getSize();
		}
		
		return HEADER_SIZE + size;
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

		//System.out.println( "size = " + _chunkSize );
		
		readSubChunks( is );	
	}

	/**
	 * Reads all subchunks of the hierarchchunk from inputstream.
	 *
	 * @param	is	the stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void readSubChunks( Ab3dsInputStream is )
		throws IOException
	{
		int id = is.readInt();
		while ( !is.isEOF() && is.getPointer() < _chunkEnd )
		{
			System.out.println( this + " found id : " + getHex(id) + " at " + is.getPointer() );
			Chunk sub = createChunk( id );
			sub.read( is );
			getChunks().addElement( sub );
			
			if ( is.getPointer() < _chunkEnd )
				id = is.readInt();
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

		writeSubChunks( os );
	}

	/**
	 * Writes all subchunks to the outputstream.
	 * 
	 * @param	os	the stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void writeSubChunks( Ab3dsOutputStream os )
		throws IOException
	{
		Vector sub = getChunks();
		for ( int i = 0 ; i < sub.size() ; i++ )
		{
			((Chunk)sub.elementAt( i )).write( os );	
		}
	}

}
