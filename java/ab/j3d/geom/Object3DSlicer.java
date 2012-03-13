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
package ab.j3d.geom;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.tessellator.*;
import ab.j3d.model.*;

/**
 * This class can be used to slice 3D objects.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Object3DSlicer
{
	/**
	 * Cutting plane that slices the object.
	 */
	private Plane3D _cuttingPlane = null;

	/**
	 * Transforms 2D plane coordinates to 3D object coordinates.
	 */
	private Matrix3D _plane2object = Matrix3D.IDENTITY;

	/**
	 * Transforms 3D object coordinates to 2D plane coordinates.
	 */
	private Matrix3D _object2plane = Matrix3D.IDENTITY;

	/**
	 * Distance from cutting plane to each vertex of the object.
	 *
	 * Set by {@link #slice} method.
	 */
	private final DoubleArray _objectVertexDistances = new DoubleArray();

	/**
	 * This flag indicates whether faces are intersected. If this is
	 * disabled, intersecting faces are simply omitted from the result.
	 */
	private boolean _intersectFaces = true;

	/**
	 * This flag indicates whether triangles are intersected. If this is
	 * disabled, intersecting triangles are simply omitted from the result.
	 */
	private boolean _intersectTriangles = true;

	/**
	 * Is intersection slice enabled.
	 */
	private boolean _sliceEnabled = false;

	/**
	 * 3D object for intersection slice.
	 *
	 * Initialized by {@link #slice} method.
	 */
	private Object3D _sliceObject = null;

	/**
	 * Appearance to use for face of {@link #_sliceObject}.
	 *
	 * This should be set before a slice operation is started.
	 */
	private Appearance _sliceAppearance = null;

	/**
	 * UV-map for face of {@link #_sliceObject}.
	 *
	 * This should be set before a slice operation is started.
	 */
	private UVMap _sliceUVMap = null;

	/**
	 * Slice intersection graph. The graph nodes are mapped by vertex index in
	 * {@link #_sliceObject}.
	 */
	private final Map<Integer,IntersectionNode> _sliceIntersectionGraph = new HashMap<Integer, IntersectionNode>();

	/**
	 * Plane vertices for intersection.
	 */
	private final HashList<Vector2D> _sliceVertices = new HashList<Vector2D>();

	/**
	 * Is top object enabled.
	 */
	private boolean _topEnabled = false;

	/**
	 * Is top object capped.
	 */
	private boolean _topCapped = true;

	/**
	 * Appearance override for {@link #_topObject}. If set to {@code null}, the
	 * original appearance will be maintained.
	 *
	 * This should be set before a slice operation is started.
	 */
	private Appearance _topAppearance = null;

	/**
	 * Appearance override for {@link #_bottomObject}. If set to {@code null}, the
	 * original appearance will be maintained.
	 *
	 * This should be set before a slice operation is started.
	 */
	private Appearance _bottomAppearance = null;

	/**
	 * 3D object with top part of sliced object.
	 *
	 * Initialized by {@link #slice}.
	 */
	private Object3D _topObject = null;

	/**
	 * Is bottom object enabled.
	 */
	private boolean _bottomEnabled = false;

	/**
	 * Is bottom object capped.
	 */
	private boolean _bottomCapped = true;

	/**
	 * 3D object with bottom part of sliced object.
	 *
	 * Initialized by {@link #slice}.
	 */
	private Object3D _bottomObject = null;

	/**
	 * Builder for top part of a face that intersects the cutting plane.
	 *
	 * Initialized by {@link #processFace}.
	 */
	private Face3DBuilder _topFace = null;

	/**
	 * Builder for bottom part of a face that intersects the cutting plane.
	 *
	 * Initialized by {@link #processFace}.
	 */
	private Face3DBuilder _bottomFace = null;

	/**
	 * Builder for top part of a face that intersects the cutting plane.
	 *
	 * Initialized by {@link #processFace}.
	 */
	private IntArray _topFaceVertexMap = null;

	/**
	 * Builder for bottom part of a face that intersects the cutting plane.
	 *
	 * Initialized by {@link #processFace}.
	 */
	private IntArray _bottomFaceVertexMap = null;

	/**
	 * Link list for top outline, (re)used by {@link #processOutline}.
	 */
	private IntArray _topOutlineNext = null;

	/**
	 * Link list for bottom outline ,(re)used by {@link #processOutline}.
	 */
	private IntArray _bottomOutlineNext = null;

	/**
	 * List of intersection vertices, (re)used by {@link #processOutline}.
	 */
	private List<Vector3D> _outlineIntersections = null;

	/**
	 * List of intersection positions, (re)used by {@link #processOutline}.
	 */
	private DoubleArray _outlineIntersectionPositions = null;

	/**
	 * New outline buffer, (re)used by {@link #processOutline}.
	 */
	private IntArray _newOutline = null;

	/**
	 * Slice object using the given cutting plane.
	 *
	 * @param   object          Object to slice.
	 * @param   cuttingPlane    Cutting plane that slices the object.
	 */
	public void slice( final Object3D object, final Plane3D cuttingPlane )
	{
		setCuttingPlane( cuttingPlane );
		slice( object );
	}

	/**
	 * Slice object.
	 *
	 * @param   object  Object to slice.
	 */
	public void slice( final Object3D object )
	{
		processObject( object );
	}

	/**
	 * Get cutting plane.
	 *
	 * @return  Cutting plane.
	 */
	public Plane3D getCuttingPlane()
	{
		return _cuttingPlane;
	}

	/**
	 * Set cutting plane.
	 *
	 * @param   cuttingPlane    Cutting plane to use.
	 */
	public void setCuttingPlane( final Plane3D cuttingPlane )
	{
		_cuttingPlane = cuttingPlane;

		final Vector3D planeNormal = cuttingPlane.getNormal();
		final double planeDistance = cuttingPlane.getDistance();

		final Matrix3D plane2object = Matrix3D.getPlaneTransform( planeNormal.multiply( planeDistance ), planeNormal, true );
		_plane2object = plane2object;
		_object2plane = plane2object.inverse();
	}

	/**
	 * Set cutting plane.
	 *
	 * @param   plane2object    Transformation from plane to object coordinates.
	 */
	public void setCuttingPlane( final Matrix3D plane2object )
	{
		_cuttingPlane = new BasicPlane3D( plane2object, true );
		_plane2object = plane2object;
		_object2plane = plane2object.inverse();
	}

	/**
	 * Get appearance for top object. This overrides the appearance of the
	 * original object.
	 *
	 * @return  Appearance override for top object;
	 *          {@code null} if original appearance is kept.
	 */
	public Appearance getTopAppearance()
	{
		return _topAppearance;
	}

	/**
	 * Set appearance for top object. This overrides the appearance of the
	 * original object.
	 *
	 * @param   appearance  Appearance override for top object;
	 *                      {@code null} to keep original appearance.
	 */
	public void setTopAppearance( final Appearance appearance )
	{
		_topAppearance = appearance;
	}

	/**
	 * Test whether the top object should be capped.
	 *
	 * @return  {@code true} if the top object is capped;
	 *          {@code false} if the top object is not capped.
	 */
	public boolean isTopCapped()
	{
		return _topCapped;
	}

	/**
	 * Set whether the top object should be capped.
	 *
	 * @param   enabled     {@code true} if the top object should be capped;
	 *                      {@code false} if the top object should not be capped.
	 */
	public void setTopCapped( final boolean enabled )
	{
		_topCapped = enabled;
	}

	/**
	 * Test whether creation of the top object is enabled.
	 *
	 * @return  {@code true} if a top object may be created;
	 *          {@code false} if a top object is never created.
	 */
	public boolean isTopEnabled()
	{
		return _topEnabled;
	}

	/**
	 * Set whether creation of the top object is enabled.
	 *
	 * @param   enabled     {@code true} if a top object may be created;
	 *                      {@code false} if a top object should not be created.
	 */
	public void setTopEnabled( final boolean enabled )
	{
		_topEnabled = enabled;
	}

	/**
	 * Get 3D object with top part of last sliced object.
	 *
	 * @return  Top part of last sliced object;
	 *          {@code null} the sliced object was completely below the cutting
	 *          plane, or no object was sliced yet.
	 */
	public Object3D getTopObject()
	{
		return _topObject;
	}

	/**
	 * Get whether faces are intersected. If this is disabled, intersecting
	 * faces are simply omitted from the result.
	 *
	 * @return  {@code true} if faces are intersected;
	 *          {@code false} if intersecting faces are removed.
	 */
	public boolean isIntersectFaces()
	{
		return _intersectFaces;
	}

	/**
	 * Set whether faces are intersected. If this is disabled, intersecting
	 * faces are simply omitted from the result.
	 *
	 * @param   enabled     {@code true} if faces are intersected;
	 *                      {@code false} if intersecting faces are removed.
	 */
	public void setIntersectFaces( final boolean enabled )
	{
		_intersectFaces = enabled;
	}

	/**
	 * Get whether triangles are intersected. If this is disabled, intersecting
	 * triangles are simply omitted from the result.
	 *
	 * @return  {@code true} if triangles are intersected;
	 *          {@code false} if intersecting triangles are removed.
	 */
	public boolean isIntersectTriangles()
	{
		return _intersectTriangles;
	}

	/**
	 * Set whether triangles are intersected. If this is disabled, intersecting
	 * triangles are simply omitted from the result.
	 *
	 * @param   enabled     {@code true} if triangles are intersected;
	 *                      {@code false} if intersecting triangles are removed.
	 */
	public void setIntersectTriangles( final boolean enabled )
	{
		_intersectTriangles = enabled;
	}

	/**
	 * Get appearance for faces on the cutting plane.
	 *
	 * @return  Appearance for faces on the cutting plane.
	 */
	public Appearance getSliceAppearance()
	{
		return _sliceAppearance;
	}

	/**
	 * Set appearance for faces on the cutting plane.
	 *
	 * @param   appearance  Appearance for faces on the cutting plane.
	 */
	public void setSliceAppearance( final Appearance appearance )
	{
		_sliceAppearance = appearance;
	}

	/**
	 * Test whether creation of the slice object is enabled.
	 *
	 * @return  {@code true} if a slice object may be created;
	 *          {@code false} if a slice object is never created.
	 */
	public boolean isSliceEnabled()
	{
		return _sliceEnabled;
	}

	/**
	 * Set whether creation of the slice object is enabled.
	 *
	 * @param   enabled     {@code true} if a slice object may be created;
	 *                      {@code false} if a slice object should not be created.
	 */
	public void setSliceEnabled( final boolean enabled )
	{
		_sliceEnabled = enabled;
	}

	/**
	 * Get UV-map used for face of slice.
	 *
	 * @return  UV-map used for face of slice;
	 *          {@code null} if no UV-map is used.
	 */
	public UVMap getSliceUVMap()
	{
		return _sliceUVMap;
	}

	/**
	 * Set UV-map for face of slice.
	 *
	 * This should be set before a slice operation is started.
	 *
	 * @param   uvMap   UV-map for face of slice; {@code null} to use no UV-map.
	 */
	public void setSliceUVMap( final UVMap uvMap )
	{
		_sliceUVMap = uvMap;
	}

	/**
	 * 3D object for intersection slice.
	 *
	 * @return  Intersection slice object;
	 *          {@code null} if the sliced object contained no faces that
	 *          intersected the cutting plane, or no object was sliced yet.
	 */
	public Object3D getSliceObject()
	{
		final Object3D result;

		final Object3D sliceObject = _sliceObject;
		if ( sliceObject != null )
		{
			final List<FaceGroup> faceGroups = sliceObject.getFaceGroups();
			result = faceGroups.isEmpty() ? null : sliceObject;
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * Test whether the bottom object should be capped.
	 *
	 * @return  {@code true} if the bottom object is capped;
	 *          {@code false} if the bottom object is not capped.
	 */
	public boolean isBottomCapped()
	{
		return _bottomCapped;
	}

	/**
	 * Set whether the bottom object should be capped.
	 *
	 * @param   enabled     {@code true} if the bottom object should be capped;
	 *                      {@code false} if the bottom object should not be capped.
	 */
	public void setBottomCapped( final boolean enabled )
	{
		_bottomCapped = enabled;
	}

	/**
	 * Test whether creation of the bottom object is enabled.
	 *
	 * @return  {@code true} if a bottom object may be created;
	 *          {@code false} if a bottom object is never created.
	 */
	public boolean isBottomEnabled()
	{
		return _bottomEnabled;
	}

	/**
	 * Set whether creation of the bottom object is enabled.
	 *
	 * @param   enabled     {@code true} if a bottom object may be created;
	 *                      {@code false} if a bottom object should not be created.
	 */
	public void setBottomEnabled( final boolean enabled )
	{
		_bottomEnabled = enabled;
	}

	/**
	 * Get appearance for bottom object. This overrides the appearance of the
	 * original object.
	 *
	 * @return  Appearance override for bottom object;
	 *          {@code null} if original appearance is kept.
	 */
	public Appearance getBottomAppearance()
	{
		return _bottomAppearance;
	}

	/**
	 * Set appearance for bottom object. This overrides the appearance of the
	 * original object.
	 *
	 * @param   appearance  Appearance override for bottom object;
	 *                      {@code null} to keep original appearance.
	 */
	public void setBottomAppearance( final Appearance appearance )
	{
		_bottomAppearance = appearance;
	}

	/**
	 * Get 3D object with bottom part of last sliced object.
	 *
	 * @return  Bottom part of last sliced object;
	 *          {@code null} the sliced object was completely above the cutting
	 *          plane, or no object was sliced yet.
	 */
	public Object3D getBottomObject()
	{
		return _bottomObject;
	}

	/**
	 * Slice object.
	 *
	 * @param   object  Object to slice.
	 */
	protected void processObject( final Object3D object )
	{
		final Plane3D cuttingPlane = getCuttingPlane();
		final Vector3D planeNormal = cuttingPlane.getNormal();
		final double planeDistance = cuttingPlane.getDistance();

		final List<Vector3D> vertexCoordinates = object.getVertexCoordinates();
		final int vertexCount = vertexCoordinates.size();

		final DoubleArray vertexDistances = _objectVertexDistances;
		vertexDistances.clear();
		vertexDistances.ensureCapacity( vertexCount );

		boolean bottom = false;
		boolean top = false;
		for ( final Vector3D point : vertexCoordinates )
		{
			final double d = Vector3D.dot( planeNormal, point ) - planeDistance;
			vertexDistances.add( d );

			if ( d < 0.0 )
			{
				bottom = true;
			}
			else
			{
				top = true;
			}
		}

		_topObject = ( top && _topEnabled ) ? bottom ? new Object3D() : object : null;
		_sliceObject = top && bottom && _sliceEnabled ? new Object3D() : null;
		_sliceVertices.clear();
		_sliceIntersectionGraph.clear();
		_bottomObject = ( bottom && _bottomEnabled ) ? top ? new Object3D() : object : null;

		if ( top && bottom )
		{
			for ( final FaceGroup faceGroup : object.getFaceGroups() )
			{
				for ( final Face3D face3D : faceGroup.getFaces() )
				{
					processFace( faceGroup, face3D );
				}
			}

			if ( !_sliceIntersectionGraph.isEmpty() )
			{
				buildCaps();
			}
		}
	}

	/**
	 * Slice face.
	 *
	 * @param   faceGroup   Face group to which the sliced face belongs.
	 * @param   face        Face to slice.
	 */
	protected void processFace( final FaceGroup faceGroup, final Face3D face )
	{
		final DoubleArray objectVertexDistances = _objectVertexDistances;
		final Object3D topObject = _topObject;
		final Object3D bottomObject = _bottomObject;
		final Appearance topAppearance = ( _topAppearance != null ) ? _topAppearance : faceGroup.getAppearance();
		final Appearance bottomAppearance = ( _bottomAppearance != null ) ? _bottomAppearance : faceGroup.getAppearance();

		final Vector3D faceNormal = face.getNormal();
		final Tessellation tessellation = face.getTessellation();
		final int faceVertexCount = face.getVertexCount();

		boolean bottom = false;
		boolean top = false;

		for ( int i = 0; i < faceVertexCount; i++ )
		{
			final Vertex3D faceVertex = face.getVertex( i );

			final double d = objectVertexDistances.get( faceVertex.vertexCoordinateIndex );
			if ( d < 0.0 )
			{
				bottom = true;
			}
			else
			{
				top = true;
			}
		}

		if ( top && bottom )
		{
			if ( _intersectFaces )
			{
				Face3DBuilder topFace = null;
				if ( topObject != null )
				{
					topFace = _topFace;
					if ( topFace == null )
					{
						topFace = new Face3DBuilder( topObject, faceNormal );
						_topFace = topFace;
					}
					else
					{
						topFace.initialize( topObject, faceNormal );
					}

					IntArray topVertexMap = _topFaceVertexMap;
					if ( topVertexMap == null )
					{
						topVertexMap = new IntArray( faceVertexCount );
						_topFaceVertexMap = topVertexMap;
					}
					else
					{
						topVertexMap.clear();
					}
					topVertexMap.setSize( faceVertexCount, -1 );
				}

				Face3DBuilder bottomFace = null;
				if ( bottomObject != null )
				{
					bottomFace = _bottomFace;
					if ( bottomFace == null )
					{
						bottomFace = new Face3DBuilder( bottomObject, faceNormal );
						_bottomFace = bottomFace;
					}
					else
					{
						bottomFace.initialize( bottomObject, faceNormal );
					}

					IntArray bottomVertexMap = _bottomFaceVertexMap;
					if ( bottomVertexMap == null )
					{
						bottomVertexMap = new IntArray( faceVertexCount );
						_bottomFaceVertexMap = bottomVertexMap;
					}
					else
					{
						bottomVertexMap.clear();
					}
					bottomVertexMap.setSize( faceVertexCount, -1 );
				}

				for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
				{
					processPrimitive( face, primitive );
				}

				if ( ( topFace != null ) || ( bottomFace != null ) )
				{
					for ( final int[] outline : tessellation.getOutlines() )
					{
						processOutline( face, outline );
					}
				}

				if ( ( topFace != null ) && !topFace.isEmpty() )
				{
					topObject.addFace( topAppearance, faceGroup.isSmooth(), faceGroup.isTwoSided(), topFace.buildFace3D() );
					topFace.reset();
				}

				if ( ( bottomFace != null ) && !bottomFace.isEmpty() )
				{
					bottomObject.addFace( bottomAppearance, faceGroup.isSmooth(), faceGroup.isTwoSided(), bottomFace.buildFace3D() );
					bottomFace.reset();
				}
			}
		}
		else if ( top ) /* face completely above */
		{
			if ( isTopEnabled() )
			{
				copyFace( face, topObject, topObject.getFaceGroup( topAppearance, faceGroup.isSmooth(), faceGroup.isTwoSided() ) );
			}
		}
		else if ( bottom ) /* face completely below */
		{
			if ( isBottomEnabled() )
			{
				copyFace( face, bottomObject, bottomObject.getFaceGroup( bottomAppearance, faceGroup.isSmooth(), faceGroup.isTwoSided() ) );
			}
		}
		else
		{
			throw new AssertionError( "face should be on at least one side!" );
		}
	}

	/**
	 * Process tessellation primitive.
	 *
	 * @param   face        Face being sliced.
	 * @param   primitive   Tessellation primitive to slice.
	 */
	protected void processPrimitive( final Face3D face, final TessellationPrimitive primitive )
	{
		final DoubleArray objectVertexDistances = _objectVertexDistances;

		boolean bottom = false;
		boolean top = false;

		for ( final int vertex : primitive.getVertices() )
		{
			final double d = objectVertexDistances.get( face.getVertex( vertex ).vertexCoordinateIndex );
			if ( d < 0.0 )
			{
				bottom = true;
			}
			else
			{
				top = true;
			}
		}

		if ( top && bottom ) /* primitive intersects cutting plane */
		{
			final int[] triangles = primitive.getTriangles();
			for ( int i = 0; i < triangles.length; i+= 3 )
			{
				final Vertex3D v1 = face.getVertex( triangles[ i ] );
				final Vertex3D v2 = face.getVertex( triangles[ i + 1 ] );
				final Vertex3D v3 = face.getVertex( triangles[ i + 2 ] );

				processTriangle( v1, v2, v3 );
			}
		}
		else if ( top ) /* primitive completely above */
		{
			if ( isTopEnabled() )
			{
				copyPrimitive( face, primitive, _topFace, _topFaceVertexMap );
			}
		}
		else if ( bottom ) /* primitive completely below */
		{
			if ( isBottomEnabled() )
			{
				copyPrimitive( face, primitive, _bottomFace, _bottomFaceVertexMap );
			}
		}
		else
		{
			throw new AssertionError( "primitive should be on at least one side!" );
		}
	}

	/**
	 * Slice triangle.
	 *
	 * @param   v1  First vertex of triangle.
	 * @param   v2  Second vertex of triangle.
	 * @param   v3  Third vertex of triangle.
	 */
	protected void processTriangle( final Vertex3D v1, final Vertex3D v2, final Vertex3D v3 )
	{
		final DoubleArray objectVertexDistances = _objectVertexDistances;
		final Face3DBuilder topFace = isTopEnabled() ? _topFace : null;
		final Face3DBuilder bottomFace = isBottomEnabled() ? _bottomFace : null;
		final boolean intersect = _intersectTriangles;

		final double d1 = objectVertexDistances.get( v1.vertexCoordinateIndex );
		final double d2 = objectVertexDistances.get( v2.vertexCoordinateIndex );
		final double d3 = objectVertexDistances.get( v3.vertexCoordinateIndex );

		if ( d1 < 0.0 )
		{
			if ( d2 < 0.0 )
			{
				if ( d3 < 0.0 )
				{
					//                above
					// -------------- intersection
					//   v1  v2  v3   below
					//
					if ( bottomFace != null )
					{
						bottomFace.addTriangle( v1, v2, v3 );
					}
				}
				else if ( intersect )
				{
					//           v3   above
					// ----i1--i2---- intersection
					//   v1  v2       below
					//
					final Vertex3D i1 = calculateIntersectionVertex( v1, d1, v3, d3 );
					final Vertex3D i2 = calculateIntersectionVertex( v2, d2, v3, d3 );

					if ( topFace != null )
					{
						topFace.addTriangle( i1, i2, v3 );
					}

					addIntersectionEdge( i1.point, i2.point );

					if ( bottomFace != null )
					{
						bottomFace.addQuad( v1, v2, i2, i1 );
					}
				}
			}
			else if ( intersect ) /* d2 >= 0.0 */
			{
				if ( d3 < 0.0 )
				{
					//       v2       above
					// ----i1--i2---- intersection
					//   v1      v3   below
					//
					final Vertex3D i1 = calculateIntersectionVertex( v1, d1, v2, d2 );
					final Vertex3D i2 = calculateIntersectionVertex( v2, d2, v3, d3 );

					if ( topFace != null )
					{
						topFace.addTriangle( i1, v2, i2 );
					}

					addIntersectionEdge( i1.point, i2.point );

					if ( bottomFace != null )
					{
						bottomFace.addQuad( v1, i1, i2, v3 );
					}
				}
				else
				{
					//       v2  v3   above
					// ----i1--i2---- intersection
					//   v1           below
					//
					final Vertex3D i1 = calculateIntersectionVertex( v1, d1, v2, d2 );
					final Vertex3D i2 = calculateIntersectionVertex( v1, d1, v3, d3 );

					if ( topFace != null )
					{
						topFace.addQuad( i1, v2, v3, i2 );
					}

					addIntersectionEdge( i1.point, i2.point );

					if ( bottomFace != null )
					{
						bottomFace.addTriangle( v1, i1, i2 );
					}
				}
			}
		}
		else /* d1 >= 0.0 */
		{
			if ( d2 >= 0.0 )
			{
				if ( d3 >= 0.0 )
				{
					//   v1  v2  v3   above
					// -------------- intersection
					//                below
					//
					if ( topFace != null )
					{
						topFace.addTriangle( v1, v2, v3 );
					}
				}
				else if ( intersect ) /* d3 < 0.0 */
				{
					//   v1  v2       above
					// -----i1--i2-- intersection
					//           v3   below
					//
					final Vertex3D i1 = calculateIntersectionVertex( v1, d1, v3, d3 );
					final Vertex3D i2 = calculateIntersectionVertex( v2, d2, v3, d3 );

					if ( topFace != null )
					{
						topFace.addQuad( v1, v2, i2, i1 );
					}

					addIntersectionEdge( i1.point, i2.point );

					if ( bottomFace != null )
					{
						bottomFace.addTriangle( i1, i2, v3 );
					}
				}
			}
			else if ( intersect ) /* d2 < 0.0 */
			{
				if ( d3 >= 0.0 )
				{
					//   v1      v3   above
					// ----i1--i2---- intersection
					//       v2       below
					//
					final Vertex3D i1 = calculateIntersectionVertex( v1, d1, v2, d2 );
					final Vertex3D i2 = calculateIntersectionVertex( v2, d2, v3, d3 );

					if ( topFace != null )
					{
						topFace.addQuad( v1, i1, i2, v3 );
					}

					addIntersectionEdge( i1.point, i2.point );

					if ( bottomFace != null )
					{
						bottomFace.addTriangle( i1, v2, i2 );
					}
				}
				else /* d3 < 0.0 */
				{
					//   v1           above
					// ----i1--i2---- intersection
					//       v2  v3   below
					//
					final Vertex3D i1 = calculateIntersectionVertex( v1, d1, v2, d2 );
					final Vertex3D i2 = calculateIntersectionVertex( v1, d1, v3, d3 );

					if ( topFace != null )
					{
						topFace.addTriangle( v1, i1, i2 );
					}

					addIntersectionEdge( i1.point, i2.point );

					if ( bottomFace != null )
					{
						bottomFace.addQuad( i1, v2, v3, i2 );
					}
				}
			}
		}
	}

	/**
	 * Slice outline.
	 *
	 * @param   face        Face being sliced.
	 * @param   outline     Outline to slice.
	 */
	protected void processOutline( final Face3D face, final int[] outline )
	{
		final DoubleArray objectVertexDistances = _objectVertexDistances;
		final Face3DBuilder topFace = ( _topObject != null ) ? _topFace : null;
		final IntArray topFaceVertexMap = ( topFace != null ) ? _topFaceVertexMap : null;
		final Face3DBuilder bottomFace = ( _bottomObject != null ) ? _bottomFace : null;
		final IntArray bottomFaceVertexMap = ( bottomFace != null ) ? _bottomFaceVertexMap : null;

		boolean bottom = false;
		boolean top = false;

		for ( final int vertex : outline )
		{
			final double d = objectVertexDistances.get( face.getVertex( vertex ).vertexCoordinateIndex );
			if ( d < 0.0 )
			{
				bottom = true;
			}
			else
			{
				top = true;
			}
		}

		if ( top && bottom )
		{
			final int outlineLength = outline.length;

			IntArray newOutline = _newOutline;
			if ( newOutline == null )
			{
				newOutline = new IntArray( outlineLength );
				_newOutline = newOutline;
			}
			else
			{
				newOutline.clear();
			}

			final boolean closed = outline[ outlineLength - 1 ] == outline[ 0 ];
			if ( closed )
			{
				/*
				 * Slicing algorithm for closed outlines.
				 *
				 * The basic principle of the algorithm is that we build two linked
				 * list of outline vertices; one for the top face, and one for the
				 * bottom face. When intersections are detected, we insert these
				 * into the link lists.
				 */

				/*
				 * Initialize linked lists and intersection containers.
				 */
				IntArray topNext = _topOutlineNext;
				if ( topNext == null )
				{
					topNext = new IntArray( outlineLength * 2 );
					_topOutlineNext = topNext;
				}
				else
				{
					topNext.clear();
				}
				topNext.setSize( outlineLength, -1 );

				IntArray bottomNext = _bottomOutlineNext;
				if ( bottomNext == null )
				{
					bottomNext = new IntArray( outlineLength * 2 );
					_bottomOutlineNext = bottomNext;
				}
				else
				{
					bottomNext.clear();
				}
				bottomNext.setSize( outlineLength, -1 );

				final Vector3D intersectionDirection = Vector3D.cross( face.getNormal(), _cuttingPlane.getNormal() );

				List<Vector3D> intersectionPoints = _outlineIntersections;
				if ( intersectionPoints == null )
				{
					intersectionPoints = new ArrayList<Vector3D>( outlineLength );
					_outlineIntersections = intersectionPoints;
				}
				else
				{
					intersectionPoints.clear();
				}

				DoubleArray intersectionPositions = _outlineIntersectionPositions;
				if ( intersectionPositions == null )
				{
					intersectionPositions = new DoubleArray( outlineLength );
					_outlineIntersectionPositions = intersectionPositions;
				}
				else
				{
					intersectionPositions.clear();
				}

				/*
				 * Step 1: Build link list.
				 *
				 * Each element in the link list is the index of the next
				 * element in the link list. A -1 indicates that the element is
				 * not valid or is the last element of an outline.
				 *
				 * Intersection entries are added at the end of the link lists,
				 * since we don't know the next intersection point on the same
				 * side yet during this loop, that next entry will be set to -1
				 * at the moment, so we can fill in those entries in the next
				 * step.
				 */
				int curIndex = 0;
				Vertex3D curVertex = face.getVertex( outline[ curIndex ] );
				double curDistance = objectVertexDistances.get( curVertex.vertexCoordinateIndex );
				boolean curBottom = ( curDistance < 0.0 );

				for ( int nextIndex = 1; nextIndex < outlineLength; nextIndex++ )
				{
					final Vertex3D nextVertex = face.getVertex( outline[ nextIndex ] );
					final double nextDistance = objectVertexDistances.get( nextVertex.vertexCoordinateIndex );
					final boolean nextSide = ( nextDistance < 0.0 );

					if ( curBottom != nextSide ) // intersection found
					{
						final Vector3D intersectionPoint = calculateIntersectionPoint( curVertex.point, curDistance, nextVertex.point, nextDistance );
						intersectionPoints.add( intersectionPoint );
						intersectionPositions.add( Vector3D.dot( intersectionDirection, intersectionPoint ) );

						if ( nextSide ) // going from top to bottom
						{
							topNext.set( curIndex, bottomNext.size() );
							topNext.add( -1 );
							bottomNext.add( nextIndex );
						}
						else // going from bottom to top
						{

							bottomNext.set( curIndex, topNext.size() );
							topNext.add( nextIndex );
							bottomNext.add( -1 );
						}
					}
					else if ( curBottom ) // bottom edge
					{
						bottomNext.set( curIndex, nextIndex );
					}
					else // top edge
					{
						topNext.set( curIndex, nextIndex );
					}

					curIndex = nextIndex;
					curVertex = nextVertex;
					curDistance = nextDistance;
					curBottom = nextSide;
				}

				/*
				 * Step 2: Link intersection points.
				 *
				 * Intersection entries in the link list must still be linked
				 * together so that the outlines can be closed.
				 *
				 * The sorting loop below iterates 2 times for each pair of
				 * intersection points that will be linked in both link lists
				 * (this removed the -1 entries that were created in step 1).
				 */
				int intervalIndex1 = -1;
				double intervalPosition1 = Double.NEGATIVE_INFINITY;

				for ( boolean inside = false; ; inside = !inside )
				{
					// find next intersection point
					int intervalIndex2 = -1;
					double intervalPosition2 = Double.POSITIVE_INFINITY;

					for ( int index = outlineLength; index < topNext.size(); index++ )
					{
						if ( ( index != intervalIndex1 ) && ( ( topNext.get( index ) < 0 ) || ( bottomNext.get( index ) < 0 ) ) )
						{
							final double position = intersectionPositions.get( index - outlineLength );
							if ( ( position >= intervalPosition1 ) && ( position < intervalPosition2 ) )
							{
								intervalIndex2 = index;
								intervalPosition2 = position;
							}
						}
					}

					if ( intervalIndex2 < 0 )
					{
						// no more intersection points => we're done
						break;
					}

					if ( inside ) // have interval pair (~~ inside polygon)
					{
						if ( topNext.get( intervalIndex1 ) < 0 ) // going from top to bottom at first intersection
						{
							topNext.set( intervalIndex1, intervalIndex2 );
							bottomNext.set( intervalIndex2, intervalIndex1 );
						}
						else // going from bottom to top at first intersection
						{
							topNext.set( intervalIndex2, intervalIndex1 );
							bottomNext.set( intervalIndex1, intervalIndex2 );
						}
					}

					intervalIndex1 = intervalIndex2;
					intervalPosition1 = intervalPosition2;
				}

				/*
				 * Step 3: Build top and bottom outlines.
				 *
				 * Walk through link lists and find all paths they contains.
				 *
				 * When a list entry is used it is set to -1 to mark it as
				 * 'used', so that it won't be found again.
				 */
				if ( topFace != null )
				{
					for ( int topStart = 0; topStart < outlineLength; topStart++ )
					{
						if ( topNext.get( topStart ) >= 0 ) // found top outline
						{
							for ( int index = topStart; index >= 0; index = topNext.set( index, -1 ) )
							{
								if ( index < outlineLength ) // add copy of original vertex
								{
									newOutline.add( copyFaceVertex( face, outline[ index ], topFace, topFaceVertexMap ) );
								}
								else // add intersection vertex
								{
									newOutline.add( topFace.getVertexIndex( intersectionPoints.get( index - outlineLength ), 0.0f, 0.0f ) );
								}
							}

							topFace.addOutline( newOutline.toArray() );
							newOutline.clear();
						}
					}
				}

				if ( bottomFace != null )
				{
					for ( int bottomStart = 0; bottomStart < outlineLength; bottomStart++ )
					{
						if ( bottomNext.get( bottomStart ) >= 0 ) // found bottom outline
						{
							for ( int index = bottomStart; index >= 0; index = bottomNext.set( index, -1 ) )
							{
								if ( index < outlineLength ) // add copy of original vertex
								{
									newOutline.add( copyFaceVertex( face, outline[ index ], bottomFace, bottomFaceVertexMap ) );
								}
								else // add intersection vertex
								{
									newOutline.add( bottomFace.getVertexIndex( intersectionPoints.get( index - outlineLength ), 0.0f, 0.0f ) );
								}
							}

							bottomFace.addOutline( newOutline.toArray() );
							newOutline.clear();
						}
					}
				}
			}
		}
		else if ( top ) // outline completely above
		{
			if ( topFace != null )
			{
				topFace.addOutline( copyFaceVertices( face, outline, topFace, topFaceVertexMap ) );
			}
		}
		else if ( bottom ) // outline completely below
		{
			if ( bottomFace != null )
			{
				bottomFace.addOutline( copyFaceVertices( face, outline, bottomFace, bottomFaceVertexMap ) );
			}
		}
		else // outline is void
		{
			throw new AssertionError( "outline should be on at least one side!" );
		}
	}

	/**
	 * Copy face to the top/bottom object.
	 *
	 * @param   face                Face to copy.
	 * @param   targetObject        Object to add copied face to.
	 * @param   targetFaceGroup     Group to add copied face to.
	 */
	protected void copyFace( final Face3D face, final Object3D targetObject, final FaceGroup targetFaceGroup )
	{
		final int vertexCount = face.getVertexCount();

		final List<Vertex3D> vertices = new ArrayList<Vertex3D>( vertexCount );
		for ( int i = 0; i < vertexCount; i++ )
		{
			final Vertex3D vertex = face.getVertex( i );
			vertices.add( new Vertex3D( vertex.point, vertex.getNormal(), targetObject.getVertexIndex( vertex.point ), vertex.colorMapU, vertex.colorMapV ) );
		}

		targetFaceGroup.addFace( new Face3D( face.getNormal(), vertices, face.getTessellation() ) );
	}

	/**
	 * Copy primitive from the currently processed face to the given
	 * (top/bottom) face.
	 *
	 * @param   face            Face being sliced.
	 * @param   primitive       Primitive to copy.
	 * @param   targetFace      Face to copy primitive to.
	 * @param   faceVertexMap   Maps vertex indices from{@code #face} to {@code targetFace}.
	 */
	protected void copyPrimitive( final Face3D face, final TessellationPrimitive primitive, final Face3DBuilder targetFace, final IntArray faceVertexMap )
	{
		final int[] vertices = copyFaceVertices( face, primitive.getVertices(), targetFace, faceVertexMap );

		if ( primitive instanceof QuadList )
		{
			targetFace.addPrimitive( new QuadList( vertices ) );
		}
		else if ( primitive instanceof QuadStrip )
		{
			targetFace.addPrimitive( new QuadStrip( vertices ) );
		}
		else if ( primitive instanceof TriangleFan )
		{
			targetFace.addPrimitive( new TriangleFan( vertices ) );
		}
		else if ( primitive instanceof TriangleList )
		{
			targetFace.addPrimitive( new TriangleList( vertices ) );
		}
		else if ( primitive instanceof TriangleStrip )
		{
			targetFace.addPrimitive( new TriangleStrip( vertices ) );
		}
		else
		{
			throw new AssertionError( "Don't know how to copy primitive: " + primitive );
		}
	}

	/**
	 * Copy vertices from one face to another. Vertices to be copied are
	 * specified using face vertex indices.
	 *
	 * The given {@code faceVertexMap} is used to reuse previously copied
	 * vertices. Its size must be equal to the number of vertices in
	 * {@code srcFace} and its elements be initialized to -1.
	 *
	 * @param   face            Face being sliced.
	 * @param   vertices        Vertices to copy.
	 * @param   targetFace      Face to copy primitive to.
	 * @param   faceVertexMap   Maps vertex indices from{@code #face} to {@code targetFace}.
	 *
	 * @return  Indices of copied face vertices in {@code targetFace}.
	 */
	protected int[] copyFaceVertices( final Face3D face, final int[] vertices, final Face3DBuilder targetFace, final IntArray faceVertexMap )
	{
		final int[] result = new int[ vertices.length ];

		for ( int i = 0; i < vertices.length; i++ )
		{
			result[ i ] = copyFaceVertex( face, vertices[ i ], targetFace, faceVertexMap );
		}

		return result;
	}

	/**
	 * Copy vertex from one face to another. The vertex to copy is specified
	 * using face vertex indices.
	 *
	 * The given {@code faceVertexMap} is used to reuse previously copied
	 * vertices. Its size must be equal to the number of vertices in
	 * {@code srcFace} and its elements be initialized to -1.
	 *
	 * @param   face            Face being sliced.
	 * @param   vertexIndex     Vertex index in {@code face}.
	 * @param   targetFace      Face to copy vertices to.
	 * @param   faceVertexMap   Maps face vertex indices from {@code face} to
	 *                          {@code targetFace}.
	 *
	 * @return  Index of copied face vertex in {@code targetFace}.
	 */
	protected int copyFaceVertex( final Face3D face, final int vertexIndex, final Face3DBuilder targetFace, final IntArray faceVertexMap )
	{
		int result;

		if ( vertexIndex < faceVertexMap.size() )
		{
			result = faceVertexMap.get( vertexIndex );
			if ( result < 0 )
			{
				result = targetFace.getVertexIndex( face.getVertex( vertexIndex ) );
				faceVertexMap.set( vertexIndex, result );
			}
		}
		else
		{
			result = targetFace.getVertexIndex( face.getVertex( vertexIndex ) );
			faceVertexMap.ensureCapacity( face.getVertexCount() );
			faceVertexMap.setSize( vertexIndex, -1 );
			faceVertexMap.add( result );
		}

		return result;
	}

	/**
	 * Calculate intersection point with cutting plane for an edge between the
	 * two given points. This only works if there is actually an intersection;
	 * if not, the result is unpredictable.
	 *
	 * @param   p1  First point of edge.
	 * @param   d1  Signed distance from the cutting plane to the first vertex.
	 * @param   p2  Second point of edge.
	 * @param   d2  Signed distance from the cutting plane to the second vertex.
	 *
	 * @return  Intersection point on cutting plane.
	 */
	protected Vector3D calculateIntersectionPoint( final Vector3D p1, final double d1, final Vector3D p2, final double d2 )
	{
		final double total = d2 - d1;
		final double r1 = d2 / total;
		final double r2 = -d1 / total;

		final double x = r1 * p1.x + r2 * p2.x;
		final double y = r1 * p1.y + r2 * p2.y;
		final double z = r1 * p1.z + r2 * p2.z;

		return new Vector3D( x, y, z );
	}

	/**
	 * Calculate intersection point with cutting plane for an edge between the
	 * two given vertices. This only works if there is actually an intersection;
	 * if not, the result is unpredictable.
	 *
	 * @param   v1  First vertex of edge.
	 * @param   d1  Signed distance from the cutting plane to the first vertex.
	 * @param   v2  Second vertex of edge.
	 * @param   d2  Signed distance from the cutting plane to the second vertex.
	 *
	 * @return  Vertex on cutting plane with interpolated properties.
	 */
	protected Vertex3D calculateIntersectionVertex( final Vertex3D v1, final double d1, final Vertex3D v2, final double d2 )
	{
		final double total = d2 - d1;
		final double r1 = d2 / total;
		final double r2 = -d1 / total;

		final Vector3D p1 = v1.point;
		final Vector3D p2 = v2.point;

		final double x = r1 * p1.x + r2 * p2.x;
		final double y = r1 * p1.y + r2 * p2.y;
		final double z = r1 * p1.z + r2 * p2.z;

		final float u = (float)( r1 * v1.colorMapU + r2 * v2.colorMapU );
		final float v = (float)( r1 * v1.colorMapV + r2 * v2.colorMapV );

		return new Vertex3D( new Vector3D( x, y, z ), -1, u, v );
	}

	/**
	 * Add intersection edge to the cutting plane. Intersection edges are the
	 * result of faces that intersect the cutting plane.
	 *
	 * @param   v1  First point of edge.
	 * @param   v2  Second point of edge.
	 */
	protected void addIntersectionEdge( final Vector3D v1, final Vector3D v2 )
	{
		if ( ( _sliceEnabled || ( _topEnabled && _topCapped ) || ( _bottomEnabled && _bottomCapped ) ) && !v1.equals( v2 ) )
		{
			final IntersectionNode node1 = addIntersectionNode( v1 );
			final IntersectionNode node2 = addIntersectionNode( v2 );
			if ( node1 != node2 )
			{
				node1.connectTo( node2 );
				node2.connectTo( node1 );
			}
		}
	}

	/**
	 * Add node to plane intersection graph.
	 *
	 * @param   point     3D point in object.
	 *
	 * @return  Node in plane intersection graph.
	 */
	protected IntersectionNode addIntersectionNode( final Vector3D point )
	{
		final Matrix3D object2plane = _object2plane;
		final Vector2D planePoint = new Vector2D( object2plane.transformX( point ), object2plane.transformY( point ) );
		final int sliceVertexIndex = _sliceVertices.indexOfOrAdd( planePoint );

		final Map<Integer,IntersectionNode> graph = _sliceIntersectionGraph;
		final Integer key = Integer.valueOf( sliceVertexIndex );
		IntersectionNode result = graph.get( key );
		if ( result == null )
		{
			result = new IntersectionNode( planePoint, sliceVertexIndex );
			graph.put( key, result );
		}

		return result;
	}

	/**
	 * Build caps from slice intersection graph.
	 */
	protected void buildCaps()
	{
		final Map<Integer, IntersectionNode> intersectionGraph = _sliceIntersectionGraph;
		if ( !intersectionGraph.isEmpty() && ( _sliceEnabled || _topCapped || _bottomCapped ) )
		{
			final Collection<IntersectionNode> nodes = new ArrayList<IntersectionNode>( intersectionGraph.values() );

			/*
			 * Remove all nodes that have only 1 connection or no connection at
			 * all. Keep doing this until no removed nodes are found.
			 */
			boolean nodeRemoved;
			do
			{
				nodeRemoved = false;

				for ( final Iterator<IntersectionNode> it = nodes.iterator(); it.hasNext(); )
				{
					final IntersectionNode node = it.next();

					final Set<IntersectionNode> connectedTo = node._connectedTo;
					if ( connectedTo.size() < 2 )
					{
						for ( final IntersectionNode connectedNode : connectedTo )
						{
							connectedNode._connectedTo.remove( node );
						}
						connectedTo.clear();

						nodeRemoved = true;
						it.remove();
					}
				}
			}
			while ( nodeRemoved );

			/*
			 * Build mesh from connected nodes and build caps from this mesh.
			 */
			if ( !nodes.isEmpty() )
			{
				final List<IntersectionNode> path = new ArrayList<IntersectionNode>();
				final Mesh mesh = new Mesh( Mesh.WindingRule.NONZERO );

				for ( final IntersectionNode node : nodes )
				{
					if ( !node._visited )
					{
						addCapContours( mesh, path, node );
					}
				}

				mesh.finish();

				buildCapFaces( mesh );
			}
		}
	}

	/**
	 * Build faces for mesh that was created for the intersection slice.
	 *
	 * @param   intersectionMesh    Mesh with intersection slice.
	 */
	protected void buildCapFaces( final Mesh intersectionMesh )
	{
		final HashList<Vector2D> planeVertices = _sliceVertices;
		final Tessellator tessellator = new Tessellator( planeVertices, intersectionMesh );

		final List<TessellationPrimitive> ccwPrimitives = tessellator.getCounterClockwisePrimitives();
		if ( !ccwPrimitives.isEmpty() )
		{
			final List<TessellationPrimitive> cwPrimitives = tessellator.getClockwisePrimitives();
			final Tessellation ccwTessellation = new Tessellation( tessellator.getCounterClockwiseOutlines(), ccwPrimitives );
			final Tessellation cwTessellation = new Tessellation( tessellator.getClockwiseOutlines(), cwPrimitives );

			final Vector3D planeNormal = _cuttingPlane.getNormal();
			final Vector3D inversePlaneNormal = planeNormal.inverse();

			final Object3D sliceObject = _sliceEnabled ? _sliceObject : null;
			final boolean sliceEnabled = ( sliceObject != null );
			final Appearance sliceAppearance = _sliceAppearance;
			final UVMap sliceUVMap = _sliceUVMap;
			final UVGenerator sliceUvGenerator = sliceEnabled ? UVGenerator.getColorMapInstance( sliceAppearance, sliceUVMap, planeNormal, false ) : null;
			final List<Vertex3D> sliceVertices = sliceEnabled ? new ArrayList<Vertex3D>( planeVertices.size() ) : null;

			final Object3D topObject = _topCapped ? _topObject : null;
			final boolean topCapEnabled = ( topObject != null );
			final Appearance topAppearance = topCapEnabled ? ( _topAppearance != null ) ? _topAppearance : sliceAppearance : null;
			final UVGenerator topUvGenerator = topCapEnabled ? UVGenerator.getColorMapInstance( topAppearance, sliceUVMap, inversePlaneNormal, false ) : null;
			final List<Vertex3D> topVertices = topCapEnabled ? new ArrayList<Vertex3D>( planeVertices.size() ) : null;

			final Object3D bottomObject = _bottomCapped ? _bottomObject : null;
			final boolean bottomCapEnabled = ( bottomObject != null );
			final Appearance bottomAppearance = bottomCapEnabled ? ( _bottomAppearance != null ) ? _bottomAppearance : sliceAppearance : null;
			final UVGenerator bottomUvGenerator = bottomCapEnabled ? UVGenerator.getColorMapInstance( bottomAppearance, sliceUVMap, planeNormal, false ) : null;
			final List<Vertex3D> bottomVertices = bottomCapEnabled ? new ArrayList<Vertex3D>( planeVertices.size() ) : null;

			final Matrix3D plane2object = _plane2object;

			for ( final Vector2D planePoint : planeVertices )
			{
				final Vector3D point = plane2object.transform( planePoint.getX(), planePoint.getY(), 0.0 );

				if ( sliceEnabled )
				{
					sliceUvGenerator.generate( point );
					sliceVertices.add( new Vertex3D( point, planeNormal, sliceObject.getVertexIndex( point ), sliceUvGenerator.getU(), sliceUvGenerator.getV() ) );
				}

				if ( topCapEnabled )
				{
					topUvGenerator.generate( point );
					topVertices.add( new Vertex3D( point, inversePlaneNormal, topObject.getVertexIndex( point ), topUvGenerator.getU(), topUvGenerator.getV() ) );
				}

				if ( bottomCapEnabled )
				{
					bottomUvGenerator.generate( point );
					bottomVertices.add( new Vertex3D( point, planeNormal, bottomObject.getVertexIndex( point ), bottomUvGenerator.getU(), bottomUvGenerator.getV() ) );
				}
			}

			if ( sliceEnabled )
			{
				sliceObject.addFace( sliceAppearance, false, true, new Face3D( sliceVertices, ccwTessellation ) );
			}

			if ( topCapEnabled )
			{
				topObject.addFace( topAppearance, false, false, new Face3D( topVertices, cwTessellation ) );
			}

			if ( bottomCapEnabled )
			{
				bottomObject.addFace( bottomAppearance, false, false, new Face3D( bottomVertices, ccwTessellation ) );
			}
		}
	}

	/**
	 * This recursive method performs a depth-first walk through the
	 * intersection graph nodes, starting at an arbitrary point. Any loop it
	 * encounters, will be added as a contour to the given {@code mesh}.
	 *
	 * @param   mesh    Intersection mesh.
	 * @param   path    Walked path sofar.
	 * @param   node    Node to walk.
	 */
	protected void addCapContours( final Mesh mesh, final List<IntersectionNode> path, final IntersectionNode node )
	{
		final int pathLength = path.size();
		path.add( node );
		node._visited = true;

		final IntersectionNode previousNode = ( pathLength > 0 ) ? path.get( pathLength - 1 ) : null;
		for ( final IntersectionNode nextNode : node._connectedTo )
		{
			if ( ( nextNode != node ) && ( nextNode != previousNode ) )
			{
				final int cycleIndex = path.indexOf( nextNode );
				if ( cycleIndex < 0 )
				{
					// continue tree walk recursively
					addCapContours( mesh, path, nextNode );
				}
				else
				{
					// found cycle => add contour
					addContour( mesh, path, cycleIndex, path.size() );
				}
			}
		}

		// track back
		path.remove( pathLength );
	}

	/**
	 * Add contour (closed path) to mesh. The path is automatically closed by
	 * linking the last point tot he first point. This method also makes sure
	 * that the added path has counter-clockwise ordering.
	 *
	 * @param   mesh    Mesh to add contour to.
	 * @param   path    List containing path points.
	 * @param   start   Index of first point in list (inclusive).
	 * @param   end     Index of last point in list (exclusive).
	 */
	protected void addContour( final Mesh mesh, final List<IntersectionNode> path, final int start, final int end )
	{
		mesh.beginContour();

		if ( isCounterClockwisePath( path, start, end  ) )
		{
			for ( int i = start; i < end; i++ )
			{
				mesh.addVertex( path.get( i )._planePoint );
			}
		}
		else
		{
			for ( int i = end; --i >= start; )
			{
				mesh.addVertex( path.get( i )._planePoint );
			}
		}

		mesh.endContour();
	}

	/**
	 * Determine whether the given contour (closed path) has counter-clockwise
	 * ordering or not. This is determined by calculating the total included
	 * angle.
	 *
	 * @param   path    List containing path points.
	 * @param   start   Index of first point in list (inclusive).
	 * @param   end     Index of last point in list (exclusive).
	 *
	 * @return  {@code true} if path is counter-clockwise;
	 *          {@code false} if path is clockwise.
	 */
	protected boolean isCounterClockwisePath( final List<IntersectionNode> path, final int start, final int end )
	{
		Vector2D firstDir = null;
		Vector2D prevPoint = path.get( end - 1 )._planePoint;
		Vector2D prevDir = null;

		double totalAngle = 0.0;

		for ( int i = start; i < end; i++ )
		{
			final Vector2D point = path.get( i )._planePoint;
			if ( ( point.x != prevPoint.x ) || ( point.y != prevPoint.y ) )
			{
				final Vector2D dir = Vector2D.direction( prevPoint, point );
				if ( ( prevDir != null ) && !prevDir.equals( dir ) )
				{
					totalAngle += getAngle( prevDir, dir );
				}

				prevPoint = point;
				prevDir = dir;

				if ( firstDir == null )
				{
					firstDir = dir;
				}
			}
		}

		if ( ( firstDir != null ) && ( firstDir != prevDir ) )
		{
			totalAngle += getAngle( prevDir, firstDir );
		}

		return ( totalAngle >= 0.0 );
	}

	/**
	 * Get angle between two direction vectors.
	 *
	 * @param   dir1    First direction vector.
	 * @param   dir2    Second direction vector.
	 *
	 * @return  Angle from first to second direction (ccw in radians).
	 */
	protected static double getAngle( final Vector2D dir1, final Vector2D dir2 )
	{
		final double result;

		// cos/sin values > 1.0 and < -1.0 occur due to rounding errors
		final double cosAngle = dir1.getX() * dir2.getX() + dir1.getY() * dir2.getY();
		if ( cosAngle >= 1.0 )
		{
			result = 0.0;
		}
		else
		{
			final double sinAngle = dir1.getX() * dir2.getY() - dir1.getY() * dir2.getX();
			if ( cosAngle <= -1.0 )
			{
				result = ( sinAngle < 0.0 ) ? -Math.PI : Math.PI;
			}
			else
			{
				result = ( sinAngle < 0.0 ) ? -Math.acos( cosAngle ) : Math.acos( cosAngle );
			}
		}

		return result;
	}

	/**
	 * This is used as a simple graph node.
	 */
	protected static class IntersectionNode
	{
		/**
		 * This flag is used by {@link Object3DSlicer#buildCaps} and
		 * {@link Object3DSlicer#buildCapFaces} to make sure we don't start at
		 * graph walk at a node that was previously visited.
		 */
		private boolean _visited = false;

		/**
		 * 2D point on intersection plane (projected by
		 * {@link Object3DSlicer#_object2plane}.
		 */
		private final Vector2D _planePoint;

		/**
		 * Index of {@link #_planePoint} in {@link Object3DSlicer#_sliceVertices}.
		 */
		private final int _sliceVertexIndex;

		/**
		 * Set of nodes to which this node is connected.
		 */
		private final Set<IntersectionNode> _connectedTo = new HashSet<IntersectionNode>();

		/**
		 * Construct node.
		 *
		 * @param   planePoint          2D point on intersection plane (projected by {@link Object3DSlicer#_object2plane}.
		 * @param   sliceVertexIndex    Index of {@code planePoint} in {@link Object3DSlicer#_sliceVertices}.
		 */
		IntersectionNode( final Vector2D planePoint, final int sliceVertexIndex )
		{
			_planePoint = planePoint;
			_sliceVertexIndex = sliceVertexIndex;
		}

		/**
		 * Connected another node to this node.
		 *
		 * @param   node    Node to connect to this node.
		 */
		public void connectTo( final IntersectionNode node )
		{
			_connectedTo.add( node );
		}

		@Override
		public int hashCode()
		{
			return _sliceVertexIndex;
		}

		@Override
		public boolean equals( final Object obj )
		{
			return ( obj == this ) || ( ( obj instanceof IntersectionNode ) && ( _sliceVertexIndex == ((IntersectionNode)obj)._sliceVertexIndex ) );
		}

		@Override
		public String toString()
		{
			return _sliceVertexIndex + ":" + _planePoint;
		}
	}
}
