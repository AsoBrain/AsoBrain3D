/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2018 Peter S. Heijnen
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
	 * Primitives that the tessellation consists of.
	 */
	@NotNull
	protected final List<TessellationPrimitive> _primitives;

	/**
	 * Outlines of tessellated shapes.
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
	 * @param   outlines    Outlines of tessellated shapes.
	 * @param   primitives  Primitives that define the tessellation.
	 */
	public Tessellation( @NotNull final List<int[]> outlines, @NotNull final List<TessellationPrimitive> primitives )
	{
		_outlines = outlines;
		_primitives = primitives;
	}

	/**
	 * Returns the primitives that make up the tessellation.
	 *
	 * @return  Collection of primitives.
	 */
	@NotNull
	public List<TessellationPrimitive> getPrimitives()
	{
		return _primitives;
	}

	/**
	 * Get outlines of tessellated shapes.
	 *
	 * @return  Outlines of tessellated shapes.
	 */
	@NotNull
	public List<int[]> getOutlines()
	{
		return _outlines;
	}

	/**
	 * Sets the outlines of the tessellated shapes. Each array is a list of
	 * indices in {@link Face3D#getVertices()}, forming a line strip.
	 *
	 * @param   outlines    Outlines to be set.
	 */
	public void setOutlines( @NotNull final List<int[]> outlines )
	{
		_outlines = outlines;
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
