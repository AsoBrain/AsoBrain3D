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
 * This chunk specifies a light.
 *
 * Chunk ID :
 * - OBJ_LIGHT		= 0x4600
 *
 * Parent chunk :
 * - EDIT_OBJECT	= 0x4000
 *
 * Possible sub chunks :
 * - LIT_SPOT		= 0x4610;
 * - LIT_OFF		= 0x4620;
 * - LIT_RAY		= 0x4627;
 * - LIT_CAST       = 0x4630;
 * - LIT_OUT_RANGE	= 0x465A;
 * - LIT_IN_RANGE	= 0x4659;
 * - LIT_MULTIPLIER = 0x465B;
 * - LIT_ROLL       = 0x4656;
 * - LIT_RAY_BIAS	= 0x4658;
 *
 * @author	Sjoerd Bouwman
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class Ab3dsLight extends HierarchyChunk 
{
	public static class State extends DataChunk
	{
		public boolean on;
		/**
		 * Constructor of Chunk with ChunkID to be used
		 * when the Chunk is read from inputstream.
		 *
		 * @param	id	the ID of the chunk.
		 */
		public State( int id )		{ super( id ); }
		/**
		 * Returns the size in bytes of the chunk.
		 *
		 * @return	the size of the chunk in bytes.
		 */
		public long getSize()		{ return HEADER_SIZE + BOOLEAN_SIZE; }
		/**
		 * Reads the chunk from the input stream.
		 * 
		 * @param	is	the stream to read from.
		 *
		 * @throws IOException when an io error occurred.
		 */
		public void read( Ab3dsInputStream is ) throws IOException
		{	readHeader( is ); on = is.readBoolean(); }
		/**
		 * Writes the chunk the output stream.
		 * 
		 * @param	os	the stream to write to.
		 *
		 * @throws IOException when an io error occurred.
		 */
		public void write( Ab3dsOutputStream os ) throws IOException
		{	writeHeader( os ); os.writeBoolean( on ); }
	}

	public static class SpotLight extends DataChunk
	{
		public float x,y,z,hotspot,falloff;
		/**
		 * Constructor of Chunk with ChunkID to be used
		 * when the Chunk is read from inputstream.
		 *
		 * @param	id	the ID of the chunk.
		 */
		public SpotLight( int id )	{ super( id ); if ( Ab3dsFile.DEBUG ) System.out.println( "  - Spotlight" ); }
		/**
		 * No-op constructor for generation purposes.
		 */
		public SpotLight()          { this( LIT_SPOT ); }
		/**
		 * Returns the size in bytes of the chunk.
		 *
		 * @return	the size of the chunk in bytes.
		 */
		public long getSize()		{ return HEADER_SIZE + 5 * FLOAT_SIZE; }

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
			x = is.readFloat();			y = is.readFloat();			z = is.readFloat();
			hotspot = is.readFloat();	falloff = is.readFloat();
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
			os.writeFloat( x );			os.writeFloat( y );			os.writeFloat( z );
			os.writeFloat( hotspot );	os.writeFloat( falloff );
		}
		public void set( float x , float y , float z , float hotspot , float falloff )
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.hotspot = hotspot;
			this.falloff = falloff;
		}
	}

	/**
	 * Position of light.
	 */
	private float _x;
	private float _y;
	private float _z;
	/**
	 * No-op constructor for generation purposes.
	 */
	public Ab3dsLight()
	{
		this( OBJ_LIGHT );
	}

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public Ab3dsLight( int id )
	{
		super(id);
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return	the size of the chunk in bytes.
	 */
	public long getSize() 
	{
		return super.getSize() + 3 * FLOAT_SIZE;
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

		_x = is.readFloat();
		_y = is.readFloat();
		_z = is.readFloat();
				
		readSubChunks( is );
	}

	/**
	 * Set parameters for light.
	 *
	 * @param	x	x-position of light.
	 * @param	y	y-position of light.
	 * @param	z	z-position of light.
	 */
	public void set( float x , float y , float z )
	{
		_x = x;
		_y = y;
		_z = z;
	}

	/**
	 * Writes the chunk the output stream.
	 * 
	 * @param	os	the stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void write( Ab3dsOutputStream os ) 
		throws java.io.IOException 
	{
		writeHeader( os );

		os.writeFloat( _x );
		os.writeFloat( _y );
		os.writeFloat( _z );

		writeSubChunks( os );
	}

}
