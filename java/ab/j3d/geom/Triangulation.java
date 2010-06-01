/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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
package ab.j3d.geom;

import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Represents the result of a triangulation operation.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface Triangulation
{
	/**
	 * Empty triangle array.
	 */
	int[] NO_TRIANGLES = new int[ 0 ];

	/**
	 * Returns the triangles that make up the triangulation.
	 *
	 * @return  Collection of triangles, each represented by an array
	 *          containing a vertex index triplet.
	 */
	Collection<Primitive> getPrimitives();

	/**
	 * Returns the triangles that make up the triangulation.
	 *
	 * @return  Collection of triangles, each represented by an array
	 *          containing a vertex index triplet.
	 */
	Collection<int[]> getTriangles();

	/**
	 * Get vertex.
	 *
	 * @param   index   Vertex index.
	 *
	 * @return  Vertex.
	 *
	 * @throws  IndexOutOfBoundsException if invalid index is specified.
	 */
	Vector3D getVertex( int index );

	/**
	 * Returns the vertices that are used in the triangulation result.
	 *
	 * @param   transform   Transformation to be applied to the vertices.
	 *
	 * @return  List of transformed vertices.
	 */
	List<Vector3D> getVertices( Matrix3D transform );

	/**
	 * Primitive that can be used in triangulation result.
	 */
	class Primitive
	{
		/**
		 * Type of primitive.
		 */
		public enum Type
		{
			/**
			 * Set of independent triangles. Vertices v0, v1, v2 define the
			 * first triangle; vertices v3, v4, v5 define the second triangle;
			 * then v6, v7, v8, and so on.
			 */
			TRIANGLES,

			/**
			 * A triangle strip is a series of connected triangles. Vertices
			 * v0, v1, v2 define the first triangle; vertices v2, v1, v3 define
			 * the second triangle; then v2, v3, v4, and so on. Notice the
			 * alternating orientation to correctly form part of a surface.
			 */
			TRIANGLE_STRIP,

			/**
			 * A triangle fan is a series of connected triangles with a central
			 * vertex. Vertices v0, v1, v2 define the first triangle; vertices
			 * v0, v2, v3 define the second triangle; then v0, v2, v3, etc.
			 */
			TRIANGLE_FAN
		}

		/**
		 * Type of primitive.
		 */
		@NotNull
		private final Type _type;

		/**
		 * Vertices that define the primitive.
		 */
		@NotNull
		protected final int[] _vertices;

		/**
		 * Cached triangles.
		 */
		private int[] _triangles;

		/**
		 * Construct primitive.
		 *
		 * @param   type        Type of primitive.
		 * @param   vertices    Vertices that define the primitive.
		 */
		public Primitive( @NotNull final Type type, @NotNull final int[] vertices )
		{
			_type = type;
			//noinspection AssignmentToCollectionOrArrayFieldFromParameter
			_vertices = vertices;
			_triangles = ( type == Type.TRIANGLES ) ? vertices : null;
		}

		/**
		 * Get type of primitive.
		 *
		 * @return  Type of primitive.
		 */
		@NotNull
		public Type getType()
		{
			return _type;
		}

		/**
		 * Get vertices that define the primitive.
		 *
		 * @return  Vertices that define the primitive.
		 */
		@NotNull
		public int[] getVertices()
		{
			//noinspection ReturnOfCollectionOrArrayField
			return _vertices;
		}

		/**
		 * Get triangles defined by this primitive.
		 *
		 * @return  Triangles defined by this primitive.
		 */
		public int[] getTriangles()
		{
			int[] result = _triangles;
			if ( result == null )
			{
				final int[] vertices = _vertices;
				final int vertexCount = vertices.length;
				if ( vertexCount < 3 )
				{
					result = NO_TRIANGLES;
				}
				else
				{
					switch ( _type )
					{
						case TRIANGLES:
							result = vertices;
							break;

						case TRIANGLE_STRIP:
						{
							/*
							 * result[ 0 ] = { vertices[0], vertices[1], vertices[2] }
							 * result[ 1 ] = { vertices[2], vertices[1], vertices[3] }
							 * result[ 2 ] = { vertices[2], vertices[3], vertices[4] }
							 * result[ 3 ] = { vertices[4], vertices[3], vertices[5] }
							 * result[ 4 ] = { vertices[4], vertices[5], vertices[6] }
							*/
							int v0 = vertices[ 0 ];
							int v1 = vertices[ 1 ];
							int v2;

							result = new int[ ( vertexCount - 2 ) * 3 ];
							int resultIndex = 0;
							boolean flip = false;

							for ( int vertexIndex = 2; vertexIndex < vertexCount; )
							{
								v2 = vertices[ vertexIndex++ ];
								result[ resultIndex++ ] = flip ? v1 : v0;
								result[ resultIndex++ ] = flip ? v0 : v1;
								result[ resultIndex++ ] = v2;
								v0 = v1;
								v1 = v2;
								flip = !flip;
							}
							break;
						}

						case TRIANGLE_FAN:
						{
							/*
							 * result[ 0 ] = { vertices[0], vertices[1], vertices[2] }
							 * result[ 1 ] = { vertices[0], vertices[2], vertices[3] }
							 * result[ 2 ] = { vertices[0], vertices[3], vertices[4] }
							 * result[ 3 ] = { vertices[0], vertices[4], vertices[5] }
							 * result[ 4 ] = { vertices[0], vertices[5], vertices[6] }
							*/
							final int v0 = vertices[ 0 ];
							int v1 = vertices[ 1 ];

							result = new int[ ( vertexCount - 2 ) * 3 ];
							int resultIndex = 0;

							for ( int vertexIndex = 2; vertexIndex < vertexCount; vertexIndex++ )
							{
								final int v2 = vertices[ vertexIndex ];
								result[ resultIndex++ ] = v0;
								result[ resultIndex++ ] = v1;
								result[ resultIndex++ ] = v2;
								v1 = v2;
							}
						}
						break;

						default :
							throw new AssertionError( "can not build triangles for " + _type );
					}
				}

				_triangles = result;
			}

			return result;
		}
	}
}
