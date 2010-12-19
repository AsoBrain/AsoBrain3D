/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
import ab.j3d.geom.*;
import ab.j3d.model.Face3D.*;
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
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean smooth, final boolean twoSided )
	{
		addFace( vertexIndices, material, ( uvMap != null ) ? uvMap.generate( material, _target._vertexCoordinates, vertexIndices, flipTexture ) : null, null, smooth, twoSided );
	}

	@Override
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Material material, @Nullable final float[] texturePoints, @Nullable final Vector3D[] vertexNormals, final boolean smooth, final boolean twoSided )
	{
		_target.addFace( new Face3D( _target, vertexIndices, material, texturePoints, vertexNormals, smooth, twoSided ) );
	}

	@Override
	protected void addFace( @NotNull final List<Vertex> vertices, @NotNull final Tessellation tessellation, @Nullable final Material material, final boolean smooth, final boolean twoSided )
	{
		_target.addFace( new Face3D( _target, vertices, tessellation, material, smooth, twoSided ) );
	}
}
