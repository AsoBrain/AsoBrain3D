/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2019 Peter S. Heijnen
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
package ab.j3d.awt.view.jogl;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.fixedfunc.*;

/**
 * Handles OpenGL state changes and caches OpenGL state to improve performance.
 *
 * @author  G. Meinders
 */
public class CachingGLStateHelper
	extends GLStateHelper
{
	/** Cached value of 'glColor'. */ private float[] _color;

	/** Cached value of 'glMaterial(GL_AMBIENT)'.   */ private float[] _materialAmbient;
	/** Cached value of 'glMaterial(GL_DIFFUSE)'.   */ private float[] _materialDiffuse;
	/** Cached value of 'glMaterial(GL_SPECULAR)'.  */ private float[] _materialSpecular;
	/** Cached value of 'glMaterial(GL_EMISSIVE)'.  */ private float[] _materialEmissive;
	/** Cached value of 'glMaterial(GL_SHININESS)'. */ private float _shininess;

	/** Cached value of 'glBlendFunc'. */ private int _blendFuncSource;
	/** Cached value of 'glBlendFunc'. */ private int _blendFuncDestination;

	/**
	 * Cached values of 'glEnable'/'glDisable'.
	 */
	private Map<Integer, Boolean> _enableStates;

	/**
	 * Number of cached operations.
	 */
	private int _cached;

	/**
	 * Total number of operations.
	 */
	private int _total;

	/**
	 * Constructs a new instance.
	 *
	 * @param   gl  OpenGL interface.
	 */
	public CachingGLStateHelper( final GL gl )
	{
		super( gl );

		_color = new float[] { Float.NaN, Float.NaN, Float.NaN, Float.NaN };

		_materialAmbient = new float[] { Float.NaN, Float.NaN, Float.NaN, Float.NaN };
		_materialDiffuse = new float[] { Float.NaN, Float.NaN, Float.NaN, Float.NaN };
		_materialSpecular = new float[] { Float.NaN, Float.NaN, Float.NaN, Float.NaN };
		_materialEmissive = new float[] { Float.NaN, Float.NaN, Float.NaN, Float.NaN };
		_shininess = Float.NaN;

		_blendFuncSource = GL.GL_ONE;
		_blendFuncDestination = GL.GL_ZERO;

		_enableStates = new HashMap<Integer, Boolean>();

		_cached = 0;
		_total = 0;
	}

	@Override
	public void setColor( final float red, final float green, final float blue, final float alpha, final float ambientFactor, final float diffuseFactor, final float specularReflectivity, final float shininess )
	{
		final GL gl = _gl;
		final GL2 gl2 = gl.getGL2();

		if ( update( _color, red, green, blue, alpha ) )
		{
			gl2.glColor4fv( _color, 0 );
		}

		if ( update( _materialAmbient, ambientFactor * red, ambientFactor * green, ambientFactor * blue, alpha ) )
		{
			gl2.glMaterialfv( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT, _materialAmbient, 0 );
		}

		if ( update( _materialDiffuse, diffuseFactor * red, diffuseFactor * green, diffuseFactor * blue, alpha ) )
		{
			gl2.glMaterialfv( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_DIFFUSE, _materialDiffuse, 0 );
		}

		if ( update( _materialSpecular, specularReflectivity, specularReflectivity, specularReflectivity, alpha ) )
		{
			gl2.glMaterialfv( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, _materialSpecular, 0 );
		}

		if ( update( _materialEmissive, 0.0f, 0.0f, 0.0f, alpha ) )
		{
			gl2.glMaterialfv( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION, _materialEmissive, 0 );
		}

		if ( _shininess != shininess )
		{
			_shininess = shininess;
			gl2.glMaterialf( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, shininess );
		}
	}

	@Override
	protected void setAppearance( final Appearance appearance, final float diffuseRed, final float diffuseGreen, final float diffuseBlue, final float diffuseAlpha )
	{
		final GL gl = _gl;
		final GL2 gl2 = gl.getGL2();

		if ( update( _color, diffuseRed, diffuseGreen, diffuseBlue, diffuseAlpha ) )
		{
			gl2.glColor4fv( _color, 0 );
		}

		final Color4 ambientColor = appearance.getAmbientColor();
		if ( update( _materialAmbient, ambientColor.getRedFloat() , ambientColor.getGreenFloat() , ambientColor.getBlueFloat() , diffuseAlpha ) )
		{
			gl2.glMaterialfv( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT , _materialAmbient, 0 );
		}

		if ( update( _materialDiffuse, diffuseRed, diffuseGreen, diffuseBlue, diffuseAlpha ) )
		{
			gl2.glMaterialfv( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_DIFFUSE , _materialDiffuse, 0 );
		}

		final Color4 specularColor = appearance.getSpecularColor();
		if ( update( _materialSpecular, specularColor.getRedFloat() , specularColor.getGreenFloat() , specularColor.getBlueFloat() , diffuseAlpha ) )
		{
			gl2.glMaterialfv( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, _materialSpecular, 0 );
		}

		final Color4 emissiveColor = appearance.getEmissiveColor();
		if ( update( _materialEmissive, emissiveColor.getRedFloat() , emissiveColor.getGreenFloat() , emissiveColor.getBlueFloat() , diffuseAlpha ) )
		{
			gl2.glMaterialfv( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION, _materialEmissive, 0 );
		}

		if ( _shininess != (float)appearance.getShininess() )
		{
			_shininess = (float)appearance.getShininess();
			gl2.glMaterialf( GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, (float)appearance.getShininess() );
		}
	}

	/**
	 * Updates the given 4-element array with the given values.
	 *
	 * @param   current     Array with 4 elements.
	 * @param   red         First element.
	 * @param   green       Second element.
	 * @param   blue        Third element.
	 * @param   alpha       Fourth element.
	 *
	 * @return  <code>true</code> if the contents of the array has changed.
	 */
	private static boolean update( final float[] current, final float red, final float green, final float blue, final float alpha )
	{
		final boolean result = ( current[ 0 ] != red ) || ( current[ 1 ] != green ) || ( current[ 2 ] != blue ) || ( current[ 3 ] != alpha );
		if ( result )
		{
			current[ 0 ] = red;
			current[ 1 ] = green;
			current[ 2 ] = blue;
			current[ 3 ] = alpha;
		}
//		count( !result );
		return result;
	}

	/*
	public void setEnabled( final int capability, final boolean enabled )
	{
		setEnabledCached( capability, enabled, false );
	}
	*/

	/**
	 * Enables or disables the specified capability.
	 *
	 * <p>
	 * So far there is no performance gain from caching. Also, there are issues
	 * with validity, e.g. for glEnable(GL_TEXTURE_2D), which is dependent on
	 * other OpenGL state (the currently active texture).
	 *
	 * @param   capability  Capability to be enabled or disabled.
	 * @param   enabled     <code>true</code> to enable the capability.
	 * @param   verify      <code>true</code> to verify cached values. (For validity testing only!)
	 */
	private void setEnabledCached( final int capability, final boolean enabled, final boolean verify )
	{
		final Map<Integer, Boolean> enableStates = _enableStates;
		final Boolean current = enableStates.get( Integer.valueOf( capability ) );

		// Verify the cached value. For testing only, as it likely exceeds any performance gain from caching.
		if ( verify && ( current != null ) )
		{
			final boolean actual = _gl.glIsEnabled( capability );
			if ( actual != current )
			{
				throw new IllegalStateException( "Illegal capability state detected: expected " + current + ", but was " + actual );
			}
		}

		if ( ( current == null ) || ( current != enabled ) )
		{
			enableStates.put( Integer.valueOf( capability ), Boolean.valueOf( enabled ) );
			if ( enabled )
			{
				_gl.glEnable( capability );
			}
			else
			{
				_gl.glDisable( capability );
			}

			count( false );
		}
		else
		{
			count ( true );
		}
	}

	@Override
	public void setBlendFunc( final int source, final int destination ) // 5%
	{
		if ( ( _blendFuncSource != source ) || ( _blendFuncDestination != destination ) )
		{
			_blendFuncSource = source;
			_blendFuncDestination = destination;
			_gl.glBlendFunc( source, destination );
//			count( false );
		}
		else
		{
//			count( true );
		}
	}

	/**
	 * Counts how many operations were performed and how many of those were
	 * cached, showing statistics occasionally.
	 *
	 * @param   cached  <code>true</code> to count a cached operation;
	 *                  <code>false</code> to count an uncached operation.
	 *
	 * @noinspection FieldRepeatedlyAccessedInMethod
	 */
	private void count( final boolean cached )
	{
		if ( cached )
		{
			_cached++;
		}
		_total++;

		if ( _total % 100 == 0 )
		{
			System.out.println( "State cache efficiency: " + Math.floor( (double)_cached * 1000.0 / (double)_total ) / 1000.0 + " of " + _total );
		}
	}
}
