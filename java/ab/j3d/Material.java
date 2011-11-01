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
package ab.j3d;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;

import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a material to be using in a 3D environment.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Material
	implements Appearance, Serializable
{
	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -1247188004008064101L;

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
		"  `reflectionMap` varchar(64) default NULL,\n" +
		"  `reflectionMin` float NOT NULL,\n" +
		"  `reflectionMax` float NOT NULL,\n" +
		"  `reflectionRed` float NOT NULL,\n" +
		"  `reflectionGreen` float NOT NULL,\n" +
		"  `reflectionBlue` float NOT NULL,\n" +
		"  PRIMARY KEY  (`ID`),\n" +
		"  UNIQUE KEY `code` (`code`)\n" +
		");";

	/**
	 * Map path prefix from where material map images are loaded.
	 */
	public static URL imagesDirectoryUrl = null;

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
	 *
	 * @see     #getAmbientColorRed
	 */
	public float ambientColorRed;

	/**
	 * Green component of ambient reflection color.
	 *
	 * @see     #getAmbientColorGreen
	 */
	public float ambientColorGreen;

	/**
	 * Blue component of ambient reflection color.
	 *
	 * @see     #getAmbientColorBlue
	 */
	public float ambientColorBlue;

	/**
	 * Red component of diffuse reflection color.
	 *
	 * @see     #getDiffuseColorRed
	 */
	public float diffuseColorRed;

	/**
	 * Green component of diffuse reflection color.
	 *
	 * @see     #getDiffuseColorGreen
	 */
	public float diffuseColorGreen;

	/**
	 * Blue component of diffuse reflection color.
	 *
	 * @see     #diffuseColorRed
	 */
	public float diffuseColorBlue;

	/**
	 * Opacity.
	 *
	 * @see     #getDiffuseColorAlpha
	 */
	public float diffuseColorAlpha;

	/**
	 * Red component of specular highlight color.
	 *
	 * @see     #getSpecularColorRed
	 */
	public float specularColorRed;

	/**
	 * Green component of specular highlight color.
	 *
	 * @see     #getSpecularColorGreen
	 */
	public float specularColorGreen;

	/**
	 * Blue component of specular highlight color.
	 *
	 * @see     #getSpecularColorBlue
	 */
	public float specularColorBlue;

	/**
	 * Specular highlight exponent.
	 *
	 * @see     #getShininess
	 */
	public int shininess;

	/**
	 * Red component of emissive color.
	 *
	 * @see     #getEmissiveColorRed
	 */
	public float emissiveColorRed;

	/**
	 * Green component of emissive reflection color.
	 *
	 * @see     #getEmissiveColorGreen
	 */
	public float emissiveColorGreen;

	/**
	 * Blue component of emissive reflection color.
	 *
	 * @see     #getEmissiveColorBlue
	 */
	public float emissiveColorBlue;

	/**
	 * Name of color map to use.
	 *
	 * @see     #getColorMap
	 */
	public String colorMap;

	/**
	 * Width of color map in meters.
	 *
	 * @see     #getColorMap
	 */
	public float colorMapWidth;

	/**
	 * Height of color map in meters.
	 *
	 * @see     #getColorMap
	 */
	public float colorMapHeight;

	/**
	 * Name of bump map to use.
	 *
	 * @see     #getBumpMap
	 */
	public String bumpMap;

	/**
	 * Width of bump map in meters.
	 *
	 * @see     #getBumpMap
	 */
	public float bumpMapWidth;

	/**
	 * Height of bump map in meters.
	 *
	 * @see     #getBumpMap
	 */
	public float bumpMapHeight;

	/**
	 * Flag to indicate that this material's texture has a 'grain'. If so,
	 * it is important how the material is oriented.
	 */
	public boolean grain;

	/**
	 * Name of the reflection map.
	 *
	 * @see     #getReflectionMap
	 */
	public String reflectionMap;

	/**
	 * Reflectivity of the material when viewed parallel to its normal.
	 *
	 * @see     #getReflectionMap
	 */
	public float reflectionMin;

	/**
	 * Reflectivity of the material when viewed perpendicular to its normal.
	 *
	 * @see     #getReflectionMap
	 */
	public float reflectionMax;

	/**
	 * Intensity of the red-component of (specular) reflections.
	 *
	 * @see     #getReflectionMap
	 */
	public float reflectionRed;

	/**
	 * Intensity of the green-component of (specular) reflections.
	 *
	 * @see     #getReflectionMap
	 */
	public float reflectionGreen;

	/**
	 * Intensity of the blue-component of (specular) reflections.
	 *
	 * @see     #getReflectionMap
	 */
	public float reflectionBlue;

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
		reflectionMap      = null;
		reflectionMin      = 0.0f;
		reflectionMax      = 0.0f;
		reflectionRed      = 1.0f;
		reflectionGreen    = 1.0f;
		reflectionBlue     = 1.0f;
		grain              = false;
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
		reflectionMap      = original.reflectionMap;
		reflectionMin      = original.reflectionMin;
		reflectionMax      = original.reflectionMax;
		reflectionRed      = original.reflectionRed;
		reflectionGreen    = original.reflectionGreen;
		reflectionBlue     = original.reflectionBlue;
		grain              = original.grain;
	}

	/**
	 * Construct texture for ARGB value.
	 *
	 * @param   argb    ARGB color specification.
	 */
	public Material( final int argb )
	{
		this();
		final Color4f color = new Color4f( argb );
		setAmbientColor( color );
		setDiffuseColor( color );
	}

	/**
	 * Construct texture for ARGB value.
	 *
	 * @param   color   Color to build appearance for.
	 */
	public Material( final Color4 color )
	{
		this();
		setAmbientColor( color );
		setDiffuseColor( color );
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
	public Material( final String code, final float ambientColorRed, final float ambientColorGreen, final float ambientColorBlue, final float diffuseColorRed, final float diffuseColorGreen, final float diffuseColorBlue, final float diffuseColorAlpha, final float specularColorRed, final float specularColorGreen, final float specularColorBlue, final int shininess, final float emissiveColorRed, final float emissiveColorGreen, final float emissiveColorBlue, final String colorMap, final float colorMapWidth, final float colorMapHeight, final boolean grain )
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
		reflectionMap           = null;
		reflectionMin           = 0.0f;
		reflectionMax           = 0.0f;
		reflectionRed           = 0.0f;
		reflectionGreen         = 0.0f;
		reflectionBlue          = 0.0f;
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
	 * @param   bumpMap             Name of bump map to use.
	 * @param   bumpMapWidth        Width of bump map in meters.
	 * @param   bumpMapHeight       Height of bump map in meters.
	 * @param   grain               Flag to indicate that material has a 'grain'.
	 * @param   reflectionMap       Name of the reflection map to use for real-time reflections.
	 * @param   reflectionMin       Reflectivity of the material when viewed parallel to its normal.
	 * @param   reflectionMax       Reflectivity of the material when viewed perpendicular to its normal.
	 * @param   reflectionRed       Intensity of the red-component of (specular) reflections.
	 * @param   reflectionGreen     Intensity of the green-component of (specular) reflections.
	 * @param   reflectionBlue      Intensity of the blue-component of (specular) reflections.
	 */
	public Material( final String code, final float ambientColorRed, final float ambientColorGreen, final float ambientColorBlue, final float diffuseColorRed, final float diffuseColorGreen, final float diffuseColorBlue, final float diffuseColorAlpha, final float specularColorRed, final float specularColorGreen, final float specularColorBlue, final int shininess, final float emissiveColorRed, final float emissiveColorGreen, final float emissiveColorBlue, final String colorMap, final float colorMapWidth, final float colorMapHeight, final String bumpMap, final float bumpMapWidth, final float bumpMapHeight, final boolean grain, final String reflectionMap, final float reflectionMin, final float reflectionMax, final float reflectionRed, final float reflectionGreen, final float reflectionBlue )
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
		this.bumpMap            = bumpMap;
		this.bumpMapWidth       = bumpMapWidth;
		this.bumpMapHeight      = bumpMapHeight;
		this.grain              = grain;
		this.reflectionMap      = reflectionMap;
		this.reflectionMin      = reflectionMin;
		this.reflectionMax      = reflectionMax;
		this.reflectionRed      = reflectionRed;
		this.reflectionGreen    = reflectionGreen;
		this.reflectionBlue     = reflectionBlue;
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

	public float getAmbientColorBlue()
	{
		return ambientColorBlue;
	}

	public float getAmbientColorRed()
	{
		return ambientColorRed;
	}

	public float getAmbientColorGreen()
	{
		return ambientColorGreen;
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
	public void setAmbientColor( final Color4 color )
	{
		ambientColorRed = color.getRedFloat();
		ambientColorGreen = color.getGreenFloat();
		ambientColorBlue = color.getBlueFloat();
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

	public float getDiffuseColorRed()
	{
		return diffuseColorRed;
	}

	public float getDiffuseColorGreen()
	{
		return diffuseColorGreen;
	}

	public float getDiffuseColorBlue()
	{
		return diffuseColorBlue;
	}

	public float getDiffuseColorAlpha()
	{
		return diffuseColorAlpha;
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
	 * @param   color   Diffuse reflection color and opacity.
	 */
	public void setDiffuseColor( final Color4 color )
	{
		diffuseColorRed = color.getRedFloat();
		diffuseColorGreen = color.getGreenFloat();
		diffuseColorBlue = color.getBlueFloat();
		diffuseColorAlpha = color.getAlphaFloat();
	}

	public float getSpecularColorRed()
	{
		return specularColorRed;
	}

	public float getSpecularColorGreen()
	{
		return specularColorGreen;
	}

	public float getSpecularColorBlue()
	{
		return specularColorBlue;
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
	 * @param   color   Specular reflection color.
	 */
	public void setSpecularColor( final Color4 color )
	{
		specularColorRed = color.getRedFloat();
		specularColorGreen = color.getGreenFloat();
		specularColorBlue = color.getBlueFloat();
	}

	public int getShininess()
	{
		return shininess;
	}

	public float getEmissiveColorRed()
	{
		return emissiveColorRed;
	}

	public float getEmissiveColorGreen()
	{
		return emissiveColorGreen;
	}

	public float getEmissiveColorBlue()
	{
		return emissiveColorBlue;
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
	 * Set emissive color.
	 * <p>
	 * This determines the amount of light emitted by this material.
	 * Note that this automatically implies a light source.
	 *
	 * @param   color   Emissive color.
	 */
	public void setEmissiveColor( final Color4 color )
	{
		emissiveColorRed = color.getRedFloat();
		emissiveColorGreen = color.getGreenFloat();
		emissiveColorBlue  = color.getBlueFloat();
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

	public TextureMap getColorMap()
	{
		final String colorMap = this.colorMap;
		return ( ( colorMap == null ) || colorMap.isEmpty() ) ? null : new MaterialTextureMap( colorMap + ".jpg", colorMapWidth, colorMapHeight );
	}

	public TextureMap getBumpMap()
	{
		final String bumpMap = this.bumpMap;
		return ( ( bumpMap == null ) || bumpMap.isEmpty() ) ? null : new MaterialTextureMap( bumpMap + ".jpg", bumpMapWidth, bumpMapHeight );
	}

	public ReflectionMap getReflectionMap()
	{
		final String reflectionMap = this.reflectionMap;
		return ( ( reflectionMap == null ) || reflectionMap.isEmpty() ) ? null : new MaterialReflectionMap( reflectionMap + ".jpg", reflectionMin, reflectionMax, reflectionRed, reflectionGreen, reflectionBlue );
	}

	/**
	 * Sets the color of (specular) reflections. For metallic materials, this
	 * is typically the color of the metal. For other materials, it is typically
	 * white, i.e. no change in color.
	 *
	 * @param   color   Reflection color.
	 */
	public void setReflectionColor( final Color4 color )
	{
		reflectionRed = color.getRedFloat();
		reflectionGreen = color.getGreenFloat();
		reflectionBlue  = color.getBlueFloat();
	}

	/**
	 * Sets the color of (specular) reflections. For metallic materials, this
	 * is typically the color of the metal. For other materials, it is typically
	 * white, i.e. no change in color.
	 *
	 * @param   color   Reflection color.
	 */
	public void setReflectionColor( final Color color )
	{
		final float[] components = color.getColorComponents( null );
		reflectionRed   = components[ 0 ];
		reflectionGreen = components[ 1 ];
		reflectionBlue  = components[ 2 ];
	}

	/**
	 * Implementation of {@link TextureMap} based on color map properties of a
	 * {@link Material}.
	 */
	protected static class MaterialTextureMap
		extends AbstractTextureMap
	{
		/**
		 * Path to texture map image.
		 */
		@Nullable
		protected final String _path;

		/**
		 * Constructs a new texture map.
		 *
		 * @param   path            Path to texture map image.
		 * @param   physicalWidth   Physical width of the texture, in meters.
		 * @param   physicalHeight  Physical height of the texture, in meters.
		 */
		protected MaterialTextureMap( @Nullable final String path, final float physicalWidth, final float physicalHeight )
		{
			super( physicalWidth, physicalHeight );
			_path = path;
		}

		public URL getImageUrl()
		{
			URL result = null;

			final String map = _path;
			if ( ( map != null ) && !map.isEmpty() )
			{
				result = getGlobalImageUrl( map );
			}

			return result;
		}

		@Nullable
		public BufferedImage loadImage()
			throws IOException
		{
			final URL imageUrl = getImageUrl();
			return ( imageUrl != null ) ? ImageIO.read( imageUrl ) : null;
		}

		@Override
		public boolean equals( final Object object )
		{
			final boolean result;
			if ( object == this )
			{
				result = true;
			}
			else if ( object instanceof MaterialTextureMap )
			{
				final MaterialTextureMap other = (MaterialTextureMap)object;
				result = super.equals( object ) && ( ( _path == null ) ? ( other._path == null ) : _path.equals( other._path ) );
			}
			else
			{
				result = false;
			}
			return result;
		}

		@Override
		public int hashCode()
		{
			return super.hashCode() ^ ( ( _path == null ) ? 0 : _path.hashCode() );
		}
	}

	/**
	 * Implementation of {@link ReflectionMap} based on reflection map
	 * properties of a {@link Material}.
	 */
	private static class MaterialReflectionMap
		extends SingleImageCubeMap
		implements ReflectionMap
	{
		/**
		 * Name of the reflection map.
		 */
		@Nullable
		private final String _reflectionMap;

		/**
		 * Reflectivity of the material when viewed parallel to its normal.
		 */
		private final float _reflectionMin;

		/**
		 * Reflectivity of the material when viewed perpendicular to its normal.
		 */
		private final float _reflectionMax;

		/**
		 * Intensity of the red-component of (specular) reflections.
		 */
		private final float _reflectionRed;

		/**
		 * Intensity of the green-component of (specular) reflections.
		 */
		private final float _reflectionGreen;

		/**
		 * Intensity of the blue-component of (specular) reflections.
		 */
		private final float _reflectionBlue;

		/**
		 * Constructs a new reflection map.
		 *
		 * @param   reflectionMap       Reflection map.
		 * @param   reflectionMin       Reflectivity of the material when viewed parallel to its normal.
		 * @param   reflectionMax       Reflectivity of the material when viewed perpendicular to its normal.
		 * @param   reflectionRed       Intensity of the red-component of (specular) reflections.
		 * @param   reflectionGreen     Intensity of the green-component of (specular) reflections.
		 * @param   reflectionBlue      Intensity of the blue-component of (specular) reflections.
		 */
		private MaterialReflectionMap( @Nullable final String reflectionMap, final float reflectionMin, final float reflectionMax, final float reflectionRed, final float reflectionGreen, final float reflectionBlue )
		{
			_reflectionMap = reflectionMap;
			_reflectionMin = reflectionMin;
			_reflectionMax = reflectionMax;
			_reflectionRed = reflectionRed;
			_reflectionGreen = reflectionGreen;
			_reflectionBlue = reflectionBlue;
		}

		public float getReflectivityMin()
		{
			return _reflectionMin;
		}

		public float getReflectivityMax()
		{
			return _reflectionMax;
		}

		public float getIntensityRed()
		{
			return _reflectionRed;
		}

		public float getIntensityGreen()
		{
			return _reflectionGreen;
		}

		public float getIntensityBlue()
		{
			return _reflectionBlue;
		}

		@Nullable
		@Override
		public BufferedImage getImage()
		{
			BufferedImage image = super.getImage();
			final String reflectionMap = _reflectionMap;

			if ( ( image == null ) && ( reflectionMap != null ) && !reflectionMap.isEmpty() )
			{
				try
				{
					final URL imageUrl = getGlobalImageUrl( reflectionMap );

					image = ImageIO.read( imageUrl );
					setImage( image );
				}
				catch ( IOException e )
				{
					/* should we prevent loading this image again? */
					e.printStackTrace();
				}
			}

			return image;
		}

		@Override
		public boolean equals( final Object other )
		{
			final boolean result;
			if ( other == this )
			{
				result = true;
			}
			else if ( other instanceof MaterialReflectionMap )
			{
				final MaterialReflectionMap map = (MaterialReflectionMap)other;
				result = ( _reflectionMin == map._reflectionMin ) &&
				         ( _reflectionMax == map._reflectionMax ) &&
				         ( _reflectionRed == map._reflectionRed ) &&
				         ( _reflectionGreen == map._reflectionGreen ) &&
				         ( _reflectionBlue == map._reflectionBlue ) &&
				         ( ( _reflectionMap == null ) ? ( map._reflectionMap == null ) : _reflectionMap.equals( map._reflectionMap ) );
			}
			else
			{
				result = false;
			}
			return result;
		}

		@Override
		public int hashCode()
		{
			return ( ( _reflectionMap == null ) ? 0 : _reflectionMap.hashCode() ) ^
			       Float.floatToRawIntBits( _reflectionMin ) ^
			       Float.floatToRawIntBits( _reflectionMax ) ^
			       Float.floatToRawIntBits( _reflectionRed ) ^
			       Float.floatToRawIntBits( _reflectionGreen ) ^
			       Float.floatToRawIntBits( _reflectionBlue );
		}
	}

	/**
	 * Get path to map image.
	 *
	 * @param   path     Name of map (<code>null</code> or empty strings allowed).
	 *
	 * @return  Map image;
	 *          <code>null</code> if map has no image or the image could not be loaded.
	 *
	 * @deprecated Images used by materials should contain this information. Ugly!
 	 */
	private static URL getGlobalImageUrl( final String path )
	{
		if ( imagesDirectoryUrl == null )
		{
			throw new AssertionError( "baseUrl -must- be set already" );
		}

		try
		{
			return new URL( imagesDirectoryUrl, path );
		}
		catch ( MalformedURLException e )
		{
			throw new IllegalArgumentException( "Bad URL spec: " + path );
		}
	}

}
