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

/**
 * Type   : {@link #NAMED_OBJECT}
 * Parent : {@link #EDIT_3DS}
 *
 * @noinspection JavaDoc
 */
class NamedObjectChunk
	extends Chunk
{
	String name = null;

	Chunk _content = null;

	NamedObjectChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		super( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		name = readCString( dataInput );
		super.processChunk( dataInput , chunkType , remainingChunkBytes - ( name.length() + 1 ) );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		Chunk content = _content;

		switch ( chunkType )
		{
			case OBJ_TRIMESH:
				if ( content != null )
					throw new IOException( "Already have content!" );

				content = new TriangleMeshChunk( dataInput , chunkType , remainingChunkBytes );
				break;

			case CAMERA_FLAG:
				if ( content != null )
					throw new IOException( "Already have content!" );

				content = new CameraChunk( dataInput , chunkType , remainingChunkBytes );
				break;

			case LIGHT_OBJ:
				if ( content != null )
					throw new IOException( "Already have content!" );

				content = new LightChunk( dataInput , chunkType , remainingChunkBytes );
				break;

			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}

		_content = content;
	}
}
