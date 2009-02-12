/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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
package ab.j3d.loader.max3ds;

import java.awt.Color;
import java.io.DataInput;
import java.io.IOException;

/**
 * Type   : {@link #COLOR_FLOAT} ,
 *          {@link #COLOR_BYTE} ,
 *          {@link #CLR_BYTE_GAMA} ,
 *          {@link #CLR_FLOAT_GAMA}.
 * Parent : any
 *
 * @noinspection JavaDoc
 */
class ColorChunk
	extends Chunk
{
	Color _color = null;

	Color _gamaColor = null;

	ColorChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		super( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case COLOR_BYTE :
				_color = new Color( dataInput.readUnsignedByte() , dataInput.readUnsignedByte() , dataInput.readUnsignedByte() );
				break;

			case COLOR_FLOAT :
				_color = new Color( dataInput.readFloat() , dataInput.readFloat() , dataInput.readFloat() , 1.0f );
				break;

			case CLR_BYTE_GAMA :
				_gamaColor = new Color( dataInput.readUnsignedByte() , dataInput.readUnsignedByte() , dataInput.readUnsignedByte() );
				break;

			case CLR_FLOAT_GAMA :
				_gamaColor = new Color( dataInput.readFloat() , dataInput.readFloat() , dataInput.readFloat() , 1.0f );
				break;

			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}
	}

	public Color getColor()
	{
		return ( _color == null ) ? _gamaColor : _color;
	}
}
