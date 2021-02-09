/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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
 */
package ab.j3d.geom;

import java.util.*;

import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Represents a tessellation.
 *
 * @author Peter S. Heijnen
 */
public class Tessellation
{
	/**
	 * Primitive list for a single triangle face.
	 * <dl>
	 *  <dt><strong>WARNING</strong></dt>
	 *  <dd>This collection is shared, do not change its contents!</dd>
	 * </dl>
	 */
	@SuppressWarnings( "PublicStaticCollectionField" )
	public static final List<TessellationPrimitive> TRIANGLE_PRIMITIVES = Collections.singletonList( new TriangleList( new int[] { 2, 1, 0 } ) );

	/**
	 * {@link Tessellation} for a triangle with outline.
	 * <pre>
	 *     2
	 *     |\
	 *     | \
	 *     |  \
	 *     |___\
	 *     0    1
	 * </pre>
	 */
	public static final Tessellation TRIANGLE = new Tessellation( Arrays.asList( new int[][] { { 0, 1, 2, 0 } } ), TRIANGLE_PRIMITIVES );

	/**
	 * {@link Tessellation} for a triangle without an outline.
	 * <pre>
	 *     2
	 *     |\
	 *     | \
	 *     |  \
	 *     |___\
	 *     0    1
	 * </pre>
	 */
	public static final Tessellation TRIANGLE_NO_OUTLINE = new Tessellation( Collections.emptyList(), TRIANGLE_PRIMITIVES );

	/**
	 * {@link Tessellation} for a triangle with a partial outline along vertices
	 * 0, 1, and 2.
	 * <pre>
	 *     2
	 *     :\
	 *     : \
	 *     :  \
	 *     :___\
	 *     0    1
	 * </pre>
	 */
	public static final Tessellation TRIANGLE_OUTLINE_012 = new Tessellation( Arrays.asList( new int[][] { { 0, 1, 2 } } ), TRIANGLE_PRIMITIVES );

	/**
	 * {@link Tessellation} for a triangle with a partial outline from vertex 0
	 * to vertex 1.
	 * <pre>
	 *     1
	 *     :\
	 *     : \
	 *     :  \
	 *     :...\
	 *     2    0
	 * </pre>
	 */
	public static final Tessellation TRIANGLE_OUTLINE_01 = new Tessellation( Arrays.asList( new int[][] { { 0, 1 } } ), TRIANGLE_PRIMITIVES );

	/**
	 * {@link Tessellation} for a triangle with a partial outline from vertex 1
	 * to vertex 2.
	 * <pre>
	 *     2
	 *     :\
	 *     : \
	 *     :  \
	 *     :...\
	 *     0    1
	 * </pre>
	 */
	public static final Tessellation TRIANGLE_OUTLINE_12 = new Tessellation( Arrays.asList( new int[][] { { 1, 2 } } ), TRIANGLE_PRIMITIVES );

	/**
	 * {@link Tessellation} for a triangle with a partial outline from vertex 0
	 * to vertex 2.
	 * <pre>
	 *     0
	 *     :\
	 *     : \
	 *     :  \
	 *     :...\
	 *     1    2
	 * </pre>
	 */
	public static final Tessellation TRIANGLE_OUTLINE_02 = new Tessellation( Arrays.asList( new int[][] { { 0, 2 } } ), TRIANGLE_PRIMITIVES );

	/**
	 * Primitive list for a single quad face.
	 * <dl>
	 *  <dt><strong>WARNING</strong></dt>
	 *  <dd>This collection is shared, do not change its contents!</dd>
	 * </dl>
	 */
	@SuppressWarnings( "PublicStaticCollectionField" )
	public static final List<TessellationPrimitive> QUAD_PRIMITIVES = Collections.singletonList( new QuadList( new int[] { 3, 2, 1, 0 } ) );

