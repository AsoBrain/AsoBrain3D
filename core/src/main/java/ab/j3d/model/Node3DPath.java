/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
package ab.j3d.model;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Path to a {@link Node3D}, keeping track of the path leading upto this node
 * and the combined transformation matrix at the node.
 *
 * @author Peter S. Heijnen
 */
public class Node3DPath
{
	/**
	 * Parent path element. This is {@code null} for the 'root'.
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
	 * @param parent    Parent path element ({@code null} for root).
	 * @param transform Combined transformation matrix at node.
	 * @param node      {@link Node3D} to which the path leads.
	 */
	public Node3DPath( @Nullable final Node3DPath parent, @NotNull final Matrix3D transform, @NotNull final Node3D node )
	{
		_parent = parent;
		_transform = transform;
		_node = node;
	}

	/**
	 * Get parent path element. This is {@code null} for the 'root'.
	 *
	 * @return Parent path element; {@code null} if this is the 'root'.
	 */
	@Nullable
	public Node3DPath getParent()
	{
		return _parent;
	}

	/**
	 * Get combined transformation matrix at the node.
	 *
	 * @return Combined transformation matrix at the node.
	 */
	@NotNull
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Get {@link Node3D} to which this path leads.
	 *
	 * @return {@link Node3D} to which this path leads.
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

	/**
	 * Give string representation of path. This allows a separator to be specified
	 * that separates the parent from this instance.
	 *
	 * @param separator Path element separator.
	 *
	 * @return String representation of path.
	 */
	public String toString( final String separator )
	{
		final Class<?> clazz = getClass();
		final Node3DPath parent = _parent;
		return ( parent != null ? parent.toString( separator ) + separator : "" ) + clazz.getSimpleName() + '@' + Integer.toHexString( hashCode() ) + "{node=" + _node + ", transform=" + _transform.toShortFriendlyString() + '}';
	}
}
