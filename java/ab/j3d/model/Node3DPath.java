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
package ab.j3d.model;

import ab.j3d.*;

/**
 * Path build by {@link AbstractNode3DVisitor}. This is used to keep track of
 * the shortest path from the node the visitor started at and a visited node.
 * It also keeps track of a current transformation matrix.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Node3DPath
{
	/**
	 * Previous element in path, if any.
	 */
	final Node3DPath _previous;

	/**
	 * Combined transformation matrix at the visited node.
	 */
	final Matrix3D _transform;

	/**
	 * Node that is visited.
	 */
	final Node3D _node;

	/**
	 * Create transform stack element.
	 *
	 * @param   previous    Previous element in path, if any.
	 * @param   transform   Combined transformation matrix at the visited node.
	 * @param   node        Node that is visited.
	 */
	public Node3DPath( final Node3DPath previous, final Matrix3D transform, final Node3D node )
	{
		_previous = previous;
		_transform = transform;
		_node = node;
	}

	/**
	 * Get previous element in path, if any.
	 *
	 * @return  Previous element in path, if any.
	 */
	public Node3DPath getPrevious()
	{
		return _previous;
	}

	/**
	 * Get transformation matrix that was stored.
	 *
	 * @return  Transformation matrix that was stored.
	 */
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Get node that is visited.
	 *
	 * @return  Node that is visited.
	 */
	public Node3D getNode()
	{
		return _node;
	}
}
