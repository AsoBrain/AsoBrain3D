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
package ab.j3d.model;

import java.awt.geom.Point2D;
import java.util.List;

import ab.j3d.Material;
import ab.j3d.Vector3D;
import ab.j3d.geom.Abstract3DObjectBuilder;
import ab.j3d.geom.UVMap;

import com.numdata.oss.ArrayTools;

/**
 * This class provides an implementation of the {@link Abstract3DObjectBuilder}
 * class for creating an {@link Object3D} instance.
 *
 * @see     Abstract3DObjectBuilder
 * @see     Object3D
 *
 * @author  HRM Bleumink
 * @author  Peter S. Heijnen
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class Object3DBuilder
	extends Abstract3DObjectBuilder
{
	/**
	 * The {@link Object3D} being build.
	 */
	private final Object3D _target;

	/**
	 * Construct builder.
	 */
	public Object3DBuilder()
	{
		_target = new Object3D();
	}

	/**
	 * Construct builder.
	 *
	 * @param   target  Target object.
	 */
	Object3DBuilder( final Object3D target )
	{
		_target = target;
	}

	/**
	 * Get {@link Node3D} that was built.
	 *
	 * @return  The {@link Node3D} that was built.
	 */
	public Object3D getObject3D()
	{
		return _target;
	}

	public int getVertexIndex( final Vector3D point )
	{
		return getVertexIndex( point.x , point.y , point.z );
	}

	/**
	 * Get number of vertices in object.
	 *
	 * @return  Number of vertices.
	 */
	public int getVertexCount()
	{
		final double[] vertexCoordinates = _target._vertexCoordinates;
		return ( vertexCoordinates == null ) ? 0 : vertexCoordinates.length / 3;
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
	private int getVertexIndex( final double x , final double y , final double z )
	{
		final int vertexCount = getVertexCount();
		final double[] vertexCoordinates = _target._vertexCoordinates;

		int index = vertexCount * 3;
		while ( ( index -= 3 ) >= 0 )
		{
			if ( ( x == vertexCoordinates[ index     ] ) &&
			     ( y == vertexCoordinates[ index + 1 ] ) &&
			     ( z == vertexCoordinates[ index + 2 ] ) )
				break;
		}

		if ( index < 0 )
		{
			index = vertexCount * 3;
			final double[] newCoords = (double[])ArrayTools.ensureLength( vertexCoordinates , double.class , -1 , index + 3 );
			newCoords[ index     ] = x;
			newCoords[ index + 1 ] = y;
			newCoords[ index + 2 ] = z;

			_target._vertexCoordinates = newCoords;
		}

		return index / 3;
	}

	public void setVertexCoordinates( final double[] vertexCoordinates )
	{
		_target.setVertexCoordinates( vertexCoordinates );
	}

	public void setVertexCoordinates( final List<Vector3D> vertexPoints )
	{
		final int vertexCount = vertexPoints.size();
		final double[] doubles = new double[ vertexCount * 3 ];
		int i = 0;
		int j = 0;
		while ( i < vertexCount )
		{
			final Vector3D point = vertexPoints.get( i++ );
			doubles[ j++ ] = point.x;
			doubles[ j++ ] = point.y;
			doubles[ j++ ] = point.z;
		}
		setVertexCoordinates( doubles );
	}

	public void setVertexNormals( final double[] vertexNormals )
	{
		_target.setVertexNormals( vertexNormals );
	}

	public void setVertexNormals( final List<Vector3D> vertexNormals )
	{
		final int vertexCount = vertexNormals.size();
		final double[] doubles = new double[ vertexCount * 3 ];
		int i = 0;
		int j = 0;
		while ( i < vertexCount )
		{
			final Vector3D normal = vertexNormals.get( i++ );
			doubles[ j++ ] = normal.x;
			doubles[ j++ ] = normal.y;
			doubles[ j++ ] = normal.z;
		}
		setVertexNormals( doubles );
	}

	public void addFace( final int[] vertexIndices , final Material material , final UVMap uvMap , final boolean flipTexture , final boolean smooth , final boolean twoSided )
	{
		addFace( vertexIndices , material , uvMap.generate( material , _target._vertexCoordinates , vertexIndices , flipTexture ) , null , smooth , twoSided );
	}

	public void addFace( final Vector3D[] points , final Material material , final Point2D.Float[] texturePoints , final Vector3D[] vertexNormals , final boolean smooth , final boolean twoSided )
	{
		final int   vertexCount   = points.length;
		final int[] vertexIndices = new int[ vertexCount ];

		for ( int i = 0 ; i < vertexCount ; i++ )
		{
			final Vector3D vertex = points[ i ];
			vertexIndices[ i ] = getVertexIndex( vertex.x , vertex.y , vertex.z );
		}

		addFace( vertexIndices , material , texturePoints , vertexNormals , smooth , twoSided );
	}

	public void addFace( final int[] vertexIndices , final Material material , final Point2D.Float[] texturePoints , final Vector3D[] vertexNormals , final boolean smooth , final boolean twoSided )
	{
		_target.addFace( new Face3D( _target , vertexIndices , material , texturePoints , vertexNormals , smooth , twoSided ) );
	}

	public void addText( final String text , final Vector3D origin , final double height , final double rotationAngle , final double obliqueAngle , final Vector3D extrusion , final Material material )
	{
		throw new AssertionError( "not implemented" );
	}
}
