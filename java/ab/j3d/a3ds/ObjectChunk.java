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
 * This chunk specifies an object (like Mesh, Light, Camera)
 * <pre>
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
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class ObjectChunk extends HierarchyChunk
{
	/**
	 * Name of object.
	 */
	private String name = null;
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public ObjectChunk( final int id )
	{
		super( id );
		if ( Ab3dsFile.DEBUG ) System.out.println( "Found object: " + getHex( id ) );
	}

	/**
	 * Constructs an object with specified name.
	 *
	 * @param   name	the name of the object.
	 */
	public ObjectChunk( final String name )
	{
		super( EDIT_OBJECT );
		this.name = name;
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return  the size of the chunk in bytes.
	 */
	public long getSize()
	{
		return super.getSize() + STRING_SIZE( name );
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

		name = is.readString();
		if ( Ab3dsFile.DEBUG ) System.out.println( "  Name = " + name );

//		is.skip( _chunkEnd - is.getPointer() );

		readSubChunks( is );
	}

	/**
	 * Returns a String representation of this chunk.
	 *
	 * @return  this chunk as a string.
	 */
	public String toString()
	{
		return super.toString() + " " + name;
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
		if ( Ab3dsFile.DEBUG ) System.out.println( "Writing object : 4000" );
		if ( Ab3dsFile.DEBUG ) System.out.println( "  Name = " + name );
		writeHeader( os );

		os.writeString( name );

		writeSubChunks( os );
	}

}
