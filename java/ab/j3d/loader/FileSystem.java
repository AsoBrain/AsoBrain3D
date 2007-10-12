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

import com.numdata.oss.ui.explorer.Explorer;
import com.numdata.oss.ui.explorer.Item;

/**
 * This interface is used to crea items from a filesystem for usage in an {@link Explorer} instance.
 * For example, to retrieve the contents of a database or a directory.
 *
 * @author Wijnand Wieskamp
 */
public interface FileSystem
{
	/**
	 * Returns an Item[]
	 *
	 * @return  Array of Items created from the filesystem.
	 */
	Item[] getItems();

	/**
	 * Returns the binary data linked to the {@link Item}. For example, if the Item is an OBJ file, it will return the OBJ file as binary data.
	 *
	 * @param   item Item to get binary data for.
	 *
	 * @return  Bytearray containing the binary data linked to the Item.
	 */
	byte[] getItemData( Item item );
}

