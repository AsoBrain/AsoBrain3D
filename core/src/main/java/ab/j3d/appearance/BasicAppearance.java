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
 * Basic implementation of {@link Appearance} interface. This simply provides a
 * mutable version for each property defined by the {@link Appearance}
 * interface.
 *
 * @author  Peter S. Heijnen
 */
public class BasicAppearance
	implements Appearance
{
	/**
	 * Custom properties of the appearance.
	 */
	private final Map<String, Object> _properties = new HashMap<>();

	/**
	 * Name of the appearance.
	 */
	@Nullable
	private String _name;

	/**
	 * Red component of ambient reflection color.
	 */
	@NotNull
	private Color4 _ambientColor = Color4.BLACK;

	/**
	 * Red component of diffuse reflection color.
	 */
	@NotNull
	private Color4 _diffuseColor = Color4.BLACK;

	/**
	 * Specular highlight exponent.
	 */
	private int _shininess = 0;

	/**
	 * Red component of specular highlight color.
	 */
	@NotNull
	private Color4 _specularColor = Color4.BLACK;

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
	private CubeMap _reflectionMap = null;

	/**
	 * Reflectivity of the material when viewed parallel to its normal.
	 */
	private float _reflectionMin = 0.0f;

	/**
	 * Reflectivity of the material when viewed perpendicular to its normal.
	 */
	private float _reflectionMax = 1.0f;

	/**
	 * Red component of emissive color.
	 */
	@NotNull
	private Color4 _emissiveColor = Color4.BLACK;

	/**
	 * Reflection color/intensity of (specular) reflections.
	 */
	@NotNull
	private Color4 _reflectionColor = Color4.WHITE;

	/**
	 * Create {@link BasicAppearance} for a solid color.
	 *
	 * @param color Color to create appearance for.
	 *
	 * @return {@link BasicAppearance}.
	 */
	public static BasicAppearance createForColor( @NotNull final Color4 color )
	{
		return createForColor( null, color );
	}

	/**
	 * Create {@link BasicAppearance} for a solid color.
	 *
	 * @param name  Name of the appearance.
	 * @param color Color to create appearance for.
	 *
	 * @return {@link BasicAppearance}.
	 */
	public static BasicAppearance createForColor( @Nullable final String name, @NotNull final Color4 color )
	{
		final BasicAppearance result = new BasicAppearance( name );
		result.setAmbientColor( color );
		result.setDiffuseColor( color );
		result.setSpecularColor( Color4.WHITE );
		result.setShininess( 16 );
		return result;
	}

	/**
	 * Constructs a new appearance.
	 */
	public BasicAppearance()
	{
		this( (String)null );
	}

	/**
	 * Constructs a new appearance.
	 *
	 * @param name Name of the appearance.
	 */
	public BasicAppearance( @Nullable final String name )
	{
		_name = name;
	}

	/**
	 * Constructs a copy of new appearance.
	 *
	 * @param original Appearance to copy.
	 */
	public BasicAppearance( @NotNull final Appearance original )
	{
		_name = original.getName();
		_ambientColor = original.getAmbientColor();
		_diffuseColor = original.getDiffuseColor();
		_specularColor = original.getSpecularColor();
		_shininess = original.getShininess();
		_emissiveColor = original.getEmissiveColor();
		_colorMap = original.getColorMap();
		_bumpMap = original.getBumpMap();
		_reflectionMap = original.getReflectionMap();
		_reflectionMin = original.getReflectionMin();
		_reflectionMax = original.getReflectionMax();
		_reflectionColor = original.getReflectionColor();
	}

	@Override
	@Nullable
	public String getName()
	{
		return _name;
	}

	public void setName( @Nullable final String name )
	{
		_name = name;
	}

	@Override
	public @NotNull Color4 getAmbientColor()
	{
		return _ambientColor;
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
	 * @param   red     Red intensity.
	 * @param   green   Green intensity.
	 * @param   blue    Blue intensity.
	 */
	public void setAmbientColor( final float red, final float green, final float blue )
	{
		setAmbientColor( new Color4f( red, green, blue ) );
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
	public void setAmbientColor( final @NotNull Color4 color )
	{
		_ambientColor = color;
	}

	@Override
	public @NotNull Color4 getDiffuseColor()
	{
		return _diffuseColor;
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
	 * @param   red     Red intensity.
	 * @param   green   Green intensity.
	 * @param   blue    Blue intensity.
	 * @param   alpha   Alpha value.
	 */
	public void setDiffuseColor( final float red, final float green, final float blue, final float alpha )
	{
		setDiffuseColor( new Color4f( red, green, blue, alpha ) );
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
	public void setDiffuseColor( final @NotNull Color4 color )
	{
		_diffuseColor = color;
	}

	@Override
	public @NotNull Color4 getSpecularColor()
	{
		return _specularColor;
	}

	/**
	 * Set specular reflection color.
	 * <p>
	 * Specular reflection is total or near total reflection of incoming
	 * light in a concentrated region. It can be used to create highlights
	 * on shiny surfaces.
	 *
	 * @param   red     Red intensity.
	 * @param   green   Green intensity.
	 * @param   blue    Blue intensity.
	 */
	public void setSpecularColor( final float red, final float green, final float blue )
	{
		setSpecularColor( new Color4f( red, green, blue ) );
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
	public void setSpecularColor( final @NotNull Color4 color )
	{
		_specularColor = color;
	}

	@Override
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

	@Override
	public @NotNull Color4 getEmissiveColor()
	{
		return _emissiveColor;
	}

	/**
	 * Set emissive color.
	 * <p>
	 * This determines the amount of light emitted by this material.
	 * Note that this automatically implies a light source.
	 *
	 * @param   red     Red intensity.
	 * @param   green   Green intensity.
	 * @param   blue    Blue intensity.
	 */
	public void setEmissiveColor( final float red, final float green, final float blue )
	{
		setEmissiveColor( new Color4f( red, green, blue ) );
	}

	/**
	 * Set emissive color.
	 * <p>
	 * This determines the amount of light emitted by this material.
	 * Note that this automatically implies a light source.
	 *
	 * @param   color   Emissive color.
	 */
	public void setEmissiveColor( final @NotNull Color4 color )
	{
		_emissiveColor = color;
	}

	@Override
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

	@Override
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

	@Override
	@Nullable
	public CubeMap getReflectionMap()
	{
		return _reflectionMap;
	}

	/**
	 * Map to use for reflections.
	 *
	 * @param   map     Reflection map; <code>null</code> if none.
	 */
	public void setReflectionMap( @Nullable final CubeMap map )
	{
		_reflectionMap = map;
	}


	@Override
	public float getReflectionMin()
	{
		return _reflectionMin;
	}

	/**
	 * Get reflectivity of the material when viewed parallel to its normal.
	 *
	 * @param   reflectivity    Reflectivity if view is parallel to normal.
	 */
	public void setReflectionMin( final float reflectivity )
	{
		_reflectionMin = reflectivity;
	}

	@Override
	public float getReflectionMax()
	{
		return _reflectionMax;
	}

	/**
	 * Get reflectivity of the material when viewed perpendicular to its normal.
	 *
	 * @param   reflectivity    Reflectivity if view is perpendicular to normal.
	 */
	public void setReflectionMax( final float reflectivity )
	{
		_reflectionMax = reflectivity;
	}

	@Override
	public @NotNull Color4 getReflectionColor()
	{
		return _reflectionColor;
	}

	/**
	 * Set reflection color/intensity of (specular) reflections.
	 *
	 * @param   red     Red intensity.
	 * @param   green   Green intensity.
	 * @param   blue    Blue intensity.
	 */
	public void setReflectionColor( final float red, final float green, final float blue )
	{
		setReflectionColor( new Color4f( red, green, blue ) );
	}

	/**
	 * Set reflection color/intensity of (specular) reflections.
	 *
	 * @param   color     Reflection color/intensity.
	 */
	public void setReflectionColor( final @NotNull Color4 color )
	{
		_reflectionColor = color;
	}

	@Override
	@NotNull
	public Map<String, Object> getProperties()
	{
		return Collections.unmodifiableMap( _properties );
	}

	public void setProperties( @NotNull final Map<String, Object> properties )
	{
		_properties.clear();
		_properties.putAll( properties );
	}

	@Override
	public void setProperty( @NotNull final String key, @Nullable final Object value )
	{
		if ( value == null )
		{
			_properties.remove( key );
		}
		else
		{
			_properties.put( key, value );
		}
	}
}
