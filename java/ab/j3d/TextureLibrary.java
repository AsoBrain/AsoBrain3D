/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2004 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
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
