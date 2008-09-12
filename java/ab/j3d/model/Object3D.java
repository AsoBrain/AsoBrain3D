/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2007 Peter S. Heijnen
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
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import ab.j3d.Bounds3D;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.PolyPoint2D;
import ab.j3d.Polyline2D;
import ab.j3d.Vector3D;
import ab.j3d.geom.BasicRay3D;
import ab.j3d.geom.GeometryTools;
import ab.j3d.geom.Ray3D;

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
	 *
	 * @see     CollisionTester
	 */
	private CollisionTester _collisionTester = null;

	/**
	 * Outline color to use when this object is painted using Java 2D. If this
	 * is set to <code>null</code>, the object outline will not be painted.
	 *
	 * @see     #fillPaint
	 * @see     #alternateOutlinePaint
	 */
	public Paint outlinePaint;

	/**
	 * Fill color to use when this object is painted using Java 2D. If this is
	 * set to <code>null</code>, the object faces will not be filled.
	 *
	 * @see     #outlinePaint
	 * @see     #alternateFillPaint
	 */
	public Paint fillPaint;

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
	 * @see     #alternateFillPaint
	 * @see     #outlinePaint
	 */
	public Paint alternateOutlinePaint;

	/**
	 * Alternate fill color to use when this object is painted using Java 2D.
	 * If this is set to <code>null</code>, the object faces will not be filled.
	 *
	 * @see     #alternateOutlinePaint
	 * @see     #fillPaint
	 */
	public Paint alternateFillPaint;

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

		outlinePaint          = Color.BLACK;
		fillPaint             = Color.WHITE;
		shadeFactor           = 0.5f;
		alternateOutlinePaint = Color.BLUE;
		alternateFillPaint    = Color.WHITE;

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

		face.invalidate();
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
		addFace( vertexIndices , material , null , null , 1.0f , smooth , false );
	}

	/**
	 * Add face to this object.
	 *
	 * @param   vertexIndices   Vertex indices of added face. These indices refer
	 *                          to vertices previously defined in this object.
	 * @param   material        Material to apply to the face.
	 * @param   textureU        Horizontal texture coordinates (<code>null</code> = none).
	 * @param   textureV        Vertical texture coordinates (<code>null</code> = none).
	 * @param   opacity         Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public final void addFace( final int[] vertexIndices , final Material material , final float[] textureU , final float[] textureV , final float opacity , final boolean smooth , final boolean twoSided )
	{
		addFace( new Face3D( this , vertexIndices , material , textureU , textureV , opacity , smooth , twoSided ) );
	}

	/**
	 * Add face based on a 2D shape to this object.
	 *
	 * @param   base            Location/orientation of face.
	 * @param   shape           Shape of face relative to the base.
	 * @param   reversePath If  set, the returned path will be reversed.
	 * @param   flipUV          Swap texture coordinates to rotate 90 degrees.
	 * @param   material        Material to apply to the face.
	 * @param   opacity         Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public final void addFace( final Matrix3D base , final Polyline2D shape , final boolean reversePath , final boolean flipUV , final Material material , final float opacity , final boolean smooth , final boolean twoSided )
	{
		final int nrVertices = shape.getPointCount() + ( shape.isClosed() ? -1 : 0 );

		final Face3D face = new Face3D( this , null , material , null , null , opacity , smooth , twoSided );
		face.ensureCapacity( nrVertices );

		if ( ( nrVertices > 2 ) && ( material != null ) && ( material.colorMap != null ) )
		{
			final double txBase = -base.xo * base.xx - base.yo * base.yx - base.zo * base.zx;
			final double tyBase = -base.xo * base.xy - base.yo * base.yy - base.zo * base.zy;

			final double[] vertU = new double[ nrVertices ];
			final double[] vertV = new double[ nrVertices ];

			double floorU = Double.POSITIVE_INFINITY;
			double floorV = Double.POSITIVE_INFINITY;

			final double modelUnit = 0.001;
			final double scaleU    = ( material.colorMapWidth  > 0.0 ) ? modelUnit / material.colorMapWidth  : 1.0;
			final double scaleV    = ( material.colorMapHeight > 0.0 ) ? modelUnit / material.colorMapHeight : 1.0;

			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );

				final double u = scaleU * ( flipUV ? ( tyBase + point.y ) : ( txBase + point.x ) );
				final double v = scaleV * ( flipUV ? ( txBase + point.x ) : ( tyBase + point.y ) );

				if ( u < floorU ) floorU = Math.floor( u );
				if ( v < floorV ) floorV = Math.floor( v );

				vertU[ i ] = u;
				vertV[ i ] = v;
			}

			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );
				face.addVertex( base.multiply( point.x , point.y , 0.0 ) , (float)( vertU[ i ] - floorU ) , (float)( vertV[ i ] - floorV ) );
			}
		}
		else
		{
			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );
				face.addVertex( base.multiply( point.x , point.y , 0.0 ) , 0.0f , 0.0f );
			}
		}

		addFace( face );
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
		addFace( vertexCoordinates , material , null , null , 1.0f , smooth , twoSided );
	}

	/**
	 * Add face to this object.
	 *
	 * @param   vertexCoordinates   Vertex coordinates that define the face.
	 * @param   material            Material to apply to the face.
	 * @param   textureU            Horizontal texture coordinates (<code>null</code> = none).
	 * @param   textureV            Vertical texture coordinates (<code>null</code> = none).
	 * @param   opacity             Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth              Face is smooth/curved vs. flat.
	 * @param   twoSided            Face is two-sided.
	 */
	public final void addFace( final Vector3D[] vertexCoordinates , final Material material , final float[] textureU , final float[] textureV , final float opacity , final boolean smooth , final boolean twoSided )
	{
		final int   nrVertices    = vertexCoordinates.length;
		final int[] vertexIndices = new int[ nrVertices ];

		for ( int i = 0 ; i < nrVertices ; i++ )
		{
			final Vector3D vertex = vertexCoordinates[ i ];
			vertexIndices[ i ] = getVertexIndex( vertex.x , vertex.y , vertex.z );
		}

		addFace( new Face3D( this , vertexIndices , material , textureU , textureV , opacity , smooth , twoSided ) );
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
						final Face3D face              = faces.get( faceIndex );
						final int    faceVertexCount   = face.getVertexCount();
						final int[]  faceVertexIndices = face.getVertexIndices();

						for ( int faceVertex = faceVertexCount ; --faceVertex >= 0 ; )
						{
							if ( faceVertexIndices[ faceVertex ] == vertexIndex )
							{
								face.calculateCross();
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
	 * Clear all data.
	 * <p />
	 * This removes all faces and vertices, essentially reverting to the state
	 * after calling the default constructor. However, internal cache/buffers
	 * are preserved to reduce memory fragmentation.
	 */
	public final void clear()
	{
		_faces.clear();
		_vertexCoordinates = null;
		invalidate();
	}

	/**
	 * Test if this object collides with another.
	 *
	 * @param   fromOtherToThis     Transformation from other object to this.
	 * @param   otherObject         Object to test collision with.
	 *
	 * @return  <code>true</code> if the objects collide;
	 *          <code>false</code> otherwise.
	 */
	public boolean collidesWith( final Matrix3D fromOtherToThis , final Object3D otherObject )
	{
		final CollisionTester collisionTester = getCollisionTester();
		return collisionTester.testCollision( fromOtherToThis , otherObject.getCollisionTester()  );
	}

	/**
	 * Get helper to perform collision tests.
	 *
	 * @return  A {@link CollisionTester} for this object.
	 */
	private CollisionTester getCollisionTester()
	{
		CollisionTester result = _collisionTester;
		if ( result == null )
		{
			result = new CollisionTester( this );
			_collisionTester = result;
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
			result = getBounds( null , null );
			_orientedBoundingBox = result;
		}

		return result;
	}

	/**
	 * Get outer bounds (bounding box) of the object. Optionally, an existing
	 * bounding box can be specified. The resulting bounds contains all vertices
	 * within the object and the existing bounding box (if any).
	 *
	 * @param   xform       Transform to apply to vertex coordinates.
	 * @param   bounds      Existing bounding box to use.
	 *
	 * @return  Combined bounding box of this object and the existing bounding box (if any).
	 */
	public final Bounds3D getBounds( final Matrix3D xform , final Bounds3D bounds )
	{
		Bounds3D result = bounds;

		final int vertexCount = getVertexCount();
		if ( vertexCount > 0 )
		{
			final boolean isXform = ( xform != null ) && ( xform != Matrix3D.INIT ) && ( !Matrix3D.INIT.equals( xform ) );

			double x1;
			double y1;
			double z1;
			double x2;
			double y2;
			double z2;

			if ( result != null )
			{
				x1 = bounds.v1.x;
				y1 = bounds.v1.y;
				z1 = bounds.v1.z;
				x2 = bounds.v2.x;
				y2 = bounds.v2.y;
				z2 = bounds.v2.z;
			}
			else
			{
				x1 = Double.POSITIVE_INFINITY;
				y1 = Double.POSITIVE_INFINITY;
				z1 = Double.POSITIVE_INFINITY;
				x2 = Double.NEGATIVE_INFINITY;
				y2 = Double.NEGATIVE_INFINITY;
				z2 = Double.NEGATIVE_INFINITY;
				result = Bounds3D.INIT;
			}

			double x;
			double y;
			double z;
			double tx;
			double ty;

			final double[] vertexCoordinates = _vertexCoordinates;

			int i = 0;
			for ( int vertex = 0 ; vertex < vertexCount ; vertex++ )
			{
				x = vertexCoordinates[ i++ ];
				y = vertexCoordinates[ i++ ];
				z = vertexCoordinates[ i++ ];

				if ( isXform )
				{
					tx = xform.transformX( x , y , z );
					ty = xform.transformY( x , y , z );
					z  = xform.transformZ( x , y , z );
					x  = tx;
					y  = ty;
				}

				if ( x < x1 ) x1 = x;
				if ( y < y1 ) y1 = y;
				if ( z < z1 ) z1 = z;
				if ( x > x2 ) x2 = x;
				if ( y > y2 ) y2 = y;
				if ( z > z2 ) z2 = z;
			}

			if ( x1 > x2 || y1 > y2 || z1 > z2 )
			{
				result = null;
			}
			else
			{
				result = result.set( result.v1.set( x1 , y1 , z1 ) , result.v2.set( x2 , y2 , z2 ) );
			}
		}

		return result;
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
	 * Get index of the specified face.
	 *
	 * @param   face    Face to get index of (may be <code>null</code>).
	 *
	 * @return  Index of face;
	 *          -1 if the face is not part of this object.
	 */
	public final int getFaceIndex( final Face3D face )
	{
		int result = -1;

		if ( ( face != null ) && ( face.getObject() == this ) )
		{
			final List<Face3D> faces = _faces;

			for ( int i = 0 ; i < faces.size() ; i++ )
			{
				if ( face == faces.get( i ) )
				{
					result = i;
					break;
				}

			}
		}

		return result;
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

				final Face3DIntersection intersection = new Face3DIntersection( objectID , object2world , face , ray , wcsPoint );
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
		_vertexNormalsDirty  = true;

		final CollisionTester collisionTester = _collisionTester;
		if ( collisionTester != null )
			collisionTester.invalidate();

		final List<Face3D> faces     = _faces;
		final int          faceCount = faces.size();

		for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
		{
			final Face3D face = faces.get( faceIndex );
			face.invalidate();
		}
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

	/**
	 * Set alternate outline paint.
	 *
	 * @param   newAlternateOutlinePaint   Alternate outline paint.
	 */
	public void setAlternateOutlinePaint( final Paint newAlternateOutlinePaint )
	{
		alternateOutlinePaint = newAlternateOutlinePaint;
	}
}
