/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2009 Peter S. Heijnen
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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ab.j3d.Bounds3D;
import ab.j3d.Bounds3DBuilder;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.model.Object3D;

/**
 * This class defines a 3D mesh.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Mesh3D
{
	/**
	 * Faces in this object (unmodifiable).
	 */
	public final List<Face> faces;

	/**
	 * Bounding box of object in the local coordinate system.
	 *
	 * @see     #getOrientedBoundingBox()
	 */
	private Bounds3D _orientedBoundingBox;

	/**
	 * Helper for collision tests.
	 *
	 * @see     CollisionNode
	 */
	private CollisionNode _collisionNode;

	/**
	 * Construct mesh.
	 *
	 * @param   faces   Faces in mesh.
	 */
	Mesh3D( final List<Face> faces )
	{
		this.faces = faces;
		_orientedBoundingBox = null;
		_collisionNode = null;
	}

	/**
	 * Add bounds of this object to a {@link Bounds3DBuilder}. Optionally.
	 *
	 * @param   builder     Builder to add bounds to.
	 * @param   xform       Transform to apply to vertex coordinates.
	 */
	public void addBounds( final Bounds3DBuilder builder , final Matrix3D xform )
	{
		if ( ( xform != null ) && ( xform != Matrix3D.INIT ) && ( !Matrix3D.INIT.equals( xform ) ) )
		{
			for ( final Face face : faces )
			{
				for ( final Vertex vertex : face.vertices )
				{
					final Vector3D point = vertex.getPoint();
					builder.addPoint( xform.transformX( point ) , xform.transformY( point ) , xform.transformZ( point ) );
				}
			}
		}
		else
		{
			final Bounds3D orientedBoundingBox = getOrientedBoundingBox();
			builder.addPoint( orientedBoundingBox.v1 );
			builder.addPoint( orientedBoundingBox.v2 );
		}
	}

	/**
	 * Test if this mesh collides with another.
	 *
	 * @param   fromOtherToThis     Transformation from other mesh to this.
	 * @param   other               Mesh to test collision with.
	 *
	 * @return  <code>true</code> if the objects collide;
	 *          <code>false</code> otherwise.
	 */
	public boolean collidesWith( final Matrix3D fromOtherToThis , final Mesh3D other )
	{
		final boolean result;

		final Bounds3D thisOrientedBoundingBox  = getOrientedBoundingBox();
		final Bounds3D otherOrientedBoundingBox = other.getOrientedBoundingBox();

		if ( GeometryTools.testOrientedBoundingBoxIntersection( thisOrientedBoundingBox , fromOtherToThis , otherOrientedBoundingBox ) )
		{
			final CollisionNode thisCollisionNode  = getCollisionNode();
			final CollisionNode otherCollisionNode = other.getCollisionNode();

			result = thisCollisionNode.collidesWith( otherCollisionNode , fromOtherToThis );
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Find intersections between this object and the specified ray. The ray
	 * starts at <code>rayOrigin</code> and points in <code>rayDirection</code>.
	 * The intersections are returned as a {@link List} containing
	 *  {@link Face3DIntersection} instances, but may well be empty.
	 * <p>
	 * Note that the arguments are specified in world coordinates (WCS) and the
	 * <code>object2world</code> transformation matrix must be specified to
	 * define this object's place in the world.
	 *
	 * @param   dest            Destination for found intersections (<code>null</code> => create new).
	 * @param   sortResult      Perform insertion sort in list.
	 * @param   objectID        ID of object to test for intersections.
	 * @param   object2world    Transformation from object to world coordinates (OCS->WCS).
	 * @param   object          Object being tested for intersection.
	 * @param   ray             Ray expression in world coordinates (WCS).
	 *
	 * @return  List containing found intersection (never <code>null</code>,
	 *          same as <code>dest</code> argument if it was non-<code>null</code>).
	 *
	 * @throws  NullPointerException if a required input argument is <code>null</code>.
	 */
	public List<Face3DIntersection> getIntersectionsWithRay( final List<Face3DIntersection> dest , final boolean sortResult , final Object objectID , final Matrix3D object2world , final Object3D object , final Ray3D ray )
	{
		final Matrix3D world2object = object2world.inverse();
		final Ray3D    ocsRay       = new BasicRay3D( world2object , ray );

		final List<Face3DIntersection> result = ( dest != null ) ? dest : new ArrayList<Face3DIntersection>();

		for ( final Face face : faces )
		{
			final Vector3D ocsPoint = GeometryTools.getIntersectionBetweenRayAndPolygon( face , ocsRay );
			if ( ocsPoint != null )
			{
				final Vector3D wcsPoint = object2world.transform( ocsPoint );

				final Face3DIntersection intersection = new Face3DIntersection( objectID , object2world , object , face , ray , wcsPoint );
				if ( sortResult )
				{
					Face3DIntersection.addSortedByDistance( result , intersection );
				}
				else
				{
					result.add( intersection );
				}
			}
		}

		return result;
	}

	/**
	 * Get bounding box of this object in the object coordinate system (OCS).
	 *
	 * @return  Bounding box of this object in the object coordinate system (OCS).
	 */
	public Bounds3D getOrientedBoundingBox()
	{
		Bounds3D result = _orientedBoundingBox;
		if ( result == null )
		{
			final Bounds3DBuilder builder = new Bounds3DBuilder();

			for ( final Face face : faces )
			{
				for ( final Vertex vertex : face.vertices )
				{
					builder.addPoint( vertex.getPoint() );
				}
			}

			result = builder.getBounds();
			_orientedBoundingBox = result;
		}

		return result;
	}

	/**
	 * Get {@link CollisionNode} for this mesh.
	 *
	 * @return  {@link CollisionNode}.
	 */
	private CollisionNode getCollisionNode()
	{
		CollisionNode result = _collisionNode;
		if ( result == null )
		{
			int nrTriangles = 0;

			for ( final Face face : faces )
			{
				final List<Triangle> triangles = face.triangulate();
				nrTriangles += triangles.size();
			}

			final List<Triangle3D> triangles = new ArrayList<Triangle3D>( nrTriangles );

			for ( final Face face : faces )
			{
				triangles.addAll( face.triangulate() );
			}

			result = new CollisionNode( triangles , 0 , triangles.size() );
			_collisionNode = result;
		}
		return result;
	}

	/**
	 * Vertex of mesh. This is used internally to share vertices between faces.
	 *
	 * This is primarily used to calculate the vertex normal. By default, this
	 * is a pseudo-normal based on the average face normals sharing a this vertex.
	 * <p />
	 * The following text is from http://nate.scuzzy.net/normals/normals.html.
	 * <p />
	 * There is no way to calculate a true vertex normal since a vertex is just
	 * a point in space, so the best we can get is an approximation for our vertex
	 * normals. If you understand the ideas behind face normals, calculating vertex
	 * normals will be a breeze. A vertex normal is simply the average of the face
	 * normals that surround a particular vertex. So how do you go about
	 * calculating a vertex normal? For every face in your face list you need to
	 * generate the normal to that face but, do not normalize it. Then for each
	 * vertex in the face add the face normal to the corresponding vertex normal
	 * for the current vertex. After you have gone through all the verticies of
	 * the face you then can normalize the face normal, do not normalize the vertex
	 * normal. After you have done this for all faces walk the vertex normal list
	 * and normalize each normal. Once you have done this you have your vertex
	 * normals. Pretty simple isn't it? Thanks to hude for this better method for
	 * calculating vertex normals.
	 * <p />
	 * PS: We do not follow the suggested implementation in this article, but use
	 *     its suggested calculations.
	 */
	public static class SharedVertex
	{
		/**
		 * Point at vertex.
		 */
		public final Vector3D point;

		/**
		 * Sum of X coordinates of unnormalized face normals.
		 */
		private double _crossSumX;

		/**
		 * Sum of Y coordinates of unnormalized face normals.
		 */
		private double _crossSumY;

		/**
		 * Sum of Z coordinates of unnormalized face normals.
		 */
		private double _crossSumZ;

		/**
		 * Vertex normal (cached).
		 */
		private Vector3D _normal;

		/**
		 * Construct vertex.
		 *
		 * @param   point   Point at vertex.
		 */
		SharedVertex( final Vector3D point )
		{
			this.point = point;
			_crossSumX = 0.0;
			_crossSumY = 0.0;
			_crossSumZ = 0.0;
			_normal = null;
		}

		/**
		 * Get/determine vertex normal.
		 *
		 * @return  Vertex normal.
		 */
		public Vector3D getNormal()
		{
			Vector3D result = _normal;
			if ( result == null )
			{
				result = Vector3D.normalize( _crossSumX , _crossSumY , _crossSumZ );
				_normal = result;
			}
			return result;
		}

		/**
		 * Set vertex normal.
		 *
		 * @param   normal  Vertex normal.
		 */
		public void setNormal( final Vector3D normal )
		{
			_normal = normal;
		}
	}

	/**
	 * This class defines a face of a mesh.
	 */
	public static class Face
		implements Polygon3D
	{
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
		public boolean twoSided;

		/**
		 * Distance component of plane relative to origin. This defines the
		 * <code>D</code> variable in the plane equation:
		 * <pre>
		 *   A * x + B * y + C * z = D
		 * </pre>
		 */
		private final double _planeDistance;

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
		private final Vector3D _normal;

		/**
		 * Triangulated face.
		 */
		private List<Triangle> _triangles;

		/**
		 * Construct new face.
		 *
		 * @param   vertices    Vertices of face.
		 * @param   material    Material to apply to the face.
		 * @param   smooth      Face is smooth/curved vs. flat.
		 * @param   twoSided    Face is two-sided.
		 */
		Face( final List<Vertex> vertices , final Material material , final boolean smooth , final boolean twoSided )
		{
			Vector3D normal = Vector3D.INIT;
			double distance = 0.0;

			if ( vertices.size() >= 3 )
			{
				final Vertex v1 = vertices.get( 0 );
				final Vertex v2 = vertices.get( 1 );
				final Vertex v3 = vertices.get( 2 );

				final Vector3D p1 = v1.getPoint();
				final Vector3D p2 = v2.getPoint();
				final Vector3D p3 = v3.getPoint();

				final Vector3D unnormalizedNormal = Vector3D.cross( p1.x - p2.x , p1.y - p2.y , p1.z - p2.z , p3.x - p2.x , p3.y - p2.y , p3.z - p2.z );

				final double l = unnormalizedNormal.length();
				if ( l > 0.0 )
				{
					normal = unnormalizedNormal.set( unnormalizedNormal.x / l , unnormalizedNormal.y / l , unnormalizedNormal.z / l );
					distance = Vector3D.dot( normal , p1 );

					for ( final Vertex vertex : vertices )
					{
						final SharedVertex sharedVertex = vertex._sharedVertex;
						sharedVertex._crossSumX += unnormalizedNormal.x;
						sharedVertex._crossSumY += unnormalizedNormal.y;
						sharedVertex._crossSumZ += unnormalizedNormal.z;
						sharedVertex._normal = null;
					}
				}
			}

			_normal = normal;
			_planeDistance = distance;
			this.vertices = vertices;
			this.material = material;
			this.smooth = smooth;
			this.twoSided = twoSided;
			_triangles = null;
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
		public List<Triangle> triangulate()
		{
			List<Triangle> result = _triangles;
			if ( result == null )
			{
				final List<Vertex> vertices = this.vertices;

				final int triangleCount = vertices.size() - 2;
				if ( triangleCount > 0 )
				{
					result = new ArrayList<Triangle>( triangleCount );

					for ( int i = 0 ; i < triangleCount ; i++ )
					{
						result.add( new Triangle( this, vertices.get( 0 ) , vertices.get( i + 1 ) , vertices.get( i + 2 ) ) );
					}

					result = Collections.unmodifiableList( result );
				}
				else
				{
					result = Collections.emptyList();
				}

				_triangles = result;
			}

			return Collections.unmodifiableList( result );
		}

		public double getDistance()
		{
			return _planeDistance;
		}

		public Vector3D getNormal()
		{
			return _normal;
		}

		public boolean isTwoSided()
		{
			return twoSided;
		}

		public int getVertexCount()
		{
			return vertices.size();
		}

		public double getX( final int index )
		{
			final Vertex vertex = vertices.get( index );
			return vertex.getPoint().x;
		}

		public double getY( final int index )
		{
			final Vertex vertex = vertices.get( index );
			return vertex.getPoint().y;
		}

		public double getZ( final int index )
		{
			final Vertex vertex = vertices.get( index );
			return vertex.getPoint().z;
		}
	}

	/**
	 * Defines a vertex of a face.
	 */
	public static class Vertex
	{
		/**
		 * Vertex properties shared with other vertices.
		 */
		private final SharedVertex _sharedVertex;

		/**
		 * Color map U coordinate.
		 */
		public float colorMapU;

		/**
		 * Color map V coordinate.
		 */
		public float colorMapV;

		/**
		 * Construct vertex.
		 *
		 * @param   sharedVertex    Shared vertex properties.
		 */
		Vertex( final SharedVertex sharedVertex )
		{
			this( sharedVertex , 0.0f , 0.0f );
		}

		/**
		 * Construct vertex.
		 *
		 * @param   sharedVertex    Shared vertex properties.
		 * @param   colorMapU       Color map U coordinate.
		 * @param   colorMapV       Color map V coordinate.
		 */
		Vertex( final SharedVertex sharedVertex , final float colorMapU , final float colorMapV )
		{
			_sharedVertex = sharedVertex;
			this.colorMapU = colorMapU;
			this.colorMapV = colorMapV;
		}

		/**
		 * Construct vertex.
		 *
		 * @param   sharedVertex    Shared vertex properties.
		 * @param   colorMapPoint   Point on color map.
		 */
		Vertex( final SharedVertex sharedVertex , final Point2D.Float colorMapPoint )
		{
			_sharedVertex = sharedVertex;
			colorMapU = colorMapPoint.x;
			colorMapV = colorMapPoint.y;
		}

		/**
		 * Get vertex normal.
		 *
		 * @return  Vertex normal.
		 */
		public Vector3D getNormal()
		{
			return _sharedVertex.getNormal();
		}

		/**
		 * Set vertex normal.
		 *
		 * @param   normal  Vertex normal.
		 */
		public void setNormal( final Vector3D normal )
		{
			_sharedVertex.setNormal( normal );
		}

		/**
		 * Get normal for this vertex.
		 *
		 * @return  Vertex normal.
		 */
		public Vector3D getPoint()
		{
			return _sharedVertex.point;
		}
	}

	/**
	 * Triangle that is part of a {@link Face}.
	 */
	public static class Triangle
		extends AbstractTriangle3D
	{
		/**
		 * Face that this triangle is part of.
		 */
		private Face _face;

		/**
		 * First vertex of triangle.
		 */
		public final Vertex v1;

		/**
		 * Second vertex of triangle.
		 */
		public final Vertex v2;

		/**
		 * Third vertex of triangle.
		 */
		public final Vertex v3;

		/**
		 * Construct triangle for face.
		 *
		 * @param   face    Face from which this triangle was created.
		 * @param   v1      First vertex of triangle.
		 * @param   v2      Second vertex of triangle.
		 * @param   v3      Third vertex of triangle.
		 */
		Triangle( final Face face , final Vertex v1 , final Vertex v2 , final Vertex v3 )
		{
			_face = face;
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
		}

		public double getDistance()
		{
			return _face._planeDistance;
		}

		public Vector3D getNormal()
		{
			return _face._normal;
		}

		public Vector3D getP1()
		{
			return v1.getPoint();
		}

		public Vector3D getP2()
		{
			return v2.getPoint();
		}

		public Vector3D getP3()
		{
			return v3.getPoint();
		}

		public boolean isTwoSided()
		{
			return _face.twoSided;
		}
	}
}
