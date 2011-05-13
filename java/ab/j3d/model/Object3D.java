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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
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
	 * List of face groups in this object.
	 */
	final List<FaceGroup> _faceGroups;

	/**
	 * Coordinates of vertex coordinates in object. Vertex coordinates are
	 * stored in an array of doubles with a triplet for each vertex.
	 */
	final HashList<Vector3D> _vertexCoordinates;

	/**
	 * Helper for collision tests.
	 */
	private CollisionNode _collisionNode = null;

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
		_faceGroups = new ArrayList<FaceGroup>();
		_vertexCoordinates = new HashList<Vector3D>();
		_orientedBoundingBox = null;
	}

	/**
	 * Returns a builder for creating the geometry of this object.
	 *
	 * @return  3D object builder.
	 */
	protected Abstract3DObjectBuilder getBuilder()
	{
		return new Object3DBuilder( this );
	}

	/**
	 * Returns the face groups in this object.
	 *
	 * @return  Face groups in this object.
	 */
	public List<FaceGroup> getFaceGroups()
	{
		return Collections.unmodifiableList( _faceGroups );
	}

	/**
	 * Add a face group to this object.
	 *
	 * @param   faceGroup    Face group to add.
	 */
	final void addFaceGroup( final FaceGroup faceGroup )
	{
		_faceGroups.add( faceGroup );
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

			for ( final FaceGroup faceGroup : getFaceGroups() )
			{
				for ( final Face3D face : faceGroup.getFaces() )
				{
					final Tessellation tessellation = face.getTessellation();
					for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
					{
						final int[] triangles = primitive.getTriangles();
						nrTriangles += triangles.length / 3;
					}
				}
			}

			final List<Triangle3D> triangles = new ArrayList<Triangle3D>( nrTriangles );
			final Bounds3DBuilder boundsBuilder = new Bounds3DBuilder();

			for ( final FaceGroup faceGroup : getFaceGroups() )
			{
				for ( final Face3D face : faceGroup.getFaces() )
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
			final Bounds3D boundingBox = getOrientedBoundingBox();
			if ( boundingBox != null )
			{
				bounds3DBuilder.addBounds( boundingBox );
			}
		}
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

		for ( final FaceGroup faceGroup : getFaceGroups() )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				final Vector3D ocsPoint = face.getIntersection( ocsRay );
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
	 * Invalidate derived data, because the face has been modified.
	 */
	void invalidate()
	{
		_orientedBoundingBox = null;
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
	 * Returns a face group with the given properties. If there an existing face
	 * group is found, it is returned. Otherwise a new face group is created.
	 *
	 * @param   appearance  Appearance.
	 * @param   smooth      Smoothing flag.
	 * @param   twoSided    Two-sided flag.
	 *
	 * @return  Face group.
	 */
	@NotNull
	protected FaceGroup getFaceGroup( final Appearance appearance, final boolean smooth, final boolean twoSided )
	{
		FaceGroup result = null;

		for ( final FaceGroup faceGroup : _faceGroups )
		{
			if ( ( faceGroup.getAppearance() == appearance ) &&
			     ( faceGroup.isSmooth() == smooth ) &&
			     ( faceGroup.isTwoSided() == twoSided ) )
			{
				result = faceGroup;
				break;
			}
		}

		if ( result == null )
		{
			result = new FaceGroup( appearance, smooth, twoSided );
			addFaceGroup( result );
		}

		return result;
	}
}
