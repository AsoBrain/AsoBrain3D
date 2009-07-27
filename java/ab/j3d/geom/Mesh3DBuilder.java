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
package ab.j3d.geom;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.istack.internal.Nullable;
import org.jetbrains.annotations.NotNull;

import ab.j3d.Material;
import ab.j3d.Vector3D;
import ab.j3d.geom.Mesh3D.Face;
import ab.j3d.geom.Mesh3D.SharedVertex;
import ab.j3d.geom.Mesh3D.Vertex;

/**
 * This class provides an implementation of the {@link Abstract3DObjectBuilder}
 * class for creating an {@link Mesh3D} instance.
 *
 * @see     Abstract3DObjectBuilder
 * @see     Mesh3D
 *
 * @author  HRM Bleumink
 * @author  Peter S. Heijnen
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Mesh3DBuilder
	extends Abstract3DObjectBuilder
{
	/**
	 * Shared vertices.
	 */
	private ArrayList<SharedVertex> _sharedVertices = new ArrayList<SharedVertex>();

	/**
	 * Maps point to index in {@link #_sharedVertices}.
	 */
	private Map<Vector3D,Integer> _sharedVertexIndexMap = new HashMap<Vector3D,Integer>();

	/**
	 * Faces.
	 */
	private List<Face> _faces = new ArrayList<Face>();

	/**
	 * Construct builder.
	 */
	public Mesh3DBuilder()
	{
	}

	/**
	 * Get {@link Mesh3D} that was built.
	 *
	 * @return  The {@link Mesh3D} that was built.
	 */
	public Mesh3D getMesh3D()
	{
		return new Mesh3D( _faces );
	}

	public int getVertexIndex( @NotNull final Vector3D point )
	{
		final int result;

		final List<SharedVertex> sharedVertices = _sharedVertices;
		final Map<Vector3D,Integer> indexMap = _sharedVertexIndexMap;

		/*
		 * Build index map on-demand.
		 */
		if ( indexMap.isEmpty() && !sharedVertices.isEmpty() )
		{
			for ( int i = 0 ; i < sharedVertices.size() ; i++ )
			{
				final SharedVertex sharedVertex =  sharedVertices.get( i );
				indexMap.put( sharedVertex.point , Integer.valueOf( i ) );
			}
		}

		/*
		 * Get index from map. Create vertex if the point was not found.
		 */
		final Integer index = indexMap.get( point );
		if ( index != null )
		{
			result = index;
		}
		else
		{
			indexMap.put( point , Integer.valueOf( result = sharedVertices.size() ) );
			sharedVertices.add( new SharedVertex( point ) );
		}

		return result;
	}

	/**
	 * Get shared vertex at the specified point. If no vertex was found
	 * at the specified point, a new one is created.
	 *
	 * @param   point   Point to get shared vertex for.
	 *
	 * @return  Shared vertex.
	 */
	private SharedVertex getSharedVertex( final Vector3D point )
	{
		return _sharedVertices.get( getVertexIndex( point ) );
	}

	public void setVertexCoordinates( @NotNull final double[] vertexPoints )
	{
		if ( !_faces.isEmpty() )
			throw new IllegalStateException( "can't set coordinates after faces were added" );

		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;
		final Map<Vector3D,Integer> indexMap = _sharedVertexIndexMap;

		indexMap.clear();
		sharedVertices.clear();
		sharedVertices.ensureCapacity( vertexPoints.length / 3 );

		for ( int i = 0 ; i < vertexPoints.length ; i += 3 )
		{
			sharedVertices.add( new SharedVertex( new Vector3D( vertexPoints[ i ] , vertexPoints[ i + 1 ] , vertexPoints[ i + 2 ] ) ) );
		}
	}

	public void setVertexCoordinates( @NotNull final List<Vector3D> vertexPoints )
	{
		if ( !_faces.isEmpty() )
			throw new IllegalStateException( "can't set coordinates after faces were added" );

		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;
		final Map<Vector3D,Integer> indexMap = _sharedVertexIndexMap;

		indexMap.clear();
		sharedVertices.clear();
		sharedVertices.ensureCapacity( vertexPoints.size() );

		for ( final Vector3D point : vertexPoints )
		{
			sharedVertices.add( new SharedVertex( point ) );
		}
	}

	public final void setVertexNormals( @NotNull final double[] vertexNormals )
	{
		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;
		if ( vertexNormals.length / 3 > sharedVertices.size() )
			throw new IllegalStateException( "can't set more normals than there are vertices" );

		for ( int i = 0 , j = 0 ; i < vertexNormals.length ; i += 3 , j++ )
		{
			final SharedVertex sharedVertex = sharedVertices.get( j );
			sharedVertex.setNormal( new Vector3D( vertexNormals[ i ] , vertexNormals[ i + 1 ] , vertexNormals[ i + 1 ] ) );
		}
	}

	public final void setVertexNormals( @NotNull final List<Vector3D> vertexNormals )
	{
		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;
		if ( vertexNormals.size() > sharedVertices.size() )
			throw new IllegalStateException( "can't set more normals than there are vertices" );

		for ( int i = 0 ; i < vertexNormals.size() ; i++ )
		{
			final Vector3D normal = vertexNormals.get( i );
			final SharedVertex sharedVertex = sharedVertices.get( i );
			sharedVertex.setNormal( normal );
		}
	}

	public final void addFace( @NotNull final Vector3D[] points , @Nullable final Material material , final boolean smooth , final boolean twoSided )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( points.length );
		for ( final Vector3D point : points )
		{
			vertices.add( new Vertex( getSharedVertex( point ) ) );
		}

		_faces.add( new Face( vertices , material , smooth , twoSided ) );
	}

	public final void addFace( @NotNull final int[] vertexIndices , @Nullable final Material material , final boolean smooth , final boolean twoSided )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( vertexIndices.length );
		for ( final int vertexIndex : vertexIndices )
		{
			vertices.add( new Vertex( _sharedVertices.get( vertexIndex ) ) );
		}

		_faces.add( new Face( vertices , material , smooth , twoSided ) );
	}

	public void addFace( @NotNull final int[] vertexIndices , @Nullable final Material material , @Nullable final UVMap uvMap , final boolean flipTexture , final boolean smooth , final boolean twoSided )
	{
		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;

		final Vector3D normal;
		if ( vertexIndices.length > 3 )
		{
			final Vector3D p1 = sharedVertices.get( vertexIndices[ 0 ] ).point;
			final Vector3D p2 = sharedVertices.get( vertexIndices[ 1 ] ).point;
			final Vector3D p3 = sharedVertices.get( vertexIndices[ 2 ] ).point;

			normal = GeometryTools.getPlaneNormal( p1 , p2 , p3 );
		}
		else
		{
			normal = null;
		}

		final List<Vertex> vertices = new ArrayList<Vertex>( vertexIndices.length );
		for ( final int vertexIndex : vertexIndices )
		{
			final SharedVertex sharedVertex = sharedVertices.get( vertexIndex );
			final Vector3D point = sharedVertex.point;

			vertices.add( ( uvMap != null ) ? new Vertex( sharedVertex , uvMap.generate( material , point , normal , flipTexture ) ) : new Vertex(  sharedVertex ) );
		}

		_faces.add( new Face( vertices , material , smooth , twoSided ) );
	}

	public void addFace( @NotNull final Vector3D[] points , @Nullable final Material material , @Nullable final Point2D.Float[] texturePoints , @Nullable final Vector3D[] vertexNormals , final boolean smooth , final boolean twoSided )
	{
		final int vertexCount = points.length;

		final List<Vertex> vertices = new ArrayList<Vertex>( vertexCount );

		for ( int i = 0 ; i < vertexCount; i++ )
		{
			final Vertex vertex = new Vertex( getSharedVertex( points[ i ] ) );

			if ( texturePoints != null )
			{
				final Point2D.Float texturePoint = texturePoints[ i ];
				vertex.colorMapU = texturePoint.x;
				vertex.colorMapV = texturePoint.y;
			}

			if ( vertexNormals != null )
			{
				vertex.setNormal( vertexNormals[ i ] );
			}

			vertices.add( vertex );
		}

		_faces.add( new Face( vertices , material , smooth , twoSided ) );
	}

	public void addFace( @NotNull final int[] vertexIndices , @Nullable final Material material , @Nullable final Point2D.Float[] texturePoints , @Nullable final Vector3D[] vertexNormals , final boolean smooth , final boolean twoSided )
	{
		final int vertexCount = vertexIndices.length;
		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;

		final List<Vertex> vertices = new ArrayList<Vertex>( vertexCount );

		for ( int i = 0 ; i < vertexCount; i++ )
		{
			final Vertex vertex = new Vertex( sharedVertices.get( vertexIndices[ i ] ) );

			if ( texturePoints != null )
			{
				final Point2D.Float texturePoint = texturePoints[ i ];
				vertex.colorMapU = texturePoint.x;
				vertex.colorMapV = texturePoint.y;
			}

			if ( vertexNormals != null )
			{
				vertex.setNormal( vertexNormals[ i ] );
			}

			vertices.add( vertex );
		}

		_faces.add( new Face( vertices , material , smooth , twoSided ) );
	}

	public void addText( @NotNull final String text , @NotNull final Vector3D origin , final double height , final double rotationAngle , final double obliqueAngle , final Vector3D extrusion , final Material material )
	{
		throw new AssertionError( "not implemented" );
	}
}