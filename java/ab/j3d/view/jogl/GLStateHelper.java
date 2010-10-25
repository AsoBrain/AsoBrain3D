/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
package ab.j3d.view.jogl;

import java.awt.*;
import java.util.*;
import javax.media.opengl.*;

import ab.j3d.*;
import ab.j3d.view.*;

/**
 * Handles OpenGL state changes that are complicated or may need to be cached.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class GLStateHelper
{
	/**
	 * OpenGL interface.
	 */
	private final GL _gl;

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
	public GLStateHelper( final GL gl )
	{
		_gl = gl;

		_color = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

		_materialAmbient = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
		_materialDiffuse = new float[] { 0.8f, 0.8f, 0.8f, 1.0f };
		_materialSpecular = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
		_materialEmissive = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
		_shininess = 0.0f;

		_blendFuncSource = GL.GL_ONE;
		_blendFuncDestination = GL.GL_ZERO;

		_enableStates = new HashMap<Integer, Boolean>();

		_cached = 0;
		_total = 0;
	}

	/**
	 * Set GL material properties.
	 *
	 * @param   color   Color to set.
	 */
	public void setColor( final Color color )
	{
		final float[] rgba  = color.getRGBComponents( null );
		final float   red   = rgba[ 0 ];
		final float   green = rgba[ 1 ];
		final float   blue  = rgba[ 2 ];
		final float   alpha = rgba[ 3 ];

		setColor( red , green , blue , alpha );
	}

	/**
	 * Sets the current color and material to the given color.
	 *
	 * @param   red     Red component.
	 * @param   green   Green component.
	 * @param   blue    Blue component.
	 * @param   alpha   Alpha component.
	 */
	public void setColor( final float red , final float green , final float blue , final float alpha )
	{
		final GL gl = _gl;

		if ( update( _color, red, green, blue, alpha ) )
		{
			gl.glColor4fv( _color, 0 );
		}

		if ( update( _materialAmbient, red, green, blue, alpha ) )
		{
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, _materialAmbient, 0 );
		}

		if ( update( _materialDiffuse, red, green, blue, alpha ) )
		{
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, _materialDiffuse, 0 );
		}

		if ( update( _materialSpecular, 0.0f, 0.0f, 0.0f, alpha ) )
		{
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, _materialSpecular, 0 );
		}

		if ( update( _materialEmissive, 0.0f, 0.0f, 0.0f, alpha ) )
		{
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, _materialEmissive, 0 );
		}
	}

	/**
	 * Set GL material properties.
	 *
	 * @param   material    {@link Material} properties.
	 * @param   style       Render style to be applied.
	 * @param   extraAlpha  Extra alpha multiplier.
	 */
	public void setMaterial( final Material material, final RenderStyle style, final float extraAlpha )
	{
		final float red;
		final float green;
		final float blue;
		final float alpha;

		if ( style.isFillEnabled() )
		{
			final Color materialDiffuse = new Color( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , extraAlpha * material.diffuseColorAlpha );
			final Color diffuse = RenderStyle.blendColors( style.getFillColor() , materialDiffuse );
			final float[] diffuseComponents = diffuse.getRGBComponents( null );

			red   = diffuseComponents[ 0 ];
			green = diffuseComponents[ 1 ];
			blue  = diffuseComponents[ 2 ];
			alpha = diffuseComponents[ 3 ];
		}
		else
		{
			red   = material.diffuseColorRed;
			green = material.diffuseColorGreen;
			blue  = material.diffuseColorBlue;
			alpha = material.diffuseColorAlpha * extraAlpha;
		}

		final GL gl = _gl;

		if ( update( _color, red, green, blue, alpha ) )
		{
			gl.glColor4fv( _color, 0 );
		}

		if ( update( _materialAmbient, material.ambientColorRed , material.ambientColorGreen , material.ambientColorBlue , alpha ) )
		{
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT , _materialAmbient, 0 );
		}

		if ( update( _materialDiffuse, red , green , blue , alpha ) )
		{
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE , _materialDiffuse, 0 );
		}

		if ( update( _materialSpecular, material.specularColorRed , material.specularColorGreen , material.specularColorBlue , alpha ) )
		{
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, _materialSpecular, 0 );
		}

		if ( update( _materialEmissive, material.emissiveColorRed , material.emissiveColorGreen , material.emissiveColorBlue , alpha ) )
		{
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, _materialEmissive, 0 );
		}

		if ( _shininess != (float)material.shininess )
		{
			_shininess = (float)material.shininess;
			gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, (float)material.shininess );
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
		final boolean result = ( current[ 0 ] != red ) || ( current[ 0 ] != green ) || ( current[ 0 ] != blue ) || ( current[ 0 ] != alpha );
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

	/**
	 * Enables or disables the specified capability.
	 *
	 * @param   capability  Capability to be enabled or disabled.
	 * @param   enabled     <code>true</code> to enable the capability.
	 */
	public void setEnabled( final int capability, final boolean enabled )
	{
		if ( enabled )
		{
			_gl.glEnable( capability );
		}
		else
		{
			_gl.glDisable( capability );
		}

		//setEnabledCached( capability, enabled, false );
	}

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

	/**
	 * Sets the blend function for pixel arithmetic.
	 *
	 * @param   source          Source factor.
	 * @param   destination     Destination factory.
	 */
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
