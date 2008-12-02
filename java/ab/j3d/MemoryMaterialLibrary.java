/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2008-2008 Peter S. Heijnen
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
package ab.j3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides a simple in-memory material library.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class MemoryMaterialLibrary
	implements MaterialLibrary
{
	/**
	 * The material database.
	 */
	private final Map<String,Material> _materials = new HashMap<String,Material>();

	/**
	 * Construct library.
	 */
	public MemoryMaterialLibrary()
	{
	}

	public Material getMaterialByCode( final String code )
	{
		if ( code == null )
			throw new NullPointerException( "code" );

		return _materials.get( code );
	}

	public List<Material> getMaterials()
	{
		return new ArrayList<Material>( _materials.values() );
	}

	public void storeMaterial( final Material material )
	{
		if ( material == null )
			throw new NullPointerException( "material" );

		final Map<String, Material> materials = _materials;

		if ( material.ID >= 0 )
		{
			for ( final Material existing : materials.values() )
			{
				if ( material.ID == existing.ID )
				{
					materials.remove( existing.code );
					break;
				}
			}
		}
		else
		{
			int maxID = -1;
			for ( final Material existing : materials.values() )
			{
				maxID = Math.max( maxID , existing.ID );
			}

			material.ID = maxID + 1;
		}

		materials.put( material.code , material );
	}
}
