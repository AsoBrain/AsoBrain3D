/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
package ab.j3d.appearance;

import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * This interface describes the appearance of an object.
 *
 * @author Peter S. Heijnen
 */
public interface Appearance
{
	/**
	 * Returns the name identifying the appearance.
	 *
	 * @return Name of the appearance.
	 */
	@Nullable
	String getName();

	/**
	 * Get ambient reflection color.
	 * <p>
	 * This determines the amount of reflected light from ambient sources
	 * (normally just 1). This value may range from almost 0 for objects
	 * that absorb most ambient light to near 1 for objects that are highly
	 * reflective. Typical values range from 0.1 to 0.2 for dull surfaces
	 * and 0.7 to 0.8 for bright surfaces.
	 *
	 * @return Ambient reflection color.
	 */
	Color4 getAmbientColor();

	/**
	 * Get diffuse reflection color and opacity.
	 * <p>
	 * This determines the amount of reflected light from diffuse sources.
	 * This value may range from almost 0 for objects that absorb most
	 * diffuse light to near 1 for objects that are highly reflective.
	 * Typical values range from 0.1 to 0.2 for dull surfaces and 0.7 to
	 * 0.8 for bright surfaces.
	 *
	 * @return Diffuse reflection color and opacity.
	 */
	Color4 getDiffuseColor();

	/**
	 * Get specular highlight color.
	 * <p>
	 * Specular reflection is total or near total reflection of incoming
	 * light in a concentrated region. It can be used to create highlights
	 * on shiny surfaces.
	 *
	 * @return Specular highlight color.
	 */
	Color4 getSpecularColor();

	/**
	 * Specular highlight exponent. This exponent is an indicator for
	 * the shininess or dullness of the material. Shiny surfaces have a
	 * large value for n (64+) and very dull surfaces approach 1. This
	 * value should be a power of 2 between 1 and 128.
	 *
	 * @return Specular highlight exponent.
	 */
	int getShininess();

	/**
	 * Get emissive color.
	 * <p>
	 * This determines the amount of light emitted by this material.
	 * Note that this doesn't automatically imply a light source.
	 *
	 * @return Emissive color.
	 */
	Color4 getEmissiveColor();

	/**
	 * Color map to use. This map provides color and possibly opacity (alpha)
	 * data. Set to <code>null</code> if no color map is used.
	 *
	 * @return Color map;
	 * <code>null</code> if no color map is available.
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
	 * @return Bump map;
	 * <code>null</code> if no bump map is available.
	 */
	@Nullable
	TextureMap getBumpMap();

	/**
	 * Reflection map to use for real-time reflections. A renderer may use the
	 * surrounding scene instead of this map.
	 *
	 * @return Reflection map;
	 * <code>null</code> if no reflection map is available.
	 */
	@Nullable
	CubeMap getReflectionMap();

	/**
	 * Get reflection intensity of the material when viewed parallel to its
	 * normal. If a reflection map is active, that map will be combined with the
	 * reflection intensity and reflection color.
	 *
	 * @return Reflectivity when viewed parallel to its normal.
	 */
	float getReflectionMin();

	/**
	 * Get reflectivity of the material when viewed perpendicular to its normal.
	 * If a reflection map is active, that map will be combined with the
	 * reflection intensity and reflection color.
	 *
	 * @return Reflectivity when viewed perpendicular to its normal.
	 */
	float getReflectionMax();

	/**
	 * Get reflection color/intensity of (specular) reflections. If a reflection
	 * map is active, that map will be combined with the reflection intensity
	 * and reflection color.
	 *
	 * @return Reflection color/intensity of (specular) reflections.
	 */
	Color4 getReflectionColor();

	/**
	 * Returns custom properties of the appearance.
	 *
	 * @return Custom properties.
	 */
	@NotNull
	Map<String, Object> getProperties();

	/**
	 * Sets a custom property. If set to {@code null}, the custom property is
	 * removed.
	 *
	 * @param key   Property key.
	 * @param value Property value.
	 */
	void setProperty( @NotNull final String key, @Nullable final Object value );
}
