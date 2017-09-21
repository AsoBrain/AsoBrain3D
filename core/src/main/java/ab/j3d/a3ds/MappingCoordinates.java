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
 * Texture mapping coordinates.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class MappingCoordinates
	extends DataChunk
{
	/**
	 * Texture X-coordinate for each vertex (a.k.a. U-coordinates).
	 */
	private float[] _mapX;

	/**
	 * Texture Y-coordinate for each vertex (a.k.a. V-coordinates).
	 */
	private float[] _mapY;

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public MappingCoordinates( final int id )
	{
		super( id );

		_mapX = null;
		_mapY = null;

		if ( Ab3dsFile.DEBUG )
			System.out.println( "Mapping coordinates" );
	}

	/**
	 * Returns the texture U-coordinate for the specified vertex.
	 *
	 * @param   vertexIndex     Vertex index.
	 *
	 * @return  U-coordinate for the vertex.
	 */
	public float getMapU( final int vertexIndex )
	{
		return _mapX[ vertexIndex ];
	}

	/**
	 * Returns the texture V-coordinate for the specified vertex.
	 *
	 * @param   vertexIndex     Vertex index.
	 *
	 * @return  V-coordinate for the vertex.
	 */
	public float getMapV( final int vertexIndex )
	{
		return _mapY[ vertexIndex ];
	}

	public long getSize()
	{
		return HEADER_SIZE + INT_SIZE + 2 * _mapX.length * FLOAT_SIZE;
	}

	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );

		final int count = is.readInt();

		_mapX = new float[ count ];
		_mapY = new float[ count ];

		for ( int i = 0 ; i < count ; i++ )
		{
			_mapX[ i ] = is.readFloat();
			_mapY[ i ] = is.readFloat();
		}

		if ( Ab3dsFile.DEBUG )
		{
			System.out.println( "Mapping" );
			for ( int i = 0 ; i < count ; i++ )
			{
				System.out.println( "Vertex : " + i + " x:" + _mapX[ i ] + " y:" + _mapY[ i ] );
			}
		}
	}

	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		writeHeader( os );

		os.writeInt( _mapX.length );

		for ( int i = 0 ; i < _mapX.length ; i++ )
		{
			os.writeFloat( _mapX[ i ] );
			os.writeFloat( _mapY[ i ] );
		}
	}

}
