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
 * Texture library that doesn't provide any textures, but does log all requests
 * it receives to {@link System#out}.
 *
 * @author Gerrit Meinders
 */
public class DebugTextureLibrary
implements TextureLibrary
{
	@Nullable
	@Override
	public BufferedImage loadImage( @NotNull final TextureMap textureMap )
	throws IOException
	{
		System.out.println( "DebugTextureLibrary.loadImage( " + textureMap.getName() + " )" );
		return null;
	}

	@Nullable
	@Override
	public InputStream openImageStream( @NotNull final TextureMap textureMap )
	throws IOException
	{
		System.out.println( "DebugTextureLibrary.openImageStream( " + textureMap.getName() + " )" );
		return null;
	}

	@Nullable
	@Override
	public File getFile( @NotNull final TextureMap textureMap )
	{
		System.out.println( "DebugTextureLibrary.getFile( " + textureMap.getName() + " )" );
		return null;
	}

	@Nullable
	@Override
	public URL getUrl( @NotNull final TextureMap textureMap )
	{
		System.out.println( "DebugTextureLibrary.getUrl( " + textureMap.getName() + " )" );
		return null;
	}
}
