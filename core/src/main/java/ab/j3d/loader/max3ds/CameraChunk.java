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

import java.io.*;

import ab.j3d.*;

/**
 * Type   : {@link #CAMERA_FLAG}
 * Parent : {@link #NAMED_OBJECT}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class CameraChunk
	extends Chunk
{
	Vector3f _position;

	Vector3f _targetLocation;

	float _bankAngle;

	float _focus;

	float _near;

	float _far;


	CameraChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		_position = new Vector3f( readFloat( in ), readFloat( in ), readFloat( in ) );
		_targetLocation = new Vector3f( readFloat( in ), readFloat( in ), readFloat( in ) );
		_bankAngle = readFloat( in );
		_focus = readFloat( in );

		super.processChunk( in, chunkType, remainingChunkBytes - 8 * 4 );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case CAMERA_RANGES:
				_near = readFloat( in );
				_far = readFloat( in );
				break;

			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}
	}
}
