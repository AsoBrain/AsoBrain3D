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

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.io.Serializable;

import com.numdata.oss.ImageTools;

/**
 * This class defines a texture to be using in a 3D environment.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class TextureSpec
	implements Serializable
{
	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -8129387219382329102L;

	/**
	 *  Database table name.
	 */
	public static final String TABLE_NAME = "TextureSpecs";

	/**
	 * Value returned by parseRGBString is an invalid string is detected.
	 */
	public static final int BADRGB = 0x00FFFFFF;

	/**
	 * Texture path prefix from where material texture images are loaded.
	 */
	public static String textureFilenamePrefix = "textures/";

	/**
	 * Texture path suffix from where material texture images are loaded.
	 *
	 * FIXME: This static prevents applications from using multiple
	 *        material libraries.
	 */
	public static String textureFilenameSuffix = ".jpg";

	/**
	 * Unique record ID.
	 */
	public long ID = -1;

	/**
	 * Code that uniquely identifies the texture (it should be used instead
	 * of ID to make it independent from the database ID).
	 */
	public String code;

	/**
	 * RGB value for textures without texture image (-1 -> has texture image).
	 */
	public int rgb;

	/**
	 * Opacity. Determines the transparency of the material. This ranges
	 * from fully opaque (1.0) to completely translucent (0.0). Any value
	 * outside this ranges renders undefined results.
	 */
	public float opacity;

	/**
	 * This scale factor can be used to convert world coordinates to
	 * texture coordinates (multiplying world coordinates with this factor
	 * will result in texture coordinates). This value is not used by
	 * the material itself, but may be used by the modelling engine to
	 * calculate texture coordinates. This value defaults to 1.0.
	 */
	public float textureScale;

	/**
	 * Ambient reflectivity coefficient. This determines the amount of
	 * reflected light from ambient sources (normally just 1). This value
	 * may range from almost 0 for objects that absorb most ambient light
	 * to near 1 for objects that are highly reflective. Typical values
	 * range from 0,1 to 0,2 for dull surfaces and 0,7 to 0,8 for bright
	 * surfaces. In many cases this will be the same as the diffuse
	 * reflectivity coefficient.
	 */
	public float  ambientReflectivity;

	/**
	 * Diffuse reflectivity coefficient. This determines the amount of
	 * reflected light from diffuse sources. This value may range from
	 * almost 0 for objects that absorb most ambient light to near 1 for
	 * objects that are highly reflective. Typical values range from 0,1
	 * to 0,2 for dull surfaces and 0,7 to 0,8 for bright surfaces.
	 */
	public float  diffuseReflectivity;

	/**
	 * Specular reflection coefficient. Specular reflection is total or
	 * near total reflection of incoming light in a concentrated region.
	 */
	public float  specularReflectivity;

	/**
	 * Specular reflection exponent. This exponent is an indicator for
	 * the shinyness or dullness of the material. Shiny surfaces have a
	 * large value for n (100+) and very dull surfaces approach 1. For
	 * optimization reasons, only 1, 2, 4, 8, 16, 32, 64, 128, and 256
	 * are supported.
	 */
	public int specularExponent;

	/**
	 * Flag to indicate that this material's texture has a 'grain'. If so,
	 * it is important how the material is oriented.
	 */
	public boolean grain;

	/**
	 * Default constructor.
	 */
	public TextureSpec()
	{
		ID                   = -1;
		code                 = null;
		rgb                  = 0x00FFFFFF;
		opacity              = 1.0f;
		textureScale         = -1;
		ambientReflectivity  = 0.3f;
		diffuseReflectivity  = 0.5f;
		specularReflectivity = 0.7f;
		specularExponent     = 8;
		grain                = false;
	}

	/**
	 * This method returns the texture color as ARGB.
	 *
	 * @return  Texture color as ARGB.
	 */
	public int getARGB()
	{
		return ( Math.round( opacity * 255 ) << 24 ) | ( rgb & 0xFFFFFF );
	}

	/**
	 * Get <code>Color</code> object to represent this texture. This is based on
	 * the result of the <code>getARGB()</code> method.
	 *
	 * @return  Color to use for representing this texture.
	 *
	 * @see     #getARGB
	 */
	public Color getColor()
	{
		return new Color( getARGB() , true );
	}

	/**
	 * Get RGB value as HTML text in '#rrggbb' format.
	 *
	 * @return  RGB value as HTML text in '#rrggbb' format.
	 */
	public String getHtmlRGB()
	{
		final int i = rgb;

		return new StringBuffer( 7 )
			.append( '#' )
			.append( Character.forDigit( ( i >> 20 ) & 15 , 16 ) )
			.append( Character.forDigit( ( i >> 16 ) & 15 , 16 ) )
			.append( Character.forDigit( ( i >> 12 ) & 15 , 16 ) )
			.append( Character.forDigit( ( i >>  8 ) & 15 , 16 ) )
			.append( Character.forDigit( ( i >>  4 ) & 15 , 16 ) )
			.append( Character.forDigit(   i         & 15 , 16 ) )
			.toString();
	}

	/**
	 * This method returns the texture bitmap width.
	 *
	 * @param   observer    Observer to use for observe image loading process.
	 *
	 * @return  Texture bitmap width; 0 if not available.
	 *
	 * @deprecated Please use <code>getImageImage()</code> instead and retrieve width/height from it.
	 */
	public int getTextureWidth( final Component observer )
	{
		final int result;

		final Image image = getTextureImage();
		if ( image != null )
		{
			ImageTools.waitFor( image , observer );
			result = image.getWidth( observer );
		}
		else
		{
			result = -1;
		}
		return result;
	}

	/**
	 * This method returns the texture bitmap height.
	 *
	 * @param   observer    Observer to use for observe image loading process.
	 *
	 * @return  Texture bitmap height; 0 if not available.
	 *
	 * @deprecated Please use <code>getImageImage()</code> instead and retrieve width/height from it.
	 */
	public int getTextureHeight( final Component observer )
	{
		final int result;

		final Image image = getTextureImage();
		if ( image != null )
		{
			ImageTools.waitFor( image , observer );
			result = image.getHeight( observer );
		}
		else
		{
			result = -1;
		}

		return result;
	}

	/**
	 * Get <code>Image</code> instance with texture image.
	 *
	 * @return  Texture image;
	 *          <code>null</code> if texture has no image or the image could not be loaded.
	 */
	public Image getTextureImage()
	{
		return isTexture() ? ImageTools.getImage( textureFilenamePrefix + code + textureFilenameSuffix ) : null;
	}

	/**
	 * This method returns <code>true</code> if the material has a texture,
	 * or <code>false</code> if not.
	 *
	 * @return  <code>true</code> if the material has a texture,
	 *          <code>false</code> if not.
	 */
	public boolean isTexture()
	{
		return ( code != null ) && ( code.length() > 0 ) && ( textureScale > 0 );
	}
}
