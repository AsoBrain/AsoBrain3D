/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
 */
package ab.j3d.awt.view.jogl;

import java.awt.image.*;
import java.io.*;
import javax.media.opengl.*;

import ab.j3d.appearance.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;

/**
 * Texture proxy for a bump map.
 *
 * @author  G. Meinders
 */
public class BumpTextureProxy
	extends TextureProxy
{
	/**
	 * Construct new texture proxy for a bump map.
	 *
	 * @param   textureMap      Texture map.
	 * @param   textureCache    Texture cache.
	 */
	public BumpTextureProxy( final TextureMap textureMap, final TextureCache textureCache )
	{
		super( textureMap, textureCache );
	}

	@Override
	public TextureData call()
		throws IOException
	{
		TextureData result = null;

		final BufferedImage image = _textureCache.loadImage( _textureMap );
		if ( image != null )
		{
			final BufferedImage normalMap = JOGLTools.createNormalMapFromBumpMap( image );
			final BufferedImage compatibleImage = createCompatibleTextureImage( normalMap );

			result = AWTTextureIO.newTextureData( GLProfile.get( GLProfile.GL2 ), compatibleImage, true );
		}

		return result;
	}
}
