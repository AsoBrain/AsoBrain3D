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

import ab.j3d.Vector3D;

/**
 * This chunk specifies a list of vertices for a mesh.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class VertexList
	extends DataChunk
{
	private Vector3D[] _vertices;

	/**
	 * Default constructor for generation purposes.
	 */
	public VertexList()
	{
		this( TRI_VERTEXLIST );
	}

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public VertexList( final int id )
	{
		super(id);
		if ( Ab3dsFile.DEBUG ) System.out.println( "  - Reading vertext list" );
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return  Size of the chunk in bytes.
	 */
	public long getSize()
	{
		return HEADER_SIZE + 2 + _vertices.length * 3 * FLOAT_SIZE;
	}

	/**
	 * Get vertex at specified index.
	 *
	 * @param   i   Index of the vertex to get.
	 *
	 * @return  Vertex at index.
	 */
	public Vector3D getVertex( final int i )
	{
		return _vertices[i];
	}

	/**
	 * Get total amount of vertices in list.
	 *
	 * @return  Vertex count.
	 */
	public int getVertexCount()
	{
		return _vertices.length;
	}

	/**
	 * Reads the chunk from the input stream.
	 *
	 * @param   is  Stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );

		final int vertexCount = is.readInt();

		_vertices = new Vector3D[ vertexCount ];

		for ( int i = 0 ; i < vertexCount ; i++ )
		{
			final float x = is.readFloat();
			final float y = is.readFloat();
			final float z = is.readFloat();
			_vertices[i] = Vector3D.INIT.set( x , y , z );
		}
	}

	/**
	 * Set all vertices at once.
	 *
	 * @param   vertices    Array of new vertices.
	 */
	public void set( final Vector3D[] vertices )
	{
		_vertices = vertices;
	}

	/**
	 * Writes the chunk the output stream.
	 *
	 * @param   os  Stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		if ( Ab3dsFile.DEBUG )
			System.out.println( "  - Writing vertex list" );

		writeHeader( os );

		os.writeInt( _vertices.length );

		for ( int i = 0 ; i < _vertices.length ; i++ )
		{
			os.writeFloat( _vertices[i].x );
			os.writeFloat( _vertices[i].y );
			os.writeFloat( _vertices[i].z );
		}
	}
}
