/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
package ab.j3d.view;

import org.jetbrains.annotations.*;

/**
 * Specifies which rendering features should be enabled, provided that the
 * required OpenGL capabilities are present.
 *
 * @author G. Meinders
 */
public class JOGLConfiguration
{
	/**
	 * Whether per-pixel lighting should be enabled.
	 */
	private boolean _perPixelLightingEnabled = true;

	/**
	 * Maximum number of OpenGL lights to use in a single rendering pass.
	 */
	private int _maximumNumberOfLights = 3;

	/**
	 * Whether reflection maps should be enabled.
	 */
	private boolean _reflectionMapsEnabled = true;

	/**
	 * Whether full scene anti-aliasing should be enabled.
	 */
	private boolean _fsaaEnabled = true;

	/**
	 * Whether shadow mapping should be enabled.
	 */
	private boolean _shadowEnabled = false;

	/**
	 * Whether multisample (soft) shadows should be enabled.
	 */
	private boolean _shadowMultisampleEnabled = false;

	/**
	 * Whether vertex buffer objects should be enabled.
	 */
	private boolean _vertexBufferObjectsEnabled = true;

	/**
	 * Constructs a new JOGL configuration with default settings.
	 */
	public JOGLConfiguration()
	{
		setDefaultOptions();
	}

	/**
	 * Create instance of {@link JOGLConfiguration} with default options.
	 *
	 * @return  {@link JOGLConfiguration} instance.
	 */
	@NotNull
	public static JOGLConfiguration createDefaultInstance()
	{
		final JOGLConfiguration result = new JOGLConfiguration();
		result.setDefaultOptions();
		return result;
	}

	/**
	 * Resets this configuration to default options.
	 */
	public void setDefaultOptions()
	{
		setPerPixelLightingEnabled( true );
		setMaximumNumberOfLights( 3 );
		setReflectionMapsEnabled( true );
		setFSAAEnabled( true );
		setShadowEnabled( false );
		setShadowMultisampleEnabled( false );
		setVertexBufferObjectsEnabled( true );
	}

	/**
	 * Create instance of {@link JOGLConfiguration} with 'safe mode' options,
	 * meaning that all optional functionality is disabled.
	 *
	 * @return  {@link JOGLConfiguration} instance.
	 */
	@NotNull
	public static JOGLConfiguration createSafeInstance()
	{
		final JOGLConfiguration result = new JOGLConfiguration();
		result.setSafeOptions();
		return result;
	}

	/**
	 * Disable all options in this configuration. This enables 'safe mode'.
	 */
	public void setSafeOptions()
	{
		setPerPixelLightingEnabled( false );
		setMaximumNumberOfLights( 3 );
		setReflectionMapsEnabled( false );
		setFSAAEnabled( false );
		setShadowEnabled( false );
		setShadowMultisampleEnabled( false );
		setVertexBufferObjectsEnabled( false );
	}

	/**
	 * Create instance of {@link JOGLConfiguration} with 'luscious mode' options,
	 * meaning that all optional features are enabled.
	 *
	 * @return  {@link JOGLConfiguration} instance.
	 */
	@NotNull
	public static JOGLConfiguration createLusciousInstance()
	{
		final JOGLConfiguration result = new JOGLConfiguration();
		result.setLusciousOptions();
		return result;
	}

	/**
	 * Enable all options in this configuration. This enabled 'luscious mode'.
	 */
	public void setLusciousOptions()
	{
		setPerPixelLightingEnabled( true );
		setMaximumNumberOfLights( 3 );
		setReflectionMapsEnabled( true );
		setFSAAEnabled( true );
		setShadowEnabled( true );
		setShadowMultisampleEnabled( true );
		setVertexBufferObjectsEnabled( true );
	}

	/**
	 * Returns whether per-pixel lighting should be enabled.
	 *
	 * @return  <code>true</code> if per-pixel lighting should be used.
	 */
	public boolean isPerPixelLightingEnabled()
	{
		return _perPixelLightingEnabled;
	}

