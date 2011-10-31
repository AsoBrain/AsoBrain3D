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
 * Type   : {@link #MAT_BLOCK}
 * Parent : {@link #EDIT_3DS}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class MaterialChunk
	extends Chunk
{
	String _name;

	Material _material;

	TextureMapChunk _textureMapOne;

	TextureMapChunk _textureMapTwo;

	TextureMapChunk _reflectionMap;

	TextureMapChunk _bumpMap;

	MaterialChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		_material = new Material();

		super.processChunk( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		final Material material = _material;

		final ColorChunk colorChunk;

		switch ( chunkType )
		{
			case MAT_NAME:
				_name = readCString( in );
				break;

			case MAT_AMB_COLOR:
				colorChunk = new ColorChunk( in, chunkType, remainingChunkBytes );
				material.setAmbientColor( colorChunk.getColor() );
				break;

			case MAT_DIF_COLOR:
				colorChunk = new ColorChunk( in, chunkType, remainingChunkBytes );
				material.setDiffuseColor( colorChunk.getColor() );
				break;

			case MAT_SPEC_CLR:
				colorChunk = new ColorChunk( in, chunkType, remainingChunkBytes );
				material.setSpecularColor( colorChunk.getColor() );
				break;

			case MAT_SHINE:
				material.shininess = (int)( 128.0f * new PercentageChunk( in, chunkType, remainingChunkBytes )._percentage );
				break;

			case MAT_ALPHA:
				material.diffuseColorAlpha = 1.0f - new PercentageChunk( in, chunkType, remainingChunkBytes )._percentage;
				break;

			case IN_TRANC_FLAG:
				break;

			case TEXMAP_ONE:
				_textureMapOne = new TextureMapChunk( in, chunkType, remainingChunkBytes );
				break;

			case MAT_TEX_BUMPMAP:
				_bumpMap = new TextureMapChunk( in, chunkType, remainingChunkBytes );
				break;

			case MAT_SOFTEN:
				break;

			case MAT_REFL_BLUR:
				break;

			case MAT_WIRE_ABS:
				break;

			case MAT_REFLECT_MAP:
				_reflectionMap = new TextureMapChunk( in, chunkType, remainingChunkBytes );
				break;

			case MAT_TWO_SIDED:
				/* how can we set this in the material? */
				break;

			case MAT_FALLOFF:
				break;

			case MAT_WIREFRAME_ON:
				break;

			case MAT_TEX2MAP:
				_textureMapTwo = new TextureMapChunk( in, chunkType, remainingChunkBytes );
				break;

			default:
				skipFully( in, remainingChunkBytes );
		}
	}
}
