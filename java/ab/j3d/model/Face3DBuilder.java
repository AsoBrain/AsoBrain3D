/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import ab.j3d.geom.*;

/**
 * This class can be used together with an {@link Object3D} to build
 * {@link Face3D} instances.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Face3DBuilder
{
	/**
	 * 3D object to build face(s) for. Only used for vertex coordinate indices.
	 *
	 * @see Object3D#getVertexIndex
	 */
	private Object3D _object = null;

	/**
	 * Face vertices.
	 */
	private final HashList<Vertex3D> _vertices = new HashList<Vertex3D>();

	/**
	 * Face outlines.
	 */
	private final List<int[]> _outlines = new ArrayList<int[]>();

	/**
	 * Independent triangles.
	 */
	private final IntArray _triangles = new IntArray( 24  );

	/**
	 * Independent quads.
	 */
	private final IntArray _quads = new IntArray( 32 );

	/**
	 * Tessellation primitives. The independent triangles and quads will be
	 * added to this list when the actual face is build.
	 */
	private final Collection<TessellationPrimitive> _primitives = new ArrayList<TessellationPrimitive>();

	/**
	 * Face normal ({@code null} => derive from vertices).
	 */
	private Vector3D _normal = null;

	/**
	 * Create uninitialized 3D face builder. The caller must still call the
	 * {@link #initialize} before starting the build process.
	 */
	public Face3DBuilder()
	{
	}

	/**
	 * Create 3D face builder for the given object and face normal.
	 *
	 * @param   object  3D object to build face(s) for.
	 * @param   normal  Face normal ({@code null} => derive from vertices).
	 */
	public Face3DBuilder( final Object3D object, final Vector3D normal )
	{
		initialize( object, normal );
	}

	/**
	 * Add outline to this face.
	 *
	 * @param   outline   Outline to add to this face.
	 */
	public void addOutline( final int[] outline )
	{
		_outlines.add( outline );
	}

	/**
	 * Add primitive to this face.
	 *
	 * Note that triangle lists are not added directly, but are added to a
	 * shared triangle list. The same is done for quad lists.
	 *
	 * @param   primitive   Primitive to add to this face.
	 */
	public void addPrimitive( final TessellationPrimitive primitive )
	{
		if ( primitive instanceof TriangleList )
		{
			addTriangles( primitive.getVertices() );
		}
		else if ( primitive instanceof QuadList )
		{
			addQuads( primitive.getVertices() );
		}
		else
		{
			_primitives.add( primitive );
		}
	}

	/**
	 * Add single untextured quad to this face.
	 *
	 * @param   v1  First vertex of quad.
	 * @param   v2  Second vertex of quad.
	 * @param   v3  Third vertex of quad.
	 * @param   v4  Fourth vertex of quad.
	 */
	public void addQuad( final Vector3D v1, final Vector3D v2, final Vector3D v3, final Vector3D v4 )
	{
		addQuad( getVertexIndex( v1, 0.0f, 0.0f ), getVertexIndex( v2, 0.0f, 0.0f ), getVertexIndex( v3, 0.0f, 0.0f ), getVertexIndex( v4, 0.0f, 0.0f ) );
	}

	/**
	 * Add single quad to this face.
	 *
	 * @param   v1  First vertex of quad.
	 * @param   v2  Second vertex of quad.
	 * @param   v3  Third vertex of quad.
	 * @param   v4  Fourth vertex of quad.
	 */
	public void addQuad( final Vertex3D v1, final Vertex3D v2, final Vertex3D v3, final Vertex3D v4 )
	{
		addQuad( getVertexIndex( v1 ), getVertexIndex( v2 ), getVertexIndex( v3 ), getVertexIndex( v4 ) );
	}

	/**
	 * Add single quad to this face.
	 *
	 * @param   v1  First vertex of quad.
	 * @param   v2  Second vertex of quad.
	 * @param   v3  Third vertex of quad.
	 * @param   v4  Fourth vertex of quad.
	 */
	public void addQuad( final int v1, final int v2, final int v3, final int v4 )
	{
		final IntArray quads = _quads;
		quads.ensureRemainingCapacity( 4 );
		quads.add( v1 );
		quads.add( v2 );
		quads.add( v3 );
		quads.add( v4 );
	}

	/**
	 * Add multiple quads to this face.
	 *
	 * @param   vertices    Quad vertices (length must be multiple of 4).
	 */
	public void addQuads( final Collection<Vertex3D> vertices )
	{
		final IntArray quads = _quads;
		quads.ensureRemainingCapacity( vertices.size() );

		for ( final Vertex3D vertex : vertices )
		{
			quads.add( getVertexIndex( vertex ) );
		}
	}

	/**
	 * Add multiple quads to this face.
	 *
	 * @param   vertices    Quad vertices (length must be multiple of 4).
	 */
	public void addQuads( final int... vertices )
	{
		_quads.add( vertices );
	}

	/**
	 * Add single triangle to this face.
	 *
	 * @param   v1  First vertex of triangle.
	 * @param   v2  Second vertex of triangle.
	 * @param   v3  Third vertex of triangle.
	 */
	public void addTriangle( final Vertex3D v1, final Vertex3D v2, final Vertex3D v3 )
	{
		addTriangle( getVertexIndex( v1 ), getVertexIndex( v2 ), getVertexIndex( v3 ) );
	}

	/**
	 * Add single triangle to this face.
	 *
	 * @param   v1  First vertex of triangle.
	 * @param   v2  Second vertex of triangle.
	 * @param   v3  Third vertex of triangle.
	 */
	public void addTriangle( final int v1, final int v2, final int v3 )
	{
		final IntArray triangles = _triangles;
		triangles.ensureRemainingCapacity( 3 );
		triangles.add( v1 );
		triangles.add( v2 );
		triangles.add( v3 );
	}

	/**
	 * Add multiple triangles to this face.
	 *
	 * @param   vertices    Triangle vertices (length must be multiple of 3).
	 */
	public void addTriangles( final Collection<Vertex3D> vertices )
	{
		final IntArray triangles = _triangles;
		triangles.ensureRemainingCapacity( vertices.size() );

		for ( final Vertex3D vertex : vertices )
		{
			triangles.add( getVertexIndex( vertex ) );
		}
	}

	/**
	 * Add multiple triangles to this face.
	 *
	 * @param   vertices    Triangle vertices (length must be multiple of 3).
	 */
	public void addTriangles( final int... vertices )
	{
		_triangles.add( vertices );
	}

	/**
	 * Build the actual face.
	 *
	 * @return  {@link Face3D} that was created.
	 */
	public Face3D buildFace3D()
	{
		if ( isEmpty() )
		{
			throw new IllegalStateException( "can't build face from nothing" );
		}

		return new Face3D( getNormal(), createVertices(), new Tessellation( createOutlines(), createPrimitives() ) );
	}

	/**
	 * This method is used by {@link #buildFace3D()} to create the result
	 * outlines.
	 *
	 * @return  Outlines.
	 */
	protected List<int[]> createOutlines()
	{
		return new ArrayList<int[]>( _outlines );
	}

	/**
	 * This method is used by {@link #buildFace3D()} to create the result
	 * primitives.
	 *
	 * @return  Primitives.
	 */
	protected List<TessellationPrimitive> createPrimitives()
	{
		final List<TessellationPrimitive> result;

		final IntArray triangles = _triangles;
		final IntArray quads = _quads;
		final Collection<TessellationPrimitive> primitives = _primitives;

		final boolean hasTriangles = !triangles.isEmpty();
		final boolean hasQuads = !quads.isEmpty();
		if ( hasTriangles || hasQuads )
		{
			final int primitiveCount = primitives.size() + ( hasTriangles ? 1 : 0 ) + ( hasQuads ? 1 : 0 );
			result = new ArrayList<TessellationPrimitive>( primitiveCount );
			result.addAll( primitives );

			if ( hasTriangles )
			{
				result.add( new TriangleList( triangles.toArray() ) );
			}

			if ( hasQuads )
			{
				result.add( new QuadList( quads.toArray() ) );
			}
		}
		else if ( !primitives.isEmpty() )
		{
			result = new ArrayList<TessellationPrimitive>( primitives );
		}
		else
		{
			result = Collections.emptyList();
		}

		return result;
	}

	/**
	 * This method is used by {@link #buildFace3D()} to create the result
	 * vertices.
	 *
	 * @return  Outlines.
	 */
	protected List<Vertex3D> createVertices()
	{
		return new ArrayList<Vertex3D>( _vertices );
	}

	/**
	 * Get face normal.
	 *
	 * @return  Face normal ({@code null} => derive from vertices).
	 */
	public Vector3D getNormal()
	{
		return _normal;
	}

	/**
	 * Get 3D object for which face(s) are built.
	 *
	 * @return  3D object for which face(s) are built.
	 */
	public Object3D getObject()
	{
		return _object;
	}

	/**
	 * Get index of vertex. If this face did not already contain the vertex, it
	 * will be added; otherwise, the index of the existing vertex is returned.
	 *
	 * @param   vertex  Vertex to get index of.
	 *
	 * @return  Vertex index.
	 */
	public int getVertexIndex( final Vertex3D vertex )
	{
		return getVertexIndex( vertex.point, vertex.colorMapU, vertex.colorMapV );
	}

	/**
	 * Get index of vertex. If this face did not already contain the vertex, it
	 * will be added; otherwise, the index of the existing vertex is returned.
	 *
	 * @param   point       Coordinates of vertex.
	 * @param   colorMapU   Color map U coordinate.
	 * @param   colorMapV   Color map V coordinate.
	 *
	 * @return  Vertex index.
	 */
	public int getVertexIndex( final Vector3D point, final float colorMapU, final float colorMapV )
	{
		return _vertices.indexOfOrAdd( new Vertex3D( point, _object.getVertexIndex( point ), colorMapU, colorMapV ) );
	}

	/**
	 * Re-initialize the builder. This can be used after {@link #buildFace3D}
	 * to start building another face.
	 *
	 * @param   object  3D object to build face(s) for.
	 * @param   normal  Face normal ({@code null} => derive from vertices).
	 */
	public void initialize( final Object3D object, final Vector3D normal )
	{
		reset();
		_object = object;
		_normal = normal;
	}

	/**
	 * Test whether there is something to build a face from or not.
	 *
	 * @return  {@code true} if there is nothing to build;
	 *          {@code false} if face can be build.
	 */
	public boolean isEmpty()
	{
		return _triangles.isEmpty() && _quads.isEmpty() && _primitives.isEmpty() && _outlines.isEmpty();
	}

	/**
	 * Resets the builder. This should be called after {@link #buildFace3D} so
	 * the builder can be used to build another face.
	 */
	public void reset()
	{
		_vertices.clear();
		_outlines.clear();
		_primitives.clear();
		_triangles.clear();
		_quads.clear();
	}

	/**
	 * Set face normal.
	 *
	 * @param   normal  Face normal ({@code null} => derive from vertices).
	 */
	public void setNormal( final Vector3D normal )
	{
		_normal = normal;
	}

	/**
	 * Set 3D object to build face(s) for. Only used for vertex coordinate
	 * indices.
	 *
	 * @param   object  3D object to build face(s) for.
	 *
	 * @see Object3D#getVertexIndex
	 */
	public void setObject( final Object3D object )
	{
		_object = object;
	}
}
