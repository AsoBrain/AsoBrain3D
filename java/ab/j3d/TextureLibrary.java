/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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

/**
 * This interface is used to retrieve textures from a library. A typical
 * implementation of this interface would generate the texture data or retrieve
 * it from some data source (a database for example).
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface TextureLibrary
{
	/**
	 * Get texture specifications from the library for the specified code.
	 *
	 * @param   code    Texture code.
	 *
	 * @return  TextureSpec instance;
	 *          <code>null</code> if an error occured (unknown texture).
	 */
	TextureSpec getTextureSpec( String code );
}
