/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
package ab.j3d.appearance;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Abstract implementation of the {@link TextureMap} interface.
 *
 * @author  Peter S. Heijnen
 */
public abstract class AbstractTextureMap
	implements TextureMap
{
	/**
	 * Name of the texture.
	 */
	private String _name = null;

	/**
	 * Physical width of map in meters (<code>0.0</code>0 if indeterminate).
	 */
	private float _physicalWidth;

	/**
	 * Physical height of map in meters (<code>0.0</code>0 if indeterminate).
	 */
	private float _physicalHeight;

	/**
	 * Construct map with indeterminate physical dimensions.
	 */
	protected AbstractTextureMap()
	{
		_name = null;
		_physicalWidth = 0.0f;
		_physicalHeight = 0.0f;
	}

	/**
	 * Construct map.
	 *
	 * @param name           Name of the texture.
	 * @param physicalWidth  Physical width in meters (<code>0.0</code> if unknown).
	 * @param physicalHeight Physical height in meters (<code>0.0</code> if unknown).
	 */
	protected AbstractTextureMap( final String name, final float physicalWidth, final float physicalHeight )
	{
		_name = name;
		_physicalWidth = physicalWidth;
		_physicalHeight = physicalHeight;
	}

	@Override
	@NotNull
	public String getName()
	{
		return _name;
	}

	/**
	 * Sets the name of the texture.
	 *
	 * @param name Texture name.
	 */
	public void setName( @NotNull final String name )
	{
		_name = name;
	}

	@Override
	public float getPhysicalWidth()
	{
		return _physicalWidth;
	}

	/**
	 * Set physical width of map in meters. If available, this can be used to
	 * correctly scale the map in a virtual environment.
	 *
	 * @param   physicalWidth   Physical width of map in meters;
	 *                          <code>0.0</code> if indeterminate.
	 */
	public void setPhysicalWidth( final float physicalWidth )
	{
		_physicalWidth = physicalWidth;
	}

	@Override
	public float getPhysicalHeight()
	{
		return _physicalHeight;
	}

	/**
	 * Set physical height of map in meters. If available, this can be used to
	 * correctly scale the map in a virtual environment.
	 *
	 * @param   physicalHeight  Physical height of map in meters;
	 *                          <code>0.0</code> if indeterminate.
	 */
	public void setPhysicalHeight( final float physicalHeight )
	{
		_physicalHeight = physicalHeight;
	}

	@Override
	public boolean equals( final Object other )
	{
		final boolean result;
		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof TextureMap )
		{
			final TextureMap map = (TextureMap)other;
			result = MathTools.equals( _name, map.getName() ) &&
			         ( _physicalWidth == map.getPhysicalWidth() ) &&
			         ( _physicalHeight == map.getPhysicalHeight() );
		}
		else
		{
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return _name.hashCode() ^
		       Float.floatToRawIntBits( _physicalWidth ) ^
		       Float.floatToRawIntBits( _physicalHeight );
	}
}
