/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
 */
package ab.j3d.pov;

import java.io.*;
import java.util.*;

/**
 * This class represents a POV-Ray mesh2 object. All lists are zero-based
 * (start with index 0). The mesh2 object has the following structure:
 * <pre>
 * mesh2
 * {
 *     vertex_vectors
 *     {
 *         [number of vertices]
 *         {@link PovVector}, {@link PovVector}, {@link PovVector},
 *         ...
 *     }
 *
 *     uv_vectors
 *     {
 *         [number of U/V vectors]
 *         {@link PovVector}, {@link PovVector}, {@link PovVector},
 *         ...
 *     }
 *
 *     normal_vectors
 *     {
 *         [number of normals]
 *         {@link PovVector}, {@link PovVector}, {@link PovVector},
 *         ...
 *     }
 *
 *     texture_list
 *     {
 *         [number of textures]
 *         texture { TEX1 }
 *         texture { TEX2 }
 *         ...
 *     }
 *
 *     face_indices
 *     {
 *         [number of face indices]
 *         &lt;index1,index2,index33&gt;[,textureIndex],
 *         ...
 *     }
 *
 *     uv_indices
 *     {
 *         [number of U/V indices]
 *         &lt;index1,index2,index3&gt;,
 *         ...
 *     }
 *
 *     normal_indices
 *     {
 *         [number of normal indices]
 *         &lt;index1,index2,index33&gt;,
 *         ...
 *     }
 *
 *     uv_mapping
 *     texture { TEX3 }     // applied to all faces that have not been textured yet.
 * }
 * </pre>
 * <table>
 *  <tr><td>vertex_vectors  </td><td>All vertices defining this mesh                          </td></tr>
 *  <tr><td>uv_vectors      </td><td>Only the x en y are used (U/V-vectors are 2D).           </td></tr>
 *  <tr><td>texture_list    </td><td>Textures should not be declared insidide a mesh for
 *                                   efficiency reasons.                                      </td></tr>
 *  <tr><td>face_indices    </td><td>Each vector points to 3 vertices in the uv_vectors list  </td></tr>
 *  <tr><td>                </td><td>wich defines a single face (triangle).                   </td></tr>
 *  <tr><td>[textureNr]     </td><td>Points to a texture in the texture list. The number of
 *                                   face indices must equal the number of U/V indices.       </td></tr>
 *  <tr><td>uv_indices      </td><td>Each vector points to 3 U/V-vectors in the uv_vectors
 *                                   list defining a single triangular texture section wich is
 *                                   mapped to the face pointed to by the corresponding face
 *                                   index.                                                   </td></tr>
 *  <tr><td>uv_mapping      </td><td>Needed when up-mapping is applied.                       </td></tr>
 *  <tr><td>texture { TEX } </td><td>Applied to all faces that have not been textured yet.    </td></tr>
 * </table>
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public class PovMesh2
	extends PovGeometry
{
	/**
	 * List containing {@link PovVector}s describing all vertices of the mesh.
	 * The list contains no duplicates.
	 */
	private final List<PovVector> _vertexVectors = new ArrayList<PovVector>();

	/**
	 * List containing {@link PovVector}s describing all U/V-vectors of the
	 * mesh. The list contains no duplicates.
	 */
	private final List<PovVector> _uvVectors = new ArrayList<PovVector>();

	/**
	 * List containing {@link PovVector}s describing all normals of the mesh.
	 * The list contains no duplicates.
	 */
	private final List<PovVector> _normalVectors = new ArrayList<PovVector>();

	/**
	 * List containing all used textures. The duplicates are filtered in the
	 * write method. The reason for this is that a face index can also have an
	 * index into the texture list [textureNr].
	 */
	private final List<PovTexture> _textureList = new ArrayList<PovTexture>();

	/**
	 * List containing all triangle in this mesh.
	 */
	private final List<Triangle> _triangles = new ArrayList<Triangle>();

	/**
	 * Whether {@link #_triangles} is sorted.
	 *
	 * @see     #sortTriangles()
	 */
	private  boolean _trianglesSorted = false;

	/**
	 * This inner class represents a single triangle in the mesh.
	 */
	static class Triangle
	{
		final int _vertexIndex1;
		final int _uvIndex1;
		final int _normalIndex1;
		final int _vertexIndex2;
		final int _uvIndex2;
		final int _normalIndex2;
		final int _vertexIndex3;
		final int _uvIndex3;
		final int _normalIndex3;
		final int _textureIndex;

		Triangle( final int vertexIndex1, final int uvIndex1, final int normalIndex1,
		          final int vertexIndex2, final int uvIndex2, final int normalIndex2,
		          final int vertexIndex3, final int uvIndex3, final int normalIndex3,
		          final int textureIndex )
		{
			_vertexIndex1 = vertexIndex1;
			_uvIndex1     = uvIndex1;
			_normalIndex1 = normalIndex1;
			_vertexIndex2 = vertexIndex2;
			_uvIndex2     = uvIndex2;
			_normalIndex2 = normalIndex2;
			_vertexIndex3 = vertexIndex3;
			_uvIndex3     = uvIndex3;
			_normalIndex3 = normalIndex3;
			_textureIndex = textureIndex;
		}

		void writeFaceIndices( final PovWriter out, final boolean includeTextureIndex )
			throws IOException
		{
			out.write( '<' );
			out.write( Integer.toString( _vertexIndex1 ) );
			out.write( ',' );
			out.write( Integer.toString( _vertexIndex2 ) );
			out.write( ',' );
			out.write( Integer.toString( _vertexIndex3 ) );
			out.write( '>' );

			if ( includeTextureIndex && ( _textureIndex >= 0 ) )
			{
				out.write( ',' );
				out.write( Integer.toString( _textureIndex ) );
			}
		}

		boolean hasUV()
		{
			return ( ( _uvIndex1 >= 0 ) && ( _uvIndex2 >= 0 ) && ( _uvIndex3 >= 0 ) );
		}

		void writeUvIndices( final PovWriter out )
			throws IOException
		{
			if ( hasUV() )
			{
				out.write( '<' );
				out.write( Integer.toString( _uvIndex1 ) );
				out.write( ',' );
				out.write( Integer.toString( _uvIndex2 ) );
				out.write( ',' );
				out.write( Integer.toString( _uvIndex3 ) );
				out.write( '>' );
			}
			else
			{
				out.write( "<0,0,0>" );
			}
		}

		boolean hasNormals()
		{
			return ( ( _normalIndex1 >= 0 ) && ( _normalIndex2 >= 0 ) && ( _normalIndex3 >= 0 ) );
		}

		void writeNormalIndices( final PovWriter out )
			throws IOException
		{
			if ( hasNormals() )
			{
				out.write( '<' );
				out.write( Integer.toString( _normalIndex1 ) );
				out.write( ',' );
				out.write( Integer.toString( _normalIndex2 ) );
				out.write( ',' );
				out.write( Integer.toString( _normalIndex3 ) );
				out.write( '>' );
			}
		}

		@Override
		public boolean equals( final Object other )
		{
			final boolean result;
			if ( other == this )
			{
				result = true;
			}
			else if ( other instanceof Triangle )
			{
				final Triangle triangle = (Triangle)other;
				result = ( _vertexIndex1 == triangle._vertexIndex1 ) &&
				         ( _uvIndex1     == triangle._uvIndex1     ) &&
				         ( _normalIndex1 == triangle._normalIndex1 ) &&
				         ( _vertexIndex2 == triangle._vertexIndex2 ) &&
				         ( _uvIndex2     == triangle._uvIndex2     ) &&
				         ( _normalIndex2 == triangle._normalIndex2 ) &&
				         ( _vertexIndex3 == triangle._vertexIndex3 ) &&
				         ( _uvIndex3     == triangle._uvIndex3     ) &&
				         ( _normalIndex3 == triangle._normalIndex3 ) &&
				         ( _textureIndex == triangle._textureIndex );
			}
			else
			{
				result = false;
			}
			return result;
		}

		@Override
		public int hashCode()
		{
			return _vertexIndex1      ^ _uvIndex1 << 1 ^ _normalIndex1 << 2 ^
			       _vertexIndex2 << 3 ^ _uvIndex2 << 4 ^ _normalIndex2 << 5 ^
			       _vertexIndex3 << 6 ^ _uvIndex3 << 7 ^ _normalIndex3 << 8 ^
			       _textureIndex << 9;
		}
	}

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
	 * Get index for the specified vertex vector in the list of vertex vectors.
	 *
	 * @param   vertex          Vertex vector whose index to determine
	 *                          (<code>null</code> => will return -1).
	 *
	 * @return  Index of vertex;
	 *          -1 if <code>vertex</code> is <code>null</code>.
	 */
	int getOrAddVertexVectorIndex( final PovVector vertex )
	{
		return getOrAddElementIndex( _vertexVectors, vertex );
	}

	/**
	 * Get index for the specified U/V vector in the list of U/V vectors.
	 *
	 * @param   uvVector        U/V vector whose index to determine
	 *                          (<code>null</code> => will return -1).
	 *
	 * @return  Index of uv;
	 *          -1 if <code>uvVector</code> is <code>null</code>.
	 */
	int getOrAddUvVectorIndex( final PovVector uvVector )
	{
		return getOrAddElementIndex( _uvVectors, uvVector );
	}

	/**
	 * Get index for the specified normal vector in the list of normal vectors.
	 *
	 * @param   normalVector    Normal vector whose index to determine
	 *                          (<code>null</code> => will return -1).
	 *
	 * @return  Index of normal vector;
	 *          -1 if <code>normalVector</code> is <code>null</code>.
	 */
	int getOrAddNormalVectorIndex( final PovVector normalVector )
	{
		return getOrAddElementIndex( _normalVectors, normalVector );
	}

	/**
	 * Get index for the specified texture in the list of textures.
	 *
	 * @param   texture    Texture whose index to determine
	 *                          (<code>null</code> => will return -1).
	 *
	 * @return  Index of texture;
	 *          -1 if <code>texture</code> is <code>null</code>.
	 */
	int getOrAddTextureIndex( final PovTexture texture )
	{
		return getOrAddElementIndex( _textureList, texture );
	}

	/**
	 * Get index for an element in the specified list. If the element is already
	 * in the list, the matching index is returned; if the element is not yet in
	 * the list, it will be added to the list, and its index will be returned.
	 *
	 * @param   list        List of elements.
	 * @param   element     Element whose index to determine
	 *                      (<code>null</code> => will return -1).
	 *
	 * @return  Index of element in list;
	 *          -1 if <code>element</code> is <code>null</code>.
	 *
	 * @throws  NullPointerException if <code>list</code> is <code>null</code>.
	 */
	private static <T> int getOrAddElementIndex( final List<T> list, final T element )
	{
		int result;

		if ( element != null )
		{
			result = list.indexOf( element );
			if ( result < 0 )
			{
				result = list.size();
				list.add( element );
			}
		}
		else
		{
			result = -1;
		}

		return result;
	}

	/**
	 * Add a new triangle to the mesh.
	 *
	 * @param   v1          First vertex' vector.
	 * @param   uv1         First vertex' U/V-vectors in texture (<code>null</code> => no texture mapping).
	 * @param   vn1         First vertex' vertex normal (<code>null</code> => no smoothing).
	 * @param   v2          Second vertex' vector.
	 * @param   uv2         Second vertex' U/V-vectors in texture (<code>null</code> => no texture mapping).
	 * @param   vn2         Second vertex' vertex normal (<code>null</code> => no smoothing).
	 * @param   v3          Thrid vertex' vector.
	 * @param   uv3         Thrid vertex' UV-vectors in texture (<code>null</code> => no texture mapping).
	 * @param   vn3         Thrid vertex' vertex normal (<code>null</code> => no smoothing).
	 * @param   texture     Texture for this face.
	 */
	public void addTriangle(
		final PovVector v1, final PovVector uv1, final PovVector vn1,
	    final PovVector v2, final PovVector uv2, final PovVector vn2,
	    final PovVector v3, final PovVector uv3, final PovVector vn3,
	    final PovTexture texture )
	{
		addTriangle(
			getOrAddVertexVectorIndex( v1 ), getOrAddUvVectorIndex( uv1 ), getOrAddNormalVectorIndex( vn1 ),
			getOrAddVertexVectorIndex( v2 ), getOrAddUvVectorIndex( uv2 ), getOrAddNormalVectorIndex( vn2 ),
			getOrAddVertexVectorIndex( v3 ), getOrAddUvVectorIndex( uv3 ), getOrAddNormalVectorIndex( vn3 ),
			getOrAddTextureIndex( texture ) );
	}

	/**
	 * Add a new triangle to the mesh.
	 *
	 * @param   v1          First vertex' vector index.
	 * @param   uv1         First vertex' U/V vectors index (<code>-1</code> => no texture mapping).
	 * @param   vn1         First vertex' vertex normal index (<code>-1</code> => no smoothing).
	 * @param   v2          Second vertex' vector index.
	 * @param   uv2         Second vertex' U/V vectors index (<code>-1</code> => no texture mapping).
	 * @param   vn2         Second vertex' vertex normal index (<code>-1</code> => no smoothing).
	 * @param   v3          Thrid vertex' vector index.
	 * @param   uv3         Thrid vertex' U/V vectors index (<code>-1</code> => no texture mapping).
	 * @param   vn3         Thrid vertex' vertex normal index (<code>-1</code> => no smoothing).
	 * @param   texture     Texture index for this face.
	 */
	public void addTriangle(
		final int v1, final int uv1, final int vn1,
	    final int v2, final int uv2, final int vn2,
	    final int v3, final int uv3, final int vn3,
	    final int texture )
	{
		_triangles.add( new Triangle( v1, uv1, vn1, v2, uv2, vn2, v3, uv3, vn3, texture ) );
		_trianglesSorted = false;
	}

	boolean hasUV()
	{
		return _uvVectors.size() > 1;
	}

	boolean hasNormals()
	{
		return _normalVectors.size() > 1;
	}

	/**
	 * Set all vertex vectors in this mesh.
	 * <dl>
	 *  <dt>WARNING:</dt>
	 *  <dd>
	 *   If this method of setting mesh properties is used, <strong>the caller
	 *   is responsible for providing consistent data</strong> (vertex vectors,
	 *   U/V vectors, normal vectors, textures, and triangles).
	 *  </dd>
	 * </dl>
	 *
	 * @param   vectors     List of vectors
	 */
	public void setVertexVectors( final List<PovVector> vectors )
	{
		if ( vectors != null )
		{
			final List<PovVector> vertexVectors = _vertexVectors;
			vertexVectors.clear();
			vertexVectors.addAll( vectors );
		}
	}

	/**
	 * Set all U/V vectors in this mesh.
	 * <dl>
	 *  <dt>WARNING:</dt>
	 *  <dd>
	 *   If this method of setting mesh properties is used, <strong>the caller
	 *   is responsible for providing consistent data</strong> (vertex vectors,
	 *   U/V vectors, normal vectors, textures, and triangles).
	 *  </dd>
	 * </dl>
	 *
	 * @param   vectors     List of vectors
	 */
	public void setUvVectors( final List<PovVector> vectors )
	{
		if ( vectors != null )
		{
			final List<PovVector> uvVectors = _uvVectors;
			uvVectors.clear();
			uvVectors.addAll( vectors );
		}
	}

	/**
	 * Set all normal vectors in this mesh.
	 * <dl>
	 *  <dt>WARNING:</dt>
	 *  <dd>
	 *   If this method of setting mesh properties is used, <strong>the caller
	 *   is responsible for providing consistent data</strong> (vertex vectors,
	 *   normal vectors, normal vectors, textures, and triangles).
	 *  </dd>
	 * </dl>
	 *
	 * @param   vectors     List of vectors
	 */
	public void setNormalVectors( final List<PovVector> vectors )
	{
		if ( vectors != null )
		{
			final List<PovVector> normalVectors = _normalVectors;
			normalVectors.clear();
			normalVectors.addAll( vectors );
		}
	}

	/**
	 * Returns the {@link PovVector}s describing all vertices of the mesh.
	 * The list contains no duplicates.
	 *
	 * @return  Vertex coordinates.
	 */
	public List<PovVector> getVertexVectors()
	{
		return Collections.unmodifiableList( _vertexVectors );
	}

	/**
	 * Returns the {@link PovVector}s describing all U/V-vectors of the
	 * mesh. The list contains no duplicates.
	 *
	 * @return  UV-coordinates.
	 */
	public List<PovVector> getUvVectors()
	{
		return Collections.unmodifiableList( _uvVectors );
	}

	/**
	 * Returns the {@link PovVector}s describing all normals of the mesh.
	 * The list contains no duplicates.
	 *
	 * @return  Vertex normals.
	 */
	public List<PovVector> getNormalVectors()
	{
		return Collections.unmodifiableList( _normalVectors );
	}

	/**
	 * Returns all used textures.
	 *
	 * @return  Textures.
	 */
	public List<PovTexture> getTextureList()
	{
		return Collections.unmodifiableList( _textureList );
	}

	/**
	 * Returns the triangles that make up the mesh.
	 *
	 * @return  Triangles.
	 */
	public List<Triangle> getTriangles()
	{
		sortTriangles();
		return Collections.unmodifiableList( _triangles );
	}

	/**
	 * Sorts triangles so that triangles with UV-indices come first;
	 * those without, come last.
	 */
	private void sortTriangles()
	{
		if ( !_trianglesSorted )
		{
			_trianglesSorted = true;
			Collections.sort( _triangles, new Comparator<Triangle>()
			{
				@Override
				public int compare( final Triangle o1, final Triangle o2 )
				{
					return o1.hasUV() ? o2.hasUV() ? 0 : -1 :
					       o2.hasUV() ? 1 : 0;
				}
			} );
		}
	}

	@Override
	public void write( final PovWriter out )
		throws IOException
	{
		final List<Triangle> triangles = getTriangles();

		out.write( "mesh2" );
		final String name = getName();
		if ( name != null )
		{
			out.write( " // " );
			out.write( name );
		}
		out.newLine();
		out.writeln( "{" );
		out.indentIn();

		writeVertexVectors( out );             // vertex_vectors {}
		writeUvVectors    ( out );             // uv_vectors {}
		writeNormalVectors( out );             // normal_vectors {}
		writeTextureList  ( out );             // texture_list {}
		writeFaceIndices  ( out, triangles ); // face_indices {}
		writeUvIndices    ( out, triangles ); // uv_indices {}
		writeNormalIndices( out, triangles ); // normal_indices {}

		if ( hasUV() )
		{
			out.writeln( "uv_mapping" );
		}

		if ( _textureList.size() == 1 )
		{
			final PovTexture texture = _textureList.get( 0 );
			texture.write( out );
		}

		writeModifiers( out );
		out.indentOut();
		out.writeln( "}" );
	}

	/**
	 * Write '<code>vertex_vectors</code>' section to the specified writer.
	 * <pre>
	 * vertex_vectors
	 * {
	 *     [number of vertices]
	 *     {@link PovVector}, {@link PovVector}, {@link PovVector},
	 *     ...
	 * }
	 * </pre>
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	void writeVertexVectors( final PovWriter out )
		throws IOException
	{
		final List<PovVector> vertexVectors = _vertexVectors;
		final int vertexCount = vertexVectors.size();

		out.writeln( "vertex_vectors" );
		out.writeln( "{" );
		out.indentIn();

		out.write( Integer.toString( vertexCount ) );
		out.write( ',' );
		out.newLine();

		for ( int i = 0; i < vertexCount; i++ )
		{
			final PovVector vector = vertexVectors.get( i );

			writeElementSeparator( out, 3, i );
			vector.write( out );
		}

		out.newLine();

		out.indentOut();
		out.writeln( "}" );
	}

	/**
	 * Write '<code>uv_vectors</code>' section to the specified writer.
	 * <pre>
	 * uv_vectors
	 * {
	 *     [number of U/V vectors]
	 *     {@link PovVector}, {@link PovVector}, {@link PovVector},
	 *     ...
	 * }
	 * </pre>
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	void writeUvVectors( final PovWriter out )
		throws IOException
	{
		if ( hasUV() )
		{
			final List<PovVector> uvVectors = _uvVectors;
			final int uvCount = uvVectors.size();

			out.writeln( "uv_vectors" );
			out.writeln( "{" );
			out.indentIn();

			out.write( Integer.toString( uvCount ) );
			out.write( ',' );
			out.newLine();

			for ( int i = 0; i < uvCount; i++ )
			{
				final PovVector vector = uvVectors.get( i );

				writeElementSeparator( out, 3, i );
				out.write( '<' );
				out.write( format( vector.getX() ) );
				out.write( ',' );
				out.write( format( vector.getY() ) );
				out.write( '>' );
			}

			out.newLine();

			out.indentOut();
			out.writeln( "}" );
		}
	}

	/**
	 * Write '<code>normal_vectors</code>' section to the specified writer.
	 * <pre>
	 * normal_vectors
	 * {
	 *     [number of normals]
	 *     {@link PovVector}, {@link PovVector}, {@link PovVector},
	 *     ...
	 * }
	 * </pre>
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	void writeNormalVectors( final PovWriter out )
		throws IOException
	{
		if ( hasNormals() )
		{
			final List<PovVector> normalVectors = _normalVectors;
			final int normalCount = normalVectors.size();

			out.writeln( "normal_vectors" );
			out.writeln( "{" );
			out.indentIn();

			out.write( Integer.toString( normalCount ) );
			out.write( ',' );
			out.newLine();

			for ( int i = 0; i < normalCount; i++ )
			{
				final PovVector vector = normalVectors.get( i );

				writeElementSeparator( out, 3, i );
				vector.write( out );
			}

			out.newLine();

			out.indentOut();
			out.writeln( "}" );
		}
	}

	/**
	 * Write '<code>texture_list</code>' section to the specified writer.
	 * <pre>
	 * texture_list
	 * {
	 *     [number of textures]
	 *     texture { TEX1 }
	 *     texture { TEX2 }
	 *     ...
	 * }
	 * </pre>
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	void writeTextureList( final PovWriter out )
		throws IOException
	{
		final List<PovTexture> textureList  = _textureList;
		final int              textureCount = textureList.size();

		if ( textureCount > 1 )
		{
			out.writeln( "texture_list" );
			out.writeln( "{" );
			out.indentIn();

			out.write( Integer.toString( textureCount ) );
			out.write( ',' );
			out.newLine();

			for ( final PovTexture texture : textureList )
			{
				texture.write( out );
			}

			out.indentOut();
			out.writeln( "}" );
		}
	}

	/**
	 * Write '<code>face_indices</code>' section to the specified writer.
	 * <pre>
	 * face_indices
	 * {
	 *     [number of face indices]
	 *     &lt;index1,index2,index33&gt;[,textureIndex],
	 *     ...
	 * }
	 * </pre>
	 *
	 * @param   out         Writer to use for output.
	 * @param   triangles   Triangles to include in section.
	 *
	 * @throws  IOException when writing failed.
	 */
	void writeFaceIndices( final PovWriter out, final List<Triangle> triangles )
		throws IOException
	{
		final boolean includeTextureIndex = ( _textureList.size() > 1 );

		out.writeln( "face_indices" );
		out.writeln( "{" );
		out.indentIn();

		out.write( Integer.toString( triangles.size() ) );
		out.writeln( "," );

		for ( int i = 0; i < triangles.size(); i++ )
		{
			writeElementSeparator( out, 6, i );

			final Triangle triangle = triangles.get( i );
			triangle.writeFaceIndices( out, includeTextureIndex );
		}
		out.newLine();

		out.indentOut();
		out.writeln( "}" );
	}

	/**
	 * Write '<code>uv_indices</code>' section to the specified writer.
	 * <pre>
	 * uv_indices
	 * {
	 *     [number of U/V indices]
	 *     &lt;index1,index2,index3&gt;,
	 *     ...
	 * }
	 * </pre>
	 *
	 * @param   out         Writer to use for output.
	 * @param   triangles   Triangles to include in section.
	 *
	 * @throws  IOException when writing failed.
	 */
	void writeUvIndices( final PovWriter out, final List<Triangle> triangles )
		throws IOException
	{
		if ( hasUV() )
		{
			out.writeln( "uv_indices" );
			out.writeln( "{" );
			out.indentIn();

			out.write( Integer.toString( triangles.size() ) );
			out.writeln( "," );

			for ( int i = 0; i < triangles.size(); i++ )
			{
				final Triangle triangle = triangles.get( i );

				writeElementSeparator( out, 6, i );
				triangle.writeUvIndices( out );
			}
			out.newLine();

			out.indentOut();
			out.writeln( "}" );
		}
	}

	/**
	 * Write '<code>normal_indices</code>' section to the specified writer.
	 * <pre>
	 * normal_indices
	 * {
	 *     [number of normal indices]
	 *     &lt;index1,index2,index33&gt;,
	 *     ...
	 * }
	 * </pre>
	 *
	 * @param   out         Writer to use for output.
	 * @param   triangles   Triangles to include in section.
	 *
	 * @throws  IOException when writing failed.
	 */
	void writeNormalIndices( final PovWriter out, final List<Triangle> triangles )
		throws IOException
	{
		if ( hasNormals() )
		{
			out.writeln( "normal_indices" );
			out.writeln( "{" );
			out.indentIn();

			out.write( Integer.toString( triangles.size() ) );
			out.writeln( "," );

			for ( int i = 0; i < triangles.size(); i++ )
			{
				writeElementSeparator( out, 3, i );

				final Triangle triangle = triangles.get( i );
				triangle.writeNormalIndices( out );
			}
			out.newLine();

			out.indentOut();
			out.writeln( "}" );
		}
	}

	private static void writeElementSeparator( final PovWriter out, final int elementsPerLine, final int elementIndex )
		throws IOException
	{
		if ( elementIndex > 0 )
		{
			if ( ( elementIndex % elementsPerLine ) == 0 )
			{
				out.writeln( "," );
			}
			else
			{
				out.write( ", " );
			}
		}
	}
}
