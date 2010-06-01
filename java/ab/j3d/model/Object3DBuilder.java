/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2010
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

import java.awt.geom.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

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

	@Override
	public int getVertexIndex( @NotNull final Vector3D point )
	{
		return _target._vertexCoordinates.indexOfOrAdd( point );
	}

	/**
	 * Get number of vertices in object.
	 *
	 * @return  Number of vertices.
	 */
	public int getVertexCount()
	{
		return _target._vertexCoordinates.size();
	}

	@Override
	public void setVertexCoordinates( @NotNull final List<Vector3D> vertexCoordinates )
	{
		_target.setVertexCoordinates( vertexCoordinates );
	}

	@Override
	public void setVertexNormals( @NotNull final double[] vertexNormals )
	{
		_target.setVertexNormals( vertexNormals );
	}

	@Override
	public void setVertexNormals( @NotNull final List<Vector3D> vertexNormals )
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

	@Override
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean smooth, final boolean twoSided )
	{
		addFace( vertexIndices, material, ( uvMap != null ) ? uvMap.generate( material, _target._vertexCoordinates, vertexIndices, flipTexture ) : null, null, smooth, twoSided );
	}

	@Override
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Material material, @Nullable final Point2D.Float[] texturePoints, @Nullable final Vector3D[] vertexNormals, final boolean smooth, final boolean twoSided )
	{
		final int   vertexCount   = points.length;
		final int[] vertexIndices = new int[ vertexCount ];

		for ( int i = 0 ; i < vertexCount ; i++ )
		{
			vertexIndices[ i ] = getVertexIndex( points[ i ] );
		}

		addFace( vertexIndices, material, texturePoints, vertexNormals, smooth, twoSided );
	}

	@Override
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Material material, @Nullable final Point2D.Float[] texturePoints, @Nullable final Vector3D[] vertexNormals, final boolean smooth, final boolean twoSided )
	{
		_target.addFace( new Face3D( _target, vertexIndices, material, texturePoints, vertexNormals, smooth, twoSided ) );
	}

	@Override
	public void addText( @NotNull final String text, @NotNull final Vector3D origin, final double height, final double rotationAngle, final double obliqueAngle, @Nullable final Vector3D extrusion, @Nullable final Material material )
	{
		throw new AssertionError( "not implemented" );
	}
}
