/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2009
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

import java.awt.geom.Point2D;

import ab.j3d.Material;
import ab.j3d.Vector3D;
import ab.j3d.geom.Polygon3D;

import com.numdata.oss.AugmentedArrayList;
import com.numdata.oss.AugmentedList;

/**
 * This class defines a 3D face of a 3D object.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Face3D
	implements Polygon3D
{
	/**
	 * Value used for normal vector if no normal vector can be determined.
	 */
	private static final Vector3D NO_NORMAL = Vector3D.INIT.set( Double.NaN , 0.0 , Double.NaN );

	/**
	 * Object to which this face belongs.
	 */
	private final Object3D _object;

	/**
	 * Vertices of this face.
	 */
	public final AugmentedList<Vertex> vertices;

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
	final double _crossX;

	/**
	 * Y component of cross product of first and second edge of this face.
	 */
	final double _crossY;

	/**
	 * Z component of cross product of first and second edge of this face.
	 */
	final double _crossZ;

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
	 * Triangulated face. Stored as triplets of vertex indices in this plane.
	 */
	private int[] _triangles = null;

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
	public Face3D( final Object3D object , final int[] vertexIndices , final Material material , final Point2D.Float[] texturePoints , final Vector3D[] vertexNormals , final boolean smooth , final boolean twoSided )
	{
		if ( object == null )
			throw new NullPointerException( "object" );

		final double[] vertexCoordinates = object._vertexCoordinates;
		final int vertexCount = vertexIndices.length;

		final AugmentedList<Vertex> vertices = new AugmentedArrayList<Vertex>( vertexCount );

		for ( int vertexIndex = 0 ; vertexIndex < vertexCount; vertexIndex++ )
		{
			final Vertex vertex = new Vertex( vertexCoordinates , vertexIndices[ vertexIndex ] );
			if ( texturePoints != null )
			{
				final Point2D.Float texturePoint = texturePoints[ vertexIndex ];
				vertex.colorMapU = texturePoint.x;
				vertex.colorMapV = texturePoint.y;
			}

			if ( vertexNormals != null )
			{
				vertex.normal = vertexNormals[ vertexIndex ];
			}

			vertices.add( vertex );
		}

		if ( vertexCount >= 3 )
		{
			int i = vertices.get( 1 ).vertexCoordinateIndex * 3;
			final double x0 = vertexCoordinates[ i ];
			final double y0 = vertexCoordinates[ i + 1 ];
			final double z0 = vertexCoordinates[ i + 2 ];

			i = vertices.get( 0 ).vertexCoordinateIndex * 3;
			final double u1 = vertexCoordinates[ i ] - x0;
			final double u2 = vertexCoordinates[ i + 1 ] - y0;
			final double u3 = vertexCoordinates[ i + 2 ] - z0;

			i = vertices.get( 2 ).vertexCoordinateIndex * 3;
			final double v1 = vertexCoordinates[ i ] - x0;
			final double v2 = vertexCoordinates[ i + 1 ] - y0;
			final double v3 = vertexCoordinates[ i + 2 ] - z0;

			final double crossX = u2 * v3 - u3 * v2;
			final double crossY = u3 * v1 - u1 * v3;
			final double crossZ = u1 * v2 - u2 * v1;

			final double l = Math.sqrt( crossX * crossX + crossY * crossY + crossZ * crossZ );
			final Vector3D n = ( l > 0.0 ) ? Vector3D.INIT.set( crossX / l, crossY / l, crossZ / l ) : NO_NORMAL;
			final double d = ( l > 0.0 ) ? Vector3D.dot( n.x , n.y , n.z , x0 , y0 , z0 ) : 0.0;

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

		_object        = object;
		this.vertices = vertices;
		this.material = material;
		this.smooth = smooth;
		_twoSided = twoSided;
	}

	public double getDistance()
	{
		return planeDistance;
	}

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
			result = Vector3D.INIT.set( vertexNormals[ i ] , vertexNormals[ i + 1 ] , vertexNormals[ i + 2 ] );
			vertex.normal = result;
		}
		return result;
	}

	public double getX( final int index )
	{
		return vertices.get( index ).point.x;
	}

	public double getY( final int index )
	{
		return vertices.get( index ).point.y;
	}

	public double getZ( final int index )
	{
		return vertices.get( index ).point.z;
	}

	/**
	 * This method returns a triangulated version of this face. The result
	 * is an array containing indices of vertices in this face. If this
	 * face has no triangles, <code>null</code> is returned.
	 * <dl>
	 *  <dt>NOTE:</dt>
	 *  <dd>This method caches its result, so it can be called multiple times
	 *      without consequence. However, the result is not cloned, so
	 *      THE CONTENTS OF THE RESULT SHOULD NOT BE MODIFIED.</dd>
	 * </dl>
	 *
	 * @return  Array with indices to vertices in this face;
	 *          <code>null</code> if this face has no triangles.
	 */
	public int[] triangulate()
	{
		int[] result = _triangles;
		if ( result == null )
		{
			final int vertexCount = getVertexCount();
			if ( vertexCount >= 3 )
			{
				final int triangleCount = vertexCount - 2;

				result = new int[ triangleCount * 3 ];

				int rc = 0;

				for ( int k = 1 ; k <= triangleCount ; k++ )
				{
					result[ rc++ ] = 0;
					result[ rc++ ] = k;
					result[ rc++ ] = k + 1;
				}
				_triangles = result;
			}
		}

		return result;
	}

	/**
	 * Defines a vertex of a face.
	 */
	public final class Vertex
	{
		/**
		 * Coordinates of vertex.
		 */
		public final Vector3D point;

		/**
		 * Index of the vertex in the {@link Object3D#_vertexCoordinates} array.
		 * <p />
		 * Because coordinates in the {@link Object3D#_vertexCoordinates} array
		 * are stored with a triplet for each vertex, these indices should be
		 * multiplied by 3 to get the 'real' index.
		 */
		public final int vertexCoordinateIndex;

		/**
		 * Color map U coordinate.
		 */
		public float colorMapU = Float.NaN;

		/**
		 * Color map V coordinate.
		 */
		public float colorMapV = Float.NaN;

		/**
		 * Cached vertex normal.
		 */
		Vector3D normal;

		/**
		 * Construct vertex.
		 *
		 * @param   vertexCoordinates       Vertex coordinates.
		 * @param   vertexCoordinateIndex   Index of vertex coordinates.
		 */
		public Vertex( final double[] vertexCoordinates , final int vertexCoordinateIndex )
		{
			final int i = vertexCoordinateIndex * 3;
			point = Vector3D.INIT.set( vertexCoordinates[ i ] , vertexCoordinates[ i + 1 ] , vertexCoordinates[ i + 2 ] );
			this.vertexCoordinateIndex = vertexCoordinateIndex;
			normal = null;
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
}
