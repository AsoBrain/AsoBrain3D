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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.numdata.oss.ui.explorer.Explorer;
import com.numdata.oss.ui.explorer.Item;

/**
 * This interface is used to create items from a filesystem for usage in an {@link Explorer} instance.
 * For example, retrieve the contents of a database or a directory.
 * Items are stored in an ArrayList at construction of the class.
 *
 * @author Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public interface FileSystem
{
	/**
	 * Returns ArrayList containing Items created from the filesystem.
	 *
	 * @return  ArrayList of Items created from the filesystem.
	 */
	ArrayList<Item> getItems();

	/**
	 * Returns an InputStream containing the data linked to the {@link Item}.
	 * For example, if the Item is an OBJ file, it will return the OBJ file as InputStream.
	 *
	 * @param   item Item to get binary data for.
	 *
	 * @return  InputStream containing the data linked to the Item.
	 *
	 * @throws  IOException when data could not be read.
	 */
	InputStream getItemData( Item item )
		throws IOException;
}

