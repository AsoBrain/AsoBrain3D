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
 * This class defines a transformation node in the graphics tree..
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Transform3D
	extends Node3D
{
	/**
	 * Transformation matrix.
	 */
	@NotNull
	private Matrix3D _transform = Matrix3D.IDENTITY;

	/**
	 * Default constructor. Initializes node to an identity transform.
	 */
	public Transform3D()
	{
	}

	/**
	 * Construct node with specific initial transformation matrix.
	 *
	 * @param   transform   Transformation matrix to initialize node to.
	 */
	public Transform3D( @NotNull final Matrix3D transform )
	{
		setTransform( transform );
	}

	/**
	 * Construct node with specific initial transformation matrix.
	 *
	 * @param   transform   Transformation matrix to initialize node to.
	 * @param   children    Nodes to add as children to this node.
	 */
	public Transform3D( @NotNull final Matrix3D transform, final Node3D... children )
	{
		this( transform );
		addChildren( children );
	}

	/**
	 * Get transformation matrix.
	 *
	 * @return  Transformation matrix.
	 */
	@NotNull
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set transformation matrix.
	 *
	 * @param   transform   Transformation matrix.
	 */
	public void setTransform( @NotNull final Matrix3D transform )
	{
		_transform = transform;
	}

	@Override
	public String toString()
	{
		final Class<? extends Node3D> clazz = getClass();
		final Matrix3D transform = getTransform();
		return clazz.getSimpleName() + '@' + Integer.toHexString( hashCode() ) + "{tag=" + getTag() + ",transform=" + transform.toShortFriendlyString() + '}';
	}
}
