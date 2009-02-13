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
 * Type   : {@link #LAYERED_FOG_OPT}
 * Parent : {@link #EDIT_3DS}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class LayeredFogChunk
	extends Chunk
{
	float _nearZ;

	float _farZ;

	float _density;

	int _type;

	Color _fogColor;

	LayeredFogChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		super( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		_nearZ = dataInput.readFloat();
		_farZ = dataInput.readFloat();
		_density = dataInput.readFloat();
		_type = dataInput.readInt();

		super.processChunk( dataInput , chunkType , remainingChunkBytes - 4 * 4 );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case COLOR_FLOAT:
				_fogColor = new Color( dataInput.readFloat() , dataInput.readFloat() , dataInput.readFloat() , 1.0f );
				break;

			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}
	}
}
