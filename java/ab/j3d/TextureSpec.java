/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;

import com.numdata.oss.TextTools;
import com.numdata.oss.ui.ImageTools;

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
	public long ID;

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
	public float ambientReflectivity;

	/**
	 * Diffuse reflectivity coefficient. This determines the amount of
	 * reflected light from diffuse sources. This value may range from
	 * almost 0 for objects that absorb most ambient light to near 1 for
	 * objects that are highly reflective. Typical values range from 0,1
	 * to 0,2 for dull surfaces and 0,7 to 0,8 for bright surfaces.
	 */
	public float diffuseReflectivity;

	/**
	 * Specular reflection coefficient. Specular reflection is total or
	 * near total reflection of incoming light in a concentrated region.
	 */
	public float specularReflectivity;

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
		this( null , 0x00FFFFFF , 1.0f , -1.0f , 0.3f , 0.5f , 0.7f , 8 , false );
	}

	/**
	 * Construct texture for RGB {@link Color}.
	 *
	 * @param   color   Color specification.
	 */
	public TextureSpec( final Color color )
	{
		this( color.getRGB() );
	}

	/**
	 * Construct texture for ARGB value (see {@link Color}).
	 *
	 * @param   argb    ARGB color specification.
	 */
	public TextureSpec( final int argb )
	{
		final int     alpha   = ( argb >> 24 ) & 0xFF;
		final int     red     = ( argb >> 16 ) & 0xFF;
		final int     green   = ( argb >> 8 ) & 0xFF;
		final int     blue    = argb & 0xFF;
		final boolean opaque  = ( ( alpha <= 0 ) || ( alpha >= 255 ) );
		final boolean isBlack = ( red <= 1 ) && ( green <=1 ) && ( blue <= 1 );

		ID                   = -1L;
		code                 = '#' + TextTools.toHexString( argb , 6 , false );
		rgb                  = isBlack ? 0x010101 : ( argb & 0xFFFFFF );
		opacity              = opaque ? 1.0f : ( (float)alpha / 255.0f );
		textureScale         = 0.0f;
		ambientReflectivity  = isBlack ? 0.10f : 0.3f;
		diffuseReflectivity  = isBlack ? 0.15f : 0.3f;
		specularReflectivity = isBlack ? 0.90f : 0.3f;
		specularExponent     = isBlack ? 16    : 8;
		grain                = false;
	}

	/**
	 * Construct texture with the specified properties.
	 *
	 * @param   code                    Code that uniquely identifies the texture.
	 * @param   rgb                     RGB color value  (-1 -> has texture image).
	 * @param   opacity                 Opacity (opaque: 1.0, completely translucent: 0.0).
	 * @param   textureScale            Scale factor from world to texture coordinates.
	 * @param   ambientReflectivity     Ambient reflectivity coefficient.
	 * @param   diffuseReflectivity     Diffuse reflectivity coefficient.
	 * @param   specularReflectivity    Specular reflection coefficient.
	 * @param   specularExponent        Specular reflection exponent.
	 * @param   grain                   Flag to indicate that texture has a 'grain'.
	 */
	public TextureSpec( final String code , final int rgb , final float opacity , final float textureScale , final float ambientReflectivity , final float diffuseReflectivity , final float specularReflectivity , final int specularExponent , final boolean grain )
	{
		ID                        = -1L;
		this.code                 = code;
		this.rgb                  = rgb;
		this.opacity              = opacity;
		this.textureScale         = textureScale;
		this.ambientReflectivity  = ambientReflectivity;
		this.diffuseReflectivity  = diffuseReflectivity;
		this.specularReflectivity = specularReflectivity;
		this.specularExponent     = specularExponent;
		this.grain                = grain;
	}

	/**
	 * This method returns the texture color as ARGB.
	 *
	 * @return  Texture color as ARGB.
	 */
	public int getARGB()
	{
		return ( Math.round( opacity * 255.0f ) << 24 ) | ( rgb & 0xFFFFFF );
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

		return "#"
			+ Character.forDigit( ( i >> 20 ) & 15 , 16 )
			+ Character.forDigit( ( i >> 16 ) & 15 , 16 )
			+ Character.forDigit( ( i >> 12 ) & 15 , 16 )
			+ Character.forDigit( ( i >>  8 ) & 15 , 16 )
			+ Character.forDigit( ( i >>  4 ) & 15 , 16 )
			+ Character.forDigit(   i         & 15 , 16 );
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

	/**
	 * Read object from byte-aray.
	 *
	 * @param   data    Byte-array to read the object from.
	 *
	 * @throws  IOException if a problem occured.
	 */
	public void read( final byte[] data )
		throws IOException
	{
		final DataInputStream is = new DataInputStream( new ByteArrayInputStream( data ) );

		ID                   = -1L;
		code                 = null;
		rgb                  = 0x00FFFFFF;
		opacity              = 1.0f;
		textureScale         = -1.0f;
		ambientReflectivity  = 0.3f;
		diffuseReflectivity  = 0.5f;
		specularReflectivity = 0.7f;
		specularExponent     = 8;
		grain                = false;

		while ( true )
		{
			final String fieldName;
			try
			{
				fieldName = is.readUTF();
			}
			catch ( EOFException e )
			{
				break;
			}

			     if ( "ID"                   .equals( fieldName ) ) ID                   = is.readLong();
			else if ( "code"                 .equals( fieldName ) ) code                 = is.readUTF();
			else if ( "rgb"                  .equals( fieldName ) ) rgb                  = is.readInt();
			else if ( "opacity"              .equals( fieldName ) ) opacity              = is.readFloat();
			else if ( "textureScale"         .equals( fieldName ) ) textureScale         = is.readFloat();
			else if ( "ambientReflectivity"  .equals( fieldName ) ) ambientReflectivity  = is.readFloat();
			else if ( "diffuseReflectivity"  .equals( fieldName ) ) diffuseReflectivity  = is.readFloat();
			else if ( "specularReflectivity" .equals( fieldName ) ) specularReflectivity = is.readFloat();
			else if ( "specularExponent"     .equals( fieldName ) ) specularExponent     = is.readInt();
			else if ( "grain"                .equals( fieldName ) ) grain                = is.readBoolean();
			else throw new IOException( "unrecognized field: " + fieldName );
		}
	}

	/**
	 * Write object to a byte-array.
	 *
	 * @return  Byte-array containing object data.
	 *
	 * @throws  IOException if a problem occured.
	 */
	public byte[] write()
		throws IOException
	{
		final ByteArrayOutputStream result = new ByteArrayOutputStream();

		final DataOutputStream os = new DataOutputStream( result );
		try
		{
			if ( ID != -1L )
			{
				os.writeUTF( "ID" );
				os.writeLong( ID );
			}

			if ( code != null )
			{
				os.writeUTF( "code" );
				os.writeUTF( code );
			}

			os.writeUTF( "rgb"                  ); os.writeInt    ( rgb                  );
			os.writeUTF( "opacity"              ); os.writeFloat  ( opacity              );
			os.writeUTF( "textureScale"         ); os.writeFloat  ( textureScale         );
			os.writeUTF( "ambientReflectivity"  ); os.writeFloat  ( ambientReflectivity  );
			os.writeUTF( "diffuseReflectivity"  ); os.writeFloat  ( diffuseReflectivity  );
			os.writeUTF( "specularReflectivity" ); os.writeFloat  ( specularReflectivity );
			os.writeUTF( "specularExponent"     ); os.writeInt    ( specularExponent     );
			os.writeUTF( "grain"                ); os.writeBoolean( grain                );
		}
		finally
		{
			os.close();
		}

		return result.toByteArray();
	}
}
