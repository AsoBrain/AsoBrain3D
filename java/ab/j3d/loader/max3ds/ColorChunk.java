/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import java.awt.*;
import java.io.*;

/**
 * Type   : {@link #COLOR_FLOAT},
 *          {@link #COLOR_BYTE},
 *          {@link #CLR_BYTE_GAMA},
 *          {@link #CLR_FLOAT_GAMA}.
 * Parent : any
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class ColorChunk
	extends Chunk
{
	Color _color;

	Color _gamaColor;

	ColorChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
//		System.out.println( "  Read: color=" + _color + ", gamaColor=" + _gamaColor );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
//		System.out.println( "ColorChunk.processChildChunk( " + chunkType + " )" );
		switch ( chunkType )
		{
			case COLOR_BYTE :
				_color = new Color( readUnsignedByte( in ), readUnsignedByte( in ), readUnsignedByte( in ) );
				break;

			case COLOR_FLOAT :
				_color = new Color( readFloat( in ), readFloat( in ), readFloat( in ), 1.0f );
				break;

			case CLR_BYTE_GAMA :
				_gamaColor = new Color( readUnsignedByte( in ), readUnsignedByte( in ), readUnsignedByte( in ) );
				break;

			case CLR_FLOAT_GAMA :
				_gamaColor = new Color( readFloat( in ), readFloat( in ), readFloat( in ), 1.0f );
				break;

			default : // Ignore unknown chunks
				System.out.println( "Skipped unknown color chunk 0x" + Integer.toHexString( chunkType ) );
				skipFully( in, remainingChunkBytes );
		}
	}

	public Color getColor()
	{
		return ( _color == null ) ? _gamaColor : _color;
	}
}
