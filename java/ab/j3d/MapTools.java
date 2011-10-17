/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import java.awt.image.*;

import com.numdata.oss.*;
import com.numdata.oss.ui.*;

/**
 * This class defines a map to be using in a 3D environment.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class MapTools
{
	/**
	 * Map path prefix from where material map images are loaded.
	 */
	public static String imageMapDirectory = "";

	/**
	 * Map path suffix from where material map images are loaded.
	 */
	public static String imageMapFilenameSuffix = ".jpg";

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private MapTools()
	{
	}

	/**
	 * Get {@link BufferedImage} instance with map image.
	 * Image will be stored in a cache. Please do
	 * not use this method for loading images that are rarely used (use the
	 * {@link #getImage(String)} for that purpose).
	 *
	 * @param   map     Name of map (<code>null</code> or empty strings allowed).
	 *
	 * @return  Map image;
	 *          <code>null</code> if map has no image or the image could not be loaded.
 	 */
	public static BufferedImage getImage( final String map )
	{
		return ImageTools.getImage( getPath( map ) );
	}

	/**
	 * Get {@link BufferedImage} instance with map image.
	 * Image wil NOT be cached, use the {@link #getImage(String)} method for that purpose.
	 *
	 * @param   map     Name of map (<code>null</code> or empty strings allowed).
	 *
	 * @return  Map image;
	 *          <code>null</code> if map has no image or the image could not be loaded.
	 */
	public static BufferedImage loadImage( final String map )
	{
		return ImageTools.load( getPath( map ) );
	}

	/**
	 * Get path to map image.
	 *
	 * @param   map     Name of map (<code>null</code> or empty strings allowed).
	 *
	 * @return  Map image;
	 *          <code>null</code> if map has no image or the image could not be loaded.
 	 */
	public static String getPath( final String map )
	{
		final String result;
		if ( TextTools.isNonEmpty( map ) )
		{
			String suffix = imageMapFilenameSuffix;
			if ( map.endsWith( ".png" ) )
			{
				suffix = "";
			}
			result = TextTools.isNonEmpty( imageMapDirectory ) ? imageMapDirectory + '/' + map + suffix : map + suffix;
		}
		else
		{
			result = null;
		}
		return result;
	}
}
