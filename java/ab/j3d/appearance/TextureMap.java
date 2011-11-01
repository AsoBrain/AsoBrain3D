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
package ab.j3d.appearance;

import java.awt.image.*;
import java.io.*;
import java.net.*;

import org.jetbrains.annotations.*;

/**
 * This interface defines a texture map.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public interface TextureMap
{
	/**
	 * Get URL for image file.
	 *
	 * Note that if this returns <code>null</code>, there may still be an image,
	 * but it may not be file-based or its URL could not be constructed.
	 *
	 * @return  {@link URL} for image file;
	 *          <code>null</code> if image file is not available.
	 */
	@Nullable
	URL getImageUrl();

	/**
	 * Load image.
	 *
	 * @return  {@link BufferedImage};
	 *          <code>null</code> if image is not available.
	 *
	 * @throws  IOException if an I/O error occurs while reading the image.
	 */
	@Nullable
	BufferedImage loadImage()
		throws IOException;

	/**
	 * Get physical width of map in meters. If available, this can be used to
	 * correctly scale the map in a virtual environment.
	 *
	 * @return  Physical width of map in meters;
	 *          <code>0.0</code> if indeterminate
	 */
	float getPhysicalWidth();

	/**
	 * Get physical height of map in meters. If available, this can be used to
	 * correctly scale the map in a virtual environment.
	 *
	 * @return  Physical height of map in meters;
	 *          <code>0.0</code> if indeterminate
	 */
	float getPhysicalHeight();
}
