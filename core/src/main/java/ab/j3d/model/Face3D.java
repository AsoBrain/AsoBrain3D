/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a 3D face of a 3D object.
 *
 * @author G.B.M. Rupert
 */
public class Face3D
implements Plane3D
{
	/**
	 * Value used for normal vector if no normal vector can be determined.
	 */
	private static final Vector3D NO_NORMAL = new Vector3D( Double.NaN, 0.0, Double.NaN );

	/**
	 * Vertices of this face.
	 */
	private final List<Vertex3D> _vertices;

	/**
	 * Tessellation of this face ({@code null} = not tessellated yet).
	 */
	@Nullable
	private Tessellation _tessellation;

	/**
	 * Cross product of first and second edge of this face.
	 */
	private Vector3D _cross = null;

	/**
	 * Plane normal. This defines the {@code A}, {@code B}, and
	 * {@code C} variables  in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 * <dl>
	 *  <dt>NOTE:</dt>
	 *  <dd>Using the individual normal components (X,Y,Z) may be more efficient,
	 *   since this does not require a {@link Vector3D} instance.</dd>
	 * </dl>
	 */
	private Vector3D _normal;

	/**
	 * Distance component of plane relative to origin. This defines the
	 * {@code D} variable in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 *
	 * @see #getNormal()
	 */
	private Double _planeDistance;

	/**
	 * Construct new face.
	 *
	 * @param object        Object to which this face belongs.
	 * @param vertexIndices Indices in {@link Object3D#getVertexCoordinates()}.
	 * @param texturePoints Texture coordinates for each vertex (optional).
	 * @param vertexNormals Normal for each vertex (optional).
	 */
	public Face3D( @NotNull final Object3D object, final int @NotNull [] vertexIndices, final float @Nullable [] texturePoints, final @Nullable Vector3D @Nullable [] vertexNormals )
	{
		this( createVertices( object, vertexIndices, texturePoints, vertexNormals ), null );
	}

	/**
	 * Construct new face.
	 *
	 * @param vertices     Vertices used by this face.
	 * @param tessellation Tessellation of this face (optional).
	 */
	public Face3D( @NotNull final List<Vertex3D> vertices, @Nullable final Tessellation tessellation )
	{
		_vertices = vertices;
		_tessellation = tessellation;
		_normal = null;
		_planeDistance = null;
	}

	/**
	 * Construct new face.
	 *
	 * @param normal       Face normal ({@link Plane3D#getNormal()}.
	 * @param vertices     Vertices used by this face.
	 * @param tessellation Tessellation of this face (optional).
	 */
	public Face3D( final Vector3D normal, @NotNull final List<Vertex3D> vertices, @Nullable final Tessellation tessellation )
	{
		_vertices = vertices;
		_tessellation = tessellation;
		_normal = normal;
		_planeDistance = null;
	}

	/**
	 * Construct new face.
	 *
	 * @param normal        Face normal ({@link Plane3D#getNormal()}.
	 * @param planeDistance Distance of plane {@link Plane3D#getDistance()}.
	 * @param vertices      Vertices used by this face.
	 * @param tessellation  Tessellation of this face (optional).
	 */
	public Face3D( @NotNull final Vector3D normal, final double planeDistance, @NotNull final List<Vertex3D> vertices, @Nullable final Tessellation tessellation )
	{
		_vertices = vertices;
		_tessellation = tessellation;
		_normal = normal;
		_planeDistance = planeDistance;
	}

	/**
	 * Construct vertices for face.
	 *
	 * @param object        Object to which this face belongs.
	 * @param vertexIndices Indices in {@link Object3D#getVertexCoordinates()}.
	 * @param texturePoints Texture coordinates for each vertex (optional).
	 * @param vertexNormals Normal for each vertex (optional).
	 *
	 * @return Vertices for face.
	 */
	public static List<Vertex3D> createVertices( @NotNull final Object3D object, final int @NotNull [] vertexIndices, final float @Nullable [] texturePoints, final @Nullable Vector3D @Nullable [] vertexNormals )
	{
		final List<Vector3D> vertexCoordinates = object.getVertexCoordinates();
		final int vertexCount = vertexIndices.length;

		final List<Vertex3D> vertices = new ArrayList<>( vertexCount );

		for ( int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++ )
		{
			final Vertex3D vertex = new Vertex3D( vertexCoordinates, vertexIndices[ vertexIndex ] );
			if ( texturePoints != null )
			{
				final int texturePointIndex = vertexIndex * 2;
				vertex.colorMapU = texturePoints[ texturePointIndex ];
				vertex.colorMapV = texturePoints[ texturePointIndex + 1 ];
			}

			if ( vertexNormals != null )
			{
				vertex.normal = vertexNormals[ vertexIndex ];
			}

			vertices.add( vertex );
		}

		return vertices;
	}

	/**
	 * Get cross product of first two edges of this face. This is an
	 * 'unnormalized normal' of the face and can be used to weigh normals of
	 * adjacent faces. This is used for the Object3D's smoothing algorithm.
	 *
	 * @return Cross product of first two edges of this face.
	 *
	 * @TODO Determine whether we can just use the real normal instead.
	 */
	public Vector3D getCross()
	{
		Vector3D result = _cross;
		if ( result == null )
		{
			final List<Vertex3D> vertices = _vertices;
			final int vertexCount = vertices.size();
			if ( vertexCount >= 3 )
			{
				int vi1 = 0;
				int vi2 = 1;
				int vi3 = 2;

				final Tessellation tessellation = _tessellation;
				if ( tessellation != null )
				{
					final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();

					final Iterator<TessellationPrimitive> it = primitives.iterator();
					if ( it.hasNext() )
					{
						final TessellationPrimitive primitive = it.next();
						final int[] vertexIndices = primitive.getVertices();
						vi1 = vertexIndices[ 2 ];
						vi2 = vertexIndices[ 1 ];
						vi3 = vertexIndices[ 0 ];
					}
				}

				final Vector3D p0 = vertices.get( vi1 ).point;
				final Vector3D p1 = vertices.get( vi2 ).point;
				final Vector3D p2 = vertices.get( vi3 ).point;

				final double u1 = p0.x - p1.x;
				final double u2 = p0.y - p1.y;
				final double u3 = p0.z - p1.z;

				final double v1 = p2.x - p1.x;
				final double v2 = p2.y - p1.y;
				final double v3 = p2.z - p1.z;

				result = Vector3D.cross( u1, u2, u3, v1, v2, v3 );
			}
			else
			{
				result = Vector3D.ZERO;
			}
			_cross = result;
		}

		return result;
	}

	public double getDistance()
	{
		Double result = _planeDistance;
		if ( result == null )
		{
			final List<Vertex3D> vertices = _vertices;
			if ( !vertices.isEmpty() )
			{
				final Vector3D normal = getNormal();
				final Vertex3D vertex = vertices.get( 0 );
				result = Vector3D.dot( normal, vertex.point );
			}
			else
			{
				result = 0.0;
			}

			_planeDistance = result;
		}
		return result;
	}

	public Vector3D getNormal()
	{
		Vector3D result = _normal;
		if ( result == null )
		{
			final Vector3D cross = getCross();
			final double length = cross.length();
			result = ( length > 0.0 ) ? new Vector3D( cross.x / length, cross.y / length, cross.z / length ) : NO_NORMAL;
			_normal = result;
		}
		return result;
	}

	/**
	 * Clears the cached face normal, such that {@link #getNormal()} will return
	 * an updated normal.
	 */
	public void updateNormal()
	{
		_normal = null;
	}

	public boolean isTwoSided()
	{
		return false;
	}

	/**
	 * Returns the vertices of this face.
	 *
	 * @return Vertices of this face.
	 */
	public List<Vertex3D> getVertices()
	{
		return Collections.unmodifiableList( _vertices );
	}

	/**
	 * Get vertex with the specified index.
	 *
	 * @param index Index of vertex in face.
	 *
	 * @return Vertex.
	 *
	 * @throws IndexOutOfBoundsException if {@code index} is out of bounds.
	 */
	public Vertex3D getVertex( final int index )
	{
		return _vertices.get( index );
	}

	/**
	 * Get number of vertices that define this face.
	 *
	 * @return Number of vertices.
	 */
	public int getVertexCount()
	{
		return _vertices.size();
	}

	/**
	 * Get vertex normal with the specified index.
	 *
	 * @param index Index of vertex in face.
	 *
	 * @return Vertex normal.
	 *
	 * @throws IndexOutOfBoundsException if {@code index} is out of bounds.
	 */
	@NotNull
	public Vector3D getVertexNormal( final int index )
	{
		final Vertex3D vertex = _vertices.get( index );
		Vector3D result = vertex.normal;
		if ( result == null )
		{
			result = getNormal();
			vertex.setNormal( result );
		}
		return result;
	}

	/**
	 * Get outlines of face.
	 *
	 * @return Outlines of tessellated shapes.
	 */
	@NotNull
	public List<int[]> getOutlines()
	{
		final Tessellation tessellation = getTessellation();
		return tessellation.getOutlines();
	}

	/**
	 * This method returns a tessellated version of this face.
	 *
	 * @return {@link Tessellation} of this face.
	 */
	@NotNull
	public Tessellation getTessellation()
	{
		Tessellation result = _tessellation;
		if ( result == null )
		{
			final int vertexCount = _vertices.size();

			final int[] outline = new int[ vertexCount + 1 ];
			for ( int i = 0; i < vertexCount; i++ )
			{
				outline[ i ] = i;
			}
			outline[ outline.length - 1 ] = outline[ 0 ];

			final List<TessellationPrimitive> primitives;
			if ( vertexCount >= 3 )
			{
				final int[] vertexIndices = new int[ vertexCount ];
				final int lastVertex = vertexCount - 1;
				for ( int i = 0; i < vertexIndices.length; i++ )
				{
					vertexIndices[ i ] = lastVertex - i;
				}
				primitives = Collections.singletonList( new TriangleFan( vertexIndices ) );
			}
			else
			{
				primitives = Collections.emptyList();
			}

			result = new Tessellation( Collections.singletonList( outline ), primitives );
			_tessellation = result;
		}

		return result;
	}

	/**
	 * Set tessellation of this face.
	 *
	 * @param tessellation Tessellation to use for this face.
	 */
	public void setTessellation( @NotNull final Tessellation tessellation )
	{
		_tessellation = tessellation;
	}

	/**
	 * Returns the intersection point between the face and the given ray.
	 * If no intersection exists {@code null} will be returned. In the
	 * following cases, there is no intersection:
	 * <ol>
	 *  <li>The ray is parallel to the face's plane;</li>
	 *  <li>The ray does not point towards the face;</li>
	 *  <li>The ray intersects the face's plane outside the face.</li>
	 * </ol>
	 *
	 * @param ray Ray to get intersection from.
	 *
	 * @return Intersection point with the ray, if any.
	 */
	@Nullable
	public Vector3D getIntersection( @NotNull final Ray3D ray )
	{
		Vector3D result = null;

		final int vertexCount = getVertexCount();
		if ( vertexCount >= 3 )
		{
			result = GeometryTools.getIntersectionBetweenRayAndPlane( this, ray );

			if ( result != null )
			{
				boolean inside = false;

				final Tessellation tessellation = getTessellation();

				for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
				{
					final int[] triangles = primitive.getTriangles();

					for ( int i = 0; i < triangles.length; i += 3 )
					{
						final Vector3D v1 = _vertices.get( triangles[ i ] ).point;
						final Vector3D v2 = _vertices.get( triangles[ i + 1 ] ).point;
						final Vector3D v3 = _vertices.get( triangles[ i + 2 ] ).point;

						if ( GeometryTools.isPointInsideTriangle( v1, v2, v3, result ) )
						{
							inside = true;
							break;
						}
					}

					if ( inside )
					{
						break;
					}
				}

				if ( !inside )
				{
					result = null;
				}
			}
		}

		return result;
	}

	@Override
	public int hashCode()
	{
		return _vertices.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		final boolean result;
		if ( obj == this )
		{
			result = true;
		}
		else if ( obj instanceof Face3D )
		{
			final Face3D other = (Face3D)obj;
			result = _vertices.equals( other._vertices ) &&
			         ( ( _tessellation == null ) ? ( other._tessellation == null ) : ( ( other._tessellation != null ) && _tessellation.equals( other._tessellation ) ) );
		}
		else
		{
			result = false;
		}
		return result;
	}
}
