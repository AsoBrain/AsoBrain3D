/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2010 Peter S. Heijnen
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
import ab.j3d.geom.Mesh3D.*;
import org.jetbrains.annotations.*;

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
	private final ArrayList<SharedVertex> _sharedVertices = new ArrayList<SharedVertex>();

	/**
	 * Maps point to index in {@link #_sharedVertices}.
	 */
	private final Map<Vector3D,Integer> _sharedVertexIndexMap = new HashMap<Vector3D,Integer>();

	/**
	 * Faces.
	 */
	private final List<Face> _faces = new ArrayList<Face>();

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

	@Override
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
				indexMap.put( sharedVertex.point, Integer.valueOf( i ) );
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
			result = sharedVertices.size();
			indexMap.put( point, Integer.valueOf( result ) );
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

	@Override
	public void setVertexCoordinates( @NotNull final List<Vector3D> vertexPoints )
	{
		if ( !_faces.isEmpty() )
		{
			throw new IllegalStateException( "can't set coordinates after faces were added" );
		}

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

	@Override
	public void setVertexNormals( @NotNull final double[] vertexNormals )
	{
		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;
		if ( vertexNormals.length / 3 > sharedVertices.size() )
		{
			throw new IllegalStateException( "can't set more normals than there are vertices" );
		}

		for ( int i = 0, j = 0 ; i < vertexNormals.length ; i += 3, j++ )
		{
			final SharedVertex sharedVertex = sharedVertices.get( j );
			sharedVertex.setNormal( new Vector3D( vertexNormals[ i ], vertexNormals[ i + 1 ], vertexNormals[ i + 1 ] ) );
		}
	}

	@Override
	public void setVertexNormals( @NotNull final List<Vector3D> vertexNormals )
	{
		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;
		if ( vertexNormals.size() > sharedVertices.size() )
		{
			throw new IllegalStateException( "can't set more normals than there are vertices" );
		}

		for ( int i = 0 ; i < vertexNormals.size() ; i++ )
		{
			final Vector3D normal = vertexNormals.get( i );
			final SharedVertex sharedVertex = sharedVertices.get( i );
			sharedVertex.setNormal( normal );
		}
	}

	@Override
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Material material, final boolean smooth, final boolean twoSided )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( points.length );
		for ( final Vector3D point : points )
		{
			vertices.add( new Vertex( getSharedVertex( point ) ) );
		}

		_faces.add( new Face( vertices, material, smooth, twoSided ) );
	}

	@Override
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Material material, final boolean smooth, final boolean twoSided )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( vertexIndices.length );
		for ( final int vertexIndex : vertexIndices )
		{
			vertices.add( new Vertex( _sharedVertices.get( vertexIndex ) ) );
		}

		_faces.add( new Face( vertices, material, smooth, twoSided ) );
	}

	@Override
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean smooth, final boolean twoSided )
	{
		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;

		final List<Vertex> vertices = new ArrayList<Vertex>( vertexIndices.length );
		for ( final int vertexIndex : vertexIndices )
		{
			vertices.add( new Vertex( sharedVertices.get( vertexIndex ) ) );
		}

		if ( uvMap != null )
		{
			final Vector3D[] vertexCoordinates = new Vector3D[ vertexIndices.length ];
			for ( int i = 0; i < vertexIndices.length; i++ )
			{
				final Vertex vertex = vertices.get( i );
				vertexCoordinates[ i ] = vertex.getPoint();
			}

			final float[] texturePoints = uvMap.generate( material, Arrays.asList( vertexCoordinates ), null, flipTexture );

			for ( int i = 0; i < vertexIndices.length; i++ )
			{
				final Vertex vertex = vertices.get( i );
				final int texturePointIndex = i * 2;
				vertex.colorMapU = texturePoints[ texturePointIndex ];
				vertex.colorMapV = texturePoints[ texturePointIndex + 1 ];
			}
		}

		_faces.add( new Face( vertices, material, smooth, twoSided ) );
	}

	@Override
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Material material, @Nullable final float[] texturePoints, @Nullable final Vector3D[] vertexNormals, final boolean smooth, final boolean twoSided )
	{
		final int vertexCount = points.length;

		final List<Vertex> vertices = new ArrayList<Vertex>( vertexCount );

		for ( int i = 0 ; i < vertexCount; i++ )
		{
			final Vertex vertex = new Vertex( getSharedVertex( points[ i ] ) );

			if ( texturePoints != null )
			{
				vertex.colorMapU = texturePoints[ i * 2 ];
				vertex.colorMapV = texturePoints[ i * 2 + 1 ];
			}

			if ( vertexNormals != null )
			{
				vertex.setNormal( vertexNormals[ i ] );
			}

			vertices.add( vertex );
		}

		_faces.add( new Face( vertices, material, smooth, twoSided ) );
	}

	@Override
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Material material, @Nullable final float[] texturePoints, @Nullable final Vector3D[] vertexNormals, final boolean smooth, final boolean twoSided )
	{
		final int vertexCount = vertexIndices.length;
		final ArrayList<SharedVertex> sharedVertices = _sharedVertices;

		final List<Vertex> vertices = new ArrayList<Vertex>( vertexCount );

		for ( int i = 0 ; i < vertexCount; i++ )
		{
			final Vertex vertex = new Vertex( sharedVertices.get( vertexIndices[ i ] ) );

			if ( texturePoints != null )
			{
				vertex.colorMapU = texturePoints[ i * 2 ];
				vertex.colorMapV = texturePoints[ i * 2 + 1 ];
			}

			if ( vertexNormals != null )
			{
				vertex.setNormal( vertexNormals[ i ] );
			}

			vertices.add( vertex );
		}

		_faces.add( new Face( vertices, material, smooth, twoSided ) );
	}

	@Override
	public void addText( @NotNull final String text, @NotNull final Vector3D origin, final double height, final double rotationAngle, final double obliqueAngle, final Vector3D extrusion, final Material material )
	{
		throw new AssertionError( "not implemented" );
	}
}