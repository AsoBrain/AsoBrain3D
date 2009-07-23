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
	private boolean _perPixelLightingEnabled = true;

	private boolean _depthPeelingEnabled = false;

	private int _maximumNumberOfLights = 3;

	public JOGLConfiguration()
	{
	}

	public boolean isPerPixelLightingEnabled()
	{
		return _perPixelLightingEnabled;
	}

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