	/**
	 * {@link Tessellation} for a quad with outline.
	 * <pre>
	 *     3___2
	 *     |   |
	 *     |   |
	 *     |   |
	 *     |___|
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD = new Tessellation( Arrays.asList( new int[][] { { 0, 1, 2, 3, 0 } } ), QUAD_PRIMITIVES );

	/**
	 * {@link Tessellation} for a quad without an outline.
	 * <pre>
	 *     3...2
	 *     :   :
	 *     :   :
	 *     :   :
	 *     :...:
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD_NO_OUTLINE = new Tessellation( Collections.emptyList(), QUAD_PRIMITIVES );

	/**
	 * {@link Tessellation} for a quad with partial outline from vertex 0 to
	 * vertex 1.
	 * <pre>
	 *     3...2
	 *     :   :
	 *     :   :
	 *     :   :
	 *     :___:
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD_OUTLINE_01 = new Tessellation( Arrays.asList( new int[][] { { 0, 1 } } ), QUAD_PRIMITIVES );

	/**
	 * {@link Tessellation} for a quad with a partial outline along vertices
	 * 0, 1, and 2.
	 * <pre>
	 *     3...2
	 *     :   |
	 *     :   |
	 *     :   |
	 *     :___|
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD_OUTLINE_012 = new Tessellation( Arrays.asList( new int[][] { { 0, 1, 2 } } ), QUAD_PRIMITIVES );

	/**
	 * {@link Tessellation} for a quad with a partial outline along vertices
	 * 0, 1, 2, and 3.
	 * <pre>
	 *     3___2
	 *     :   |
	 *     :   |
	 *     :   |
	 *     :___|
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD_OUTLINE_0123 = new Tessellation( Arrays.asList( new int[][] { { 0, 1, 2, 3 } } ), QUAD_PRIMITIVES );

	/**
	 * {@link Tessellation} for a quad with partial outlines from vertex 0 to
	 * vertex 1 and from vertex 2 to vertex 3.
	 * <pre>
	 *     3___2
	 *     :   :
	 *     :   :
	 *     :   :
	 *     :___:
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD_OUTLINE_01_23 = new Tessellation( Arrays.asList( new int[][] { { 0, 1 }, { 2, 3 } } ), QUAD_PRIMITIVES );

	/**
	 * {@link Tessellation} for a quad with a partial outline from vertex 1 to
	 * vertex 2.
	 * <pre>
	 *     3...2
	 *     :   |
	 *     :   |
	 *     :   |
	 *     :...|
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD_OUTLINE_12 = new Tessellation( Arrays.asList( new int[][] { { 1, 2 } } ), QUAD_PRIMITIVES );

	/**
	 * {@link Tessellation} for a quad with a partial outline along vertices
	 * 1, 2, and 3.
	 * <pre>
	 *     3___2
	 *     :   |
	 *     :   |
	 *     :   |
	 *     :...|
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD_OUTLINE_123 = new Tessellation( Arrays.asList( new int[][] { { 1, 2, 3 } } ), QUAD_PRIMITIVES );

	/**
	 * {@link Tessellation} for a quad with partial outline from vertex 2 to
	 * vertex 3.
	 * <pre>
	 *     3___2
	 *     :   :
	 *     :   :
	 *     :   :
	 *     :...:
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD_OUTLINE_23 = new Tessellation( Arrays.asList( new int[][] { { 2, 3 } } ), QUAD_PRIMITIVES );

	/**
	 * {@link Tessellation} for a quad with a partial outline along vertices
	 * 2, 3, 0, and 1.
	 * <pre>
	 *     3___2
	 *     |   :
	 *     |   :
	 *     |   :
	 *     |___:
	 *     0   1
	 * </pre>
	 */
	public static final Tessellation QUAD_OUTLINE_2301 = new Tessellation( Arrays.asList( new int[][] { { 2, 3, 0, 1 } } ), QUAD_PRIMITIVES );

	/**
	 * Primitives that the tessellation consists of.
	 */
	@NotNull
	protected final List<TessellationPrimitive> _primitives;

	/**
	 * Outlines of tessellated shapes. Each entry in this list is an array with
	 * a list of indices in {@link Face3D#getVertices()} that forms a line
	 * strip.
	 */
	@NotNull
	private List<int[]> _outlines;

	/**
	 * Constructs a new tessellation.
	 * <dl>
	 *  <dt><strong>WARNING</strong></dt>
	 *  <dd>The supplied collections are used as-is for efficiency. Therefore,
	 *    changes to the collections are not isolated from this object!</dd>
	 * </dl>
	 *
	 * @param outlines   Outlines of tessellated shapes.
	 * @param primitives Primitives that define the tessellation.
	 */
	public Tessellation( @NotNull final List<int[]> outlines, @NotNull final List<TessellationPrimitive> primitives )
	{
		_outlines = outlines;
		_primitives = primitives;
	}

	@NotNull
	public List<TessellationPrimitive> getPrimitives()
	{
		return _primitives;
	}

	@NotNull
	public List<int[]> getOutlines()
	{
		return _outlines;
	}

	public void setOutlines( @NotNull final List<int[]> outlines )
	{
		_outlines = outlines;
	}

	@Override
	public int hashCode()
	{
		return _primitives.hashCode() * 31 + _outlines.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		boolean result = false;
		if ( obj == this )
		{
			result = true;
		}
		else if ( obj instanceof Tessellation )
		{
			final Tessellation other = (Tessellation)obj;
			result = _primitives.equals( other._primitives ) &&
			         _outlines.size() == other._outlines.size();
			if ( result )
			{
				for ( int i = 0; i < _outlines.size(); i++ )
				{
					if ( !Arrays.equals( _outlines.get( i ), other._outlines.get( i ) ) )
					{
						result = false;
						break;
					}
				}
			}
		}
		return result;
	}
}
