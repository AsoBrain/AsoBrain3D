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
import java.util.*;

/**
 * Type:   {@link #KEYFRAMES}
 * Parent: {@link #MAIN_3DS}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class KeyFramesChunk
	extends Chunk
{
	int _animationLen = 0;

	int _begin = 0;

	int _end = 0;

	Map<String,KeyFrameChunk> _objectKeyframes;

	List<KeyFrameChunk> _cameraKeyframes;

	List<KeyFrameChunk> _lightKeyframes;

	KeyFramesChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		_objectKeyframes = new HashMap<String,KeyFrameChunk>();
		_cameraKeyframes = new ArrayList<KeyFrameChunk>();
		_lightKeyframes = new ArrayList<KeyFrameChunk>();

		super.processChunk( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case KEY_HEADER :
				readShort( in ); /* revision */
				readCString( in ); /* filename */
				_animationLen = readInt( in );
				break;

			case KEY_SEGMENT :
				_begin = readInt( in );
				_end = readInt( in );
				break;

			case KEY_CURTIME :
				readInt( in ); /* current frame */
				break;

			case KEY_OBJECT :
				final KeyFrameChunk keyFrame = new KeyFrameChunk( in, chunkType, remainingChunkBytes );
				_objectKeyframes.put( keyFrame._name, keyFrame );
				break;

			case KEY_CAM_TARGET :
			case KEY_CAMERA_OBJECT :
				_cameraKeyframes.add( new KeyFrameChunk( in, chunkType, remainingChunkBytes ) );
				break;

			case KEY_OMNI_LI_INFO :
			case KEY_AMB_LI_INFO :
			case KEY_SPOT_TARGET :
			case KEY_SPOT_OBJECT :
				_lightKeyframes.add( new KeyFrameChunk( in, chunkType, remainingChunkBytes ) );
				break;

			case KEY_VIEWPORT :
			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}
	}
}
