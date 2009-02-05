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

import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
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

	public int getVertexIndex( final Vector3D point )
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

	public void setVertexCoordinates( final double[] vertexPoints )
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

	public void setVertexCoordinates( final List<Vector3D> vertexPoints )
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

	public final void setVertexNormals( final double[] vertexNormals )
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

	public final void setVertexNormals( final List<Vector3D> vertexNormals )
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

	/**
	 * Add face to this object.
	 *
	 * @param   vertices    Vertices of face.
	 * @param   material    Material to apply to the face.
	 * @param   smooth      Face is smooth/curved vs. flat.
	 * @param   twoSided    Face is two-sided.
	 */
	private void addFace( final List<Vertex> vertices , final Material material , final boolean smooth , final boolean twoSided )
	{
		_faces.add( new Face( vertices , material , smooth , twoSided ) );
	}

	public final void addFace( final Vector3D[] points , final Material material , final boolean smooth , final boolean twoSided )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( points.length );
		for ( final Vector3D point : points )
		{
			vertices.add( new Vertex( getSharedVertex( point ) ) );
		}

		addFace( vertices , material , smooth , twoSided );
	}

	public final void addFace( final int[] vertexIndices , final Material material , final boolean smooth , final boolean twoSided )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( vertexIndices.length );
		for ( final int vertexIndex : vertexIndices )
		{
			vertices.add( new Vertex( _sharedVertices.get( vertexIndex ) ) );
		}

		addFace( vertices , material , smooth , twoSided );
	}

	public void addFace( final Vector3D[] points , final Material material , final UVMap uvMap , final boolean flipTexture , final boolean smooth , final boolean twoSided )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( points.length );

		final Vector3D normal = GeometryTools.getPlaneNormal( points );
		for ( final Vector3D point : points )
		{
			vertices.add( new Vertex( getSharedVertex( point ) , uvMap.generate( material , point , normal , flipTexture ) ) );
		}

		addFace( vertices , material , smooth , twoSided );
	}

	public void addFace( final int[] vertexIndices , final Material material , final UVMap uvMap , final boolean flipTexture , final boolean smooth , final boolean twoSided )
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

			vertices.add( new Vertex( sharedVertex , uvMap.generate( material , point , normal , flipTexture ) ) );
		}

		addFace( vertices , material , smooth , twoSided );
	}

	public final void addFace( final int[] vertexIndices , final Material material , final float[] textureU , final float[] textureV , final boolean smooth , final boolean twoSided )
	{
		if ( ( textureU != null ) && ( textureV != null ) )
		{
			final ArrayList<SharedVertex> sharedVertices = _sharedVertices;
			final List<Vertex> vertices = new ArrayList<Vertex>( vertexIndices.length );

			for ( int i = 0 ; i < vertexIndices.length ; i++ )
			{
				vertices.add( new Vertex( sharedVertices.get( vertexIndices[ i ] ) , textureU[ i ] , textureV[ i ] ) );
			}

			addFace( vertices , material , smooth , twoSided );
		}
		else
		{
			addFace( vertexIndices , material , smooth , twoSided );
		}
	}

	public final void addFace( final Vector3D[] points , final Material material , final float[] textureU , final float[] textureV , final boolean smooth , final boolean twoSided )
	{
		if ( ( textureU != null ) && ( textureV != null ) )
		{
			final List<Vertex> vertices = new ArrayList<Vertex>( points.length );

			for ( int i = 0 ; i < points.length ; i++ )
			{
				vertices.add( new Vertex( getSharedVertex( points[ i ] ) , textureU[ i ] , textureV[ i ] ) );
			}

			addFace( vertices , material , smooth , twoSided );
		}
		else
		{
			addFace( points , material , smooth , twoSided );
		}
	}

	public void addLine( final Vector3D point1 , final Vector3D point2 , final int stroke , final Material material )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( 3 );
		vertices.add( new Vertex( getSharedVertex( point1 ) ) );
		vertices.add( new Vertex( getSharedVertex( point2 ) ) );
		addFace( vertices , material , false , true );
	}

	public void addTriangle( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Material material , final boolean hasBackface )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( 3 );
		vertices.add( new Vertex( getSharedVertex( point1 ) ) );
		vertices.add( new Vertex( getSharedVertex( point2 ) ) );
		vertices.add( new Vertex( getSharedVertex( point3 ) ) );
		addFace( vertices , material , false , hasBackface );
	}

	public void addTriangle( final Vector3D point1 , final float colorMapU1 , final float colorMapV1 , final Vector3D point2 , final float colorMapU2 , final float colorMapV2 , final Vector3D point3 , final float colorMapU3 , final float colorMapV3 , final Material material , final boolean hasBackface )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( 3 );
		vertices.add( new Vertex( getSharedVertex( point1 ) , colorMapU1 , colorMapV1 ) );
		vertices.add( new Vertex( getSharedVertex( point2 ) , colorMapU2 , colorMapV2 ) );
		vertices.add( new Vertex( getSharedVertex( point3 ) , colorMapU3 , colorMapV3 ) );
		addFace( vertices , material , false , hasBackface );
	}

	public void addQuad( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Vector3D point4 , final Material material , final boolean hasBackface )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( 4 );
		vertices.add( new Vertex( getSharedVertex( point1 ) ) );
		vertices.add( new Vertex( getSharedVertex( point2 ) ) );
		vertices.add( new Vertex( getSharedVertex( point3 ) ) );
		vertices.add( new Vertex( getSharedVertex( point4 ) ) );
		addFace( vertices , material , false , hasBackface );
	}

	public void addQuad( final Vector3D point1 , final float colorMapU1 , final float colorMapV1 , final Vector3D point2 , final float colorMapU2 , final float colorMapV2 ,final Vector3D point3 , final float colorMapU3 , final float colorMapV3 , final Vector3D point4 , final float colorMapU4 , final float colorMapV4 , final Material material , final boolean hasBackface )
	{
		final List<Vertex> vertices = new ArrayList<Vertex>( 4 );
		vertices.add( new Vertex( getSharedVertex( point1 ) , colorMapU1 , colorMapV1 ) );
		vertices.add( new Vertex( getSharedVertex( point2 ) , colorMapU2 , colorMapV2 ) );
		vertices.add( new Vertex( getSharedVertex( point3 ) , colorMapU3 , colorMapV3 ) );
		vertices.add( new Vertex( getSharedVertex( point4 ) , colorMapU4 , colorMapV4 ) );
		addFace( vertices , material, false, hasBackface );
	}

	public void addCircle( final Vector3D centerPoint , final double radius , final Vector3D normal , final Vector3D extrusion , final int stroke , final Material material , final boolean fill )
	{
		final Matrix3D  base      = Matrix3D.getPlaneTransform( centerPoint , normal , true );
		final Ellipse2D ellipse2d = new Ellipse2D.Double( -radius , -radius , radius * 2.0 , radius * 2.0 );

		addExtrudedShape( ellipse2d , radius * 0.02 , extrusion , base , material , false , material , false , material , false , true , false , false );
	}

	public void addText( final String text , final Vector3D origin , final double height , final double rotationAngle , final double obliqueAngle , final Vector3D extrusion , final Material material )
	{
	}
}