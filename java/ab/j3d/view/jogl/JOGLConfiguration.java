/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2009-2009 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
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
	private boolean _perPixelLightingEnabled = false;

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
}
