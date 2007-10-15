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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import com.numdata.oss.io.FileExtensionFilter;
import com.numdata.oss.ui.explorer.Item;

/**
 * Creates an ArrayList of Items from a specified directory.
 * It's possible to add an {@link FileExtensionFilter} to filter files.
 *
 *
 * @author  Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public class DiskFileSystem
	implements FileSystem
{

	/**
	 * ArrayList of items generated from the target directory in conjuction with the {@link FileExtensionFilter}.
	 */
	private ArrayList<Item> _items;

	/**
	 * The directory to read from.
	 */
	private String _targetDirectory;

	/**
	 * Constructor creates a DiskFileSystem containing files within the target directory.
	 *
	 * @param   targetDirectory Directory to read from.
	 *
	 * @throws  IOException when directory or file can't be read.
	 */
	public DiskFileSystem( final String targetDirectory )
		throws IOException
	{
		this( targetDirectory , null );
	}

	/**
	 * Constructor creates a DiskFileSystem containing files within the target directory, filtered by given {@link FileExtensionFilter}.
	 *
	 * @param   targetDirectory Directory to read from.
	 * @param   filter          {@link FileExtensionFilter} to apply on target directory.
	 *
	 * @throws  IOException when directory or file can't be read.
	 */
	public DiskFileSystem( final String targetDirectory , final FileExtensionFilter filter )
		throws IOException
	{
		_targetDirectory = targetDirectory;
		final File directory = new File( _targetDirectory );
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
			BufferedImage thumb;
			for ( final File file : filesInDirectory )
			{
				final File thumbnail = new File( file.getAbsolutePath() + "_thumbnail" );
				if ( thumbnail.exists() && thumbnail.canRead() )
				{
					thumb = ImageIO.read( thumbnail );
				}
				else
				{
					thumb = null;
				}
				final Item item = new Item( file.isDirectory() ? Item.DIRECTORY : Item.FILE , file.getName() , null , thumb );
				_items.add( item );
			}
		}
		else
		{
			throw new IOException( "Can't read from directory \"" + directory +"\"." );
		}
	}

	public ArrayList<Item> getItems()
	{
		final ArrayList<Item> result = (ArrayList<Item>)_items.clone();
		return result;
	}

	public InputStream getItemData( final Item item )
		throws FileNotFoundException
	{
		final FileInputStream result;
		final File file = new File( _targetDirectory + item.getName() );
		if ( file.canRead() && file.isFile() )
		{
			result = new FileInputStream( file );
		}
		else
		{
			result = null;
		}
		return result;
	}
}
