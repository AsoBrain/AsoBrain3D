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
 * Type   : {@link #TEXMAP_ONE},
 *          {@link #MAT_TEX_BUMPMAP},
 *          {@link #MAT_REFLECT_MAP},
 *          {@link #MAT_TEX2MAP}
 * Parent : {@link #MAT_BLOCK}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class TextureMapChunk
	extends Chunk
{
	String _name;

	int _flags;

	float _blur;

	float _bumpPercentage;

	float _vScale;

	float _uScale;

	TextureMapChunk( final DataInput dataInput , final int chunkType , final int chunkSize )
		throws IOException
	{
		super( dataInput , chunkType , chunkSize );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case MAT_TEXNAME:
				_name = readCString( dataInput , remainingChunkBytes );
				break;

			case MAT_TEX_FLAGS:
				_flags = dataInput.readUnsignedShort();
				break;

			case MAT_TEX_BLUR:
				_blur = dataInput.readFloat();
				break;

			case MAT_TEX_BUMP_PER:
				_bumpPercentage = (float)dataInput.readShort() / 100.0f;
				break;

			case TEXTURE_V_SCALE:
				_vScale = dataInput.readFloat();
				break;

			case TEXTURE_U_SCALE:
				_uScale = dataInput.readFloat();
				break;

			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}
	}
}
