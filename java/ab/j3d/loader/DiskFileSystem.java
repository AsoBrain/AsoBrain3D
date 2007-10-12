/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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
package ab.j3d.loader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;

import com.numdata.oss.io.FileExtensionFilter;
import com.numdata.oss.ui.explorer.Item;

/**
 * Creates an Item[] of specified files from a specified directory.
 *ArrayList<Item>
 * @author Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public class DiskFileSystem
	implements FileSystem{

	/**
	 * ArrayList of items generated from the targetdirectory in conjuction with the {@link FileExtensionFilter}.
	 */
	private ArrayList<Item> _items;

	/**
	 * Constructor creates a DiskFileSystem containing files within the targetdirectory.
	 *
	 * @param   targetDirectory Directory to read from.
	 *
	 * @throws  IOException     When directory or file can't be read.
	 */
	public DiskFileSystem( final String targetDirectory )
		throws IOException
	{
		this( targetDirectory, null );
	}

	/**
	 * Constructor creates a DiskFileSystem containing files within the targetdirectory, filtered by given {@link FileExtensionFilter}.
	 *
	 * @param   targetDirectory Directory to read from.
	 * @param   filter {@link FileExtensionFilter} to apply on targetdirectory
	 *
	 * @throws  IOException     When directory or file can't be read.
	 */
	public DiskFileSystem( final String targetDirectory , final FileExtensionFilter filter )
		throws IOException
	{
		final File directory = new File( targetDirectory );
		if ( directory.isDirectory() && directory.canRead() )
		{
			final File[] filesInDirectory;
			if ( filter == null )
			{
				filesInDirectory = directory.listFiles();
			}
			else
			{
				filesInDirectory = directory.listFiles( filter );
			}
			_items = new ArrayList<Item>();
			for ( final File file : filesInDirectory )
			{
				final File thumbnail = new File( file.getAbsolutePath() + "_thumbnail" );
				BufferedImage thumb = null;
				if ( thumbnail.exists() && thumbnail.canRead() )
				{
					thumb = ImageIO.read( thumbnail );
				}
				final Item item = new Item(
				file.isDirectory() ? Item.DIRECTORY : Item.FILE , file.getName() , null , thumb );
				_items.add( item );
			}
		}
		else
		{
			throw new IOException( "Can't read from directory \"" + directory +"\"" );
		}
	}

	public ArrayList<Item> getItems()
	{
		return (ArrayList<Item>)Collections.unmodifiableList( _items );
	}

	public byte[] getItemData( final Item item )
	{
		return null;
	}
}
