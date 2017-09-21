/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.model;

/**
 * A point light emits lights in all directions equally from a single point.
 * Point lights are an efficient means to model many common light sources, such
 * as light bulbs.
 *
 * <p> Light intensity is specified using float values for the three color
 * components (red, green and blue). Intensity values start at {@code 0.0}, or
 * total darkness (i.e. no light is emitted). At an intensity of {@code 1.0}, an
 * unattenuated light illuminating a surface, with full diffuse reflectivity and
 * its normal parallel to the light direction, would be just bright enough such
 * that the exact diffuse color of the surface is perceived. There is no upper
 * bound to light intensity, except for the constraints imposed by the {@code
 * float} data type.
 *
 * <p> Light attenuation defines how the light fades over distance. The
 * attenuation at a given distance ({@code d}) is calculated from the constant
 * ({@code c}), linear ({@code l}) and quadratic ({@code q}) attenuation
 * factors, as show below.
 *
 * <pre>
 *                    1
 * attenuation = ------------
 *               c + ld + qd²
 * </pre>
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "OverridableMethodCallDuringObjectConstruction", "WeakerAccess" } )
public class Light3D
extends Node3D
{
	/**
	 * Diffuse light intensity (red component).
	 */
	private float _diffuseColorRed = 0;

	/**
	 * Diffuse light intensity (green component).
	 */
	private float _diffuseColorGreen = 0;

	/**
	 * Diffuse light intensity (blue component).
	 */
	private float _diffuseColorBlue = 0;

	/**
	 * Specular light intensity (red component).
	 */
	private float _specularColorRed = 0;

	/**
	 * Specular light intensity (green component).
	 */
	private float _specularColorGreen = 0;

	/**
	 * Specular light intensity (blue component).
	 */
	private float _specularColorBlue = 0;

	/**
	 * Constant attenuation factor.
	 */
	private float _constantAttenuation = 0;

	/**
	 * Linear attenuation factor.
	 */
	private float _linearAttenuation = 0;

	/**
	 * Quadratic attenuation factor.
	 */
	private float _quadraticAttenuation = 0;

	/**
	 * Whether the light casts shadows.
	 */
	private boolean _castingShadows = false;

	/**
	 * Constructs a light with an intensity of {@code 1.0} with quadratic
	 * attenuation.
	 */
	public Light3D()
	{
		setIntensity( 1 );
		setAttenuation( 0, 0, 1 );
	}

	/**
	 * Constructs a white light with the specified intensity and fall-off
	 * distance.
	 *
	 * <p> Light intensity is specified here as a non-negative integer, with
	 * values from {@code 0} to {@code 255} being mapped to light intensities
	 * ranging from {@code 0.0} to {@code 1.0} (see {@link Light3D}), while
	 * greater values may be used for even brighter lights. The fall-off
	 * distance specifies the distance at which the light reaches half its
	 * specified intensity.
	 *
	 * @param intensity Intensity of the light.
	 * @param fallOff   Fall-off distance.
	 *
	 * @deprecated Use {@link #Light3D(float, float)}.
	 */
	@Deprecated
	public Light3D( final int intensity, final double fallOff )
	{
		setIntensity( intensity / 255.0f );
		setFallOff( fallOff );
	}

	/**
	 * Constructs a white light with the specified intensity and fall-off
	 * distance.
	 *
	 * @param intensity Intensity of the light.
	 * @param fallOff   Fall-off distance.
	 */
	public Light3D( final float intensity, final float fallOff )
	{
		setIntensity( intensity );
		setFallOff( fallOff );
	}

	public float getDiffuseRed()
	{
		return _diffuseColorRed;
	}

	public float getDiffuseGreen()
	{
		return _diffuseColorGreen;
	}

	public float getDiffuseBlue()
	{
		return _diffuseColorBlue;
	}

	/**
	 * Sets the intensity of the light for diffuse reflection.
	 *
	 * @param intensity Intensity of diffuse reflection.
	 */
	public void setDiffuse( final float intensity )
	{
		if ( intensity < 0 )
		{
			throw new IllegalArgumentException( "intensity:" + intensity );
		}

		setDiffuse( intensity, intensity, intensity );
	}

	/**
	 * Sets the intensity of the light for diffuse reflection.
	 *
	 * @param red   Intensity of the red color component.
	 * @param green Intensity of the green color component.
	 * @param blue  Intensity of the blue color component.
	 */
	public void setDiffuse( final float red, final float green, final float blue )
	{
		if ( ( red < 0 ) || ( green < 0 ) || ( blue < 0 ) )
		{
			throw new IllegalArgumentException( "red: " + red + ", green:" + green + ", blue:" + blue );
		}

		_diffuseColorRed = red;
		_diffuseColorGreen = green;
		_diffuseColorBlue = blue;
	}

	public float getSpecularRed()
	{
		return _specularColorRed;
	}

	public float getSpecularGreen()
	{
		return _specularColorGreen;
	}

	public float getSpecularBlue()
	{
		return _specularColorBlue;
	}

	/**
	 * Sets the intensity of the light for specular reflection.
	 *
	 * @param intensity Intensity of specular reflection.
	 */
	public void setSpecular( final float intensity )
	{
		if ( intensity < 0 )
		{
			throw new IllegalArgumentException( "intensity:" + intensity );
		}

		setSpecular( intensity, intensity, intensity );
	}

	/**
	 * Sets the intensity of the light for specular reflection.
	 *
	 * @param red   Intensity of the red color component.
	 * @param green Intensity of the green color component.
	 * @param blue  Intensity of the blue color component.
	 */
	public void setSpecular( final float red, final float green, final float blue )
	{
		if ( ( red < 0 ) || ( green < 0 ) || ( blue < 0 ) )
		{
			throw new IllegalArgumentException( "red: " + red + ", green:" + green + ", blue:" + blue );
		}

		_specularColorRed = Math.max( 0, red );
		_specularColorGreen = Math.max( 0, green );
		_specularColorBlue = Math.max( 0, blue );
	}

	public float getConstantAttenuation()
	{
		return _constantAttenuation;
	}

	public float getLinearAttenuation()
	{
		return _linearAttenuation;
	}

	public float getQuadraticAttenuation()
	{
		return _quadraticAttenuation;
	}

	/**
	 * Sets the light attenuation factors.
	 *
	 * @param constantFactor  Constant light attenuation factor.
	 * @param linearFactor    Linear light attenuation factor.
	 * @param quadraticFactor Quadratic light attenuation factor.
	 */
	public void setAttenuation( final float constantFactor, final float linearFactor, final float quadraticFactor )
	{
		_constantAttenuation = constantFactor;
		_linearAttenuation = linearFactor;
		_quadraticAttenuation = quadraticFactor;
	}

	/**
	 * Returns the distance where the light reaches half its specified
	 * intensity, i.e. where the light attenuation equals {@code 0.5}. For
	 * lights with only constant attenuation, this method always returns {@code
	 * 0.0}.
	 *
	 * @return Distance at which the light has half its specified intensity.
	 */
	public float getHalfIntensityDistance()
	{
		return getDistanceByIntensity( 0.5f );
	}

	/**
	 * Returns the distance where the light reaches its specified intensity,
	 * i.e. where the light attenuation equals {@code 1.0}. For lights with only
	 * constant attenuation, this method always returns {@code 0.0}.
	 *
	 * @return Distance at which the light has half its specified intensity.
	 */
	public float getFullIntensityDistance()
	{
		return getDistanceByIntensity( 1 );
	}

	/**
	 * Returns the distance where the light attenuation equals the specified
	 * intensity, relative to the light's specified intensity. For lights with
	 * only constant attenuation, this method always returns {@code 0.0}.
	 *
	 * @param relativeIntensity Relative light intensity, e.g. {@code 1.0} for
	 *                          the specified intensity of this light.
	 *
	 * @return Distance at which the specified light intensity is reached.
	 */
	private float getDistanceByIntensity( final float relativeIntensity )
	{
		final float quadratic = _quadraticAttenuation;
		final float linear = _linearAttenuation;
		final float constant = _constantAttenuation - ( 1 / relativeIntensity );

		/*
		 * Solve 'd' in the equation 'qd^2 + ld + c = 0.0', yielding the
		 * distance where the light reaches the specified relative intensity.
		 */
		final float result;
		if ( quadratic == 0 )
		{
			if ( linear == 0 )
			{
				result = 0;
			}
			else
			{
				result = -constant / linear;
			}
		}
		else
		{
			// Using the quadratic equation.
			result = ( -linear + (float)Math.sqrt( (double)( linear * linear - 4 * quadratic * constant ) ) ) / ( 2 * quadratic );
		}

		return result;
	}

	/**
	 * Returns the perceived (gray scale) intensity of the light. This value is
	 * based only on the diffuse intensity of the light.
	 *
	 * @return Light intensity.
	 */
	public float getIntensity()
	{
		return 0.3f * _diffuseColorRed + 0.59f * _diffuseColorGreen + 0.11f * _diffuseColorBlue;
	}

	/**
	 * Sets the intensity of the light, for both diffuse and specular reflection
	 * and for all color components, to the given value.
	 *
	 * @param intensity Light intensity.
	 */
	public void setIntensity( final float intensity )
	{
		if ( intensity < 0 )
		{
			throw new IllegalArgumentException( "intensity < 0.0: " + intensity );
		}

		setDiffuse( intensity, intensity, intensity );
		setSpecular( intensity, intensity, intensity );
	}

	/**
	 * Sets the intensity of the light, for both diffuse and specular
	 * reflection, to the given values.
	 *
	 * @param red   Intensity of the red color component.
	 * @param green Intensity of the green color component.
	 * @param blue  Intensity of the blue color component.
	 */
	public void setIntensity( final float red, final float green, final float blue )
	{
		if ( ( red < 0 ) || ( green < 0 ) || ( blue < 0 ) )
		{
			throw new IllegalArgumentException( "red: " + red + ", green:" + green + ", blue:" + blue );
		}

		setDiffuse( red, green, blue );
		setSpecular( red, green, blue );
	}

	/**
	 * Sets the fall-off distance of the light.
	 *
	 * @param fallOff Fall-off distance to be set.
	 *
	 * @see #setFallOff(float)
	 */
	public void setFallOff( final double fallOff )
	{
		setFallOff( (float)fallOff );
	}

	/**
	 * Sets the fall-off distance of the light. This is the distance at which
	 * the light reaches half its specified intensity. Such attenuation is
	 * realized by using the following attenuation factors:
	 * <pre>
	 *    constant  = 0.0
	 *    linear    = 0.0
	 *    quadratic = 2.0 / fallOff²
	 * </pre>
	 *
	 * <p> When set to {@code 0.0}, the light will not be attenuated.
	 *
	 * @param fallOff Fall-off distance to be set.
	 */
	public void setFallOff( final float fallOff )
	{
		if ( fallOff < 0 )
		{
			throw new IllegalArgumentException( "fallOff: " + fallOff );
		}

		if ( fallOff == 0 )
		{
			setAttenuation( 1, 0, 0 );
		}
		else
		{
			setAttenuation( 0, 0, 2 / ( fallOff * fallOff ) );
		}
	}

	public boolean isCastingShadows()
	{
		return _castingShadows;
	}

	public void setCastingShadows( final boolean castingShadows )
	{
		_castingShadows = castingShadows;
	}
}
