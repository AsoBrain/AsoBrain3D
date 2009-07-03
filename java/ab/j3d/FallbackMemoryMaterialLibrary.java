/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2009 Peter S. Heijnen
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

/**
 * Memory-based material library that uses another material library as a
 * fallback.
 * <p>
 * Records are always stored in memory, but may be retrieved either from memory
 * or from the underlying material library. This provides the ability to
 * temporarily add records to a library (e.g. for testing purposes), while
 * leaving the underlying library intact.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class FallbackMemoryMaterialLibrary
	extends MemoryMaterialLibrary
{
	/**
	 * Underlying material library used as a fallback.
	 */
	private MaterialLibrary _fallback;

	/**
	 * Constructs a new memory material library with the given fallback.
	 *
	 * @param   fallback    Material library used as a fallback.
	 */
	public FallbackMemoryMaterialLibrary( final MaterialLibrary fallback )
	{
		_fallback = fallback;
	}

	public Material getMaterialByCode( final String code )
		throws IOException
	{
		Material result = super.getMaterialByCode( code );

		if ( result == null )
		{
			result = _fallback.getMaterialByCode( code );
		}

		return result;
	}

	public List<Material> getMaterials()
		throws IOException
	{
		final List<Material> fromSuper = super.getMaterials();
		final List<Material> fromFallback = _fallback.getMaterials();

		final List<Material> result = new ArrayList<Material>( fromSuper.size() + fromFallback.size() );
		result.addAll( fromSuper );
		result.addAll( fromFallback );
		return result;
	}
}