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
import ab.j3d.appearance.*;
import ab.j3d.geom.*;

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
	private final List<FaceGroup> _faceGroups;

	/**
	 * Coordinates of vertex coordinates in object. Vertex coordinates are
	 * stored in an array of doubles with a triplet for each vertex.
	 */
	private final List<Vector3D> _vertices;

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
	 * Low-detail representation of the object.
	 */
	private Node3D _lowDetail = null;

	/**
	 * Threshold value for using the low-detail representation of
	 * the object, specified as the area in pixels occupied by the
	 * object in image space. If the approximated area occupied by the object
	 * is smaller, the low-detail representation will be rendered instead.
	 */
	private double _lowDetailThreshold = 0.0;

	/**
	 * Construct base object. Additional properties need to be set to make the
	 * object usable.
	 */
	public Object3D()
	{
		_faceGroups = new ArrayList<FaceGroup>();
		_vertices = new HashList<Vector3D>();
		_orientedBoundingBox = null;
	}

	/**
	 * Construct base object. Additional properties need to be set to make the
	 * object usable.
	 *
	 * @param   vertices    Object vertices.
	 */
	public Object3D( final List<Vector3D> vertices )
	{
		_faceGroups = new ArrayList<FaceGroup>();
		_vertices = vertices;
		_orientedBoundingBox = null;
	}

	/**
	 * Returns a builder for creating the geometry of this object.
	 *
	 * @return  3D object builder.
	 */
	public Object3DBuilder getBuilder()
	{
		return new Object3DBuilder( this );
	}

	/**
	 * Calculates vertex normals and removes edges based on the angles between
	 * adjacent faces.
	 *
	 * @param   maximumSmoothAngle  Maximum smoothing angle, in degrees.
	 * @param   maximumEdgeAngle    Maximum angle for which edges are removed.
	 * @param   separateMaterials   <code>true</code> to treat faces with
	 *                              different materials as not adjacent.
	 */
	public void smooth( final double maximumSmoothAngle, final double maximumEdgeAngle, final boolean separateMaterials )
	{
		final int vertexCount = getVertexCount();
		if ( separateMaterials )
		{
			for ( final FaceGroup faceGroup : getFaceGroups() )
			{
				smooth( Collections.singletonList( faceGroup ), maximumSmoothAngle, maximumEdgeAngle, vertexCount );
			}
		}
		else
		{
			smooth( getFaceGroups(), maximumSmoothAngle, maximumEdgeAngle, vertexCount );
		}
	}

	/**
	 * Calculates vertex normals and removes edges based on the angles between
	 * adjacent faces.
	 *
	 * @param   faceGroups          Face groups to be smoothed.
	 * @param   maximumSmoothAngle  Maximum smoothing angle, in degrees.
	 * @param   maximumEdgeAngle    Maximum angle for which edges are removed.
	 * @param   vertexCount         Number of vertex coordinates stored in the
	 *                              object containing the faces.
	 */
	private static void smooth( final List<FaceGroup> faceGroups, final double maximumSmoothAngle, final double maximumEdgeAngle, final int vertexCount )
	{
		final boolean smoothing = maximumSmoothAngle > 0.0;
		final boolean edgeRemoval = maximumEdgeAngle > 0.0;

		/*
		 * Map faces by vertex coordinate.
		 */
		final List<List<Face3D>> facesByVertexCoordinate = new ArrayList<List<Face3D>>( Collections.nCopies( vertexCount, Collections.<Face3D>emptyList() ) );
		for ( final FaceGroup faceGroup : faceGroups )
		{
			faceGroup.setSmooth( smoothing );
			for ( final Face3D face : faceGroup.getFaces() )
			{
				for ( final Vertex3D vertex : face.getVertices() )
				{
					List<Face3D> faceList = facesByVertexCoordinate.get( vertex.vertexCoordinateIndex );
					if ( faceList.isEmpty() )
					{
						faceList = new ArrayList<Face3D>();
						facesByVertexCoordinate.set( vertex.vertexCoordinateIndex, faceList );
					}
					faceList.add( face );
				}
			}
		}

		if ( edgeRemoval )
		{
			/*
			 * Remove outline segments between smoothed faces.
			 */
			final double minCosEdges = Math.cos( Math.toRadians( maximumEdgeAngle ) );
			final List<int[]> outlines = new LinkedList<int[]>();
			for ( final FaceGroup faceGroup : faceGroups )
			{
				for ( final Face3D face : faceGroup.getFaces() )
				{
					boolean outlinesModified = false;
					outlines.clear();

					for ( final int[] outline : face.getOutlines() )
					{
						int outlineStart = 0;
						final List<Vertex3D> faceVertices = face.getVertices();
						List<Face3D> startFaces = facesByVertexCoordinate.get( faceVertices.get( outline[ 0 ] ).vertexCoordinateIndex );

						for ( int i = 1; i < outline.length; i++ )
						{
							final List<Face3D> endFaces = facesByVertexCoordinate.get( faceVertices.get( outline[ i ] ).vertexCoordinateIndex );

							/*
							 * Find face on the opposite side of this edge.
							 */
							Face3D symmetric = null;
							for ( final Face3D startFace : startFaces )
							{
								if ( endFaces.contains( startFace ) )
								{
									symmetric = startFace;
									break;
								}
							}

							/*
							 * If requested, apply only to faces with the same
							 * material.
							 */
							if ( symmetric != null )
							{
								final double cos = Vector3D.dot( face.getNormal(), symmetric.getNormal() );
								if ( cos >= minCosEdges )
								{
									if ( i - outlineStart > 1 )
									{
										final int[] fragment = new int[ i - outlineStart ];
										System.arraycopy( outline, outlineStart, fragment, 0, i - outlineStart );
										outlines.add( fragment );
									}

									outlineStart = i;
								}
							}

							startFaces = endFaces;
						}

						if ( outlineStart == 0 )
						{
							outlines.add( outline );
						}
						else
						{
							outlinesModified = true;
							if ( outline.length - outlineStart > 1 )
							{
								final int[] fragment = new int[ outline.length - outlineStart ];
								System.arraycopy( outline, outlineStart, fragment, 0, outline.length - outlineStart );
								outlines.add( fragment );
							}
						}
					}

					if ( outlinesModified )
					{
						final Tessellation tessellation = face.getTessellation();
						switch ( outlines.size() )
						{
							case 0:
								tessellation.setOutlines( Collections.<int[]>emptyList() );
								break;
							case 1:
								tessellation.setOutlines( Collections.singletonList( outlines.get( 0 ) ) );
								break;
							default:
								tessellation.setOutlines( new ArrayList<int[]>( outlines ) );
						}
					}
				}
			}
		}

		if ( smoothing )
		{
			/*
			 * Determine smoothing groups and apply smoothing.
			 */
			final List<Face3D> visited = new ArrayList<Face3D>();
			final double minCosSmooth = Math.cos( Math.toRadians( maximumSmoothAngle ) );
			for ( int i = 0; i < facesByVertexCoordinate.size(); i++ )
			{
				final List<Face3D> faceList = facesByVertexCoordinate.get( i );
				for ( final Face3D face1 : faceList )
				{
					if ( !visited.contains( face1 ) )
					{
						/*
						 * Find other faces in the same smoothing group.
						 */
						final int groupStart = visited.size();
						visited.add( face1 );
						for ( final Face3D face2 : faceList )
						{
							/*
							 * If requested, place different materials in
							 * different smoothing groups.
							 */
							if ( !visited.contains( face2 ) )
							{
								final double cos = Vector3D.dot( face1.getNormal(), face2.getNormal() );
								if ( cos >= minCosSmooth )
								{
									visited.add( face2 );
								}
							}
						}
						final int groupEnd = visited.size();

						/*
						 * Calculate smooth normal.
						 */
						double normalX = 0.0;
						double normalY = 0.0;
						double normalZ = 0.0;
						for ( int j = groupStart; j < groupEnd; j++ )
						{
							final Face3D face = visited.get( j );
							final Vector3D cross = face.getCross();
							normalX += cross.x;
							normalY += cross.y;
							normalZ += cross.z;
						}
						final Vector3D normal = Vector3D.normalize( normalX, normalY, normalZ );

						/*
						 * Set vertex normals.
						 */
						for ( int j = groupStart; j < groupEnd; j++ )
						{
							final Face3D face = visited.get( j );
							for ( final Vertex3D vertex : face.getVertices() )
							{
								if ( vertex.vertexCoordinateIndex == i )
								{
									vertex.setNormal( normal );
								}
							}
						}
					}
				}
				visited.clear();
			}
		}
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
	public final void addFaceGroup( final FaceGroup faceGroup )
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

			result = ( thisCollisionNode != null ) && ( otherCollisionNode != null ) &&
			         thisCollisionNode.collidesWith( otherCollisionNode, fromOtherToThis );
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
			final List<Vector3D> vertexCoordinates = _vertices;

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
						final List<Vertex3D> faceVertices = face.getVertices();
						for ( final Vertex3D vertex : faceVertices )
						{
							boundsBuilder.addPoint( vertexCoordinates.get( vertex.vertexCoordinateIndex ) );
						}

						final int[] primitiveTriangles = primitive.getTriangles();
						for ( int i = 0 ; i < primitiveTriangles.length; i += 3 )
						{
							final Vertex3D v1 = faceVertices.get( primitiveTriangles[ i ] );
							final Vertex3D v2 = faceVertices.get( primitiveTriangles[ i + 1 ] );
							final Vertex3D v3 = faceVertices.get( primitiveTriangles[ i + 2 ] );

							triangles.add( new BasicTriangle3D( v1.point, v2.point, v3.point, true ) );
						}
					}
				}
			}

			result = triangles.isEmpty() ? null : new CollisionNode( triangles, 0, triangles.size() );

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
	protected Bounds3D calculateOrientedBoundingBox()
	{
		Bounds3D result = null;

		final List<Vector3D> vertexCoordinates = _vertices;
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
		if ( ( transform != null ) && ( transform != Matrix3D.IDENTITY ) && ( !Matrix3D.IDENTITY.equals( transform ) ) )
		{
			final List<Vector3D> vertexCoordinates = _vertices;
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
	 * Get vertex with the given index from this object.
	 *
	 * @param   index   Vertex index.
	 *
	 * @return  Vertex (coordinates).
	 */
	public Vector3D getVertex( final int index )
	{
		return _vertices.get( index );
	}

	/**
	 * Get index of vertex at the specified point. If no vertex was found at the
	 * specified point, a new one is created.
	 *
	 * @param   point   Coordinates of vertex get vertex index of.
	 *
	 * @return  Vertex index.
	 */
	public int getVertexIndex( final Vector3D point )
	{
		final int result;

		final List<Vector3D> vertices = _vertices;
		if ( vertices instanceof HashList )
		{
			result = ((HashList<Vector3D>)vertices).indexOfOrAdd( point );
		}
		else
		{
			vertices.add( point );
			result = vertices.size();
		}

		return result;
	}

	/**
	 * Add vertex for the given point and return its index.
	 *
	 * @param   point   Coordinates of vertex to add.
	 *
	 * @return  Vertex index.
	 */
	public int addVertex( final Vector3D point )
	{
		final List<Vector3D> vertexCoordinates = _vertices;
		final int result = vertexCoordinates.size();
		vertexCoordinates.add( point );
		return result;
	}

	/**
	 * Get coordinates of all vertices in this object.
	 *
	 * @return  Point coordinates (one triplet per vertex).
	 */
	public final List<Vector3D> getVertexCoordinates()
	{
		return Collections.unmodifiableList( _vertices );
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
		return _vertices.size();
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
	 * @throws IllegalStateException if faces have been added to the object.
	 */
	public final void setVertexCoordinates( final Collection<Vector3D> vertexCoordinates )
	{
		_vertices.clear();
		_vertices.addAll( vertexCoordinates );
		invalidate();
	}

	/**
	 * Add {@link Face3D} to this object.
	 *
	 * @param   appearance  Appearance.
	 * @param   smooth      Smoothing flag.
	 * @param   twoSided    Two-sided flag.
	 * @param   face        Face to add.
	 */
	public void addFace( final Appearance appearance, final boolean smooth, final boolean twoSided, final Face3D face )
	{
		final FaceGroup faceGroup = getFaceGroup( appearance, smooth, twoSided );
		faceGroup.addFace( face );
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
	public FaceGroup getFaceGroup( final Appearance appearance, final boolean smooth, final boolean twoSided )
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

	/**
	 * Returns a low-detail representation of the object or the object itself,
	 * based on the visual size of the object.
	 *
	 * @param   area    Approximate visual size of the object, in pixels.
	 *
	 * @return  Object suitable for the specified visual size.
	 */
	public Node3D getLevelOfDetail( final double area )
	{
		final Node3D lowDetail = _lowDetail;
		return ( area >= getLowDetailThreshold() ) ? this :
		       ( ( lowDetail instanceof Object3D ) && ( lowDetail != this ) ) ? ( (Object3D)lowDetail ).getLevelOfDetail( area ) : lowDetail;
	}

	/**
	 * Returns a low-detail representation of the object. If <code>null</code>
	 * is returned, the object will not be rendered at low detail levels.
	 * Objects that should always be rendered at full detail may return
	 * <code>this</code>.
	 *
	 * @return  Low-detail representation of the object.
	 */
	public Node3D getLowDetail()
	{
		return _lowDetail;
	}

	/**
	 * Sets the low-detail representation of the object. If <code>null</code>
	 * is returned, the object will not be rendered below the {@link #getLowDetailThreshold()
	 * low-detail threshold}. Objects that should always be rendered at full
	 * detail may return <code>this</code>.
	 *
	 * @param   lowDetail   Low-detail representation of the object.
	 */
	public void setLowDetail( final Node3D lowDetail )
	{
		_lowDetail = lowDetail;
	}

	/**
	 * Returns the threshold value for using the low-detail representation of
	 * the object, specified as the area in pixels occupied by the
	 * object in image space. If the approximated area occupied by the object
	 * is smaller, the low-detail representation will be rendered instead.
	 *
	 * @return  Low-detail threshold, in pixels.
	 */
	public double getLowDetailThreshold()
	{
		return _lowDetailThreshold;
	}

	/**
	 * Sets the threshold value for using the low-detail representation of
	 * the object, specified as the area in pixels occupied by the
	 * object in image space. If the approximated area occupied by the object
	 * is smaller, the low-detail representation will be rendered instead.
	 *
	 * @param   lowDetailThreshold  Low-detail threshold, in pixels.
	 */
	public void setLowDetailThreshold( final double lowDetailThreshold )
	{
		_lowDetailThreshold = lowDetailThreshold;
	}

	/**
	 * Returns whether this object provides a low-detail version.
	 *
	 * @return  <code>true</code> if a low-detail version is available.
	 */
	public boolean isLowDetailAvailable()
	{
		return ( getLowDetail() != this ) && ( getLowDetailThreshold() > 0.0 );
	}

	/**
	 * Low-detail representation of the object, consisting of a bounding box.
	 */
	protected static class LowDetailObject3D
		extends Object3D
	{
		/**
		 * Bounding box.
		 */
		private final Bounds3D _bounds;

		/**
		 * Appearance.
		 */
		private final Appearance _appearance;

		/**
		 * Constructs a new instance.
		 *
		 * @param   bounds      Bounding box.
		 * @param   appearance  Appearance.
		 */
		protected LowDetailObject3D( final Bounds3D bounds, final Appearance appearance )
		{
			_bounds = bounds;
			_appearance = appearance;

			final Vector3D p1 = new Vector3D( bounds.v1.x, bounds.v1.y, bounds.v1.z );
			final Vector3D p2 = new Vector3D( bounds.v2.x, bounds.v1.y, bounds.v1.z );
			final Vector3D p3 = new Vector3D( bounds.v1.x, bounds.v2.y, bounds.v1.z );
			final Vector3D p4 = new Vector3D( bounds.v2.x, bounds.v2.y, bounds.v1.z );
			final Vector3D p5 = new Vector3D( bounds.v1.x, bounds.v1.y, bounds.v2.z );
			final Vector3D p6 = new Vector3D( bounds.v2.x, bounds.v1.y, bounds.v2.z );
			final Vector3D p7 = new Vector3D( bounds.v1.x, bounds.v2.y, bounds.v2.z );
			final Vector3D p8 = new Vector3D( bounds.v2.x, bounds.v2.y, bounds.v2.z );

			final Object3DBuilder builder = getBuilder();
			final BoxUVMap uvMap = new BoxUVMap( Scene.MM );
			builder.addQuad( p1, p3, p7, p5, appearance, uvMap, false );
			builder.addQuad( p2, p6, p8, p4, appearance, uvMap, false );
			builder.addQuad( p1, p5, p6, p2, appearance, uvMap, false );
			builder.addQuad( p4, p8, p7, p3, appearance, uvMap, false );
			builder.addQuad( p1, p2, p4, p3, appearance, uvMap, false );
			builder.addQuad( p5, p7, p8, p6, appearance, uvMap, false );
		}

		@Override
		public Bounds3D getOrientedBoundingBox()
		{
			return _bounds;
		}

		@Override
		public boolean equals( final Object object )
		{
			boolean result = false;
			if ( object == this )
			{
				result = true;
			}
			else if ( object instanceof LowDetailObject3D )
			{
				final LowDetailObject3D other = (LowDetailObject3D)object;
				result = _bounds.equals( other._bounds ) &&
				         _appearance.equals( other._appearance );
			}
			return result;
		}

		@Override
		public int hashCode()
		{
			int result = _bounds.hashCode();
			result = 31 * result + _appearance.hashCode();
			return result;
		}
	}
}
