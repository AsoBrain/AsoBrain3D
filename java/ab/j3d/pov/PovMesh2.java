/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2006
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
package ab.j3d.pov;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.numdata.oss.io.IndentingWriter;

/**
 * This class represents a POV-Ray mesh2 object. All lists are zero-based
 * (start with index 0). The mesh2 object has the following structure:
 *
 * -----------------------------------------------------------------------------
 *
 * mesh2
 * {
 *     vertex_vectors
 *     {
 *         [number of vertices]
 *         < povvector > , < povvector > , < povvector > ,
 *         ...
 *     }
 *     uv_vectors
 *     {
 *         [number of uv coordinates]
 *         < povvector > , < povvector > , < povvector > ,
 *         ...
 *     }
 *     texture_list
 *     {
 *         [number of textures]
 *         texture { TEX1 }
 *         texture { TEX2 }
 *         ...
 *     }
 *     face_indices
 *     {
 *         [number of face indices]
 *         < povvector > , [textureNr] ,< povvector > , [textureNr] , < povvector > ,
 *         ...
 *     }
 *     uv_indices
 *     {
 *         [number of uv indices]
 *         < povvector > , < povvector > , < povvector > ,
 *         ...
 *     }
 *     uv_mapping           // needed when up-mapping is applied.
 *     texture { TEX3 }     // applied to all faces that have not been textured yet.
 * }
 *
 * -----------------------------------------------------------------------------
 *
 * vertex_vectors:   All vertices defining this mesh
 * uv_vectors:       Only the x en y are used (uv-coordinates are 2d).
 * texture_list:     Textures should not be declared insidide a mesh for
 *                   efficiency reasons.
 * face_indices:     Each vector points to 3 vertices in the uv_vectors list
 *                   wich defines a single face (triangle).
 * uv_indices:       Each vector points to 3 uv-coordinates in the uv_vectors
 *                   list defining a single triangular texture section wich is
 *                   mapped to the face pointed to by the corresponding face
 *                   index.
 * uv_mapping:       Needed when up-mapping is applied.
 * texture { TEX3 }: Applied to all faces that have not been textured yet.
 * [textureNr]:      Points to a texture in the texture list. The number of
 *                   face indices must equal the number of uv indices.
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public final class PovMesh2
	extends PovGeometry
{
	/**
	 * List containing {@link PovVector}s describing all vertices of the mesh.
	 * The list contains no duplicates.
	 */
	private final List _vertexvectors = new ArrayList();

	/**
	 * List containing {@link PovVector}s describing all uv-coordinates of the
	 * mesh. The list contains no duplicates.
	 */
	private final List _uvvectors = new ArrayList();

	/**
	 * List containing all used textures. The duplicates are filtered in the
	 * write method. The reason for this is that a face index can also have an
	 * index into the texture list [textureNr].
	 */
	private final List _duplicateTextureList = new ArrayList();

	/**
	 * List containing {@link PovVector}s, every {@link PovVector} points to 3
	 * vertices in the vertexvectors list.
	 */
	private final List _faceindices = new ArrayList();

	/**
	 * List containing {@link PovVector}s, every {@link PovVector} points to 3
	 * uv-coordinates in the uvvectors list.
	 */
	private final List _uvindices = new ArrayList();

	/**
	 * Construct new {@link PovMesh2}.
	 *
	 * @param meshName The name for this mesh.
	 */
	public PovMesh2( final String meshName )
	{
		super( meshName );
	}

	/**
	 * Add a new face (triangle) to the mesh.
	 *
	 * @param v1 First face vertex.
	 * @param v2 Second face vertex.
	 * @param v3 Third face vertex.
	 * @param uv1 First uv-coordinate of face.
	 * @param uv2 Second uv-coordinate of face.
	 * @param uv3 Third uv-coordinate of face.
	 * @param povTexture Texture for this face.
	 */
	public void addTriangle( final PovVector v1 , final PovVector v2 , final PovVector v3 ,
	                         final PovVector uv1 , final PovVector uv2 , final PovVector uv3 ,
	                         final PovTexture povTexture )
	{
		final List vertexVectors = _vertexvectors;
		final List uvvectors     = _uvvectors;

		_duplicateTextureList.add( povTexture );

		/*
		 * For each vertex, if the vertex has nog been added yet to the list,
		 * add it, then add an index to the vertices in the face_indices list.
		 * If a vertex has been added already,only a new index is created to
		 * the vertex.
		 */
		if ( vertexVectors.indexOf( v1 ) == -1 )
			vertexVectors.add( v1 );

		if ( vertexVectors.indexOf( v2 ) == -1 )
			vertexVectors.add( v2 );

		if ( vertexVectors.indexOf( v3 ) == -1 )
			vertexVectors.add( v3 );

		final PovVector faceIndex = new PovVector( (double)vertexVectors.indexOf( v1 ) ,
		                                           (double)vertexVectors.indexOf( v2 ) ,
		                                           (double)vertexVectors.indexOf( v3 ) );
		_faceindices.add( faceIndex );

		if ( uv1 != null && uv2 != null && uv3 != null )
		{
			/*
			 * Same method as for the vertices.
			 */
			if ( uvvectors.indexOf( uv1 ) == -1 )
				uvvectors.add( uv1 );

			if ( uvvectors.indexOf( uv2 ) == -1 )
				uvvectors.add( uv2 );

			if ( uvvectors.indexOf( uv3 ) == -1 )
				uvvectors.add( uv3 );

			final PovVector uvIndex = new PovVector( (double)uvvectors.indexOf( uv1 ) ,
													 (double)uvvectors.indexOf( uv2 ) ,
													 (double)uvvectors.indexOf( uv3 ) );
			_uvindices.add( uvIndex );
		}
		else
		{
			/*
			 * The number of face indices must equals the number of uv-indices.
			 * This does however result in unnessesary indices when the object
			 * contains a mixture of textures and unicolors and when no
			 * uv-mapping is needed.
			 *
			 * @FIXME:
			 *
			 * Get rid of unnessesary indices (might not be possible at
			 * all since the number of face indices must equal the
			 * number of uv-indices).
			 */
			_uvindices.add( new PovVector( 0.0 , 0.0 , 0.0 ) );
		}
	}

	/**
	 * This method writes this mesh2 object to the given outputstream in the
	 * format described above.
	 *
	 * @param out The outputstream to write to.
	 * @throws IOException When there is an error writing to out.
	 */
	public void write( final IndentingWriter out )
		throws IOException
	{
		final List         vertexVectors        = _vertexvectors;
		final List         uvVectors            = _uvvectors;
		final List         uvIndices            = _uvindices;
		final List         duplicateTextureList = _duplicateTextureList;
		final List         faceIndices          = _faceindices;
		final int          vertexCount          = vertexVectors.size();
		final int          uvVectorsCount       = uvVectors.size();
		final int          faceIndiceCount      = faceIndices.size();
		final int          textureCount         = duplicateTextureList.size();
		final int          uvIndicesCount       = uvIndices.size();
		final NumberFormat floatFormat          = FLOAT_FORMAT;
		final NumberFormat intFormat            = INT_FORMAT;

		out.writeln( "mesh2" );
		out.writeln( "{" );
		out.indentIn();
		out.writeln( "vertex_vectors" );
		out.writeln( "{" );
		out.indentIn();
		out.write  ( String.valueOf( vertexCount ) );
		out.writeln( "," );

		for ( int i = 0 ; i < vertexCount ; i++ )
		{
			final PovVector vector = (PovVector)vertexVectors.get( i );

			if ( i > 0 )
			{
				if ( ( i % 3 ) == 0 )
					out.writeln( " ," );
				else
					out.write( " , " );
			}

			vector.write( out );
		}

		out.writeln();
		out.indentOut();
		out.writeln( "}" );

		if ( uvVectors.size() > 0 )
		{
			out.writeln( "uv_vectors" );
			out.writeln( "{" );
			out.indentIn();
			out.write  ( String.valueOf( uvVectorsCount ) );
			out.writeln( "," );

			for ( int i = 0 ; i < uvVectorsCount ; i++ )
			{
				final PovVector uvVector = (PovVector)uvVectors.get( i );

				if ( i > 0 )
				{
					if ( ( i % 3 ) == 0 )
						out.writeln( " ," );
					else
						out.write( " , " );
				}

				out.write( "< " );
				out.write( floatFormat.format( uvVector.v.x ) );
				out.write( " , " );
				out.write( floatFormat.format( uvVector.v.y ) );
				out.write( " >" );
			}

			out.writeln();
			out.indentOut();
			out.writeln( "}" );
		}

		final List textureList = new ArrayList();
		for ( int i = 0 ; i < textureCount ; i++ )
		{
			final PovTexture povTexture = (PovTexture)duplicateTextureList.get( i );

			if ( !textureList.contains( povTexture ) )
			{
				textureList.add( povTexture );
			}
		}

		final int numTextures = textureList.size();
		if ( numTextures > 1 )
		{
			out.writeln( "texture_list" );
			out.writeln( "{" );
			out.indentIn();
			out.write( String.valueOf( textureList.size() ) );
			out.writeln( "," );

			for ( int i = 0 ; i < textureList.size() ; i++ )
			{
				final PovTexture povTexture = (PovTexture)textureList.get( i );
				povTexture.write( out );
			}

			out.indentOut();
			out.writeln( "}" );
		}

		out.writeln( "face_indices" );
		out.writeln( "{" );
		out.indentIn();
		out.write( String.valueOf( faceIndiceCount ) );
		out.writeln( "," );

		for ( int i = 0 ; i < faceIndiceCount ; i++ )
		{
			if ( i > 0 )
			{
				if ( ( i % 3 ) == 0 )
					out.writeln( " ," );
				else
					out.write( " , " );
			}

			final PovVector index = (PovVector) _faceindices.get( i );

			out.write( "< " );
			out.write( intFormat.format( index.v.x ) );
			out.write( " , " );
			out.write( intFormat.format( index.v.y ) );
			out.write( " , " );
			out.write( intFormat.format( index.v.z ) );
			out.write( " >" );

			if ( numTextures > 1 )
			{
				out.write( " , " );
				out.write( String.valueOf( textureList.indexOf( duplicateTextureList.get( i ) ) ) );
			}
		}

		out.writeln();
		out.indentOut();
		out.writeln( "}" );

		if ( uvIndices.size() > 0 )
		{
			out.writeln( "uv_indices" );
			out.writeln( "{" );
			out.indentIn();
			out.write( String.valueOf( uvIndicesCount ) );
			out.writeln( "," );

			for ( int i = 0 ; i < uvIndicesCount ; i++ )
			{
				if ( i > 0 )
				{
					if ( ( i % 3 ) == 0 )
						out.writeln( " ," );
					else
						out.write( " , " );
				}

				final PovVector index = (PovVector) uvIndices.get( i );

				out.write( "< " );
				out.write( intFormat.format( index.v.x ) );
				out.write( " , " );
				out.write( intFormat.format( index.v.y ) );
				out.write( " , " );
				out.write( intFormat.format( index.v.z ) );
				out.write( " >" );
			}

			out.writeln();
			out.indentOut();
			out.writeln( "}" );
		}

		if ( uvVectors.size() > 0 ) out.writeln( "uv_mapping" );
		if ( numTextures == 1 )
		{
			final PovTexture tex = (PovTexture)textureList.get( 0 );
			tex.write( out );
		}

		writeTexture( out );
		out.indentOut();
		out.writeln( "}" );
	}
}
