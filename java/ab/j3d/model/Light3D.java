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
package ab.j3d.model;

/**
 * A point light emits lights in all directions equally from a single point.
 * Point lights are an efficient means to model many common light sources, such
 * as light bulbs.
 *
 * <p>
 * Light intensity is specified using float values for the three color
 * components (red, green and blue). Intensity values start at <code>0.0</code>,
 * or total darkness (i.e. no light is emitted). At an intensity of
 * <code>1.0</code>, an unattenuated light illuminating a surface, with full
 * diffuse reflectivity and its normal parallel to the light direction, would be
 * just bright enough such that the exact diffuse color of the surface is
 * perceived. There is no upper bound to light intensity, except for the
 * contraints imposed by the <code>float</code> data type.
 *
 * <p>
 * Light attenuation defines how the light fades over distance. The attenuation
 * at a given distance (<code>d</code>) is calculated from the constant
 * (<code>c</code>), linear (<code>l</code>) and quadratic (<code>q</code>)
 * attenuation factors, as show below.

 * <pre>
 *                    1
 * attenuation = ------------
 *               c + ld + qd²
 * </pre>
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Light3D
	extends Node3D
{
	/**
	 * Diffuse light intensity (red component).
	 */
	private float _diffuseColorRed = 0.0f;

	/**
	 * Diffuse light intensity (green component).
	 */
	private float _diffuseColorGreen = 0.0f;

	/**
	 * Diffuse light intensity (blue component).
	 */
	private float _diffuseColorBlue = 0.0f;

	/**
	 * Specular light intensity (red component).
	 */
	private float _specularColorRed = 0.0f;

	/**
	 * Specular light intensity (green component).
	 */
	private float _specularColorGreen = 0.0f;

	/**
	 * Specular light intensity (blue component).
	 */
	private float _specularColorBlue = 0.0f;

	/**
	 * Constant attenuation factor.
	 */
	private float _constantAttenuation = 0.0f;

	/**
	 * Linear attenuation factor.
	 */
	private float _linearAttenuation = 0.0f;

	/**
	 * Quadratic attenuation factor.
	 */
	private float _quadraticAttenuation = 0.0f;

	/**
	 * Constructs a light with an intensity of <code>1.0</code> with quadratic
	 * attenuation.
	 */
	public Light3D()
	{
		setIntensity( 1.0f );
		setAttenuation( 0.0f , 0.0f , 1.0f );
	}

	/**
	 * Constructs a white light with the specified intensity and fall-off
	 * distance.
	 *
	 * <p>
	 * Light intensity is specified here as a non-negative integer, with values
	 * from <code>0</code> to <code>255</code> being mapped to light intensities
	 * ranging from <code>0.0</code> to <code>1.0</code> (see {@link Light3D}),
	 * while greater values may be used for even brighter lights. The fall-off
	 * distance specifies the distance at which the light reaches half its
	 * specified intensity.
	 *
	 * @param   intensity   Intensity of the light.
	 * @param   fallOff     Fall-off distance.
	 */
	public Light3D( final int intensity , final double fallOff )
	{
		if ( intensity < 0 )
		{
			throw new IllegalArgumentException( "intensity < 0: " + intensity );
		}

		if ( fallOff < 0.0 )
		{
			throw new IllegalArgumentException( "fallOff < 0.0: " + fallOff );
		}

		setIntensity( (float)intensity / 255.0f );
		setFallOff( fallOff );
	}

	/**
	 * Returns the light intensity of the red color component, for diffuse
	 * reflection.
	 *
	 * @return  Light intensity (red).
	 */
	public float getDiffuseRed()
	{
		return _diffuseColorRed;
	}

	/**
	 * Returns the light intensity of the green color component, for diffuse
	 * reflection.
	 *
	 * @return  Light intensity (green).
	 */
	public float getDiffuseGreen()
	{
		return _diffuseColorGreen;
	}

	/**
	 * Returns the light intensity of the blue color component, for diffuse
	 * reflection.
	 *
	 * @return  Light intensity (blue).
	 */
	public float getDiffuseBlue()
	{
		return _diffuseColorBlue;
	}

	/**
	 * Sets the intensity of the light for diffuse reflection.
	 *
	 * @param   redIntensity    Intensity of the red color component.
	 * @param   greenIntensity  Intensity of the green color component.
	 * @param   blueIntensity   Intensity of the blue color component.
	 */
	public void setDiffuse( final float redIntensity , final float greenIntensity , final float blueIntensity )
	{
		if ( redIntensity < 0.0f )
		{
			throw new IllegalArgumentException( "redIntensity < 0.0: " + redIntensity );
		}

		if ( greenIntensity < 0.0f )
		{
			throw new IllegalArgumentException( "greenIntensity < 0.0: " + redIntensity );
		}

		if ( blueIntensity < 0.0f )
		{
			throw new IllegalArgumentException( "blueIntensity < 0.0: " + redIntensity );
		}

		_diffuseColorRed   = Math.max( 0.0f , redIntensity   );
		_diffuseColorGreen = Math.max( 0.0f , greenIntensity );
		_diffuseColorBlue  = Math.max( 0.0f , blueIntensity  );
	}

	/**
	 * Returns the light intensity of the red color component, for specular
	 * reflection.
	 *
	 * @return  Light intensity (red).
	 */
	public float getSpecularRed()
	{
		return _specularColorRed;
	}

	/**
	 * Returns the light intensity of the green color component, for specular
	 * reflection.
	 *
	 * @return  Light intensity (green).
	 */
	public float getSpecularGreen()
	{
		return _specularColorGreen;
	}

	/**
	 * Returns the light intensity of the blue color component, for specular
	 * reflection.
	 *
	 * @return  Light intensity (blue).
	 */
	public float getSpecularBlue()
	{
		return _specularColorBlue;
	}

	/**
	 * Sets the intensity of the light for specular reflection.
	 *
	 * @param   redIntensity    Intensity of the red color component.
	 * @param   greenIntensity  Intensity of the green color component.
	 * @param   blueIntensity   Intensity of the blue color component.
	 */
	public void setSpecular( final float redIntensity , final float greenIntensity , final float blueIntensity )
	{
		if ( redIntensity < 0.0f )
		{
			throw new IllegalArgumentException( "redIntensity < 0.0: " + redIntensity );
		}

		if ( greenIntensity < 0.0f )
		{
			throw new IllegalArgumentException( "greenIntensity < 0.0: " + redIntensity );
		}

		if ( blueIntensity < 0.0f )
		{
			throw new IllegalArgumentException( "blueIntensity < 0.0: " + redIntensity );
		}

		_specularColorRed   = Math.max( 0.0f , redIntensity   );
		_specularColorGreen = Math.max( 0.0f , greenIntensity );
		_specularColorBlue  = Math.max( 0.0f , blueIntensity  );
	}

	/**
	 * Returns the constant attenuation factor.
	 *
	 * @return  Constant attenuation factor.
	 */
	public float getConstantAttenuation()
	{
		return _constantAttenuation;
	}

	/**
	 * Returns the linear attenuation factor.
	 *
	 * @return  Linear attenuation factor.
	 */
	public float getLinearAttenuation()
	{
		return _linearAttenuation;
	}

	/**
	 * Returns the quadratic attenuation factor.
	 *
	 * @return  Quadratic attenuation factor.
	 */
	public float getQuadraticAttenuation()
	{
		return _quadraticAttenuation;
	}

	/**
	 * Sets the light attenuation factors.
	 *
	 * @param   constantFactor      Constant light attenuation factor.
	 * @param   linearFactor        Linear light attenuation factor.
	 * @param   quadraticFactor     Quadratic light attenuation factor.
	 */
	public void setAttenuation( final float constantFactor , final float linearFactor , final float quadraticFactor )
	{
		_constantAttenuation  = constantFactor;
		_linearAttenuation    = linearFactor;
		_quadraticAttenuation = quadraticFactor;
	}

	/**
	 * Returns the distance where the light reaches half its specified
	 * intensity, i.e. where the light attenuation equals <code>0.5</code>.
	 * For lights with only constant attenuation, this method always returns
	 * <code>0.0</code>.
	 *
	 * @return  Distance at which the light has half its specified intensity.
	 */
	public float getHalfIntensityDistance()
	{
		return getDistanceByIntensity( 0.5f );
	}

	/**
	 * Returns the distance where the light reaches its specified intensity,
	 * i.e. where the light attenuation equals <code>1.0</code>. For lights with
	 * only constant attenuation, this method always returns <code>0.0</code>.
	 *
	 * @return  Distance at which the light has half its specified intensity.
	 */
	public float getFullIntensityDistance()
	{
		return getDistanceByIntensity( 1.0f );
	}

	/**
	 * Returns the distance where the light attenuation equals the specified
	 * intensity, relative to the light's specified intensity. For lights with
	 * only constant attenuation, this method always returns <code>0.0</code>.
	 *
	 * @param   relativeIntensity   Relative light intensity, e.g.
	 *                              <code>1.0</code> for the specified intensity
	 *                              of this light.
	 *
	 * @return  Distance at which the specified light intensity is reached.
	 */
	private float getDistanceByIntensity( final float relativeIntensity )
	{
		final float q = _quadraticAttenuation;
		final float l = _linearAttenuation;
		final float c = _constantAttenuation - ( 1.0f / relativeIntensity );

		/*
		 * Solve 'd' in the equation 'qd^2 + ld + c = 0.0', yielding the
		 * distance where the light reaches the specified relative intensity.
		 */
		final float result;
		if ( q == 0.0f )
		{
			if ( l == 0.0f )
			{
				result = 0.0f;
			}
			else
			{
				result = -c / l;
			}
		}
		else
		{
			// Using the quadratic equation.
			result = ( -l + (float)Math.sqrt( (double)( l * l - 4.0f * q * c ) ) ) / ( 2.0f * q );
		}

		return result;
	}

	/**
	 * Returns the perceived (grayscale) intensity of the light. This value is
	 * based only on the diffuse intensity of the light.
	 *
	 * @return  Light intensity.
	 */
	public float getIntensity()
	{
		return 0.3f * _diffuseColorRed + 0.59f * _diffuseColorGreen + 0.11f * _diffuseColorBlue;
	}

	/**
	 * Sets the intensity of the light, for both diffuse and specular reflection
	 * and for all color components, to the given value.
	 *
	 * @param   intensity   Light intensity.
	 */
	public void setIntensity( final float intensity )
	{
		setDiffuse( intensity , intensity , intensity );
		setSpecular( intensity , intensity , intensity );
	}

	/**
	 * Sets the intensity of the light, for both diffuse and specular
	 * reflection, to the given values.
	 *
	 * @param   redIntensity    Intensity of the red color component.
	 * @param   greenIntensity  Intensity of the green color component.
	 * @param   blueIntensity   Intensity of the blue color component.
	 */
	public void setIntensity( final float redIntensity , final float greenIntensity , final float blueIntensity )
	{
		setDiffuse( redIntensity , greenIntensity , blueIntensity );
		setSpecular( redIntensity , greenIntensity , blueIntensity );
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
	 * <p>
	 * When set to <code>0.0</code>, the light will not be attenuated.
	 *
	 * @param   fallOff     Fall-off distance to be set.
	 */
	public void setFallOff( final double fallOff )
	{
		if ( fallOff < 0.0 )
		{
			throw new IllegalArgumentException( "fallOff < 0.0: " + fallOff );
		}
		else if ( fallOff == 0.0 )
		{
			setAttenuation( 1.0f , 0.0f , 0.0f );
		}
		else
		{
			setAttenuation( 0.0f , 0.0f , 2.0f / (float)( fallOff * fallOff ) );
		}
	}
}
