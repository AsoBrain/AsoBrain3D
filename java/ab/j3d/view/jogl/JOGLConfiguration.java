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
	 * Whether per-pixel lighting should be enabled. This applies only when
	 * support for OpenGL shaders is available.
	 */
	private boolean _perPixelLightingEnabled = true;

	/**
	 * Whether depth-peeling should be enabled. This applies only when the
	 * required OpenGL functionality is available.
	 */
	private boolean _depthPeelingEnabled = false;

	/**
	 * Maximum number of OpenGL lights to use.
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
	 * Constructs a new JOGL configuration with default settings.
	 */
	public JOGLConfiguration()
	{
	}

	/**
	 * Returns whether per-pixel lighting should be enabled, when support is
	 * available.
	 *
	 * @return  <code>true</code> if per-pixel lighting should be used.
	 */
	public boolean isPerPixelLightingEnabled()
	{
		return _perPixelLightingEnabled;
	}

	/**
	 * Sets whether per-pixel lighting should be enabled, when support is
	 * available.
	 *
	 * @param   perPixelLightingEnabled     <code>true</code> if per-pixel
	 *                                      lighting should be used.
	 */
	public void setPerPixelLightingEnabled( final boolean perPixelLightingEnabled )
	{
		_perPixelLightingEnabled = perPixelLightingEnabled;
	}

	public boolean isDepthPeelingEnabled()
	{
		return _depthPeelingEnabled;
	}

	public void setDepthPeelingEnabled( final boolean depthPeelingEnabled )
	{
		_depthPeelingEnabled = depthPeelingEnabled;
	}

	public int getMaximumNumberOfLights()
	{
		return _maximumNumberOfLights;
	}

	public void setMaximumNumberOfLights( final int maximumNumberOfLights )
	{
		_maximumNumberOfLights = maximumNumberOfLights;
	}

	public boolean isReflectionMapsEnabled()
	{
		return _reflectionMapsEnabled;
	}

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
}
