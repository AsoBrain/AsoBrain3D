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
import org.jetbrains.annotations.*;

/**
 * Path to a {@link Node3D}, keeping track of the path leading upto this node
 * and the combined transformation matrix at the node.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Node3DPath
{
	/**
	 * Parent path element. This is <code>null</code> for the 'root'.
	 */
	@Nullable
	private final Node3DPath _parent;

	/**
	 * Combined transformation matrix at the node.
	 */
	@NotNull
	private final Matrix3D _transform;

	/**
	 * {@link Node3D} to which this path leads.
	 */
	@NotNull
	private final Node3D _node;

	/**
	 * Create path element.
	 *
	 * @param   parent      Parent path element (<code>null</code> for root).
	 * @param   transform   Combined transformation matrix at node.
	 * @param   node        {@link Node3D} to which the path leads.
	 */
	public Node3DPath( @Nullable final Node3DPath parent, @NotNull final Matrix3D transform, @NotNull final Node3D node )
	{
		_parent = parent;
		_transform = transform;
		_node = node;
	}

	/**
	 * Get parent path element. This is <code>null</code> for the 'root'.
	 *
	 * @return  Parent path element;
	 *          <code>null</code> if this is the 'root'.
	 */
	@Nullable
	public Node3DPath getParent()
	{
		return _parent;
	}

	/**
	 * Get combined transformation matrix at the node.
	 *
	 * @return  Combined transformation matrix at the node.
	 */
	@NotNull
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Get {@link Node3D} to which this path leads.
	 *
	 * @return  {@link Node3D} to which this path leads.
	 */
	@NotNull
	public Node3D getNode()
	{
		return _node;
	}

	@Override
	public String toString()
	{
		final Class<?> clazz = getClass();
		return clazz.getSimpleName() + '@' + Integer.toHexString( hashCode() ) + "{node=" + _node + ", transform=" + _transform.toShortFriendlyString() + ", parent=" + _parent + '}';
	}
}
