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

import org.jetbrains.annotations.*;

/**
 * Builder of a {@link Tessellation} directed by a {@link Tessellator}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface TessellationBuilder
{
	/**
	 * Adds the given vertex to the tessellation, returning its index.
	 *
	 * @param   x   X coordinate of vertex to be added.
	 * @param   y   Y coordinate of vertex to be added.
	 * @param   z   Z coordinate of vertex to be added.
	 *
	 * @return  Vertex index.
	 */
	int addVertex( double x, double y, double z );

	/**
	 * Adds the outline of a tessellated shape.
	 *
	 * @param   outline     Outline to add to the tessellation.
	 */
	void addOutline( @NotNull int[] outline );

	/**
	 * Adds the given primitive to the tessellation.
	 *
	 * @param   primitive   Primitive to add to the tessellation.
	 */
	void addPrimitive( @NotNull TessellationPrimitive primitive );

	/**
	 * Get tessellation that was build. Calling this method before completion
	 * of the building process renders unpredictable results.
	 *
	 * @return  {@link Tessellation} that was build.
	 */
	@NotNull
	Tessellation getTessellation();
}