/*
 * $Id$
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

import com.numdata.oss.ui.*;
import org.jetbrains.annotations.*;

/**
 * Texture map loaded from a local file.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class FileTextureMap
	extends AbstractTextureMap
{
	/**
	 * File containing the texture image.
	 */
	@NotNull
	private final File _file;

	/**
	 * Constructs a texture map from an image read from the given file.
	 *
	 * @param   file    File containing the texture image.
	 */
	public FileTextureMap( @NotNull final File file )
	{
		_file = file;
	}

	@Override
	public BufferedImage getImage( final boolean useCache )
	{
		final String path = _file.getPath();
		return useCache ? ImageTools.getImage( path ) : ImageTools.load( path );
	}

	@Override
	public boolean equals( final Object object )
	{
		final boolean result;
		if ( object == this )
		{
			result = true;
		}
		else if ( object instanceof FileTextureMap )
		{
			final FileTextureMap map = (FileTextureMap)object;
			result = super.equals( object ) &&
			         _file.equals( map._file );
		}
		else
		{
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ _file.hashCode();
	}
}
