/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2009 Peter S. Heijnen
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
import java.awt.image.BufferedImage;
import java.io.Serializable;

import ab.j3d.loader.ResourceLoader;

import com.numdata.oss.TextTools;

/**
 * This class defines a material to be using in a 3D environment.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Material
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
	 * SQL create statement (for MySQL).
	 */
	public static final String MYSQL_CREATE_STATEMENT = "CREATE TABLE `Materials` (\n" +
		"  `ID` int(11) NOT NULL auto_increment,\n" +
		"  `code` varchar(64) NOT NULL,\n" +
		"  `ambientColorRed` float NOT NULL,\n" +
		"  `ambientColorGreen` float NOT NULL,\n" +
		"  `ambientColorBlue` float NOT NULL,\n" +
		"  `diffuseColorRed` float NOT NULL,\n" +
		"  `diffuseColorGreen` float NOT NULL,\n" +
		"  `diffuseColorBlue` float NOT NULL,\n" +
		"  `diffuseColorAlpha` float NOT NULL,\n" +
		"  `specularColorRed` float NOT NULL,\n" +
		"  `specularColorGreen` float NOT NULL,\n" +
		"  `specularColorBlue` float NOT NULL,\n" +
		"  `shininess` int(11) NOT NULL,\n" +
		"  `emissiveColorRed` float NOT NULL,\n" +
		"  `emissiveColorGreen` float NOT NULL,\n" +
		"  `emissiveColorBlue` float NOT NULL,\n" +
		"  `colorMap` varchar(64) default NULL,\n" +
		"  `colorMapWidth` float NOT NULL,\n" +
		"  `colorMapHeight` float NOT NULL,\n" +
		"  `bumpMap` varchar(64) default NULL,\n" +
		"  `bumpMapWidth` float NOT NULL,\n" +
		"  `bumpMapHeight` float NOT NULL,\n" +
		"  `grain` tinyint(1) NOT NULL,\n" +
		"  PRIMARY KEY  (`ID`),\n" +
		"  UNIQUE KEY `code` (`code`)\n" +
		");";

	/**
	 * Unique record ID.
	 */
	public int ID;

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
	public float colorMapWidth;

	/**
	 * Height of color map in meters. This can be used if the map has this
	 * physical dimension, in which case it can be correctly scaled in a virtual
	 * environment. Set to 0 if undetermined.
	 */
	public float colorMapHeight;

	/**
	 * Name of bump map to use. This map specifies a height offset at each
	 * pixel, used to create the illusion of highly detailed geometry that
	 * includes bumps, scratches and so on. The map should be in grayscale and
	 * should have no transparency.
	 * Set to <code>null</code> if no bump map is used.
	 */
	public String bumpMap;

	/**
	 * Width of bump map in meters. This can be used if the map has this
	 * physical dimension, in which case it can be correctly scaled in a virtual
	 * environment. Set to 0 if undetermined.
	 */
	public float bumpMapWidth;

	/**
	 * Height of bump map in meters. This can be used if the map has this
	 * physical dimension, in which case it can be correctly scaled in a virtual
	 * environment. Set to 0 if undetermined.
	 */
	public float bumpMapHeight;

	/**
	 * Flag to indicate that this material's texture has a 'grain'. If so,
	 * it is important how the material is oriented.
	 */
	public boolean grain;

	/**
	 * Loader for resources related to this material (e.g. maps).
	 */
	public transient ResourceLoader resourceLoader;

	/**
	 * Default constructor.
	 */
	public Material()
	{
		ID                 = -1;
		code               = null;
		ambientColorRed    = 1.0f;
		ambientColorGreen  = 1.0f;
		ambientColorBlue   = 1.0f;
		diffuseColorRed    = 1.0f;
		diffuseColorGreen  = 1.0f;
		diffuseColorBlue   = 1.0f;
		diffuseColorAlpha  = 1.0f;
		specularColorRed   = 1.0f;
		specularColorGreen = 1.0f;
		specularColorBlue  = 1.0f;
		shininess          = 16;
		emissiveColorRed   = 0.0f;
		emissiveColorGreen = 0.0f;
		emissiveColorBlue  = 0.0f;
		colorMap           = null;
		colorMapWidth      = 0.0f;
		colorMapHeight     = 0.0f;
		bumpMap            = null;
		bumpMapWidth       = 0.0f;
		bumpMapHeight      = 0.0f;
		grain              = false;
		resourceLoader     = null;
	}

	/**
	 * Clone constructor. Note that the {@link #ID} field is reset to
	 * <code>-1</code> to mark the clone as a new record, all other fields are
	 * copied as-is.
	 *
	 * @param   original    Original to clone.
	 */
	public Material( final Material original )
	{
		ID                 = -1;
		code               = original.code;
		ambientColorRed    = original.ambientColorRed;
		ambientColorGreen  = original.ambientColorGreen;
		ambientColorBlue   = original.ambientColorBlue;
		diffuseColorRed    = original.diffuseColorRed;
		diffuseColorGreen  = original.diffuseColorGreen;
		diffuseColorBlue   = original.diffuseColorBlue;
		diffuseColorAlpha  = original.diffuseColorAlpha;
		specularColorRed   = original.specularColorRed;
		specularColorGreen = original.specularColorGreen;
		specularColorBlue  = original.specularColorBlue;
		shininess          = original.shininess;
		emissiveColorRed   = original.emissiveColorRed;
		emissiveColorGreen = original.emissiveColorGreen;
		emissiveColorBlue  = original.emissiveColorBlue;
		colorMap           = original.colorMap;
		colorMapWidth      = original.colorMapWidth;
		colorMapHeight     = original.colorMapHeight;
		bumpMap            = original.bumpMap;
		bumpMapWidth       = original.bumpMapWidth;
		bumpMapHeight      = original.bumpMapHeight;
		grain              = original.grain;
		resourceLoader     = original.resourceLoader;
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
		this();
		setAmbientColor( argb );
		setDiffuseColor( argb );
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
	public Material( final String code , final float ambientColorRed , final float ambientColorGreen , final float ambientColorBlue , final float diffuseColorRed , final float diffuseColorGreen , final float diffuseColorBlue , final float diffuseColorAlpha , final float specularColorRed , final float specularColorGreen , final float specularColorBlue , final int shininess , final float emissiveColorRed , final float emissiveColorGreen , final float emissiveColorBlue , final String colorMap , final float colorMapWidth , final float colorMapHeight , final boolean grain )
	{
		ID                      = -1;
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
		bumpMap                 = null;
		bumpMapWidth            = 0.0f;
		bumpMapHeight           = 0.0f;
		this.grain              = grain;
		resourceLoader          = null;
	}

	/**
	 * Get {@link BufferedImage} instance with color map image.
	 *
	 * @param   useCache        Use caching of image data if available.
	 *
	 * @return  Color map image;
	 *          <code>null</code> if no color map was defined or could be loaded.
 	 */
	public BufferedImage getColorMapImage( final boolean useCache )
	{
		final String map = colorMap;
		return TextTools.isNonEmpty( map ) ? useCache ? MapTools.getImage( map ) : MapTools.loadImage( map ) : null;
	}

	/**
	 * Get {@link BufferedImage} instance with bump map image.
	 *
	 * @param   useCache        Use caching of image data if available.
	 *
	 * @return  Bump map image;
	 *          <code>null</code> if no color map was defined or could be loaded.
 	 */
	public BufferedImage getBumpMapImage( final boolean useCache )
	{
		final String map = bumpMap;
		return TextTools.isNonEmpty( map ) ? useCache ? MapTools.getImage( map ) : MapTools.loadImage( map ) : null;
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
	public float getAmbientReflectivity()
	{
		return 0.3f * ambientColorRed + 0.59f * ambientColorGreen + 0.11f * ambientColorBlue;
	}

	/**
	 * Get combined diffuse reflectivity. This is based on the weighted average
	 * red, green, and blue diffuse reflectivity color components.
	 *
	 * @return  Combined diffuse reflectivity.
	 */
	public float getDiffuseReflectivity()
	{
		return 0.3f * diffuseColorRed + 0.59f * diffuseColorGreen + 0.11f * diffuseColorBlue;
	}

	/**
	 * Get combined specular reflectivity. This is based on the weighted average
	 * red, green, and blue specular reflectivity color components.
	 *
	 * @return  Combined specular reflectivity.
	 */
	public float getSpecularReflectivity()
	{
		return 0.3f * specularColorRed + 0.59f * specularColorGreen + 0.11f * specularColorBlue;
	}

	/**
	 * Get combined emission. This is based on the weighted average red, green,
	 * and blue emissive color components.
	 *
	 * @return  Combined emission.
	 */
	public float getEmission()
	{
		return 0.3f * emissiveColorRed + 0.59f * emissiveColorGreen + 0.11f * emissiveColorBlue;
	}

	/**
	 * Set ambient reflection color.
	 * <p>
	 * This determines the amount of reflected light from ambient sources
	 * (normally just 1). This value may range from almost 0 for objects
	 * that absorb most ambient light to near 1 for objects that are highly
	 * reflective. Typical values range from 0.1 to 0.2 for dull surfaces
	 * and 0,7 to 0,8 for bright surfaces.
	 *
	 * @param   color   Ambient reflection color.
	 */
	public void setAmbientColor( final Color color )
	{
		final float[] components = color.getColorComponents( null );
		ambientColorRed   = components[ 0 ];
		ambientColorGreen = components[ 1 ];
		ambientColorBlue  = components[ 2 ];
	}

	/**
	 * Set ambient reflection color.
	 * <p>
	 * This determines the amount of reflected light from ambient sources
	 * (normally just 1). This value may range from almost 0 for objects
	 * that absorb most ambient light to near 1 for objects that are highly
	 * reflective. Typical values range from 0.1 to 0.2 for dull surfaces
	 * and 0,7 to 0,8 for bright surfaces.
	 *
	 * @param   rgb     Ambient reflection color.
	 */
	public void setAmbientColor( final int rgb )
	{
		ambientColorRed   = (float)( ( rgb >> 16 ) & 0xFF ) / 255.0f;
		ambientColorGreen = (float)( ( rgb >>  8 ) & 0xFF ) / 255.0f;
		ambientColorBlue  = (float)(   rgb         & 0xFF ) / 255.0f;
	}

	/**
	 * Set diffuse reflection color and opacity.
	 * <p>
	 * This determines the amount of reflected light from diffuse sources.
	 * This value may range from almost 0 for objects that absorb most
	 * diffuse light to near 1 for objects that are highly reflective.
	 * Typical values range from 0.1 to 0.2 for dull surfaces and 0.7 to
	 * 0.8 for bright surfaces.
	 *
	 * @param   color   Diffuse reflection color and opacity.
	 */
	public void setDiffuseColor( final Color color )
	{
		final float[] components = color.getRGBComponents( null );
		diffuseColorRed   = components[ 0 ];
		diffuseColorGreen = components[ 1 ];
		diffuseColorBlue  = components[ 2 ];
		diffuseColorAlpha = components[ 3 ];
	}

	/**
	 * Set diffuse reflection color and opacity.
	 * <p>
	 * This determines the amount of reflected light from diffuse sources.
	 * This value may range from almost 0 for objects that absorb most
	 * diffuse light to near 1 for objects that are highly reflective.
	 * Typical values range from 0.1 to 0.2 for dull surfaces and 0.7 to
	 * 0.8 for bright surfaces.
	 *
	 * @param   argb    Diffuse reflection color and opacity.
	 */
	public void setDiffuseColor( final int argb )
	{
		final int iAlpha =         ( ( argb >> 24 ) & 0xFF );
		diffuseColorRed   = (float)( ( argb >> 16 ) & 0xFF ) / 255.0f;
		diffuseColorGreen = (float)( ( argb >>  8 ) & 0xFF ) / 255.0f;
		diffuseColorBlue  = (float)(   argb         & 0xFF ) / 255.0f;
		diffuseColorAlpha = ( iAlpha < 255 ) ? ( (float)iAlpha / 255.0f ) : 1.0f;
	}

	/**
	 * Set specular reflection color.
	 * <p>
	 * Specular reflection is total or near total reflection of incoming
	 * light in a concentrated region. It can be used to create highlights
	 * on shiny surfaces.
	 *
	 * @param   color   Specular reflection color.
	 */
	public void setSpecularColor( final Color color )
	{
		final float[] components = color.getColorComponents( null );
		specularColorRed   = components[ 0 ];
		specularColorGreen = components[ 1 ];
		specularColorBlue  = components[ 2 ];
	}


	/**
	 * Set specular reflection color.
	 * <p>
	 * Specular reflection is total or near total reflection of incoming
	 * light in a concentrated region. It can be used to create highlights
	 * on shiny surfaces.
	 *
	 * @param   rgb     Specular reflection color.
	 */
	public void setSpecularColor( final int rgb )
	{
		specularColorRed   = (float)( ( rgb >> 16 ) & 0xFF ) / 255.0f;
		specularColorGreen = (float)( ( rgb >>  8 ) & 0xFF ) / 255.0f;
		specularColorBlue  = (float)(   rgb         & 0xFF ) / 255.0f;
	}


	/**
	 * Set emissive color.
	 * <p>
	 * This determines the amount of light emitted by this material.
	 * Note that this automatically implies a light source.
	 *
	 * @param   color   Emissive color.
	 */
	public void setEmissiveColor( final Color color )
	{
		final float[] components = color.getColorComponents( null );
		emissiveColorRed   = components[ 0 ];
		emissiveColorGreen = components[ 1 ];
		emissiveColorBlue  = components[ 2 ];
	}

	/**
	 * Set emissive color.
	 * <p>
	 * This determines the amount of light emitted by this material.
	 * Note that this automatically implies a light source.
	 *
	 * @param   rgb     Emissive color.
	 */
	public void setEmissiveColor( final int rgb )
	{
		emissiveColorRed   = (float)( ( rgb >> 16 ) & 0xFF ) / 255.0f;
		emissiveColorGreen = (float)( ( rgb >>  8 ) & 0xFF ) / 255.0f;
		emissiveColorBlue  = (float)(   rgb         & 0xFF ) / 255.0f;
	}
}
