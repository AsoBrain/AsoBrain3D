/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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
package ab.j3d.model;

import java.util.Collection;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * Represents the result of a triangulation operation.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface Triangulation
{
	/**
	 * Returns the triangles that make up the triangulation.
	 *
	 * @return  Collection of triangles, each represented by an array
	 *          containing a vertex index triplet.
	 */
	Collection<int[]> getTriangles();

	/**
	 * Returns the vertices that are used in the triangulation result.
	 *
	 * @param   transform   Transformation to be applied to the vertices.
	 *
	 * @return  List of transformed vertices.
	 */
	List<Vector3D> getVertices( final Matrix3D transform );
}
