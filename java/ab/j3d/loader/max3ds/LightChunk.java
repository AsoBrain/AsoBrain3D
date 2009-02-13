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

import java.awt.Color;
import java.io.DataInput;
import java.io.IOException;

import ab.j3d.Vector3f;

/**
 * Type   : {@link #LIGHT_OBJ}
 * Parent : {@link #NAMED_OBJECT}
 *
 * @noinspection JavaDoc
 */
class LightChunk
	extends Chunk
{
	Vector3f _location;

	Color _lightColor;

	float _outerRange;

	float _innerRange;

	float _multiplier;

	SpotLightChunk _spotLightChunk;

	boolean _attenuateOn;

	LightChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		super( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		_location = new Vector3f( dataInput.readFloat() , dataInput.readFloat() , dataInput.readFloat() );

		super.processChunk( dataInput , chunkType , remainingChunkBytes - 3 * 4 );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case COLOR_FLOAT:
				_lightColor = new Color( dataInput.readFloat() , dataInput.readFloat() , dataInput.readFloat() , 1.0f );
				break;

			case LIGHT_OUT_RANGE:
				_outerRange = dataInput.readFloat();
				break;

			case LIGHT_IN_RANGE:
				_innerRange = dataInput.readFloat();
				break;

			case LIGHT_MULTIPLIER:
				_multiplier = dataInput.readFloat();
				break;

			case LIGHT_SPOTLIGHT:
				_spotLightChunk = new SpotLightChunk( dataInput , chunkType , remainingChunkBytes );
				break;

			case LIGHT_ATTENU_ON:
				_attenuateOn = true;
				break;

			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}
	}
}
