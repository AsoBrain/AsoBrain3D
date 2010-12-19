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
 * Represents a tessellation.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
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
	private final List<int[]> _outlines;

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
	 * Returns the primitices that make up the tessellation.
	 *
	 * @return  Collection of primitives.
	 */
	@NotNull
	public Collection<TessellationPrimitive> getPrimitives()
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
}
