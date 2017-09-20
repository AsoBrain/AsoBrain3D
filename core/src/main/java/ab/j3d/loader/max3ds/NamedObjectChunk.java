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

/**
 * Type   : {@link #NAMED_OBJECT}
 * Parent : {@link #EDIT_3DS}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class NamedObjectChunk
	extends Chunk
{
	String name;

	Chunk _content;

	NamedObjectChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		name = readCString( in );
		super.processChunk( in, chunkType, remainingChunkBytes - ( name.length() + 1 ) );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		Chunk content = _content;

		switch ( chunkType )
		{
			case OBJ_TRIMESH:
				if ( content != null )
				{
					throw new IOException( "Already have content!" );
				}

				content = new TriangleMeshChunk( in, chunkType, remainingChunkBytes );
				break;

			case CAMERA_FLAG:
				if ( content != null )
				{
					throw new IOException( "Already have content!" );
				}

				content = new CameraChunk( in, chunkType, remainingChunkBytes );
				break;

			case LIGHT_OBJ:
				if ( content != null )
				{
					throw new IOException( "Already have content!" );
				}

				content = new LightChunk( in, chunkType, remainingChunkBytes );
				break;

			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}

		_content = content;
	}
}
