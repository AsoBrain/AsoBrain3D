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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;

import com.numdata.db.DbServices;
import com.numdata.oss.TextTools;
import com.numdata.oss.ui.explorer.Item;

/**
 * Creates an ArrayList of Items from a database table. Table name is defined in {@link ModelFile}.
 *
 * @author Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public class DatabaseFileSystem
implements FileSystem
{

	/**
	 * DbServices for database access
	 */
	private DbServices _db = new DbServices();

	/**
	 * Arraylist containing the items generated from database records.
	 */
	private ArrayList<Item> _items;

	/**
	 * Hashmap linking item name to its ID from database. (Used by querying HQ
	 * model data)
	 */
	private Map<String, Integer> _idItem = new HashMap<String, Integer>();

	/**
	 * Map containing the LQ model data, so it doesn't have to be queried for a
	 * second time.
	 */
	private Map<String, ModelFile> _models = new HashMap<String, ModelFile>();

	/**
	 * Constructs a new databasefilesystem.
	 *
	 * @throws IOException when no connection to database could be made or low-res modelfile could not be loaded.
	 */
	public DatabaseFileSystem()
	throws IOException
	{

		final List<ModelFile> dbResult = _db.retrieveList( "thumbnail, id, lo", ModelFile.class, null );
		_items = new ArrayList<Item>();
		for ( final ModelFile aDbResult : dbResult )
		{
			final ZipInputStream zipInputStream = new ZipInputStream( new ByteArrayInputStream( aDbResult.lo ) );
			String name = null;
			ZipEntry zipEntry;
			while ( !TextTools.isNonEmpty( name ) && ( zipEntry = zipInputStream
			.getNextEntry() ) != null )
			{
				final String entryName = zipEntry.getName();
				if ( entryName.endsWith( ".obj" ) )
				{
					name = entryName;
					_idItem.put( name, Integer.valueOf( aDbResult.ID ) );
					_models.put( name, aDbResult );
				}
			}
			if ( TextTools.isNonEmpty( name ) )
			{
				final BufferedImage thumb;
				if ( aDbResult.thumbnail != null && aDbResult.thumbnail.length > 0 )
				{
					final ByteArrayInputStream bis = new ByteArrayInputStream( aDbResult.thumbnail );
					thumb = ImageIO.read( bis );
					_items.add( new Item( Item.FILE, name, "Description(Change in DataBaseFileSystem line 33!", thumb ) );
				}
				else
				{
					System.out
					.println( "aDbResult.thumbnail.length =< 0. No thumbnail loaded. " );
				}

			}
		}

	}

	public ArrayList<Item> getItems()
	{
		final ArrayList<Item> result = (ArrayList<Item>)_items.clone();
		return result;
	}

	public InputStream getItemData( final Item item, final boolean highQuality )
	throws IOException
	{
		final int id = _idItem.get( item.getName() );
		final ModelFile modelFile;
		InputStream result = null;
		if ( !highQuality )
		{
			//Look for low quality model data in hashmap (should be always there)
			if ( ( modelFile = _models.get( item.getName() ) ) != null )
			{
				if ( modelFile.lo.length > 0 )
				{
					result = new ByteArrayInputStream( modelFile.lo );
				}
			}
			else
			{
				// It's not there for some magic reason.
				throw new IOException( "Found more then one record!" );
			}
		}
		else
		{
			final List<ModelFile> dbResult = _db.retrieveList(ModelFile.HIGH_MODEL, ModelFile.class, "ID = '" + id + "' ");
			if ( dbResult.size() == 1 )
			{
				final byte[] b  = dbResult.get( 0 ).hi;
				result = new ByteArrayInputStream( b );
			}
			else if ( dbResult.size() > 1)
			{
				//Should never happen.
				throw new IOException( "Found more then one record for ID = " + _idItem.get( item.getName() ) + " --> Database broken?");
			}
			else
			{
				//Should also never happen.
				throw new IOException( "Found no record for ID = " + id + " --> Database broken?");
			}
		}
		return result;
	}
}
