/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.appearance;

import org.jetbrains.annotations.*;

/**
 * Basic implementation of {@link Appearance} interface. This simply provides a
 * mutable version for each property defined by the {@link Appearance}
 * interface.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class BasicAppearance
	implements Appearance
{
	/**
	 * Red component of ambient reflection color.
	 */
	private float _ambientColorRed = 0.0f;

	/**
	 * Green component of ambient reflection color.
	 */
	private float _ambientColorGreen = 0.0f;

	/**
	 * Blue component of ambient reflection color.
	 */
	private float _ambientColorBlue = 0.0f;

	/**
	 * Red component of diffuse reflection color.
	 */
	private float _diffuseColorRed = 0.0f;

	/**
	 * Green component of diffuse reflection color.
	 */
	private float _diffuseColorGreen = 0.0f;

	/**
	 * Blue component of diffuse reflection color.
	 */
	private float _diffuseColorBlue = 0.0f;

	/**
	 * Opacity.
	 */
	private float _diffuseColorAlpha = 0.0f;

	/**
	 * Red component of specular highlight color.
	 */
	private float _specularColorRed = 0.0f;

	/**
	 * Green component of specular highlight color.
	 */
	private float _specularColorGreen = 0.0f;

	/**
	 * Blue component of specular highlight color.
	 */
	private float _specularColorBlue = 0.0f;

	/**
	 * Specular highlight exponent.
	 */
	private int _shininess = 0;

	/**
	 * Red component of emissive color.
	 */
	private float _emissiveColorRed = 0.0f;

	/**
	 * Green component of emissive reflection color.
	 */
	private float _emissiveColorGreen = 0.0f;

	/**
	 * Blue component of emissive reflection color.
	 */
	private float _emissiveColorBlue = 0.0f;

	/**
	 * Color map to use.
	 */
	@Nullable
	private TextureMap _colorMap = null;

	/**
	 * Bump map to use.
	 */
	@Nullable
	private TextureMap _bumpMap = null;

	/**
	 * Map to use for reflections.
	 */
	@Nullable
	private ReflectionMap _reflectionMap = null;

	public float getAmbientColorRed()
	{
		return _ambientColorRed;
	}

	/**
	 * Set red component of ambient reflection color.
	 *
	 * @param   red     Red component of ambient reflection color.
	 */
	public void setAmbientColorRed( final float red )
	{
		_ambientColorRed = red;
	}

	public float getAmbientColorGreen()
	{
		return _ambientColorGreen;
	}

	/**
	 * Green component of ambient reflection color.
	 *
	 * @param   green   Green component of ambient reflection color.
	 */
	public void setAmbientColorGreen( final float green )
	{
		_ambientColorGreen = green;
	}

	public float getAmbientColorBlue()
	{
		return _ambientColorBlue;
	}

	/**
	 * Blue component of ambient reflection color.
	 *
	 * @param   blue    Blue component of ambient reflection color.
	 */
	public void setAmbientColorBlue( final float blue )
	{
		_ambientColorBlue = blue;
	}

	public float getDiffuseColorRed()
	{
		return _diffuseColorRed;
	}

	/**
	 * Red component of diffuse reflection color.
	 *
	 * @param   red     Red component of diffuse reflection color.
	 */
	public void setDiffuseColorRed( final float red )
	{
		_diffuseColorRed = red;
	}

	public float getDiffuseColorGreen()
	{
		return _diffuseColorGreen;
	}

	/**
	 * Green component of diffuse reflection color.
	 *
	 * @param   green   Green component of diffuse reflection color.
	 */
	public void setDiffuseColorGreen( final float green )
	{
		_diffuseColorGreen = green;
	}

	public float getDiffuseColorBlue()
	{
		return _diffuseColorBlue;
	}

	/**
	 * Blue component of diffuse reflection color.
	 *
	 * @param   blue    Blue component of diffuse reflection color.
	 */
	public void setDiffuseColorBlue( final float blue )
	{
		_diffuseColorBlue = blue;
	}

	public float getDiffuseColorAlpha()
	{
		return _diffuseColorAlpha;
	}

	/**
	 * Opacity. Determines the transparency of the material. This ranges
	 * from fully opaque (1.0) to completely translucent (0.0). Any value
	 * outside this ranges renders undefined results.
	 *
	 * @param   alpha   Opacity.
	 */
	public void setDiffuseColorAlpha( final float alpha )
	{
		_diffuseColorAlpha = alpha;
	}

	public float getSpecularColorRed()
	{
		return _specularColorRed;
	}

	/**
	 * Red component of specular highlight color.
	 *
	 * @param   red     Red component of specular highlight color.
	 */
	public void setSpecularColorRed( final float red )
	{
		_specularColorRed = red;
	}

	public float getSpecularColorGreen()
	{
		return _specularColorGreen;
	}

	/**
	 * Green component of specular highlight color.
	 *
	 * @param   green   Green component of specular highlight color.
	 */
	public void setSpecularColorGreen( final float green )
	{
		_specularColorGreen = green;
	}

	public float getSpecularColorBlue()
	{
		return _specularColorBlue;
	}

	/**
	 * Blue component of specular highlight color.
	 *
	 * @param   blue   Blue component of specular highlight color.
	 */
	public void setSpecularColorBlue( final float blue )
	{
		_specularColorBlue = blue;
	}

	public int getShininess()
	{
		return _shininess;
	}

	/**
	 * Specular highlight exponent.
	 *
	 * @param   shininess   Specular highlight exponent.
	 */
	public void setShininess( final int shininess )
	{
		_shininess = shininess;
	}

	public float getEmissiveColorRed()
	{
		return _emissiveColorRed;
	}

	/**
	 * Red component of emissive color.
	 *
	 * @param   red     Red component of emissive color.
	 */
	public void setEmissiveColorRed( final float red )
	{
		_emissiveColorRed = red;
	}

	public float getEmissiveColorGreen()
	{
		return _emissiveColorGreen;
	}

	/**
	 * Green component of emissive reflection color.
	 *
	 * @param   green   Green component of emissive reflection color.
	 */
	public void setEmissiveColorGreen( final float green )
	{
		_emissiveColorGreen = green;
	}

	public float getEmissiveColorBlue()
	{
		return _emissiveColorBlue;
	}

	/**
	 * Blue component of emissive reflection color.
	 *
	 * @param   blue    Blue component of emissive reflection color.
	 */
	public void setEmissiveColorBlue( final float blue )
	{
		_emissiveColorBlue = blue;
	}

	@Nullable
	public TextureMap getColorMap()
	{
		return _colorMap;
	}

	/**
	 * Color map to use.
	 *
	 * @param   map     Color map; <code>null</code> if none.
	 */
	public void setColorMap( @Nullable final TextureMap map )
	{
		_colorMap = map;
	}

	@Nullable
	public TextureMap getBumpMap()
	{
		return _bumpMap;
	}

	/**
	 * Name of bump map to use.
	 *
	 * @param   map     Bump map; <code>null</code> if none.
	 */
	public void setBumpMap( @Nullable final TextureMap map )
	{
		_bumpMap = map;
	}

	@Nullable
	public ReflectionMap getReflectionMap()
	{
		return _reflectionMap;
	}

	/**
	 * Map to use for reflections.
	 *
	 * @param   map     Reflection map; <code>null</code> if none.
	 */
	public void setReflectionMap( @Nullable final ReflectionMap map )
	{
		_reflectionMap = map;
	}
}