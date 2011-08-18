/* $Id$
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
package ab.j3d.model;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * This {@link Node3DVisitor} calculates the combined bounds of 3D objects it
 * visits.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Bounds3DBuilderVisitor
	implements Node3DVisitor
{
	/**
	 * Builder of 3D bounds.
	 */
	private Bounds3DBuilder _builder;

	/**
	 * Construct visitor with default builders.
	 */
	public Bounds3DBuilderVisitor()
	{
		this( new Bounds3DBuilder() );
	}

	/**
	 * Create visitor for the given builder.
	 *
	 * @param   builder     Builder of 3D bounds.
	 */
	public Bounds3DBuilderVisitor( final Bounds3DBuilder builder )
	{
		_builder = builder;
	}

	/**
	 * Get bounding box from builder.
	 *
	 * @return  Bounding box as {@link Bounds3D} instance;
	 *          <code>null</code> if no bounding box could be determined.
	 *
	 * @see     Bounds3DBuilder#getBounds
	 */
	@Nullable
	public Bounds3D getBounds()
	{
		final Bounds3DBuilder builder = getBuilder();
		return builder.getBounds();
	}

	/**
	 * Get {@link Bounds3DBuilder} used by this visitor.
	 *
	 * @return  {@link Bounds3DBuilder}.
	 */
	public Bounds3DBuilder getBuilder()
	{
		return _builder;
	}

	/**
	 * Set {@link Bounds3DBuilder} to use by this visitor.
	 *
	 * @param   builder     Builder of 3D bounds.
	 */
	public void setBuilder( final Bounds3DBuilder builder )
	{
		_builder = builder;
	}

	@Override
	public boolean visitNode( @NotNull final Node3DPath path )
	{
		final Node3D node = path.getNode();
		if ( node instanceof Object3D )
		{
			final Object3D object3d = (Object3D) node;
			object3d.addBounds( getBuilder(), path.getTransform() );
		}
		return true;
	}
}
