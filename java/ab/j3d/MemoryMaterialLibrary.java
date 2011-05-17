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
package ab.j3d;

import java.io.*;
import java.util.*;

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

	@Override
	public Material getMaterialByCode( final String code )
		throws IOException
	{
		if ( code == null )
		{
			throw new IllegalArgumentException( "code" );
		}

		return _materials.get( code );
	}

	@Override
	public List<Material> getMaterials()
		throws IOException
	{
		return new ArrayList<Material>( _materials.values() );
	}

	@Override
	public void storeMaterial( final Material material )
		throws IOException
	{
		if ( material == null )
		{
			throw new IllegalArgumentException( "material" );
		}

		final Map<String,Material> materials = _materials;

		if ( material.ID >= 0 )
		{
			final Material materialWithSameCode = materials.get( material.code );

			if ( materialWithSameCode != null )
			{
				if ( materialWithSameCode.ID != material.ID )
				{
					throw new IOException( "Duplicate material code: " + material.code );
				}
				// Otherwise, the 'materialWithSameCode' will be replaced by the call to 'put' below.
			}
			else
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
		}
		else
		{
			int maxID = -1;
			for ( final Material existing : materials.values() )
			{
				if ( material.code.equalsIgnoreCase( existing.code ) )
				{
					throw new IOException( "Duplicate material code: " + material.code );
				}

				maxID = Math.max( maxID, existing.ID );
			}

			material.ID = maxID + 1;
		}

		materials.put( material.code, material );
	}
}
