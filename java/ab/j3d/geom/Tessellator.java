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
package ab.j3d.geom;

import java.util.*;

import ab.j3d.*;
import ab.j3d.geom.tessellator.*;

/**
 * Transforms 2-dimensional shapes into tessellated primitives. The result
 * consists primarily of {@link TessellationPrimitive} objects stored in a
 * {@link Tessellation}.
 *
 * The tessellation algorithm implemented as part of the OpenGL Sample
 * Implementation developed by Silicon Graphics, Inc.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class Tessellator
{
	/**
	 * Mesh that was created.
	 */
	private final Mesh _mesh;

	/**
	 * Create tessellator for the given mesh.
	 *
	 * @param   mesh    Mesh to tessellate.
	 */
	public Tessellator( final Mesh mesh )
	{
		_mesh = mesh;
	}

	/**
	 * Constructs triangles for interior of shape.
	 *
	 * @param   vertexList          List of 2D vertices to use in result.
	 * @param   counterClockwise    Construct counter-clockwise primitives.
	 *
	 * @return  All triangles in the mesh.
	 */
	public int[] constructTriangles( final HashList<Vector2D> vertexList, final boolean counterClockwise )
	{
		if ( _mesh == null )
		{
			throw new IllegalStateException( "must defineShape() first" );
		}

		return _mesh.constructTriangles( vertexList, counterClockwise );
	}

	/**
	 * Constructs tessellation of shape using primitives (triangle fans,
	 * triangle strips, and triangle lists).
	 *
	 * @param   vertexList          Vertex list.
	 * @param   counterClockwise    Construct counter-clockwise primitives.
	 *
	 * @return  List of primitives that form the tessellation.
	 */
	public List<TessellationPrimitive> constructPrimitives( final HashList<Vector2D> vertexList, final boolean counterClockwise )
	{
		if ( _mesh == null )
		{
			throw new IllegalStateException( "must defineShape() first" );
		}

		return _mesh.constructPrimitives( vertexList, counterClockwise );
	}

	/**
	 * Construct outlines of shape. An outline is created for each boundary
	 * between the "inside" and "outside" of the mesh.
	 *
	 * @param   vertexList          List of 2D vertices in tessellation result.
	 * @param   counterClockwise    Construct counter-clockwise outlines.
	 *
	 * @return  Outlines of shape.
	 */
	public List<int[]> constructOutlines( final HashList<Vector2D> vertexList, final boolean counterClockwise )
	{
		return _mesh.constructOutlines( vertexList, counterClockwise );
	}
}
