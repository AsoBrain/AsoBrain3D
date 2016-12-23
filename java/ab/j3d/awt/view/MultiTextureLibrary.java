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
import java.util.*;

import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * Texture library that combines multiple texture libraries.
 *
 * @author Gerrit Meinders
 */
public class MultiTextureLibrary
	implements TextureLibrary
{
	/**
	 * Texture libraries.
	 */
	private final List<TextureLibrary> _textureLibraries = new ArrayList<TextureLibrary>();

	public void addTextureLibrary( @NotNull final TextureLibrary textureLibrary )
	{
		_textureLibraries.add( textureLibrary );
	}

	public void removeTextureLibrary( @NotNull final TextureLibrary textureLibrary )
	{
		_textureLibraries.add( textureLibrary );
	}

	public void clearTextureLibraries()
	{
		_textureLibraries.clear();
	}

	@Nullable
	@Override
	public BufferedImage loadImage( @NotNull final TextureMap textureMap )
	throws IOException
	{
		BufferedImage result = null;
		for ( final TextureLibrary textureLibrary : _textureLibraries )
		{
			result = textureLibrary.loadImage( textureMap );
			if ( result != null )
			{
				break;
			}
		}
		return result;
	}

	@Nullable
	@Override
	public InputStream openImageStream( @NotNull final TextureMap textureMap )
	throws IOException
	{
		InputStream result = null;
		for ( final TextureLibrary textureLibrary : _textureLibraries )
		{
			result = textureLibrary.openImageStream( textureMap );
			if ( result != null )
			{
				break;
			}
		}
		return result;
	}

	@Nullable
	@Override
	public File getFile( @NotNull final TextureMap textureMap )
	{
		File result = null;
		for ( final TextureLibrary textureLibrary : _textureLibraries )
		{
			result = textureLibrary.getFile( textureMap );
			if ( result != null )
			{
				break;
			}
		}
		return result;
	}

	@Nullable
	@Override
	public URL getUrl( @NotNull final TextureMap textureMap )
	{
		URL result = null;
		for ( final TextureLibrary textureLibrary : _textureLibraries )
		{
			result = textureLibrary.getUrl( textureMap );
			if ( result != null )
			{
				break;
			}
		}
		return result;
	}
}
