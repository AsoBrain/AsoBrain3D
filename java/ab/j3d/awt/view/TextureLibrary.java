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
package ab.j3d.awt.view;

import java.awt.image.*;
import java.io.*;
import java.net.*;

import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * Library providing texture images.
 *
 * @author Gerrit Meinders
 */
public interface TextureLibrary
{
	/**
	 * Loads the image for the given texture map.
	 *
	 * @param textureMap Texture map to load.
	 *
	 * @return Texture map image.
	 *
	 * @throws IOException if an I/O error occurs while reading the image.
	 */
	@Nullable
	BufferedImage loadImage( @NotNull TextureMap textureMap )
	throws IOException;

	/**
	 * Opens the image for the given texture map as a stream.
	 *
	 * @param textureMap Texture map to load.
	 *
	 * @return Texture map image stream.
	 *
	 * @throws IOException if an I/O error occurs while reading the image.
	 */
	@Nullable
	InputStream openImageStream( @NotNull TextureMap textureMap )
	throws IOException;

	/**
	 * Returns a local file containing the image for the given texture map, if
	 * available.
	 *
	 * @param textureMap Texture map.
	 *
	 * @return Texture image file, if available.
	 */
	@Nullable
	File getFile( @NotNull TextureMap textureMap );

	/**
	 * Returns a URL of the image for the given texture map, if available.
	 *
	 * @param textureMap Texture map.
	 *
	 * @return Texture image URL, if available.
	 */
	@Nullable
	URL getUrl( @NotNull TextureMap textureMap );
}
