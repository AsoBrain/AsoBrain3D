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

/**
 * This interface is used to retrieve resources from another resource, e.g. a
 * zip archive.
 *
 * @author Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public interface ResourceLoader
{
	/**
	 * Loads a resource from the resource the loader was constructed with.
	 *
	 * @param   name Name of the resource to be loaded.
	 *
	 * @return  {@link InputStream} containing the resource.
	 *
	 * @throws  IOException if resource could not be loaded.
	 */
	InputStream getResource( String name )
		throws IOException;
}
