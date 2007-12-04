/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2006-2006 Numdata BV.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Numdata BV (http://www.numdata.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Numdata" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission of Numdata BV. For written permission, please contact
 *    info@numdata.com.
 *
 * 5. Products derived from this software may not be called "Numdata",
 *    nor may "Numdata" appear in their name, without prior written
 *    permission of Numdata BV.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE NUMDATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package ab.j3d;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import ab.j3d.loader.ResourceLoader;

/**
 * Material which loads its texture from a resourceloader.
 *
 * @author $
 * @version $
 *
 */
public class ResourceLoaderMaterial
	extends Material
{
	/**
	 * Resourceloader to use.
	 */
	private ResourceLoader _resourceloader;

	/**
	 * Texture cache
	 */
	private Map<String,SoftReference<BufferedImage>> _textureCache = new HashMap<String,SoftReference<BufferedImage>>();

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4730962090640159465L;

	/**
	 * Constructs a new ResourceLoaderMaterial with given {@link ResourceLoader}
	 *
	 * @param resourceLoader     {@link ResourceLoader} to use.
	 */
	public ResourceLoaderMaterial( final ResourceLoader resourceLoader )
	{
		_resourceloader = resourceLoader;
	}

	/**
	 * Construct ResourceLoaderMaterial for ARGB value.
	 *
	 * @param   argb    ARGB color specification.
	 *
	 * @see     java.awt.Color
	 */
	public ResourceLoaderMaterial( final int argb )
	{
		super( argb );
		_resourceloader = null;
	}

	public BufferedImage getColorMapImage( final boolean useCache )
	{
		BufferedImage result;
		final ResourceLoader resourceloader = _resourceloader;
		if ( resourceloader != null )
		{
			final Map<String,SoftReference<BufferedImage>> textureCache = _textureCache;
			if ( useCache && textureCache.containsKey( super.colorMap ) && textureCache
			.get( super.colorMap ) != null )
			{
				result = textureCache.get( super.colorMap ).get();
			}
			else
			{

				try
				{
					final InputStream is = resourceloader.getResource( super.colorMap );
					result = ImageIO.read( is );
					textureCache.put( super.colorMap , new SoftReference<BufferedImage>( result ) );

				}
				catch ( IOException e )
				{
					result = null;
					e.printStackTrace();
				}
			}
		}
		else
			result = super.getColorMapImage( true );
		return result;
	}
}