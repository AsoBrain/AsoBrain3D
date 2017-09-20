/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
import org.jetbrains.annotations.*;

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
	 * List of 2D vertices used in results.
	 */
	@NotNull
	private final HashList<Vector2D> _vertexList;

	/**
	 * Mesh that was created.
	 */
	@NotNull
	private final Mesh _mesh;

	/**
	 * Cached result of {@link #getCounterClockwiseTriangles()} .
	 */
	private int[] _counterClockwiseTriangles = null;

	/**
	 * Cached result of {@link #getCounterClockwisePrimitives()}.
	 */
	private List<TessellationPrimitive> _counterClockwisePrimitives = null;

	/**
	 * Cached result of {@link #getCounterClockwiseOutlines()}.
	 */
	private List<int[]> _counterClockwiseOutlines = null;

	/**
	 * Cached result of {@link #getClockwiseTriangles()}.
	 */
	private int[] _clockwiseTriangles = null;

	/**
	 * Cached result of {@link #getClockwisePrimitives()}.
	 */
	private List<TessellationPrimitive> _clockwisePrimitives = null;

	/**
	 * Cached result of {@link #getClockwiseOutlines()}.
	 */
	private List<int[]> _clockwiseOutlines = null;

	/**
	 * Create tessellator for the given mesh.
	 *
	 * @param   mesh    Mesh to tessellate.
	 */
	public Tessellator( @NotNull final Mesh mesh )
	{
		this( new HashList<Vector2D>(), mesh );
	}

	/**
	 * Create tessellator for the given mesh.
	 *
	 * @param   vertexList  List of 2D vertices to use in results.
	 * @param   mesh        Mesh to tessellate.
	 */
	public Tessellator( @NotNull final HashList<Vector2D> vertexList, @NotNull final Mesh mesh )
	{
		_vertexList = vertexList;
		_mesh = mesh;
	}

	/**
	 * Constructs counter-clockwise triangles for interior of shape.
	 *
	 * @return  All triangles in the mesh.
	 */
	public int[] getCounterClockwiseTriangles()
	{
		int[] result = _counterClockwiseTriangles;
		if ( result == null )
		{
			result = _mesh.constructTriangles( _vertexList, true );
			_counterClockwiseTriangles = result;
		}
		return result;
	}

	/**
	 * Constructs tessellation of shape using counter-clockwise primitives
	 * (triangle fans, triangle strips, and triangle lists).
	 *
	 * @return  List of primitives that form the tessellation.
	 */
	public List<TessellationPrimitive> getCounterClockwisePrimitives()
	{
		List<TessellationPrimitive> result = _counterClockwisePrimitives;
		if ( result == null )
		{
			result = _mesh.constructPrimitives( _vertexList, true );
			_counterClockwisePrimitives = result;
		}
		return result;
	}

	/**
	 * Construct counter-clockwise outlines of shape. An outline is created for
	 * each boundary between the "inside" and "outside" of the mesh.
	 *
	 * @return  Outlines of shape.
	 */
	public List<int[]> getCounterClockwiseOutlines()
	{
		List<int[]> result = _counterClockwiseOutlines;
		if ( result == null )
		{
			result = _mesh.constructOutlines( _vertexList, true );
			_counterClockwiseOutlines = result;
		}
		return result;
	}

	/**
	 * Constructs clockwise triangles for interior of shape.
	 *
	 * @return  All triangles in the mesh.
	 */
	public int[] getClockwiseTriangles()
	{
		int[] result = _clockwiseTriangles;
		if ( result == null )
		{
			result = _mesh.constructTriangles( _vertexList, false );
			_clockwiseTriangles = result;
		}
		return result;
	}

	/**
	 * Constructs tessellation of shape using clockwise primitives (triangle
	 * fans, triangle strips, and triangle lists).
	 *
	 * @return  List of primitives that form the tessellation.
	 */
	public List<TessellationPrimitive> getClockwisePrimitives()
	{
		List<TessellationPrimitive> result = _clockwisePrimitives;
		if ( result == null )
		{
			result = _mesh.constructPrimitives( _vertexList, false );
			_clockwisePrimitives = result;
		}
		return result;
	}

	/**
	 * Construct clockwise outlines of shape. An outline is created for each
	 * boundary between the "inside" and "outside" of the mesh.
	 *
	 * @return  Outlines of shape.
	 */
	public List<int[]> getClockwiseOutlines()
	{
		List<int[]> result = _clockwiseOutlines;
		if ( result == null )
		{
			result = _mesh.constructOutlines( _vertexList, false );
			_clockwiseOutlines = result;
		}
		return result;
	}

	/**
	 * Get vertices that are used in the results.
	 *
	 * @return  Vertices that are used in the results.
	 */
	@NotNull
	public HashList<Vector2D> getVertexList()
	{
		return _vertexList;
	}
}
