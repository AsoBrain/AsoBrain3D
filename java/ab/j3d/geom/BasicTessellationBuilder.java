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
import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Basic {@link TessellationBuilder} implementation.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class BasicTessellationBuilder
	extends AbstractTessellationBuilder
	implements Tessellation
{
	/**
	 * Transform that is applied to points.
	 */
	@Nullable
	protected Matrix3D _transform;

	/**
	 * Vertex coordinates used in the tesselation.
	 */
	@NotNull
	protected final HashList<Vector3D> _vertices;

	/**
	 * Constructs a new tesselation.
	 */
	public BasicTessellationBuilder()
	{
		this( null );
	}

	/**
	 * Constructs a new tesselation.
	 *
	 * @param   transform   Optional transform to apply to points.
	 */
	public BasicTessellationBuilder( @Nullable final Matrix3D transform )
	{
		_transform = transform;
		_vertices = new HashList<Vector3D>();
	}

	/**
	 * Get transform that is applied to points.
	 *
	 * @return  Transform that is applied to points;
	 *          <code>null</code> if no transform is applied.
	 */
	@Nullable
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set transform to apply to points.
	 *
	 * @param   transform   Transform to apply to points;
	 *                      <code>null</code> to not apply a transform.
	 */
	public void setTransform( @Nullable final Matrix3D transform )
	{
		_transform = transform;
	}

	@Override
	public int addVertex( final double x, final double y, final double z )
	{
		final Matrix3D transform = _transform;
		return _vertices.indexOfOrAdd( ( transform != null ) ? transform.transform( x, y, z ) : Vector3D.INIT.set( x, y, z ) );
	}

	@NotNull
	@Override
	public Tessellation getTessellation()
	{
		return this;
	}

	/**
	 * Get vertices that were added.
	 *
	 * @return  Vertices that were added.
	 */
	@Override
	@NotNull
	public List<Vector3D> getVertices()
	{
		return Collections.unmodifiableList( _vertices );
	}
}