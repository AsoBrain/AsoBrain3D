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
 * Type:   {@link #FOG_FLAG}
 * Parent: {@link #EDIT_3DS}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class FogChunk
	extends Chunk
{
	float _nearPlane;

	float _nearDensity;

	float _farPlane;

	float _farDensity;

	boolean _useBackGround;

	Color _background;

	FogChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		_nearPlane = readFloat( in );
		_nearDensity = readFloat( in );
		_farPlane = readFloat( in );
		_farDensity = readFloat( in );

		super.processChunk( in, chunkType, remainingChunkBytes - 4 * 4 );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case COLOR_FLOAT :
				_background = new Color( readFloat( in ), readFloat( in ), readFloat( in ), 1.0f );
				break;

			case FOG_BACKGROUND :
				_useBackGround = true;
				break;

			default:
				skipFully( in, remainingChunkBytes );
		}
	}
}
