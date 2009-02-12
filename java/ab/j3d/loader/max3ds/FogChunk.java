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
 * Type:   {@link #FOG_FLAG}
 * Parent: {@link #EDIT_3DS}
 *
 * @noinspection JavaDoc
 */
class FogChunk
	extends Chunk
{
	float _nearPlane = 0.0f;

	float _nearDensity = 0.0f;

	float _farPlane = 0.0f;

	float _farDensity = 0.0f;

	boolean _useBackGround = false;

	Color _background = null;

	FogChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		super( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		_nearPlane = dataInput.readFloat();
		_nearDensity = dataInput.readFloat();
		_farPlane = dataInput.readFloat();
		_farDensity = dataInput.readFloat();

		super.processChunk( dataInput , chunkType , remainingChunkBytes - 4 * 4 );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case COLOR_FLOAT :
				_background = new Color( dataInput.readFloat() , dataInput.readFloat() , dataInput.readFloat() , 1.0f );
				break;

			case FOG_BACKGROUND :
				_useBackGround = true;
				break;

			default:
				skipFully( dataInput , remainingChunkBytes );
		}
	}
}
