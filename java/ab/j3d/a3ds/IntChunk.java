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
 * This chunk simply holds an integer value.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class IntChunk
	extends DataChunk
{
	/**
	 * Integer value of chunk.
	 */
	public int _value;

	/**
	 * Constructor of Chunk with ChunkID to be used when the Chunk is read from
	 * inputstream.
	 *
	 * @param id ID of the chunk.
	 */
	public IntChunk( final int id )
	{
		this( id , 0 );
	}

	/**
	 * DoubleByteChunk constructor comment.
	 *
	 * @param id int
	 */
	public IntChunk( final int id , final int value )
	{
		super( id );
		_value = value;
	}

	/**
	 * Get integer value of chunk.
	 *
	 * @return  Integer value of chunk.
	 */
	public int getValue()
	{
		return _value;
	}

	/**
	 * Set integer value of chunk.
	 *
	 * @param   value   Integer value of chunk.
	 */
	public void setValue( final int value )
	{
		_value = value;
	}

	public long getSize()
	{
		return HEADER_SIZE + INT_SIZE;
	}

	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );
		setValue( is.readInt() );
	}

	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		writeHeader( os );
		os.writeInt( getValue() );
	}
}
