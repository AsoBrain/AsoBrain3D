/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.control;

import ab.j3d.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Represents a side of a box control based on a content node.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class ContentNodeBoxSide
	extends BoxSide
{
	/**
	 * Content node to be controlled.
	 */
	@NotNull
	private final ContentNode _node;

	/**
	 * Precedence of this side.
	 */
	private int _precedence = 0;

	/**
	 * Constructs a new instance.
	 *
	 * @param   node            Content node to be controlled.
	 * @param   side            Normal on this side of the box.
	 * @param   insideOfBox     <code>true</code> for the inside of the box;
	 *                          <code>false</code> for the outside of the box.
	 */
	public ContentNodeBoxSide( @NotNull final ContentNode node, @NotNull final Side side, final boolean insideOfBox )
	{
		super( side, insideOfBox );
		_node = node;
	}

	@Override
	protected Matrix3D getTransform()
	{
		return _node.getTransform();
	}

	@Override
	protected Bounds3D getBounds()
	{
		return _node.getBounds();
	}

	public int getPrecedence()
	{
		return _precedence;
	}

	/**
	 * Sets the precedence of this side.
	 *
	 * @param   precedence  Precedence to be set.
	 */
	public void setPrecedence( final int precedence )
	{
		_precedence = precedence;
	}
}
