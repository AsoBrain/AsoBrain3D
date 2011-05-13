/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2007-2007 Numdata BV.  All rights reserved.
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

import ab.j3d.appearance.*;
import ab.j3d.loader.*;
import com.numdata.oss.*;

/**
 * Material which loads its texture from a resourceloader.
 *
 * @author  Wijnand Wieskamp
 * @version $Revision$
 */
public class ResourceLoaderMaterial
	extends Material
{
	/**
	 * {@link ResourceLoader} to use.
	 */
	private ResourceLoader _resourceloader;

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4730962090640159465L;

	/**
	 * Constructs a new {@link ResourceLoaderMaterial} with given {@link ResourceLoader}
	 *
	 * @param resourceLoader     {@link ResourceLoader} to use.
	 */
	public ResourceLoaderMaterial( final ResourceLoader resourceLoader )
	{
		_resourceloader = resourceLoader;
	}

	/**
	 * Construct {@link ResourceLoaderMaterial} for ARGB value.
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

	@Override
	public TextureMap getColorMap()
	{
		return TextTools.isEmpty( colorMap ) ? null : new ResourceLoaderTextureMap( _resourceloader, colorMap, colorMapWidth, colorMapHeight );
	}

	@Override
	public TextureMap getBumpMap()
	{
		return TextTools.isEmpty( bumpMap ) ? null : new ResourceLoaderTextureMap( _resourceloader, bumpMap, bumpMapWidth, bumpMapHeight );
	}

}
