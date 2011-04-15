/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
import org.jetbrains.annotations.*;

/**
 * This class defines a 3D face of a 3D object.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ ($Date$, $Author$)
 */
public class Face3D
	implements Plane3D
{
	/**
	 * Value used for normal vector if no normal vector can be determined.
	 */
	private static final Vector3D NO_NORMAL = Vector3D.INIT.set( Double.NaN, 0.0, Double.NaN );

	/**
	 * Object to which this face belongs.
	 */
	private final Object3D _object;

	/**
	 * Vertices of this face.
	 */
	public final List<Vertex> vertices;

	/**
	* Smoothing flag this face. Smooth faces are used to approximate
	 * smooth/curved/rounded parts of objects.
	 * <p />
	 * This information would typically be used to select the most appropriate
	 * shading algorithm.
	*/
	public boolean smooth;

	/**
	 * Material of this face.
	 */
	public Material material;

	/**
	 * Flag to indicate that the plane is two-sided. This means, that the
	 * plane is 'visible' on both sides.
	 */
	private final boolean _twoSided;

	/**
	 * X component of cross product of first and second edge of this face.
	 */
	public final double _crossX;

	/**
	 * Y component of cross product of first and second edge of this face.
	 */
	public final double _crossY;

	/**
	 * Z component of cross product of first and second edge of this face.
	 */
	public final double _crossZ;

	/**
	 * Distance component of plane relative to origin. This defines the
	 * <code>D</code> variable in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 *
	 * @see     #normal
	 */
	public final double planeDistance;

	/**
	 * Plane normal. This defines the <code>A</code>, <code>B</code>, and
	 * <code>C</code> variables  in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 * <dl>
	 *  <dt>NOTE:</dt>
	 *  <dd>Using the individual normal components (X,Y,Z) may be more efficient,
	 *   since this does not require a {@link Vector3D} instance.</dd>
	 * </dl>
	 */
	public final Vector3D normal;

	/**
	 * Tessellation of this face.
	 */
	@Nullable
	private Tessellation _tessellation = null;

	/**
	 * Construct new face.
	 *
	 * @param   object          Object to which this face belongs.
	 * @param   vertexIndices   Indices in {@link Object3D#_vertexCoordinates}.
	 * @param   material        Material to apply to the face.
	 * @param   texturePoints   Texture coordinates for each vertex (optional).
	 * @param   vertexNormals   Normal for each vertex (optional).
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public Face3D( @NotNull final Object3D object, @NotNull final int[] vertexIndices, @Nullable final Material material, @Nullable final float[] texturePoints, @Nullable final Vector3D[] vertexNormals, final boolean smooth, final boolean twoSided )
	{
		this( object, createVertices( object, vertexIndices, texturePoints, vertexNormals ), null, material, smooth, twoSided );
	}

	/**
	 * Construct new face.
	 *
	 * @param   object          Object to which this face belongs.
	 * @param   vertices        Vertices used by this face.
	 * @param   tessellation     Tessellation of this face (optional).
	 * @param   material        Material to apply to the face.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public Face3D( @NotNull final Object3D object, @NotNull final List<Vertex> vertices, @Nullable final Tessellation tessellation, @Nullable final Material material, final boolean smooth, final boolean twoSided )
	{
		_object = object;
		this.vertices = vertices;
		_tessellation = tessellation;
		this.material = material;
		this.smooth = smooth;
		_twoSided = twoSided;

		final int vertexCount = vertices.size();
		if ( vertexCount >= 3 )
		{
			int vi1 = 0;
			int vi2 = 1;
			int vi3 = 2;

			if ( tessellation != null )
			{
				final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();
				final Iterator<TessellationPrimitive> i = primitives.iterator();
				if ( i.hasNext() )
				{
					final TessellationPrimitive primitive = i.next();
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

			final double crossX = u2 * v3 - u3 * v2;
			final double crossY = u3 * v1 - u1 * v3;
			final double crossZ = u1 * v2 - u2 * v1;

			final double l = Math.sqrt( crossX * crossX + crossY * crossY + crossZ * crossZ );
			final Vector3D n = ( l > 0.0 ) ? Vector3D.INIT.set( crossX / l, crossY / l, crossZ / l ) : NO_NORMAL;
			final double d = ( l > 0.0 ) ? Vector3D.dot( n.x, n.y, n.z, p1.x, p1.y, p1.z ) : 0.0;

			_crossX = crossX;
			_crossY = crossY;
			_crossZ = crossZ;
			normal = n;
			planeDistance = d;
		}
		else
		{
			_crossX = 0.0;
			_crossY = 0.0;
			_crossZ = 0.0;
			normal = NO_NORMAL;
			planeDistance = 0.0;
		}
	}

	/**
	 * Construct vertices for face.
	 *
	 * @param   object          Object to which this face belongs.
	 * @param   vertexIndices   Indices in {@link Object3D#_vertexCoordinates}.
	 * @param   texturePoints   Texture coordinates for each vertex (optional).
	 * @param   vertexNormals   Normal for each vertex (optional).
	 *
	 * @return  Vertices for face.
	 */
	public static List<Vertex> createVertices( @NotNull final Object3D object, @NotNull final int[] vertexIndices, @Nullable final float[] texturePoints, @Nullable final Vector3D[] vertexNormals )
	{
		final List<Vector3D> vertexCoordinates = object._vertexCoordinates;
		final int vertexCount = vertexIndices.length;

		final List<Vertex> vertices = new ArrayList<Vertex>( vertexCount );

		for ( int vertexIndex = 0 ; vertexIndex < vertexCount; vertexIndex++ )
		{
			final Vertex vertex = new Vertex( vertexCoordinates, vertexIndices[ vertexIndex ] );
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

	@Override
	public double getDistance()
	{
		return planeDistance;
	}

	@Override
	public Vector3D getNormal()
	{
		return normal;
	}

	/**
	 * Get object to which this face belongs.
	 *
	 * @return  Object to which this face belongs.
	 */
	public Object3D getObject()
	{
		return _object;
	}

	/**
	 * Returns the vertices of this face.
	 *
	 * @return  Vertices of this face.
	 */
	public List<Vertex> getVertices()
	{
		return Collections.unmodifiableList( vertices );
	}

	/**
	 * Returns the smoothing flag of this face. Smooth faces are used to
	 * approximate smooth/curved/rounded parts of objects.
	 *
	 * <p>
	 * This information would typically be used to select the most appropriate
	 * shading algorithm.
	 *
	 * @return  Smoothing flag.
	 */
	public boolean isSmooth()
	{
		return smooth;
	}

	/**
	 * Returns the material of this face.
	 *
	 * @return  Material of this face.
	 */
	public Material getMaterial()
	{
		return material;
	}

	@Override
	public boolean isTwoSided()
	{
		return _twoSided;
	}

	/**
	 * Get vertex with the specified index.
	 *
	 * @param   index   Index of vertex in face.
	 *
	 * @return  Vertex.
	 *
	 * @throws  IndexOutOfBoundsException if <code>index</code> is out of bounds.
	 */
	public Vertex getVertex( final int index )
	{
		return vertices.get( index );
	}

	/**
	 * Get number of vertices that define this face.
	 *
	 * @return  Number of vertices.
	 */
	public int getVertexCount()
	{
		return vertices.size();
	}

	/**
	 * Get vertex normal with the specified index.
	 *
	 * @param   index   Index of vertex in face.
	 *
	 * @return  Vertex normal.
	 *
	 * @throws  IndexOutOfBoundsException if <code>index</code> is out of bounds.
	 */
	public Vector3D getVertexNormal( final int index )
	{
		final Vertex vertex = vertices.get( index );

		Vector3D result = vertex.normal;
		if ( result == null )
		{
			final double[] vertexNormals = _object.getVertexNormals();
			final int i = vertex.vertexCoordinateIndex * 3;
			result = Vector3D.INIT.set( vertexNormals[ i ], vertexNormals[ i + 1 ], vertexNormals[ i + 2 ] );
			vertex.normal = result;
		}
		return result;
	}

	/**
	 * Get outlines of face.
	 *
	 * @return  Outlines of tessellated shapes.
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
	 * @return  {@link Tessellation} of this face.
	 */
	@NotNull
	public Tessellation getTessellation()
	{
		Tessellation result = _tessellation;
		if ( result == null )
		{
			final List<Vertex> vertices = this.vertices;
			final int vertexCount = vertices.size();

			final int[] outline = new int[ vertexCount + 1 ];
			final int lastVertex = vertexCount - 1;
			for ( int i = 0; i < vertexCount; i++ )
			{
				outline[ i ] = lastVertex - i;
			}
			outline[ outline.length - 1 ] = outline[ 0 ];

			final List<TessellationPrimitive> primitives;
			if ( vertexCount >= 3 )
			{
				primitives = Collections.<TessellationPrimitive>singletonList( new TriangleFan( outline ) );
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
	 * @param   tessellation     Tessellation to use for this face.
	 */
	public void setTessellation( @NotNull final Tessellation tessellation )
	{
		_tessellation = tessellation;
	}

	/**
	 * Defines a vertex of a face.
	 */
	public static final class Vertex
	{
		/**
		 * Coordinates of vertex.
		 */
		public final Vector3D point;

		/**
		 * Index of the vertex in the {@link Object3D#_vertexCoordinates} array.
		 */
		public int vertexCoordinateIndex;

		/**
		 * Color map U coordinate.
		 */
		public float colorMapU;

		/**
		 * Color map V coordinate.
		 */
		public float colorMapV;

		/**
		 * Vertex normal.
		 */
		Vector3D normal;

		/**
		 * Construct vertex.
		 *
		 * @param   point                   Coordinates of vertex.
		 * @param   vertexCoordinateIndex   Index of vertex coordinates.
		 * @param   colorMapU               Color map U coordinate.
		 * @param   colorMapV               Color map V coordinate.
		 */
		public Vertex( final Vector3D point, final int vertexCoordinateIndex, final float colorMapU, final float colorMapV )
		{
			this.point = point;
			this.vertexCoordinateIndex = vertexCoordinateIndex;
			this.colorMapU = colorMapU;
			this.colorMapV = colorMapV;
			normal = null;
		}

		/**
		 * Construct vertex.
		 *
		 * @param   point                   Coordinates of vertex.
		 * @param   vertexCoordinateIndex   Index of vertex coordinates.
		 */
		public Vertex( final Vector3D point, final int vertexCoordinateIndex )
		{
			this.point = point;
			this.vertexCoordinateIndex = vertexCoordinateIndex;
			normal = null;
			colorMapU = Float.NaN;
			colorMapV = Float.NaN;
		}

		/**
		 * Construct vertex.
		 *
		 * @param   vertexCoordinates       Vertex coordinates.
		 * @param   vertexCoordinateIndex   Index of vertex coordinates.
		 */
		public Vertex( final List<Vector3D> vertexCoordinates, final int vertexCoordinateIndex )
		{
			this( vertexCoordinates.get( vertexCoordinateIndex ), vertexCoordinateIndex );
		}

		/**
		 * Set vertex normal.
		 *
		 * @param   normal  Vertex normal.
		*/
		public void setNormal( final Vector3D normal )
		{
			this.normal = normal;
		}
	}

	/**
	 * Returns the intersection point between the face and the given ray.
	 * If no intersection exists <code>null</code> will be returned. In the
	 * following cases, there is no intersection:
	 * <ol>
	 *  <li>The ray is parallel to the face's plane;</li>
	 *  <li>The ray does not point towards the face;</li>
	 *  <li>The ray intersects the face's plane outside the face.</li>
	 * </ol>
	 *
	 * @param   ray     Ray to get intersection from.
	 *
	 * @return  Intersection point with the ray, if any.
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

					for ( int i = 0 ; i < triangles.length ; i += 3 )
					{
						final Vector3D v1 = vertices.get( triangles[ i ] ).point;
						final Vector3D v2 = vertices.get( triangles[ i + 1 ] ).point;
						final Vector3D v3 = vertices.get( triangles[ i + 2 ] ).point;

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
}
