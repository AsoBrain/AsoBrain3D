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
 * This interface describes the appearance of an object.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public interface Appearance
{
	/**
	 * Red component of ambient reflection color.
	 * <p>
	 * This determines the amount of reflected light from ambient sources
	 * (normally just 1). This value may range from almost 0 for objects
	 * that absorb most ambient light to near 1 for objects that are highly
	 * reflective. Typical values range from 0.1 to 0.2 for dull surfaces
	 * and 0.7 to 0.8 for bright surfaces.
	 *
	 * @return  Red component of ambient reflection color.
	 */
	float getAmbientColorRed();

	/**
	 * Green component of ambient reflection color.
	 *
	 * @return  Green component of ambient reflection color.
	 *
	 * @see     #getAmbientColorRed
	 */
	float getAmbientColorGreen();

	/**
	 * Blue component of ambient reflection color.
	 *
	 * @return  Blue component of ambient reflection color.
	 *
	 * @see     #getAmbientColorRed
	 */
	float getAmbientColorBlue();

	/**
	 * Red component of diffuse reflection color.
	 * <p>
	 * This determines the amount of reflected light from diffuse sources.
	 * This value may range from almost 0 for objects that absorb most
	 * diffuse light to near 1 for objects that are highly reflective.
	 * Typical values range from 0.1 to 0.2 for dull surfaces and 0.7 to
	 * 0.8 for bright surfaces.
	 *
	 * @return  Red component of diffuse reflection color.
	 */
	float getDiffuseColorRed();

	/**
	 * Green component of diffuse reflection color.
	 *
	 * @return  Green component of diffuse reflection color.
	 *
	 * @see     #getDiffuseColorRed
	 */
	float getDiffuseColorGreen();

	/**
	 * Blue component of diffuse reflection color.
	 *
	 * @return  Blue component of diffuse reflection color.
	 *
	 * @see     #getDiffuseColorRed
	 */
	float getDiffuseColorBlue();

	/**
	 * Opacity. Determines the transparency of the material. This ranges
	 * from fully opaque (1.0) to completely translucent (0.0). Any value
	 * outside this ranges renders undefined results.
	 *
	 * @return  Opacity.
	 */
	float getDiffuseColorAlpha();

	/**
	 * Red component of specular highlight color.
	 * <p>
	 * Specular reflection is total or near total reflection of incoming
	 * light in a concentrated region. It can be used to create highlights
	 * on shiny surfaces.
	 *
	 * @return  Red component of specular highlight color.
	 */
	float getSpecularColorRed();

	/**
	 * Green component of specular highlight color.
	 *
	 * @return  Green component of specular highlight color.
	 *
	 * @see     #getSpecularColorRed
	 */
	float getSpecularColorGreen();

	/**
	 * Blue component of specular highlight color.
	 *
	 * @return  Blue component of specular highlight color.
	 *
	 * @see     #getSpecularColorRed
	 */
	float getSpecularColorBlue();

	/**
	 * Specular highlight exponent. This exponent is an indicator for
	 * the shininess or dullness of the material. Shiny surfaces have a
	 * large value for n (64+) and very dull surfaces approach 1. This
	 * value should be a power of 2 between 1 and 128.
	 *
	 * @return  Specular highlight exponent.
	 */
	int getShininess();

	/**
	 * Red component of emissive color.
	 * <p>
	 * This determines the amount of light emitted by this material.
	 * Note that this doesn't automatically imply a light source.
	 *
	 * @return  Red component of emissive color.
	 */
	float getEmissiveColorRed();

	/**
	 * Green component of emissive reflection color.
	 *
	 * @return  Green component of emissive reflection color.
	 *
	 * @see     #getEmissiveColorRed
	 */
	float getEmissiveColorGreen();

	/**
	 * Blue component of emissive reflection color.
	 *
	 * @return  Blue component of emissive reflection color.
	 *
	 * @see     #getEmissiveColorRed
	 */
	float getEmissiveColorBlue();

	/**
	 * Color map to use. This map provides color and possibly opacity (alpha)
	 * data. Set to <code>null</code> if no color map is used.
	 *
	 * @return  Color map;
	 *          <code>null</code> if no color map is available.
	 */
	@Nullable
	TextureMap getColorMap();

	/**
	 * Name of bump map to use. This map specifies a height offset at each
	 * pixel, used to create the illusion of highly detailed geometry that
	 * includes bumps, scratches and so on. The map should be in grayscale and
	 * should have no transparency.
	 * Set to <code>null</code> if no bump map is used.
	 *
	 * @return  Bump map;
	 *          <code>null</code> if no bump map is available.
	 */
	@Nullable
	TextureMap getBumpMap();

	/**
	 * Reflection map to use for real-time reflections. A renderer may use the
	 * surrounding scene instead of this map.
	 *
	 * @return  Reflection map;
	 *          <code>null</code> if no reflection map is available.
	 */
	@Nullable
	ReflectionMap getReflectionMap();
}