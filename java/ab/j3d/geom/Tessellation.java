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
import org.jetbrains.annotations.*;

/**
 * Represents a tessellation that may be build by a {@link TessellationBuilder}
 * directed by a {@link Tessellator}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface Tessellation
{
	/**
	 * Returns the vertices that are used in the tessellation.
	 *
	 * @return  List of vertices.
	 */
	@NotNull
	List<? extends Vector3D> getVertices();

	/**
	 * Get outlines of tessellated shapes.
	 *
	 * @return  Outlines of tessellated shapes.
	 */
	@NotNull
	List<int[]> getOutlines();

	/**
	 * Returns the primitices that make up the tessellation.
	 *
	 * @return  Collection of primitives.
	 */
	Collection<TessellationPrimitive> getPrimitives();
}
