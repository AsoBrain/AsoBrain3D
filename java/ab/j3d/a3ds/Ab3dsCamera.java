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
 * This chunk specifies a camera.
 *
 * Chunk ID :
 * - OBJ_CAMERA		= 0x4700
 *
 * Parent chunk :
 * - EDIT_OBJECT	= 0x4000
 *
 * @author	Sjoerd Bouwman
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class Ab3dsCamera extends HierarchyChunk 
{
	/**
	 * Position of camera.
	 */
	private float _x;
	private float _y;
	private float _z;

	/**
	 * Target position of camera.
	 */
	private float _tx;
	private float _ty;
	private float _tz;

	/**
	 * Tilting of camera.
	 */
	private float _bank;

	/**
	 * Lens specification in mm.
	 */
	private float _lens;
	/**
	 * No-op constructor for generation purposes.
	 */
	public Ab3dsCamera()
	{
		this( OBJ_CAMERA );
	}

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param	id	the ID of the chunk.
	 */
	public Ab3dsCamera( int id ) 
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
		return super.getSize() + 8 * FLOAT_SIZE;
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
			
		_tx = is.readFloat();
		_ty = is.readFloat();
		_tz = is.readFloat();

		_bank = is.readFloat();
		_lens = is.readFloat();

		readSubChunks( is );
	}

	/**
	 * Parameters for the camera.
	 *
	 * @param	x		x-position of camera
	 * @param	y		y-position of camera
	 * @param	z		z-position of camera
	 * @param	tx		target x-position of camera
	 * @param	ty		target y-position of camera
	 * @param	tz		target z-position of camera
	 * @param	bank	camera angle (tilt).
	 * @param	lens	lens specification in mm.
	 */
	public void set( float x , float y , float z , float tx , float ty , float tz , float bank , float lens )
	{
		_x = x;
		_y = y;
		_z = z;
		_tx = tx;
		_ty = ty;
		_tz = tz;
		_bank = bank;
		_lens = lens;
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

		os.writeFloat( _tx );
		os.writeFloat( _ty );
		os.writeFloat( _tz );
		
		os.writeFloat( _bank );
		os.writeFloat( _lens );

		writeSubChunks( os );
	}

}
