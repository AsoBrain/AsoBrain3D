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

	TextureMapChunk( final InputStream in, final int chunkType, final int chunkSize )
		throws IOException
	{
		super( in, chunkType, chunkSize );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case MAT_TEXNAME:
				_name = readCString( in, remainingChunkBytes );
				break;

			case MAT_TEX_FLAGS:
				_flags = readUnsignedShort( in );
				break;

			case MAT_TEX_BLUR:
				_blur = readFloat( in );
				break;

			case MAT_TEX_BUMP_PER:
				_bumpPercentage = (float)readShort( in ) / 100.0f;
				break;

			case TEXTURE_V_SCALE:
				_vScale = readFloat( in );
				break;

			case TEXTURE_U_SCALE:
				_uScale = readFloat( in );
				break;

			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}
	}
}
