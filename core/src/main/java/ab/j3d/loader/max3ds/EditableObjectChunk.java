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

import ab.j3d.*;

/**
 * Type   : {@link #EDIT_3DS}
 * Parent : {@link #MAIN_3DS}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class EditableObjectChunk
	extends Chunk
{
	Map<String,MaterialChunk> _materials;

	Map<String,TriangleMeshChunk> _meshes;

	Map<String,LightChunk> _lights;

	Map<String,CameraChunk> _cameras;

	float _masterScale;

	float _shadowMapRange;

	float _rayTraceBias;

	Vector3f _oConstPlanes;

	Color4 _ambientColor;

	Color4 _backgroundColor;

	String _backgroundBigMap;

	boolean _useBackgroundColor;

	float _shadowBias;

	short _shadowMapSize;

	LayeredFogChunk _fogOptions;

	FogChunk _fog;

	DistanceQueueChunk _distanceQueue;

	EditableObjectChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		_materials = new HashMap<String,MaterialChunk>();
		_meshes = new HashMap<String,TriangleMeshChunk>();
		_lights = new HashMap<String,LightChunk>();
		_cameras = new HashMap<String,CameraChunk>();

		super.processChunk( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		final ColorChunk colorChunk;

		switch ( chunkType )
		{
			case MASTER_SCALE :
				_masterScale = readFloat( in );
				break;

			case SHADOW_MAP_RANGE :
				_shadowMapRange = readFloat( in );
				break;

			case RAYTRACE_BIAS :
				_rayTraceBias = readFloat( in );
				break;

			case O_CONSTS :
				_oConstPlanes = new Vector3f( readFloat( in ), readFloat( in ), readFloat( in ) );
				break;

			case GEN_AMB_COLOR :
				colorChunk = new ColorChunk( in, chunkType, remainingChunkBytes );
				_ambientColor = colorChunk.getColor();
				break;

			case BACKGRD_COLOR :
				colorChunk = new ColorChunk( in, chunkType, remainingChunkBytes );
				_backgroundColor = colorChunk.getColor();
				break;

			case BACKGRD_BITMAP :
				_backgroundBigMap = readCString( in );
				break;

			case USE_BCK_COLOR :
				_useBackgroundColor = true;
				break;

			case FOG_FLAG :
				_fog = new FogChunk( in, chunkType, remainingChunkBytes );
				break;

			case SHADOW_BIAS :
				_shadowBias = readFloat( in );
				break;

			case SHADOW_MAP_SIZE :
				_shadowMapSize = readShort( in );
				break;

			case LAYERED_FOG_OPT :
				_fogOptions = new LayeredFogChunk( in, chunkType, remainingChunkBytes );
				break;

			case DISTANCE_QUEUE :
				_distanceQueue = new DistanceQueueChunk( in, chunkType, remainingChunkBytes );
				break;

			case NAMED_OBJECT :
				final NamedObjectChunk namedObjectChunk = new NamedObjectChunk( in, chunkType, remainingChunkBytes );

				final Chunk content = namedObjectChunk._content;
				if ( content instanceof TriangleMeshChunk )
				{
					_meshes.put( namedObjectChunk.name, (TriangleMeshChunk)content );
				}
				else if ( content instanceof LightChunk )
				{
					_lights.put( namedObjectChunk.name, (LightChunk)content );
				}
				else if ( content instanceof CameraChunk )
				{
					_cameras.put( namedObjectChunk.name, (CameraChunk)content );
				}
				else
				{
					throw new IOException( "Unrecognized named object '" + namedObjectChunk.name + " ' content: " + content );
				}
				break;

			case MAT_BLOCK :
				final MaterialChunk material = new MaterialChunk( in, chunkType, remainingChunkBytes );
				_materials.put( material._name, material );
				break;

			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}
	}
}
