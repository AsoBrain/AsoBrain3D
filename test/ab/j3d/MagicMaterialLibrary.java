/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2010 Peter S. Heijnen
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

import org.jetbrains.annotations.*;

/**
 * Material library that automagically creates materials that do not exist yet
 * when they are requested.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class MagicMaterialLibrary
	extends FallbackMemoryMaterialLibrary
{
	/**
	 * Constructs a new memory material library with the given fallback.
	 *
	 * @param   fallback    Material library used as a fallback.
	 */
	public MagicMaterialLibrary( final MaterialLibrary fallback )
	{
		super( fallback );
	}

	@NotNull
	@Override
	public Material getMaterialByCode( @NotNull final String code )
		throws IOException
	{
		Material result = super.getMaterialByCode( code );
		if ( result == null )
		{
			result = new Material( 0x80123456 );
			result.code = code;
			storeMaterial( result );
		}
		return result;
	}
}