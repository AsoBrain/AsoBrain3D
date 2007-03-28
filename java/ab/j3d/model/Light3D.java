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
package ab.j3d.model;

import ab.j3d.TextureSpec;

/**
 * This class is a light node in the graphics tree. It contains a
 * LightModel that defines the light associated with this node.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Light3D
	extends Node3D
{
	/**
	 * Intensity of light (0-255).
	 */
	private final int _intensity;

	/**
	 * Fall-off of light. This is factor f in the formula:
	 * <pre>
	 *               f
	 *    Il = ------------
	 *         f + distance
	 * </pre>
	 * This is used to calculate the light intensity at a specific distance.
	 * Obviously, this value must be greater than 0. Greater values result
	 * in less light intensity fall-off.
	 *
	 * Setting this to a negative value, will create an ambient light source.
	 */
	private final double _fallOff;

	/**
	 * Constructor.
	 *
	 * @param   intensity   Intensity of white light (0-255).
	 * @param   fallOff     Light fall-off characteristic (negavtive => ambient).
	 */
	public Light3D( final int intensity , final double fallOff )
	{
		_intensity = intensity;
		_fallOff   = fallOff;
	}

	/**
	 * Returns the intensity of the light. An intensity of zero indicates that
	 * no light is emitted (i.e. the light is off), while an intensity of 255
	 * indicates a 'typical' white light. Higher values may be specified for
	 * lights that are even brighter.
	 *
	 * @return  Intensity value.
	 */
	public int getIntensity()
	{
		return _intensity;
	}

	/**
	 * Returns the fall-off distance of the light, which is the distance at
	 * which the light reaches half its specified intensity.
	 *
	 * @return  Fall-off distance.
	 */
	public double getFallOff()
	{
		return _fallOff;
	}

	/**
	 * Calculate vertex shading properties based on this light source. Several
	 * metrics about the vertex must be provided. Note that lightNormals will
	 * only be provided if the requiresNormalsOrDistance() method of this light
	 * returns <code>true</code>.
	 *
	 * <code>viewNormals</code> is an array of tripplets for the x,y,z values
	 * respectively.
	 *
	 * <code>lightNormalAndDist</code> contains light normals and distances
	 * stored as quads; x,y,z of the light direction vector (normal), and
	 * distance as last value.
	 *
	 * All indices must be absolute (so you may need to multiply them by three
	 * or four before passing them as arguments).
	 * <p>
	 * Diffuse reflection is based on a 0-256 scale and all affecting light
	 * sources should add to this value.
	 * <p>
	 * Because of current limitations, only one specular light source is supported
	 * (since there is only one set of specular properties). Therefore, only one
	 * light source can fill this in (this is currently priority based, with the
	 * brightest light source overruling fainter light sources).
	 *
	 * @param   texture             Texture of surface.
	 * @param   lightNormalAndDist  Float array with light normals and distance.
	 * @param   lightIndex          Index in light array.
	 * @param   nx                  X-coordinate of normal
	 * @param   ny                  Y-coordinate of normal
	 * @param   nz                  Z-coordinate of normal
	 * @param   ds                  Diffuse reflection result array.
	 * @param   sxs                 Specular reflection X-component result array.
	 * @param   sys                 Specular reflection Y-component result array.
	 * @param   sfs                 Specular reflection fraction result array.
	 * @param   targetIndex         Index in result arrays.
	 */
	public void calculateShadingProperties(
		final TextureSpec texture ,
		final double[] lightNormalAndDist , final int lightIndex ,
		final double nx , final double ny , final double nz ,
		final int[] ds , final int[] sxs , final int[] sys , final int[] sfs, final int targetIndex )
	{
		/*
		 * Handle ambient light.
		 */
		if ( _fallOff < 0.0 )
		{
			/*
			 * Calculate diffuse reflection of the ambient light
			 * using the following formula:
			 *
			 *     Id = Il * Ka
			 *
			 * Where:
			 *     Il       = light intensity
			 *     Ka       = ambient reflectivity of texture
			 *
			 * We just add this to any existing diffuse reflection value.
			 */
			final int Id = (int)( _intensity * texture.ambientReflectivity * 256.0 );
			ds[ targetIndex ] += Id;
		}
		/*
		 * Handle point light
		 */
		else
		{
			/*
			 * Get direction of light.
			 */
			final double lx = lightNormalAndDist[ lightIndex + 0 ];
			final double ly = lightNormalAndDist[ lightIndex + 1 ];
			final double lz = lightNormalAndDist[ lightIndex + 2 ];
			final double ld = lightNormalAndDist[ lightIndex + 3 ];

			/*
			 * Get cos( angle ) between light and normal (this is
			 * simply the inner product of the light direction and
			 * normal vectors.
			 */
			final double lightAngle = nx * lx + ny * ly + nz * lz;

			/*
			 * Abort if light shines from back side.
			 */
			if ( lightAngle <= 0 ) return;

			/*
			 * Calculate light intensity of light at the given distance.
			 */
			final double Il = _fallOff / ( _fallOff + ld );

			/*
			 * Start with the diffuse reflection part of the point light.
			 * As basis, we use the following formula:
			 *
			 *     Id = Il * Kd * cos( gamma )
			 *
			 * Where:
			 *     Il       = light intensity
			 *     Kd       = diffusion reflection coefficient of texture,
			 *     gamma    = angle between normal and light
			 *
			 * We just add this to any existing diffuse reflection value.
			 */
			final int Id = (int)( _intensity * Il * texture.diffuseReflectivity * lightAngle * 1024.0 );
			ds[ targetIndex ] += Id;

			/*
			 * To add specular reflection, we have the following formula:
			 *
			 *     Is = Il * Ks * cos^n( theta )
			 *
			 * Where:
			 *     Il       = light intensity
			 *     Ks       = specular reflection coefficient of texture,
			 *     n        = specular reflection exponent
			 *     theta    = angle between light and view
			 *
			 * However, the cos^n factor is not calculated here. Instead, the
			 * the x and y coordinates of the light direction vector are stored
			 * in the shading properties array. These are used by the rendering
			 * engine to interpolate the light normal accross a face. The result
			 * of the first part of the formula is also stored.
			 *
			 * Since only one specular light source is supported at this moment,
			 * the properties will only be stored if this light is brighter than
			 * any previous specular light source.
			 */
			final int Is = (int)(_intensity * Il * texture.specularReflectivity * 2048.0 );
			if ( Is > sfs[ targetIndex ] )
			{
				sxs[ targetIndex ] = (int)(32767.5 * lx + 32768.0 );
				sys[ targetIndex ] = (int)(32767.5 * ly + 32768.0 );
				sfs[ targetIndex ] = ( Is > 131072 ) ? 131072 : Is;
			}
		}
	}

	/**
	 * This function should return <code>true</code> if this light model requires
	 * the normal of a surface to calculate its color. This is the case for most
	 * light sources, but an "ambient" light is one exception to this rule (there
	 * may be more). If this function returns <code>false</code> time can be saved
	 * by not calculating the normals.
	 *
	 * @return  <code>true</code> if surface normals are required by
	 *          calculateColor(), <code>false</code> otherwise.
	 */
	public boolean requiresNormalsOrDistance()
	{
		return _fallOff >= 0.0;
	}
}
