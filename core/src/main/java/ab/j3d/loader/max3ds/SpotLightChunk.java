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
 * Type   : {@link #LIGHT_SPOTLIGHT}
 * Parent : {@link #LIGHT_OBJ}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class SpotLightChunk
	extends Chunk
{
	Vector3f _target;

	float _hotSpot;

	float _fallOff;

	boolean _shadowed;

	float _roll;

	short _shadowSize;

	float _bias;

	float _shadowFilter;

	float _shadowBias;

	boolean _seeCone;

	boolean _spotOvershoot;

	SpotLightChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		_target = new Vector3f( readFloat( in ), readFloat( in ), readFloat( in ) );
		_hotSpot = readFloat( in );
		_fallOff = readFloat( in );

		super.processChunk( in, chunkType, remainingChunkBytes - 5 * 4 );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case LIGHT_SPOT_ROLL :
				_roll = readFloat( in );
				break;

			case LIGHT_SPOT_SHADOWED :
				_shadowed = true;
				break;

			case LIGHT_SPOT_BIAS :
				_bias = readFloat( in );
				break;

			case LIGHT_LOC_SHADOW :
				_shadowBias   = readFloat( in );
				_shadowFilter = readFloat( in );
				_shadowSize   = readShort( in );
				break;

			case LIGHT_SEE_CONE :
				_seeCone = true;
				break;

			case LIGHT_SPOT_OVERSHOOT :
				_spotOvershoot = true;
				break;

			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}
	}
}
