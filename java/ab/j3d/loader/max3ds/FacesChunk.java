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
import java.util.ArrayList;
import java.util.List;

/**
 * Type   : {@link #FACES_ARRAY}
 * Parent : {@link #OBJ_TRIMESH}
 *
 * @noinspection JavaDoc
 */
class FacesChunk
	extends Chunk
{
	int _numberOfFaces = 0;

	int[][] _faces = null;

	int[] _smoothingGroups = null;

	final List<String> _materialNames = new ArrayList<String>();

	final List<int[]> _materialIndices = new ArrayList<int[]>();

	FacesChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		super( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		final int numberOfFaces = dataInput.readUnsignedShort();

		final int[][] faces = new int[ numberOfFaces ][];

		for ( int i = 0; i < numberOfFaces; i++ )
		{
			faces[ i ] = new int[] { dataInput.readUnsignedShort() , dataInput.readUnsignedShort() , dataInput.readUnsignedShort() };
			/*short flag =*/ dataInput.readShort();
		}

		_numberOfFaces = numberOfFaces;
		_faces = faces;

		super.processChunk( dataInput , chunkType , remainingChunkBytes - ( 2 + numberOfFaces * ( 3 * 2 + 2 ) ) );
	}


	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case SMOOTH_GROUP:
				readSmoothing( dataInput );
				break;

			case MESH_MAT_GROUP:
				readMeshMaterialGroup( dataInput );
				break;

			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}
	}

	private void readMeshMaterialGroup( final DataInput dataInput )
		throws IOException
	{
		final String name = readCString( dataInput );

		final int numberOfFaces = dataInput.readUnsignedShort();

		final int[] appliedFacesIndexes = new int[ numberOfFaces ];
		for ( int i = 0; i < numberOfFaces; i++ )
		{
			appliedFacesIndexes[ i ] = dataInput.readUnsignedShort();
		}

		_materialNames.add( name );
		_materialIndices.add( appliedFacesIndexes );
	}

	private void readSmoothing( final DataInput dataInput )
		throws IOException
	{
		final int numberOfFaces = _numberOfFaces;

		final int[] smoothingGroups = new int[ numberOfFaces ];
		for ( int i = 0 ; i < numberOfFaces ; i++ )
		{
			smoothingGroups[ i ] = dataInput.readInt();
		}

		_smoothingGroups = smoothingGroups;
	}
}
