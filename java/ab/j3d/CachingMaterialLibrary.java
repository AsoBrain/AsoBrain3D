/* $Id$
 * ====================================================================
 * Copyright (C) 2008-2008 Numdata BV
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.numdata.oss.Cache;
import com.numdata.oss.TextTools;


/**
 * This class wraps an {@link MaterialLibrary} with caching functionality.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class CachingMaterialLibrary
	implements MaterialLibrary
{
	/**
	 * Wrapped library.
	 */
	private final MaterialLibrary _library;

	/**
	 * Cache of materials by code.
	 */
	private final Map<String,Material> _cache;

	/**
	 * Total number of materials; <code>-1</code> if unknown.
	 */
	private int _materialCount = -1;

	/**
	 * Construct library.
	 *
	 * @param   library     Library to wrap.
	 */
	public CachingMaterialLibrary( final MaterialLibrary library )
	{
		_library = library;
		_cache = new Cache<String,Material>();
	}

	public final Material getMaterialByCode( final String code )
		throws IOException
	{
		Material result = null;

		if ( code == null )
			throw new NullPointerException( "code" );

		if ( TextTools.isNonEmpty( code ) )
		{
			final Map<String,Material> cache = _cache;

			result = cache.get( code );
			if ( ( result == null ) && !isCacheComplete() && !cache.containsKey( code ) )
			{
				result = _library.getMaterialByCode( code );
				cache.put( code , result );
			}
		}

		return result;
	}

	/**
	 * Returns whether the cache for this library contains all materials.
	 *
	 * @return  <code>true</code> if all materials are cached;
	 *          <code>false</code> otherwise.
	 */
	private boolean isCacheComplete()
	{
		return ( _materialCount >= 0 ) && ( _materialCount == _cache.size() );
	}

	public List<Material> getMaterials()
		throws IOException
	{
		List<Material> result = null;

		final Map<String, Material> cache = _cache;

		if ( isCacheComplete() )
		{
			result = new ArrayList<Material>( cache.size() );
			for ( final Material material : cache.values() )
			{
				if ( material != null )
				{
					result.add( material );
				}
			}
		}

		if ( ( result == null ) || !isCacheComplete() )
		{
			result = _library.getMaterials();

			cache.clear();

			for ( final Material material : result )
			{
				cache.put( material.code , material );
			}
		}

		return result;
	}

	public void storeMaterial( final Material material )
		throws IOException
	{
		if ( material == null )
			throw new NullPointerException( "material" );

		if ( material.code == null )
			throw new NullPointerException( "material.code" );

		_library.storeMaterial( material );
		_cache.put( material.code , material );
	}
}
