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
 * Abstract {@link TessellationBuilder} base implementation.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public abstract class AbstractTessellationBuilder
	implements TessellationBuilder
{
	/**
	 * Outlines of tessellated shapes.
	 */
	protected final List<int[]> _outlines;

	/**
	 * Primitives that the tessellation consists of.
	 */
	protected final List<TessellationPrimitive> _primitives;

	/**
	 * Constructs new tessellation builder.
	 */
	public AbstractTessellationBuilder()
	{
		_outlines = new ArrayList<int[]>();
		_primitives = new ArrayList<TessellationPrimitive>();
	}

	@Override
	public void addOutline( @NotNull final int[] outline )
	{
		_outlines.add( outline );
	}

	@Override
	public void addPrimitive( @NotNull final TessellationPrimitive primitive )
	{
		_primitives.add( primitive );
	}

	/**
	 * Get outlines of tessellated shaped.
	 *
	 * @return  Outlines of tessellated shapes.
	 */
	@NotNull
	public List<int[]> getOutlines()
	{
		return Collections.unmodifiableList( _outlines );
	}

	/**
	 * Get primitives that were added.
	 *
	 * @return  Primitives that were added.
	 */
	public List<TessellationPrimitive> getPrimitives()
	{
		return Collections.unmodifiableList( _primitives );
	}
}