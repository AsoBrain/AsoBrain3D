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
 * This chunk simply holds a float value.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class FloatChunk
	extends DataChunk
{
	/**
	 * Float value of chunk.
	 */
	private float _value;

	/**
	 * Constructor of Chunk with ChunkID to be used when the Chunk is read from
	 * inputstream.
	 *
	 * @param   id  ID of the chunk.
	 */
	public FloatChunk( final int id )
	{
		super( id );
		_value = 0;
	}

	public long getSize()
	{
		return HEADER_SIZE + FLOAT_SIZE;
	}

	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );
		_value = is.readFloat();
	}

	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		writeHeader( os );
		os.writeFloat( _value );
	}
}
