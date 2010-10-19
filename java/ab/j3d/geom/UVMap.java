/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2010 Peter S. Heijnen
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

import java.awt.geom.*;
import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Defines a mapping from spatial coordinates to texture coordinates.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface UVMap
{
	/**
	 * Generates texture coordinates for the given vertices.
	 *
	 * @param   material            Material used to define texture scale.
	 * @param   vertexCoordinates   Vertex coordinates.
	 * @param   vertexIndices       Indices for all vertices in the face.
	 * @param   flipTexture         Flip texture direction.
	 *
	 * @return  Texture coordinates for each vertex.
	 */
	float[] generate( @Nullable Material material , @NotNull List<? extends Vector3D> vertexCoordinates , @Nullable int[] vertexIndices , boolean flipTexture );

	/**
	 * Generate 2D point on texture for the given 3D point.
	 *
	 * @param   material            Material used to define texture scale.
	 * @param   point               Point.
	 * @param   normal              Normal of face to map texture on.
	 * @param   flipTexture         Flip texture direction.
	 *
	 * @return  Texture coordinates.
	 */
	Point2D.Float generate( @Nullable Material material , @NotNull Vector3D point , @NotNull Vector3D normal , boolean flipTexture );
}
