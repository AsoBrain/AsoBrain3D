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
 * This chunk specifies a list of vertices for a mesh.
 *
 * @author	Sjoerd Bouwman
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class VertexList extends DataChunk 
{
	private Vertex[] vertices;
	
	public static class Vertex
	{
		public float x;
		public float y;
		public float z;
		public Vertex( float x , float y , float z )
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	/**
	 * No-op constructor for generation purposes.
	 */
	public VertexList()
	{
		this( TRI_VERTEXLIST );
	}

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public VertexList( int id ) 
	{
		super(id);
		if ( Ab3dsFile.DEBUG ) System.out.println( "  - Reading vertext list" );
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		return HEADER_SIZE + 2 + vertices.length * 3 * FLOAT_SIZE;
	}

	/**
	 * Get vertex at specified index.
	 *
	 * @param	i	the index of the vertex to get.
	 *
	 * @return	the vertex at index.
	 */
	public Vertex getVertex( int i )
	{
		return vertices[i];
	}

	/**
	 * Get total amount of vertices in list.
	 *
	 * @return	vertex count.
	 */
	public int getVertexCount()
	{
		return vertices.length;
	}

	/**
	 * Reads the chunk from the input stream.
	 * 
	 * @param	is	the stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read( Ab3dsInputStream is ) throws IOException 
	{
		readHeader( is );
		
		int vertexCount = is.readInt();

		vertices = new Vertex[ vertexCount ];

		for ( int i = 0 ; i < vertexCount ; i++ )
		{
			float x = is.readFloat();
			float y = is.readFloat();
			float z = is.readFloat();
			vertices[i] = new Vertex( x , y , z );
		}
	}

	/**
	 * Set all vertices at once.
	 *
	 * @param	vertices	array of new vertices.
	 */
	public void set( Vertex[] vertices )
	{
		this.vertices = vertices;
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
		if ( Ab3dsFile.DEBUG ) System.out.println( "  - Writing vertex list" );
		writeHeader( os );

		os.writeInt( vertices.length );

		for ( int i = 0 ; i < vertices.length ; i++ )
		{
			os.writeFloat( vertices[i].x );
			os.writeFloat( vertices[i].y );
			os.writeFloat( vertices[i].z );
		}
	}

}
