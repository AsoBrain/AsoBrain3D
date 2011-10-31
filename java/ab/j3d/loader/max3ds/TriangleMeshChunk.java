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
 * Type   : {@link #OBJ_TRIMESH}.
 * Parent : {@link #NAMED_OBJECT}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class TriangleMeshChunk
	extends Chunk
{
	List<Vector3D> _vertices;

	Vector2f[] _textureCoordinates;

	byte _color;

	FacesChunk _faces;

	Matrix3D _transform;

	TriangleMeshChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case VERTEX_LIST:
				readVertexCoordinates( in );
				break;

			case TEXT_COORDS:
				readTextureCoordinates( in );
				break;

			case COORD_SYS:
				readCoordinateSystem( in );
				break;

			case FACES_ARRAY:
				_faces = new FacesChunk( in, chunkType, remainingChunkBytes );
				break;

			case VERTEX_OPTIONS:
				readOptions( in );
				break;

			case MESH_COLOR:
				_color = readByte( in );
				break;

			case MESH_TEXTURE_INFO:
				readMeshTextureInfo( in );
				break;

			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}
	}

	private static void readMeshTextureInfo( final InputStream in )
		throws IOException
	{
		/*final short type    =*/ readShort( in );
		/*final float xTiling =*/ readFloat( in );
		/*final float yTiling =*/ readFloat( in );
		/*final float Xicon   =*/ readFloat( in );
		/*final float Yicon   =*/ readFloat( in );
		/*final float Zicon   =*/ readFloat( in );

		final float[][] matrix = new float[ 4 ][ 3 ];
		for ( int i = 0; i < 4; i++ )
		{
			for ( int j = 0; j < 3; j++ )
			{
				matrix[ i ][ j ] = readFloat( in );
			}
		}

		/*final float scaling   =*/ readFloat( in );
		/*final float planIconW =*/ readFloat( in );
		/*final float planIconH =*/ readFloat( in );
		/*final float cylIconH  =*/ readFloat( in );
	}

	private static void readOptions( final InputStream in )
		throws IOException
	{
		final int numberOfOptions = readUnsignedShort( in );
		for ( int i = 0; i < numberOfOptions; i++ )
		{
			/*final short option =*/ readShort( in );
		}
	}

	private void readCoordinateSystem( final InputStream in )
		throws IOException
	{
		final double rotationXX = (double)readFloat( in );
		final double rotationYX = (double)readFloat( in );
		final double rotationZX = (double)readFloat( in );
		final double rotationXY = (double)readFloat( in );
		final double rotationYY = (double)readFloat( in );
		final double rotationZY = (double)readFloat( in );
		final double rotationXZ = (double)readFloat( in );
		final double rotationYZ = (double)readFloat( in );
		final double rotationZZ = (double)readFloat( in );
		final double originX    = (double)readFloat( in );
		final double originY    = (double)readFloat( in );
		final double originZ    = (double)readFloat( in );

		_transform = Matrix3D.IDENTITY.set( rotationXX, rotationXY, rotationXZ, originX,
		                                    rotationYX, rotationYY, rotationYZ, originY,
		                                    rotationZX, rotationZY, rotationZZ, originZ );
	}

	private void readVertexCoordinates( final InputStream in )
		throws IOException
	{
		final int numberOfVertices = readUnsignedShort( in );

		final List<Vector3D> vertices = new ArrayList<Vector3D>( numberOfVertices );
		for ( int i = 0; i < numberOfVertices; i++ )
		{
			vertices.add( new Vector3D( (double)readFloat( in ), (double)readFloat( in ), (double)readFloat( in ) ) );
		}

		_vertices = vertices;
	}

	private void readTextureCoordinates( final InputStream in )
		throws IOException
	{
		final Vector2f[] textureCoordinates = new Vector2f[ readUnsignedShort( in ) ];

		for ( int i = 0; i < textureCoordinates.length; i++ )
		{
			textureCoordinates[ i ] = new Vector2f( readFloat( in ), readFloat( in ) );
		}

		_textureCoordinates = textureCoordinates;
	}
}