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
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class StandardMapping extends DataChunk
{
	public static final int PLANNAR = 0;
	public static final int CYLINDRICAL = 1;
	public static final int SPHERICAL = 2;

	private int 	_type;
	private float	_tilingX;
	private float	_tilingY;
	private float	_iconX;
	private float	_iconY;
	private float	_iconZ;
	private float[][] _matrix;
	private float	_scaling;
	private float	_planIconW;
	private float	_planIconH;
	private float	_cylIconH;
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public StandardMapping( final int id )
	{
		super( id );
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return  the size of the chunk in bytes.
	 */
	public long getSize()
	{
		return HEADER_SIZE + INT_SIZE + 9 * FLOAT_SIZE + 12 * FLOAT_SIZE;
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

		if ( Ab3dsFile.DEBUG )
			System.out.println( "Default mapping, size = " + _chunkSize );
		_type = is.readInt();
		_tilingX = is.readFloat();
		_tilingY = is.readFloat();
		_iconX = is.readFloat();
		_iconY = is.readFloat();
		_iconZ = is.readFloat();
		_matrix = new float[4][3];
		for ( int i = 0 ; i < 4 ; i++ )
		{
			for ( int j = 0 ; j < 4 ; j++ )
			{
				_matrix[i][j] = is.readFloat();
			}
		}
		_scaling = is.readFloat();
		_planIconW = is.readFloat();
		_planIconH = is.readFloat();
		_cylIconH = is.readFloat();
	}

	/**
	 * Writes the chunk the output stream.
	 *
	 * @param   os	the stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void write( final Ab3dsOutputStream os )
		throws java.io.IOException
	{
		writeHeader( os );

		os.writeInt( _type );
		os.writeFloat( _tilingX );
		os.writeFloat( _tilingY );
		os.writeFloat( _iconX );
		os.writeFloat( _iconY );
		os.writeFloat( _iconZ );
		for ( int i = 0 ; i < 4 ; i++ )
		{
			for ( int j = 0 ; j < 4 ; j++ )
			{
				os.writeFloat( _matrix[i][j] );
			}
		}
		os.writeFloat( _scaling );
		os.writeFloat( _planIconW );
		os.writeFloat( _planIconH );
		os.writeFloat( _cylIconH );
	}

}
