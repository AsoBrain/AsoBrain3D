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

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.geom.tessellator.*;
import ab.j3d.geom.tessellator.Mesh.*;
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
	 * Mesh that was created.
	 */
	private Mesh _mesh = null;

	/**
	 * Defines a shape to tessellate. Any curves in the shape are flattened
	 * using the given flatness.
	 *
	 * @param   shape       Shape to tessellate.
	 * @param   flatness    Maximum distance between line segments and the
	 *                      curves they approximate.
	 */
	public void defineShape( @NotNull final Shape shape, final double flatness )
	{
		final PathIterator iterator = shape.getPathIterator( null, flatness );

		final WindingRule windingRule;

		switch ( iterator.getWindingRule() )
		{
			case PathIterator.WIND_EVEN_ODD:
				windingRule = Mesh.WindingRule.ODD;
				break;

			case PathIterator.WIND_NON_ZERO:
				windingRule = Mesh.WindingRule.NONZERO;
				break;

			default:
				throw new AssertionError( "Illegal winding rule: " + iterator.getWindingRule() );
		}

		final Mesh mesh = new Mesh( windingRule );
		tessellateShape( mesh, iterator );
		mesh.finish();
		_mesh = mesh;
	}

	/**
	 * Add contour(s) from a {@link PathIterator}.
	 *
	 * @param   mesh            Mesh to add contours to.
	 * @param   pathIterator    Path iterator to create contour from.
	 */
	private static void tessellateShape( final Mesh mesh, @NotNull final PathIterator pathIterator )
	{
		final LinkedList<double[]> points = new LinkedList<double[]>();
		double[] cur = null;

		for ( ; !pathIterator.isDone() ; pathIterator.next() )
		{
			final double[] coords = new double[ 2 ];
			switch ( pathIterator.currentSegment( coords ) )
			{
				case PathIterator.SEG_MOVETO :
					points.clear();
					points.add( coords );
					cur = coords;
					break;

				case PathIterator.SEG_LINETO :
					if ( ( cur == null ) || !MathTools.almostEqual( coords[ 0 ], cur[ 0 ] ) || !MathTools.almostEqual( coords[ 1 ], cur[ 1 ] ) )
					{
						points.add( coords );
					}
					cur = coords;
					break;

				case PathIterator.SEG_CLOSE :
					if ( points.size() > 2 )
					{
						final double[] start = points.getFirst();

						final double[] last = points.getLast();
						if ( MathTools.almostEqual( start[ 0 ], last[ 0 ] ) && MathTools.almostEqual( start[ 1 ], last[ 1 ] ) )
						{
							points.removeLast();
						}

						if ( points.size() > 2 )
						{
							mesh.beginContour();

							for ( final double[] point : points )
							{
								mesh.addVertex( point[ 0 ], point[ 1 ] );
							}

							mesh.endContour();
						}

						points.clear();
						cur = start;
					}
					break;
			}
		}
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
