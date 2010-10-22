/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2010
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

/**
 * Specifies which rendering features should be enabled, provided that the
 * required OpenGL capabilities are present.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class JOGLConfiguration
{
	/**
	 * Whether per-pixel lighting should be enabled.
	 */
	private boolean _perPixelLightingEnabled = true;

	/**
	 * Whether depth-peeling should be enabled.
	 */
	private boolean _depthPeelingEnabled = false;

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
	 * Constructs a new JOGL configuration with default settings.
	 */
	public JOGLConfiguration()
	{
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
	 * Returns whether depth-peeling should be enabled.
	 *
	 * @return  <code>true</code> if depth-peeling should be enabled.
	 */
	public boolean isDepthPeelingEnabled()
	{
		return _depthPeelingEnabled;
	}

	/**
	 * Sets whether depth-peeling should be enabled.
	 *
	 * @param   depthPeelingEnabled     <code>true</code> if depth-peeling
	 *                                  should be enabled.
	 */
	public void setDepthPeelingEnabled( final boolean depthPeelingEnabled )
	{
		_depthPeelingEnabled = depthPeelingEnabled;
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
}
