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

import java.awt.*;
import java.util.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.geom.*;
import ab.j3d.model.Face3D.*;
import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class defined a 3D object node in a 3D tree. The 3D object consists of
 * vertices, edges, and faces.
 *
 * @author  Peter S. Heijnen
 * @author  G.B.M. Rupert
 * @version $Revision$ ($Date$, $Author$)
 */
public class Object3D
	extends Node3D
{
	/**
	 * List of faces in this object.
	 */
	final List<Face3D> _faces;

	/**
	 * Coordinates of vertex coordinates in object. Vertex coordinates are
	 * stored in an array of doubles with a triplet for each vertex.
	 */
	final HashList<Vector3D> _vertexCoordinates;

	/**
	 * Array of floats with normals of each face of the model. Normals are
	 * stored in an array of floats with a triplet for each normal (x,y,z).
	 * The number of normals is equal to the number of faces (obviously).
	 * <p>
	 * This is only calculated when the model changes (indicated by the
	 * _normalsDirty field).
	 */
	double[] _vertexNormals;

	/**
	 * This internal flag is set to indicate that the vertex coordinates or
	 * faces changed, so the vertex normals need to be re-calculated.
	 */
	boolean _vertexNormalsDirty;

	/**
	 * Helper for collision tests.
	 */
	private CollisionNode _collisionNode = null;

	/**
	 * Outline color to use when this object is painted using Java 2D. If this
	 * is set to <code>null</code>, the object outline will not be painted.
	 *
	 * @see     #fillColor
	 * @see     #alternateOutlineColor
	 */
	public Color outlineColor;

	/**
	 * Fill color to use when this object is painted using Java 2D. If this is
	 * set to <code>null</code>, the object faces will not be filled.
	 *
	 * @see     #outlineColor
	 * @see     #alternateFillColor
	 */
	public Color fillColor;

	/**
	 * Amount of shading that may be applied (0=none, 1=extreme) when this
	 * object is painted using Java 2D.
	 */
	public float shadeFactor;

	/**
	 * Alternate outline color to use when this object is painted using Java 2D.
	 * If this is set to <code>null</code>, the object outline will not be
	 * painted.
	 *
	 * @see     #alternateFillColor
	 * @see     #outlineColor
	 */
	public Color alternateOutlineColor;

	/**
	 * Alternate fill color to use when this object is painted using Java 2D.
	 * If this is set to <code>null</code>, the object faces will not be filled.
	 *
	 * @see     #alternateOutlineColor
	 * @see     #fillColor
	 */
	public Color alternateFillColor;

	/**
	 * Bounding box of object in the local coordinate system.
	 *
	 * @see     #getOrientedBoundingBox()
	 */
	private Bounds3D _orientedBoundingBox;

	/**
	 * Construct base object. Additional properties need to be set to make the
	 * object usable.
	 */
	public Object3D()
	{
		_faces                = new ArrayList<Face3D>();
		_vertexCoordinates    = new HashList<Vector3D>();
		_vertexNormals        = null;
		_vertexNormalsDirty   = true;

		outlineColor = Color.BLACK;
		fillColor = Color.WHITE;
		shadeFactor           = 0.5f;
		alternateOutlineColor = Color.BLUE;
		alternateFillColor = Color.WHITE;

		_orientedBoundingBox  = null;
	}

	/**
	 * Add a face to this object.
	 *
	 * @param   face    Face to add.
	 */
	protected final void addFace( final Face3D face )
	{
		_faces.add( face );
	}

	/**
	 * The following text is from http://nate.scuzzy.net/normals/normals.html.
	 *
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
	 *
	 * PS: We do not follow the suggested implementation in this article, but use
	 *     its suggested calculations.
	 */
	void calculateVertexNormals()
	{
		if ( _vertexNormalsDirty )
		{
			synchronized( this )
			{
				final int          vertexCount = getVertexCount();
				final List<Face3D> faces       = _faces;
				final int          faceCount   = faces.size();

				_vertexNormals = null;
				final double[] vertexNormals = new double[ vertexCount * 3 ];
				_vertexNormals = vertexNormals;

				/*
				 * Sum unnormalized normals of faces that use this vertex.
				 */
				for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
				{
					final Face3D face = faces.get( faceIndex );

					if ( face.smooth )
					{
						for ( final Vertex vertex : face.vertices )
						{
							final int i = vertex.vertexCoordinateIndex * 3;
							vertexNormals[ i     ] += face._crossX;
							vertexNormals[ i + 1 ] += face._crossY;
							vertexNormals[ i + 2 ] += face._crossZ;
						}
					}
				}

				/*
				 * Now normalize the normals.
				 */
				for ( int vertexIndex = 0 ; vertexIndex < vertexCount ; vertexIndex++ )
				{
					final int i = vertexIndex * 3;
					final double x = vertexNormals[ i     ];
					final double y = vertexNormals[ i + 1 ];
					final double z = vertexNormals[ i + 2 ];

					if ( ( x != 0.0 ) || ( y != 0.0 ) || ( z != 0.0 ) )
					{
						double norm = x * x + y * y + z * z;
						if ( !MathTools.almostEqual( norm, 1.0 ) )
						{
							norm = 1.0 / Math.sqrt( norm );
							vertexNormals[ i     ] = norm * x;
							vertexNormals[ i + 1 ] = norm * y;
							vertexNormals[ i + 2 ] = norm * z;
						}
					}
				}

				_vertexNormalsDirty = false;
			}
		}
	}

	/**
	 * Test collision between two scene (sub)graphs. Note that an object can
	 * collide with itself.
	 *
	 * @param   subTree1    First scene (sub)graph to test.
	 * @param   subTree2    Second scene (sub)graph to test.
	 *
	 * @return  <code>true</code> if the two graphs collide;
	 *          <code>false</code> otherwise.
	 */
	public static boolean testCollision( final Node3D subTree1, final Node3D subTree2 )
	{
		boolean result = false;

		if ( ( subTree1 != null ) && ( subTree2 != null ) )
		{
			final Node3DCollector collector1 = new Node3DCollector( Object3D.class );
			Node3DTreeWalker.walk( collector1, subTree1 );
			final List<Node3DPath> objects1 = collector1.getCollectedNodes();

			if ( !objects1.isEmpty() )
			{
				final Node3DCollector collector2 = new Node3DCollector( Object3D.class );
				Node3DTreeWalker.walk( collector2, subTree2 );
				final List<Node3DPath> objects2 = collector2.getCollectedNodes();

				if ( !objects2.isEmpty() )
				{
					for ( final Node3DPath path1 : objects1 )
					{
						final Object3D object1 = (Object3D) path1.getNode();
						final Matrix3D object1ToWcs = path1.getTransform();

						for ( final Node3DPath path2 : objects2 )
						{
							final Object3D object2 = (Object3D) path2.getNode();
							final Matrix3D object2ToWcs = path2.getTransform();
							final Matrix3D object2to1 = object2ToWcs.multiplyInverse( object1ToWcs );

							if ( object1.collidesWith( object2to1, object2 ) )
							{
								result = true;
								break;
							}
						}

						if ( result )
						{
							break;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Test if this object collides with another.
	 *
	 * @param   fromOtherToThis     Transformation from other object to this.
	 * @param   other         Object to test collision with.
	 *
	 * @return  <code>true</code> if the objects collide;
	 *          <code>false</code> otherwise.
	 */
	public boolean collidesWith( final Matrix3D fromOtherToThis, final Object3D other )
	{
		final boolean result;

		final Bounds3D thisOrientedBoundingBox  = getOrientedBoundingBox();
		final Bounds3D otherOrientedBoundingBox = other.getOrientedBoundingBox();

		if ( ( thisOrientedBoundingBox != null ) && ( otherOrientedBoundingBox != null ) && GeometryTools.testOrientedBoundingBoxIntersection( thisOrientedBoundingBox, fromOtherToThis, otherOrientedBoundingBox ) )
		{
			final CollisionNode thisCollisionNode = getCollisionNode();
			final CollisionNode otherCollisionNode = other.getCollisionNode();

			result = thisCollisionNode.collidesWith( otherCollisionNode, fromOtherToThis );
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Get helper for collision tests.
	 *
	 * @return  Helper for collision tests.
	 */
	private CollisionNode getCollisionNode()
	{
		CollisionNode result = _collisionNode;
		if ( result == null )
		{
			final List<Vector3D> vertexCoordinates = _vertexCoordinates;

			int nrTriangles = 0;

			for ( final Face3D face : _faces )
			{
				final Tessellation tessellation = face.getTessellation();
				for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
				{
					final int[] triangles = primitive.getTriangles();
					nrTriangles += triangles.length / 3;
				}
			}

			final List<Triangle3D> triangles = new ArrayList<Triangle3D>( nrTriangles );
			final Bounds3DBuilder boundsBuilder = new Bounds3DBuilder();

			for ( final Face3D face : _faces )
			{
				final Tessellation tessellation = face.getTessellation();
				for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
				{
					final List<Vertex> vertices = face.vertices;
					for ( final Vertex vertex : vertices )
					{
						boundsBuilder.addPoint( vertexCoordinates.get( vertex.vertexCoordinateIndex ) );
					}

					final int[] primitiveTriangles = primitive.getTriangles();
					for ( int i = 0 ; i < primitiveTriangles.length; i += 3 )
					{
						final Vertex v1 = vertices.get( primitiveTriangles[ i ] );
						final Vertex v2 = vertices.get( primitiveTriangles[ i + 1 ] );
						final Vertex v3 = vertices.get( primitiveTriangles[ i + 2 ] );

						triangles.add( new BasicTriangle3D( v1.point, v2.point, v3.point, true ) );
					}
				}
			}

			result = new CollisionNode( triangles, 0, triangles.size() );

			_collisionNode = result;
		}
		return result;
	}

	/**
	 * Get bounding box of this object in the object coordinate system (OCS).
	 *
	 * @return  Bounding box in the object coordinate system (OCS);
	 *          <code>null</code> if object is empty.
	 */
	@Nullable
	public Bounds3D getOrientedBoundingBox()
	{
		Bounds3D result = _orientedBoundingBox;
		if ( result == null )
		{
			result = calculateOrientedBoundingBox();
			_orientedBoundingBox = result;
		}

		return result;
	}

	/**
	 * Get bounding box of this object in the object coordinate system (OCS).
	 *
	 * @return  Bounding box in the object coordinate system (OCS);
	 *          <code>null</code> if object is empty.
	 */
	@Nullable
	protected Bounds3D calculateOrientedBoundingBox()
	{
		Bounds3D result = null;

		final List<Vector3D> vertexCoordinates = _vertexCoordinates;
		if ( !vertexCoordinates.isEmpty() )
		{
			final Bounds3DBuilder builder = new Bounds3DBuilder();

			for ( final Vector3D point : vertexCoordinates )
			{
				builder.addPoint( point );
			}

			result = builder.getBounds();
		}

		return result;
	}

	/**
	 * Add bounds of this object to a {@link Bounds3DBuilder}. Optionally.
	 *
	 * @param   bounds3DBuilder     Builder to add bounds to.
	 * @param   transform           Transform to apply to vertex coordinates.
	 */
	public void addBounds( final Bounds3DBuilder bounds3DBuilder, final Matrix3D transform )
	{
		if ( ( transform != null ) && ( transform != Matrix3D.INIT ) && ( !Matrix3D.INIT.equals( transform ) ) )
		{
			final List<Vector3D> vertexCoordinates = _vertexCoordinates;
			if ( !vertexCoordinates.isEmpty() )
			{
				for ( final Vector3D point : vertexCoordinates )
				{
					bounds3DBuilder.addPoint( transform, point );
				}
			}
		}
		else
		{
			bounds3DBuilder.addBounds( getOrientedBoundingBox() );
		}
	}

	/**
	 * Get face with the specified index.
	 *
	 * @param   index   Index of face to retrieve.
	 *
	 * @return  Face with the specified index.
	 */
	public final Face3D getFace( final int index )
	{
		return _faces.get( index );
	}

	/**
	 * Returns the unmodifiable list of all faces.
	 *
	 * @return  Faces.
	 */
	public List<Face3D> getFaces()
	{
		return Collections.unmodifiableList( _faces );
	}

	/**
	 * Get number of faces in the model.
	 *
	 * @return  Number of faces.
	 */
	public final int getFaceCount()
	{
		return _faces.size();
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
	 * @param   path            Path in scene graph to this object.
	 * @param   object2world    Transformation from object to world coordinates (OCS->WCS).
	 * @param   ray             Ray expression in world coordinates (WCS).
	 *
	 * @return  List containing found intersection (never <code>null</code>,
	 *          same as <code>dest</code> argument if it was non-<code>null</code>).
	 *
	 * @throws  NullPointerException if a required input argument is <code>null</code>.
	 */
	public List<Face3DIntersection> getIntersectionsWithRay( final List<Face3DIntersection> dest, final boolean sortResult, final Object objectID, final Node3DPath path, final Matrix3D object2world, final Ray3D ray )
	{
		final Matrix3D world2object = object2world.inverse();
		final Ray3D    ocsRay       = new BasicRay3D( world2object, ray );

		final List<Face3DIntersection> result = ( dest != null ) ? dest : new ArrayList<Face3DIntersection>();

		final int faceCount = getFaceCount();
		for ( int i = 0 ; i < faceCount ; i++ )
		{
			final Face3D face = getFace( i );

			final Vector3D ocsPoint = GeometryTools.getIntersectionBetweenRayAndPolygon( face, ocsRay );
			if ( ocsPoint != null )
			{
				final Vector3D wcsPoint = object2world.transform( ocsPoint );

				final Face3DIntersection intersection = new Face3DIntersection( objectID, object2world, this, path, face, ray, wcsPoint );
				if ( sortResult )
				{
					Face3DIntersection.addSortedByDistance( result, intersection );
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
	 * Get coordinates of all vertices in this object.
	 *
	 * @return  Point coordinates (one triplet per vertex).
	 */
	public final List<Vector3D> getVertexCoordinates()
	{
		return Collections.unmodifiableList( _vertexCoordinates );
	}

	/**
	 * Get number of vertices in the model. Vertices are shared amongst faces to
	 * reduce the number of transformations required (vertices have an average
	 * usage count that approaches 3, so the gain is significant).
	 *
	 * @return  Number of vertices.
	 */
	public final int getVertexCount()
	{
		return _vertexCoordinates.size();
	}

	/**
	 * Get transformed vertex normals. By default, vertex normals are
	 * pseudo-normals based on average face normals at each vertex.
	 * <dl>
	 *  <dt>WARNING:</dt>
	 *  <dd>The returned array is shared, so it should not be altered in any
	 *      way unless you wish the world to end.</dd>
	 * </dd>
	 *
	 * @return  Vertex normals.
	 */
	public final double[] getVertexNormals()
	{
		calculateVertexNormals();
		//noinspection ReturnOfCollectionOrArrayField
		return _vertexNormals;
	}

	/**
	 * Invalidate derived data, because the face has been modified.
	 */
	void invalidate()
	{
		_orientedBoundingBox = null;
		_vertexNormalsDirty = true;
		_collisionNode = null;
	}

	/**
	 * Set coordinates of all vertices in this object. This is only allowed when
	 * no faces have been added yet, since the object integrity is otherwise
	 * lost.
	 *
	 * @param   vertexCoordinates   Vertex coordinates (one triplet per vertex).
	 *
	 * @throws  IllegalStateException if faces have been added to the object.
	 */
	final void setVertexCoordinates( final Collection<Vector3D> vertexCoordinates )
	{
		_vertexCoordinates.clear();
		_vertexCoordinates.addAll( vertexCoordinates );
		invalidate();
	}

	/**
	 * Set vertex normals. Vertex normals are pseudo-normals based on average
	 * face normals at common vertices.
	 * <dl>
	 *  <dt>WARNING:</dt>
	 *  <dd>The specified array is used as-is, so it should not be altered in
	 *      any way unless you wish the world to end.</dd>
	 * </dd>
	 *
	 * @param   vertexNormals   Vertex normals (one triplet per vertex).
	 */
	final void setVertexNormals( final double[] vertexNormals )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_vertexNormals = vertexNormals;
		_vertexNormalsDirty = false;
	}
}