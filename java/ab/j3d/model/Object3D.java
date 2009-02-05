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
package ab.j3d.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import ab.j3d.Bounds3D;
import ab.j3d.Bounds3DBuilder;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.geom.BasicRay3D;
import ab.j3d.geom.BasicTriangle3D;
import ab.j3d.geom.CollisionNode;
import ab.j3d.geom.GeometryTools;
import ab.j3d.geom.Ray3D;
import ab.j3d.geom.Triangle3D;
import ab.j3d.model.Face3D.Vertex;

import com.numdata.oss.ArrayTools;

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
	double[] _vertexCoordinates;

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
		_vertexCoordinates    = null;
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
	 * Transform this object.
	 *
	 * @param   transform   Transformation to apply.
	 */
	public void getTransformedInstance( final Matrix3D transform )
	{
		final Object3D result = new Object3D();
		result._vertexCoordinates = transform.transform( _vertexCoordinates , null , _vertexCoordinates.length / 3 );
		result.outlineColor = outlineColor;
		result.fillColor = fillColor;
		result.alternateFillColor = alternateFillColor;
		result.alternateOutlineColor = alternateOutlineColor;
		result.shadeFactor = shadeFactor;
	}

	/**
	 * Add a face to this object.
	 *
	 * @param   face    Face to add.
	 */
	protected final void addFace( final Face3D face )
	{
		_faces.add( face );
		_vertexNormalsDirty = true;
	}

	/**
	 * Add face to this object.
	 *
	 * @param   vertexIndices   Vertex indices of added face. These indices refer
	 *                          to vertices previously defined in this object.
	 * @param   material        Material to apply to the face.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 */
	public final void addFace( final int[] vertexIndices , final Material material , final boolean smooth )
	{
		addFace( vertexIndices , material , null , null , smooth , false );
	}

	/**
	 * Add face to this object.
	 *
	 * @param   vertexIndices   Vertex indices of added face. These indices refer
	 *                          to vertices previously defined in this object.
	 * @param   material        Material to apply to the face.
	 * @param   textureU        Horizontal texture coordinates (<code>null</code> = none).
	 * @param   textureV        Vertical texture coordinates (<code>null</code> = none).
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public final void addFace( final int[] vertexIndices, final Material material , final float[] textureU , final float[] textureV , final boolean smooth , final boolean twoSided )
	{
		addFace( new Face3D( this , vertexIndices , material , textureU , textureV , smooth , twoSided ) );
	}

	/**
	 * Add face to this object.
	 *
	 * @param   vertexCoordinates   Vertex coordinates that define the face.
	 * @param   material            Material to apply to the face.
	 * @param   smooth              Face is smooth/curved vs. flat.
	 * @param   twoSided            Face is two-sided.
	 */
	public final void addFace( final Vector3D[] vertexCoordinates , final Material material , final boolean smooth , final boolean twoSided )
	{
		addFace( vertexCoordinates , material , null , null , smooth , twoSided );
	}

	/**
	 * Add face to this object.
	 *
	 * @param   vertexCoordinates   Vertex coordinates that define the face.
	 * @param   material            Material to apply to the face.
	 * @param   textureU            Horizontal texture coordinates (<code>null</code> = none).
	 * @param   textureV            Vertical texture coordinates (<code>null</code> = none).
	 * @param   smooth              Face is smooth/curved vs. flat.
	 * @param   twoSided            Face is two-sided.
	 */
	public final void addFace( final Vector3D[] vertexCoordinates , final Material material , final float[] textureU , final float[] textureV , final boolean smooth , final boolean twoSided )
	{
		final int   nrVertices    = vertexCoordinates.length;
		final int[] vertexIndices = new int[ nrVertices ];

		for ( int i = 0 ; i < nrVertices ; i++ )
		{
			final Vector3D vertex = vertexCoordinates[ i ];
			vertexIndices[ i ] = getVertexIndex( vertex.x , vertex.y , vertex.z );
		}

		addFace( new Face3D( this , vertexIndices , material , textureU , textureV , smooth , twoSided ) );
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

				final double[] vertexNormals = (double[])ArrayTools.ensureLength( _vertexNormals , double.class , -1 , vertexCount * 3 );
				_vertexNormals = vertexNormals;

				for ( int vertexIndex = 0 ; vertexIndex < vertexCount ; vertexIndex++ )
				{
					double vnx = 0.0;
					double vny = 0.0;
					double vnz = 0.0;

					/*
					 * Sum unnormalized normals of faces that use this vertex.
					 */
					for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
					{
						final Face3D face = faces.get( faceIndex );
						final List<Vertex> vertices = face.vertices;

						for ( final Vertex vertex : vertices )
						{
							if ( vertex.vertexCoordinateIndex == vertexIndex )
							{
								vnx += face._crossX;
								vny += face._crossY;
								vnz += face._crossZ;
								break;
							}
						}
					}

					/*
					 * Normalize vertex normal.
					 */
					if ( ( vnx != 0.0 ) || ( vny != 0.0 ) || ( vnz != 0.0 ) )
					{
						final double l = Math.sqrt( vnx * vnx + vny * vny + vnz * vnz );
						vnx /= l;
						vny /= l;
						vnz /= l;
					}

					final int ni = vertexIndex * 3;
					vertexNormals[ ni     ] = vnx;
					vertexNormals[ ni + 1 ] = vny;
					vertexNormals[ ni + 2 ] = vnz;
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
	public static boolean testCollision( final Node3D subTree1 , final Node3D subTree2 )
	{
		boolean result = false;

		if ( ( subTree1 != null ) && ( subTree2 != null ) )
		{
			final Node3DCollection<Object3D> objects1 = subTree1.collectNodes( null , Object3D.class , Matrix3D.INIT , false );
			if ( objects1 != null )
			{
				final Node3DCollection<Object3D> objects2  = subTree2.collectNodes( null , Object3D.class , Matrix3D.INIT , false );
				if ( objects2 != null )
				{
					for ( int i = 0 ; !result && ( i < objects1.size() ) ; i++ )
					{
						final Object3D object1      = objects1.getNode( i );
						final Matrix3D object1ToWcs = objects1.getMatrix( i );
						final Matrix3D wcsToObject1 = object1ToWcs.inverse();

						for ( int j = 0 ; !result && ( j < objects2.size() ) ; j++ )
						{
							final Object3D object2      = objects2.getNode( j );
							final Matrix3D object2ToWcs = objects2.getMatrix( j );

							result = object1.collidesWith( object2ToWcs.multiply( wcsToObject1 ) , object2 );
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
	public boolean collidesWith( final Matrix3D fromOtherToThis , final Object3D other )
	{
		final boolean result;

		final Bounds3D thisOrientedBoundingBox  = getOrientedBoundingBox();
		final Bounds3D otherOrientedBoundingBox = other.getOrientedBoundingBox();

		if ( GeometryTools.testOrientedBoundingBoxIntersection( thisOrientedBoundingBox , fromOtherToThis , otherOrientedBoundingBox ) )
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
			final double[] vertexCoordinates = getVertexCoordinates();

			int nrTriangles = 0;

			for ( final Face3D face : _faces )
			{
				final int[] triangles = face.triangulate();
				if ( triangles != null )
				{
					nrTriangles += triangles.length / 3;
				}
			}

			final List<Triangle3D> triangles = new ArrayList<Triangle3D>( nrTriangles );

			final Bounds3DBuilder boundsBuilder = new Bounds3DBuilder();

			for ( final Face3D face : _faces )
			{
				final int[] faceTriangles = face.triangulate();
				if ( faceTriangles != null )
				{
					final List<Vertex> vertices = face.vertices;
					for ( final Vertex vertex : vertices )
					{
						final int i = vertex.vertexCoordinateIndex * 3;
						boundsBuilder.addPoint( vertexCoordinates[ i ] , vertexCoordinates[ i + 1 ] , vertexCoordinates[ i + 2 ] );
					}

					for ( int triangleIndex = 0 ; triangleIndex < faceTriangles.length ; triangleIndex += 3 )
					{
						final Vertex v1 = vertices.get( faceTriangles[ triangleIndex     ] );
						final Vertex v2 = vertices.get( faceTriangles[ triangleIndex + 1 ] );
						final Vertex v3 = vertices.get( faceTriangles[ triangleIndex + 2 ] );

						final int vi1 = v1.vertexCoordinateIndex * 3;
						final int vi2 = v2.vertexCoordinateIndex * 3;
						final int vi3 = v3.vertexCoordinateIndex * 3;

						final Vector3D p1 = Vector3D.INIT.set( vertexCoordinates[ vi1 ] , vertexCoordinates[ vi1 + 1 ] , vertexCoordinates[ vi1 + 2 ] );
						final Vector3D p2 = Vector3D.INIT.set( vertexCoordinates[ vi2 ] , vertexCoordinates[ vi2 + 1 ] , vertexCoordinates[ vi2 + 2 ] );
						final Vector3D p3 = Vector3D.INIT.set( vertexCoordinates[ vi3 ] , vertexCoordinates[ vi3 + 1 ] , vertexCoordinates[ vi3 + 2 ] );

						triangles.add( new BasicTriangle3D( p1 , p2 , p3 , true ) );
					}
				}
			}

			result = new CollisionNode( triangles , 0 , triangles.size() );

			_collisionNode = result;
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
			final int vertexCount = getVertexCount();
			if ( vertexCount > 0 )
			{
				final double[] vertexCoordinates = getVertexCoordinates();

				final Bounds3DBuilder builder = new Bounds3DBuilder();

				int i = 0;
				for ( int vertex = 0 ; vertex < vertexCount ; vertex++ )
				{
					final double x = vertexCoordinates[ i++ ];
					final double y = vertexCoordinates[ i++ ];
					final double z = vertexCoordinates[ i++ ];

					builder.addPoint( x , y , z );
				}

				result = builder.getBounds();
				_orientedBoundingBox = result;
			}
		}

		return result;
	}

	/**
	 * Add bounds of this object to a {@link Bounds3DBuilder}. Optionally.
	 *
	 * @param   bounds3DBuilder     Builder to add bounds to.
	 * @param   xform               Transform to apply to vertex coordinates.
	 */
	public void addBounds( final Bounds3DBuilder bounds3DBuilder , final Matrix3D xform )
	{
		if ( ( xform != null ) && ( xform != Matrix3D.INIT ) && ( !Matrix3D.INIT.equals( xform ) ) )
		{
			final int      vertexCount       = getVertexCount();
			final double[] vertexCoordinates = getVertexCoordinates();

			int i = 0;
			for ( int vertex = 0 ; vertex < vertexCount ; vertex++ )
			{
				final double x = vertexCoordinates[ i++ ];
				final double y = vertexCoordinates[ i++ ];
				final double z = vertexCoordinates[ i++ ];

				bounds3DBuilder.addPoint( xform.transformX( x , y , z ) , xform.transformY( x , y , z ) , xform.transformZ( x , y , z ) );
			}
		}
		else
		{
			final Bounds3D orientedBoundingBox = getOrientedBoundingBox();
			bounds3DBuilder.addPoint( orientedBoundingBox.v1 );
			bounds3DBuilder.addPoint( orientedBoundingBox.v2 );
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
	 * Get number of faces in the model.
	 *
	 * @return  Number of faces.
	 */
	public final int getFaceCount()
	{
		return _faces.size();
	}

	/**
	 * Get transformed face normals.
	 *
	 * @param   transform   Transformation to apply to normals (only the
	 *                      rotational part of the transformation is applied).
	 * @param   dest        Target array for result to allow reuse of buffers.
	 *
	 * @return  Transformed face normals.
	 */
	public final double[] getFaceNormals( final Matrix3D transform , final double[] dest )
	{
		final List<Face3D> faces        = _faces;
		final int          faceCount    = faces.size();
		final int          resultLength = faceCount * 3;
		final double[]     result       = (double[])ArrayTools.ensureLength( dest , double.class , -1 , resultLength );

		int resultIndex = 0;
		for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
		{
			final Face3D   face       = faces.get( faceIndex );
			final Vector3D faceNormal = face.getNormal();

			result[ resultIndex++ ] = faceNormal.x;
			result[ resultIndex++ ] = faceNormal.y;
			result[ resultIndex++ ] = faceNormal.z;
		}

		return ( transform != null ) ? transform.rotate( result , result , faceCount ) : result;
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
	 * @param   ray             Ray expression in world coordinates (WCS).
	 *
	 * @return  List containing found intersection (never <code>null</code>,
	 *          same as <code>dest</code> argument if it was non-<code>null</code>).
	 *
	 * @throws  NullPointerException if a required input argument is <code>null</code>.
	 */
	public List<Face3DIntersection> getIntersectionsWithRay( final List<Face3DIntersection> dest , final boolean sortResult , final Object objectID , final Matrix3D object2world , final Ray3D ray )
	{
		final Matrix3D world2object = object2world.inverse();
		final Ray3D    ocsRay       = new BasicRay3D( world2object , ray );

		final List<Face3DIntersection> result = ( dest != null ) ? dest : new ArrayList<Face3DIntersection>();

		final int faceCount = getFaceCount();
		for ( int i = 0 ; i < faceCount ; i++ )
		{
			final Face3D face = getFace( i );

			final Vector3D ocsPoint = GeometryTools.getIntersectionBetweenRayAndPolygon( face , ocsRay );
			if ( ocsPoint != null )
			{
				final Vector3D wcsPoint = object2world.multiply( ocsPoint );

				final Face3DIntersection intersection = new Face3DIntersection( objectID , object2world , this , face , ray , wcsPoint );
				if ( sortResult )
					Face3DIntersection.addSortedByDistance( result , intersection );
				else
					result.add( intersection );
			}
		}

		return result;
	}

	/**
	 * Get coordinates of all vertices in this object.
	 *
	 * @return  Point coordinates (one triplet per vertex).
	 */
	public final double[] getVertexCoordinates()
	{
		return _vertexCoordinates;
	}

	/**
	 * Get transformed vertex coordinates.
	 *
	 * @param   transform   Transformation to apply to vertex coordinates.
	 * @param   dest        Target array for result to allow reuse of buffers.
	 *
	 * @return  Transformed vertex coordinates.
	 */
	public final double[] getVertexCoordinates( final Matrix3D transform , final double[] dest )
	{
		final double[] result;

		final double[] vertexCoordinates = _vertexCoordinates;
		final int      vertexCount       = getVertexCount();

		if ( ( transform != null ) && ( transform != Matrix3D.INIT ) )
		{
			result = transform.transform( vertexCoordinates , dest , vertexCount );
		}
		else
		{
			final int resultLength = vertexCount * 3;
			result = ( ( dest != null ) && ( dest.length >= resultLength ) ) ? dest : new double[ resultLength ];
			System.arraycopy( vertexCoordinates , 0 , result , 0 , resultLength );
		}

		return result;
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
		return ( _vertexCoordinates == null ) ? 0 : _vertexCoordinates.length / 3;
	}

	/**
	 * Get vertex index for a point with the specified coordinates. If the vertex
	 * is not defined yet, a new vertex is added and its index is returned.
	 *
	 * @param   x   X component of vertex coordinates.
	 * @param   y   Y component of vertex coordinates.
	 * @param   z   Z component of vertex coordinates.
	 *
	 * @return  The index of the vertex.
	 */
	public final int getVertexIndex( final double x , final double y , final double z )
	{
		final double[] vertexCoordinates = getVertexCoordinates();
		final int      vertexCount       = getVertexCount();

		int index = vertexCount * 3;
		while ( ( index -= 3 ) >= 0 )
		{
			if ( ( x == vertexCoordinates[ index     ] )
			     && ( y == vertexCoordinates[ index + 1 ] )
			     && ( z == vertexCoordinates[ index + 2 ] ) )
				break;
		}

		if ( index < 0 )
		{
			index = vertexCount * 3;
			final double[] newCoords = (double[])ArrayTools.ensureLength( vertexCoordinates , double.class , -1 , index + 3 );
			newCoords[ index     ] = x;
			newCoords[ index + 1 ] = y;
			newCoords[ index + 2 ] = z;

			_vertexCoordinates = newCoords;

			_vertexNormalsDirty = true;
		}

		return index / 3;
	}

	/**
	 * Get transformed vertex normals. Vertex normals are pseudo-normals based
	 * on average face normals at each vertex.
	 *
	 * @param   transform   Transformation to apply to normals (only the
	 *                      rotational part of the transformation is applied).
	 * @param   dest        Target array for result to allow reuse of buffers.
	 *
	 * @return  Transformed vertex normals.
	 */
	public final double[] getVertexNormals( final Matrix3D transform , final double[] dest )
	{
		final double[] result;

		calculateVertexNormals();

		final int      vertexCount   = getVertexCount();
		final double[] vertexNormals = _vertexNormals;

		if ( ( transform != null ) && ( transform != Matrix3D.INIT ) )
		{
			result = transform.rotate( vertexNormals , dest , vertexCount );
		}
		else
		{
			final int resultLength = vertexCount * 3;
			result = ( ( dest != null ) && ( dest.length >= resultLength ) ) ? dest : new double[ resultLength ];
			System.arraycopy( vertexNormals , 0 , result , 0 , resultLength );
		}

		return result;
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
	public final void setVertexCoordinates( final double[] vertexCoordinates )
	{
		_vertexCoordinates = vertexCoordinates;

		invalidate();
	}

	/**
	 * Set vertex normals. Vertex normals are pseudo-normals based on average
	 * face normals at common vertices.
	 *
	 * @param   vertexNormals   Vertex normals (one triplet per vertex).
	 */
	public final void setVertexNormals( final double[] vertexNormals )
	{
		_vertexNormals = vertexNormals;
		_vertexNormalsDirty = false;
	}
}
