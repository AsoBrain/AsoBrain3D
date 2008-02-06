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

/**
 * This interface is used to retrieve materials from a library. A typical
 * implementation of this interface would generate the material data or retrieve
 * it from some data source (a database for example).
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface MaterialLibrary
{
	/**
	 * Get all available material codes from the library.
	 *
	 * @return  List of material codes;
	 *          <code>null</code> if an error occured.
	 */
	String[] getMaterialCodes();

	/**
	 * Get material from the library for the specified code.
	 * <dl>
	 *  <dt>IMPORTANT:</dt>
	 *  <dd>For efficiency, implementations of this method may return a shared
	 *   {@link Material} instance. Modification may corrupt the library.</dd>
	 * </dl>
	 *
	 * @param   code    Material code.
	 *
	 * @return  Material instance;
	 *          <code>null</code> if an error occured (unknown material).
	 */
	Material getMaterial( String code );

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
	 * @throws  NullPointerException if <code>material</code> is <code>null</code>.
	 * @throws  IOException the material could not be stored.
	 */
	void storeMaterial( Material material )
		throws IOException;
}
