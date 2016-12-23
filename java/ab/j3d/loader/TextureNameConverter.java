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

import org.jetbrains.annotations.*;

/**
 * Performs an implementation-specific conversion of a texture name. This is
 * typically needed if the target format or container doesn't support certain
 * characters.
 *
 * @author Gerrit Meinders
 */
public interface TextureNameConverter
{
	/**
	 * Converts the given URL to a string.
	 *
	 * @param url URL to convert.
	 *
	 * @return String representation of the URL.
	 */
	@NotNull
	String convert( @NotNull String url );
}
