/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2008 Peter S. Heijnen
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

import java.io.IOException;
import java.util.List;

/**
 * This interface is used to retrieve materials from a library.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface MaterialLibrary
{
	/**
	 * Get material with the specified code from the library.
	 * <dl>
	 *  <dt>IMPORTANT:</dt>
	 *  <dd>For efficiency, implementations of this method may return a shared
	 *   {@link Material} instance. Modification may corrupt the library.</dd>
	 * </dl>
	 *
	 * @param   code    Material code.
	 *
	 * @return  Material instance;
	 *          <code>null</code> if no matching material was found.
	 *
	 * @throws  NullPointerException if the argument is <code>null</code>.
	 * @throws  IOException if an error occured while accesing the library.
	 */
	Material getMaterialByCode( String code )
		throws IOException;

	/**
	 * Get all materials from the library.
	 * <dl>
	 *  <dt>IMPORTANT:</dt>
	 *  <dd>For efficiency, implementations of this method may return shared
	 *   {@link Material} instances. Modification may corrupt the library.</dd>
	 * </dl>
	 *
	 * @return  Materials.
	 *
	 * @throws  IOException if an error occured while accesing the library.
	 */
	List<Material> getMaterials()
		throws IOException;

	/**
	 * This method is used to store a material.
	 * <dl>
	 *  <dt>IMPORTANT:</dt>
	 *  <dd>For efficiency, implementations may store and share the
	 *   {@link Material} instance. Modification may corrupt the library.</dd>
	 * </dl>
	 *
	 * @param   material    Material to store.
	 *
	 * @throws  NullPointerException if the argument is <code>null</code>.
	 * @throws  IOException if an error occured while accesing the library.
	 */
	void storeMaterial( Material material )
		throws IOException;
}
