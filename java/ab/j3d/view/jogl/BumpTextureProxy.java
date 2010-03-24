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

import java.awt.image.BufferedImage;

import com.sun.opengl.util.texture.TextureData;

import ab.j3d.MapTools;

/**
 * Texture proxy for a bump map.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class BumpTextureProxy
	extends TextureProxy
{
	/**
	 * Construct new texture proxy for a bump map.
	 *
	 * @param   name            Name of the texture image.
	 * @param   textureCache    Texture cache.
	 *
	 * @see     MapTools#loadImage
	 */
	public BumpTextureProxy( final String name , final TextureCache textureCache )
	{
		super( name , textureCache );
	}

	@Override
	public TextureData call()
	{
		TextureData result = null;

		final BufferedImage bufferedImage = MapTools.loadImage( _name );
		if ( bufferedImage != null )
		{
			final BufferedImage normalMap = JOGLTools.createNormalMapFromBumpMap( bufferedImage );
			final BufferedImage compatibleImage = createCompatibleTextureImage( normalMap );

			result = createTextureData( compatibleImage );
		}

		return result;
	}
}
