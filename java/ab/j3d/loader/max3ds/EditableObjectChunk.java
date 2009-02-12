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
import java.util.HashMap;
import java.util.Map;

import ab.j3d.Vector3f;

/**
 * Type   : {@link #EDIT_3DS}
 * Parent : {@link #MAIN_3DS}
 *
 * @noinspection JavaDoc
 */
class EditableObjectChunk
	extends Chunk
{
	final Map<String,MaterialChunk> _materials = new HashMap<String,MaterialChunk>();

	final Map<String,TriangleMeshChunk> _meshes = new HashMap<String,TriangleMeshChunk>();

	final Map<String,LightChunk> _lights = new HashMap<String,LightChunk>();

	final Map<String,CameraChunk> _cameras = new HashMap<String,CameraChunk>();

	float _masterScale = 0.0f;

	float _shadowMapRange = 0.0f;

	float _rayTraceBias = 0.0f;

	Vector3f _oConstPlanes = null;

	Color _ambientColor = null;

	Color _backgroundColor = null;

	String _backgroundBigMap = null;

	boolean _useBackgroundColor = false;

	float _shadowBias = 0.0f;

	short _shadowMapSize = (short)0;

	LayeredFogChunk _fogOptions = null;

	FogChunk _fog = null;

	DistanceQueueChunk _distanceQueue = null;

	EditableObjectChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		super( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		final ColorChunk colorChunk;

		switch ( chunkType )
		{
			case MASTER_SCALE :
				_masterScale = dataInput.readFloat();
				break;

			case SHADOW_MAP_RANGE :
				_shadowMapRange = dataInput.readFloat();
				break;

			case RAYTRACE_BIAS :
				_rayTraceBias = dataInput.readFloat();
				break;

			case O_CONSTS :
				_oConstPlanes = new Vector3f( dataInput.readFloat() , dataInput.readFloat() , dataInput.readFloat() );
				break;

			case GEN_AMB_COLOR :
				colorChunk = new ColorChunk( dataInput , chunkType , remainingChunkBytes );
				_ambientColor = colorChunk.getColor();
				break;

			case BACKGRD_COLOR :
				colorChunk = new ColorChunk( dataInput , chunkType , remainingChunkBytes );
				_backgroundColor = colorChunk.getColor();
				break;

			case BACKGRD_BITMAP :
				_backgroundBigMap = readCString( dataInput );
				break;

			case USE_BCK_COLOR :
				_useBackgroundColor = true;
				break;

			case FOG_FLAG :
				_fog = new FogChunk( dataInput , chunkType , remainingChunkBytes );
				break;

			case SHADOW_BIAS :
				_shadowBias = dataInput.readFloat();
				break;

			case SHADOW_MAP_SIZE :
				_shadowMapSize = dataInput.readShort();
				break;

			case LAYERED_FOG_OPT :
				_fogOptions = new LayeredFogChunk( dataInput , chunkType , remainingChunkBytes );
				break;

			case DISTANCE_QUEUE :
				_distanceQueue = new DistanceQueueChunk( dataInput , chunkType , remainingChunkBytes );
				break;

			case NAMED_OBJECT :
				final NamedObjectChunk namedObjectChunk = new NamedObjectChunk( dataInput , chunkType , remainingChunkBytes );

				final Chunk content = namedObjectChunk._content;
				if ( content instanceof TriangleMeshChunk )
				{
					_meshes.put( namedObjectChunk.name , (TriangleMeshChunk)content );
				}
				else if ( content instanceof LightChunk )
				{
					_lights.put( namedObjectChunk.name , (LightChunk)content );
				}
				else if ( content instanceof CameraChunk )
				{
					_cameras.put( namedObjectChunk.name , (CameraChunk)content );
				}
				else
				{
					throw new IOException( "Unrecognied named object '" + namedObjectChunk.name + " ' content: " + content );
				}
				break;

			case MAT_BLOCK :
				final MaterialChunk material = new MaterialChunk( dataInput , chunkType , remainingChunkBytes );
				_materials.put( material._name , material );
				break;

			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}
	}
}
