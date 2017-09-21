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
package ab.j3d.loader;

import java.net.*;

import org.jetbrains.annotations.*;

/**
 * Converts URLs to strings with {@link URL#toExternalForm()} and removes a
 * specific prefix (if found).
 */
public class RemovePrefixConverter
implements TextureNameConverter
{
	/**
	 * Prefix to remove.
	 */
	private final String _removePrefix;

	/**
	 * Constructs a new instance.
	 *
	 * @param removePrefix Prefix to remove.
	 */
	public RemovePrefixConverter( @NotNull final String removePrefix )
	{
		_removePrefix = removePrefix;
	}

	@NotNull
	public String convert( @NotNull final String name )
	{
		String result = name;
		final String removeUrlPrefix = _removePrefix;
		if ( result.startsWith( removeUrlPrefix ) )
		{
			result = result.substring( removeUrlPrefix.length() );
		}
		return result;
	}
}