	/**
	 * Sets whether per-pixel lighting should be enabled.
	 *
	 * @param   perPixelLightingEnabled     <code>true</code> if per-pixel
	 *                                      lighting should be used.
	 */
	public void setPerPixelLightingEnabled( final boolean perPixelLightingEnabled )
	{
		_perPixelLightingEnabled = perPixelLightingEnabled;
	}

	/**
	 * Returns the maximum number of OpenGL lights to use in a single
	 * rendering pass.
	 *
	 * @return  Maximum number of lights per rendering pass.
	 */
	public int getMaximumNumberOfLights()
	{
		return _maximumNumberOfLights;
	}

	/**
	 * Sets the maximum number of OpenGL lights to use in a single
	 * rendering pass.
	 *
	 * @param   lights  Maximum number of lights per rendering pass.
	 */
	public void setMaximumNumberOfLights( final int lights )
	{
		_maximumNumberOfLights = lights;
	}

	/**
	 * Returns whether reflection maps should be enabled.
	 *
	 * @return  <code>true</code> if reflection maps should be enabled.
	 */
	public boolean isReflectionMapsEnabled()
	{
		return _reflectionMapsEnabled;
	}

	/**
	 * Sets whether reflection maps should be enabled.
	 *
	 * @param   reflectionMapsEnabled   <code>true</code> if reflection maps
	 *                                  should be enabled.
	 */
	public void setReflectionMapsEnabled( final boolean reflectionMapsEnabled )
	{
		_reflectionMapsEnabled = reflectionMapsEnabled;
	}

	/**
	 * Returns whether full scene anti-aliasing (FSAA) should be used.
	 *
	 * @return  <code>true</code> if full scene anti-aliasing should be used.
	 */
	public boolean isFSAAEnabled()
	{
		return _fsaaEnabled;
	}

	/**
	 * Sets whether full scene anti-aliasing (FSAA) should be used.
	 *
	 * @param   fsaaEnabled     <code>true</code> to use full scene
	 *                          anti-aliasing.
	 */
	public void setFSAAEnabled( final boolean fsaaEnabled )
	{
		_fsaaEnabled = fsaaEnabled;
	}

	/**
	 * Returns whether shadows should be enabled.
	 *
	 * @return  <code>true</code> if shadows should be enabled.
	 */
	public boolean isShadowEnabled()
	{
		return _shadowEnabled;
	}

	/**
	 * Sets whether shadows should be enabled.
	 *
	 * @param   shadowEnabled   <code>true</code> if shadows should be enabled.
	 */
	public void setShadowEnabled( final boolean shadowEnabled )
	{
		_shadowEnabled = shadowEnabled;
	}

	/**
	 * Returns whether multisample (soft) shadows should be enabled.
	 *
	 * @return  <code>true</code> if multisample (soft) shadows should be enabled.
	 */
	public boolean isShadowMultisampleEnabled()
	{
		return _shadowMultisampleEnabled;
	}

	/**
	 * Sets whether multisample (soft) shadows should be enabled.
	 *
	 * @param   shadowMultisampleEnabled    <code>true</code> if multisample
	 *                                      (soft) shadows should be enabled.
	 */
	public void setShadowMultisampleEnabled( final boolean shadowMultisampleEnabled )
	{
		_shadowMultisampleEnabled = shadowMultisampleEnabled;
	}

	/**
	 * Returns whether vertex buffer objects should be enabled.
	 *
	 * @return  <code>true</code> if vertex buffer objects should be enabled.
	 */
	public boolean isVertexBufferObjectsEnabled()
	{
		return _vertexBufferObjectsEnabled;
	}

	/**
	 * Sets whether vertex buffer objects should be enabled.
	 *
	 * @param   vertexBufferObjects     <code>true</code> if vertex buffer
	 *                                  objects should be enabled.
	 */
	public void setVertexBufferObjectsEnabled( final boolean vertexBufferObjects )
	{
		_vertexBufferObjectsEnabled = vertexBufferObjects;
	}
}
