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

/**
 * Primitive that can be used in a {@link Tessellation}.
 *
 * @author  Peter S. Heijnen
 * @interface
 */
export default interface TessellationPrimitive {
	/**
	 * Get vertices that define the primitive.
	 *
	 * @return  Vertices that define the primitive.
	 */
	getVertices(): number[];

	/**
	 * Get triangles defined by this primitive.
	 *
	 * @return Triangles defined by this primitive.
	 */
	getTriangles(): number[];
}
