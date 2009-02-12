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

import ab.j3d.Vector3f;

/**
 * Type   : {@link #CAMERA_FLAG}
 * Parent : {@link #NAMED_OBJECT}
 *
 * @noinspection JavaDoc
 */
class CameraChunk
	extends Chunk
{
	Vector3f _position = null;

	Vector3f _targetLocation = null;

	float _bankAngle = 0.0f;

	float _focus = 0.0f;

	float _near = 0.0f;

	float _far = 0.0f;

	CameraChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		super( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		_position = new Vector3f( dataInput.readFloat() , dataInput.readFloat() , dataInput.readFloat() );
		_targetLocation = new Vector3f( dataInput.readFloat() , dataInput.readFloat() , dataInput.readFloat() );
		_bankAngle = dataInput.readFloat();
		_focus = dataInput.readFloat();

		super.processChunk( dataInput , chunkType , remainingChunkBytes - 8 * 4 );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case CAMERA_RANGES:
				_near = dataInput.readFloat();
				_far = dataInput.readFloat();
				break;

			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}
	}
}
