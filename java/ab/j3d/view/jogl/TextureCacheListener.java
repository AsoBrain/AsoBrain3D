/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2010-2010 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view.jogl;

import org.jetbrains.annotations.NotNull;

/**
 * Event listener that is notified by a texture cache when the state of
 * a texture changes.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface TextureCacheListener
{
	/**
	 * Notifies the listener that a texture was loaded or changed.
	 *
	 * @param   textureCache    Texture cache that loaded the texture.
	 * @param   textureProxy    Texture that was loaded.
	 */
	void textureChanged( @NotNull final TextureCache textureCache , @NotNull final TextureProxy textureProxy );
}
