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
 * This chunk specifies a list of faces for a mesh.
 *
 * @author	Sjoerd Bouwman
 * @version	$Revision$ $Date$
 */
public class FaceList extends HierarchyChunk 
{
	/**
	 * Array of faces in list.
	 */
	private Face[] faces = null;

	public static class FaceMaterial extends DataChunk
	{
		public String name;
		public int[]  faces;
		/**
		 * Constructor of Chunk with ChunkID to be used
		 * when the Chunk is read from inputstream.
		 *
		 * @param	id	the ID of the chunk.
		 */
		public FaceMaterial( int id ) { super( id ); if ( Ab3dsFile.DEBUG ) System.out.println( "  - Reading face materials" ); }
		public FaceMaterial( String name ) { this( TRI_MATERIAL ); this.name = name; }
		/**
		 * Returns the size in bytes of the chunk.
		 *
		 * @return	the size of the chunk in bytes.
		 */
		public long getSize()          { return HEADER_SIZE + STRING_SIZE( name ) + INT_SIZE + faces.length * INT_SIZE; }
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
			if ( Ab3dsFile.DEBUG ) System.out.println( "  Material = " + name );
			faces = new int[is.readInt()];
			for ( int i = 0 ; i < faces.length ; i++ )
				faces[i] = is.readInt();
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
			
			os.writeString( name );
			os.writeInt( faces.length );
			for ( int i = 0 ; i < faces.length ; i++ )
				os.writeInt( faces[i] );
		}
		public void set( int[] faces )
		{
			this.faces = faces;
		}
	}
	
	public static class Face
	{
		private int v1;
		private int v2;
		private int v3;
		private int info;
		public Face( int v1 , int v2 , int v3 , int info )
		{
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.info = info;
		}
		public int getFaceVertex( int index )
		{
			switch( index )
			{
				case 1 : return v1;
				case 2 : return v2;
				case 3 : return v3;
			}
			throw new RuntimeException( "Face always has 3 vertices (index 1..3)" );
		}			
	}

	/**
	 * No-op constructor for generation purposes.
	 */
	public FaceList()
	{
		this( TRI_FACEL1 );
	}

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public FaceList( int id ) 
	{
		super(id);
		if ( Ab3dsFile.DEBUG ) System.out.println( "  - Reading face list" );
	}

	/**
	 * Get face at specified index.
	 *
	 * @param	index	the index of Face to get.
	 *
	 * @return	Face at specified index.
	 */
	public Face getFace( int index )
	{
		return faces[index];
	}

	/**
	 * Get total amount of faces in list.
	 *
	 * return 	facecount.
	 */
	public int getFaceCount()
	{
		return faces.length;
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		return super.getSize() + INT_SIZE + faces.length * 4 * INT_SIZE;
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

		int faceCount = is.readInt();

		faces = new Face[ faceCount ];
		
		for ( int i = 0 ; i < faceCount ; i++ )
		{
			int v1 = is.readInt();
			int v2 = is.readInt();
			int v3 = is.readInt();
			int info = is.readInt();
			faces[ i ] = new Face( v1 , v2 , v3 , info );
		}

		readSubChunks( is );
	}

	/**
	 * Set all faces at once.
	 *
	 * @param	face	Array of new faces.
	 */
	public void set( Face[] faces )
	{
		this.faces = faces;
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
		if ( Ab3dsFile.DEBUG ) System.out.println( "  - Writing face list" );
		writeHeader( os );

		os.writeInt( faces.length );

		for ( int i = 0 ; i < faces.length ; i++ )
		{
			os.writeInt( faces[i].v1 );
			os.writeInt( faces[i].v2 );
			os.writeInt( faces[i].v3 );
			os.writeInt( faces[i].info );
		}

		writeSubChunks( os );
	}

}
