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
 * This chunk specifies an object (like Mesh, Light, Camera)
 *
 * Chunk ID : 
 * - EDIT_OBJECT	= 0x4000
 *
 * Parent chunk :
 * - EDIT3DS 		= 0x3D3D
 * 
 * Possible sub chunks :
 * - OBJ_TRIMESH	= 0x4100
 * - OBJ_LIGHT		= 0x4600
 * - OBJ_CAMERA		= 0x4700
 *
 * @author	Sjoerd Bouwman
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class ObjectChunk extends HierarchyChunk 
{
	/**
	 * Name of object.
	 */
	private String name = null;
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public ObjectChunk( int id )
	{
		super( id );
		if ( Ab3dsFile.DEBUG ) System.out.println( "Found object: " + getHex( id ) );
	}

	/**
	 * Constructs an object with specified name.
	 *
	 * @param	name	the name of the object.
	 */
	public ObjectChunk( String name )
	{
		super( EDIT_OBJECT );
		this.name = name;
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		return super.getSize() + STRING_SIZE( name );
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

		name = is.readString();
		if ( Ab3dsFile.DEBUG ) System.out.println( "  Name = " + name );

//		is.skip( _chunkEnd - is.getPointer() );
		
		readSubChunks( is );
	}

	/**
	 * Returns a String representation of this chunk.
	 *
	 * @return	this chunk as a string.
	 */
	public String toString()
	{
		return super.toString() + " " + name;
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
		if ( Ab3dsFile.DEBUG ) System.out.println( "Writing object : 4000" );
		if ( Ab3dsFile.DEBUG ) System.out.println( "  Name = " + name );
		writeHeader( os );
		
		os.writeString( name );
		
		writeSubChunks( os );
	}

}
