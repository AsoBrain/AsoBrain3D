/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2007 Peter S. Heijnen
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

import java.awt.image.BufferedImage;

import com.numdata.oss.TextTools;
import com.numdata.oss.ui.ImageTools;

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
	private static String imageMapDirectory = "maps";

	/**
	 * Map path suffix from where material map images are loaded.
	 */
	private static String imageMapFilenameSuffix = ".jpg";


	/**
	 * Default constructor
	 */
	private MapTools()
	{
	}

	/**
	 * Get <code>Image</code> instance with map image.
	 * @param   map Image map
	 * @return  Map image;
	 *          <code>null</code> if map has no image or the image could not be loaded.
 	 */
	public static BufferedImage getImage( final String map )
	{
		return TextTools.isNonEmpty( map ) ? ImageTools.getImage( imageMapDirectory + '/' + map + imageMapFilenameSuffix ) : null;
	}

	/**
	 * Get <code>Image</code> instance with map image.
	 * @param map Image map
	 * @return Map image;
	 *          <code>null</code> if map has no image or the image could not be loaded.
	 */
	public static BufferedImage loadImage( final String map )
	{
		return TextTools.isNonEmpty( map ) ? ImageTools.load( imageMapDirectory + '/' + map + imageMapFilenameSuffix ) : null;
	}


	/**
	 * Returns the map path prefix from where material map images are loaded.
	 * @return Map path suffix from where material map images are loaded.
	 */
	public static String getImageMapDirectory()
	{
		return imageMapDirectory;
	}

	/**
	 * Sets the <code>Image</code> path prefix from where material map images are loaded.
	 * @param mapDirectory Path prefix from where material map images are loaded.
	 */
	public static void setImageMapDirectory( final String mapDirectory )
	{
		imageMapDirectory = mapDirectory;
	}

	/**
	 * Returns the map path suffix from where material map images are loaded.
	 * @return Map path suffix from where material map images are loaded.
	 */
	public static String getImageMapFilenameSuffix()
	{
		return imageMapFilenameSuffix;
	}


	/**
	 * Sets the <code>Image</code> path suffix from where material map images are loaded.
	 * @param mapFilenameSuffix Path suffix from where material map images are loaded.
	 */
	public static void setImageMapFilenameSuffix( final String mapFilenameSuffix )
	{
		imageMapFilenameSuffix = mapFilenameSuffix;
	}
}
