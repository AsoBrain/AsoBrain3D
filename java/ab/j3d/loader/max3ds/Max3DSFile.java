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

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

import com.numdata.oss.io.LittleEndianDataInputStream;

/**
 * Type   : {@link #MAIN_3DS}
 * Parent : none
 *
 * @noinspection JavaDoc
 */
public class Max3DSFile
	extends Chunk
{
	public EditableObjectChunk _editableObject = null;

	public KeyFramesChunk _keyframes = null;

	public Max3DSFile( final InputStream in )
		throws IOException
	{
		this( (DataInput)new LittleEndianDataInputStream( in ) );
	}

	public Max3DSFile( final DataInput dataInput )
		throws IOException
	{
		super( dataInput , dataInput.readUnsignedShort() , dataInput.readInt() - 6 );
	}

	protected void processChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		if ( chunkType != MAIN_3DS )
		{
			throw new IOException( "Header doesn't match 0x4D4D; Header=" + Integer.toHexString( chunkType ) );
		}

		super.processChunk( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case TDS_VERSION :
				/*int version =*/dataInput.readInt();
				break;

			case EDIT_3DS :
				_editableObject = new EditableObjectChunk( dataInput , chunkType , remainingChunkBytes );
				break;

			case KEYFRAMES :
				_keyframes = new KeyFramesChunk( dataInput , chunkType , remainingChunkBytes );
				break;

			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}
	}
}