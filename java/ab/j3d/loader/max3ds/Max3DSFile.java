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
import ab.j3d.geom.*;
import ab.j3d.model.*;

/**
 * 3D Studio or 3D Studio MAX (<code>.3DS</colorMap>) file.
 * <p>
 * A <code>3DS</code> file consists of chunks, each starting with an ID and the
 * chunk length. This allows easy skipping of unrecognized chunks.
 * <p>
 * Type   : {@link #MAIN_3DS}
 * Parent : none
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
public class Max3DSFile
	extends Chunk
{
	public EditableObjectChunk _editableObject;

	public KeyFramesChunk _keyframes;

	/**
	 * Read 3D Studio file.
	 *
	 * @param   in  Stream to read file from.
	 *
	 * @throws  IOException if a read error occured.
	 */
	public Max3DSFile( final InputStream in )
		throws IOException
	{
		super( in, readUnsignedShort( in ), readInt( in ) - 6 );
	}

	/**
	 * Create scene from contents of this file.
	 *
	 * @return  3D scene.
	 */
	public void addMeshesToScene( final Scene scene )
	{
		final EditableObjectChunk editableObject = _editableObject;
		final Map<String,TriangleMeshChunk> meshes = editableObject._meshes;
		final Map<String,MaterialChunk> materials = editableObject._materials;

		for ( final Map.Entry<String,TriangleMeshChunk> meshChunkEntry : meshes.entrySet() )
		{
			final String name = meshChunkEntry.getKey();
			final TriangleMeshChunk triangleMeshChunk = meshChunkEntry.getValue();

			final Matrix3D transform = Matrix3D.IDENTITY; // triangleMeshChunk._transform;

			final Object3DBuilder builder = new Object3DBuilder();
			buildMesh( builder, materials, triangleMeshChunk );
			final Object3D object3d = builder.getObject3D();

			scene.addContentNode( name, transform, object3d );
		}
	}

	/**
	 * Create 3D node with contents of this file.
	 *
	 * @return  3D node with contents of this file.
	 */
	public Node3D createSceneNode3D()
	{
		final Node3D result = new Node3D();
		buildScene( result );
		return ( result.getChildCount() == 1 ) ? result.getChild( 0 ) : result;
	}

	/**
	 * Build 3D scene with contents of this file.
	 *
	 * @param   target  Target node to add contents to.
	 */
	public void buildScene( final Node3D target )
	{
		final EditableObjectChunk editableObject = _editableObject;
		final Map<String,TriangleMeshChunk> meshes = editableObject._meshes;
		final Map<String,MaterialChunk> materials = editableObject._materials;

		for ( final TriangleMeshChunk triangleMeshChunk : meshes.values() )
		{
			final Matrix3D transform = triangleMeshChunk._transform;

			final Object3DBuilder builder = new Object3DBuilder();
			buildMesh( builder, materials, triangleMeshChunk );
			final Object3D object3d = builder.getObject3D();

			if ( Matrix3D.IDENTITY.equals( transform ) )
			{
				target.addChild( object3d );
			}
			else
			{
				final Transform3D transform3d = new Transform3D( transform );
				transform3d.addChild( object3d );
				target.addChild( transform3d );
			}
		}
	}

	public static void buildMesh( final Abstract3DObjectBuilder builder, final Map<String,MaterialChunk> materials, final TriangleMeshChunk mesh )
	{
		final List<Vector3D> vertices = mesh._vertices;
		final Vector2f[] textureCoordinates = mesh._textureCoordinates;
		final FacesChunk facesChunk = mesh._faces;

		final int     numberOfFaces   = facesChunk._numberOfFaces;
		final int[][] faces           = facesChunk._faces;
		final int[]   smoothingGroups = facesChunk._smoothingGroups;

		builder.setVertexCoordinates( vertices );

		final Vector3D[] faceNormals = new Vector3D[ numberOfFaces ];
		for ( int i = 0; i < numberOfFaces; i++ )
		{
			final int[] face = faces[ i ];
			faceNormals[ i ] = GeometryTools.getPlaneNormal( vertices.get( face[ 2 ] ), vertices.get( face[ 1 ] ), vertices.get( face[ 0 ] ) );
		}

		final MaterialChunk[] faceMaterials = new MaterialChunk[ numberOfFaces ];
		for ( int i = 0; i < facesChunk._materialNames.size(); i++ )
		{
			final MaterialChunk material = materials.get( facesChunk._materialNames.get( i ) );

			for ( final int faceIndex : facesChunk._materialIndices.get(  i ) )
			{
				faceMaterials[ faceIndex ] = material;
			}
		}

		final int[][] faceIndicesPerVertex;
		{
			final int[] faceCountPerVertex = new int[ vertices.size() ];
			for ( int faceIndex = 0; faceIndex < numberOfFaces; faceIndex++ )
			{
				final int[] face = faces[ faceIndex ];
				faceCountPerVertex[ face[ 0 ] ]++;
				faceCountPerVertex[ face[ 1 ] ]++;
				faceCountPerVertex[ face[ 2 ] ]++;
			}

			faceIndicesPerVertex = new int[ vertices.size() ][];
			for ( int vertexIndex = 0; vertexIndex < faceIndicesPerVertex.length; vertexIndex++ )
			{
				faceIndicesPerVertex[ vertexIndex ] = new int[ faceCountPerVertex[ vertexIndex ] ];
			}

			for ( int faceIndex = 0; faceIndex < numberOfFaces; faceIndex++ )
			{
				final int[] face = faces[ faceIndex ];

				for ( int i = 0; i < 3; i++ )
				{
					final int vertexIndex = face[ i ];
					faceIndicesPerVertex[ vertexIndex ][ --faceCountPerVertex[ vertexIndex ] ] = faceIndex;
				}
			}
		}

		final Vector3D[] facePoints = new Vector3D[ 3 ];
		final Vector3D[] vertexNormals = new Vector3D[ 3 ];
		final float[] texturePoints = ( textureCoordinates != null ) ? new float[ 6 ] : null;

		for ( int faceIndex = 0; faceIndex < faces.length; faceIndex++ )
		{
			final int[] face = faces[ faceIndex ];
			final Vector3D faceNormal = faceNormals[ faceIndex ];
			boolean isSmooth = false;

			for ( int k = 0; k < 3; k++ )
			{
				final int vertexIndex = face[ k ];
				facePoints[ 2 - k ] = vertices.get( vertexIndex );

				final Vector3D vertexNormal;

				final int smoothingGroupMask = smoothingGroups[ faceIndex ];
				if ( smoothingGroupMask != 0 )
				{
					isSmooth = true;
					double nx = 0.0;
					double ny = 0.0;
					double nz = 0.0;

					for ( final int otherFaceIndex : faceIndicesPerVertex[ vertexIndex ] )
					{
						if ( ( smoothingGroups[ otherFaceIndex ] & smoothingGroupMask ) != 0 )
						{
							final Vector3D n = faceNormals[ otherFaceIndex ];
							if ( n != null )
							{
								nx += n.x;
								ny += n.y;
								nz += n.z;
							}
						}
					}

					vertexNormal = Vector3D.normalize( nx, ny, nz );
				}
				else
				{
					vertexNormal = faceNormal;
				}

				vertexNormals[ 2 - k ] = vertexNormal;

				if ( textureCoordinates != null )
				{
					final Vector2f texturePoint = textureCoordinates[ vertexIndex ];
					texturePoints[ ( 2 - k ) * 2     ] = texturePoint.getX();
					texturePoints[ ( 2 - k ) * 2 + 1 ] = texturePoint.getY();
				}
			}

			final MaterialChunk materialChunk = faceMaterials[ faceIndex ];
			builder.addFace( facePoints, materialChunk != null ? materialChunk._material : null, texturePoints, vertexNormals, isSmooth, true );
		}
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		if ( chunkType != MAIN_3DS )
		{
			throw new IOException( "Header doesn't match 0x4D4D; Header=" + Integer.toHexString( chunkType ) );
		}

		super.processChunk( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case TDS_VERSION :
				/*int version =*/readInt( in );
				break;

			case EDIT_3DS :
				_editableObject = new EditableObjectChunk( in, chunkType, remainingChunkBytes );
				break;

			case KEYFRAMES :
				_keyframes = new KeyFramesChunk( in, chunkType, remainingChunkBytes );
				break;

			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}
	}
}