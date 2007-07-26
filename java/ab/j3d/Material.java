/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2007 Peter S. Heijnen
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

import java.io.Serializable;

/**
 * This class defines a material to be using in a 3D environment.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Material
	implements Serializable
{
	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -8129387219382329102L;

	/**
	 *  Database table name.
	 */
	public static final String TABLE_NAME = "Materials";

	/**
	 * Value returned by parseRGBString is an invalid string is detected.
	 */
	public static final int BADRGB = 0x00FFFFFF;

	/**
	 * Unique record ID.
	 */
	public long ID;

	/**
	 * Code that uniquely identifies the texture (it should be used instead
	 * of ID to make it independent from the database ID).
	 */
	public String code;

	/**
	 * Red component of ambient reflection color.
	 * <p>
	 * This determines the amount of reflected light from ambient sources
	 * (normally just 1). This value may range from almost 0 for objects
	 * that absorb most ambient light to near 1 for objects that are highly
	 * reflective. Typical values range from 0.1 to 0.2 for dull surfaces
	 * and 0,7 to 0,8 for bright surfaces.
	 */
	public float ambientColorRed;

	/**
	 * Green component of ambient reflection color.
	 *
	 * @see     #ambientColorRed
	 */
	public float ambientColorGreen;

	/**
	 * Blue component of ambient reflection color.
	 *
	 * @see     #ambientColorRed
	 */
	public float ambientColorBlue;

	/**
	 * Red component of diffuse reflection color.
	 * <p>
	 * This determines the amount of reflected light from diffuse sources.
	 * This value may range from almost 0 for objects that absorb most
	 * diffuse light to near 1 for objects that are highly reflective.
	 * Typical values range from 0.1 to 0.2 for dull surfaces and 0.7 to
	 * 0.8 for bright surfaces.
	 */
	public float diffuseColorRed;

	/**
	 * Green component of diffuse reflection color.
	 *
	 * @see     #diffuseColorRed
	 */
	public float diffuseColorGreen;

	/**
	 * Blue component of diffuse reflection color.
	 *
	 * @see     #diffuseColorRed
	 */
	public float diffuseColorBlue;

	/**
	 * Opacity. Determines the transparency of the material. This ranges
	 * from fully opaque (1.0) to completely translucent (0.0). Any value
	 * outside this ranges renders undefined results.
	 */
	public float diffuseColorAlpha;

	/**
	 * Red component of specular reflection color.
	 * <p>
	 * Specular reflection is total or near total reflection of incoming
	 * light in a concentrated region. It can be used to create highlights
	 * on shiny surfaces.
	 */
	public float specularColorRed;

	/**
	 * Green component of specular reflection color.
	 *
	 * @see     #specularColorRed
	 */
	public float specularColorGreen;

	/**
	 * Blue component of specular reflection color.
	 *
	 * @see     #specularColorRed
	 */
	public float specularColorBlue;

	/**
	 * Specular reflection exponent. This exponent is an indicator for
	 * the shininess or dullness of the material. Shiny surfaces have a
	 * large value for n (64+) and very dull surfaces approach 1. This
	 * value should be a power of 2 between 1 and 128.
	 */
	public int shininess;

	/**
	 * Red component of emissive color.
	 * <p>
	 * This determines the amount of light emitted by this material.
	 * Note that this does automatically imply a light source.
	 */
	public float emissiveColorRed;

	/**
	 * Green component of emissive reflection color.
	 *
	 * @see     #emissiveColorRed
	 */
	public float emissiveColorGreen;

	/**
	 * Blue component of emissive reflection color.
	 *
	 * @see     #emissiveColorRed
	 */
	public float emissiveColorBlue;

	/**
	 * Name of color map to use. This map provides color and possibly
	 * opacity (alpha) data. Set to <code>null</code> if no color map is
	 * used.
	 */
	public String colorMap;

	/**
	 * Width of color map in meters. This can be used if the map has this
	 * physical dimension, in which case it can be correctly scaled in a virtual
	 * environment. Set to 0 if undetermined.
	 */
	public double colorMapWidth;

	/**
	 * Height of color map in meters. This can be used if the map has this
	 * physical dimension, in which case it can be correctly scaled in a virtual
	 * environment. Set to 0 if undetermined.
	 */
	public double colorMapHeight;

	/**
	 * Flag to indicate that this material's texture has a 'grain'. If so,
	 * it is important how the material is oriented.
	 */
	public boolean grain;

	/**
	 * Default constructor.
	 */
	public Material()
	{
		this( -1 ); /* opaque white */
	}

	/**
	 * Construct texture for ARGB value.
	 *
	 * @param   argb    ARGB color specification.
	 *
	 * @see     java.awt.Color
	 */
	public Material( final int argb )
	{
		final int iAlpha = ( ( argb >> 24 ) & 0xFF );
		final int iRed   = ( ( argb >> 16 ) & 0xFF );
		final int iGreen = ( ( argb >>  8 ) & 0xFF );
		final int iBlue  = (   argb         & 0xFF );

		final float alpha = ( ( iAlpha > 0 ) && ( iAlpha < 255 ) ) ? ( (float)iAlpha / 255.0f ) : 1.0f;
		final float red   = (float)iRed   / 255.0f;
		final float green = (float)iGreen / 255.0f;
		final float blue  = (float)iBlue  / 255.0f;

		ID                 = -1L;
		code               = null;
		ambientColorRed    = 0.2f * red;
		ambientColorGreen  = 0.2f * green;
		ambientColorBlue   = 0.2f * blue;
		diffuseColorRed    = red;
		diffuseColorGreen  = green;
		diffuseColorBlue   = blue;
		diffuseColorAlpha  = alpha;
		specularColorRed   = 1.0f;
		specularColorGreen = 1.0f;
		specularColorBlue  = 1.0f;
		shininess          = 16;
		emissiveColorRed   = 0.0f;
		emissiveColorGreen = 0.0f;
		emissiveColorBlue  = 0.0f;
		colorMap           = null;
		colorMapWidth      = 0.0;
		colorMapHeight     = 0.0;
		grain              = false;
	}

	/**
	 * Construct material with the specified properties.
	 *
	 * @param   code                Code that uniquely identifies the material.
	 * @param   ambientColorRed     Red component of ambient reflection color.
	 * @param   ambientColorGreen   Green component of ambient reflection color.
	 * @param   ambientColorBlue    Blue component of ambient reflection color.
	 * @param   diffuseColorRed     Red component of diffuse reflection color.
	 * @param   diffuseColorGreen   Green component of diffuse reflection color.
	 * @param   diffuseColorBlue    Blue component of diffuse reflection color.
	 * @param   diffuseColorAlpha   Opacity (opaque: 1.0, completely translucent: 0.0).
	 * @param   specularColorRed    Red component of specular reflection color.
	 * @param   specularColorGreen  Green component of specular reflection color.
	 * @param   specularColorBlue   Blue component of specular reflection color.
	 * @param   shininess           Specular reflection exponent.
	 * @param   emissiveColorRed    Red component of emissive color.
	 * @param   emissiveColorGreen  Green component of emissive color.
	 * @param   emissiveColorBlue   Blue component of emissive color.
	 * @param   colorMap            Name of color map (<code>null</code> => none).
	 * @param   colorMapWidth       Width of color map in meters (<code>0</code> => undetermined).
	 * @param   colorMapHeight      Height of texture color map in meters (<code>0</code> => undetermined).
	 * @param   grain               Flag to indicate that material has a 'grain'.
	 */
	public Material( final String code , final float ambientColorRed , final float ambientColorGreen , final float ambientColorBlue , final float diffuseColorRed , final float diffuseColorGreen , final float diffuseColorBlue , final float diffuseColorAlpha , final float specularColorRed , final float specularColorGreen , final float specularColorBlue , final int shininess , final float emissiveColorRed , final float emissiveColorGreen , final float emissiveColorBlue , final String colorMap , final double colorMapWidth , final double colorMapHeight , final boolean grain )
	{
		ID                      = -1L;
		this.code               = code;
		this.ambientColorRed    = ambientColorRed;
		this.ambientColorGreen  = ambientColorGreen;
		this.ambientColorBlue   = ambientColorBlue;
		this.diffuseColorRed    = diffuseColorRed;
		this.diffuseColorGreen  = diffuseColorGreen;
		this.diffuseColorBlue   = diffuseColorBlue;
		this.diffuseColorAlpha  = diffuseColorAlpha;
		this.specularColorRed   = specularColorRed;
		this.specularColorGreen = specularColorGreen;
		this.specularColorBlue  = specularColorBlue;
		this.shininess          = shininess;
		this.emissiveColorRed   = emissiveColorRed;
		this.emissiveColorGreen = emissiveColorGreen;
		this.emissiveColorBlue  = emissiveColorBlue;
		this.colorMap           = colorMap;
		this.colorMapWidth      = colorMapWidth;
		this.colorMapHeight     = colorMapHeight;
		this.grain              = grain;
	}

	/**
	 * This method returns the material color as ARGB.
	 *
	 * @return  Texture color as ARGB.
	 */
	public int getARGB()
	{
		return   Math.round( diffuseColorBlue  * 255.0f )
		     | ( Math.round( diffuseColorGreen * 255.0f ) <<  8 )
		     | ( Math.round( diffuseColorRed   * 255.0f ) << 16 )
		     | ( Math.round( diffuseColorAlpha * 255.0f ) << 24 );
	}

	/**
	 * Get combined ambient reflectivity. This is based on the weighted average
	 * red, green, and blue ambient reflectivity color components.
	 *
	 * @return  Combined ambient reflectivity.
	 */
	public double getAmbientReflectivity()
	{
		return 0.3 * (double)ambientColorRed  + 0.59 * (double)ambientColorGreen  + 0.11 * (double)ambientColorBlue;
	}

	/**
	 * Get combined diffuse reflectivity. This is based on the weighted average
	 * red, green, and blue diffuse reflectivity color components.
	 *
	 * @return  Combined diffuse reflectivity.
	 */
	public double getDiffuseReflectivity()
	{
		return 0.3 * (double)diffuseColorRed  + 0.59 * (double)diffuseColorGreen  + 0.11 * (double)diffuseColorBlue;
	}

	/**
	 * Get combined specular reflectivity. This is based on the weighted average
	 * red, green, and blue specular reflectivity color components.
	 *
	 * @return  Combined specular reflectivity.
	 */
	public double getSpecularReflectivity()
	{
		return 0.3 * (double)specularColorRed + 0.59 * (double)specularColorGreen + 0.11 * (double)specularColorBlue;
	}

	/**
	 * Get combined emission. This is based on the weighted average red, green,
	 * and blue emissive color components.
	 *
	 * @return  Combined emission.
	 */
	public double getEmission()
	{
		return 0.3 * (double)emissiveColorRed + 0.59 * (double)emissiveColorGreen + 0.11 * (double)emissiveColorBlue;
	}
}
