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

import org.jetbrains.annotations.*;

/**
 * A material library wrapper that prevents modification of the underlying
 * library.
 *
 * <p>
 * In this implementation, the <code>Material</code> objects themselves are not
 * copied or wrapped. As such, any changes to these objects will affect the
 * contents of the library.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ReadOnlyMaterialLibrary
	implements MaterialLibrary
{
	/**
	 * Wrapped material library.
	 */
	private MaterialLibrary _source;

	/**
	 * Construct new read-only library around the given material library.
	 *
	 * @param   source  Library to be wrapped.
	 */
	public ReadOnlyMaterialLibrary( @NotNull final MaterialLibrary source )
	{
		_source = source;
	}

	@Override
	public Material getMaterialByCode( final String code )
		throws IOException
	{
		return _source.getMaterialByCode( code );
	}

	@Override
	public List<Material> getMaterials()
		throws IOException
	{
		return _source.getMaterials();
	}

	@Override
	public void storeMaterial( final Material material )
		throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
