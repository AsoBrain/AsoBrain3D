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

/**
 * Type   : {@link #FACES_ARRAY}
 * Parent : {@link #OBJ_TRIMESH}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class FacesChunk
	extends Chunk
{
	int _numberOfFaces;

	int[][] _faces;

	int[] _smoothingGroups;

	List<String> _materialNames;

	List<int[]> _materialIndices;

	FacesChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		final int numberOfFaces = readUnsignedShort( in );

		final int[][] faces = new int[ numberOfFaces ][];

		for ( int i = 0; i < numberOfFaces; i++ )
		{
			faces[ i ] = new int[] { readUnsignedShort( in ), readUnsignedShort( in ), readUnsignedShort( in ) };
			/*short flag =*/ readShort( in );
		}

		_materialNames = new ArrayList<String>();
		_materialIndices = new ArrayList<int[]>();
		_numberOfFaces = numberOfFaces;
		_faces = faces;

		super.processChunk( in, chunkType, remainingChunkBytes - ( 2 + numberOfFaces * ( 3 * 2 + 2 ) ) );
	}


	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case SMOOTH_GROUP:
				readSmoothing( in );
				break;

			case MESH_MAT_GROUP:
				readMeshMaterialGroup( in );
				break;

			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}
	}

	private void readMeshMaterialGroup( final InputStream in )
		throws IOException
	{
		final String name = readCString( in );

		final int numberOfFaces = readUnsignedShort( in );

		final int[] appliedFacesIndexes = new int[ numberOfFaces ];
		for ( int i = 0; i < numberOfFaces; i++ )
		{
			appliedFacesIndexes[ i ] = readUnsignedShort( in );
		}

		_materialNames.add( name );
		_materialIndices.add( appliedFacesIndexes );
	}

	private void readSmoothing( final InputStream in )
		throws IOException
	{
		final int numberOfFaces = _numberOfFaces;

		final int[] smoothingGroups = new int[ numberOfFaces ];
		for ( int i = 0; i < numberOfFaces; i++ )
		{
			smoothingGroups[ i ] = readInt( in );
		}

		_smoothingGroups = smoothingGroups;
	}
}
