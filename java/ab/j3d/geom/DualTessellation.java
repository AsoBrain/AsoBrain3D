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

import org.jetbrains.annotations.*;

/**
 * {@link TessellationBuilder} that does not build an tesselation by itself but
 * uses two underlying builders to build two tesselations at the same time. A
 * typical application would build tesselations in different coordinate systems.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class DualTessellation
	implements TessellationBuilder
{
	/**
	 * First tesselation to build.
	 */
	private final TessellationBuilder _first;

	/**
	 * Tessellation that is build together with this one.
	 */
	private final TessellationBuilder _second;

	/**
	 * Reverse vertex order of primitives in second tesselation.
	 */
	private final boolean _reverseSecondOrder;

	/**
	 * Maps vertices from this tesselation to the second tesselation.
	 */
	private final Map<Integer,Integer> _firstToSecondVertices;

	/**
	 * Construct tesselation.
	 *
	 * @param   first             First tesselation to build.
	 * @param   second           Second tesselation to build.
	 * @param   reverseSecond    Reverse vertex order of primitives in
	 *                              second tesselation.
	 */
	DualTessellation( final TessellationBuilder first, final TessellationBuilder second, final boolean reverseSecond )
	{
		_first = first;
		_second = second;
		_reverseSecondOrder = reverseSecond;
		_firstToSecondVertices = new HashMap<Integer,Integer>();
	}

	@Override
	public int addVertex( final double x, final double y, final double z )
	{
		final int vertex1 = _first.addVertex( x, y, z );

		final Integer key = Integer.valueOf( vertex1 );
		if ( !_firstToSecondVertices.containsKey( key ) )
		{
			final int vertex2 = _second.addVertex( x, y, z );
			_firstToSecondVertices.put( key, Integer.valueOf( vertex2 ) );
		}

		return vertex1;
	}

	@Override
	public void addOutline( @NotNull final int[] outline )
	{
		_first.addOutline( outline );
		_second.addOutline( createSecondOutline( outline, _firstToSecondVertices ) );
	}

	@Override
	public void addPrimitive( @NotNull final TessellationPrimitive primitive )
	{
		_first.addPrimitive( primitive );
		addPrimitiveCopy( _second, primitive, _firstToSecondVertices, _reverseSecondOrder );
	}

	@NotNull
	@Override
	public Tessellation getTessellation()
	{
		/* should call underlying builders instead */
		throw new AssertionError();
	}

	/**
	 * Create copy of an outline. Vertices in the result are mapped using the
	 * specified <code>vertexMap</code>.
	 *
	 * @param   outline     Outline to copy.
	 * @param   vertexMap   Maps vertex indices from the given outline to the
	 *                      resulting outline.
	 *
	 * @return  Outline.
	 */
	@NotNull
	protected static int[] createSecondOutline( @NotNull final int[] outline, @NotNull final Map<Integer, Integer> vertexMap )
	{
		final int length = outline.length;

		final int[] result = new int[ length ];

		for ( int i = 0; i < length; i++ )
		{
			result[ i ] = vertexMap.get( Integer.valueOf( outline[ i ] ) );
		}

		return result;
	}

	/**
	 * Adds a copy of a primitive to the given tessellation builder. Vertices
	 * in the result are mapped using the specified <code>vertexMap</code>, and,
	 * if desired, the vertices in the result can also have reversed
	 * clockwise/counter-clockwise ordering.
	 *
	 * @param   result                  Tessellation builder.
	 * @param   primitive               Primitive to copy.
	 * @param   vertexMap               Maps vertex indices from the specified
	 *                                  primitive to the resulting primitive.
	 * @param   reverseClockOrdering    Reverse clock ordering of result.
	 */
	protected static void addPrimitiveCopy( @NotNull final TessellationBuilder result, @NotNull final TessellationPrimitive primitive, @NotNull final Map<Integer, Integer> vertexMap, final boolean reverseClockOrdering )
	{
		final int[] vertices1 = primitive.getVertices();

		final int length = vertices1.length;

		if ( primitive instanceof TriangleFan )
		{
			final int[] vertices2 = new int[ length ];
			for ( int i = 0; i < length; i++ )
			{
				final int vertexIndex = reverseClockOrdering ? ( i == 0 ) ? 0 : ( length - i ) : i;
				vertices2[ i ] = vertexMap.get( Integer.valueOf( vertices1[ vertexIndex ] ) );
			}

			result.addPrimitive( new TriangleFan( vertices2 ) );
		}
		else if ( primitive instanceof TriangleList )
		{
			final int last = length - 1;

			final int[] vertices2 = new int[ length ];
			for ( int i = 0; i < length; i++ )
			{
				final int vertexIndex = reverseClockOrdering ? ( last - i ) : i;
				vertices2[ i ] = vertexMap.get( Integer.valueOf( vertices1[ vertexIndex ] ) );
			}

			result.addPrimitive( new TriangleList( vertices2 ) );
		}
		else if ( primitive instanceof TriangleStrip )
		{
			if ( reverseClockOrdering )
			{
				final int oddLength = ( length - 1 ) | 1;
				if ( oddLength >= 3 )
				{
					final int[] vertices2 = new int[ oddLength ];
					final int last = oddLength - 1;
					for ( int i = 0; i < oddLength; i++ )
					{
						vertices2[ i ] = vertexMap.get( Integer.valueOf( vertices1[ last - i ] ) );
					}
					result.addPrimitive( new TriangleStrip( vertices2 ) );
				}

				if ( oddLength != length )
				{
					final int[] vertices2 = new int[ 3 ];
					vertices2[ 0 ] = vertexMap.get( Integer.valueOf( vertices1[ length - 3 ] ) );
					vertices2[ 1 ] = vertexMap.get( Integer.valueOf( vertices1[ length - 2 ] ) );
					vertices2[ 2 ] = vertexMap.get( Integer.valueOf( vertices1[ length - 1 ] ) );
					result.addPrimitive( new TriangleList( vertices2 ) );
				}
			}
			else
			{
				final int[] vertices2 = new int[ length ];
				for ( int i = 0; i < length; i++ )
				{
					vertices2[ i ] = vertexMap.get( Integer.valueOf( vertices1[ i ] ) );
				}
				result.addPrimitive( new TriangleStrip( vertices2 ) );
			}
		}
		else
		{
			throw new RuntimeException( "Unrecognized primitive: " + primitive );
		}
	}
}
