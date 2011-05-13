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
 * This chunk is the base class for any chunk that does not contain sub chunks.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public abstract class DataChunk
	extends Chunk
{
	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	protected DataChunk( final int id )
	{
		super( id );
	}

	/**
	 * Reads color from stream. There may be multiple nested color chunks.
	 *
	 * @param   is      Stream to read from.
	 * @param   size    Bytes to be read.
	 *
	 * @return  Ab3dsRGB chunk that was read.
	 *
	 * @throws IOException when reading failed.
	 */
	protected Ab3dsRGB readColor( final Ab3dsInputStream is, final long size )
		throws IOException
	{
		final long start = is.getPointer();

		Ab3dsRGB result = null;

		do
		{
			final Ab3dsRGB rgb = (Ab3dsRGB)createChunk( is.readInt(), this );
			rgb.read( is );

			if ( ( result == null ) || ( !result.isGamma() && rgb.isGamma() ) )
			{
				result = rgb;
			}
		}
		while ( is.getPointer() - start < size );

		return result;
	}

	/**
	 * Reads double byte from stream
	 *
	 * @param   is      Stream to read from.
	 *
	 * @return  double byte that was read.
	 *
	 * @throws  IOException when reading failed.
	 */
	protected static int readDoubleByte( final Ab3dsInputStream is )
		throws IOException
	{
		is.readInt();
		is.readLong();
		return is.readInt();
	}

	/**
	 * Writes color to stream
	 *
	 * @param   os      Stream to write to.
	 * @param   id      ID of the color chunk.
	 * @param   chunk   Color chunk to write.
	 *
	 * @throws IOException when writing failed.
	 */
	protected static void writeColor( final Ab3dsOutputStream os , final int id , final Ab3dsRGB chunk )
		throws IOException
	{
		os.writeInt( id );
		os.writeLong( HEADER_SIZE + chunk.getSize() );
		chunk.write( os );
	}

	/**
	 * Writes a double byte chunk to stream
	 *
	 * @param   os      Stream to write to.
	 * @param   id      ID of the color chunk.
	 * @param   value   Double byte to write.
	 *
	 * @throws IOException when writing failed.
	 */
	protected static void writeDoubleByte( final Ab3dsOutputStream os , final int id , final int value )
		throws IOException
	{
		os.writeInt( id );
		os.writeLong( 2*HEADER_SIZE + INT_SIZE );
		os.writeInt( DOUBLE_BYTE );
		os.writeLong( HEADER_SIZE + INT_SIZE );
		os.writeInt( value );
	}

}
